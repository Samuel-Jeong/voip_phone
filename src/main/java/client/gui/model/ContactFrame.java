package client.gui.model;

import client.gui.FrameManager;
import client.gui.model.contact.ContactPanel;
import client.gui.model.contact.base.ContactInfo;
import client.gui.model.contact.base.ContactManager;
import client.gui.model.util.JTextFieldLimit;
import config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AppInstance;
import service.ServiceManager;
import signal.SignalManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @class public class ContentFrame extends JPanel
 * @brief ContentFrame class
 */
public class ContactFrame extends JPanel {

    private static final Logger logger = LoggerFactory.getLogger(ContactFrame.class);

    //////////////////////////////////////////////////////////////////////
    // Button
    private final JButton clientToggleButton;
    private final JButton selectContactButton;
    private final JButton addContactButton;
    private final JButton removeContactButton;
    private final JButton modifyContactButton;

    //////////////////////////////////////////////////////////////////////
    // Panel
    private final ContactPanel contactPanel;

    //////////////////////////////////////////////////////////////////////

    public ContactFrame() {
        JPanel mainPanel = new JPanel();
        GridBagConstraints contactGB = new GridBagConstraints();
        contactGB.anchor = GridBagConstraints.WEST;
        contactGB.ipadx = 10;
        contactGB.ipady = 10;
        mainPanel.setLayout(new GridBagLayout());

        JPanel buttonPanel = new JPanel();
        GridBagConstraints buttonPanelGB = new GridBagConstraints();
        buttonPanelGB.anchor = GridBagConstraints.WEST;
        buttonPanelGB.ipadx = 10;
        buttonPanelGB.ipady = 10;
        buttonPanel.setLayout(new GridBagLayout());

        clientToggleButton = new JButton("Back");
        clientToggleButton.addActionListener(new ClientToggleListener());
        clientToggleButton.setEnabled(true);
        buttonPanelGB.gridx = 0;
        buttonPanelGB.gridy = 0;
        buttonPanel.add(clientToggleButton, buttonPanelGB);

        selectContactButton = new JButton("Select");
        selectContactButton.addActionListener(new SelectContactListener());
        selectContactButton.setEnabled(true);
        buttonPanelGB.gridx = 1;
        buttonPanel.add(selectContactButton, buttonPanelGB);

        addContactButton = new JButton("Add");
        addContactButton.addActionListener(new AddContactListener());
        addContactButton.setEnabled(true);
        buttonPanelGB.gridx = 2;
        buttonPanel.add(addContactButton, buttonPanelGB);

        modifyContactButton = new JButton("Modify");
        modifyContactButton.addActionListener(new ModifyContactListener());
        modifyContactButton.setEnabled(true);
        buttonPanelGB.gridx = 3;
        buttonPanel.add(modifyContactButton, buttonPanelGB);

        removeContactButton = new JButton("Remove");
        removeContactButton.addActionListener(new RemoveContactListener());
        removeContactButton.setEnabled(true);
        buttonPanelGB.gridx = 4;
        buttonPanel.add(removeContactButton, buttonPanelGB);

        contactGB.gridx = 0;
        contactGB.gridy = 0;
        mainPanel.add(buttonPanel, contactGB);

        contactGB.gridx = 0;
        contactGB.gridy = 1;
        contactPanel = new ContactPanel();
        contactPanel.createContactPanel();
        mainPanel.add(contactPanel.getPanel(), contactGB);

        add(mainPanel);
    }

    //////////////////////////////////////////////////////////////////////

    public String[][] getTableData(JTable table) {
        DefaultTableModel defaultTableModel = (DefaultTableModel) table.getModel();
        if (defaultTableModel == null) {
            return null;
        }

        int rowCount = defaultTableModel.getRowCount();
        int columnCount = defaultTableModel.getColumnCount();
        String[][] tableData = new String[rowCount][columnCount];

        for (int i = 0 ; i < rowCount ; i++) {
            for (int j = 0; j < columnCount; j++) {
                String data = (String) defaultTableModel.getValueAt(i, j);
                if (data != null) {
                    tableData[i][j] = data;
                }
            }
        }

        return tableData;
    }

