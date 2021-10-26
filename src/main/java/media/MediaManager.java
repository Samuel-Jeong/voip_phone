package media;

import media.netty.NettyChannelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;

/**
 * @class public class MediaManager
 * @brief MediaManager class
 */
public class MediaManager {

    private static final Logger logger = LoggerFactory.getLogger(MediaManager.class);

    private static MediaManager mediaManager = null;
    private final NettyChannelManager nettyChannelManager;

    private int priorityCodecId = -1;
    private int priorityDtmfCodecId = 101;
    private String priorityCodec = null;
    private String priorityCodecSamplingRate = null;

    // AUDIO
    public static final String ALAW = AudioFormat.Encoding.ALAW.toString();
    public static final String ULAW = AudioFormat.Encoding.ULAW.toString();
    public static final String EVS = "EVS";
    public static final String AMR_NB = "AMR";
    public static final String AMR_WB = "AMR-WB";

    public static final int AMR_NB_MAX_MODE_SET = 7;
    public static final int AMR_WB_MAX_MODE_SET = 8;

    private final String[] supportedAudioCodecList = {
            ALAW,
            ULAW,
            EVS,
            AMR_NB,
            AMR_WB,
    };

    // VIDEO
    public static final String H264 = "H264";

    private final String[] supportedVideoCodecList = {
            H264
    };

    ////////////////////////////////////////////////////////////////////////////////

    public MediaManager() {
        nettyChannelManager = NettyChannelManager.getInstance();
    }

    public static MediaManager getInstance () {
        if (mediaManager == null) {
            mediaManager = new MediaManager();
        }

        return mediaManager;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public void start () {
        try {
            nettyChannelManager.start();
        } catch (Exception e) {
            logger.warn("Fail to start the sip stack.");
        }
    }

    public void stop () {
        try {
            nettyChannelManager.stop();
        } catch (Exception e) {
            logger.warn("Fail to start the sip stack.");
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

    public String[] getSupportedAudioCodecList() {
        return supportedAudioCodecList;
    }

    public String[] getSupportedVideoCodecList() {
        return supportedVideoCodecList;
    }

    public String getPriorityCodec() {
        return priorityCodec;
    }

    public int getPriorityCodecId() {
        return priorityCodecId;
    }

    public int getPriorityDtmfCodecId() {
        return priorityDtmfCodecId;
    }

    public String getPriorityCodecSamplingRate() {
        return priorityCodecSamplingRate;
    }

    public void setPriorityCodec(String priorityCodec) {
        int prevPriorityCodecId = priorityCodecId;
        String prevPriorityCodecSamplingRate = priorityCodecSamplingRate;

        if (priorityCodec != null) {
            for (String codec : supportedAudioCodecList) {
                if (priorityCodec.equals(codec) && codec.equals(MediaManager.ALAW)) {
                    priorityCodecId = 8;
                    priorityCodecSamplingRate = "8000";
                } else if (priorityCodec.equals(codec) && codec.equals(MediaManager.ULAW)) {
                    priorityCodecId = 0;
                    priorityCodecSamplingRate = "8000";
                } else if (priorityCodec.equals(codec) && codec.equals(MediaManager.EVS)) {
                    priorityCodecId = 98;
                    priorityCodecSamplingRate = "8000";
                } else if (priorityCodec.equals(codec) && codec.equals(MediaManager.AMR_NB)) {
                    priorityCodecId = 97;
                    priorityCodecSamplingRate = "8000";
                } else if (priorityCodec.equals(codec) && codec.equals(MediaManager.AMR_WB)) {
                    priorityCodecId = 96;
                    priorityDtmfCodecId = 102;
                    priorityCodecSamplingRate = "16000";
                } else if (priorityCodec.equals(codec) && codec.equals(MediaManager.H264)) {
                    priorityCodecId = 110;
                    priorityCodecSamplingRate = "90000";
                }
            }
        }

        logger.debug("MediaManager: priorityCodec({}/{}/{}) is changed to [{}/{}/{}].",
                this.priorityCodec, prevPriorityCodecId, prevPriorityCodecSamplingRate,
                priorityCodec, priorityCodecId, priorityCodecSamplingRate
        );
        this.priorityCodec = priorityCodec;
    }
}
