package client.gui.model.contact;

import client.gui.model.contact.base.ContactInfo;
import client.gui.model.contact.base.ContactManager;
import javafx.scene.control.SelectionMode;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

/**
 * @class public class ContactPanel
 * @brief ContactPanel class
 */
public class ContactPanel {

    public static final int CONTACT_PANEL_TABLE_MAX_WIDTH = 670;
    public static final int CONTACT_PANEL_TABLE_MAX_HEIGHT = 559;
    public static final int CONTACT_PANEL_TABLE_MIN_HEIGHT = 25;
    public static final Dimension CONTACT_PANEL_ROW_DIMENSION = new Dimension(CONTACT_PANEL_TABLE_MAX_WIDTH, CONTACT_PANEL_TABLE_MIN_HEIGHT);

    private static final String[] CONTACT_PANEL_TABLE_HEADERS = { "name", "email", "mdn", "sip_ip", "sip_port" };
    private static final int[] CONTACT_PANEL_TABLE_HEADER_WIDTH = { 100, 180, 180, 140, 67 };

    private JPanel panel;
    private JTable contactTable;

    public ContactPanel() {
        // Nothing
    }

    public void createContactPanel() {
        panel = new JPanel();
        panel.setOpaque(false);

        DefaultTableModel defaultTableModel = new DefaultTableModel(CONTACT_PANEL_TABLE_HEADERS, 0) { @Override public boolean isCellEditable(int row, int column){ return false; }};
        contactTable = new JTable(defaultTableModel);
        contactTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactTable.setSize(CONTACT_PANEL_TABLE_MAX_WIDTH, CONTACT_PANEL_TABLE_MAX_HEIGHT);
        contactTable.setPreferredSize(new Dimension(CONTACT_PANEL_TABLE_MAX_WIDTH, 400));

        JTableHeader jTableHeader = contactTable.getTableHeader();
        jTableHeader.setSize(CONTACT_PANEL_ROW_DIMENSION);
        jTableHeader.setPreferredSize(CONTACT_PANEL_ROW_DIMENSION);

        TableColumnModel columnModel = contactTable.getColumnModel();
        for (int i = 0; i < CONTACT_PANEL_TABLE_HEADERS.length; i++) {
            columnModel.getColumn(i).setWidth(CONTACT_PANEL_TABLE_HEADER_WIDTH[i]);
            columnModel.getColumn(i).setPreferredWidth(CONTACT_PANEL_TABLE_HEADER_WIDTH[i]);
        }

        JScrollPane jScrollPane = new JScrollPane(contactTable);
        jScrollPane.setPreferredSize(new Dimension(CONTACT_PANEL_TABLE_MAX_WIDTH, CONTACT_PANEL_TABLE_MAX_HEIGHT));
        jScrollPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        panel.add(jScrollPane, BorderLayout.WEST);

        for (ContactInfo contactInfo : ContactManager.getInstance().cloneContactInfoSet()) {
            addContact(contactInfo);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

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

    public void modifyContact(int row, ContactInfo contactInfo) {
        if (contactInfo == null) {
            return;
        }

        DefaultTableModel model = (DefaultTableModel) contactTable.getModel();
        model.removeRow(row);
        model.insertRow(row, contactInfo.toArray());
    }

    ////////////////////////////////////////////////////////////////////////////////

    public static String[] getContactPanelTableHeaders() {
        return CONTACT_PANEL_TABLE_HEADERS;
    }

    public static int[] getContactPanelTableHeaderWidth() {
        return CONTACT_PANEL_TABLE_HEADER_WIDTH;
    }

}
