package media.sdp.base.field;

/**
 * @class public class TimeField
 * @brief TimeField class
 */
public class TimeField {

    private char timeType;
    private String startTime;
    private String endTime;

    public TimeField(char timeType, String startTime, String endTime) {
        this.timeType = timeType;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public char getTimeType() {
        return timeType;
    }

    public void setTimeType(char timeType) {
        this.timeType = timeType;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "TimeField{" +
                "timeType=" + timeType +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                '}';
    }
}
