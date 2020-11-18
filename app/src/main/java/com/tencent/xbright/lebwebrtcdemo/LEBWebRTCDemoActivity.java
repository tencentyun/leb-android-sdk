package com.tencent.xbright.lebwebrtcdemo;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.tencent.xbright.lebwebrtcdemo.R;
import com.tencent.xbright.lebwebrtcsdk.LEBWebRTCStatsReport;
import com.tencent.xbright.lebwebrtcsdk.LEBWebRTCEvents;
import com.tencent.xbright.lebwebrtcsdk.LEBWebRTCView;
import com.tencent.xbright.lebwebrtcsdk.LEBWebRTCParameters;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;

/**
 * LEB WebRTC Demo Activity
 * weifei@tencent.com
 * 2020.4.8
 *
 * 注意：本demo只演示了快直播的拉流和停流的流程，没有实现业务和app本身的逻辑，比如：
 *      1. Surface相关，前后台切换、全屏缩放、屏幕旋转等逻辑。
 *      2. Audio相关，音频设备检测、请求和释放音频焦点等。
 *      3. 播放相关，完善pause、resume、stop等相关逻辑。
 */

public class LEBWebRTCDemoActivity extends AppCompatActivity implements LEBWebRTCEvents {
    private static final String TAG = "WebRTCDemoActivity";
    private String              mWebRTCUrl;
    private LEBWebRTCParameters mLEBWebRTCParameters;
    private LEBWebRTCView       mWebRTCView;
    private TextView         mStatsView;
    private FrameLayout      mVideoViewLayout;
    private boolean          mShowStats = true;
    private String           mRequestPullUrl = "https://webrtc.liveplay.myqcloud.com/webrtc/v1/pullstream";//请求拉流
    private String           mRequestStopUrl = "https://webrtc.liveplay.myqcloud.com/webrtc/v1/stopstream";//停止拉流
    private String           mSvrSig;//服务器签名，后面每个请求必须携带这个字段内容, 业务无需理解字段内容

    private int              mSignalVersion = 0;
    private boolean          mDisableEncryption = false;
    private int              mAudioFormat = LEBWebRTCParameters.OPUS; //LEBWebRTCParameters.AAC_LATM, LEBWebRTCParameters.AAC_ADTS
    private boolean          mEnableHwDecode = true;
    private boolean          mEnableSEICallback = false;

    public static void start(Context context, int signalversion, String streamurl, String pullurl, boolean enableHwDecode,
                             int audioFormat, boolean disableEncryption, boolean enableSEICallback) {
        Intent intent = new Intent(context, LEBWebRTCDemoActivity.class);
        intent.putExtra("signalversion", signalversion);
        intent.putExtra("streamurl", streamurl);
        intent.putExtra("pullurl", pullurl);
        intent.putExtra("enableHwDecode", enableHwDecode);
        intent.putExtra("audioFormat", audioFormat);
        intent.putExtra("disableEncryption", disableEncryption);
        intent.putExtra("enableSEICallback", enableSEICallback);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        makeFullScreen();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webrtc_demo);

        mVideoViewLayout = findViewById(R.id.id_video_layout);
        mStatsView    = findViewById(R.id.id_stats);

        mSignalVersion = getIntent().getIntExtra("signalversion", 0);
        mWebRTCUrl = getIntent().getStringExtra("streamurl");
        mRequestPullUrl = getIntent().getStringExtra("pullurl");
        mAudioFormat = getIntent().getIntExtra("audioFormat", 0);
        mDisableEncryption = getIntent().getBooleanExtra("disableEncryption", false);
        mEnableHwDecode = getIntent().getBooleanExtra("enableHwDecode", true);
        mEnableSEICallback = getIntent().getBooleanExtra("enableSEICallback", false);

        Log.d(TAG, "set signal version: " + mSignalVersion + " stream url: " + mWebRTCUrl +
                " pull url: " + mRequestPullUrl + " hwDecode: " + mEnableHwDecode +
                " audioFormat: " + mAudioFormat + " disableEncryp: " + mDisableEncryption +
                " enableSEICallback: " + mEnableSEICallback);
        mLEBWebRTCParameters = new LEBWebRTCParameters();
        mLEBWebRTCParameters.setStreamUrl(mWebRTCUrl);
        mLEBWebRTCParameters.setLoggingSeverity(LEBWebRTCParameters.LOG_VERBOSE);
        mLEBWebRTCParameters.enableHwDecode(mEnableHwDecode);
        mLEBWebRTCParameters.disableEncryption(mDisableEncryption);
        mLEBWebRTCParameters.enableSEICallback(mEnableSEICallback);
        mLEBWebRTCParameters.setConnectionTimeOutInMs(5000);//5s
        mLEBWebRTCParameters.setStatsReportPeriodInMs(1000);
        mLEBWebRTCParameters.setAudioFormat(mAudioFormat);

