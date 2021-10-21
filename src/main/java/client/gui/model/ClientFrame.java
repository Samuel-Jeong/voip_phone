package client.gui.model;

import client.VoipClient;
import client.gui.model.dtmf.DtmfPanel;
import client.gui.model.wav.WavPanel;
import config.ConfigManager;
import media.MediaManager;
import media.module.codec.amr.AmrManager;
import media.module.codec.evs.EvsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AppInstance;
import service.ServiceManager;
import signal.SignalManager;
import signal.base.CallInfo;
import signal.module.CallManager;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * @class public class ClientFrame extends JFrame
 * @brief ClientFrame Class
 */
public class ClientFrame extends JFrame {

    private static final Logger logger = LoggerFactory.getLogger(ClientFrame.class);

    private VoipClient voipClient = null;

    //////////////////////////////////////////////////////////////////////
    // Text
    private static final JTextArea logTextArea = new JTextArea(18, 35);
    private final JTextField proxyTextField;
    private final JTextField remoteTextField;
    private final JTextField hostNameTextField = new JTextField(23);
    private final JTextField sipIpTextField = new JTextField(23);
    private final JTextField sipPortTextField = new JTextField(23);
    private final JTextField toSipIpTextField = new JTextField(23);
    private final JTextField toSipPortTextField = new JTextField(23);
    private final JTextField mediaIpTextField = new JTextField(23);
    private final JTextField mediaPortTextField = new JTextField(23);
    private final JTextField recordPathTextField = new JTextField(23);
    private final JTextField fieldWavFile = new JTextField(34);

    //////////////////////////////////////////////////////////////////////
    // Button
    private final JButton callButton;
    private final JButton regiButton;
    private final JButton byeButton;
    private final JButton clearButton;
    private final JButton startButton;
    private final JButton stopButton;
    private final JButton exitButton;
    private JButton optionApplyButton;
    private final JButton fileUploadButton;

    //////////////////////////////////////////////////////////////////////
    // CheckBox
    private JCheckBox useClientCheck;
    private JCheckBox useProxyCheck;
    private JCheckBox proxyModeCheck;
    private JCheckBox callAutoAcceptCheck;
    private JCheckBox rawFileCheck;
    private JCheckBox encFileCheck;
    private JCheckBox decFileCheck;
    private JCheckBox dtmfCheck;
    private JCheckBox sendWavCheck;
    private final JCheckBox mikeMuteCheck;
    private final JCheckBox speakerMuteCheck;

    //////////////////////////////////////////////////////////////////////
    // ComboBox
    private JComboBox audioCodecSelectCombo;

    //////////////////////////////////////////////////////////////////////
    // Slider
    private JSlider speakerSlider;
    private JSlider mikeSlider;

    //////////////////////////////////////////////////////////////////////

    //private AudioSelectFrame audioSelectFrame;

    public ClientFrame(String name) {
        super(name);

        try {
            //UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            logger.warn("Fail to apply theme.");
        }

        //setUndecorated(true);
        setBounds(500, 400, 435, 500);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        /////////////////////////////////////////////

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image image = toolkit.getImage("src/main/resources/icon.png");
        this.setIconImage(image);

        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        this.setTitle(configManager.getHostName());

        /*TrayIcon trayIcon = new TrayIcon(image, "Client - " + userConfig.getHostName());
        trayIcon.setImageAutoSize(true);
        SystemTray systemTray = SystemTray.getSystemTray();
        systemTray.add(trayIcon);*/

        /////////////////////////////////////////////
        // Top Panel
        JPanel mainPanel = new JPanel();
        FlowLayout flowLayout = new FlowLayout();
        flowLayout.setAlignment(FlowLayout.LEFT);
        mainPanel.setLayout(flowLayout);

        proxyTextField = new JTextField(38);
        remoteTextField = new JTextField(38);

        callButton = new JButton("Call");
        callButton.addActionListener(new CallListener());
        callButton.setEnabled(false);

        regiButton = new JButton("Register");
        regiButton.addActionListener(new RegiListener());
        regiButton.setEnabled(false);

        byeButton = new JButton("Bye");
        byeButton.addActionListener(new ByeListener());
        byeButton.setEnabled(false);

        clearButton = new JButton("Clear");
        clearButton.addActionListener(new ClearListener());
        clearButton.setEnabled(false);

        startButton = new JButton("ON");
        startButton.addActionListener(new StartListener());
        startButton.setEnabled(true);

        stopButton = new JButton("OFF");
        stopButton.addActionListener(new StopListener());
        stopButton.setEnabled(false);

        mikeMuteCheck = new JCheckBox("Mike Mute", false);
        mikeMuteCheck.addActionListener(new MuteListener());
        mikeMuteCheck.setEnabled(false);

        speakerMuteCheck = new JCheckBox("Speaker Mute", false);
        speakerMuteCheck.addActionListener(new MuteListener());
        speakerMuteCheck.setEnabled(false);

        mainPanel.add(startButton);
        mainPanel.add(stopButton);
        mainPanel.add(mikeMuteCheck);
        mainPanel.add(speakerMuteCheck);

        proxyTextField.setText("");
        mainPanel.add(proxyTextField);
        mainPanel.add(regiButton);

        remoteTextField.setText("");
        mainPanel.add(remoteTextField);
        mainPanel.add(callButton);
        mainPanel.add(byeButton);
        mainPanel.add(clearButton);

        /////////////////////////////////////////////

        exitButton = new JButton("Exit");
        exitButton.addActionListener(new ExitListener());
        exitButton.setEnabled(true);

        mainPanel.add(exitButton);

        /////////////////////////////////////////////

        mainPanel.setPreferredSize(new Dimension(500, 100));
        this.add(mainPanel, "Center");

        // Bottom Panel
        JPanel logPanel = new JPanel(new BorderLayout());

        logTextArea.setEditable(false);

        JScrollPane jScrollPane = new JScrollPane(logTextArea);
        jScrollPane.createVerticalScrollBar();
        jScrollPane.createHorizontalScrollBar();

        logPanel.add(jScrollPane, "Center");
        mainPanel.add(logPanel, "South");

        /////////////////////////////////////////////
        // Option Panel
        JPanel optionPanel = createOptionPanel();

        /////////////////////////////////////////////
        // Media Panel
        JPanel mediaPanel = createMediaPanel();
        if (configManager.isProxyMode()) {
            speakerSlider.setEnabled(false);
            mikeSlider.setEnabled(false);
        }

        /////////////////////////////////////////////
        // Keypad Panel
        JPanel keypadPanel = DtmfPanel.createKeypadPanel();

        // Wav Panel
        fileUploadButton = new JButton("Upload");
        fileUploadButton.addActionListener(new FileUploadClickListener());
        fileUploadButton.setEnabled(configManager.isSendWav());

        fieldWavFile.setEditable(false);
        JPanel wavPanel = WavPanel.createWavPanel(fileUploadButton, fieldWavFile);

        /////////////////////////////////////////////
        // Tab Panel

        JTabbedPane jTabbedPane = new JTabbedPane();
        add(jTabbedPane);

        jTabbedPane.addTab("phone", mainPanel);
        jTabbedPane.addTab("keypad", keypadPanel);
        jTabbedPane.addTab("wav", wavPanel);
        jTabbedPane.addTab("option", optionPanel);
        jTabbedPane.addTab("media", mediaPanel);

        jTabbedPane.setBackgroundAt(0, Color.GRAY);
        jTabbedPane.setBackgroundAt(1, Color.GRAY);
        jTabbedPane.setBackgroundAt(2, Color.GRAY);
        jTabbedPane.setBackgroundAt(2, Color.GRAY);
        jTabbedPane.setBackgroundAt(4, Color.GRAY);

        proxyTextField.setEnabled(false);
        remoteTextField.setEnabled(false);
    }

