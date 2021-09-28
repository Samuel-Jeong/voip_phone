package media.netty.module;

import config.ConfigManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import media.netty.NettyChannelManager;
import media.netty.handler.ClientChannelHandler;
import media.netty.handler.ServerChannelHandler;
import media.netty.module.base.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AppInstance;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @class public class NettyChannel
 * @brief NettyChannel class
 */
public class NettyChannel {

    private static final Logger logger = LoggerFactory.getLogger(NettyChannel.class);

    private NioEventLoopGroup group;
    private Bootstrap b;

    /*메시지 수신용 채널 */
    private Channel serverChannel;

    /* MessageSender Map */
    /* Key: To MDN, value: MessageSender */
    private final HashMap<String, MessageSender> messageSenderMap = new HashMap<>();
    private final ReentrantLock messageSenderLock = new ReentrantLock();

    private final int listenPort;

    ////////////////////////////////////////////////////////////////////////////////

    public NettyChannel(int port) {
        this.listenPort = port;
    }

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @fn public void run(String key)
     * @brief Netty Channel 을 구동하는 함수
     * netty server option 과 channel handler 를 설정한다.
     * @param key ServerChannelHandler key
     */
    public void run (String key) {
        int nioThreadCount = AppInstance.getInstance().getConfigManager().getNettyServerConsumerCount();
        int nettyRecvBufferSize = AppInstance.getInstance().getConfigManager().getUdpRcvBufferSize();
        int nettySendBufferSize = AppInstance.getInstance().getConfigManager().getUdpSndBufferSize();

        group = new NioEventLoopGroup(nioThreadCount);
        b = new Bootstrap();
        b.group(group).channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, false)
                .option(ChannelOption.SO_SNDBUF, nettySendBufferSize)
                .option(ChannelOption.SO_RCVBUF, nettyRecvBufferSize)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    public void initChannel (final NioDatagramChannel ch) {
                        final ChannelPipeline pipeline = ch.pipeline();
                        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
                        if (configManager.isUseClient()) {
                            pipeline.addLast(
                                    //new DefaultEventExecutorGroup(1),
                                    new ClientChannelHandler()
                            );
                        } else if (configManager.isProxyMode()) {
                            pipeline.addLast(
                                    //new DefaultEventExecutorGroup(1),
                                    new ServerChannelHandler(
                                            NettyChannelManager.getInstance().getProxyJitter(key),
                                            key
                                    )
                            );
                        }
                    }
                });
    }

    /**
     * @fn public void stop()
     * @brief Netty Channel 을 종료하는 함수
     */
    public void stop () {
        deleteAllMessageSenders();
        group.shutdownGracefully();
    }

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @param ip   바인딩할 ip
     * @param port 바인당할 port
     * @return 성공 시 생성된 Channel, 실패 시 null 반환
     * @fn public Channel openChannel(String ip, int port)
     * @brief Netty Server Channel 을 생성하는 함수
     */
    public Channel openChannel (String ip, int port) {
        if (serverChannel != null) {
            logger.warn("Channel is already opened.");
            return null;
        }

        InetAddress address;
        ChannelFuture channelFuture;

        try {
            address = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            logger.warn("UnknownHostException is occurred. (ip={})", ip, e);
            return null;
        }

        try {
            channelFuture = b.bind(address, port).sync();
            serverChannel = channelFuture.channel();
            logger.debug("Channel is opened. (ip={}, port={})", address, port);

            return channelFuture.channel();
        } catch (Exception e) {
            logger.warn("Channel is interrupted. (address={}:{})", ip, port, e);
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /**
     * @fn public void closeChannel()
     * @brief Netty Server Channel 을 닫는 함수
     */
    public void closeChannel ( ) {
        if (serverChannel == null) {
            logger.warn("Channel is already closed.");
            return;
        }

        serverChannel.close();
        logger.debug("Channel is closed.");
    }

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @fn public MessageSender addMessageSender (String ip, int port)
     * @brief MessageSender 을 새로 추가하는 함수
     * @param key MessageSender key
     * @param ip   바인딩할 ip
     * @param port 바인당할 port
     * @return 성공 시 생성된 MessageSender 객체, 실패 시 null 반환
     */
    public MessageSender addMessageSender (String key, String ip, int port) {
        try {
            messageSenderLock.lock();

            if (messageSenderMap.get(key) != null) {
                logger.trace("MessageSender is already connected. (key={})", key);
                return null;
            }

            MessageSender messageSender = new MessageSender(
                    key,
                    ip,
                    port
            ).start(b);

            if (messageSender == null) {
                logger.warn("Fail to create MessageSender. (key={})", key);
                return null;
            }

            messageSenderMap.putIfAbsent(
                    key,
                    messageSender
            );

            logger.debug("MessageSender is created. (key={})", key);
            return messageSenderMap.get(key);
        } catch (Exception e) {
            logger.warn("MessageSender is interrupted. (key={}, ip={}, port={})", key, ip, port, e);
            Thread.currentThread().interrupt();
            return null;
        } finally {
            messageSenderLock.unlock();
        }
    }

    /**
     * @fn public void deleteMessageSender (String key)
     * @brief Netty MessageSender 을 삭제하는 함수
     * @param key MessageSender key
     */
    public void deleteMessageSender (String key) {
        try {
            messageSenderLock.lock();

            MessageSender messageSender = messageSenderMap.get(key);
            if (messageSender == null) {
                logger.warn("MessageSender is null. Fail to delete the MessageSender. (key={})", key);
                return;
            }

            messageSender.stop();
            logger.debug("MessageSender is deleted. (key={})", key);
        } catch (Exception e) {
            logger.warn("Fail to delete the MessageSender. (key={})", key, e);
        } finally {
            messageSenderLock.unlock();
        }
    }

    public void deleteAllMessageSenders () {
        try {
            messageSenderLock.lock();

            for (Map.Entry<String, MessageSender> entry : getCloneMessageSenderMap().entrySet()) {
                String key = entry.getKey();
                if (key == null) {
                    continue;
                }

                MessageSender messageSender = entry.getValue();
                if (messageSender == null) {
                    continue;
                }

                messageSender.stop();
                messageSenderMap.remove(key);
            }
        } catch (Exception e) {
            logger.warn("Fail to delete all the MessageSenders.", e);
        } finally {
            messageSenderLock.unlock();
        }
    }

    public Map<String, MessageSender> getCloneMessageSenderMap() {
        HashMap<String, MessageSender> cloneMap;

        try {
            messageSenderLock.lock();

            cloneMap = (HashMap<String, MessageSender>) messageSenderMap.clone();
        } catch (Exception e) {
            logger.warn("Fail to clone the message sender map.");
            cloneMap = messageSenderMap;
        } finally {
            messageSenderLock.unlock();
        }

        return cloneMap;
    }

    /**
     * @fn public MessageSender getMessageSender (String key)
     * @brief 지정한 key 에 해당하는 MessageSender 를 반환하는 함수
     * @param key MessageSender key
     * @return 성공 시 MessageSender 객체, 실패 시 null 반환
     */
    public MessageSender getMessageSender (String key) {
        try {
            messageSenderLock.lock();

            return messageSenderMap.get(key);
        } catch (Exception e) {
            logger.warn("Fail to get the messageSender. (key={})", key, e);
            return null;
        } finally {
            messageSenderLock.unlock();
        }
    }

    /**
     * @fn public boolean sendMessage (String key, ByteBuf buf, String remoteIp, int remotePort)
     * @brief 메시지 송신 함수
     * @param key MessageSender key
     * @param buf ByteBuf (UDP data, IP packet payload)
     * @param remoteIp Remote IP
     * @param remotePort Remote Port
     * @return 성공 시 true, 실패 시 false 반환
     */
    public boolean sendMessage (String key, ByteBuf buf, String remoteIp, int remotePort) {
        if (buf == null) {
            return false;
        }

        try {
            MessageSender messageSender = getMessageSender(key);
            if (messageSender == null) {
                messageSender = addMessageSender(key, remoteIp, remotePort);
            } else if (!messageSender.getIp().equals(remoteIp) || messageSender.getPort() != remotePort) {
                deleteMessageSender(key);
                messageSender = addMessageSender(key, remoteIp, remotePort);
            }

            if (messageSender != null) {
                if (!messageSender.isActive()) {
                    logger.warn("MessageSender is not active or deleted. Send failed.");
                    return false;
                }

                messageSender.send(buf, remoteIp, remotePort);
            } else {
                logger.warn("MessageSender is not initialized yet.");
                return false;
            }
        } catch (Exception e) {
            logger.warn("MessageSender fails to send the message.");
        }

        return true;
    }

    public int getListenPort() {
        return listenPort;
    }
}
