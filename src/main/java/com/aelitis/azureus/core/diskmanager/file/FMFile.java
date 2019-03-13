/*
 * File    : FMFile.java
 * Created : 12-Feb-2004
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

package com.aelitis.azureus.core.diskmanager.file;

/**
 * @author parg
 *
 */

import java.io.File;

import org.gudy.azureus2.core3.util.DirectByteBuffer;


public interface 
FMFile 
{
	int	FT_LINEAR					= 1;
	int	FT_COMPACT					= 2;
	int	FT_PIECE_REORDER			= 3;
	int	FT_PIECE_REORDER_COMPACT	= 4;
	
	int	FM_READ		= 1;
	int FM_WRITE	= 2;
	
	String
	getName();
	
	boolean
	exists();
	
	FMFileOwner
	getOwner();
	
	void
	moveFile(
            File new_file)
	
		throws FMFileManagerException;
	
	void
	renameFile(
            String new_name)
	
		throws FMFileManagerException;
	
	void
	setAccessMode(
            int mode)
	
		throws FMFileManagerException;
	
	int
	getAccessMode();
	
	void
	setStorageType(
            int type)
	
		throws FMFileManagerException;
	
	int
	getStorageType();
	
	void
	ensureOpen(
            String reason)

		throws FMFileManagerException;
	
	long
	getLength()
		
		throws FMFileManagerException;

	void
	setLength(
            long length)
	
		throws FMFileManagerException;
	
	void
	setPieceComplete(
            int piece_number,
            DirectByteBuffer piece_data)
	
		throws FMFileManagerException;
	
	void
	read(
            DirectByteBuffer buffer,
            long offset)
	
		throws FMFileManagerException;
	
	void
	read(
            DirectByteBuffer[] buffers,
            long offset)
	
		throws FMFileManagerException;
	
	void
	write(
            DirectByteBuffer buffer,
            long position)
	
		throws FMFileManagerException;
	
	void
	write(
            DirectByteBuffer[] buffers,
            long position)
	
		throws FMFileManagerException;
	
	void
	flush()
	
		throws FMFileManagerException;
	
	void
	close()
	
		throws FMFileManagerException;
	
	boolean
	isOpen();
	
	void
	delete()
	
		throws FMFileManagerException;
	
	FMFile
	createClone()
	
		throws FMFileManagerException;
	
	boolean
	isClone();
}
