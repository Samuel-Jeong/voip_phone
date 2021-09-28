package service;

import client.VoipClient;
import client.gui.FrameManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.base.TaskUnit;
import signal.SignalManager;
import signal.base.CallInfo;
import signal.module.CallManager;

/**
 * @author jamesj
 * @class public class CallCancelHandler extends TaskUnit
 * @brief CallCancelHandler
 */
public class CallCancelHandler extends TaskUnit {

    private static final Logger logger = LoggerFactory.getLogger(CallCancelHandler.class);

    private final String callId;
    private final String toHostName;
    private final String toIp;
    private final int toPort;

    ////////////////////////////////////////////////////////////////////////////////

    public CallCancelHandler(String callId, String toHostName, String toIp, int toPort, int interval) {
        super(interval);

        this.callId = callId;
        this.toHostName = toHostName;
        this.toIp = toIp;
        this.toPort = toPort;
    }

    ////////////////////////////////////////////////////////////////////////////////

    @Override
    public void run () {
        CallInfo callInfo = CallManager.getInstance().getCallInfo(callId);
        if (callInfo == null) {
            return;
        }

        if (callInfo.getIsCallStarted() && !callInfo.getIsInviteAccepted()) {
            FrameManager.getInstance().processByeToFrame(ServiceManager.CLIENT_FRAME_NAME);

            SignalManager.getInstance().sendCancel(callId, toHostName, toIp, toPort);

            VoipClient voipClient = VoipClient.getInstance();
            logger.debug("Cancel to [{}]", voipClient.getRemoteHostName());
        }

        TaskManager.getInstance().removeTask(CallCancelHandler.class.getSimpleName() + callId);
    }

}
