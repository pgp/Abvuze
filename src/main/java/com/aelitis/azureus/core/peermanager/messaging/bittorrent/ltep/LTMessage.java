/*
 * Created on 17 Sep 2007
 * Created by Allan Crooks
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
 */
package com.aelitis.azureus.core.peermanager.messaging.bittorrent.ltep;

import com.aelitis.azureus.core.peermanager.messaging.Message;

/**
 * @author Allan Crooks
 *
 */
public interface LTMessage extends Message {
	
	String LT_FEATURE_ID = "LT1";

	String ID_LT_HANDSHAKE       = "lt_handshake";
	byte[] ID_LT_HANDSHAKE_BYTES = ID_LT_HANDSHAKE.getBytes();
	int SUBID_LT_HANDSHAKE       = 0;
	
	String ID_UT_PEX             = "ut_pex";
	byte[] ID_UT_PEX_BYTES       = ID_UT_PEX.getBytes();
	int SUBID_UT_PEX             = 1;
	
	// Placeholder message indicating that a message was sent for an extension which has
	// been disabled.
    String ID_DISABLED_EXT       = "disabled_extension";
	byte[] ID_DISABLED_EXT_BYTES = ID_DISABLED_EXT.getBytes();
	int SUBID_DISABLED_EXT       = 2;
	
	String ID_UT_METADATA        = "ut_metadata";
	byte[] ID_UT_METADATA_BYTES  = ID_UT_METADATA.getBytes();
	int SUBID_UT_METADATA        = 3;
	
	String ID_UT_UPLOAD_ONLY        = "upload_only";
	byte[] ID_UT_UPLOAD_ONLY_BYTES  = ID_UT_UPLOAD_ONLY.getBytes();
	int SUBID_UT_UPLOAD_ONLY        = 4;

	// 10 -> 20 reserved for non-public nets
}
