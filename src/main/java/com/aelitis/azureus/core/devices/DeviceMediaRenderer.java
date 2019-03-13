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

import java.io.File;
import java.net.InetAddress;

import com.aelitis.azureus.core.devices.DeviceManager.UnassociatedDevice;


public interface 
DeviceMediaRenderer
	extends Device, TranscodeTarget
{	
	int RS_PS3		= 1;
	int RS_XBOX		= 2;
	int RS_ITUNES	= 3;
	int RS_WII		= 4;
	int RS_BROWSER	= 5;
	int RS_OTHER	= 6;

		/**
		 * THIS WILL CHANGE!!!
		 * @return	RS_<x>
		 */

        int
	getRendererSpecies();

	boolean
	canFilterFilesView();
	
	void
	setFilterFilesView(
            boolean filter);
	
	boolean
	getFilterFilesView();
	
		// copy to device
	
	boolean
	canCopyToDevice();
	
	boolean
	getAutoCopyToDevice();
		
	void
	setAutoCopyToDevice(
            boolean auto);
	
	int
	getCopyToDevicePending();
	
	boolean
	canAutoStartDevice();
	
	boolean
	getAutoStartDevice();
	
	void
	setAutoStartDevice(
            boolean auto);
	
		// copy to folder
	
	boolean
	canCopyToFolder();
	
	void
	setCanCopyToFolder(
            boolean can);
	
	File
	getCopyToFolder();
	
	void
	setCopyToFolder(
            File file);
	
	int
	getCopyToFolderPending();
	
	boolean
	getAutoCopyToFolder();
		
	void
	setAutoCopyToFolder(
            boolean auto);
	
	void
	manualCopy()
	
		throws DeviceManagerException;
	
		// associate
	
	boolean
	canAssociate();
	
	void
	associate(
            UnassociatedDevice assoc);
	
	boolean
	canShowCategories();
	
	void
	setShowCategories(
            boolean b);
	
	boolean
	getShowCategories();
	
	boolean
	isRSSPublishEnabled();
	
	void
	setRSSPublishEnabled(
            boolean enabled);
	
	long
	getAutoShareToTagID();
	
	void
	setAutoShareToTagID(
            long id);
	
	InetAddress
	getAddress();
	
	boolean
	canRestrictAccess();
	
	String
	getAccessRestriction();
	
	void
	setAccessRestriction(
            String str);
}
