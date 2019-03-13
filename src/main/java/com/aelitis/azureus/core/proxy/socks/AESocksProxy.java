/*
 * Created on 08-Dec-2004
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

package com.aelitis.azureus.core.proxy.socks;

/**
 * @author parg
 *
 */

public interface 
AESocksProxy 
{
	String	PV_4		= "V4";
	String	PV_4a		= "V4a";
	String	PV_5		= "V5";
	
	int
	getPort();
	
	AESocksProxyPlugableConnection
	getDefaultPlugableConnection(
            AESocksProxyConnection basis);
	
		/**
		 * Set the next SOCKS proxy in a chain - i.e. this socks proxy's default plugable connection
		 * will connect onwards using this SOCKS proxy
		 *  
		 * @param host
		 * @param port
		 */

        void
	setNextSOCKSProxy(
                String host,
                int port,
                String proxy_version);
	
	String
	getNextSOCKSProxyHost();
	
	int
	getNextSOCKSProxyPort();
	
	String
	getNextSOCKSProxyVersion();
	
	void
	setAllowExternalConnections(
            boolean permit);
	
	void
	destroy();
}
