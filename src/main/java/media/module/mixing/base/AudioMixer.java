package media.module.mixing.base;

import config.ConfigManager;
import media.MediaManager;
import media.module.codec.amr.AmrManager;
import media.module.codec.evs.EvsManager;
import media.module.codec.pcm.ALawTranscoder;
import media.module.codec.pcm.ULawTranscoder;
import media.record.RecordManager;
import media.record.wav.WavFileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AppInstance;
import service.TaskManager;
import service.base.TaskUnit;
import signal.module.CallManager;

import javax.sound.sampled.AudioFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @class public class AudioMixer
 * @brief AudioMixer class
 */
public class AudioMixer {

    private static final Logger logger = LoggerFactory.getLogger(AudioMixer.class);

    /* Key: Buffer-ID (using call-id), value: AudioBuffer object */
    private final HashMap<String, AudioBuffer> audioBufferMap = new HashMap<>();
    private final ReentrantLock audioBufferMapLock = new ReentrantLock();

    /* RecordManager */
    private RecordManager mixRecordManager = null;

    /* 합성 파일 이름 */
    private final String mixFileName;
    /* Codec Sampling rate */
    private final int samplingRate;
    /* Codec sample size per block (one block align) */
    private final int sampleSize;
    /* Channel size */
    private final short channelSize;
    /* Cur RTP Payload Length */
    private int curRtpPayloadLength = 0;

    private final String[] evsDecArgv;
    private final ConcurrentCyclicFIFO<byte[]> recvBuffer = new ConcurrentCyclicFIFO<>();
    private ScheduledThreadPoolExecutor recvTaskExecutor;

    //////////////////////////////////////////////////////////////////////

    /**
     * @fn public AudioMixer(String mixFileName, int samplingRate, int sampleSize)
     * @brief AudioMixer 생성자
     * @param mixFileName 합성 파일 이름
     * @param samplingRate Codec Sampling rate
     * @param sampleSize Codec sample size
     * @param channelSize Channel size
     */
    public AudioMixer(String mixFileName, int samplingRate, int sampleSize, short channelSize) {
        this.mixFileName = mixFileName;
        this.samplingRate = samplingRate;
        this.sampleSize = sampleSize;
        this.channelSize = channelSize;

        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        if (configManager.isRawFile()) {
            mixRecordManager = new RecordManager(
                    configManager.getRecordPath(),
                    //20
                    MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_WB)? 40 : 20
            );
        }

        evsDecArgv = new String[]{
                "EVS_dec.exe",
                //"-q",
                //"-VOIP",
                //"-NO_DELAY_CMP",
                "8",
                "none",
                "none"
        };

