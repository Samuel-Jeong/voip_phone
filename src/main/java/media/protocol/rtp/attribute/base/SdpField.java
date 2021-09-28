package media.protocol.rtp.attribute.base;

/**
 * @interface public interface SdpField
 * @brief SdpField interface
 */
public interface SdpField {

    String CRLF = "\r\n";
    String FIELD_SEPARATOR = "=";

    /**
     * Gets the type of the field
     *
     * @return the char that represents the field
     */
    char getFieldType ( );

}

