/*
 * File    : GeneralStats.java
 * Created : 15 dec. 2003}
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
package org.gudy.azureus2.core3.stats.transfer;

/**
 * @author Olivier
 *
 */
public interface GeneralStats {

  /**
   * @return the number of downloaded bytes
   */
  long getDownloadedBytes();
  
  /**
   * @return the number of uploaded bytes
   */
  long getUploadedBytes();
  
  /**
   * @return the total lifetime 'up time' in seconds
   */
  long getTotalUpTime();
  
  /**
   * Method variants that can return values relative to the last mark
   * @param since_mark
   * @return
   */
  long getDownloadedBytes(boolean since_mark);
  long getUploadedBytes(boolean since_mark);
  long getTotalUpTime(boolean since_mark);
  int getAverageDownloadSpeed(boolean since_mark);
  int getAverageUploadSpeed(boolean since_mark);
  
  long getMarkTime();
  /**
   * Sets a mark against the overall up/down/uptime so that the methods that report relative to
   * the mark return values relative to it
   */

  void
  setMark();
  
  void
  clearMark();
  
  /**
   * @return this session uptime in seconds
   */
  long getSessionUpTime();
  
  /**
   * @return the average download speed in bytes per second
   */
  int getAverageDownloadSpeed();
  
  /**
   * @return the average upload speed in bytes per second
   */
  int getAverageUploadSpeed();
}
