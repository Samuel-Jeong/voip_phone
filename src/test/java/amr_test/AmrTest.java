package amr_test;

import media.module.codec.amr.AmrManager;
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
public class AmrTest {

    private static final Logger logger = LoggerFactory.getLogger(AmrTest.class);

    String curUserDir = System.getProperty("user.dir");

    @Test
    public void testStart ( ) {
        curUserDir += "/src/test/resources/amrTest/";

        // AMR-NB
        amrNbDecodeTest(amrNbEncodeTest());

        // AMR-WB
        amrWbDecodeTest(amrWbEncodeTest());
    }

    private int amrNbEncodeTest() {
        RecordManager amrRecordManager = new RecordManager(curUserDir, 20);
        amrRecordManager.openFileStream(
                //"amr_test1.amr",
                "amr_test2.amr",
                true,
                false
        );

        try {
            AmrManager.getInstance().startEncAmrNb();

            //byte[] srcData = Files.readAllBytes(Paths.get(curUserDir + "amr_test1.wav"));
            byte[] srcData = Files.readAllBytes(Paths.get(curUserDir + "amr_test2.wav"));
            byte[] srcDataExceptWavHeader = new byte[srcData.length - 14];
            System.arraycopy(srcData, 0, srcDataExceptWavHeader, 0, srcData.length - 14);
            logger.debug("[AMR-NB ENC]: [8000] srcData.length={}", srcDataExceptWavHeader.length);

            //byte[] dstData = AmrManager.getInstance().encAmrNb(5, srcData);
            byte[] dstData = AmrManager.getInstance().encAmrNb(7, srcDataExceptWavHeader);
            if (dstData != null) {
                logger.debug("[AMR-NB ENC]: [8000] dstData.length={}", dstData.length);
                amrRecordManager.writeFileStream(dstData);
                amrRecordManager.closeFileStream();
            }

            AmrManager.getInstance().stopEncAmrNb();
            return srcData.length;
        } catch (Exception e) {
            logger.warn("AmrTest.encodeTest.Exception", e);
        }

        return -1;
    }

    private void amrNbDecodeTest(int dstDataLen) {
        if (dstDataLen < 0) {
            return;
        }

        RecordManager pcmRecordManager = new RecordManager(curUserDir, 20);
        pcmRecordManager.openFileStream(
                //"amr_test1.pcm",
                "amr_test2.pcm",
                true,
                false
        );

        try {
            AmrManager.getInstance().startDecAmrNb();

            //byte[] srcData = Files.readAllBytes(Paths.get(curUserDir + "amr_test1.amr"));
            byte[] srcData = Files.readAllBytes(Paths.get(curUserDir + "amr_test2.amr"));
            logger.debug("[AMR-NB DEC]: [8000] srcData.length={}", srcData.length);

            byte[] dstData = AmrManager.getInstance().decAmrNb(dstDataLen, srcData);
            if (dstData != null) {
                logger.debug("[AMR-NB DEC]: [8000] dstData.length={}", dstData.length);
                pcmRecordManager.writeFileStream(dstData);
                pcmRecordManager.closeFileStream();
            }

            AmrManager.getInstance().stopDecAmrNb();
        } catch (Exception e) {
            logger.warn("AmrTest.decodeTest.Exception", e);
        }
    }

    private int amrWbEncodeTest() {
        RecordManager amrRecordManager = new RecordManager(curUserDir, 20);
        amrRecordManager.openFileStream(
                //"amr_test1.amrwb",
                "amr_test2.amrwb",
                true,
                false
        );

        try {
            AmrManager.getInstance().startEncAmrWb();

            //byte[] srcData = Files.readAllBytes(Paths.get(curUserDir + "amr_test1.wav"));
            byte[] srcData = Files.readAllBytes(Paths.get(curUserDir + "amr_test2.wav"));
            byte[] srcDataExceptWavHeader = new byte[srcData.length - 14];
            System.arraycopy(srcData, 0, srcDataExceptWavHeader, 0, srcData.length - 14);
            logger.debug("[AMR-WB ENC]: [8000] srcData.length={}", srcDataExceptWavHeader.length);

            //byte[] dstData = AmrManager.getInstance().encAmrNb(5, srcData);
            byte[] dstData = AmrManager.getInstance().encAmrWb(8, srcDataExceptWavHeader);
            if (dstData != null) {
                logger.debug("[AMR-WB ENC]: [8000] dstData.length={}", dstData.length);
                amrRecordManager.writeFileStream(dstData);
                amrRecordManager.closeFileStream();
            }

            AmrManager.getInstance().stopEncAmrWb();
            return srcData.length;
        } catch (Exception e) {
            logger.warn("AmrTest.encodeTest.Exception", e);
        }

        return -1;
    }

    private void amrWbDecodeTest(int dstDataLen) {
        if (dstDataLen < 0) {
            return;
        }

        RecordManager pcmRecordManager = new RecordManager(curUserDir, 20);
        pcmRecordManager.openFileStream(
                //"amr_test1.pcm2",
                "amr_test2.pcm2",
                true,
                false
        );

        try {
            AmrManager.getInstance().startDecAmrWb();

            //byte[] srcData = Files.readAllBytes(Paths.get(curUserDir + "amr_test1.amrwb"));
            byte[] srcData = Files.readAllBytes(Paths.get(curUserDir + "amr_test2.amrwb"));
            logger.debug("[AMR-WB DEC]: [8000] srcData.length={}", srcData.length);

            byte[] dstData = AmrManager.getInstance().decAmrWb(dstDataLen, srcData);
            if (dstData != null) {
                logger.debug("[AMR-WB DEC]: [8000] dstData.length={}", dstData.length);
                pcmRecordManager.writeFileStream(dstData);
                pcmRecordManager.closeFileStream();
            }

            AmrManager.getInstance().stopDecAmrWb();
        } catch (Exception e) {
            logger.warn("AmrTest.decodeTest.Exception", e);
        }
    }

}
