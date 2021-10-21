package client.module.base;

/**
 * @class public class MediaFrame
 * @brief MediaFrame class
 */
public class MediaFrame {

    private boolean isDtmf;
    private byte[] data;

    public MediaFrame(boolean isDtmf, byte[] data) {
        this.isDtmf = isDtmf;
        this.data = data;
    }

    public boolean isDtmf() {
        return isDtmf;
    }

    public void setDtmf(boolean dtmf) {
        isDtmf = dtmf;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
