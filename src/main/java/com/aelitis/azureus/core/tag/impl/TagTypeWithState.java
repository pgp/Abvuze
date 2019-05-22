/*
 * Created on Mar 22, 2013
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


package com.aelitis.azureus.core.tag.impl;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.aelitis.azureus.core.tag.Tag;
import com.aelitis.azureus.core.tag.Taggable;
import com.aelitis.azureus.core.tag.TaggableResolver;

public class 
TagTypeWithState
	extends TagTypeBase
{
	private final List<Tag>	tags = new CopyOnWriteArrayList<>();

	private TaggableResolver		resolver;
	
	protected
	TagTypeWithState(
		int			tag_type,
		int			tag_features,
		String		tag_name )
	{
		super( tag_type, tag_features, tag_name );
	}
	
	protected
	TagTypeWithState(
		int					tag_type,
		TaggableResolver	_resolver,
		int					tag_features,
		String				tag_name )
	{
		super( tag_type, tag_features, tag_name );
		
		resolver = _resolver;
	}
	
	protected Taggable
	resolveTaggable(
		String		id )
	{
		if ( resolver == null ){
			
			return( super.resolveTaggable( id ));
		}
		
		return( resolver.resolveTaggable( id ));
	}
	
	protected TaggableResolver
	getResolver()
	{
		return( resolver );
	}
	
	protected void
	removeTaggable(
		TaggableResolver	_resolver,
		Taggable			taggable )
	{	
		if ( resolver == _resolver ){
			
			for ( Tag t: tags ){
				
				t.removeTaggable( taggable );
			}
		}
		
		super.removeTaggable(_resolver, taggable );
	}
	
	public void
	addTag(
		Tag		t )
	{
		tags.add( t );
		
		if ( t instanceof TagWithState ){
		
			getTagManager().tagCreated((TagWithState)t );
		}
		
		super.addTag( t );		
	}
	
	public void
	removeTag(
		Tag		t )
	{
		tags.remove( t );
		
		if ( t instanceof TagWithState ){
			
			getTagManager().tagRemoved((TagWithState)t );
		}
		
		super.removeTag( t );		
	}
	
	public List<Tag> getTags() {
		return tags;
	}
}
