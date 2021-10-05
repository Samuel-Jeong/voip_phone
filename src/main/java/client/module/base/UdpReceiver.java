package client.module.base;

import client.VoipClient;
import media.MediaManager;
import media.dtmf.DtmfUnit;
import media.module.codec.amr.AmrManager;
import media.module.codec.evs.EvsManager;
import media.module.codec.pcm.ALawTranscoder;
import media.module.codec.pcm.ULawTranscoder;
import media.module.mixing.base.AudioBuffer;
import media.module.mixing.base.AudioFrame;
import media.module.mixing.base.ConcurrentCyclicFIFO;
import media.protocol.rtp.util.RtpUtil;
import media.record.RecordManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.base.TaskUnit;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @class public class UdpSender extends TaskUnit
 * @brief UdpSender class
 */
public class UdpReceiver extends TaskUnit {

    private static final Logger logger = LoggerFactory.getLogger(UdpReceiver.class);

    private int curRtpPayloadLength = 0;

    private final ConcurrentCyclicFIFO<MediaFrame> recvBuffer = new ConcurrentCyclicFIFO<>();
    private ScheduledThreadPoolExecutor recvTaskExecutor;

    private final AudioBuffer audioBuffer;

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @fn protected UdpReceiver(AudioBuffer audioBuffer, int interval)
     * @brief UdpReceiver 생성자 함수
     * @param audioBuffer AudioBuffer
     * @param interval Task interval
     */
    public UdpReceiver(AudioBuffer audioBuffer, int interval) {
        super(interval);

        this.audioBuffer = audioBuffer;
    }

