package service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.base.TaskUnit;
import signal.module.RegiManager;

/**
 * @author jamesj
 * @class public class RegiDeleteHandler extends TaskUnit
 * @brief RegiDeleteHandler
 */
public class RegiDeleteHandler extends TaskUnit {

    private static final Logger logger = LoggerFactory.getLogger(RegiDeleteHandler.class);

    private final String name;
    private final String fromNo;

    ////////////////////////////////////////////////////////////////////////////////

    public RegiDeleteHandler(String name, String fromNo, int interval) {
        super(interval);

        this.name = name;
        this.fromNo = fromNo;
    }

    ////////////////////////////////////////////////////////////////////////////////

    @Override
    public void run ( ) {
        logger.debug("Register Deletion is running for [fromNo={}]", fromNo);
        RegiManager.getInstance().deleteRegi(fromNo);
        TaskManager.getInstance().removeTask(name);
    }

}
