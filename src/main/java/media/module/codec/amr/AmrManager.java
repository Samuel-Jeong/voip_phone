package media.module.codec.amr;

import client.gui.FrameManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @class public class AmrManager
 * @brief AmrManager class
 */
public class AmrManager {

    private static final Logger logger = LoggerFactory.getLogger(AmrManager.class);

    private static AmrManager amrManager = null;

    ////////////////////////////////////////////////////////////////////////////////

    public AmrManager() {
        String curUserDir = System.getProperty("user.dir");
        curUserDir += "/src/main/resources/lib/amr/libamrjni.so";

        try {
            System.load(curUserDir);
        } catch (Exception e) {
            logger.warn("Fail to load the amr library. (path={})", curUserDir);
            FrameManager.getInstance().popUpErrorMsg("Fail to load the amr library. (path=" + curUserDir + ")");
            System.exit(1);
        }
    }

    public static AmrManager getInstance () {
        if (amrManager == null) {
            amrManager = new AmrManager();

        }
        return amrManager;
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
