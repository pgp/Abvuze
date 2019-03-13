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

import com.aelitis.net.upnp.UPnPDevice;

public interface 
DeviceManager 
{
		// not the best place for these, but it'll do for the moment
	
	String CONFIG_VIEW_HIDE_REND_GENERIC	= "device.sidebar.ui.rend.hidegeneric";
	String CONFIG_VIEW_SHOW_ONLY_TAGGED		= "device.sidebar.ui.rend.showonlytagged";
	
	DeviceTemplate[]
	getDeviceTemplates(
            int device_type);
	
	DeviceManufacturer[]
	getDeviceManufacturers(
            int device_type);
	
	Device[]
	getDevices();
	
	Device
	addVirtualDevice(
            int type,
            String uid,
            String classification,
            String name)
	
		throws DeviceManagerException;
	
	Device addInetDevice(
		int					type, 
		String					uid, 
		String					classification,
		String					name,
		InetAddress					address )

		throws DeviceManagerException;

	void
	search(
            int max_millis,
            DeviceSearchListener listener);
	
	boolean
	getAutoSearch();
	
	void
	setAutoSearch(
            boolean auto);
	
	int
	getAutoHideOldDevicesDays();
	
	void
	setAutoHideOldDevicesDays(
            int days);
	
	boolean
	isRSSPublishEnabled();
	
	void
	setRSSPublishEnabled(
            boolean enabled);

	String
	getRSSLink();
	
	UnassociatedDevice[]
	getUnassociatedDevices();
	
	TranscodeManager
	getTranscodeManager();
	
	File
	getDefaultWorkingDirectory();
	
	void
	setDefaultWorkingDirectory(
            File dir);
	
	boolean
	isBusy(
            int device_type);
	
	DeviceOfflineDownloaderManager
	getOfflineDownlaoderManager();
	
	boolean
	isTiVoEnabled();
	
	void
	setTiVoEnabled(
            boolean enabled);
	
	boolean
	getDisableSleep();
	
	void
	setDisableSleep(
            boolean b);
	
	String
	getLocalServiceName();
	
	void
	addDiscoveryListener(
            DeviceManagerDiscoveryListener listener);
	
	void
	removeDiscoveryListener(
            DeviceManagerDiscoveryListener listener);
	
	void
	addListener(
            DeviceManagerListener listener);
	
	void
	removeListener(
            DeviceManagerListener listener);
	
	interface
	UnassociatedDevice
	{
		InetAddress
		getAddress();
		
		String
		getDescription();
	}
	
	interface
	DeviceManufacturer
	{
		String
		getName();
		
		DeviceTemplate[]
		getDeviceTemplates();
	}

	/**
	 * @param upnpDevice
	 * @return
	 *
	 * @since 5.0.0.1
	 */
	Device
	findDevice(
			UPnPDevice upnpDevice);
}
