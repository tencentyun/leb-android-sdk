## leb android sdk release history

### v2.0.9
    1. 增加最小jitter delay限制接口
    2. 修复一些问题

### v2.0.8
    1. 支持audio only and video only播放模式
    2. 增加视频解码器状态回调接
    3. 整理demo代码

### v2.0.5
    1. 增加最大音频JitterBuffer队列大小设置接口
    2. 增加追帧功能开关接口

### v2.0.4
    1. 修复不设置日志回调时crash问题

### v2.0.3
    1. 重构接口, LEBWebRTCView变为接口类，增加LEBWebRTCSurfaceView和LEBWebRTCTextureView
    2. 支持日志回调
    3. 修复切后台再切前台黑屏问题

### v2.0.2
    1. 支持画面截图
    2. 支持旋转显示
    3. 支持等比例缩放和平铺缩放
    4. 支持音频增益设置

### v2.0.1
    扩展标准WebRTC，支持如下特性：
    1. 支持AAC，包括LC、HE和HEv2
    2. 支持H265，包括硬解和软解
    3. 支持H264和H265的B帧解码
    4. 支持关闭加密
    5. 支持H264和H265的SEI数据回调

### v1.0.7
    1. support builtin h264 software decoder

### v1.0.6
    1. clear frame when stop play
    2. polish stats info, add delay, jiterbufferdelay, nackssent and rtt

### v1.0.5
    1. add audio jitter buffer config

### v1.0.4
    1. force to use platform software decoder when hwdecode is not enabled

### v1.0.3
    1. fix a problem for multi-instances
    2. add api for playing in mute

### v1.0.2
    1. fix a build problem

### v1.0.1
    1. update document

### v1.0.0
    1. init sdk and demo
