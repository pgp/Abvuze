/*
 * Created on Feb 20, 2005
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

package com.aelitis.azureus.core.peermanager.messaging.azureus;

import com.aelitis.azureus.core.peermanager.messaging.Message;

/**
 * A core AZ type peer message.
 */
public interface AZMessage extends Message {
	String AZ_FEATURE_ID = "AZ1";

  String ID_AZ_HANDSHAKE        	= "AZ_HANDSHAKE";
  byte[] ID_AZ_HANDSHAKE_BYTES      = ID_AZ_HANDSHAKE.getBytes();
  int SUBID_AZ_HANDSHAKE			= 0;
  
  String ID_AZ_PEER_EXCHANGE    	= "AZ_PEER_EXCHANGE";
  byte[] ID_AZ_PEER_EXCHANGE_BYTES  = ID_AZ_PEER_EXCHANGE.getBytes();
  int SUBID_AZ_PEER_EXCHANGE		= 1;
  
  String ID_AZ_GENERIC_MAP    		= "AZ_GENERIC_MAP";
  byte[] ID_AZ_GENERIC_MAP_BYTES    = ID_AZ_GENERIC_MAP.getBytes();
  int SUBID_AZ_GENERIC_MAP			= 2;
  
  String ID_AZ_REQUEST_HINT    		= "AZ_REQUEST_HINT";
  byte[] ID_AZ_REQUEST_HINT_BYTES  = ID_AZ_REQUEST_HINT.getBytes();
  int SUBID_ID_AZ_REQUEST_HINT		= 3;

  String ID_AZ_HAVE	    			= "AZ_HAVE";
  byte[] ID_AZ_HAVE_BYTES 			= ID_AZ_HAVE.getBytes();
  int SUBID_ID_AZ_HAVE				= 4;
  
  String ID_AZ_BAD_PIECE	    	= "AZ_BAD_PIECE";
  byte[] ID_AZ_BAD_PIECE_BYTES 		= ID_AZ_BAD_PIECE.getBytes();
  int SUBID_ID_AZ_BAD_PIECE			= 5;
  
  String ID_AZ_STAT_REQUEST	    	= "AZ_STAT_REQ";
  byte[] ID_AZ_STAT_REQUEST_BYTES 	= ID_AZ_STAT_REQUEST.getBytes();
  int SUBID_ID_AZ_STAT_REQUEST		= 6;
  
  String ID_AZ_STAT_REPLY	    	= "AZ_STAT_REP";
  byte[] ID_AZ_STAT_REPLY_BYTES 	= ID_AZ_STAT_REPLY.getBytes();
  int SUBID_ID_AZ_STAT_REPLY		= 7;
  
  String ID_AZ_METADATA	    		= "AZ_METADATA";
  byte[] ID_AZ_METADATA_BYTES 		= ID_AZ_METADATA.getBytes();
  int SUBID_ID_AZ_METADATA			= 8;

  
  //TODO

  String ID_AZ_SESSION_SYN      	= "AZ_SESSION_SYN";
  byte[] ID_AZ_SESSION_SYN_BYTES    = ID_AZ_SESSION_SYN.getBytes();
  
  String ID_AZ_SESSION_ACK      	= "AZ_SESSION_ACK";
  byte[] ID_AZ_SESSION_ACK_BYTES    = ID_AZ_SESSION_ACK.getBytes();
  
  String ID_AZ_SESSION_END      	= "AZ_SESSION_END";
  byte[] ID_AZ_SESSION_END_BYTES    = ID_AZ_SESSION_END.getBytes();
   
  String ID_AZ_SESSION_BITFIELD 		= "AZ_SESSION_BITFIELD";
  byte[] ID_AZ_SESSION_BITFIELD_BYTES 	= ID_AZ_SESSION_BITFIELD.getBytes();
  
  String ID_AZ_SESSION_CANCEL   	= "AZ_SESSION_CANCEL";
  byte[] ID_AZ_SESSION_CANCEL_BYTES = ID_AZ_SESSION_CANCEL.getBytes();
  
  String ID_AZ_SESSION_HAVE     	= "AZ_SESSION_HAVE";
  byte[] ID_AZ_SESSION_HAVE_BYTES   = ID_AZ_SESSION_HAVE.getBytes();
  
  String ID_AZ_SESSION_PIECE    	= "AZ_SESSION_PIECE";
  byte[] ID_AZ_SESSION_PIECE_BYTES  = ID_AZ_SESSION_PIECE.getBytes();
  
  String ID_AZ_SESSION_REQUEST  		= "AZ_SESSION_REQUEST";
  byte[] ID_AZ_SESSION_REQUEST_BYTES  	= ID_AZ_SESSION_REQUEST.getBytes();
}
