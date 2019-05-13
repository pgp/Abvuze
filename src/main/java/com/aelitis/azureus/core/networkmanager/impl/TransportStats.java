/*
 * Created on May 19, 2005
 * Created by Alon Rohter
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

package com.aelitis.azureus.core.networkmanager.impl;

import java.util.*;

import org.gudy.azureus2.core3.util.Timer;
import org.gudy.azureus2.core3.util.TimerEvent;
import org.gudy.azureus2.core3.util.TimerEventPerformer;

public class TransportStats {

  private static final int PRINT_INTERVAL = 60*1000;
  private static final int GRANULARITY = 10;  //bytes
  
  private final TreeMap read_sizes = new TreeMap();
  private final TreeMap write_sizes = new TreeMap();
  
  private long total_reads = 0;
  private long total_writes = 0;
  
  
  public TransportStats() {
    Timer printer = new Timer("TransportStats:Printer");
    printer.addPeriodicEvent(
        PRINT_INTERVAL,
        new TimerEventPerformer() {
          public void perform( TimerEvent ev ) {
            printStats();
          }
        }
    );
  }
  
  
  public void bytesRead( int num_bytes_read ) {
    total_reads++;
    updateSizes( read_sizes, num_bytes_read );
  }
  
  
  public void bytesWritten( int num_bytes_written ) {
    total_writes++;
    updateSizes( write_sizes, num_bytes_written );
  }
  
  
  private void updateSizes( TreeMap io_sizes, int num_bytes ) {
    Integer size_key;
    
    if( num_bytes == 0 ) {
      size_key = 0;
    }
    else {
      size_key = (num_bytes / GRANULARITY) + 1;
    }
      
    Long count = (Long)io_sizes.get( size_key );
    
    if( count == null ) {
      io_sizes.put( size_key, 1L);
    }
    else {
      io_sizes.put( size_key, count + 1);
    }
  }
  
  
  private void printStats() {
    System.out.println( "\n------------------------------" );
    System.out.println( "***** TCP SOCKET READ SIZE STATS *****" );
    printSizes( read_sizes, total_reads );
    
    System.out.println( "\n***** TCP SOCKET WRITE SIZE STATS *****" );
    printSizes( write_sizes, total_writes );
    System.out.println( "------------------------------" );
  }
  
 
  
  private void printSizes( TreeMap size_map, long num_total ) {
    int prev_high = 1;

      for (Object o : size_map.entrySet()) {
          Map.Entry entry = (Map.Entry) o;
          int key = (Integer) entry.getKey();
          long count = (Long) entry.getValue();

          long percentage = (count * 100) / num_total;

          if (key == 0) {
              if (percentage > 3) {
                  System.out.println("[0 bytes]= x" + percentage + "%");
              }
          } else {
              int high = key * GRANULARITY;

              if (percentage > 3) {
                  System.out.println("[" + prev_high + "-" + (high - 1) + " bytes]= x" + percentage + "%");
              }

              prev_high = high;
          }
      }
  }

  
}
