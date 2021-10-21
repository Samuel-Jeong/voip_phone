package media.sdp.base;

import media.sdp.base.attribute.RtpAttribute;
import media.sdp.base.attribute.base.RtpMapAttributeFactory;
import media.sdp.base.field.ConnectionField;
import media.sdp.base.media.MediaDescriptionFactory;
import media.sdp.base.media.MediaFactory;
import media.sdp.base.session.SessionDescriptionFactory;
import media.sdp.base.time.TimeDescriptionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @class public class Sdp
 * @brief Sdp class
 *
 * RFC 4566
 */
public class Sdp {

    private static final Logger logger = LoggerFactory.getLogger(Sdp.class);

    public static final String AUDIO = "audio";
    public static final String VIDEO = "video";
    public static final String APPLICATION = "application";

    private final String id;

    private SessionDescriptionFactory sessionDescriptionFactory = null;
    private TimeDescriptionFactory timeDescriptionFactory = null;
    private MediaDescriptionFactory mediaDescriptionFactory = null;

    ////////////////////////////////////////////////////////////////////////////////

    public Sdp(String id) {
        this.id = id;
    }

    public Sdp cloneSdp(Sdp otherSdp) {
        Sdp newSdp = new Sdp(id);

        newSdp.setSessionDescriptionFactory(otherSdp.getSessionDescriptionFactory());
        newSdp.setTimeDescriptionFactory(otherSdp.getTimeDescriptionFactory());
        newSdp.setMediaDescriptionFactory(otherSdp.getMediaDescriptionFactory());

        return newSdp;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public String getId() {
        return id;
    }

    public SessionDescriptionFactory getSessionDescriptionFactory() {
        return sessionDescriptionFactory;
    }

    public void setSessionDescriptionFactory(SessionDescriptionFactory sessionDescriptionFactory) {
        this.sessionDescriptionFactory = sessionDescriptionFactory;
    }

    public TimeDescriptionFactory getTimeDescriptionFactory() {
        return timeDescriptionFactory;
    }

    public void setTimeDescriptionFactory(TimeDescriptionFactory timeDescriptionFactory) {
        this.timeDescriptionFactory = timeDescriptionFactory;
    }

    public MediaDescriptionFactory getMediaDescriptionFactory() {
        return mediaDescriptionFactory;
    }

    public void setMediaDescriptionFactory(MediaDescriptionFactory mediaDescriptionFactory) {
        this.mediaDescriptionFactory = mediaDescriptionFactory;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public String getData (boolean isRaw) {
        return sessionDescriptionFactory.getData() +
                timeDescriptionFactory.getData() +
                mediaDescriptionFactory.getData(isRaw);
    }

    ////////////////////////////////////////////////////////////////////////////////

    public void setSessionOriginAddressByOtherSdp(Sdp otherSdp) {
        if (otherSdp == null) {
            return;
        }

        String prevSessionAddress = this.sessionDescriptionFactory.getOriginField().getOriginAddress();
        this.sessionDescriptionFactory.getOriginField().
                setOriginAddress(
                        otherSdp.getSessionDescriptionFactory().getOriginField().
                                getOriginAddress()
                );
        logger.debug("({}) Session Origin Address is changed. ({} > {})",
                id, prevSessionAddress, this.sessionDescriptionFactory.getOriginField().getOriginAddress()
        );
    }

    public void setSessionOriginAddress(String originAddress) {
        this.sessionDescriptionFactory.getOriginField().setOriginAddress(originAddress);
    }

    public String getSessionOriginAddress() {
        return this.sessionDescriptionFactory.getOriginField().getOriginAddress();
    }

    public void setSessionConnectionAddressByOtherSdp(Sdp otherSdp) {
        if (otherSdp == null) {
            return;
        }

        ConnectionField connectionField = this.sessionDescriptionFactory.getConnectionField();
        if (connectionField == null) {
            return;
        }

        String prevConnectionAddress = connectionField.getConnectionAddress();
        connectionField.
                setConnectionAddress(
                        otherSdp.getSessionDescriptionFactory().getConnectionField().
                                getConnectionAddress()
                );

        logger.debug("({}) Session Connection Address is changed. ({} > {})",
                id,
                prevConnectionAddress,
                connectionField.getConnectionAddress()
        );
    }

    public void setSessionConnectionAddress(String connectionAddress) {
        ConnectionField connectionField = this.sessionDescriptionFactory.getConnectionField();
        if (connectionField == null) {
            return;
        }

        connectionField.setConnectionAddress(connectionAddress);
    }

    public String getSessionConnectionAddress() {
        ConnectionField connectionField = this.sessionDescriptionFactory.getConnectionField();
        if (connectionField == null) {
            return null;
        }

        return connectionField.getConnectionAddress();
    }

    public void setMediaConnectionAddressByOtherSdp(String mediaType, Sdp otherSdp) {
        if (mediaType == null || otherSdp == null) {
            return;
        }

        MediaFactory mediaFactory = this.mediaDescriptionFactory.getMediaFactory(mediaType);
        if (mediaFactory == null) {
            return;
        }

        MediaFactory otherMediaFactory = otherSdp.getMediaDescriptionFactory().getMediaFactory(mediaType);
        if (otherMediaFactory == null) {
            return;
        }

        ConnectionField connectionField = mediaFactory.getConnectionField();
        if (connectionField == null) {
            return;
        }

        ConnectionField otherConnectionField = otherMediaFactory.getConnectionField();
        if (otherConnectionField == null) {
            return;
        }

        String prevConnectionAddress = connectionField.getConnectionAddress();
        connectionField.setConnectionAddress(
                otherConnectionField.getConnectionAddress()
        );

        logger.debug("({}) Media ({}) Connection Address is changed. ({} > {})",
                id,
                mediaType,
                prevConnectionAddress,
                connectionField.getConnectionAddress()
        );
    }

    public void setMediaPort(String mediaType, int port) {
        if (mediaType == null || port <= 0) {
            return;
        }

        MediaFactory mediaFactory = this.mediaDescriptionFactory.getMediaFactory(mediaType);
        if (mediaFactory == null) {
            return;
        }

        int prevMediaPort = mediaFactory.getMediaField().getMediaPort();
        mediaFactory.getMediaField().setMediaPort(
                port
        );

        logger.debug("({}) Media port is changed. ({} > {})",
                id,
                prevMediaPort,
                port
        );
    }

    public int getMediaPort(String mediaType) {
        if (mediaType == null) {
            return -1;
        }

        MediaFactory mediaFactory = this.mediaDescriptionFactory.getMediaFactory(mediaType);
        if (mediaFactory == null) {
            return -1;
        }

        return mediaFactory.getMediaField().getMediaPort();
    }

    public void setMediaPortByOtherSdp(String mediaType, Sdp otherSdp) {
        if (mediaType == null || otherSdp == null) {
            return;
        }

        MediaFactory mediaFactory = this.mediaDescriptionFactory.getMediaFactory(mediaType);
        if (mediaFactory == null) {
            return;
        }

        MediaDescriptionFactory otherSdpMdFactory = otherSdp.getMediaDescriptionFactory();
        if (otherSdpMdFactory == null) {
            return;
        }
        MediaFactory otherMediaFactory = otherSdpMdFactory.getMediaFactory(mediaType);
        if (otherMediaFactory == null) {
            return;
        }

        int otherMediaPort = otherMediaFactory.getMediaField().getMediaPort();
        if (otherMediaPort <= 0) {
            return;
        }

        int prevMediaPort = mediaFactory.getMediaField().getMediaPort();
        mediaFactory.getMediaField().setMediaPort(
                otherMediaPort
        );

        logger.debug("({}) Media port is changed. ({} > {})",
                id,
                prevMediaPort,
                otherMediaPort
        );
    }

    public boolean isDtmfEnabled (String mediaType, boolean isIntersected) {
        if (mediaType == null) {
            return false;
        }

        boolean result = false;

        List<RtpAttribute> codecList;
        if (isIntersected) {
            codecList = this.mediaDescriptionFactory.getIntersectedCodecList(mediaType);
        } else {
            codecList = this.mediaDescriptionFactory.getCodecList(mediaType);
        }

        for (RtpAttribute rtpAttribute : codecList) {
            RtpMapAttributeFactory rtpMapAttributeFactory = rtpAttribute.getRtpMapAttributeFactory();
            if (rtpMapAttributeFactory == null) {
                continue;
            }

            if (rtpMapAttributeFactory.getCodecName().equals(RtpMapAttributeFactory.DTMF)) {
                result = true;
                break;
            }
        }

        return result;
    }

    public int getAttributeCount (String mediaType, boolean isIntersected, boolean isDtmf) {
        if (mediaType == null) {
            return 0;
        }

        int result = 0;

        List<RtpAttribute> codecList;
        if (isIntersected) {
            codecList = this.mediaDescriptionFactory.getIntersectedCodecList(mediaType);
        } else {
            codecList = this.mediaDescriptionFactory.getCodecList(mediaType);
        }

        for (RtpAttribute rtpAttribute : codecList) {
            RtpMapAttributeFactory rtpMapAttributeFactory = rtpAttribute.getRtpMapAttributeFactory();
            if (rtpMapAttributeFactory == null) {
                continue;
            }

            if (isDtmf) {
                if (rtpMapAttributeFactory.getCodecName().equals(RtpMapAttributeFactory.DTMF)) {
                    result++;
                }
            } else {
                if (!rtpMapAttributeFactory.getCodecName().equals(RtpMapAttributeFactory.DTMF)) {
                    result++;
                }
            }
        }

        return result;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public boolean intersect (String mediaType, Sdp otherSdp) {
        if (mediaType == null || otherSdp == null) {
            return false;
        }

        //int otherSdpDtmfAttributeCount = otherSdp.getAttributeCount(AUDIO, false, true);
        int otherSdpCodecAttributeCount = otherSdp.getAttributeCount(AUDIO, false, false);
        if (otherSdpCodecAttributeCount == 0) {
            logger.warn("({}) Other sdp has not any codec attributes. (sdp=\n{})", id, otherSdp.getData(true));
            return false;
        }

        List<RtpAttribute> intersectedCodecList = new ArrayList<>();
        List<RtpAttribute> otherSdpCodecList = otherSdp.getMediaDescriptionFactory().getIntersectedCodecList(AUDIO);
        if (otherSdpCodecList.isEmpty()) {
            otherSdpCodecList = otherSdp.getMediaDescriptionFactory().getCodecList(AUDIO);
        }

        // 현재 SDP 가 기준
        for (RtpAttribute curRtpAttribute : mediaDescriptionFactory. getCodecList(AUDIO)) {
            RtpMapAttributeFactory curRtpMapAttributeFactory = curRtpAttribute.getRtpMapAttributeFactory();
            if (curRtpMapAttributeFactory == null) {
                continue;
            }

            for (RtpAttribute otherRtpAttribute : otherSdpCodecList) {
                RtpMapAttributeFactory otherRtpMapAttributeFactory = otherRtpAttribute.getRtpMapAttributeFactory();
                if (otherRtpMapAttributeFactory == null) {
                    continue;
                }

                // 1) Codec Name Matched
                if (otherRtpMapAttributeFactory.getCodecName().equals(curRtpMapAttributeFactory.getCodecName())
                        && otherRtpMapAttributeFactory.getSamplingRate().equals(curRtpMapAttributeFactory.getSamplingRate())) {
                    //logger.debug("other: {}, cur: {}", otherAttributeFactory.getCodecName(), curAttributeFactory.getCodecName());
                    if (!intersectedCodecList.contains(curRtpAttribute)) {
                        intersectedCodecList.add(curRtpAttribute);
                    }
                    break;
                }
            }
        }

        if (!intersectedCodecList.isEmpty()) {
            this.mediaDescriptionFactory.setIntersectedCodecList(
                    mediaType,
                    intersectedCodecList
            );
            return true;
        } else {
            return false;
        }
    }


}
