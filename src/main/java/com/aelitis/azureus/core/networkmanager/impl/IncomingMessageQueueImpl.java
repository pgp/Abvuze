/*
 * Created on Oct 17, 2004
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

package com.aelitis.azureus.core.networkmanager.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

import org.gudy.azureus2.core3.util.*;

import com.aelitis.azureus.core.networkmanager.IncomingMessageQueue;
import com.aelitis.azureus.core.networkmanager.NetworkConnection;
import com.aelitis.azureus.core.peermanager.messaging.*;



/**
 * Inbound peer message queue.
 */
public class IncomingMessageQueueImpl implements IncomingMessageQueue{
  
  private volatile ArrayList<MessageQueueListener> listeners = new ArrayList<>();  //copy-on-write
  private final AEMonitor listeners_mon = new AEMonitor( "IncomingMessageQueue:listeners" );

  private MessageStreamDecoder stream_decoder;
  private final NetworkConnection connection;

  
  /**
   * Create a new incoming message queue.
   * @param stream_decoder default message stream decoder
   * @param connection owner to read from
   */
  public IncomingMessageQueueImpl( MessageStreamDecoder stream_decoder, NetworkConnection connection ) {
	  if (stream_decoder == null) {
		  throw new NullPointerException("stream_decoder is null"); 
	  }
    this.connection = connection;
    this.stream_decoder = stream_decoder;
  }
  
  
  /**
   * Set the message stream decoder that will be used to decode incoming messages.
   * @param new_stream_decoder to use
   */
  public void setDecoder( MessageStreamDecoder new_stream_decoder ) {
    ByteBuffer already_read = stream_decoder.destroy();
    connection.getTransport().setAlreadyRead( already_read );
    stream_decoder = new_stream_decoder;
    stream_decoder.resumeDecoding();
  }
  
  public MessageStreamDecoder
  getDecoder()
  {
	  return( stream_decoder );
  }
  
  /**
   * Get the percentage of the current message that has already been received.
   * @return percentage complete (0-99), or -1 if no message is currently being received
   */
  public int getPercentDoneOfCurrentMessage() {
    return stream_decoder.getPercentDoneOfCurrentMessage();
  }
  
  
  
  /**
   * Receive (read) message(s) data from the underlying transport.
   * @param max_bytes to read
   * @return number of bytes received
   * @throws IOException on receive error
   */
  public int[] receiveFromTransport( int max_bytes, boolean protocol_is_free ) throws IOException {
    if( max_bytes < 1 ) {
    	
    	// Not yet fully supporting free-protocol for downloading
    	
      if ( !protocol_is_free ){
    	  
    	  Debug.out( "max_bytes < 1: " +max_bytes );
      }
      
      return new int[2];
    }
    
    if( listeners.isEmpty() ) {
      Debug.out( "no queue listeners registered!" );
      throw new IOException( "no queue listeners registered!" );
    }
    
    int bytes_read;
    
    try{
	    	//perform decode op
    	
	    bytes_read = stream_decoder.performStreamDecode( connection.getTransport(), max_bytes );
    
    }catch( RuntimeException e ){
    	
    	Debug.out( "Stream decode for " + connection.getString() + " failed: " + Debug.getNestedExceptionMessageAndStack(e));
    	
    	throw( e );
    }
    
    //check if anything was decoded and notify listeners if so
    Message[] messages = stream_decoder.removeDecodedMessages();
    if( messages != null ) {
      for( int i=0; i < messages.length; i++ ) {
        Message msg = messages[ i ];
        
        if( msg == null ) {
        	System.out.println( "received msg == null [messages.length=" +messages.length+ ", #" +i+ "]: " +connection.getTransport().getDescription() );
        	continue;
        }
        
        ArrayList listeners_ref = listeners;  //copy-on-write
        boolean handled = false;

          for (Object o : listeners_ref) {
              MessageQueueListener mql = (MessageQueueListener) o;
              if (mql.messageReceived(msg)) {
                  handled = true;
              }
          }
        
        if( !handled ) {
          if( listeners_ref.size() > 0 ) {
            System.out.println( "no registered listeners [out of " +listeners_ref.size()+ "] handled decoded message [" +msg.getDescription()+ "]" );
          }
          
          DirectByteBuffer[] buffs = msg.getData();
            for (DirectByteBuffer buff : buffs) {
                buff.returnToPool();
            }
        }
      }
    }
    
    int protocol_read = stream_decoder.getProtocolBytesDecoded();
    if( protocol_read > 0 ) {
      ArrayList listeners_ref = listeners;  //copy-on-write
        for (Object o : listeners_ref) {
            MessageQueueListener mql = (MessageQueueListener) o;
            mql.protocolBytesReceived(protocol_read);
        }
    }
    
    int data_read = stream_decoder.getDataBytesDecoded();
    if( data_read > 0 ) {
      ArrayList listeners_ref = listeners;  //copy-on-write
        for (Object o : listeners_ref) {
            MessageQueueListener mql = (MessageQueueListener) o;
            mql.dataBytesReceived(data_read);
        }
    }
    
    	// ideally bytes_read = data_read + protocol_read. in case it isn't then we want to
    	// return bytes_read = d + p with bias to p
    
    data_read = bytes_read - protocol_read;
    
    if ( data_read < 0 ){
    	
    	protocol_read 	= bytes_read;
    	
    	data_read		= 0;
    }
    
    return( new int[]{ data_read, protocol_read });   
  }
  

  
  
