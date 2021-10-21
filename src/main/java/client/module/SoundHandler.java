package client.module;

import client.module.base.PcmGenerator;
import client.module.base.UdpReceiver;
import client.module.base.UdpSender;
import media.module.mixing.base.AudioBuffer;
import media.module.mixing.base.ConcurrentCyclicFIFO;
import media.netty.NettyChannelManager;
import media.netty.module.NettyChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.ServiceManager;

import javax.sound.sampled.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @class public class SoundHandler
 * @brief SoundHandler Class
 */
public class SoundHandler {

    private static final Logger logger = LoggerFactory.getLogger(SoundHandler.class);

    private SourceDataLine sourceDataLine;
    private TargetDataLine targetDataLine;

    private final Map<String, Line> targetLineMap = new HashMap<>();
    private final Map<String, Line> sourceLineMap = new HashMap<>();

    private AudioFormat sourceAudioFormat;
    private String sourceCodec;
    private String sourceSamplingRate;
    private int sourceSampleSize;
    private int sourceFrameSize;
    private float sourceFrameRate;
    private int sourceChannelSize;
    private boolean sourceIsBigEndian;
    private int sourcePayloadId;

    private AudioFormat targetAudioFormat;
    private String targetCodec;
    private String targetSamplingRate;
    private int targetSampleSize;
    private int targetFrameSize;
    private float targetFrameRate;
    private int targetChannelSize;
    private boolean targetIsBigEndian;
    private int targetPayloadId;

    /* Mike Data Buffer */
    private final ConcurrentCyclicFIFO<byte[]> mikeBuffer = new ConcurrentCyclicFIFO<>();
    /* Speaker Data Buffer */
    private final AudioBuffer speakerBuffer = new AudioBuffer("SpeakerBuffer");

    ////////////////////////////////////////////////////////////////////////////////

    private final ScheduledThreadPoolExecutor mikeExecutor = new ScheduledThreadPoolExecutor(2);
    private final ScheduledThreadPoolExecutor pcmExecutor = new ScheduledThreadPoolExecutor(2);
    private final ScheduledThreadPoolExecutor speakerExecutor = new ScheduledThreadPoolExecutor(2);

    private ScheduledFuture<?> mikeScheduledFuture = null;
    private ScheduledFuture<?> pcmScheduledFuture = null;
    private ScheduledFuture<?> speakerScheduledFuture = null;
    private UdpSender udpSender = null;
    private UdpReceiver udpReceiver = null;
    private PcmGenerator pcmGenerator = null;

    ////////////////////////////////////////////////////////////////////////////////

    public SoundHandler() {
        // Nothing
    }

    ////////////////////////////////////////////////////////////////////////////////

