/*
 * Created on Feb 11, 2009
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


package com.aelitis.azureus.core.content;

import org.gudy.azureus2.plugins.disk.DiskManagerFileInfo;

public interface 
AzureusContentFile 
{
	String	PT_TITLE			= "title";			// String
	String	PT_CREATOR			= "creator";		// String
	String	PT_DATE				= "date";			// Long, millis
	String	PT_DURATION			= "duration";		// Long, millis
	String	PT_VIDEO_WIDTH		= "video_width";	// Long
	String	PT_VIDEO_HEIGHT		= "video_height";	// Long
	String	PT_CATEGORIES		= "cats";			// String[]
	String	PT_TAGS				= "tags";			// String[]
	String	PT_PERCENT_DONE		= "percent";		// Long, thousandths
	String	PT_ETA				= "eta";			// Long, seconds
	
	DiskManagerFileInfo
	getFile();
	
	Object
	getProperty(
            String name);
}
