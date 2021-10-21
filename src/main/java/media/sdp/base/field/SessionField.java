package media.sdp.base.field;

/**
 * @class public class SessionField
 * @brief SessionField class
 */
public class SessionField {

    private char sessionType;
    private String sessionName;

    public SessionField(char sessionType, String sessionName) {
        this.sessionType = sessionType;
        this.sessionName = sessionName;
    }

    public char getSessionType() {
        return sessionType;
    }

    public void setSessionType(char sessionType) {
        this.sessionType = sessionType;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    @Override
    public String toString() {
        return "SessionField{" +
                "sessionType=" + sessionType +
                ", sessionName='" + sessionName + '\'' +
                '}';
    }
}
