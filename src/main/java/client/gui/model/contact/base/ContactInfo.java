package client.gui.model.contact.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * @class public class ContactInfo
 * @brief ContactInfo class
 */
public class ContactInfo {

    private static final Logger logger = LoggerFactory.getLogger(ContactInfo.class);

    public static final int CONTACT_CONTENT_NUM = 5;

    private String name;
    private String email;
    private String mdn;
    private String sipIp;
    private int sipPort;

    ////////////////////////////////////////////////////////////////////////////////

    public ContactInfo(String name, String email, String mdn, String sipIp, int sipPort) {
        this.name = name;
        this.email = email;
        this.mdn = mdn;
        this.sipIp = sipIp;
        this.sipPort = sipPort;
    }

    public ContactInfo() {
        // Nothing
    }

    ////////////////////////////////////////////////////////////////////////////////

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMdn() {
        return mdn;
    }

    public void setMdn(String mdn) {
        this.mdn = mdn;
    }

    public String getSipIp() {
        return sipIp;
    }

    public void setSipIp(String sipIp) {
        this.sipIp = sipIp;
    }

    public int getSipPort() {
        return sipPort;
    }

    public void setSipPort(int sipPort) {
        this.sipPort = sipPort;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public boolean setData(String[] data) {
        if (data.length != CONTACT_CONTENT_NUM) {
            logger.warn("Fail to set the data into the contact info. (data={})", Arrays.stream(data).toArray());
            return false;
        }

        name = data[0];
        email = data[1];
        mdn = data[2];
        sipIp = data[3];
        sipPort = Integer.parseInt(data[4]);

        return true;
    }

    public String[] toArray() {
        String[] infos = new String[CONTACT_CONTENT_NUM];
        infos[0] = name;
        infos[1] = email;
        infos[2] = mdn;
        infos[3] = sipIp;
        infos[4] = String.valueOf(sipPort);
        return infos;
    }

    @Override
    public String toString() {
        return name +
                "," + email +
                "," + mdn +
                "," + sipIp +
                "," + sipPort;
    }
}