    public String[] getTableDataAtRow(JTable table, int rowPos) {
        DefaultTableModel defaultTableModel = (DefaultTableModel) table.getModel();
        if (defaultTableModel == null) {
            return null;
        }

        int columnCount = defaultTableModel.getColumnCount() ;
        String[] tableData = new String[columnCount];

        for (int j = 0; j < columnCount; j++) {
            String data = (String) defaultTableModel.getValueAt(rowPos, j);
            if (data != null) {
                tableData[j] = data;
            }
        }

        return tableData;
    }

    public ContactPanel getContactPanel() {
        return contactPanel;
    }

    public void addContactToTable(ContactInfo contactInfo) {
        contactPanel.addContact(contactInfo);
    }

    public void modifyContactToTable(int row, ContactInfo contactInfo) {
        contactPanel.modifyContact(row, contactInfo);
    }

    public void removeContactFromTable() {
        JTable contactTable = contactPanel.getContactTable();
        if (contactTable == null) {
            return;
        }

        int selectedIndex = contactTable.getSelectedRow();
        if (selectedIndex != -1) {
            DefaultTableModel model = (DefaultTableModel) contactTable.getModel();

            //String[][] tableData = getTableData(contactTable);
            String[] tableData = getTableDataAtRow(contactTable, selectedIndex);
            if (tableData != null) {
                if (selectedIndex >= 0 && selectedIndex < contactTable.getRowCount()) {
                    //String[] selectedData = tableData[selectedIndex];
                    ContactInfo contactInfo = new ContactInfo();
                    contactInfo.setData(tableData);

                    if (ContactManager.getInstance().removeContactInfoFromFile(contactInfo)) {
                        logger.debug("Success to remove the contact info. ({})", contactInfo);
                    }
                }

                model.removeRow(selectedIndex);
            }
        }
    }

    //////////////////////////////////////////////////////////////////////

