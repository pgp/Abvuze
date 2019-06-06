/*
 * Created on 28-Sep-2005
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

package com.aelitis.azureus.core.diskmanager.file.impl;

import java.io.RandomAccessFile;

import org.gudy.azureus2.core3.util.DirectByteBuffer;

import com.aelitis.azureus.core.diskmanager.file.FMFileManagerException;

public interface 
FMFileAccess 
{
	interface FMFileAccessIO {
		void io(RandomAccessFile raf,
				DirectByteBuffer[] buffers,
				long offset)
				throws FMFileManagerException;
	}

	void
	aboutToOpen()
	
		throws FMFileManagerException;
		
	long
	getLength(
            RandomAccessFile raf)
	
		throws FMFileManagerException;
	
	void
	setLength(
            RandomAccessFile raf,
            long length)
	
		throws FMFileManagerException;
	
	void
	read(
            RandomAccessFile raf,
            DirectByteBuffer[] buffers,
            long offset)
	
		throws FMFileManagerException;
	
	void
	write(
            RandomAccessFile raf,
            DirectByteBuffer[] buffers,
            long position)
	
		throws FMFileManagerException;
	
	void
	flush()
	
		throws FMFileManagerException;
	
	boolean
	isPieceCompleteProcessingNeeded(
            int piece_number);
	
	void
	setPieceComplete(
            RandomAccessFile raf,
            int piece_number,
            DirectByteBuffer piece_data)
	
		throws FMFileManagerException;
	 
	FMFileImpl
	getFile();
	
	String
	getString();
}
