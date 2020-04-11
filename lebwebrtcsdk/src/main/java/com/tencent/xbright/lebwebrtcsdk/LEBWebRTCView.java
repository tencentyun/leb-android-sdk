package com.tencent.xbright.lebwebrtcsdk;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;

import org.webrtc.EglBase;
import org.webrtc.Logging;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * LEB WebRTC View
 *
 * weifei@tencent.com
 * 2020.4.8
 */

public class LEBWebRTCView extends SurfaceViewRenderer {
    private static final String TAG = "LEBWebRTCView";

    private LEBWebRTCClient mLEBWebRTCClient;
    private EglBase mEglBase;
    private LEBWebRTCEvents mLEBWebrtcEventObserver;
    private LEBWebRTCParameters mLEBWebRTCParameters;
    private long mStartTimestampInMs = 0;
    private long mFirstFrameRenderDelay = 0;
    private boolean mFirstFrameRendered = false;
    private AtomicBoolean mStarted = new AtomicBoolean(false);
    private ScheduledExecutorService mPeriodExecutorService;

    private int videoWidth = 0;
    private int videoHeight = 0;

    /**
     * 初始化
     * @param rtcParams
     * @param rtcEvents
     */

    public void initilize(@NonNull LEBWebRTCParameters rtcParams, @NonNull LEBWebRTCEvents rtcEvents) {
        mEglBase = EglBase.create();
        mLEBWebrtcEventObserver = rtcEvents;
        mLEBWebRTCParameters = rtcParams;
        initVideoRenderView();
    }

    /**
     * 设置remote sdp
     * @param sdp
     */
    public void setRemoteSDP(final String sdp) {
        if (mStarted.get() == true) {
            Log.d(TAG, "setRemoteSdp: " + sdp);
            mLEBWebRTCClient.setRemoteSDP(sdp);
        }
    }

    /**
     * 启动WebRTC SDK，offer创建成功后通过回调向信令服务器请求拉流
     * 启动的结果通过WebRTCEvents回调来通知
     */
    public void startPlay() {
        if (mStarted.compareAndSet(false, true)) {
            Log.d(TAG, "start LEBWebRTCView");
            if (mLEBWebRTCClient == null) {
                mLEBWebRTCClient = new LEBWebRTCClient(mLEBWebRTCParameters, this, mEglBase, mLEBWebrtcEventObserver);
            }
            if (mPeriodExecutorService == null) {
                mPeriodExecutorService = Executors.newScheduledThreadPool(1);
            }

            mStartTimestampInMs = System.currentTimeMillis();
            mLEBWebRTCClient.start();
            mPeriodExecutorService.schedule(new Runnable() {
                @Override
                public void run() {
                    if ((LEBWebRTCClient.mConnectState == LEBWebRTCEvents.ConnectionState.STATE_WEBRTC_CONNECTED) &&
                            !mFirstFrameRendered) {
                        LEBWebRTCClient.mConnectState = LEBWebRTCEvents.ConnectionState.STATE_WEBRTC_CONNECTED;
                        mLEBWebrtcEventObserver.onEventConnectFailed(LEBWebRTCClient.mConnectState);
                    }
                }
            }, mLEBWebRTCParameters.getConnectionTimeOutInMs() + 5000, TimeUnit.MILLISECONDS);
        } else if (mStarted.get() == true) {
            Log.d(TAG, "resume LEBWebRTCView");
            mLEBWebRTCClient.resume();
        }

    }

    public void pausePlay() {
        if (mStarted.get() == true) {
            Log.d(TAG, "pause LEBWebRTCView");
            mLEBWebRTCClient.pause();
        }
    }


    /**
     * 停止播放断开连接
     */
    public void stopPlay() {
        if (mStarted.compareAndSet(true, false)) {
            Log.d(TAG, "stop LEBWebRTCView");
            if (mLEBWebRTCClient != null) {
                mLEBWebRTCClient.stop();
                mLEBWebRTCClient = null;
            }
        }

    }

    /**
     * 释放资源
     */
    public void release() {
        Log.d(TAG, "releae LEBWebRTCView");
        if (mStarted.get() == false) {
            if (mPeriodExecutorService != null) {
                mPeriodExecutorService.shutdownNow();
                mPeriodExecutorService = null;
            }
            super.release();
        }
    }


    private void initVideoRenderView() {
        Log.v(TAG, "initVideoRenderView");
        super.init(mEglBase.getEglBaseContext(), new RendererCommon.RendererEvents() {
            @Override
            public void onFirstFrameRendered() {
                Log.d(TAG, "onFirstFrameRendered");
                LEBWebRTCClient.mConnectState = LEBWebRTCEvents.ConnectionState.STATE_FIRST_FRAME_RENDERED;
                mFirstFrameRendered = true;
                mLEBWebrtcEventObserver.onEventFirstFrameRendered();
                mFirstFrameRenderDelay = System.currentTimeMillis() - mStartTimestampInMs;
                if (mLEBWebRTCClient != null) {
                    mLEBWebRTCClient.getStatsReport().mFirstFrameRenderDelay = mFirstFrameRenderDelay;
                }
            }

            @Override
            public void onFrameResolutionChanged(int w, int h, int r) {
                Log.d(TAG, "onFrameResolutionChanged " + w + "x" + h);
                videoWidth = w;
                videoHeight = h;
                mLEBWebrtcEventObserver.onEventResolutionChanged(w, h);
            }
        });
        super.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        super.setEnableHardwareScaler(true);
        super.setZOrderMediaOverlay(true);
    }

    public LEBWebRTCView(Context ctx) {
        super(ctx);
    }

    public LEBWebRTCView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
    }

}
