/*
 * File    : TOTorrent.java
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

package org.gudy.azureus2.core3.torrent;

import java.io.*;
import java.net.*;
import java.util.*;

import org.gudy.azureus2.core3.util.*;

public interface 
TOTorrent
{
	String	DEFAULT_IGNORE_FILES	= ".DS_Store;Thumbs.db;desktop.ini";
	
		/**
		 * A Map additional property defined for holding AZ specific properties that are
		 * deemed to be exportable to the world
		 */

        String	AZUREUS_PROPERTIES				= "azureus_properties";
	
		/**
		 * These ones are *not* exportable to the world
		 */

        String	AZUREUS_PRIVATE_PROPERTIES		= "azureus_private_properties";

	String ENCODING_ACTUALLY_UTF8_KEYS = "utf8 keys";

	/**
	 * Get the name of the torrent
	 * @return
	 */

    byte[]
	getName();
	
	/**
	 * A "simple torrent" is one that consists of a single file on its own (i.e. not in a
	 * nested directory). 
	 * @return
	 */

    boolean
	isSimpleTorrent();
	
	/**
	 * Comment is an optional torrent property
	 * @return
	 */

    byte[]
	getComment();

	void
	setComment(
            String comment);
	
	/**
	 * Gets the creation date of the torrent. Optional property, 0 returned if not set
	 * @return
	 */

    long
	getCreationDate();
	
	void
	setCreationDate(
            long date);
	
	byte[]
	getCreatedBy();
	
	void
	setCreatedBy(
            byte[] cb);
	
	boolean
	isCreated();
	
	/**
	 * A torrent must have a URL that identifies the tracker. This method returns it. However
	 * an extension to this exists to allow multiple trackers, and their backups, to be defined.
	 * See below
	 * @return
	 */
    URL
	getAnnounceURL();

	/**
	 * 
	 * @param url
	 * @return true-changed; false-not changed
	 */
    boolean
	setAnnounceURL(
            URL url);
		
	/**
	 * When a group of sets of trackers is defined their URLs are accessed via this method
	 * @return the group, always present, which may have 0 members
	 */

    TOTorrentAnnounceURLGroup
	getAnnounceURLGroup();  
	 
	 /**
	  * This method provides access to the SHA1 hash values (20 bytes each) that correspond
	  * to the pieces of the torrent.
	  * @return
	  * @exception	can fail if re-reading of piece hashes for space spacing fails 
	  */

     boolean
	isDecentralised();
	
	byte[][]
	getPieces()
	
		throws TOTorrentException;

		/**
		 * This method exists to support the temporary discarding of piece hashes to conserver
		 * memory. It should only be used with care! 
		 * @param pieces
		 */

        void
	setPieces(
                byte[][] pieces)
	
		throws TOTorrentException;
	
	/**
	 * Returns the piece length used for the torrent
	 * @return
	 */
    long
	getPieceLength();

	int
	getNumberOfPieces();
	
	long
	getSize();
	
	int
	getFileCount();
	
	/**
	 * A torrent consists of one or more files. These are accessed via this method.
	 * @return
	 */
    TOTorrentFile[]
	getFiles();
	
	 /**
	  * A torrent has a unique SHA1 (20 byte) hash that is computed from some of its contents.
	  * It is used, for example, when contacting a tracker to identify the torrent.
	  * @return
	  * @throws TOTorrentException
	  */

     byte[]
	getHash()
				
		throws TOTorrentException;

	/**
	 * convenience method to get a wrapped hash for performance purposes
	 * @return
	 * @throws TOTorrentException
	 */

    HashWrapper
	getHashWrapper()
				
		throws TOTorrentException;

		/**
		 * Only supported for decentralised torrents, allows an alternative hash to be used
		 * for them in the DHT
		 * @param hash
		 * @throws TOTorrentException
		 */

        void
	setHashOverride(
                byte[] hash)
	
		throws TOTorrentException;
	
	/**
	 * compares two torrents by hash
	 * @param other
	 * @return
	 */

    boolean
	hasSameHashAs(
            TOTorrent other);
	
	boolean
	getPrivate();
	
		/**
		 * Note - changing the private attribute CHANGES THE TORRENT HASH 
		 * @param _private
		 */

        void
	setPrivate(
                boolean _private)
	
		throws TOTorrentException;
	
	/**
	 * The additional properties are used for holding non-core data for Azureus' own user
	 * @param name		name of the property (e.g. "encoding")
	 * @param value		value. This will be encoded with default encoding
	 */

    void
	setAdditionalStringProperty(
            String name,
            String value);
		
	String
	getAdditionalStringProperty(
            String name);
		
	void
	setAdditionalByteArrayProperty(
            String name,
            byte[] value);
	
	byte[]
	getAdditionalByteArrayProperty(
            String name);
	
	void
	setAdditionalLongProperty(
            String name,
            Long value);
		
	Long
	getAdditionalLongProperty(
            String name);
		
	
	void
	setAdditionalListProperty(
            String name,
            List value);
		
	List
	getAdditionalListProperty(
            String name);
		
	void
	setAdditionalMapProperty(
            String name,
            Map value);
		
	Map
	getAdditionalMapProperty(
            String name);
	
	Object
	getAdditionalProperty(
            String name);

	/**
	 * set an arbitrary property. Make sure its compatible with bencoding!
	 */

    void
	setAdditionalProperty(
            String name,
            Object value);
	
	void
	removeAdditionalProperty(
            String name);
	
	/**
	 * remove all additional properties to clear out the torrent
	 */

    void
	removeAdditionalProperties();
	
	 /**
	  * This method will serialise a torrent using the standard "b-encoding" mechanism into a file
	  * @param file
	  * @throws TOTorrentException
	  */
     void
	serialiseToBEncodedFile(
             File file)
		  
		throws TOTorrentException;

	 /**
	  * This method will serialise a torrent into a Map consistent with that used by the 
	  * "b-encoding" routines defined elsewhere
	  * @return
	  * @throws TOTorrentException
	  */
     Map
	serialiseToMap()
		  
		throws TOTorrentException;

	/**
	 * This method will serialise a torrent using an XML encoding to a file
	 * @param file
	 * @throws TOTorrentException
	 */

    void
   serialiseToXMLFile(
            File file)
		  
	   throws TOTorrentException;

   void
   addListener(
           TOTorrentListener l);
   
   void
   removeListener(
           TOTorrentListener l);
   
   AEMonitor
   getMonitor();

	 /**
	  * A diagnostic method for dumping the tracker contents to "stdout"
	  *
	  */
     void
	print();

	/**
	 * Retrieves the utf8 name of the torrent ONLY if the torrent specified one
	 * in it's info map.  Otherwise, returns null (you'll have to use getName()
	 * and decode it yourself)
	 */
	String getUTF8Name();
}
