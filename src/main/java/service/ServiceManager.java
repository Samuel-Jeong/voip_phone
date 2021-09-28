package service;

import client.gui.FrameManager;
import config.ConfigManager;
import media.MediaManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import signal.SignalManager;
import signal.module.ResourceManager;

/**
 * @class public class ServiceManager
 * @brief Voip Phone 의 전체 Service 관리 클래스
 */
public class ServiceManager {

    private static final Logger logger = LoggerFactory.getLogger(ServiceManager.class);

    private static ServiceManager serviceManager = null;

    private static final int DELAY = 1000;
    public static final String CLIENT_FRAME_NAME = "client";

    private final SignalManager signalManager;
    private final MediaManager mediaManager;

    private final TaskManager taskManager;

    private final FrameManager frameManager;

    private boolean isQuit = false;

    ////////////////////////////////////////////////////////////////////////////////

    public ServiceManager() {
        Runtime.getRuntime().addShutdownHook(new ShutDownHookHandler("ShutDownHookHandler", Thread.currentThread()));

        signalManager = SignalManager.getInstance();
        mediaManager = MediaManager.getInstance();
        taskManager = TaskManager.getInstance();
        frameManager = FrameManager.getInstance();
    }

    public static ServiceManager getInstance ( ) {
        if (serviceManager == null) {
            serviceManager = new ServiceManager();
        }

        return serviceManager;
    }

    ////////////////////////////////////////////////////////////////////////////////

    private void start () {
        ResourceManager.getInstance().initResource();

        signalManager.start();
        //mediaManager.start();

        taskManager.addTask(HaHandler.class.getSimpleName(), new HaHandler(DELAY));

        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        if (configManager.getLongCallTime() != 0) {
            taskManager.addTask(LongCallRemover.class.getSimpleName(), new LongCallRemover(DELAY));
        }

        if (frameManager != null) {
            frameManager.start(CLIENT_FRAME_NAME);
        }

        logger.debug("All services are opened.");
    }

    public void stop () {
        taskManager.stop();

        mediaManager.stop();
        signalManager.stop();

        if (frameManager != null) {
            frameManager.stop(CLIENT_FRAME_NAME);
        }

        ResourceManager.getInstance().releaseResource();

        isQuit = true;

        logger.debug("All services are closed.");
    }

    /**
     * @fn public void loop ()
     * @brief Main Service Loop
     */
    public void loop () {
        start();

        while (!isQuit) {
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
                target.join();
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
