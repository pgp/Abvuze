/*
 * Created on Jul 17, 2004
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

package com.aelitis.azureus.core.peermanager.messaging.bittorrent;

import com.aelitis.azureus.core.peermanager.messaging.Message;

/**
 * A bittorrent peer protocol message.
 */
public interface BTMessage extends Message {
  String BT_FEATURE_ID = "BT1";
  
  String ID_BT_CHOKE        	= "BT_CHOKE";
  byte[] ID_BT_CHOKE_BYTES      = ID_BT_CHOKE.getBytes();
  int SUBID_BT_CHOKE			= 0;
  
  String ID_BT_UNCHOKE      	= "BT_UNCHOKE";
  byte[] ID_BT_UNCHOKE_BYTES    = ID_BT_UNCHOKE.getBytes();
  int SUBID_BT_UNCHOKE			= 1;
  
  String ID_BT_INTERESTED   	= "BT_INTERESTED";
  byte[] ID_BT_INTERESTED_BYTES = ID_BT_INTERESTED.getBytes();
  int SUBID_BT_INTERESTED		= 2;
  
  String ID_BT_UNINTERESTED 		= "BT_UNINTERESTED";
  byte[] ID_BT_UNINTERESTED_BYTES 	= ID_BT_UNINTERESTED.getBytes();
  int SUBID_BT_UNINTERESTED			= 3;
  
  String ID_BT_HAVE         	= "BT_HAVE";
  byte[] ID_BT_HAVE_BYTES       = ID_BT_HAVE.getBytes();
  int SUBID_BT_HAVE				= 4;
  
  String ID_BT_BITFIELD     	= "BT_BITFIELD";
  byte[] ID_BT_BITFIELD_BYTES   = ID_BT_BITFIELD.getBytes();
  int SUBID_BT_BITFIELD			= 5;
  
  String ID_BT_REQUEST      	= "BT_REQUEST";
  byte[] ID_BT_REQUEST_BYTES    = ID_BT_REQUEST.getBytes();
  int SUBID_BT_REQUEST			= 6;
  
  String ID_BT_PIECE        	= "BT_PIECE";
  byte[] ID_BT_PIECE_BYTES      = ID_BT_PIECE.getBytes();
  int SUBID_BT_PIECE			= 7;
  
  String ID_BT_CANCEL       	= "BT_CANCEL";
  byte[] ID_BT_CANCEL_BYTES     = ID_BT_CANCEL.getBytes();
  int SUBID_BT_CANCEL			= 8;
    
  String ID_BT_DHT_PORT         = "BT_DHT_PORT";
  byte[] ID_BT_DHT_PORT_BYTES   = ID_BT_DHT_PORT.getBytes();
  int SUBID_BT_DHT_PORT         = 9;
  
  String ID_BT_HANDSHAKE    	= "BT_HANDSHAKE";
  byte[] ID_BT_HANDSHAKE_BYTES  = ID_BT_HANDSHAKE.getBytes();
  int SUBID_BT_HANDSHAKE		= 10;

  String ID_BT_KEEP_ALIVE   	= "BT_KEEP_ALIVE";
  byte[] ID_BT_KEEP_ALIVE_BYTES = ID_BT_KEEP_ALIVE.getBytes();
  int SUBID_BT_KEEP_ALIVE		= 11;

  String ID_BT_SUGGEST_PIECE   	= "BT_SUGGEST_PIECE";
  byte[] ID_BT_SUGGEST_PIECE_BYTES = ID_BT_SUGGEST_PIECE.getBytes();
  int SUBID_BT_SUGGEST_PIECE	= 13;
  
  String ID_BT_HAVE_ALL   		= "BT_HAVE_ALL";
  byte[] ID_BT_HAVE_ALL_BYTES 	= ID_BT_HAVE_ALL.getBytes();
  int SUBID_BT_HAVE_ALL			= 14;

  String ID_BT_HAVE_NONE	   	= "BT_HAVE_NONE";
  byte[] ID_BT_HAVE_NONE_BYTES = ID_BT_HAVE_NONE.getBytes();
  int SUBID_BT_HAVE_NONE		= 15;

  String ID_BT_REJECT_REQUEST  	= "BT_REJECT_REQUEST";
  byte[] ID_BT_REJECT_REQUEST_BYTES = ID_BT_REJECT_REQUEST.getBytes();
  int SUBID_BT_REJECT_REQUEST	= 16;

  String ID_BT_ALLOWED_FAST   	= "BT_ALLOWED_FAST";
  byte[] ID_BT_ALLOWED_FAST_BYTES = ID_BT_ALLOWED_FAST.getBytes();
  int SUBID_BT_ALLOWED_FAST		= 17;
  
  String ID_BT_LT_EXT_MESSAGE        = "BT_LT_EXT_MESSAGE";
  byte[] ID_BT_LT_EXT_MESSAGE_BYTES  = ID_BT_LT_EXT_MESSAGE.getBytes();
  int SUBID_BT_LT_EXT_MESSAGE	= 20;
}