    private JPanel createOptionPanel() {
        ConfigManager configManager = AppInstance.getInstance().getConfigManager();

        JPanel optionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        optionPanel.setAlignmentX(LEFT_ALIGNMENT);

        // Option Check Buttons
        useClientCheck = new JCheckBox("UseClient", false);
        useClientCheck.addActionListener(new UseClientCheckListener());

        useProxyCheck = new JCheckBox("UseProxy", false);
        useProxyCheck.addActionListener(new UseClientCheckListener());

        proxyModeCheck = new JCheckBox("ProxyMode", false);
        proxyModeCheck.addActionListener(new UseProxyCheckListener());

        callAutoAcceptCheck = new JCheckBox("Auto-Accept", false);
        if (configManager.isCallAutoAccept()) {
            callAutoAcceptCheck.setSelected(true);
        }

        useProxyCheck.setSelected(configManager.isUseProxy());

        if (configManager.isUseClient()) {
            useClientCheck.setSelected(true);
            proxyModeCheck.setEnabled(false);
        } else if (configManager.isProxyMode()) {
            proxyModeCheck.setSelected(true);
            useClientCheck.setEnabled(false);
            useProxyCheck.setEnabled(false);
            startButton.setEnabled(false);
            stopButton.setEnabled(false);
            callAutoAcceptCheck.setEnabled(false);
        }

        JPanel optionCheckPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        optionCheckPanel.setAlignmentX(LEFT_ALIGNMENT);

        JPanel optionCheckGridPanel = new JPanel(new GridLayout(1, 1));
        optionCheckGridPanel.add(useClientCheck);
        optionCheckGridPanel.add(useProxyCheck);
        optionCheckGridPanel.add(proxyModeCheck);
        optionCheckGridPanel.add(callAutoAcceptCheck);

        optionCheckPanel.add(optionCheckGridPanel);
        optionPanel.add(optionCheckPanel);
        optionPanel.add(Box.createVerticalStrut(5));
        //

        // Option Label Panel
        JPanel optionLabelGridPanel = new JPanel(new GridLayout(8, 1));

        JPanel hostNameLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        hostNameLabelPanel.setAlignmentX(LEFT_ALIGNMENT);
        JLabel hostNameLabel = new JLabel("Host-Name : ");
        hostNameLabelPanel.add(hostNameLabel);
        optionLabelGridPanel.add(hostNameLabelPanel);

        JPanel sipIpLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sipIpLabelPanel.setAlignmentX(LEFT_ALIGNMENT);
        JLabel sipIpLabel = new JLabel("FROM-IP : ");
        sipIpLabelPanel.add(sipIpLabel);
        optionLabelGridPanel.add(sipIpLabelPanel);

        JPanel sipPortLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sipPortLabelPanel.setAlignmentX(LEFT_ALIGNMENT);
        JLabel sipPortLabel = new JLabel("FROM-Port : ");
        sipPortLabelPanel.add(sipPortLabel);
        optionLabelGridPanel.add(sipPortLabelPanel);

        JPanel proxySipIpLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        proxySipIpLabelPanel.setAlignmentX(LEFT_ALIGNMENT);
        JLabel proxySipIpLabel = new JLabel("TO-IP : ");
        proxySipIpLabelPanel.add(proxySipIpLabel);
        optionLabelGridPanel.add(proxySipIpLabelPanel);

        JPanel proxySipPortLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        proxySipPortLabelPanel.setAlignmentX(LEFT_ALIGNMENT);
        JLabel proxySipPortLabel = new JLabel("TO-Port : ");
        proxySipPortLabelPanel.add(proxySipPortLabel);
        optionLabelGridPanel.add(proxySipPortLabelPanel);

        JPanel mediaIpLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mediaIpLabelPanel.setAlignmentX(LEFT_ALIGNMENT);
        JLabel mediaIpLabel = new JLabel("Media-IP : ");
        mediaIpLabelPanel.add(mediaIpLabel);
        optionLabelGridPanel.add(mediaIpLabelPanel);

        JPanel mediaPortLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mediaPortLabelPanel.setAlignmentX(LEFT_ALIGNMENT);
        JLabel mediaPortLabel = new JLabel("Media-Port : ");
        mediaPortLabelPanel.add(mediaPortLabel);
        optionLabelGridPanel.add(mediaPortLabelPanel);

        JPanel recordPathLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        recordPathLabelPanel.setAlignmentX(LEFT_ALIGNMENT);
        JLabel recordPathLabel = new JLabel("Record-Path : ");
        recordPathLabelPanel.add(recordPathLabel);
        optionLabelGridPanel.add(recordPathLabelPanel);

        optionPanel.add(optionLabelGridPanel);
        //

        // Option Text Panel
        JPanel optionTextGridPanel = new JPanel(new GridLayout(8, 1));

        hostNameTextField.setEnabled(true);
        hostNameTextField.setEditable(true);
        hostNameTextField.setText(configManager.getHostName());

        sipIpTextField.setEnabled(true);
        sipIpTextField.setEditable(true);
        sipIpTextField.setText(configManager.getFromIp());

        sipPortTextField.setEnabled(true);
        sipPortTextField.setEditable(true);
        sipPortTextField.setText(String.valueOf(configManager.getFromPort()));

        toSipIpTextField.setEnabled(true);
        toSipIpTextField.setEditable(true);
        toSipIpTextField.setText(configManager.getToIp());

        toSipPortTextField.setEnabled(true);
        toSipPortTextField.setEditable(true);
        toSipPortTextField.setText(String.valueOf(configManager.getToPort()));

        mediaIpTextField.setEnabled(true);
        mediaIpTextField.setEditable(true);
        mediaIpTextField.setText(String.valueOf(configManager.getNettyServerIp()));

        mediaPortTextField.setEnabled(true);
        mediaPortTextField.setEditable(true);
        mediaPortTextField.setText(String.valueOf(configManager.getNettyServerPort()));

        recordPathTextField.setEnabled(true);
        recordPathTextField.setEditable(true);
        recordPathTextField.setText(String.valueOf(configManager.getRecordPath()));

        optionTextGridPanel.add(hostNameTextField);
        optionTextGridPanel.add(sipIpTextField);
        optionTextGridPanel.add(sipPortTextField);
        optionTextGridPanel.add(toSipIpTextField);
        optionTextGridPanel.add(toSipPortTextField);
        optionTextGridPanel.add(mediaIpTextField);
        optionTextGridPanel.add(mediaPortTextField);
        optionTextGridPanel.add(recordPathTextField);

        optionPanel.add(optionTextGridPanel);
        //

        optionPanel.add(Box.createVerticalStrut(5));

        // Apply button
        JPanel optionApplyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        optionApplyPanel.setAlignmentX(LEFT_ALIGNMENT);

        JPanel optionApplyGridPanel = new JPanel(new GridLayout(1, 1));

        optionApplyButton = new JButton("Apply");
        optionApplyButton.addActionListener(new OptionApplyListener());
        optionApplyButton.setEnabled(true);

        optionApplyGridPanel.add(optionApplyButton);
        optionApplyPanel.add(optionApplyGridPanel);
        optionPanel.add(optionApplyPanel);
        //

        return optionPanel;
    }

