/*
 * File    : IpFilter.java
 * Created : 1 oct. 2003 12:27:26
 * By      : Olivier 
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
 
package org.gudy.azureus2.core3.ipfilter;



/**
 * @author Olivier
 * 
 */

import java.util.List;
import java.io.File;
import java.net.InetAddress;


public interface 
IpFilter 
{
	File getFile();
	
	void save() throws Exception;
	
	void
	reload()
	
		throws Exception;
	
	/**
	 * deprecated and to be removed after 2.0.8.0. Left in to support old SafePeer plugin
	 * version that uses this stuff directly... 
	 * @deprecated
	 * @return
	 */
    List
	getIpRanges();
	
	IpRange[]
	getRanges();

	boolean
	isInRange(
            String ipAddress);
  
	boolean
	isInRange(
            String ipAddress,
            String torrent_name,
            byte[] torrent_hash);
	
	boolean
	isInRange(
            String ipAddress,
            String torrent_name,
            byte[] torrent_hash,
            boolean loggable);
	
	boolean
	isInRange(
            InetAddress ipAddress,
            String torrent_name,
            byte[] torrent_hash,
            boolean loggable);
	
	IpRange
	createRange(
            boolean sessionOnly);
	
	void
	addRange(
            IpRange range);
	
	void
	removeRange(
            IpRange range);
	
	int
	getNbRanges();
	
	int
	getNbIpsBlocked();
	
	int
	getNbIpsBlockedAndLoggable();
	
	BlockedIp[]
	getBlockedIps();
	
	void
	clearBlockedIPs();
	
	boolean
	ban(
            String ipAddress,
            String torrent_name,
            boolean manual);
	
	boolean
	ban(
            String ipAddress,
            String torrent_name,
            boolean manual,
            int ban_for_mins);
	
	void
	unban(String ipAddress);
	
	void
	unban(String ipAddress, boolean block);

	int
	getNbBannedIps();
	
	BannedIp[]
	getBannedIps();
	
	void
	clearBannedIps();
	
	void
	addExcludedHash(
            byte[] hash);
	
	void
	removeExcludedHash(
            byte[] hash);
	
	boolean
	isEnabled();

	void
	setEnabled(
            boolean enabled);
	
	boolean
	getInRangeAddressesAreAllowed();
	
	void
	setInRangeAddressesAreAllowed(
            boolean b);

	void
	markAsUpToDate();
	
	long
	getLastUpdateTime();
	
	long
	getTotalAddressesInRange();
	
	void
	addListener(
            IPFilterListener l);
	
	void
	removeListener(
            IPFilterListener l);
	
	void
	addExternalHandler(
            IpFilterExternalHandler handler);
	
	void
	removeExternalHandler(
            IpFilterExternalHandler handler);

	void reloadSync()
			throws Exception;
}
