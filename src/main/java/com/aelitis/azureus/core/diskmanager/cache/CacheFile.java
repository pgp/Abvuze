/*
 * Created on 03-Aug-2004
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

package com.aelitis.azureus.core.diskmanager.cache;

import java.io.File;

import org.gudy.azureus2.core3.torrent.TOTorrentFile;
import org.gudy.azureus2.core3.util.DirectByteBuffer;

/**
 * @author parg
 *
 */
public interface 
CacheFile 
{
	int	CT_LINEAR					= 1;
	int CT_COMPACT					= 2;
	int CT_PIECE_REORDER			= 3;
	int CT_PIECE_REORDER_COMPACT	= 4;

	int	CF_READ		= 1;
	int CF_WRITE	= 2;
	
	short CP_NONE		= 0x0000;
	short CP_READ_CACHE	= 0x0001;
	short CP_FLUSH		= 0x0002;
	
	TOTorrentFile
	getTorrentFile();
	
	boolean
	exists();
		
	void
	moveFile(
            File new_file)
	
		throws CacheFileManagerException;
	
	void
	renameFile(
            String new_name)
	
		throws CacheFileManagerException;
	
	void
	setAccessMode(
            int mode)
	
		throws CacheFileManagerException;
	
	int
	getAccessMode();
	
	void
	setStorageType(
            int type)
	
		throws CacheFileManagerException;
	
	int
	getStorageType();
	
	long
	getLength()
	
		throws CacheFileManagerException;

	long
	compareLength(
            long compare_to)
	
		throws CacheFileManagerException;
	
	void
	setLength(
            long length)
	
		throws CacheFileManagerException;
	
	void
	setPieceComplete(
            int piece_number,
            DirectByteBuffer piece_data)
	
		throws CacheFileManagerException;
	
	void
	read(
            DirectByteBuffer buffer,
            long offset,
            short policy)
	
		throws CacheFileManagerException;
	
	void
	read(
            DirectByteBuffer[] buffers,
            long offset,
            short policy)
	
		throws CacheFileManagerException;

	void
	write(
            DirectByteBuffer buffer,
            long position)
	
		throws CacheFileManagerException;
	
	void
	write(
            DirectByteBuffer[] buffers,
            long position)
	
		throws CacheFileManagerException;

	
		/**
		 * writes the block to the cache and gives control of the buffer to the cache.
		 * @param buffer
		 * @param position
		 * @throws CacheFileManagerException	write failed and buffer *not* taken - i.e. caller must de-allocate
		 */

        void
	writeAndHandoverBuffer(
                DirectByteBuffer buffer,
                long position)
	
		throws CacheFileManagerException;
	
	void
	writeAndHandoverBuffers(
            DirectByteBuffer[] buffers,
            long position)
	
		throws CacheFileManagerException;

		/**
		 * flushes the cache to disk but retains entries
		 * @throws CacheFileManagerException
		 */

        void
	flushCache()
	
		throws CacheFileManagerException;
	
		/**
		 * flushes the cache and discards entries
		 * @throws CacheFileManagerException
		 */

        void
	clearCache()
	
		throws CacheFileManagerException;

	void
	close()
	
		throws CacheFileManagerException;
	
	boolean
	isOpen();
	
	long
	getSessionBytesRead();
	
	long
	getSessionBytesWritten();
	
	void
	delete()

		throws CacheFileManagerException;
}
