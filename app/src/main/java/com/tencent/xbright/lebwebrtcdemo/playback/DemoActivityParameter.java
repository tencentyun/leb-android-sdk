package com.tencent.xbright.lebwebrtcdemo.playback;

import android.os.Parcel;
import android.os.Parcelable;

import com.tencent.xbright.lebwebrtcsdk.LEBWebRTCParameters;

public class DemoActivityParameter implements Parcelable {
    public boolean mEncryption = true;

    public boolean mEnableHwDecode = true;

    public boolean mReceiveAudio = true;

    public boolean mReceiveVideo = true;

    public boolean mSEICallback = false;

    public int mAudioFormat = LEBWebRTCParameters.OPUS;

    public int mMinJitterDelayMs = 1000;
    public String mPlaybackStreamUrl;
    public String mPlaybackPullUrl;
    public String mPlaybackStopUrl;

    public DemoActivityParameter() {
    }

    protected DemoActivityParameter(Parcel in) {
        mEncryption = in.readByte() != 0;
        mEnableHwDecode = in.readByte() != 0;
        mReceiveAudio = in.readByte() != 0;
        mReceiveVideo = in.readByte() != 0;
        mSEICallback = in.readByte() != 0;
        mAudioFormat = in.readInt();
        mMinJitterDelayMs = in.readInt();
        mPlaybackStreamUrl = in.readString();
        mPlaybackPullUrl = in.readString();
        mPlaybackStopUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (mEncryption ? 1 : 0));
        dest.writeByte((byte) (mEnableHwDecode ? 1 : 0));
        dest.writeByte((byte) (mReceiveAudio ? 1 : 0));
        dest.writeByte((byte) (mReceiveVideo ? 1 : 0));
        dest.writeByte((byte) (mSEICallback ? 1 : 0));
        dest.writeInt(mAudioFormat);
        dest.writeInt(mMinJitterDelayMs);
        dest.writeString(mPlaybackStreamUrl);
        dest.writeString(mPlaybackPullUrl);
        dest.writeString(mPlaybackStopUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DemoActivityParameter> CREATOR = new Creator<DemoActivityParameter>() {
        @Override
        public DemoActivityParameter createFromParcel(Parcel in) {
            return new DemoActivityParameter(in);
        }

        @Override
        public DemoActivityParameter[] newArray(int size) {
            return new DemoActivityParameter[size];
        }
    };
}
