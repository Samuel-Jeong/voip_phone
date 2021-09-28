/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2017, Telestax Inc and individual contributors
 * by the @authors tag.
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
import media.protocol.rtp.base.RtpFormat;
import media.protocol.rtp.base.RtpFrame;

/**
 * @interface public interface JitterBuffer
 * @brief JitterBuffer interface
 */
public interface JitterBuffer {

    /**
     * Offers a packet to the jitter buffer.
     *
     * @param packet The RTP packet
     * @param format The format of the RTP packet
     */
    void write (RtpPacket packet, RtpFormat format);

    /**
     * Consumes a frame from the jitter buffer.
     *
     * @return The next ordered frame in the jitter buffer.
     */
    RtpFrame read ();

    /**
     * Sets a listener to be warned of events raised by the jitter buffer.
     *
     * @param listener the listener
     */
    void setListener (BufferListener listener);

    /**
     * Sets whether the buffer is active or not.
     *
     * @param inUse inUse
     */
    void setInUse (boolean inUse);

    /**
     * Restarts the jitter buffer.
     */
    void reStart();

    int getQueueSize();

}
