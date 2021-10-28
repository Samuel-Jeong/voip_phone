package client.gui.model.contact.base;

import client.gui.model.contact.ContactPanel;

/**
 * @class public class ContactInfo
 * @brief ContactInfo class
 */
public class ContactInfo {

    private String name;
    private String email;
    private String phoneNumber;
    private String sipIp;
    private int sipPort;

    public ContactInfo(String name, String email, String phoneNumber, String sipIp, int sipPort) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.sipIp = sipIp;
        this.sipPort = sipPort;
    }

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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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

    public String[] toArray() {
        String[] infos = new String[ContactPanel.CONTACT_CONTENT_NUM];
        infos[0] = name;
        infos[1] = email;
        infos[2] = phoneNumber;
        infos[3] = sipIp;
        infos[4] = String.valueOf(sipPort);
        return infos;
    }

    @Override
    public String toString() {
        return name +
                "," + email +
                "," + phoneNumber +
                "," + sipIp +
                "," + sipPort;
    }
}
