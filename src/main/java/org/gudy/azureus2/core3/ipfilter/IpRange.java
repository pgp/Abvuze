/*
 * File    : IpRange.java
 * Created : 8 oct. 2003 13:02:23
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
public interface 
IpRange
{
	String
	getDescription();
	
	void
	setDescription(
            String str);
		
	boolean
	isValid();
  
  boolean
  isSessionOnly();
	
	String
	getStartIp();
	
	void
	setStartIp(
            String str);
		
	String
	getEndIp();
	
	void
	setEndIp(
            String str);
  
  void
  setSessionOnly(
          boolean sessionOnly);
		
	boolean isInRange(String ipAddress);
	
	void checkValid();
	
	int
	compareStartIpTo(
            IpRange other);
	
	int
	compareEndIpTo(
            IpRange other);
	
	int
	compareDescription(
            IpRange other);

	/**
	 * @return
	 */
	long getEndIpLong();

	/**
	 * @return
	 */
	long getStartIpLong();

	/**
	 * @return
	 */
    long getMergedEndLong();

	/**
	 * @return
	 */
    IpRange[] getMergedEntries();

	/**
	 * 
	 */
    void resetMergeInfo();

	/**
	 * @return
	 */
    boolean getMerged();

	/**
	 * 
	 */
    void setMerged();

	/**
	 * @param endIpLong
	 */
    void setMergedEnd(long endIpLong);

	/**
	 * @param e2
	 */
    void addMergedEntry(IpRange e2);

	/**
	 * @return
	 *
	 * @since 3.0.1.5
	 */
    boolean getAddedToRangeList();
}
