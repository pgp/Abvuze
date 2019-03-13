/*
 * Created on 31-Jul-2004
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

package org.gudy.azureus2.core3.disk.impl.access;

import org.gudy.azureus2.core3.disk.*;
import org.gudy.azureus2.core3.disk.impl.DiskManagerFileInfoImpl;
import org.gudy.azureus2.core3.util.DirectByteBuffer;

/**
 * @author parg
 *
 */
public interface 
DMWriter 
{
	void
	start();
	
	void
	stop();
	
	boolean
	zeroFile(
            DiskManagerFileInfoImpl file,
            long length) throws DiskManagerException;

	DiskManagerWriteRequest
	createWriteRequest(
            int pieceNumber,
            int offset,
            DirectByteBuffer data,
            Object user_data);
	
	void
	writeBlock(
            DiskManagerWriteRequest request,
            DiskManagerWriteRequestListener listener);
	
	boolean
	hasOutstandingWriteRequestForPiece(
            int piece_number);
}
