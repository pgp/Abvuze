/*
 * File    : Peer.java
 * Created : 01-Dec-2003
 * By      : parg
 * 
 * Azureus - a Java Bittorrent client
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details ( see the LICENSE file ).
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.gudy.azureus2.plugins.peers;

/**
 * @author parg
 *
 */

import java.util.List;

import org.gudy.azureus2.core3.peer.PEPeer;
import org.gudy.azureus2.plugins.messaging.Message;
import org.gudy.azureus2.plugins.network.Connection;
import org.gudy.azureus2.plugins.network.ConnectionStub;
import org.gudy.azureus2.plugins.network.RateLimiter;


public interface 
Peer 
{
	int CONNECTING 		= PEPeer.CONNECTING;
	int HANDSHAKING 	= PEPeer.HANDSHAKING;
	int TRANSFERING 	= PEPeer.TRANSFERING;
	int CLOSING 		= PEPeer.CLOSING;
	int DISCONNECTED 	= PEPeer.DISCONNECTED;
  

	Object PR_PRIORITY_CONNECTION 	= new Object();
	Object PR_PROTOCOL				= new Object();
	Object PR_PROTOCOL_QUALIFIER	= new Object();
	
	void
	bindConnection(
            ConnectionStub stub);
	
	PeerManager
	getManager();
	
	int getState();	// from above set

	byte[] getId();
  
  
	/**
	 * Get the peer's local TCP connection port.
	 * @return local port
	 */

    String getIp();

	/**
	 * Get the TCP port this peer is listening for incoming connections on.
	 * @return TCP port, or 0 if port is unknown
	 */
    int getTCPListenPort();

	/**
	 * Get the UDP port this peer is listening for incoming connections on.
	 * @return UDP port, or 0 if port is unknown
	 */
    int getUDPListenPort();

	/**
	 * Get the UDP port this peer is listening on for non-data connections
	 * @return
	 */

    int
	getUDPNonDataListenPort();

	int getPort();
	
	boolean
	isLANLocal();
	
	boolean[] getAvailable();
	/**
	 * @param pieceNumber int
	 * @return true if this peers makes this piece available
	 */
    boolean isPieceAvailable(int pieceNumber);
   
	boolean
	isTransferAvailable();
	
		/**
		 * Rate control - gives the maximum number of bytes that can be read from this
		 * connection at this time and returns the actual number read
		 * @param max
		 * @return
		 */

        int
	readBytes(
                int max);
	
	int
	writeBytes(
            int max);
	
	/**
	 * This is much list isTransferAvailable(), except is more comprehensive.
	 * That is; it checks a few more factors, within the object for speed,
	 * so that a more timely status is considered and the caller doesn't need
	 * to try to check each thing on it's own.
	 * @return true if several factors say downloading can be tried.
	 */
    boolean isDownloadPossible();
	
	boolean isChoked();

	boolean isChoking();

	boolean isInterested();

	boolean isInteresting();

	boolean isSeed();
 
	boolean isSnubbed();
	
	long getSnubbedTime();
 
	void setSnubbed(boolean b);
	
	PeerStats getStats();
 	
	boolean isIncoming();

		/**
		 * @deprecated This erroneously returns percent in 1000 (i.e. 100% = 1000 :) Therefore replaces
		 * with something more accurately named!
		 * @return
		 */

        int getPercentDone();

	int getPercentDoneInThousandNotation();
	
	String getClient();

	boolean isOptimisticUnchoke();
  
	void setOptimisticUnchoke(boolean is_optimistic);
		
	List
	getExpiredRequests();
  		
	List
	getRequests();
	
	int
	getMaximumNumberOfRequests();
	
	int
	getNumberOfRequests();

	void
	cancelRequest(
            PeerReadRequest request);

	boolean
	requestAllocationStarts(
            int[] base_priorities);
	
	int[]
	getPriorityOffsets();
	       	       	
	void
	requestAllocationComplete();
	
	boolean
	addRequest(
            PeerReadRequest request);


	void
	close(
            String reason,
            boolean closedOnError,
            boolean attemptReconnect);
	
	int
	getPercentDoneOfCurrentIncomingRequest();
		
	int[]
	getOutgoingRequestedPieceNumbers(); 
	
	int
	getOutgoingRequestCount();
	
	int
	getPercentDoneOfCurrentOutgoingRequest();
  
  /**
   * Add peer listener.
   * @param listener
   * @deprecated use addListener( PeerListener2 )
   */
  void	addListener(PeerListener listener);
	

  /**
   * Remove peer listener.
   * @param listener
   * @deprecated use removeListener( PeerListener2 )
   */
  void removeListener(PeerListener listener);
  
	  /**
	   * Add peer listener.
	   * @param listener
	   */
      void	addListener(PeerListener2 listener);
	

  /**
   * Remove peer listener.
   * @param listener
   */
  void removeListener(PeerListener2 listener);
  
  
  /**
   * Get the network connection that backs this peer.
   * @return connection
   */
  Connection getConnection();
  
  
  /**
   * Whether or not this peer supports the advanced messaging API.
   * @return true if extended messaging is supported, false if not
   */
  boolean supportsMessaging();
  
  
  /**
   * Get the list of messages that this peer and us mutually understand.
   * @return messages available for use, or null of supported is yet unknown
   */
  Message[] getSupportedMessages();

  void
  setUserData(
          Object key,
          Object value);
  
  Object
  getUserData(
          Object key);
  
  byte[] getHandshakeReservedBytes();
  
  boolean
  isPriorityConnection();
  
  void
  setPriorityConnection(
          boolean is_priority);
  
	  /**
	   * @since 4.7.0.3
	   * @param limiter		create via ConnectionManager
	   * @param is_upload		false -> download limit
	   */

      void
  addRateLimiter(
              RateLimiter limiter,
              boolean is_upload);

  void
  removeRateLimiter(
          RateLimiter limiter,
          boolean is_upload);
  
  RateLimiter[]
  getRateLimiters(
          boolean is_upload);
}
