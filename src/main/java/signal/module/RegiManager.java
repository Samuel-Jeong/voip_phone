package signal.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.RegiDeleteHandler;
import service.TaskManager;
import signal.base.RegiInfo;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @class public class RegiManager
 * @brief RegiManager class
 */
public class RegiManager {

    private static final Logger logger = LoggerFactory.getLogger(RegiManager.class);

    private static RegiManager regiManager = null;

    private final HashMap<String, RegiInfo> regiMap = new HashMap<>();
    private final ReentrantLock regiMapLock = new ReentrantLock();

    ////////////////////////////////////////////////////////////////////////////////

    public RegiManager() {
        // Nothing
    }

    public static RegiManager getInstance () {
        if (regiManager == null) {
            regiManager = new RegiManager();
        }
        return regiManager;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public void scheduleRegi(String mdn, int expires) {
        if (getRegi(mdn) == null) {
            logger.warn("Fail to schedule the register delete handler. Not found the register info. (mdn={})", mdn);
            return;
        }

        String handlerName = RegiDeleteHandler.class.getSimpleName() + "_" + mdn;
        TaskManager.getInstance().addTask(handlerName, new RegiDeleteHandler(handlerName, mdn, expires));
    }

    ////////////////////////////////////////////////////////////////////////////////

    public int getRegiMapSize() {
        try {
            regiMapLock.lock();

            return regiMap.size();
        } catch (Exception e) {
            logger.warn("RegiManager.getRegiMapSize.Exception", e);
            return 0;
        } finally {
            regiMapLock.unlock();
        }
    }

    public void addRegi(String mdn, String ip, int port, int expires) {
        if (mdn == null || ip == null || port <= 0 || expires <= 0) {
            logger.warn("Fail to add a new register info. Argument error is occurred. (mdn={}, ip={}, port={}, expires={})",
                    mdn, ip, port, expires
            );
            return;
        }

        try {
            regiMapLock.lock();

            regiMap.putIfAbsent(
                    mdn,
                    new RegiInfo(
                            mdn,
                            ip,
                            port,
                            expires
                    )
            );
        } catch (Exception e) {
            logger.warn("RegiManager.addRegi.Exception", e);
        } finally {
            regiMapLock.unlock();
        }
    }

    public RegiInfo getRegi(String mdn) {
        if (mdn == null) { return null; }

        try {
            regiMapLock.lock();

            return regiMap.get(mdn);
        } catch (Exception e) {
            logger.warn("RegiManager.getRegi.Exception", e);
            return null;
        } finally {
            regiMapLock.unlock();
        }
    }

    public void deleteRegi(String mdn) {
        if (mdn == null) { return; }

        try {
            regiMapLock.lock();

            regiMap.remove(mdn);
        } catch (Exception e) {
            logger.warn("RegiManager.deleteRegi.Exception", e);
        } finally {
            regiMapLock.unlock();
        }
    }

}
