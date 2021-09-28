/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package media.protocol.rtp.jitter;

import media.protocol.rtp.RtpPacket;
import media.protocol.rtp.base.Memory;
import media.protocol.rtp.base.RtpClock;
import media.protocol.rtp.base.RtpFormat;
import media.protocol.rtp.base.RtpFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @class public class FixedJitterBuffer implements JitterBuffer, Serializable
 * @brief FixedJitterBuffer class
 */
public class FixedJitterBuffer implements JitterBuffer, Serializable {

    private static final Logger logger = LoggerFactory.getLogger(FixedJitterBuffer.class);

    private final String name;

    // The underlying buffer size
    private static final int QUEUE_SIZE = 10000;

    private final Lock lock = new ReentrantLock();

    // the underlying buffer
    private final ArrayList<RtpFrame> queue = new ArrayList<>(QUEUE_SIZE);

    // RTP clock
    private final RtpClock rtpClock;

    // first received sequence number
    private long isn = -1;

    // allowed jitter
    private final long jitterBufferSize;

    // packet arrival dead line measured on RTP clock.
    // initial value equals to infinity
    private long arrivalDeadLineTimeStamp = 0;

    // packet arrival dead line measured on RTP clock.
    // initial value equals to infinity
    private long droppedInRaw = 0;

    // The number of dropped packets
    private int dropCount;

    // buffer's monitor
    private BufferListener listener;
    private final AtomicBoolean ready;

    /* used to calculate network jitter. currentTransit measures the relative time it takes for an RTP packet to arrive from the remote server to MMS */
    private long currentTransit = 0;

    /* continuously updated value of network jitter */
    private long currentJitter = 0;

    // currently used format
    private RtpFormat format;

    private Boolean useBuffer = true;

    //////////////////////////////////////////////////////////////////////

    public FixedJitterBuffer (String name, RtpClock clock, int jitterBufferSize) {
        this.name = name;
        this.rtpClock = clock;
        this.rtpClock.setClockRate(8000);

        this.jitterBufferSize = jitterBufferSize;
        this.ready = new AtomicBoolean(false);

        logger.debug("({}) FixedJitterBuffer is created.", name);
    }

    //////////////////////////////////////////////////////////////////////

    private void initJitter (RtpPacket firstPacket) {
        long arrival = rtpClock.getLocalRtpTime();
        long firstPacketTimestamp = firstPacket.getTimeStamp();
        currentTransit = arrival - firstPacketTimestamp;
    }

    /**
     * Calculates the current network jitter, which is an estimate of the statistical variance of the RTP data packet
     * interarrival time: http://tools.ietf.org/html/rfc3550#appendix-A.8
     */
    private void estimateJitter (RtpPacket newPacket) {
        long arrival = rtpClock.getLocalRtpTime();
        long newPacketTimestamp = newPacket.getTimeStamp();
        long transit = arrival - newPacketTimestamp;
        long d = transit - currentTransit;
        if (d < 0) {
            d = -d;
        }

        currentTransit = transit;
        currentJitter += d - ((currentJitter + 8) >> 4);
    }

    public boolean bufferInUse ( ) {
        return this.useBuffer;
    }

    @Override
    public void setInUse (boolean useBuffer) {
        this.useBuffer = useBuffer;
    }

    @Override
    public void setListener (BufferListener listener) {
        this.listener = listener;
    }

    //////////////////////////////////////////////////////////////////////

    @Override
    public void write (RtpPacket packet, RtpFormat format) {
        if (format == null) {
            logger.warn("({}) No format specified. Packet dropped!", name);
            return;
        }

        boolean locked = false;
        try {
            locked = this.lock.tryLock() || this.lock.tryLock(5, TimeUnit.MILLISECONDS);
            if (locked) {
                safeWrite(packet, format);
            }
        } catch (InterruptedException e) {
            logger.warn("({}) Could not acquire write lock for jitter buffer. Dropped packet.", name);
        } finally {
            if (locked) {
                this.lock.unlock();
            }
        }
    }

