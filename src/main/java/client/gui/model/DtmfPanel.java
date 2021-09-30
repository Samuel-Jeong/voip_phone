package client.gui.model;

import config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AppInstance;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static client.gui.model.ClientFrame.appendText;

/**
 * @class public class DtmfPanel
 * @brief DtmfPanel class
 */
public class DtmfPanel {

    private static final Logger logger = LoggerFactory.getLogger(DtmfPanel.class);

    private static final JButton[] dtmfButtons = new JButton[12];

    public static JPanel createKeypadPanel() {
        JPanel keypadPanel = new JPanel();
        keypadPanel.setLayout(new GridLayout(4, 3));

        for (int i = 0; i < dtmfButtons.length; i++) {
            if (i == 9) {
                dtmfButtons[i] = new JButton("*");
            } else if (i == 10) {
                dtmfButtons[i] = new JButton("0");
            } else if (i == 11) {
                dtmfButtons[i] = new JButton("#");
            } else {
                dtmfButtons[i] = new JButton(String.valueOf(i + 1));
            }

            switch (i) {
                case 0:
                    dtmfButtons[i].addActionListener(new Dtmf1Listener());
                    break;
                case 1:
                    dtmfButtons[i].addActionListener(new Dtmf2Listener());
                    break;
                case 2:
                    dtmfButtons[i].addActionListener(new Dtmf3Listener());
                    break;
                case 3:
                    dtmfButtons[i].addActionListener(new Dtmf4Listener());
                    break;
                case 4:
                    dtmfButtons[i].addActionListener(new Dtmf5Listener());
                    break;
                case 5:
                    dtmfButtons[i].addActionListener(new Dtmf6Listener());
                    break;
                case 6:
                    dtmfButtons[i].addActionListener(new Dtmf7Listener());
                    break;
                case 7:
                    dtmfButtons[i].addActionListener(new Dtmf8Listener());
                    break;
                case 8:
                    dtmfButtons[i].addActionListener(new Dtmf9Listener());
                    break;
                case 9:
                    dtmfButtons[i].addActionListener(new Dtmf10Listener());
                    break;
                case 10:
                    dtmfButtons[i].addActionListener(new Dtmf11Listener());
                    break;
                case 11:
                    dtmfButtons[i].addActionListener(new Dtmf12Listener());
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + i);
            }

            //buttons[i].setPreferredSize(new Dimension(20, 20));
            keypadPanel.add(dtmfButtons[i]);
        }

        return keypadPanel;
    }

    static class Dtmf1Listener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // Client 만 사용 가능
            ConfigManager configManager = AppInstance.getInstance().getConfigManager();
            if (!configManager.isUseClient()) {
                return;
            }

            // 1
            if(e.getSource() == dtmfButtons[0]) {
                logger.debug("DTMF: 1");
                appendText("DTMF: 1\n");
            }
        }
    }

    static class Dtmf2Listener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // Client 만 사용 가능
            ConfigManager configManager = AppInstance.getInstance().getConfigManager();
            if (!configManager.isUseClient()) {
                return;
            }

            // 2
            if(e.getSource() == dtmfButtons[1]) {
                logger.debug("DTMF: 2");
                appendText("DTMF: 2\n");
            }
        }
    }

    static class Dtmf3Listener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // Client 만 사용 가능
            ConfigManager configManager = AppInstance.getInstance().getConfigManager();
            if (!configManager.isUseClient()) {
                return;
            }

            // 3
            if(e.getSource() == dtmfButtons[2]) {
                logger.debug("DTMF: 3");
                appendText("DTMF: 3\n");
            }
        }
    }

    static class Dtmf4Listener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // Client 만 사용 가능
            ConfigManager configManager = AppInstance.getInstance().getConfigManager();
            if (!configManager.isUseClient()) {
                return;
            }

            // 4
            if(e.getSource() == dtmfButtons[3]) {
                logger.debug("DTMF: 4");
                appendText("DTMF: 4\n");
            }
        }
    }

    static class Dtmf5Listener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // Client 만 사용 가능
            ConfigManager configManager = AppInstance.getInstance().getConfigManager();
            if (!configManager.isUseClient()) {
                return;
            }

            // 5
            if(e.getSource() == dtmfButtons[4]) {
                logger.debug("DTMF: 5");
                appendText("DTMF: 5\n");
            }
        }
    }

    static class Dtmf6Listener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // Client 만 사용 가능
            ConfigManager configManager = AppInstance.getInstance().getConfigManager();
            if (!configManager.isUseClient()) {
                return;
            }

            // 6
            if(e.getSource() == dtmfButtons[5]) {
                logger.debug("DTMF: 6");
                appendText("DTMF: 6\n");
            }
        }
    }

    static class Dtmf7Listener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // Client 만 사용 가능
            ConfigManager configManager = AppInstance.getInstance().getConfigManager();
            if (!configManager.isUseClient()) {
                return;
            }

            // 7
            if(e.getSource() == dtmfButtons[6]) {
                logger.debug("DTMF: 7");
                appendText("DTMF: 7\n");
            }
        }
    }

    static class Dtmf8Listener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // Client 만 사용 가능
            ConfigManager configManager = AppInstance.getInstance().getConfigManager();
            if (!configManager.isUseClient()) {
                return;
            }

            // 8
            if(e.getSource() == dtmfButtons[7]) {
                logger.debug("DTMF: 8");
                appendText("DTMF: 8\n");
            }
        }
    }

    static class Dtmf9Listener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // Client 만 사용 가능
            ConfigManager configManager = AppInstance.getInstance().getConfigManager();
            if (!configManager.isUseClient()) {
                return;
            }

            // 9
            if(e.getSource() == dtmfButtons[8]) {
                logger.debug("DTMF: 9");
                appendText("DTMF: 9\n");
            }
        }
    }

    static class Dtmf10Listener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // Client 만 사용 가능
            ConfigManager configManager = AppInstance.getInstance().getConfigManager();
            if (!configManager.isUseClient()) {
                return;
            }

            // *
            if(e.getSource() == dtmfButtons[9]) {
                logger.debug("DTMF: *");
                appendText("DTMF: *\n");
            }
        }
    }

    static class Dtmf11Listener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // Client 만 사용 가능
            ConfigManager configManager = AppInstance.getInstance().getConfigManager();
            if (!configManager.isUseClient()) {
                return;
            }

            // 0
            if(e.getSource() == dtmfButtons[10]) {
                logger.debug("DTMF: 0");
                appendText("DTMF: 0\n");
            }
        }
    }

    static class Dtmf12Listener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // Client 만 사용 가능
            ConfigManager configManager = AppInstance.getInstance().getConfigManager();
            if (!configManager.isUseClient()) {
                return;
            }

            // #
            if(e.getSource() == dtmfButtons[11]) {
                logger.debug("DTMF: #");
                appendText("DTMF: #\n");
            }
        }
    }

}
