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


package com.aelitis.azureus.core.tag.impl;

import java.util.*;
import java.util.regex.Pattern;

import org.gudy.azureus2.core3.disk.DiskManager;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.download.DownloadManagerState;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.tracker.client.TRTrackerScraperResponse;
import org.gudy.azureus2.core3.util.AENetworkClassifier;
import org.gudy.azureus2.core3.util.AERunnable;
import org.gudy.azureus2.core3.util.AsyncDispatcher;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.FrequencyLimitedDispatcher;
import org.gudy.azureus2.core3.util.SimpleTimer;
import org.gudy.azureus2.core3.util.SystemTime;
import org.gudy.azureus2.core3.util.TimerEvent;
import org.gudy.azureus2.core3.util.TimerEventPerformer;
import org.gudy.azureus2.core3.util.TimerEventPeriodic;
import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.download.DownloadListener;
import org.gudy.azureus2.pluginsimpl.local.PluginCoreUtils;

import com.aelitis.azureus.core.AzureusCore;
import com.aelitis.azureus.core.AzureusCoreFactory;
import com.aelitis.azureus.core.AzureusCoreLifecycleAdapter;
import com.aelitis.azureus.core.AzureusCoreRunningListener;
import com.aelitis.azureus.core.tag.Tag;
import com.aelitis.azureus.core.tag.TagFeatureProperties;
import com.aelitis.azureus.core.tag.TagFeatureProperties.TagProperty;
import com.aelitis.azureus.core.tag.TagFeatureProperties.TagPropertyListener;
import com.aelitis.azureus.core.tag.TagListener;
import com.aelitis.azureus.core.tag.TagType;
import com.aelitis.azureus.core.tag.TagTypeListener;
import com.aelitis.azureus.core.tag.Taggable;
import com.aelitis.azureus.core.tag.TaggableLifecycleAdapter;

