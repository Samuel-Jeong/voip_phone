package client.module.base;

import client.VoipClient;
import config.ConfigManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import media.MediaManager;
import media.module.codec.amr.AmrManager;
import media.module.codec.evs.EvsManager;
import media.module.codec.pcm.ALawTranscoder;
import media.module.codec.pcm.ULawTranscoder;
import media.module.mixing.base.ConcurrentCyclicFIFO;
import media.netty.module.NettyChannel;
import media.protocol.jrtp.JRtp;
import media.protocol.rtp.RtpPacket;
import media.protocol.rtp.util.RtpUtil;
import media.record.RecordManager;
import media.sdp.base.SdpUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AppInstance;
import service.base.TaskUnit;
import signal.base.CallInfo;
import signal.module.CallManager;

import javax.sound.sampled.AudioFormat;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @class public class UdpSender extends TaskUnit
 * @brief UdpSender class
 */
public class UdpSender extends TaskUnit {

    private static final Logger logger = LoggerFactory.getLogger(UdpSender.class);

    private final int TIME_DELAY;

    /* Netty Channel to send the message */
    private final NettyChannel nettyChannel;
    /* Mike Data Buffer */
    private final ConcurrentCyclicFIFO<byte[]> mikeBuffer;

    private final ConcurrentCyclicFIFO<MediaFrame> sendBuffer = new ConcurrentCyclicFIFO<>();
    private ScheduledThreadPoolExecutor executor;

    /* JRtp Message object */
    private final JRtp jRtp = new JRtp();
    /* RTP Message object */
    private final RtpPacket rtpPacket = new RtpPacket();

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @fn protected UdpSender(ConcurrentCyclicFIFO<byte[]> mikeBuffer, NettyChannel nettyChannel, int interval)
     * @brief UdpSender 생성자 함수
     * @param nettyChannel Netty Channel
     * @param interval Task interval
     */
    public UdpSender(ConcurrentCyclicFIFO<byte[]> mikeBuffer, NettyChannel nettyChannel, int interval) {
        super(interval);

        this.TIME_DELAY = interval * 8;
        this.nettyChannel = nettyChannel;
        this.mikeBuffer = mikeBuffer;
    }

