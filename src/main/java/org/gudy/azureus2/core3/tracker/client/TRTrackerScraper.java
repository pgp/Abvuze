/*
 * File    : TRTrackerScraper.java
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

/**
 * @author parg
 *
 */

import java.net.URL;
import org.gudy.azureus2.core3.torrent.*;
import org.gudy.azureus2.plugins.download.DownloadScrapeResult;

public interface 
TRTrackerScraper 
{
	int REFRESH_MINIMUM_SECS		= 2*60;
	
	TRTrackerScraperResponse
	scrape(
            TOTorrent torrent);
		
		// scrape an explicit URL from the (multi-tracker) torrent's set of URLs
	
	TRTrackerScraperResponse
	scrape(
            TOTorrent torrent,
            URL target_url);
	
	TRTrackerScraperResponse
	scrape(
            TOTorrent torrent,
            boolean force);
		
	TRTrackerScraperResponse
	scrape(
            TRTrackerAnnouncer tracker_client);

	void
	setScrape(
            TOTorrent torrent,
            URL url,
            DownloadScrapeResult result);
	
	TRTrackerScraperResponse
	peekScrape(
            TOTorrent torrent,
            URL target_url);
	
	void
	remove(
            TOTorrent torrent);
				
	void
	setClientResolver(
            TRTrackerScraperClientResolver resolver);
	
	void
	addListener(
            TRTrackerScraperListener l);
	
	void
	removeListener(
            TRTrackerScraperListener l);
}
