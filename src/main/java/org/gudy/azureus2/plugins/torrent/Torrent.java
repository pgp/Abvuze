/*
 * File    : Torrent.java
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

package org.gudy.azureus2.plugins.torrent;

/**
 * @author parg
 *
 */

import java.io.File;
import java.net.URL;
import java.util.Map;

public interface 
Torrent
{
	String
	getName();
	
	URL
	getAnnounceURL();
	
	void
	setAnnounceURL(
            URL url);
	
		/**
		 * get the announce list for multi-tracker torrents. Will always be present but
		 * may contain 0 sets which means that this is not a multi-tracker torrent
		 * @return
		 */

        TorrentAnnounceURLList
	getAnnounceURLList();
	
	byte[]
	getHash();
	
	/**
	 * If size is 0 then this is an "external" torrent and we only know its hash (and name 
	 * constructed from hash). e.g. we don't know file details
	 * @return
	 */

    long
	getSize();
	
	String
	getComment();
	
	void
	setComment(
            String comment);
	
	/**
	 * UNIX epoch format in seconds
	 */
    long
	getCreationDate();
	
	String
	getCreatedBy();
		
	long
	getPieceSize();
	
	long
	getPieceCount();
	
	byte[][]
    getPieces();
	
	TorrentFile[]
	getFiles();
	
	String
	getEncoding();
	
	void
	setEncoding(
            String encoding)
	
		throws TorrentEncodingException;
	
	void
	setDefaultEncoding()
	
		throws TorrentEncodingException;
	
		/**
		 * Access to top-level properties in the torrent
		 * @param name
		 * @return
		 */

        Object
	getAdditionalProperty(
                String name);

		/**
		 * Removal all non-standard properties (excluding plugin-properties below)
		 * @return
		 */

        Torrent
	removeAdditionalProperties();
	
		/**
		 * Set a property specific to this plugin
		 * @param name
		 * @param value
		 */

        void
	setPluginStringProperty(
                String name,
                String value);
	
		/**
		 * Get a property specific to this plugin
		 * @param name
		 * @return
		 */

        String
	getPluginStringProperty(
                String name);
	
		/**
		 * Sets a map property in the torrent, retrievable via getMapProperty
		 * @param name	should be unique across plugins (i.e. prefix it with something unique)
		 * @param value	bencodable Map value
		 */

        void
	setMapProperty(
                String name,
                Map value);
	
		/**
		 * Gets a previously set map value
		 * @see setMapProperty
		 * @param name	should be unique across plugins (i.e. prefix it with something unique)
		 * @return
		 */

        Map
	getMapProperty(
                String name);
	
		/**
		 * A decentralised torrent uses the DHT only as a "tracker"
		 * @return
		 */

        boolean
	isDecentralised();
	
		/**
		 * Decentralised backup permits the DHT to be used as a tracker when the
		 * "real" tracker is unavailable
		 * @return
		 */

        boolean
	isDecentralisedBackupEnabled();
	
		/**
		 * By default torrents with OK trackers are not tracked in the DHT. This allows a specific
		 * torrent to be marked so that it will be 
		 * @param requested
		 */

        void
	setDecentralisedBackupRequested(
                boolean requested);
	
	boolean
	isDecentralisedBackupRequested();
	
		/**
		 * A private torrent is either explicitly private via info/private or
		 * has decentralised backup disabled and peer exchange disabled
		 * @return
		 */

        boolean
	isPrivate();
	
	void
	setPrivate(
            boolean priv);
	
		/**
		 * @since 2501
		 * @return
		 */

        boolean
	wasCreatedByUs();
	
		/** 
		 * Gets the magnet URI for the torrent - throws exception if not available
		 * @return
		 */

        URL
	getMagnetURI()
	
		throws TorrentException;
	
	Map
	writeToMap()
	
		throws TorrentException;
	
	void
	writeToFile(
            File file)
	
		throws TorrentException;
  
	byte[]
	writeToBEncodedData()
	
		throws TorrentException;
	
		/**
		 * Saves the torrent to its persistent location
		 * @throws TorrentException
		 */

        void
	save()
	
		throws TorrentException;  
	
		/**
		 * sets the torrent complete - i.e. ready for seeding. Doing this avoids
		 * a recheck on torrent addition
		 * @param data_dir
		 * @throws TorrentException
		 */

        void
	setComplete(
                File data_dir)
	
		throws TorrentException;
	
	boolean
	isComplete();
	
	/**
	 * Returns <tt>true</tt> if the torrent is a single file torrent,
	 * <tt>false</tt> if it is a multi file torrent.
	 * 
	 * @since 3.0.4.3
	 */
    boolean isSimpleTorrent();
	
	Torrent
	getClone()
	
		throws TorrentException;
}
