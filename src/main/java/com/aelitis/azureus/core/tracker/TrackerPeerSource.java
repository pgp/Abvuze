/*
 * Created on Dec 8, 2009
 * Created by Paul Gardner
 * 
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
 */


package com.aelitis.azureus.core.tracker;

public interface 
TrackerPeerSource 
{
	int TP_UNKNOWN		= 0;
	int TP_TRACKER		= 1;
	int TP_HTTP_SEED	= 2;
	int TP_DHT			= 3;
	int TP_LAN			= 4;
	int TP_PEX			= 5;
	int TP_INCOMING		= 6;
	int TP_PLUGIN		= 7;
	
	int ST_UNKNOWN		= 0;
	int ST_DISABLED		= 1;
	int ST_STOPPED		= 2;
	int ST_QUEUED		= 3;
	int ST_UPDATING		= 4;
	int ST_ONLINE 		= 5;
	int ST_ERROR		= 6;
	int ST_AVAILABLE	= 7;
	int ST_UNAVAILABLE	= 8;
	int ST_INITIALISING	= 9;

	
	int
	getType();
	
	String
	getName();
	
	int
	getStatus();
	
	String
	getStatusString();
	
	int
	getSeedCount();
	
	int
	getLeecherCount();
	
	int
	getPeers();
	
		/**
		 * If the tracker source supports 'number downloads completed' this will be >= 0 
		 * @return
		 */

        int
	getCompletedCount();
	
	int
	getLastUpdate();
	
	int
	getSecondsToUpdate();
	
	int
	getInterval();
	
	int
	getMinInterval();
	
	boolean
	isUpdating();
	
	boolean
	canManuallyUpdate();
	
	void
	manualUpdate();
	
	boolean
	canDelete();
	
	void
	delete();
}