    public void start() {
        try {
            if (executor == null) {
                executor = new ScheduledThreadPoolExecutor(5);

                SendTask sendTask = new SendTask(
                        1
                        //MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_WB)? 40 : 20
                );
                executor.scheduleAtFixedRate(
                        sendTask,
                        sendTask.getInterval(),
                        sendTask.getInterval(),
                        TimeUnit.MILLISECONDS
                );
                logger.debug("UdpSender SendTask is added.");
            }

            switch (MediaManager.getInstance().getPriorityCodec()) {
                case MediaManager.EVS:
                    EvsManager.getInstance().startUdpSenderTask(sendBuffer);
                    break;
                case (MediaManager.AMR_NB):
                    AmrManager.getInstance().startEncAmrNb();
                    break;
                case (MediaManager.AMR_WB):
                    AmrManager.getInstance().startEncAmrWb();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            logger.warn("UdpSender.start.Exception", e);
        }
    }

    public void stop() {
        sendBuffer.clear();
        switch (MediaManager.getInstance().getPriorityCodec()) {
            case MediaManager.EVS:
                EvsManager.getInstance().stopUdpSenderTask();
                break;
            case (MediaManager.AMR_NB):
                AmrManager.getInstance().stopEncAmrNb();
                break;
            case (MediaManager.AMR_WB):
                AmrManager.getInstance().stopEncAmrWb();
                break;
            default:
                break;
        }

        if (executor != null) {
            executor.shutdown();
            executor = null;
            logger.debug("UdpSender SendTask is removed.");
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @fn public void run()
     * @brief UdpSender 비즈니스 로직을 실행하는 함수
     */
    @Override
    public void run() {
        VoipClient voipClient = VoipClient.getInstance();

        try {
            byte[] data = mikeBuffer.poll();
            if (data == null || data.length == 0) {
                return;
            }

            // 1) Pre-process the audio data.
            // PCM
            if (voipClient.getTargetAudioFormat().getEncoding().toString().equals(
                    AudioFormat.Encoding.PCM_SIGNED.toString())) {
                // Convert to little endian.
                if (voipClient.isTargetBigEndian()) {
                    data = RtpUtil.changeByteOrder(data);
                }

                RecordManager pcmRecordManager = voipClient.getTargetPcmRecordManager();
                if (pcmRecordManager != null) {
                    pcmRecordManager.addData(data);
                }

                // ALAW
                if (MediaManager.getInstance().getPriorityCodec().equals(AudioFormat.Encoding.ALAW.toString())) {
                    data = ALawTranscoder.encode(
                            data
                    );

                    RecordManager aLawRecordManager = voipClient.getTargetALawRecordManager();
                    if (aLawRecordManager != null) {
                        aLawRecordManager.addData(data);
                    }
                }
                // ULAW
                else if (MediaManager.getInstance().getPriorityCodec().equals(AudioFormat.Encoding.ULAW.toString())) {
                    data = ULawTranscoder.encode(
                            data
                    );

                    RecordManager uLawRecordManager = voipClient.getTargetULawRecordManager();
                    if (uLawRecordManager != null) {
                        uLawRecordManager.addData(data);
                    }
                }
                // EVS
                else if (MediaManager.getInstance().getPriorityCodec().equals(MediaManager.EVS)) {
                    EvsManager.getInstance().addUdpSenderInputData(data);
                    return;
                }
                // AMR-NB
                else if (MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_NB)) {
                    data = AmrManager.getInstance().encAmrNb(
                            MediaManager.AMR_NB_MAX_MODE_SET,
                            data
                    );

                    if (data != null) {
                        RecordManager amrNbRecordManager = voipClient.getTargetAmrNbRecordManager();
                        if (amrNbRecordManager != null) {
                            amrNbRecordManager.addData(data);
                        }
                    }
                }
                // AMR-WB
                else if (MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_WB)) {
                    data = AmrManager.getInstance().encAmrWb(
                            MediaManager.AMR_WB_MAX_MODE_SET,
                            data
                    );

                    if (data != null) {
                        RecordManager amrWbRecordManager = voipClient.getTargetAmrWbRecordManager();
                        if (amrWbRecordManager != null) {
                            amrWbRecordManager.addData(data);
                        }
                    }
                }
            }

            sendBuffer.offer(
                    new MediaFrame(
                            false,
                            data
                    )
            );
        } catch (Exception e){
            logger.warn("Fail to read the data.", e);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

    private class SendTask extends TaskUnit {

        protected SendTask(int interval) {
            super(interval);
        }

        @Override
        public void run() {
            MediaFrame mediaFrame = sendBuffer.poll();
            if (mediaFrame == null) {
                return;
            }

            boolean isDtmf = mediaFrame.isDtmf();
            byte[] data = mediaFrame.getData();

            // 2) Broadcast the rtp packet.
            Map<String, CallInfo> callInfoMap = CallManager.getInstance().getCloneCallMap();
            if (callInfoMap.isEmpty()) { return; }

            for (Map.Entry<String, CallInfo> entry : callInfoMap.entrySet()) {
                CallInfo callInfo = entry.getValue();
                if (callInfo == null) {
                    continue;
                }

                SdpUnit remoteSdpUnit = callInfo.getSdpUnit();
                if (remoteSdpUnit == null) {
                    return;
                }

                // 3) Insert the encoded data into a rtp packet.
                int seqNum;
                if (isDtmf) {
                    seqNum = callInfo.getDtmfSeqNum();
                    rtpPacket.setValue(
                            2, 0, 0, 0, 0, 101,
                            seqNum,
                            callInfo.getDtmfTimestamp(),
                            callInfo.getDtmfSsrc(),
                            data,
                            data.length
                    );
                    callInfo.setDtmfSeqNum(seqNum + 1);
                } else {
                    seqNum = callInfo.getAudioSeqNum();
                    rtpPacket.setValue(
                            2, 0, 0, 0, 0, MediaManager.getInstance().getPriorityCodecId(),
                            seqNum,
                            callInfo.getAudioTimestamp(),
                            callInfo.getAudioSsrc(),
                            data,
                            data.length
                    );
                    callInfo.setAudioSeqNum(seqNum + 1);
                }

                // Set final data
                ConfigManager configManager = AppInstance.getInstance().getConfigManager();
                VoipClient voipClient = VoipClient.getInstance();

                byte[] bufData;
                if (configManager.isUseProxy()) {
                    jRtp.setData(
                            configManager.getNettyServerIp(),
                            nettyChannel.getListenPort(),
                            (int) voipClient.getTargetAudioFormat().getSampleRate(),
                            voipClient.getTargetSampleSize(),
                            voipClient.getTargetChannelSize(),
                            voipClient.getTargetVolume(),
                            rtpPacket.getData()
                    );
                    bufData = jRtp.getData();
                } else {
                    bufData = rtpPacket.getData();
                }

                // 3) Send the rtp packet.
                ByteBuf buf = Unpooled.copiedBuffer(
                        bufData
                );

                if (nettyChannel.sendMessage(
                        remoteSdpUnit.getId(),
                        buf,
                        remoteSdpUnit.getRemoteIp(),
                        remoteSdpUnit.getRemotePort())) {
                    logger.trace("Send RTP. (callId={}, src={}, dst={}:{})",
                            remoteSdpUnit.getCallId(), nettyChannel.getListenPort(),
                            remoteSdpUnit.getRemoteIp(), remoteSdpUnit.getRemotePort()
                    );
                } else {
                    logger.warn("Fail to send RTP. (callId={}, src={}, dst={}:{})",
                            remoteSdpUnit.getCallId(), nettyChannel.getListenPort(),
                            remoteSdpUnit.getRemoteIp(), remoteSdpUnit.getRemotePort()
                    );
                }

                if (isDtmf) {
                    callInfo.setDtmfTimestamp(callInfo.getDtmfTimestamp() + TIME_DELAY); // 20ms per 1 rtp packet (sampling-rate: 8000)
                    if (callInfo.getDtmfSeqNum() >= CallInfo.MAX_SEQ_NUM) {
                        callInfo.initDtmfSeqNum();
                    }
                    if (callInfo.getDtmfTimestamp() >= Long.MAX_VALUE - 1) {
                        callInfo.initDtmfTimestamp();
                    }
                } else {
                    callInfo.setAudioTimestamp(callInfo.getAudioTimestamp() + TIME_DELAY); // 20ms per 1 rtp packet (sampling-rate: 8000)
                    if (callInfo.getAudioSeqNum() >= CallInfo.MAX_SEQ_NUM) {
                        callInfo.initAudioSeqNum();
                    }
                    if (callInfo.getAudioTimestamp() >= Long.MAX_VALUE - 1) {
                        callInfo.initAudioTimestamp();
                    }
                }
            }
        }
    }

    public ConcurrentCyclicFIFO<MediaFrame> getSendBuffer() {
        return sendBuffer;
    }
}
