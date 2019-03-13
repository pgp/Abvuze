/*
 * Created on May 19, 2008
 * Created by Paul Gardner
 * 
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


package com.aelitis.azureus.core.vuzefile;

import java.util.*;

public interface 
VuzeFileComponent 
{
	int COMP_TYPE_NONE						= 0x00000000;
	int COMP_TYPE_METASEARCH_TEMPLATE		= 0x00000001;
	int COMP_TYPE_V3_NAVIGATION				= 0x00000002;
	int COMP_TYPE_V3_CONDITION_CHECK		= 0x00000004;
	int COMP_TYPE_PLUGIN					= 0x00000008;
	int COMP_TYPE_SUBSCRIPTION				= 0x00000010;
	int COMP_TYPE_SUBSCRIPTION_SINGLETON	= 0x00000020;
	int COMP_TYPE_CUSTOMIZATION				= 0x00000040;
	int COMP_TYPE_CONTENT_NETWORK			= 0x00000080;
	int COMP_TYPE_METASEARCH_OPERATION		= 0x00000100;
	int COMP_TYPE_DEVICE					= 0x00000200;
	int COMP_TYPE_CONFIG_SETTINGS			= 0x00000400;
	int COMP_TYPE_ADD_TORRENT				= 0x00000800;

	int
	getType();
	
	String
	getTypeName();
	
	Map
	getContent();
	
	void
	setProcessed();
	
	boolean
	isProcessed();
	
	void
	setData(
            Object key,
            Object value);
	
	Object
	getData(
            Object key);
}