    class ClientToggleListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == clientToggleButton) {
                FrameManager.getInstance().change(ServiceManager.CLIENT_FRAME_NAME);
            }
        }
    }

    class SelectContactListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == selectContactButton) {
                JTable contactTable = contactPanel.getContactTable();
                if (contactTable == null) {
                    return;
                }

                int selectedIndex = contactTable.getSelectedRow();
                if (selectedIndex != -1) {
                    //String[][] tableData = getTableData(contactTable);
                    String[] tableData = getTableDataAtRow(contactTable, selectedIndex);
                    if (tableData != null) {
                        if (selectedIndex >= 0 && selectedIndex < contactTable.getRowCount()) {
                            //String[] selectedData = tableData[selectedIndex];
                            ContactInfo contactInfo = new ContactInfo();
                            if (contactInfo.setData(tableData)) {
                                //
                                // 1) Set remote host name
                                FrameManager.getInstance().setRemoteHostName(contactInfo.getPhoneNumber());
                                //

                                //
                                // 2) Set remote sip ip & port
                                ConfigManager configManager = AppInstance.getInstance().getConfigManager();

                                String toIp = contactInfo.getSipIp();
                                if (toIp != null) {
                                    SignalManager.getInstance().setToIp(toIp);
                                    configManager.setToIp(toIp);
                                    configManager.setIniValue(ConfigManager.SECTION_SIGNAL, ConfigManager.FIELD_TO_IP, toIp);
                                    FrameManager.getInstance().setToSipIp(toIp);
                                }

                                int toPort = contactInfo.getSipPort();
                                if (toPort > 0) {
                                    SignalManager.getInstance().setToPort(toPort);
                                    configManager.setToPort(toPort);
                                    configManager.setIniValue(ConfigManager.SECTION_SIGNAL, ConfigManager.FIELD_TO_PORT, String.valueOf(toPort));
                                    FrameManager.getInstance().setToSipPort(String.valueOf(toPort));
                                }
                                //

                                logger.debug("Contact info is selected. ({})", contactInfo);
                            }
                        }
                    }

                    FrameManager.getInstance().change(ServiceManager.CLIENT_FRAME_NAME);
                }
            }
        }

    }

    class AddContactListener implements ActionListener {

        private ContactAddFrame contactAddFrame;

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == addContactButton) {
                if (contactAddFrame != null) {
                    contactAddFrame.dispose();
                }

                contactAddFrame = new ContactAddFrame();
            }
        }

        class ContactAddFrame extends JFrame implements ActionListener{

            private final JButton okButton;

            private final JTextField nameInputField = new JTextField(20);
            private final JTextField emailInputField = new JTextField(20);
            private final JTextField phoneNumberInputField = new JTextField(20);
            private final JTextField sipIpInputField = new JTextField(20);
            private final JTextField sipPortInputField = new JTextField(20);

            public ContactAddFrame() {
                super("ContactAddFrame");

                //
                JPanel jPanel = new JPanel();
                GridBagConstraints mainGB = new GridBagConstraints();
                mainGB.anchor = GridBagConstraints.WEST;
                mainGB.ipadx = 10; mainGB.ipady = 10;
                jPanel.setLayout(new GridBagLayout());
                //

                //
                JLabel nameInputLabel = FrameManager.getInstance().createLabel("name: ");
                mainGB.gridx = 0; mainGB.gridy = 0;
                jPanel.add(nameInputLabel, mainGB);
                mainGB.gridx = 1; mainGB.gridy = 0;
                nameInputField.setDocument(new JTextFieldLimit(10));
                jPanel.add(nameInputField, mainGB);
                //

                //
                JLabel emailInputLabel = FrameManager.getInstance().createLabel("email: ");
                mainGB.gridx = 0; mainGB.gridy = 1;
                jPanel.add(emailInputLabel, mainGB);
                mainGB.gridx = 1; mainGB.gridy = 1;
                emailInputField.setDocument(new JTextFieldLimit(20));
                jPanel.add(emailInputField, mainGB);
                //

                //
                JLabel phoneNumberInputLabel = FrameManager.getInstance().createLabel("phone-number: ");
                mainGB.gridx = 0; mainGB.gridy = 2;
                jPanel.add(phoneNumberInputLabel, mainGB);
                mainGB.gridx = 1; mainGB.gridy = 2;
                phoneNumberInputField.setDocument(new JTextFieldLimit(20));
                jPanel.add(phoneNumberInputField, mainGB);
                //

                //
                JLabel sipIpInputLabel = FrameManager.getInstance().createLabel("sip-ip: ");
                mainGB.gridx = 0; mainGB.gridy = 3;
                jPanel.add(sipIpInputLabel, mainGB);
                mainGB.gridx = 1; mainGB.gridy = 3;
                sipIpInputField.setDocument(new JTextFieldLimit(15));
                jPanel.add(sipIpInputField, mainGB);
                //

                //
                JLabel sipPortInputLabel = FrameManager.getInstance().createLabel("sip-port: ");
                mainGB.gridx = 0; mainGB.gridy = 4;
                jPanel.add(sipPortInputLabel, mainGB);
                mainGB.gridx = 1; mainGB.gridy = 4;
                sipPortInputField.setDocument(new JTextFieldLimit(5));
                jPanel.add(sipPortInputField, mainGB);
                //

                //
                JPanel buttonPanel = new JPanel();
                GridBagConstraints buttonGB = new GridBagConstraints();
                buttonGB.ipadx = 10; buttonGB.ipady = 10;
                buttonGB.anchor = GridBagConstraints.WEST;
                buttonPanel.setLayout(new GridBagLayout());

                okButton = new JButton("Ok");
                okButton.addActionListener(new ApplyContactInfoListener());
                buttonGB.gridx = 0; buttonGB.gridy = 0;
                buttonPanel.add(okButton, buttonGB);

                JButton closeButton = new JButton("Close");
                closeButton.addActionListener(this);
                buttonGB.gridx = 1; buttonGB.gridy = 0;
                buttonPanel.add(closeButton, buttonGB);

                mainGB.gridx = 0; mainGB.gridy = 5;
                jPanel.add(buttonPanel, mainGB);
                //

                add(jPanel);

                setBounds(500, 400, 450, 450);
                setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
                setLocationRelativeTo(null);
                setVisible(true);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }

            class ApplyContactInfoListener implements ActionListener {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if(e.getSource() == okButton) {
                        String resultMsg;

                        String name = nameInputField.getText();
                        String email = emailInputField.getText();
                        String phoneNumber = phoneNumberInputField.getText();
                        String sipIp = sipIpInputField.getText();

                        int sipPort = 0;
                        String sipPortString = sipPortInputField.getText();
                        if (sipPortString != null && sipPortString.length() > 0) {
                            sipPort = Integer.parseInt(sipPortString);
                        }

                        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
                        if (configManager.getFromPort() == sipPort) {
                            resultMsg = "Fail to modify the contact info. Same port number with local. (" + sipPort + ")";
                            logger.warn("Fail to modify the contact info. Same port number with local. ({})", sipPort);
                            FrameManager.getInstance().popUpWarnMsgToFrame(resultMsg);
                            FrameManager.getInstance().appendTextToFrame(resultMsg + "\n");
                            return;
                        }

                        ContactInfo contactInfo = ContactManager.getInstance().addContactInfo(
                                name,
                                email,
                                phoneNumber,
                                sipIp,
                                sipPort
                        );

                        if (contactInfo != null) {
                            // 1) Add to the table
                            addContactToTable(contactInfo);

                            // 2) Add to the file
                            if (ContactManager.getInstance().addContactInfoToFile(contactInfo)) {
                                resultMsg = "Success to add the contact info. (" + contactInfo + ")";
                                logger.debug("Success to add the contact info. ({})", contactInfo);
                            } else {
                                resultMsg = "Fail to add the contact info. (FILE IO ERROR) (" + contactInfo + ")";
                                logger.warn("Fail to add the contact info. (FILE IO ERROR) ({})", contactInfo);
                                FrameManager.getInstance().popUpWarnMsgToFrame(resultMsg);
                            }
                        } else {
                            resultMsg = "Fail to add the contact info. (" + phoneNumber + ")";
                            logger.warn("Fail to add the contact info. ({})", phoneNumber);
                            FrameManager.getInstance().popUpWarnMsgToFrame(resultMsg);
                            FrameManager.getInstance().appendTextToFrame(resultMsg + "\n");
                            return;
                        }

                        FrameManager.getInstance().appendTextToFrame(resultMsg + "\n");

                        dispose();
                    }
                }
            }
        }
    }

    class ModifyContactListener implements ActionListener {

        private ContactModifyFrame contactModifyFrame;

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == modifyContactButton) {
                if (contactModifyFrame != null) {
                    contactModifyFrame.dispose();
                }

                JTable contactTable = contactPanel.getContactTable();
                if (contactTable == null) {
                    return;
                }

                int selectedIndex = contactTable.getSelectedRow();
                if (selectedIndex != -1) {
                    String[] tableData = getTableDataAtRow(contactTable, selectedIndex);
                    if (tableData != null) {
                        if (selectedIndex >= 0 && selectedIndex < contactTable.getRowCount()) {
                            contactModifyFrame = new ContactModifyFrame(tableData, selectedIndex);
                        }
                    }
                }
            }
        }

        class ContactModifyFrame extends JFrame implements ActionListener{

            private final JButton okButton;

            private final JTextField nameInputField = new JTextField(20);
            private final JTextField emailInputField = new JTextField(20);
            private final JTextField phoneNumberInputField = new JTextField(20);
            private final JTextField sipIpInputField = new JTextField(20);
            private final JTextField sipPortInputField = new JTextField(20);

            private final String curPhoneNumber;
            private final int selectedRowIndex;

            public ContactModifyFrame(String[] tableData, int selectedRowIndex) {
                super("ContactModifyFrame");

                logger.debug("[Modify] tableData: {}, selectedRowIndex: {}", tableData, selectedRowIndex);
                this.selectedRowIndex = selectedRowIndex;
                this.curPhoneNumber = tableData[2];

                //
                JPanel jPanel = new JPanel();
                GridBagConstraints mainGB = new GridBagConstraints();
                mainGB.anchor = GridBagConstraints.WEST;
                mainGB.ipadx = 10; mainGB.ipady = 10;
                jPanel.setLayout(new GridBagLayout());
                //

                //
                JLabel nameInputLabel = FrameManager.getInstance().createLabel("name: ");
                mainGB.gridx = 0; mainGB.gridy = 0;
                jPanel.add(nameInputLabel, mainGB);
                mainGB.gridx = 1; mainGB.gridy = 0;
                nameInputField.setDocument(new JTextFieldLimit(10));
                nameInputField.setText(tableData[0]);
                jPanel.add(nameInputField, mainGB);
                //

                //
                JLabel emailInputLabel = FrameManager.getInstance().createLabel("email: ");
                mainGB.gridx = 0; mainGB.gridy = 1;
                jPanel.add(emailInputLabel, mainGB);
                mainGB.gridx = 1; mainGB.gridy = 1;
                emailInputField.setDocument(new JTextFieldLimit(20));
                emailInputField.setText(tableData[1]);
                jPanel.add(emailInputField, mainGB);
                //

                //
                JLabel phoneNumberInputLabel = FrameManager.getInstance().createLabel("phone-number: ");
                mainGB.gridx = 0; mainGB.gridy = 2;
                jPanel.add(phoneNumberInputLabel, mainGB);
                mainGB.gridx = 1; mainGB.gridy = 2;
                phoneNumberInputField.setDocument(new JTextFieldLimit(20));
                phoneNumberInputField.setText(tableData[2]);
                jPanel.add(phoneNumberInputField, mainGB);
                //

                //
                JLabel sipIpInputLabel = FrameManager.getInstance().createLabel("sip-ip: ");
                mainGB.gridx = 0; mainGB.gridy = 3;
                jPanel.add(sipIpInputLabel, mainGB);
                mainGB.gridx = 1; mainGB.gridy = 3;
                sipIpInputField.setDocument(new JTextFieldLimit(15));
                sipIpInputField.setText(tableData[3]);
                jPanel.add(sipIpInputField, mainGB);
                //

                //
                JLabel sipPortInputLabel = FrameManager.getInstance().createLabel("sip-port: ");
                mainGB.gridx = 0; mainGB.gridy = 4;
                jPanel.add(sipPortInputLabel, mainGB);
                mainGB.gridx = 1; mainGB.gridy = 4;
                sipPortInputField.setDocument(new JTextFieldLimit(5));
                sipPortInputField.setText(tableData[4]);
                jPanel.add(sipPortInputField, mainGB);
                //

                //
                JPanel buttonPanel = new JPanel();
                GridBagConstraints buttonGB = new GridBagConstraints();
                buttonGB.ipadx = 10; buttonGB.ipady = 10;
                buttonGB.anchor = GridBagConstraints.WEST;
                buttonPanel.setLayout(new GridBagLayout());

                okButton = new JButton("Ok");
                okButton.addActionListener(new ApplyContactInfoListener());
                buttonGB.gridx = 0; buttonGB.gridy = 0;
                buttonPanel.add(okButton, buttonGB);

                JButton closeButton = new JButton("Close");
                closeButton.addActionListener(this);
                buttonGB.gridx = 1; buttonGB.gridy = 0;
                buttonPanel.add(closeButton, buttonGB);

                mainGB.gridx = 0; mainGB.gridy = 5;
                jPanel.add(buttonPanel, mainGB);
                //

                add(jPanel);

                setBounds(500, 400, 450, 450);
                setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
                setLocationRelativeTo(null);
                setVisible(true);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }

            class ApplyContactInfoListener implements ActionListener {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if(e.getSource() == okButton) {
                        String resultMsg;

                        String name = nameInputField.getText();
                        String email = emailInputField.getText();
                        String phoneNumber = phoneNumberInputField.getText();
                        String sipIp = sipIpInputField.getText();

                        int sipPort = 0;
                        String sipPortString = sipPortInputField.getText();
                        if (sipPortString != null && sipPortString.length() > 0) {
                            sipPort = Integer.parseInt(sipPortString);
                        }

                        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
                        if (configManager.getFromPort() == sipPort) {
                            resultMsg = "Fail to modify the contact info. Same port number with local. (" + sipPort + ")";
                            logger.warn("Fail to modify the contact info. Same port number with local. ({})", sipPort);
                            FrameManager.getInstance().popUpWarnMsgToFrame(resultMsg);
                            FrameManager.getInstance().appendTextToFrame(resultMsg + "\n");
                            return;
                        }

                        ContactInfo contactInfo = ContactManager.getInstance().getContactInfoByPhoneNumber(curPhoneNumber);
                        if (contactInfo == null) {
                            resultMsg = "Fail to modify the contact info. Not found contact info. (" + curPhoneNumber + ")";
                            logger.warn("Fail to modify the contact info. Not found contact info. ({})", curPhoneNumber);
                            FrameManager.getInstance().popUpWarnMsgToFrame(resultMsg);
                            FrameManager.getInstance().appendTextToFrame(resultMsg + "\n");
                            return;
                        }

                        ContactInfo otherContactInfo = ContactManager.getInstance().getContactInfoByPhoneNumber(phoneNumber);
                        if (otherContactInfo != null) {
                            resultMsg = "Fail to modify the contact info. Other contact info is detected by the phone number. (" + phoneNumber + ")";
                            logger.warn("Fail to modify the contact info. Other contact info is detected by the phone number. ({})", phoneNumber);
                            FrameManager.getInstance().popUpWarnMsgToFrame(resultMsg);
                            FrameManager.getInstance().appendTextToFrame(resultMsg + "\n");
                            return;
                        }

                        contactInfo.setName(name);
                        contactInfo.setEmail(email);
                        contactInfo.setPhoneNumber(phoneNumber);
                        contactInfo.setSipIp(sipIp);
                        contactInfo.setSipPort(sipPort);

                        // 1) Add to the table
                        modifyContactToTable(selectedRowIndex, contactInfo);

                        // 2) Add to the file
                        if (ContactManager.getInstance().reWriteContactInfoFromFile(ContactManager.getInstance().cloneContactInfoSet())) {
                            resultMsg = "Success to modify the contact info. (" + contactInfo + ")";
                            logger.debug("Success to modify the contact info. ({})", contactInfo);
                        } else {
                            resultMsg = "Fail to modify the contact info. (" + contactInfo + ")";
                            logger.warn("Fail to modify the contact info. ({})", contactInfo);
                            FrameManager.getInstance().popUpWarnMsgToFrame(resultMsg);
                        }

                        FrameManager.getInstance().appendTextToFrame(resultMsg + "\n");

                        dispose();
                    }
                }
            }
        }
    }

    class RemoveContactListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == removeContactButton) {
                removeContactFromTable();
            }
        }
    }
}
