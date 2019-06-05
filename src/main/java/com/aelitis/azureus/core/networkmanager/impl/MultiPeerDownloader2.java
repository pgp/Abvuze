/*
 * Created on Apr 22, 2005
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
import java.util.*;

import org.gudy.azureus2.core3.util.AEDiagnostics;
import org.gudy.azureus2.core3.util.AEMonitor;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.SystemTime;

import com.aelitis.azureus.core.networkmanager.*;


/**
 * 
 */

public class MultiPeerDownloader2 implements RateControlledEntity {

	private static final int MOVE_TO_IDLE_TIME	= 500;
	
	private static final Object	ADD_ACTION 		= new Object();
	private static final Object	REMOVE_ACTION 	= new Object();
	
	private volatile ArrayList connections_cow = new ArrayList();  //copied-on-write
	private final AEMonitor connections_mon = new AEMonitor( "MultiPeerDownloader" );

	private final RateHandler main_handler;

	private List			pending_actions;
	private final connectionList	active_connections 	= new connectionList();
	private final connectionList	idle_connections 	= new connectionList();

	private long			last_idle_check;
	
	private volatile EventWaiter		waiter;
	
	/**
	 * Create new downloader using the given "global" rate handler to limit all peers managed by this downloader.
	 * @param main_handler
	 */
	public 
	MultiPeerDownloader2( 
		RateHandler _main_handler ) 
	{
		main_handler = _main_handler;
	}

	public RateHandler 
	getRateHandler() 
	{
		return( main_handler );
	}

	/**
	 * Add the given connection to the downloader.
	 * @param connection to add
	 */
	public void addPeerConnection( NetworkConnectionBase connection ) {
		EventWaiter waiter_to_kick = null;
		try{
			connections_mon.enter();
				//copy-on-write
			int cow_size = connections_cow.size();
			if ( cow_size == 0 ){
				waiter_to_kick = waiter;
				if ( waiter_to_kick != null ){
					waiter = null;
				}
			}
			ArrayList conn_new = new ArrayList( cow_size + 1 );
			conn_new.addAll( connections_cow );
			conn_new.add( connection );
			connections_cow = conn_new;
			
			if ( pending_actions == null ){
				
				pending_actions = new ArrayList();
			}
			
			pending_actions.add( new Object[]{ ADD_ACTION, connection });
		}finally{ 
			connections_mon.exit();  
		}
		
		if ( waiter_to_kick != null ){
			
			waiter_to_kick.eventOccurred();
		}
	}


	/**
	 * Remove the given connection from the downloader.
	 * @param connection to remove
	 * @return true if the connection was found and removed, false if not removed
	 */
	public boolean removePeerConnection( NetworkConnectionBase connection ) {
		try{
			connections_mon.enter();
			//copy-on-write
			ArrayList conn_new = new ArrayList( connections_cow );
			boolean removed = conn_new.remove( connection );
			if( !removed ) return false;
			connections_cow = conn_new;
			
			if ( pending_actions == null ){
				
				pending_actions = new ArrayList();
			}
			
			pending_actions.add( new Object[]{ REMOVE_ACTION, connection });
			return true;
		}finally{ 
			connections_mon.exit();  
		}
	}




	public boolean 
	canProcess( 
		EventWaiter waiter ) 
	{
		int[] allowed = main_handler.getCurrentNumBytesAllowed();

        // Not yet fully supporting free-protocol for downloading && allowed[1] == 0 ){
        return allowed[0] >= 1;
    }

	public long
	getBytesReadyToWrite()
	{
		return( 0 );
	}

	public int
	getConnectionCount( EventWaiter _waiter )
	{
		int result = connections_cow.size();
		
		if ( result == 0 ){
			
			waiter = _waiter;
		}
		
		return( result );
	}

	public int
	getReadyConnectionCount(
		EventWaiter	waiter )
	{
		int	res = 0;

        for (Object o : connections_cow) {

            NetworkConnectionBase connection = (NetworkConnectionBase) o;

            if (connection.getTransportBase().isReadyForRead(waiter) == 0) {

                res++;
            }
        }

		return( res );
	}

