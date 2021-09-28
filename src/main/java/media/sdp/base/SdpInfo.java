package media.sdp.base;

import media.sdp.SdpAttribute;

import java.util.ArrayList;
import java.util.List;

/**
 * @class public class SdpInfo
 * @brief SdpInfo Definition Class
 */
public class SdpInfo {

    List<SdpAttribute> attributes = null;

    ////////////////////////////////////////////////////////////////////////////////

    public void addAttribute (String name, String payloadId, String description) {
        if (attributes == null) {
            attributes = new ArrayList<>();
        }

        attributes.add(new SdpAttribute(name, payloadId, description));
    }

    public void addAttribute (String name, String description) {
        if (attributes == null) {
            attributes = new ArrayList<>();
        }

        attributes.add(new SdpAttribute(name, description));
    }

    public SdpAttribute getAttributeByName (String name) {
        if (attributes.isEmpty() || name == null) {
            return null;
        }

        for (SdpAttribute sdpAttribute : attributes) {
            if (sdpAttribute.getName().equals(name)) {
                return sdpAttribute;
            }
        }

        return null;
    }

    public List<SdpAttribute> getAttributes ( ) {
        return attributes;
    }

    @Override
    public String toString() {
        return "SdpInfo{" +
                "attributes=" + attributes +
                '}';
    }
}
