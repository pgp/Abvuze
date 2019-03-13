/*
 * File    : TRTrackerServer.java
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

package org.gudy.azureus2.core3.tracker.server;

import java.net.InetAddress;
import java.util.Set;

import org.gudy.azureus2.core3.util.Constants;

public interface 
TRTrackerServer 
{
	String	DEFAULT_NAME	= Constants.APP_NAME;
	
	int DEFAULT_MIN_RETRY_DELAY 		= 120;
	int DEFAULT_MAX_RETRY_DELAY 		= 3600;
	int DEFAULT_INC_BY					= 60;
	int DEFAULT_INC_PER			 		= 10;
	int DEFAULT_SCRAPE_RETRY_PERCENTAGE	= 200;
	
	int	DEFAULT_SCRAPE_CACHE_PERIOD				= 5000;
	int	DEFAULT_ANNOUNCE_CACHE_PERIOD			= 500;
	int	DEFAULT_ANNOUNCE_CACHE_PEER_THRESHOLD	= 500;
	
	int DEFAULT_TRACKER_PORT 		= 6969;
	int DEFAULT_TRACKER_PORT_SSL	= 7000;
		
	int DEFAULT_NAT_CHECK_SECS		= 15;
	
	String
	getName();
	
	int
	getPort();
	
	String
	getHost();
	
	InetAddress
	getBindIP();
	
	void
	setReady();
	
	void
	setEnabled(
            boolean enabled);
	
	boolean
	isSSL();
	
	void
	setEnableKeepAlive(
            boolean enable);
	
	TRTrackerServerTorrent
	permit(
            String originator,
            byte[] hash,
            boolean explicit)
		
		throws TRTrackerServerException;
		
	TRTrackerServerTorrent
	permit(
            String originator,
            byte[] hash,
            boolean explicit,
            boolean enabled)
		
		throws TRTrackerServerException;
	
	void
	deny(
            byte[] hash,
            boolean explicit)
		
		throws TRTrackerServerException;
		
	TRTrackerServerTorrentStats
	getStats(
            byte[] hash);
		
	TRTrackerServerPeer[]
	getPeers(
            byte[] hash);

	TRTrackerServerStats
	getStats();
	
	void
	setBiasedPeers(
            Set ips);
	
	void
	addListener(
            TRTrackerServerListener l);
		
	void
	removeListener(
            TRTrackerServerListener l);
	
	void
	addListener2(
            TRTrackerServerListener2 l);
		
	void
	removeListener2(
            TRTrackerServerListener2 l);
	
	
	void
	addRequestListener(
            TRTrackerServerRequestListener l);
	
	void
	removeRequestListener(
            TRTrackerServerRequestListener l);
	
	void
	addAuthenticationListener(
            TRTrackerServerAuthenticationListener l);
	
	void
	removeAuthenticationListener(
            TRTrackerServerAuthenticationListener l);
	
	void
	close();
}