public class 
TagPropertyConstraintHandler 
	implements TagTypeListener, DownloadListener
{
	private final AzureusCore		azureus_core;
	private final TagManagerImpl	tag_manager;
		
	private boolean		initialised;
	private boolean 	initial_assignment_complete;
	private boolean		stopping;
	
	final Map<Tag,TagConstraint>	constrained_tags 	= new HashMap<>();
	
	private boolean	dm_listener_added;
	
	final Map<Tag,Map<DownloadManager,Long>>			apply_history 		= new HashMap<>();
	
	private final AsyncDispatcher	dispatcher = new AsyncDispatcher( "tag:constraints" );
	
	private final FrequencyLimitedDispatcher	freq_lim_dispatcher =
		new FrequencyLimitedDispatcher(
			new AERunnable()
			{
				public void
				runSupport()
				{
					checkFreqLimUpdates();
				}
			},
			5000 );
	
	final IdentityHashMap<DownloadManager,List<TagConstraint>>	freq_lim_pending = new IdentityHashMap<>();
	
	
	private TimerEventPeriodic		timer;
		
	private
	TagPropertyConstraintHandler()
	{
		azureus_core	= null;
		tag_manager		= null;
	}

	protected
	TagPropertyConstraintHandler(
		AzureusCore		_core,
		TagManagerImpl	_tm )
	{
		azureus_core	= _core;
		tag_manager		= _tm;
		
		if( azureus_core != null ){
			
			azureus_core.addLifecycleListener(
				new AzureusCoreLifecycleAdapter()
				{
					@Override
					public void 
					stopping(AzureusCore core) 
					{
						stopping	= true;
					}
				});
		}
		
		tag_manager.addTaggableLifecycleListener(
			Taggable.TT_DOWNLOAD,
			new TaggableLifecycleAdapter()
			{
				public void
				initialised(
					List<Taggable>	current_taggables )
				{
					try{
						TagType tt = tag_manager.getTagType( TagType.TT_DOWNLOAD_MANUAL );
						
						tt.addTagTypeListener( TagPropertyConstraintHandler.this, true );

					}finally{
						
						AzureusCoreFactory.addCoreRunningListener(
							new AzureusCoreRunningListener()
							{	
								public void 
								azureusCoreRunning(
									AzureusCore core )
								{
									synchronized( constrained_tags ){
																				
										initialised = true;

										apply( core.getGlobalManager().getDownloadManagers(), true );
									}
								}
							});
					}
				}
				
				public void
				taggableCreated(
					Taggable		taggable )
				{
					apply((DownloadManager)taggable, null, false );
				}
			});
	}
	
	private static Object	process_lock = new Object();
	private static int		processing_disabled_count;
	
	private static List<Object[]>	processing_queue = new ArrayList<>();
	
	public void
	setProcessingEnabled(
		boolean	enabled )
	{
		synchronized( process_lock ){
			
			if ( enabled ){
				
				processing_disabled_count--;
				
				if ( processing_disabled_count == 0 ){
					
					List<Object[]> to_do = new ArrayList<>(processing_queue);
					
					processing_queue.clear();
					
					for ( Object[] entry: to_do ){
						
						TagConstraint 	constraint 	= (TagConstraint)entry[0];
						Object			target		= entry[1];
						
						try{
							
							if ( target instanceof DownloadManager ){
								
								constraint.apply((DownloadManager)target);
								
							}else{
								
								constraint.apply((List<DownloadManager>)target);
							}
						}catch( Throwable e ){
							
							Debug.out( e );
						}
					}
				}
			}else{
				
				processing_disabled_count++;
			}
		}
	}
	
	private static boolean
	canProcess(
		TagConstraint		constraint,
		DownloadManager		dm )
	{
		synchronized( process_lock ){

			if ( processing_disabled_count == 0 ){
				
				return( true );
				
			}else{
			
				processing_queue.add( new Object[]{ constraint, dm });
				
				return( false );
			}
		}
	}
	
	private static boolean
	canProcess(
		TagConstraint				constraint,
		List<DownloadManager>		dms )
	{
		synchronized( process_lock ){

			if ( processing_disabled_count == 0 ){
				
				return( true );
				
			}else{
			
				processing_queue.add( new Object[]{ constraint, dms });
				
				return( false );
			}
		}
	}
	
	public void
	tagTypeChanged(
		TagType		tag_type )
	{
	}
	
	@Override
	public void tagEventOccurred(TagEvent event ) {
		int	type = event.getEventType();
		Tag	tag = event.getTag();
		if ( type == TagEvent.ET_TAG_ADDED ){
			tagAdded( tag );
		}else if ( type == TagEvent.ET_TAG_REMOVED ){
			tagRemoved( tag );
		}
	}
	
	public void
	tagAdded(
		Tag			tag )
	{
		TagFeatureProperties tfp = (TagFeatureProperties)tag;
		
		TagProperty prop = tfp.getProperty( TagFeatureProperties.PR_CONSTRAINT );
		
		if ( prop != null ){
			
			prop.addListener(
				new TagPropertyListener() 
				{
					public void
					propertyChanged(
						TagProperty		property )
					{		
						handleProperty( property );
					}
					
					public void
					propertySync(
						TagProperty		property )
					{	
					}
				});
			
			handleProperty( prop );
		}
		
		tag.addTagListener(
			new TagListener() 
			{	
				public void 
				taggableSync(
					Tag tag ) 
				{
				}
				
				public void 
				taggableRemoved(
					Tag 		tag, 
					Taggable 	tagged ) 
				{
					apply((DownloadManager)tagged, tag, true );
				}
				
				public void 
				taggableAdded(
					Tag 		tag,
					Taggable 	tagged ) 
				{
					apply((DownloadManager)tagged, tag, true );
				}
			}, false );
	}
	
	private void
	checkTimer()
	{
			// already synchronized on constrainted_tags by callers
		
		if ( constrained_tags.size() > 0 ){
			
			if ( timer == null ){
				
				timer = 
					SimpleTimer.addPeriodicEvent(
						"tag:constraint:timer",
						30*1000,
						new TimerEventPerformer() {
							
							public void 
							perform(
								TimerEvent event) 
							{
								apply_history.clear();
								
								apply();
							}
						});
				
				AzureusCoreFactory.addCoreRunningListener(
					new AzureusCoreRunningListener()
					{
						public void 
						azureusCoreRunning(
							AzureusCore core )
						{
							synchronized( constrained_tags ){
								
								if ( timer != null ){
							
									azureus_core.getPluginManager().getDefaultPluginInterface().getDownloadManager().getGlobalDownloadEventNotifier().addListener( TagPropertyConstraintHandler.this );
									
									dm_listener_added = true;
								}
							}
						}
					});
			}
			
		}else if ( timer != null ){
			
			timer.cancel();
			
			timer = null;
			
			if ( dm_listener_added ){
				
				azureus_core.getPluginManager().getDefaultPluginInterface().getDownloadManager().getGlobalDownloadEventNotifier().removeListener( this );
			}
			
			apply_history.clear();
		}
	}
	
	private void
	checkFreqLimUpdates()
	{
		dispatcher.dispatch(
			new AERunnable() 
			{
				public void 
				runSupport() 
				{
					synchronized( freq_lim_pending ){
						
						for ( Map.Entry<DownloadManager,List<TagConstraint>> entry: freq_lim_pending.entrySet()){
					
							for ( TagConstraint con: entry.getValue()){
							
								con.apply( entry.getKey());
							}
						}
						
						freq_lim_pending.clear();
					}
				}
			});
	}
	
	public void
	stateChanged(
		Download		download,
		int				old_state,
		int				new_state )
	{
		List<TagConstraint>	interesting = new ArrayList<>();
		
		synchronized( constrained_tags ){

			if ( !initialised ){
				
				return;
			}
			
			for ( TagConstraint tc: constrained_tags.values()){
				
				if ( tc.dependOnDownloadState()){
					
					interesting.add( tc );
				}
			}
		}
		
		if ( interesting.size() > 0 ){
			
			DownloadManager dm = PluginCoreUtils.unwrap( download );
			
			synchronized( freq_lim_pending ){
				
				freq_lim_pending.put( dm, interesting );
			}
		
			freq_lim_dispatcher.dispatch();
		}
	}
 
	public void
	positionChanged(
		Download	download, 
		int 		oldPosition,
		int 		newPosition )
	{
	}
	
	public void
	tagRemoved(
		Tag			tag )
	{
		synchronized( constrained_tags ){
			
			if ( constrained_tags.containsKey( tag )){
				
				constrained_tags.remove( tag );
				
				checkTimer();
			}
		}
	}
	
	private boolean
	isStopping()
	{
		return( stopping );
	}
	
	private void
	handleProperty(
		TagProperty		property )
	{
		Tag	tag = property.getTag();
				
		synchronized( constrained_tags ){
		
			String[] value = property.getStringList();
			
			String 	constraint;
			String	options;
			
			if ( value == null ){
				
				constraint 	= "";
				options		= "";
				
			}else{
		
				constraint 	= value.length>0&&value[0]!=null?value[0].trim():"";
				options		= value.length>1&&value[1]!=null?value[1].trim():"";				
			}
						
			if ( constraint.length() == 0 ){

				constrained_tags.remove( tag );
			}else{
				
				TagConstraint con = constrained_tags.get( tag );
				
				if ( con != null && con.getConstraint().equals( constraint ) && con.getOptions().equals( options )){
					
					return;
				}
				
				con = new TagConstraint( this, tag, constraint, options );
				
				constrained_tags.put( tag, con );
								
				if ( initialised ){
				
					apply( con );
				}
			}
			
			checkTimer();
		}
	}
	
	private void
	apply(
		final DownloadManager				dm,
		Tag									related_tag,
		boolean								auto )
	{
		if ( dm.isDestroyed()){
			
			return;
		}
		
		synchronized( constrained_tags ){
			
			if ( constrained_tags.size() == 0 || !initialised ){
				
				return;
			}
			
			if ( auto && !initial_assignment_complete ){
				
				return;
			}
		}
				
		dispatcher.dispatch(
			new AERunnable() 
			{
				public void 
				runSupport() 
				{
					List<TagConstraint>	cons;
					
					synchronized( constrained_tags ){
					
						cons = new ArrayList<>(constrained_tags.values());
					}
					
					for ( TagConstraint con: cons ){
							
						con.apply( dm );
					}
				}
			});
	}
	
	private void
	apply(
		final List<DownloadManager>		dms,
		final boolean					initial_assignment )
	{
		synchronized( constrained_tags ){
			
			if ( constrained_tags.size() == 0 || !initialised ){
				
				return;
			}
		}
		
		dispatcher.dispatch(
			new AERunnable() 
			{
				public void 
				runSupport() 
				{
					List<TagConstraint>	cons;
					
					synchronized( constrained_tags ){
					
						cons = new ArrayList<>(constrained_tags.values());
					}
						
						// set up initial constraint tagged state without following implications
					
					for ( TagConstraint con: cons ){
						
						con.apply( dms );
					}
						
					if ( initial_assignment ){
						
						synchronized( constrained_tags ){
						
							initial_assignment_complete = true;
						}
					
							// go over them one more time to pick up consequential constraints
						
						for ( TagConstraint con: cons ){
							
							con.apply( dms );
						}
					}
				}
			});
	}
	
	private void
	apply(
		final TagConstraint		constraint )
	{
		synchronized( constrained_tags ){
			
			if ( !initialised ){
				
				return;
			}
		}
		
		dispatcher.dispatch(
			new AERunnable() 
			{
				public void 
				runSupport() 
				{
					List<DownloadManager> dms = azureus_core.getGlobalManager().getDownloadManagers();

					constraint.apply( dms );
				}
			});
	}
	
	private void
	apply()
	{
		synchronized( constrained_tags ){
			
			if ( constrained_tags.size() == 0 || !initialised ){
				
				return;
			}
		}
		
		dispatcher.dispatch(
			new AERunnable() 
			{
				public void 
				runSupport() 
				{
					List<DownloadManager> dms = azureus_core.getGlobalManager().getDownloadManagers();
					
					List<TagConstraint>	cons;
					
					synchronized( constrained_tags ){
					
						cons = new ArrayList<>(constrained_tags.values());
					}
					
					for ( TagConstraint con: cons ){
						
						con.apply( dms );
					}
				}
			});
	}
	
	private TagConstraint.ConstraintExpr
	compileConstraint(
		String		expr )
	{
		return( new TagConstraint( this, null, expr, null ).expr );
	}
	
	private static class
	TagConstraint
	{
		private final TagPropertyConstraintHandler	handler;
		private final Tag							tag;
		private final String						constraint;
		
		private final boolean		auto_add;
		private final boolean		auto_remove;
		
		private final ConstraintExpr	expr;
		
		private boolean	depends_on_download_state;
		
		private
		TagConstraint(
			TagPropertyConstraintHandler	_handler,
			Tag								_tag,
			String							_constraint,
			String							options )
		{
			handler		= _handler;
			tag			= _tag;
			constraint	= _constraint;
		
			if ( options == null ){
				
				auto_add	= true;
				auto_remove	= true;
				
			}else{
					// 0 = add+remove; 1 = add only; 2 = remove only
				
				auto_add 	= !options.contains( "am=2;" );
				auto_remove = !options.contains( "am=1;" );
			}
			
			ConstraintExpr compiled_expr = null;
			
			try{
				compiled_expr = compileStart( constraint, new HashMap<>());
				
			}catch( Throwable e ){
				
				Debug.out( "Invalid constraint: " + constraint + " - " + Debug.getNestedExceptionMessage( e ));
				
			}finally{
				
				expr = compiled_expr;
			}
		}
		
		private boolean
		dependOnDownloadState()
		{
			return( depends_on_download_state );
		}

		private ConstraintExpr
		compileStart(
			String						str,
			Map<String,ConstraintExpr>	context )
		{		
			str = str.trim();
			
			if ( str.equalsIgnoreCase( "true" )){
				
				return( new ConstraintExprTrue());
			}
			
			char[] chars = str.toCharArray();
				
			boolean	in_quote 	= false;
				
			int	level 			= 0;
			int	bracket_start 	= 0;
			
			StringBuilder result = new StringBuilder( str.length());
			
			for ( int i=0;i<chars.length;i++){
					
				char c = chars[i];
																	
				if ( c == '"' ){
	
					if ( i == 0 || chars[i-1] != '\\' ){
						
						in_quote = !in_quote;
					}
				}
				
				if ( !in_quote ){
					
					if ( c == '(' ){
						
						level++;
						
						if ( level == 1 ){
							
							bracket_start = i+1;
						}
					}else if ( c == ')' ){
						
						level--;
						
						if ( level == 0 ){
						
							String bracket_text = new String( chars, bracket_start, i-bracket_start ).trim();
							
							if ( result.length() > 0 && Character.isLetterOrDigit( result.charAt( result.length()-1 ))){
								
									// function call
								
								String key = "{" + context.size() + "}";
								
								context.put( key, new ConstraintExprParams( bracket_text ));
																
								result.append( "(" ).append( key ).append( ")" );
								
							}else{
								
								ConstraintExpr sub_expr = compileStart( bracket_text, context );
								
								String key = "{" + context.size() + "}";
								
								context.put(key, sub_expr );
								
								result.append( key );
							}
						}
					}else if ( level == 0 ){
						
						if ( !Character.isWhitespace( c )){
						
							result.append( c );
						}
					}
				}else if ( level == 0 ){
						
					result.append( c );
					
				}
			}
			
			if ( level != 0 ){
				
				throw( new RuntimeException( "Unmatched '(' in \"" + str + "\"" ));
			}
			
			if ( in_quote ){
				
				throw( new RuntimeException( "Unmatched '\"' in \"" + str + "\"" ));
			}
			
			return( compileBasic( result.toString(), context ));
		}
		
		private ConstraintExpr
		compileBasic(
			String						str,
			Map<String,ConstraintExpr>	context )
		{	
			if ( str.startsWith( "{" )){
				
				return( context.get( str ));
				
			}else if ( str.contains( "||" )){
				
				String[] bits = str.split( "\\|\\|" );
				
				return( new ConstraintExprOr( compile( bits, context )));
				
			}else if ( str.contains( "&&" )){
				
				String[] bits = str.split( "&&" );
				
				return( new ConstraintExprAnd( compile( bits, context )));
				
			}else if ( str.contains( "^" )){
				
				String[] bits = str.split( "\\^" );
				
				return( new ConstraintExprXor( compile( bits, context )));
				
			}else if ( str.startsWith( "!" )){
				
				return( new ConstraintExprNot( compileBasic( str.substring(1).trim(), context )));
				
			}else{
				
				int	pos = str.indexOf( '(' );
				
				if ( pos > 0 && str.endsWith( ")" )){
					
					String func = str.substring( 0, pos );
					
					String key = str.substring( pos+1, str.length() - 1 ).trim();
					
					ConstraintExprParams params = (ConstraintExprParams)context.get( key );
										
					return( new ConstraintExprFunction( func, params ));

				}else{
					
					throw( new RuntimeException( "Unsupported construct: " + str ));
				}
			}
		}
		
		private ConstraintExpr[]
		compile(
			String[]					bits,
			Map<String,ConstraintExpr>	context )
		{
			ConstraintExpr[] res = new ConstraintExpr[ bits.length ];
			
			for ( int i=0; i<bits.length;i++){
				
				res[i] = compileBasic( bits[i].trim(), context );
			}
			
			return( res );
		}
		
		private String
		getConstraint()
		{
			return( constraint );
		}
		
		private String
		getOptions()
		{
			if ( auto_add ){
				return( "am=1;" );
			}else if ( auto_remove ){
				return( "am=2;" );
			}else{
				return( "am=0;" );
			}
		}
		
		private void
		apply(
			DownloadManager			dm )
		{
			if ( dm.isDestroyed() || !dm.isPersistent()){
				
				return;
			}

			if ( expr == null ){
				
				return;
			}
			
			if ( handler.isStopping()){
				
				return;
			}
			
			if ( !canProcess( this, dm )){
				
				return;
			}
			
			Set<Taggable>	existing = tag.getTagged();
						
			if ( testConstraint( dm )){
				
				if ( auto_add ){
					
					if ( !existing.contains( dm )){
						
						if( canAddTaggable( dm )){
						
							if ( handler.isStopping()){
								
								return;
							}
							
							tag.addTaggable( dm );
						}
					}
				}
			}else{
				
				if ( auto_remove ){
				
					if ( existing.contains( dm )){
					
						if ( handler.isStopping()){
							
							return;
						}
						
						tag.removeTaggable( dm );
					}
				}
			}
		}
		
		private void
		apply(
			List<DownloadManager>	dms )
		{
			if ( expr == null ){
				
				return;
			}

			if ( handler.isStopping()){
				
				return;
			}
			
			if ( !canProcess( this, dms )){
				
				return;
			}
			
			Set<Taggable>	existing = tag.getTagged();
			
			for ( DownloadManager dm: dms ){
			
				if ( dm.isDestroyed() || !dm.isPersistent()){
					
					continue;
				}
				
				if ( testConstraint( dm )){
					
					if ( auto_add ){
						
						if ( !existing.contains( dm )){
							
							if ( canAddTaggable( dm )){
							
								if ( handler.isStopping()){
									
									return;
								}
								
								tag.addTaggable( dm );
							}
						}
					}
				}else{
					
					if ( auto_remove ){
						
						if ( existing.contains( dm )){
							
							if ( handler.isStopping()){
								
								return;
							}
							
							tag.removeTaggable( dm );
						}
					}
				}
			}
		}
		

		
		private boolean
		canAddTaggable(
			DownloadManager		dm )
		{
			long	now = SystemTime.getMonotonousTime();
				
			Map<DownloadManager,Long> recent_dms = handler.apply_history.get( tag );
				
			if ( recent_dms != null ){
					
				Long time = recent_dms.get( dm );
					
				if ( time != null && now - time < 1000 ){
					
					System.out.println( "Not applying constraint as too recently actioned: " + dm.getDisplayName() + "/" + tag.getTagName( true ));

					return( false );
				}
			}
			
			if ( recent_dms == null ){
					
				recent_dms = new HashMap<>();
					
				handler.apply_history.put( tag, recent_dms );
			}
				
			recent_dms.put( dm, now );
			
			return( true );
		}
		
		private boolean
		testConstraint(
			DownloadManager	dm )
		{
			List<Tag> dm_tags = handler.tag_manager.getTagsForTaggable( dm );
			
			return( expr.eval( dm, dm_tags ));
		}
	
		private interface
		ConstraintExpr
		{
			boolean
			eval(
					DownloadManager dm,
					List<Tag> tags);
			
			String
			getString();
		}
		
		private static class
		ConstraintExprTrue
			implements ConstraintExpr
		{
			public boolean
			eval(
				DownloadManager		dm,
				List<Tag>			tags )
			{
				return( true );
			}
			
			public String
			getString()
			{
				return( "true" );
			}
		}
		
		private static class
		ConstraintExprParams
			implements  ConstraintExpr
		{
			private final String	value;
			
			private
			ConstraintExprParams(
				String	_value )
			{
				value = _value.trim();
			}
			
			public boolean
			eval(
				DownloadManager		dm,
				List<Tag>			tags )
			{
				return( false );
			}
			
			public Object[]
			getValues()
			{
				if ( value.length() == 0 ){
					
					return( new String[0]);
					
				}else if ( !value.contains( "," )){
				
					return( new Object[]{ value });
					
				}else{
					
					char[]	chars = value.toCharArray();
					
					boolean in_quote = false;
					
					List<String>	params = new ArrayList<>(16);
					
					StringBuilder current_param = new StringBuilder( value.length());
					
					for (int i=0;i<chars.length;i++){
					
						char c = chars[i];
						
						if ( c == '"' ){
							
							if ( i == 0 || chars[i-1] != '\\' ){
								
								in_quote = !in_quote;
							}
						}
						
						if ( c == ',' && !in_quote ){
							
							params.add( current_param.toString());
							
							current_param.setLength( 0 );
							
						}else{
							
							if ( in_quote || !Character.isWhitespace( c )){
							
								current_param.append( c );
							}
						}
					}
					
					params.add( current_param.toString());
					
					return( params.toArray(new Object[0]));
				}
			}
			
			public String
			getString()
			{
				return( value );
			}
		}
		
		private static class
		ConstraintExprNot
			implements  ConstraintExpr
		{
			private final ConstraintExpr expr;
			
			private
			ConstraintExprNot(
				ConstraintExpr	e )
			{
				expr = e;
			}
			
			public boolean
			eval(
				DownloadManager		dm,
				List<Tag>			tags )		
			{
				return( !expr.eval( dm, tags ));
			}
			
			public String
			getString()
			{
				return( "!(" + expr.getString() + ")");
			}
		}
		
		private static class
		ConstraintExprOr
			implements  ConstraintExpr
		{
			private final ConstraintExpr[]	exprs;
			
			private
			ConstraintExprOr(
				ConstraintExpr[]	_exprs )
			{
				exprs = _exprs;
			}
			
			public boolean
			eval(
				DownloadManager		dm,
				List<Tag>			tags )		
			{
				for ( ConstraintExpr expr: exprs ){
					
					if ( expr.eval( dm, tags )){
						
						return( true );
					}
				}
				
				return( false );
			}
			
			public String
			getString()
			{
				String res = "";
				
				for ( int i=0;i<exprs.length;i++){
					
					res += (i==0?"":"||") + exprs[i].getString();
				}
				
				return( "(" + res + ")" );
			}
		}
		
		private static class
		ConstraintExprAnd
			implements  ConstraintExpr
		{
			private final ConstraintExpr[]	exprs;
			
			private
			ConstraintExprAnd(
				ConstraintExpr[]	_exprs )
			{
				exprs = _exprs;
			}
			
			public boolean
			eval(
				DownloadManager		dm,
				List<Tag>			tags )
			{
				for ( ConstraintExpr expr: exprs ){
					
					if ( !expr.eval( dm, tags )){
						
						return( false );
					}
				}
				
				return( true );
			}
			
			public String
			getString()
			{
				String res = "";
				
				for ( int i=0;i<exprs.length;i++){
					
					res += (i==0?"":"&&") + exprs[i].getString();
				}
				
				return( "(" + res + ")" );
			}
		}
		
		private static class
		ConstraintExprXor
			implements  ConstraintExpr
		{
			private final ConstraintExpr[]	exprs;
			
			private
			ConstraintExprXor(
				ConstraintExpr[]	_exprs )
			{
				exprs = _exprs;
				
				if ( exprs.length < 2 ){
					
					throw( new RuntimeException( "Two or more arguments required for ^" ));
				}
			}
			
			public boolean
			eval(
				DownloadManager		dm,
				List<Tag>			tags )
			{
				boolean res = exprs[0].eval( dm, tags );
				
				for ( int i=1;i<exprs.length;i++){
					
					res = res ^ exprs[i].eval( dm, tags );
				}
				
				return( res );
			}
			
			public String
			getString()
			{
				String res = "";
				
				for ( int i=0;i<exprs.length;i++){
					
					res += (i==0?"":"^") + exprs[i].getString();
				}
				
				return( "(" + res + ")" );
			}
		}
		
		private static final int FT_HAS_TAG		= 1;
		private static final int FT_IS_PRIVATE	= 2;
		
		private static final int FT_GE			= 3;
		private static final int FT_GT			= 4;
		private static final int FT_LE			= 5;
		private static final int FT_LT			= 6;
		private static final int FT_EQ			= 7;
		private static final int FT_NEQ			= 8;
		
		private static final int FT_CONTAINS	= 9;
		private static final int FT_MATCHES		= 10;
		
		private static final int FT_HAS_NET			= 11;
		private static final int FT_IS_COMPLETE		= 12;
		private static final int FT_CAN_ARCHIVE		= 13;
		private static final int FT_IS_FORCE_START	= 14;
		private static final int FT_JAVASCRIPT		= 15;
		private static final int FT_IS_CHECKING		= 16;
		private static final int FT_IS_STOPPED		= 17;
		private static final int FT_IS_PAUSED		= 18;
		private static final int FT_IS_ERROR		= 19;

		
		static final Map<String,Integer>	keyword_map = new HashMap<>();
		
		private static final int	KW_SHARE_RATIO		= 0;
		private static final int	KW_AGE 				= 1;
		private static final int	KW_PERCENT 			= 2;
		private static final int	KW_DOWNLOADING_FOR 	= 3;
		private static final int	KW_SEEDING_FOR 		= 4;
		private static final int	KW_SWARM_MERGE 		= 5;
		private static final int	KW_LAST_ACTIVE 		= 6;
		private static final int	KW_SEED_COUNT 		= 7;
		private static final int	KW_PEER_COUNT 		= 8;
		private static final int	KW_SEED_PEER_RATIO 	= 9;
		private static final int	KW_RESUME_IN 		= 10;
		private static final int	KW_MIN_OF_HOUR 		= 11;
		private static final int	KW_HOUR_OF_DAY 		= 12;
		private static final int	KW_DAY_OF_WEEK 		= 13;
		private static final int	KW_TAG_AGE 			= 14;
		
		static{
			keyword_map.put( "shareratio", KW_SHARE_RATIO );
			keyword_map.put( "share_ratio", KW_SHARE_RATIO );
			keyword_map.put( "age", KW_AGE );
			keyword_map.put( "percent", KW_PERCENT );
			keyword_map.put( "downloadingfor", KW_DOWNLOADING_FOR );
			keyword_map.put( "downloading_for", KW_DOWNLOADING_FOR );
			keyword_map.put( "seedingfor", KW_SEEDING_FOR );
			keyword_map.put( "seeding_for", KW_SEEDING_FOR );
			keyword_map.put( "swarmmergebytes", KW_SWARM_MERGE );
			keyword_map.put( "swarm_merge_bytes", KW_SWARM_MERGE );
			keyword_map.put( "lastactive", KW_LAST_ACTIVE );
			keyword_map.put( "last_active", KW_LAST_ACTIVE );
			keyword_map.put( "seedcount", KW_SEED_COUNT );
			keyword_map.put( "seed_count", KW_SEED_COUNT );
			keyword_map.put( "peercount", KW_PEER_COUNT );
			keyword_map.put( "peer_count", KW_PEER_COUNT );
			keyword_map.put( "seedpeerratio", KW_SEED_PEER_RATIO );
			keyword_map.put( "seed_peer_ratio", KW_SEED_PEER_RATIO );
			keyword_map.put( "resumein", KW_RESUME_IN );
			keyword_map.put( "resume_in", KW_RESUME_IN );
			
			keyword_map.put( "minofhour", KW_MIN_OF_HOUR );
			keyword_map.put( "min_of_hour", KW_MIN_OF_HOUR );
			keyword_map.put( "hourofday", KW_HOUR_OF_DAY );
			keyword_map.put( "hour_of_day", KW_HOUR_OF_DAY );
			keyword_map.put( "dayofweek", KW_DAY_OF_WEEK );
			keyword_map.put( "day_of_week", KW_DAY_OF_WEEK );
			keyword_map.put( "tagage", KW_TAG_AGE );
			keyword_map.put( "tag_age", KW_TAG_AGE );

		}
		
		private class
		ConstraintExprFunction
			implements  ConstraintExpr
		{
			
			private	final String 				func_name;
			private final ConstraintExprParams	params_expr;
			private final Object[]				params;
			
			private final int	fn_type;
			
			private
			ConstraintExprFunction(
				String 					_func_name,
				ConstraintExprParams	_params )
			{
				func_name	= _func_name;
				params_expr	= _params;
				
				params		= _params.getValues();
				
				boolean	params_ok = false;

				switch (func_name) {
					case "hasTag":

						fn_type = FT_HAS_TAG;

						params_ok = params.length == 1 && getStringLiteral(params, 0);

						break;
					case "hasNet":

						fn_type = FT_HAS_NET;

						params_ok = params.length == 1 && getStringLiteral(params, 0);

						if (params_ok) {

							params[0] = AENetworkClassifier.internalise((String) params[0]);

							params_ok = params[0] != null;
						}
						break;
					case "isPrivate":

						fn_type = FT_IS_PRIVATE;

						params_ok = params.length == 0;

						break;
					case "isForceStart":

						fn_type = FT_IS_FORCE_START;

						depends_on_download_state = true;

						params_ok = params.length == 0;

						break;
					case "isChecking":

						fn_type = FT_IS_CHECKING;

						depends_on_download_state = true;

						params_ok = params.length == 0;

						break;
					case "isComplete":

						fn_type = FT_IS_COMPLETE;

						depends_on_download_state = true;

						params_ok = params.length == 0;

						break;
					case "isStopped":

						fn_type = FT_IS_STOPPED;

						depends_on_download_state = true;

						params_ok = params.length == 0;

						break;
					case "isError":

						fn_type = FT_IS_ERROR;

						depends_on_download_state = true;

						params_ok = params.length == 0;

						break;
					case "isPaused":

						fn_type = FT_IS_PAUSED;

						depends_on_download_state = true;

						params_ok = params.length == 0;

						break;
					case "canArchive":

						fn_type = FT_CAN_ARCHIVE;

						params_ok = params.length == 0;

						break;
					case "isGE":

						fn_type = FT_GE;

						params_ok = params.length == 2;

						break;
					case "isGT":

						fn_type = FT_GT;

						params_ok = params.length == 2;

						break;
					case "isLE":

						fn_type = FT_LE;

						params_ok = params.length == 2;

						break;
					case "isLT":

						fn_type = FT_LT;

						params_ok = params.length == 2;

						break;
					case "isEQ":

						fn_type = FT_EQ;

						params_ok = params.length == 2;

						break;
					case "isNEQ":

						fn_type = FT_NEQ;

						params_ok = params.length == 2;

						break;
					case "contains":

						fn_type = FT_CONTAINS;

						params_ok = params.length == 2;

						break;
					case "matches":

						fn_type = FT_MATCHES;

						params_ok = params.length == 2 && getStringLiteral(params, 1);

						break;
					case "javascript":

						fn_type = FT_JAVASCRIPT;

						params_ok = params.length == 1 && getStringLiteral(params, 0);

						depends_on_download_state = true;    // dunno so let's assume so


						break;
					default:

						throw (new RuntimeException("Unsupported function '" + func_name + "'"));
				}
				
				if ( !params_ok ){
					
					throw( new RuntimeException( "Invalid parameters for function '" + func_name + "': " + params_expr.getString()));
	
				}
			}
					
			public boolean
			eval(
				DownloadManager		dm,
				List<Tag>			tags )
			{
				switch( fn_type ){
					case FT_HAS_TAG:{
					
						String tag_name = (String)params[0];
						
						for ( Tag t: tags ){
							
							if ( t.getTagName( true ).equals( tag_name )){
								
								return( true );
							}
						}
						
						return( false );
					}
					case FT_HAS_NET:{
						
						String net_name = (String)params[0];
						
						if ( net_name != null ){
							
							String[] nets = dm.getDownloadState().getNetworks();
							
							if ( nets != null ){
								
								for ( String net: nets ){
									
									if (Objects.equals(net,net_name)){
										
										return( true );
									}
								}
							}
						}
						
						return( false );
					}
					case FT_IS_PRIVATE:{
					
						TOTorrent t = dm.getTorrent();
					
						return( t != null && t.getPrivate());
					}
					case FT_IS_FORCE_START:{
						
						return( dm.isForceStart());
					}
					case FT_IS_CHECKING:{
						
						int state = dm.getState();
						
						if ( state == DownloadManager.STATE_CHECKING ){
							
							return( true );
							
						}else if ( state == DownloadManager.STATE_SEEDING ){
							
							DiskManager disk_manager = dm.getDiskManager();

							if ( disk_manager != null ){
							
								return( disk_manager.getCompleteRecheckStatus() != -1 );
							}
						}
						
						return( false );
					}
					case FT_IS_COMPLETE:{
						
						return( dm.isDownloadComplete( false ));
					}
					case FT_IS_STOPPED:{
						
						int state = dm.getState();
						
						return( state == DownloadManager.STATE_STOPPED && !dm.isPaused());
					}
					case FT_IS_ERROR:{
						
						int state = dm.getState();
						
						return( state == DownloadManager.STATE_ERROR );
					}
					case FT_IS_PAUSED:{
						
						return( dm.isPaused());
					}
					case FT_CAN_ARCHIVE:{
						
						Download dl = PluginCoreUtils.wrap( dm );
						
						return( dl != null && dl.canStubbify());
					}
					case FT_GE:
					case FT_GT:
					case FT_LE:
					case FT_LT:
					case FT_EQ:
					case FT_NEQ:{
									
						Number n1 = getNumeric( dm, params, 0 );
						Number n2 = getNumeric( dm, params, 1 );
					
						switch( fn_type ){
						
							case FT_GE:
								return( n1.doubleValue() >= n2.doubleValue());
							case FT_GT:
								return( n1.doubleValue() > n2.doubleValue());
							case FT_LE:
								return( n1.doubleValue() <= n2.doubleValue());
							case FT_LT:
								return( n1.doubleValue() < n2.doubleValue());
							case FT_EQ:
								return( n1.doubleValue() == n2.doubleValue());
							case FT_NEQ:
								return( n1.doubleValue() != n2.doubleValue());
						}
						
						return( false );
					}
					case FT_CONTAINS:{
						
						String	s1 = getString( dm, params, 0 );
						String	s2 = getString( dm, params, 1 );
						
						return( s1.contains( s2 ));
					}
					case FT_MATCHES:{
						
						String	s1 = getString( dm, params, 0 );
						
						if ( params[1] == null ){
							
							return( false );
							
						}else if ( params[1] instanceof Pattern ){
							
							return(((Pattern)params[1]).matcher( s1 ).find());
							
						}else{
							
							try{
								Pattern p = Pattern.compile((String)params[1], Pattern.CASE_INSENSITIVE );
								
								params[1] = p;
								
								return( p.matcher( s1 ).find());
								
							}catch( Throwable e ){
								
								Debug.out( "Invalid constraint pattern: " + params[1] );
								
								params[1] = null;
							}
						}
						
						return( false );
					}
					case FT_JAVASCRIPT:{
												
						Object result =
							handler.tag_manager.evalScript( 
								tag, 
								"javascript( " + params[0] + ")",
								dm,
								"inTag" );
						
						if ( result instanceof Boolean ){
							
							return((Boolean)result);
						}
						
						return( false );
					}
				}
				
				return( false );
			}
			
			private boolean
			getStringLiteral(
				Object[]	args,
				int			index )
			{
				Object _arg = args[index];
				
				if ( _arg instanceof String ){
					
					String arg = (String)_arg;
				
					if ( arg.startsWith( "\"" ) && arg.endsWith( "\"" )){
						
						args[index] = arg.substring( 1, arg.length() - 1 );
						
						return( true );
					}
				}
					
				return( false );
			}
			
			private String
			getString(
				DownloadManager		dm,
				Object[]			args,
				int					index )
			{
				String str = (String)args[index];
				
				if ( str.startsWith( "\"" ) && str.endsWith( "\"" )){
					
					return( str.substring( 1, str.length() - 1 ));
					
				}else if ( str.equals( "name" )){
					
					return( dm.getDisplayName());
					
				}else{
					
					Debug.out( "Invalid constraint string: " + str );
					
					String result = "\"\"";
					
					args[index] = result;
					
					return( result );
				}
			}
	
			private Number
			getNumeric(
				DownloadManager		dm,
				Object[]			args,
				int					index )
			{
				Object arg = args[index];
				
				if ( arg instanceof Number ){
					
					return((Number)arg);
				}
				
				String str = (String)arg;
				
				Number result = 0;
				
				try{
					if ( Character.isDigit( str.charAt(0))){
					
						if ( str.contains( "." )){
							
							result = Float.parseFloat( str );
							
						}else{
							
							result = Long.parseLong( str );
						}
						
						return( result );
					}else{
						
						Integer kw = keyword_map.get( str.toLowerCase( Locale.US ));
						
						if ( kw == null ){
							
							Debug.out( "Invalid constraint keyword: " + str );
							
							return( result );
						}
						
						switch( kw ){
							case KW_SHARE_RATIO:{
								result = null;	// don't cache this!
								
								int sr = dm.getStats().getShareRatio();
								
								if ( sr == -1 ){
									
									return( Integer.MAX_VALUE );
									
								}else{
									
									return(sr / 1000.0f);
								}
							}
							case KW_PERCENT:{
							
								result = null;	// don't cache this!
								
									// 0->1000
								
								int percent = dm.getStats().getPercentDoneExcludingDND();
			
								return(percent / 10.0f);
							}
							case KW_AGE:{
							
								result = null;	// don't cache this!
									
								long added = dm.getDownloadState().getLongParameter( DownloadManagerState.PARAM_DOWNLOAD_ADDED_TIME );
			
								if ( added <= 0 ){
									
									return( 0 );
								}
								
								return(( SystemTime.getCurrentTime() - added )/1000 );		// secs
							}
							case KW_DOWNLOADING_FOR:{
							
								result = null;	// don't cache this!
								
								return( dm.getStats().getSecondsDownloading());
							}
							case KW_SEEDING_FOR:{
								
								result = null;	// don't cache this!
								
								return( dm.getStats().getSecondsOnlySeeding());
							}
							case KW_LAST_ACTIVE:{
								
								result = null;	// don't cache this!
								
								DownloadManagerState dms = dm.getDownloadState();
																			
								long	timestamp = dms.getLongAttribute( DownloadManagerState.AT_LAST_ADDED_TO_ACTIVE_TAG );
								
								if ( timestamp <= 0 ){
									
									return( Long.MAX_VALUE );
								}
								
								return(( SystemTime.getCurrentTime() - timestamp )/1000 );
							}
							case KW_RESUME_IN:{
								
								result = null;	// don't cache this!
								
								long resume_millis = dm.getAutoResumeTime();
								
								long	now = SystemTime.getCurrentTime();
								
								if ( resume_millis <= 0 || resume_millis <= now ){
									
									return( 0 );
								}
								
								return(( resume_millis - now )/1000 );
							}
							case KW_MIN_OF_HOUR:{
								
								result = null;	// don't cache this!
								
								long	now = SystemTime.getCurrentTime();
								
								GregorianCalendar cal = new GregorianCalendar();
								
								cal.setTime( new Date( now ));
								
								return( cal.get( Calendar.MINUTE ));
							}
							case KW_HOUR_OF_DAY:{
								
								result = null;	// don't cache this!
								
								long	now = SystemTime.getCurrentTime();
								
								GregorianCalendar cal = new GregorianCalendar();
								
								cal.setTime( new Date( now ));
								
								return( cal.get( Calendar.HOUR_OF_DAY ));
							}
							case KW_DAY_OF_WEEK:{
								
								result = null;	// don't cache this!
								
								long	now = SystemTime.getCurrentTime();
								
								GregorianCalendar cal = new GregorianCalendar();
								
								cal.setTime( new Date( now ));
								
								return( cal.get( Calendar.DAY_OF_WEEK ));
							}
							case KW_SWARM_MERGE:{
								
								result = null;	// don't cache this!
								
								return( dm.getDownloadState().getLongAttribute( DownloadManagerState.AT_MERGED_DATA ));
							}
							case KW_SEED_COUNT:{
								
								result = null;	// don't cache this!

								TRTrackerScraperResponse response = dm.getTrackerScrapeResponse();
								
								int	seeds = dm.getNbSeeds();
								
								if ( response != null && response.isValid()){
									
									seeds = Math.max( seeds, response.getSeeds());
								}
								
								return( Math.max( 0, seeds ));
							}
							case KW_PEER_COUNT:{
								
								result = null;	// don't cache this!

								TRTrackerScraperResponse response = dm.getTrackerScrapeResponse();
								
								int	peers = dm.getNbSeeds();
								
								if ( response != null && response.isValid()){
									
									peers = Math.max( peers, response.getPeers());
								}
								
								return( Math.max( 0, peers ));
							}
							case KW_SEED_PEER_RATIO:{
								
								result = null;	// don't cache this!

								TRTrackerScraperResponse response = dm.getTrackerScrapeResponse();
								
								int	seeds = dm.getNbSeeds();
								int	peers = dm.getNbSeeds();

								if ( response != null && response.isValid()){
									
									seeds = Math.max( seeds, response.getSeeds());
									peers = Math.max( peers, response.getPeers());
								}
								
								float ratio;
								
								if ( peers < 0 || seeds < 0 ){
									
									ratio = 0;
									
								}else{
									
									if ( peers == 0 ){
										
										if ( seeds == 0 ){
											
											ratio = 0;
											
										}else{
											
											ratio = Float.MAX_VALUE;
										}
									}else{
										
										ratio = (float)seeds/peers;
									}
								}
								
								return( ratio );
							}
							case KW_TAG_AGE:{
								
								result = null;	// don't cache this!
									
								long tag_added = tag.getTaggableAddedTime( dm );
								
								if ( tag_added <= 0 ){
									
									return( 0 );
								}
								
								long age = (( SystemTime.getCurrentTime() - tag_added )/1000 );		// secs
								
								if ( age < 0 ){
									
									age = 0;
								}
								
								return( age );
							}

							default:{
							
								Debug.out( "Invalid constraint keyword: " + str );
							
								return( result );
							}
						}
					}
				}catch( Throwable e){
					
					Debug.out( "Invalid constraint numeric: " + str );
	
					return( result );
					
				}finally{
					
					if ( result != null ){
						
							// cache literal results 
						
						args[index] = result;
					}
				}
			}
			
			public String
			getString()
			{
				return( func_name + "(" + params_expr.getString() + ")" );
			}
		}
	}
	
	public static void
	main(
		String[]	args )
	{
		TagPropertyConstraintHandler handler = new TagPropertyConstraintHandler();
		
		//System.out.println( handler.compileConstraint( "!(hasTag(\"bil\") && (hasTag( \"fred\" ))) || hasTag(\"toot\")" ).getString());
		System.out.println( handler.compileConstraint( "isGE( shareratio, 1.5)" ).getString());
	}
}
