package client.gui.model.contact;

import client.gui.model.contact.base.ContactInfo;
import client.gui.model.contact.base.ContactManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * @class public class ContactPanel
 * @brief ContactPanel class
 */
public class ContactPanel {

    private JPanel panel;
    private JTable contactTable;

    public ContactPanel() {
        // Nothing
    }

    public void createContactPanel() {
        panel = new JPanel();
        panel.setOpaque(false);

        String[] contactTableHeaders = { "name", "email", "phone_number", "sip_ip", "sip_port" };
        DefaultTableModel defaultTableModel = new DefaultTableModel(contactTableHeaders, 0);
        contactTable = new JTable(defaultTableModel);
        contactTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane jScrollPane = new JScrollPane(contactTable);
        jScrollPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        panel.add(jScrollPane, BorderLayout.WEST);

        for (ContactInfo contactInfo : ContactManager.getInstance().cloneContactInfoSet()) {
            addContact(contactInfo);
        }
    }

    public JTable getContactTable() {
        return contactTable;
    }

    public JPanel getPanel() {
        return panel;
    }

    public void addContact(ContactInfo contactInfo) {
        if (contactInfo == null) {
            return;
        }

        DefaultTableModel model = (DefaultTableModel) contactTable.getModel();
        model.addRow(contactInfo.toArray());
    }
}
