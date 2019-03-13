/*
 * Created on 27-Aug-2004
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

package org.gudy.azureus2.core3.tracker.client;

import java.net.URL;

import org.gudy.azureus2.core3.util.HashWrapper;

/**
 * @author parg
 *
 */

public interface 
TRTrackerScraperClientResolver 
{
	Character FL_NONE					= 'n';
	Character FL_INCOMPLETE_STOPPED		= 's';
	Character FL_INCOMPLETE_QUEUED		= 'q';
	Character FL_INCOMPLETE_RUNNING		= 'r';
	Character FL_COMPLETE_STOPPED		= 'S';
	Character FL_COMPLETE_QUEUED		= 'Q';
	Character FL_COMPLETE_RUNNING		= 'R';
		
	boolean
	isScrapable(
            HashWrapper torrent_hash);
	
		/**
		 * 
		 * @param hash
		 * @return
		 */

        int[]
	getCachedScrape(
                HashWrapper hash);

	boolean
	isNetworkEnabled(
            HashWrapper hash,
            URL url);
	
	String[]
	getEnabledNetworks(
            HashWrapper hash);
	
		/**
		 * Two kinds of extensions: entry [0] = String (or null) that gets passed with the scrape verbotem after infohash
		 * entry [1] = Character - status of download, aggregated into a single String passed with scrape
		 * status flags are above FL_ values
		 * @param hash
		 * @return
		 */

        Object[]
	getExtensions(
                HashWrapper hash);
	
	boolean
	redirectTrackerUrl(
            HashWrapper hash,
            URL old_url,
            URL new_url);
}
