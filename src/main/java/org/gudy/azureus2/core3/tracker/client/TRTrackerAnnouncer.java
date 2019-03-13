/*
 * File    : TRTrackerClient.java
 * Created : 5 Oct. 2003
 * By      : Parg 
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

package org.gudy.azureus2.core3.tracker.client;

import java.util.Map;
import java.net.URL;

import org.gudy.azureus2.core3.torrent.*;
import org.gudy.azureus2.core3.util.IndentWriter;
import org.gudy.azureus2.plugins.download.DownloadAnnounceResult;

import com.aelitis.azureus.core.tracker.TrackerPeerSource;

public interface 
TRTrackerAnnouncer 
{
		
		
	
	byte AZ_TRACKER_VERSION_1	= 1;	// anything before 2 ;)
	byte AZ_TRACKER_VERSION_2	= 2;	// supports azcompact
	byte AZ_TRACKER_VERSION_3	= 3;	// supports experimental alternative secret for crypto
	
	byte AZ_TRACKER_VERSION_CURRENT	= AZ_TRACKER_VERSION_3;
	
	int REFRESH_MINIMUM_SECS		= 60;
	int DEFAULT_PEERS_TO_CACHE		= 512;
	
	int TS_INITIALISED		= 1;
	int TS_DOWNLOADING		= 2;
	int TS_COMPLETED		= 3;
	int TS_STOPPED			= 4;

	
	void
	setAnnounceDataProvider(
            TRTrackerAnnouncerDataProvider provider);
	
	TOTorrent
	getTorrent();
	
	URL
	getTrackerURL();
	
	void
	setTrackerURL(
            URL url);
			
	void
	resetTrackerUrl(
            boolean shuffle);
	
	void
	setIPOverride(
            String override);
		
	void
	clearIPOverride();
	
	byte[]
	getPeerId();
	
	void
	setRefreshDelayOverrides(
            int percentage);
	
	int
	getTimeUntilNextUpdate();
	
	/** 
	 * Last Update Time in seconds
	 */
    int
	getLastUpdateTime();
			
	void
	update(
            boolean force);
	
	void
	complete(
            boolean already_reported);
	
	void
	stop(
            boolean for_queue);
	
	void
	destroy();
	
	int
	getStatus();
	
	boolean
	isManual();
	
	String
	getStatusString();
	
	TRTrackerAnnouncer
	getBestAnnouncer();
	
	TRTrackerAnnouncerResponse
	getLastResponse();
	
		/**
		 * returns a Map containing "bencoded" entries representing a cache of tracker
		 * responses.
		 * @return
		 */

        Map
	getTrackerResponseCache();
	
		/**
		 * sets the response cache. This may be used by the tracker client to return peer
		 * details when the tracker is offline 
		 * @param map
		 */

        void
	setTrackerResponseCache(
                Map map);
	
		/**
		 * remove a specific entry from the cache if present
		 * @param ip
		 * @param tcp_port
		 */

        void
	removeFromTrackerResponseCache(
                String ip,
                int tcp_port);
	
		/**
		 * Gets a delegate tracker peer source for reporting against
		 * @param set
		 * @return
		 */

        TrackerPeerSource
	getTrackerPeerSource(
                TOTorrentAnnounceURLSet set);
	
	TrackerPeerSource
	getCacheTrackerPeerSource();

	/**
	 * This method forces all listeners to get an explicit "urlChanged" event to get them
	 * to re-examine the tracker
	 */

    void
	refreshListeners();
	
	void
	setAnnounceResult(
            DownloadAnnounceResult result);
	
	void
	addListener(
            TRTrackerAnnouncerListener l);
		
	void
	removeListener(
            TRTrackerAnnouncerListener l);
	
	void
	generateEvidence(
            IndentWriter writer);
}
