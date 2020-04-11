package com.tencent.xbright.lebwebrtcsdk;

/**
 * 快直播播放状态报告
 * weifei@tencent.com
 * 2020.4.1
 */
public class LEBWebRTCStatsReport {
    //video stats
    public long   mFirstVideoPacketDelay;//从启动到收到第一包视频数据的延时
    public long   mFirstFrameRenderDelay; //从启动到首帧渲染延时
    public double mFramerate; //解码帧率
    public long   mVideoBitrate; //视频码率
    public long   mFramesDecoded; //解码帧数
    public long   mFramesDropped; //丢帧数
    public long   mFramesReceived; //接收帧数
    public int    mPacketsLost; //丢包个数
    public long   mFrameWidth; //视频宽度
    public long   mFrameHeight; //视频高度

    //audio stats
    public long   mFirstAudioPacketDelay;//从启动到收到第一包音频数据的延时
    public int    mAudioPacketsLost; //丢包个数
    public long   mAudioPacketsReceived; //接收包数
    public long   mAudioBitrate;//音频码率

    //play stats
    public double mAverageFrameRate;//平均帧率
    public long   mAverageBitRate;//平均码率
    public long   mPlayTime;//播放时长
}
