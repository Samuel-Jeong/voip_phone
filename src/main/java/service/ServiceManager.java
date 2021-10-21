package service;

import client.gui.FrameManager;
import config.ConfigManager;
import media.MediaManager;
import media.module.codec.amr.AmrManager;
import media.module.codec.evs.EvsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import signal.SignalManager;
import signal.module.ResourceManager;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @class public class ServiceManager
 * @brief Voip Phone 의 전체 Service 관리 클래스
 */
public class ServiceManager {

    private static final Logger logger = LoggerFactory.getLogger(ServiceManager.class);

    private static ServiceManager serviceManager = null;

    private static final int DELAY = 1000;

    public static final String CLIENT_FRAME_NAME = "CLIENT";
    public static final String CONFIG_ERROR_FRAME_NAME = "CONFIG_ERROR";

    private final AtomicBoolean isQuit = new AtomicBoolean(false);

    ////////////////////////////////////////////////////////////////////////////////

    public ServiceManager() {
        Runtime.getRuntime().addShutdownHook(new ShutDownHookHandler("ShutDownHookHandler", Thread.currentThread()));
    }

    public static ServiceManager getInstance ( ) {
        if (serviceManager == null) {
            serviceManager = new ServiceManager();
        }

        return serviceManager;
    }

    ////////////////////////////////////////////////////////////////////////////////

    private void start () {
        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        if (!configManager.load()) {
            JOptionPane pane = new JOptionPane(
                    "Not found the config path.",
                    JOptionPane.ERROR_MESSAGE
            );

            JDialog d = pane.createDialog(null, "Error");
            d.pack();
            d.setModal(false);
            d.setVisible(true);
            while (pane.getValue() == JOptionPane.UNINITIALIZED_VALUE) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    // ignore
                }
            }
            System.exit(1);
        }

        FrameManager.getInstance().start(CLIENT_FRAME_NAME);

        if (MediaManager.getInstance().getPriorityCodec().equals(MediaManager.EVS)) {
            EvsManager.getInstance().init();
        }

        if (MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_NB)
                || MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_WB)) {
            AmrManager.getInstance().init();
        }

        ResourceManager.getInstance().initResource();

        SignalManager.getInstance().start();
        //mediaManager.start();

        TaskManager.getInstance().addTask(HaHandler.class.getSimpleName(), new HaHandler(DELAY));

        if (configManager.getLongCallTime() != 0) {
            TaskManager.getInstance().addTask(LongCallRemover.class.getSimpleName(), new LongCallRemover(DELAY));
        }

        logger.debug("All services are opened.");
    }

    public void stop () {
        TaskManager.getInstance().stop();

        MediaManager.getInstance().stop();
        SignalManager.getInstance().stop();

        ResourceManager.getInstance().releaseResource();

        FrameManager.getInstance().stop(CLIENT_FRAME_NAME);

        isQuit.set(true);
        logger.debug("All services are closed.");
    }

    /**
     * @fn public void loop ()
     * @brief Main Service Loop
     */
    public void loop () {
        start();

        while (!isQuit.get()) {
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                logger.warn("ServiceManager.loop.InterruptedException", e);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @class private static class ShutDownHookHandler extends Thread
     * @brief Graceful Shutdown 을 처리하는 클래스
     * Runtime.getRuntime().addShutdownHook(*) 에서 사용됨
     */
    private static class ShutDownHookHandler extends Thread {

        // shutdown 로직 후에 join 할 thread
        private final Thread target;

        public ShutDownHookHandler (String name, Thread target) {
            super(name);

            this.target = target;
            logger.debug("ShutDownHookHandler is initiated. (target={})", target.getName());
        }

        /**
         * @fn public void run ()
         * @brief 정의된 Shutdown 로직을 수행하는 함수
         */
        @Override
        public void run ( ) {
            try {
                shutDown();
                //target.join();
                logger.debug("ShutDownHookHandler's target is finished successfully. (target={})", target.getName());
            } catch (Exception e) {
                logger.warn("ShutDownHookHandler.run.Exception", e);
            }
        }

        /**
         * @fn private void shutDown ()
         * @brief Runtime 에서 선언된 Handler 에서 사용할 서비스 중지 함수
         */
        private void shutDown ( ) {
            logger.warn("Process is about to quit. (Ctrl+C)");
            ServiceManager.getInstance().stop();
        }
    }

}
