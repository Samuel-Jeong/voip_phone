package client.gui.model;

import client.VoipClient;
import client.gui.FrameManager;
import client.gui.model.contact.ContactPanel;
import client.gui.model.contact.base.ContactInfo;
import client.gui.model.contact.base.ContactManager;
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

        removeContactButton = new JButton("Remove");
        removeContactButton.addActionListener(new RemoveContactListener());
        removeContactButton.setEnabled(true);
        buttonPanelGB.gridx = 3;
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

        int rowCount = defaultTableModel.getRowCount() - 1;
        int columnCount = defaultTableModel.getColumnCount() - 1;
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

        int columnCount = defaultTableModel.getColumnCount() - 1;
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
                                }

                                int toPort = contactInfo.getSipPort();
                                if (toPort > 0) {
                                    SignalManager.getInstance().setToPort(toPort);
                                    configManager.setToPort(toPort);
                                    configManager.setIniValue(ConfigManager.SECTION_SIGNAL, ConfigManager.FIELD_TO_PORT, String.valueOf(toPort));
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
                jPanel.add(nameInputField, mainGB);
                //

                //
                JLabel emailInputLabel = FrameManager.getInstance().createLabel("email: ");
                mainGB.gridx = 0; mainGB.gridy = 1;
                jPanel.add(emailInputLabel, mainGB);
                mainGB.gridx = 1; mainGB.gridy = 1;
                jPanel.add(emailInputField, mainGB);
                //

                //
                JLabel phoneNumberInputLabel = FrameManager.getInstance().createLabel("phone-number: ");
                mainGB.gridx = 0; mainGB.gridy = 2;
                jPanel.add(phoneNumberInputLabel, mainGB);
                mainGB.gridx = 1; mainGB.gridy = 2;
                jPanel.add(phoneNumberInputField, mainGB);
                //

                //
                JLabel sipIpInputLabel = FrameManager.getInstance().createLabel("sip-ip: ");
                mainGB.gridx = 0; mainGB.gridy = 3;
                jPanel.add(sipIpInputLabel, mainGB);
                mainGB.gridx = 1; mainGB.gridy = 3;
                jPanel.add(sipIpInputField, mainGB);
                //

                //
                JLabel sipPortInputLabel = FrameManager.getInstance().createLabel("sip-port: ");
                mainGB.gridx = 0; mainGB.gridy = 4;
                jPanel.add(sipPortInputLabel, mainGB);
                mainGB.gridx = 1; mainGB.gridy = 4;
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
                        String name = nameInputField.getText();
                        String email = emailInputField.getText();
                        String phoneNumber = phoneNumberInputField.getText();
                        String sipIp = sipIpInputField.getText();
                        int sipPort = Integer.parseInt(sipPortInputField.getText());

                        ContactInfo contactInfo = ContactManager.getInstance().addContactInfo(
                                name,
                                email,
                                phoneNumber,
                                sipIp,
                                sipPort
                        );
                        addContactToTable(contactInfo);

                        if (ContactManager.getInstance().addContactInfoToFile(contactInfo)) {
                            logger.debug("Success to add the contact info. ({})", contactInfo);
                        }

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
