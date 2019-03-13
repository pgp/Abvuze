/*
 * File    : PEPeerControl.java
 * Created : 21-Oct-2003
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
 
package org.gudy.azureus2.core3.peer.impl;

/**
 * @author parg
 *
 */


import java.util.Map;

import org.gudy.azureus2.core3.peer.PEPeer;
import org.gudy.azureus2.core3.peer.PEPeerManager;

import com.aelitis.azureus.core.peermanager.peerdb.PeerItem;


public interface
PEPeerControl
	extends PEPeerManager
{
	boolean
	validateReadRequest(
            PEPeerTransport originator,
            int pieceNumber,
            int offset,
            int length);

	boolean
	validateHintRequest(
            PEPeerTransport originator,
            int pieceNumber,
            int offset,
            int length);
	
	void
	havePiece(
            int pieceNumber,
            int pieceLength,
            PEPeer pcOrigin);

	void
	updateSuperSeedPiece(
            PEPeer peer,
            int pieceNumber);
	
	boolean
	isPrivateTorrent();
	
	int
	getExtendedMessagingMode();
  
	boolean
	isPeerExchangeEnabled();
	
	byte[][]
	getSecrets(
            int crypto_level);
	
	int
	getUploadPriority();
	
	int
	getHiddenPiece();
	
	void addPeerTransport(PEPeerTransport transport);
	
	int
	getConnectTimeout(
            int ct_def);
	
	int[]
	getMaxConnections();
    
    boolean
    doOptimisticDisconnect(
            boolean pending_lan_local_peer,
            boolean force,
            String network);
    
	int getNbActivePieces();

	int getNbPeersStalledPendingLoad();
	
	// Snubbed peers accounting
    void incNbPeersSnubbed();
	void decNbPeersSnubbed();
	void setNbPeersSnubbed(int n);
	int getNbPeersSnubbed();
	
	void
	badPieceReported(
            PEPeerTransport originator,
            int piece_number);
	
	boolean
	isFastExtensionPermitted(
            PEPeerTransport originator);
	
	void
	reportBadFastExtensionUse(
            PEPeerTransport originator);
	
	void
	statsRequest(
            PEPeerTransport originator,
            Map request);
	
	void
	statsReply(
            PEPeerTransport originator,
            Map reply);
	
	boolean isRTA();
	
	void
	peerDiscovered(
            PEPeerTransport finder,
            PeerItem pi);
}