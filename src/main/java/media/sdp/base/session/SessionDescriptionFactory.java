package media.sdp.base.session;

import media.sdp.base.SdpFactory;
import media.sdp.base.field.ConnectionField;
import media.sdp.base.field.OriginField;
import media.sdp.base.field.SessionField;
import media.sdp.base.field.VersionField;

/**
 * @class public class SessionDescriptionFactory
 * @brief SessionDescriptionFactory class
 */
public class SessionDescriptionFactory extends SdpFactory {

    // Mandatory
    private VersionField versionField;
    private OriginField originField;
    private SessionField sessionField;

    // Optional
    private ConnectionField connectionField;

    // AMF Fields
    private OriginField amfOriginField;
    private ConnectionField amfConnectionField;

    ////////////////////////////////////////////////////////////////////////////////

    public SessionDescriptionFactory(
            char versionType, int version,
            char originType, String originUserName,
            String originAddress, String originAddressType, String originNetworkType,
            long originSessionId, long originSessionVersion,
            char sessionType, String sessionName) {

        this.versionField = new VersionField(
                versionType,
                version
        );

        this.originField = new OriginField(
                originType,
                originUserName,
                originAddress,
                originAddressType,
                originNetworkType,
                originSessionId,
                originSessionVersion
        );

        this.sessionField = new SessionField(
                sessionType,
                sessionName
        );
    }

    ////////////////////////////////////////////////////////////////////////////////

    public VersionField getVersionField() {
        return versionField;
    }

    public void setVersionField(VersionField versionField) {
        this.versionField = versionField;
    }

    public OriginField getOriginField() {
        return originField;
    }

    public void setOriginField(OriginField originField) {
        this.originField = originField;
    }

    public SessionField getSessionField() {
        return sessionField;
    }

    public void setSessionField(SessionField sessionField) {
        this.sessionField = sessionField;
    }

    public void setConnectionField(char connectionType, String connectionAddress, String connectionAddressType, String connectionNetworkType) {
        this.connectionField = new ConnectionField(
                connectionType,
                connectionAddress,
                connectionAddressType,
                connectionNetworkType
        );
    }

    public void setConnectionField(ConnectionField connectionField) {
        this.connectionField = connectionField;
    }

    public ConnectionField getConnectionField() {
        return connectionField;
    }

    public OriginField getAmfOriginField() {
        return amfOriginField;
    }

    public void setAmfOriginField(OriginField amfOriginField) {
        this.amfOriginField = amfOriginField;
    }

    public ConnectionField getAmfConnectionField() {
        return amfConnectionField;
    }

    public void setAmfConnectionField(ConnectionField amfConnectionField) {
        this.amfConnectionField = amfConnectionField;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public String getData() {
        // Version
        String result = versionField.getVersionType() + "=" +
                versionField.getVersion() +
                CRLF;

        // Origin
        if (amfOriginField != null) {
            result += originField.getOriginType() + "=" +
                    amfOriginField.getOriginUserName() + " " +
                    originField.getSessionId() + " " +
                    originField.getSessionVersion() + " " +
                    amfOriginField.getOriginNetworkType() + " " +
                    amfOriginField.getOriginAddressType() + " " +
                    amfOriginField.getOriginAddress() +
                    CRLF;
        } else {
            result += originField.getOriginType() + "=" +
                    originField.getOriginUserName() + " " +
                    originField.getSessionId() + " " +
                    originField.getSessionVersion() + " " +
                    originField.getOriginNetworkType() + " " +
                    originField.getOriginAddressType() + " " +
                    originField.getOriginAddress() +
                    CRLF;
        }

        // Session
        result += sessionField.getSessionType() + "=" +
                sessionField.getSessionName() +
                CRLF;

        // Connection
        if (connectionField != null) {
            if (amfConnectionField != null) {
                result += amfConnectionField.getConnectionType() + "=" +
                        amfConnectionField.getConnectionNetworkType() + " " +
                        amfConnectionField.getConnectionAddressType() + " " +
                        amfConnectionField.getConnectionAddress() +
                        CRLF;
            } else {
                result += connectionField.getConnectionType() + "=" +
                        connectionField.getConnectionNetworkType() + " " +
                        connectionField.getConnectionAddressType() + " " +
                        connectionField.getConnectionAddress() +
                        CRLF;
            }
        }

        return result;
    }

}
