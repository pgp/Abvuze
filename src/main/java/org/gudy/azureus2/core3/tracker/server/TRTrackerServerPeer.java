/*
 * File    : TRTrackerServerPeer.java
 * Created : 31-Oct-2003
 * By      : parg
 * 
 * Azureus - a Java Bittorrent client
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details ( see the LICENSE file ).
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 
package org.gudy.azureus2.core3.tracker.server;

import java.util.Map;

/**
 * @author parg
 */

public interface 
TRTrackerServerPeer 
	extends TRTrackerServerPeerBase
{
	byte	NAT_CHECK_UNKNOWN				= 0;
	byte	NAT_CHECK_DISABLED				= 1;
	byte	NAT_CHECK_INITIATED				= 2;
	byte	NAT_CHECK_OK					= 3;
	byte	NAT_CHECK_FAILED				= 4;
	byte	NAT_CHECK_FAILED_AND_REPORTED	= 5;
	
	byte	CRYPTO_NONE				= 0;
	byte	CRYPTO_SUPPORTED		= 1;
	byte	CRYPTO_REQUIRED			= 2;
	
	
	long
	getUploaded();
	
	long
	getDownloaded();
	
	long
	getAmountLeft();
	
	String
	getIPRaw();
	
	byte[]
	getPeerID();
	
		/**
		 * returns the current NAT status of the peer
		 * @return
		 */

        byte
	getNATStatus();
	
	boolean
	isBiased();
	
	void
	setBiased(
            boolean bias);
	
	void
	setUserData(
            Object key,
            Object data);
	
	Object
	getUserData(
            Object key);
	
	Map
	export();
}
