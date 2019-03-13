/*
 * Created on 31-Jul-2004
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

package org.gudy.azureus2.core3.disk.impl;

/**
 * @author parg
 *
 */

import org.gudy.azureus2.core3.disk.DiskManager;
import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;
import org.gudy.azureus2.core3.disk.impl.piecemapper.DMPieceList;
import org.gudy.azureus2.core3.download.DownloadManagerState;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentException;

import com.aelitis.azureus.core.diskmanager.access.DiskAccessController;

public interface 
DiskManagerHelper 
	extends DiskManager
{
	DiskAccessController
	getDiskAccessController();
	
	DMPieceList
	getPieceList(
            int piece_number);
		
	byte[]
	getPieceHash(
            int piece_number)
	
		throws TOTorrentException;
		
	/**
	 * Stops the disk manager and informs the download manager that things have gone
	 * wrong. 
	 * @param reason
	 */

    void
	setFailed(
            String reason);
	
	void
	setFailed(
            DiskManagerFileInfo file,
            String reason);
	
	long
	getAllocated();
	
	void
	setAllocated(
            long num);
	
	void
	setPercentDone(
            int num);
		
	void
	setPieceDone(
            DiskManagerPieceImpl piece,
            boolean done);
	
	TOTorrent
	getTorrent();
	
	String[]
	getStorageTypes();
	
	String getStorageType(int fileIndex);
	
	void
	accessModeChanged(
            DiskManagerFileInfoImpl file,
            int old_mode,
            int new_mode);
	
	void
    skippedFileSetChanged(
            DiskManagerFileInfo file);
	
	void
	priorityChanged(
            DiskManagerFileInfo file);
	
	String
	getInternalName();
	
	DownloadManagerState
	getDownloadState();
	
	DiskManagerRecheckScheduler
	getRecheckScheduler();

}
