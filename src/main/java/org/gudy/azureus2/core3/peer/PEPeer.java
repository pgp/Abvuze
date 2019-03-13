/*
 * File    : PEPeerSocket.java
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
package org.gudy.azureus2.core3.peer;


import java.net.InetAddress;
import java.util.Map;

import org.gudy.azureus2.core3.disk.DiskManagerReadRequest;
import org.gudy.azureus2.plugins.network.Connection;

import com.aelitis.azureus.core.networkmanager.LimitedRateGroup;
import com.aelitis.azureus.core.peermanager.messaging.Message;
import com.aelitis.azureus.core.peermanager.piecepicker.util.BitFlags;
import com.aelitis.azureus.core.tag.Taggable;


/**
 * @author Olivier
 * @author MjrTom
 *			2005/Oct/08: lastPiece handling
 *
 */

public interface 
PEPeer 
	extends Taggable
{
	int CONNECTING 		= 10;
	int HANDSHAKING 	= 20;
	int TRANSFERING 	= 30;
	int CLOSING      	= 40;
	int DISCONNECTED 	= 50;
	
	
	// these should be maintained to match above list ordinals /10
	// if they don't than some debug info won't work right (not too big of a deal)
    String[] StateNames = { "Twinkle",
		"Connecting", "Handshaking", "Transfering", "Closing", "Disconnected"
	};
	
	int MESSAGING_BT_ONLY = 1;
	int MESSAGING_AZMP = 2;
	int MESSAGING_LTEP = 3;
	//used for plugins, such as webseeds
    int MESSAGING_EXTERN = 4;
	
	
  /**
   * Add peer listener.
   * @param listener
   */
  void addListener(PEPeerListener listener);
  
  
  /**
   * Remove peer listener.
   * @param listener
   */
  void removeListener(PEPeerListener listener);
  
  
	int getPeerState();	// from above set
  
	PEPeerManager
	getManager();

	String
	getPeerSource();
	
	byte[] getId();

	String getIp();
	
	/**
	 * @return an ipv6 address under which the peer should be connectable if it announced one, null otherwise
	 */
    InetAddress getAlternativeIPv6();
  
  /**
   * Get the peer's local TCP connection port.
   * @return local port
   */
  int getPort();
 
	/**
	 * Gets the host name for the IP, if possible, IP as string otherwise
   * @return hostname or IP
   */
    String getIPHostName();

  
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

  int getUDPNonDataListenPort();
  
	BitFlags getAvailable();
	/**
	 * @param pieceNumber int
	 * @return true if this peers makes this piece available
	 */
    boolean isPieceAvailable(int pieceNumber);

	boolean
	transferAvailable();
	
	void setSnubbed(boolean b);	// explicit un-snub
  
  /**
   * Is the peer choking me.
   * @return true if I am choked by the peer, false if not
   */
  boolean isChokingMe();
	
	boolean isUnchokeOverride();

  /**
   * Am I choking the peer.
   * @return true if the peer is choked, false if not
   */
  boolean isChokedByMe();

	void
	sendChoke();
	
	void
	sendUnChoke();
	
  /**
   * Am I Interested in the peer.
   * @return true if peer is interesting, false if not
   */
  boolean isInteresting();

  /**
   * Is the peer Interested in me.
   * @return true if the peer is interested in me, false if not
   */
  boolean isInterested();

	/**
	 * checks several factors within the object so the caller wouldn't need to
	 * for convienience and speed.
	 * @return true if none of several criteria indicate a request can't be made of the peer  
	 */
    boolean isDownloadPossible();
	
	boolean isSeed();
	
	/**
	 * @return true if the peer is only uploading and can't use the data we have
	 *         to offer (we're seeding) or we can't use the data he has to offer
	 *         (we're downloading)
	 */
    boolean isRelativeSeed();
 
	boolean isSnubbed();
	
	long getSnubbedTime();
 
	PEPeerStats getStats();
 	
	boolean isIncoming();

	boolean hasReceivedBitField();
	
  /**
   * Get the peer's torrent completion percentage in thousand-notation,
   * i.e. 53.7% is returned as the value 0537.
   * @return the percentage the peer has complete
   */
  int getPercentDoneInThousandNotation();

  
	String getClient();

	boolean isOptimisticUnchoke();
  void setOptimisticUnchoke(boolean is_optimistic);
	
	//Used in super-seed mode
	//The lower the better
    void setUploadHint(int timeToSpread);
	
	int getUploadHint();
	
	void setUniqueAnnounce(int uniquePieceNumber);
	
	int getUniqueAnnounce();
   
	int getConsecutiveNoRequestCount();
	void setConsecutiveNoRequestCount(int num);

	void setUploadRateLimitBytesPerSecond(int bytes);
	void setDownloadRateLimitBytesPerSecond(int bytes);
	int getUploadRateLimitBytesPerSecond();
	int getDownloadRateLimitBytesPerSecond();
	
	void
	addRateLimiter(
            LimitedRateGroup limiter,
            boolean upload);
	
	LimitedRateGroup[]
	getRateLimiters(
            boolean upload);
	
	void
	removeRateLimiter(
            LimitedRateGroup limiter,
            boolean upload);
	
	void
	setUploadDisabled(
            Object key,
            boolean disabled);
	
	void
	setDownloadDisabled(
            Object key,
            boolean disabled);

	boolean
	isUploadDisabled();
	
	boolean
	isDownloadDisabled();
	
	void
	updateAutoUploadPriority(
            Object key,
            boolean inc);
	
  /** To retreive arbitrary objects against a peer. */
  Object getData(String key);
  /** To store arbitrary objects against a peer. */
  void setData(String key, Object value);
  
  Object getUserData(Object key);
  /** To store arbitrary objects against a peer. */
  void setUserData(Object key, Object value);

  /**
   * Get the connection that backs this peer.
   * @return connection
   */

  Connection getPluginConnection();
  
  
  /**
   * Whether or not this peer supports the advanced messaging API.
   * @return true if extended messaging is supported, false if not
   */
  boolean supportsMessaging();
  
  /**
   * @Return the handshaked messaging type, {@link PEPeer} constants
   */
  int getMessagingMode();

  
  
  /**
   * Returns name of encryption used by the peer
   * @return
   */
  String
  getEncryption();
  
  String
  getProtocol();
  
  String
  getProtocolQualifier();
  
  /**
   * Get the list of messages that this peer and us both understand.
   * @return messages available for use, or null of supported is yet unknown or unavailable
   */
  Message[] getSupportedMessages();
  
  /**
   * adds the reserved piece for piece picking by this peer
   */
  void addReservedPieceNumber(int pieceNumber);
  
  void removeReservedPieceNumber(int pieceNumber);
  
  /**
   * Get the reserved pieces for piece picking by this peer
   */
  int[] getReservedPieceNumbers();
  
  int getIncomingRequestCount();
  int getOutgoingRequestCount();
  
  /**
   * amount of data queued for delivery to peer
   * @return
   */

  int getOutboundDataQueueSize();
  
  /**
   * get a list of piece numbers the peer has requested
   * @return list of Long() representing the piece number requested, in order
   */
  int[] getIncomingRequestedPieceNumbers();
  
  /**
   * get a list of piece numbers the we have requested from peer
   * @return list of Long() representing the piece number requested, oldest
   *          to newest
   */
  int[] getOutgoingRequestedPieceNumbers();
  
  int
  getPercentDoneOfCurrentIncomingRequest();
  
  int
  getPercentDoneOfCurrentOutgoingRequest();
  
  long
  getBytesRemaining();
  
  	/**
  	 * Enable suspended lazy bitfield for this peer - we will appear incomplete until this is disabled, at
  	 * which point (well, fairly soon after) the bitfield will be completed  
  	 * @param enable
  	 */

    void
  setSuspendedLazyBitFieldEnabled(
            boolean enable);
  
  /**
   * Get the time since this connection was first established.
   * NOTE: This method will always return 0 at any time before
   * the underlying transport is fully connected, i.e. before
   * handshaking begins.
   * @return time count in ms
   */
  long getTimeSinceConnectionEstablished();

	void setLastPiece(int i);
	int getLastPiece();
	
	boolean
	isLANLocal();
	
		/**
		 * Send a request hint to the peer. 
		 * @param piece_number
		 * @param offset
		 * @param length
		 * @param life
		 * @return true if sent, false otherwise
		 */

        boolean
	sendRequestHint(
                int piece_number,
                int offset,
                int length,
                int life);
	
		/**
		 * Get current request hint for a given piece for this peer. 
		 * @return null if no hint int[]{ piece_number, offset, length } if hint found
		 */

        int[]
	getRequestHint();
	        		 
	void
	clearRequestHint();

	void
	sendStatsRequest(
            Map request);
	
	void
	sendRejectRequest(
            DiskManagerReadRequest request);
	
	void
	setHaveAggregationEnabled(
            boolean enabled);
	
	byte[] getHandshakeReservedBytes();
	
	String getClientNameFromPeerID();
	String getClientNameFromExtensionHandshake();
	
	boolean isPriorityConnection();
	
	void setPriorityConnection(boolean is_priority);
	
	boolean
	isClosed();
}