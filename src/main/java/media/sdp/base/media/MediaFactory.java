package media.sdp.base.media;

import media.sdp.base.SdpFactory;
import media.sdp.base.attribute.RtpAttribute;
import media.sdp.base.attribute.base.AttributeFactory;
import media.sdp.base.attribute.base.FmtpAttributeFactory;
import media.sdp.base.attribute.base.RtpMapAttributeFactory;
import media.sdp.base.field.BandwidthField;
import media.sdp.base.field.ConnectionField;
import media.sdp.base.field.MediaField;

import java.util.*;

/**
 * @class public class MediaFactory
 * @brief MediaFactory class
 */
public class MediaFactory extends SdpFactory {

    public static final String RTPMAP = "rtpmap";
    public static final String FMTP = "fmtp";
    public static final String DTMF = "telephone-event";

    private final Map<String, RtpAttribute> attributeFactoryMap = new LinkedHashMap<>();
    private List<RtpAttribute> intersectedCodecList = null;

    // Mandatory
    private MediaField mediaField;

    // Optional
    private final List<BandwidthField> bandwidthFieldList = new ArrayList<>();
    private ConnectionField connectionField;
    private int amfMediaPort = -1;

    ////////////////////////////////////////////////////////////////////////////////

    public MediaFactory(
            char type,
            String mediaType, Vector mediaFormats, int mediaPort,
            String protocol, int portCount) {
        this.mediaField = new MediaField(
                type,
                mediaType,
                mediaPort,
                protocol,
                Arrays.asList((String[]) mediaFormats.toArray(new String[mediaFormats.size()])),
                portCount
        );
    }

    ////////////////////////////////////////////////////////////////////////////////

    public void addBandwidthField(char bandwidthType, String modifier, int value) {
        BandwidthField bandwidthField = new BandwidthField(
                bandwidthType,
                modifier,
                value
        );

        bandwidthFieldList.add(bandwidthField);
    }

    public List<BandwidthField> getBandwidthFieldList() {
        return bandwidthFieldList;
    }

    public String getBandwidthData () {
        StringBuilder result = new StringBuilder();
        //for (int i = bandwidthFieldList.size() - 1; i >= 0; i--) {
        //BandwidthField bandwidthField = bandwidthFieldList.get(i);
        for (BandwidthField bandwidthField : bandwidthFieldList) {
            if (bandwidthField != null) {
                result.append(bandwidthField.getBandwidthType()).append("=").
                        append(bandwidthField.getModifier()).append(":").
                        append(bandwidthField.getValue()).
                        append(CRLF);
            }
        }

        return result.toString();
    }

    public void setConnectionField(char connectionType, String connectionAddress, String connectionAddressType, String connectionNetworkType) {
        this.connectionField = new ConnectionField(
                connectionType,
                connectionAddress,
                connectionAddressType,
                connectionNetworkType
        );
    }

    public ConnectionField getConnectionField() {
        return connectionField;
    }

    public void setConnectionField(ConnectionField connectionField) {
        this.connectionField = connectionField;
    }

    public String getConnectionData () {
        if (connectionField != null) {
            return connectionField.getConnectionType() + "=" +
                    connectionField.getConnectionNetworkType() + " " +
                    connectionField.getConnectionAddressType() + " " +
                    connectionField.getConnectionAddress() +
                    CRLF;
        }

        return "";
    }

    public List<RtpAttribute> getIntersectedCodecList() {
        if (intersectedCodecList == null) {
            return getCodecList();
        }
        return intersectedCodecList;
    }

    public void setIntersectedCodecList(List<RtpAttribute> intersectedCodecList) {
        this.intersectedCodecList = intersectedCodecList;
    }

    public MediaField getMediaField() {
        return mediaField;
    }

    public void setMediaField(MediaField mediaField) {
        this.mediaField = mediaField;
    }

    public int getAmfMediaPort() {
        return amfMediaPort;
    }

