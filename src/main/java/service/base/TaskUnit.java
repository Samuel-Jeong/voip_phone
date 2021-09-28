package service.base;

/**
 * @author jamesj
 * @class public class TaskUnit
 * @brief Task Unit Abstract Class
 */
public abstract class TaskUnit implements Runnable {

    private int interval;

    protected TaskUnit(int interval) {
        this.interval = interval;
    }

    public int getInterval ( ) {
        return interval;
    }

    public void setInterval (int interval) {
        this.interval = interval;
    }
}
