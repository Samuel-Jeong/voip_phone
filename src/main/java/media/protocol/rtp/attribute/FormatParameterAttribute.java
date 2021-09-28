package media.protocol.rtp.attribute;


import media.protocol.rtp.attribute.base.AttributeField;

/**
 * @class public class FormatParameterAttribute extends AttributeField
 * @brief FormatParameterAttribute class
 */
public class FormatParameterAttribute extends AttributeField {

    public static final String ATTRIBUTE_TYPE = "fmtp";

    private static final short DEFAULT_FORMAT = -1;

    private int format;
    private String params;

    //////////////////////////////////////////////////////////////////////

    public FormatParameterAttribute (int format, String params) {
        super(ATTRIBUTE_TYPE);

        this.format = format;
        this.params = params;
    }

    public FormatParameterAttribute ( ) {
        this(DEFAULT_FORMAT, null);
    }

    //////////////////////////////////////////////////////////////////////

    public int getFormat ( ) {
        return format;
    }

    public void setFormat (int format) {
        this.format = format;
    }

    public String getParams ( ) {
        return params;
    }

    public void setParams (String params) {
        this.params = params;
    }

    //////////////////////////////////////////////////////////////////////

    @Override
    public String toString ( ) {
        super.builder.setLength(0);
        super.builder.append(BEGIN).append(ATTRIBUTE_TYPE).append(ATTRIBUTE_SEPARATOR);
        super.builder.append(this.format).append(" ").append(this.params);
        return super.builder.toString();
    }

}