        logger.debug("AudioMixer is created. (mixFileName={}, samplingRate={})",
                mixFileName, samplingRate
        );
    }

    //////////////////////////////////////////////////////////////////////

    /**
     * @fn public void start ()
     * @brief AudioMixer 로직을 시작하는 함수
     * 합성 파일 스트림을 열고, 음성 데이터(pcm)를 쓰기 전에 wave 파일 헤더를 추가한다.
     */
    public void start () {
        try {
            if (mixRecordManager != null) {
                mixRecordManager.start();
                mixRecordManager.openFileStream(mixFileName, true, true);
                mixRecordManager.writeFileStream(
                        WavFileInfo.getHeader(
                                channelSize,
                                MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_WB) ? 16000 : 8000,
                                sampleSize
                        )
                );
            }

            if (recvTaskExecutor == null) {
                recvTaskExecutor = new ScheduledThreadPoolExecutor(5);

                try {
                    RecvTask recvTask = new RecvTask(
                            1
                            //MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_WB) ? 40 : 20
                    );

                    recvTaskExecutor.scheduleAtFixedRate(
                            recvTask,
                            recvTask.getInterval(),
                            recvTask.getInterval(),
                            TimeUnit.MILLISECONDS
                    );
                } catch (Exception e) {
                    logger.warn("AudioMixer.start.executor.Exception", e);
                    return;
                }

                logger.debug("AudioMixer RecvTask is added.");
            }

            TaskManager.getInstance().addTask(
                    MixTask.class.getSimpleName() + "_" + mixFileName,
                    new MixTask(
                            MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_WB) ? 40 : 20
                    )
            );

            switch (MediaManager.getInstance().getPriorityCodec()) {
                case MediaManager.EVS:
                    EvsManager.getInstance().startAudioMixerTask(evsDecArgv, recvBuffer);
                    break;
                case MediaManager.AMR_NB:
                    AmrManager.getInstance().startDecAmrNb();
                    break;
                case MediaManager.AMR_WB:
                    AmrManager.getInstance().startDecAmrWb();
                    break;
                default:
                    break;
            }

            curRtpPayloadLength = MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_WB) ? 640 : 320;
        } catch (Exception e) {
            logger.warn("AudioMixer.start.Exception", e);
        }
    }

    /**
     * @fn public void stop ()
     * @brief AudioMixer 로직을 중지하는 함수
     * 합성 파일 스트림을 닫고, 최종적으로 wave 파일의 chunk size 를 (헤더에) 기록한다.
     */
    public void stop () {
        TaskManager.getInstance().removeTask(MixTask.class.getSimpleName() + "_" + mixFileName);

        recvBuffer.clear();
        switch (MediaManager.getInstance().getPriorityCodec()) {
            case MediaManager.EVS:
                EvsManager.getInstance().stopAudioMixerTask();
                break;
            case MediaManager.AMR_NB:
                AmrManager.getInstance().stopDecAmrNb();
                break;
            case MediaManager.AMR_WB:
                AmrManager.getInstance().stopDecAmrWb();
                break;
            default:
                break;
        }

        if (recvTaskExecutor != null) {
            recvTaskExecutor.shutdown();
            recvTaskExecutor = null;
            logger.debug("AudioMixer RecvTask is removed.");
        }

        if (mixRecordManager != null) {
            mixRecordManager.stop();
            mixRecordManager.closeFileStream();
            WavFileInfo.setChunkSizeInFile(
                    mixFileName,
                    mixRecordManager.getTotalDataSize() * getChannelSize() * getSampleSize() / 8
            );

            // Remove noise
            /*try {
                AudioNoiseRemover audioNoiseRemover = new AudioNoiseRemover(getSamplingRate());
                byte[] totalBytes = Files.readAllBytes(Paths.get(mixFileName));

                mixRecordManager.openFileStream(mixFileName, true, true);
                mixRecordManager.writeFileStream(
                        WavFile.getHeader(
                                channelSize,
                                MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_WB)? 16000 : 8000,
                                sampleSize
                        )
                );

                ArrayList<byte[]> array512Bytes = new ArrayList<>();
                int totalBytesLen = totalBytes.length;

                for (int curBytesIndex = 0; curBytesIndex < totalBytesLen; curBytesIndex += 512) {
                    byte[] bytes512 = new byte[512];

                    if ((curBytesIndex + 512) > totalBytesLen) {
                        int remainBytesLen = totalBytesLen - curBytesIndex;
                        System.arraycopy(totalBytes, curBytesIndex, bytes512, 0, remainBytesLen);
                        array512Bytes.add(bytes512);
                        break;
                    }

                    System.arraycopy(totalBytes, curBytesIndex, bytes512, 0, 512);
                    array512Bytes.add(bytes512);
                }

                for (byte[] curBytes : array512Bytes) {
                    AudioFrame audioFrame = new AudioFrame();
                    audioFrame.setData(curBytes);

                    int[] convertedBytes = audioNoiseRemover.removeNoise(
                            audioFrame.getConvertedData()
                    );

                    audioFrame.setConvertedData(convertedBytes);

                    curBytes = audioFrame.getData(false);
                    mixRecordManager.writeFileStream(curBytes);
                }

                mixRecordManager.closeFileStream();
                WavFile.setChunkSizeInFile(
                        mixFileName,
                        mixRecordManager.getTotalDataSize() * getChannelSize() * getSampleSize() / 8
                );

                mixRecordManager = null;
            } catch (Exception e) {
                logger.warn("stop.Files.readAllBytes.Exception", e);
            }*/
        }
    }

    //////////////////////////////////////////////////////////////////////

    /**
     * @fn public int getAudioBufferMapSize ()
     * @brief AudioBuffer map 크기를 반환하는 함수
     * @return AudioBuffer map 크기 (등록된 AudioBuffer 개수)
     */
    public int getAudioBufferMapSize () {
        try {
            audioBufferMapLock.lock();

            return audioBufferMap.size();
        } catch (Exception e) {
            logger.warn("AudioMixer.getAudioBufferMapSize.Exception", e);
            return 0;
        } finally {
            audioBufferMapLock.unlock();
        }
    }

    /**
     * @fn public void addAudioBuffer (String bufferId)
     * @brief 지정한 Buffer-ID 에 해당하는 AudioBuffer 객체를 새로 추가하는 함수
     * @param bufferId Buffer-ID (using call-id)
     */
    public void addAudioBuffer (String bufferId) {
        try {
            audioBufferMapLock.lock();

            audioBufferMap.putIfAbsent(
                    bufferId,
                    new AudioBuffer(bufferId)
            );
        } catch (Exception e) {
            logger.warn("AudioMixer.addAudioBuffer.Exception", e);
        } finally {
            audioBufferMapLock.unlock();
        }
    }

    /**
     * @fn public AudioBuffer getAudioBuffer (String bufferId)
     * @brief 지정한 Buffer-ID 에 해당하는 AudioBuffer 객체를 반환하는 함수
     * @param bufferId Buffer-ID (using call-id)
     * @return 성공 시 AudioBuffer 객체, 실패 시 null 반환
     */
    public AudioBuffer getAudioBuffer (String bufferId) {
        try {
            audioBufferMapLock.lock();

            return audioBufferMap.get(bufferId);
        } catch (Exception e) {
            logger.warn("AudioMixer.getAudioBuffer.Exception", e);
            return null;
        } finally {
            audioBufferMapLock.unlock();
        }
    }

    /**
     * @fn public void removeAudioBuffer (String bufferId)
     * @brief 지정한 Buffer-ID 에 해당하는 AudioBuffer 객체를 삭제하는 함수
     * @param bufferId Buffer-ID (using call-id)
     * @return 성공 시 삭제된 AudioBuffer 객체, 실패 시 null 반환
     */
    public AudioBuffer removeAudioBuffer (String bufferId) {
        try {
            audioBufferMapLock.lock();

            return audioBufferMap.remove(bufferId);
        } catch (Exception e) {
            logger.warn("AudioMixer.removeAudioBuffer.Exception", e);
            return null;
        } finally {
            audioBufferMapLock.unlock();
        }
    }

    /**
     * @fn public Map<String, AudioBuffer> getCloneAudioBufferMap()
     * @brief AudioBuffer map 을 clone 하는 함수
     * shallow copy 가 아닌 deep copy 를 통해 concurrent exception 을 사전에 차단한다.
     * @return 성공 시 클론된(깊은 복사된, 포인터 및 값도 같이 복사된) AudioBuffer map, 실패 시(예외 발생 시) 얕은 복사된(포인터만 복사된) AudioBuffer map 반환
     */
    public Map<String, AudioBuffer> getCloneAudioBufferMap() {
        HashMap<String, AudioBuffer> cloneMap;

        try {
            audioBufferMapLock.lock();

            try {
                cloneMap = (HashMap<String, AudioBuffer>) audioBufferMap.clone();
            } catch (Exception e) {
                logger.warn("Fail to clone the audio buffer map.");
                cloneMap = audioBufferMap;
            }
        } catch (Exception e) {
            logger.warn("AudioMixer.getCloneAudioBufferMap.Exception", e);
            return null;
        } finally {
            audioBufferMapLock.unlock();
        }

        return cloneMap;
    }

    /**
     * @fn public String getMixFileName()
     * @brief 합성 파일 이름을 반환하는 함수
     * @return 합성 파일 이름을 반환
     */
    public String getMixFileName() {
        return mixFileName;
    }

    /**
     * @fn public int getSamplingRate()
     * @brief Codec Sampling rate 를 반환하는 함수
     * @return Codec Sampling rate 를 반환
     */
    public int getSamplingRate() {
        return samplingRate;
    }

    /**
     * @fn public int getSampleSize()
     * @brief Codec Sample size 를 반환하는 함수
     * @return Codec Sample size 를 반환
     */
    public int getSampleSize() {
        return sampleSize;
    }

    /**
     * @fn public int getChannelSize()
     * @brief Channel size 를 반환하는 함수
     * @return Channel size 를 반환
     */
    public short getChannelSize() {
        return channelSize;
    }

    //////////////////////////////////////////////////////////////////////

    private class RecvTask extends TaskUnit {

        protected RecvTask(int interval) {
            super(interval);
        }

        @Override
        public void run() {
            if (getAudioBufferMapSize() == 0) {
                return;
            }

            for (Map.Entry<String, AudioBuffer> entry : getCloneAudioBufferMap().entrySet()) {
                AudioBuffer audioBuffer = entry.getValue();
                if (audioBuffer == null) {
                    continue;
                }

                AudioFrame audioFrame = audioBuffer.evolve();
                if (audioFrame == null) {
                    continue;
                }

                // 1) Priority Codec 에 따라 디코딩하여 PCM 데이터로 변환
                byte[] data = audioFrame.getData(true);
                if (data == null || data.length == 0 || isByteArrayFullOfZero(data)) {
                    return;
                }

                // ALAW
                if (MediaManager.getInstance().getPriorityCodec().equals(MediaManager.ALAW)) {
                    data = ALawTranscoder.decode(
                            data
                    );
                }
                // ULAW
                else if (MediaManager.getInstance().getPriorityCodec().equals(MediaManager.ULAW)) {
                    data = ULawTranscoder.decode(
                            data
                    );
                }
                // EVS
                else if (MediaManager.getInstance().getPriorityCodec().equals(MediaManager.EVS)) {
                    EvsManager.getInstance().addAudioMixerInputData(data);
                    return;
                }
                // AMR-NB
                else if (MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_NB)) {
                    data = AmrManager.getInstance().decAmrNb(
                            curRtpPayloadLength,
                            data
                    );
                }
                // AMR-WB
                else if (MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_WB)) {
                    data = AmrManager.getInstance().decAmrWb(
                            curRtpPayloadLength,
                            data
                    );
                }

                if (data == null) {
                    return;
                }

                recvBuffer.offer(data);
            }
        }
    }

    /**
     * @class private class MixTask extends TaskUnit
     * @brief 활성화된 AudioBuffer 에 저장된 음성 데이터(pcm)를 모두 하나의 음셩 데이터로 합성하는 클래스
     * 1. 하나의 WAVE 파일로 녹취 및 저장
     * 2. V_{Session-ID}_mix.wav 파일 이름으로 저장
     * 3. Session-ID 는 B2BUA Proxy 에서 관리하는 여러 개의 호가 참여한 세션 식별값
     */
    private class MixTask extends TaskUnit {

        /* 전체 합성 음성 데이터 */
        private int[] totalBytes;
        /* 현재 음성 데이터 합성 횟수 */
        private int sourcesCount = 0;
        /* 패킷 전송 주기 */
        //private final long period = 20000000L; // 20 ms
        /* 합성 파일에 저장될 바이트 데이터의 단위 크기 */
        //private final int PACKET_SIZE = (int) (period / 1000000) * getSamplingRate() / 1000 * getSampleSize() / 8;

        ////////////////////////////////////////////////////////////////////////////////

        /**
         * @fn protected MixTask(int interval)
         * @brief MixTask 생성자 함수
         * @param interval Task interval
         */
        protected MixTask(int interval) {
            super(interval);

            //logger.warn("MixTask: packetSize={}", PACKET_SIZE);
        }

        ////////////////////////////////////////////////////////////////////////////////

        /**
         * @fn public void run()
         * @brief MixTask 의 비즈니스 로직을 수행하는 함수
         */
        @Override
        public void run() {
            try {
                byte[] data = recvBuffer.poll();
                if (data == null || data.length == 0 || isByteArrayFullOfZero(data)) {
                    return;
                }

                //logger.debug("AudioMixer.MixTask > data.length: {}", data.length);

                AudioFrame audioFrame = new AudioFrame(false);
                audioFrame.setData(data, false);
                mix(audioFrame.getConvertedData());
            } catch (Exception e) {
                logger.warn("MixTask.Exception", e);
            }
        }

        ////////////////////////////////////////////////////////////////////////////////

        /**
         * @fn public void mix (int[] currentData)
         * @brief 활성화된 여러 음성 데이터들을 하나의 음성 데이터로 합성하고 합성 파일에 기록하는 함수
         * Audio Clipping 포함
         * @param currentData int array
         */
        public void mix (int[] currentData) {
            if (currentData == null) {
                return;
            }

            // 1) Copy & Mix the current data to totalBytes.
            int index;

            int curPacketSize;
            if (sourcesCount == 0) {
                curPacketSize = currentData.length;
                totalBytes = new int[curPacketSize];
                System.arraycopy(currentData, 0, totalBytes, 0, currentData.length);
            } else {
                if (totalBytes.length < currentData.length) {
                    curPacketSize = currentData.length;
                    int[] newBytes = new int[curPacketSize];
                    System.arraycopy(totalBytes, 0, newBytes, 0, totalBytes.length);
                    totalBytes = newBytes;
                }

                /////////////////////////////////////////////////////////////////
                // Audio Clipping (by float)
                for (index = 0; index < currentData.length; index++) {
                    float result;
                    float curPcm = (float) totalBytes[index] / 0x8000;
                    float newPcm = (float) currentData[index] / 0x8000;

                    result = curPcm + newPcm;
                    if (result > 1.0f) {
                        result = 1.0f;
                    } else if (result < -1.0f) {
                        result = -1.0f;
                    }

                    result *= 0x8000;
                    totalBytes[index] = Math.round(result);
                }
            }
            sourcesCount++;

            if (sourcesCount == 0) {
                return;
            }

            // 2) If sources are equal to the call size, record the total data.
            if (sourcesCount >= CallManager.getInstance().getCallMapSize()) {
                // Audio-Clipping (ByteOrder: little-endian)
                int outputIndex = 0;
                byte[] dataArray = new byte[totalBytes.length * 2];
                for (int outputCount = 0; outputCount < totalBytes.length;) {
                    dataArray[outputIndex++] = (byte) (totalBytes[outputCount]);
                    dataArray[outputIndex++] = (byte) (totalBytes[outputCount++] >> 8);
                }

                mixRecordManager.addData(dataArray);
                //mixRecordManager.writeFileStream(dataArray);

                sourcesCount = 0;
            }
        }
    }

    private boolean isByteArrayFullOfZero(byte[] data) {
        for (byte datum : data) {
            if (datum != 0) {
                return false;
            }
        }
        return true;
    }

}
