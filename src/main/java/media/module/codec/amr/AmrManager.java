package media.module.codec.amr;

import client.gui.FrameManager;
import config.ConfigManager;
import media.MediaManager;
import org.scijava.nativelib.NativeLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AppInstance;
import service.ServiceManager;

/**
 * @class public class AmrManager
 * @brief AmrManager class
 */
public class AmrManager {

    private static final Logger logger = LoggerFactory.getLogger(AmrManager.class);

    private static AmrManager amrManager = null;

    ////////////////////////////////////////////////////////////////////////////////

    public AmrManager() {
        // Nothing
    }

    public static AmrManager getInstance () {
        if (amrManager == null) {
            amrManager = new AmrManager();

        }
        return amrManager;
    }

    public void init() {
        /*String curUserDir = System.getProperty("user.dir");
        if (curUserDir.endsWith("bin")) {
            curUserDir = curUserDir.substring(0, curUserDir.lastIndexOf("bin") - 1);
        }

        if (SystemManager.getInstance().getOs().contains("win")) {
            curUserDir += "\\lib\\libamrjni.dll";
        } else {
            curUserDir += "/lib/libamrjni.so";
        }*/

        try {
            //FrameManager.getInstance().appendTextToFrame(ServiceManager.CLIENT_FRAME_NAME, "Loading... the amr library. (path=" + curUserDir + ")");
            FrameManager.getInstance().appendTextToFrame(ServiceManager.CLIENT_FRAME_NAME, "Loading... the amr library.\n");
            NativeLoader.loadLibrary("amrjni");
            //System.load(curUserDir);
            FrameManager.getInstance().appendTextToFrame(ServiceManager.CLIENT_FRAME_NAME, "Loaded the amr library.\n");
            //FrameManager.getInstance().appendTextToFrame(ServiceManager.CLIENT_FRAME_NAME, "Loaded the amr library. (path=" + curUserDir + ")");
        } catch (Exception e) {
            //logger.error("Fail to load the amr library. (path={})", curUserDir);
            FrameManager.getInstance().appendTextToFrame(ServiceManager.CLIENT_FRAME_NAME, "Fail to load the amr library.\n" + e.getMessage());
            //FrameManager.getInstance().popUpErrorMsg("Fail to load the amr library.\n" + e.getMessage());
            FrameManager.getInstance().popUpWarnMsgToFrame(ServiceManager.CLIENT_FRAME_NAME, "Fail to load the amr library.\n" + e.getMessage());

            String[] audioCodecStrArray = MediaManager.getInstance().getSupportedAudioCodecList();
            ConfigManager configManager = AppInstance.getInstance().getConfigManager();
            configManager.setPriorityAudioCodec(audioCodecStrArray[0]);
            configManager.setIniValue(ConfigManager.SECTION_MEDIA, ConfigManager.FIELD_PRIORITY_CODEC, audioCodecStrArray[0]);
            FrameManager.getInstance().selectPriorityCodec(ServiceManager.CLIENT_FRAME_NAME, audioCodecStrArray[0]);
            logger.debug("Priority audio codec option is changed. (before=[{}], after=[{}])", configManager.getPriorityAudioCodec(), audioCodecStrArray[0]);

            return;
        }

        FrameManager.getInstance().popUpInfoMsgToFrame(
                ServiceManager.CLIENT_FRAME_NAME,
                //"Success to load the amr library. (path=" + curUserDir + ")"
                "Success to load the amr library."
        );
    }

    ////////////////////////////////////////////////////////////////////////////////

    //
    public native byte[] enc_amrnb(int req_mode, byte[] src_data);
    public byte[] encAmrNb(int reqMode, byte[] srcData) {
        return enc_amrnb(reqMode, srcData);
    }

    public native void start_enc_amrnb();
    public void startEncAmrNb() {
        start_enc_amrnb();
    }

    public native void stop_enc_amrnb();
    public void stopEncAmrNb() {
        stop_enc_amrnb();
    }

    public native byte[] dec_amrnb(int dst_data_len, byte[] src_data);
    public byte[] decAmrNb(int dstDataLen, byte[] srcData) {
        return dec_amrnb(dstDataLen, srcData);
    }

    public native void start_dec_amrnb();
    public void startDecAmrNb() {
        start_dec_amrnb();
    }

    public native void stop_dec_amrnb();
    public void stopDecAmrNb() {
        stop_dec_amrnb();
    }
    //

    //
    public native byte[] enc_amrwb(int req_mode, byte[] src_data);
    public byte[] encAmrWb(int reqMode, byte[] srcData) {
        return enc_amrwb(reqMode, srcData);
    }

    public native void start_enc_amrwb();
    public void startEncAmrWb() {
        start_enc_amrwb();
    }

    public native void stop_enc_amrwb();
    public void stopEncAmrWb() {
        stop_enc_amrwb();
    }

    public native byte[] dec_amrwb(int dst_data_len, byte[] src_data);
    public byte[] decAmrWb(int dstDataLen, byte[] srcData) {
        return dec_amrwb(dstDataLen, srcData);
    }

    public native void start_dec_amrwb();
    public void startDecAmrWb() {
        start_dec_amrwb();
    }

    public native void stop_dec_amrwb();
    public void stopDecAmrWb() {
        stop_dec_amrwb();
    }
    //

}
