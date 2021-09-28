package media.sdp;

import gov.nist.javax.sdp.SessionDescriptionImpl;
import gov.nist.javax.sdp.parser.SDPAnnounceParser;
import media.sdp.base.SdpInfo;
import media.sdp.base.SdpUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sdp.Attribute;
import javax.sdp.Connection;
import javax.sdp.MediaDescription;
import javax.sdp.SdpParseException;
import java.util.Vector;

/**
 * @class public class SdpParser
 * @brief SdpParser Class
 */
public class SdpParser {

    private static final Logger logger = LoggerFactory.getLogger(SdpParser.class);

    public static final String AUDIO_DESCRIPTION = "audio";

    private final String callId;

    public SdpParser(String callId) {
        this.callId = callId;
    }

    public SdpUnit parse (String sdp) throws Exception {
        if (sdp == null || sdp.length() == 0) {
            return null;
        }

        SDPAnnounceParser parser = new SDPAnnounceParser(sdp);
        SessionDescriptionImpl sdi = parser.parse();
        if (sdi.getVersion().getVersion() != 0) {
            logger.warn("({}) sdp version is not 0. sdp={}", callId, sdp);
            return null;
        }

        Vector mdVector = sdi.getMediaDescriptions(false);
        if (mdVector == null || mdVector.isEmpty()) {
            logger.warn("({}) sdp hasn't media description. sdp={}", callId, sdp);
            return null;
        }

        SdpUnit sdpUnit = new SdpUnit();
        Connection connection = sdi.getConnection();
        for (Object o : mdVector) {
            MediaDescription md = (MediaDescription) o;
            if (md != null && md.getMedia() != null) {
                addSdpInfo(sdpUnit, md, connection);
            }
        }

        return sdpUnit;
    }


    private void addSdpInfo (SdpUnit sdpUnit, MediaDescription md, Connection connection) throws SdpParseException {
        Connection mdConnection = md.getConnection();
        if (mdConnection == null) {
            mdConnection = connection;
        }

        if (mdConnection != null) {
            sdpUnit.setCallId(callId);
            sdpUnit.setRemoteIp(mdConnection.getAddress());
            sdpUnit.setRemotePort(md.getMedia().getMediaPort());

            String mediaType = md.getMedia().getMediaType();
            if (mediaType.equals(AUDIO_DESCRIPTION)) {
                SdpInfo sdpInfo = new SdpInfo();
                addAttribute(md, sdpInfo);
                sdpUnit.addSdpInfo(mediaType, sdpInfo);
            }
        }
    }

    private void addAttribute (MediaDescription md, SdpInfo sdpInfo) throws SdpParseException {
        Vector<String> mediaFormats = new Vector<>();

        for (Object obj : md.getMedia().getMediaFormats(false)) {
            String payloadId = (String) obj;
            mediaFormats.add(payloadId);
        }

        for (Object obj : md.getAttributes(false)) {
            parseAttribute(sdpInfo, (Attribute) obj, mediaFormats);
        }
    }

    private void parseAttribute (SdpInfo sdpInfo, Attribute attr, Vector<String> mediaFormats) throws SdpParseException {
        String name = attr.getName();
        if (name == null) { return; }

        logger.debug("mediaFormats: {}", mediaFormats);
        logger.debug("attr: {}", attr);
        if (!attr.hasValue()) {
            sdpInfo.addAttribute(attr.getName(), null);
        } else {
            String value = attr.getValue();
            logger.debug("value: {}", value);

            if ((name.equals(SdpAttribute.NAME_RTPMAP) || (name.equals(SdpAttribute.NAME_FMTP)))) {
                int space = value.indexOf(' ');
                if (space > 0) {
                    String payloadId = value.substring(0, space);
                    logger.debug("payloadId: {}", payloadId);
                    if (mediaFormats.contains(payloadId)) {
                        String description = value.substring(space + 1);
                        logger.debug("description: {}", description);

                        sdpInfo.addAttribute(name, payloadId, description);
                    }
                }
            } else {
                sdpInfo.addAttribute(attr.getName(), value);
            }
        }

        logger.debug("SdpInfo: {}", sdpInfo);
    }

}
