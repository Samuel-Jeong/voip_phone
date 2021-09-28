package evs_test;

import media.module.codec.evs.EvsManager;
import media.record.RecordManager;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @class public class EvsTest
 * @brief EvsTest
 */
public class EvsTest {

    private static final Logger logger = LoggerFactory.getLogger(EvsTest.class);

    String curUserDir = System.getProperty("user.dir");

    @Test
    public void testStart ( ) {
        curUserDir += "/src/test/resources/evsTest/";

        //decodeTest(encodeTest());
        partialDecodeTest(partialEncodeTest());
        //decodeTest((partialEncodeTest()));
    }

    private int encodeTest() {
        // File
        String[] encArgv = new String[]{
                "EVS_enc.exe",
                //"-q",
                //"-max_band",
                //"WB",
                //"-NO_DELAY_CMP",
                "8000",
                "8",
                "none",
                "none"
                //curUserDir + "test.pcm",
                //curUserDir + "test.evs"
        };

        RecordManager evsRecordManager = new RecordManager(curUserDir, 20);
        evsRecordManager.openFileStream(
                //"test.evs",
                //"Digital Presentation_48000.evs",
                "amr_test2.evs",
                true,
                false
        );

        try {
            //byte[] srcData = Files.readAllBytes(Paths.get(curUserDir + "amr_test1.wav"));
            byte[] srcData = Files.readAllBytes(Paths.get(curUserDir + "amr_test2.wav"));
            byte[] srcDataExceptWavHeader = new byte[srcData.length - 14];
            System.arraycopy(srcData, 0, srcDataExceptWavHeader, 0, srcData.length - 14);
            logger.debug("[EVS ENC]: [8000] srcData.length={}", srcDataExceptWavHeader.length);

            byte[] dstData = EvsManager.getInstance().encEvs(encArgv, srcDataExceptWavHeader);
            if (dstData != null) {
                logger.debug("[EVS ENC]: [8000] dstData.length={}", dstData.length);
                evsRecordManager.writeFileStream(dstData);
                evsRecordManager.closeFileStream();
            }
            return srcDataExceptWavHeader.length;
        } catch (Exception e) {
            logger.warn("EvsTest.encodeTest.Exception", e);
        }

        return -1;
    }

    private int partialEncodeTest() {
        // File
        String[] encArgv = new String[]{
                "EVS_enc.exe",
                //"-q",
                //"-max_band",
                //"WB",
                //"-NO_DELAY_CMP",
                "8000",
                "8",
                "none",
                "none"
                //curUserDir + "test.pcm",
                //curUserDir + "test.evs"
        };

        RecordManager evsRecordManager = new RecordManager(curUserDir, 20);
        evsRecordManager.openFileStream(
                //"test.evs",
                //"Digital Presentation_48000.evs",
                "amr_test2.evs",
                true,
                false
        );

        try {
            //byte[] srcData = Files.readAllBytes(Paths.get(curUserDir + "amr_test1.wav"));
            byte[] srcData = Files.readAllBytes(Paths.get(curUserDir + "amr_test2.wav"));
            byte[] srcDataExceptWavHeader = new byte[srcData.length - 14];
            System.arraycopy(srcData, 0, srcDataExceptWavHeader, 0, srcData.length - 14);
            logger.debug("[EVS PARTIAL ENC]: [8000] srcData.length={}", srcDataExceptWavHeader.length);

            int totalDataLength = 0;
            int curDataLength = srcDataExceptWavHeader.length;
            int remainDataLength = 320 * 10;
            int curCopiedLength = 320 * 10;

            for (int i = 0; i < srcDataExceptWavHeader.length; i += 320 * 10) {
                if (curDataLength - 320 * 10 < 0) {
                    //remainDataLength = curDataLength;
                    remainDataLength = 320 * 10;
                    curCopiedLength = curDataLength;
                    curDataLength = 0;
                }

                byte[] splitedData = new byte[remainDataLength];
                System.arraycopy(srcDataExceptWavHeader, i, splitedData, 0, curCopiedLength);

                byte[] dstData = EvsManager.getInstance().encEvs(encArgv, splitedData);
                if (dstData != null) {
                    evsRecordManager.writeFileStream(dstData);
                    totalDataLength += dstData.length;
                }

                if (curDataLength == 0) {
                    break;
                } else {
                    curDataLength -= 320 * 10;
                }
            }

            evsRecordManager.closeFileStream();
            logger.debug("[EVS PARTIAL ENC]: [8000] dstData.length={}", totalDataLength);

            return srcDataExceptWavHeader.length;
        } catch (Exception e) {
            logger.warn("EvsTest.partialEncodeTest.Exception", e);
        }

        return -1;
    }

