/*
 * Created on 31-Jan-2005
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
DHTTransportFullStats 
{
		// DB
	
	long
	getDBValueCount();
	
	long
	getDBKeyCount();

	long
	getDBValuesStored();
	
	long
	getDBKeysBlocked();
	
	long
	getDBKeyDivSizeCount();
	
	long
	getDBKeyDivFreqCount();

	long
	getDBStoreSize();

		// Router
	
	long
	getRouterNodes();
	
	long
	getRouterLeaves();
	
	long
	getRouterContacts();
	
	long
	getRouterUptime();
	
	int
	getRouterCount();
	
		// Transport
	
		// totals
	
	long
	getTotalBytesReceived();
	
	long
	getTotalBytesSent();
	
	long
	getTotalPacketsReceived();
	
	long
	getTotalPacketsSent();
	
	long
	getTotalPingsReceived();
	
	long
	getTotalFindNodesReceived();
	
	long
	getTotalFindValuesReceived();
	
	long
	getTotalStoresReceived();
	
	long
	getTotalKeyBlocksReceived();
	
	long
	getIncomingRequests();
	
		// averages
	
	long
	getAverageBytesReceived();
	
	long
	getAverageBytesSent();
	
	long
	getAveragePacketsReceived();
	
	long
	getAveragePacketsSent();
	
	String
	getVersion();
	
	String
	getString();
}
