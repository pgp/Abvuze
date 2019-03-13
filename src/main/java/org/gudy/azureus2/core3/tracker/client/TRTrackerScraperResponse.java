/*
 * File    : TRTrackerScraperResponse.java
 * Created : 09-Oct-2003
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
 
package org.gudy.azureus2.core3.tracker.client;

import java.net.URL;

import org.gudy.azureus2.core3.util.HashWrapper;


/**
 * @author parg
 *
 */
public interface 
TRTrackerScraperResponse 
{
	int	ST_INITIALIZING    = 0;
	int ST_ERROR           = 1;
	int	ST_ONLINE          = 2;
	int	ST_SCRAPING        = 3;
	
	HashWrapper
	getHash();
	
	int getCompleted();
	
	void setCompleted(int completed);
	
	int
	getSeeds();
	
	int
	getPeers();
	
	void
	setSeedsPeers(int iSeeds, int iPeers);
  
	int
 	getStatus();

		// time status last set in seconds
	
	int
	getScrapeTime();
	
	/**
	 * When the scrape started (in ms)
	 */
    long
	getScrapeStartTime();

	void
	setScrapeStartTime(
            long time);
  
	long
	getNextScrapeStartTime();

	void
	setNextScrapeStartTime(
            long nextScrapeStartTime);

	String
	getStatusString();

	boolean isValid();
	
	URL
	getURL();
	
	boolean
	isDHTBackup();
	
	String
	getString();
}
