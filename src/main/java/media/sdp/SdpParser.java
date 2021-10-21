/*
 * Copyright (C) 2021. Uangel Corp. All rights reserved.
 */
package media.sdp;

import gov.nist.javax.sdp.SessionDescriptionImpl;
import gov.nist.javax.sdp.parser.SDPAnnounceParser;
import media.sdp.base.Sdp;
import media.sdp.base.attribute.base.AttributeFactory;
import media.sdp.base.attribute.base.FmtpAttributeFactory;
import media.sdp.base.attribute.base.RtpMapAttributeFactory;
import media.sdp.base.media.MediaDescriptionFactory;
import media.sdp.base.media.MediaFactory;
import media.sdp.base.session.SessionDescriptionFactory;
import media.sdp.base.time.TimeDescriptionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sdp.*;
import java.util.Vector;

public class SdpParser {

    private static final Logger logger = LoggerFactory.getLogger(SdpParser.class);

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @fn public Sdp parseSdp (String callId, String sdpStr) throws Exception
     * @param sdpStr sdp message
     * @return Sdp
     */
    public Sdp parseSdp (String callId, String sdpStr) throws Exception {
        if (sdpStr == null || sdpStr.length() == 0) {
            return null;
        }

        Sdp sdp = new Sdp(callId);

        SDPAnnounceParser parser = new SDPAnnounceParser(sdpStr);
        SessionDescriptionImpl sdi = parser.parse();
        if (sdi.getVersion().getVersion() != 0) {
            logger.warn("({}) sdp version is not 0. sdp={}", callId, sdpStr);
            return null;
        }

        // 1) Session Description
        SessionDescriptionFactory sessionDescriptionFactory = new SessionDescriptionFactory(
                sdi.getVersion().getTypeChar(),
                sdi.getVersion().getVersion(),
                sdi.getOrigin().getTypeChar(),
                sdi.getOrigin().getUsername(),
                sdi.getOrigin().getAddress(),
                sdi.getOrigin().getAddressType(),
                sdi.getOrigin().getNetworkType(),
                sdi.getOrigin().getSessionId(),
                sdi.getOrigin().getSessionVersion(),
                sdi.getSessionName().getTypeChar(),
                sdi.getSessionName().getValue()
        );

        Connection connection = sdi.getConnection();
        if (connection != null) {
            sessionDescriptionFactory.setConnectionField(
                    connection.getTypeChar(),
                    connection.getAddress(),
                    connection.getAddressType(),
                    connection.getNetworkType()
            );
        }

        sdp.setSessionDescriptionFactory(sessionDescriptionFactory);
        //logger.debug("({}) Session Description=\n{}", callId, sdp.getSessionDescriptionFactory().getData());

        // 2) Time Description
        Vector tdVector = sdi.getTimeDescriptions(false);
        if (tdVector == null || tdVector.isEmpty()) {
            logger.warn("({}) sdp hasn't time description. sdp={}", callId, sdpStr);
            return null;
        }

        for (Object o : tdVector) {
            TimeDescription timeDescription = (TimeDescription) o;
            if (timeDescription != null) {
                String timeData = timeDescription.toString();

                int equalPos = timeData.indexOf("=");
                int spacePos = timeData.indexOf(" ");
                int crlfPos = timeData.indexOf("\r\n");

                String startTimeStr = timeData.substring(equalPos + 1, spacePos);
                String endTimeStr = timeData.substring(spacePos + 1, crlfPos);

                TimeDescriptionFactory timeDescriptionFactory = new TimeDescriptionFactory(
                        timeDescription.getTime().getTypeChar(),
                        startTimeStr,
                        endTimeStr
                );
                sdp.setTimeDescriptionFactory(timeDescriptionFactory);
                break;
            }
        }
        //logger.debug("({}) Time Description=\n{}", callId, sdp.getTimeDescriptionFactory().getData());

        // 3) Media Description
        Vector mdVector = sdi.getMediaDescriptions(false);
        if (mdVector == null || mdVector.isEmpty()) {
            logger.warn("({}) sdp hasn't media description. sdp={}", callId, sdpStr);
            return null;
        }

        MediaDescriptionFactory mediaDescriptionFactory = new MediaDescriptionFactory();

        for (Object o1 : mdVector) {
            MediaDescription md = (MediaDescription) o1;
            if (md != null) {
                Media media = md.getMedia();
                if (media != null && media.getMediaType().equals(Sdp.AUDIO)) {
                    // Media
                    MediaFactory mediaFactory = new MediaFactory(
                            media.getTypeChar(),
                            media.getMediaType(),
                            media.getMediaFormats(false),
                            media.getMediaPort(),
                            media.getProtocol(),
                            media.getPortCount()
                    );

                    // Bandwidth
                    Vector bwVector = md.getBandwidths(false);
                    if (bwVector != null && !bwVector.isEmpty()) {
                        for (Object o : bwVector) {
                            BandWidth bandWidth = (BandWidth) o;
                            if (bandWidth == null) {
                                continue;
                            }

                            mediaFactory.addBandwidthField(
                                    bandWidth.getTypeChar(),
                                    bandWidth.getType(),
                                    bandWidth.getValue()
                            );
                        }
                    }

                    // Connection
                    /*Connection mediaConnection = md.getConnection();
                    if (mediaConnection != null) {
                        mediaFactory.setConnectionField(
                                mediaConnection.getTypeChar(),
                                mediaConnection.getAddress(),
                                mediaConnection.getAddressType(),
                                mediaConnection.getNetworkType()
                        );
                    }*/

                    // Attributes
                    Vector adVector = md.getAttributes(false);
                    if (adVector == null || adVector.isEmpty()) {
                        logger.warn("({}) sdp hasn't attribute description. sdp={}", callId, sdpStr);
                        return null;
                    }

                    //for (int i = adVector.size() - 1; i >= 0; i--) {
                    for (Object o : adVector) {
                        Attribute attribute = (Attribute) o;
                        if (attribute == null) {
                            continue;
                        }

                        String attributeName = attribute.getName();
                        if (attributeName == null) {
                            continue;
                        }

                        if (attributeName.equals(MediaFactory.RTPMAP)) {
                            RtpMapAttributeFactory rtpMapAttributeFactory = new RtpMapAttributeFactory(attribute.getTypeChar(), attribute.getName(), attribute.getValue(), mediaFactory.getMediaField().getMediaFormats());
                            mediaFactory.addRtpAttributeFactory(rtpMapAttributeFactory);
                            //logger.debug("({}) [{}] RtpMapAttributeFactory=\n{}", callId, rtpMapAttributeFactory.getPayloadId(), rtpMapAttributeFactory.getData());
                        } else if (attributeName.equals(MediaFactory.FMTP)) {
                            FmtpAttributeFactory fmtpAttributeFactory = new FmtpAttributeFactory(attribute.getTypeChar(), attribute.getName(), attribute.getValue(), mediaFactory.getMediaField().getMediaFormats());
                            mediaFactory.addFmtpAttributeFactory(fmtpAttributeFactory);
                            //logger.debug("({}) [{}] FmtpAttributeFactory=\n{}", callId, fmtpAttributeFactory.getPayloadId(), fmtpAttributeFactory.getData());
                        } else {
                            AttributeFactory attributeFactory = new AttributeFactory(attribute.getTypeChar(), attribute.getName(), attribute.getValue(), mediaFactory.getMediaField().getMediaFormats());
                            mediaFactory.addAttributeFactory(attributeFactory);
                            //logger.debug("({}) [{}] AttributeFactory=\n{}", callId, attributeFactory.getPayloadId(), attributeFactory.getData());
                        }
                    }

                    mediaDescriptionFactory.addMediaFactory(mediaFactory);
                    //logger.debug("({}) Media=\n{}", callId, mediaFactory.getData(false));
                    break;
                }
            }
        }

        sdp.setMediaDescriptionFactory(mediaDescriptionFactory);
        //logger.debug("({}) Media Description=\n{}", callId, sdp.getMediaDescriptionFactory().getData(false));

        return sdp;
    }

}