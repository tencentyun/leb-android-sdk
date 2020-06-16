package com.tencent.xbright.lebwebrtcdemo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    Button btnConnect;
    EditText inputUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnConnect = findViewById(R.id.connect);
        btnConnect.setOnClickListener(this);

        inputUrl = findViewById(R.id.url);
    }

    @Override
    public void onClick(View v) {
        String url = inputUrl.getText().toString();
        if (!url.startsWith("webrtc://")) {
            url = "webrtc://6721.liveplay.myqcloud.com/live/6721_d71956d9cc93e4a467b11e06fdaf039a";
        }
        LEBWebRTCDemoActivity.start(MainActivity.this, url);
    }
}
