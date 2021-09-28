# VOIP_Phone
- Voice Over Internet Protocol Phone : IP 네트워크를 사용한 음성 통화 프로그램
- L3 : IP / L4 : UDP / L7 : SIP, SDP, RTP
- 지원 코덱 : ALAW(8000), ULAW(8000), AMR-NB(8000), AMR-WB(16000), EVS(8000)

## 1) 기능
### 1) 양자 통화
#### 1-1) End to End : 프록시 경유해서 통화
#### 1-2) Peer to Peer : 피어끼리 직접 통화
### 2) 그룹 통화
#### 2-1) 프록시에 첫 번째로 통화 시도한 참여자가 대화방을 생성
#### 2-2) 대화방 생성 후 다른 참여자가 프록시에 통화 시도하여 해당 대화방에 참여
#### 2-3) 모든 참여자가 대화방을 나가면 대화방 자동 삭제
#### 2-4) 아직 참여자 제한 없음, 대화방은 하나만 생성 가능 > 외부 인터페이스에서 제어해야함, SIP 로는 제어 불가능
### 3) 녹취
#### 3-1) 클라이언트
##### 3-1-1) 내 목소리 녹취
##### 3-1-2) 상대방 목소리 녹취
#### 3-2) 프록시
##### 3-2-1) 양자 통화 음성 합성 녹취
##### 3-2-2) 그룹 통화 음성 합성 녹취
#### 4) 프로그램
##### 4-1) 우선순위 코덱 설정
##### 4-2) 스피커, 마이크는 선택된 코덱에 따라 샘플링 레이트 결정 (8000 or 16000)
##### 4-3) 볼륨 조절 (스피커, 마이크)

