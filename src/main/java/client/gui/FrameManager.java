package client.gui;

import client.VoipClient;
import client.gui.model.ClientFrame;
import client.gui.model.ContactFrame;
import config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AppInstance;
import service.ServiceManager;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

/**
 * @class public class FrameManager extends JFrame
 * @brief FrameManager class
 */
public class FrameManager extends JFrame { // Main frame

    private static final Logger logger = LoggerFactory.getLogger(FrameManager.class);

    private static FrameManager manager;

    private ClientFrame clientFrame;
    private ContactFrame contactFrame;

    ////////////////////////////////////////////////////////////////////////////////

    private FrameManager() {
        super("MAIN_FRAME");

        try {
            //UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            logger.warn("Fail to apply theme.");
        }

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image image = toolkit.getImage("src/main/resources/icon.png");
        this.setIconImage(image);

        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        this.setTitle(configManager.getHostName());

        Dimension dimension = toolkit.getScreenSize();
        setBounds(dimension.width / 2, dimension.height / 2, 750, 600);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        //setResizable(false);
    }

    public static FrameManager getInstance() {
        if (manager == null) {
            manager = new FrameManager();
        }

        return manager;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public void openClientFrame() {
        if(clientFrame == null) {
            clientFrame = new ClientFrame();
            //clientFrame.setVisible(true);
        }
    }

    public void closeClientFrameFrame() {
        if(clientFrame != null) {
            //clientFrame.setVisible(false);
            clientFrame = null;
        }
    }

    public ClientFrame getClientFrame() {
        return clientFrame;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public void openContactFrame() {
        if(contactFrame == null) {
            contactFrame = new ContactFrame();
            //contactFrame.setVisible(true);
        }
    }

    public void closeContactFrameFrame() {
        if(contactFrame != null) {
            //contactFrame.setVisible(false);
            contactFrame = null;
        }
    }

    public ContactFrame getContactFrame() {
        return contactFrame;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public void start () {
        openClientFrame();
        openContactFrame();

        /////////////////////////////////////////////

        getContentPane().add(clientFrame);
        setVisible(true);
        //pack();
    }

    public void stop () {
        closeContactFrameFrame();
        closeClientFrameFrame();
    }

    public void change (String name) {
        getContentPane().removeAll();

        if (name.equals(ServiceManager.CLIENT_FRAME_NAME)) {
            getContentPane().add(clientFrame);
        } else if (name.equals(ServiceManager.CONTACT_FRAME_NAME)) {
            getContentPane().add(contactFrame);
        } else {
            getContentPane().add(clientFrame);
        }

        revalidate();
        repaint();
    }

    ////////////////////////////////////////////////////////////////////////////////

    public void popUpInfoMsgToFrame(String msg) {
        if (clientFrame == null) {
            return;
        }

        clientFrame.popUpInfoMsg(msg);
    }

    public void popUpWarnMsgToFrame(String msg) {
        if (clientFrame == null) {
            return;
        }

        clientFrame.popUpWarnMsg(msg);
    }

    public int popUpErrorMsgToFrame(String msg) {
        if (clientFrame == null) {
            return JOptionPane.CLOSED_OPTION;
        }

        return clientFrame.popUpErrorMsg(msg);
    }

    public void popUpErrorMsg(String msg) {
        JOptionPane pane = new JOptionPane(
                msg,
                JOptionPane.ERROR_MESSAGE
        );

        JDialog d = pane.createDialog(null, "Error");
        d.pack();
        d.setModal(false);
        d.setVisible(true);
        while (pane.getValue() == JOptionPane.UNINITIALIZED_VALUE) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                // ignore
            }
        }
    }

    public void appendTextToFrame(String msg) {
        if (clientFrame == null) {
            return;
        }

        clientFrame.appendText(msg);
    }

    public void selectPriorityCodec(String codecName) {
        if (clientFrame == null) {
            return;
        }

        clientFrame.selectPriorityCodec(codecName);
    }

    public boolean processInviteToFrame(String remoteHostName) {
        if (remoteHostName == null) { return false; }

        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        if (configManager.isUseClient()) {
            if (clientFrame == null) {
                return false;
            }

            clientFrame.inputRemoteTextField(remoteHostName);

            VoipClient voipClient = VoipClient.getInstance();
            voipClient.setRemoteHostName(remoteHostName);
        }

        return true;
    }

    public void setRemoteHostName(String remoteHostName) {
        if (remoteHostName == null) { return; }

        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        if (configManager.isUseClient()) {
            if (clientFrame == null) {
                return;
            }

            clientFrame.inputRemoteTextField(remoteHostName);

            VoipClient voipClient = VoipClient.getInstance();
            voipClient.setRemoteHostName(remoteHostName);
        }
    }

    public void setToSipIp(String toSipIp) {
        if (toSipIp == null) { return; }

        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        if (configManager.isUseClient()) {
            if (clientFrame == null) {
                return;
            }

            clientFrame.setToSipIpTextField(toSipIp);
        }
    }

    public void setToSipPort(String toSipPort) {
        if (toSipPort == null) { return; }

        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        if (configManager.isUseClient()) {
            if (clientFrame == null) {
                return;
            }

            clientFrame.setToSipPortTextField(toSipPort);
        }
    }

    public boolean processAutoInviteToFrame(String remoteHostName) {
        if (remoteHostName == null) { return false; }

        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        if (configManager.isUseClient()) {
            if (clientFrame == null) {
                return false;
            }

            clientFrame.getByeButton().setEnabled(true);
            clientFrame.getCallButton().setEnabled(false);
            clientFrame.getStopButton().setEnabled(false);
            clientFrame.getMikeMuteCheck().setEnabled(true);
            clientFrame.getSpeakerMuteCheck().setEnabled(true);

            clientFrame.inputRemoteTextField(remoteHostName);

            VoipClient voipClient = VoipClient.getInstance();
            voipClient.setRemoteHostName(remoteHostName);

            logger.debug("Call from [{}]", voipClient.getRemoteHostName());
            clientFrame.appendText("Call from [" + voipClient.getRemoteHostName() + "].\n");
        }

        return true;
    }

    public boolean processRegisterToFrame(String mdn) {
        if (mdn == null) { return false; }

        if(clientFrame == null) { return false; }

        clientFrame.appendText("Success to register. (mdn=" + mdn + ")\n");

        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        if (configManager.isUseClient()) {
            clientFrame.getRegiButton().setEnabled(false);
            clientFrame.getCallButton().setEnabled(true);
        }

        return true;
    }

    public boolean processByeToFrame(String name) {
        if (name == null) { return false; }

        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        if (configManager.isUseClient()) {
            if (clientFrame == null) {
                return false;
            }

            clientFrame.getCallButton().setEnabled(true);
            clientFrame.getByeButton().setEnabled(false);
            clientFrame.getStopButton().setEnabled(true);
            clientFrame.getMikeMuteCheck().setEnabled(false);
            clientFrame.getSpeakerMuteCheck().setEnabled(false);
        }

        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public JLabel createLabel(String msg) {
        JLabel label = new JLabel(msg);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        return label;
    }


}
