package com.tencent.xbright.lebwebrtcsdk;

import com.tencent.xbright.lebwebrtcsdk.LEBWebRTCStatsReport;

import org.webrtc.RTCStats;
import org.webrtc.RTCStatsReport;

import java.math.BigInteger;
import java.util.Map;


/**
 * 接收WebRTC的性能数据
 */
public class LEBWebRTCStatsCollector {

    public LEBWebRTCStatsReport mLEBWebRTCStatsReport;

    private StatsRecord mLastStatsRecord;
    private StatsRecord mFirstStatsRecodrd;

    public static class StatsRecord {
        //audio stats
        long decodeTimeMs;
        long timestamp;
        long packetsReceived;
        int packetsLost;
        long bytesReceived;
        long frameWidth;
        long frameHeight;
        long framesReceived;
        long framesDecoded;
        long framesDropped;
        long rtt;
        long freezeCount;
        double totalFreezesDuration;
        double totalRoundTripTime;
        long responsesReceived;

        //audio stats
        long audioPacketsReceived;
        int audioPacketsLost;
        long audioBytesReceived;

    }

    public LEBWebRTCStatsCollector(){
        this.mLEBWebRTCStatsReport = new LEBWebRTCStatsReport();
    }

    public void onStatsDelivered(RTCStatsReport rtcStatsReport) {
        //Log.d(Config.TAG, "WebRTC stats: " + rtcStatsReport.toString());
        StatsRecord newStatsRecord = new StatsRecord();
        newStatsRecord.timestamp = (long) (rtcStatsReport.getTimestampUs());

        Map<String, RTCStats> rtcStatsMap = rtcStatsReport.getStatsMap();
        for (Map.Entry<String, RTCStats> entry : rtcStatsMap.entrySet()) {
            Map<String, Object> rtcStatsMemberMap = entry.getValue().getMembers();
            String type = entry.getValue().getType();
            String kind = (String) rtcStatsMemberMap.get("kind");
            if ("inbound-rtp".equalsIgnoreCase(type) && "video".equalsIgnoreCase(kind)) {
                if (rtcStatsMemberMap.get("packetsReceived") != null) {
                    newStatsRecord.packetsReceived = (long) rtcStatsMemberMap.get("packetsReceived");
                }
                if (rtcStatsMemberMap.get("packetsLost") != null) {
                    newStatsRecord.packetsLost = (int) rtcStatsMemberMap.get("packetsLost");
                }
                if (rtcStatsMemberMap.get("bytesReceived") != null) {
                    newStatsRecord.bytesReceived = ((BigInteger) rtcStatsMemberMap.get("bytesReceived")).longValue();
                }
            }

            if ("inbound-rtp".equalsIgnoreCase(type) && "audio".equalsIgnoreCase(kind)) {
                if (rtcStatsMemberMap.get("packetsReceived") != null) {
                    newStatsRecord.audioPacketsReceived = (long) rtcStatsMemberMap.get("packetsReceived");
                }
                if (rtcStatsMemberMap.get("packetsLost") != null) {
                    newStatsRecord.audioPacketsLost = (int) rtcStatsMemberMap.get("packetsLost");
                }
                if (rtcStatsMemberMap.get("bytesReceived") != null) {
                    newStatsRecord.audioBytesReceived = ((BigInteger) rtcStatsMemberMap.get("bytesReceived")).longValue();
                }
            }

            if ("track".equalsIgnoreCase(type) && "video".equalsIgnoreCase(kind)) {
                if (rtcStatsMemberMap.get("frameWidth") != null) {
                    newStatsRecord.frameWidth = (long) rtcStatsMemberMap.get("frameWidth");
                }
                if (rtcStatsMemberMap.get("frameHeight") != null) {
                    newStatsRecord.frameHeight = (long) rtcStatsMemberMap.get("frameHeight");
                }
                if (rtcStatsMemberMap.get("framesReceived") != null) {
                    newStatsRecord.framesReceived = (long) rtcStatsMemberMap.get("framesReceived");
                }
                if (rtcStatsMemberMap.get("framesDecoded") != null) {
                    newStatsRecord.framesDecoded = (long) rtcStatsMemberMap.get("framesDecoded");
                }
                if (rtcStatsMemberMap.get("framesDropped") != null) {
                    newStatsRecord.framesDropped = (long) rtcStatsMemberMap.get("framesDropped");
                }
                if (rtcStatsMemberMap.get("decodeTimeMs") != null) {
                    newStatsRecord.decodeTimeMs = (long) rtcStatsMemberMap.get("decodeTimeMs");
                }
                if (rtcStatsMemberMap.get("freezeCount") != null) {
                    newStatsRecord.freezeCount = (long) rtcStatsMemberMap.get("freezeCount");
                }
                if (rtcStatsMemberMap.get("totalFreezesDuration") != null) {
                    newStatsRecord.totalFreezesDuration = (double) rtcStatsMemberMap.get("totalFreezesDuration");
                }
            }

            if ("candidate-pair".equalsIgnoreCase(type)) {
                if (rtcStatsMemberMap.get("currentRoundTripTime") != null) {
                    newStatsRecord.rtt = (long) ((double)rtcStatsMemberMap.get("currentRoundTripTime") * 1000);
                }
                if (rtcStatsMemberMap.get("totalRoundTripTime") != null) {
                    newStatsRecord.totalRoundTripTime = (double)rtcStatsMemberMap.get("totalRoundTripTime");
                }
                if (rtcStatsMemberMap.get("responsesReceived") != null) {
                    newStatsRecord.responsesReceived = ((BigInteger) rtcStatsMemberMap.get("responsesReceived")).longValue();
                }
            }
        }

        if (mFirstStatsRecodrd == null) {
            mFirstStatsRecodrd = newStatsRecord;
        }

        if (mLastStatsRecord == null) {
            mLastStatsRecord = newStatsRecord;
        } else {
            mLEBWebRTCStatsReport.mFramesReceived = newStatsRecord.framesReceived;
            mLEBWebRTCStatsReport.mFramesDecoded = newStatsRecord.framesDecoded;
            mLEBWebRTCStatsReport.mFramesDropped = newStatsRecord.framesDropped;
            mLEBWebRTCStatsReport.mPacketsLost = newStatsRecord.packetsLost;
            mLEBWebRTCStatsReport.mAudioPacketsLost = newStatsRecord.audioPacketsLost;
            mLEBWebRTCStatsReport.mAudioPacketsReceived = newStatsRecord.audioPacketsReceived;
            mLEBWebRTCStatsReport.mFrameWidth = newStatsRecord.frameWidth;
            mLEBWebRTCStatsReport.mFrameHeight = newStatsRecord.frameHeight;
            if (newStatsRecord.timestamp != mLastStatsRecord.timestamp) {
                mLEBWebRTCStatsReport.mFramerate = ((newStatsRecord.framesDecoded - mLastStatsRecord.framesDecoded) * 1000000.0 /
                        (newStatsRecord.timestamp - mLastStatsRecord.timestamp));
                mLEBWebRTCStatsReport.mVideoBitrate = (newStatsRecord.bytesReceived - mLastStatsRecord.bytesReceived) * 8 /
                        ((newStatsRecord.timestamp - mLastStatsRecord.timestamp) / 1000);
                mLEBWebRTCStatsReport.mAudioBitrate = (newStatsRecord.audioBytesReceived - mLastStatsRecord.audioBytesReceived) * 8 /
                        ((newStatsRecord.timestamp - mLastStatsRecord.timestamp) / 1000);
            }

            if (newStatsRecord.timestamp != mFirstStatsRecodrd.timestamp) {
                mLEBWebRTCStatsReport.mAverageFrameRate = (newStatsRecord.framesDecoded - mFirstStatsRecodrd.framesDecoded) * 1000000 /
                        (newStatsRecord.timestamp - mFirstStatsRecodrd.timestamp);
                mLEBWebRTCStatsReport.mAverageBitRate = (newStatsRecord.bytesReceived - mFirstStatsRecodrd.bytesReceived) * 8 /
                        ((newStatsRecord.timestamp - mFirstStatsRecodrd.timestamp) / 1000);
            }

            mLEBWebRTCStatsReport.mPlayTime = (newStatsRecord.timestamp - mFirstStatsRecodrd.timestamp) / 1000000 + 1;
            mLastStatsRecord = newStatsRecord;
        }
    }
}
