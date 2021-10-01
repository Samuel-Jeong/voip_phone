package client.gui.model.wav;

import javax.swing.*;
import java.awt.*;

/**
 * @class public class WavPanel
 * @brief WavPanel class
 */
public class WavPanel {

    ////////////////////////////////////////////////////////////////////////////////

    public static JPanel createWavPanel(JButton fileUploadButton, JTextField fieldWavFile) {
        JPanel wavPanel = new JPanel();
        wavPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        wavPanel.setOpaque(false);

        JLabel labelWavFile = new JLabel("Uploaded wav file : ");
        wavPanel.add(labelWavFile);

        wavPanel.add(fieldWavFile);

        wavPanel.add(fileUploadButton);

        return wavPanel;
    }

}
