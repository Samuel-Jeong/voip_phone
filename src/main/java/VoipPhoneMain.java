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

      ConfigManager configManager = new ConfigManager(args[1].trim());
      AppInstance.getInstance().setConfigManager(configManager);
      ServiceManager.getInstance().loop();
   }

}
