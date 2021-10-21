package signal;

import config.ConfigManager;
import media.sdp.base.Sdp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AppInstance;
import signal.module.NonceGenerator;
import signal.module.SipUtil;

import javax.sip.message.Response;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @class public class SignalManager implements SipListener
 * @brief SignalManager Class
 */
public class SignalManager {

    private static final Logger logger = LoggerFactory.getLogger(SignalManager.class);

    private static SignalManager signalManager = null;

    private final AtomicBoolean isStarted = new AtomicBoolean(false);
    private final SipUtil sipUtil;
    private Sdp localSdp;

    //////////////////////////////////////////////////////////////////////

    public SignalManager() {
        sipUtil = new SipUtil();
    }

    public static SignalManager getInstance () {
        if (signalManager == null) {
            signalManager = new SignalManager();
        }

        return signalManager;
    }

    //////////////////////////////////////////////////////////////////////

    public void init () {
        sipUtil.init();

        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        localSdp = configManager.loadSdpConfig("LOCAL");
    }

    public void start () {
        try {
            init();
            sipUtil.start();
            isStarted.set(true);
        } catch (Exception e) {
            logger.warn("Fail to start the sip stack.");
        }
    }

    public void stop () {
        try {
            if (isStarted.get()) {
                sipUtil.stop();
                isStarted.set(false);
            }
        } catch (Exception e) {
            logger.warn("Fail to stop the sip stack.");
        }
    }

    /////////////////////////////////////////////////////////////////////
    // REGISTER

    /**
     * @fn public void sendRegister ()
     * @brief REGISTER Method 를 보내는 함수
     */
    public void sendRegister(boolean isRecv401, Response response401) {
        sipUtil.sendRegister(isRecv401, response401);
    }

    /////////////////////////////////////////////////////////////////////
    // INVITE

    public void sendInvite(String fromHostName, String toHostName, String toIp, int toPort) {
        sipUtil.sendInvite(NonceGenerator.createRandomNonce(), fromHostName, toHostName, toIp, toPort);
    }

    public void sendInviteOk(String callId) {
        sipUtil.sendInviteOk(callId);
    }

    /////////////////////////////////////////////////////////////////////
    // CANCEL

    public void sendCancel(String callId, String toHostName, String toIp, int toPort) {
        sipUtil.sendCancel(callId, toHostName, toIp, toPort);
    }

    /////////////////////////////////////////////////////////////////////
    // BYE

    public void sendBye(String callId, String toHostName, String toIp, int toPort) {
        sipUtil.sendBye(callId, toHostName, toIp, toPort);
    }

    /////////////////////////////////////////////////////////////////////

    public String getToIp() {
        return sipUtil.getToIp();
    }

    public int getToPort() {
        return sipUtil.getToPort();
    }

    public Sdp getLocalSdp() {
        return localSdp;
    }

}