	public int 
	doProcessing( 
		EventWaiter waiter,
		int			max_bytes ) 
	{
			// Note - this is single threaded
		
		// System.out.println( "MPD: do process - " + connections_cow.size() + "/" + active_connections.size() + "/" + idle_connections.size());
		
		int[] bytes_allowed = main_handler.getCurrentNumBytesAllowed();
		
		int num_bytes_allowed = bytes_allowed[0];
		
		boolean protocol_is_free = bytes_allowed[1] > 0;
		
		if ( num_bytes_allowed < 1 ){ // Not yet fully supporting free-protocol for downloading && !protocol_is_free ){
			
			return 0;
		}

		if ( max_bytes > 0 && max_bytes < num_bytes_allowed ){
		
			num_bytes_allowed = max_bytes;
		}
		
		if ( pending_actions != null ){
			
			try{
				connections_mon.enter();

                for (Object pending_action : pending_actions) {

                    Object[] entry = (Object[]) pending_action;

                    NetworkConnectionBase connection = (NetworkConnectionBase) entry[1];

                    if (entry[0] == ADD_ACTION) {

                        active_connections.add(connection);

                    } else {

                        active_connections.remove(connection);

                        idle_connections.remove(connection);
                    }
                }
				
				pending_actions = null;
				
			}finally{
				
				connections_mon.exit();
			}
		}
		
		long now = SystemTime.getSteppedMonotonousTime();
		
		if ( now - last_idle_check > MOVE_TO_IDLE_TIME ){
			
			last_idle_check = now;
			
				// move any active ones off of the idle queue
		
			connectionEntry	entry = idle_connections.head();

			while( entry != null ){
				
				NetworkConnectionBase connection = entry.connection;

				connectionEntry next = entry.next;

				if ( connection.getTransportBase().isReadyForRead( waiter ) == 0 ){
				
					// System.out.println( "   moving to active " + connection.getString());
					
					idle_connections.remove( entry );
					
					active_connections.addToStart( entry );
				}
				
				entry = next;
			}
		}
		
			// process the active set

		int num_bytes_remaining = num_bytes_allowed;

		int	data_bytes_read		= 0;
		int protocol_bytes_read = 0;
		
		connectionEntry	entry = active_connections.head();
		
		int	num_entries = active_connections.size();
		
		for (int i=0; i<num_entries && entry != null &&  num_bytes_remaining > 0;i++ ){
			
			NetworkConnectionBase connection = entry.connection;

			connectionEntry next = entry.next;

			long	ready = connection.getTransportBase().isReadyForRead( waiter );

			// System.out.println( "   " + connection.getString() + " - " + ready );
			
			if ( ready == 0 ){
				
				int	mss = connection.getMssSize();
				
				int allowed = num_bytes_remaining > mss ? mss : num_bytes_remaining;

				int bytes_read = 0;

				try{
					int[] read = connection.getIncomingMessageQueue().receiveFromTransport( allowed, protocol_is_free );
					
					data_bytes_read 	+= read[0];
					protocol_bytes_read	+= read[1];
					
					bytes_read = read[0] + read[1];
					
				}catch( Throwable e ) {

					if( AEDiagnostics.TRACE_CONNECTION_DROPS ) {
						if( e.getMessage() == null ) {
							Debug.out( "null read exception message: ", e );
						}
						else {
							if(!e.getMessage().contains("end of stream on socket read") &&
								!e.getMessage().contains(
									"An existing connection was forcibly closed by the remote host") &&
								!e.getMessage().contains("Connection reset by peer") &&
								!e.getMessage().contains(
									"An established connection was aborted by the software in your host machine")) {

								System.out.println( "MP: read exception [" +connection.getTransportBase().getDescription()+ "]: " +e.getMessage() );
							}
						}
					}

					if (! (e instanceof IOException )){

							// one day upgrade this exception to an IOException
						
						if ( !Debug.getNestedExceptionMessage( e ).contains( "Incorrect mix" )){
						
							Debug.printStackTrace(e);
						}
					}

					connection.notifyOfException( e );
				}

				num_bytes_remaining -= bytes_read;
				
				// System.out.println( "   moving to end " + connection.getString());

				active_connections.moveToEnd( entry );
			
			}else if ( ready > MOVE_TO_IDLE_TIME ){
				
				// System.out.println( "   moving to idle " + connection.getString());

				active_connections.remove( entry );
				
				idle_connections.addToEnd( entry );
			}
			
			entry = next;
		}

		int total_bytes_read = num_bytes_allowed - num_bytes_remaining;
		
		if ( total_bytes_read > 0 ){
			
			main_handler.bytesProcessed( data_bytes_read, protocol_bytes_read );
			
			return total_bytes_read;
		}

		return 0;  //zero bytes read
	}


