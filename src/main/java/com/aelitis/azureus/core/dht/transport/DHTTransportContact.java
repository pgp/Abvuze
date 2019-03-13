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
import java.io.*;
import java.net.InetSocketAddress;

import com.aelitis.azureus.core.dht.netcoords.DHTNetworkPosition;

public interface 
DHTTransportContact
{
	int RANDOM_ID_TYPE1	= 1;
	int RANDOM_ID_TYPE2	= 2;
	
	int
	getMaxFailForLiveCount();
	
	int
	getMaxFailForUnknownCount();
	
	int
	getInstanceID();
	
	byte[]
	getID();
	
	byte
	getProtocolVersion();
	
	long
	getClockSkew();
	
	int
	getRandomIDType();
	
	void
	setRandomID(
            int id);
	
	int
	getRandomID();
	
	void
	setRandomID2(
            byte[] id);
	
	byte[]
	getRandomID2();
	
	String
	getName();
	
	byte[]
	getBloomKey();
	
	InetSocketAddress
	getAddress();
	
	InetSocketAddress
	getTransportAddress();
	
	InetSocketAddress
	getExternalAddress();
	
	boolean
	isAlive(
            long timeout);

	void
	isAlive(
            DHTTransportReplyHandler handler,
            long timeout);
	
	boolean
	isValid();
	
	boolean
	isSleeping();
	
	void
	sendPing(
            DHTTransportReplyHandler handler);
	
	void
	sendImmediatePing(
            DHTTransportReplyHandler handler,
            long timeout);

	void
	sendStats(
            DHTTransportReplyHandler handler);
	
	void
	sendStore(
            DHTTransportReplyHandler handler,
            byte[][] keys,
            DHTTransportValue[][] value_sets,
            boolean immediate);
	
	void
	sendQueryStore(
            DHTTransportReplyHandler handler,
            int header_length,
            List<Object[]> key_details);
	
	void
	sendFindNode(
            DHTTransportReplyHandler handler,
            byte[] id,
            short flags);
		
	void
	sendFindValue(
            DHTTransportReplyHandler handler,
            byte[] key,
            int max_values,
            short flags);
		
	void
	sendKeyBlock(
            DHTTransportReplyHandler handler,
            byte[] key_block_request,
            byte[] key_block_signature);

	DHTTransportFullStats
	getStats();
	
	void
	exportContact(
            DataOutputStream os)
	
		throws IOException, DHTTransportException;
	
	Map<String, Object>
	exportContactToMap();
	
	void
	remove();
	
	void
	createNetworkPositions(
            boolean is_local);
			
	DHTNetworkPosition[]
	getNetworkPositions();
	
	DHTNetworkPosition
	getNetworkPosition(
            byte position_type);

	DHTTransport
	getTransport();
	
	String
	getString();
}
