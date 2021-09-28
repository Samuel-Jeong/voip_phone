package signal.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import signal.base.RoomInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @class public class GroupCallManager
 * @brief GroupCallManager class
 */
public class GroupCallManager {

    private static final Logger logger = LoggerFactory.getLogger(GroupCallManager.class);

    /* GroupCallManager Singleton object */
    private static GroupCallManager groupCallManager = null;

    /* Key: Room-ID (using session-id), value: RoomInfo */
    private final HashMap<String, RoomInfo> roomMap = new HashMap<>();
    /* Room Map Lock */
    private final ReentrantLock roomMapLock = new ReentrantLock();

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @fn public GroupCallManager()
     * @brief GroupCallManager 생성자 함수
     */
    public GroupCallManager() {
        // Nothing
    }

    public static GroupCallManager getInstance () {
        if (groupCallManager == null) {
            groupCallManager = new GroupCallManager();
        }
        return groupCallManager;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public int getRoomMapSize() {
        try {
            roomMapLock.lock();

            return roomMap.size();
        } catch (Exception e) {
            logger.warn("Fail to get the room map size.");
            return 0;
        } finally {
            roomMapLock.unlock();
        }
    }

    public void addRoomInfo(String roomId, String callId) {
        if (roomId == null || callId == null) {
            logger.warn("Fail to add the room info. (roomId={})", roomId);
            return;
        }

        try {
            roomMapLock.lock();

            RoomInfo roomInfo = roomMap.get(roomId);
            if (roomInfo == null) {
                // Room 없으면 새로 생성
                roomInfo = new RoomInfo(roomId);
                roomMap.putIfAbsent(roomId, roomInfo);
            }

            // Room 에 지정한 Call 추가
            if (roomInfo.getRemoteCallList(callId) == null) {
                roomInfo.addCall(callId);
                logger.debug("Success to add the callId in the room. (callId={})", callId);
            } else {
                logger.warn("Fail to add the callId in the room. (callId={})", callId);
            }

            logger.debug("Success to add the room info. (roomId={}, callId={})", roomId, callId);
        } catch (Exception e) {
            logger.warn("Fail to add the room info. (roomId={})", roomId);
        } finally {
            roomMapLock.unlock();
        }
    }

    public RoomInfo deleteRoomInfo(String roomId, String callId) {
        if (roomId == null || callId == null) { return null; }

        try {
            roomMapLock.lock();

            RoomInfo roomInfo = roomMap.get(roomId);
            if (roomInfo == null) {
                return null;
            }

            if (roomInfo.deleteCall(callId) != null) {
                logger.debug("Success to delete the callId in the room. (roomId={}, callId={})", roomId, callId);
            }

            // Room 안에 모든 호가 삭제되면 Room 도 삭제
            if (roomInfo.getCallMapSize() == 0) {
                logger.debug("Success to delete the room info. (roomId={}, callId={})", roomId, callId);
                return roomMap.remove(roomId);
            } else {
                return roomInfo;
            }
        } catch (Exception e) {
            logger.warn("Fail to delete the room info. (roomId={}, callId={})", roomId, callId);
            return null;
        } finally {
            roomMapLock.unlock();
        }
    }

    public Map<String, RoomInfo> getCloneRoomMap( ) {
        HashMap<String, RoomInfo> cloneMap;

        try {
            roomMapLock.lock();

            cloneMap = (HashMap<String, RoomInfo>) roomMap.clone();
        } catch (Exception e) {
            logger.warn("Fail to clone the room map.");
            cloneMap = roomMap;
        } finally {
            roomMapLock.unlock();
        }

        return cloneMap;
    }

    public RoomInfo getRoomInfo(String roomId) {
        if (roomId == null) { return null; }

        try {
            roomMapLock.lock();

            return roomMap.get(roomId);
        } catch (Exception e) {
            logger.warn("Fail to get the room info. (roomId={})", roomId);
            return null;
        } finally {
            roomMapLock.unlock();
        }
    }

    public void clearRoomInfoMap() {
        try {
            roomMapLock.lock();

            roomMap.clear();
            logger.debug("Success to clear the room map.");
        } catch (Exception e) {
            logger.warn("Fail to clear the room map.");
        } finally {
            roomMapLock.unlock();
        }
    }


}
