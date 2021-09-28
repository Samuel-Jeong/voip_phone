package service;

import client.VoipClient;
import config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.base.TaskUnit;
import signal.base.CallInfo;
import signal.module.CallManager;

import java.util.Map;

/**
 * @class public class LongCallRemover extends TaskUnit
 * @brief LongCallRemover Class
 */
public class LongCallRemover extends TaskUnit {

    private static final Logger logger = LoggerFactory.getLogger(LongCallRemover.class);

    private final long longCallTime;

    public LongCallRemover(int interval) {
        super(interval);

        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        longCallTime = configManager.getLongCallTime();
    }

    @Override
    public void run() {
        Map<String, CallInfo> callInfoMap = CallManager.getInstance().getCloneCallMap();
        if (!callInfoMap.isEmpty()) {
            for (Map.Entry<String, CallInfo> entry : callInfoMap.entrySet()) {
                CallInfo callInfo = entry.getValue();
                if (callInfo == null) {
                    continue;
                }

                long nowTime = System.currentTimeMillis();
                if ((nowTime - callInfo.getCreatedTime()) >= longCallTime) {
                    String callId = callInfo.getCallId();
                    CallManager.getInstance().deleteCallInfo(callId);

                    ConfigManager configManager = AppInstance.getInstance().getConfigManager();
                    if (configManager.isUseClient()) {
                        VoipClient.getInstance().stop();
                    }

                    logger.warn("Remove the long call. (callId={}, timeout={}(msec))", callId, longCallTime);
                }
            }
        }
    }

}