    private void safeWrite (RtpPacket rtpPacket, RtpFormat format) {
        if (this.format == null || this.format.getID() != format.getID()) {
            this.format = format;
        }

        // if this is first packet then synchronize clock
        if (isn == -1) {
            rtpClock.synchronize(rtpPacket.getTimeStamp());
            isn = rtpPacket.getSeqNum();
            initJitter(rtpPacket);
        } else {
            estimateJitter(rtpPacket);
        }

        // update clock rate
        rtpClock.setClockRate(this.format.getClockRate());

        // drop outstanding packets
        // packet is outstanding if its timestamp of arrived packet is less
        // then consumer media time
        if (rtpPacket.getTimeStamp() < this.arrivalDeadLineTimeStamp) {
            logger.warn("({}) drop packet: arrivalDeadLineTimeStamp={}, packet={}",
                    name, arrivalDeadLineTimeStamp, rtpPacket
            );
            dropCount++;

            // checking if not dropping too much
            droppedInRaw++;
            if (droppedInRaw == QUEUE_SIZE / 2 || queue.size() == 0) {
                arrivalDeadLineTimeStamp = 0;
            } else {
                return;
            }
        }

        byte[] rtpTotalData = rtpPacket.getData();
        int rtpTotalLength = rtpTotalData.length;

        RtpFrame rtpFrame = Memory.allocate(rtpTotalLength);
        if (rtpFrame == null) {
            logger.warn("({}) Fail to allocate the rtp frame. (rtpTotalLength={})", name, rtpTotalLength);
            return;
        }

        // put packet into buffer irrespective of its sequence number
        rtpFrame.setHeader(null);
        rtpFrame.setSequenceNumber(rtpPacket.getSeqNum());
        // here time is in milliseconds
        rtpFrame.setTimestamp(rtpClock.convertToAbsoluteTime(rtpPacket.getTimeStamp(), rtpClock.getClockRate()));
        rtpFrame.setOffset(0);
        rtpFrame.setLength(rtpTotalLength);
        rtpFrame.setData(rtpTotalData);

        // set format
        rtpFrame.setRtpFormat(this.format);

        // make checks only if have packet
        droppedInRaw = 0;

        // find correct position to insert a packet
        // use timestamp since its always positive
        int currIndex = queue.size() - 1;
        while (currIndex >= 0 && queue.get(currIndex).getTimestamp() > rtpFrame.getTimestamp()) {
            currIndex--;
        }

        // check for duplicate packet
        if (currIndex >= 0 && queue.get(currIndex).getSequenceNumber() == rtpFrame.getSequenceNumber()) {
            return;
        }

        queue.add(currIndex + 1, rtpFrame);

        // recalculate duration of each frame in queue and overall duration
        // since we could insert the frame in the middle of the queue
        // known duration of media wich contains in this buffer.
        long duration = 0;
        if (queue.size() > 1) {
            duration = queue.get(queue.size() - 1).getTimestamp() - queue.get(0).getTimestamp();
        }

        for (int i = 0; i < queue.size() - 1; i++) {
            // duration measured by wall clock
            long d = queue.get(i + 1).getTimestamp() - queue.get(i).getTimestamp();
            // in case of RFC2833 event timestamp remains same
            queue.get(i).setDuration(d > 0 ? d : 0);
        }

        // if overall duration is negative we have some mess here,try to
        // reset
        if (duration < 0 && queue.size() > 1) {
            logger.warn("({}) Something messy happened. Resetting jitter buffer!", name);
            reset();
            return;
        }

        // overflow > only now remove packet if overflow, possibly the same packet we just received
        if (queue.size() > QUEUE_SIZE) {
            logger.warn("({}) Jitter Buffer overflow! (duration={}ms, queueSize={})", name, duration, queue.size() - 1);
            dropCount++;
            queue.remove(0).recycle();
        }

        // check if this buffer already full
        boolean readyTest = (!useBuffer || (duration >= jitterBufferSize && queue.size() > 1));
        if (ready.compareAndSet(false, readyTest)) {
            if (ready.get() && listener != null) {
                listener.onFill();
            }
        }
    }

    //////////////////////////////////////////////////////////////////////

    @Override
    public RtpFrame read () {
        RtpFrame frame = null;
        boolean locked = false;
        try {
            locked = this.lock.tryLock() || this.lock.tryLock(5, TimeUnit.MILLISECONDS);
            if (locked) {
                frame = safeRead();
            } else {
                this.ready.set(false);
            }
        } catch (InterruptedException e) {
            logger.warn("({}) Could not acquire reading lock for jitter buffer.", name);
            this.ready.set(false);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
        return frame;
    }

    private RtpFrame safeRead () {
        if (queue.size() == 0) {
            this.ready.set(false);
            return null;
        }

        // extract packet
        RtpFrame frame = queue.remove(0);

        // buffer empty now > change ready flag.
        if (queue.size() == 0) {
            this.ready.set(false);

            arrivalDeadLineTimeStamp = 0;
            // set it as 1 ms since otherwise will be dropped by pipe
            frame.setDuration(1);
        }

        arrivalDeadLineTimeStamp = rtpClock.convertToRtpTime(frame.getTimestamp() + frame.getDuration());

        // convert duration to nanoseconds
        frame.setDuration(frame.getDuration() * 1000000L);
        frame.setTimestamp(frame.getTimestamp() * 1000000L);

        return frame;
    }

    //////////////////////////////////////////////////////////////////////

    public String getName() {
        return name;
    }

    public void reset () {
        boolean locked = false;
        try {
            locked = lock.tryLock() || lock.tryLock(5, TimeUnit.MILLISECONDS);
            if (locked) {
                while (queue.size() > 0) {
                    queue.remove(0).recycle();
                }
            }
        } catch (InterruptedException e) {
            logger.warn("({}) Could not acquire lock to reset jitter buffer.", name);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    public int getQueueSize () {
        synchronized (queue) {
            return queue.size();
        }
    }

    public void reStart() {
        reset();

        this.ready.set(false);
        arrivalDeadLineTimeStamp = 0;
        dropCount = 0;
        droppedInRaw = 0;
        format = null;
        isn = -1;

        logger.debug("({}) Restarted jitter buffer.", name);
    }

}
