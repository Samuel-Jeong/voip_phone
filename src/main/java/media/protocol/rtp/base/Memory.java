package media.protocol.rtp.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @class public class Memory
 * @brief Memory class
 */
public class Memory {

    private static final Logger logger = LoggerFactory.getLogger(Memory.class);

    private static final HashMap<Integer, MemoryPartition> memoryPartitionMap = new HashMap<>();
    private static final ReentrantLock memoryPartitionMapLock = new ReentrantLock();

    //////////////////////////////////////////////////////////////////////

    public static RtpFrame allocate(int size)
    {
        try {
            memoryPartitionMapLock.lock();

            MemoryPartition currPartition = memoryPartitionMap.get(size);
            if(currPartition == null) {
                currPartition = new MemoryPartition(size);

                MemoryPartition oldPartition = memoryPartitionMap.putIfAbsent(
                        size,
                        currPartition
                );

                if(oldPartition != null) {
                    currPartition = oldPartition;
                }
            }

            return currPartition.allocate();
        } catch (Exception e) {
            logger.warn("Memory.allocate.Exception", e);
            return null;
        } finally {
            memoryPartitionMapLock.unlock();
        }
    }

}
