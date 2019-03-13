/*
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
package org.gudy.azureus2.core3.disk;

import org.gudy.azureus2.plugins.peers.PeerReadRequest;

/**
 * 
 * This class represents a Bittorrent Request.
 * and a time stamp to know when it was created.
 * 
 * Request may expire after some time, which is used to determine who is snubbed.
 * 
 * @author Olivier
 *
 * 
 */
public interface 
DiskManagerReadRequest
	extends PeerReadRequest, DiskManagerRequest
{  
	int getPieceNumber();
 
	int getOffset();
 
	int getLength();
	
	long getTimeCreated(long now);
	
	void setTimeSent(long time);
	
	long getTimeSent();
	
		/**
		 * If flush is set then data held in memory will be flushed to disk during the read operation
		 * @param flush
		 */

        void
	setFlush(
                boolean flush);
	
	boolean
	getFlush();
	
	void
	setUseCache(
            boolean cache);
	
	boolean
	getUseCache();
	
	void
	setLatencyTest();
	
	boolean
	isLatencyTest();
	
	 /**
	   * We override the equals method
	   * 2 requests are equals if
	   * all their bt fields (piece number, offset, length) are equal
	   */
     boolean equals(Object o);
	int	hashCode();
}