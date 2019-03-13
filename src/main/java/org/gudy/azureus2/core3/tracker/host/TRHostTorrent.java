/*
 * File    : TRHostTorrent.java
 * Created : 26-Oct-2003
 * By      : stuff
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
 *
 */

import org.gudy.azureus2.core3.torrent.*;
import org.gudy.azureus2.core3.tracker.server.TRTrackerServerTorrent;

public interface 
TRHostTorrent 
{
	int	TS_FAILED		= 0;
	int	TS_STOPPED		= 1;
	int	TS_STARTED		= 2;
	int	TS_PUBLISHED	= 3;
	
	void
	start();
	
	void
	stop();
	
	void
	remove()
	
		throws TRHostTorrentRemovalVetoException;
	
	/**
	 * doesn't guarantee that removal will be successful as conditions may change
	 * @return true if OK, exception thrown otherwise
	 * @throws TRHostTorrentRemovalVetoException
	 */

    boolean
	canBeRemoved()
	
		throws TRHostTorrentRemovalVetoException;
	
	int
	getStatus();
	
	boolean
	isPersistent();
	
	boolean
	isPassive();
	
	void
	setPassive(
            boolean passive);
	
	long
	getDateAdded();
	
	TOTorrent
	getTorrent();
	
	void
	setTorrent(
            TOTorrent torrent);
	
	TRTrackerServerTorrent
	getTrackerTorrent();
	
	int
	getPort();
	
	TRHostPeer[]
	getPeers();
	
	int
	getSeedCount();
	
	int
	getLeecherCount();
	
	int
	getBadNATCount();

	long
	getAnnounceCount();
	
	long
	getAverageAnnounceCount();
	
	long
	getScrapeCount();
	
	long
	getAverageScrapeCount();
	
	long
	getCompletedCount();
	
	long
	getTotalUploaded();
	
	long
	getTotalDownloaded();
		
	long
	getTotalLeft();
	
	long
	getAverageUploaded();
	
	long
	getAverageDownloaded();
	
	long
	getTotalBytesIn();
	
	long
	getAverageBytesIn();
	
	long
	getTotalBytesOut();
	
	long
	getAverageBytesOut();
	
	void
	disableReplyCaching();
	
	void
	addListener(
            TRHostTorrentListener l);
	
	void
	removeListener(
            TRHostTorrentListener l);
	
	void
	addRemovalListener(
            TRHostTorrentWillBeRemovedListener l);
	
	void
	removeRemovalListener(
            TRHostTorrentWillBeRemovedListener l);

  /** To retreive arbitrary objects against this object. */
  Object getData(String key);
  /** To store arbitrary objects against this object. */
  void setData(String key, Object value);
}
