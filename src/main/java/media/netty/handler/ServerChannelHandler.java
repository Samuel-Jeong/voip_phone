package media.netty.handler;

import config.ConfigManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import media.protocol.jrtp.JRtp;
import media.protocol.rtp.RtpPacket;
import media.protocol.rtp.attribute.format.AudioProfile;
import media.protocol.rtp.attribute.format.FormatFactory;
import media.protocol.rtp.base.RtpFormat;
import media.protocol.rtp.jitter.JitterBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AppInstance;

/**
 * @class public class ServerChannelHandlerServerChannelHandler extends SimpleChannelInboundHandler<DatagramPacket>
 */
public class ServerChannelHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final Logger logger = LoggerFactory.getLogger(ServerChannelHandler.class);

    private final ConfigManager configManager = AppInstance.getInstance().getConfigManager();

    private final String name;
    private final JitterBuffer jitterBuffer;

    ////////////////////////////////////////////////////////////////////////////////

    public ServerChannelHandler(JitterBuffer jitterBuffer, String name) {
        this.jitterBuffer = jitterBuffer;

        this.name = name;
        logger.debug("ServerChannelHandler is created. (name={})", name);
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
        if (!configManager.isProxyMode()) {
            return;
        }

        // 1) Read data
        ByteBuf buf = msg.content();
        if (buf == null) {
            return;
        }

        int readBytes = buf.readableBytes();
        byte[] data = new byte[readBytes];
        buf.getBytes(0, data);
        if (buf.readableBytes() <= 0) {
            return;
        }

        JRtp jRtp = new JRtp(data);
        String ip = jRtp.getIp();
        int port = jRtp.getPort();
        int samplingRate = jRtp.getSamplingRate();
        int sampleSize = jRtp.getSampleSize();
        int channelSize = jRtp.getChannelSize();
        short gain = jRtp.getGain();
        byte[] rtpData = jRtp.getRtpData();
        //

        // 2) Jittering
        try {
            RtpPacket rtpPacket = new RtpPacket(rtpData, rtpData.length);

            String codecName = AudioProfile.getCodecNameFromID(rtpPacket.getPayloadType());
            if (codecName == null) {
                logger.warn("Wrong codec is detected. Discarded. (payloadId={})", rtpPacket.getPayloadType());
                return;
            }

            jitterBuffer.write(
                    rtpPacket,
                    new RtpFormat(
                            rtpPacket.getPayloadType(),
                            FormatFactory.createAudioFormat(
                                    codecName,
                                    samplingRate,
                                    sampleSize,
                                    channelSize
                            ),
                            gain,
                            8000,
                            ip, port
                    )
            );
        } catch (Exception e) {
            logger.warn("({}) Fail to handle UDP Packet.", name, e);
        }
        //
    }

    @Override
    public void exceptionCaught (ChannelHandlerContext ctx, Throwable cause) {
        //logger.warn("ServerHandler.exceptionCaught", cause);
        //ctx.close();
    }

}
