/*
 * File    : GlobalManagerStats.java
 * Created : 23-Oct-2003
 * By      : stuff
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

package org.gudy.azureus2.core3.global;

/**
 * @author parg
 *
 */
public interface 
GlobalManagerStats 
{
	/**
	 * Data Receive Rate over the last 10s
	 */
    int getDataReceiveRate();
	int getDataReceiveRateNoLAN();
	int getDataReceiveRateNoLAN(int average_period);

	int getProtocolReceiveRate();
	int getProtocolReceiveRateNoLAN();
	int getProtocolReceiveRateNoLAN(int average_period);
	
	int getDataAndProtocolReceiveRate();
		
	/**
	 * Data Send Rate over the last 10s
	 */
    int getDataSendRate();
	int getDataSendRateNoLAN();
	int getDataSendRateNoLAN(int average_period);
	  
	int getProtocolSendRate();
	int getProtocolSendRateNoLAN();
	int getProtocolSendRateNoLAN(int average_period);
	  
	int getDataAndProtocolSendRate();
	
	/**
	 * Smoothed Send Rate, including data and protocol, based on 
	 * "Stats Smoothing Secs" (default to 60s, min 30s)
	 */
    long	getSmoothedSendRate();
	/**
	 * Smoothed Receive Rate, including data and protocol, based on 
	 * "Stats Smoothing Secs" (default to 60s, min 30s)
	 */
    long	getSmoothedReceiveRate();
	
	int getDataSendRateAtClose();
  
	long getTotalDataBytesReceived();
  
	long getTotalProtocolBytesReceived();
	
	long getTotalDataBytesSent();
  
	long getTotalProtocolBytesSent();
  
  	long getTotalSwarmsPeerRate(boolean downloading, boolean seeding);
  
		// set methods
		
	void	dataBytesSent(int bytes, boolean LAN);
	void protocolBytesSent(int bytes, boolean LAN);
		
	void dataBytesReceived(int bytes, boolean LAN);
	void protocolBytesReceived(int bytes, boolean LAN);
		
	void
	discarded(
            int bytes);
}
