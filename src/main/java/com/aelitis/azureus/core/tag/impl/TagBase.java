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


package com.aelitis.azureus.core.tag.impl;

import java.io.File;
import java.util.*;

import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.internat.MessageText;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.IndentWriter;
import org.gudy.azureus2.core3.util.ListenerManager;
import org.gudy.azureus2.core3.util.ListenerManagerDispatcher;
import org.gudy.azureus2.core3.util.SimpleTimer;
import org.gudy.azureus2.pluginsimpl.local.PluginCoreUtils;

import com.aelitis.azureus.core.tag.Tag;
import com.aelitis.azureus.core.tag.TagException;
import com.aelitis.azureus.core.tag.TagFeatureExecOnAssign;
import com.aelitis.azureus.core.tag.TagFeatureFileLocation;
import com.aelitis.azureus.core.tag.TagFeatureLimits;
import com.aelitis.azureus.core.tag.TagFeatureNotifications;
import com.aelitis.azureus.core.tag.TagFeatureProperties;
import com.aelitis.azureus.core.tag.TagFeatureRSSFeed;
import com.aelitis.azureus.core.tag.TagFeatureRateLimit;
import com.aelitis.azureus.core.tag.TagListener;
import com.aelitis.azureus.core.tag.Taggable;
import com.aelitis.azureus.core.tag.TagFeatureProperties.TagProperty;
import com.aelitis.azureus.core.tag.TagFeatureProperties.TagPropertyListener;
import com.aelitis.azureus.core.util.CopyOnWriteList;

