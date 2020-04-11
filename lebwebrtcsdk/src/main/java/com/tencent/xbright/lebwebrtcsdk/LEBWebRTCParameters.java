package com.tencent.xbright.lebwebrtcsdk;

/**
 * 快直播WebRTC参数
 * weifei@tencent.com
 * 2020.4.1
 */
public class LEBWebRTCParameters {

    // 快直播播放地址
    private String mStreamUrl;

    // 是否使用硬件解码
    private boolean mEnableHwDecode = true;

    // 播放状态回调周期
    private int mStatsReportPeriodInMs = 1000;

    // WebRTC日志级别
    public static final int LOG_VERBOSE = 0x00;
    public static final int LOG_INFO    = 0x01;
    public static final int LOG_WARNING = 0x02;
    public static final int LOG_ERROR   = 0x03;
    public static final int LOG_NONE    = 0x04;
    private int mLoggingSeverity = LOG_NONE;

    // WebRTC连接超时
    private int mConnectoionTimeoutInMs = 5000;//ms


    public LEBWebRTCParameters(){}

    public void setStreamUrl(String url) {
        mStreamUrl = url;
    }

    public String getStreamUrl() {
        return mStreamUrl;
    }

    public void setLoggingSeverity(int loggingSeverity) {
        mLoggingSeverity = loggingSeverity;
    }

    public int getLoggingSeverity() {
        return mLoggingSeverity;
    }

    public void enableHwDecode(boolean hwDecode) {
        mEnableHwDecode = hwDecode;
    }

    public boolean isEnableHwDecode() {
        return mEnableHwDecode;
    }

    public void setConnectionTimeOutInMs(int timeOutInMs) {
        mConnectoionTimeoutInMs = timeOutInMs;
    }

    public int getConnectionTimeOutInMs() {
        return mConnectoionTimeoutInMs;
    }

    public void setStatsReportPeriodInMs(int statsReportPeriodInMs) {
        mStatsReportPeriodInMs = statsReportPeriodInMs;
    }

    public int getStatsReportPeriodInMs() {
        return mStatsReportPeriodInMs;
    }
}
