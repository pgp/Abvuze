/*
 * Created on 11-Dec-2005
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

package org.gudy.azureus2.core3.peer;

import java.util.Map;

import org.gudy.azureus2.core3.disk.DiskManagerReadRequest;
import org.gudy.azureus2.core3.disk.DiskManagerReadRequestListener;
import org.gudy.azureus2.core3.logging.LogRelation;
import org.gudy.azureus2.core3.tracker.client.TRTrackerScraperResponse;

import com.aelitis.azureus.core.peermanager.PeerManagerRegistration;

public interface 
PEPeerManagerAdapter 
{
	String
	getDisplayName();
	
	int
	getUploadRateLimitBytesPerSecond();
	
	int
	getDownloadRateLimitBytesPerSecond();
	
	int
	getMaxUploads();
	
	int[]
	getMaxConnections();
	
	int[]
	getMaxSeedConnections();
	
	int
	getExtendedMessagingMode();
	
	boolean
	isPeerExchangeEnabled();
	
	boolean
	isMetadataDownload();
	
	int
	getUploadPriority();
	
	int
	getTorrentInfoDictSize();
	
	byte[]
	getTorrentInfoDict(
            PEPeer peer);
		/**
		 * See NetworkManager.CRYPTO_OVERRIDE constants
		 * @return
		 */

        int
	getCryptoLevel();
	
	long
	getRandomSeed();
	
	boolean
	isPeriodicRescanEnabled();
	
	void
	setStateFinishing();
	
	void
	setStateSeeding(
            boolean never_downloaded);
	
	void
	restartDownload(boolean forceRecheck);
	
	TRTrackerScraperResponse
	getTrackerScrapeResponse();
	
	String
	getTrackerClientExtensions();
	
	void
	setTrackerRefreshDelayOverrides(
            int percent);
	
	boolean
	isNATHealthy();
	
	void
	addPeer(
            PEPeer peer);
	
	void
	removePeer(
            PEPeer peer);
	
	void
	addPiece(
            PEPiece piece);
	
	void
	removePiece(
            PEPiece piece);
	
	void
	discarded(
            PEPeer peer,
            int bytes);
	
	void
	protocolBytesReceived(
            PEPeer peer,
            int bytes);
	
	void
	dataBytesReceived(
            PEPeer peer,
            int bytes);
	
	void
	protocolBytesSent(
            PEPeer peer,
            int bytes);
	
	void
	dataBytesSent(
            PEPeer peer,
            int bytes);
	
	void
	statsRequest(
            PEPeer originator,
            Map request,
            Map reply);

	PeerManagerRegistration
	getPeerManagerRegistration();
	
	void
	addHTTPSeed(
            String address,
            int port);
	
	byte[][]
	getSecrets(
            int crypto_level);
	
	void
	enqueueReadRequest(
            PEPeer peer,
            DiskManagerReadRequest request,
            DiskManagerReadRequestListener listener);
	
	LogRelation
	getLogRelation();
	
	int getPosition();
	
	boolean
	isPeerSourceEnabled(
            String peer_source);
	
	boolean
	isNetworkEnabled(
            String network);
	
	String[]
	getEnabledNetworks();
	
	void
	priorityConnectionChanged(
            boolean added);
	
	boolean
	hasPriorityConnection();
	
	int getPermittedBytesToReceive();
	void permittedReceiveBytesUsed(int bytes);
	
	int getPermittedBytesToSend();
	void	permittedSendBytesUsed(int bytes);
}
