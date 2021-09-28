package media.record;

import media.MediaManager;
import media.module.mixing.base.ConcurrentCyclicFIFO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.base.TaskUnit;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @class public class RecordManager
 * @brief RecordManager class
 */
public class RecordManager {

    private static final Logger logger = LoggerFactory.getLogger(RecordManager.class);

    private int curRtpPayloadLength = 0;

    /* Concurrent Cyclic FIFO Buffer */
    private final ConcurrentCyclicFIFO<byte[]> buffer = new ConcurrentCyclicFIFO<>();
    private ScheduledThreadPoolExecutor executor = null;

    /* FileStream */
    private FileOutputStream fileStream = null;
    private final ReentrantLock fileStreamLock = new ReentrantLock();

    /* 녹취 시간 간격 */
    private final int interval;

    /* 경로 (파일 이름 제외) */
    private final String path;
    /* 파일 이름 (경로 포함) */
    private String fullFilePath = null;
    /* 파일 이름 (경로 제외) */
    private String fileName = null;
    /* FileStream dead lock 을 방지하기 위한 종료 플래그 */
    private boolean isQuit = false;

    /* 녹취 데이터 총 바이트 수 */
    private int totalDataSize = 0;

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @fn public RecordManager(String path, int interval)
     * @brief RecordManager 생성자 함수
     * @param path 녹취 파일 경로 (절대 경로)
     * @param interval 녹취 시간 간격 (ms)
     */
    public RecordManager(String path, int interval) {
        this.path = path;
        this.interval = interval;

        File dirFile = new File(this.path);
        if (dirFile.isDirectory() && !dirFile.exists()) {
            if (dirFile.mkdirs()) {
                logger.debug("Directory is created. ({})", path);
            }
        }
    }

    public void start () {
        if (executor == null) {
            executor = new ScheduledThreadPoolExecutor(5);

            try {
                RecordTask recordTask = new RecordTask(interval);
                executor.scheduleAtFixedRate(
                        recordTask,
                        recordTask.getInterval(),
                        recordTask.getInterval(),
                        TimeUnit.MILLISECONDS
                );
            } catch (Exception e) {
                logger.warn("TaskManager.addTask.Exception", e);
            }

            logger.debug("RecordManager RecordTask is added.");
        }

        curRtpPayloadLength = MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_WB)? 640 : 320;
    }

    public void stop () {
        buffer.clear();

        if (executor != null) {
            executor.shutdown();
            executor = null;
            logger.debug("RecordManager RecordTask is removed.");
        }
    }

    public void addData (byte[] data) {
        if (executor != null) {
            try {
                if (data == null) {
                    return;
                }

                int totalDataLength = data.length;
                int curDataLength = data.length;
                int remainDataLength = curRtpPayloadLength;

                if (totalDataLength > curRtpPayloadLength) {
                    // Data length 가 320 bytes 보다 크면, 320 bytes 씩 분할해서 20 ms 마다 파일에 쌓도록 설정
                    for (int i = 0; i < data.length; i += curRtpPayloadLength) {
                        if (curDataLength - curRtpPayloadLength < 0) {
                            remainDataLength = curDataLength;
                            curDataLength = 0;
                        }

                        byte[] splitedData = new byte[remainDataLength];
                        System.arraycopy(data, i, splitedData, 0, remainDataLength);
                        buffer.offer(splitedData);

                        if (curDataLength == 0) {
                            break;
                        } else {
                            curDataLength -= curRtpPayloadLength;
                        }
                    }
                } else {
                    buffer.offer(data);
                }
            } catch (Exception e) {
                logger.warn("RecordManager.addData.Exception", e);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @fn private void openFileStream (String fileName, boolean isDelete)
     * @brief FileOutputStream 을 open 하는 함수
     * @param fileName File name
     * @param isDelete 파일 삭제 여부
     */
    public void openFileStream (String fileName, boolean isDelete, boolean append) {
        File ramFile = new File(fileName);
        if (ramFile.isDirectory()) {
            logger.warn("Fail to open fileStream. File name is path. ({})", fileName);
            return;
        } else {
            if (!fileName.contains(File.separator)) {
                fileName = this.path + File.separator + fileName;
                ramFile = new File(fileName);
            }

            if (isDelete) {
                // 파일 append 를 막기 위해 기존 파일을 지운다.
                removeFile(fileName);
                this.fullFilePath = fileName;
            }
        }

        try {
            fileStreamLock.lock();

            if (fileStream == null) {
                isQuit = false;
                fileStream = new FileOutputStream(ramFile, append);
                this.fileName = ramFile.getName();
                logger.trace("FileStream is opened. (name={})", ramFile.getName());
            }
        } catch (Exception e) {
            logger.warn("Fail to open the fileStream. (name={})", ramFile.getName(), e);
        } finally {
            fileStreamLock.unlock();
        }
    }

    /**
     * @fn public void closeFileStream ()
     * @brief FileOutputStream 을 close 하는 함수
     */
    public void closeFileStream () {
        try {
            fileStreamLock.lock();

            if (fileStream != null) {
                isQuit = true;
                fileStream.flush();
                fileStream.close();
                fileStream = null;
                logger.trace("FileStream is closed. (name={})", fileName);
            }
        } catch (Exception e) {
            logger.warn("Fail to close the fileStream. (name={})", fileName, e);
        } finally {
            fileStreamLock.unlock();
        }
    }

    /**
     * @fn public void removeFile (String fileName)
     * @brief 지정한 파일을 삭제하는 함수
     * @param fileName 파일 이름 (경로 포함)
     */
    public void removeFile (String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            if (file.delete()) {
                logger.trace("File is deleted. ({})", file.getAbsolutePath());
                this.totalDataSize = 0;
            }
        }
    }

    /**
     * @fn public void writeFileStream (byte[] data)
     * @brief FileOutputStream 에 write 하는 함수
     * @param data Media Data (byte)
     */
    public void writeFileStream (byte[] data) {
        if (isQuit) {
            closeFileStream();
            return;
        }

        try {
            fileStreamLock.lock();

            if (fileStream != null) {
                fileStream.write(data);
                totalDataSize += data.length;
            }
        } catch (Exception e) {
            logger.warn("Fail to write media data. (name={})", fileName, e);
        } finally {
            fileStreamLock.unlock();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @fn public int getTotalDataSize()
     * @brief 녹취된 전체 음성 데이터 크기를 반환하는 함수
     * @return 녹취된 전체 음성 데이터 크기를 반환
     */
    public int getTotalDataSize() {
        return totalDataSize;
    }

    /**
     * @fn public String getFullFilePath()
     * @brief 녹취 파일의 전체 경로를 반환하는 함수
     * @return 녹취 파일의 전체 경로를 반환
     */
    public String getFullFilePath() {
        return fullFilePath;
    }

    public int getInterval() {
        return interval;
    }

    ////////////////////////////////////////////////////////////////////////////////

    private class RecordTask extends TaskUnit {

        protected RecordTask(int interval) {
            super(interval);
        }

        @Override
        public void run() {
            byte[] data = buffer.poll();
            if (data == null || data.length == 0) {
                return;
            }

            writeFileStream(data);
        }
    }

}
