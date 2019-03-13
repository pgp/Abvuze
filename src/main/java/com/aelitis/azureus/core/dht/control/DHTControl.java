/*
 * Created on 12-Jan-2005
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

package com.aelitis.azureus.core.dht.control;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.List;

import com.aelitis.azureus.core.dht.DHTOperationListener;
import com.aelitis.azureus.core.dht.db.DHTDB;
import com.aelitis.azureus.core.dht.router.DHTRouter;
import com.aelitis.azureus.core.dht.transport.*;

/**
 * @author parg
 *
 */

public interface 
DHTControl 
{
	int		K_DEFAULT								= 20;
	int		B_DEFAULT								= 4;
	int		MAX_REP_PER_NODE_DEFAULT				= 5;
	int		SEARCH_CONCURRENCY_DEFAULT				= 5;
	int		LOOKUP_CONCURRENCY_DEFAULT				= 10;
	int		CACHE_AT_CLOSEST_N_DEFAULT				= 1;
	int		ORIGINAL_REPUBLISH_INTERVAL_DEFAULT		= 8*60*60*1000;
	int		CACHE_REPUBLISH_INTERVAL_DEFAULT		=   30*60*1000;
	int		ENCODE_KEYS_DEFAULT						= 1;
	int		ENABLE_RANDOM_DEFAULT					= 1;
	
	void
	seed(
            boolean full_wait);
		
	boolean
	isSeeded();
	
	/**
	 * Manually mark things as seeded
	 */

    void
	setSeeded();
	
	void
	setSuspended(
            boolean susp);
	
	void
	put(
            byte[] key,
            String description,
            byte[] value,
            short flags,
            byte life_hours,
            byte replication_control,
            boolean high_priority,
            DHTOperationListener listener);
	
	boolean
	isDiversified(
            byte[] key);
	
	DHTTransportValue
	getLocalValue(
            byte[] key);
		
	List<DHTTransportValue>
	getStoredValues(
            byte[] key);
	
	void
	get(
            byte[] key,
            String description,
            short flags,
            int max_values,
            long timeout,
            boolean exhaustive,
            boolean high_priority,
            DHTOperationListener listener);
		
	byte[]
	remove(
            byte[] key,
            String description,
            DHTOperationListener listener);
	
	byte[]
	remove(
            DHTTransportContact[] contacts,
            byte[] key,
            String description,
            DHTOperationListener listener);
	
	DHTControlStats
	getStats();
	
	void
	setSleeping(
            boolean asleep);
	
	DHTTransport
	getTransport();
	
	DHTRouter
	getRouter();
	
	DHTDB
	getDataBase();
	
	DHTControlActivity[]
	getActivities();
	
	void
	exportState(
            DataOutputStream os,
            int max)
		
		throws IOException;
		
	void
	importState(
            DataInputStream is)
		
		throws IOException;
	
		// support methods for DB
	
	List<DHTTransportContact>
	getClosestKContactsList(
            byte[] id,
            boolean live_only);
	
	List<DHTTransportContact>
	getClosestContactsList(
            byte[] id,
            int num_to_return,
            boolean live_only);
	
	void
	putEncodedKey(
            byte[] key,
            String description,
            DHTTransportValue value,
            long timeout,
            boolean original_mappings);
	
	void
	putDirectEncodedKeys(
            byte[][] keys,
            String description,
            DHTTransportValue[][] value_sets,
            List<DHTTransportContact> contacts);
	
	void
	putDirectEncodedKeys(
            byte[][] keys,
            String description,
            DHTTransportValue[][] value_sets,
            DHTTransportContact contact,
            DHTOperationListener listener);
	
	int
	computeAndCompareDistances(
            byte[] n1,
            byte[] n2,
            byte[] pivot);
	
	byte[]
	computeDistance(
            byte[] n1,
            byte[] n2);
	
	int
	compareDistances(
            byte[] n1,
            byte[] n2);
	
	boolean
	verifyContact(
            DHTTransportContact c,
            boolean direct);
	
	boolean
	lookup(
            byte[] id,
            String description,
            long timeout,
            DHTOperationListener listener);
	
	boolean
	lookupEncoded(
            byte[] id,
            String description,
            long timeout,
            boolean high_priority,
            DHTOperationListener listener);
	
	byte[]
	getObfuscatedKey(
            byte[] plain_key);
	
	
	List<DHTControlContact>
	getContacts();
	
		// debug method only
	
	void
	pingAll();
	
	void
	addListener(
            DHTControlListener l);
	
	void
	removeListener(
            DHTControlListener l);
	
	void
	destroy();
	
	void
	print(
            boolean full);
}
