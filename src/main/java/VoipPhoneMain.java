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
         logger.error("Argument Error. (&0: Mode(0:jar, 1:bin), &1: configPath, &2: contactPath)");
         return;
      }

      String configPath = null;
      String contactPath = null;

      int mode = Integer.parseInt(args[0].trim());
      if (mode == 0) { // jar
         if (args.length == 3) {
            configPath = args[1].trim();
            contactPath = args[2].trim();
         } else {
            logger.error("Argument Error. (&0: Mode(0:jar, 1:bin), &1: configPath(mandatory) &2: contactPath(mandatory))");
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

         contactPath = curUserDir;
         if (SystemManager.getInstance().getOs().contains("win")) {
            contactPath += "\\contact\\contact.txt";
         } else {
            contactPath += "/contact/contact.txt";
         }
      }

      if (configPath != null) {
         ConfigManager configManager = new ConfigManager(configPath);
         configManager.setContactPath(contactPath);
         AppInstance.getInstance().setConfigManager(configManager);
         ServiceManager.getInstance().loop();
      } else {
         logger.error("Argument Error. (&0: Mode(0:jar, 1:bin), &1: configPath(optional))");
      }
   }

}
