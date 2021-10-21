package media.sdp.base.field;

import java.util.List;

/**
 * @class public class MediaField
 * @brief MediaField class
 */
public class MediaField {

    private char type;
    private String mediaType;
    private int mediaPort;
    private String protocol;
    private List<String> mediaFormats;
    private int portCount;

    public MediaField(char type, String mediaType, int mediaPort, String protocol, List<String> mediaFormats, int portCount) {
        this.type = type;
        this.mediaType = mediaType;
        this.mediaPort = mediaPort;
        this.protocol = protocol;
        this.mediaFormats = mediaFormats;
        this.portCount = portCount;
    }

    public char getType() {
        return type;
    }

    public void setType(char type) {
        this.type = type;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public int getMediaPort() {
        return mediaPort;
    }

    public void setMediaPort(int mediaPort) {
        this.mediaPort = mediaPort;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public List<String> getMediaFormats() {
        return mediaFormats;
    }

    public void setMediaFormats(List<String> mediaFormats) {
        this.mediaFormats = mediaFormats;
    }

    public int getPortCount() {
        return portCount;
    }

    public void setPortCount(int portCount) {
        this.portCount = portCount;
    }

    @Override
    public String toString() {
        return "MediaField{" +
                "type=" + type +
                ", mediaType='" + mediaType + '\'' +
                ", mediaPort=" + mediaPort +
                ", protocol='" + protocol + '\'' +
                ", mediaFormats=" + mediaFormats +
                ", portCount=" + portCount +
                '}';
    }
}
