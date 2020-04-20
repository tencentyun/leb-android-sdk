# signal https接口

```	
【必选接口】
https://live.rtc.qq.com:8687/webrtc/v1/pullstream           //请求拉流
https://live.rtc.qq.com:8687/webrtc/v1/stopstream           //停止拉流
```
	
```
【可选接口】
https://live.rtc.qq.com:8687/webrtc/v1/reportquality        //质量上报，2s上报一次，可选接口
https://live.rtc.qq.com:8687/webrtc/v1/reporticestate       //ice状态上报，状态变化时上报
https://live.rtc.qq.com:8687/webrtc/v1/firstframeusetime    //首帧耗时上报
```

### 所有的接口都是post请求，请求参数是json格式，所有参数字段都是必填的，各个接口具体参数如下。

## 【必选】pullstream 

	//req 
	{
		streamurl:  string //拉流地址
		sessionid:  string //业务生成的唯一key，标识本次拉流
		clientinfo: string //终端类型信息
		localsdp: {
			type: string
			sdp:  string
		}
		clientip:   string //客户端IP
		seipass:    int    //是否带SEI
	}

	//rsp
	{
		errcode:  int
		errmsg:   string
		remotesdp: {
			type: string
			sdp:  string
		}
		svrsig: string           //服务器签名，后面每个请求必须携带这个字段内容,业务无需理解字段内容
	}

- streamurl：是快直播的拉流url，与[腾讯云标准直播拉流url](https://cloud.tencent.com/document/product/267/13551#step5)基本相同，只需要将腾讯云标准直播拉流url前面的"rtmp"替换为"webrtc"即可。快直播拉流url格式是`"webrtc://domain/path/stream_id`，如果需要[防盗链鉴权](https://cloud.tencent.com/document/product/267/32735)，
则拉流url格式是`"webrtc://domain/path/stream_id?txSecret=xxx&txTime=xxx"`。与标准直播相同，快直播也支持拉不同分辨率、码率的转码流，转码流url生成请参考[云直播转码](https://cloud.tencent.com/document/product/267/32736)

- clientinfo：是终端类型信息，格式：设备型号\_操作系统及版本号\_业务应用名及版本号，如SM-G9600\_android8.0.0\_腾讯快直播demo1.0。如果是浏览器，则可以通过navigator.userAgent接口获取上述信息

- localsdp、remotesdp：是标准的sdp协议(参考[rfc4566](https://tools.ietf.org/html/rfc4566))，localsdp的type值固定是"offer"，remotesdp的type值固定是"answer"。
如果是浏览器，获取本地sdp的接口是RTCPeerConnection::createOffer，拿到返回的remotesdp之后，则调用RTCPeerConnection::setRemoteDescription进行设置

- clientip：可选参数，默认不填。表示客户端IP，如 "101.87.40.217"

- seipass：可选参数，默认不填。表示是否需要 SEI 包，是填1，不是填0

- errcode：值为0，则表示请求拉流成功，非0则表示失败，具体失败原因参考errmsg
  - 100011 参数 clientip 为无效IP

## 【必选】stopstream

	//req 
	{
		streamurl:  string //拉流地址
		svrsig:     string //pullstream返回的服务器签名
	}

	//rsp
	{
		errcode: int
		errmsg:  string
	}

停止拉流必须调用本接口通知后台停止拉流，否则后台会在数据通道超时断开前继续下发音视频数据，残留的UDP通道既浪费后台资源，也会影响业务并发和计费带宽统计


## 【可选】reportquality
    //req
    {
        streamurl:           string
        svrsig:              string //pullstream返回的服务器签名
        beginutime:    		 string //统计的开始时间
        endutime:      		 string //统计的结束时间
        audiossrc:           int
        audiocodec:          string //音频编码类型，如opus、aac
        audiopktrecv:        int    //实际收到的音频数据包数
        audiobyterecv:       int    //接收的音频数据字节数
        audiopktlost:        int    //音频数据丢包数
        videossrc:           int
        videocodec:          string //视频编码类型，如vp8、h264
        videopktrecv:        int    //实际收到的视频数据包数
        videobyterecv:       int    //接收的视频数据字节数
        videopktlost:        int    //视频数据丢包数
        videowidth:          int    //视频画面分辨率宽度
        videoheight:         int    //视频画面分辨率高度
        videoframedec:       int    //解码帧率
        videoframerecv:      int    //接收帧率
    }

    //rsp
    {
        errcode: int
        errmsg:  string
    }

质量上报，每两秒统计一次最近两秒下行音视频的帧率、码率、接收包数、丢包数等信息。

## 【可选】reporticestate
	//req
	{
		streamurl:          string
		iceConnectionState: string
		iceGatheringState:  string
		svrsig:             string //pullstream返回的服务器签名
	}

	//rsp
	{
		errcode: int
		errmsg:  string
	}

上报ice的状态，每次ice状态变化，上报新的ice状态

## 【可选】firstframeusetime
	//req
	{
		streamurl:        string
	    starttime:        string
	    firstplaytime:    string
		svrsig:           string //pullstream返回的服务器签名
	}

	//rsp
	{
		errcode: int
		errmsg:  string
	}

上报首帧耗时，耗时计算是从发起拉流请求到渲染出首帧画面。
