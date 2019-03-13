/*
 * File    : PEPeerTransport
 * Created : 15-Oct-2003
 * By      : Olivier
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
 
  /*
 * Created on 4 juil. 2003
 *
 */
package org.gudy.azureus2.core3.peer.impl;

import java.util.List;
import java.util.Map;

import org.gudy.azureus2.core3.disk.DiskManagerReadRequest;
import org.gudy.azureus2.core3.peer.PEPeer;
import org.gudy.azureus2.core3.util.IndentWriter;

import com.aelitis.azureus.core.peermanager.peerdb.PeerItem;

public interface
PEPeerTransport
	extends PEPeer
{	

  int CONNECTION_PENDING                = 0;
  int CONNECTION_CONNECTING             = 1;
  int CONNECTION_WAITING_FOR_HANDSHAKE  = 2;
  int CONNECTION_FULLY_ESTABLISHED      = 4;
  
  		/**
  		 * Start message processing for the peer
  		 */

        void
  	start();
  	
	void
	sendChoke();
	
	void
	sendUnChoke();
	
	void
	sendHave(
            int piece);
		
	void
	sendCancel(
            DiskManagerReadRequest request);
	
	void
	sendBadPiece(
            int piece_number);
	
	void
	sendStatsRequest(
            Map request);
	
	void
	sendStatsReply(
            Map reply);
			
		/**
		 * Two methods that allow a peer to aggregate the individual requests generated during an
		 * allocation cycle if so desired
		 * @return true if the peer is managing request priorities and doesn't want end-game random
		 * allocation behaviour
		 */

        boolean
	requestAllocationStarts(
                int[] base_priorities);
	
	void
	requestAllocationComplete();
	
  /**
   * 
   * @param pieceNumber
   * @param pieceOffset
   * @param pieceLength
   * @param return_duplicates - if true and request already exists it will be returned, if false -> null
   * @return request if actually requested, null otherwise
   */

  DiskManagerReadRequest
	request(
          int pieceNumber,
          int pieceOffset,
          int pieceLength,
          boolean return_duplicates);

	/**
	 * Returns the index of this request in the peer's queue or -1 if not found
	 * @return
	 */

    int
	getRequestIndex(
            DiskManagerReadRequest request);
  
  /**
   * Close the peer connection
   * @param reason for closure
   */
  void closeConnection(String reason);
			
		
	boolean
	transferAvailable();
	
	long
	getLastMessageSentTime();
	
	List
	getExpiredRequests();
  	
		/**
		 * peer-specific request max. return -1 to use the default piece-picker allocation method
		 * @return
		 */

        int
	getMaxNbRequests();
	
	int
	getNbRequests();
	
	PEPeerControl
	getControl();
  
		/**
		 * Any priority offsets this peer has, or null if none
		 * @return
		 */

        int[]
	getPriorityOffsets();
  
	/**
	 * Check if we need to send a keep-alive message.
	 * A keep-alive is sent if no other message has been sent within the last 2min.
	 */
    void doKeepAliveCheck();
  
  /**
   * Check for possible connection timeouts.
   * @return true if the connection has been timed-out, false if not
   */
  boolean doTimeoutChecks();

  
  /**
   * Perform checks related to performance optimizations,
   * i.e. tune buffering related to send/receive speed.
   */
  void doPerformanceTuningCheck();
  
  
  /**
   * Get the specific peer connection state.
   * @return connection state
   */
  int getConnectionState();
  
  
  /**
   * Get the time since the last (most-recent) data (payload) message was received.
   * @return time count in ms, or -1 if we've never received a data message from them
   */
  long getTimeSinceLastDataMessageReceived();
  
  /**
   * Get the time since the most-recent data that was actually written to disk was received.
   * @return time count in ms, or -1 if we've never received usefull data from them
   */
  long getTimeSinceGoodDataReceived();
  
  /**
   * Get the time since the last (most-recent) data (payload) message was sent.
   * @return time count in ms, or -1 if we've never sent them a data message
   */
  long getTimeSinceLastDataMessageSent();
  
  
  long getUnchokedForMillis();
  
  long getLatency();
  
  /**
   * Do any peer exchange processing/updating.
   */
  void updatePeerExchange();
  
  
  /**
   * Get the peer's address + port identification item.
   * @return id
   */
  PeerItem getPeerItemIdentity();
  
  /**
   * is peer waiting for a disk read with no network writes queued
   * @return
   */

  boolean isStalledPendingLoad();
  
  /**
   * Is the connection within the local LAN network.
   * @return true if within LAN, false of outside the LAN segment
   */
  boolean isLANLocal();
  
  boolean
  isTCP();
  
	/**
	 * if it doesn't go as expected when trying to find a piece to ask a peer for,
	 * need to double check if we're still interested in them, and get the BT protocol sycnhed
	 */
    void checkInterested();
	
		/**
		 * Attempts to reconnect to the same peer
		 * @param tryUDP try to initate a UDP connection if true, just reestablish the previous state otherwise
		 * @param tryIPv6 TODO
		 * @return null if reconnect not possible, reconnected peer otherwise
		 */

        PEPeerTransport
	reconnect(boolean tryUDP, boolean tryIPv6);
	
	/**
	 * This method is called to check if it is safe to reconnect to a peer, i.e. avoid hammering
	 * exchanging data with a peer should work as it takes time to setup the connection and negotiate things before that happens
	 * @return true if we exchanged payload data with the peer during the current connection
	 */
    boolean isSafeForReconnect();
	
	String
	getNetwork();
	
	void
	generateEvidence(
            IndentWriter writer);
}