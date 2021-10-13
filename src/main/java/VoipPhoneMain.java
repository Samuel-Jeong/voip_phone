import config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AppInstance;
import service.ServiceManager;
import system.SystemManager;

/**
 * @class public class VoipPhoneMain
 * @brief VOIP Main Class
 */
public class VoipPhoneMain {

   private static final Logger logger = LoggerFactory.getLogger(VoipPhoneMain.class);

   public static void main(String[] args) {
      if (args.length < 1) {
         logger.error("Argument Error. (&0: Mode(0:jar, 1:bin), &1: configPath(optional))");
         return;
      }

      String configPath = null;
      int mode = Integer.parseInt(args[0].trim());
      if (mode == 0) { // jar
         if (args.length == 2) {
            configPath = args[1].trim();
         } else {
            logger.error("Argument Error. (&0: Mode(0:jar, 1:bin), &1: configPath(optional))");
            return;
         }
      } else if (mode == 1) { // binary
         String curUserDir = System.getProperty("user.dir");
         if (curUserDir.endsWith("bin")) {
            curUserDir = curUserDir.substring(0, curUserDir.lastIndexOf("bin") - 1);
         }

         configPath = curUserDir;
         if (SystemManager.getInstance().getOs().contains("win")) {
            configPath += "\\config\\user_conf.ini";
         } else {
            configPath += "/config/user_conf.ini";
         }
      }

      if (configPath != null) {
         ConfigManager configManager = new ConfigManager(configPath);
         AppInstance.getInstance().setConfigManager(configManager);
         ServiceManager.getInstance().loop();
      } else {
         logger.error("Argument Error. (&0: Mode(0:jar, 1:bin), &1: configPath(optional))");
      }
   }

}
