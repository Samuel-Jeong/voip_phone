package media.netty;

import config.ConfigManager;
import io.netty.channel.Channel;
import media.netty.module.NettyChannel;
import media.protocol.rtp.base.Clock;
import media.protocol.rtp.base.RtpClock;
import media.protocol.rtp.jitter.FixedJitterBuffer;
import media.protocol.rtp.jitter.JitterBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AppInstance;
import service.ServerRtpHandler;
import service.TaskManager;
import signal.module.ResourceManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @class public class NettyChannelManager
 * @brief Netty channel manager 클래스
 * RTP Netty Channel 을 관리한다.
 */
public class NettyChannelManager {

    private static final Logger logger = LoggerFactory.getLogger(NettyChannelManager.class);

    private static NettyChannelManager manager = null;

    /* Client 용 Netty Channel */
    private NettyChannel clientChannel = null;

    /* Proxy 용 Netty Channel 관리 map */
    /* Key: Channel-ID (using call-id), value: NettyChannel */
    private final HashMap<String, NettyChannel> proxyChannelMap = new HashMap<>();
    /* Proxy Channel Map Lock */
    private final ReentrantLock proxyChannelMapLock = new ReentrantLock();

    /* Key: Mix Audio Key (using call-id), value: JitterBuffer */
    private final HashMap<String, JitterBuffer> proxyJitterMap = new HashMap<>();
    /* Proxy Jitter Map Lock */
    private final ReentrantLock proxyJitterMapLock = new ReentrantLock();

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @fn private NettyChannelManager ()
     * @brief NettyChannelManager 생성자 함수
     */
    private NettyChannelManager() {
        // Nothing
    }

    /**
     * @return 최초 호출 시 새로운 NettyChannelManager 전역 변수, 이후 모든 호출에서 항상 이전에 생성된 변수 반환
     * @fn public static NettyChannelManager getInstance ()
     * @brief NettyChannelManager 싱글턴 변수를 반환하는 함수
     */
    public static NettyChannelManager getInstance () {
        if (manager == null) {
            manager = new NettyChannelManager();

        }
        return manager;
    }

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @fn public void start ()
     * @brief Remote 연동을 위한 netty channel 를 구동하는 함수
     */
    public void start () {
        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        if (configManager.isUseClient()) {
            openClientChannel();
        }
    }

