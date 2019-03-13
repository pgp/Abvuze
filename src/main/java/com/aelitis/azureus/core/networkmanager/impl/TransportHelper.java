/*
 * Created on 21 Jun 2006
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

package com.aelitis.azureus.core.networkmanager.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;


public interface 
TransportHelper 
{
	InetSocketAddress
	getAddress();

	String
	getName(boolean verbose);
	
	boolean
	minimiseOverheads();
	
	int
	getConnectTimeout();
	
	int
	getReadTimeout();
	
	boolean
	delayWrite(
            ByteBuffer buffer);
	
	boolean
	hasDelayedWrite();
	
	int
	write(
            ByteBuffer buffer,
            boolean partial_write)
	
		throws IOException;  	

    long
    write(
            ByteBuffer[] buffers,
            int array_offset,
            int length)
    
    	throws IOException;

    int
    read(
            ByteBuffer buffer)
    
    	throws IOException;  	

    long
    read(
            ByteBuffer[] buffers,
            int array_offset,
            int length)
    
    	throws IOException;  	

    void
    pauseReadSelects();
    
    void
    pauseWriteSelects();
 
    void
    resumeReadSelects();
    
    void
    resumeWriteSelects();
    
    void
    registerForReadSelects(
            selectListener listener,
            Object attachment);
    
    void
    registerForWriteSelects(
            selectListener listener,
            Object attachment);
    
    void
    cancelReadSelects();
    
    void
    cancelWriteSelects();
    
    boolean
    isClosed();
    
    void
    close(
            String reason);
    
    void
    failed(
            Throwable reason);
    
    interface
    selectListener
    {
    	boolean
    	selectSuccess(
                TransportHelper helper,
                Object attachment);

        void
        selectFailure(
                TransportHelper helper,
                Object attachment,
                Throwable msg);
    }
    
    void
    setUserData(
            Object key,
            Object data);
    
    Object
    getUserData(
            Object key);
    
	void
	setTrace(
            boolean on);
	
	void
	setScatteringMode(long forBytes);
}
