/*
 * Created on 11-Jan-2005
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

package com.aelitis.azureus.core.dht;

import java.io.*;
import java.util.List;

import com.aelitis.azureus.core.dht.control.DHTControl;
import com.aelitis.azureus.core.dht.db.DHTDB;
import com.aelitis.azureus.core.dht.nat.DHTNATPuncher;
import com.aelitis.azureus.core.dht.router.DHTRouter;
import com.aelitis.azureus.core.dht.transport.DHTTransport;
import com.aelitis.azureus.core.dht.transport.DHTTransportContact;
import com.aelitis.azureus.core.dht.transport.DHTTransportValue;

/**
 * @author parg
 *
 */

public interface 
DHT 
{
		// all property values are Integer values
	
	String	PR_CONTACTS_PER_NODE					= "EntriesPerNode";
	String	PR_NODE_SPLIT_FACTOR					= "NodeSplitFactor";
	String	PR_SEARCH_CONCURRENCY					= "SearchConcurrency";
	String	PR_LOOKUP_CONCURRENCY					= "LookupConcurrency";
	String	PR_MAX_REPLACEMENTS_PER_NODE			= "ReplacementsPerNode";
	String	PR_CACHE_AT_CLOSEST_N					= "CacheClosestN";
	String	PR_ORIGINAL_REPUBLISH_INTERVAL			= "OriginalRepublishInterval";
	String	PR_CACHE_REPUBLISH_INTERVAL				= "CacheRepublishInterval";
	String	PR_ENCODE_KEYS							= "EncodeKeys";
	String	PR_ENABLE_RANDOM_LOOKUP					= "EnableRandomLookup";

	short		FLAG_NONE				= 0x0000;
	short		FLAG_SINGLE_VALUE		= FLAG_NONE;
	short		FLAG_DOWNLOADING		= 0x0001;
	short		FLAG_SEEDING			= 0x0002;
	short		FLAG_MULTI_VALUE		= 0x0004;
	short		FLAG_STATS				= 0x0008;
	short		FLAG_ANON				= 0x0010;
	short		FLAG_PRECIOUS			= 0x0020;
	short		FLAG_BRIDGED			= 0x0040;
	
		// only a single byte is serialized for flags so these ones ain't going nowhere remote!
	
	short		FLAG_PUT_AND_FORGET		= 0x0100;			// local only
	short		FLAG_OBFUSCATE_LOOKUP	= 0x0200;			// local only
	short		FLAG_LOOKUP_FOR_STORE	= 0x0400;			// local only
	short		FLAG_HIGH_PRIORITY		= 0x0800;			// local only, used in plugin to transmit priority through call stack

	int 	MAX_VALUE_SIZE		= 512;

	byte	REP_FACT_NONE			= 0;
	byte	REP_FACT_DEFAULT		= (byte)0xff;
	
		// diversification types, don't change as serialised!!!!
	
	byte	DT_NONE			= 1;
	byte	DT_FREQUENCY	= 2;
	byte	DT_SIZE			= 3;
	
	String[]	DT_STRINGS = { "", "None", "Freq", "Size" };
	
	int		NW_MAIN			= 0;
	int		NW_CVS			= 1;
	int		NW_MAIN_V6		= 3;
	
	void
	put(
            byte[] key,
            String description,
            byte[] value,
            short flags,
            DHTOperationListener listener);
	
		/**
		 * default is HIGH PRIORITY. if you change to low priority then do so consistently as
		 * operations can get out of order otherwise
		 * @param key
		 * @param description
		 * @param value
		 * @param flags
		 * @param high_priority
		 * @param listener
		 */

        void
	put(
                byte[] key,
                String description,
                byte[] value,
                short flags,
                boolean high_priority,
                DHTOperationListener listener);
	
	void
	put(
            byte[] key,
            String description,
            byte[] value,
            short flags,
            byte life_hours,
            boolean high_priority,
            DHTOperationListener listener);

	void
	put(
            byte[] key,
            String description,
            byte[] value,
            short flags,
            byte life_hours,
            byte replication_control,    // 4 bits 1->14 republish hours; 0=vuze default | 4 bits 0->15 maintain replicas; [ff=no replication control-use default]
            boolean high_priority,
            DHTOperationListener listener);
	
		/**
		 * Returns value if originated from here for key
		 * @param key
		 * @return
		 */

        DHTTransportValue
	getLocalValue(
                byte[] key);
	
	List<DHTTransportValue>
	getStoredValues(
            byte[] key);

		/**
		 * @param key
		 * @param max_values
		 * @param timeout
		 * @param listener
		 */

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
	
	boolean
	isDiversified(
            byte[] key);
	
	int
	getIntProperty(
            String name);
	
	DHTTransport
	getTransport();
	
	DHTRouter
	getRouter();
	
	DHTControl
	getControl();
	
	DHTDB
	getDataBase();
	
	DHTNATPuncher
	getNATPuncher();
	
	DHTStorageAdapter
	getStorageAdapter();
	
		/**
		 * externalises information that allows the DHT to be recreated at a later date
		 * and populated via the import method
		 * @param os
		 * @param max  maximum to export, 0 -> all
		 * @throws IOException
		 */

        void
	exportState(
                DataOutputStream os,
                int max)
	
		throws IOException;
	
		/**
		 * populate the DHT with previously exported state 
		 * @param is
		 * @throws IOException
		 */

        void
	importState(
                DataInputStream is)
	
		throws IOException;
	
		/**
		 * Integrate the node into the DHT
		 * Can be invoked more than once if additional state is imported
		 */

        void
	integrate(
                boolean full_wait);

	void
	setSuspended(
            boolean susp);
	
	void
	destroy();
	
	boolean
	isSleeping();
	
	void
	setLogging(
            boolean on);

	DHTLogger
	getLogger();
	
	void
	print(
            boolean full);
	
	void
	addListener(
            DHTListener listener);
	
	void
	removeListener(
            DHTListener listener);
}
