package com.tencent.xbright.lebwebrtcdemo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.tencent.bugly.crashreport.CrashReport;

public class
MainActivity extends AppCompatActivity implements View.OnClickListener{
    Button btnConnect;
    EditText inputUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // bugly初始化
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(getApplicationContext());
        strategy.setAppChannel("myChannel");  //设置渠道
        strategy.setAppVersion("1.0.7");      //App的版本, 这里设置了SDK version
        strategy.setAppPackageName("com.tencent.xbright.lebwebrtcdemo");  //App的包名
        CrashReport.initCrashReport(getApplicationContext(), "e3243444c9", false, strategy);

        btnConnect = findViewById(R.id.connect);
        btnConnect.setOnClickListener(this);

        inputUrl = findViewById(R.id.url);
    }

    @Override
    public void onClick(View v) {
        String url = inputUrl.getText().toString();
        if (!url.startsWith("webrtc://")) {
            url = "webrtc://5664.liveplay.myqcloud.com/live/5664_harchar1";
        }
        LEBWebRTCDemoActivity.start(MainActivity.this, url);
    }
}
