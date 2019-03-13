/*
 * Created on May 29, 2009
 * Created by Paul Gardner
 * 
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
 */


package com.aelitis.azureus.core.instancemanager;

import java.net.InetAddress;

import com.aelitis.azureus.plugins.dht.DHTPlugin;
import com.aelitis.azureus.plugins.upnp.UPnPPlugin;

public interface 
AZInstanceManagerAdapter 
{
	String
	getID();
	
	int[]
	getPorts();
	
	DHTPlugin
	getDHTPlugin();
	
	UPnPPlugin
	getUPnPPlugin();
	
	InetAddress
	getPublicAddress();
	
	VCPublicAddress
	getVCPublicAddress();
		
	AZInstanceTracked.TrackTarget
	track(
            byte[] hash);
	
	void
	addListener(
            StateListener listener);
	
	interface
	StateListener
	{
		void
		started();
		
		void
		stopped();
	}
	
	interface
	VCPublicAddress
	{
		String
		getAddress();
		
		long
		getCacheTime();
	}
}
