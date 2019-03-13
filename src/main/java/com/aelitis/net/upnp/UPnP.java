/*
 * Created on 14-Jun-2004
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

package com.aelitis.net.upnp;

import java.util.Map;

/**
 * @author parg
 *
 */

public interface 
UPnP 
{
	UPnPRootDevice[]
	getRootDevices();
	
		/**
		 * resets by removing all root devices and then rediscovering them
		 *
		 */

        void
	reset();
	
		/**
		 * scan for new
		 */

        void
	search();
	
	void
	search(
            String[] STs);
	
	void
	injectDiscoveryCache(
            Map cache);
	
	UPnPSSDP
	getSSDP();
	
		/**
		 * Logs a message to all registered log listeners
		 * @param str
		 */

        void
	log(
                String str);
	
	void
	addRootDeviceListener(
            UPnPListener l);
	
	void
	removeRootDeviceListener(
            UPnPListener l);
	
	void
	addLogListener(
            UPnPLogListener l);
		
	void
	removeLogListener(
            UPnPLogListener l);
}
