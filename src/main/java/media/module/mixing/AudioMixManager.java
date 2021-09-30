package media.module.mixing;

import media.dtmf.DtmfUnit;
import media.module.mixing.base.AudioBuffer;
import media.module.mixing.base.AudioFrame;
import media.module.mixing.base.AudioMixer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @class public class AudioMixManager
 * @brief AudioMixManager class
 */
public class AudioMixManager {

    private static final Logger logger = LoggerFactory.getLogger(AudioMixManager.class);

    /* AudioMixManager Singleton object */
    private static AudioMixManager audioMixManager = null;

    /* Key: , Value: Mix File Name */
    private final HashMap<String, AudioMixer> audioMixerMap = new HashMap<>();
    private final ReentrantLock audioMixerMapLock = new ReentrantLock();

    //////////////////////////////////////////////////////////////////////

    /**
     * @fn public AudioMixManager()
     * @brief AudioMixManager 생성자 함수
     */
    public AudioMixManager() {
        // Nothing
    }

    /**
     * @fn public static AudioMixManager getInstance ()
     * @brief AudioMixManager Singleton 객체를 반환하는 함수
     * @return AUdioMixManager Singleton 객체를 반환
     */
    public static AudioMixManager getInstance () {
        if (audioMixManager == null) {
            audioMixManager = new AudioMixManager();
        }

        return audioMixManager;
    }

    //////////////////////////////////////////////////////////////////////

    /**
     * @fn public void perform (String mixerId, String bufferId, int samplingRate, byte[] rtpData)
     * @brief 오디오 데이터를 합성하기 위해 지정한 오디오 버퍼에 데이터를 추가하는 함수
     * @param mixerId Mixer ID
     * @param bufferId Buffer ID
     * @param samplingRate 리샘플링 타겟 Sampling rate
     * @param sampleSize 샘플 사이즈 (bit per sample)
     * @param channelSize Channel Size (1: mono, 2: stereo)
     * @param gain Gain of the source line
     * @param rtpData RTP Payload data (음성 데이터)
     */
    public void perform(String mixerId, String bufferId, int samplingRate, int sampleSize, int channelSize, short gain, byte[] rtpData) {
        if (mixerId == null || bufferId == null || samplingRate <= 0 || channelSize <= 0 || rtpData == null) {
            logger.warn("Fail to perform. Parameter error. (mixerId={}, bufferId={}, samplingRate={}, channelSize={}, rtpData={})", mixerId, bufferId, samplingRate, channelSize, rtpData);
            return;
        }

        AudioMixer audioMixer = getAudioMixer(mixerId);
        if (audioMixer == null) {
            logger.warn("Fail to get the audio mixer. (mixerId={})", mixerId);
            return;
        }

        AudioBuffer audioBuffer = audioMixer.getAudioBuffer(bufferId);
        if (audioBuffer == null) {
            logger.warn("Fail to get the audio buffer. (bufferId={})", bufferId);
            return;
        }


        // 2) Offer the data (Sampling-rate: 8000, Codec: PCM_SIGNED, Byte-order: little_endian)
        AudioFrame audioFrame = new AudioFrame(false);
        audioFrame.setData(rtpData, true);
        audioFrame.setGain(gain);
        audioFrame.setSamplingRate(samplingRate);
        audioFrame.setSampleSize(sampleSize);
        audioFrame.setChannelSize(channelSize);
        audioBuffer.offer(audioFrame);
    }

    //////////////////////////////////////////////////////////////////////

