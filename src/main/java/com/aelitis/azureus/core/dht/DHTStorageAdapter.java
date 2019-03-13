/*
 * Created on 12-Mar-2005
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

import java.io.DataInputStream;
import java.io.IOException;

import org.gudy.azureus2.core3.util.HashWrapper;

import com.aelitis.azureus.core.dht.transport.DHTTransportContact;
import com.aelitis.azureus.core.dht.transport.DHTTransportValue;

/**
 * @author parg
 *
 */

public interface 
DHTStorageAdapter 
{
	int
	getNetwork();
	
		// local value operations
	
		/**
		 * Create a new storage key for a given key
		 * @return null if the key shouldn't be allocated (e.g.out of space)
		 */

        DHTStorageKey
	keyCreated(
                HashWrapper key,
                boolean local);
	
	void
	keyDeleted(
            DHTStorageKey adapter_key);
	
	void
	keyRead(
            DHTStorageKey adapter_key,
            DHTTransportContact contact);
	
	DHTStorageKeyStats
	deserialiseStats(
            DataInputStream is)
	
		throws IOException;
	
	void
	valueAdded(
            DHTStorageKey key,
            DHTTransportValue value);
	
	void
	valueUpdated(
            DHTStorageKey key,
            DHTTransportValue old_value,
            DHTTransportValue new_value);
	
	void
	valueDeleted(
            DHTStorageKey key,
            DHTTransportValue value);
	
		// local lookup/put operations
	
	boolean
	isDiversified(
            byte[] key);
	
	byte[][]
	getExistingDiversification(
            byte[] key,
            boolean put_operation,
            boolean exhaustive_get,
            int max_depth);
	
	byte[][]
	createNewDiversification(
            String description,
            DHTTransportContact cause,
            byte[] key,
            boolean put_operation,
            byte diversification_type,
            boolean exhaustive_get,
            int max_depth);
	
	int
	getNextValueVersions(
            int num);
	
	DHTStorageBlock
	keyBlockRequest(
            DHTTransportContact direct_sender,
            byte[] request,
            byte[] signature);
	
	DHTStorageBlock
	getKeyBlockDetails(
            byte[] key);
	
	DHTStorageBlock[]
	getDirectKeyBlocks();

	byte[]
	getKeyForKeyBlock(
            byte[] request);
	
	void
	setStorageForKey(
            String key,
            byte[] data);
	
	byte[]
	getStorageForKey(
            String key);
	
	int
	getRemoteFreqDivCount();
	
	int
	getRemoteSizeDivCount();
	
	int
	getKeyCount();
}
