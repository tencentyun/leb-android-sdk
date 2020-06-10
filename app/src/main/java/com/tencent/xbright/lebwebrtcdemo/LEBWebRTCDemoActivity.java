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
    private String           mRequestStopUrl = "https://webrtc.liveplay.myqcloud.com//webrtc/v1/stopstream";//停止拉流
    private String           mSvrSig;//服务器签名，后面每个请求必须携带这个字段内容, 业务无需理解字段内容

    private int  mVideoViewLayoutWidth = 0;
    private int  mVideoViewLayoutHeight = 0;
    private int  cachedHeight;

    public static void start(Context context, String url) {
        Intent intent = new Intent(context, LEBWebRTCDemoActivity.class);
        intent.putExtra("url", url);
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

        mWebRTCUrl = getIntent().getStringExtra("url");

        Log.d(TAG, "set stream url: " + mWebRTCUrl);
        mLEBWebRTCParameters = new LEBWebRTCParameters();
        mLEBWebRTCParameters.setStreamUrl(mWebRTCUrl);
        mLEBWebRTCParameters.setLoggingSeverity(LEBWebRTCParameters.LOG_NONE);
        mLEBWebRTCParameters.enableHwDecode(true);
        mLEBWebRTCParameters.setConnectionTimeOutInMs(5000);//5s
        mLEBWebRTCParameters.setStatsReportPeriodInMs(1000);

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
        signalingStop();
    }

    private void makeFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onEventOfferCreated(String sdp) {
        Log.v(TAG, "LEBWebRTC offer created");
        signalingStart(sdp);
    }

    @Override
    public void onEventConnected() {
        Log.v(TAG, "LEBWebRTC Connected");
    }

    @Override
    public void onEventConnectFailed(ConnectionState state) {
        Log.v(TAG, "LEBWebRTC Connect Failed, state: " + state);
    }

    @Override
    public void onEventDisconnect() {
        Log.v(TAG, "LEBWebRTC Disconnect");
    }

    @Override
    public void onEventFirstPacketReceived(int mediType) {
        Log.v(TAG, "LEBWebRTC onEventFirstPacketReceived " + mediType);
    }

    @Override
    public void onEventFirstFrameRendered() {
        Log.v(TAG, "LEBWebRTC onFirstFrameRendered");
    }

    @Override
    public void onEventResolutionChanged(int width, int height) {
        Log.v(TAG, "LEBWebRTC onEventResolutionChanged, width " + width + " height " + height);
    }

    @Override
    public void onEventStatsReport(LEBWebRTCStatsReport LEBWebRTCStatsReport) {
        if (mShowStats) {
            runOnUiThread(() -> {
                String stats =
                        "***** video stats *****\n" +
                        "PlayTime: " + LEBWebRTCStatsReport.mPlayTime +
                        " s" +
                        "\n" +
                        "Receive/Decode/Drop: " +
                        LEBWebRTCStatsReport.mFramesReceived +
                        "/" +
                        LEBWebRTCStatsReport.mFramesDecoded +
                        "/" +
                        LEBWebRTCStatsReport.mFramesDropped +
                        "\n" +
                        "DecodeFrameRate: " + String.format("%.2f", LEBWebRTCStatsReport.mFramerate) +
                        " fps" +
                        "\n" +
                        "BitRate: " +
                        (LEBWebRTCStatsReport.mVideoBitrate) +
                        " kbps" +
                        "\n" +
                        "PacketsLost: " +
                        LEBWebRTCStatsReport.mPacketsLost +
                        "\n" +
                        "FrameResolution: " +
                        LEBWebRTCStatsReport.mFrameWidth +
                        " x " +
                        LEBWebRTCStatsReport.mFrameHeight +
                        "\n" +
                        "1stVideoPacketDelay: " +
                        LEBWebRTCStatsReport.mFirstVideoPacketDelay +
                        " ms" +
                        "\n" +
                        "1stRenderedDelay: " +
                        LEBWebRTCStatsReport.mFirstFrameRenderDelay +
                        " ms" +
                        "\n" +
                        "\n***** audio stats *****\n" +
                        "PacketsLost: " +
                        LEBWebRTCStatsReport.mAudioPacketsLost +
                        "\n" +
                        "PacketsReceived: " +
                        LEBWebRTCStatsReport.mAudioPacketsReceived +
                        "\n" +
                        "Bitrate: " +
                        LEBWebRTCStatsReport.mAudioBitrate +
                        " kbps" +
                        "\n" +
                        "1stAudioPacketDelay: " +
                         LEBWebRTCStatsReport.mFirstVideoPacketDelay +
                         " ms" +
                         "\n";
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
                                mWebRTCView.stopPlay();
                                mWebRTCView.release();
                            }
                        } catch (JSONException e) {
                            Log.d(TAG, "response JSON parsing error: " + e.toString());
                        }
                    }
                });
        httpConnection.send();
    }

}
