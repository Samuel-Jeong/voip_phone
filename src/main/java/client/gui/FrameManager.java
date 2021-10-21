package client.gui;

import client.VoipClient;
import client.gui.model.ClientFrame;
import config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AppInstance;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @class public class FrameManager
 * @brief FrameManager class
 */
public class FrameManager {

    private static final Logger logger = LoggerFactory.getLogger(FrameManager.class);

    private static FrameManager manager;

    Map<String, ClientFrame> frameMap;

    ////////////////////////////////////////////////////////////////////////////////

    private FrameManager() {
        frameMap = new HashMap<>();
    }

    public static FrameManager getInstance() {
        if (manager == null) {
            manager = new FrameManager();
        }

        return manager;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public void addFrame(String name) {
        if (name == null) { return; }
        ClientFrame ClientFrame = new ClientFrame(name);
        frameMap.put(name, ClientFrame);
    }

    public void openFrame(String name) {
        if (name == null) { return; }
        ClientFrame frame = getFrame(name);
        if(frame != null) {
            frame.setVisible(true);
        }
    }

    public void closeFrame(String name) {
        if (name == null) { return; }
        ClientFrame frame = getFrame(name);
        if(frame != null) {
            frame.setVisible(false);
        }
    }

    public void deleteFrame(String name) {
        if (name == null) { return; }
        frameMap.remove(name);
    }

    public ClientFrame getFrame(String name) {
        if (name == null) { return null; }

        ClientFrame frame = null;
        if(frameMap.containsKey(name)) {
            frame = frameMap.get(name);
        }

        return frame;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public void start (String name) {
        addFrame(name);
        openFrame(name);
    }

    public void stop (String name) {
        closeFrame(name);
        deleteFrame(name);
    }

    ////////////////////////////////////////////////////////////////////////////////

    public void popUpInfoMsgToFrame(String name, String msg) {
        ClientFrame frame = getFrame(name);
        if (frame == null) {
            return;
        }

        frame.popUpInfoMsg(msg);
    }

    public void popUpWarnMsgToFrame(String name, String msg) {
        ClientFrame frame = getFrame(name);
        if (frame == null) {
            return;
        }

        frame.popUpWarnMsg(msg);
    }

    public int popUpErrorMsgToFrame(String name, String msg) {
        ClientFrame frame = getFrame(name);
        if (frame == null) {
            return JOptionPane.CLOSED_OPTION;
        }

        return frame.popUpErrorMsg(msg);
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

    public void appendTextToFrame(String name, String msg) {
        ClientFrame frame = getFrame(name);
        if (frame == null) {
            return;
        }

        frame.appendText(msg);
    }

    public void selectPriorityCodec(String name, String codecName) {
        ClientFrame frame = getFrame(name);
        if (frame == null) {
            return;
        }

        frame.selectPriorityCodec(codecName);
    }

    public boolean processInviteToFrame(String name, String remoteHostName) {
        if (name == null) { return false; }

        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        if (configManager.isUseClient()) {
            ClientFrame frame = getFrame(name);
            if (frame == null) {
                return false;
            }

            frame.inputRemoteTextField(remoteHostName);

            VoipClient voipClient = VoipClient.getInstance();
            voipClient.setRemoteHostName(remoteHostName);
        }

        return true;
    }

    public boolean processAutoInviteToFrame(String name, String remoteHostName) {
        if (name == null) { return false; }

        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        if (configManager.isUseClient()) {
            ClientFrame frame = getFrame(name);
            if (frame == null) {
                return false;
            }

            frame.getByeButton().setEnabled(true);
            frame.getCallButton().setEnabled(false);
            frame.getStopButton().setEnabled(false);
            frame.getMikeMuteCheck().setEnabled(true);
            frame.getSpeakerMuteCheck().setEnabled(true);

            frame.inputRemoteTextField(remoteHostName);

            VoipClient voipClient = VoipClient.getInstance();
            voipClient.setRemoteHostName(remoteHostName);

            logger.debug("Call from [{}]", voipClient.getRemoteHostName());
            frame.appendText("Call from [" + voipClient.getRemoteHostName() + "].\n");
        }

        return true;
    }

    public boolean processRegisterToFrame(String name, String mdn) {
        if (name == null) { return false; }

        ClientFrame frame = getFrame(name);
        if(frame == null) { return false; }

        frame.appendText("Success to register. (mdn=" + mdn + ")\n");

        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        if (configManager.isUseClient()) {
            frame.getRegiButton().setEnabled(false);
            frame.getCallButton().setEnabled(true);
        }

        return true;
    }

    public boolean processByeToFrame(String name) {
        if (name == null) { return false; }

        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        if (configManager.isUseClient()) {
            ClientFrame frame = getFrame(name);
            if (frame == null) {
                return false;
            }

            frame.getCallButton().setEnabled(true);
            frame.getByeButton().setEnabled(false);
            frame.getStopButton().setEnabled(true);
            frame.getMikeMuteCheck().setEnabled(false);
            frame.getSpeakerMuteCheck().setEnabled(false);
        }

        return true;
    }

}
