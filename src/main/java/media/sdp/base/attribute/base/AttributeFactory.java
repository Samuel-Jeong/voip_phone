package media.sdp.base.attribute.base;

import media.sdp.base.SdpFactory;

import java.util.List;

/**
 * @class public class AttributeFactory
 * @brief AttributeFactory class
 */
public class AttributeFactory extends SdpFactory {

    char type;
    String name;
    String payloadId = null;
    String value;
    String valueExceptPayloadId = null;

    ////////////////////////////////////////////////////////////////////////////////

    public AttributeFactory(char type, String name, String value, List<String> mediaFormats) {
        this.type = type;
        this.name = name;

        if (value != null) {
            value = value.trim();
            String[] spl = value.split(" ");
            if (spl.length == 0) {
                payloadId = name + ":" + value;
            } else {
                for (String mediaFormat : mediaFormats) {
                    if (mediaFormat.equals(spl[0])) {
                        payloadId = spl[0];
                        StringBuilder valueSb = new StringBuilder();
                        valueSb.append(" ");
                        for (int i = 1; i < spl.length; i++) {
                            valueSb.append(spl[i]);
                            if (i + 1 < spl.length) {
                                valueSb.append(" ");
                            }
                        }
                        valueExceptPayloadId = valueSb.toString();
                        break;
                    }
                }

                if (payloadId == null) {
                    payloadId = name + ":" + value;
                }
            }
        } else {
            payloadId = name;
        }

        this.value = value;
    }

    public char getType() {
        return type;
    }

    public void setType(char type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPayloadId() {
        return payloadId;
    }

    public void setPayloadId(String payloadId) {
        this.payloadId = payloadId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValueExceptPayloadId() {
        return valueExceptPayloadId;
    }

    public void setValueExceptPayloadId(String valueExceptPayloadId) {
        this.valueExceptPayloadId = valueExceptPayloadId;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public String getData () {
        String data = type + "=" + name;

        if (value != null) {
            data += ":";
            data += value;
        }

        data += CRLF;
        return data;
    }
}