public abstract class 
TagBase
	implements Tag, SimpleTimer.TimerTickReceiver
{
	protected static final String	AT_RATELIMIT_UP					= "rl.up";
	protected static final String	AT_RATELIMIT_DOWN				= "rl.down";
	protected static final String	AT_VISIBLE						= "vis";
	protected static final String	AT_PUBLIC						= "pub";
	protected static final String	AT_GROUP						= "gr";
	protected static final String	AT_CAN_BE_PUBLIC				= "canpub";
	protected static final String	AT_ORIGINAL_NAME				= "oname";
	protected static final String	AT_IMAGE_ID						= "img.id";
	protected static final String	AT_COLOR_ID						= "col.rgb";
	protected static final String	AT_RSS_ENABLE					= "rss.enable";
	protected static final String	AT_RATELIMIT_UP_PRI				= "rl.uppri";
	protected static final String	AT_XCODE_TARGET					= "xcode.to";
	protected static final String	AT_FL_MOVE_COMP					= "fl.comp";
	protected static final String	AT_FL_MOVE_COMP_OPT				= "fl.comp.o";
	protected static final String	AT_FL_COPY_COMP					= "fl.copy";
	protected static final String	AT_FL_COPY_COMP_OPT				= "fl.copy.o";
	protected static final String	AT_FL_INIT_LOC					= "fl.init";
	protected static final String	AT_FL_INIT_LOC_OPT				= "fl.init.o";
	protected static final String	AT_RATELIMIT_MIN_SR				= "rl.minsr";
	protected static final String	AT_RATELIMIT_MAX_SR				= "rl.maxsr";
	protected static final String	AT_RATELIMIT_MAX_SR_ACTION		= "rl.maxsr.a";
	protected static final String	AT_RATELIMIT_MAX_AGGREGATE_SR	= "rl.maxaggsr";
	protected static final String	AT_RATELIMIT_MAX_AGGREGATE_SR_ACTION	= "rl.maxaggsr.a";
	protected static final String	AT_RATELIMIT_MAX_AGGREGATE_SR_PRIORITY	= "rl.maxaggsr.p";
	protected static final String	AT_PROPERTY_PREFIX				= "pp.";
	protected static final String	AT_EOA_PREFIX					= "eoa.";
	protected static final String	AT_BYTES_UP						= "b.up";
	protected static final String	AT_BYTES_DOWN					= "b.down";
	protected static final String	AT_DESCRIPTION					= "desc";
	protected static final String	AT_MAX_TAGGABLES				= "max.t";
	protected static final String	AT_REMOVAL_STRATEGY				= "max.t.r";
	protected static final String	AT_EOS_SCRIPT					= "eos.scr";
	protected static final String	AT_NOTIFICATION_POST			= "noti.post";
	protected static final String	AT_LIMIT_ORDERING				= "max.t.o";

	private static final String[] EMPTY_STRING_LIST = {};
	
	final TagTypeBase	tag_type;
	
	private final int			tag_id;
	private String		tag_name;
	
	private static final int TL_ADD 	= 1;
	private static final int TL_REMOVE 	= 2;
	private static final int TL_SYNC 	= 3;
	
	private final ListenerManager<TagListener>	t_listeners 	=
		ListenerManager.createManager(
			"TagListeners",
			new ListenerManagerDispatcher<TagListener>()
			{
				public void
				dispatch(
					TagListener			listener,
					int					type,
					Object				value )
				{					
					if ( type == TL_ADD ){
						
						listener.taggableAdded(TagBase.this,(Taggable)value);
						
					}else if ( type == TL_REMOVE ){
						
						listener.taggableRemoved(TagBase.this,(Taggable)value);
						
					}else if ( type == TL_SYNC ){
						
						listener.taggableSync( TagBase.this );
					}
				}
			});	
		
	private final Map<org.gudy.azureus2.plugins.tag.TagListener, TagListener>	listener_map = new HashMap<>();
	
	private Boolean	is_visible;
	private Boolean	is_public;
	private String	group;
	private int[]	colour;
	private String	description;
	
	
	private TagFeatureRateLimit		tag_rl;
	private TagFeatureRSSFeed		tag_rss;
	private TagFeatureFileLocation	tag_fl;
	private TagFeatureLimits		tag_limits;
	
	private Map<String,Object>		transient_properties;
	
	protected
	TagBase(
		TagTypeBase			_tag_type,
		int					_tag_id,
		String				_tag_name )
	{
		tag_type		= _tag_type;
		tag_id			= _tag_id;
		tag_name		= _tag_name;
		
		if ( getManager().isEnabled()){
		
			is_visible 	= readBooleanAttribute( AT_VISIBLE, null );
			is_public 	= readBooleanAttribute( AT_PUBLIC, null );
			group		= readStringAttribute( AT_GROUP, null );
			description = readStringAttribute( AT_DESCRIPTION, null );
			
			if ( this instanceof TagFeatureRateLimit ){
				
				tag_rl = (TagFeatureRateLimit)this;
			}
			
			if ( this instanceof TagFeatureRSSFeed ){
				
				tag_rss = (TagFeatureRSSFeed)this;
				
				if ( tag_rss.isTagRSSFeedEnabled()){
					
					getManager().checkRSSFeeds( this, true );
				}
			}
			
			if ( this instanceof TagFeatureFileLocation ){
				
				tag_fl = (TagFeatureFileLocation)this;
			}	
			
			if ( this instanceof TagFeatureLimits ){
				
				tag_limits = (TagFeatureLimits)this;
			}
		}
	}
		
	protected void
	initialized()
	{
		loadPersistentStuff();
				
		loadTransientStuff();
	}
	
	public Tag
	getTag()
	{
		return( this );
	}
	
	protected void
	addTag()
	{		
		if ( getManager().isEnabled()){
		
			tag_type.addTag( this );
		}
	}
	
	protected TagManagerImpl
	getManager()
	{
		return( tag_type.getTagManager());
	}
	
	public TagTypeBase
	getTagType()
	{
		return( tag_type );
	}
	
	public int
	getTagID()
	{
		return( tag_id );
	}
	
	public long 
	getTagUID() 
	{
		return((((long)getTagType().getTagType())<<32) | tag_id );
	}
	
	public String
	getTagName()
	{
		return( getTagName( true ));
	}
	
	protected String
	getTagNameRaw()
	{
		return( tag_name );
	}
	
	public String
	getTagName(
		boolean		localize )
	{
		if ( localize ){
			
			if ( tag_name.startsWith( "tag." )){
			
				return( MessageText.getString( tag_name ));
				
			}else{
				
				return( tag_name );
			}
		}else{
		
			if ( tag_name.startsWith( "tag." )){
			
				return( tag_name );
				
			}else{
				
				String original_name = readStringAttribute( AT_ORIGINAL_NAME, null );

				if ( original_name != null && original_name.startsWith( "tag." )){
					
					return( original_name );
				}
				
				return( "!" + tag_name + "!" );
			}
		}
	}
	
	public void 
	setTagName(
		String name )
	
		throws TagException 
	{
		if ( getTagType().isTagTypeAuto()){
			
			throw( new TagException( "Not supported" ));
		}
		
		if ( tag_name.startsWith( "tag." )){
		
			String original_name = readStringAttribute( AT_ORIGINAL_NAME, null );
		
			if ( original_name == null ){
			
				writeStringAttribute( AT_ORIGINAL_NAME, tag_name );
			}
		}
		
		tag_name = name;
				
		tag_type.fireChanged( this );
	}
	
		// public
	
	public boolean
	isPublic()
	{
		boolean pub = is_public==null?getPublicDefault():is_public;
		
		if ( pub ){
			
			boolean[] autos = isTagAuto();
			
			if ( autos[0] || autos[1] ){
				
				pub = false;
			}
		}
		
		return( pub );
	}
	
	public void
	setPublic(
		boolean	v )
	{
		if ( is_public == null || v != is_public ){
			
			if ( v && !canBePublic()){
				
				Debug.out( "Invalid attempt to set public" );
				
				return;
			}
			
			is_public	= v;
			
			writeBooleanAttribute( AT_PUBLIC, v );
			
			tag_type.fireChanged( this );
		}
	}
	
	protected boolean
	getPublicDefault()
	{
		if ( !getCanBePublicDefault()){
			
			return( false );
		}
		
		return( tag_type.getTagManager().getTagPublicDefault());
	}
	
	public void
	setCanBePublic(
		boolean	can_be_public )
	{
		writeBooleanAttribute( AT_CAN_BE_PUBLIC, can_be_public );
		
		if ( !can_be_public ){
			
			if ( isPublic()){
				
				setPublic( false );
			}
		}
	}
	
	public boolean
	canBePublic()
	{
		return( readBooleanAttribute( AT_CAN_BE_PUBLIC, getCanBePublicDefault()));
	}
	
	protected boolean
	getCanBePublicDefault()
	{
		return( true );
	}
	
	public boolean[]
	isTagAuto() 
	{
		return( new boolean[]{ false, false });
	}
	
		// visible
	
	public boolean
	isVisible()
	{
		return( is_visible==null?getVisibleDefault():is_visible );
	}
	
	public void
	setVisible(
		boolean	v )
	{
		if ( is_visible == null || v != is_visible ){
			
			is_visible	= v;
			
			writeBooleanAttribute( AT_VISIBLE, v );
			
			tag_type.fireChanged( this );
		}
	}
	
	public String
	getGroup()
	{
		return( group );
	}
	
	public void
	setGroup(
		String		new_group )
	{
		if ( group == null && new_group == null ){
			
			return;
		}
		
		if (group == null || !group.equals(new_group)){
			
			group	= new_group;
			
			writeStringAttribute( AT_GROUP, new_group );
			
			tag_type.fireChanged( this );
		}	
	}
	
	protected boolean
	getVisibleDefault()
	{
		return( true );
	}

	public String
	getImageID()
	{
		return( readStringAttribute( AT_IMAGE_ID, null ));
	}
	
	public void
	setImageID(
		String		id )
	{
		writeStringAttribute( AT_IMAGE_ID, id );
	}
	
	private int[]
	decodeRGB(
		String str )
	{
		if ( str == null ){
			
			return( null );
		}
		
		String[] bits = str.split( "," );
		
		if ( bits.length != 3 ){
			
			return( null );
		}
		
		int[] rgb = new int[3];
		
		for ( int i=0;i<bits.length;i++){
			
			try{
				
				rgb[i] = Integer.parseInt(bits[i]);
				
			}catch( Throwable e ){
				
				return( null );
			}
		}
		
		return( rgb );
	}
	
	private String
	encodeRGB(
		int[]	rgb )
	{
		if ( rgb == null || rgb.length != 3 ){
			
			return( null );
		}
		
		return( rgb[0]+","+rgb[1]+","+rgb[2] );
	}
	
	public boolean
	isColorDefault()
	{
		return( decodeRGB( readStringAttribute( AT_COLOR_ID, null )) == null );
	}
	
	public int[]
	getColor()
	{
		int[] result = colour;
		
		if ( result == null ){
			
			result = decodeRGB( readStringAttribute( AT_COLOR_ID, null ));
		
			if ( result == null ){
			
				result = tag_type.getColorDefault();
			}
			
			colour = result;
		}
		
		return( result );
	}
	
	public void
	setColor(
		int[]		rgb )
	{		
		writeStringAttribute( AT_COLOR_ID, encodeRGB( rgb ));
	
		colour = null;

		tag_type.fireChanged( this );
	}
	
	public boolean
	isTagRSSFeedEnabled()
	{
		if ( tag_rss != null ){
		
			return( readBooleanAttribute( AT_RSS_ENABLE, false ));
		}
		
		return( false );
	}
	
	public void
	setTagRSSFeedEnabled(
		boolean		enable )
	{
		if ( tag_rss != null ){
			
			if ( isTagRSSFeedEnabled() != enable ){
			
				writeBooleanAttribute( AT_RSS_ENABLE, enable );
				
				tag_type.fireChanged( this );
				
				tag_type.getTagManager().checkRSSFeeds( this, enable );
			}
		}
	}
	
		// initial save location
	
	public boolean
	supportsTagInitialSaveFolder()
	{
		return( false );
	}
	
	public File
	getTagInitialSaveFolder()
	{
		if ( tag_fl != null ){
			
			String str = readStringAttribute( AT_FL_INIT_LOC, null );
			
			if ( str == null ){
				
				return( null );
				
			}else{
				
				return( new File( str ));
			}
		}
		
		return( null );
	}
	
	public void
	setTagInitialSaveFolder(
		File		folder )
	{
		if ( tag_fl != null ){
			
			File	existing = getTagInitialSaveFolder();
			
			if ( existing == null && folder == null ){
				
				return;
				
			}else if (existing == null || !existing.equals(folder)){
				
				writeStringAttribute( AT_FL_INIT_LOC, folder==null?null:folder.getAbsolutePath());
				
				tag_type.fireChanged( this );
			}
		}
	}
	
	public long
	getTagInitialSaveOptions()
	{
		if ( tag_fl != null ){
			
			return( readLongAttribute( AT_FL_INIT_LOC_OPT, TagFeatureFileLocation.FL_DEFAULT ));
		}
		
		return( TagFeatureFileLocation.FL_NONE );
	}
	
	public void
	setTagInitialSaveOptions(
		long		options )
	{
		if ( tag_fl != null ){
			
			long	existing = getTagInitialSaveOptions();
			
			if ( existing != options ){
				
				writeLongAttribute( AT_FL_INIT_LOC_OPT, options );
				
				tag_type.fireChanged( this );
			}
		}	
	}
	
		// move on complete
	
	public boolean
	supportsTagMoveOnComplete()
	{
		return( false );
	}
	
	public File
	getTagMoveOnCompleteFolder()
	{
		if ( tag_fl != null ){
			
			String str = readStringAttribute( AT_FL_MOVE_COMP, null );
			
			if ( str == null ){
				
				return( null );
				
			}else{
				
				return( new File( str ));
			}
		}
		
		return( null );
	}
	
	public void
	setTagMoveOnCompleteFolder(
		File		folder )
	{
		if ( tag_fl != null ){
			
			File	existing = getTagMoveOnCompleteFolder();
			
			if ( existing == null && folder == null ){
				
				return;
				
			}else if (existing == null || !existing.equals(folder)){
				
				writeStringAttribute( AT_FL_MOVE_COMP, folder==null?null:folder.getAbsolutePath());
				
				tag_type.fireChanged( this );
			}
		}
	}
	
	public long
	getTagMoveOnCompleteOptions()
	{
		if ( tag_fl != null ){
			
			return( readLongAttribute( AT_FL_MOVE_COMP_OPT, TagFeatureFileLocation.FL_DEFAULT ));
		}
		
		return( TagFeatureFileLocation.FL_NONE );
	}
	
	public void
	setTagMoveOnCompleteOptions(
		long		options )
	{
		if ( tag_fl != null ){
			
			long	existing = getTagMoveOnCompleteOptions();
			
			if ( existing != options ){
				
				writeLongAttribute( AT_FL_MOVE_COMP_OPT, options );
				
				tag_type.fireChanged( this );
			}
		}	
	}
	
		// copy on complete
		
	public boolean
	supportsTagCopyOnComplete()
	{
		return( false );
	}
	
	public File
	getTagCopyOnCompleteFolder()
	{
		if ( tag_fl != null ){
			
			String str = readStringAttribute( AT_FL_COPY_COMP, null );
			
			if ( str == null ){
				
				return( null );
				
			}else{
				
				return( new File( str ));
			}
		}
		
		return( null );
	}
	
	public void
	setTagCopyOnCompleteFolder(
		File		folder )
	{
		if ( tag_fl != null ){
			
			File	existing = getTagCopyOnCompleteFolder();
			
			if ( existing == null && folder == null ){
				
				return;
				
			}else if (existing == null || !existing.equals(folder)){
				
				writeStringAttribute( AT_FL_COPY_COMP, folder==null?null:folder.getAbsolutePath());
				
				tag_type.fireChanged( this );
			}
		}
	}

	public long
	getTagCopyOnCompleteOptions()
	{
		if ( tag_fl != null ){
			
			return( readLongAttribute( AT_FL_COPY_COMP_OPT, TagFeatureFileLocation.FL_DEFAULT ));
		}
		
		return( TagFeatureFileLocation.FL_NONE );
	}
	
	public void
	setTagCopyOnCompleteOptions(
		long		options )
	{
		if ( tag_fl != null ){
			
			long	existing = getTagCopyOnCompleteOptions();
			
			if ( existing != options ){
				
				writeLongAttribute( AT_FL_COPY_COMP_OPT, options );
				
				tag_type.fireChanged( this );
			}
		}	
	}
	
		// min ratio
	
	public int
	getTagMinShareRatio()
	{
		return( -1 );
	}
	
	public void
	setTagMinShareRatio(
		int		sr )
	{
		Debug.out( "not supported" );
	}
	
		// max ratio
	
	public int
	getTagMaxShareRatio()
	{
		return( -1 );
	}
	
	public void
	setTagMaxShareRatio(
		int		sr )
	{
		Debug.out( "not supported" );
	}

	public int
	getTagMaxShareRatioAction()
	{
		return( -1 );
	}
	
	public void
	setTagMaxShareRatioAction(
		int		action )
	{
		Debug.out( "not supported" );
	}
	
		// aggregate share ratio
	
	public int
	getTagAggregateShareRatio()
	{
		return( -1 );
	}
	
	public int
	getTagMaxAggregateShareRatio()
	{
		return( -1 );
	}
	
	public void
	setTagMaxAggregateShareRatio(
		int		sr )
	{
		Debug.out( "not supported" );
	}
	
	public int
	getTagMaxAggregateShareRatioAction()
	{
		return( -1 );
	}
	
	public void
	setTagMaxAggregateShareRatioAction(
		int		action )
	{
		Debug.out( "not supported" );
	}
	
	public boolean
	getTagMaxAggregateShareRatioHasPriority()
	{
		return( true );
	}
	
	public void
	setTagMaxAggregateShareRatioHasPriority(
		boolean		priority )
	{
		Debug.out( "not supported" );
	}
	
		// limits
	
	public int
	getMaximumTaggables()
	{
		if ( tag_limits != null ){
		
			return( readLongAttribute( AT_MAX_TAGGABLES, 0L ).intValue());
		}
		
		return( -1 );
	}
	
	public void
	setMaximumTaggables(
		int		max )
	{
		if ( tag_limits != null ){
			
			if ( getMaximumTaggables() != max ){
			
				writeLongAttribute( AT_MAX_TAGGABLES, max );
				
				tag_type.fireChanged( this );
				
				checkMaximumTaggables();
			}
		}
	}
	
	protected void
	checkMaximumTaggables()
	{	
	}
	
	public int
	getRemovalStrategy()
	{
		if ( tag_limits != null ){
		
			return( readLongAttribute( AT_REMOVAL_STRATEGY, (long)TagFeatureLimits.RS_DEFAULT ).intValue());
		}
		
		return( -1 );
	}
	
	public void
	setRemovalStrategy(
		int		id )
	{
		if ( tag_limits != null ){
			
			if ( getRemovalStrategy() != id ){
			
				writeLongAttribute( AT_REMOVAL_STRATEGY, id );
				
				tag_type.fireChanged( this );
			}
		}	
	}
	
	public int
	getOrdering()
	{
		if ( tag_limits != null ){
		
			return( readLongAttribute( AT_LIMIT_ORDERING, (long)TagFeatureLimits.OP_DEFAULT ).intValue());
		}
		
		return( -1 );
	}
	
	public void
	setOrdering(
		int		id )
	{
		if ( tag_limits != null ){
			
			if ( getOrdering() != id ){
			
				writeLongAttribute( AT_LIMIT_ORDERING, id );
				
				tag_type.fireChanged( this );
			}
		}	
	}
	
	public TagProperty[]
	getSupportedProperties()
	{
		return( new TagProperty[0] );
	}
	
	public TagProperty
	getProperty(
		String		name )
	{
		TagProperty[] props = getSupportedProperties();
		
		for ( TagProperty prop: props ){
			
			if (Objects.equals(prop.getName(false),name)){
				
				return( prop );
			}
		}
		
		return( null );
	}
	
	protected TagProperty
	createTagProperty(
		String		name,
		int			type )
	{
		return( new TagPropertyImpl( name, type ));
	}
	
		// exec on assign
	
	public int
	getSupportedActions()
	{
		return( TagFeatureExecOnAssign.ACTION_NONE );
	}
	
	public boolean
	supportsAction(
		int		action )
	{
		return((getSupportedActions() & action ) != 0 );
	}
	
	public boolean
	isActionEnabled(
		int		action )
	{
		if ( !supportsAction( action )){
						
			return( false );
		}
		
		return( readBooleanAttribute( AT_PROPERTY_PREFIX + action, false ));
	}
	
	public void
	setActionEnabled(
		int			action,
		boolean		enabled )
	{
		if ( !supportsAction( action )){
			
			if ( enabled ){
			
				Debug.out( "not supported" );
			}
			
			return;
		}
		
		writeBooleanAttribute( AT_PROPERTY_PREFIX + action, enabled );
	}
	
	public String
	getActionScript()
	{
		String script = readStringAttribute( AT_EOS_SCRIPT, "" );
		
		if ( script == null ){
			
			script = "";
		}
		
		return( script );
	}
	
	public void
	setActionScript(
		String		script )
	{
		if ( script == null ){
			
			script = "";
		}
		
		script = script.trim();
		
		writeStringAttribute( AT_EOS_SCRIPT, script);
			
		setActionEnabled( TagFeatureExecOnAssign.ACTION_SCRIPT, script.length() > 0 );
	}
	
		// notifications
	
	public int
	getPostingNotifications()
	{
		return( readLongAttribute( AT_NOTIFICATION_POST, (long)TagFeatureNotifications.NOTIFY_NONE ).intValue());
	}
	
	public void
	setPostingNotifications(
		int		flags )
	{
		writeLongAttribute( AT_NOTIFICATION_POST, flags );
	}
	
		// others
	
	public void
	addTaggable(
		Taggable	t )
	{
		t_listeners.dispatch( TL_ADD, t );
		
		tag_type.taggableAdded( this, t );
		
		tag_type.fireChanged( this );
		
		if ( tag_limits != null ){
			
			checkMaximumTaggables();
		}
	}
	
	public void
	removeTaggable(
		Taggable	t )
	{
		t_listeners.dispatch( TL_REMOVE, t );
		
		tag_type.taggableRemoved( this, t );

		tag_type.fireChanged( this );

	}
	
	protected void
	sync()
	{
		t_listeners.dispatch( TL_SYNC, null );
		
		tag_type.taggableSync( this );
		
		savePersistentStuff();
	}
	
	protected void
	closing()
	{
		savePersistentStuff();
	}
	
	public void
	removeTag()
	{
		boolean was_rss = isTagRSSFeedEnabled();
		
		tag_type.removeTag( this );
		
		if ( was_rss ){
		
			tag_type.getTagManager().checkRSSFeeds( this, false );
		}
				
		saveTransientStuff();
	}
	
	public String
	getDescription()
	{
		return( description );
	}
	
	public void
	setDescription(
		String		str )
	{
		String existing = getDescription();
		
		if (Objects.equals(existing,str)){
			
			return;
			
		}else if ( str == null || existing == null ){
			
		}else if ( str.equals( existing )){
			
			return;
		}
			
		description = str;
		
		writeStringAttribute( AT_DESCRIPTION, str );
		
		tag_type.fireChanged( this );
	}
	
	@Override
	public void 
	setTransientProperty(
		String property, Object value ) 
	{
		synchronized( this ){
			
			if ( transient_properties == null ){
				
				if ( value == null ){
					
					return;
				}
				
				transient_properties = new HashMap<>();
			}		
			
			if ( value == null ){
				
				transient_properties.remove( property );
				
			}else{
				
				transient_properties.put( property, value );
			}
			
			tag_type.fireChanged( this );
		}
	}
	
	@Override
	public Object 
	getTransientProperty(String property) 
	{
		synchronized( this ){
			
			if ( transient_properties == null ){
				
				return( null );
			}
			
			return( transient_properties.get( property ));
		}
	}
	
	public void
	addTagListener(
		TagListener	listener,
		boolean		fire_for_existing )
	{
		if (!t_listeners.hasListener(listener)) {
			t_listeners.addListener( listener );
		}
		
		if ( fire_for_existing ){
			
			for ( Taggable t: getTagged()){
				
				listener.taggableAdded( this, t );
			}
		}
	}
	
	protected void
	destroy()
	{
		Set<Taggable>	taggables = getTagged();
		
		for( Taggable t: taggables ){
			
			t_listeners.dispatch( TL_REMOVE, t );
			
			tag_type.taggableRemoved( this, t );
		}
	}
	
	public void
	removeTagListener(
		TagListener	listener )
	{
		t_listeners.removeListener( listener );
	}
	
	public List<org.gudy.azureus2.plugins.tag.Taggable>
	getTaggables()
	{
		Set<Taggable> taggables = getTagged();
		
		List<org.gudy.azureus2.plugins.tag.Taggable> result = new ArrayList<>(taggables.size());
		
		for ( Taggable t: taggables ){
			
			if ( t instanceof DownloadManager ){
				
				result.add(PluginCoreUtils.wrap((DownloadManager)t));
			}
		}
		
		return( result );
	}
	
	@Override
	public void 
	requestAttention()
	{
		tag_type.requestAttention( this );
	}
	
	public void 
	addListener(
		final org.gudy.azureus2.plugins.tag.TagListener listener ) 
	{
		synchronized( listener_map ){
			
			TagListener l = listener_map.get( listener );
			
			if ( l != null ){
				
				Debug.out( "listener already added" );
				
				return;
			}
			
			l = new TagListener() {
				
				public void taggableSync(Tag tag) {
					listener.taggableSync(tag);
				}
				
				public void taggableRemoved(Tag tag, Taggable tagged) {
					listener.taggableRemoved(tag, tagged);
				}
				
				public void taggableAdded(Tag tag, Taggable tagged) {
					listener.taggableAdded(tag, tagged);
				}
			};
			
			listener_map.put( listener, l );
			
			addTagListener( l, false );
		}
		
	}
	
	public void 
	removeListener(
		org.gudy.azureus2.plugins.tag.TagListener listener ) 
	{
		synchronized( listener_map ){
			
			TagListener l = listener_map.remove( listener );
			
			if ( l == null ){
				
				Debug.out( "listener not found" );
				
				return;
			}
			
			removeTagListener( l );
		}
	}
	
	protected Boolean
	readBooleanAttribute(
		String		attr,
		Boolean		def )
	{
		return( tag_type.readBooleanAttribute( this, attr, def ));
	}
	
	protected boolean
	writeBooleanAttribute(
		String	attr,
		Boolean	value )
	{
		return( tag_type.writeBooleanAttribute( this, attr, value ));
	}
	
	protected Long
	readLongAttribute(
		String	attr,
		Long	def )
	{
		return( tag_type.readLongAttribute( this, attr, def ));
	}
	
	protected boolean
	writeLongAttribute(
		String	attr,
		long	value )
	{
		return( tag_type.writeLongAttribute( this, attr, value ));
	}
	
	protected String
	readStringAttribute(
		String	attr,
		String	def )
	{
		return( tag_type.readStringAttribute( this, attr, def ));
	}
	
	protected void
	writeStringAttribute(
		String	attr,
		String	value )
	{
		tag_type.writeStringAttribute( this, attr, value );
	}
	
	protected String[]
	readStringListAttribute(
		String		attr,
		String[]	def )
	{
		return( tag_type.readStringListAttribute( this, attr, def ));
	}
	
	protected boolean
	writeStringListAttribute(
		String		attr,
		String[]	value )
	{
		return( tag_type.writeStringListAttribute( this, attr, value ));
	}
	
	private static final Map<Long,long[][]>	session_cache = new HashMap<>();
	
	private long[]						total_up_at_start;
	private long[]						total_down_at_start;
	
	private long[]						session_up;
	private long[]						session_down;
	
	private void
	loadTransientStuff()
	{
		if ( tag_rl != null && tag_rl.supportsTagRates()){
			
			synchronized( session_cache ){
				
				long[][] entry = session_cache.get( getTagUID());
				
				if ( entry != null ){
					
					total_up_at_start	= entry[0];
					total_down_at_start	= entry[1];
					session_up 			= entry[2];
					session_down 		= entry[3];
				}
			}
		}
	}
	
	private void
	saveTransientStuff()
	{
			// ipset tags get removed and then re-added when the schedule is updated so we need to
			// stash away their state and reload it when they get added back
		
		if ( tag_rl != null && tag_rl.supportsTagRates()){

			long[] session_up 		= getTagSessionUploadTotalRaw();
			long[] session_down 	= getTagSessionDownloadTotalRaw();
					
			synchronized( session_cache ){
				
				session_cache.put( getTagUID(), new long[][]{ total_up_at_start, total_down_at_start, session_up, session_down });
			}
		}
	}
	
	private void
	loadPersistentStuff()
	{
		if ( tag_rl != null && tag_rl.supportsTagRates()){

			String[] ups = readStringListAttribute( AT_BYTES_UP, null );
			
			if ( ups != null ){
				
				total_up_at_start = new long[ups.length];
				
				for ( int i=0;i<ups.length;i++){
					
					try{					
						total_up_at_start[i] = Long.parseLong( ups[i] );
						
					}catch( Throwable e ){
						
						Debug.out( e );
					}
				}
			}
			
			String[] downs = readStringListAttribute( AT_BYTES_DOWN, null );
			
			if ( downs != null ){
				
				total_down_at_start = new long[downs.length];
				
				for ( int i=0;i<downs.length;i++){
					
					try{					
						total_down_at_start[i] = Long.parseLong( downs[i] );
						
					}catch( Throwable e ){
						
						Debug.out( e );
					}
				}
			}
		}
	}
	
	private void
	savePersistentStuff()
	{
		if ( tag_rl != null && tag_rl.supportsTagRates()){
			
			long[] session_up = getTagSessionUploadTotalRaw();
			
			if ( session_up != null ){
				
				String[] ups = new String[session_up.length];
				
				for ( int i=0;i<ups.length;i++ ){
					
					long l = session_up[i];
					
					if ( total_up_at_start != null && total_up_at_start.length > i ){
						
						l += total_up_at_start[i];
					}
					
					ups[i] = String.valueOf( l );
				}
				
				writeStringListAttribute( AT_BYTES_UP, ups );
			}
			
			long[] session_down = getTagSessionDownloadTotalRaw();
			
			if ( session_down != null ){
				
				String[] downs = new String[session_down.length];
				
				for ( int i=0;i<downs.length;i++ ){
					
					long l = session_down[i];
					
					if ( total_down_at_start != null && total_down_at_start.length > i ){
						
						l += total_down_at_start[i];
					}
					
					downs[i] = String.valueOf( l );
				}
				
				writeStringListAttribute( AT_BYTES_DOWN, downs );
			}
		}
	}
	
	public long[]
	getTagUploadTotal()
	{
		long[] result = getTagSessionUploadTotalRaw();
		
		if ( result != null ){
			
			if ( total_up_at_start != null && total_up_at_start.length == result.length ){
				
				for ( int i=0;i<result.length;i++ ){
					
					result[i] += total_up_at_start[i];
				}
			}
		}
		
		return( result );
	}
	
	private long[] session_up_reset;
	private long[] session_down_reset;
	
	public long[]
	getTagSessionUploadTotal()
	{
		long[] result = getTagSessionUploadTotalRaw();
		
		if ( result != null && session_up_reset != null && result.length == session_up_reset.length ){
			
			for ( int i=0;i<result.length;i++){
				
				result[i] -= session_up_reset[i];
			}
		}
		
		return( result );
	}
	
	public void
	resetTagSessionUploadTotal()
	{
		session_up_reset = getTagSessionUploadTotalRaw();
	}
	
	private long[]
	getTagSessionUploadTotalRaw()
	{
		if ( tag_rl == null || !tag_rl.supportsTagRates()){
			
			return( null );
		}
				
		long[] result =  getTagSessionUploadTotalCurrent();
		
		if ( result != null && session_up != null ){
			
			if ( result.length == session_up.length ){
				
				for ( int i=0;i<result.length;i++){
					
					result[i] += session_up[i];
				}
			}else{
				
				Debug.out( "derp" );
			}
		}
		
		return( result );
	}
	
	protected long[]
	getTagSessionUploadTotalCurrent()
	{	
		return( null );
	}

	public long[]
	getTagDownloadTotal()
	{
		long[] result = getTagSessionDownloadTotalRaw();
		
		if ( result != null ){
			
			if ( total_down_at_start != null && total_down_at_start.length == result.length ){
				
				for ( int i=0;i<result.length;i++ ){
					
					result[i] += total_down_at_start[i];
				}
			}
		}
		
		return( result );
	}
	
	public long[]
	getTagSessionDownloadTotal()
	{
		long[] result = getTagSessionDownloadTotalRaw();
		
		if ( result != null && session_down_reset != null && result.length == session_down_reset.length ){
			
			for ( int i=0;i<result.length;i++){
				
				result[i] -= session_down_reset[i];
			}
		}
		
		return( result );
	}
	
	public void
	resetTagSessionDownloadTotal()
	{
		session_down_reset = getTagSessionDownloadTotalRaw();
	}
	
	private long[]
	getTagSessionDownloadTotalRaw()
	{
		if ( tag_rl == null || !tag_rl.supportsTagRates()){
			
			return( null );
		}
				
		long[] result =  getTagSessionDownloadTotalCurrent();
		
		if ( result != null && session_down != null ){
			
			if ( result.length == session_down.length ){
				
				for ( int i=0;i<result.length;i++){
					
					result[i] += session_down[i];
				}
			}else{
				
				Debug.out( "derp" );
			}
		}
		
		return( result );
	}
	
	protected long[]
	getTagSessionDownloadTotalCurrent()
	{	
		return( null );
	}
	
	private static final int HISTORY_MAX_SECS = 30*60;
	private volatile boolean history_retention_required;
	private long[]	history;
	private int		history_pos;
	private boolean	history_wrapped;
	private boolean	timer_registered;
	
	public void
	setRecentHistoryRetention(
		boolean	required )
	{
		if ( tag_rl == null || !tag_rl.supportsTagRates()){
			
			return;
		}
		
		synchronized( this ){
			
			if ( required ){
				
				if ( !history_retention_required ){
					
					history 	= new long[HISTORY_MAX_SECS];
					
					history_pos	= 0;
					
					history_retention_required = true;
					
					if ( !timer_registered ){
						
						SimpleTimer.addTickReceiver( this );
						
						timer_registered = true;
					}
				}
			}else{
				
				history = null;
				
				history_retention_required = false;
				
				if ( timer_registered ){
					
					SimpleTimer.removeTickReceiver( this );
					
					timer_registered = false;
				}
			}
		}
	}
	
	public int[][]
 	getRecentHistory()
 	{
 		synchronized( this ){

 			if ( history == null ){
 		
 				return( new int[2][0] );
 				
 			}else{
 			
 				int	entries = history_wrapped?HISTORY_MAX_SECS:history_pos;
 				int	start	= history_wrapped?history_pos:0;
 				
 				int[][] result = new int[2][entries];
 				
 				int	pos = start;
 				
 				for ( int i=0;i<entries;i++){
 					
 					if ( pos == HISTORY_MAX_SECS ){
 						
 						pos = 0;
 					}
 					
 					long entry = history[pos++];
 					
 					int	send_rate 	= (int)((entry>>32)&0xffffffffL);
 					int	recv_rate 	= (int)((entry)    &0xffffffffL);
 					
 					result[0][i] = send_rate;
 					result[1][i] = recv_rate;
  				}
 				
 				return( result );
 			}
 		}
 	}
	
	@Override
	public long 
	getTaggableAddedTime(
		Taggable taggble ) 
	{
		return( -1 );
	}
	
 	
 	public void
 	tick(
 		long	mono_now,
 		int 	count )
 	{
 		if ( !history_retention_required ){
 			
 			return;
 		}
 		
  		long send_rate 			= tag_rl.getTagCurrentUploadRate();
 		long receive_rate 		= tag_rl.getTagCurrentDownloadRate();
 		
 		long	entry = 
 			(((send_rate)<<32) & 0xffffffff00000000L ) |
 			(((receive_rate)   & 0x00000000ffffffffL ));
  			
 		
 		synchronized( this ){
 			
 			if ( history != null ){
 				
 				history[history_pos++] = entry;
 				
 				if ( history_pos == HISTORY_MAX_SECS ){
 					
 					history_pos 	= 0;
 					history_wrapped	= true;
 				}
 			}
 		}
 	}
 	
 	private class
 	TagPropertyImpl
 		implements TagProperty
 	{
 		private final String		name;
 		private final int			type;
 		
 		private final CopyOnWriteList<TagPropertyListener>	listeners = new CopyOnWriteList<>();
 		
 		private
 		TagPropertyImpl(
 			String		_name,
 			int			_type )
 		{
 			name		= _name;
 			type		= _type;
 		}
 		
 		public Tag 
 		getTag() 
 		{
 			return( TagBase.this );
 		}
 		
		public int
		getType()
		{
			return( type );
		}
		
		public String
		getName(
			boolean	localize )
		{
			if ( localize ){
				
				return( MessageText.getString( "tag.property." + name ));
				
			}else{
			
				return( name );
			}
		}
		
		public void
		setStringList(
			String[]	value )
		{
			if ( writeStringListAttribute( AT_PROPERTY_PREFIX + name, value )){
				
				for ( TagPropertyListener l: listeners ){
					
					try{
						l.propertyChanged( this );
						
					}catch( Throwable e ){
						
						Debug.out( e );
					}
				}
				
				tag_type.fireChanged( TagBase.this );
			}
		}
		
		public String[]
		getStringList()
		{
			return( readStringListAttribute( AT_PROPERTY_PREFIX + name, EMPTY_STRING_LIST ));
		}
		
		public void
		setBoolean(
			Boolean	value )
		{
			if ( writeBooleanAttribute( AT_PROPERTY_PREFIX + name, value )){
				
				for ( TagPropertyListener l: listeners ){
					
					try{
						l.propertyChanged( this );
						
					}catch( Throwable e ){
						
						Debug.out( e );
					}
				}
				
				tag_type.fireChanged( TagBase.this );
			}
		}
		
		public Boolean
		getBoolean()
		{
			return( readBooleanAttribute( AT_PROPERTY_PREFIX + name, null ));
		}
		
		public void
		setLong(
			Long	value )
		{
			if ( writeLongAttribute( AT_PROPERTY_PREFIX + name, value )){
				
				for ( TagPropertyListener l: listeners ){
					
					try{
						l.propertyChanged( this );
						
					}catch( Throwable e ){
						
						Debug.out( e );
					}
				}
				
				tag_type.fireChanged( TagBase.this );
			}
		}
		
		public Long
		getLong()
		{
			return( readLongAttribute( AT_PROPERTY_PREFIX + name, null ));
		}
		
		public String
		getString()
		{
			String	value = null;
			
			switch( getType()){
				case TagFeatureProperties.PT_STRING_LIST:{
					String[] vals = getStringList();
					
					if ( vals != null && vals.length > 0 ){
						value = "";
						
						String name = getName( false );
						
						if ( name.equals( TagFeatureProperties.PR_TRACKER_TEMPLATES )){
							
							String str_merge 	= MessageText.getString("label.merge" );
							String str_replace 	= MessageText.getString("label.replace" );
							String str_remove 	= MessageText.getString("Button.remove" );

							for ( String val: vals ){
								String[] bits = val.split( ":" );
								String type = bits[0];
								String str 	= bits[1];
								
								if ( type.equals("m")){
									str += ": " + str_merge;
								}else if ( type.equals( "r" )){
									str += ": " + str_replace;
								}else{
									str += ": " + str_remove;
								}
								value += (value.length()==0?"":"," ) + str;
							}
						}else if ( name.equals( TagFeatureProperties.PR_CONSTRAINT )){
							
							value += vals[0];
							
							if ( vals.length > 1 ){
								
								String options = vals[1];
								
								boolean auto_add 	= !options.contains( "am=2;" );
								boolean auto_remove = !options.contains( "am=1;" );
								
								if ( auto_add && auto_remove ){
									
								}else if ( auto_add || auto_remove ){
									
									value += "," + MessageText.getString( "label.scope" );
									
									value += "=";
									
									if ( auto_add ){
										
										value += MessageText.getString( "label.addition.only" );
										
									}else{
										
										value += MessageText.getString( "label.removal.only" );
									}
								}	
							}
						}else{
							for ( String val: vals ){
								value += (value.length()==0?"":"," ) + val;
							}
						}
					}
					break;
				}
				case TagFeatureProperties.PT_BOOLEAN:{
					Boolean val = getBoolean();
					if ( val != null ){
						value = String.valueOf( val );
					}
					break;
				}
				case TagFeatureProperties.PT_LONG:{
					Long val = getLong();
					if ( val != null ){
						value = String.valueOf( val );
					}
					break;
				}
				default:{
					value = "Unknown type";
				}
			}
			
			if ( value == null ){
				
				return( "" );
				
			}else{
			
				return( getName( true ) + "=" + value );
			}
		}
		
		public void
		addListener(
			TagPropertyListener		listener )
		{
			listeners.add( listener );	
		}
		
		public void
		removeListener(
			TagPropertyListener		listener )
		{
			listeners.remove( listener );
		}
		
		public void
		syncListeners()
		{
			for ( TagPropertyListener l: listeners ){
				
				try{
					l.propertySync( this );
					
				}catch( Throwable e ){
					
					Debug.out( e );
				}
			}
		}
 	}
 	
	public void
	generate(
		IndentWriter		writer )
	{
		writer.println( tag_name );
		
		try{
			writer.indent();
			
			tag_type.generateConfig( writer, this );
			
		}finally{
			
			writer.exdent();
		}
	}
}
