package media.sdp.base.field;

/**
 * @class public class VersionField
 * @brief VersionField class
 */
public class VersionField {

    private char versionType;
    private int version;

    public VersionField(char versionType, int version) {
        this.versionType = versionType;
        this.version = version;
    }

    public char getVersionType() {
        return versionType;
    }

    public void setVersionType(char versionType) {
        this.versionType = versionType;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "VersionField{" +
                "versionType=" + versionType +
                ", version=" + version +
                '}';
    }
}
