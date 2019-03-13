/*
 * Created on 15-Nov-2004
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

package org.gudy.azureus2.core3.download;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.gudy.azureus2.core3.category.Category;
import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.util.IndentWriter;
import org.gudy.azureus2.plugins.download.Download;

import com.aelitis.azureus.core.util.LinkFileMap;

/**
 * @author parg
 */

public interface 
DownloadManagerState 
{
	String AT_VERSION					= "version";
	String AT_CATEGORY					= "category";
	String AT_NETWORKS					= "networks";
	String AT_USER						= "user";
	String AT_PEER_SOURCES				= "peersources";
	String AT_PEER_SOURCES_DENIED		= "peersourcesdenied";
	String AT_TRACKER_CLIENT_EXTENSIONS	= "trackerclientextensions";
	String AT_FILE_LINKS_DEPRECATED		= "filelinks";
	String AT_FILE_LINKS2				= "filelinks2";
	String AT_FILE_STORE_TYPES			= "storetypes";
	String AT_FILE_DOWNLOADED			= "filedownloaded";
	String AT_FLAGS						= "flags";
	String AT_PARAMETERS				= "parameters";
	String AT_DISPLAY_NAME              = "displayname";
	String AT_USER_COMMENT              = "comment";
	String AT_RELATIVE_SAVE_PATH        = "relativepath";
	String AT_SECRETS				 	= "secrets";
	String AT_RESUME_STATE		 		= "resumecomplete";
	String AT_PRIMARY_FILE		 		= "primaryfile";
	String AT_PRIMARY_FILE_IDX		 	= "primaryfileidx";
	String AT_TIME_SINCE_DOWNLOAD		= "timesincedl";
	String AT_TIME_SINCE_UPLOAD			= "timesinceul";
	String AT_AVAIL_BAD_TIME			= "badavail";
	String AT_TIME_STOPPED				= "timestopped";
	String AT_INCOMP_FILE_SUFFIX		= "incompfilesuffix";
	String AT_SCRAPE_CACHE				= "scrapecache";	// long value, seeds in upper word, leechers in lower
	String AT_SCRAPE_CACHE_SOURCE		= "scsrc";			// int value - 0=tracker; 1=subscription etc
	String AT_REORDER_MIN_MB			= "reordermb";
	String AT_MD_INFO_DICT_SIZE			= "mdinfodictsize";
	String AT_FILE_OTHER_HASHES			= "fileotherhashes";
	String AT_CANONICAL_SD_DMAP			= "canosavedir";
	String AT_DND_SUBFOLDER				= "dnd_sf";
	String AT_PEAK_RECEIVE_RATE			= "pkdo";
	String AT_PEAK_SEND_RATE			= "pkup";
	String AT_DL_FILE_ALERTS			= "df_alerts";
	String AT_SHARE_RATIO_PROGRESS		= "sr.prog";		// long: left word - timestamp in secs, right word sr in 1000ths
	String AT_FILES_EXPANDED			= "file.expand";	// boolean
	String AT_MERGED_DATA				= "mergedata";		// long
	String AT_DND_PREFIX				= "dnd_pfx";		// string
	String AT_AGGREGATE_SCRAPE_CACHE	= "agsc";			// string <update_time_mins>,<seeds>,<leechers>
	String AT_COMPLETE_LAST_TIME		= "complt";			// long - last time download reported complete, -1 if reported incomplete, 0 if unknown
	String AT_LAST_ADDED_TO_ACTIVE_TAG	= "last.act.tag";	// long - last time added to active tag
	
	Object[][] ATTRIBUTE_DEFAULTS = {
		{ AT_VERSION, -1},
		{ AT_TIME_SINCE_DOWNLOAD, -1},
		{ AT_TIME_SINCE_UPLOAD, -1},
		{ AT_AVAIL_BAD_TIME, (long) -1},
		{ AT_SCRAPE_CACHE, (long) -1},
		{ AT_SCRAPE_CACHE_SOURCE, 0},
		{ AT_REORDER_MIN_MB, -1},
		{ AT_SHARE_RATIO_PROGRESS, 0L},
	};
	
	long FLAG_ONLY_EVER_SEEDED						= Download.FLAG_ONLY_EVER_SEEDED;
	long FLAG_SCAN_INCOMPLETE_PIECES				= Download.FLAG_SCAN_INCOMPLETE_PIECES;
	long FLAG_DISABLE_AUTO_FILE_MOVE    			= Download.FLAG_DISABLE_AUTO_FILE_MOVE;
	long FLAG_MOVE_ON_COMPLETION_DONE   			= Download.FLAG_MOVE_ON_COMPLETION_DONE;
	long FLAG_LOW_NOISE								= Download.FLAG_LOW_NOISE;
	long FLAG_ALLOW_PERMITTED_PEER_SOURCE_CHANGES	= Download.FLAG_ALLOW_PERMITTED_PEER_SOURCE_CHANGES;
	long FLAG_DO_NOT_DELETE_DATA_ON_REMOVE  		= Download.FLAG_DO_NOT_DELETE_DATA_ON_REMOVE;
	long FLAG_FORCE_DIRECT_DELETE			  		= Download.FLAG_FORCE_DIRECT_DELETE;
	long FLAG_DISABLE_IP_FILTER				  		= Download.FLAG_DISABLE_IP_FILTER;
	long FLAG_METADATA_DOWNLOAD				  		= Download.FLAG_METADATA_DOWNLOAD;
	long FLAG_ERROR_REPORTED				  		= Download.FLAG_ERROR_REPORTED;
	long FLAG_INITIAL_NETWORKS_SET					= Download.FLAG_INITIAL_NETWORKS_SET;
	
	String	PARAM_MAX_PEERS							= "max.peers";
	String	PARAM_MAX_PEERS_WHEN_SEEDING			= "max.peers.when.seeding";
	String	PARAM_MAX_PEERS_WHEN_SEEDING_ENABLED	= "max.peers.when.seeding.enabled";
	String	PARAM_MAX_SEEDS							= "max.seeds";
	String	PARAM_MAX_UPLOADS						= "max.uploads";
	String	PARAM_MAX_UPLOADS_WHEN_SEEDING			= "max.uploads.when.seeding";
	String	PARAM_MAX_UPLOADS_WHEN_SEEDING_ENABLED	= "max.uploads.when.seeding.enabled";
	String	PARAM_STATS_COUNTED						= "stats.counted";
	String	PARAM_DOWNLOAD_ADDED_TIME				= "stats.download.added.time";
	String	PARAM_DOWNLOAD_COMPLETED_TIME			= "stats.download.completed.time";
	String	PARAM_DOWNLOAD_FILE_COMPLETED_TIME		= "stats.download.file.completed.time";
	String	PARAM_DOWNLOAD_LAST_ACTIVE_TIME			= "stats.download.last.active.time";
	String	PARAM_MAX_UPLOAD_WHEN_BUSY				= "max.upload.when.busy";
	String  PARAM_DND_FLAGS							= "dndflags";
	String  PARAM_RANDOM_SEED						= "rand";
	String	PARAM_UPLOAD_PRIORITY					= "up.pri";
	String	PARAM_MIN_SHARE_RATIO					= "sr.min";		// in thousandths - 1000 = sr of 1.0
	String	PARAM_MAX_SHARE_RATIO					= "sr.max";		// in thousandths - 1000 = sr of 1.0
	
	int DEFAULT_MAX_UPLOADS		= 4;
	int MIN_MAX_UPLOADS			= 2;
	int DEFAULT_UPLOAD_PRIORITY	= 0;
	
	Object[][] PARAMETERS = {
		{ PARAM_MAX_PEERS, 0},
		{ PARAM_MAX_PEERS_WHEN_SEEDING, 0},
		{ PARAM_MAX_PEERS_WHEN_SEEDING_ENABLED, Boolean.FALSE},
		{ PARAM_MAX_SEEDS, 0},
		{ PARAM_MAX_UPLOADS, (long) DEFAULT_MAX_UPLOADS},
		{ PARAM_MAX_UPLOADS_WHEN_SEEDING, DEFAULT_MAX_UPLOADS},
		{ PARAM_MAX_UPLOADS_WHEN_SEEDING_ENABLED, Boolean.FALSE},
		{ PARAM_STATS_COUNTED, Boolean.FALSE},
		{ PARAM_DOWNLOAD_ADDED_TIME, 0L},
		{ PARAM_DOWNLOAD_FILE_COMPLETED_TIME, 0L},
		{ PARAM_DOWNLOAD_COMPLETED_TIME, 0L},
		{ PARAM_DOWNLOAD_LAST_ACTIVE_TIME, 0L},
		{ PARAM_MAX_UPLOAD_WHEN_BUSY, 0L},
		{ PARAM_DND_FLAGS, 0L},
		{ PARAM_RANDOM_SEED, 0L},
		{ PARAM_UPLOAD_PRIORITY, DEFAULT_UPLOAD_PRIORITY},
		{ PARAM_MIN_SHARE_RATIO, 0},
		{ PARAM_MAX_SHARE_RATIO, 0},
	};
		
	TOTorrent
	getTorrent();
	
	DownloadManager
	getDownloadManager();
	
	File
	getStateFile();
	
	void
	setFlag(
            long flag,
            boolean set);
	
	boolean
	getFlag(
            long flag);
	
	long
	getFlags();

		/**
		 * Reset to default value
		 * @param name
		 */

        void
	setParameterDefault(
                String name);
	
	int
	getIntParameter(
            String name);
	
	void
	setIntParameter(
            String name,
            int value);
	
	long
	getLongParameter(
            String name);
	
	void
	setLongParameter(
            String name,
            long value);
	
	boolean
	getBooleanParameter(
            String name);
	
	void
	setBooleanParameter(
            String name,
            boolean value);
	
	void
	clearResumeData();
	
	Map
	getResumeData();
	
	void
	setResumeData(
            Map data);
	
	boolean
	isResumeDataComplete();
	
	void
	clearTrackerResponseCache();
	
	Map
	getTrackerResponseCache();
	
	void
	setTrackerResponseCache(
            Map value);
	
	Category
	getCategory();
	
	void
	setCategory(
            Category cat);
	
	String getDisplayName();
	void setDisplayName(String name);
	
	String getUserComment();
	void setUserComment(String name);
	
	String getRelativeSavePath();

	void setPrimaryFile(DiskManagerFileInfo dmfi);
	DiskManagerFileInfo getPrimaryFile();

	String
	getTrackerClientExtensions();
	
	void
	setTrackerClientExtensions(
            String value);
	
	String[]		// from AENetworkClassifier constants
	getNetworks();
	
	boolean
	isNetworkEnabled(
            String network); //from AENetworkClassifier constants
	
	void
	setNetworks(
            String[] networks);	// from AENetworkClassifier constants
	
	void
	setNetworkEnabled(
            String network,                // from AENetworkClassifier constants
            boolean enabled);
	
	String[]		// from PEPeerSource constants
	getPeerSources();
	
	boolean
	isPeerSourcePermitted(
            String peerSource);
	
	void
	setPeerSourcePermitted(
            String peerSource,
            boolean permitted);
	
	boolean
	isPeerSourceEnabled(
            String peerSource); // from PEPeerSource constants
	
	void
	setPeerSources(
            String[] sources);	// from PEPeerSource constants

	void
	setPeerSourceEnabled(
            String source,        // from PEPeerSource constants
            boolean enabled);
	
		// file links
	
	void
	setFileLink(
            int source_index,
            File link_source,
            File link_destination);

	void
	setFileLinks(
            List<Integer> source_indexes,
            List<File> link_sources,
            List<File> link_destinations);
	
	void
	clearFileLinks();
	
	File
	getFileLink(
            int source_index,
            File link_source);
	
		/**
		 * returns a File -> File map of the defined links (empty if no links)
		 * @return
		 */

        LinkFileMap
	getFileLinks();
	
	/**
	 * @return
	 */
	boolean isOurContent();
	
	// General access - make sure you use an AT_ value defined above when calling
	// these methods.
    void setAttribute(String name, String value);
	String getAttribute(String name);
	void	setMapAttribute(String name, Map value);
	Map getMapAttribute(String name);
	void	setListAttribute(String name, String[] values);
	String[]	getListAttribute(String name);
	String getListAttribute(String name, int idx);
	void setIntAttribute(String name, int value);
	int getIntAttribute(String name);
	void setLongAttribute(String name, long value);
	long getLongAttribute(String name);
	void setBooleanAttribute(String name, boolean value);
	boolean getBooleanAttribute(String name);
	boolean hasAttribute(String name);
	
	void
	setActive(
            boolean active);
	
	void discardFluff();
	
	void
	save();
	
	boolean
	exportState(
            File target_dir);
	
		/**
		 * deletes the saved state
		 */

        void
	delete();
	
	void
	addListener(
            DownloadManagerStateListener l);
	
	void
	removeListener(
            DownloadManagerStateListener l);

	/**
	 * @param name
	 * @return
	 */
	boolean parameterExists(String name);
	
	void generateEvidence(IndentWriter writer);
	
	void dump(IndentWriter writer);
	
	/**
	 * This method should only be invoked in matching try-finally pairs. If it is invoked with true
	 * multiple times it must be invoked with false the equal amount of times to reallow state
	 * writes
	 * 
	 * @param suppress
	 *            when set to true prevents flushing of the state/increments the internal nesting
	 *            counter, decrements/allows flush otherwise
	 */
    void suppressStateSave(boolean suppress);
	
	void addListener(DownloadManagerStateAttributeListener l, String attribute, int event_type);
	void removeListener(DownloadManagerStateAttributeListener l, String attribute, int event_type);
}
