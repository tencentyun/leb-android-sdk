package com.tencent.xbright.lebwebrtcdemo;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

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
 */

public class LEBWebRTCDemoActivity extends AppCompatActivity implements LEBWebRTCEvents {
    private static final String TAG = "WebRTCDemoActivity";
    private String              mWebRTCUrl;
    private LEBWebRTCParameters mLEBWebRTCParameters;
    private LEBWebRTCView       mWebRTCView;
    private TextView         mStatsView;
    private FrameLayout      mVideoViewLayout;
    private boolean          mShowStats = true;

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
        mLEBWebRTCParameters.setLoggingSeverity(LEBWebRTCParameters.LOG_VERBOSE);
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
        mWebRTCView.stopPlay();
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
                         "\n";
                mStatsView.setText(stats);
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
    private void signalingStart (final String sdp) {
         //"需要用户实现信令请求，发送offer，并获取remote sdp"
    }

    //向信令服务器请求停流
    private void signalingStop () {
        //"需要用户实现停流请求"
    }

}
