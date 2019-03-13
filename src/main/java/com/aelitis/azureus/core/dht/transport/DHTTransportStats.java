/*
 * Created on 18-Jan-2005
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

package com.aelitis.azureus.core.dht.transport;


/**
 * @author parg
 *
 */

public interface 
DHTTransportStats 
{
	int	STAT_SENT		= 0;
	int	STAT_OK			= 1;
	int	STAT_FAILED		= 2;
	int	STAT_RECEIVED	= 3;
	
		/**
		 * returns pings sent, pings succeeded, pings failed, pings received
		 * @return
		 */

        long[]
	getPings();
	
	long[]
	getFindNodes();
	
	long[]
	getFindValues();
	
	long[]
	getStores();
	
	long[]
	getQueryStores();
	
	long[]
	getData();
	
	long[]
	getKeyBlocks();
	
		// aliens are indexed by these constants

	int AT_FIND_NODE		= 0;
	int AT_FIND_VALUE		= 1;
	int AT_PING				= 2;
	int AT_STATS			= 3;
	int AT_STORE			= 4;
	int AT_KEY_BLOCK		= 5;
	int AT_QUERY_STORE		= 6;
	
	long[]
	getAliens();
	
	long
	getIncomingRequests();
	
	long
	getPacketsSent();
	
	long
	getPacketsReceived();
	
	long
	getRequestsTimedOut();
	
	long
	getBytesSent();
	
	long
	getBytesReceived();
	
	DHTTransportStats
	snapshot();
	
	long
	getSkewAverage();
	
		/**
		 * -1 if stats not yet available 
		 * @return
		 */

        int
	getRouteablePercentage();
	
	int[]
	getRTTHistory();
	
	String
	getString();
}
