/*
 * Created on Sep 3, 2014
 * Created by Paul Gardner
 * 
 * Copyright 2014 Azureus Software, Inc.  All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */


package com.aelitis.azureus.plugins.dht;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

import com.aelitis.azureus.core.dht.DHT;

public interface 
DHTPluginInterface 
{
	byte		FLAG_SINGLE_VALUE	= DHT.FLAG_SINGLE_VALUE;
	byte		FLAG_DOWNLOADING	= DHT.FLAG_DOWNLOADING;
	byte		FLAG_SEEDING		= DHT.FLAG_SEEDING;
	byte		FLAG_MULTI_VALUE	= DHT.FLAG_MULTI_VALUE;
	byte		FLAG_STATS			= DHT.FLAG_STATS;
	byte		FLAG_ANON			= DHT.FLAG_ANON;
	byte		FLAG_PRECIOUS		= DHT.FLAG_PRECIOUS;
	byte		FLAG_BRIDGED		= DHT.FLAG_BRIDGED;

	int			MAX_VALUE_SIZE		= DHT.MAX_VALUE_SIZE;


	
	boolean
	isEnabled();
	
	boolean
	isExtendedUseAllowed();
	
	boolean
	isInitialising();
	
	boolean
	isSleeping();
	
	DHTPluginContact
	getLocalAddress();
	
	String
	getNetwork();
	
	DHTPluginKeyStats
	decodeStats(
            DHTPluginValue value);
	
	void
	registerHandler(
            byte[] handler_key,
            DHTPluginTransferHandler handler,
            Map<String, Object> options);
	
	void
	unregisterHandler(
            byte[] handler_key,
            DHTPluginTransferHandler handler);

	DHTPluginContact
	importContact(
            InetSocketAddress address);
	
	DHTPluginContact
	importContact(
            InetSocketAddress address,
            byte version);
	
	DHTPluginContact
	importContact(
            InetSocketAddress address,
            byte version,
            boolean is_cvs);
	
	DHTPluginContact
	importContact(
            Map<String, Object> map);
	
	void
	get(
            byte[] original_key,
            String description,
            byte flags,
            int max_values,
            long timeout,
            boolean exhaustive,
            boolean high_priority,
            DHTPluginOperationListener original_listener);
	
	void
	put(
            byte[] key,
            String description,
            byte[] value,
            byte flags,
            DHTPluginOperationListener listener);
	
	DHTInterface[]
	getDHTInterfaces();
	
	List<DHTPluginValue>
	getValues();

	List<DHTPluginValue>
	getValues(
            byte[] key);

	void
	remove(
            byte[] key,
            String description,
            DHTPluginOperationListener listener);
	
	void
	remove(
            DHTPluginContact[] targets,
            byte[] key,
            String description,
            DHTPluginOperationListener listener);
	
	void
	addListener(
            DHTPluginListener l);

	
	void
	removeListener(
            DHTPluginListener l);

	
	void
	log(
            String str);

	interface
	DHTInterface
	{
		byte[]
		getID();
		
		boolean
		isIPV6();
		
		int
		getNetwork();
				
		DHTPluginContact[]
		getReachableContacts();
		
		DHTPluginContact[]
		getRecentContacts();
		
		List<DHTPluginContact>
		getClosestContacts(
                byte[] to_id,
                boolean live_only);
	}
	
}