  /**
   * Notifty the queue (and its listeners) of a message received externally on the queue's behalf.
   * @param message received externally
   */
  public void notifyOfExternallyReceivedMessage( Message message ) throws IOException{
    ArrayList listeners_ref = listeners;  //copy-on-write
    boolean handled = false;

    DirectByteBuffer[] dbbs = message.getData();
    int size = 0;
      for (DirectByteBuffer dbb : dbbs) {
          size += dbb.remaining(DirectByteBuffer.SS_NET);
      }


      for (Object o : listeners_ref) {
          MessageQueueListener mql = (MessageQueueListener) o;
          if (mql.messageReceived(message)) {
              handled = true;
          }

          if (message.getType() == Message.TYPE_DATA_PAYLOAD) {
              mql.dataBytesReceived(size);
          } else {
              mql.protocolBytesReceived(size);
          }
      }
    
    if( !handled ) {
      if( listeners_ref.size() > 0 ) {
        System.out.println( "no registered listeners [out of " +listeners_ref.size()+ "] handled decoded message [" +message.getDescription()+ "]" );
      }
      
      DirectByteBuffer[] buffs = message.getData();
        for (DirectByteBuffer buff : buffs) {
            buff.returnToPool();
        }
    }
  }
  
 
  
  /**
   * Manually resume processing (reading) incoming messages.
   * NOTE: Allows us to resume docoding externally, in case it was auto-paused internally.
   */
  public void resumeQueueProcessing() {
    stream_decoder.resumeDecoding();
  }
  

  
  /**
   * Add a listener to be notified of queue events.
   * @param listener
   */
  public void registerQueueListener( MessageQueueListener listener ) {
    try{  listeners_mon.enter();
      //copy-on-write
      ArrayList<MessageQueueListener> new_list = new ArrayList<>(listeners.size() + 1);
      
      if ( listener.isPriority()){
    	  boolean	added = false;
          for (MessageQueueListener existing : listeners) {
              if (added || existing.isPriority()) {
              } else {
                  new_list.add(listener);
                  added = true;
              }
              new_list.add(existing);
          }
    	  if ( !added ){
    		  new_list.add( listener );
    	  }
      }else{
	      new_list.addAll( listeners );
	      new_list.add( listener );
      }
      listeners = new_list;
    }
    finally{  listeners_mon.exit();  }
  }
  
  
  /**
   * Cancel queue event notification listener.
   * @param listener
   */
  public void cancelQueueListener( MessageQueueListener listener ) {
    try{  listeners_mon.enter();
      //copy-on-write
      ArrayList new_list = new ArrayList( listeners );
      new_list.remove( listener );
      listeners = new_list;
    }
    finally{  listeners_mon.exit();  }
  }
  
  
  
  
  /**
   * Destroy this queue.
   */
  public void destroy() {
    stream_decoder.destroy();
  }
  
}
