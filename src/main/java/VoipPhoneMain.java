import config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AppInstance;
import service.ServiceManager;

/**
 * @class public class VoipPhoneMain
 * @brief VOIP Main Class
 */
public class VoipPhoneMain {

   private static final Logger logger = LoggerFactory.getLogger(VoipPhoneMain.class);

   public static void main(String[] args) {
      if (args.length != 2) {
         logger.error("Argument Error. (&0: VoipPhoneMain, &1: config_path)");
         return;
      }

      String configPath = args[1].trim();
      logger.debug("Config path: {}", configPath);
      ConfigManager configManager = new ConfigManager(configPath);

      AppInstance appInstance = AppInstance.getInstance();
      appInstance.setConfigManager(configManager);

      ServiceManager serviceManager = ServiceManager.getInstance();
      serviceManager.loop();
   }

}
