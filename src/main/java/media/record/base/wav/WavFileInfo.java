package media.record.base.wav;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @class public class WavFile
 * @brief WavFile class
 */
public class WavFileInfo {

    private static final Logger logger = LoggerFactory.getLogger(WavFileInfo.class);

    public static byte[] getHeader (short channelSize, int samplingRate, int sampleSize) {
        return WavHeader.getData(channelSize, samplingRate, (short) sampleSize);
    }

    public static void setChunkSizeInFile (String fileName, int dataSize) {
        byte[] sizes = ByteBuffer
                .allocate(8)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(dataSize - 8) // ChunkSize
                .putInt(dataSize - 44) // Chunk Size
                .array();

        // 읽기-쓰기 모드로 인스턴스 생성
        try (RandomAccessFile accessWave = new RandomAccessFile(fileName, "rw")) {
            // ChunkSize
            accessWave.seek(4); // 4바이트 지점으로 가서
            accessWave.write(sizes, 0, 4); // 사이즈 채움
            // Chunk Size
            accessWave.seek(40); // 40바이트 지점으로 가서
            accessWave.write(sizes, 4, 4); // 채움
        } catch (Exception e) {
            logger.warn("AudioMixer.stop.Exception", e);
        }
    }

}
