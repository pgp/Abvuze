/*
 * Created on 15-Dec-2005
 * Created by Paul Gardner
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

package com.aelitis.azureus.plugins.extseed;

import java.net.URL;
import java.util.List;

import org.gudy.azureus2.plugins.peers.Peer;
import org.gudy.azureus2.plugins.peers.PeerManager;
import org.gudy.azureus2.plugins.peers.PeerReadRequest;
import org.gudy.azureus2.plugins.torrent.Torrent;

public interface 
ExternalSeedReader 
{
	Torrent
	getTorrent();
	
	String
	getName();
	
	String
	getType();
	
	String
	getStatus();
	
		// transient peers are moved from the download on failure
	
	boolean
	isTransient();
	
	boolean
	isPermanentlyUnavailable();
	
	URL
	getURL();
	
	String
	getIP();
	
	int
	getPort();
	
	boolean
	isActive();
	
	boolean
	sameAs(
            ExternalSeedReader other);
	
	boolean
	checkActivation(
            PeerManager peer_manager,
            Peer peer);
	
	void
	addRequests(
            List<PeerReadRequest> requests);
	
	void
	cancelRequest(
            PeerReadRequest request);
	
	int
	getMaximumNumberOfRequests();

	void
	calculatePriorityOffsets(
            PeerManager peer_manager,
            int[] base_priorities);
	
	int[]
	getPriorityOffsets();

	void
	cancelAllRequests();
	
	int
	getRequestCount();
	
	List<PeerReadRequest>
	getExpiredRequests();
	
	List<PeerReadRequest>
	getRequests();
	
	int
	readBytes(
            int max);
	
	int
	getPercentDoneOfCurrentIncomingRequest();
	
	int[]
   	getOutgoingRequestedPieceNumbers();

   	int
   	getOutgoingRequestCount();
	       	
	byte[]
	read(
            int piece_number,
            int offset,
            int length,
            int timeout)
	
		throws ExternalSeedException;
	
	void
	deactivate(
            String reason);
	
	void
	addListener(
            ExternalSeedReaderListener l);
	
	void
	removeListener(
            ExternalSeedReaderListener l);
}
