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
            url = "webrtc://6721.liveplay.now.qq.com/live/6721_338ed8ff85c9f15e54294e4aa4c98b1e?txSecret=41e3eca391b3fbfb77e65768d91cb605&txTime=5E3C4262";
        }
        LEBWebRTCDemoActivity.start(MainActivity.this, url);
    }
}
