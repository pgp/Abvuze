/*
 * File    : PRUDPPacketReceiver.java
 * Created : 20-Jan-2004
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

package com.aelitis.net.udp.uc;

/**
 * @author parg
 *
 */

import java.net.*;

public interface 
PRUDPPacketHandler 
{
	int	PRIORITY_LOW		= 2;
	int	PRIORITY_MEDIUM		= 1;
	int	PRIORITY_HIGH		= 0;
	
	int	PRIORITY_IMMEDIATE	= 99;

		/**
		 * Asynchronous send and receive
		 * @param request_packet
		 * @param destination_address
		 * @param receiver
		 * @throws PRUDPPacketHandlerException
		 */

        void
	sendAndReceive(
                PRUDPPacket request_packet,
                InetSocketAddress destination_address,
                PRUDPPacketReceiver receiver,
                long timeout,
                int priority)
	
		throws PRUDPPacketHandlerException;
	
		/**
		 * Synchronous send and receive
		 * @param auth
		 * @param request_packet
		 * @param destination_address
		 * @return
		 * @throws PRUDPPacketHandlerException
		 */

        PRUDPPacket
	sendAndReceive(
                PasswordAuthentication auth,
                PRUDPPacket request_packet,
                InetSocketAddress destination_address)
	
		throws PRUDPPacketHandlerException;
	
	PRUDPPacket
	sendAndReceive(
            PasswordAuthentication auth,
            PRUDPPacket request_packet,
            InetSocketAddress destination_address,
            long timeout_millis)
	
		throws PRUDPPacketHandlerException;
	
	PRUDPPacket
	sendAndReceive(
            PasswordAuthentication auth,
            PRUDPPacket request_packet,
            InetSocketAddress destination_address,
            long timeout_millis,
            int priority)
	
		throws PRUDPPacketHandlerException;
	
		/**
		 * Send only
		 * @param request_packet
		 * @param destination_address
		 * @throws PRUDPPacketHandlerException
		 */

        void
	send(
                PRUDPPacket request_packet,
                InetSocketAddress destination_address)
	
		throws PRUDPPacketHandlerException;
	
	PRUDPRequestHandler
	getRequestHandler();
	
	void
	setRequestHandler(
            PRUDPRequestHandler request_handler);
	
	void
	primordialSend(
            byte[] data,
            InetSocketAddress target)
	
		throws PRUDPPacketHandlerException;
	
	boolean
	hasPrimordialHandler();
	
	void
	addPrimordialHandler(
            PRUDPPrimordialHandler handler);
	
	void
	removePrimordialHandler(
            PRUDPPrimordialHandler handler);
	
	int
	getPort();
	
	InetAddress
	getBindIP();
	
	void
	setDelays(
            int send_delay,
            int receive_delay,
            int queued_request_timeout);
	
	void
	setExplicitBindAddress(
            InetAddress address);
	
	PRUDPPacketHandlerStats
	getStats();
	
	PRUDPPacketHandler
	openSession(
            InetSocketAddress target)
	
		throws PRUDPPacketHandlerException;
	
	void
	closeSession()
	
		throws PRUDPPacketHandlerException;
	
	void
	destroy();
}
