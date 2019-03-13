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

import java.util.List;


public interface 
TagType 
{
	int TT_DOWNLOAD_CATEGORY	= 1;
	int TT_DOWNLOAD_STATE		= 2;
	int TT_DOWNLOAD_MANUAL		= 3;
	int TT_PEER_IPSET			= 4;

		/**
		 * Unique type denoting this species of tag
		 * @return
		 */

        int
	getTagType();
	
	String
	getTagTypeName(
            boolean localize);
		
	boolean
	isTagTypeAuto();
	
	boolean
	isTagTypePersistent();
	
	long
	getTagTypeFeatures();
	
	boolean
	hasTagTypeFeature(
            long feature);
	
	Tag
	createTag(
            String name,
            boolean auto_add)
	
		throws TagException;
	
	void
	addTag(
            Tag t);
	
	void
	removeTag(
            Tag t);
	
	Tag
	getTag(
            int tag_id);
	
	Tag
	getTag(
            String tag_name,
            boolean is_localized);
	
	List<Tag>
	getTags();
	
	List<Tag>
	getTagsForTaggable(
            Taggable taggable);
	
	void
	removeTagType();
	
	TagManager
	getTagManager();
	
	int[]
	getColorDefault();
	
	void
	addTagTypeListener(
            TagTypeListener listener,
            boolean fire_for_existing);
	
	void
	removeTagTypeListener(
            TagTypeListener listener);
	
		/**
		 * taggable-specific listneer for this tag-type
		 * @param taggable
		 * @param listener
		 */

        void
	addTagListener(
                Taggable taggable,
                TagListener listener);
	
	void
	removeTagListener(
            Taggable taggable,
            TagListener listener);
}
