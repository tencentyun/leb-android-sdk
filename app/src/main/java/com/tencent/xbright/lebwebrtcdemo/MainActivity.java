package com.tencent.xbright.lebwebrtcdemo;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.xbright.lebwebrtcsdk.LEBWebRTCParameters;

import androidx.appcompat.app.AppCompatActivity;

import static android.widget.TextView.BufferType.EDITABLE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "MainActivity";
    Button btnConnect;
    EditText streamUrlText;
    String   streamUrl = "webrtc://5664.liveplay.myqcloud.com/live/5664_harchar1";
    EditText pullUrlText;
    String   pullUrl = "http://webrtc.liveplay.myqcloud.com/webrtc/v1/pullstream";

    RadioGroup videoCodec;
    boolean enbleHwDecode = true;

    RadioGroup audioCodec;
    int audioFormat = LEBWebRTCParameters.OPUS; //LEBWebRTCParameters.AAC_LATM, LEBWebRTCParameters.AAC_ADTS

    RadioGroup encryption;
    boolean disableEncryption = false;

    RadioGroup seiCallback;
    boolean enableSEICallback = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // bugly初始化
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(getApplicationContext());
        strategy.setAppChannel("myChannel");  //设置渠道
        strategy.setAppVersion("2.0.4");      //App的版本, 这里设置了SDK version
        strategy.setAppPackageName("com.tencent.xbright.lebwebrtcdemo");  //App的包名
        CrashReport.initCrashReport(getApplicationContext(), "e3243444c9", false, strategy);

        btnConnect = findViewById(R.id.connect);
        btnConnect.setOnClickListener(this);

        streamUrlText = findViewById(R.id.streamurl);
        streamUrlText.setText(streamUrl, EDITABLE);
        pullUrlText = findViewById(R.id.pullurl);
        pullUrlText.setText(pullUrl, EDITABLE);



        videoCodec = findViewById(R.id.video);
        videoCodec.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                switch (checkedId) {
                    case R.id.hw_decode:
                        enbleHwDecode = true;
                        break;
                    case R.id.sw_decode:
                        enbleHwDecode = false;
                        break;
                }
                Log.d(TAG, "enbleHwDecode: " + enbleHwDecode);
            }
        });

        audioCodec = findViewById(R.id.audio);
        audioCodec.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.opus:
                        audioFormat = LEBWebRTCParameters.OPUS;
                        break;
                    case R.id.aac_latm:
                        audioFormat = LEBWebRTCParameters.AAC_LATM;
                        break;
                    case R.id.aac_adts:
                        audioFormat = LEBWebRTCParameters.AAC_ADTS;
                        break;
                }
                Log.d(TAG, "audioFormat: " + audioFormat);
            }
        });

        encryption = findViewById(R.id.encrypted);
        encryption.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.on:
                        disableEncryption = false;
                        break;
                    case R.id.off:
                        disableEncryption = true;
                        break;
                }
                Log.d(TAG, "disableEncryption: " + disableEncryption);
            }
        });

        seiCallback = findViewById(R.id.sei_callback);
        seiCallback.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.sei_on:
                        enableSEICallback = true;
                        break;
                    case R.id.sei_off:
                        enableSEICallback = false;
                        break;
                }
                Log.d(TAG, "enableSEICallback: " + enableSEICallback);
            }
        });

        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    @Override
    public void onClick(View v) {

        if (streamUrlText.getText().toString().startsWith("webrtc://")) {
            streamUrl = streamUrlText.getText().toString();
        }

        if (pullUrlText.getText().toString().startsWith("https://") || pullUrlText.getText().toString().startsWith("http://")) {
            pullUrl = pullUrlText.getText().toString();

        }
        LEBWebRTCDemoActivity.start(MainActivity.this, 0, streamUrl, pullUrl, enbleHwDecode, audioFormat, disableEncryption, enableSEICallback);
    }
}