    private JPanel createMediaPanel() {
        JPanel mediaPanel = new JPanel();
        mediaPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        mediaPanel.setAlignmentX(LEFT_ALIGNMENT);

        // Codec select panel
        JPanel codecSelectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        codecSelectPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel codecDescriptionPanel = new JPanel(new GridLayout(1, 1));

        JPanel codecDesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel codecSelectLabel = new JLabel("Audio Codec : ");
        codecDesPanel.add(codecSelectLabel);
        codecDescriptionPanel.add(codecDesPanel);

        String[] audioCodecStrArray = MediaManager.getInstance().getSupportedAudioCodecList();
        JPanel audioCodecComboPanel = new JPanel(new GridLayout(1, 1));

        audioCodecSelectCombo = new JComboBox(audioCodecStrArray);

        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        if (configManager.getPriorityAudioCodec().equals(AudioFormat.Encoding.ALAW.toString())) {
            audioCodecSelectCombo.setSelectedItem(AudioFormat.Encoding.ALAW);
        } else if (configManager.getPriorityAudioCodec().equals(AudioFormat.Encoding.ULAW.toString())) {
            audioCodecSelectCombo.setSelectedItem(AudioFormat.Encoding.ULAW);
        } else if (configManager.getPriorityAudioCodec().equals(MediaManager.EVS)) {
            audioCodecSelectCombo.setSelectedItem(MediaManager.EVS);
        } else if (configManager.getPriorityAudioCodec().equals(MediaManager.AMR_NB)) {
            audioCodecSelectCombo.setSelectedItem(MediaManager.AMR_NB);
        } else if (configManager.getPriorityAudioCodec().equals(MediaManager.AMR_WB)) {
            audioCodecSelectCombo.setSelectedItem(MediaManager.AMR_WB);
        }

        audioCodecSelectCombo.addActionListener(e -> {
            try {
                JComboBox cb = (JComboBox) e.getSource();
                int index = cb.getSelectedIndex();

                String codecName = audioCodecStrArray[index];
                logger.debug("Selected audio codec name : {}", codecName);

                String curPriorityCodec = configManager.getPriorityAudioCodec();
                if (!curPriorityCodec.equals(codecName)) {
                    logger.debug("Priority audio codec option is changed. (before=[{}], after=[{}])", configManager.getPriorityAudioCodec(), codecName);
                    appendText("Priority audio codec option is changed. ([" + curPriorityCodec + "] > [" + codecName + "])\n");
                    configManager.setPriorityAudioCodec(codecName);
                    configManager.setIniValue(ConfigManager.SECTION_MEDIA, ConfigManager.FIELD_PRIORITY_CODEC, codecName);
                    MediaManager.getInstance().setPriorityCodec(codecName);

                    if (MediaManager.getInstance().getPriorityCodec().equals(MediaManager.EVS)) {
                        EvsManager.getInstance().init();
                    }

                    if (MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_NB)
                            || MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_WB)) {
                        AmrManager.getInstance().init();
                    }
                }
            } catch (Exception exception) {
                logger.warn("ClientFrame.codecSelectPanel.Exception", exception);
            }
        });

        audioCodecComboPanel.add(audioCodecSelectCombo);
        codecSelectPanel.add(audioCodecComboPanel);

        mediaPanel.add(codecDescriptionPanel);
        mediaPanel.add(codecSelectPanel);

        mediaPanel.add(Box.createVerticalStrut(5));
        //

        // Volume slider
        JPanel sliderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sliderPanel.setAlignmentX(LEFT_ALIGNMENT);

        ////////////
        JPanel volumeDescriptionPanel = new JPanel(new GridLayout(2, 1));

        JPanel speakerDesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel speakerLabel = new JLabel("Speaker : ");
        speakerDesPanel.add(speakerLabel);

        JPanel mikeDesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel mikeLabel = new JLabel("Mike : ");
        mikeDesPanel.add(mikeLabel);

        volumeDescriptionPanel.add(speakerDesPanel);
        volumeDescriptionPanel.add(mikeDesPanel);

        for (UIManager.LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(laf.getName())){
                try {
                    UIManager.setLookAndFeel(laf.getClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        JPanel volumePanel = new JPanel(new GridLayout(2,1,20,20));
        volumePanel.setOpaque(false);
        //sliderPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        UIDefaults sliderDefaults = new UIDefaults();

        sliderDefaults.put("Slider.thumbWidth", 20);
        sliderDefaults.put("Slider.thumbHeight", 20);
        sliderDefaults.put("Slider:SliderThumb.backgroundPainter", (Painter<JComponent>) (g, c, w, h) -> {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setStroke(new BasicStroke(2f));
            g.setColor(Color.RED);
            g.fillOval(1, 1, w-3, h-3);
            g.setColor(Color.WHITE);
            g.drawOval(1, 1, w-3, h-3);
        });
        sliderDefaults.put("Slider:SliderTrack.backgroundPainter", (Painter<JComponent>) (g, c, w, h) -> {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setStroke(new BasicStroke(2f));
            g.setColor(Color.GRAY);
            g.fillRoundRect(0, 6, w-1, 8, 8, 8);
            g.setColor(Color.WHITE);
            g.drawRoundRect(0, 6, w-1, 8, 8, 8);
        });

        speakerSlider = new JSlider(0, 100, configManager.getSpeakerVolume());
        volumePanel.add(speakerSlider);
        speakerSlider.putClientProperty("Nimbus.Overrides",sliderDefaults);
        speakerSlider.putClientProperty("Nimbus.Overrides.InheritDefaults",false);
        speakerSlider.addChangeListener(
                e -> {
                    int curSpeakerVolume = speakerSlider.getValue();
                    //logger.debug("Speaker volume is changed. (before=[{}], after=[{}])", configManager.getSpeakerVolume(), curSpeakerVolume);
                    configManager.setSpeakerVolume(curSpeakerVolume);
                    configManager.setIniValue(ConfigManager.SECTION_MEDIA, ConfigManager.FIELD_SPEAKER_VOLUME, String.valueOf(curSpeakerVolume));
                }
        );

        mikeSlider = new JSlider(0, 100, configManager.getMikeVolume());
        volumePanel.add(mikeSlider);
        mikeSlider.putClientProperty("Nimbus.Overrides",sliderDefaults);
        mikeSlider.putClientProperty("Nimbus.Overrides.InheritDefaults",false);
        mikeSlider.addChangeListener(
                e -> {
                    int curMikeVolume = mikeSlider.getValue();
                    //logger.debug("Mike volume is changed. (before=[{}], after=[{}])", configManager.getMikeVolume(), curMikeVolume);
                    configManager.setMikeVolume(curMikeVolume);
                    configManager.setIniValue(ConfigManager.SECTION_MEDIA, ConfigManager.FIELD_MIKE_VOLUME, String.valueOf(curMikeVolume));
                }
        );

        sliderPanel.add(volumeDescriptionPanel);
        sliderPanel.add(volumePanel);
        mediaPanel.add(sliderPanel);
        mediaPanel.add(Box.createVerticalStrut(5));
        //

        // Record CheckBox
        JPanel recordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        recordPanel.setAlignmentX(LEFT_ALIGNMENT);
        recordPanel.setOpaque(false);

        JPanel recordDescriptionPanel = new JPanel(new GridLayout(1, 1));
        recordDescriptionPanel.setOpaque(false);

        JPanel recordDesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        recordDesPanel.setOpaque(false);

        JLabel recordSelectLabel = new JLabel("Record type : ");
        recordDesPanel.add(recordSelectLabel);
        recordDescriptionPanel.add(recordDesPanel);

        JPanel recordFileCheckPanel = new JPanel(new GridLayout(1, 1));
        recordFileCheckPanel.setOpaque(false);

        rawFileCheck = new JCheckBox("PCM", false);
        rawFileCheck.addActionListener(new RawFileCheckListener());
        if (configManager.isRawFile()) {
            rawFileCheck.setSelected(true);
        }

        encFileCheck = new JCheckBox("DEC", false);
        encFileCheck.addActionListener(new EncFileCheckListener());
        if (configManager.isEncFile()) {
            encFileCheck.setSelected(true);
        }

        decFileCheck = new JCheckBox("ENC", false);
        decFileCheck.addActionListener(new DecFileCheckListener());
        if (configManager.isDecFile()) {
            decFileCheck.setSelected(true);
        }

        recordFileCheckPanel.add(rawFileCheck);
        recordFileCheckPanel.add(encFileCheck);
        recordFileCheckPanel.add(decFileCheck);
        recordPanel.add(recordDescriptionPanel);
        recordPanel.add(recordFileCheckPanel);
        mediaPanel.add(recordPanel);
        mediaPanel.add(Box.createVerticalStrut(5));
        //

        // DTMF Checkbox
        JPanel dtmfPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dtmfPanel.setAlignmentX(LEFT_ALIGNMENT);
        dtmfPanel.setOpaque(false);

        JPanel dtmfDescriptionPanel = new JPanel(new GridLayout(1, 1));
        dtmfDescriptionPanel.setOpaque(false);

        JPanel dtmfDesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dtmfDesPanel.setOpaque(false);

        JLabel dtmfSelectLabel = new JLabel("DTMF : ");
        dtmfDesPanel.add(dtmfSelectLabel);
        dtmfDescriptionPanel.add(dtmfDesPanel);

        JPanel dtmfCheckPanel = new JPanel(new GridLayout(1, 1));
        dtmfCheckPanel.setOpaque(false);

        dtmfCheck = new JCheckBox("", false);
        dtmfCheck.addActionListener(new DtmfCheckListener());
        dtmfCheck.setSelected(configManager.isDtmf());

        dtmfCheckPanel.add(dtmfCheck);
        dtmfPanel.add(dtmfDescriptionPanel);
        dtmfPanel.add(dtmfCheckPanel);
        mediaPanel.add(dtmfPanel);
        mediaPanel.add(Box.createVerticalStrut(5));
        //

        // Wav Checkbox
        JPanel wavPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wavPanel.setAlignmentX(LEFT_ALIGNMENT);
        wavPanel.setOpaque(false);

        JPanel wavDescriptionPanel = new JPanel(new GridLayout(1, 1));
        wavDescriptionPanel.setOpaque(false);

        JPanel wavDesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wavDesPanel.setOpaque(false);

        JLabel wavSelectLabel = new JLabel("Send Wav : ");
        wavDesPanel.add(wavSelectLabel);
        wavDescriptionPanel.add(wavDesPanel);

        JPanel wavCheckPanel = new JPanel(new GridLayout(1, 1));
        wavCheckPanel.setOpaque(false);

        sendWavCheck = new JCheckBox("", false);
        sendWavCheck.addActionListener(new SendWavCheckListener());
        if (configManager.isSendWav()) {
            sendWavCheck.setSelected(true);
        }

        wavCheckPanel.add(sendWavCheck);
        wavPanel.add(wavDescriptionPanel);
        wavPanel.add(wavCheckPanel);
        mediaPanel.add(wavPanel);
        mediaPanel.add(Box.createVerticalStrut(5));
        //

        return mediaPanel;
    }

    public void selectPriorityCodec(String codecName) {
        if (audioCodecSelectCombo != null) {
            audioCodecSelectCombo.setSelectedItem(codecName);
        }
    }

    public void inputProxyTextField(String content) {
        proxyTextField.setText(content);
    }

    public String readProxyTextField() {
        return proxyTextField.getText();
    }

    public void inputRemoteTextField(String content) {
        remoteTextField.setText(content);
    }

    public String readRemoteTextField() {
        return remoteTextField.getText();
    }

    public void writeText(String content) {
        logTextArea.setText(content);
    }

    public static void appendText(String content) {
        logTextArea.append(content);
    }

    public void cleanText() {
        logTextArea.setText("");
    }

    public String readText() {
        return logTextArea.getText();
    }

    public String readHostNameTextArea() {
        return hostNameTextField.getText();
    }

    public String readSipIpTextArea() {
        return sipIpTextField.getText();
    }

    public String readSipPortTextArea() {
        return sipPortTextField.getText();
    }

    public String readToSipIpTextArea() {
        return toSipIpTextField.getText();
    }

    public String readToSipPortTextArea() {
        return toSipPortTextField.getText();
    }

    public String readMediaIpTextArea() {
        return mediaIpTextField.getText();
    }

    public String readMediaPortTextArea() {
        return mediaPortTextField.getText();
    }

    public String readRecordPathTextArea() {
        return recordPathTextField.getText();
    }

    public int getSpeakerSliderValue() {
        return speakerSlider.getValue();
    }

    public void setSpeakerSliderValue(int speakerSliderValue) {
        this.speakerSlider.setValue(speakerSliderValue);
    }

    public int getMikeSliderValue() {
        return mikeSlider.getValue();
    }

    public void setMikeSliderValue(int mikeSliderValue) {
        this.mikeSlider.setValue(mikeSliderValue);
    }

    ////////////////////////////////////////////////////////////////////////////////

    public JButton getRegiButton() {
        return regiButton;
    }

    public JButton getCallButton() {
        return callButton;
    }

    public JButton getByeButton() {
        return byeButton;
    }

    public JButton getStopButton() {
        return stopButton;
    }

    public JCheckBox getMikeMuteCheck() {
        return mikeMuteCheck;
    }

    public JCheckBox getSpeakerMuteCheck() {
        return speakerMuteCheck;
    }

    ////////////////////////////////////////////////////////////////////////////////

    private void startAudioFrame () {
        try {
            // Speaker
            AudioFormat speakerFormat = new AudioFormat(
                    MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_WB)? 16000.0f : 8000.0f,
                    16,
                    1,
                    true,
                    true
            );
            SourceDataLine speaker = AudioSystem.getSourceDataLine(speakerFormat);
            logger.debug("> Selected speaker: {}", speaker.getLineInfo().toString());

            VoipClient.getInstance().closeSpeaker();
            VoipClient.getInstance().setSpeaker(speaker);
            //

            // Microphone
            AudioFormat microphoneFormat = new AudioFormat(
                    MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_WB)? 16000.0f : 8000.0f,
                    16,
                    1,
                    true,
                    true
            );

            TargetDataLine microphone = AudioSystem.getTargetDataLine(microphoneFormat);
            logger.debug("> Selected mike: {}", microphone.getLineInfo().toString());

            VoipClient.getInstance().closeMike();
            VoipClient.getInstance().setMike(microphone);
            //
        } catch (Exception e) {
            logger.warn("ClientFrame.startAudioFrame.Exception", e);
        }

        /*if (audioSelectFrame == null) {
            audioSelectFrame = new AudioSelectFrame();
            audioSelectFrame.start();
        }*/
    }

    private void stopAudioFrame () {
        /*if (audioSelectFrame != null) {
            audioSelectFrame.stop();
            audioSelectFrame = null;
        }*/
    }

    ////////////////////////////////////////////////////////////////////////////////

    class UseClientCheckListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == useClientCheck) {
                proxyModeCheck.setEnabled(!useClientCheck.isSelected());
            }
        }
    }

    class UseProxyCheckListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == proxyModeCheck) {
                useClientCheck.setEnabled(!proxyModeCheck.isSelected());
                useProxyCheck.setEnabled(!proxyModeCheck.isSelected());
            }
        }
    }

    class RawFileCheckListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == rawFileCheck) {
                ConfigManager configManager = AppInstance.getInstance().getConfigManager();
                logger.debug("Raw File Record option is changed. (before=[{}], after=[{}])", configManager.isRawFile(), rawFileCheck.isSelected());
                appendText("Raw File Record option is changed. ([" + configManager.isRawFile() + "] > [" + rawFileCheck.isSelected() + "])\n");

                configManager.setRawFile(rawFileCheck.isSelected());
                configManager.setIniValue(ConfigManager.SECTION_RECORD, ConfigManager.FIELD_RAW_FILE, String.valueOf(rawFileCheck.isSelected()));
            }
        }
    }

    class DtmfCheckListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == dtmfCheck) {
                ConfigManager configManager = AppInstance.getInstance().getConfigManager();
                logger.debug("DTMF option is changed. (before=[{}], after=[{}])", configManager.isDtmf(), dtmfCheck.isSelected());
                appendText("DTMF option is changed. ([" + configManager.isDtmf() + "] > [" + dtmfCheck.isSelected() + "])\n");

                configManager.setDtmf(dtmfCheck.isSelected());
                configManager.setIniValue(ConfigManager.SECTION_MEDIA, ConfigManager.FIELD_DTMF, String.valueOf(dtmfCheck.isSelected()));

                JButton[] dtmfButtons = DtmfPanel.getDtmfButtons();
                for (JButton dtmfButton : dtmfButtons) {
                    dtmfButton.setEnabled(dtmfCheck.isSelected());
                }
            }
        }
    }

    class SendWavCheckListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == sendWavCheck) {
                ConfigManager configManager = AppInstance.getInstance().getConfigManager();
                logger.debug("Send Wav option is changed. (before=[{}], after=[{}])", configManager.isSendWav(), sendWavCheck.isSelected());
                appendText("Send Wav option is changed. ([" + configManager.isSendWav() + "] > [" + sendWavCheck.isSelected() + "])\n");

                fileUploadButton.setEnabled(sendWavCheck.isSelected());
                configManager.setSendWav(sendWavCheck.isSelected());
                configManager.setIniValue(ConfigManager.SECTION_MEDIA, ConfigManager.FIELD_SEND_WAV, String.valueOf(sendWavCheck.isSelected()));
            }
        }
    }

    class EncFileCheckListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == encFileCheck) {
                ConfigManager configManager = AppInstance.getInstance().getConfigManager();
                logger.debug("Encoded File Record option is changed. (before=[{}], after=[{}])", configManager.isEncFile(), encFileCheck.isSelected());
                appendText("Encoded File Record option is changed. ([" + configManager.isEncFile() + "] > [" + encFileCheck.isSelected() + "])\n");

                configManager.setEncFile(encFileCheck.isSelected());
                configManager.setIniValue(ConfigManager.SECTION_RECORD, ConfigManager.FIELD_ENC_FILE, String.valueOf(encFileCheck.isSelected()));
            }
        }
    }

    class DecFileCheckListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == decFileCheck) {
                ConfigManager configManager = AppInstance.getInstance().getConfigManager();
                logger.debug("Decoded File Record option is changed. (before=[{}], after=[{}])", configManager.isDecFile(), decFileCheck.isSelected());
                appendText("Decoded File Record option is changed. ([" + configManager.isDecFile() + "] > [" + decFileCheck.isSelected() + "])\n");

                configManager.setDecFile(decFileCheck.isSelected());
                configManager.setIniValue(ConfigManager.SECTION_RECORD, ConfigManager.FIELD_DEC_FILE, String.valueOf(decFileCheck.isSelected()));
            }
        }
    }

    class FileUploadClickListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == fileUploadButton) {
                ConfigManager configManager = AppInstance.getInstance().getConfigManager();
                if (!configManager.isSendWav()) {
                    return;
                }

                try {
                    JFileChooser jFileChooser;
                    if (configManager.getLastWavPath() != null) {
                        File lastWaveFile = new File(configManager.getLastWavPath());
                        if (lastWaveFile.exists()) {
                            jFileChooser = new JFileChooser(configManager.getLastWavPath());
                        } else {
                            jFileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                        }
                    } else {
                        jFileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                    }

                    FileNameExtensionFilter fileNameExtensionFilter = new FileNameExtensionFilter(
                            "*.wav",
                            "wav"
                    );
                    jFileChooser.setFileFilter(fileNameExtensionFilter);

                    int returnValue = jFileChooser.showOpenDialog(null);
                    // int returnValue = jfc.showSaveDialog(null);
                    if (returnValue == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = jFileChooser.getSelectedFile();

                        if (selectedFile != null && selectedFile.length() > 0 &&
                                selectedFile.getAbsolutePath().endsWith("wav")) {
                            String absolutePath = selectedFile.getAbsolutePath();

                            logger.debug("Success to upload the wav file. (path=[{}])",
                                    absolutePath
                            );
                            appendText(
                                    "Success to upload the wav file. (path=["
                                            + absolutePath
                                            + "])\n"
                            );
                            popUpInfoMsg(
                                    "Success to upload the wav file. (path=["
                                            + absolutePath
                                            + "])"
                            );

                            configManager.setLastWavPath(selectedFile.getPath());
                            configManager.setIniValue(ConfigManager.SECTION_MEDIA, ConfigManager.FIELD_LAST_WAV_PATH, selectedFile.getPath());
                            fieldWavFile.setText(absolutePath);

                            //
                            VoipClient.getInstance().setWavFilePath(
                                    absolutePath
                            );
                            //
                        } else {
                            logger.debug("Fail to upload the wav file.");
                            appendText("Fail to upload the wav file.\n");
                            popUpErrorMsg("Fail to upload the wav file.");
                        }
                    }
                } catch (Exception e1) {
                    logger.warn("WavPanel.FileUploadClickListener.actionPerformed.Exception", e1);
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

    class CallListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            ConfigManager configManager = AppInstance.getInstance().getConfigManager();
            if (!configManager.isUseClient()) {
                logger.warn("This program is not client mode. Fail to call.");
                appendText("This program is not client mode. Fail to call.\n");
                return;
            }

            /*if (VoipClient.getInstance().getMike() == null || VoipClient.getInstance().getSpeaker() == null) {
                logger.warn("Mike or speaker is not initiated. Fail to call.");
                appendText("Mike or speaker is not initiated. Fail to call.\n");
                return;
            }*/

            if(e.getSource() == callButton) {
                String inputStr = String.valueOf(readRemoteTextField());
                if(inputStr == null || inputStr.length() == 0) {
                    logger.warn("Remote host name is null. Fail to call.");
                    appendText("Remote host name is null. Fail to call.\n");
                    popUpWarnMsg("Remote host name is null. Fail to call.");
                    return;
                }
                voipClient.setRemoteHostName(inputStr.trim());

                CallInfo callInfo = CallManager.getInstance().findCallInfoByFromMdn(voipClient.getRemoteHostName());
                if (callInfo == null) { // 이전에 수신한 INVITE 가 없으면, 지정한 remote host 로 INVITE 송신
                    SignalManager.getInstance().sendInvite(
                            configManager.getHostName(),
                            voipClient.getRemoteHostName(),
                            SignalManager.getInstance().getToIp(),
                            SignalManager.getInstance().getToPort()
                    );
                    logger.debug("Call to [{}]", voipClient.getRemoteHostName());
                    appendText("Call to [" + voipClient.getRemoteHostName() + "].\n");
                } else { // 이미 수신한 INVITE 가 있으면, 지정한 remote host 로 INVITE 200 OK 송신
                    if (callInfo.getIsCallRecv() && !configManager.isCallAutoAccept()) {
                        SignalManager.getInstance().sendInviteOk(callInfo.getCallId());
                    }

                    logger.debug("Call from [{}]", voipClient.getRemoteHostName());
                    appendText("Call from [" + voipClient.getRemoteHostName() + "].\n");
                    popUpInfoMsg("Call from [" + voipClient.getRemoteHostName() + "].");
                }

                byeButton.setEnabled(true);
                callButton.setEnabled(false);
                stopButton.setEnabled(false);
                mikeMuteCheck.setEnabled(true);
                speakerMuteCheck.setEnabled(true);
            }
        }
    }

    class RegiListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == regiButton) {
                /*if (VoipClient.getInstance().getMike() == null || VoipClient.getInstance().getSpeaker() == null) {
                    logger.warn("Mike or speaker is not initiated. Fail to register.");
                    appendText("Mike or speaker is not initiated. Fail to register.\n");
                    return;
                }*/

                String inputStr = String.valueOf(readProxyTextField());
                if(inputStr == null || inputStr.length() == 0) {
                    logger.warn("Proxy host name is null. Fail to register.");
                    appendText("Proxy host name is null. Fail to register.\n");
                    return;
                }

                voipClient.setProxyHostName(inputStr.trim());
                SignalManager.getInstance().sendRegister(false, null);

                logger.debug("Register to [{}]", voipClient.getProxyHostName());
                appendText("Register to [" + voipClient.getProxyHostName() + "].\n");
            }
        }
    }

    class ByeListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == byeButton) {
                CallInfo callInfo = CallManager.getInstance().findCallInfoByToMdn(voipClient.getRemoteHostName());
                if (callInfo == null) {
                    callInfo = CallManager.getInstance().findCallInfoByFromMdn(voipClient.getRemoteHostName());
                    if (callInfo == null) {
                        logger.warn("Fail to find the call. [{}]", voipClient.getRemoteHostName());
                        appendText("Fail to find the call [" + voipClient.getRemoteHostName() + "].\n");
                    }
                }

                ConfigManager configManager = AppInstance.getInstance().getConfigManager();
                String proxyIp = configManager.getToIp();
                int proxyPort = configManager.getToPort();

                if (callInfo != null) {
                    if (callInfo.getIsInviteAccepted()) {
                        SignalManager.getInstance().sendBye(callInfo.getCallId(), voipClient.getRemoteHostName(), proxyIp, proxyPort);
                        logger.debug("Bye to [{}]", voipClient.getRemoteHostName());
                        appendText("Bye to [" + voipClient.getRemoteHostName() + "].\n");
                    } else {
                        SignalManager.getInstance().sendCancel(callInfo.getCallId(), voipClient.getRemoteHostName(), proxyIp, proxyPort);
                        logger.debug("Cancel to [{}]", voipClient.getRemoteHostName());
                        appendText("Cancel to [" + voipClient.getRemoteHostName() + "].\n");
                    }
                }

                callButton.setEnabled(true);
                byeButton.setEnabled(false);
                stopButton.setEnabled(true);
                mikeMuteCheck.setEnabled(false);
                speakerMuteCheck.setEnabled(false);
            }
        }
    }

    class ClearListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == clearButton) {
                cleanText();
            }
        }
    }

    class StartListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == startButton) {
                if (!useClientCheck.isSelected() && !proxyModeCheck.isSelected()) {
                    appendText("! Fail to start. Type is not selected.\n");
                    return;
                }

                if (MediaManager.getInstance().getPriorityCodec() == null) {
                    appendText("! Fail to start. Priority codec is not selected.\n");
                    return;
                }

                voipClient = VoipClient.getInstance();
                voipClient.init();

                startAudioFrame();

                useClientCheck.setEnabled(false);
                useProxyCheck.setEnabled(false);
                proxyModeCheck.setEnabled(false);
                callAutoAcceptCheck.setEnabled(false);

                optionApplyButton.setEnabled(false);
                clearButton.setEnabled(true);
                startButton.setEnabled(false);
                stopButton.setEnabled(true);

                callButton.setEnabled(!useProxyCheck.isSelected());
                regiButton.setEnabled(useProxyCheck.isSelected());

                proxyTextField.setEnabled(useProxyCheck.isSelected());
                remoteTextField.setEnabled(true);

                hostNameTextField.setEnabled(false);
                sipIpTextField.setEnabled(false);
                sipPortTextField.setEnabled(false);
                toSipIpTextField.setEnabled(false);
                toSipPortTextField.setEnabled(false);
                mediaIpTextField.setEnabled(false);
                mediaPortTextField.setEnabled(false);
                recordPathTextField.setEnabled(false);

                audioCodecSelectCombo.setEnabled(false);
                rawFileCheck.setEnabled(false);
                encFileCheck.setEnabled(false);
                decFileCheck.setEnabled(false);

                dtmfCheck.setEnabled(false);
                if (dtmfCheck.isSelected()) {
                    JButton[] dtmfButtons = DtmfPanel.getDtmfButtons();
                    for (JButton dtmfButton : dtmfButtons) {
                        dtmfButton.setEnabled(true);
                    }
                }
                sendWavCheck.setEnabled(false);

                fileUploadButton.setEnabled(false);

                appendText("> Phone is on.\n");
            }
        }
    }

    class StopListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == stopButton) {
                if (voipClient != null) {
                    voipClient.stop();
                }

                //stopAudioFrame();

                ConfigManager configManager = AppInstance.getInstance().getConfigManager();

                if (configManager.isUseClient()) {
                    callAutoAcceptCheck.setEnabled(true);
                    useClientCheck.setEnabled(true);
                } else if (configManager.isProxyMode()) {
                    callAutoAcceptCheck.setEnabled(false);
                    proxyModeCheck.setEnabled(true);
                }

                useProxyCheck.setEnabled(true);
                optionApplyButton.setEnabled(true);
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                regiButton.setEnabled(false);
                callButton.setEnabled(false);
                byeButton.setEnabled(false);
                clearButton.setEnabled(false);

                proxyTextField.setEnabled(false);
                remoteTextField.setEnabled(false);

                hostNameTextField.setEnabled(true);
                sipIpTextField.setEnabled(true);
                sipPortTextField.setEnabled(true);
                toSipIpTextField.setEnabled(true);
                toSipPortTextField.setEnabled(true);
                mediaIpTextField.setEnabled(true);
                mediaPortTextField.setEnabled(true);
                recordPathTextField.setEnabled(true);

                audioCodecSelectCombo.setEnabled(true);
                rawFileCheck.setEnabled(true);
                encFileCheck.setEnabled(true);
                decFileCheck.setEnabled(true);

                dtmfCheck.setEnabled(true);
                JButton[] dtmfButtons = DtmfPanel.getDtmfButtons();
                for (JButton dtmfButton : dtmfButtons) {
                    dtmfButton.setEnabled(false);
                }
                sendWavCheck.setEnabled(true);

                fileUploadButton.setEnabled(configManager.isSendWav());

                appendText("> Phone is off.\n");
            }
        }
    }

    class MuteListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // Mike Mute > UdpSender
            if(e.getSource() == mikeMuteCheck) {
                if (mikeMuteCheck.isSelected()) {
                    logger.debug("Mike mute on");
                    appendText("Mike mute on\n");

                    VoipClient.getInstance().muteMikeOn();
                } else {
                    logger.debug("Mike mute off");
                    appendText("Mike mute off\n");

                    VoipClient.getInstance().muteMikeOff();
                }
            }
            // Speaker Mute > UdpReceiver
            else if (e.getSource() == speakerMuteCheck) {
                if (speakerMuteCheck.isSelected()) {
                    logger.debug("Speaker mute on");
                    appendText("Speaker mute on\n");

                    VoipClient.getInstance().muteSpeakerOn();
                } else {
                    logger.debug("Speaker mute off");
                    appendText("Speaker mute off\n");

                    VoipClient.getInstance().muteSpeakerOff();
                }
            }
        }
    }

    class ExitListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == exitButton) {
                if (voipClient != null && voipClient.isStarted()) {
                    voipClient.stop();
                }

                ServiceManager.getInstance().stop();
                System.exit(1);
            }
        }
    }

    class OptionApplyListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == optionApplyButton) {
                ConfigManager configManager = AppInstance.getInstance().getConfigManager();

                ///////////////////////////////////////////////////////////////////////////
                if (useClientCheck.isSelected() != configManager.isUseClient()) {
                    logger.debug("UseClient option is changed. (before=[{}], after=[{}])", configManager.isUseClient(), useClientCheck.isSelected());
                    appendText("UseClient option is changed. ([" + configManager.isUseClient() + "] > [" + useClientCheck.isSelected() + "])\n");

                    configManager.setUseClient(useClientCheck.isSelected());
                    configManager.setIniValue(ConfigManager.SECTION_COMMON, ConfigManager.FIELD_USE_CLIENT, String.valueOf(useClientCheck.isSelected()));

                    if (useClientCheck.isSelected()) {
                        callAutoAcceptCheck.setEnabled(true);
                        startButton.setEnabled(true);
                        speakerSlider.setEnabled(true);
                        mikeSlider.setEnabled(true);
                    }
                }

                if (useProxyCheck.isSelected() != configManager.isUseProxy()) {
                    logger.debug("UseProxy option is changed. (before=[{}], after=[{}])", configManager.isUseProxy(), useProxyCheck.isSelected());
                    appendText("UseProxy option is changed. ([" + configManager.isUseProxy() + "] > [" + useProxyCheck.isSelected() + "])\n");

                    configManager.setUseProxy(useProxyCheck.isSelected());
                    configManager.setIniValue(ConfigManager.SECTION_COMMON, ConfigManager.FIELD_USE_PROXY, String.valueOf(useProxyCheck.isSelected()));
                }

                if (proxyModeCheck.isSelected() != configManager.isProxyMode()) {
                    logger.debug("UseProxy option is changed. (before=[{}], after=[{}])", configManager.isProxyMode(), proxyModeCheck.isSelected());
                    appendText("UseProxy option is changed. ([" + configManager.isProxyMode() + "] > [" + proxyModeCheck.isSelected() + "])\n");

                    configManager.setProxyMode(proxyModeCheck.isSelected());
                    configManager.setIniValue(ConfigManager.SECTION_SIGNAL, ConfigManager.FIELD_PROXY_MODE, String.valueOf(proxyModeCheck.isSelected()));

                    if (proxyModeCheck.isSelected()) {
                        callAutoAcceptCheck.setEnabled(false);
                        startButton.setEnabled(false);
                        stopButton.setEnabled(false);
                        speakerSlider.setEnabled(false);
                        mikeSlider.setEnabled(false);
                    }
                }

                if (callAutoAcceptCheck.isSelected() != configManager.isCallAutoAccept()) {
                    logger.debug("CallAutoAccept option is changed. (before=[{}], after=[{}])", configManager.isCallAutoAccept(), callAutoAcceptCheck.isSelected());
                    appendText("CallAutoAccept option is changed. ([" + configManager.isCallAutoAccept() + "] > [" + callAutoAcceptCheck.isSelected() + "])\n");

                    configManager.setCallAutoAccept(callAutoAcceptCheck.isSelected());
                    configManager.setIniValue(ConfigManager.SECTION_COMMON, ConfigManager.FIELD_CALL_AUTO_ACCEPT, String.valueOf(callAutoAcceptCheck.isSelected()));
                }

                if (!useClientCheck.isSelected() && !proxyModeCheck.isSelected()) {
                    startButton.setEnabled(false);
                    stopButton.setEnabled(false);
                }

                ///////////////////////////////////////////////////////////////////////////
                String inputStr = String.valueOf(readHostNameTextArea());
                if(inputStr == null || inputStr.length() == 0) {
                    logger.warn("Host-name option is null. Fail to set the option.");
                    appendText("Host-name option is null. Fail to set the option.\n");
                    return;
                }

                inputStr = inputStr.trim();
                logger.debug("|Host-name: {}", inputStr);
                if (!configManager.getHostName().equals(inputStr)) {
                    logger.debug("Host-name option is changed. (before=[{}], after=[{}])", configManager.getHostName(), inputStr);
                    appendText("Host-name option is changed. ([" + configManager.getHostName() + "] > [" + inputStr + "])\n");
                    configManager.setHostName(inputStr);
                    configManager.setIniValue(ConfigManager.SECTION_SIGNAL, ConfigManager.FIELD_HOST_NAME, inputStr);
                    setTitle("Client - " + configManager.getHostName());
                }

                ///////////////////////////////////////////////////////////////////////////
                inputStr = String.valueOf(readSipIpTextArea());
                if(inputStr == null || inputStr.length() == 0) {
                    logger.warn("FROM-IP option is null. Fail to set the option.");
                    appendText("FROM-IP option is null. Fail to set the option.\n");
                    return;
                }

                inputStr = inputStr.trim();
                logger.debug("|FROM-IP: {}", inputStr);
                if (!configManager.getFromIp().equals(inputStr)) {
                    logger.debug("FROM-IP option is changed. (before=[{}], after=[{}])", configManager.getFromIp(), inputStr);
                    appendText("FROM-IP option is changed. ([" + configManager.getFromIp() + "] > [" + inputStr + "])\n");
                    configManager.setFromIp(inputStr);
                    configManager.setIniValue(ConfigManager.SECTION_SIGNAL, ConfigManager.FIELD_FROM_IP, inputStr);
                }

                ///////////////////////////////////////////////////////////////////////////
                inputStr = String.valueOf(readSipPortTextArea());
                if(inputStr == null || inputStr.length() == 0) {
                    logger.warn("FROM-Port option is null. Fail to set the option.");
                    appendText("FROM-Port option is null. Fail to set the option.\n");
                    return;
                }

                inputStr = inputStr.trim();
                logger.debug("|FROM-Port: {}", inputStr);
                if (configManager.getFromPort() != Integer.parseInt(inputStr)) {
                    logger.debug("FROM-Port option is changed. (before=[{}], after=[{}])", configManager.getFromPort(), inputStr);
                    appendText("FROM-Port option is changed. ([" + configManager.getFromPort() + "] > [" + inputStr + "])\n");
                    configManager.setFromPort(Integer.parseInt(inputStr));
                    configManager.setIniValue(ConfigManager.SECTION_SIGNAL, ConfigManager.FIELD_FROM_PORT, inputStr);
                }

                ///////////////////////////////////////////////////////////////////////////
                inputStr = String.valueOf(readToSipIpTextArea());
                if(inputStr == null || inputStr.length() == 0) {
                    logger.warn("TO-IP option is null. Fail to set the option.");
                    appendText("TO-IP option is null. Fail to set the option.\n");
                    return;
                }

                inputStr = inputStr.trim();
                logger.debug("|TO-IP: {}", inputStr);
                if (!configManager.getToIp().equals(inputStr)) {
                    logger.debug("TO-IP option is changed. (before=[{}], after=[{}])", configManager.getToIp(), inputStr);
                    appendText("TO-IP option is changed. ([" + configManager.getToIp() + "] > [" + inputStr + "])\n");
                    configManager.setToIp(inputStr);
                    configManager.setIniValue(ConfigManager.SECTION_SIGNAL, ConfigManager.FIELD_TO_IP, inputStr);
                }

                ///////////////////////////////////////////////////////////////////////////
                inputStr = String.valueOf(readToSipPortTextArea());
                if(inputStr == null || inputStr.length() == 0) {
                    logger.warn("TO-Port option is null. Fail to set the option.");
                    appendText("TO-Port option is null. Fail to set the option.\n");
                    return;
                }

                inputStr = inputStr.trim();
                logger.debug("|TO-Port: {}", inputStr);
                if (configManager.getToPort() != Integer.parseInt(inputStr)) {
                    logger.debug("TO-Port option is changed. (before=[{}], after=[{}])", configManager.getToPort(), inputStr);
                    appendText("TO-Port option is changed. ([" + configManager.getToPort() + "] > [" + inputStr + "])\n");
                    configManager.setToPort(Integer.parseInt(inputStr));
                    configManager.setIniValue(ConfigManager.SECTION_SIGNAL, ConfigManager.FIELD_TO_PORT, inputStr);
                }

                ///////////////////////////////////////////////////////////////////////////
                inputStr = String.valueOf(readMediaIpTextArea());
                if(inputStr == null || inputStr.length() == 0) {
                    logger.warn("Media-IP option is null. Fail to set the option.");
                    appendText("Media-IP option is null. Fail to set the option.\n");
                    return;
                }

                inputStr = inputStr.trim();
                logger.debug("|Media-IP: {}", inputStr);
                if (!configManager.getNettyServerIp().equals(inputStr)) {
                    logger.debug("Media-IP option is changed. (before=[{}], after=[{}])", configManager.getNettyServerIp(), inputStr);
                    appendText("Media-IP option is changed. ([" + configManager.getNettyServerIp() + "] > [" + inputStr + "])\n");
                    configManager.setNettyServerIp(inputStr);
                    configManager.setIniValue(ConfigManager.SECTION_MEDIA, ConfigManager.FIELD_NETTY_SERVER_IP, inputStr);
                }

                ///////////////////////////////////////////////////////////////////////////
                inputStr = String.valueOf(readMediaPortTextArea());
                if(inputStr == null || inputStr.length() == 0) {
                    logger.warn("Media-Port option is null. Fail to set the option.");
                    appendText("Media-Port option is null. Fail to set the option.\n");
                    return;
                }

                inputStr = inputStr.trim();
                logger.debug("|Media-Port: {}", inputStr);
                if (configManager.getNettyServerPort() != Integer.parseInt(inputStr)) {
                    logger.debug("Media-Port option is changed. (before=[{}], after=[{}])", configManager.getNettyServerPort(), inputStr);
                    appendText("Media-Port option is changed. ([" + configManager.getNettyServerPort() + "] > [" + inputStr + "])\n");
                    configManager.setNettyServerPort(Integer.parseInt(inputStr));
                    configManager.setIniValue(ConfigManager.SECTION_MEDIA, ConfigManager.FIELD_NETTY_SERVER_PORT, inputStr);
                }

                ///////////////////////////////////////////////////////////////////////////
                inputStr = String.valueOf(readRecordPathTextArea());
                if(inputStr == null || inputStr.length() == 0) {
                    logger.warn("Record-Path option is null. Fail to set the option.");
                    appendText("Record-Path option is null. Fail to set the option.\n");
                    return;
                }

                inputStr = inputStr.trim();
                logger.debug("|Record-Path: {}", inputStr);
                if (!configManager.getRecordPath().equals(inputStr)) {
                    logger.debug("Record-Path is changed. (before=[{}], after=[{}])", configManager.getRecordPath(), inputStr);
                    appendText("Record-Path is changed. ([" + configManager.getRecordPath() + "] > [" + inputStr + "])\n");
                    configManager.setRecordPath(inputStr);
                    configManager.setIniValue(ConfigManager.SECTION_RECORD, ConfigManager.FIELD_RECORD_PATH, inputStr);
                }

                ///////////////////////////////////////////////////////////////////////////
                SignalManager.getInstance().init();

                logger.warn("Success to apply the option.");
                appendText("Success to apply the option.\n");
                popUpInfoMsg("Success to apply the option.");
            }
        }
    }

    public void popUpInfoMsg(String msg) {
        JOptionPane.showMessageDialog(
                new JFrame(),
                msg,
                "Info",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public void popUpWarnMsg(String msg) {
        JOptionPane.showMessageDialog(
                new JFrame(),
                msg,
                "Warn",
                JOptionPane.WARNING_MESSAGE
        );
    }

    public int popUpErrorMsg(String msg) {
        return JOptionPane.showConfirmDialog(
                new JFrame(),
                msg,
                "Error",
                JOptionPane.YES_OPTION,
                JOptionPane.ERROR_MESSAGE
        );
    }

}
