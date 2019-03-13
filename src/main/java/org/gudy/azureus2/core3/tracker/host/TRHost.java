/*
 * File    : TRHost.java
 * Created : 24-Oct-2003
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
 
package org.gudy.azureus2.core3.tracker.host;

/**
 * @author parg
 */

import java.net.InetAddress;

import org.gudy.azureus2.core3.torrent.*;
import org.gudy.azureus2.core3.tracker.server.*;

public interface 
TRHost
{
	int DEFAULT_MIN_RETRY_DELAY 		= TRTrackerServer.DEFAULT_MIN_RETRY_DELAY;
	int DEFAULT_MAX_RETRY_DELAY 		= TRTrackerServer.DEFAULT_MAX_RETRY_DELAY;
	int DEFAULT_INC_BY					= TRTrackerServer.DEFAULT_INC_BY;
	int DEFAULT_INC_PER			 		= TRTrackerServer.DEFAULT_INC_PER;
	int DEFAULT_SCRAPE_RETRY_PERCENTAGE	= TRTrackerServer.DEFAULT_SCRAPE_RETRY_PERCENTAGE;

	int	DEFAULT_SCRAPE_CACHE_PERIOD				= TRTrackerServer.DEFAULT_SCRAPE_CACHE_PERIOD;
	int	DEFAULT_ANNOUNCE_CACHE_PERIOD			= TRTrackerServer.DEFAULT_ANNOUNCE_CACHE_PERIOD;
	int	DEFAULT_ANNOUNCE_CACHE_PEER_THRESHOLD	= TRTrackerServer.DEFAULT_ANNOUNCE_CACHE_PEER_THRESHOLD;
	
	int DEFAULT_PORT 					= TRTrackerServer.DEFAULT_TRACKER_PORT;
	int DEFAULT_PORT_SSL				= TRTrackerServer.DEFAULT_TRACKER_PORT_SSL;

	void
	initialise(
            TRHostTorrentFinder finder);
		
	String
	getName();
	
	InetAddress
	getBindIP();
	
	TRHostTorrent
	hostTorrent(
            TOTorrent torrent,
            boolean persistent,
            boolean passive)
	
		throws TRHostException;
	
	TRHostTorrent
	publishTorrent(
            TOTorrent torrent)
		
		throws TRHostException;
				
	TRHostTorrent[]
	getTorrents();
	
		/**
		 * returns the host torrent for the torrent if it exists, null otherwise
		 * @param torrent
		 * @return
		 */

        TRHostTorrent
	getHostTorrent(
                TOTorrent torrent);
	
	void
	addListener(
            TRHostListener l);
		
	void
	removeListener(
            TRHostListener l);
	
	void
	addListener2(
            TRHostListener2 l);
		
	void
	removeListener2(
            TRHostListener2 l);
	
	void
	addAuthenticationListener(
            TRHostAuthenticationListener l);
	
	void
	removeAuthenticationListener(
            TRHostAuthenticationListener l);
	
	void
	close();
}
