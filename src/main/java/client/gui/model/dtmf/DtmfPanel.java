package client.gui.model.dtmf;

import media.dtmf.module.DtmfHandler;
import media.dtmf.base.DtmfTask;
import media.dtmf.base.DtmfUnit;
import media.dtmf.module.DtmfTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

                dtmfButtons[i].setMultiClickThreshhold(100);

                int delay = 20;

                switch (i) {
                    case 0: dtmfButtons[i].addMouseListener(new MouseCustomAdapter(DtmfUnit.DIGIT_1, volume, delay)); break;
                    case 1: dtmfButtons[i].addMouseListener(new MouseCustomAdapter(DtmfUnit.DIGIT_2, volume, delay)); break;
                    case 2: dtmfButtons[i].addMouseListener(new MouseCustomAdapter(DtmfUnit.DIGIT_3, volume, delay)); break;
                    case 3: dtmfButtons[i].addMouseListener(new MouseCustomAdapter(DtmfUnit.DIGIT_4, volume, delay)); break;
                    case 4: dtmfButtons[i].addMouseListener(new MouseCustomAdapter(DtmfUnit.DIGIT_5, volume, delay)); break;
                    case 5: dtmfButtons[i].addMouseListener(new MouseCustomAdapter(DtmfUnit.DIGIT_6, volume, delay)); break;
                    case 6: dtmfButtons[i].addMouseListener(new MouseCustomAdapter(DtmfUnit.DIGIT_7, volume, delay)); break;
                    case 7: dtmfButtons[i].addMouseListener(new MouseCustomAdapter(DtmfUnit.DIGIT_8, volume, delay)); break;
                    case 8: dtmfButtons[i].addMouseListener(new MouseCustomAdapter(DtmfUnit.DIGIT_9, volume, delay)); break;
                    case 9: dtmfButtons[i].addMouseListener(new MouseCustomAdapter(DtmfUnit.DIGIT_10, volume, delay)); break;
                    case 10: dtmfButtons[i].addMouseListener(new MouseCustomAdapter(DtmfUnit.DIGIT_0, volume, delay)); break;
                    case 11: dtmfButtons[i].addMouseListener(new MouseCustomAdapter(DtmfUnit.DIGIT_11, volume, delay)); break;
                    default: throw new IllegalStateException("Unexpected value: " + i);
                }

                //buttons[i].setPreferredSize(new Dimension(20, 20));
                dtmfButtons[i].setEnabled(false);
                keypadPanel.add(dtmfButtons[i]);
            }
        } catch (Exception e) {
            logger.warn("DtmfPanel.createKeypadPanel.Exception", e);
            return null;
        }

        return keypadPanel;
    }

    public static JButton[] getDtmfButtons() {
        return dtmfButtons;
    }

    ////////////////////////////////////////////////////////////////////////////////

    private static class MouseCustomAdapter extends MouseAdapter {

        private final DtmfTaskManager dtmfTaskManager;

        public MouseCustomAdapter(int digit, int volume, int interval) {
            dtmfTaskManager = new DtmfTaskManager(digit, volume, interval);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            dtmfTaskManager.startDtmfTask();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            dtmfTaskManager.stopDtmfTask();
            dtmfTaskManager.handle();
            dtmfTaskManager.clearDtmfTask();
        }
    }

}
