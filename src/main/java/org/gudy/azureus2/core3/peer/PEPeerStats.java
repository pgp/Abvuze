/*
 * File    : PEPeerStats
 * Created : 15-Oct-2003
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
 
 package org.gudy.azureus2.core3.peer;

/**
 * Provides peer statistics.
 * It uses Average to compute its different averages.
 */

public interface 
PEPeerStats 
{   
  PEPeer getPeer();
  void setPeer(PEPeer p);
  
  ////// SET METHODS ///////
  /**
   * The given number of data (payload) bytes have been sent to the peer.
   * @param num_bytes
   */
  void dataBytesSent(int num_bytes);
  
  /**
   * The given number of protocol (overhead) bytes have been sent to the peer.
   * @param num_bytes
   */
  void protocolBytesSent(int num_bytes);
  
  /**
   * The given number of data (payload) bytes have been received from the peer.
   * @param num_bytes
   */
  void dataBytesReceived(int num_bytes);
  
  /**
   * The given number of protocol (overhead) bytes have been received from the peer.
   * @param num_bytes
   */
  void protocolBytesReceived(int num_bytes);
  
  
  /**
   * The given number of bytes received from the peer were discarded.
   * @param num_bytes
   */
  void bytesDiscarded(int num_bytes);
  
  /**
   * The peer has completed a piece of the given byte size.
   * @param piece_size
   */
  void hasNewPiece(int piece_size);
  
  
  /**
   * The peer has statistically sent a piecce of the given byte size.
   * @param piece_size
   */
  void statisticalSentPiece(int piece_size);
  
  
  
  //////GET METHODS ///////
  /**
   * Get the the average bytes-per-second speed that we are receiving piece data from the peer.
   * @return average speed.
   */
  long getDataReceiveRate();
  
  /**
   * Get the the average bytes-per-second speed that we are receiving protocol messages from the peer.
   * @return average speed.
   */
  long getProtocolReceiveRate();

  
  /**
   * Get the total number of data (payload) bytes received from the peer.
   * @return total
   */
  long getTotalDataBytesReceived();
  
  /**
   * Get the total number of protocol (overhead) bytes received from the peer.
   * @return total
   */
  long getTotalProtocolBytesReceived();
  
  
  /**
   * Get the the average bytes-per-second speed that we are sending piece data to the peer.
   * @return average speed.
   */
  long getDataSendRate();
  
  /**
   * Get the the average bytes-per-second speed that we are sending protocol messages to the peer.
   * @return average speed.
   */
  long getProtocolSendRate();
  

  /**
   * Get the total number of data (payload) bytes sent to the peer.
   * @return total
   */
  long getTotalDataBytesSent();
  
  /**
   * Get the total number of protocol (overhead) bytes sent to the peer.
   * @return total
   */
  long getTotalProtocolBytesSent();
  
  
  
  /**
   * Get the the longer-average bytes-per-second speed at which the peer is uploading data to us.
   * @return average speed
   */
  long getSmoothDataReceiveRate();
  
  /**
   * Get the total number of discarded bytes received from the peer.
   * @return total discarded
   */
  long getTotalBytesDiscarded();
  
  /**
   * Get the estimated total download rate of the peer.
   * @return estimated rate in bytes-per-second
   */
  long getEstimatedDownloadRateOfPeer();
  
  /**
   * Get the estimated total upload rate of the peer.
   * @return estimated rate in bytes-per-second
   */
  long getEstimatedUploadRateOfPeer();
  
  long getEstimatedSecondsToCompletion();
  
  /**
   * Get the number of bytes downloaded in total by this peer
   * (includes data downloaded from all other peers).
   * @return total download bytes done
   */
  long getTotalBytesDownloadedByPeer();
  
  /**
   * Disk access stats methods
   * @param bytes
   */
  void diskReadComplete(long bytes);
  int getTotalDiskReadCount();
  int getAggregatedDiskReadCount();
  long getTotalDiskReadBytes();
  
  void setUploadRateLimitBytesPerSecond(int bytes);
  void setDownloadRateLimitBytesPerSecond(int bytes);
  int getUploadRateLimitBytesPerSecond();
  int getDownloadRateLimitBytesPerSecond();
  
  	// external rate control
  
  int getPermittedBytesToSend();
  void permittedSendBytesUsed(int num);
  
  int getPermittedBytesToReceive();
  void permittedReceiveBytesUsed(int num);
  
}