    private void partialDecodeTest(int dstDataLen) {
        if (dstDataLen < 0) {
            return;
        }

        // File
        String[] decArgv = new String[]{
                "EVS_dec.exe",
                //"-q",
                //"-VOIP",
                //"-NO_DELAY_CMP",
                "8",
                "none",
                "none"
                //curUserDir + "test.evs",
                //curUserDir + "test.pcm2"
        };

        RecordManager pcmRecordManager = new RecordManager(curUserDir, 20);
        pcmRecordManager.openFileStream(
                //"test2.pcm2",
                //"test.pcm2",
                //"Digital Presentation_48000.pcm2",
                "amr_test2.pcm2",
                true,
                false
        );

        try {
            byte[] srcData = Files.readAllBytes(Paths.get(curUserDir + "amr_test2.evs"));
            logger.debug("[EVS PARTIAL DEC]: [8000] srcData.length={}", srcData.length);

            int totalDataLength = 0;
            int curDataLength = srcData.length;
            int remainDataLength = 324 * 10;
            int curCopiedLength = 324 * 10;

            for (int i = 0; i < srcData.length; i += 324 * 10) {
                if (curDataLength - 324 * 10 < 0) {
                    //remainDataLength = curDataLength;
                    remainDataLength = 324 * 10;
                    curCopiedLength = curDataLength;
                    curDataLength = 0;
                }

                byte[] splitedData = new byte[remainDataLength];
                System.arraycopy(srcData, i, splitedData, 0, curCopiedLength);

                byte[] dstData = EvsManager.getInstance().decEvs(decArgv, remainDataLength, splitedData);
                if (dstData != null) {
                    pcmRecordManager.writeFileStream(dstData);
                    totalDataLength += dstData.length;
                    //logger.debug("totalDataLength: {}, splitedData: {}", totalDataLength, splitedData.length);
                }

                if (curDataLength == 0) {
                    break;
                } else {
                    curDataLength -= 324 * 10;
                }
            }

            pcmRecordManager.closeFileStream();
            logger.debug("[EVS PARTIAL DEC]: [8000] dstData.length={}", totalDataLength);
        } catch (Exception e) {
            logger.warn("EvsTest.partialDecodeTest.Exception", e);
        }
    }

    private void decodeTest(int dstDataLen) {
        if (dstDataLen < 0) {
            return;
        }

        // File
        String[] decArgv = new String[]{
                "EVS_dec.exe",
                //"-q",
                //"-VOIP",
                //"-NO_DELAY_CMP",
                "8",
                "none",
                "none"
                //curUserDir + "test.evs",
                //curUserDir + "test.pcm2"
        };

        RecordManager pcmRecordManager = new RecordManager(curUserDir, 20);
        pcmRecordManager.openFileStream(
                //"test2.pcm2",
                //"test.pcm2",
                //"Digital Presentation_48000.pcm2",
                "amr_test2.pcm2",
                true,
                false
        );

        try {
            byte[] srcData = Files.readAllBytes(Paths.get(curUserDir + "amr_test2.evs"));
            logger.debug("[EVS DEC]: [8000] srcData.length={}", srcData.length);

            //byte[] dstData = srcData;
            byte[] dstData = EvsManager.getInstance().decEvs(decArgv, dstDataLen, srcData);
            if (dstData != null) {
                logger.debug("[EVS DEC]: [8000] dstData.length={}", dstData.length);
                pcmRecordManager.writeFileStream(dstData);
                pcmRecordManager.closeFileStream();
            }
        } catch (Exception e) {
            logger.warn("EvsTest.decodeTest.Exception", e);
        }
    }

}
