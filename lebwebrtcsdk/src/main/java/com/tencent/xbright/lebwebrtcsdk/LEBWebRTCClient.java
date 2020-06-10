package com.tencent.xbright.lebwebrtcsdk;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.EglBase;
import org.webrtc.Logging;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.RTCStatsCollectorCallback;
import org.webrtc.RTCStatsReport;
import org.webrtc.SurfaceViewRenderer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * LEB WebRTC client
 *  weifei@tencent.com
 *  2020.4.1
 */
public class LEBWebRTCClient implements PeerConnectionClient.PeerConnectionEvent {
    private static final String TAG = "LEBWebRTCClient";
    private PeerConnectionClient mPCClient;
    private ExecutorService mExecutorService;
    private ScheduledExecutorService mPeriodExecutorService;
    private ScheduledFuture<?> statsSchedulder;
    private LEBWebRTCEvents mWebrtcEventObserver;
    private LEBWebRTCStatsCollector mStatsCollector;
    private SurfaceViewRenderer mSurfaceView;
    private LEBWebRTCParameters mLEBWebRTCParameters;
    private EglBase mEglBase;
    private boolean mPCConnected = false;
    private long mStartTimestampInMs = 0;

    public LEBWebRTCEvents.ConnectionState mConnectState = LEBWebRTCEvents.ConnectionState.STATE_BEGIN;


    public LEBWebRTCClient(LEBWebRTCParameters rtcParams, SurfaceViewRenderer surfaceView, EglBase eglBase, LEBWebRTCEvents events) {
        Logging.d(TAG, "init LEBWebRTCClient");
        mSurfaceView = surfaceView;
        mWebrtcEventObserver = events;
        mEglBase = eglBase;
        mLEBWebRTCParameters = rtcParams;
    }

    private void initInternalWebRTC(){
        Logging.d(TAG, "initInternalWebRTC");

        if (mPCClient == null) {
            mPCClient = new PeerConnectionClient(mSurfaceView.getContext().getApplicationContext(), mEglBase, this, mLEBWebRTCParameters);
            mPCClient.setVideoRender(mSurfaceView);
        }
        if (mExecutorService == null) {
            mExecutorService = Executors.newFixedThreadPool(1);
        }
        if (mPeriodExecutorService == null) {
            mPeriodExecutorService = Executors.newScheduledThreadPool(1);
        }
        if (mStatsCollector == null) {
            mStatsCollector = new LEBWebRTCStatsCollector();
        }

        //开始创建peerconnection
        if (mPCConnected == false) {
            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        mPCClient.createPeerConnection();
                        mPCClient.createOffer();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }

    /**
     * 启动WebRTC
     */
    public void start() {
        Logging.d(TAG, "Start WebRTC");
        mStartTimestampInMs = System.currentTimeMillis();
        initInternalWebRTC();
        mPeriodExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                if (!mPCConnected) {
                    mConnectState = LEBWebRTCEvents.ConnectionState.STATE_WEBRTC_TIMEOUT;
                    mWebrtcEventObserver.onEventConnectFailed(mConnectState);
                }
            }
        }, mLEBWebRTCParameters.getConnectionTimeOutInMs(), TimeUnit.MILLISECONDS);
    }

    /**
     * 暂停webrtc播放
     */
    public void pause() {
        if (mPCClient != null) {
            mSurfaceView.pauseVideo();//setFpsReduction(0)
            mPCClient.setMute(true);
        }
    }

    /**
     * 继续webrtc播放
     */
    public void resume() {
        if (mPCClient != null) {
            mSurfaceView.setFpsReduction(Float.POSITIVE_INFINITY);
            mPCClient.setMute(false);
        }
    }

    /**
     * 停止WebRTC
     */
    public void stop() {
        Logging.d(TAG, "Stop WebRTC");

        mPCConnected = false;
        if (mPCClient != null) {
            stopGetStat();
            mPCClient.close();

            mPCClient.setVideoRender(null);
            mPCClient = null;
        }

        if (mExecutorService != null) {
            mExecutorService.shutdownNow();
            mExecutorService = null;
        }

        if (mPeriodExecutorService != null) {
            mPeriodExecutorService.shutdownNow();
            mPeriodExecutorService = null;
        }
        mStatsCollector=null;
    }

    /**
     * 静音播放
     * @return
     */
    public void mute(boolean isMute) {
        mPCClient.setMute(isMute);
    }

    public LEBWebRTCStatsReport getStatsReport() {
        return (mStatsCollector != null)? mStatsCollector.mLEBWebRTCStatsReport : null;
    }

    private void startGetStat(final int periodInMs) {
        statsSchedulder = mPeriodExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                mPCClient.getStats(new RTCStatsCollectorCallback() {
                    @Override
                    public void onStatsDelivered(RTCStatsReport rtcStatsReport) {

                        mStatsCollector.onStatsDelivered(rtcStatsReport);
                        mWebrtcEventObserver.onEventStatsReport(mStatsCollector.mLEBWebRTCStatsReport);

                    }
                });
            }
        }, 1000, periodInMs, TimeUnit.MILLISECONDS);
    }

    private void stopGetStat() {
        try {
            statsSchedulder.cancel(true);
        } catch (Exception e) {
            Log.d(TAG, "Failed to stop getting statistics");
        }
    }

    @Override
    public void onCreateOfferSuccess(final String sdp) {
        mConnectState = LEBWebRTCEvents.ConnectionState.STATE_OFFER_CREATED;
        mWebrtcEventObserver.onEventOfferCreated(sdp);
    }

    @Override
    public void onIceCandidate(String candidate, String sdpMid, int sdpMLineIndex) {
        Log.d(TAG, "onIceCandidate");
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        if (iceConnectionState == PeerConnection.IceConnectionState.COMPLETED) {
            mConnectState = LEBWebRTCEvents.ConnectionState.STATE_ICE_COMPLETED;
        }
    }

    @Override
    public void onConnectionChange(PeerConnection.PeerConnectionState newState) {
        if (newState == PeerConnection.PeerConnectionState.CONNECTED) {
            mWebrtcEventObserver.onEventConnected();
            mPCConnected = true;
            startGetStat(mLEBWebRTCParameters.getStatsReportPeriodInMs());
            mConnectState = LEBWebRTCEvents.ConnectionState.STATE_WEBRTC_CONNECTED;
        } else if (newState == PeerConnection.PeerConnectionState.DISCONNECTED) {
            mWebrtcEventObserver.onEventDisconnect();
        } else if (newState == PeerConnection.PeerConnectionState.FAILED) {
            mWebrtcEventObserver.onEventConnectFailed(mConnectState);
        }
    }

    @Override
    public void onFirstPacketReceived(MediaStreamTrack.MediaType media_type) {
        if (MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO == media_type) {
            mWebrtcEventObserver.onEventFirstPacketReceived(0);
            getStatsReport().mFirstAudioPacketDelay = System.currentTimeMillis() - mStartTimestampInMs;
        } else if (MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO == media_type){
            mWebrtcEventObserver.onEventFirstPacketReceived(1);
            getStatsReport().mFirstVideoPacketDelay = System.currentTimeMillis() - mStartTimestampInMs;
        }
    }

    public void setRemoteSDP(final String sdp) {
        if (mPCClient != null) {
            mPCClient.setRemoteSDP(sdp);
        }
    }
}
