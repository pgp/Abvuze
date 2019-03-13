/*
 * File    : PEPeerManager
 * Created : 5 Oct. 2003
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

package org.gudy.azureus2.core3.peer;

/**
 * @author parg
 * @author MjrTom
 *			2005/Oct/08: pieceAdded => addPiece to simplify new piece-picking, getAvgAvail
 *
 */

import java.util.*;

import org.gudy.azureus2.core3.disk.*;
import org.gudy.azureus2.core3.peer.impl.PEPeerTransport;
import org.gudy.azureus2.core3.peer.util.PeerIdentityDataID;
import org.gudy.azureus2.core3.tracker.client.TRTrackerAnnouncerResponse;
import org.gudy.azureus2.core3.util.DirectByteBuffer;
import org.gudy.azureus2.core3.util.IndentWriter;
import org.gudy.azureus2.plugins.peers.PeerDescriptor;

import com.aelitis.azureus.core.networkmanager.LimitedRateGroup;
import com.aelitis.azureus.core.peermanager.peerdb.PeerExchangerItem;
import com.aelitis.azureus.core.peermanager.piecepicker.PiecePicker;
import com.aelitis.azureus.core.tracker.TrackerPeerSource;


public interface 
PEPeerManager 
{
	DiskManager getDiskManager();
	PiecePicker getPiecePicker();
	
	PEPeerManagerAdapter	getAdapter();
	
	void
	start();
		
	void
	stopAll();

	byte[]
	getHash();

	String
	getDisplayName();
	
	PeerIdentityDataID
	getPeerIdentityDataID();
	
	byte[]
	getPeerId();

	int[] getAvailability();

	int getAvailability(int pieceNumber);
	
	float getAvgAvail();

	float getMinAvailability();

	float getMinAvailability(int file_index);

	long getAvailWentBadTime();

	long getBytesUnavailable();
	
	boolean hasDownloadablePiece();
	
    int	getBytesQueuedForUpload();
    int	getNbPeersWithUploadQueued();
    int	getNbPeersWithUploadBlocked();
    int	getNbPeersUnchoked();
    
    /** Often better to use getPiece(pieceNumber)
     */
    PEPiece[]	getPieces();

    /** @return PEPiece or null if piece not currently active
     */
    PEPiece		getPiece(int pieceNumber);

    
	PEPeerManagerStats
	getStats();

	void
	processTrackerResponse(
            TRTrackerAnnouncerResponse response);
		
	int getNbPeers();

	int getNbSeeds();
	
	int getPieceLength(int pieceNumber);
		
	long getRemaining();
	
	long getHiddenBytes();
	
	long getETA(boolean smoothed);
	
	String getElapsedTime();
	
	// Time Started in ms
    long getTimeStarted(boolean mono_time);

	long getTimeStartedSeeding(boolean mono_time);
	
	void
	addListener(
            PEPeerManagerListener l);
		
	void
	removeListener(
            PEPeerManagerListener l);
  
	void addPiece(PEPiece piece, int pieceNumber, PEPeer for_peer);
  
  boolean needsMD5CheckOnCompletion(int pieceNumber);
  
  boolean
  isSeeding();
  
  boolean
  isMetadataDownload();
	
  int
  getTorrentInfoDictSize();
  
  void
  setTorrentInfoDictSize(
          int size);
  
  boolean
  isSuperSeedMode();
  
  boolean
  canToggleSuperSeedMode();
  
  void
  setSuperSeedMode(boolean on);
  
  boolean
  seedPieceRecheck();
  
  int getNbRemoteTCPConnections();
  int getNbRemoteUDPConnections();
  int getNbRemoteUTPConnections();
  
  long getLastRemoteConnectionTime();
  
  int
  getMaxNewConnectionsAllowed(String network);
  
  boolean
  hasPotentialConnections();
  
  /**
   * Data bytes received.
   * @param l
   */
  void	dataBytesReceived(PEPeer peer, int l);
	
  /**
   * Data bytes sent.
   * @param l
   */
  void	dataBytesSent(PEPeer peer, int l);
	
  /**
   * Protocol bytes sent.
   * @param length
   */
  void protocolBytesSent(PEPeer peer, int length);
  
  /**
   * Protocol bytes received.
   * @param length
   */
  void protocolBytesReceived(PEPeer peer, int length);
  
  
  
	void
	discarded(
            PEPeer peer,
            int l);
	
	PEPeerStats
	createPeerStats(
            PEPeer owner);
	
	List<PEPeer>
	getPeers();
	
	List<PEPeer>
	getPeers(
            String address);
	
	int
	getPendingPeerCount();
	
	PeerDescriptor[]
   	getPendingPeers();
	
	PeerDescriptor[]
	getPendingPeers(
            String address);
	
	void
	addPeer(
            PEPeer peer);
  
  
  /**
   * Add a new peer, using the default internal PEPeer implementation
   * (like for peers given in announce reply), using the given address
   * and port.
   * @param ip_address of peer to inject
   * @param tcp_port of peer to inject
   * @param udp_port of peer to inject (0 if unknown)
   * @param use_crypto use encrypted transport
   */

  void
	addPeer(
          String ip_address,
          int tcp_port,
          int udp_port,
          boolean use_crypto,
          Map user_data);
  
	void
	peerDiscovered(
            String peer_source,
            String ip_address,
            int tcp_port,
            int udp_port,
            boolean use_crypto);
	
	void
	removePeer(
            PEPeer peer);
	
	void
	removePeer(
            PEPeer peer,
            String reason);
	
	void
	peerAdded(PEPeer pc);

	void
	peerRemoved(PEPeer pc);
	
	DiskManagerReadRequest
	createDiskManagerRequest(
            int pieceNumber,
            int offset,
            int length);
	
	void
	requestCanceled(
            DiskManagerReadRequest item);
		
	boolean
	requestExists(
            String peer_ip,
            int piece_number,
            int offset,
            int length);
	
	boolean
	validatePieceReply(
            PEPeerTransport originator,
            int pieceNumber,
            int offset,
            DirectByteBuffer data);
	
	void
	writeBlock(
            int pieceNumber,
            int offset,
            DirectByteBuffer data,
            Object sender,            // either a PEPeer or a String
            boolean cancel);
  
//  public void writeBlockAndCancelOutstanding(int pieceNumber, int offset, DirectByteBuffer data,PEPeer sender);
  
  boolean isWritten(int piece_number, int offset);

  /**
   * Are we in end-game mode?
   * @return true if in end game mode, false if not
   */
  boolean isInEndGameMode();
  
  /**
   * Notify the manager that the given peer connection has been closed.
   * @param peer closed
   */
  void peerConnectionClosed(PEPeerTransport peer, boolean connect_failed, boolean network_failed);
  
  
  
  /**
   * Register a peer connection for peer exchange handling.
   * NOTE: Creation could fail if the peer is not eligible for peer exchange (like if it's remote port is unknown).
   * @param base_peer exchaning with
   * @return peer database connection item, or null if creation failed
   */
  PeerExchangerItem createPeerExchangeConnection(PEPeerTransport base_peer);
  
  
  /**
   * Notify that the given peer connection represents our own client.
   * @param self peer
   */
  void peerVerifiedAsSelf(PEPeerTransport self);
  
  
  /**
   * Get the limited rate group used for upload limiting.
   * @return upload limit group
   */
  LimitedRateGroup getUploadLimitedRateGroup();
  
  /**
   * Get the limited rate group used for download limiting.
   * @return download limit group
   */
  LimitedRateGroup getDownloadLimitedRateGroup();
  
  int getUploadRateLimitBytesPerSecond();
  
  int getDownloadRateLimitBytesPerSecond();
  
  /** To retreive arbitrary objects against this object. */
  Object getData(String key);
  /** To store arbitrary objects against this object. */
  void setData(String key, Object value);
  
  
  /**
   * Get the average completion percentage of connected peers.
   * @return average percent complete in thousand notation
   */
  int getAverageCompletionInThousandNotation();

	/**
	 * Locate an existing transport via peer id byte identity.
	 * @param peer_id to look for
	 * @return transport with matching identity, or null if no match is found
	 */
    PEPeerTransport getTransportFromIdentity(byte[] peer_id);
	
	/**
	 * Locate an existing transport via [IP] Address.
	 * @param peer String to look for
	 * @return PEPeerTransport with matching address String, or null if no match is found
	 */
    PEPeerTransport getTransportFromAddress(String peer);
	
	boolean
	getPreferUDP();
	
	void
	setPreferUDP(
            boolean prefer);
	
	void
	addRateLimiter(
            LimitedRateGroup group,
            boolean upload);
	
	void
	removeRateLimiter(
            LimitedRateGroup group,
            boolean upload);
	
	TrackerPeerSource
	getTrackerPeerSource();
	
	boolean
	isPeerSourceEnabled(
            String peer_source);
	
	boolean
	isNetworkEnabled(
            String network);
	
	int
	getPartitionID();

	boolean
	isDestroyed();
	
	void
	generateEvidence(
            IndentWriter writer);
	
	void
	setStatsReceiver(
            StatsReceiver receiver);
	
	interface
	StatsReceiver
	{
		void
		receiveStats(
                PEPeer peer,
                Map stats);
	}

}
