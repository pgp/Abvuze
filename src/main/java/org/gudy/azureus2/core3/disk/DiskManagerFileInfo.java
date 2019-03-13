/*
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
 *
 * Created on 3 juil. 2003
 *
 */
package org.gudy.azureus2.core3.disk;

import java.io.File;
import java.io.IOException;

import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.torrent.TOTorrentFile;
import org.gudy.azureus2.core3.util.DirectByteBuffer;

/**
 * @author Olivier
 * 
 */
public interface 
DiskManagerFileInfo 
{
	int READ 	= 1;
	int WRITE 	= 2;

	int	ST_LINEAR			= 1;
	int	ST_COMPACT			= 2;
	int	ST_REORDER			= 3;
	int	ST_REORDER_COMPACT	= 4;
	
		// set methods
		
	void setPriority(int p);
	
	void setSkipped(boolean b);
	 
	/**
	 * Relink the file to the destination given - this method deals with if the file
	 * is part of a simple torrent or not (so it may set the download name to keep it
	 * in sync). If you just want a simple relink, use setLinkAtomic.
	 * 
	 * @param link_destination
	 * @return
	 */
    boolean
	setLink(
            File link_destination);
	
	boolean setLinkAtomic(File link_destination);
	
		// gets the current link, null if none
	
	File
	getLink();
	
		/**
		 * Download must be stopped before calling this!
		 * @param type	one of ST_LINEAR or ST_COMPACT
		 */

        boolean setStorageType(int type);
	
	int
	getStorageType();
	
	 	// get methods
	 	
	int getAccessMode();
	
	long getDownloaded();
	
	String getExtension();
		
	int getFirstPieceNumber();
  
	int getLastPieceNumber();
	
	long getLength();
		
	int getNbPieces();
			
	int getPriority();
	
	boolean isSkipped();
	
	int	getIndex();
	
	DownloadManager	getDownloadManager();
	
	DiskManager getDiskManager();
	
	File getFile(boolean follow_link);
	
	TOTorrentFile
	getTorrentFile();
	
	DirectByteBuffer
	read(
            long offset,
            int length)
	
		throws IOException;
	
	void
	flushCache()
	
		throws	Exception;
	
	int
	getReadBytesPerSecond();
	
	int
	getWriteBytesPerSecond();
	
	long
	getETA();
	
	void
	close();
	
	void
	addListener(
            DiskManagerFileInfoListener listener);
	
	void
	removeListener(
            DiskManagerFileInfoListener listener);
	
}
