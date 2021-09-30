package media.module.codec.evs;

import client.VoipClient;
import client.module.base.MediaFrame;
import media.module.codec.evs.base.EvsTaskUnit;
import media.module.mixing.base.ConcurrentCyclicFIFO;
import media.record.RecordManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.base.TaskUnit;

import javax.print.attribute.standard.Media;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @class public class EvsManager
 * @brief EvsManager class
 */
public class EvsManager {

    private static final Logger logger = LoggerFactory.getLogger(EvsManager.class);

    private final int EVS_ENC_DATA_SIZE = 324;
    private final int EVS_DEC_DATA_SIZE = 320;

    private static EvsManager evsManager = null;

    private EvsTaskUnit udpSenderTaskUnit = null;
    private EvsTaskUnit udpReceiverTaskUnit = null;
    private EvsTaskUnit audioMixerTaskUnit = null;

    ////////////////////////////////////////////////////////////////////////////////

    public EvsManager() {
        //String curUserHomeDir = System.getProperty("user.home");
        String curUserDir = System.getProperty("user.dir");
        System.load(curUserDir + "/src/main/resources/lib/evs/libevsjni.so");
    }

    public static EvsManager getInstance () {
        if (evsManager == null) {
            evsManager = new EvsManager();

        }
        return evsManager;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // JNI

    public native byte[] enc_evs(String[] args, byte[] src_data);
    public byte[] encEvs(String[] args, byte[] srcData) {
        return enc_evs(args, srcData);
    }

    public native byte[] dec_evs(String[] args, int dst_data_len, byte[] src_data);
    public byte[] decEvs(String[] args, int dstDataLen, byte[] src_data) {
        return dec_evs(args, dstDataLen, src_data);
    }

    ////////////////////////////////////////////////////////////////////////////////

    public void startUdpSenderTask(ConcurrentCyclicFIFO<MediaFrame> outputBuffer) {
        if (udpSenderTaskUnit == null) {
            udpSenderTaskUnit = new EvsTaskUnit(10);
            udpSenderTaskUnit.setOutputMediaFrameBuffer(outputBuffer);

            ScheduledThreadPoolExecutor udpSenderTaskExecutor = udpSenderTaskUnit.getTaskExecutor();
            if (udpSenderTaskExecutor == null) {
                udpSenderTaskExecutor = new ScheduledThreadPoolExecutor(5);

                try {
                    UdpSenderTask udpSenderTask = new UdpSenderTask(
                            20,
                            udpSenderTaskUnit
                    );

                    udpSenderTaskExecutor.scheduleAtFixedRate(
                            udpSenderTask,
                            udpSenderTask.getInterval(),
                            udpSenderTask.getInterval(),
                            TimeUnit.MILLISECONDS
                    );
                } catch (Exception e) {
                    logger.warn("EvsManager.startUdpSenderTask.Exception", e);
                }

                logger.debug("EvsManager UdpSenderTask is added.");
            }
        }
    }

    public void stopUdpSenderTask() {
        if (udpSenderTaskUnit != null) {
            udpSenderTaskUnit.clearInputBuffer();

            ScheduledThreadPoolExecutor udpSenderTaskExecutor = udpSenderTaskUnit.getTaskExecutor();
            if (udpSenderTaskExecutor != null) {
                udpSenderTaskUnit.setOutputMediaFrameBuffer(null);
                udpSenderTaskExecutor.shutdown();
                logger.debug("EvsManager UdpSenderTask is removed.");
            }

            udpSenderTaskUnit = null;
        }
    }

    public void addUdpSenderInputData(byte[] data) {
        if (udpSenderTaskUnit == null) {
            return;
        }

        if (data.length != EVS_DEC_DATA_SIZE) {
            logger.warn("UdpSender input data length is not [{}]. (length={})", EVS_DEC_DATA_SIZE, data.length);
            return;
        }

        udpSenderTaskUnit.getInputBuffer().offer(data);
    }

    ////////////////////////////////////////////////////////////////////////////////

    public void startUdpReceiverTask(ConcurrentCyclicFIFO<MediaFrame> outputBuffer) {
        if (udpReceiverTaskUnit == null) {
            udpReceiverTaskUnit = new EvsTaskUnit(10);
            udpReceiverTaskUnit.setOutputMediaFrameBuffer(outputBuffer);

            ScheduledThreadPoolExecutor udpReceiverTaskExecutor = udpReceiverTaskUnit.getTaskExecutor();
            if (udpReceiverTaskExecutor == null) {
                udpReceiverTaskExecutor = new ScheduledThreadPoolExecutor(5);

                try {
                    UdpReceiverTask udpReceiverTask = new UdpReceiverTask(
                            20,
                            udpReceiverTaskUnit
                    );

                    udpReceiverTaskExecutor.scheduleAtFixedRate(
                            udpReceiverTask,
                            udpReceiverTask.getInterval(),
                            udpReceiverTask.getInterval(),
                            TimeUnit.MILLISECONDS
                    );
                } catch (Exception e) {
                    logger.warn("EvsManager.startUdpReceiverTask.Exception", e);
                }

                logger.debug("EvsManager UdpReceiverTask is added.");
            }
        }
    }

    public void stopUdpReceiverTask() {
        if (udpReceiverTaskUnit != null) {
            udpReceiverTaskUnit.clearInputBuffer();

            ScheduledThreadPoolExecutor udpReceiverTaskExecutor = udpReceiverTaskUnit.getTaskExecutor();
            if (udpReceiverTaskExecutor != null) {
                udpReceiverTaskUnit.setOutputMediaFrameBuffer(null);
                udpReceiverTaskExecutor.shutdown();
                logger.debug("EvsManager UdpReceiverTask is removed.");
            }

            udpReceiverTaskUnit = null;
        }
    }

    public void addUdpReceiverInputData(byte[] data) {
        if (udpReceiverTaskUnit == null) {
            return;
        }

        /*if (data.length != udpReceiverTaskUnit.getDataSize()) {
            logger.warn("UdpReceiver input data length is not [{}]. (length={})", udpReceiverTaskUnit.getDataSize(), data.length);
            return;
        }*/

        //logger.debug("addUdpReceiverInputData: data.length={}", data.length);
        udpReceiverTaskUnit.getInputBuffer().offer(data);
    }

    ////////////////////////////////////////////////////////////////////////////////

    public void startAudioMixerTask(String[] evsDecArgv, ConcurrentCyclicFIFO<byte[]> outputBuffer) {
        if (evsDecArgv == null || outputBuffer == null) {
            return;
        }

        if (audioMixerTaskUnit == null) {
            audioMixerTaskUnit = new EvsTaskUnit(10);
            audioMixerTaskUnit.setOutputByteBuffer(outputBuffer);

            ScheduledThreadPoolExecutor audioMixerTaskExecutor = audioMixerTaskUnit.getTaskExecutor();
            if (audioMixerTaskExecutor == null) {
                audioMixerTaskExecutor = new ScheduledThreadPoolExecutor(5);

                try {
                    AudioMixerTask audioMixerTask = new AudioMixerTask(
                            20,
                            audioMixerTaskUnit,
                            evsDecArgv
                    );

                    audioMixerTaskExecutor.scheduleAtFixedRate(
                            audioMixerTask,
                            audioMixerTask.getInterval(),
                            audioMixerTask.getInterval(),
                            TimeUnit.MILLISECONDS
                    );
                } catch (Exception e) {
                    logger.warn("EvsManager.startAudioMixerTask.Exception", e);
                }

                logger.debug("EvsManager AudioMixerTask is added.");
            }
        }
    }

    public void stopAudioMixerTask() {
        if (audioMixerTaskUnit != null) {
            audioMixerTaskUnit.clearInputBuffer();

            ScheduledThreadPoolExecutor audioMixerTaskExecutor = audioMixerTaskUnit.getTaskExecutor();
            if (audioMixerTaskExecutor != null) {
                audioMixerTaskUnit.setOutputByteBuffer(null);
                audioMixerTaskExecutor.shutdown();
                logger.debug("EvsManager AudioMixerTask is removed.");
            }

            audioMixerTaskUnit = null;
        }
    }

    public void addAudioMixerInputData(byte[] data) {
        if (audioMixerTaskUnit == null) {
            return;
        }

        /*if (data.length != audioMixerTaskUnit.getDataSize()) {
            logger.warn("AudioMixer input data length is not [{}]. (length={})", audioMixerTaskUnit.getDataSize(), data.length);
            return;
        }*/

        //logger.debug("addAudioMixerInputData: data.length={}", data.length);
        audioMixerTaskUnit.getInputBuffer().offer(data);
    }

    ////////////////////////////////////////////////////////////////////////////////

    private class UdpSenderTask extends TaskUnit {

        final EvsTaskUnit udpSenderTaskUnit;
        int curDataCount = 0;
        int totalDataLength = 0;

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        /////////////////////////////////////////////

        protected UdpSenderTask(int interval, EvsTaskUnit udpSenderTaskUnit) {
            super(interval);
            this.udpSenderTaskUnit = udpSenderTaskUnit;
            logger.debug("udpSenderTaskUnit: {}", udpSenderTaskUnit);
        }

        @Override
        public void run() {
            if (udpSenderTaskUnit == null) {
                logger.warn("udpSenderTaskUnit is null.");
                return;
            }

            try {
                byte[] curData = udpSenderTaskUnit.getInputBuffer().poll();
                if (curData == null || curData.length == 0) {
                    //logger.warn("UdpSenderTask > curData is null.");
                    return;
                }

                // 10 개씩 모아서 encode
                byteArrayOutputStream.write(curData);
                //System.arraycopy(curData, 0, totalData, totalDataLength, udpSenderTaskUnit.getDataSize());
                totalDataLength += EVS_DEC_DATA_SIZE;

                curDataCount++;
                if (curDataCount < udpSenderTaskUnit.getMergeCount()) {
                    //logger.debug("({}) udpSenderTaskUnit > curDataSize={} (input)", curDataCount, curData.length);
                    return;
                }

                byte[] totalData = byteArrayOutputStream.toByteArray(); // 3200 ([320 * 10])
                byte[] newData = EvsManager.getInstance().encEvs( // 3240 ([320 * 10] + [4 * 10])
                        VoipClient.getInstance().getEvsEncArgv(),
                        totalData
                );

                if (newData != null) {
                    // record
                    RecordManager evsRecordManager = VoipClient.getInstance().getTargetEvsRecordManager();
                    if (evsRecordManager != null) {
                        evsRecordManager.addData(newData);
                        //evsRecordManager.writeFileStream(currentByteBuffer);
                    }

                    // send
                    totalDataLength = 0;
                    if (udpSenderTaskUnit.getOutputMediaFrameBuffer() != null) {
                        for (int i = 0; i < curDataCount; i++) {
                            curData = new byte[EVS_ENC_DATA_SIZE];
                            System.arraycopy(newData, totalDataLength, curData, 0, EVS_ENC_DATA_SIZE);
                            udpSenderTaskUnit.getOutputMediaFrameBuffer().offer(
                                    new MediaFrame(
                                            false,
                                            curData
                                    )
                            );
                            totalDataLength += EVS_ENC_DATA_SIZE;
                        }
                    }
                }

                // clear
                Arrays.fill(totalData, (byte) 0);
                totalDataLength = 0;
                curDataCount = 0;
                byteArrayOutputStream.reset();
            } catch (Exception e) {
                logger.warn("UdpSenderTask.run.Exception", e);
            } finally {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    logger.warn("UdpSenderTask.run.IOException", e);
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

    private class UdpReceiverTask extends TaskUnit {

        final EvsTaskUnit udpReceiverTaskUnit;
        int curDataCount = 0;
        int totalDataLength = 0;

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        /////////////////////////////////////////////

        protected UdpReceiverTask(int interval, EvsTaskUnit udpReceiverTaskUnit) {
            super(interval);
            this.udpReceiverTaskUnit = udpReceiverTaskUnit;
            logger.debug("udpReceiverTaskUnit: {}", udpReceiverTaskUnit);
        }

        @Override
        public void run() {
            if (udpReceiverTaskUnit == null) {
                logger.warn("udpReceiverTaskUnit is null.");
                return;
            }

            try {
                byte[] curData = udpReceiverTaskUnit.getInputBuffer().poll();
                if (curData == null || curData.length == 0) {
                    //logger.warn("UdpReceiverTask > curData is null.");
                    return;
                }

                // record
                RecordManager evsRecordManager = VoipClient.getInstance().getSourceEvsRecordManager();
                if (evsRecordManager != null) {
                    evsRecordManager.addData(curData);
                    //evsRecordManager.writeFileStream(data);
                }

                // 10 개씩 모아서 decode
                byteArrayOutputStream.write(curData);
                //System.arraycopy(curData, 0, totalData, totalDataLength, udpReceiverTaskUnit.getDataSize());
                totalDataLength += EVS_ENC_DATA_SIZE;
                //logger.debug("udpReceiverTaskUnit > totalDataLength: {}", totalDataLength);

                curDataCount++;
                if (curDataCount < udpReceiverTaskUnit.getMergeCount()) {
                    //logger.debug("({}) udpReceiverTaskUnit > curDataSize={} (input)", curDataCount, curData.length);
                    return;
                }

                byte[] totalData = byteArrayOutputStream.toByteArray(); // 3240
                byte[] newData = EvsManager.getInstance().decEvs( // 3200
                        VoipClient.getInstance().getEvsDecArgv(),
                        EVS_ENC_DATA_SIZE * udpReceiverTaskUnit.getMergeCount(),
                        totalData
                );

                if (newData != null) {
                    //logger.debug("udpReceiverTaskUnit > newDataSize={}", newData.length);

                    // send
                    totalDataLength = 0;
                    if (udpReceiverTaskUnit.getOutputMediaFrameBuffer() != null) {
                        for (int i = 0; i < curDataCount; i++) {
                            curData = new byte[EVS_DEC_DATA_SIZE];
                            System.arraycopy(newData, totalDataLength, curData, 0, EVS_DEC_DATA_SIZE);
                            udpReceiverTaskUnit.getOutputMediaFrameBuffer().offer(
                                    new MediaFrame(
                                            false,
                                            curData
                                    )
                            );
                            totalDataLength += EVS_DEC_DATA_SIZE;
                            //logger.debug("udpReceiverTaskUnit > curDataSize={} (output)", curData.length);
                        }
                    }
                }

                // clear
                Arrays.fill(totalData, (byte) 0);
                totalDataLength = 0;
                curDataCount = 0;
                byteArrayOutputStream.reset();
            } catch (Exception e) {
                logger.warn("UdpReceiverTask.run.Exception", e);
            } finally {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    logger.warn("UdpReceiverTask.run.IOException", e);
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

    private class AudioMixerTask extends TaskUnit {

        final String[] evsDecArgv;
        final EvsTaskUnit audioMixerTaskUnit;
        int curDataCount = 0;
        int totalDataLength = 0;

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        /////////////////////////////////////////////

        protected AudioMixerTask(int interval, EvsTaskUnit audioMixerTaskUnit, String[] evsDecArgv) {
            super(interval);
            this.evsDecArgv = evsDecArgv;
            this.audioMixerTaskUnit = audioMixerTaskUnit;
            logger.debug("audioMixerTaskUnit: {}", audioMixerTaskUnit);
        }

        @Override
        public void run() {
            if (audioMixerTaskUnit == null) {
                logger.warn("audioMixerTaskUnit is null.");
                return;
            }

            try {
                byte[] curData = audioMixerTaskUnit.getInputBuffer().poll();
                if (curData == null || curData.length == 0) {
                    //logger.warn("AudioMixerTask > curData is null.");
                    return;
                }

                // 10 개씩 모아서 decode
                byteArrayOutputStream.write(curData);
                //System.arraycopy(curData, 0, totalData, totalDataLength, audioMixerTaskUnit.getDataSize());
                totalDataLength += EVS_ENC_DATA_SIZE;

                curDataCount++;
                if (curDataCount < audioMixerTaskUnit.getMergeCount()) {
                    //logger.debug("({}) audioMixerTaskUnit > curDataSize={} (input)", curDataCount, curData.length);
                    return;
                }

                byte[] totalData = byteArrayOutputStream.toByteArray(); // 3240
                byte[] newData = EvsManager.getInstance().decEvs( // 3200
                        evsDecArgv,
                        EVS_ENC_DATA_SIZE * audioMixerTaskUnit.getMergeCount(),
                        totalData
                );

                if (newData != null) {
                    //logger.debug("audioMixerTaskUnit > newDataSize={}", newData.length);

                    // send
                    totalDataLength = 0;
                    if (audioMixerTaskUnit.getOutputByteBuffer() != null) {
                        for (int i = 0; i < curDataCount; i++) {
                            curData = new byte[EVS_DEC_DATA_SIZE];
                            System.arraycopy(newData, totalDataLength, curData, 0, EVS_DEC_DATA_SIZE);
                            audioMixerTaskUnit.getOutputByteBuffer().offer(curData);
                            totalDataLength += EVS_DEC_DATA_SIZE;
                            //logger.debug("audioMixerTaskUnit > curDataSize={} (output)", curData.length);
                        }
                    }
                }

                // clear
                Arrays.fill(totalData, (byte) 0);
                totalDataLength = 0;
                curDataCount = 0;
                byteArrayOutputStream.reset();
            } catch (Exception e) {
                logger.warn("AudioMixerTask.run.Exception", e);
            } finally {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    logger.warn("AudioMixerTask.run.IOException", e);
                }
            }
        }
    }

}
