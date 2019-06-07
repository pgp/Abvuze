/*
 * Created on Jan 25, 2005
 * Created by Alon Rohter
 * Copyright (C) Azureus Software, Inc, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package com.aelitis.azureus.core.peermanager.messaging;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.aelitis.azureus.core.networkmanager.Transport;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.DirectByteBuffer;

/**
 * Decodes a message stream into separate messages.
 */
public interface MessageStreamDecoder {
  /**
   * Decode message stream from the given transport.
   * @param transport to decode from
   * @param max_bytes to decode/read from the stream
   * @return number of bytes decoded
   * @throws IOException on decoding error
   */
  int performStreamDecode(Transport transport, int max_bytes) throws IOException;
  
  /**
   * Get the messages decoded from the transport, if any, from the last decode op.
   * @return decoded messages, or null if no new complete messages were decoded
   */
  Message[] removeDecodedMessages();
  
  /**
   * Get the number of protocol (overhead) bytes decoded from the transport, from the last decode op.
   * @return number of protocol bytes recevied
   */
  int getProtocolBytesDecoded();
  
  /**
   * Get the number of (piece) data bytes decoded from the transport, from the last decode op.
   * @return number of data bytes received
   */
  int getDataBytesDecoded();
  
  
  /**
   * Get the percentage of the current message that has already been received (read from the transport).
   * @return percentage complete (0-99), or -1 if no message is currently being received
   */
  int getPercentDoneOfCurrentMessage();
  
  
  /**
   * Pause message decoding.
   */
  void pauseDecoding();
  
  /**
   * Resume message decoding.
   */
  void resumeDecoding();
  
  /**
   * Destroy this decoder, i.e. perform cleanup.
   * @return any bytes already-read and still remaining within the decoder
   */
  ByteBuffer destroy();

  static int preReadProcess(int allowed,
                            ByteBuffer[] decode_array,
                            DirectByteBuffer payload_buffer,
                            boolean reading_length_mode,
                            boolean destroyed,
                            int[] pre_read_start_buffer_and_pos) {
    if( allowed < 1 ) {
      Debug.out( "allowed < 1" );
    }

    decode_array[ 0 ] = payload_buffer == null ? null : payload_buffer.getBuffer( DirectByteBuffer.SS_MSG );  //ensure the decode array has the latest payload pointer

    int bytes_available = 0;
    boolean shrink_remaining_buffers = false;
    int start_buff = reading_length_mode ? 1 : 0;
    boolean marked = false;

    for( int i = start_buff; i < 2; i++ ) {  //set buffer limits according to bytes allowed
      ByteBuffer bb = decode_array[ i ];

      if( bb == null ) {
        Debug.out( "preReadProcess:: bb["+i+"] == null, decoder destroyed=" +destroyed );

        throw( new RuntimeException( "decoder destroyed" ));
      }


      if( shrink_remaining_buffers ) {
        bb.limit( 0 );  //ensure no read into this next buffer is possible
      }
      else {
        int remaining = bb.remaining();

        if( remaining < 1 )  continue;  //skip full buffer

        if( !marked ) {
          pre_read_start_buffer_and_pos[0] = i;
          pre_read_start_buffer_and_pos[1] = bb.position();
          marked = true;
        }

        if( remaining > allowed ) {  //read only part of this buffer
          bb.limit( bb.position() + allowed );  //limit current buffer
          bytes_available += bb.remaining();
          shrink_remaining_buffers = true;  //shrink any tail buffers
        }
        else {  //full buffer is allowed to be read
          bytes_available += remaining;
          allowed -= remaining;  //count this buffer toward allowed and move on to the next
        }
      }
    }

    return bytes_available;
  }
    
}
