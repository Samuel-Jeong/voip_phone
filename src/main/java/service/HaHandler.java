package service;

import client.VoipClient;
import config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.base.TaskUnit;
import signal.module.CallManager;
import signal.module.GroupCallManager;
import signal.module.RegiManager;
import system.SystemManager;

/**
 * @author jamesj
 * @class public class HaHandler extends TaskUnit
 * @brief HaHandler
 */
public class HaHandler extends TaskUnit {

    private static final Logger logger = LoggerFactory.getLogger(HaHandler.class);

    ////////////////////////////////////////////////////////////////////////////////

    public HaHandler(int interval) {
        super(interval);
    }

    ////////////////////////////////////////////////////////////////////////////////

    @Override
    public void run ( ) {
        SystemManager systemManager = SystemManager.getInstance();
        ConfigManager configManager = AppInstance.getInstance().getConfigManager();

        String cpuUsageStr = systemManager.getCpuUsage();
        String memoryUsageStr = systemManager.getHeapMemoryUsage();

        if (configManager.isUseClient()) {
            logger.debug("[CLIENT ({})] Call=[{}] | cpu=[{}], mem=[{}]",
                    configManager.getHostName(),
                    CallManager.getInstance().getCallMapSize(),
                    cpuUsageStr,
                    memoryUsageStr
            );
        } else {
            logger.debug("[SERVER ({})] Regi=[{}], Room=[{}], Call=[{}] | cpu=[{}], mem=[{}]",
                    configManager.getHostName(),
                    RegiManager.getInstance().getRegiMapSize(),
                    GroupCallManager.getInstance().getRoomMapSize(),
                    CallManager.getInstance().getCallMapSize(),
                    cpuUsageStr,
                    memoryUsageStr
            );
        }
    }

}
