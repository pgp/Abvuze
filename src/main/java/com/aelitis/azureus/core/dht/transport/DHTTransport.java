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

import java.io.*;
import java.util.Map;

public interface 
DHTTransport 
{	
	byte GF_NONE				= 0x00;
	byte GF_DHT_SLEEPING		= 0x01;

	byte
	getProtocolVersion();
	
	byte
	getMinimumProtocolVersion();
	
	int
	getNetwork();

	boolean
	isIPV6();
	
	byte
	getGenericFlags();
	
	void
	setGenericFlag(
            byte flag,
            boolean value);
	
	void
	setSuspended(
            boolean susp);
	
		/**
		 * Gives access to the node ID for this transport 
		 * @return
		 */

        DHTTransportContact
	getLocalContact();
	
	int
	getPort();
	
	void
	setPort(
            int port)
	
		throws DHTTransportException;
	
	long
	getTimeout();
	
	void
	setTimeout(
            long millis);
	
	DHTTransportContact
	importContact(
            DataInputStream is,
            boolean is_bootstrap)
	
		throws IOException, DHTTransportException;
	
		/**
		 * Set the handler for incoming requests
		 * @param receiver
		 */

        void
	setRequestHandler(
                DHTTransportRequestHandler receiver);
	
	DHTTransportStats
	getStats();
	
		// direct contact-contact communication
	
	void
	registerTransferHandler(
            byte[] handler_key,
            DHTTransportTransferHandler handler);
	
	void
	registerTransferHandler(
            byte[] handler_key,
            DHTTransportTransferHandler handler,
            Map<String, Object> options);
	
	void
	unregisterTransferHandler(
            byte[] handler_key,
            DHTTransportTransferHandler handler);
	
	byte[]
	readTransfer(
            DHTTransportProgressListener listener,
            DHTTransportContact target,
            byte[] handler_key,
            byte[] key,
            long timeout)
	
		throws DHTTransportException;
	
	void
	writeTransfer(
            DHTTransportProgressListener listener,
            DHTTransportContact target,
            byte[] handler_key,
            byte[] key,
            byte[] data,
            long timeout)
	
		throws DHTTransportException;
	
	byte[]
	writeReadTransfer(
            DHTTransportProgressListener listener,
            DHTTransportContact target,
            byte[] handler_key,
            byte[] data,
            long timeout)
	
		throws DHTTransportException;

	boolean
	supportsStorage();
	
	boolean
	isReachable();
	
	DHTTransportContact[]
	getReachableContacts();
	
	DHTTransportContact[]
	getRecentContacts();
	
	void
	addListener(
            DHTTransportListener l);
	
	void
	removeListener(
            DHTTransportListener l);
}
