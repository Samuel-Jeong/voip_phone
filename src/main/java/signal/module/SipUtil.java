package signal.module;

import client.VoipClient;
import client.gui.FrameManager;
import config.ConfigManager;
import media.MediaManager;
import media.module.mixing.AudioMixManager;
import media.netty.NettyChannelManager;
import media.netty.module.NettyChannel;
import media.sdp.SdpParser;
import media.sdp.base.Sdp;
import media.sdp.base.attribute.RtpAttribute;
import media.sdp.base.media.MediaFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.*;
import signal.SignalManager;
import signal.base.CallInfo;
import signal.base.RegiInfo;

import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

/**
 * @class public class SipUtil
 * @brief SipUtil class
 */
public class SipUtil implements SipListener {

    private static final Logger logger = LoggerFactory.getLogger(SipUtil.class);

    ////////////////////////////////////////////////////////////////////////////////
    // COMMON
    private final Random random = new Random();

    ////////////////////////////////////////////////////////////////////////////////
    // SIP Stack Variables
    private static final String AUTHENTICATION_ALGORITHM = "MD5";
    private static final String SIP_TRANSPORT_TYPE = "udp";

    private AddressFactory addressFactory = null;
    private HeaderFactory headerFactory = null;
    private MessageFactory messageFactory = null;
    private SipStack sipStack = null;
    private SipProvider sipProvider = null;

    private Address hostAddress = null;
    private Address contactAddress = null;
    private ContactHeader contactHeader = null;
    private String hostIp;
    private int hostPort;
    private String hostName;
    private int defaultRegisterExpires;

    ////////////////////////////////////////////////////////////////////////////////
    // SIP Object Variables
    private String toIp;
    private int toPort;

    ////////////////////////////////////////////////////////////////////////////////
    // SIP Register Variables
    private final String md5PassWd = "1234";
    private String userNonce = null;

    private String sessionId = NonceGenerator.createRandomNonce();

    ////////////////////////////////////////////////////////////////////////////////

    public SipUtil() {
        // Nothing
    }

    ////////////////////////////////////////////////////////////////////////////////

    public String getToIp() {
        return toIp;
    }