	public int getPriority() {  return RateControlledEntity.PRIORITY_HIGH;  }

	public boolean getPriorityBoost(){ return false; }

	public String
	getString()
	{
		StringBuilder str = new StringBuilder();

		str.append("MPD (").append(connections_cow.size()).append("/").append(active_connections.size()).append("/")
			.append(idle_connections.size()).append(": ");

		int	num = 0;

        for (Object o : connections_cow) {

            NetworkConnectionBase connection = (NetworkConnectionBase) o;

            if (num++ > 0) {

                str.append(",");
            }

            str.append(connection.getString());
        }

		return( str.toString());
	}
	
	protected static class
	connectionList
	{
		private int				size;
		
		private connectionEntry	head;
		private connectionEntry	tail;
		
		protected connectionEntry
		add(
			NetworkConnectionBase		connection )
		{
			connectionEntry entry = new connectionEntry( connection );
			
			if ( head == null ){
				
				head = tail = entry;
				
			}else{
				
				tail.next	= entry;
				entry.prev	= tail;
				
				tail = entry;
			}
			
			size++;
			
			return( entry );
		}
		
		protected void
		addToEnd(
			connectionEntry		entry )
		{
			entry.next = null;
			entry.prev = tail;

			if ( tail == null ){
				
				head = tail = entry;
				
			}else{
			
				tail.next	= entry;
				tail 		= entry;
			}
			
			size++;
		}
		
		protected void
		addToStart(
			connectionEntry		entry )
		{
			entry.next = head;
			entry.prev = null;

			if ( head == null ){
				
				head = tail = entry;
				
			}else{
				
				head.prev	= entry;
				head 		= entry;
			}
			
			size++;
		}
		
		protected void
		moveToEnd(
			connectionEntry		entry )
		{
			if ( entry != tail ){
				
				connectionEntry prev 	= entry.prev;
				connectionEntry	next	= entry.next;

				if ( prev == null ){
					
					head	= next;
					
				}else{
					
					prev.next = next;
				}
				
				next.prev = prev;
				
				entry.prev 	= tail;
				entry.next	= null;
				
				tail.next	= entry;
				tail		= entry;
			}
		}
		
		protected connectionEntry
		remove(
			NetworkConnectionBase		connection )
		{
			connectionEntry	entry = head;
			
			while( entry != null ){
				
				if ( entry.connection == connection ){
					
					remove( entry );
					
					return( entry );
					
				}else{
					
					entry = entry.next;
				}
			}
			
			return( null );
		}
		
		protected void
		remove(
			connectionEntry	entry )
		{
			connectionEntry prev 	= entry.prev;
			connectionEntry	next	= entry.next;
			
			if ( prev == null ){
			
				head	= next;
				
			}else{
				
				prev.next = next;
			}
			
			if ( next == null ){
				
				tail	= prev;
				
			}else{
				
				next.prev = prev;
			}
			
			size--;
		}
		
		protected int
		size()
		{
			return( size );
		}
		
		protected connectionEntry
		head()
		{
			return( head );
		}
	}
	
	protected static class
	connectionEntry
	{
		private connectionEntry next;
		private connectionEntry prev;
		
		final NetworkConnectionBase	connection;
		
		protected
		connectionEntry(
			NetworkConnectionBase		_connection )
		{
			connection = _connection;
		}
		
	}
}
