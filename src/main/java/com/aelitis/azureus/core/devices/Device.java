/*
 * Created on Jan 27, 2009
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


package com.aelitis.azureus.core.devices;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;

import com.aelitis.azureus.core.vuzefile.VuzeFile;

public interface 
Device 
{
	int DT_UNKNOWN				= 0;
	int DT_INTERNET_GATEWAY		= 1;
	int DT_CONTENT_DIRECTORY	= 2;
	int DT_MEDIA_RENDERER		= 3;
	int DT_INTERNET				= 4;
	int DT_OFFLINE_DOWNLOADER	= 5;
		
	int
	getType();
	
	String
	getID();
	
	String
	getName();
	
	void
	setName(
            String name,
            boolean isAutomaticName);
	
	String
	getClassification();
		
	String
	getShortDescription();
	
	void
	alive();
	
	boolean
	isAlive();
	
	boolean
	isLivenessDetectable();
	
	boolean
	isBusy();
	
	boolean
	isManual();
	
	void
	setHidden(
            boolean is_hidden);
	
	boolean
	isHidden();
	
	void
	setTagged(
            boolean is_tagged);
	
	boolean
	isTagged();
	
	boolean
	isBrowsable();
	
	browseLocation[]
	getBrowseLocations();
		
	InetAddress
	getAddress();
	
	void
	setAddress(
            InetAddress address);
	
	void
	setTransientProperty(
            Object key,
            Object value);
	
	Object
	getTransientProperty(
            Object key);
	
		/**
		 * Array of resource strings and their associated values
		 * @return
		 */

        String[][]
	getDisplayProperties();
	
	void
	requestAttention();
	
	void
	remove();
	
	void
	setCanRemove(
            boolean can);
	
	boolean
	canRemove();
	
	String
	getInfo();
	
	String
	getError();
	
	String
	getStatus();
	
	void
	addListener(
            DeviceListener listener);
	
	void
	removeListener(
            DeviceListener listener);
	
	String
	getString();
	
	interface
	browseLocation
	{
		String
		getName();
		
		URL
		getURL();
	}

	boolean
	isGenericUSB();
	
	void
	setGenericUSB(boolean b);
	
	String
	getImageID();
	
	void
	setImageID(String id);

	boolean
	isNameAutomatic();
	
	void
	setExportable(
            boolean b);
	
	boolean
	isExportable();
	
	URL
	getWikiURL();
	
	VuzeFile
	getVuzeFile()
	
		throws IOException;

	TranscodeProfile[]
	getDirectTranscodeProfiles();
}
