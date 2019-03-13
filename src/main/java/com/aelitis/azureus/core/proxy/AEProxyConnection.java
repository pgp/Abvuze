/*
 * Created on 08-Dec-2004
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

package com.aelitis.azureus.core.proxy;

import java.nio.channels.SocketChannel;

/**
 * @author parg
 *
 */

public interface 
AEProxyConnection 
{
	String
	getName();
	
		/**
		 * returns the non-blocking channel associated with the initiator of this proxy
		 * connection
		 * @return
		 */

        SocketChannel
	getSourceChannel();
	
		// state manipulation methods
	
	void
	setReadState(
            AEProxyState state);
	
	void
	setWriteState(
            AEProxyState state);
	
	void
	setConnectState(
            AEProxyState state);
	
		// selector manipulation
	
	void
	requestReadSelect(
            SocketChannel channel);
	
	void
	cancelReadSelect(
            SocketChannel channel);
	
	void
	requestWriteSelect(
            SocketChannel channel);
	
	void
	cancelWriteSelect(
            SocketChannel channel);
	
	void
	requestConnectSelect(
            SocketChannel channel);
	
	void
	cancelConnectSelect(
            SocketChannel channel);
	
		/**
		 * marks the transition between connecting and connected
		 *
		 */

        void
	setConnected();
	
		/**
		 * marks the last time that something happened on the connection for read timeout
		 * purposes
		 */

        void
	setTimeStamp();
	
		/**
		 * indicate that the connection has failed
		 * @param cause
		 */

        void
	failed(
                Throwable cause);
	
		/**
		 * close the connection
		 *
		 */

        void
	close();
	
	boolean
	isClosed();
	
	void
	addListener(
            AEProxyConnectionListener l);
	
	void
	removeListener(
            AEProxyConnectionListener l);
}
