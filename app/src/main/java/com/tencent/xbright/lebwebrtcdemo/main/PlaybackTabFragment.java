package com.tencent.xbright.lebwebrtcdemo.main;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tencent.xbright.lebwebrtcdemo.playback.DemoActivityParameter;
import com.tencent.xbright.lebwebrtcdemo.playback.LEBWebRTCDemoActivity;
import com.tencent.xbright.lebwebrtcdemo.R;
import com.tencent.xbright.lebwebrtcsdk.LEBWebRTCParameters;

import static android.widget.TextView.BufferType.EDITABLE;

public class PlaybackTabFragment extends Fragment {
    private static final String TAG = "PlaybackTabFragment";

    private EditText streamUrlText;
    private String   streamUrl = "webrtc://5664.liveplay.myqcloud.com/live/5664_harchar1";
    private EditText pullUrlText;
    private String   pullUrl = "http://webrtc.liveplay.myqcloud.com/webrtc/v1/pullstream";//140.249.28.162
    private String   stopUrl = "http://webrtc.liveplay.myqcloud.com/webrtc/v1/stopstream";

    private int audioFormat = LEBWebRTCParameters.OPUS; //LEBWebRTCParameters.AAC_LATM, LEBWebRTCParameters.AAC_ADTS

    private boolean receiveAudio = true;
    private boolean receiveVideo = true;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playback_tab, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Button btnConnect = view.findViewById(R.id.connect);
        btnConnect.setOnClickListener(this::startPlayback);

        streamUrlText = view.findViewById(R.id.stream_url);
        streamUrlText.setHint(streamUrl);
        pullUrlText = view.findViewById(R.id.signal_url);
        pullUrlText.setHint(pullUrl);

        RadioGroup playMode = view.findViewById(R.id.play_mode);
        playMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.play_normal:
                        receiveAudio = true;
                        receiveVideo = true;
                        break;
                    case R.id.play_audio_only:
                        receiveAudio = true;
                        receiveVideo = false;
                        break;
                    case R.id.play_video_only:
                        receiveAudio = false;
                        receiveVideo = true;
                        break;
                }
                Log.d(TAG, "playMode receiveAudio: " + receiveAudio + " receiveVideo: " + receiveVideo);
            }
        });

        RadioGroup audioCodec = view.findViewById(R.id.audio);
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

        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    private void startPlayback(View v) {
        if (streamUrlText.getText().toString().startsWith("webrtc://")) {
            streamUrl = streamUrlText.getText().toString();
        }

        if (pullUrlText.getText().toString().startsWith("https://") || pullUrlText.getText().toString().startsWith("http://")) {
            pullUrl = pullUrlText.getText().toString();

        }
        String stream = streamUrl.replace("livepush", "liveplay");
        View root = getView();
        DemoActivityParameter parameter = new DemoActivityParameter();
        parameter.mEncryption = ((Switch)root.findViewById(R.id.encrypted_switch)).isChecked();
        parameter.mEnableHwDecode = ((Switch)root.findViewById(R.id.video_hwaccel_switch)).isChecked();
        parameter.mReceiveAudio = receiveAudio;
        parameter.mReceiveVideo = receiveVideo;
        parameter.mSEICallback = ((Switch)root.findViewById(R.id.sei_callback)).isChecked();
        parameter.mAudioFormat = audioFormat;
        parameter.mPlaybackStreamUrl = stream;
        parameter.mPlaybackPullUrl = pullUrl;
        parameter.mPlaybackStopUrl = stopUrl;
        LEBWebRTCDemoActivity.start(getActivity(), parameter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        streamUrlText = null;
        pullUrlText = null;
    }
}
