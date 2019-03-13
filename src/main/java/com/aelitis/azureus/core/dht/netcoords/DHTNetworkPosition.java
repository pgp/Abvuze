/*
 * Created on 24-Apr-2006
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

package com.aelitis.azureus.core.dht.netcoords;

import java.io.*;

public interface 
DHTNetworkPosition 
{
	byte	POSITION_TYPE_NONE				= 0;
	byte	POSITION_TYPE_VIVALDI_V1		= 1;
	//public static byte	POSITION_TYPE_VIVALDI_V2	= 3;	// was 2 but serialisation format changed to include header and 5-dimensions
	//public static byte	POSITION_TYPE_VIVALDI_V2	= 4;	// staleness added
    byte	POSITION_TYPE_VIVALDI_V2		= 5;	// another increment
	
	byte
	getPositionType();
	
		/**
		 * number of bytes on wire
		 * @return
		 */

        int
	getSerialisedSize();
	
		/**
		 * @param other
		 * @return Float.NaN if no value available
		 */

        float
	estimateRTT(
                DHTNetworkPosition other);
	
	void
	update(
            byte[] other_id,
            DHTNetworkPosition other,
            float rtt);
	
	boolean
	isValid();
	
	double[]
	getLocation();
	
	void
	serialise(
            DataOutputStream os)
	
		throws IOException;
}
