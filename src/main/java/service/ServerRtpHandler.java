package service;

import config.ConfigManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import media.dtmf.DtmfUnit;
import media.module.mixing.AudioMixManager;
import media.netty.NettyChannelManager;
import media.netty.module.NettyChannel;
import media.protocol.jrtp.JRtp;
import media.protocol.rtp.RtpPacket;
import media.protocol.rtp.attribute.format.base.RtpAudioFormat;
import media.protocol.rtp.base.RtpFormat;
import media.protocol.rtp.base.RtpFrame;
import media.protocol.rtp.jitter.JitterBuffer;
import media.sdp.base.Sdp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.base.TaskUnit;
import signal.base.CallInfo;
import signal.base.RoomInfo;
import signal.module.CallManager;
import signal.module.GroupCallManager;

/**
 * @class public class ServerRtpHandler extends TaskUnit
 * @brief ServerRtpHandler class
 */
public class ServerRtpHandler extends TaskUnit {

    private static final Logger logger = LoggerFactory.getLogger(ServerRtpHandler.class);

    private final String key;
    private final JitterBuffer jitterBuffer;

    ////////////////////////////////////////////////////////////////////////////////

    public ServerRtpHandler(String key, JitterBuffer jitterBuffer, int interval) {
        super(interval);

        this.key = key;
        this.jitterBuffer = jitterBuffer;

        logger.debug("({}) ServerRtpHandler is created.", key);
    }

    ////////////////////////////////////////////////////////////////////////////////

    @Override
    public void run() {
        try {
            // 1) Read data
            RtpFrame rtpFrame = jitterBuffer.read();
            if (rtpFrame == null) {
                return;
            }

            byte[] rtpData = rtpFrame.getData();
            if (rtpData == null) {
                return;
            }

            RtpFormat rtpFormat = rtpFrame.getRtpFormat();
            if (rtpFormat == null) {
                return;
            }

            String ip = rtpFormat.getIp();
            int port = rtpFormat.getPort();
            short gain = (short) rtpFormat.getGain();
            RtpAudioFormat audioFormat = (RtpAudioFormat) rtpFormat.getFormat();
            int samplingRate = audioFormat.getSampleRate();
            int sampleSize = audioFormat.getSampleSize();
            int channelSize = audioFormat.getChannels();
            //

            // 2) Find CallInfo
            CallInfo callInfo = CallManager.getInstance().findCallInfoByMediaAddress(ip, port);
            if (callInfo == null) {
                //logger.warn("({}) Fail to find the call info. (ip={}, port={})", key, ip, port);
                return;
            }
            String callId = callInfo.getCallId();
            //

            // 3) Check Call Type & Send Rtp data
            // 3-1) Group call
            if (callInfo.getIsRoomEntered()) {
                RoomInfo roomInfo = GroupCallManager.getInstance().getRoomInfo(callInfo.getSessionId());
                if (roomInfo == null) {
                    //logger.warn("({}) Fail to find the room info. (roomId={})", key, callInfo.getSessionId());
                    return;
                }

                for (String curCallId : roomInfo.cloneRemoteCallList(callId)) {
                    CallInfo remoteCallInfo = CallManager.getInstance().getCallInfo(curCallId);
                    if (remoteCallInfo == null) {
                        continue;
                    }

                    perform(
                            callId,
                            ip, port,
                            remoteCallInfo,
                            samplingRate,
                            sampleSize,
                            channelSize,
                            gain,
                            rtpData
                    );
                }
            }
            // 3-2) Relay call
            else {
                CallInfo remoteCallInfo = callInfo.getRemoteCallInfo();
                if (remoteCallInfo == null) {
                    //logger.warn("({}) Fail to find the remote call info. (ip={}, port={})", key, ip, port);
                    return;
                }

                perform(
                        callId,
                        ip, port,
                        remoteCallInfo,
                        samplingRate,
                        sampleSize,
                        channelSize,
                        gain,
                        rtpData
                );
            }
            //

            // 4) Mix Audio
            RtpPacket rtpPacket = new RtpPacket(rtpData, rtpData.length);
            if (rtpPacket.getPayloadType() != DtmfUnit.DTMF_TYPE) {
                AudioMixManager.getInstance().perform(
                        callInfo.getSessionId(),
                        callInfo.getCallId(),
                        samplingRate,
                        sampleSize,
                        channelSize,
                        gain,
                        rtpPacket.getPayload()
                );
            }
            //
        } catch (Exception e) {
            logger.warn("({}) Fail to process the rtp data.", key, e);
        }
    }

    private void perform (
            String callId,
            String ip, int port,
            CallInfo remoteCallInfo,
            int samplingRate, int sampleSize, int channelSize, short gain, byte[] rtpData) {
        // 1) Get Remote sdp unit
        Sdp remoteSdp = remoteCallInfo.getSdp();
        if (remoteSdp == null) {
            logger.warn("({}) Fail to find the remote sdp unit. (ip={}, port={})", key, ip, port);
            return;
        }
        //

        // 2) Get Proxy channel
        NettyChannel nettyChannel = NettyChannelManager.getInstance().getProxyChannel(callId);
        if (nettyChannel == null) {
            logger.trace("({}) Fail to find the netty channel. (ip={}, port={})", key, ip, port);
            return;
        }
        //

        // 3) Make Rtp data
        ByteBuf byteBuf;
        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        if (configManager.isRelay()) {
            byteBuf = Unpooled.copiedBuffer(rtpData);
        } else {
            JRtp jRtp = new JRtp();
            jRtp.setData(
                    configManager.getNettyServerIp(),
                    nettyChannel.getListenPort(),
                    samplingRate,
                    sampleSize,
                    channelSize,
                    gain,
                    rtpData
            );

            byteBuf = Unpooled.copiedBuffer(jRtp.getData());
        }
        //

        // 4) Send JRtp data
        if (nettyChannel.sendMessage(remoteSdp.getId(), byteBuf, remoteSdp.getSessionOriginAddress(), remoteSdp.getMediaPort(Sdp.AUDIO))) {
            logger.trace("({}) Relay RTP. (curCallId={}, remoteCallId={}, src={}:{}, dst={}:{})", key, callId, remoteSdp.getId(), ip, port, remoteSdp.getSessionOriginAddress(), remoteSdp.getMediaPort(Sdp.AUDIO));
        }
        //
    }

}
