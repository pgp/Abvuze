/*
 * Created on Oct 21, 2014
 * Created by Paul Gardner
 * 
 * Copyright 2014 Azureus Software, Inc.  All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */


package com.aelitis.azureus.plugins.net.buddy;

import java.util.Map;

import org.gudy.azureus2.core3.download.DownloadManager;

import com.aelitis.azureus.plugins.net.buddy.BuddyPluginBeta.ChatInstance;
import com.aelitis.azureus.plugins.net.buddy.BuddyPluginBeta.ChatMessage;

public interface 
BuddyPluginViewInterface 
{
	void
	openChat(
            ChatInstance chat);
	
	String	VP_SWT_COMPOSITE	= "swt_comp";
	String	VP_DOWNLOAD			= "download";		// DownloadAdapter
	String	VP_CHAT				= "chat";			// ChatInstance
	
	View
	buildView(
            Map<String, Object> properties,
            ViewListener listener);
	
	String
	renderMessage(
            ChatInstance chat,
            ChatMessage message);
	
	void
	selectClassicTab();
	
	interface
	DownloadAdapter
	{
		DownloadManager
		getCoreDownload();
		
		String[]
		getNetworks();
		
		String
		getChatKey();
	}
	
	interface
	View
	{
		void
		activate();
		
		void
		handleDrop(
                String drop);
		
		void
		destroy();
	}
	
	interface
	ViewListener
	{
		void
		chatActivated(
                ChatInstance chat);
	}
}
