/*
 * File    : TrackerTorrent.java
 * Created : 08-Dec-2003
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

package org.gudy.azureus2.plugins.tracker;

/**
 * @author parg
 *
 */

import org.gudy.azureus2.plugins.torrent.*;

public interface 
TrackerTorrent
{
	int	TS_STARTED		= 0;
	int	TS_STOPPED		= 1;
	int	TS_PUBLISHED	= 2;
	
	void
	start()
	
		throws TrackerException;
	
	void
	stop()
	
		throws TrackerException;

	void
	remove()
	
		throws TrackerTorrentRemovalVetoException;
	
	boolean
	canBeRemoved()
	
		throws TrackerTorrentRemovalVetoException;

	Torrent
	getTorrent();
	
	TrackerPeer[]
	getPeers();
	
	int
	getStatus();
	
	int
	getSeedCount();
	
	int
	getLeecherCount();
	
		/**
		 * Gives access to the number of peers that have failed NAT checks, 0 if NAT checking
		 * is disabled.
		 * @return
		 */

        int
	getBadNATCount();
	
	long
	getTotalUploaded();
	
	long
	getTotalDownloaded();
	
	long
	getAverageUploaded();
	
	long
	getAverageDownloaded();
	
	long
	getTotalLeft();
	
	long
	getCompletedCount();
	
		/**
		 * total bytes received by the tracker for this torrent - announce and scrape requests
		 * @return
		 */

        long
	getTotalBytesIn();
	
		/**
		 * average rate of bytes in
		 * @return
		 */

        long
	getAverageBytesIn();
	
		/**
		 * total bytes sent as response by the tracker - announce and scrape responses
		 * @return
		 */

        long
	getTotalBytesOut();
	
		/**
		 * average bytes sent
		 * @return
		 */

        long
	getAverageBytesOut();
	
	long
	getScrapeCount();

	long
	getAverageScrapeCount();
	
	long
	getAnnounceCount();

	long
	getAverageAnnounceCount();
	
		/**
		 * stops the tracker from caching replies to announces/scrapes for this torrent
		 */

        void
	disableReplyCaching();
	
	boolean
	isPassive();
	
		/**
		 * @since 2.3.0.5
		 * @return
		 */

        long
	getDateAdded();
	
	void
	addListener(
            TrackerTorrentListener listener);
	
	void
	removeListener(
            TrackerTorrentListener listener);
	
	void
	addRemovalListener(
            TrackerTorrentWillBeRemovedListener listener);
	
	void
	removeRemovalListener(
            TrackerTorrentWillBeRemovedListener listener);
}
