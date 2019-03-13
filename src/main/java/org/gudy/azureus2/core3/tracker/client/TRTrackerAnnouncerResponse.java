/*
 * File    : TRTrackerResponse.java
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

import java.net.URL;
import java.util.Map;

import org.gudy.azureus2.core3.util.HashWrapper;

public interface 
TRTrackerAnnouncerResponse 
{
	int	ST_OFFLINE			= 0;
	int ST_REPORTED_ERROR	= 1;
	int	ST_ONLINE			= 2;
	
	/**
	 * Returns the current status of the tracker
	 * @return	see above ST_ set
	 */

    int
	getStatus();
	
	String
	getStatusString();
	
	HashWrapper
	getHash();
	
	/**
	 * This value is always available
	 * @return time to wait before requerying tracker
	 */

    long
	getTimeToWait();
	
	/**
	 * Returns any additional textual information associated with reponse. 
	 * If the status is ST_REPORTED_ERROR, this will return the error description
	 * (possibly directly from the tracker).
	 * 
	 * @return	Additional information
	 */

    String
	getAdditionalInfo();
	
	/**
	 * 
	 * @return	peers reported by tracker. this will include the local peer as well
	 */

    TRTrackerAnnouncerResponsePeer[]
	getPeers();
	
	void
	setPeers(
            TRTrackerAnnouncerResponsePeer[] peers);
	
	Map
	getExtensions();
	
	URL
	getURL();
	
	int
	getScrapeCompleteCount();
	
	int
	getScrapeIncompleteCount();
	
	int
	getScrapeDownloadedCount();
	
	void
	print();
}