    public void setAmfMediaPort(int amfMediaPort) {
        this.amfMediaPort = amfMediaPort;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public void addAttributeFactory(AttributeFactory attributeFactory) {
        String payloadId = attributeFactory.getPayloadId();
        if (payloadId == null) {
            return;
        }

        RtpAttribute rtpAttribute = attributeFactoryMap.get(payloadId);
        if (rtpAttribute == null) {
            rtpAttribute = new RtpAttribute(
                    payloadId
            );
            rtpAttribute.setCustomAttributeFactory(attributeFactory);

            attributeFactoryMap.putIfAbsent(
                    payloadId,
                    rtpAttribute
            );
        }
    }

    public void addRtpAttributeFactory(RtpMapAttributeFactory rtpMapAttributeFactory) {
        String payloadId = rtpMapAttributeFactory.getPayloadId();
        if (payloadId == null) {
            return;
        }

        RtpAttribute rtpAttribute = attributeFactoryMap.get(payloadId);
        if (rtpAttribute == null) {
            rtpAttribute = new RtpAttribute(
                    payloadId
            );
            rtpAttribute.setRtpMapAttributeFactory(rtpMapAttributeFactory);

            attributeFactoryMap.putIfAbsent(
                    payloadId,
                    rtpAttribute
            );
        }
    }

    public void addFmtpAttributeFactory(FmtpAttributeFactory fmtpAttributeFactory) {
        String payloadId = fmtpAttributeFactory.getPayloadId();
        if (payloadId == null) {
            return;
        }

        RtpAttribute rtpAttribute = attributeFactoryMap.get(payloadId);
        if (rtpAttribute != null) {
            if (fmtpAttributeFactory.getName().equals(FMTP)) {
                rtpAttribute.addFmtpAttributeFactory(fmtpAttributeFactory);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

    public List<RtpAttribute> getCodecList () {
        List<RtpAttribute> codecList = new ArrayList<>();

        for (String format : mediaField.getMediaFormats()) {
            for (Map.Entry<String, RtpAttribute> entry : attributeFactoryMap.entrySet()) {
                if (entry == null) {
                    continue;
                }

                RtpAttribute rtpAttribute = entry.getValue();
                if (rtpAttribute == null) {
                    continue;
                }

                if (format.equals(rtpAttribute.getPayloadId())) {
                    codecList.add(rtpAttribute);
                    break;
                }
            }
        }

        return codecList;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public String getData (boolean isRaw) {
        // Media
        String mediaString = mediaField.getType() + "=" +
                mediaField.getMediaType() + " ";
        if (amfMediaPort > -1) {
            mediaString += amfMediaPort + " ";
        } else {
            mediaString += mediaField.getMediaPort() + " ";
        }
        mediaString += mediaField.getProtocol() + " ";

        StringBuilder data = new StringBuilder(
                mediaString
        );

        int i = 0;
        List<RtpAttribute> curCodecList = intersectedCodecList;
        if (curCodecList == null) {
            curCodecList = getCodecList();
        }

        // CODEC : Payload ID
        for (RtpAttribute rtpAttribute : curCodecList) {
            String codecName = rtpAttribute.getRtpMapAttributeFactory().getCodecName();
            if (codecName.equals(DTMF)) {
                i++;
                continue;
            }

            data.append(rtpAttribute.getPayloadId());

            if (!isRaw) {
                break;
            }

            if ((i + 1) < curCodecList.size()) {
                data.append(" ");
            }

            i++;
        }

        if (data.charAt(data.length() - 1) != ' ') {
            data.append(" ");
        }

        // CODEC : Payload ID
        i = 0;
        for (RtpAttribute rtpAttribute : curCodecList) {
            String codecName = rtpAttribute.getRtpMapAttributeFactory().getCodecName();
            if (!codecName.equals(DTMF)) {
                i++;
                continue;
            }

            data.append(rtpAttribute.getPayloadId());

            if (!isRaw) {
                break;
            }

            if ((i + 1) < curCodecList.size()) {
                data.append(" ");
            }

            i++;
        }
        data.append(CRLF);
        //

        // Bandwidth
        data.append(getBandwidthData());
        //

        // Connection
        data.append(getConnectionData());
        //

        // Attributes
        // CODEC : RTPMAP & FMTP
        for (RtpAttribute rtpAttribute : curCodecList) {
            String codecName = rtpAttribute.getRtpMapAttributeFactory().getCodecName();
            if (codecName.equals(DTMF)) {
                continue;
            }

            data.append(rtpAttribute.getData());
            if (!isRaw) {
                break;
            }
        }

        // DTMF : RTPMAP & FMTP
        for (RtpAttribute rtpAttribute : curCodecList) {
            String codecName = rtpAttribute.getRtpMapAttributeFactory().getCodecName();
            if (!codecName.equals(DTMF)) {
                continue;
            }

            data.append(rtpAttribute.getData());
            if (!isRaw) {
                break;
            }
        }

        // CUSTOM
        List<RtpAttribute> rtpAttributeList = new ArrayList<>(attributeFactoryMap.values());
        for (RtpAttribute rtpAttribute : rtpAttributeList) {
            if (!mediaField.getMediaFormats().contains(rtpAttribute.getPayloadId())) {
                AttributeFactory attributeFactory = rtpAttribute.getCustomAttributeFactory();
                if (attributeFactory != null &&
                        (attributeFactory.getName().equals("visited-realm") ||
                                attributeFactory.getName().equals("omr-m-cksum") ||
                                attributeFactory.getName().equals("omr-s-cksum"))) {
                    continue;
                }

                data.append(rtpAttribute.getData());
            }
        }
        //

        return data.toString();
    }

}