    public void initSound () {
        sourceLineMap.clear();
        targetLineMap.clear();

        try {
            Mixer.Info[] infoMixers = AudioSystem.getMixerInfo();
            for (Mixer.Info infoMixer : infoMixers) {
                Mixer mixer = AudioSystem.getMixer(infoMixer);
                String mixerName = infoMixer.getName();

                Line.Info[] targetLineInfos = mixer.getTargetLineInfo();
                for (Line.Info targetLineInfo : targetLineInfos) {
                    if (targetLineInfo.getLineClass().equals(TargetDataLine.class)) {
                        targetLineMap.putIfAbsent(mixerName, mixer.getLine(targetLineInfo));
                        break;
                    }
                }

                Line.Info[] sourceLineInfos = mixer.getSourceLineInfo();
                for (Line.Info sourceLineInfo : sourceLineInfos) {
                    if (sourceLineInfo.getLineClass().equals(SourceDataLine.class)) {
                        sourceLineMap.putIfAbsent(mixerName, mixer.getLine(sourceLineInfo));
                        break;
                    }
                }
            }

            if (targetLineMap.isEmpty() || sourceLineMap.isEmpty()) {
                logger.error("|Fail to get the audio mixers.");
            } else {
                logger.trace("> sourceLineMap: {}", sourceLineMap);
                logger.trace("> targetLineMap: {}", targetLineMap);
            }
        } catch (Exception e) {
            logger.error("|Fail to get the audio mixers. Program ends.", e);
            ServiceManager.getInstance().stop();
            System.exit(1);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

    private void setSourceDataLine(SourceDataLine sourceDataLine) {
        this.sourceDataLine = sourceDataLine;

        sourceAudioFormat = this.sourceDataLine.getFormat();
        sourceCodec = this.sourceDataLine.getFormat().getEncoding().toString();
        sourceSamplingRate = String.valueOf(this.sourceDataLine.getFormat().getSampleRate());
        sourceSampleSize = this.sourceDataLine.getFormat().getSampleSizeInBits();
        sourceFrameSize = this.sourceDataLine.getFormat().getFrameSize();
        sourceFrameRate = this.sourceDataLine.getFormat().getFrameRate();
        sourceChannelSize = this.sourceDataLine.getFormat().getChannels();
        sourceIsBigEndian = this.sourceDataLine.getFormat().isBigEndian();

        /*if (sourceCodec.equals(PCM_SIGNED.toString()) || sourceCodec.equals(PCM_UNSIGNED.toString())) {
            sourceCodec = MediaManager.getInstance().getPriorityCodec();
            if (sourceCodec.equals(ALAW.toString())) {
                sourcePayloadId = 8;
            } else if (sourceCodec.equals(ULAW.toString())) {
                sourcePayloadId = 0;
            }
            sourceSamplingRate = "8000";
            sourceFrameRate = 8000;
        } else if (sourceCodec.equals(ALAW.toString())) {
            sourcePayloadId = 8;
        } else if (sourceCodec.equals(ULAW.toString())) {
            sourcePayloadId = 0;
        } else if (sourceCodec.equals(PCM_FLOAT.toString())) {
            sourcePayloadId = 100;
        } else {
            sourcePayloadId = -1;
            logger.error("|Fail to get the source(speaker) audio codec({}). Program ends.", sourceCodec);
        }*/
    }

    private void setTargetDataLine(TargetDataLine targetDataLine) {
        this.targetDataLine = targetDataLine;

        targetAudioFormat = this.targetDataLine.getFormat();
        targetCodec = this.targetDataLine.getFormat().getEncoding().toString();
        targetSamplingRate = String.valueOf(this.targetDataLine.getFormat().getSampleRate());
        targetSampleSize = this.targetDataLine.getFormat().getSampleSizeInBits();
        targetFrameSize = this.targetDataLine.getFormat().getFrameSize();
        targetFrameRate = this.targetDataLine.getFormat().getFrameRate();
        targetChannelSize = this.targetDataLine.getFormat().getChannels();
        targetIsBigEndian = this.targetDataLine.getFormat().isBigEndian();

        /*if (targetCodec.equals(PCM_SIGNED.toString()) || targetCodec.equals(PCM_UNSIGNED.toString())) {
            targetCodec = MediaManager.getInstance().getPriorityCodec();
            if (targetCodec.equals(ALAW.toString())) {
                targetPayloadId = 8;
            } else if (targetCodec.equals(ULAW.toString())) {
                targetPayloadId = 0;
            }
            targetSamplingRate = "8000";
            targetFrameRate = 8000;
        } else if (targetCodec.equals(ALAW.toString())) {
            targetPayloadId = 8;
        } else if (targetCodec.equals(ULAW.toString())) {
            targetPayloadId = 0;
        } else if (targetCodec.equals(PCM_FLOAT.toString())) {
            targetPayloadId = 100;
        } else {
            targetPayloadId = -1;
            logger.error("|Fail to get the target(mike) audio codec({}). Program ends.", targetCodec);
        }*/
    }

    ////////////////////////////////////////////////////////////////////////////////

    public void startSpeaker(SourceDataLine sourceDataLine) {
        try {
            if (sourceDataLine == null) {
                logger.debug("Fail to initiate speaker.");
                return;
            } else {
                setSourceDataLine(sourceDataLine);

                logger.debug("Selected source line: lineInfo={}, audioFormat={}, supportedControls={}",
                        sourceDataLine.getLineInfo(),
                        sourceDataLine.getFormat(),
                        sourceDataLine.getControls()
                );

                if (speakerScheduledFuture == null) {
                    udpReceiver = new UdpReceiver(
                            speakerBuffer,
                            1
                    );

                    speakerScheduledFuture = speakerExecutor.scheduleAtFixedRate(
                            udpReceiver,
                            1000,
                            udpReceiver.getInterval(),
                            TimeUnit.MILLISECONDS
                    );
                    udpReceiver.start();
                }
            }

            sourceDataLine.open();
            sourceDataLine.start();
            logger.debug("Started speaker.");
        } catch (Exception e) {
            logger.warn("SoundHandler.LineUnavailableException", e);
        }
    }

    public void startMike(TargetDataLine targetDataLine) {
        try {
            if (targetDataLine == null) {
                logger.debug("Fail to initiate mike.");
                return;
            } else {
                setTargetDataLine(targetDataLine);

                logger.debug("Selected target line: lineInfo={}, audioFormat={}, supportedControls={}",
                        targetDataLine.getLineInfo(),
                        targetDataLine.getFormat(),
                        targetDataLine.getControls()
                );
            }

            targetDataLine.open();
            targetDataLine.start();
        } catch (Exception e) {
            logger.warn("SoundHandler.LineUnavailableException", e);
            return;
        }

        try {
            AudioInputStream stream = new AudioInputStream(targetDataLine);
            NettyChannel nettyChannel = NettyChannelManager.getInstance().getClientChannel();
            if (nettyChannel != null) {
                if (pcmGenerator == null) {
                    pcmGenerator = new PcmGenerator(
                            mikeBuffer,
                            stream,
                            1
                    );
                }

                if (pcmScheduledFuture == null) {
                    pcmScheduledFuture = pcmExecutor.scheduleAtFixedRate(
                            pcmGenerator,
                            1000,
                            pcmGenerator.getInterval(),
                            TimeUnit.MILLISECONDS
                    );
                }

                if (udpSender == null) {
                    udpSender = new UdpSender(
                            mikeBuffer,
                            nettyChannel,
                            1
                    );
                }
                udpSender.start();

                if (mikeScheduledFuture == null) {
                    mikeScheduledFuture = mikeExecutor.scheduleAtFixedRate(
                            udpSender,
                            1000,
                            udpSender.getInterval(),
                            TimeUnit.MILLISECONDS
                    );
                }

                logger.debug("Started mike.");
            } else {
                logger.warn("Fail to start mike.");
            }
        } catch (Exception e) {
            logger.warn("SoundHandler.startMike.Exception", e);
        }
    }

    public void stopSpeaker() {
        if (udpReceiver != null) {
            udpReceiver.stop();
            udpReceiver = null;
        }

        if (speakerScheduledFuture != null) {
            speakerScheduledFuture.cancel(true);
            speakerScheduledFuture = null;
        }

        speakerBuffer.resetBuffer();

        if (sourceDataLine != null) {
            sourceDataLine.stop();
            sourceDataLine.flush();
            sourceDataLine = null;
        }

        logger.debug("Stopped speaker.");
    }

    public void stopMike() {
        if (pcmScheduledFuture != null) {
            pcmScheduledFuture.cancel(true);
            pcmGenerator = null;
            pcmScheduledFuture = null;
        }

        if (udpSender != null) {
            udpSender.stop();
            udpSender = null;
        }

        if (mikeScheduledFuture != null) {
            mikeScheduledFuture.cancel(true);
            mikeScheduledFuture = null;
        }

        mikeBuffer.clear();

        if (targetDataLine != null) {
            targetDataLine.stop();
            targetDataLine.flush();
            targetDataLine = null;
        }

        logger.debug("Stopped mike.");
    }

    public void muteMikeOn() {
        if (udpSender != null) {
            udpSender.setMute(true);
        }
    }

    public void muteMikeOff() {
        if (udpSender != null) {
            udpSender.setMute(false);
        }
    }

    public void muteSpeakerOn() {
        if (udpReceiver != null) {
            udpReceiver.setMute(true);
        }
    }

    public void muteSpeakerOff() {
        if (udpReceiver != null) {
            udpReceiver.setMute(false);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

    public AudioBuffer getSpeakerBuffer() {
        return speakerBuffer;
    }

    public SourceDataLine getSourceDataLine() {
        return sourceDataLine;
    }

    public AudioFormat getSourceAudioFormat() {
        return sourceAudioFormat;
    }

    public String getSourceCodec() {
        return sourceCodec;
    }

    public String getSourceSamplingRate() {
        return sourceSamplingRate;
    }

    public int getSourceSampleSize() {
        return sourceSampleSize;
    }

    public int getSourceFrameSize() {
        return sourceFrameSize;
    }

    public float getSourceFrameRate() {
        return sourceFrameRate;
    }

    public int getSourceChannelSize() {
        return sourceChannelSize;
    }

    public boolean isSourceIsBigEndian() {
        return sourceIsBigEndian;
    }

    public int getSourcePayloadId() {
        return sourcePayloadId;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public TargetDataLine getTargetDataLine() {
        return targetDataLine;
    }

    public AudioFormat getTargetAudioFormat() {
        return targetAudioFormat;
    }

    public String getTargetCodec() {
        return targetCodec;
    }

    public String getTargetSamplingRate() {
        return targetSamplingRate;
    }

    public int getTargetSampleSize() {
        return targetSampleSize;
    }

    public int getTargetFrameSize() {
        return targetFrameSize;
    }

    public float getTargetFrameRate() {
        return targetFrameRate;
    }

    public int getTargetChannelSize() {
        return targetChannelSize;
    }

    public boolean isTargetIsBigEndian() {
        return targetIsBigEndian;
    }

    public int getTargetPayloadId() {
        return targetPayloadId;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public Map<String, Line> getTargetLineMap() {
        return targetLineMap;
    }

    public Map<String, Line> getSourceLineMap() {
        return sourceLineMap;
    }

    public UdpSender getUdpSender() {
        return udpSender;
    }

    public UdpReceiver getUdpReceiver() {
        return udpReceiver;
    }
}
