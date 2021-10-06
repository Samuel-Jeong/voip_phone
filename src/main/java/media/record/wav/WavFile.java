package media.record.wav;

import media.record.wav.base.LittleEndianInt;
import media.record.wav.base.LittleEndianShort;
import media.record.wav.base.WavFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * @class public class WavFile
 * @brief WavFile class
 */
public class WavFile {

    private static final Logger logger = LoggerFactory.getLogger(WavFile.class);

    //wave file constants
    //"RIFF" in ascii
    private static final int RIFF_CHUNK_ID = 0x52494646;
    //"WAVE" in ascii
    private static final int WAVE_FORMAT = 0x57415645;
    //"fmt" in ascii
    private static final int SUBCHUNK1ID = 0x666d7420;
    //PCM uses 16. That's the only wave we will support. Read as little endian
    private static final int SUBCHUNK1SIZE = 16;
    //Format = 1 for pcm. Others mean compression
    private static final int AUDIO_FORMAT = 1;
    //"data" in ascii
    private static final int SUBCHUNK2ID = 0x64617461;
    //Data should start at byte 44
    private static final int DATA_START_OFFSET = 44;

    private final File inputFile;

    //Wave file header
    //RIFF section
    private int chunkID;
    private LittleEndianInt chunkSize;
    private int format;
    //fmt section
    private int subChunk1ID;
    private LittleEndianInt subChunk1Size;
    private LittleEndianShort audioFormat;
    private LittleEndianShort numChannels;
    private LittleEndianInt sampleRate;
    private LittleEndianInt byteRate;
    private LittleEndianShort blockAlign;
    private LittleEndianShort bitsPerSample;
    //Data header
    private int subChunk2ID;
    private LittleEndianInt subChunk2Size;
    //Input stream
    private InputStream inputStream;
    //Sample position in bytes
    private long dataOffset = 0;
    //ByteBuffer for conversion from byte[] to integers
    private ByteBuffer bb;

    private final long totalLength;

    ////////////////////////////////////////////////////////////////////////////////

