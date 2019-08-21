/*
 * Created on 12-Jul-2004
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

package org.gudy.azureus2.core3.global.impl;

import java.io.File;

import org.gudy.azureus2.core3.global.*;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.tracker.host.*;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.HashWrapper;
import org.gudy.azureus2.core3.util.TorrentUtils;
import org.gudy.azureus2.pluginsimpl.local.download.DownloadManagerImpl;


class
GlobalManagerHostSupport
	implements 	TRHostTorrentFinder
{
	protected final GlobalManager	gm;
	protected final TRHost		host;
	
	protected
	GlobalManagerHostSupport(
		GlobalManager	_gm )
	{
		gm		= _gm;
		
	    host = TRHostFactory.getSingleton();
		  
		host.initialise( this );
	}
	
	public TOTorrent
	lookupTorrent(
		byte[]		hash )
	{
		DownloadManager dm = gm.getDownloadManager( new HashWrapper( hash ));
		
		if ( dm != null ){
			
			TOTorrent torrent = dm.getTorrent();
			
			if ( torrent != null ){
				
				return( torrent );
			}
		}

		return DownloadManagerImpl.getStubTorrent(hash);
	}
	
	protected void
	torrentRemoved(
		String			torrent_file_str,
		TOTorrent		torrent )
	{
		TRHostTorrent	host_torrent = host.getHostTorrent( torrent );
		
		if ( host_torrent != null ){
			
				// it we remove a torrent while it is hosted then we flip it into passive mode to
				// keep it around in a sensible state
			
				// we've got to ensure that the torrent's file location is available in the torrent itself
				// as we're moving from download-managed persistence to host managed :(
			
				// check file already exists - might have already been deleted as in the
				// case of shared resources
			
			File	torrent_file = new File( torrent_file_str );
			
			if ( torrent_file.exists()){
				
				try{
					TorrentUtils.writeToFile( host_torrent.getTorrent(), torrent_file, false );
				
					host_torrent.setPassive( true );
					
				}catch( Throwable e ){
					
					Debug.out( "Failed to make torrent '" + torrent_file_str + "' passive: " + Debug.getNestedExceptionMessage(e));
				}
			}
		}
	}
	
	protected void
	torrentAdded(
		String			torrent_file_str,
		TOTorrent		torrent )
	{
		TRHostTorrent	host_torrent = host.getHostTorrent( torrent );
		
		if ( host_torrent != null ){
			
			if ( host_torrent.getTorrent() != torrent ){
				
				host_torrent.setTorrent( torrent );
			}
		}
	}
	
	protected void
	destroy()
	{			
		host.close();
	}
}