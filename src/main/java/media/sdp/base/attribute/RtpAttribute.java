package media.sdp.base.attribute;

import media.sdp.base.attribute.base.AttributeFactory;
import media.sdp.base.attribute.base.FmtpAttributeFactory;
import media.sdp.base.attribute.base.RtpMapAttributeFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @class public class RtpAttribute
 * @brief RtpAttribute class
 */
public class RtpAttribute {

    private String payloadId;
    private AttributeFactory customAttributeFactory;
    private RtpMapAttributeFactory rtpMapAttributeFactory;
    private final List<FmtpAttributeFactory> fmtpAttributeFactoryList = new ArrayList<>();

    ////////////////////////////////////////////////////////////////////////////////

    public RtpAttribute(String payloadId) {
        this.payloadId = payloadId;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public String getPayloadId() {
        return payloadId;
    }

    public void setPayloadId(String payloadId) {
        this.payloadId = payloadId;
    }

    public AttributeFactory getCustomAttributeFactory() {
        return customAttributeFactory;
    }

    public void setCustomAttributeFactory(AttributeFactory customAttributeFactory) {
        this.customAttributeFactory = customAttributeFactory;
    }

    public RtpMapAttributeFactory getRtpMapAttributeFactory() {
        return rtpMapAttributeFactory;
    }

    public void setRtpMapAttributeFactory(RtpMapAttributeFactory rtpMapAttributeFactory) {
        this.rtpMapAttributeFactory = rtpMapAttributeFactory;
    }

    public FmtpAttributeFactory getFirstModeSetFmtpAttributeFactory() {
        for (FmtpAttributeFactory modeSetFmtpAttributeFactory : fmtpAttributeFactoryList) {
            if (!modeSetFmtpAttributeFactory.getModeSetList().isEmpty()) {
                return modeSetFmtpAttributeFactory;
            }
        }

        return null;
    }

    public List<FmtpAttributeFactory> getFmtpAttributeFactoryList() {
        return fmtpAttributeFactoryList;
    }

    public void addFmtpAttributeFactory(FmtpAttributeFactory fmtpAttributeFactory) {
        if (fmtpAttributeFactoryList.contains(fmtpAttributeFactory)) {
            return;
        }

        fmtpAttributeFactoryList.add(0, fmtpAttributeFactory);
    }

    public void clearFmtpAttributeFactoryList() {
        fmtpAttributeFactoryList.clear();
    }

    ////////////////////////////////////////////////////////////////////////////////

    public String getData () {
        StringBuilder data = new StringBuilder();

        if (customAttributeFactory != null) {
            data.append(customAttributeFactory.getData());
        }

        if (rtpMapAttributeFactory != null) {
            data.append(rtpMapAttributeFactory.getData());

            for (FmtpAttributeFactory fmtpAttributeFactory : fmtpAttributeFactoryList) {
                data.append(fmtpAttributeFactory.getData());
            }
        }

        return data.toString();
    }

    ////////////////////////////////////////////////////////////////////////////////

    public FmtpAttributeFactory getModeSetAttribute() {
        for (FmtpAttributeFactory attributeFactory : fmtpAttributeFactoryList) {
            if (attributeFactory.getValue().contains("mode-set")) {
                return attributeFactory;
            }
        }

        return null;
    }

    public boolean isOctetAlignMode() {
        FmtpAttributeFactory octetAlignAttribute = getOctetAlignAttribute();
        if (octetAlignAttribute != null) {
            return getOctetAlignAttribute().isOaMode() || rtpMapAttributeFactory.isOctetAlign();
        } else {
            return rtpMapAttributeFactory.isOctetAlign();
        }
    }

    public FmtpAttributeFactory getOctetAlignAttribute() {
        for (FmtpAttributeFactory attributeFactory : fmtpAttributeFactoryList) {
            if (attributeFactory.getValue().contains("octet-align")) {
                return attributeFactory;
            }
        }

        return null;
    }

}