    public void start() {
        if (recvTaskExecutor == null) {
            recvTaskExecutor = new ScheduledThreadPoolExecutor(5);

            try {
                RecvTask recvTask = new RecvTask(
                        20
                        //MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_WB)? 40 : 20
                );

                recvTaskExecutor.scheduleAtFixedRate(
                        recvTask,
                        recvTask.getInterval(),
                        recvTask.getInterval(),
                        TimeUnit.MILLISECONDS
                );
            } catch (Exception e) {
                logger.warn("UdpReceiver.start.Exception", e);
            }

            logger.debug("UdpReceiver RecvTask is added.");
        }

        switch (MediaManager.getInstance().getPriorityCodec()) {
            case MediaManager.EVS:
                EvsManager.getInstance().startUdpReceiverTask(recvBuffer);
                break;
            case MediaManager.AMR_NB:
                AmrManager.getInstance().startDecAmrNb();
                break;
            case MediaManager.AMR_WB:
                AmrManager.getInstance().startDecAmrWb();
                break;
            default:
                break;
        }

        curRtpPayloadLength = MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_WB)? 640 : 320;
    }

    public void stop() {
        recvBuffer.clear();
        switch (MediaManager.getInstance().getPriorityCodec()) {
            case MediaManager.EVS:
                EvsManager.getInstance().stopUdpReceiverTask();
                break;
            case MediaManager.AMR_NB:
                AmrManager.getInstance().stopDecAmrNb();
                break;
            case MediaManager.AMR_WB:
                AmrManager.getInstance().stopDecAmrWb();
                break;
            default:
                break;
        }

        if (recvTaskExecutor != null) {
            recvTaskExecutor.shutdown();
            recvTaskExecutor = null;
            logger.debug("UdpReceiver RecvTask is removed.");
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
            AudioFrame audioFrame = audioBuffer.evolve();
            if (audioFrame == null) {
                return;
            }

            boolean isDtmf = audioFrame.isDtmf();
            byte[] data = audioFrame.getData(true);
            if (data == null || data.length == 0) {
                return;
            }

            // PCM
            if (!isDtmf && !isByteArrayFullOfZero(data)) {
                if (voipClient.getSourceAudioFormat().getEncoding().toString().equals(
                        AudioFormat.Encoding.PCM_SIGNED.toString())) {
                    // Decode pcm data to other codec data
                    // ALAW
                    if (MediaManager.getInstance().getPriorityCodec().equals(AudioFormat.Encoding.ALAW.toString())) {
                        RecordManager aLawRecordManager = voipClient.getSourceALawRecordManager();
                        if (aLawRecordManager != null) {
                            aLawRecordManager.addData(data);
                            //aLawRecordManager.writeFileStream(data);
                        }

                        data = ALawTranscoder.decode(
                                data
                        );
                    }
                    // ULAW
                    else if (MediaManager.getInstance().getPriorityCodec().equals(AudioFormat.Encoding.ULAW.toString())) {
                        RecordManager uLawRecordManager = voipClient.getSourceULawRecordManager();
                        if (uLawRecordManager != null) {
                            uLawRecordManager.addData(data);
                        }

                        data = ULawTranscoder.decode(
                                data
                        );
                    }
                    // EVS
                    else if (MediaManager.getInstance().getPriorityCodec().equals("EVS")) {
                        EvsManager.getInstance().addUdpReceiverInputData(data);
                        return;
                    }
                    // AMR-NB
                    else if (MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_NB)) {
                        RecordManager amrNbRecordManager = voipClient.getSourceAmrNbRecordManager();
                        if (amrNbRecordManager != null) {
                            amrNbRecordManager.addData(data);
                        }

                        data = AmrManager.getInstance().decAmrNb(
                                curRtpPayloadLength,
                                data
                        );
                    }
                    // AMR-WB
                    else if (MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_WB)) {
                        RecordManager amrWbRecordManager = voipClient.getSourceAmrWbRecordManager();
                        if (amrWbRecordManager != null) {
                            amrWbRecordManager.addData(data);
                        }

                        data = AmrManager.getInstance().decAmrWb(
                                curRtpPayloadLength,
                                data
                        );
                    }

                    if (data == null) {
                        return;
                    }
                    //
                }

                addData(data, false);
            } else {
                addData(data, true);
            }

        } catch (Exception e){
            logger.warn("Fail to write the data.", e);
        }
    }

    public void addData (byte[] data, boolean isDtmf) {
        if (recvTaskExecutor != null) {
            try {
                if (data == null) {
                    return;
                }

                int totalDataLength = data.length;
                int curDataLength = data.length;
                int remainDataLength = curRtpPayloadLength;

                if (totalDataLength > curRtpPayloadLength) {
                    // Data length 가 320 bytes 보다 크면, 320 bytes 씩 분할해서 20 ms 마다 스피커에 송출하도록 설정
                    for (int i = 0; i < data.length; i += curRtpPayloadLength) {
                        if (curDataLength - curRtpPayloadLength < 0) {
                            remainDataLength = curDataLength;
                            curDataLength = 0;
                        }

                        byte[] splitedData = new byte[remainDataLength];
                        System.arraycopy(data, i, splitedData, 0, remainDataLength);
                        recvBuffer.offer(
                                new MediaFrame(
                                        isDtmf,
                                        splitedData
                                )
                        );

                        if (curDataLength == 0) {
                            break;
                        } else {
                            curDataLength -= curRtpPayloadLength;
                        }
                    }
                } else {
                    recvBuffer.offer(
                            new MediaFrame(
                                    isDtmf,
                                    data
                    ));
                }
            } catch (Exception e) {
                logger.warn("UdpReceiver.addData.Exception", e);
            }
        }
    }

    private boolean isByteArrayFullOfZero(byte[] data) {
        for (byte datum : data) {
            if (datum != 0) {
                return false;
            }
        }
        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////

    private class RecvTask extends TaskUnit {

        protected RecvTask(int interval) {
            super(interval);
        }

        @Override
        public void run() {
            byte[] data;

            MediaFrame mediaFrame = recvBuffer.poll();
            if (mediaFrame == null) {
                return;
            }

            boolean isDtmf = mediaFrame.isDtmf();
            data = mediaFrame.getData();

            if (!isByteArrayFullOfZero(data)) {
                if (!isDtmf) {
                    if (VoipClient.getInstance().getSourceAudioFormat().getEncoding().toString().equals(
                            AudioFormat.Encoding.PCM_SIGNED.toString())) {
                        RecordManager pcmRecordManager = VoipClient.getInstance().getSourcePcmRecordManager();
                        if (pcmRecordManager != null) {
                            pcmRecordManager.addData(data);
                            //pcmRecordManager.writeFileStream(data);
                        }

                        // Convert to big endian.
                        if (VoipClient.getInstance().isSourceBigEndian()) {
                            data = RtpUtil.changeByteOrder(data);
                        }
                    }
                } else {
                    logger.debug("DTMF is detected!");
                    DtmfUnit dtmfUnit = new DtmfUnit(data);
                    logger.debug("DTMF digit: {}", dtmfUnit);
                }
            }

            SourceDataLine sourceDataLine = VoipClient.getInstance().getSourceLine();
            if (sourceDataLine != null) {
                sourceDataLine.write(data, 0, data.length);
            }
        }
    }

    public ConcurrentCyclicFIFO<MediaFrame> getRecvBuffer() {
        return recvBuffer;
    }
}