## 2) 설계
### 2-1) 구성도
![voip_phone_구성도2](https://user-images.githubusercontent.com/37236920/119951330-c9886d80-bfd6-11eb-854a-a683dd7e5718.png)

### 2-2) 정상 호 Flow
![voip_phone_normal_flow](https://user-images.githubusercontent.com/37236920/120750601-99464f00-c541-11eb-901f-7acaecfec2e0.png)

### 2-3) 비정상 호 Flow 1
![voip_phone_abnormal_flow1](https://user-images.githubusercontent.com/37236920/120751081-5cc72300-c542-11eb-8716-84cd3e57b29e.png)

### 2-4) 비정상 호 Flow 2
![voip_phone_abnormal_flow2](https://user-images.githubusercontent.com/37236920/120751099-6781b800-c542-11eb-92fd-c4ca72b73b9e.png)

### 2-5) 비정상 호 Flow 3
![voip_phone_abnormal_flow3](https://user-images.githubusercontent.com/37236920/120751114-6d779900-c542-11eb-9c25-f58c63ab8db9.png)

### 2-6) 비정상 호 Flow 4
![voip_phone_abnormal_flow4](https://user-images.githubusercontent.com/37236920/120751132-749ea700-c542-11eb-9828-0b32efc9c84f.png)

## 3) SIP 메시지 예시
### 3-1) Group Call
#### 3-1-1) Register
##### 3-1-1-1) Register request : Client > proxy
12:09:22.149 [AWT-EventQueue-0] DEBUG signal.module.SipUtil - | REGISTER Request sent. (request=REGISTER sip:v@192.168.2.159:51000;lr SIP/2.0  
Call-ID: 325ab5f96ec6ce7c50832889c35636e4@192.168.2.159  
CSeq: 1 REGISTER  
From: "01012341234" <sip:01012341234@192.168.2.159:52000;lr>;tag=3328  
To: <sip:01012341234@192.168.2.159:52000>  
Via: SIP/2.0/UDP 192.168.2.159:52000;branch=z9hG4bK-343939-9424947dcd4ebc2534b6f5404f57b509  
Max-Forwards: 70  
Allow: INVITE,ACK,CANCEL,BYE,MESSAGE  
Supported: path,gruu,outbound  
Expires: 6000000  
Contact: <sip:01012341234@192.168.2.159:52000>  
Content-Length: 0  
  
  
##### 3-1-1-2) 401 response : Proxy > Client
12:09:22.301 [EventScannerThread] DEBUG signal.module.SipUtil - | Recv REGISTER: REGISTER sip:v@192.168.2.159:51000;lr SIP/2.0  
Call-ID: 325ab5f96ec6ce7c50832889c35636e4@192.168.2.159  
CSeq: 1 REGISTER  
From: "01012341234" <sip:01012341234@192.168.2.159:52000;lr>;tag=3328  
To: <sip:01012341234@192.168.2.159:52000>  
Via: SIP/2.0/UDP 192.168.2.159:52000;branch=z9hG4bK-343939-9424947dcd4ebc2534b6f5404f57b509  
Max-Forwards: 70  
Allow: INVITE,ACK,CANCEL,BYE,MESSAGE  
Supported: path,gruu,outbound  
Expires: 6000000  
Contact: <sip:01012341234@192.168.2.159:52000>  
Content-Length: 0  
  
12:09:22.305 [EventScannerThread] DEBUG signal.module.SipUtil - | Send 401 UNAUTHORIZED for REGISTER (FromNo=01012341234): SIP/2.0 401 Unauthorized  
CSeq: 1 REGISTER  
Call-ID: 325ab5f96ec6ce7c50832889c35636e4@192.168.2.159  
From: "01012341234" <sip:01012341234@192.168.2.159:52000;lr>;tag=3328  
To: <sip:01012341234@192.168.2.159:52000>  
Via: SIP/2.0/UDP 192.168.2.159:52000;branch=z9hG4bK-343939-9424947dcd4ebc2534b6f5404f57b509  
WWW-Authenticate: voip_server algorithm=MD5,realm="voip_server",nonce="2JpQVl8m3iihrRPANslfr2SpVoUZxPWIZ0Jmugt0RNC_87gf4Ze7NRgc8tnRX9QC"  
Content-Length: 0  
  
  
##### 3-1-1-3) Register request : Client > Proxy
12:09:22.330 [EventScannerThread] DEBUG signal.module.SipUtil - | Recv Response: SIP/2.0 401 Unauthorized  
CSeq: 1 REGISTER  
Call-ID: 325ab5f96ec6ce7c50832889c35636e4@192.168.2.159  
From: "01012341234" <sip:01012341234@192.168.2.159:52000;lr>;tag=3328  
To: <sip:01012341234@192.168.2.159:52000>  
Via: SIP/2.0/UDP 192.168.2.159:52000;branch=z9hG4bK-343939-9424947dcd4ebc2534b6f5404f57b509  
WWW-Authenticate: voip_server algorithm=MD5,realm="voip_server",nonce="2JpQVl8m3iihrRPANslfr2SpVoUZxPWIZ0Jmugt0RNC_87gf4Ze7NRgc8tnRX9QC"  
Content-Length: 0  
  
12:09:22.330 [EventScannerThread] DEBUG signal.module.SipUtil - | Recv 401 Unauthorized. Authentication will be processed.  
12:09:22.331 [EventScannerThread] DEBUG signal.module.SipUtil - | REGISTER Request sent. (request=REGISTER sip:v@192.168.2.159:51000;lr SIP/2.0  
Call-ID: 5ddca18e7fa80945cf64d0270c6b9858@192.168.2.159  
CSeq: 1 REGISTER  
From: "01012341234" <sip:01012341234@192.168.2.159:52000;lr>;tag=6136  
To: <sip:01012341234@192.168.2.159:52000>  
Via: SIP/2.0/UDP 192.168.2.159:52000;branch=z9hG4bK-343939-d5cff0717a72ce3d3b6b96deed8d1a24  
Max-Forwards: 70  
Allow: INVITE,ACK,CANCEL,BYE,MESSAGE  
Supported: path,gruu,outbound  
Expires: 6000000  
Contact: <sip:01012341234@192.168.2.159:52000>  
Authorization: 01012341234 username="01012341234",realm="voip_server",nonce="2JpQVl8m3iihrRPANslfr2SpVoUZxPWIZ0Jmugt0RNC_87gf4Ze7NRgc8tnRX9QC",uri="sip:v@192.168.2.159:51000;lr",algorithm=MD5,response="�T�7�R�f��!"  
Content-Length: 0  
  
  
##### 3-1-1-4) 200 ok response : Proxy > Client
12:09:22.334 [EventScannerThread] DEBUG signal.module.SipUtil - | Recv REGISTER: REGISTER sip:v@192.168.2.159:51000;lr SIP/2.0  
Call-ID: 5ddca18e7fa80945cf64d0270c6b9858@192.168.2.159  
CSeq: 1 REGISTER  
From: "01012341234" <sip:01012341234@192.168.2.159:52000;lr>;tag=6136  
To: <sip:01012341234@192.168.2.159:52000>  
Via: SIP/2.0/UDP 192.168.2.159:52000;branch=z9hG4bK-343939-d5cff0717a72ce3d3b6b96deed8d1a24  
Max-Forwards: 70  
Allow: INVITE,ACK,CANCEL,BYE,MESSAGE  
Supported: path,gruu,outbound  
Expires: 6000000  
Contact: <sip:01012341234@192.168.2.159:52000>  
Authorization: 01012341234 username="01012341234",realm="voip_server",nonce="2JpQVl8m3iihrRPANslfr2SpVoUZxPWIZ0Jmugt0RNC_87gf4Ze7NRgc8tnRX9QC",uri="sip:v@192.168.2.159:51000;lr",algorithm=MD5,response="�T�7�R�f��!"  
Content-Length: 0  
  
12:09:22.335 [EventScannerThread] DEBUG service.TaskManager - | Task [RegiDeleteHandler_01012341234] is added. (interval=6000000)  
12:09:22.335 [EventScannerThread] DEBUG signal.module.SipUtil - | Send 200 OK for REGISTER (FromNo=01012341234): SIP/2.0 200 OK  
CSeq: 1 REGISTER  
Call-ID: 5ddca18e7fa80945cf64d0270c6b9858@192.168.2.159  
From: "01012341234" <sip:01012341234@192.168.2.159:52000;lr>;tag=6136  
To: <sip:01012341234@192.168.2.159:52000>  
Via: SIP/2.0/UDP 192.168.2.159:52000;branch=z9hG4bK-343939-d5cff0717a72ce3d3b6b96deed8d1a24  
Content-Length: 0  
  
12:09:22.336 [EventScannerThread] DEBUG signal.module.SipUtil - | Success to process the register to [client] frame. (fromNo=01012341234)  
12:09:22.336 [EventScannerThread] DEBUG signal.module.SipUtil - | Success to register. (fromNo=01012341234)  
  
  
##### 3-1-1-5) 200 ok response : Client
12:09:22.340 [EventScannerThread] DEBUG signal.module.SipUtil - | Recv Response: SIP/2.0 200 OK  
CSeq: 1 REGISTER  
Call-ID: 5ddca18e7fa80945cf64d0270c6b9858@192.168.2.159  
From: "01012341234" <sip:01012341234@192.168.2.159:52000;lr>;tag=6136  
To: <sip:01012341234@192.168.2.159:52000>  
Via: SIP/2.0/UDP 192.168.2.159:52000;branch=z9hG4bK-343939-d5cff0717a72ce3d3b6b96deed8d1a24  
Content-Length: 0  
  
  
#### 3-1-2) Invite
##### 3-1-2-1) Invite request : Client > Proxy
12:09:39.289 [AWT-EventQueue-0] DEBUG signal.module.SipUtil - | INVITE Request sent. (request=INVITE sip:voip_server@192.168.2.159:51000;lr SIP/2.0  
Call-ID: 5c401b5a83cfb87fe2b935be46b66f9b@192.168.2.159  
CSeq: 1 INVITE  
From: "01012341234" <sip:01012341234@192.168.2.159:52000;lr>;tag=6995  
To: "voip_server" <sip:voip_server@192.168.2.159:51000;lr>  
Via: SIP/2.0/UDP 192.168.2.159:52000;branch=z9hG4bK-343939-bf322672efc90cfa49fd10432f3c8b4b  
Max-Forwards: 70  
Contact: <sip:01012341234@192.168.2.159:52000>  
Content-Type: application/sdp  
Content-Length: 117  
  
v=0  
o=- 0 0 IN IP4 192.168.2.159  
s=-  
c=IN IP4 192.168.2.159  
t=0 0  
m=audio 2700 RTP/AVP 8  
a=rtpmap:8 ALAW/8000  
)  
  
  
##### 3-1-2-2) 1xx + 200 ok response : Proxy > Client
12:09:29.332 [EventScannerThread] DEBUG signal.module.SipUtil - | Recv INVITE: INVITE sip:voip_server@192.168.2.159:51000;lr SIP/2.0  
Call-ID: 65418859862d6426061dd057df9b5b73@192.168.2.159  
CSeq: 1 INVITE  
From: "01056785678" <sip:01056785678@192.168.2.159:50000;lr>;tag=1022  
To: "voip_server" <sip:voip_server@192.168.2.159:51000;lr>  
Via: SIP/2.0/UDP 192.168.2.159:50000;branch=z9hG4bK-353434-c85cda0b70db0404cf8b310e44a9bdbf  
Max-Forwards: 70  
Contact: <sip:01056785678@192.168.2.159:50000>  
Content-Type: application/sdp  
Content-Length: 117  
  
v=0  
o=- 0 0 IN IP4 192.168.2.159  
s=-  
c=IN IP4 192.168.2.159  
t=0 0  
m=audio 2500 RTP/AVP 8  
a=rtpmap:8 ALAW/8000  
 (callId=65418859862d6426061dd057df9b5b73@192.168.2.159)  
12:09:29.334 [EventScannerThread] DEBUG signal.base.CallInfo - [CALL] (beA82spdUQmwx86zSv6PWFk9Ay5MTAlmnAWvT40A3nRiu0X-80wubwoukDH8n3xM) (65418859862d6426061dd057df9b5b73@192.168.2.159) (01056785678) (voip_server) CallInfo(callId=65418859862d6426061dd057df9b5b73@192.168.2.159, ip=192.168.2.159, port=50000) is created.  
12:09:29.337 [EventScannerThread] DEBUG media.module.mixing.base.AudioMixer - AudioMixer is created. (mixFileName=/Users/jamesj/Desktop/voip_test/V_beA82spdUQmwx86zSv6PWFk9Ay5MTAlmnAWvT40A3nRiu0X-80wubwoukDH8n3xM_mix.wav, samplingRate=8000)  
12:09:29.337 [EventScannerThread] WARN media.module.mixing.base.AudioMixer - MixTask: packetSize=160  
12:09:29.337 [EventScannerThread] DEBUG service.TaskManager - | Task [MixTask_/Users/jamesj/Desktop/voip_test/V_beA82spdUQmwx86zSv6PWFk9Ay5MTAlmnAWvT40A3nRiu0X-80wubwoukDH8n3xM_mix.wav] is added. (interval=1)  
12:09:29.340 [EventScannerThread] DEBUG media.module.mixing.AudioMixManager - Success to add the audio mixer. (mixerId=beA82spdUQmwx86zSv6PWFk9Ay5MTAlmnAWvT40A3nRiu0X-80wubwoukDH8n3xM)  
12:09:29.342 [EventScannerThread] DEBUG media.module.mixing.base.AudioBuffer - AudioBuffer is created. (id=65418859862d6426061dd057df9b5b73@192.168.2.159)  
12:09:29.342 [EventScannerThread] DEBUG media.module.mixing.AudioMixManager - Success to add the audio buffer. (mixerId=beA82spdUQmwx86zSv6PWFk9Ay5MTAlmnAWvT40A3nRiu0X-80wubwoukDH8n3xM, bufferId=65418859862d6426061dd057df9b5b73@192.168.2.159)  
12:09:29.384 [EventScannerThread] DEBUG media.sdp.SdpParser - mediaFormats: [8]  
12:09:29.384 [EventScannerThread] DEBUG media.sdp.SdpParser - attr: a=rtpmap:8 ALAW/8000  
12:09:29.384 [EventScannerThread] DEBUG media.sdp.SdpParser - value: 8 ALAW/8000  
12:09:29.384 [EventScannerThread] DEBUG media.sdp.SdpParser - payloadId: 8  
12:09:29.384 [EventScannerThread] DEBUG media.sdp.SdpParser - description: ALAW/8000  
12:09:29.385 [EventScannerThread] DEBUG media.sdp.SdpParser - SdpInfo: SdpInfo{attributes=[SdpAttribute{name='rtpmap', payloadId='8', description='ALAW/8000', codec='ALAW', sampleRate=8000}]}  
12:09:29.387 [EventScannerThread] DEBUG signal.module.SipUtil - | Send 100 Trying for INVITE: SIP/2.0 100 Trying  
CSeq: 1 INVITE  
Call-ID: 65418859862d6426061dd057df9b5b73@192.168.2.159  
From: "01056785678" <sip:01056785678@192.168.2.159:50000;lr>;tag=1022  
To: "voip_server" <sip:voip_server@192.168.2.159:51000;lr>  
Via: SIP/2.0/UDP 192.168.2.159:50000;branch=z9hG4bK-353434-c85cda0b70db0404cf8b310e44a9bdbf  
Content-Length: 0  
  
12:09:29.389 [EventScannerThread] DEBUG signal.module.SipUtil - | Send 180 Ringing for INVITE: SIP/2.0 180 Ringing  
CSeq: 1 INVITE  
Call-ID: 65418859862d6426061dd057df9b5b73@192.168.2.159  
From: "01056785678" <sip:01056785678@192.168.2.159:50000;lr>;tag=1022  
To: "voip_server" <sip:voip_server@192.168.2.159:51000;lr>;tag=9327  
Via: SIP/2.0/UDP 192.168.2.159:50000;branch=z9hG4bK-353434-c85cda0b70db0404cf8b310e44a9bdbf  
Contact: <sip:voip_server@192.168.2.159:51000>  
Content-Length: 0  
  
12:09:29.389 [EventScannerThread] DEBUG signal.module.SipUtil - | Group Call(beA82spdUQmwx86zSv6PWFk9Ay5MTAlmnAWvT40A3nRiu0X-80wubwoukDH8n3xM) has started by [01056785678]  
12:09:29.390 [EventScannerThread] DEBUG signal.module.SipUtil - | Send 200 OK for INVITE: SIP/2.0 200 OK  
CSeq: 1 INVITE  
Call-ID: 65418859862d6426061dd057df9b5b73@192.168.2.159  
From: "01056785678" <sip:01056785678@192.168.2.159:50000;lr>;tag=1022  
To: "voip_server" <sip:voip_server@192.168.2.159:51000;lr>;tag=9327  
Via: SIP/2.0/UDP 192.168.2.159:50000;branch=z9hG4bK-353434-c85cda0b70db0404cf8b310e44a9bdbf  
Contact: <sip:voip_server@192.168.2.159:51000>  
Content-Type: application/sdp  
Content-Length: 117  
  
v=0  
o=- 0 0 IN IP4 192.168.2.159  
s=-  
c=IN IP4 192.168.2.159  
t=0 0  
m=audio 2600 RTP/AVP 8  
a=rtpmap:8 ALAW/8000  
  
  
##### 3-1-2-3) ACK : Client > Proxy
12:09:39.297 [EventScannerThread] DEBUG signal.module.SipUtil - | Recv Response: SIP/2.0 100 Trying  
CSeq: 1 INVITE  
Call-ID: 5c401b5a83cfb87fe2b935be46b66f9b@192.168.2.159  
From: "01012341234" <sip:01012341234@192.168.2.159:52000;lr>;tag=6995  
To: "voip_server" <sip:voip_server@192.168.2.159:51000;lr>  
Via: SIP/2.0/UDP 192.168.2.159:52000;branch=z9hG4bK-343939-bf322672efc90cfa49fd10432f3c8b4b  
Content-Length: 0  
  
12:09:39.301 [AWT-EventQueue-0] DEBUG service.TaskManager - | Task [CallCancelHandler5c401b5a83cfb87fe2b935be46b66f9b@192.168.2.159] is added. (interval=60000)  
12:09:39.301 [AWT-EventQueue-0] DEBUG client.gui.model.ClientFrame - | Call to [voip_server]  
12:09:39.302 [EventScannerThread] DEBUG signal.module.SipUtil - | Recv Response: SIP/2.0 180 Ringing  
CSeq: 1 INVITE  
Call-ID: 5c401b5a83cfb87fe2b935be46b66f9b@192.168.2.159  
From: "01012341234" <sip:01012341234@192.168.2.159:52000;lr>;tag=6995  
To: "voip_server" <sip:voip_server@192.168.2.159:51000;lr>;tag=3191  
Via: SIP/2.0/UDP 192.168.2.159:52000;branch=z9hG4bK-343939-bf322672efc90cfa49fd10432f3c8b4b  
Contact: <sip:voip_server@192.168.2.159:51000>  
Content-Length: 0  
  
12:09:39.303 [EventScannerThread] DEBUG signal.module.SipUtil - | Recv Response: SIP/2.0 200 OK  
CSeq: 1 INVITE  
Call-ID: 5c401b5a83cfb87fe2b935be46b66f9b@192.168.2.159  
From: "01012341234" <sip:01012341234@192.168.2.159:52000;lr>;tag=6995  
To: "voip_server" <sip:voip_server@192.168.2.159:51000;lr>;tag=3191  
Via: SIP/2.0/UDP 192.168.2.159:52000;branch=z9hG4bK-343939-bf322672efc90cfa49fd10432f3c8b4b  
Contact: <sip:voip_server@192.168.2.159:51000>  
Content-Type: application/sdp  
Content-Length: 117  
  
v=0  
o=- 0 0 IN IP4 192.168.2.159  
s=-  
c=IN IP4 192.168.2.159  
t=0 0  
m=audio 2600 RTP/AVP 8  
a=rtpmap:8 ALAW/8000  
  
12:09:39.331 [EventScannerThread] DEBUG media.sdp.SdpParser - mediaFormats: [8]  
12:09:39.331 [EventScannerThread] DEBUG media.sdp.SdpParser - attr: a=rtpmap:8 ALAW/8000  
12:09:39.331 [EventScannerThread] DEBUG media.sdp.SdpParser - value: 8 ALAW/8000  
12:09:39.331 [EventScannerThread] DEBUG media.sdp.SdpParser - payloadId: 8  
12:09:39.331 [EventScannerThread] DEBUG media.sdp.SdpParser - description: ALAW/8000  
12:09:39.332 [EventScannerThread] DEBUG media.sdp.SdpParser - SdpInfo: SdpInfo{attributes=[SdpAttribute{name='rtpmap', payloadId='8', description='ALAW/8000', codec='ALAW', sampleRate=8000}]}  
12:09:39.332 [EventScannerThread] DEBUG signal.module.SipUtil - RemoteCodec: ALAW, LocalCodec: ALAW  
12:09:39.334 [EventScannerThread] DEBUG signal.module.SipUtil - | Send ACK for INVITE: ACK sip:voip_server@192.168.2.159:51000 SIP/2.0  
Call-ID: 5c401b5a83cfb87fe2b935be46b66f9b@192.168.2.159  
CSeq: 1 ACK  
Via: SIP/2.0/UDP 192.168.2.159:52000;branch=z9hG4bK-343939-6e55fc53538469fe1e73b0bd1907e8e8  
From: "01012341234" <sip:01012341234@192.168.2.159:52000;lr>;tag=6995  
To: "voip_server" <sip:voip_server@192.168.2.159:51000;lr>;tag=3191  
Max-Forwards: 70  
Content-Length: 0  
  
#### 3-1-3) Bye
##### 3-1-3-1) Bye request : Client > Proxy
12:10:06.416 [AWT-EventQueue-0] DEBUG signal.module.SipUtil - | BYE Request sent. (request=BYE sip:voip_server@192.168.2.159:51000;lr SIP/2.0  
Call-ID: a19301958f4cca386451578c304c7841@192.168.2.159  
CSeq: 1 BYE  
From: "01012341234" <sip:01012341234@192.168.2.159:52000;lr>;tag=5021  
To: "voip_server" <sip:voip_server@192.168.2.159:51000;lr>;tag=5204  
Via: SIP/2.0/UDP 192.168.2.159:52000;branch=z9hG4bK-343939-5c9dfb432c0b99865405004a95f52fa6  
Max-Forwards: 70  
Contact: <sip:01012341234@192.168.2.159:52000>  
Content-Length: 0  
  
  
##### 3-1-3-2) 200 ok response : Proxy > Client
12:09:48.377 [EventScannerThread] DEBUG signal.module.SipUtil - | Recv BYE: BYE sip:voip_server@192.168.2.159:51000;lr SIP/2.0  
Call-ID: 5c401b5a83cfb87fe2b935be46b66f9b@192.168.2.159  
CSeq: 1 BYE  
From: "01012341234" <sip:01012341234@192.168.2.159:52000;lr>;tag=2822  
To: "voip_server" <sip:voip_server@192.168.2.159:51000;lr>;tag=4958  
Via: SIP/2.0/UDP 192.168.2.159:52000;branch=z9hG4bK-343939-1c87167dcecde487ae5876624edbae3d  
Max-Forwards: 70  
Contact: <sip:01012341234@192.168.2.159:52000>  
Content-Length: 0  
  
12:09:48.378 [EventScannerThread] DEBUG media.netty.module.NettyChannel - | Channel is closed.  
12:09:48.386 [EventScannerThread] DEBUG media.netty.NettyChannelManager - | Success to close the proxy channel. (key=5c401b5a83cfb87fe2b935be46b66f9b@192.168.2.159)  
12:09:48.386 [EventScannerThread] DEBUG signal.base.CallInfo - [CALL] (beA82spdUQmwx86zSv6PWFk9Ay5MTAlmnAWvT40A3nRiu0X-80wubwoukDH8n3xM) (5c401b5a83cfb87fe2b935be46b66f9b@192.168.2.159) (01012341234) (voip_server) Remote call info is removed.  
12:09:48.386 [EventScannerThread] DEBUG media.module.mixing.AudioMixManager - Success to delete the audio buffer. (mixerId=beA82spdUQmwx86zSv6PWFk9Ay5MTAlmnAWvT40A3nRiu0X-80wubwoukDH8n3xM, bufferId=5c401b5a83cfb87fe2b935be46b66f9b@192.168.2.159)  
12:09:48.387 [EventScannerThread] DEBUG signal.module.GroupCallManager - Success to delete the callId in the room. (roomId=beA82spdUQmwx86zSv6PWFk9Ay5MTAlmnAWvT40A3nRiu0X-80wubwoukDH8n3xM, callId=5c401b5a83cfb87fe2b935be46b66f9b@192.168.2.159)  
12:09:48.388 [EventScannerThread] DEBUG signal.module.SipUtil - | Send 200 OK for BYE: SIP/2.0 200 OK  
CSeq: 1 BYE  
Call-ID: 5c401b5a83cfb87fe2b935be46b66f9b@192.168.2.159  
From: "01012341234" <sip:01012341234@192.168.2.159:52000;lr>;tag=2822  
To: "voip_server" <sip:voip_server@192.168.2.159:51000;lr>;tag=4958  
Via: SIP/2.0/UDP 192.168.2.159:52000;branch=z9hG4bK-343939-1c87167dcecde487ae5876624edbae3d  
Content-Length: 0  
  
  
##### 3-1-3-3-) 200 ok response : Client
12:10:06.527 [EventScannerThread] DEBUG signal.module.SipUtil - | Recv Response: SIP/2.0 200 OK  
CSeq: 1 BYE  
Call-ID: a19301958f4cca386451578c304c7841@192.168.2.159  
From: "01012341234" <sip:01012341234@192.168.2.159:52000;lr>;tag=5021  
To: "voip_server" <sip:voip_server@192.168.2.159:51000;lr>;tag=5204  
Via: SIP/2.0/UDP 192.168.2.159:52000;branch=z9hG4bK-343939-5c9dfb432c0b99865405004a95f52fa6  
Content-Length: 0
  
  
  
### 3-2) Relay Call






