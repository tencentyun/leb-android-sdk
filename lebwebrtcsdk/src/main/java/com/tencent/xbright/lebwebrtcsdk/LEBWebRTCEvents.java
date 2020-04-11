package com.tencent.xbright.lebwebrtcsdk;

/**
 * LEBWebRTC状态与事件
 */
public interface LEBWebRTCEvents {

    enum ConnectionState
    {
        // 开始建立连接
        STATE_BEGIN,
        // OFFER创建
        STATE_OFFER_CREATED,
        // ICE完成
        STATE_ICE_COMPLETED,
        // 连接建立
        STATE_WEBRTC_CONNECTED,
        // 渲染第一帧
        STATE_FIRST_FRAME_RENDERED,
        // 连接超时
        STATE_WEBRTC_TIMEOUT,
    }

    // offer创建成功
    void onEventOfferCreated(String sdp);

    // 连接成功
    void onEventConnected();

    // 连接失败
    void onEventConnectFailed(ConnectionState cs);

    // 断开断开
    void onEventDisconnect();

    // 断开断开
    void onEventFirstPacketReceived(int mediType);//0:audio, 1:video

    // 渲染首帧
    void onEventFirstFrameRendered();

    // 分辨率切换
    void onEventResolutionChanged(int width, int height);

    // 统计数据
    void onEventStatsReport(LEBWebRTCStatsReport webRTCStatsReport);
}
