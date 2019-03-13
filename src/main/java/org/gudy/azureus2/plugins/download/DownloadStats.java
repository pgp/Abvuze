/*
 * File    : DownloadStats.java
 * Created : 08-Jan-2004
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

package org.gudy.azureus2.plugins.download;

/**
 * @author parg
 * This class gives access to various stats associated with the download
 */

public interface 
DownloadStats 
{
	int HEALTH_STOPPED    		= 1;
	int HEALTH_NO_TRACKER 		= 2;
	int HEALTH_NO_REMOTE  		= 3;
	int HEALTH_OK  				= 4;
	/** not connected to any peer and downloading */
    int HEALTH_KO 				= 5;
	/** download in STATE_ERROR, see {@link #getStatus()} for error */
    int HEALTH_ERROR 				= 6;
	
	/**
	 * Returns an overall string representing the state of the download
	 * @return
   *
   * @since 2.0.7.0
	 */
    String
	getStatus();
	
	/**
	 * Returns an overall string representing the state of the download
	 * *localised*
	 * @return
   *
   * @since 2.3.0.7
	 */
    String
	getStatus(boolean localised);
	
	/**
	 * Gives access to the directory into which the download is being saved
	 * @return
   *
   * @since 2.0.7.0
	 */
    String
	getDownloadDirectory();
	
	/**
	 * Gives access to the target file or directory that the download is being saved to
	 * @return
   *
   * @since 2.0.7.0
	 */
    String
	getTargetFileOrDir();
	
	/**
	 * returns an general status string for the tracker
	 * @return
   *
   * @since 2.0.7.0
	 */
    String
	getTrackerStatus();
	
	/**
	 * returns a value between 0 and 1000 giving the completion status of the current download
	 * task (e.g. checking, downloading)
	 * @return
   *
   * @since 2.0.7.0
	 */
    int
	getCompleted();
	
  /** Retrieve the level of download completion, *including* DND files. If
   * the user has one or more DND file, return value will never reach 1000.
   * <P>
   * To understand the bLive parameter, you must know a bit about the
   * Torrent activation process:<br>
   * 1) Torrent goes into ST_WAITING<br>
   * 2) Torrent moves to ST_PREPARING<br>
   * 3) Torrent moves to ST_DOWNLOADING or ST_SEEDING<br>
   * <P>
   * While in ST_PREPARING, Completion Level is rebuilt (either via Fast Resume
   * or via piece checking). Quite often, the download completion level before
   * ST_PREPARING and after ST_PREPARING are identical.
   * <P>
   * Before going into ST_PREPARING, we store the download completion level.
   * If you wish to retrieve this value instead of the live "building" one,
   * pass false for the parameter.
   *
   * @param bLive true - Always returns the known completion level of the torrent
   *               false - In the case of ST_PREPARING, return completion level 
   *                       before of the torrent ST_PREPARING started.  
   *                       Otherwise, same as true.
   * @return 0 - 1000
   *
   * @since 2.0.8.0
	 */
  int
	getDownloadCompleted(boolean bLive);

	/**
	 * if isChecking then returns completeness, 1000 -> 100%
	 * @return
	 */

    int
	getCheckingDoneInThousandNotation();

	/**
	 * resets totals. stops and restarts torrent if running to do so
	 * @since 4511. Supply -1 to leave a value unchanged
	 */

    void
	resetUploadedDownloaded(
            long new_up,
            long new_down);
	
	/**
	 * Gives the number of bytes downloaded
	 * @return
   *
   * @since 2.0.7.0
	 */
    long
	getDownloaded();
	
	/**
	 * @since 5.1.0.1
	 * @param include_protocol
	 * @return
	 */

    long
	getDownloaded(
            boolean include_protocol);
	
	/**
	 * Gives number of bytes remaining.  *Includes* DND files
	 * 
	 * @since 2.5.0.2
	 */

    long
	getRemaining();

	long
	getRemainingExcludingDND();

	/**
	 * Gives the number of bytes uploaded
	 * @return
   *
   * @since 2.0.7.0
	 */
    long
	getUploaded();

	/**
	 * @since 5.1.0.1
	 * @param include_protocol
	 * @return
	 */

    long
	getUploaded(
            boolean include_protocol);
	
	/**
	 * Gives the number of bytes discarded.
	 * Does not include {@link #getHashFails()}
	 * (ex. end game mode where multiple peers send same block, et)
	 * @return
   *
   * @since 2.0.7.0
	 */
    long
	getDiscarded();
	
	/**
	 * Gives average number of bytes downloaded in last second 
	 * @return
   *
   * @since 2.0.7.0
	 */
    long
	getDownloadAverage();
	
	/**
	 * @since 5.1.0.1
	 * @param include_protocol
	 * @return
	 */
    long
	getDownloadAverage(
            boolean include_protocol);

	/**
	 * Gives average number of bytes uploaded in last second 
	 * @return
   *
   * @since 2.0.7.0
	 */
    long
	getUploadAverage();
	
	/**
	 * @since 5.1.0.1
	 * @param include_protocol
	 * @return
	 */

    long
	getUploadAverage(
            boolean include_protocol);
	
	/**
	 * Gives average number of bytes computed for torrent in last second 
	 * @return
   *
   * @since 2.0.7.0
	 */
    long
	getTotalAverage();
	
	/**
	 * Gives the elapsed download time as a string
	 * @return
   *
   * @since 2.0.7.0
	 */
    String
	getElapsedTime();
	
	/**
	 * Gives the estimated time to completion as a string
	 * @return
   *
   * @since 2.0.7.0
	 */
    String
	getETA();

	/**
	 * ETA time in seconds.
	 * 
	 * @return
	 *   0 = download is complete.<BR>
	 * < 0 = download is complete and it took -xxx time to complete.<BR>
	 * 	-1 = unknown eta (no peer manager) or download completed 1s ago<BR>
	 * 315360000000L = incomplete and 0 average speed<BR> 
	 * 
	 * @since 4.2.0.3
	 */

    long
	getETASecs();
	
	/**
	 * Gives the number of bytes thrown away due to piece hash check fails
	 * @return
   *
   * @since 2.0.7.0
	 */
    long
	getHashFails();
	
	/**
	 * Gives the share ratio of the torrent in 1000ths (i.e. 1000 = share ratio of 1)
	 * @return
   *
   * @since 2.0.7.0
	 */
    int
	getShareRatio();

  /** in ms since epoch
   *
   * @since 2.0.8.0
	 */
  long
	getTimeStarted();
	
  /**
   * Time that the torrent started seeding.
   * @return the difference, measured in milliseconds, between the torrent 
   *         started seeding and midnight, January 1, 1970 UTC.  see
   *         SystemTime.getCurrentTime().
   *         -1 is not seeding
   *
   * @since 2.1.0.0
	 */
  long
	getTimeStartedSeeding();

	/**
	 * Gives the currently seen availability of the torrent
	 * @return
   *
   * @since 2.0.8.2
	 */
    float
	getAvailability();


  /**
   * Return the # of seconds that the torrent has been downloading.  This 
   * number is totalled across sessions.
   *
   * @return -1 if it has never downloaded
   *
   * @since 2.1.0.0
	 */
  long
	getSecondsDownloading();

  /**
   * Return the # of seconds that the torrent has been only seeding.  This 
   * number is totalled across sessions, and does not include the time
   * seeding during the download phase.
   *
   * @return -1 if it has never seeded
   *
   * @since 2.1.0.0
	 */
  long
	getSecondsOnlySeeding();
	
	/**
	 * Returns the number of seconds running time since data was downloaded, -1 if never 
	 * @since 2501
	 * @return
	 */

    long
	getSecondsSinceLastDownload();
	
	/**
	 * Returns the number of seconds running time since data was uploaded, -1 if never 
	 * @since 2501
	 * @return
	 */

    long
	getSecondsSinceLastUpload();
	
	/**
	 * returns an indication of the health of the torrent 
	 * @return	see above HEALTH constants
	 */

    int
	getHealth();

	/**
	 * Return the number of bytes of data fromt he torrent that is unavailable
	 * given the current sources (peers).
	 *
	 * Does not exclude DND files
	 * 
	 * @return -1 if could not be determined
	 * 
	 * @since 4.8.0.1
	 */
    long
	getBytesUnavailable();
}
