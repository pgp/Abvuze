/*
 * Created on Feb 13, 2009
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
import java.net.URL;

import org.gudy.azureus2.plugins.disk.DiskManagerFileInfo;

public interface 
TranscodeFile 
{
		// don't change these, they are serialised
	
	String PT_COMPLETE		= "comp";
	String PT_COPIED		= "copied";
	String PT_COPY_FAILED	= "copy_fail";
	String PT_CATEGORY		= "cat";
	String PT_TAGS			= "tags";
	
	String
	getName();
	
	DiskManagerFileInfo
	getSourceFile()
	
		throws TranscodeException;
	
	DiskManagerFileInfo
	getTargetFile() 
	
		throws TranscodeException;
	
	String
	getProfileName();
	
	long
	getCreationDateMillis();
	
	boolean
	isComplete();
	
	boolean
	getTranscodeRequired();
	
	boolean
	isCopiedToDevice();
	
	long
	getCopyToDeviceFails();
	
	void
	retryCopyToDevice();
	
	boolean
	isTemplate();
	
	long
	getDurationMillis();
	
	long
	getVideoWidth();
	
	long
	getVideoHeight();
	
	long
	getEstimatedTranscodeSize();
	
	String[]
	getCategories();
	
	void
	setCategories(
            String[] cats);
	
	String[]
	getTags(
            boolean localize);
	
	void
	setTags(
            String[] tags);
	
	Device
	getDevice();
	
	File
	getCacheFileIfExists();
	
		/**
		 * Will return null unless there is a job in existance for this file
		 * @return
		 */

        TranscodeJob
	getJob();
	
	URL
	getStreamURL();
	
	URL
	getStreamURL(
            String host);
	
	void
	delete(
            boolean delete_cache_file)
	
		throws TranscodeException;
	
	void
	setTransientProperty(
            Object key,
            Object value);
			
	Object
	getTransientProperty(
            Object key);

	boolean
	isDeleted();

	boolean
	isCopyingToDevice();
}
