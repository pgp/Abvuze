/*
 * File    : PEPeerManagerStats.java
 * Created : 05-Nov-2003
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

package org.gudy.azureus2.core3.peer;


public interface 
PEPeerManagerStats 
{
  
	void discarded(PEPeer peer, int length);
	void hashFailed(int length);
	  
	void dataBytesReceived(PEPeer peer, int length);
	void protocolBytesReceived(PEPeer peer, int length);
  
	void dataBytesSent(PEPeer peer, int length);
	void protocolBytesSent(PEPeer peer, int length);
  
  	void haveNewPiece(int pieceLength);
  
  	void haveNewConnection(boolean incoming);
  	
	long getDataReceiveRate();
	long getProtocolReceiveRate();
  
	long getDataSendRate();
	long getProtocolSendRate();
   
	long getPeakDataReceiveRate();
	long getPeakDataSendRate();
	
	long getSmoothedDataReceiveRate();
	long getSmoothedDataSendRate();
	
	long getTotalDataBytesSent();
	long getTotalProtocolBytesSent();
  
  	long getTotalDataBytesReceived();
  	long getTotalProtocolBytesReceived();
  
	long getTotalDataBytesSentNoLan();
	long getTotalProtocolBytesSentNoLan();
  
  	long getTotalDataBytesReceivedNoLan();
  	long getTotalProtocolBytesReceivedNoLan();

	long getTotalAverage();

	long getTotalHashFailBytes();
	long getTotalDiscarded();
	
	int getTimeSinceLastDataReceivedInSeconds();
	int getTimeSinceLastDataSentInSeconds();
	
	int getTotalIncomingConnections();
	int getTotalOutgoingConnections();
	
	int getPermittedBytesToReceive();
	void permittedReceiveBytesUsed(int bytes);
	
	int getPermittedBytesToSend();
	void	permittedSendBytesUsed(int bytes);
}
