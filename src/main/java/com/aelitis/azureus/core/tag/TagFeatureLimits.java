/*
 * Created on Nov 6, 2015
 * Created by Paul Gardner
 * 
 * Copyright 2015 Azureus Software, Inc.  All rights reserved.
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


package com.aelitis.azureus.core.tag;

public interface 
TagFeatureLimits 
{
	int RS_NONE						= 0;
	int RS_ARCHIVE					= 1;
	int RS_REMOVE_FROM_LIBRARY		= 2;
	int RS_DELETE_FROM_COMPUTER		= 3;
	int RS_MOVE_TO_OLD_TAG			= 4;
	
	int RS_DEFAULT		= RS_NONE;
	
	int OP_ADDED_TO_VUZE			= 0;
	int OP_ADED_TO_TAG				= 1;
	
	int OP_DEFAULT					= OP_ADDED_TO_VUZE;

	int
	getMaximumTaggables();
	
	void
	setMaximumTaggables(
            int max);
	
	int
	getRemovalStrategy();
	
	void
	setRemovalStrategy(
            int id);
	
	int
	getOrdering();
	
	void
	setOrdering(
            int id);
}
