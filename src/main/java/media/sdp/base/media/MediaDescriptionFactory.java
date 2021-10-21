package media.sdp.base.media;

import media.sdp.base.SdpFactory;
import media.sdp.base.attribute.RtpAttribute;
import media.sdp.base.attribute.base.RtpMapAttributeFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @class public class MediaDescriptionFactory
 * @brief MediaDescriptionFactory class
 */
public class MediaDescriptionFactory extends SdpFactory {

    private final Map<String, MediaFactory> mediaFactoryMap = new HashMap<>();

    ////////////////////////////////////////////////////////////////////////////////

    public MediaDescriptionFactory() {
        // Nothing
    }

    ////////////////////////////////////////////////////////////////////////////////

    public void addMediaFactory(MediaFactory mediaFactory) {
        if (mediaFactoryMap.get(mediaFactory.getMediaField().getMediaType()) != null) {
            return;
        }

        mediaFactoryMap.putIfAbsent(
                mediaFactory.getMediaField().getMediaType(),
                mediaFactory
        );
    }

    public MediaFactory getMediaFactory(String mediaType) {
        return mediaFactoryMap.get(mediaType);
    }

    public List<RtpAttribute> getIntersectedCodecList(String mediaType) {
        MediaFactory mediaFactory = mediaFactoryMap.get(mediaType);
        if (mediaFactory == null) {
            return Collections.emptyList();
        }

        return mediaFactory.getIntersectedCodecList();
    }

    public void setIntersectedCodecList(String mediaType, List<RtpAttribute> intersectedCodecList) {
        MediaFactory mediaFactory = mediaFactoryMap.get(mediaType);
        if (mediaFactory == null) {
            return;
        }

        mediaFactory.setIntersectedCodecList(intersectedCodecList);
    }

    public String getIntersectedDtmfPayloadId(String mediaType) {
        for (RtpAttribute rtpAttribute : getIntersectedCodecList(mediaType)) {
            if (rtpAttribute.getRtpMapAttributeFactory().getCodecName().equals(RtpMapAttributeFactory.DTMF)) {
                return rtpAttribute.getPayloadId();
            }
        }
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public String getData (boolean isRaw) {
        StringBuilder data = new StringBuilder();

        for (Map.Entry<String, MediaFactory> entry : mediaFactoryMap.entrySet()) {
            if (entry == null) {
                continue;
            }

            MediaFactory mediaFactory = entry.getValue();
            if (mediaFactory == null) {
                continue;
            }

            data.append(
                    mediaFactory.getData(
                            isRaw
                    )
            );
        }

        return data.toString();
    }

    ////////////////////////////////////////////////////////////////////////////////

    public List<RtpAttribute> getCodecList (String mediaType) {
        if (mediaFactoryMap.get(mediaType) != null) {
            return mediaFactoryMap.get(mediaType).getCodecList();
        }
        return Collections.emptyList();
    }

}
