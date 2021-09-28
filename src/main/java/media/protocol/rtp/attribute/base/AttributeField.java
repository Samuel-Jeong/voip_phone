package media.protocol.rtp.attribute.base;

/**
 * @class public class AttributeField implements SdpField
 * @brief AttributeField class
 */
public class AttributeField implements SdpField {

    // text parsing
    public static final String ATTRIBUTE_SEPARATOR = ":";

    public static final char FIELD_TYPE = 'a';
    protected static final String BEGIN = "a=";
    protected static final int BEGIN_LENGTH = BEGIN.length();

    protected final StringBuilder builder;

    protected String key;
    protected String value;

    //////////////////////////////////////////////////////////////////////

    protected AttributeField ( ) {
        this.builder = new StringBuilder(BEGIN);
    }

    protected AttributeField (String key, String value) {
        this();
        this.key = key;
        this.value = value;
    }

    protected AttributeField (String key) {
        this(key, null);
    }

    //////////////////////////////////////////////////////////////////////

    public String getKey ( ) {
        return key;
    }

    public String getValue ( ) {
        return value;
    }

    @Override
    public char getFieldType ( ) {
        return FIELD_TYPE;
    }

    //////////////////////////////////////////////////////////////////////

    @Override
    public String toString ( ) {
        // Clean String Builder
        this.builder.setLength(BEGIN_LENGTH);
        this.builder.append(this.key);
        if (this.value != null && !this.value.isEmpty()) {
            this.builder.append(ATTRIBUTE_SEPARATOR).append(this.value);
        }
        return this.builder.toString();
    }

}