    public int getToPort() {
        return toPort;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public void start () {
        try {
            sipStack.start();
        } catch (Exception e) {
            logger.warn("Fail to start the sip stack.");
        }
    }

    public void stop () {
        try {
            CallManager.getInstance().clearCallInfoMap();
            sipStack.stop();
        } catch (Exception e) {
            logger.warn("Fail to stop the sip stack.");
        }
    }

    public void init() {
        SipFactory sipFactory = SipFactory.getInstance();

        try {
            ConfigManager configManager = AppInstance.getInstance().getConfigManager();
            hostName = configManager.getHostName();
            hostIp = configManager.getFromIp();
            hostPort = configManager.getFromPort();

            defaultRegisterExpires = configManager.getDefaultRegisterExpires();
            toIp = configManager.getToIp();
            toPort = configManager.getToPort();

            sipFactory.setPathName("gov.nist");

            //Date curTime = new Date();
            //SimpleDateFormat timeFormat = new SimpleDateFormat("yyyyMMddHH");
            //String curTimeStr = timeFormat.format(curTime);

            Properties properties = new Properties();
            properties.setProperty("javax.sip.STACK_NAME", hostName);
            //properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "TRACE");
            //properties.setProperty("gov.nist.javax.sip.DEBUG_LOG", hostName + "_" + curTimeStr + "_debug.log");
            //properties.setProperty("gov.nist.javax.sip.SERVER_LOG", hostName + "_" + curTimeStr + "_server.log");

            if (sipStack != null) {
                sipStack.stop();
            }

            sipStack = sipFactory.createSipStack(properties);
        } catch (Exception e) {
            logger.warn("{} PeerUnavailableException", LogFormatter.getCallLogHeader(null, null, null, null), e);
            //ServiceManager.getInstance().stop();
            System.exit(1);
        }

        try {
            if (addressFactory == null) { addressFactory = sipFactory.createAddressFactory(); }
            if (headerFactory == null) { headerFactory = sipFactory.createHeaderFactory(); }
            if (messageFactory == null) { messageFactory = sipFactory.createMessageFactory(); }

            ListeningPoint listeningPoint = sipStack.createListeningPoint(hostIp, hostPort, "udp");
            SipUtil listener = this;

            sipProvider = sipStack.createSipProvider(listeningPoint);
            sipProvider.addSipListener(listener);
            sipProvider.setAutomaticDialogSupportEnabled(false);

            SipURI sipUri = addressFactory.createSipURI(hostName, hostIp);
            sipUri.setHost(hostIp);
            sipUri.setPort(hostPort);
            sipUri.setLrParam();
            hostAddress = addressFactory.createAddress(hostName, sipUri);

            hostIp = listeningPoint.getIPAddress();
            hostPort = listeningPoint.getPort();
            contactAddress = addressFactory.createAddress("<sip:" + hostName + "@" + hostIp + ":" + hostPort + ">");
            contactHeader = headerFactory.createContactHeader(contactAddress);

            logger.debug("sipStack: [{}] [{}] [{}] [{}]",
                    sipStack.getStackName(),
                    listeningPoint.getIPAddress(),
                    listeningPoint.getPort(),
                    listeningPoint.getTransport()
            );
        } catch (Exception e) {
            logger.warn("SignalManager.Exception", e);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @fn public String parseSipIp(Header header)
     * @brief From 또는 To header 에서 SIP IP 를 문자열로 파싱하는 함수
     * @param header From or To header
     * @return 성공 시 SIP IP String, 실패 시 null 반환
     */
    public String parseSipIp(Header header) {
        if (header == null) {
            return null;
        }

        URI uri;
        if (header.getName().equals(FromHeader.NAME)) {
            FromHeader fromHeader = (FromHeader) header;
            uri = fromHeader.getAddress().getURI();
        } else if (header.getName().equals(ToHeader.NAME)) {
            ToHeader toHeader = (ToHeader) header;
            uri = toHeader.getAddress().getURI();
        } else {
            return null;
        }

        if (uri == null) {
            return null;
        }

        String uriScheme = uri.toString();
        if (uriScheme == null) {
            return null;
        }

        String address = uriScheme.substring(uriScheme.indexOf("@") + 1);
        return address.substring(0, address.indexOf(":"));
    }

    /**
     * @fn public int parseSipPort(Header header)
     * @brief From 또는 To header 에서 SIP Port 를 정수로 파싱하는 함수
     * @param header From or To header
     * @return 성공 시 SIP Port Integer, 실패 시 null 반환
     */
    public int parseSipPort(Header header) {
        if (header == null) {
            return -1;
        }

        URI uri;
        if (header.getName().equals(FromHeader.NAME)) {
            FromHeader fromHeader = (FromHeader) header;
            uri = fromHeader.getAddress().getURI();
        } else if (header.getName().equals(ToHeader.NAME)) {
            ToHeader toHeader = (ToHeader) header;
            uri = toHeader.getAddress().getURI();
        } else {
            return -1;
        }

        if (uri == null) {
            return -1;
        }

        String uriScheme = uri.toString();
        if (uriScheme == null) {
            return -1;
        }

        String address = uriScheme.substring(uriScheme.indexOf("@") + 1);
        return Integer.parseInt(address.substring(address.indexOf(":") + 1, address.indexOf(";")));
    }

    //////////////////////////////////////////////////////////////////////

    @Override
    public void processRequest(RequestEvent requestEvent) {
        if (sipProvider == null) { return; }

        Request request = requestEvent.getRequest();
        if (request == null) {
            return;
        }

        try {
            if (Request.REGISTER.equals(request.getMethod())) {
                processRegister(requestEvent);
            } else if (Request.INVITE.equals(request.getMethod())) {
                processInvite(requestEvent);
            } else if (Request.BYE.equals(request.getMethod())) {
                processBye(requestEvent);
            } else if (Request.CANCEL.equals(request.getMethod())) {
                processCancel(requestEvent);
            } else if (Request.MESSAGE.equals(request.getMethod())) {
                processMessage(requestEvent);
            } else if (Request.ACK.equals(request.getMethod())) {
                processAck(requestEvent);
            } else {
                logger.warn("Undefined Request is detected.");
            }
        } catch (Exception e) {
            logger.warn("Fail to process the request.");
        }
    }

    @Override
    public void processResponse(ResponseEvent responseEvent) {
        Response response = responseEvent.getResponse();
        if (response == null) {
            return;
        }

        CSeqHeader cSeqHeader = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
        String requestMethodName = cSeqHeader.getMethod();

        logger.debug("Recv Response: {}", response);

        CallIdHeader callIdHeader = (CallIdHeader) response.getHeader(CallIdHeader.NAME);
        if (callIdHeader == null) {
            logger.warn("Fail to find the from header in the response. ({})", response);
            return;
        }

        String callId = callIdHeader.getCallId();

        CallInfo callInfo = null;
        if (!requestMethodName.equals(Request.REGISTER)) {
            callInfo = CallManager.getInstance().getCallInfo(callId);
            if (callInfo == null) {
                return;
            }
        }

        switch (response.getStatusCode()) {
            case Response.TRYING:
            case Response.RINGING:
                break;
            case Response.OK:
                processOk(responseEvent);
                break;
            case Response.UNAUTHORIZED:
                logger.debug("Recv 401 Unauthorized. Authentication will be processed.");
                if (requestMethodName.equals(Request.REGISTER)) {
                    sendRegister(true, response);
                } else {
                    sendRegister(true, null);
                    callInfo.setIsInviteUnauthorized(true);
                }
                break;
            case Response.FORBIDDEN:
                logger.debug("Recv 403 Forbidden.");
                if (requestMethodName.equals(Request.INVITE)) {
                    logger.debug("Call is not started.");
                    if (FrameManager.getInstance().processByeToFrame(ServiceManager.CLIENT_FRAME_NAME)) {
                        logger.debug("Success to process the 403 response to [{}] frame. (callId={})", ServiceManager.CLIENT_FRAME_NAME, callId);
                    }

                    TaskManager.getInstance().removeTask(CallCancelHandler.class.getSimpleName() + callId);
                    callInfo.setIsCallStarted(false);

                    CallManager.getInstance().deleteCallInfo(callId);
                } else {
                    logger.debug("Fail to register.");
                }
                break;
            case Response.REQUEST_TERMINATED:
                logger.debug("Recv 487 Request Terminated. Call is not started.");
                if (requestMethodName.equals(Request.INVITE)) {
                    if (FrameManager.getInstance().processByeToFrame(ServiceManager.CLIENT_FRAME_NAME)) {
                        logger.debug("Success to process the 487 response to [{}] frame. (callId={})", ServiceManager.CLIENT_FRAME_NAME, callId);
                    }

                    TaskManager.getInstance().removeTask(CallCancelHandler.class.getSimpleName() + callId);
                    callInfo.setIsCallStarted(false);
                    sendAck(responseEvent);

                    CallManager.getInstance().deleteCallInfo(callId);
                }
                break;
            default:
                logger.warn("Undefined response is detected. (responseCode={})", response.getStatusCode());
                break;
        }
    }

    @Override
    public void processTimeout(TimeoutEvent timeoutEvent) {
        Transaction transaction;
        if (timeoutEvent.isServerTransaction()) {
            transaction = timeoutEvent.getServerTransaction();
        } else {
            transaction = timeoutEvent.getClientTransaction();
        }

        if (transaction != null) {
            logger.warn("Transaction is timeout. (transactionState={}, dialog={})", transaction.getState(), transaction.getDialog());
        }
    }

    @Override
    public void processIOException(IOExceptionEvent ioExceptionEvent) {
        logger.warn("SignalManager.IOExceptionEvent (host={})", ioExceptionEvent.getHost());
    }

    @Override
    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
        if (transactionTerminatedEvent.isServerTransaction()) {
            ServerTransaction serverTransaction = transactionTerminatedEvent.getServerTransaction();
            if (serverTransaction != null && serverTransaction.getState() == TransactionState.TERMINATED) {
                logger.debug("ServerTransaction is terminated. (branchId={}, state={})", serverTransaction.getBranchId(), serverTransaction.getState());
            }
        } else {
            ClientTransaction clientTransaction = transactionTerminatedEvent.getClientTransaction();
            if (clientTransaction != null && clientTransaction.getState() == TransactionState.TERMINATED) {
                logger.debug("ClientTransaction is terminated. (branchId={}, state={})", clientTransaction.getBranchId(), clientTransaction.getState());
            }
        }
    }

    @Override
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        Dialog dialog = dialogTerminatedEvent.getDialog();
        if (dialog != null && dialog.getState() == DialogState.TERMINATED) {
            logger.debug("Dialog is terminated. (dialogId={}, state={}, callId={})", dialog.getDialogId(), dialog.getState(), dialog.getCallId());
        }
    }

    //////////////////////////////////////////////////////////////////////
    // REGISTER

    /**
     * @fn public void sendRegister ()
     * @brief REGISTER Method 를 보내는 함수
     * 클라이언트만 사용하는 함수
     */
    public void sendRegister (boolean isRecv401, Response response401) {
        try {
            String proxyHostName = VoipClient.getInstance().getProxyHostName();

            // Create SIP URI
            SipURI sipUri = addressFactory.createSipURI(proxyHostName, toIp);
            sipUri.setHost(toIp);
            sipUri.setPort(toPort);
            sipUri.setLrParam();

            // Add Route Header
            Address addressTo = addressFactory.createAddress(proxyHostName, sipUri);
            // Create the request URI for the SIP message
            URI requestURI = addressTo.getURI();

            // Create the SIP message headers
            // Via
            List<ViaHeader> viaHeaders = new ArrayList<>();
            ViaHeader viaHeader = this.headerFactory.createViaHeader(hostIp, hostPort, SIP_TRANSPORT_TYPE, null);
            viaHeaders.add(viaHeader);

            // Max-Forwards
            MaxForwardsHeader maxForwardsHeader = this.headerFactory.createMaxForwardsHeader(70);

            // Call -Id
            CallIdHeader callIdHeader = this.sipProvider.getNewCallId();

            // CSeq
            CSeqHeader cSeqHeader = this.headerFactory.createCSeqHeader(1L, Request.REGISTER);

            // From
            FromHeader fromHeader = this.headerFactory.createFromHeader(hostAddress, String.valueOf(random.nextInt(10000)));

            // To
            ToHeader toHeader = this.headerFactory.createToHeader(contactAddress, null);

            // Allow
            String allowList = Request.INVITE + ","
                    + Request.ACK + ","
                    + Request.CANCEL + ","
                    + Request.BYE + ","
                    + Request.MESSAGE;
            AllowHeader allowHeader = this.headerFactory.createAllowHeader(allowList);

            // Supported
            String supportedList = "path,gruu,outbound";
            SupportedHeader supportedHeader = this.headerFactory.createSupportedHeader(supportedList);

            // Expires
            ExpiresHeader expiresHeader = this.headerFactory.createExpiresHeader(defaultRegisterExpires);

            // Create the REGISTER request
            Request request = this.messageFactory.createRequest (
                    requestURI,
                    Request.REGISTER,
                    callIdHeader,
                    cSeqHeader,
                    fromHeader,
                    toHeader,
                    viaHeaders,
                    maxForwardsHeader
            );

            request.addHeader(allowHeader);
            request.addHeader(supportedHeader);
            request.addHeader(expiresHeader);
            request.addHeader(contactHeader);

            // 401 응답 수신하면 인증 과정 수행
            if (isRecv401 && response401 != null) {
                AuthorizationHeader authorizationHeader = this.headerFactory.createAuthorizationHeader(hostName);

                WWWAuthenticateHeader wwwAuthenticateHeader = (WWWAuthenticateHeader) response401.getHeader(WWWAuthenticateHeader.NAME);

                String userName = fromHeader.getAddress().getDisplayName();
                authorizationHeader.setUsername(userName);
                authorizationHeader.setRealm(wwwAuthenticateHeader.getRealm());
                authorizationHeader.setNonce(wwwAuthenticateHeader.getNonce());
                authorizationHeader.setURI(requestURI);
                authorizationHeader.setAlgorithm(wwwAuthenticateHeader.getAlgorithm());

                // MD5 Hashing
                MessageDigest messageDigest = MessageDigest.getInstance(AUTHENTICATION_ALGORITHM);

                messageDigest.update(userName.getBytes(StandardCharsets.UTF_8));
                messageDigest.update(wwwAuthenticateHeader.getRealm().getBytes(StandardCharsets.UTF_8));
                messageDigest.update(md5PassWd.getBytes(StandardCharsets.UTF_8));
                byte[] a1 = messageDigest.digest();
                messageDigest.reset();

                String uri = requestURI.getScheme();
                messageDigest.update(Request.REGISTER.getBytes(StandardCharsets.UTF_8));
                messageDigest.update(uri.getBytes(StandardCharsets.UTF_8));
                byte[] a2 = messageDigest.digest();
                messageDigest.reset();

                messageDigest.update(a1);
                messageDigest.update(a2);
                userNonce = new String(messageDigest.digest());

                authorizationHeader.setResponse(userNonce);
                request.addHeader(authorizationHeader);
            }

            ClientTransaction clientTransaction = sipProvider.getNewClientTransaction(request);
            clientTransaction.sendRequest();
            logger.debug("REGISTER Request sent. (request={})", request);
        }
        catch ( Exception e ) {
            logger.warn("REGISTER Request sent failed.", e);
        }
    }

    /**
     * @fn public void processRegister(RequestEvent requestEvent)
     * @brief REGISTER 요청을 처리하는 함수
     * 프록시만 사용하는 함수
     * @param requestEvent 요청 이벤트
     */
    public void processRegister(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        if (request != null) {
            logger.debug("Recv REGISTER: {}", request);
        } else {
            return;
        }

        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        boolean isUseClient = configManager.isUseClient();
        if (isUseClient) {
            logger.debug("This program is client. Fail to process the REGISTER request.");
            return;
        }

        try {
            CallIdHeader callIdHeader = (CallIdHeader) request.getHeader(CallIdHeader.NAME);
            if (callIdHeader == null) { return; }

            FromHeader fromHeader = (FromHeader) request.getHeader(FromHeader.NAME);
            String fromNo = fromHeader.getAddress().getDisplayName();

            RegiInfo regiInfo = RegiManager.getInstance().getRegi(fromNo);

            ExpiresHeader expiresHeader = (ExpiresHeader) request.getHeader(ExpiresHeader.NAME);
            int expires = expiresHeader.getExpires();

            MessageDigest messageDigest = MessageDigest.getInstance(AUTHENTICATION_ALGORITHM);
            Response response;

            if (regiInfo != null) {
                AuthorizationHeader authorizationHeader = (AuthorizationHeader) request.getHeader(AuthorizationHeader.NAME);
                if (authorizationHeader == null) {
                    response = messageFactory.createResponse(200, request);
                } else {
                    String responseNonce = authorizationHeader.getResponse();
                    if (responseNonce.equals(userNonce)) {
                        response = messageFactory.createResponse(200, request);
                    } else {
                        // Nonce 가 일치하지 않으면 인증 실패 > 403 Forbidden 으로 응답
                        response = messageFactory.createResponse(403, request);
                        sipProvider.sendResponse(response);
                        logger.warn("Fail to authenticate. Send 403 Forbidden for the register. (recvNonce={}, userNonce={})", responseNonce, userNonce);
                        return;
                    }
                }

                RegiManager.getInstance().scheduleRegi(fromNo, expires);

                logger.debug("Send 200 OK for REGISTER (FromNo={}): {}", fromNo, response);

                if (FrameManager.getInstance().processRegisterToFrame(ServiceManager.CLIENT_FRAME_NAME, fromNo)) {
                    logger.debug("Success to process the register to [{}] frame. (fromNo={})", ServiceManager.CLIENT_FRAME_NAME, fromNo);
                }

                logger.debug("Success to register. (fromNo={})", fromNo);
            } else {
                String fromSipIp = parseSipIp(fromHeader);
                int fromSipPort = parseSipPort(fromHeader);

                RegiManager.getInstance().addRegi(fromNo, fromSipIp, fromSipPort, expires);

                response = messageFactory.createResponse(401, request);

                WWWAuthenticateHeader wwwAuthenticateHeader = this.headerFactory.createWWWAuthenticateHeader(hostName);
                wwwAuthenticateHeader.setAlgorithm(AUTHENTICATION_ALGORITHM);
                wwwAuthenticateHeader.setRealm(hostName);
                wwwAuthenticateHeader.setNonce(NonceGenerator.createRandomNonce());
                response.addHeader(wwwAuthenticateHeader);

                // MD5 Hashing
                messageDigest.update(fromNo.getBytes(StandardCharsets.UTF_8));
                messageDigest.update(hostName.getBytes(StandardCharsets.UTF_8));
                messageDigest.update(md5PassWd.getBytes(StandardCharsets.UTF_8));
                byte[] a1 = messageDigest.digest();
                messageDigest.reset();

                String requestUriScheme = request.getRequestURI().getScheme();
                messageDigest.update(Request.REGISTER.getBytes(StandardCharsets.UTF_8));
                messageDigest.update(requestUriScheme.getBytes(StandardCharsets.UTF_8));
                byte[] a2 = messageDigest.digest();
                messageDigest.reset();

                messageDigest.update(a1);
                messageDigest.update(a2);
                userNonce = new String(messageDigest.digest());
                logger.debug("Send 401 UNAUTHORIZED for REGISTER (fromNo={}): {}", fromNo, response);
            }

            sipProvider.sendResponse(response);
        } catch (Exception e) {
            logger.warn("Fail to send the response for the REGISTER request.", e);
        }
    }

    /////////////////////////////////////////////////////////////////////
    // INVITE

    /**
     * @fn public void sendInvite (String sessionId, String fromHostName, String toHostName, String toIp, int toPort)
     * @brief INVITE 요청을 보내는 함수
     * @param sessionId Session Id (일회성)
     * @param fromHostName From Host name
     * @param toHostName To Host name
     * @param toIp Host sip ip
     * @param toPort Host sip port
     */
    public void sendInvite (String sessionId, String fromHostName, String toHostName, String toIp, int toPort) {
        if (sessionId == null || toHostName == null || toIp == null || toPort <= 0) {
            logger.warn("Fail to send the invite request. (sessionId={}, toHostName={}, toIP={}, toPort={})", sessionId, toHostName, toIp, toPort);
            return;
        }

        try {
            ConfigManager configManager = AppInstance.getInstance().getConfigManager();

            // Create SIP URI
            SipURI toSipUri = addressFactory.createSipURI(toHostName, toIp);
            toSipUri.setHost(toIp);
            toSipUri.setPort(toPort);
            toSipUri.setLrParam();

            // Add Route Header
            Address toAddress = addressFactory.createAddress(toHostName, toSipUri);
            // Create the request URI for the SIP message
            URI requestURI = toAddress.getURI();

            // Create the SIP message headers
            // The " Via" headers
            List<ViaHeader> viaHeaders = new ArrayList<>();
            ViaHeader viaHeader = this.headerFactory.createViaHeader (hostIp , hostPort, SIP_TRANSPORT_TYPE, null);
            viaHeaders.add(viaHeader);

            // The "Max - Forwards " header
            MaxForwardsHeader maxForwardsHeader = this.headerFactory.createMaxForwardsHeader(70);

            // The "Call -Id" header
            CallIdHeader callIdHeader = this.sipProvider.getNewCallId();

            // The " CSeq " header
            CSeqHeader cSeqHeader = this.headerFactory.createCSeqHeader(1L,Request.INVITE);

            // The " From " header
            Address fromAddress = hostAddress;
            if (configManager.isProxyMode()) {
                SipURI sipUri = addressFactory.createSipURI(fromHostName, hostIp);
                sipUri.setHost(hostIp);
                sipUri.setPort(hostPort);
                sipUri.setLrParam();
                fromAddress = addressFactory.createAddress(fromHostName, sipUri);
            }
            FromHeader fromHeader = this.headerFactory.createFromHeader(fromAddress, String.valueOf(random.nextInt(10000)));

            // The "To" header
            ToHeader toHeader = this.headerFactory.createToHeader(toAddress, null);

            CallInfo callInfo = CallManager.getInstance().addCallInfo(
                    sessionId,
                    callIdHeader.getCallId(),
                    fromHeader.getAddress().getDisplayName(),
                    hostIp,
                    hostPort,
                    toHeader.getAddress().getDisplayName(),
                    toIp,
                    toPort
            );

            String callId = callInfo.getCallId();

            if (configManager.isProxyMode()) {
                AudioMixManager.getInstance().addAudioMixer(
                        callInfo.getSessionId(),
                        callId,
                        configManager.getRecordPath() + File.separator + "V_" + callInfo.getSessionId() + "_mix.wav",
                        //Integer.parseInt(MediaManager.getInstance().getPriorityCodecSamplingRate()),
                        MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_WB)? 16000 : 8000,
                        16,
                        (short) 1
                );
            }

            // Create the REGISTER request
            Request request = this.messageFactory.createRequest (
                    requestURI,
                    Request.INVITE,
                    callIdHeader,
                    cSeqHeader,
                    fromHeader,
                    toHeader,
                    viaHeaders,
                    maxForwardsHeader
            );

            request.addHeader(contactHeader);

            List userAgentList = new ArrayList();
            userAgentList.add(getRandomStr(6));
            UserAgentHeader userAgentHeader = headerFactory.createUserAgentHeader(userAgentList);
            request.addHeader(userAgentHeader);

            // SDP
            int listenPort;
            NettyChannel nettyChannel;
            if (configManager.isUseClient()) {
                NettyChannelManager.getInstance().start();
                nettyChannel = NettyChannelManager.getInstance().getClientChannel();
                listenPort = nettyChannel.getListenPort();
            } else {
                //if (!configManager.isRelay()) {
                if (NettyChannelManager.getInstance().addProxyChannel(callId)) {
                    nettyChannel = NettyChannelManager.getInstance().getProxyChannel(callId);
                    listenPort = nettyChannel.getListenPort();
                } else {
                    logger.warn("Fail to send invite. (callId={})", callId);
                    return;
                }
                /*} else {
                    listenPort = configManager.getNettyServerPort();
                }*/
            }

            ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("application", "sdp");
            Sdp localSdp = SignalManager.getInstance().getLocalSdp();
            localSdp.setMediaPort(Sdp.AUDIO, listenPort);
            byte[] contents = localSdp.getData(false).getBytes();
            request.setContent(contents, contentTypeHeader);

            // Create new client transaction & send the invite request
            ClientTransaction clientTransaction = sipProvider.getNewClientTransaction(request);
            clientTransaction.sendRequest();
            logger.debug("INVITE Request sent. (request={})", request);

            FrameManager.getInstance().getFrame(ServiceManager.CLIENT_FRAME_NAME).appendText("Invite to [" + callInfo.getToNo() + "].\n");

            // Set dialog option
            Dialog dialog = clientTransaction.getDialog();
            if (dialog == null) {
                dialog = sipProvider.getNewDialog(clientTransaction);
            }
            dialog.terminateOnBye(true);

            // Set Call flags
            callInfo.setCallIdHeader(callIdHeader);
            callInfo.setIsCallCanceled(false);
            callInfo.setIsCallStarted(true);

            // Schedule call cancel handler
            String callCancelHandlerId = CallCancelHandler.class.getSimpleName() + callId;
            TaskManager.getInstance().addTask(
                    callCancelHandlerId,
                    new CallCancelHandler(
                            callId,
                            toHostName,
                            toIp,
                            toPort,
                            configManager.getCallRecvDuration()
                    )
            );
            callInfo.setCallCancelHandlerId(callCancelHandlerId);
        }
        catch ( Exception e ) {
            logger.warn("INVITE Request sent failed.", e);
        }
    }

