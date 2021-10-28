package client.gui.model;

import client.gui.FrameManager;
import client.gui.model.contact.ContactPanel;
import client.gui.model.contact.base.ContactInfo;
import client.gui.model.contact.base.ContactManager;
import service.ServiceManager;

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

    public ContactPanel getContactPanel() {
        return contactPanel;
    }

    public void addContactToTable(ContactInfo contactInfo) {
        contactPanel.addContact(contactInfo);
    }

    public int removeContactFromTable() {
        JTable contactTable = contactPanel.getContactTable();
        if (contactTable == null) {
            return -1;
        }

        int selectedIndex = contactTable.getSelectedRow();
        if (selectedIndex != -1) {
            DefaultTableModel model = (DefaultTableModel) contactTable.getModel();
            model.removeRow(selectedIndex);
        }

        return selectedIndex;
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
                    DefaultTableModel model = (DefaultTableModel) contactTable.getModel();
                    // TODO

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

            private final JTextField nameInputField = new JTextField(30);
            private final JTextField emailInputField = new JTextField(30);
            private final JTextField phoneNumberInputField = new JTextField(30);
            private final JTextField sipIpInputField = new JTextField(30);
            private final JTextField sipPortInputField = new JTextField(30);
            
            public ContactAddFrame() {
                super("ContactAddFrame");

                //
                JPanel jPanel = new JPanel();
                GridBagConstraints mainGB = new GridBagConstraints();
                mainGB.ipadx = 10;
                mainGB.ipady = 10;
                mainGB.anchor = GridBagConstraints.WEST;
                jPanel.setLayout(new GridBagLayout());
                //

                //
                JPanel nameInputPanel = new JPanel();
                GridBagConstraints nameInputGB = new GridBagConstraints();
                nameInputGB.ipadx = 100;
                nameInputGB.ipady = 10;
                nameInputGB.anchor = GridBagConstraints.WEST;
                nameInputPanel.setLayout(new GridBagLayout());

                JLabel nameInputLabel = FrameManager.getInstance().createLabel("name: ");
                nameInputGB.gridx = 0;
                nameInputGB.gridy = 0;
                nameInputPanel.add(nameInputLabel, nameInputGB);
                nameInputGB.gridx = 1;
                nameInputPanel.add(nameInputField, nameInputGB);

                mainGB.gridx = 0;
                mainGB.gridy = 0;
                jPanel.add(nameInputPanel, mainGB);
                //

                //
                JPanel emailInputPanel = new JPanel();
                GridBagConstraints emailInputGB = new GridBagConstraints();
                emailInputGB.ipadx = 100;
                emailInputGB.ipady = 10;
                emailInputGB.anchor = GridBagConstraints.WEST;
                emailInputPanel.setLayout(new GridBagLayout());

                JLabel emailInputLabel = FrameManager.getInstance().createLabel("email: ");
                emailInputGB.gridx = 0;
                emailInputGB.gridy = 0;
                emailInputPanel.add(emailInputLabel, emailInputGB);
                emailInputGB.gridx = 1;
                emailInputPanel.add(emailInputField, emailInputGB);

                mainGB.gridx = 0;
                mainGB.gridy = 1;
                jPanel.add(emailInputPanel, mainGB);
                //

                //
                JPanel phoneNumberInputPanel = new JPanel();
                GridBagConstraints phoneNumberInputGB = new GridBagConstraints();
                phoneNumberInputGB.ipadx = 100;
                phoneNumberInputGB.ipady = 10;
                phoneNumberInputGB.anchor = GridBagConstraints.WEST;
                phoneNumberInputPanel.setLayout(new GridBagLayout());

                JLabel phoneNumberInputLabel = FrameManager.getInstance().createLabel("phone-number: ");
                phoneNumberInputGB.gridx = 0;
                phoneNumberInputGB.gridy = 0;
                phoneNumberInputPanel.add(phoneNumberInputLabel, phoneNumberInputGB);
                phoneNumberInputGB.gridx = 1;
                phoneNumberInputPanel.add(phoneNumberInputField, phoneNumberInputGB);

                mainGB.gridx = 0;
                mainGB.gridy = 2;
                jPanel.add(phoneNumberInputPanel, mainGB);
                //

                //
                JPanel sipIpInputPanel = new JPanel();
                GridBagConstraints sipIpInputGB = new GridBagConstraints();
                sipIpInputGB.ipadx = 100;
                sipIpInputGB.ipady = 10;
                sipIpInputGB.anchor = GridBagConstraints.WEST;
                sipIpInputPanel.setLayout(new GridBagLayout());

                JLabel sipIpInputLabel = FrameManager.getInstance().createLabel("sip-ip: ");
                sipIpInputGB.gridx = 0;
                sipIpInputGB.gridy = 0;
                sipIpInputPanel.add(sipIpInputLabel, sipIpInputGB);
                sipIpInputGB.gridx = 1;
                sipIpInputPanel.add(sipIpInputField, sipIpInputGB);

                mainGB.gridx = 0;
                mainGB.gridy = 3;
                jPanel.add(sipIpInputPanel, mainGB);
                //

                //
                JPanel sipPortInputPanel = new JPanel();
                GridBagConstraints sipPortInputGB = new GridBagConstraints();
                sipPortInputGB.ipadx = 100;
                sipPortInputGB.ipady = 10;
                sipPortInputGB.anchor = GridBagConstraints.WEST;
                sipPortInputPanel.setLayout(new GridBagLayout());

                JLabel sipPortInputLabel = FrameManager.getInstance().createLabel("sip-port: ");
                sipPortInputGB.gridx = 0;
                sipPortInputGB.gridy = 0;
                sipPortInputPanel.add(sipPortInputLabel, sipPortInputGB);
                sipPortInputGB.gridx = 1;
                sipPortInputGB.anchor = GridBagConstraints.EAST;
                sipPortInputPanel.add(sipPortInputField, sipPortInputGB);

                mainGB.gridx = 0;
                mainGB.gridy = 4;
                jPanel.add(sipPortInputPanel, mainGB);
                //

                //
                okButton = new JButton("Ok");
                okButton.addActionListener(new ApplyContactInfoListener());
                mainGB.gridx = 0;
                mainGB.gridy = 5;
                jPanel.add(okButton, mainGB);

                JButton closeButton = new JButton("Close");
                closeButton.addActionListener(this);
                mainGB.gridx = 1;
                mainGB.gridy = 5;
                jPanel.add(closeButton, mainGB);
                //

                add(jPanel);

                setBounds(500, 400, 400, 400);
                setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
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

                        // TODO : File update

                        //

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
                int selectedIndex = 0;
                if ((selectedIndex = removeContactFromTable()) > 0) {
                    // TODO : File update
                    //
                }
            }
        }
    }
}
