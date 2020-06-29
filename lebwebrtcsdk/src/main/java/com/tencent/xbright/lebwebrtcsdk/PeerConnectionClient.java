package com.tencent.xbright.lebwebrtcsdk;

import android.content.Context;

import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.PlatformSoftwareVideoDecoderFactory;
import org.webrtc.RTCStatsCollectorCallback;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpTransceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSink;
import org.webrtc.VideoTrack;
import java.util.ArrayList;

/**
 * PeerConnectionClient
 * weifei@tencent.com
 * 2020.4.1
 */
public class PeerConnectionClient implements PeerConnection.Observer, RtpReceiver.Observer{
    private static final String TAG = "PeerConnectionClient";

    private PeerConnection mPC;
    private PeerConnectionFactory mPCFactory;
    private SDPObserver mSdpObserver;

    private PeerConnectionEvent mPcEvent;
    private EglBase mEglBase;

    private VideoSink mVideoRender;
    private AudioTrack mRemoteAudioTrack;
    private LEBWebRTCParameters mLEBWebRTCParameters;

    private VideoTrack mVideoTrack;
    private AudioTrack mAudioTrack;

    public interface PeerConnectionEvent {
        void onCreateOfferSuccess(String sdp);
        void onIceCandidate(String candidate, String sdpMid, int sdpMLineIndex);
        void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState);
        void onConnectionChange(PeerConnection.PeerConnectionState newState);
        void onFirstPacketReceived(MediaStreamTrack.MediaType media_type);
    }

    public PeerConnectionClient(Context applicationContext, EglBase eglBase, PeerConnectionEvent event, LEBWebRTCParameters webRTCParameters) {
        mEglBase = eglBase;
        mPcEvent = event;
        mLEBWebRTCParameters = webRTCParameters;
        mSdpObserver = new SDPObserver();

        initPeerConnectionFactory(applicationContext);
    }

    public void setVideoRender(VideoSink render) {
        mVideoRender = render;
    }

    public void close() {
        if(mPC != null) {
            mPC.dispose();
            mPC = null;
        }
        if (mPCFactory != null) {
            mPCFactory.dispose();
            mPCFactory = null;
        }
    }

    public void setMute(boolean bMute) {
        if (mAudioTrack != null) {
            mAudioTrack.setEnabled(!bMute);
        }
    }


    public void createPeerConnection() {
        if (mPC == null) {
            PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(new ArrayList<>());

            rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;
            rtcConfig.audioJitterBufferMaxPackets = 20;
            rtcConfig.audioJitterBufferFastAccelerate = true;
            mPC = mPCFactory.createPeerConnection(rtcConfig, this);
        }

        if (mPC == null) {
            throw new IllegalArgumentException("illegal PeerConnection");
        }
    }

    public void createOffer() {
        MediaConstraints mediaConstraints = new MediaConstraints();
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        mPC.createOffer(mSdpObserver, mediaConstraints);
    }

    public void setRemoteSDP(final String sdp) {
        SessionDescription sessionDescription = new SessionDescription(
                SessionDescription.Type.ANSWER, sdp);
        mPC.setRemoteDescription(mSdpObserver, sessionDescription);
    }

    public void addRemoteIceCandidate(String candidate, String sdpMid, int sdpMLineIndex) {
        IceCandidate iceCandidate = new IceCandidate(sdpMid, sdpMLineIndex, candidate);
        mPC.addIceCandidate(iceCandidate);
    }


    public void getStats(RTCStatsCollectorCallback callback) {
        mPC.getStats(callback);
    }
    
    private void initPeerConnectionFactory(Context applicationContext) {
        final VideoEncoderFactory encoderFactory = new DefaultVideoEncoderFactory(
                mEglBase.getEglBaseContext(),
                false,
                true
        );

        VideoDecoderFactory decoderFactory = null;
        if (mLEBWebRTCParameters.isEnableHwDecode()) {
            decoderFactory = new DefaultVideoDecoderFactory(mEglBase.getEglBaseContext());
        } else {
            decoderFactory = new PlatformSoftwareVideoDecoderFactory(mEglBase.getEglBaseContext());
        }
        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions.builder(applicationContext)
                .setFieldTrials(new String("WebRTC-FlexFEC-03/Enabled/WebRTC-FlexFEC-03-Advertised/Enabled/"))
                .setEnableInternalTracer(true)
                .createInitializationOptions());

        mPCFactory = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .setOptions(null)
                .createPeerConnectionFactory();

        Logging.enableLogToDebugOutput(Logging.Severity.values()[mLEBWebRTCParameters.getLoggingSeverity()]);
    }

    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        Logging.d(TAG, "onSignalingChange " + signalingState);
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        Logging.d(TAG, "onIceConnectionChange " + iceConnectionState);
        if (mPcEvent != null) {
            mPcEvent.onIceConnectionChange(iceConnectionState);
        }
    }

    @Override
    public void onConnectionChange(PeerConnection.PeerConnectionState newState) {
        Logging.d(TAG, "PeerConnectionState: " + newState);
        if (mPcEvent != null) {
            mPcEvent.onConnectionChange(newState);
        }
    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {
        Logging.d(TAG, "onIceConnectionReceivingChange " + b);
    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        Logging.d(TAG, "onIceGatheringChange " + iceGatheringState);
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
        Logging.d(TAG, "onIceCandidate: " + iceCandidate.toString());
        if (mPcEvent != null) {
            mPcEvent.onIceCandidate(iceCandidate.sdp, iceCandidate.sdpMid, iceCandidate.sdpMLineIndex);
        }
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
        mPC.removeIceCandidates(iceCandidates);
    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
        Logging.d(TAG, "onAddStream");
    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {
        Logging.d(TAG, "onRemoveStream");
    }

    @Override
    public void onDataChannel(final DataChannel dataChannel) {
        Logging.d(TAG, "onDataChannel");
    }

    @Override
    public void onRenegotiationNeeded() {
        Logging.d(TAG, "onRenegotiationNeeded");
    }

    @Override
    public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
        rtpReceiver.SetObserver(this::onFirstPacketReceived);
        MediaStreamTrack track = rtpReceiver.track();
        if (track instanceof VideoTrack) {
            Logging.d(TAG, "onAddTrack VideoTrack");
            VideoTrack remoteVideoTrack = (VideoTrack) track;
            remoteVideoTrack.setEnabled(true);
            if (mVideoRender != null) {
                remoteVideoTrack.addSink(mVideoRender);
            }
            mVideoTrack = (VideoTrack)track;
        } else if (track instanceof AudioTrack) {
            Logging.d(TAG, "onAddTrack AudioTrack");
            mAudioTrack = (AudioTrack)track;
        }
    }

    @Override
    public void onTrack(RtpTransceiver transceiver) {
        Logging.d(TAG, "onTrack");
    }

    private class SDPObserver implements SdpObserver {

        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
            Logging.d(TAG, "SdpObserver onCreateSuccess");
            mPC.setLocalDescription(mSdpObserver, sessionDescription);
            if (mPcEvent != null) {
                mPcEvent.onCreateOfferSuccess(sessionDescription.description);
            }
        }

        @Override
        public void onSetSuccess() {
            Logging.d(TAG, "SdpObserver onSetSuccess");
        }

        @Override
        public void onCreateFailure(String msg) {
            Logging.e(TAG, "SdpObserver onCreateFailure: " + msg);
        }

        @Override
        public void onSetFailure(String msg) {
            Logging.e(TAG, "SdpObserver onCreateFailure: " + msg);
        }
    }

    @Override
    public void onFirstPacketReceived(MediaStreamTrack.MediaType media_type) {
        Logging.e(TAG, "onFirstPacketReceived: " + media_type);
        if (mPcEvent != null) {
            mPcEvent.onFirstPacketReceived(media_type);
        }
    }
}
