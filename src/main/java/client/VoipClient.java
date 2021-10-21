package client;

import client.module.SoundHandler;
import config.ConfigManager;
import media.MediaManager;
import media.dtmf.base.DtmfUnit;
import media.module.mixing.base.AudioFrame;
import media.record.RecordManager;
import media.record.wav.WavFile;
import media.record.wav.WavFileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AppInstance;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Line;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @class public class VoipClient
 * @brief Voip Client Class
 */
public class VoipClient {

    private static final Logger logger = LoggerFactory.getLogger(VoipClient.class);

    private static VoipClient voipClient = null;

    private final SoundHandler soundHandler;
    private SourceDataLine speaker = null;
    private TargetDataLine mike = null;

    private String remoteHostName = null;
    private String proxyHostName = null;

    private final AtomicBoolean isMikeOn = new AtomicBoolean(false);
    private final AtomicBoolean isSpeakerOn = new AtomicBoolean(false);

    private RecordManager targetPcmRecordManager = null; // 8000
    private RecordManager targetEvsRecordManager = null; // 8000
    private RecordManager targetALawRecordManager = null; // 8000
    private RecordManager targetULawRecordManager = null; // 8000
    private RecordManager targetAmrNbRecordManager = null; // 8000
    private RecordManager targetAmrWbRecordManager = null; // 16000

    private RecordManager sourcePcmRecordManager = null; // 8000
    private RecordManager sourceEvsRecordManager = null; // 8000
    private RecordManager sourceALawRecordManager = null; // 8000
    private RecordManager sourceULawRecordManager = null; // 8000
    private RecordManager sourceAmrNbRecordManager = null; // 8000
    private RecordManager sourceAmrWbRecordManager = null; // 16000

    private String[] evsEncArgv = null;
    private String[] evsDecArgv = null;

    private String wavFilePath = null;
    private WavFile wavFile = null;

    ////////////////////////////////////////////////////////////////////////////////

    public VoipClient() {
        soundHandler = new SoundHandler();
    }

    public static VoipClient getInstance ( ) {
        if (voipClient == null) {
            voipClient = new VoipClient();
        }

        return voipClient;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public RecordManager getTargetEvsRecordManager() {
        return targetEvsRecordManager;
    }

    public RecordManager getSourceEvsRecordManager() {
        return sourceEvsRecordManager;
    }

    public String[] getEvsEncArgv() {
        return evsEncArgv;
    }

    public String[] getEvsDecArgv() {
        return evsDecArgv;
    }

    public RecordManager getSourcePcmRecordManager() {
        return sourcePcmRecordManager;
    }

    public RecordManager getSourceALawRecordManager() {
        return sourceALawRecordManager;
    }

    public RecordManager getSourceULawRecordManager() {
        return sourceULawRecordManager;
    }

    public RecordManager getTargetPcmRecordManager() {
        return targetPcmRecordManager;
    }

    public RecordManager getTargetALawRecordManager() {
        return targetALawRecordManager;
    }

    public RecordManager getTargetULawRecordManager() {
        return targetULawRecordManager;
    }

    public RecordManager getTargetAmrNbRecordManager() {
        return targetAmrNbRecordManager;
    }

    public RecordManager getTargetAmrWbRecordManager() {
        return targetAmrWbRecordManager;
    }

    public RecordManager getSourceAmrNbRecordManager() {
        return sourceAmrNbRecordManager;
    }

    public RecordManager getSourceAmrWbRecordManager() {
        return sourceAmrWbRecordManager;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public void initSourceRecordEnv() {
        Date curTime = new Date();
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyyMMddHH");
        String curTimeStr = timeFormat.format(curTime);

        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        if (configManager.isRawFile()) {
            if (sourcePcmRecordManager == null) {
                sourcePcmRecordManager = new RecordManager(
                        configManager.getRecordPath(),
                        //20
                        MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_WB)? 40 : 20
                );
                sourcePcmRecordManager.start();

                sourcePcmRecordManager.openFileStream(
                        configManager.getHostName() + "_" +
                                VoipClient.getInstance().getRemoteHostName() + "_" +
                                curTimeStr +
                                "_recv.wav",
                        true,
                        true
                );
                sourcePcmRecordManager.writeFileStream(
                        WavFileInfo.getHeader(
                                (short) voipClient.getSourceAudioFormat().getChannels(),
                                //(int) voipClient.getSourceAudioFormat().getSampleRate(),
                                MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_WB)? 16000 : 8000,
                                VoipClient.getInstance().getSourceSampleSize()
                        )
                );
                logger.debug("VoipClient: sourcePcmRecordManager is initiated. (path={}, channels={}, samplingRate={}, sampleSize={})",
                        sourcePcmRecordManager.getFullFilePath(),
                        (short) voipClient.getSourceAudioFormat().getChannels(),
                        //(int) voipClient.getSourceAudioFormat().getSampleRate(),
                        MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_WB)? 16000 : 8000,
                        VoipClient.getInstance().getSourceSampleSize()
                );
            }
        }

