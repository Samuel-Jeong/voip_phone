import config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AppInstance;
import signal.SignalManager;

import java.util.Objects;

public class TestAppInstance {

    private static final Logger logger = LoggerFactory.getLogger(TestAppInstance.class);

    public TestAppInstance( ) {
        String configFile = "user_conf.ini";
        AppInstance instance = AppInstance.getInstance();

        if (instance.getConfigManager() == null) {
            logger.info("class root: {}", Objects.requireNonNull(this.getClass().getResource("/")).getPath());

            instance.setConfigPath(System.getProperty("user.dir") + "/src/test/config/");
            ConfigManager configManager = new ConfigManager(instance.getConfigPath() + configFile);
            instance.setConfigManager(configManager);

            SignalManager signalManager = SignalManager.getInstance();
            signalManager.start();

//            EngineNettyChannelManager.getInstance().start();
//            if (AppInstance.getInstance().getUserConfig().getRedunPort() > 0) {
//                RedunNettyChannelManager.getInstance().start();
//            }
        }
    }
}