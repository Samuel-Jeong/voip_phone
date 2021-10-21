package media.dtmf.module;

import client.gui.model.dtmf.DtmfSoundGenerator;
import media.dtmf.base.DtmfTask;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @class public class DtmfTaskManager
 * @brief DtmfTaskManager class
 */
public class DtmfTaskManager {

    private ScheduledThreadPoolExecutor dtmfTaskExecutor;
    private final DtmfTask dtmfTask;

    ////////////////////////////////////////////////////////////////////////////////

    public DtmfTaskManager(int digit, int volume, int interval) {
        dtmfTask = new DtmfTask(digit, volume, interval);
    }

    ////////////////////////////////////////////////////////////////////////////////

    public void startDtmfTask() {
        if (dtmfTaskExecutor == null) {
            dtmfTaskExecutor = new ScheduledThreadPoolExecutor(2);
            dtmfTaskExecutor.scheduleAtFixedRate(
                    dtmfTask,
                    dtmfTask.getInterval(),
                    dtmfTask.getInterval(),
                    TimeUnit.MILLISECONDS
            );
        }
    }

    public void stopDtmfTask() {
        if (dtmfTaskExecutor != null) {
            dtmfTaskExecutor.shutdown();
            dtmfTaskExecutor = null;
        }
    }

    public void handle() {
        DtmfHandler.handle(
                dtmfTask.getDigit(),
                dtmfTask.getVolume(),
                dtmfTask.getEventDuration(),
                true
        );
    }

    ////////////////////////////////////////////////////////////////////////////////

    public void clearDtmfTask() {
        dtmfTask.setEventDuration(0);
    }
}
