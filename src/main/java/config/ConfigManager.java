package config;

import client.gui.FrameManager;
import media.MediaManager;
import org.ini4j.Ini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.ServiceManager;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * @class public class UserConfig
 * @brief UserConfig Class
 */
public class ConfigManager {

    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);

    private final String configPath;
    private Ini ini = null;

    // Section String
    public static final String SECTION_COMMON = "COMMON"; // COMMON Section 이름
    public static final String SECTION_SIGNAL = "SIGNAL"; // SIGNAL Section 이름
    public static final String SECTION_MEDIA = "MEDIA"; // MEDIA Section 이름
    public static final String SECTION_RECORD = "RECORD"; // RECORD Section 이름

    // Field String
    public static final String FIELD_UDP_RCV_BUFFER_SIZE = "UDP_RCV_BUFFER_SIZE";
    public static final String FIELD_UDP_SND_BUFFER_SIZE = "UDP_SND_BUFFER_SIZE";
    public static final String FIELD_LONG_CALL_TIME = "LONG_CALL_TIME";
    public static final String FIELD_USE_CLIENT = "USE_CLIENT";
    public static final String FIELD_USE_PROXY = "USE_PROXY";
    public static final String FIELD_PROXY_MODE = "PROXY_MODE";
    public static final String FIELD_CALL_AUTO_ACCEPT = "CALL_AUTO_ACCEPT";

    public static final String FIELD_HOST_NAME = "HOST_NAME";
    public static final String FIELD_FROM_IP = "FROM_IP";
    public static final String FIELD_FROM_PORT = "FROM_PORT";
    public static final String FIELD_TO_IP = "TO_IP";
    public static final String FIELD_TO_PORT = "TO_PORT";
    public static final String FIELD_DEFAULT_REGISTER_EXPIRES = "DEFAULT_REGISTER_EXPIRES";
    public static final String FIELD_CALL_RECV_DURATION = "CALL_RECV_DURATION";

    public static final String FIELD_SEND_WAV = "SEND_WAV";
    public static final String FIELD_LAST_WAV_PATH = "LAST_WAV_PATH";
    public static final String FIELD_DTMF = "DTMF";
    public static final String FIELD_PRIORITY_CODEC = "PRIORITY_CODEC";
    public static final String FIELD_NETTY_SERVER_CONSUMER_COUNT = "NETTY_SERVER_CONSUMER_COUNT";
    public static final String FIELD_NETTY_SERVER_IP = "NETTY_SERVER_IP";
    public static final String FIELD_NETTY_SERVER_PORT = "NETTY_SERVER_PORT";
    public static final String FIELD_SPEAKER_VOLUME = "SPEAKER_VOLUME";
    public static final String FIELD_MIKE_VOLUME = "MIKE_VOLUME";
    public static final String FIELD_RELAY = "RELAY";

    public static final String FIELD_RECORD_PATH = "PATH";
    public static final String FIELD_RAW_FILE = "RAW_FILE";
    public static final String FIELD_ENC_FILE = "ENC_FILE";
    public static final String FIELD_DEC_FILE = "DEC_FILE";

    // COMMON
    private int udpRcvBufferSize = 0; // UDP Recv Buffer 크기
    private int udpSndBufferSize = 0; // UDP Send Buffer 크기
    private long longCallTime = 0; // Long Call 삭제 시간
    private boolean useClient = false; // Client 사용 여부
    private boolean useProxy = false; // Client 가 Proxy 사용 여부
    private boolean isProxyMode = false; // SIP Proxy 여부 (현재 프로세스를 Proxy 로 사용할지 여부)
    private boolean callAutoAccept = false; // 호 자동 수락 여부

    // SIGNAL
    private String hostName = null; // SIP Host 이름
    private String fromIp = null; // SIP Host IP
    private int fromPort = 0; // SIP Host Port
    private String toIp = null; // SIP Remote IP
    private int toPort = 0; // SIP Remote Port
    private int defaultRegisterExpires = 0; // Proxy 인 경우 default 사용자 등록 시간
    private int callRecvDuration = 0; // 호 시작 시 상대방이 전화 받을 때까지 대기하는 시간 > 설정된 시간이 지나면 자동으로 Cancel 보낸다.

    // MEDIA
    private boolean sendWav = false;
    private String lastWavPath = null;
    private boolean dtmf = false;
    private String priorityAudioCodec;
    private int nettyServerConsumerCount = 0; // Netty Server 당 Consumer 개수
    private String nettyServerIp; // Netty Server IP List
    private int nettyServerPort; // Netty Server Port List
    private int speakerVolume; // Speaker Volume
    private int mikeVolume; // Mike Volume
    private boolean relay; // 미디어 릴레이 여부

    // RECORD
    private String recordPath = null; // 녹취 파일 저장 경로
    private boolean rawFile = false; // Raw File 녹취 여부
    private boolean encFile = false; // Encoded File 녹취 여부
    private boolean decFile = false; // Decoded File 녹취 여부

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @fn public AuditConfig(String configPath)
     * @brief AuditConfig 생성자 함수
     * @param configPath Config 파일 경로 이름
     */
    public ConfigManager(String configPath) {
        this.configPath = configPath;
        logger.debug("Config path: {}", configPath);
    }

    ////////////////////////////////////////////////////////////////////////////////

    public boolean load() {
        File iniFile = new File(configPath);
        if (!iniFile.isFile() || !iniFile.exists()) {
            logger.warn("Not found the config path. (path={})", configPath);
            return false;
        }

        try {
            this.ini = new Ini(iniFile);

            loadCommonConfig();
            loadSignalConfig();
            loadMediaConfig();
            loadRecordConfig();

            logger.info("Load config [{}]", configPath);
        } catch (IOException e) {
            logger.error("ConfigManager.IOException", e);
            return false;
        }

        return true;
    }

    /**
     * @fn private void loadCommonConfig()
     * @brief COMMON Section 을 로드하는 함수
     */
    private void loadCommonConfig() {
        this.udpRcvBufferSize = Integer.parseInt(getIniValue(SECTION_COMMON, FIELD_UDP_RCV_BUFFER_SIZE));
        this.udpSndBufferSize = Integer.parseInt(getIniValue(SECTION_COMMON, FIELD_UDP_SND_BUFFER_SIZE));

        this.longCallTime = Long.parseLong(getIniValue(SECTION_COMMON, FIELD_LONG_CALL_TIME));
        if (this.longCallTime < 0 || this.longCallTime >= Long.MAX_VALUE) {
            this.longCallTime = 600000; // 10 분
        }

        this.useClient = Boolean.parseBoolean(getIniValue(SECTION_COMMON, FIELD_USE_CLIENT));
        this.useProxy = Boolean.parseBoolean(getIniValue(SECTION_COMMON, FIELD_USE_PROXY));

        this.isProxyMode = Boolean.parseBoolean(getIniValue(SECTION_COMMON, FIELD_PROXY_MODE));

        // Client process > proxy 옵션 false 처리
        if (this.useClient) {
            this.isProxyMode = false;
        }

        // Proxy process > client 옵션 false 처리
        if (this.isProxyMode) {
            this.useClient = false;
            this.useProxy = false;
        }

        this.callAutoAccept = Boolean.parseBoolean(getIniValue(SECTION_COMMON, FIELD_CALL_AUTO_ACCEPT));

        logger.debug("Load [{}] config...(OK)", SECTION_COMMON);
    }

    /**
     * @fn private void loadSignalConfig()
     * @brief SIGNAL Section 을 로드하는 함수
     */
    private void loadSignalConfig() {
        this.hostName = getIniValue(SECTION_SIGNAL, FIELD_HOST_NAME);

        this.fromIp = getIniValue(SECTION_SIGNAL, FIELD_FROM_IP);
        this.fromPort = Integer.parseInt(getIniValue(SECTION_SIGNAL, FIELD_FROM_PORT));
        if (this.fromPort <= 0 || this.fromPort >= 65536) {
            this.fromPort = 5000;
        }

        this.toIp = getIniValue(SECTION_SIGNAL, FIELD_TO_IP);
        this.toPort = Integer.parseInt(getIniValue(SECTION_SIGNAL, FIELD_TO_PORT));
        if (this.toPort <= 0 || this.toPort >= 65536) {
            logger.debug("! Remote sip port is wrong. Program ends. (remoteSipPort={})", this.toPort);
            ServiceManager.getInstance().stop();
        }

        this.defaultRegisterExpires = Integer.parseInt(getIniValue(SECTION_SIGNAL, FIELD_DEFAULT_REGISTER_EXPIRES));
        if (this.defaultRegisterExpires <= 0 || this.defaultRegisterExpires >= Integer.MAX_VALUE) {
            this.defaultRegisterExpires =  600000; // 10분
        }

        this.callRecvDuration = Integer.parseInt(getIniValue(SECTION_SIGNAL, FIELD_CALL_RECV_DURATION));
        if (this.callRecvDuration <= 0 || this.callRecvDuration >= Integer.MAX_VALUE) {
            this.callRecvDuration =  60000; // 1분
        }

        logger.debug("Load [{}] config...(OK)", SECTION_SIGNAL);
    }

    /**
     * @fn private void loadMediaConfig()
     * @brief MEDIA Section 을 로드하는 함수
     */
    private void loadMediaConfig() {
        this.sendWav = Boolean.parseBoolean(getIniValue(SECTION_MEDIA, FIELD_SEND_WAV));
        this.lastWavPath = getIniValue(SECTION_MEDIA, FIELD_LAST_WAV_PATH);

        this.dtmf = Boolean.parseBoolean(getIniValue(SECTION_MEDIA, FIELD_DTMF));

        this.priorityAudioCodec = getIniValue(SECTION_MEDIA, FIELD_PRIORITY_CODEC);
        MediaManager.getInstance().setPriorityCodec(this.priorityAudioCodec);

        this.nettyServerConsumerCount = Integer.parseInt(getIniValue(SECTION_MEDIA, FIELD_NETTY_SERVER_CONSUMER_COUNT));

        this.nettyServerIp = getIniValue(SECTION_MEDIA, FIELD_NETTY_SERVER_IP);
        this.nettyServerPort = Integer.parseInt(getIniValue(SECTION_MEDIA, FIELD_NETTY_SERVER_PORT));
        if (this.nettyServerPort <= 0 || this.nettyServerPort >= 65536) {
            logger.debug("! Media port is wrong. Program ends. (mediaPort={})", this.nettyServerPort);
            ServiceManager.getInstance().stop();
        }

        this.speakerVolume = Integer.parseInt(getIniValue(SECTION_MEDIA, FIELD_SPEAKER_VOLUME));
        if (this.speakerVolume <= 0 || this.speakerVolume > 100) {
            this.speakerVolume = 100;
        }

        this.mikeVolume = Integer.parseInt(getIniValue(SECTION_MEDIA, FIELD_MIKE_VOLUME));
        if (this.mikeVolume <= 0 || this.mikeVolume > 100) {
            this.mikeVolume = 100;
        }

        this.relay = Boolean.parseBoolean(getIniValue(SECTION_MEDIA, FIELD_RELAY));

        logger.debug("Load [{}] config...(OK)", SECTION_MEDIA);
    }

    /**
     * @fn private void loadRecordConfig()
     * @brief RECORD Section 을 로드하는 함수
     */
    private void loadRecordConfig() {
        this.recordPath = getIniValue(SECTION_RECORD, FIELD_RECORD_PATH);

        this.rawFile = Boolean.parseBoolean(getIniValue(SECTION_RECORD, FIELD_RAW_FILE));
        this.encFile = Boolean.parseBoolean(getIniValue(SECTION_RECORD, FIELD_ENC_FILE));
        this.decFile = Boolean.parseBoolean(getIniValue(SECTION_RECORD, FIELD_DEC_FILE));

        logger.debug("Load [{}] config...(OK)", SECTION_RECORD);
    }

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @fn private String getIniValue(String section, String key)
     * @brief INI 파일에서 지정한 section 과 key 에 해당하는 value 를 가져오는 함수
     * @param section Section
     * @param key Key
     * @return 성공 시 value, 실패 시 null 반환
     */
    private String getIniValue(String section, String key) {
        String value = ini.get(section,key);
        if (value == null) {
            logger.warn("[ {} ] \" {} \" is null.", section, key);
            ServiceManager.getInstance().stop();
            System.exit(1);
            return null;
        }

        value = value.trim();
        logger.debug("\tGet Config [{}] > [{}] : [{}]", section, key, value);
        return value;
    }

    /**
     * @fn public void setIniValue(String section, String key, String value)
     * @brief INI 파일에 새로운 value 를 저장하는 함수
     * @param section Section
     * @param key Key
     * @param value Value
     */
    public void setIniValue(String section, String key, String value) {
        try {
            ini.put(section, key, value);
            ini.store();

            logger.debug("\tSet Config [{}] > [{}] : [{}]", section, key, value);
        } catch (IOException e) {
            logger.warn("Fail to set the config. (section={}, field={}, value={})", section, key, value);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

    public int getUdpRcvBufferSize() {
        return udpRcvBufferSize;
    }

    public void setUdpRcvBufferSize(int udpRcvBufferSize) {
        this.udpRcvBufferSize = udpRcvBufferSize;
    }

    public int getUdpSndBufferSize() {
        return udpSndBufferSize;
    }

    public void setUdpSndBufferSize(int udpSndBufferSize) {
        this.udpSndBufferSize = udpSndBufferSize;
    }

    public long getLongCallTime() {
        return longCallTime;
    }

    public void setLongCallTime(long longCallTime) {
        this.longCallTime = longCallTime;
    }

    public boolean isUseClient() {
        return useClient;
    }

    public void setUseClient(boolean useClient) {
        this.useClient = useClient;
    }

    public boolean isCallAutoAccept() {
        return callAutoAccept;
    }

    public void setCallAutoAccept(boolean callAutoAccept) {
        this.callAutoAccept = callAutoAccept;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getFromIp() {
        return fromIp;
    }

    public void setFromIp(String fromIp) {
        this.fromIp = fromIp;
    }

    public int getFromPort() {
        return fromPort;
    }

    public void setFromPort(int fromPort) {
        this.fromPort = fromPort;
    }

    public boolean isProxyMode() {
        return isProxyMode;
    }

    public void setProxyMode(boolean proxyMode) {
        isProxyMode = proxyMode;
    }

    public String getToIp() {
        return toIp;
    }

    public void setToIp(String toIp) {
        this.toIp = toIp;
    }

    public int getToPort() {
        return toPort;
    }

    public void setToPort(int toPort) {
        this.toPort = toPort;
    }

    public int getDefaultRegisterExpires() {
        return defaultRegisterExpires;
    }

    public void setDefaultRegisterExpires(int defaultRegisterExpires) {
        this.defaultRegisterExpires = defaultRegisterExpires;
    }

    public int getCallRecvDuration() {
        return callRecvDuration;
    }

    public void setCallRecvDuration(int callRecvDuration) {
        this.callRecvDuration = callRecvDuration;
    }

    public String getPriorityAudioCodec() {
        return priorityAudioCodec;
    }

    public void setPriorityAudioCodec(String priorityAudioCodec) {
        this.priorityAudioCodec = priorityAudioCodec;
    }

    public int getNettyServerConsumerCount() {
        return nettyServerConsumerCount;
    }

    public void setNettyServerConsumerCount(int nettyServerConsumerCount) {
        this.nettyServerConsumerCount = nettyServerConsumerCount;
    }

    public String getNettyServerIp() {
        return nettyServerIp;
    }

    public void setNettyServerIp(String nettyServerIp) {
        this.nettyServerIp = nettyServerIp;
    }

    public int getNettyServerPort() {
        return nettyServerPort;
    }

    public void setNettyServerPort(int nettyServerPort) {
        this.nettyServerPort = nettyServerPort;
    }

    public String getRecordPath() {
        return recordPath;
    }

    public void setRecordPath(String recordPath) {
        this.recordPath = recordPath;
    }

    public boolean isRawFile() {
        return rawFile;
    }

    public void setRawFile(boolean rawFile) {
        this.rawFile = rawFile;
    }

    public boolean isEncFile() {
        return encFile;
    }

    public void setEncFile(boolean encFile) {
        this.encFile = encFile;
    }

    public boolean isDecFile() {
        return decFile;
    }

    public void setDecFile(boolean decFile) {
        this.decFile = decFile;
    }

    public int getSpeakerVolume() {
        return speakerVolume;
    }

    public void setSpeakerVolume(int speakerVolume) {
        this.speakerVolume = speakerVolume;
    }

    public int getMikeVolume() {
        return mikeVolume;
    }

    public void setMikeVolume(int mikeVolume) {
        this.mikeVolume = mikeVolume;
    }

    public boolean isRelay() {
        return relay;
    }

    public void setRelay(boolean relay) {
        this.relay = relay;
    }

    public boolean isUseProxy() {
        return useProxy;
    }

    public void setUseProxy(boolean useProxy) {
        this.useProxy = useProxy;
    }

    public boolean isDtmf() {
        return dtmf;
    }

    public void setDtmf(boolean dtmf) {
        this.dtmf = dtmf;
    }

    public boolean isSendWav() {
        return sendWav;
    }

    public void setSendWav(boolean sendWav) {
        this.sendWav = sendWav;
    }

    public String getLastWavPath() {
        return lastWavPath;
    }

    public void setLastWavPath(String lastWavPath) {
        this.lastWavPath = lastWavPath;
    }
}
