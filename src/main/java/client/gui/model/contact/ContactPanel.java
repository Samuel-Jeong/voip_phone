package client.gui.model.contact;

import client.gui.model.contact.base.ContactInfo;
import client.gui.model.contact.base.ContactManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

/**
 * @class public class ContactPanel
 * @brief ContactPanel class
 */
public class ContactPanel {

    private static final Logger logger = LoggerFactory.getLogger(ContactPanel.class);

    private JPanel panel;
    private JTable contactTable;

    public ContactPanel() {
        // Nothing
    }

    public void createContactPanel() {
        panel = new JPanel();
        panel.setOpaque(false);

        String[] contactTableHeaders = { "name", "email", "mdn", "sip_ip", "sip_port" };
        int[] columnsWidth = { 100, 180, 180, 140, 67 };

        DefaultTableModel defaultTableModel = new DefaultTableModel(contactTableHeaders, 0);

        contactTable = new JTable(defaultTableModel);
        contactTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //contactTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        //contactTable.getModel().addTableModelListener(e -> ColumnsAutoSizer.sizeColumnsToFit(contactTable));
        contactTable.setSize(670, 550);
        contactTable.setPreferredSize(new Dimension(670, 400));

        JTableHeader jTableHeader = contactTable.getTableHeader();
        jTableHeader.setSize(new Dimension(670, 25));
        jTableHeader.setPreferredSize(new Dimension(670, 25));

        TableColumnModel columnModel = contactTable.getColumnModel();
        for (int i = 0; i < contactTableHeaders.length; i++) {
            columnModel.getColumn(i).setWidth(columnsWidth[i]);
            columnModel.getColumn(i).setPreferredWidth(columnsWidth[i]);
        }

        /*int i = 0;
        for (int width : columnsWidth) {
            TableColumn column = contactTable.getColumnModel().getColumn(i++);
            column.setMinWidth(width);
            column.setMaxWidth(width);
            column.setPreferredWidth(width);
        }*/

        JScrollPane jScrollPane = new JScrollPane(contactTable);
        jScrollPane.setPreferredSize(new Dimension(670, 550));
        jScrollPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        panel.add(jScrollPane, BorderLayout.WEST);

        for (ContactInfo contactInfo : ContactManager.getInstance().cloneContactInfoSet()) {
            addContact(contactInfo);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

    static class ColumnsAutoSizer {

        public static void sizeColumnsToFit(JTable table) {
            sizeColumnsToFit(table, 10);
        }

        public static void sizeColumnsToFit(JTable table, int columnMargin) {
            JTableHeader tableHeader = table.getTableHeader();
            if(tableHeader == null) {
                // can't auto size a table without a header
                return;
            }

            FontMetrics headerFontMetrics = tableHeader.getFontMetrics(tableHeader.getFont());
            int[] minWidths = new int[table.getColumnCount()];
            int[] maxWidths = new int[table.getColumnCount()];

            for(int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
                int headerWidth = headerFontMetrics.stringWidth(table.getColumnName(columnIndex));
                minWidths[columnIndex] = headerWidth + columnMargin;
                int maxWidth = getMaximalRequiredColumnWidth(table, columnIndex, headerWidth);
                maxWidths[columnIndex] = Math.max(maxWidth, minWidths[columnIndex]) + columnMargin;
            }

            adjustMaximumWidths(table, minWidths, maxWidths);

            for(int i = 0; i < table.getColumnCount(); i++) {
                if(minWidths[i] > 0) {
                    table.getColumnModel().getColumn(i).setMinWidth(minWidths[i]);
                }

                if(maxWidths[i] > 0) {
                    table.getColumnModel().getColumn(i).setMaxWidth(maxWidths[i]);
                    table.getColumnModel().getColumn(i).setWidth(maxWidths[i]);
                }
            }
        }

        private static void adjustMaximumWidths(JTable table, int[] minWidths, int[] maxWidths) {
            if(table.getWidth() > 0) {
                // to prevent infinite loops in exceptional situations
                int breaker = 0;

                // keep stealing one pixel of the maximum width of the highest column until we can fit in the width of the table
                while(sum(maxWidths) > table.getWidth() && breaker < 10000) {
                    int highestWidthIndex = findLargestIndex(maxWidths);
                    maxWidths[highestWidthIndex] -= 1;
                    maxWidths[highestWidthIndex] = Math.max(maxWidths[highestWidthIndex], minWidths[highestWidthIndex]);
                    breaker++;
                }
            }
        }

        private static int getMaximalRequiredColumnWidth(JTable table, int columnIndex, int headerWidth) {
            int maxWidth = headerWidth;
            TableColumn column = table.getColumnModel().getColumn(columnIndex);
            TableCellRenderer cellRenderer = column.getCellRenderer();

            if(cellRenderer == null) {
                cellRenderer = new DefaultTableCellRenderer();
            }

            for(int row = 0; row < table.getModel().getRowCount(); row++) {
                Component rendererComponent = cellRenderer.getTableCellRendererComponent(table,
                        table.getModel().getValueAt(row, columnIndex),
                        false,
                        false,
                        row,
                        columnIndex);
                double valueWidth = rendererComponent.getPreferredSize().getWidth();
                maxWidth = (int) Math.max(maxWidth, valueWidth);
            }

            return maxWidth;
        }

        private static int findLargestIndex(int[] widths) {
            int largestIndex = 0;
            int largestValue = 0;

            for(int i = 0; i < widths.length; i++) {
                if(widths[i] > largestValue) {
                    largestIndex = i;
                    largestValue = widths[i];
                }
            }

            return largestIndex;
        }

        private static int sum(int[] widths) {
            int sum = 0;

            for(int width : widths) {
                sum += width;
            }

            return sum;
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

}
