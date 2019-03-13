/*
 * Created on 16 Jun 2006
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

package com.aelitis.azureus.core.networkmanager;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.aelitis.azureus.core.networkmanager.Transport.ConnectListener;

public interface 
ProtocolEndpoint 
{
	int	PROTOCOL_TCP	= 1;
	int	PROTOCOL_UDP	= 2;
	int	PROTOCOL_UTP	= 3;

	int CONNECT_PRIORITY_SUPER_HIGHEST	= 0;
	int CONNECT_PRIORITY_HIGHEST		= 1;
	int CONNECT_PRIORITY_HIGH			= 2;
	int CONNECT_PRIORITY_MEDIUM			= 3;
	int CONNECT_PRIORITY_LOW			= 4;
	  

	int
	getType();
	
	ConnectionEndpoint
	getConnectionEndpoint();
	
	void
	setConnectionEndpoint(
            ConnectionEndpoint ce);
	
	InetSocketAddress
	getAddress();
	
	InetSocketAddress
	getAdjustedAddress(
            boolean to_lan);
	
	Transport
	connectOutbound(
            boolean connect_with_crypto,
            boolean allow_fallback,
            byte[][] shared_secrets,
            ByteBuffer initial_data,
            int priority,
            ConnectListener listener);
	
	String
	getDescription();
}
