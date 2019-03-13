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

package com.aelitis.azureus.core.dht.transport;

/**
 * @author parg
 *
 */

import java.util.*;

public interface 
DHTTransportRequestHandler 
{
	void
	pingRequest(
            DHTTransportContact contact);
		
	void
	keyBlockRequest(
            DHTTransportContact contact,
            byte[] key_block_request,
            byte[] key_block_signature);
	
	DHTTransportFullStats
	statsRequest(
            DHTTransportContact contact);
	
	DHTTransportStoreReply
	storeRequest(
            DHTTransportContact contact,
            byte[][] keys,
            DHTTransportValue[][] value_sets);
	
	DHTTransportQueryStoreReply
	queryStoreRequest(
            DHTTransportContact contact,
            int header_len,
            List<Object[]> keys);
	
	DHTTransportContact[]
	findNodeRequest(
            DHTTransportContact contact,
            byte[] id);
	
	DHTTransportFindValueReply
	findValueRequest(
            DHTTransportContact contact,
            byte[] key,
            int max_values,
            short flags);

		/**
		 * Mechanism for reporting that a contact has been imported
		 * @param contact
		 */

        void
	contactImported(
                DHTTransportContact contact,
                boolean is_bootstrap);
	
	void
	contactRemoved(
            DHTTransportContact contact);
	
	int
	getTransportEstimatedDHTSize();
	
	void
	setTransportEstimatedDHTSize(
            int size);
}
