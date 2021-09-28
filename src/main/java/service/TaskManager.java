package service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.base.TaskUnit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author jamesj
 * @class public class TaskManager
 * @brief Task Manager
 */
public class TaskManager {

    private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);

    private static TaskManager taskManager = null;

    private final ScheduledThreadPoolExecutor executor;

    private final Map<String, ScheduledFuture<?>> taskMap = new ConcurrentHashMap<>();

    ////////////////////////////////////////////////////////////////////////////////

    public TaskManager( ) {
        executor = new ScheduledThreadPoolExecutor(100);
    }

    public static TaskManager getInstance ( ) {
        if (taskManager == null) {
            taskManager = new TaskManager();
        }

        return taskManager;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public void stop ( ) {
        for (ScheduledFuture<?> scheduledFuture : taskMap.values()) {
            scheduledFuture.cancel(true);
        }

        executor.shutdown();
        logger.debug("Interval Task Manager ends.");
    }

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @fn public void addJob (String name, TaskUnit runner)
     * @brief TaskManager 에 새로운 Task 를 등록하는 함수
     */
    public void addTask (String name, TaskUnit taskUnit) {
        if (taskMap.get(name) != null) {
            logger.warn("TaskManager: Hashmap Key duplication error.");
            return;
        }

        ScheduledFuture<?> scheduledFuture = null;
        try {
            scheduledFuture = executor.scheduleAtFixedRate(
                    taskUnit,
                    taskUnit.getInterval(),
                    taskUnit.getInterval(),
                    TimeUnit.MILLISECONDS
            );
        } catch (Exception e) {
            logger.warn("TaskManager.addTask.Exception", e);
        }

        logger.debug("Task [{}] is added. (interval={})", name, taskUnit.getInterval());
        taskMap.put(name, scheduledFuture);
    }

    public ScheduledFuture<?> findTask (String name) {
        if (taskMap.isEmpty()) {
            return null;
        }

        return taskMap.get(name);
    }

    public void removeTask (String name) {
        if (taskMap.isEmpty()) {
            return;
        }

        ScheduledFuture<?> scheduledFuture = findTask(name);
        if (scheduledFuture == null) {
            return;
        }

        try {
            scheduledFuture.cancel(true);
            taskMap.remove(name);
            logger.debug("Task [{}] is removed.", name);
        } catch (Exception e) {
            logger.warn("TaskManager.removeTask.Exception", e);
        }
    }

}
