package client.gui.model.dtmf;

import client.VoipClient;
import client.module.SoundHandler;
import client.module.base.MediaFrame;
import client.module.base.UdpReceiver;
import client.module.base.UdpSender;
import config.ConfigManager;
import media.dtmf.DtmfUnit;
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

    private static final int DTMF_COUNT = 12;
    private static final JButton[] dtmfButtons = new JButton[DTMF_COUNT];

    private static final int volume = 12;

    ////////////////////////////////////////////////////////////////////////////////

    public static JPanel createKeypadPanel() {
        JPanel keypadPanel = new JPanel();

        try {
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
                dtmfButtons[i].setEnabled(false);
                keypadPanel.add(dtmfButtons[i]);
            }

            DtmfSoundGenerator.getInstance().start();
        } catch (Exception e1) {
            logger.warn("DtmfPanel.createDtmfPanel.Exception", e1);
            DtmfSoundGenerator.getInstance().stop();
        }

        return keypadPanel;
    }

    ////////////////////////////////////////////////////////////////////////////////

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
                DtmfSoundGenerator.getInstance().playTone(1);
                logger.debug("DTMF: 1");

                if (VoipClient.getInstance().isStarted()) {
                    SoundHandler soundHandler = VoipClient.getInstance().getSoundHandler();
                    if (soundHandler == null) {
                        return;
                    }

                    DtmfUnit dtmfUnit = new DtmfUnit(
                            DtmfUnit.DIGIT_1,
                            true,
                            false,
                            volume,
                            1000
                    );

                    UdpSender udpSender = soundHandler.getUdpSender();
                    if (udpSender != null) {
                        udpSender.getSendBuffer().offer(
                                new MediaFrame(
                                        true,
                                        dtmfUnit.getData()
                                )
                        );
                    }

                    UdpReceiver udpReceiver = soundHandler.getUdpReceiver();
                    if (udpReceiver != null) {
                        udpReceiver.getRecvBuffer().offer(
                                new MediaFrame(
                                        true,
                                        dtmfUnit.getData()
                                )
                        );
                    }

                    appendText("DTMF: 1\n");
                }
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
                DtmfSoundGenerator.getInstance().playTone(2);
                logger.debug("DTMF: 2");

                if (VoipClient.getInstance().isStarted()) {
                    SoundHandler soundHandler = VoipClient.getInstance().getSoundHandler();
                    if (soundHandler == null) {
                        return;
                    }

                    DtmfUnit dtmfUnit = new DtmfUnit(
                            DtmfUnit.DIGIT_2,
                            true,
                            false,
                            volume,
                            1000
                    );

                    UdpSender udpSender = soundHandler.getUdpSender();
                    if (udpSender != null) {
                        udpSender.getSendBuffer().offer(
                                new MediaFrame(
                                        true,
                                        dtmfUnit.getData()
                                )
                        );
                    }

                    UdpReceiver udpReceiver = soundHandler.getUdpReceiver();
                    if (udpReceiver != null) {
                        udpReceiver.getRecvBuffer().offer(
                                new MediaFrame(
                                        true,
                                        dtmfUnit.getData()
                                )
                        );
                    }

                    appendText("DTMF: 2\n");
                }
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
                DtmfSoundGenerator.getInstance().playTone(3);
                logger.debug("DTMF: 3");

                if (VoipClient.getInstance().isStarted()) {
                    SoundHandler soundHandler = VoipClient.getInstance().getSoundHandler();
                    if (soundHandler == null) {
                        return;
                    }

                    DtmfUnit dtmfUnit = new DtmfUnit(
                            DtmfUnit.DIGIT_3,
                            true,
                            false,
                            volume,
                            1000
                    );

                    UdpSender udpSender = soundHandler.getUdpSender();
                    if (udpSender != null) {
                        udpSender.getSendBuffer().offer(
                                new MediaFrame(
                                        true,
                                        dtmfUnit.getData()
                                )
                        );
                    }

                    UdpReceiver udpReceiver = soundHandler.getUdpReceiver();
                    if (udpReceiver != null) {
                        udpReceiver.getRecvBuffer().offer(
                                new MediaFrame(
                                        true,
                                        dtmfUnit.getData()
                                )
                        );
                    }

                    appendText("DTMF: 3\n");
                }
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
                DtmfSoundGenerator.getInstance().playTone(4);
                logger.debug("DTMF: 4");

                if (VoipClient.getInstance().isStarted()) {
                    SoundHandler soundHandler = VoipClient.getInstance().getSoundHandler();
                    if (soundHandler == null) {
                        return;
                    }

                    DtmfUnit dtmfUnit = new DtmfUnit(
                            DtmfUnit.DIGIT_4,
                            true,
                            false,
                            volume,
                            1000
                    );

                    UdpSender udpSender = soundHandler.getUdpSender();
                    if (udpSender != null) {
                        udpSender.getSendBuffer().offer(
                                new MediaFrame(
                                        true,
                                        dtmfUnit.getData()
                                )
                        );
                    }

                    UdpReceiver udpReceiver = soundHandler.getUdpReceiver();
                    if (udpReceiver != null) {
                        udpReceiver.getRecvBuffer().offer(
                                new MediaFrame(
                                        true,
                                        dtmfUnit.getData()
                                )
                        );
                    }

                    appendText("DTMF: 4\n");
                }
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
                DtmfSoundGenerator.getInstance().playTone(5);
                logger.debug("DTMF: 5");

                if (VoipClient.getInstance().isStarted()) {
                    SoundHandler soundHandler = VoipClient.getInstance().getSoundHandler();
                    if (soundHandler == null) {
                        return;
                    }

                    DtmfUnit dtmfUnit = new DtmfUnit(
                            DtmfUnit.DIGIT_5,
                            true,
                            false,
                            volume,
                            1000
                    );

                    UdpSender udpSender = soundHandler.getUdpSender();
                    if (udpSender != null) {
                        udpSender.getSendBuffer().offer(
                                new MediaFrame(
                                        true,
                                        dtmfUnit.getData()
                                )
                        );
                    }

                    UdpReceiver udpReceiver = soundHandler.getUdpReceiver();
                    if (udpReceiver != null) {
                        udpReceiver.getRecvBuffer().offer(
                                new MediaFrame(
                                        true,
                                        dtmfUnit.getData()
                                )
                        );
                    }

                    logger.debug("DTMF: 5");
                    appendText("DTMF: 5\n");
                }
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
                DtmfSoundGenerator.getInstance().playTone(6);
                logger.debug("DTMF: 6");

                if (VoipClient.getInstance().isStarted()) {
                    SoundHandler soundHandler = VoipClient.getInstance().getSoundHandler();
                    if (soundHandler == null) {
                        return;
                    }

                    DtmfUnit dtmfUnit = new DtmfUnit(
                            DtmfUnit.DIGIT_6,
                            true,
                            false,
                            volume,
                            1000
                    );

                    UdpSender udpSender = soundHandler.getUdpSender();
                    if (udpSender != null) {
                        udpSender.getSendBuffer().offer(
                                new MediaFrame(
                                        true,
                                        dtmfUnit.getData()
                                )
                        );
                    }

                    UdpReceiver udpReceiver = soundHandler.getUdpReceiver();
                    if (udpReceiver != null) {
                        udpReceiver.getRecvBuffer().offer(
                                new MediaFrame(
                                        true,
                                        dtmfUnit.getData()
                                )
                        );
                    }

                    appendText("DTMF: 6\n");
                }
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
                DtmfSoundGenerator.getInstance().playTone(7);
                logger.debug("DTMF: 7");

                if (VoipClient.getInstance().isStarted()) {
                    SoundHandler soundHandler = VoipClient.getInstance().getSoundHandler();
                    if (soundHandler == null) {
                        return;
                    }

                    DtmfUnit dtmfUnit = new DtmfUnit(
                            DtmfUnit.DIGIT_7,
                            true,
                            false,
                            volume,
                            1000
                    );

                    UdpSender udpSender = soundHandler.getUdpSender();
                    if (udpSender != null) {
                        udpSender.getSendBuffer().offer(
                                new MediaFrame(
                                        true,
                                        dtmfUnit.getData()
                                )
                        );
                    }

                    appendText("DTMF: 7\n");
                }
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
                DtmfSoundGenerator.getInstance().playTone(8);
                logger.debug("DTMF: 8");

                if (VoipClient.getInstance().isStarted()) {
                    SoundHandler soundHandler = VoipClient.getInstance().getSoundHandler();
                    if (soundHandler == null) {
                        return;
                    }

                    DtmfUnit dtmfUnit = new DtmfUnit(
                            DtmfUnit.DIGIT_8,
                            true,
                            false,
                            volume,
                            1000
                    );

                    UdpSender udpSender = soundHandler.getUdpSender();
                    if (udpSender != null) {
                        udpSender.getSendBuffer().offer(
                                new MediaFrame(
                                        true,
                                        dtmfUnit.getData()
                                )
                        );
                    }

                    UdpReceiver udpReceiver = soundHandler.getUdpReceiver();
                    if (udpReceiver != null) {
                        udpReceiver.getRecvBuffer().offer(
                                new MediaFrame(
                                        true,
                                        dtmfUnit.getData()
                                )
                        );
                    }

                    appendText("DTMF: 8\n");
                }
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
                DtmfSoundGenerator.getInstance().playTone(9);
                logger.debug("DTMF: 9");

                if (VoipClient.getInstance().isStarted()) {
                    SoundHandler soundHandler = VoipClient.getInstance().getSoundHandler();
                    if (soundHandler == null) {
                        return;
                    }

                    DtmfUnit dtmfUnit = new DtmfUnit(
                            DtmfUnit.DIGIT_9,
                            true,
                            false,
                            volume,
                            1000
                    );

                    UdpSender udpSender = soundHandler.getUdpSender();
                    if (udpSender != null) {
                        udpSender.getSendBuffer().offer(
                                new MediaFrame(
                                        true,
                                        dtmfUnit.getData()
                                )
                        );
                    }

                    UdpReceiver udpReceiver = soundHandler.getUdpReceiver();
                    if (udpReceiver != null) {
                        udpReceiver.getRecvBuffer().offer(
                                new MediaFrame(
                                        true,
                                        dtmfUnit.getData()
                                )
                        );
                    }

                    appendText("DTMF: 9\n");
                }
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
                DtmfSoundGenerator.getInstance().playTone(10);
                logger.debug("DTMF: *");

                if (VoipClient.getInstance().isStarted()) {
                    SoundHandler soundHandler = VoipClient.getInstance().getSoundHandler();
                    if (soundHandler == null) {
                        return;
                    }

                    DtmfUnit dtmfUnit = new DtmfUnit(
                            DtmfUnit.DIGIT_10,
                            true,
                            false,
                            volume,
                            1000
                    );

                    UdpSender udpSender = soundHandler.getUdpSender();
                    if (udpSender != null) {
                        udpSender.getSendBuffer().offer(
                                new MediaFrame(
                                        true,
                                        dtmfUnit.getData()
                                )
                        );
                    }

                    UdpReceiver udpReceiver = soundHandler.getUdpReceiver();
                    if (udpReceiver != null) {
                        udpReceiver.getRecvBuffer().offer(
                                new MediaFrame(
                                        true,
                                        dtmfUnit.getData()
                                )
                        );
                    }

                    appendText("DTMF: *\n");
                }
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
                DtmfSoundGenerator.getInstance().playTone(0);
                logger.debug("DTMF: 0");

                if (VoipClient.getInstance().isStarted()) {
                    SoundHandler soundHandler = VoipClient.getInstance().getSoundHandler();
                    if (soundHandler == null) {
                        return;
                    }

                    DtmfUnit dtmfUnit = new DtmfUnit(
                            DtmfUnit.DIGIT_0,
                            true,
                            false,
                            volume,
                            1000
                    );

                    UdpSender udpSender = soundHandler.getUdpSender();
                    if (udpSender != null) {
                        udpSender.getSendBuffer().offer(
                                new MediaFrame(
                                        true,
                                        dtmfUnit.getData()
                                )
                        );
                    }

                    UdpReceiver udpReceiver = soundHandler.getUdpReceiver();
                    if (udpReceiver != null) {
                        udpReceiver.getRecvBuffer().offer(
                                new MediaFrame(
                                        true,
                                        dtmfUnit.getData()
                                )
                        );
                    }

                    appendText("DTMF: 0\n");
                }
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
                DtmfSoundGenerator.getInstance().playTone(11);
                logger.debug("DTMF: #");

                if (VoipClient.getInstance().isStarted()) {
                    SoundHandler soundHandler = VoipClient.getInstance().getSoundHandler();
                    if (soundHandler == null) {
                        return;
                    }

                    DtmfUnit dtmfUnit = new DtmfUnit(
                            DtmfUnit.DIGIT_11,
                            true,
                            false,
                            volume,
                            1000
                    );

                    UdpSender udpSender = soundHandler.getUdpSender();
                    if (udpSender != null) {
                        udpSender.getSendBuffer().offer(
                                new MediaFrame(
                                        true,
                                        dtmfUnit.getData()
                                )
                        );
                    }

                    UdpReceiver udpReceiver = soundHandler.getUdpReceiver();
                    if (udpReceiver != null) {
                        udpReceiver.getRecvBuffer().offer(
                                new MediaFrame(
                                        true,
                                        dtmfUnit.getData()
                                )
                        );
                    }

                    appendText("DTMF: #\n");
                }
            }
        }
    }

    public static JButton[] getDtmfButtons() {
        return dtmfButtons;
    }

}