    /**
     * @fn public void addAudioMixer (String mixerId, String bufferId, String mixFileName, int samplingRate, int sampleSize)
     * @brief AudioMixer 와 AudioBuffer 를 새로 추가하는 함수
     * 1. mixerId 로 AudioMixer 를 추가, AudioMixer 는 Session 과 1:1 매칭 (mixerId == sessionId)
     * 2. bufferId 로 AudioBuffer 를 추가, AudioMixer 에 AudioBuffer 가 1:n 매칭 (bufferId == callId)
     * @param mixerId Mixer-ID
     * @param bufferId Buffer-ID
     * @param mixFileName 합성 녹취 파일 이름
     * @param samplingRate Codec Sampling rate
     * @param sampleSize Codec Sample size
     */
    public void addAudioMixer (String mixerId, String bufferId, String mixFileName, int samplingRate, int sampleSize, short channelSize) {
        if (mixerId == null || mixFileName == null || samplingRate <= 0 || sampleSize <= 0 || channelSize <= 0) {
            logger.warn("Fail to add new audio mixer. Parameter error. (mixerId={}, bufferId={}, mixFileName={}, samplingRate={}, sampleSize={}, channelSize={})", mixerId, bufferId, mixFileName, samplingRate, sampleSize, channelSize);
            return;
        }

        try {
            audioMixerMapLock.lock();

            AudioMixer audioMixer = audioMixerMap.get(mixerId);
            if (audioMixer == null) {
                audioMixerMap.putIfAbsent(
                        mixerId,
                        new AudioMixer(
                                mixFileName,
                                samplingRate,
                                sampleSize,
                                channelSize
                        )
                );

                audioMixer = audioMixerMap.get(mixerId);
                audioMixer.start();
                logger.debug("Success to add the audio mixer. (mixerId={})", mixerId);
            }

            if (audioMixer.getAudioBuffer(bufferId) == null) {
                audioMixer.addAudioBuffer(
                        bufferId
                );
                logger.debug("Success to add the audio buffer. (mixerId={}, bufferId={})", mixerId, bufferId);
            } else {
                logger.debug("Fail to add the audio buffer. (mixerId={}, bufferId={})", mixerId, bufferId);
            }
        } catch (Exception e) {
            logger.warn("AudioMixManager.addAudioMixer.Exception", e);
        } finally {
            audioMixerMapLock.unlock();
        }
    }

    /**
     * @fn public AudioMixer getAudioMixer (String mixerId)
     * @brief 지정한 mixerId 로 AudioMixer 객체를 반환하는 함수
     * @param mixerId Mixer-ID
     * @return 성공 시 AudioMixer 객체, 실패 시 null 반환
     */
    public AudioMixer getAudioMixer (String mixerId) {
        if (mixerId == null) {
            return null;
        }

        try {
            audioMixerMapLock.lock();

            return audioMixerMap.get(mixerId);
        } catch (Exception e) {
            logger.warn("AudioMixManager.getAudioMixer.Exception", e);
            return null;
        } finally {
            audioMixerMapLock.unlock();
        }
    }

    /**
     * @fn public void removeAudioMixer (String mixerId, String bufferId)
     * @brief 지정한 mixerId 로 AudioMixer 객체를 삭제하는 함수
     * 1. Buffer-ID 에 해당하는 AudioBuffer 도 삭제
     * 2. 지정한 AudioMixer 에 등록된 AudioBuffer 가 모두 삭제되면 AudioMixer 도 삭제
     * @param mixerId Mixer-ID
     * @param bufferId Buffer-ID
     */
    public void removeAudioMixer (String mixerId, String bufferId) {
        if (mixerId == null || bufferId == null) {
            return;
        }

        try {
            audioMixerMapLock.lock();

            AudioMixer audioMixer = audioMixerMap.get(mixerId);
            if (audioMixer == null) {
                return;
            }

            if (audioMixer.removeAudioBuffer(bufferId) != null) {
                logger.debug("Success to delete the audio buffer. (mixerId={}, bufferId={})", mixerId, bufferId);
            }

            if (audioMixer.getAudioBufferMapSize() == 0) {
                audioMixer.stop();
                audioMixerMap.remove(mixerId);
                logger.debug("Success to delete the audio mixer. (mixerId={})", mixerId);
            }
        } catch (Exception e) {
            logger.warn("AudioMixManager.removeAudioMixer.Exception", e);
        } finally {
            audioMixerMapLock.unlock();
        }
    }

}
