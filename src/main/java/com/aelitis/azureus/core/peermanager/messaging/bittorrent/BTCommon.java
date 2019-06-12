package com.aelitis.azureus.core.peermanager.messaging.bittorrent;

import com.aelitis.azureus.core.peermanager.messaging.MessageException;
import org.gudy.azureus2.core3.util.DirectByteBuffer;

public class BTCommon {

    public static int[] deserializeRefact(DirectByteBuffer data, String id) throws MessageException {
        if( data == null ) {
            throw new MessageException( "[" +id + "] decode error: data == null" );
        }

        if( data.remaining( DirectByteBuffer.SS_MSG ) != 12 ) {
            throw new MessageException( "[" +id + "] decode error: payload.remaining[" +data.remaining( DirectByteBuffer.SS_MSG )+ "] != 12" );
        }

        int num = data.getInt( DirectByteBuffer.SS_MSG );
        if( num < 0 ) {
            throw new MessageException( "[" +id + "] decode error: num < 0" );
        }

        int offset = data.getInt( DirectByteBuffer.SS_MSG );
        if( offset < 0 ) {
            throw new MessageException( "[" +id + "] decode error: offset < 0" );
        }

        int length = data.getInt( DirectByteBuffer.SS_MSG );
        if( length < 0 ) {
            throw new MessageException( "[" +id + "] decode error: length < 0" );
        }

        data.returnToPool();

        return new int[]{num,offset,length};
    }
}
