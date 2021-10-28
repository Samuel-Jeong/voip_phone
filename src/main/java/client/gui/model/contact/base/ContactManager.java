package client.gui.model.contact.base;

import client.gui.model.contact.ContactPanel;
import config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AppInstance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedHashSet;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @class public class ContactManager
 * @brief ContactManager class
 */
public class ContactManager {

    private static final Logger logger = LoggerFactory.getLogger(ContactManager.class);

    private final int MAX_CONTACT_NUM = 500;

    private static ContactManager contactManager = null;

    private final LinkedHashSet<ContactInfo> contactMap;
    private final ReentrantLock contactSetLock = new ReentrantLock();

    ////////////////////////////////////////////////////////////////////////////////

    public ContactManager() {
        contactMap = new LinkedHashSet<>(MAX_CONTACT_NUM);

        try {
            ConfigManager configManager = AppInstance.getInstance().getConfigManager();
            String contactFileName = configManager.getContactPath();
            if (contactFileName != null) {
                // Load contact info
                File contactFile = new File(contactFileName);
                if (contactFile.exists()) {
                    BufferedReader inFile = new BufferedReader(new FileReader(contactFile));
                    String data;

                    while ((data = inFile.readLine()) != null) {
                        //logger.debug("[CONTACT] [{}]", data);
                        String[] content = data.split(",");
                        if (content.length != ContactPanel.CONTACT_CONTENT_NUM) {
                            continue;
                        }

                        String name = content[0];
                        String email = content[1];
                        String phoneNumber = content[2];
                        String sipIp = content[3];
                        int sipPort = Integer.parseInt(content[4]);

                        addContactInfo(
                                name,
                                email,
                                phoneNumber,
                                sipIp,
                                sipPort
                        );
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Fail to load the contacts.", e);
        }
    }

    public static ContactManager getInstance () {
        if (contactManager == null) {
            contactManager = new ContactManager();
        }

        return contactManager;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public int getContactSetSize() {
        try {
            contactSetLock.lock();

            return contactMap.size();
        } catch (Exception e) {
            logger.warn("Fail to get the contact set size.", e);
            return 0;
        } finally {
            contactSetLock.unlock();
        }
    }

    public ContactInfo addContactInfo(String name, String email, String phoneNumber, String sipIp, int sipPort) {
        if (name == null || email == null || phoneNumber == null || sipIp == null || sipPort <= 0) {
            logger.warn("Fail to add the contact info. (name={}, email={}, phoneNumber={}, sipIp={}, sipPort={})", name, email, phoneNumber, sipIp, sipPort);
            return null;
        }

        try {
            contactSetLock.lock();

            ContactInfo contactInfo = new ContactInfo(name, email, phoneNumber, sipIp, sipPort);
            if (contactMap.add(contactInfo)) {
                logger.debug("Success to add the contact info. ({})", contactInfo);
                return contactInfo;
            } else {
                logger.warn("Fail to add the contact info. ({})", contactInfo);
                return null;
            }
        } catch (Exception e) {
            logger.warn("Fail to add the contact info. (name={}, email={}, phoneNumber={}, sipIp={}, sipPort={})", name, email, phoneNumber, sipIp, sipPort, e);
            return null;
        } finally {
            contactSetLock.unlock();
        }
    }

    public void deleteContactInfo(ContactInfo contactInfo) {
        if (contactInfo == null) {
            return;
        }

        try {
            contactSetLock.lock();

            if (contactMap.remove(contactInfo)) {
                logger.debug("Success to delete the contact info. ({})", contactInfo);
            } else {
                logger.warn("Fail to delete the contact info. ({})", contactInfo);
            }
        } catch (Exception e) {
            logger.warn("Fail to delete the contact info. (contactInfo={})", contactInfo, e);
        } finally {
            contactSetLock.unlock();
        }
    }

    public LinkedHashSet<ContactInfo> cloneContactInfoSet() {
        try {
            contactSetLock.lock();

            return (LinkedHashSet<ContactInfo>) contactMap.clone();
        } catch (Exception e) {
            logger.warn("Fail to clone the contact set.", e);
            return null;
        } finally {
            contactSetLock.unlock();
        }
    }

    public void clearContactInfoSet() {
        try {
            contactSetLock.lock();

            contactMap.clear();
            logger.debug("Success to clear the contact set.");
        } catch (Exception e) {
            logger.warn("Fail to clear the contact set.", e);
        } finally {
            contactSetLock.unlock();
        }
    }

}
