/*
 * Created on 06-Aug-2004
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

/**
 * @author parg
 *
 */

import org.gudy.azureus2.core3.torrent.TOTorrent;

public interface 
CacheFileManagerStats 
{
	long
	getSize();
	
	long
	getUsedSize();
	
	long
	getBytesWrittenToCache();
	
	long
	getBytesWrittenToFile();
	
	long
	getBytesReadFromCache();
	
	long
	getBytesReadFromFile();
	
	long
	getAverageBytesWrittenToCache();
	
	long
	getAverageBytesWrittenToFile();
	
	long
	getAverageBytesReadFromCache();
	
	long
	getAverageBytesReadFromFile();

	long
	getCacheReadCount();
	
	long
	getCacheWriteCount();
	
	long
	getFileReadCount();
	
	long
	getFileWriteCount();
	
		// returns the number of bytes in the requested range that are in cache
	
	boolean[] getBytesInCache(TOTorrent torrent, long[] absoluteOffsets, long[] lengths);
}