    /**
     * @fn public void processInvite(RequestEvent requestEvent)
     * @brief INVITE 요청을 수신하여 처리하는 함수
     * @param requestEvent 요청 이벤트
     */
    public void processInvite(RequestEvent requestEvent) {
        try {
            ConfigManager configManager = AppInstance.getInstance().getConfigManager();
            boolean isUseClient = configManager.isUseClient();
            boolean isProxyMode = configManager.isProxyMode();

            // Get request
            Request request = requestEvent.getRequest();
            if (request == null) { return; }

            // Call-Id
            CallIdHeader callIdHeader = (CallIdHeader) request.getHeader(CallIdHeader.NAME);
            if (callIdHeader == null) { return; }

            // Via
            ViaHeader viaHeader = (ViaHeader) request.getHeader(ViaHeader.NAME);

            // From
            FromHeader inviteFromHeader = (FromHeader) request.getHeader(FromHeader.NAME);
            String fromNo = inviteFromHeader.getAddress().getDisplayName();

            // To
            ToHeader inviteToHeader = (ToHeader) request.getHeader(ToHeader.NAME);
            String toNo = inviteToHeader.getAddress().getDisplayName();

            String callId = callIdHeader.getCallId();
            logger.debug("Recv INVITE: {} (callId={})", request, callId);

            // 프록시 입장에서 Ingoing invite request 먼저 수신
            // Register 등록 여부 검사 (Server 인 경우만 실행)
            if (isProxyMode) {
                RegiInfo regiInfo = RegiManager.getInstance().getRegi(fromNo);

                // 미등록 > 401 Unauthorized 로 호 거절
                if (regiInfo == null) {
                    Response response = messageFactory.createResponse(401, request);
                    sipProvider.sendResponse(response);

                    logger.warn("Unauthorized user is detected. Fail to process the invite request. (callId={})", callId);
                    return;
                }

                // Remote host 등록 여부 검사
                if (!toNo.equals(configManager.getHostName())) {
                    if (toNo.equals(fromNo)
                            || RegiManager.getInstance().getRegi(toNo) == null) {
                        Response response = messageFactory.createResponse(403, request);
                        sipProvider.sendResponse(response);

                        logger.warn("403 Forbidden. Fail to process the invite request. (toNo={}, callId={})", toNo, callId);
                        return;
                    }
                }

                if (CallManager.getInstance().getCallMapSize() == 0) {
                    sessionId = NonceGenerator.createRandomNonce();
                }
            }
            //

            String sipIp = parseSipIp(inviteFromHeader);
            int sipPort = parseSipPort(inviteFromHeader);

            CallInfo callInfo = CallManager.getInstance().addCallInfo(
                    sessionId,
                    callId,
                    fromNo,
                    hostIp,
                    hostPort,
                    toNo,
                    sipIp,
                    sipPort
            );

            if (isProxyMode) {
                AudioMixManager.getInstance().addAudioMixer(
                        callInfo.getSessionId(),
                        callId,
                        configManager.getRecordPath() + File.separator + "V_" + callInfo.getSessionId() + "_mix.wav",
                        //Integer.parseInt(MediaManager.getInstance().getPriorityCodecSamplingRate()),
                        MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_WB)? 16000 : 8000,
                        16,
                        (short) 1
                );
            }

            callInfo.setFirstViaHeader(
                    headerFactory.createViaHeader(
                            viaHeader.getHost(),
                            viaHeader.getPort(),
                            viaHeader.getTransport(),
                            viaHeader.getBranch()
                    )
            );
            callInfo.setCallIdHeader(callIdHeader);
            callInfo.setInviteRequest(request);

            // Get Server transaction
            ServerTransaction serverTransaction = requestEvent.getServerTransaction();
            if (serverTransaction == null) {
                serverTransaction = sipProvider.getNewServerTransaction(request);
            }
            callInfo.setInviteServerTransaction(serverTransaction);

            // Dialog setting > terminate on bye
            Dialog dialog = serverTransaction.getDialog();
            if (dialog == null) {
                dialog = sipProvider.getNewDialog(serverTransaction);
            }
            dialog.terminateOnBye(true);

            // Get sdp
            byte[] rawSdpData = request.getRawContent();
            if (rawSdpData != null) {
                SdpParser sdpParser = new SdpParser();
                Sdp sdp = sdpParser.parseSdp(callId, new String(rawSdpData));
                CallManager.getInstance().addSdpIntoCallInfo(callId, sdp);
            }

            if (isUseClient) {
                callInfo.setIsCallRecv(true);
                if (FrameManager.getInstance().processInviteToFrame(ServiceManager.CLIENT_FRAME_NAME, fromNo)) {
                    logger.debug("Success to process the invite request to [{}] frame. (callId={}, remoteHostName={})", ServiceManager.CLIENT_FRAME_NAME, callId, fromNo);
                }
            }

            FrameManager.getInstance().getFrame(ServiceManager.CLIENT_FRAME_NAME).appendText("Invite from [" + callInfo.getFromNo() + "].\n");

            // 1) Send 100 Trying
            Response tryingResponse = messageFactory.createResponse(Response.TRYING, request);
            serverTransaction.sendResponse(tryingResponse);
            logger.debug("Send 100 Trying for INVITE: {}", tryingResponse);

            // 2) Send 180 Ringing
            Response ringingResponse = messageFactory.createResponse(Response.RINGING, request);
            ringingResponse.addHeader(contactHeader);

            ToHeader ringingToHeader = (ToHeader) ringingResponse.getHeader(ToHeader.NAME);
            String toTag = Integer.toString(random.nextInt(10000));
            ringingToHeader.setTag(toTag); // Application is supposed to set.

            serverTransaction.sendResponse(ringingResponse);
            logger.debug("Send 180 Ringing for INVITE: {}", ringingResponse);

            // Proxy mode 이고, Proxy 로 직접 호 연결 시도가 감지되면, Group Call 을 시작한다. > Session Room 생성
            if (isProxyMode) {
                if (toNo.equals(configManager.getHostName())) {
                    logger.debug("Group Call({}) has started by [{}]", callInfo.getSessionId(), fromNo);

                    // 1) Group Call
                    // Proxy 에서 Ingoing invite 에 대한 200 OK 바로 전송
                    if (sendInviteOk(callId)) {
                        GroupCallManager.getInstance().addRoomInfo(
                                callInfo.getSessionId(),
                                callId
                        );
                    }
                } else {
                    logger.debug("Relay Call({}) has started by [{}]", callInfo.getSessionId(), fromNo);

                    // 2) Relay Call
                    // 프록시 입장에서 Ingoing invite 에 대한 100, 180 응답 전송 후, Outgoing invite 전송
                    // Send the outgoing invite request to remote host
                    RegiInfo remoteRegiInfo = RegiManager.getInstance().getRegi(toNo);
                    if (remoteRegiInfo == null) {
                        // Remote Call Info 가 없으면 ingoing peer 로 cancel 전송
                        logger.warn("Fail to find the remote peer info. Send the cancel request to ingoing peer. (callId={})", callId);
                        sendCancel(
                                callId,
                                fromNo,
                                sipIp,
                                sipPort
                        );
                        return;
                    }

                    sendInvite(
                            callInfo.getSessionId(),
                            fromNo,
                            remoteRegiInfo.getFromNo(),
                            remoteRegiInfo.getIp(),
                            remoteRegiInfo.getPort()
                    );
                }
            }

            // 클라이언트 입장에서 자동 호 수락 옵션이 켜져있으면, 상대방 UA (프록시) 로 200 OK 를 바로 전송한다.
            if (isUseClient && configManager.isCallAutoAccept()) {
                if (sendInviteOk(callId)) {
                    if (FrameManager.getInstance().processAutoInviteToFrame(ServiceManager.CLIENT_FRAME_NAME, fromNo)) {
                        logger.debug("Success to process the auto-invite to [{}] frame. (callId={})", ServiceManager.CLIENT_FRAME_NAME, callId);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Fail to process INVITE.", e);
        }
    }

    /**
     * @fn public boolean sendInviteOk()
     * @brief Call 유입 시 승인하기 위해 200 OK 를 보내는 함수
     * @return 성공 시 true, 실패 시 false 반환
     */
    public boolean sendInviteOk(String callId) {
        try {
            CallInfo callInfo = CallManager.getInstance().getCallInfo(callId);
            if (callInfo == null) {
                logger.warn("({}) Fail to send the invite 200 OK. Not found the callInfo.", callId);
                return false;
            }

            if (callInfo.getInviteRequest() == null || callInfo.getInviteServerTransaction() == null) {
                logger.warn("({}) Fail to send 200 OK for INVITE. Not found the invite server transaction.", callId);
                return false;
            }

            if (callInfo.getIsCallCanceled()) {
                logger.warn("({}) Call is canceled. Fail to send the invite 200 ok.", callId);
                return false;
            }

            int listenPort;
            NettyChannel nettyChannel;
            ConfigManager configManager = AppInstance.getInstance().getConfigManager();
            if (configManager.isProxyMode()) {
                //if (!configManager.isRelay()) {
                if (NettyChannelManager.getInstance().addProxyChannel(callId)) {
                    nettyChannel = NettyChannelManager.getInstance().getProxyChannel(callId);
                    listenPort = nettyChannel.getListenPort();
                } else {
                    logger.warn("({}) Fail to send invite 200 ok.", callId);
                    sendCancel(
                            callId,
                            callInfo.getFromNo(),
                            callInfo.getFromSipIp(),
                            callInfo.getFromSipPort()
                    );
                    return false;
                }
                /*} else {
                    listenPort = configManager.getNettyServerPort();
                }*/
            } else {
                NettyChannelManager.getInstance().start();
                nettyChannel = NettyChannelManager.getInstance().getClientChannel();
                listenPort = nettyChannel.getListenPort();
            }

            // Send 200 OK
            Response okResponse = messageFactory.createResponse(Response.OK, callInfo.getInviteRequest());
            okResponse.addHeader(contactHeader);

            byte[] rawSdpData = callInfo.getInviteRequest().getRawContent();
            if (rawSdpData != null) {
                SdpParser sdpParser = new SdpParser();
                Sdp remoteSdp = sdpParser.parseSdp(callId, new String(rawSdpData));
                Sdp localSdp = SignalManager.getInstance().getLocalSdp();
                localSdp.setMediaPort(Sdp.AUDIO, listenPort);

                if (remoteSdp != null) {
                    if (remoteSdp.intersect(Sdp.AUDIO, SignalManager.getInstance().getLocalSdp())) {
                        List<RtpAttribute> otherSdpCodecList = remoteSdp.getMediaDescriptionFactory().getIntersectedCodecList(Sdp.AUDIO);
                        String remoteCodec = otherSdpCodecList.get(0).getRtpMapAttributeFactory().getCodecName();
                        String localCodec = MediaManager.getInstance().getPriorityCodec();
                        logger.debug("({}) RemoteCodec: {}, LocalCodec: {}", callId, remoteCodec, localCodec);

                        if (!localCodec.equals(remoteCodec)) {
                            logger.debug("({}) Send CANCEL to remote call.", callId);
                            if (FrameManager.getInstance().processByeToFrame(ServiceManager.CLIENT_FRAME_NAME)) {
                                logger.debug("Success to process the cancel request to [{}] frame. (callId={})", ServiceManager.CLIENT_FRAME_NAME, callId);
                            }
                            sendCancel(callId, callInfo.getToNo(), callInfo.getToSipIp(), callInfo.getToSipPort());
                            return false;
                        }
                    }
                }

                byte[] contents = localSdp.getData(false).getBytes();
                ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("application", "sdp");
                okResponse.setContent(contents, contentTypeHeader);

                callInfo.getInviteServerTransaction().sendResponse(okResponse);
                logger.debug("({}) Send 200 OK for INVITE: {}", callId, okResponse);
                callInfo.setIsInviteAccepted(true);
            } else {
                // NO SDP
                logger.debug("({}) NO SDP is detected. Fail to send 200 ok for the invite request.", callId);
                return false;
            }

            if (configManager.isUseClient()) {
                VoipClient.getInstance().start();
            }
        } catch (Exception e) {
            logger.warn("({}) Fail to send 200 OK for the invite request.", callId, e);
        }

        return true;
    }

    /////////////////////////////////////////////////////////////////////
    // ACK

    /**
     * @fn public void sendAck(ResponseEvent responseEvent)
     * @brief ACK 요청을 보내는 함수
     * @param responseEvent 응답 이벤트
     */
    public void sendAck(ResponseEvent responseEvent) {
        CSeqHeader cSeqHeader = (CSeqHeader) responseEvent.getResponse().getHeader(CSeqHeader.NAME);
        if (cSeqHeader != null && cSeqHeader.getMethod().equals(Request.INVITE)) {
            try {
                Request request = responseEvent.getDialog().createAck(cSeqHeader.getSeqNumber());
                responseEvent.getDialog().sendAck(request);
                logger.debug("Send ACK for INVITE: {}", request);
            } catch (Exception e) {
                logger.warn("Fail to send the ACK request for the INVITE 200 OK.", e);
            }
        }
    }

    /**
     * @fn public void processAck(RequestEvent requestEvent)
     * @brief ACK 요청을 처리하는 함수
     * @param requestEvent 요청 이벤트
     */
    public void processAck(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        logger.debug("Recv ACK: {}", request);
    }

    /////////////////////////////////////////////////////////////////////
    // CANCEL

    /**
     * @fn public void sendCancel(String callId, String toHostName, String toIp, int toPort)
     * @brief CANCEL 요청을 보내는 함수
     * @param callId Call-Id
     */
    public void sendCancel(String callId, String toHostName, String toIp, int toPort) {
        CallInfo callInfo = CallManager.getInstance().getCallInfo(callId);
        if (callInfo == null) {
            return;
        }

        if (callInfo.getIsInviteAccepted()) {
            logger.warn("Call is already accepted. Fail to send the cancel request. (callId={})", callId);
            return;
        }

        try {
            // Create SIP URI
            SipURI sipUri = addressFactory.createSipURI(toHostName, toIp);
            sipUri.setHost(toIp);
            sipUri.setPort(toPort);
            sipUri.setLrParam();

            // Add Route Header
            Address addressTo = addressFactory.createAddress(toHostName, sipUri);
            // Create the request URI for the SIP message
            URI requestURI = addressTo.getURI();

            // Create the SIP message headers
            // The " Via" headers
            List<ViaHeader> viaHeaders = new ArrayList<>();
            ViaHeader viaHeader = this.headerFactory.createViaHeader (hostIp , hostPort, SIP_TRANSPORT_TYPE, null);
            viaHeaders.add(viaHeader);

            // The "Max - Forwards " header
            MaxForwardsHeader maxForwardsHeader = this.headerFactory.createMaxForwardsHeader(70);

            // The "Call -Id" header
            CallIdHeader callIdHeader = callInfo.getCallIdHeader();
            if (callIdHeader == null) { return; }

            // The " CSeq " header
            CSeqHeader cSeqHeader = this.headerFactory.createCSeqHeader(1L, Request.CANCEL);

            // The " From " header
            FromHeader fromHeader = this.headerFactory.createFromHeader(hostAddress, String.valueOf(random.nextInt(10000)));

            // The "To" header
            ToHeader toHeader = this.headerFactory.createToHeader(addressTo, String.valueOf(random.nextInt(10000)));

            // The "Reason" header
            ReasonHeader reasonHeader = this.headerFactory.createReasonHeader("SIP", Response.DECLINE, "Decline");

            // Create the BYE request
            Request request = this.messageFactory.createRequest (
                    requestURI,
                    Request.CANCEL,
                    callIdHeader,
                    cSeqHeader,
                    fromHeader,
                    toHeader,
                    viaHeaders,
                    maxForwardsHeader
            );

            request.addHeader(contactHeader);
            request.addHeader(reasonHeader);

            CallManager.getInstance().deleteCallInfo(callId);

            sipProvider.sendRequest(request);
            logger.debug("CANCEL Request sent. (request={})", request);

            ConfigManager configManager = AppInstance.getInstance().getConfigManager();
            if (configManager.isProxyMode()) {
                NettyChannelManager.getInstance().deleteProxyChannel(callId);
                callInfo.setRemoteCallInfo(null);
                AudioMixManager.getInstance().removeAudioMixer(
                        callInfo.getSessionId(),
                        callId
                );

                if (callInfo.getIsRoomEntered()) {
                    GroupCallManager.getInstance().deleteRoomInfo(callInfo.getSessionId(), callId);
                }
            }

            callInfo.setCallIdHeader(null);
            callInfo.setIsInviteAccepted(false);
            callInfo.setIsCallStarted(false);
            callInfo.setIsCallRecv(false);
            callInfo.setIsCallCanceled(true);
        } catch (Exception e) {
            logger.warn("CANCEL Request sent failed.", e);
        }
    }

    /**
     * @fn public void processCancel(RequestEvent requestEvent)
     * @brief CANCEL 요청을 처리하는 함수
     * @param requestEvent 요청 이벤트
     */
    public void processCancel (RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        CallIdHeader callIdHeader = (CallIdHeader) request.getHeader(CallIdHeader.NAME);
        if (callIdHeader == null) { return; }

        String callId = callIdHeader.getCallId();
        CallInfo callInfo = CallManager.getInstance().getCallInfo(callId);
        if (callInfo == null) {
            return;
        }

        FromHeader fromHeader = (FromHeader) request.getHeader(FromHeader.NAME);
        String fromNo = fromHeader.getAddress().getDisplayName();

        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        boolean isUseClient = configManager.isUseClient();
        boolean isProxyMode = configManager.isProxyMode();

        try {
            if (CallManager.getInstance().deleteCallInfo(callId) != null) {
                logger.debug("Recv CANCEL: {}", request);

                if (isProxyMode) {
                    CallInfo remoteCallInfo = CallManager.getInstance().findOtherCallInfo(callInfo.getSessionId(), callId);
                    if (remoteCallInfo != null) {
                        String toHostName = callInfo.getFromNo().equals(configManager.getHostName()) ? remoteCallInfo.getFromNo() : callInfo.getToNo();
                        sendCancel(
                                remoteCallInfo.getCallId(),
                                toHostName,
                                remoteCallInfo.getToSipIp(),
                                remoteCallInfo.getToSipPort()
                        );
                        logger.debug("Send CANCEL to remote call. ({})", remoteCallInfo.getCallId());
                    } else {
                        logger.warn("Fail to send the cancel request to remote call. ({})", callId);
                    }
                }

                FrameManager.getInstance().getFrame(ServiceManager.CLIENT_FRAME_NAME).appendText("Cancel from [" + fromNo + "].\n");

                callInfo.setIsInviteAccepted(false);
                callInfo.setIsCallStarted(false);
                callInfo.setIsCallCanceled(true);
                callInfo.setIsCallRecv(false);

                Response response = messageFactory.createResponse(200, request);
                sipProvider.sendResponse(response);
                logger.debug("Send 200 OK for CANCEL: {}", response);

                if (isUseClient) {
                    if (FrameManager.getInstance().processByeToFrame(ServiceManager.CLIENT_FRAME_NAME)) {
                        logger.debug("Success to process the cancel request to [{}] frame. (callId={})", ServiceManager.CLIENT_FRAME_NAME, callId);
                    }
                }

                if (configManager.isProxyMode()) {
                    NettyChannelManager.getInstance().deleteProxyChannel(callId);
                    callInfo.setRemoteCallInfo(null);
                    AudioMixManager.getInstance().removeAudioMixer(
                            callInfo.getSessionId(),
                            callId
                    );

                    if (callInfo.getIsRoomEntered()) {
                        GroupCallManager.getInstance().deleteRoomInfo(callInfo.getSessionId(), callId);
                    }
                }

                send487(callId);
            }
        } catch (Exception e) {
            logger.warn("Fail to send the 200 OK response for the CANCEL request. (callId={})", callId, e);
        }
    }

    /////////////////////////////////////////////////////////////////////
    // BYE

    /**
     * @fn public void sendBye(String callId, String toHostName, String toIp, int toPort)
     * @brief BYE 요청을 보내는 함수
     * @param callId Call-Id
     */
    public void sendBye(String callId, String toHostName, String toIp, int toPort) {
        CallInfo callInfo = CallManager.getInstance().getCallInfo(callId);
        if (callInfo == null) {
            return;
        }

        if (!callInfo.getIsInviteAccepted()) {
            logger.warn("Call is not accepted. Fail to send the bye request. (callId={})", callId);
            return;
        }

        try {
            // Create SIP URI
            SipURI sipUri = addressFactory.createSipURI(toHostName, toIp);
            sipUri.setHost(toIp);
            sipUri.setPort(toPort);
            sipUri.setLrParam();

            // Add Route Header
            Address addressTo = addressFactory.createAddress(toHostName, sipUri);
            // Create the request URI for the SIP message
            URI requestURI = addressTo.getURI();

            // Create the SIP message headers
            // The " Via" headers
            List<ViaHeader> viaHeaders = new ArrayList<>();
            ViaHeader viaHeader = this.headerFactory.createViaHeader (hostIp , hostPort, SIP_TRANSPORT_TYPE, null);
            viaHeaders.add(viaHeader);

            // The "Max - Forwards " header
            MaxForwardsHeader maxForwardsHeader = this.headerFactory.createMaxForwardsHeader(70);

            // The "Call -Id" header
            CallIdHeader callIdHeader = callInfo.getCallIdHeader();
            if (callIdHeader == null) { return; }

            // The " CSeq " header
            CSeqHeader cSeqHeader = this.headerFactory.createCSeqHeader(1L, Request.BYE);

            // The " From " header
            FromHeader fromHeader = this.headerFactory.createFromHeader(hostAddress, String.valueOf(random.nextInt(10000)));

            // The "To" header
            ToHeader toHeader = this.headerFactory.createToHeader(addressTo, String.valueOf(random.nextInt(10000)));

            // Create the BYE request
            Request request = this.messageFactory.createRequest (
                    requestURI,
                    Request.BYE,
                    callIdHeader,
                    cSeqHeader,
                    fromHeader,
                    toHeader,
                    viaHeaders,
                    maxForwardsHeader
            );

            CallManager.getInstance().deleteCallInfo(callId);

            request.addHeader(contactHeader);

            ClientTransaction clientTransaction = sipProvider.getNewClientTransaction(request);
            clientTransaction.sendRequest();

            ConfigManager configManager = AppInstance.getInstance().getConfigManager();
            if (configManager.isProxyMode()) {
                NettyChannelManager.getInstance().deleteProxyChannel(callId);
                callInfo.setRemoteCallInfo(null);
                AudioMixManager.getInstance().removeAudioMixer(
                        callInfo.getSessionId(),
                        callId
                );

                if (callInfo.getIsRoomEntered()) {
                    GroupCallManager.getInstance().deleteRoomInfo(callInfo.getSessionId(), callId);
                }
            } else {
                NettyChannelManager.getInstance().stop();
                VoipClient.getInstance().stop();
            }

            if (configManager.isUseClient()) {
                if (FrameManager.getInstance().processByeToFrame(ServiceManager.CLIENT_FRAME_NAME)) {
                    logger.debug("Success to process the bye request to [{}] frame. (callId={})", ServiceManager.CLIENT_FRAME_NAME, callId);
                }
            }

            callInfo.setCallIdHeader(null);
            callInfo.setIsInviteAccepted(false);
            callInfo.setIsCallStarted(false);
            callInfo.setIsCallRecv(false);
            logger.debug("BYE Request sent. (request={})", request);
        } catch (Exception e) {
            logger.warn("BYE Request sent failed.", e);
        }
    }

    /**
     * @fn public void processBye(RequestEvent requestEvent)
     * @brief BYE 요청을 처리하는 함수
     * @param requestEvent 요청 이벤트
     */
    public void processBye (RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        CallIdHeader callIdHeader = (CallIdHeader) request.getHeader(CallIdHeader.NAME);
        if (callIdHeader == null) { return; }

        String callId = callIdHeader.getCallId();
        CallInfo callInfo = CallManager.getInstance().getCallInfo(callId);
        if (callInfo == null) {
            return;
        }

        FromHeader fromHeader = (FromHeader) request.getHeader(FromHeader.NAME);
        String fromNo = fromHeader.getAddress().getDisplayName();

        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        boolean isUseClient = configManager.isUseClient();
        boolean isProxyMode = configManager.isProxyMode();

        CallInfo remoteCallInfo = callInfo.getRemoteCallInfo();

        try {
            if (CallManager.getInstance().deleteCallInfo(callId) != null) {
                logger.debug("Recv BYE: {}", request);

                if (isProxyMode) {
                    NettyChannelManager.getInstance().deleteProxyChannel(callId);
                    callInfo.setRemoteCallInfo(null);
                    AudioMixManager.getInstance().removeAudioMixer(
                            callInfo.getSessionId(),
                            callId
                    );

                    if (callInfo.getIsRoomEntered()) {
                        GroupCallManager.getInstance().deleteRoomInfo(callInfo.getSessionId(), callId);
                    } else {
                        if (remoteCallInfo != null){
                            String toHostName = callInfo.getFromNo().equals(configManager.getHostName()) ? remoteCallInfo.getFromNo() : callInfo.getToNo();
                            sendBye(remoteCallInfo.getCallId(), toHostName, remoteCallInfo.getToSipIp(), remoteCallInfo.getToSipPort());
                            logger.debug("Send BYE to remote call. ({})", remoteCallInfo.getCallId());
                        } else{
                            logger.warn("Fail to send the bye request to remote call. ({})", callId);
                        }
                    }
                } else {
                    NettyChannelManager.getInstance().stop();
                    VoipClient.getInstance().stop();
                }

                FrameManager.getInstance().getFrame(ServiceManager.CLIENT_FRAME_NAME).appendText("Bye from [" + fromNo + "].\n");

                callInfo.setIsInviteAccepted(false);
                callInfo.setIsCallStarted(false);
                callInfo.setIsCallRecv(false);

                Response response = messageFactory.createResponse(200, request);
                sipProvider.sendResponse(response);
                logger.debug("Send 200 OK for BYE: {}", response);

                if (isUseClient) {
                    if (FrameManager.getInstance().processByeToFrame(ServiceManager.CLIENT_FRAME_NAME)) {
                        logger.debug("Success to process the bye request to [{}] frame. (callId={})", ServiceManager.CLIENT_FRAME_NAME, callId);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Fail to send the 200 OK response for the BYE request. (callId={})", callId, e);
        }
    }

    /////////////////////////////////////////////////////////////////////

    public void send487(String callId) {
        CallInfo callInfo = CallManager.getInstance().getCallInfo(callId);
        if (callInfo == null) {
            return;
        }

        if (callInfo.getInviteRequest() == null || callInfo.getInviteServerTransaction() == null) {
            logger.warn("Fail to send 487 Request Terminated for INVITE.");
            return;
        }

        try {
            // Send 487 Request Terminated
            Response okResponse = messageFactory.createResponse(Response.REQUEST_TERMINATED, callInfo.getInviteRequest());
            callInfo.getInviteServerTransaction().sendResponse(okResponse);
            logger.debug("Send 487 Request Terminated for INVITE: {}", okResponse);
        } catch (Exception e) {
            logger.warn("Fail to send 487 Request Terminated for the invite request.", e);
        }
    }

    /////////////////////////////////////////////////////////////////////
    // MESSAGE

    /**
     * @fn public void processMessage(RequestEvent requestEvent)
     * @brief MESSAGE 요청을 처리하는 함수
     * @param requestEvent 요청 이벤트
     */
    public void processMessage(RequestEvent requestEvent) {
        ServerTransaction serverTransaction = requestEvent.getServerTransaction();
        Request request = requestEvent.getRequest();

        CallIdHeader callIdHeader = (CallIdHeader) request.getHeader(CallIdHeader.NAME);
        if (callIdHeader == null) { return; }

        String callId = callIdHeader.getCallId();
        CallInfo callInfo = CallManager.getInstance().getCallInfo(callId);
        if (callInfo == null) {
            return;
        }

        logger.debug("Recv Message: {}", request);

        try {
            Response response = messageFactory.createResponse(200, request);
            serverTransaction.sendResponse(response);
            logger.debug("Send 200 OK for MESSAGE: {}", response);

            ContentDispositionHeader contentDispositionHeader = (ContentDispositionHeader) request.getHeader(ContentDispositionHeader.NAME);
            if (contentDispositionHeader != null) {
                FrameManager.getInstance().getFrame(ServiceManager.CLIENT_FRAME_NAME).appendText("[" + contentDispositionHeader.getName() + "]\n");
            }
        } catch (Exception e) {
            logger.warn("Fail to send the 200 OK response for the MESSAGE request.", e);
        }
    }

    /////////////////////////////////////////////////////////////////////
    // 200 OK

    /**
     * @fn public void processOk (ResponseEvent responseEvent)
     * @brief 200 OK 응답을 처리하는 함수
     * @param responseEvent 응답 이벤트
     */
    public void processOk (ResponseEvent responseEvent) {
        Response response = responseEvent.getResponse();

        CallIdHeader callIdHeader = (CallIdHeader) response.getHeader(CallIdHeader.NAME);
        if (callIdHeader == null) { return; }

        CSeqHeader cSeqHeader = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
        if (cSeqHeader == null) { return; }

        String methodName = cSeqHeader.getMethod();
        String callId = callIdHeader.getCallId();

        CallInfo callInfo = null;

        // REGISTER 아닌 경우에만 CallInfo 처리
        if (!methodName.equals(Request.REGISTER)) {
            callInfo = CallManager.getInstance().getCallInfo(callId);
            if (callInfo == null) {
                return;
            }
        }

        try {
            ConfigManager configManager = AppInstance.getInstance().getConfigManager();
            boolean isUseClient = configManager.isUseClient();

            // INVITE 200 OK 처리
            if (methodName.equals(Request.INVITE)) {
                if (callInfo.getIsCallCanceled()) {
                    logger.warn("({}) Call is canceled. Fail to process the invite 200 ok.", callId);
                    return;
                }

                callInfo.setIsInviteAccepted(true);
                callInfo.setIsInviteUnauthorized(false);

                byte[] rawSdpData = response.getRawContent();
                if (rawSdpData != null) {
                    try {
                        SdpParser sdpParser = new SdpParser();
                        Sdp remoteSdp = sdpParser.parseSdp(callId, new String(rawSdpData));

                        // 우선 순위 코덱 일치 확인
                        if (remoteSdp != null) {
                            if (remoteSdp.intersect(Sdp.AUDIO, SignalManager.getInstance().getLocalSdp())) {
                                List<RtpAttribute> otherSdpCodecList = remoteSdp.getMediaDescriptionFactory().getIntersectedCodecList(Sdp.AUDIO);
                                String remoteCodec = otherSdpCodecList.get(0).getRtpMapAttributeFactory().getCodecName();
                                String localCodec = MediaManager.getInstance().getPriorityCodec();
                                logger.debug("({}) RemoteCodec: {}, LocalCodec: {}", callId, remoteCodec, localCodec);

                                if (localCodec.equals(remoteCodec)) {
                                    CallManager.getInstance().addSdpIntoCallInfo(callId, remoteSdp);
                                } else {
                                    sendAck(responseEvent);
                                    sendBye(callId, callInfo.getToNo(), callInfo.getToSipIp(), callInfo.getToSipPort());
                                    logger.debug("({}) Send BYE to remote call.", callId);

                                    if (FrameManager.getInstance().processByeToFrame(ServiceManager.CLIENT_FRAME_NAME)) {
                                        logger.debug("({}) Success to process the bye request to [{}] frame.", callId, ServiceManager.CLIENT_FRAME_NAME);
                                    }
                                    return;
                                }
                            }
                        }

                        if (configManager.isProxyMode()) {
                            if (!callInfo.getIsRoomEntered()) {
                                // Set remote call info > 프록시만 설정 : from mdn callInfo 에 to mdn callInfo 를 remote 로 설정
                                CallInfo remoteCallInfo = CallManager.getInstance().findOtherCallInfo(callInfo.getSessionId(), callId);
                                if (remoteCallInfo != null) {
                                    if (sendInviteOk(remoteCallInfo.getCallId())) {
                                        callInfo.setRemoteCallInfo(remoteCallInfo);
                                        remoteCallInfo.setRemoteCallInfo(callInfo);
                                        logger.warn("({}) Success to set the remote peer and send the invite 200 ok response. (remoteCallId={})", callId, remoteCallInfo.getCallId());
                                    }
                                } else {
                                    logger.warn("({}) Fail to set the remote peer and send the invite 200 ok response.", callId);
                                }
                            }
                        } else {
                            VoipClient.getInstance().start();
                        }

                        // INVITE 200 OK 인 경우 ACK 전송
                        sendAck(responseEvent);
                    } catch (Exception e) {
                        logger.warn("({}) Fail to process the invite 200 OK.", callId, e);
                    }
                }
            } else if (methodName.equals(Request.REGISTER)) {
                // 클라이언트인 경우에만 REGISTER 200 OK 수신
                if (isUseClient) {
                    FromHeader inviteFromHeader = (FromHeader) response.getHeader(FromHeader.NAME);
                    String fromNo = inviteFromHeader.getAddress().getDisplayName();

                    if (FrameManager.getInstance().processRegisterToFrame(ServiceManager.CLIENT_FRAME_NAME, fromNo)) {
                        logger.debug("({}) Success to process the register to [{}] frame.", ServiceManager.CLIENT_FRAME_NAME, callId);
                    }

                    logger.debug("({}) Success to register. (mdn={})", callId, fromNo);
                }
            }
        } catch (Exception e) {
            logger.warn("({}) Fail to process the 200 OK response for the {} request", callId, methodName, e);
        }
    }

    private String getRandomStr(int size) {
        if(size > 0) {
            char[] tmp = new char[size];
            for(int i=0; i<tmp.length; i++) {
                int div = (int) Math.floor( Math.random() * 2 );
                if(div == 0) { // 0이면 숫자로
                    tmp[i] = (char) (Math.random() * 10 + '0') ;
                }else { //1이면 알파벳
                    tmp[i] = (char) (Math.random() * 26 + 'A') ;
                }
            }
            return new String(tmp);
        }

        return null;
    }

}