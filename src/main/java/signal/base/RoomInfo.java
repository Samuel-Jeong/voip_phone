package signal.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import signal.module.CallManager;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @class public class RoomInfo
 * @brief RoomInfo
 */
public class RoomInfo {

    private static final Logger logger = LoggerFactory.getLogger(RoomInfo.class);

    /* Room ID (Random UUID) */
    private final String roomId;

    /* Key: call-id, value: remote call list */
    private final HashMap<String, List<String>> callMap = new HashMap<>();
    /* call map lock */
    private final ReentrantLock callMapLock = new ReentrantLock();

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @fn public RoomInfo(String roomId)
     * @brief RoomInfo 생성자 함수
     * @param roomId Room-ID (using call-id)
     */
    public RoomInfo(String roomId) {
        this.roomId = roomId;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public String getRoomId() {
        return roomId;
    }

    public int getCallMapSize() {
        try {
            callMapLock.lock();

            return callMap.size();
        } catch (Exception e) {
            logger.warn("Fail to get the call map size.", e);
            return 0;
        } finally {
            callMapLock.unlock();
        }
    }

    public void addCall(String callId) {
        if (callId == null) {
            logger.warn("CallId is null. Fail to add the call-id.");
            return;
        }

        try {
            callMapLock.lock();

            CallInfo callInfo = CallManager.getInstance().getCallInfo(callId);
            if (callInfo == null) {
                logger.warn("Fail to add the call info. (callId={})", callId);
                return;
            }

            List<String> remoteCallList = callMap.get(callId);
            if (remoteCallList == null) {
                remoteCallList = new ArrayList<>();
                callMap.putIfAbsent(callId, remoteCallList);
                callInfo.setIsRoomEntered(true);
            }

            addCallToRemoteCallList(callId);
            addRemoteCallListToCall(callId);

            logger.debug("Success to add the call info. (callId={})", callId);
        } catch (Exception e) {
            logger.warn("Fail to add the call info. (callId={})", callId, e);
        } finally {
            callMapLock.unlock();
        }
    }

    private void addRemoteCallListToCall(String callId) {
        List<String> remoteCallList = callMap.get(callId);
        if (remoteCallList != null) {
            // 자신의 call-id 를 제외한 모든 remote call-id 를 자신의 remoteCallList 에 추가
            remoteCallList.addAll(callMap.keySet());
            remoteCallList.remove(callId);
        }
    }

    private void addCallToRemoteCallList(String callId) {
        for (Map.Entry<String, List<String>> entry : callMap.entrySet()) {
            String curCallId = entry.getKey();
            if (curCallId == null || curCallId.equals(callId)) { continue; }

            // 현재 room 에 참여한 call 의 remoteCallList 에 방금 참여한 call-id 를 새로 추가
            List<String> remoteCallList = entry.getValue();
            if (remoteCallList != null) {
                remoteCallList.add(callId);
            }
        }
    }

    public List<String> deleteCall(String callId) {
        if (callId == null) { return null; }

        try {
            callMapLock.lock();

            List<String> remoteCallList = callMap.get(callId);
            if (remoteCallList == null) {
                return null;
            }

            deleteCallFromRemoteCallList(callId);
            return callMap.remove(callId);
        } catch (Exception e) {
            logger.warn("Fail to delete the call map.", e);
            return null;
        } finally {
            callMapLock.unlock();
        }
    }

    private void deleteCallFromRemoteCallList(String callId) {
        for (Map.Entry<String, List<String>> entry : callMap.entrySet()) {
            String curCallId = entry.getKey();
            if (curCallId == null || curCallId.equals(callId)) { continue; }

            // 현재 room 에 참여한 call 의 remoteCallList 에서 지정한 call-id 를 삭제
            List<String> remoteCallList = entry.getValue();
            if (remoteCallList != null) {
                remoteCallList.remove(callId);
            }
        }
    }

    public List<String> cloneRemoteCallList(String callId) {
        List<String> remoteCallList = getRemoteCallList(callId);
        if (remoteCallList == null) {
            return Collections.emptyList();
        }

        return new ArrayList<>(remoteCallList);
    }

    public List<String> getRemoteCallList(String callId) {
        if (callId == null) { return null; }

        try {
            callMapLock.lock();

            return callMap.get(callId);
        } catch (Exception e) {
            logger.warn("Fail to get the remote call list. (callId={})", callId, e);
            return null;
        } finally {
            callMapLock.unlock();
        }
    }

    public void clearCallMap() {
        try {
            callMapLock.lock();

            callMap.clear();
        } catch (Exception e) {
            logger.warn("Fail to clear the call map.", e);
        } finally {
            callMapLock.unlock();
        }
    }

}
