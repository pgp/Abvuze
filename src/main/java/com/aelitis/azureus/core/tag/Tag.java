/*
 * Created on Mar 20, 2013
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

import java.util.Set;


public interface 
Tag 
	extends org.gudy.azureus2.plugins.tag.Tag
{
	String	TP_SETTINGS_REQUESTED	= "Settings Requested";	// Boolean
	
		/**
		 * Unique type denoting this species of tag
		 * @return
		 */

        TagType
	getTagType();

		/**
		 * Unique ID within this tag type 
		 * @return
		 */

        int
	getTagID();
	
		/**
		 * Unique across tag types and can be used to lookup by TagManager::lookuptagByUID
		 * @return
		 */

        long
	getTagUID();
	
	String
	getTagName(
            boolean localize);
		
	void
	setTagName(
            String name)
	
		throws TagException;
	
	int
	getTaggableTypes();
	
	void
	setCanBePublic(
            boolean can_be_public);
	
	boolean
	canBePublic();
	
	boolean
	isPublic();
	
	void
	setPublic(
            boolean pub);
	
	/**
	 * @return [auto_add,auto_remove]
	 */

    boolean[]
	isTagAuto();
	
	boolean
	isVisible();
	
	void
	setVisible(
            boolean visible);
	
	String
	getGroup();
	
	void
	setGroup(
            String group);
	
	String
	getImageID();
	
	void
	setImageID(
            String id);
	
	int[]
	getColor();
	
	void
	setColor(
            int[] rgb);
	
	void
	addTaggable(
            Taggable t);
	
	void
	removeTaggable(
            Taggable t);
	
	int
	getTaggedCount();
	
	Set<Taggable>
	getTagged();
	
	boolean
	hasTaggable(
            Taggable t);
	
	void
	removeTag();
	
	String
	getDescription();
	
	void
	setDescription(
            String desc);
	
	void
	setTransientProperty(
            String property,
            Object value);
	
	Object
	getTransientProperty(
            String property);
	
	long
	getTaggableAddedTime(
            Taggable taggble);
	
	void
	requestAttention();
	
	void
	addTagListener(
            TagListener listener,
            boolean fire_for_existing);
	
	void
	removeTagListener(
            TagListener listener);
}
