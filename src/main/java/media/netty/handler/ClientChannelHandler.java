package media.netty.handler;

import client.VoipClient;
import config.ConfigManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import media.protocol.rtp.RtpPacket;
import media.protocol.rtp.module.RtpQosHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AppInstance;

/**
 * @class public class ClientChannelHandler extends SimpleChannelInboundHandler<DatagramPacket>
 */
public class ClientChannelHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final Logger logger = LoggerFactory.getLogger(ClientChannelHandler.class);

    private final ConfigManager configManager = AppInstance.getInstance().getConfigManager();

    private final RtpQosHandler rtpQosHandler = new RtpQosHandler();

    ////////////////////////////////////////////////////////////////////////////////

    public ClientChannelHandler() {
        logger.debug("ClientChannelHandler is created.");
    }

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @param ctx ChannelHandlerContext {@link ChannelHandlerContext}
     * @param msg UDP 패킷 데이터
     * @fn protected void channelRead0 (ChannelHandlerContext ctx, DatagramPacket msg)
     * @brief UDP 패킷을 Media Server 으로부터 수신하는 함수
     */
    @Override
    protected void channelRead0 (ChannelHandlerContext ctx, DatagramPacket msg) {
        ByteBuf buf = msg.content();
        if (buf == null) {
            return;
        }

        try {
            if (buf.readableBytes() > 0) {
                handleRtpPacket(buf);
            }
        } catch (Exception e) {
            logger.warn("Fail to handle UDP Packet.", e);
        }
    }

    @Override
    public void exceptionCaught (ChannelHandlerContext ctx, Throwable cause) {
        //logger.warn("ServerHandler.exceptionCaught", cause);
        //ctx.close();
    }

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @fn public void handleRtpPacket(ByteBuf buf)
     * @brief RTP Packet 을 처리하는 함수
     * @param buf ByteBuf 객체
     */
    private void handleRtpPacket(ByteBuf buf) {
        try {
            if (configManager.isUseClient()) {
                VoipClient voipClient = VoipClient.getInstance();
                if (!voipClient.isStarted()) {
                    return;
                }

                // 1) Read a packet data.
                int readBytes = buf.readableBytes();
                byte[] data = new byte[readBytes];
                buf.getBytes(0, data);
                if (buf.readableBytes() <= 0) {
                    return;
                }
                //

                // 2) Check QoS.
                RtpPacket rtpPacket = new RtpPacket(data, data.length);
                /*if (!rtpQosHandler.checkSeqNum(rtpPacket.getSeqNum())) {
                    //logger.trace("Wrong RTP Packet is detected. Discarded. (jRtp={}, rtpPacket={})", jRtp, rtpPacket);
                    logger.warn("Wrong RTP Packet is detected. Discarded. (rtpPacket={})", rtpPacket);
                    return;
                }*/
                byte[] payload = rtpPacket.getPayload();
                //

                // 3) Write the audio data to the speaker
                if (payload.length > 0) {
                    voipClient.writeToSpeaker(payload);
                    logger.trace("Recv RTP. (remoteHostName={}, payloadLength={})", voipClient.getRemoteHostName(), payload.length);
                }
                //
            }
        } catch (Exception e) {
            logger.warn("ClientChannelHandler.handleRtpPacket.Exception", e);
        }
    }

}