        mWebRTCView = findViewById(R.id.id_surface_view);
        mWebRTCView.initilize(mLEBWebRTCParameters, this);
    }

    @Override
    protected void onResume() {
        Log.v(TAG, "onResume");
        super.onResume();
        mWebRTCView.startPlay();
    }

    @Override
    protected void onPause() {
        Log.v(TAG, "onPause");
        super.onPause();
        mWebRTCView.pausePlay();
    }

    @Override
    protected void onStop() {
        Log.v(TAG, "onStop");
        super.onStop();
        mWebRTCView.stopPlay();
        mWebRTCView.release();
        // 可以不调signalStop(), 后台在连接断开后会保底停止下发数据和计费
        signalingStop();
    }

    private void makeFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onEventOfferCreated(String sdp) {
        Log.v(TAG, "LEBWebRTC onEventOfferCreated");
        signalingStart(sdp);
    }

    @Override
    public void onEventConnected() {
        Log.v(TAG, "LEBWebRTC onEventConnected");
    }

    @Override
    public void onEventConnectFailed(ConnectionState state) {
        Log.v(TAG, "LEBWebRTC onEventConnectFailed, state: " + state);
    }

    @Override
    public void onEventDisconnect() {
        Log.v(TAG, "LEBWebRTC onEventDisconnect");
    }

    @Override
    public void onEventFirstPacketReceived(int mediType) {
        Log.v(TAG, "LEBWebRTC onEventFirstPacketReceived " + mediType);
    }

    @Override
    public void onEventFirstFrameRendered() {
        Log.v(TAG, "LEBWebRTC onEventFirstFrameRendered");
    }

    @Override
    public void onEventResolutionChanged(int width, int height) {
        Log.v(TAG, "LEBWebRTC onEventResolutionChanged, width " + width + " height " + height);
    }

    @Override
    public void onEventSEIReceived(ByteBuffer data) {// 解码线程，不要阻塞，没有start code
        byte[] sei = new byte[data.capacity()];
        data.get(sei);
        Log.v(TAG, "onEventSEIReceived: nal_type " + (sei[0]&0x1f) + " size " + sei.length);
    }

    @Override
    public void onEventStatsReport(LEBWebRTCStatsReport statsReport) {
        if (mShowStats) {
            runOnUiThread(() -> {
                String stats =
                        "disableEncryption: " + mLEBWebRTCParameters.isDisableEncryption() + "\n" +
                        "AudioFormat: " + mLEBWebRTCParameters.getAudioFormat() + "\n" +
                        "***** video stats *****\n" +
                        "PlayTime: " + (statsReport.mPlayTimeMs/1000 + 1) + " s" + "\n" +
                        "Receive/Decode/Drop: " + statsReport.mFramesReceived + "/" + statsReport.mFramesDecoded + "/" + statsReport.mFramesDropped + "\n" +
                        "DecodeFrameRate: " + String.format("%.2f", statsReport.mFramerate) + " fps" + "\n" +
                        "BitRate: " + (statsReport.mVideoBitrate) + " kbps" + "\n" +
                        "PacketsLost: " + statsReport.mVideoPacketsLost + "\n" +
                        "FrameResolution: " + statsReport.mFrameWidth + " x " + statsReport.mFrameHeight + "\n" +
                        "1stVideoPacketDelay: " + statsReport.mFirstVideoPacketDelayMs + " ms" + "\n" +
                        "1stRenderedDelayMs: " + statsReport.mFirstFrameRenderDelayMs + " ms" + "\n" +
                        "DelayMs: " + statsReport.mVideoDelayMs + " ms" + "\n" +
                        "JitterBufferDelayMs: " + statsReport.mVdieoJitterBufferDelayMs + " ms" + "\n" +
                        "NacksSent: " + statsReport.mVideoNacksSent + "\n" +
                        "RTT: " + statsReport.mRTT + " ms" + "\n" +
                        "***** audio stats *****\n" +
                        "PacketsLost: " + statsReport.mAudioPacketsLost + "\n" +
                        "PacketsReceived: " + statsReport.mAudioPacketsReceived + "\n" +
                        "Bitrate: " + statsReport.mAudioBitrate + " kbps" + "\n" +
                        "1stAudioPacketDelayMs: " + statsReport.mFirstAudioPacketDelayMs + " ms" + "\n" +
                        "DelayMs: " + statsReport.mAudioDelayMs + " ms" + "\n" +
                        "JitterBufferDelayMs: " + statsReport.mAudioJitterBufferDelayMs + " ms" + "\n" +
                        "NacksSent: " + statsReport.mAudioNacksSent + "\n";
                mStatsView.setText(stats);
                //Log.d(TAG, "perf stats: " + stats);
            });
        }
    }

    // Put a |key|->|value| mapping in |json|.
    private void jsonPut(JSONObject json, String key, Object value) {
        try {
            json.put(key, value);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    // 向信令服务器发送offer，获取remote sdp, 通过setRemoteSDP接口设置给sdk
    private void signalingStart(final String sdp) {
        JSONObject jsonObject = new JSONObject();
        JSONObject lsdp = new JSONObject();
        jsonPut(lsdp,"sdp", sdp);
        jsonPut(lsdp,"type", "offer");
        jsonPut(jsonObject,"localsdp", lsdp);
        jsonPut(jsonObject,"sessionid", "xxxxxx");//业务生成的唯一key, 标识本次拉流, 用户可自定义
        jsonPut(jsonObject,"clientinfo", "xxxxxx");//终端类型信息, 用户可自定义

        jsonPut(jsonObject,"streamurl", mLEBWebRTCParameters.getStreamUrl());
        Log.d(TAG, "Connecting to signaling server: " + mRequestPullUrl);
        Log.d(TAG, "send offer sdp: " + sdp);
        AsyncHttpURLConnection httpConnection =
                new AsyncHttpURLConnection("POST", mRequestPullUrl, jsonObject.toString(), "origin url", "application/json", new AsyncHttpURLConnection.AsyncHttpEvents() {
                    @Override
                    public void onHttpError(String errorMessage) {
                        Log.e(TAG, "connection error: " + errorMessage);
                        //events.onSignalingParametersError(errorMessage);
                    }

                    @Override
                    public void onHttpComplete(String response) {
                        try {
                            JSONObject json = new JSONObject(response);
                            int errcode = json.optInt("errcode");
                            mSvrSig = json.optString("svrsig");
                            JSONObject rsdp = new JSONObject(json.optString("remotesdp"));
                            String type = rsdp.optString("type");
                            String sdp = (rsdp.optString("sdp"));
                            Log.d(TAG, "response from signaling server: " + response);
                            Log.d(TAG, "svrsig info: " + mSvrSig);
                            if (errcode == 0 && type.equals("answer") && sdp.length() > 0) {
                                Log.d(TAG, "answer sdp = " + sdp);
                                mWebRTCView.setRemoteSDP(sdp);
                            }
                        } catch (JSONException e) {
                            Log.d(TAG, "response JSON parsing error: " + e.toString());
                        }
                    }
                });
        httpConnection.send();
    }

    //向信令服务器请求停流
    private void signalingStop() {
        JSONObject jsonObject = new JSONObject();
        jsonPut(jsonObject,"streamurl", mLEBWebRTCParameters.getStreamUrl());
        jsonPut(jsonObject,"svrsig", mSvrSig);
        AsyncHttpURLConnection httpConnection =
                new AsyncHttpURLConnection("POST", mRequestStopUrl, jsonObject.toString(), "origin url", "application/json", new AsyncHttpURLConnection.AsyncHttpEvents() {
                    @Override
                    public void onHttpError(String errorMessage) {
                        Log.e(TAG, "connection error: " + errorMessage);
                        //events.onSignalingParametersError(errorMessage);
                    }

                    @Override
                    public void onHttpComplete(String response) {
                        try {
                            JSONObject json = new JSONObject(response);
                            int errcode = json.optInt("errcode");
                            String errMsg = json.optString("errmsg");
                            Log.d(TAG, "response from signling server: " + response);
                            if (errcode == 0) {
                                Log.d(TAG,"request to stop success");
                            }
                        } catch (JSONException e) {
                            Log.d(TAG, "response JSON parsing error: " + e.toString());
                        }
                    }
                });
        httpConnection.send();
    }

}
