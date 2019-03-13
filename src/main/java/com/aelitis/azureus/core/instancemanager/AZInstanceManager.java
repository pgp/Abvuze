/*
 * Created on 20-Dec-2005
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

package com.aelitis.azureus.core.instancemanager;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.regex.PatternSyntaxException;


public interface 
AZInstanceManager 
{
	int	AT_TCP				= 1;
	int	AT_UDP				= 2;
	int	AT_UDP_NON_DATA		= 3;
	
	void
	initialize();
	
	boolean
	isInitialized();
	
	AZInstance
	getMyInstance();
	
	int
	getOtherInstanceCount(
            boolean block_if_needed);
	
	AZInstance[]
	getOtherInstances();
	
	void
	updateNow();
	
	AZInstanceTracked[]
	track(
            byte[] hash,
            AZInstanceTracked.TrackTarget target);
	
	InetSocketAddress
	getLANAddress(
            InetSocketAddress external_address,
            int address_type);
	
	InetSocketAddress
	getExternalAddress(
            InetSocketAddress lan_address,
            int address_type);
	
	boolean
	isLANAddress(
            InetAddress address);
	
	boolean
	isExternalAddress(
            InetAddress address);
	
	boolean
	addLANSubnet(
            String subnet)
	
		throws PatternSyntaxException;
	
	void
	addLANAddress(
            InetAddress address);
	
	void
	removeLANAddress(
            InetAddress address);
	
	boolean
	getIncludeWellKnownLANs();
	
	void
	setIncludeWellKnownLANs(
            boolean include);
	
	long
	getClockSkew();
	
	boolean
	addInstance(
            InetAddress explicit_address);
	
	void
	addListener(
            AZInstanceManagerListener l);
	
	void
	removeListener(
            AZInstanceManagerListener l);
}
