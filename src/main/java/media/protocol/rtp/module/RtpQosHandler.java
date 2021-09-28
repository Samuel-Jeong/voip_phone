package media.protocol.rtp.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import signal.base.CallInfo;

/**
 * @class public class RtpQosHandler
 * @brief RtpQosHandler
 */
public class RtpQosHandler {

    private static final Logger logger = LoggerFactory.getLogger(RtpQosHandler.class);

    private final int maxSeqNumGap;

    private int prevSeqNum = 0;

    ////////////////////////////////////////////////////////////////////////////////

    public RtpQosHandler() {
        maxSeqNumGap = CallInfo.MAX_RANDOM_SEQ_NUM;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public boolean checkSeqNum(int curSeqNum) {
        if (curSeqNum <= 0) {
            return false;
        }

        // 1) 이전 SeqNum 가 0 보다 큰 경우 > 두 번째 패킷 순서부터 QoS 검사
        if (prevSeqNum > 0) {
            int seqGap = Math.abs(curSeqNum - prevSeqNum);

            // 2) 현재 SeqNum 이 이전 SeqNum 보다 작은 경우 false 반환 (순서 역전)
            if ((seqGap >= 1 && seqGap <= maxSeqNumGap) &&
                    (prevSeqNum > curSeqNum)) {
                logger.trace("Wrong sequence number. Packet order is reversed. (prev={}, cur={})", prevSeqNum, curSeqNum);
                return false;
            }
        }

        prevSeqNum = curSeqNum;
        return true;
    }

}
