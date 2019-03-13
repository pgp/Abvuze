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
import java.util.Map;

/**
 * @author parg
 *
 */

public interface 
DistributedDatabaseContact 
{
	byte[]
	getID();
	
	String
	getName();
	
	int
	getVersion();
	
	InetSocketAddress
	getAddress();
	
	int
	getDHT();
	
	boolean
	isAlive(
            long timeout);
	
		// async version - event types: complete -> alive, timeout -> dead
	
	void
	isAlive(
            long timeout,
            DistributedDatabaseListener listener);
	
	boolean
	isOrHasBeenLocal();
	
	Map<String,Object>
	exportToMap();
	
		/**
		 * Tries to open a NAT tunnel to the contact. Should only be used if direct contact fails
		 * @return
		 */

        boolean
	openTunnel();
	
	DistributedDatabaseValue
	call(
            DistributedDatabaseProgressListener listener,
            DistributedDatabaseTransferType type,
            DistributedDatabaseValue data,
            long timeout)
	
		throws DistributedDatabaseException;
	
	void
	write(
            DistributedDatabaseProgressListener listener,
            DistributedDatabaseTransferType type,
            DistributedDatabaseKey key,
            DistributedDatabaseValue data,
            long timeout)
	
		throws DistributedDatabaseException;
	
	DistributedDatabaseValue
	read(
            DistributedDatabaseProgressListener listener,
            DistributedDatabaseTransferType type,
            DistributedDatabaseKey key,
            long timeout)
	
		throws DistributedDatabaseException;
}
