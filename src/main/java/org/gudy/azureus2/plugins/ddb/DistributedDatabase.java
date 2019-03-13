/*
 * Created on 18-Feb-2005
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

package org.gudy.azureus2.plugins.ddb;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

import com.aelitis.azureus.plugins.dht.DHTPluginInterface;

/**
 * @author parg
 *
 */

public interface 
DistributedDatabase 
{
	int	OP_NONE				= 0x00000000;
	int	OP_EXHAUSTIVE_READ	= 0x00000001;
	int	OP_PRIORITY_HIGH	= 0x00000002;
	
		// diversification types
	
	byte	DT_NONE			= 1;
	byte	DT_FREQUENCY	= 2;
	byte	DT_SIZE			= 3;
	
		// dht types
	
	int	DHT_MAIN	= 1;
	int	DHT_CVS		= 2;
	
	boolean
	isAvailable();

	boolean
	isInitialized();
	
	boolean
	isExtendedUseAllowed();
	
	String
	getNetwork();
	
	DHTPluginInterface
	getDHTPlugin();
	
	DistributedDatabaseContact
	getLocalContact();
	
	DistributedDatabaseKey
	createKey(
            Object key)
	
		throws DistributedDatabaseException;
	
	DistributedDatabaseKey
	createKey(
            Object key,
            String description)
	
		throws DistributedDatabaseException;
	

	DistributedDatabaseValue
	createValue(
            Object value)
	
		throws DistributedDatabaseException;
		
	DistributedDatabaseContact
	importContact(
            InetSocketAddress address)
	
		throws DistributedDatabaseException;
	
	DistributedDatabaseContact
	importContact(
            InetSocketAddress address,
            byte protocol_version)
	
		throws DistributedDatabaseException;
	
	DistributedDatabaseContact
	importContact(
            InetSocketAddress address,
            byte protocol_version,
            int preferred_dht)
	
		throws DistributedDatabaseException;
	
	DistributedDatabaseContact
	importContact(
            Map<String, Object> map)
	
		throws DistributedDatabaseException;
	
	void
	write(
            DistributedDatabaseListener listener,
            DistributedDatabaseKey key,
            DistributedDatabaseValue value)
	
		throws DistributedDatabaseException;
	
	void
	write(
            DistributedDatabaseListener listener,
            DistributedDatabaseKey key,
            DistributedDatabaseValue[] values)
	
		throws DistributedDatabaseException;

	void
	read(
            DistributedDatabaseListener listener,
            DistributedDatabaseKey key,
            long timeout)
	
		throws DistributedDatabaseException;
	
	void
	read(
            DistributedDatabaseListener listener,
            DistributedDatabaseKey key,
            long timeout,
            int options)
	
		throws DistributedDatabaseException;
	
	void
	readKeyStats(
            DistributedDatabaseListener listener,
            DistributedDatabaseKey key,
            long timeout)
	
		throws DistributedDatabaseException;
	
		/**
		 * Get all locally held (direct+indirect) values for a key
		 * @param key
		 * @return
		 */

        List<DistributedDatabaseValue>
	getValues(
                DistributedDatabaseKey key)
		
		throws DistributedDatabaseException;
	
	void
	delete(
            DistributedDatabaseListener listener,
            DistributedDatabaseKey key)
	
		throws DistributedDatabaseException;
	
	void
	delete(
            DistributedDatabaseListener listener,
            DistributedDatabaseKey key,
            DistributedDatabaseContact[] targets)
	
		throws DistributedDatabaseException;

	void
	addTransferHandler(
            DistributedDatabaseTransferType type,
            DistributedDatabaseTransferHandler handler)
	
		throws DistributedDatabaseException;
	
	DistributedDatabaseTransferType
	getStandardTransferType(
            int standard_type)
	
		throws DistributedDatabaseException;
	
	void
	addListener(
            DistributedDatabaseListener l);
	
	void
	removeListener(
            DistributedDatabaseListener l);
}
