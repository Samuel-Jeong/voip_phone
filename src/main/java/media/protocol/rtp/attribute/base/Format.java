package media.protocol.rtp.attribute.base;

/**
 * @class public class Format implements Cloneable
 * @brief Format class
 */
public class Format implements Cloneable {

    //encoding name
    private EncodingName name;

    //any specific options
    private Text options;

    private Boolean sendPTime = false;

    /**
     * Creates new descriptor.
     *
     * @param name the encoding name
     */
    public Format(EncodingName name) {
        this.name = name;
    }


    /**
     * Gets the encoding name.
     *
     * @return the encoding name.
     */
    public EncodingName getName() {
        return name;
    }

    /**
     * Modifies encoding name.
     *
     * @param name new encoding name.
     */
    public void setName(EncodingName name) {
        this.name = name;
    }

    /**
     * Gets options
     *
     * @return options as text.
     */
    public Text getOptions() {
        return options;
    }

    /**
     * Modify options.
     *
     * @param options new options.
     */
    public void setOptions(Text options) {
        this.options = options;
    }

    @Override
    public String toString() {
        return name.toString();
    }

    /**
     * Compares two format descriptors.
     *
     * @param fmt the another format descriptor
     * @return
     */
    public boolean matches(Format fmt) {
        return this.name.equals(fmt.name);
    }

    public boolean shouldSendPTime() {
        return sendPTime;
    }

    public void setSendPTime(Boolean newValue) {
        sendPTime=newValue;
    }

    @Override
    public Format clone() {
        return null;
    }

}