    /**
     * @fn public void stop ()
     * @brief Remote 연동을 위한 netty channel 를 종료하는 함수
     */
    public void stop () {
        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        if (configManager.isUseClient()) {
            closeClientChannel();
        }

        if (configManager.isProxyMode()) {
            deleteAllProxyChannels();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @fn private void openClientChannel ()
     * @brief Remote 로부터 packet 을 수신하기 위한 모든 channel 을 open 하는 함수
     */
    private void openClientChannel() {
        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        if (configManager.isUseClient()) {
            if (clientChannel == null) {
                int port = ResourceManager.getInstance().takePort();
                clientChannel = new NettyChannel(port);
                clientChannel.run(null);

                Channel channel = clientChannel.openChannel(
                        configManager.getNettyServerIp(),
                        port
                );

                if (channel == null) {
                    logger.warn("Fail to open client channel.");
                } else {
                    /*TaskManager.getInstance().addTask(
                            ClientRtpHandler.class.getSimpleName(),
                            new ClientRtpHandler(
                                    clientChannel.getJitterBuffer(),
                                    1
                            )
                    );*/

                    logger.debug("Success to open client channel.");
                }
            } else {
                logger.warn("Fail to open client channel.");
            }
        }
    }

    /**
     * @fn private void closeClientChannel ()
     * @brief Remote 로부터 packet 을 수신하기 위한 모든 channel 을 close 하는 함수
     */
    private void closeClientChannel() {
        if (clientChannel != null) {
            //TaskManager.getInstance().removeTask(ClientRtpHandler.class.getSimpleName());

            int port = clientChannel.getListenPort();
            clientChannel.closeChannel();
            clientChannel.stop();
            clientChannel = null;
            ResourceManager.getInstance().restorePort(port);

            logger.debug("Success to close the client channel.");
        }
    }

    public NettyChannel getClientChannel() {
        return clientChannel;
    }

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @fn public boolean addProxyChannel (String key)
     * @brief 지정한 key 로 Proxy channel 을 새로 추가하는 함수
     * @param key Channel-ID
     * @return 성공 시 true, 실패 시 false 반환
     */
    public boolean addProxyChannel(String key) {
        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        if (configManager.isProxyMode()) {
            try {
                proxyChannelMapLock.lock();

                if (proxyChannelMap.get(key) != null) {
                    logger.trace("Fail to add the proxy channel. Key is duplicated. (key={})", key);
                    return false;
                }

                addProxyJitter(key);

                int port = ResourceManager.getInstance().takePort();
                if (port == -1) {
                    logger.warn("Fail to add the proxy channel. Port is full. (key={})", key);
                    return false;
                }

                NettyChannel serverChannel = new NettyChannel(port);
                serverChannel.run(key);

                // 메시지 수신용 채널 open
                // Port 다르게 해야함
                Channel channel = serverChannel.openChannel(
                        configManager.getNettyServerIp(),
                        port
                );

                if (channel == null) {
                    serverChannel.closeChannel();
                    serverChannel.stop();
                    deleteProxyJitter(key);
                    logger.warn("Fail to add the proxy channel. (key={})", key);
                    return false;
                }

                proxyChannelMap.putIfAbsent(key, serverChannel);
                logger.debug("Success to add proxy channel (key={}).", key);
            } catch (Exception e) {
                logger.warn("Fail to add proxy channel (key={}).", key, e);
            } finally {
                proxyChannelMapLock.unlock();
            }

            return true;
        }

        return false;
    }

    /**
     * @fn public void deleteProxyChannel(String key)
     * @brief 지정한 key 에 해당하는 Proxy channel 을 삭제하는 함수
     * @param key Channel-ID
     */
    public void deleteProxyChannel(String key) {
        try {
            proxyChannelMapLock.lock();

            if (!proxyChannelMap.isEmpty()) {
                NettyChannel nettyChannel = proxyChannelMap.get(key);
                if (nettyChannel == null) {
                    return;
                }

                deleteProxyJitter(key);

                int port = nettyChannel.getListenPort();
                nettyChannel.closeChannel();
                nettyChannel.stop();
                ResourceManager.getInstance().restorePort(port);
                proxyChannelMap.remove(key);

                logger.debug("Success to close the proxy channel. (key={})", key);
            }
        } catch (Exception e) {
            logger.warn("Fail to close the proxy channel. (key={})", key, e);
        } finally {
            proxyChannelMapLock.unlock();
        }
    }

    /**
     * @fn public void deleteAllProxyChannels ()
     * @brief 모든 Proxy channel 을 삭제하는 함수
     */
    public void deleteAllProxyChannels () {
        try {
            proxyChannelMapLock.lock();

            if (!proxyChannelMap.isEmpty()) {
                for (Map.Entry<String, NettyChannel> entry : getCloneProxyChannelMap().entrySet()) {
                    NettyChannel nettyChannel = entry.getValue();
                    if (nettyChannel == null) {
                        continue;
                    }

                    int port = nettyChannel.getListenPort();
                    nettyChannel.closeChannel();
                    nettyChannel.stop();
                    ResourceManager.getInstance().restorePort(port);
                    proxyChannelMap.remove(entry.getKey());
                }

                logger.debug("Success to close all proxy channel(s).");
            }
        } catch (Exception e) {
            logger.warn("Fail to close all proxy channel(s).", e);
        } finally {
            proxyChannelMapLock.unlock();
        }
    }

    /**
     * @fn public NettyChannel getProxyChannel(String key)
     * @brief 지정한 키의 Proxy Channel 을 반환하는 함수
     * @param key Channel-ID
     * @return 성공 시 Proxy Channel 을 반환, 실패 시 null 반환
     */
    public NettyChannel getProxyChannel(String key) {
        try {
            proxyChannelMapLock.lock();

            return proxyChannelMap.get(key);
        } catch (Exception e) {
            logger.warn("NettyChannelManager.getProxyChannel.Exception", e);
            return null;
        } finally {
            proxyChannelMapLock.unlock();
        }
    }

    public Map<String, NettyChannel> getCloneProxyChannelMap() {
        HashMap<String, NettyChannel> cloneMap;

        try {
            proxyChannelMapLock.lock();

            try {
                cloneMap = (HashMap<String, NettyChannel>) proxyChannelMap.clone();
            } catch (Exception e) {
                logger.warn("Fail to clone the proxy channel map.");
                cloneMap = proxyChannelMap;
            }
        } catch (Exception e) {
            logger.warn("NettyChannelManager.getCloneProxyChannelMap.Exception", e);
            return null;
        } finally {
            proxyChannelMapLock.unlock();
        }

        return cloneMap;
    }


    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @fn public void addProxyJitter(String key)
     * @brief Proxy JitterBuffer 를 새로 추가하는 함수
     * @param key Jitter Buffer key
     */
    public void addProxyJitter(String key) {
        try {
            if (key == null) {
                return;
            }

            proxyJitterMapLock.lock();

            if (proxyJitterMap.get(key) != null) {
                logger.warn("Fail to add proxy jitter. Duplication key. (key={})", key);
                return;
            }

            String jitterBufferKey = ServerRtpHandler.class.getSimpleName() + "_" + key;
            JitterBuffer jitterBuffer = new FixedJitterBuffer(
                    jitterBufferKey,
                    new RtpClock(new Clock()),
                    10000
            );

            TaskManager.getInstance().addTask(
                    jitterBufferKey,
                    new ServerRtpHandler(
                            key,
                            jitterBuffer,
                            20
                    )
            );

            proxyJitterMap.putIfAbsent(
                    key,
                    jitterBuffer
            );
        } catch (Exception e) {
            logger.warn("Fail to add proxy jitter. (key={})", key, e);
        } finally {
            proxyJitterMapLock.unlock();
        }
    }

    /**
     * @fn public void deleteProxyJitter(String key)
     * @brief Proxy JitterBuffer 를 삭제하는 함수
     * @param key Jitter Buffer key
     */
    public void deleteProxyJitter(String key) {
        try {
            if (key == null) {
                return;
            }

            proxyJitterMapLock.lock();

            if (proxyJitterMap.get(key) == null) {
                logger.warn("Fail to delete proxy jitter. Unknown key. (key={})", key);
                return;
            }

            TaskManager.getInstance().removeTask(
                    ServerRtpHandler.class.getSimpleName() + "_" + key
            );

            proxyJitterMap.remove(key);
        } catch (Exception e) {
            logger.warn("Fail to delete proxy jitter. (key={})", key, e);
        } finally {
            proxyJitterMapLock.unlock();
        }
    }

    /**
     * @fn public void getProxyJitter(String key)
     * @brief Proxy JitterBuffer 를 반환하는 함수
     * @param key Jitter Buffer key
     */
    public JitterBuffer getProxyJitter(String key) {
        try {
            if (key == null) {
                return null;
            }

            proxyJitterMapLock.lock();

            return proxyJitterMap.get(key);
        } catch (Exception e) {
            logger.warn("Fail to get proxy jitter. (key={})", key, e);
            return null;
        } finally {
            proxyJitterMapLock.unlock();
        }
    }

}
