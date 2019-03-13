/*
 * Created on Sep 4, 2013
 * Created by Paul Gardner
 * 
 * Copyright 2013 Azureus Software, Inc.  All rights reserved.
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


package com.aelitis.azureus.core.tag;

public interface 
TagFeatureProperties 
{
	String	PR_TRACKERS 			= "trackers";			// string list
	String	PR_UNTAGGED 			= "untagged";			// boolean
	String	PR_TRACKER_TEMPLATES 	= "tracker_templates";	// string list
	String	PR_CONSTRAINT		 	= "constraint";			// string list
	
	int		PT_STRING_LIST	= 1;
	int		PT_BOOLEAN		= 2;
	int		PT_LONG			= 3;
	
	TagProperty[]
	getSupportedProperties();
	
	TagProperty
	getProperty(
            String name);
	
	interface
	TagProperty
	{
		Tag
		getTag();
		
		int
		getType();
		
		String
		getName(
                boolean localize);
		
		void
		setStringList(
                String[] value);
			
		String[]
		getStringList();
		
		void
		setBoolean(
                Boolean value);
			
		Boolean
		getBoolean();
		
		void
		setLong(
                Long value);
			
		Long
		getLong();
		
		String
		getString();
		
		void
		addListener(
                TagPropertyListener listener);
		
		void
		removeListener(
                TagPropertyListener listener);
		
		void
		syncListeners();
	}
	
	interface
	TagPropertyListener
	{
		void
		propertyChanged(
                TagProperty property);
		
		void
		propertySync(
                TagProperty property);
	}
}