    public WavFile(File inputFile) {
        this.inputFile = inputFile;
        totalLength = inputFile.length();

        try {
            inputStream = new FileInputStream(inputFile);
        } catch (FileNotFoundException e) {
            logger.warn("Fail to set the wav file.");
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

    public boolean open() {
        return readHeader();
    }

    public void close() throws IOException {
        inputStream.close();
    }

    ////////////////////////////////////////////////////////////////////////////////

    private boolean readHeader() {
        //Declare buffers for use in storing bytes read in form inputStream
        byte[] intByteBuffer = new byte[4];
        byte[] shortByteBuffer = new byte[2];

        try {
            //ChunkID
            int readBytes = inputStream.read(intByteBuffer);
            if (readBytes > 0) {
                chunkID = bytesToInt(intByteBuffer);
                //Check to see if valid RIFF file
                if (chunkID != RIFF_CHUNK_ID) {
                    throw new WavFileException("NOT A VALID RIFF FILE: chunkid [" + chunkID + "]");
                }
            } else {
                throw new WavFileException("Fail to read the file.");
            }

            //ChunkSize
            readBytes = inputStream.read(intByteBuffer);
            if (readBytes > 0) {
                chunkSize = new LittleEndianInt(bytesToInt(intByteBuffer));
            } else {
                throw new WavFileException("Fail to read the file.");
            }

            //Format
            readBytes = inputStream.read(intByteBuffer);
            if (readBytes > 0) {
                format = bytesToInt(intByteBuffer);
                if (format != WAVE_FORMAT) {
                    throw new WavFileException("INVALID WAV FORMAT");
                }
            } else {
                throw new WavFileException("Fail to read the file.");
            }

            //Subchunk1ID
            readBytes = inputStream.read(intByteBuffer);
            if (readBytes > 0) {
                subChunk1ID = bytesToInt(intByteBuffer);
                if (subChunk1ID != SUBCHUNK1ID) {
                    throw new WavFileException("INVALID SUBCHUNK 1 ID");
                }
            } else {
                throw new WavFileException("Fail to read the file.");
            }

            //SubChunk1Size
            readBytes = inputStream.read(intByteBuffer);
            if (readBytes > 0) {
                subChunk1Size = new LittleEndianInt(bytesToInt(intByteBuffer));
                if (subChunk1Size.convert() != SUBCHUNK1SIZE) {
                    throw new WavFileException("NON PCM FILES ARE NOT SUPPORTED: chunk size[" + subChunk1Size.convert() + "]");
                }
            } else {
                throw new WavFileException("Fail to read the file.");
            }

            //Audio Format
            readBytes = inputStream.read(shortByteBuffer);
            if (readBytes > 0) {
                audioFormat = new LittleEndianShort(bytesToShort(shortByteBuffer, true));
                if (audioFormat.convert() != AUDIO_FORMAT) {
                    throw new WavFileException("COMPRESSED WAVE FILE NOT SUPPORTED: format[" + audioFormat.convert() + "]");
                }
            } else {
                throw new WavFileException("Fail to read the file.");
            }

            //NumChannels
            readBytes = inputStream.read(shortByteBuffer);
            if (readBytes > 0) {
                numChannels = new LittleEndianShort(bytesToShort(shortByteBuffer, true));
                if (numChannels.convert() > 2 || numChannels.convert() < 0) {
                    throw new WavFileException("INVALID NUMBER OF CHANNELS: numChannels[" + numChannels.convert() + "]");
                }
            } else {
                throw new WavFileException("Fail to read the file.");
            }

            //SampleRate
            readBytes = inputStream.read(intByteBuffer);
            if (readBytes > 0) {
                sampleRate = new LittleEndianInt(bytesToInt(intByteBuffer));
            } else {
                throw new WavFileException("Fail to read the file.");
            }

            //ByteRate
            readBytes = inputStream.read(intByteBuffer);
            if (readBytes > 0) {
                byteRate = new LittleEndianInt(bytesToInt(intByteBuffer));
            } else {
                throw new WavFileException("Fail to read the file.");
            }

            //BlockAlign
            readBytes = inputStream.read(shortByteBuffer);
            if (readBytes > 0) {
                blockAlign = new LittleEndianShort(bytesToShort(shortByteBuffer, true));
            } else {
                throw new WavFileException("Fail to read the file.");
            }

            //BitsPerSample
            //We only support 16
            //support floating point IEEE 32bit
            readBytes = inputStream.read(shortByteBuffer);
            if (readBytes > 0) {
                bitsPerSample = new LittleEndianShort(bytesToShort(shortByteBuffer, true));
            } else {
                throw new WavFileException("Fail to read the file.");
            }

            //SubChunk2ID
            readBytes = inputStream.read(intByteBuffer);
            if (readBytes > 0) {
                subChunk2ID = bytesToInt(intByteBuffer);
                if (subChunk2ID != SUBCHUNK2ID) {
                    throw new WavFileException("INVALID DATA HEADER");
                }
            } else {
                throw new WavFileException("Fail to read the file.");
            }

            //Subchunk2Size
            readBytes = inputStream.read(intByteBuffer);
            if (readBytes > 0) {
                subChunk2Size = new LittleEndianInt(bytesToInt(intByteBuffer));
            } else {
                throw new WavFileException("Fail to read the file.");
            }
        } catch (Exception e) {
            logger.warn("WavFile.readHeader.Exception", e);
            return false;
        }

        //Everything loaded fine
        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public boolean isStereo() {
        return (numChannels.convert() == 2);
    }

    public int getNumChannels() {
        return numChannels.convert();
    }

    public int getSampleRate() {
        return sampleRate.convert();
    }

    public int getBitRate() {
        return bitsPerSample.convert();
    }

    public int getFileSize() {
        return subChunk1Size.convert() + 8;
    }

    public int getNumFrames() {
        return (chunkSize.convert() / blockAlign.convert());
    }

    ////////////////////////////////////////////////////////////////////////////////

    //
    public AudioInputStream loadWavFileToAudioInputStream() throws IOException, UnsupportedAudioFileException {
        if (inputFile != null) {
            return AudioSystem.getAudioInputStream(inputFile);
        }

        return null;
    }

    public byte[] convertAudioInputStream2ByteArray(AudioInputStream stream) {
        if (stream == null) {
            return new byte[0];
        }

        try {
            long skip = stream.skip(DATA_START_OFFSET);
            if (skip > 0) {
                logger.debug("Audio Data [{}] bytes is skipped. (path={})", skip, inputFile.getAbsolutePath());
            }

            //int size = (int) (stream.getFrameLength() * stream.getFormat().getFrameSize());
            int size = (int) (stream.getFrameLength());
            if (size <= 0) {
                return new byte[0];
            }

            logger.debug("frameLength: {}, frameSize: {}, size: {}", stream.getFrameLength(), stream.getFormat().getFrameSize(), size);

            byte[] array = new byte[size];
            if (stream.read(array) > 0) {
                return array;
            }
        } catch (Exception e) {
            logger.warn("WavFile.convertAudioInputStream2ByteArray.IOException", e);
            return new byte[0];
        }

        return new byte[0];
    }

    public static AudioInputStream convertByteArray2AudioInputStream(byte[] array, AudioFormat format) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(array);
        return new AudioInputStream(
                byteArrayInputStream,
                format,
                (array.length / (2L * format.getChannels()))
        );
    }
    //

    //
    private long readSample(long offset) throws IOException {
        long sample = 0;
        byte[] buffer = new byte[bitsPerSample.convert() / 8];
        long skipBytes = inputStream.skip(offset);
        if (skipBytes > 0) {
            logger.trace("Wav file's [{}] bytes is skipped.", skipBytes);
        }

        int delta = inputStream.read(buffer);
        if (delta != -1) {
            dataOffset += delta;
        }

        if (bitsPerSample.convert() == 16) {
            sample = bytesToShort(buffer, true);
        }

        return sample;
    }

    public int readFrames(double[] frameBuffer) throws IOException {
        return readFrames(frameBuffer, 0);
    }

    public int readFrames(double[] frameBuffer, int offset) throws IOException {
        //int curLength = 0;

        for (int f = 0; f < frameBuffer.length; f++) {
            frameBuffer[f] = (double) readSample(offset) / (double) (Long.MAX_VALUE >> (64 - bitsPerSample.convert()));
            /*if (frameBuffer[f] != 0) {
                curLength++;
            }*/
        }

        return frameBuffer.length;
        //return curLength;
    }
    //

    //
    public byte[] convertWavToRawAll(byte[] data) {
        return Arrays.copyOfRange(
                data,
                DATA_START_OFFSET,
                data.length
        );
    }

    public byte[] audioToByteAll() {
        if (inputFile == null) {
            return null;
        }

        try {
            FileInputStream fileInputStream = new FileInputStream(inputFile);
            long skip = fileInputStream.skip(DATA_START_OFFSET);
            if (skip > 0) {
                logger.debug("Audio Data [{}] bytes is skipped. (path={})", skip, inputFile.getAbsolutePath());
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

            int read;
            byte[] buff = new byte[1024];
            while ((read = bufferedInputStream.read(buff)) > 0) {
                byteArrayOutputStream.write(buff, 0, read);
            }

            byteArrayOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            logger.warn("WavFile.audioToByteAll.Exception", e);
            return null;
        }
    }
    //

    ////////////////////////////////////////////////////////////////////////////////

    public static byte[] convertDoubleArray2ByteArray(double[] array) {
        double max16bit = 32768.0; // 16 bits
        byte[] output = new byte[2 * array.length];

        for (int i = 0; i < array.length; i++) {
            int b = (array[i] == 1.0) ? Short.MAX_VALUE : (short) (array[i] * max16bit);

            // little endian
            output[2 * i] = (byte) b;
            output[2 * i + 1] = (byte) (b >> 8);
        }

        return output;
    }

    public long bytesToLong(byte[] bytes) {
        bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.BIG_ENDIAN);
        return bb.getLong();
    }

    public int bytesToInt(byte[] bytes) {
        bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.BIG_ENDIAN);
        return bb.getInt();
    }

    public short bytesToShort(byte[] bytes, boolean isBigEndian) {
        bb = ByteBuffer.wrap(bytes);
        bb.order(isBigEndian? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        return bb.getShort();
    }

    ////////////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return "[" +
                "\n\tsize=" + totalLength +
                "\n\tchunkID=" + chunkID +
                ", \n\tchunkSize=" + chunkSize +
                ", \n\tformat=" + format +
                ", \n\tsubChunk1ID=" + subChunk1ID +
                ", \n\tsubChunk1Size=" + subChunk1Size +
                ", \n\taudioFormat=" + audioFormat +
                ", \n\tnumChannels=" + numChannels +
                ", \n\tsampleRate=" + sampleRate +
                ", \n\tbyteRate=" + byteRate +
                ", \n\tblockAlign=" + blockAlign +
                ", \n\tbitsPerSample=" + bitsPerSample +
                ", \n\tsubChunk2ID=" + subChunk2ID +
                ", \n\tsubChunk2Size=" + subChunk2Size +
                "\n]";
    }
}
