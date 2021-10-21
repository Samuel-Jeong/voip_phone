package media.sdp.base.field;

/**
 * @class public class ConnectionField
 * @brief ConnectionField class
 */
public class ConnectionField {

    private char connectionType;
    private String connectionAddress;
    private String connectionAddressType;
    private String connectionNetworkType;

    public ConnectionField(char connectionType, String connectionAddress, String connectionAddressType, String connectionNetworkType) {
        this.connectionType = connectionType;
        this.connectionAddress = connectionAddress;
        this.connectionAddressType = connectionAddressType;
        this.connectionNetworkType = connectionNetworkType;
    }

    public char getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(char connectionType) {
        this.connectionType = connectionType;
    }

    public String getConnectionAddress() {
        return connectionAddress;
    }

    public void setConnectionAddress(String connectionAddress) {
        this.connectionAddress = connectionAddress;
    }

    public String getConnectionAddressType() {
        return connectionAddressType;
    }

    public void setConnectionAddressType(String connectionAddressType) {
        this.connectionAddressType = connectionAddressType;
    }

    public String getConnectionNetworkType() {
        return connectionNetworkType;
    }

    public void setConnectionNetworkType(String connectionNetworkType) {
        this.connectionNetworkType = connectionNetworkType;
    }

    @Override
    public String toString() {
        return "ConnectionField{" +
                "connectionType=" + connectionType +
                ", connectionAddress='" + connectionAddress + '\'' +
                ", connectionAddressType='" + connectionAddressType + '\'' +
                ", connectionNetworkType='" + connectionNetworkType + '\'' +
                '}';
    }
}
