/*
 * Created on 19 Jun 2006
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

package org.gudy.azureus2.plugins.messaging.generic;

import org.gudy.azureus2.plugins.messaging.MessageException;
import org.gudy.azureus2.plugins.network.RateLimiter;
import org.gudy.azureus2.plugins.utils.PooledByteBuffer;

public interface 
GenericMessageConnection 
{
	int TT_NONE			= 0;
	int TT_TCP			= 0;
	int TT_UDP			= 0;
	int TT_INDIRECT		= 0;
	
	GenericMessageEndpoint
	getEndpoint();
	
	void
	connect()
	
		throws MessageException;
	
	void
	send(
            PooledByteBuffer message)
	
		throws MessageException;
	
	void
	close()
	
		throws MessageException;
	
	int
	getMaximumMessageSize();
	
	String
	getType();
	
	int
	getTransportType();
	
	void
	addInboundRateLimiter(
            RateLimiter limiter);
	
	void
	removeInboundRateLimiter(
            RateLimiter limiter);

	void
	addOutboundRateLimiter(
            RateLimiter limiter);
	
	void
	removeOutboundRateLimiter(
            RateLimiter limiter);
	
	void
	addListener(
            GenericMessageConnectionListener listener);
	
	void
	removeListener(
            GenericMessageConnectionListener listener);
}