        if (configManager.isDecFile()) {
            if (MediaManager.getInstance().getPriorityCodec().equals(AudioFormat.Encoding.ALAW.toString())) {
                if (sourceALawRecordManager == null) {
                    sourceALawRecordManager = new RecordManager(configManager.getRecordPath(), 20);
                    sourceALawRecordManager.start();

                    sourceALawRecordManager.openFileStream(
                            configManager.getHostName() + "_" +
                                    VoipClient.getInstance().getRemoteHostName() + "_" +
                                    curTimeStr +
                                    "_recv.alaw",
                            true,
                            true
                    );
                    logger.debug("VoipClient: sourceALawRecordManager is initiated. (path={})", sourceALawRecordManager.getFullFilePath());
                }
            } else if (MediaManager.getInstance().getPriorityCodec().equals(AudioFormat.Encoding.ULAW.toString())) {
                if (sourceULawRecordManager == null) {
                    sourceULawRecordManager = new RecordManager(configManager.getRecordPath(), 20);
                    sourceULawRecordManager.start();

                    sourceULawRecordManager.openFileStream(
                            configManager.getHostName() + "_" +
                                    VoipClient.getInstance().getRemoteHostName() + "_" +
                                    curTimeStr +
                                    "_recv.ulaw",
                            true,
                            true
                    );
                    logger.debug("VoipClient: sourceULawRecordManager is initiated. (path={})", sourceULawRecordManager.getFullFilePath());
                }
            } else if (MediaManager.getInstance().getPriorityCodec().equals(MediaManager.EVS)) {
                if (sourceEvsRecordManager == null) {
                    sourceEvsRecordManager = new RecordManager(configManager.getRecordPath(), 20);
                    sourceEvsRecordManager.start();

                    sourceEvsRecordManager.openFileStream(
                            configManager.getHostName() + "_" +
                                    VoipClient.getInstance().getRemoteHostName() + "_" +
                                    curTimeStr +
                                    "_recv.evs",
                            true,
                            true
                    );
                    logger.debug("VoipClient: sourceEvsRecordManager is initiated. (path={})", sourceEvsRecordManager.getFullFilePath());
                }

                evsDecArgv = new String[]{
                        "EVS_dec.exe",
                        //"-q",
                        //"-VOIP",
                        //"-NO_DELAY_CMP",
                        "8",
                        //String.valueOf(voipClient.getTargetAudioFormat().getSampleRate() / 1000),
                        "none",
                        "none"
                };
            } else if (MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_NB)) {
                if (sourceAmrNbRecordManager == null) {
                    sourceAmrNbRecordManager = new RecordManager(configManager.getRecordPath(), 20);
                    sourceAmrNbRecordManager.start();

                    sourceAmrNbRecordManager.openFileStream(
                            configManager.getHostName() + "_" +
                                    VoipClient.getInstance().getRemoteHostName() + "_" +
                                    curTimeStr +
                                    "_recv.amrnb",
                            true,
                            true
                    );
                    logger.debug("VoipClient: sourceAmrNbRecordManager is initiated. (path={})", sourceAmrNbRecordManager.getFullFilePath());
                }
            } else if (MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_WB)) {
                if (sourceAmrWbRecordManager == null) {
                    sourceAmrWbRecordManager = new RecordManager(configManager.getRecordPath(), 40);
                    sourceAmrWbRecordManager.start();

                    sourceAmrWbRecordManager.openFileStream(
                            configManager.getHostName() + "_" +
                                    VoipClient.getInstance().getRemoteHostName() + "_" +
                                    curTimeStr +
                                    "_recv.amrwb",
                            true,
                            true
                    );
                    logger.debug("VoipClient: sourceAmrWbRecordManager is initiated. (path={})", sourceAmrWbRecordManager.getFullFilePath());
                }
            }
        }
    }

    public void finishSourceRecordEnv() {
        if (sourcePcmRecordManager != null) {
            sourcePcmRecordManager.stop();
            sourcePcmRecordManager.closeFileStream();
            WavFileInfo.setChunkSizeInFile(
                    sourcePcmRecordManager.getFullFilePath(),
                    sourcePcmRecordManager.getTotalDataSize() * getSourceChannelSize() * getSourceSampleSize() / 8
            );
            logger.debug("VoipClient: sourcePcmRecordManager is finished. (path={})", sourcePcmRecordManager.getFullFilePath());
            sourcePcmRecordManager = null;
        }

        if (sourceEvsRecordManager != null) {
            sourceEvsRecordManager.stop();
            sourceEvsRecordManager.closeFileStream();
            logger.debug("VoipClient: sourceEvsRecordManager is finished. (path={})", sourceEvsRecordManager.getFullFilePath());
            sourceEvsRecordManager = null;
        }

        if (sourceALawRecordManager != null) {
            sourceALawRecordManager.stop();
            sourceALawRecordManager.closeFileStream();
            logger.debug("VoipClient: sourceALawRecordManager is finished. (path={})", sourceALawRecordManager.getFullFilePath());
            sourceALawRecordManager = null;
        }

        if (sourceULawRecordManager != null) {
            sourceULawRecordManager.stop();
            sourceULawRecordManager.closeFileStream();
            logger.debug("VoipClient: sourceULawRecordManager is finished. (path={})", sourceULawRecordManager.getFullFilePath());
            sourceULawRecordManager = null;
        }

        if (sourceAmrNbRecordManager != null) {
            sourceAmrNbRecordManager.stop();
            sourceAmrNbRecordManager.closeFileStream();
            logger.debug("VoipClient: sourceAmrNbRecordManager is finished. (path={})", sourceAmrNbRecordManager.getFullFilePath());
            sourceAmrNbRecordManager = null;
        }

        if (sourceAmrWbRecordManager != null) {
            sourceAmrWbRecordManager.stop();
            sourceAmrWbRecordManager.closeFileStream();
            logger.debug("VoipClient: sourceAmrWbRecordManager is finished. (path={})", sourceAmrWbRecordManager.getFullFilePath());
            sourceAmrWbRecordManager = null;
        }
    }

    public void initTargetRecordEnv() {
        Date curTime = new Date();
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyyMMddHH");
        String curTimeStr = timeFormat.format(curTime);

        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        if (configManager.isRawFile()) {
            if (targetPcmRecordManager == null) {
                targetPcmRecordManager = new RecordManager(
                        configManager.getRecordPath(),
                        //20
                        MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_WB)? 40 : 20
                );
                targetPcmRecordManager.start();

                targetPcmRecordManager.openFileStream(
                        configManager.getHostName() + "_" +
                                VoipClient.getInstance().getRemoteHostName() + "_" +
                                curTimeStr +
                                "_send.wav",
                        true,
                        true
                );
                targetPcmRecordManager.writeFileStream(
                        WavFileInfo.getHeader(
                                (short) voipClient.getTargetAudioFormat().getChannels(),
                                //(int) voipClient.getTargetAudioFormat().getSampleRate(),
                                MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_WB)? 16000 : 8000,
                                VoipClient.getInstance().getTargetSampleSize()
                        )
                );
                logger.debug("VoipClient: targetPcmRecordManager is initiated. (path={}, channels={}, samplingRate={}, sampleSize={})",
                        targetPcmRecordManager.getFullFilePath(),
                        (short) voipClient.getTargetAudioFormat().getChannels(),
                        //(int) voipClient.getTargetAudioFormat().getSampleRate(),
                        MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_WB)? 16000 : 8000,
                        VoipClient.getInstance().getTargetSampleSize()
                );
            }
        }

        if (configManager.isEncFile()) {
            if (MediaManager.getInstance().getPriorityCodec().equals(AudioFormat.Encoding.ALAW.toString())) {
                if (targetALawRecordManager == null) {
                    targetALawRecordManager = new RecordManager(configManager.getRecordPath(), 20);
                    targetALawRecordManager.start();

                    targetALawRecordManager.openFileStream(
                            configManager.getHostName() + "_" +
                                    VoipClient.getInstance().getRemoteHostName() + "_" +
                                    curTimeStr +
                                    "_send.alaw",
                            true,
                            true
                    );
                    logger.debug("VoipClient: targetALawRecordManager is initiated. (path={})", targetALawRecordManager.getFullFilePath());
                }
            } else if (MediaManager.getInstance().getPriorityCodec().equals(AudioFormat.Encoding.ULAW.toString())) {
                if (targetULawRecordManager == null) {
                    targetULawRecordManager = new RecordManager(configManager.getRecordPath(), 20);
                    targetULawRecordManager.start();

                    targetULawRecordManager.openFileStream(
                            configManager.getHostName() + "_" +
                                    VoipClient.getInstance().getRemoteHostName() + "_" +
                                    curTimeStr +
                                    "_send.ulaw",
                            true,
                            true
                    );
                    logger.debug("VoipClient: targetULawRecordManager is initiated. (path={})", targetULawRecordManager.getFullFilePath());
                }
            } else if (MediaManager.getInstance().getPriorityCodec().equals(MediaManager.EVS)) {
                if (targetEvsRecordManager == null) {
                    targetEvsRecordManager = new RecordManager(configManager.getRecordPath(), 20);
                    targetEvsRecordManager.start();

                    targetEvsRecordManager.openFileStream(
                            configManager.getHostName() + "_" +
                                    VoipClient.getInstance().getRemoteHostName() + "_" +
                                    curTimeStr +
                                    "_send.evs",
                            true,
                            true
                    );
                    logger.debug("VoipClient: targetEvsRecordManager is initiated. (path={})", targetEvsRecordManager.getFullFilePath());
                }

                evsEncArgv = new String[]{
                        "EVS_enc.exe",
                        //"-q",
                        //"-max_band",
                        //"WB",
                        //"-NO_DELAY_CMP",
                        "8000",
                        "8",
                        //String.valueOf(voipClient.getTargetAudioFormat().getSampleRate()),
                        //String.valueOf(voipClient.getTargetAudioFormat().getSampleRate() / 1000),
                        "none",
                        "none"
                };
            } else if (MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_NB)) {
                if (targetAmrNbRecordManager == null) {
                    targetAmrNbRecordManager = new RecordManager(configManager.getRecordPath(), 20);
                    targetAmrNbRecordManager.start();

                    targetAmrNbRecordManager.openFileStream(
                            configManager.getHostName() + "_" +
                                    VoipClient.getInstance().getRemoteHostName() + "_" +
                                    curTimeStr +
                                    "_send.amrnb",
                            true,
                            true
                    );
                    logger.debug("VoipClient: targetAmrNbRecordManager is initiated. (path={})", targetAmrNbRecordManager.getFullFilePath());
                }
            } else if (MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_WB)) {
                if (targetAmrWbRecordManager == null) {
                    targetAmrWbRecordManager = new RecordManager(configManager.getRecordPath(), 40);
                    targetAmrWbRecordManager.start();

                    targetAmrWbRecordManager.openFileStream(
                            configManager.getHostName() + "_" +
                                    VoipClient.getInstance().getRemoteHostName() + "_" +
                                    curTimeStr +
                                    "_send.amrwb",
                            true,
                            true
                    );
                    logger.debug("VoipClient: targetAmrWbRecordManager is initiated. (path={})", targetAmrWbRecordManager.getFullFilePath());
                }
            }
        }
    }

    public void finishTargetRecordEnv() {
        if (targetPcmRecordManager != null) {
            targetPcmRecordManager.stop();
            targetPcmRecordManager.closeFileStream();
            WavFileInfo.setChunkSizeInFile(
                    targetPcmRecordManager.getFullFilePath(),
                    targetPcmRecordManager.getTotalDataSize() * getTargetChannelSize() * getTargetSampleSize() / 8
            );
            logger.debug("targetPcmRecordManager.getTotalDataSize(): {}", targetPcmRecordManager.getTotalDataSize());
            logger.debug("VoipClient: targetPcmRecordManager is finished. (path={})", targetPcmRecordManager.getFullFilePath());
            targetPcmRecordManager = null;
        }

        if (targetEvsRecordManager != null) {
            targetEvsRecordManager.stop();
            targetEvsRecordManager.closeFileStream();
            logger.debug("VoipClient: targetEvsRecordManager is finished. (path={})", targetEvsRecordManager.getFullFilePath());
            targetEvsRecordManager = null;
        }

        if (targetALawRecordManager != null) {
            targetALawRecordManager.stop();
            targetALawRecordManager.closeFileStream();
            logger.debug("VoipClient: targetALawRecordManager is finished. (path={})", targetALawRecordManager.getFullFilePath());
            targetALawRecordManager = null;
        }

        if (targetULawRecordManager != null) {
            targetULawRecordManager.stop();
            targetULawRecordManager.closeFileStream();
            logger.debug("VoipClient: targetULawRecordManager is finished. (path={})", targetULawRecordManager.getFullFilePath());
            targetULawRecordManager = null;
        }

        if (targetAmrNbRecordManager != null) {
            targetAmrNbRecordManager.stop();
            targetAmrNbRecordManager.closeFileStream();
            logger.debug("VoipClient: targetAmrNbRecordManager is finished. (path={})", targetAmrNbRecordManager.getFullFilePath());
            targetAmrNbRecordManager = null;
        }

        if (targetAmrWbRecordManager != null) {
            targetAmrWbRecordManager.stop();
            targetAmrWbRecordManager.closeFileStream();
            logger.debug("VoipClient: targetAmrWbRecordManager is finished. (path={})", targetAmrWbRecordManager.getFullFilePath());
            targetAmrWbRecordManager = null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

    public void init() {
        soundHandler.initSound();
    }

    ////////////////////////////////////////////////////////////////////////////////

    public void openMike() {
        soundHandler.startMike(mike);
        initTargetRecordEnv();
        isMikeOn.set(true);
    }

    public void openSpeaker() {
        soundHandler.startSpeaker(speaker);
        initSourceRecordEnv();
        isSpeakerOn.set(true);
    }

    public void closeMike() {
        finishTargetRecordEnv();
        soundHandler.stopMike();
        isMikeOn.set(false);
    }

    public void closeSpeaker() {
        finishSourceRecordEnv();
        soundHandler.stopSpeaker();
        isSpeakerOn.set(false);
    }

    public void muteMikeOn() {
        soundHandler.muteMikeOn();
    }

    public void muteMikeOff() {
        soundHandler.muteMikeOff();
    }

    public void muteSpeakerOn() {
        soundHandler.muteSpeakerOn();
    }

    public void muteSpeakerOff() {
        soundHandler.muteSpeakerOff();
    }


    ////////////////////////////////////////////////////////////////////////////////

    public void start () {
        if (wavFilePath != null && wavFile == null) {
            try {
                wavFile = new WavFile(new File(wavFilePath));
                wavFile.open();
                logger.debug("WavFile is opened. ({})", wavFile);
            } catch (Exception e) {
                logger.warn("VoipClient.start.Exception", e);
            }
        }

        if (!isStarted()) {
            openMike();
            openSpeaker();
        }
    }

    public void stop () {
        if (isStarted()) {
            closeMike();
            closeSpeaker();
        }

        try {
            if (wavFile != null) {
                wavFile.close();
                logger.debug("WavFile is closed. ({})", wavFile);
                wavFile = null;
            }
        } catch (Exception e) {
            logger.warn("VoipClient.stop.Exception", e);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

    public boolean isStarted() {
        return isMikeOn.get() && isSpeakerOn.get();
    }

    ////////////////////////////////////////////////////////////////////////////////

    public String getRemoteHostName() {
        return remoteHostName;
    }

    public void setRemoteHostName(String remoteHostName) {
        this.remoteHostName = remoteHostName;
    }

    public String getProxyHostName() {
        return proxyHostName;
    }

    public void setProxyHostName(String proxyHostName) {
        this.proxyHostName = proxyHostName;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public void writeToSpeaker(byte[] data, int payloadType) {
        AudioFrame audioFrame = new AudioFrame(payloadType == DtmfUnit.DTMF_TYPE);
        audioFrame.setData(data, true);
        soundHandler.getSpeakerBuffer().offer(audioFrame);
    }

    public SoundHandler getSoundHandler() {
        return soundHandler;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public SourceDataLine getSourceLine() {
        return soundHandler.getSourceDataLine();
    }

    public TargetDataLine getTargetLine() {
        return soundHandler.getTargetDataLine();
    }

    public AudioFormat getSourceAudioFormat() {
        return soundHandler.getSourceAudioFormat();
    }

    public String getSourceCodec() {
        return soundHandler.getSourceCodec();
    }

    public String getSourceSamplingRate() {
        return soundHandler.getSourceSamplingRate();
    }

    public int getSourceSampleSize() {
        return soundHandler.getSourceSampleSize();
    }

    public int getSourceFrameSize() {
        return soundHandler.getSourceFrameSize();
    }

    public float getSourceFrameRate() {
        return soundHandler.getSourceFrameRate();
    }

    public int getSourceChannelSize() {
        return soundHandler.getSourceChannelSize();
    }

    public boolean isSourceBigEndian() {
        return soundHandler.isSourceIsBigEndian();
    }

    public int getSourcePayloadId() {
        return soundHandler.getSourcePayloadId();
    }

    ////////////////////////////////////////////////////////////////////////////////

    public AudioFormat getTargetAudioFormat() {
        return soundHandler.getTargetAudioFormat();
    }

    public String getTargetCodec() {
        return soundHandler.getTargetCodec();
    }

    public String getTargetSamplingRate() {
        return soundHandler.getTargetSamplingRate();
    }

    public int getTargetSampleSize() {
        return soundHandler.getTargetSampleSize();
    }

    public int getTargetFrameSize() {
        return soundHandler.getTargetFrameSize();
    }

    public float getTargetFrameRate() {
        return soundHandler.getTargetFrameRate();
    }

    public int getTargetChannelSize() {
        return soundHandler.getTargetChannelSize();
    }

    public boolean isTargetBigEndian() {
        return soundHandler.isTargetIsBigEndian();
    }

    public int getTargetPayloadId() {
        return soundHandler.getTargetPayloadId();
    }

    public short getSourceVolume() {
        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        return (short) configManager.getSpeakerVolume();
    }

    public short getTargetVolume() {
        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        return (short) configManager.getMikeVolume();
    }

    ////////////////////////////////////////////////////////////////////////////////

    public Map<String, Line> getSpeakerLineMap() {
        return soundHandler.getSourceLineMap();
    }

    public Map<String, Line> getMikeLineMap() {
        return soundHandler.getTargetLineMap();
    }

    public void setSpeaker(Line line) {
        this.speaker = (SourceDataLine) line;
    }

    public Line getSpeaker() {
        return speaker;
    }

    public void setMike(Line line) {
        this.mike = (TargetDataLine) line;
    }

    public Line getMike() {
        return mike;
    }

    public String getWavFilePath() {
        return wavFilePath;
    }

    public void setWavFilePath(String wavFilePath) {
        this.wavFilePath = wavFilePath;
    }

    public WavFile getWavFile() {
        return wavFile;
    }

    public void setWavFile(WavFile wavFile) {
        this.wavFile = wavFile;
    }
}
