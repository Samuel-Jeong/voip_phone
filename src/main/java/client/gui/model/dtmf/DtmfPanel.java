package client.gui.model.dtmf;

import media.dtmf.DtmfUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
        keypadPanel.setOpaque(false);

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

                dtmfButtons[i].setMultiClickThreshhold(500);

                switch (i) {
                    case 0:
                        DtmfListener dtmf1Listener = new DtmfListener(DtmfUnit.DIGIT_1, volume);
                        dtmfButtons[i].addMouseListener(new MouseCustomAdapter(50, 100, dtmf1Listener));
                        dtmfButtons[i].addActionListener(dtmf1Listener);
                        break;
                    case 1:
                        DtmfListener dtmf2Listener = new DtmfListener(DtmfUnit.DIGIT_2, volume);
                        dtmfButtons[i].addMouseListener(new MouseCustomAdapter(50, 100, dtmf2Listener));
                        dtmfButtons[i].addActionListener(dtmf2Listener);
                        break;
                    case 2:
                        DtmfListener dtmf3Listener = new DtmfListener(DtmfUnit.DIGIT_3, volume);
                        dtmfButtons[i].addMouseListener(new MouseCustomAdapter(50, 100, dtmf3Listener));
                        dtmfButtons[i].addActionListener(dtmf3Listener);
                        break;
                    case 3:
                        DtmfListener dtmf4Listener = new DtmfListener(DtmfUnit.DIGIT_4, volume);
                        dtmfButtons[i].addMouseListener(new MouseCustomAdapter(50, 100, dtmf4Listener));
                        dtmfButtons[i].addActionListener(dtmf4Listener);
                        break;
                    case 4:
                        DtmfListener dtmf5Listener = new DtmfListener(DtmfUnit.DIGIT_5, volume);
                        dtmfButtons[i].addMouseListener(new MouseCustomAdapter(50, 100, dtmf5Listener));
                        dtmfButtons[i].addActionListener(dtmf5Listener);
                        break;
                    case 5:
                        DtmfListener dtmf6Listener = new DtmfListener(DtmfUnit.DIGIT_6, volume);
                        dtmfButtons[i].addMouseListener(new MouseCustomAdapter(50, 100, dtmf6Listener));
                        dtmfButtons[i].addActionListener(dtmf6Listener);
                        break;
                    case 6:
                        DtmfListener dtmf7Listener = new DtmfListener(DtmfUnit.DIGIT_7, volume);
                        dtmfButtons[i].addMouseListener(new MouseCustomAdapter(50, 100, dtmf7Listener));
                        dtmfButtons[i].addActionListener(dtmf7Listener);
                        break;
                    case 7:
                        DtmfListener dtmf8Listener = new DtmfListener(DtmfUnit.DIGIT_8, volume);
                        dtmfButtons[i].addMouseListener(new MouseCustomAdapter(50, 100, dtmf8Listener));
                        dtmfButtons[i].addActionListener(dtmf8Listener);
                        break;
                    case 8:
                        DtmfListener dtmf9Listener = new DtmfListener(DtmfUnit.DIGIT_9, volume);
                        dtmfButtons[i].addMouseListener(new MouseCustomAdapter(50, 100, dtmf9Listener));
                        dtmfButtons[i].addActionListener(dtmf9Listener);
                        break;
                    case 9:
                        DtmfListener dtmf10Listener = new DtmfListener(DtmfUnit.DIGIT_10, volume);
                        dtmfButtons[i].addMouseListener(new MouseCustomAdapter(50, 100, dtmf10Listener));
                        dtmfButtons[i].addActionListener(dtmf10Listener);
                        break;
                    case 10:
                        DtmfListener dtmf0Listener = new DtmfListener(DtmfUnit.DIGIT_0, volume);
                        dtmfButtons[i].addMouseListener(new MouseCustomAdapter(50, 100, dtmf0Listener));
                        dtmfButtons[i].addActionListener(dtmf0Listener);
                        break;
                    case 11:
                        DtmfListener dtmf11Listener = new DtmfListener(DtmfUnit.DIGIT_11, volume);
                        dtmfButtons[i].addMouseListener(new MouseCustomAdapter(50, 100, dtmf11Listener));
                        dtmfButtons[i].addActionListener(dtmf11Listener);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + i);
                }

                //buttons[i].setPreferredSize(new Dimension(20, 20));
                dtmfButtons[i].setEnabled(false);
                keypadPanel.add(dtmfButtons[i]);
            }

            //DtmfSoundGenerator.getInstance().start();
        } catch (Exception e1) {
            logger.warn("DtmfPanel.createDtmfPanel.Exception", e1);
            //DtmfSoundGenerator.getInstance().stop();
        }

        return keypadPanel;
    }

    public static JButton[] getDtmfButtons() {
        return dtmfButtons;
    }

    ////////////////////////////////////////////////////////////////////////////////

    private static class MouseCustomAdapter extends MouseAdapter {

        private final Timer timer;
        private final DtmfListener dtmfListener;

        public MouseCustomAdapter(int delay, int initialDelay, DtmfListener dtmfListener) {
            this.dtmfListener = dtmfListener;

            timer = new Timer(delay, dtmfListener);
            timer.setInitialDelay(initialDelay);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            dtmfListener.setFinished(false);
            logger.debug("DtmfListener isFinished: {}", dtmfListener.isFinished());
            timer.start();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            dtmfListener.setFinished(true);
            timer.stop();
            logger.debug("DtmfListener isFinished: {}", dtmfListener.isFinished());
        }
    }

}
