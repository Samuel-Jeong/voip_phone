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
        wavPanel.setOpaque(false);
        GridBagConstraints wavGB = new GridBagConstraints();
        wavGB.anchor = GridBagConstraints.WEST;
        wavPanel.setLayout(new GridBagLayout());

        JLabel labelWavFile = new JLabel("Uploaded wav file : ");
        wavGB.gridx = 0;
        wavGB.gridy = 0;
        wavGB.ipadx = 10;
        wavGB.ipady = 10;
        wavPanel.add(labelWavFile, wavGB);

        wavGB.gridx = 0;
        wavGB.gridy = 1;
        wavPanel.add(fieldWavFile, wavGB);

        wavGB.gridx = 0;
        wavGB.gridy = 2;
        wavPanel.add(fileUploadButton, wavGB);

        return wavPanel;
    }

}
