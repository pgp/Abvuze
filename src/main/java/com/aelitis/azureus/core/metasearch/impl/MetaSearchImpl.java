/*
 * Created on May 6, 2008
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


package com.aelitis.azureus.core.metasearch.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.gudy.azureus2.core3.internat.MessageText;
import org.gudy.azureus2.core3.util.AERunnable;
import org.gudy.azureus2.core3.util.AsyncDispatcher;
import org.gudy.azureus2.core3.util.BDecoder;
import org.gudy.azureus2.core3.util.Base32;
import org.gudy.azureus2.core3.util.Constants;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.DelayedEvent;
import org.gudy.azureus2.core3.util.FileUtil;
import org.gudy.azureus2.core3.util.IndentWriter;
import org.gudy.azureus2.core3.util.RandomUtils;
import org.gudy.azureus2.core3.util.SimpleTimer;
import org.gudy.azureus2.core3.util.SystemTime;
import org.gudy.azureus2.core3.util.TimerEvent;
import org.gudy.azureus2.core3.util.TimerEventPerformer;
import org.gudy.azureus2.core3.util.TimerEventPeriodic;
import org.gudy.azureus2.plugins.utils.StaticUtilities;
import org.gudy.azureus2.plugins.utils.resourcedownloader.ResourceDownloader;
import org.gudy.azureus2.plugins.utils.resourcedownloader.ResourceDownloaderFactory;
import org.gudy.azureus2.plugins.utils.search.SearchProvider;

import com.aelitis.azureus.core.messenger.config.PlatformMetaSearchMessenger;
import com.aelitis.azureus.core.metasearch.*;
import com.aelitis.azureus.core.metasearch.impl.plugin.PluginEngine;
import com.aelitis.azureus.core.metasearch.impl.web.WebEngine;
import com.aelitis.azureus.core.metasearch.impl.web.rss.RSSEngine;
import com.aelitis.azureus.core.util.CopyOnWriteList;
import com.aelitis.azureus.core.vuzefile.VuzeFile;
import com.aelitis.azureus.core.vuzefile.VuzeFileComponent;
import com.aelitis.azureus.core.vuzefile.VuzeFileHandler;

public class 
MetaSearchImpl
	implements MetaSearch
{
	private static final String	CONFIG_FILE = "metasearch.config";
		
	private MetaSearchManagerImpl	manager;
	
	private CopyOnWriteList<EngineImpl> engines 	= new CopyOnWriteList<>();
	private Map<String,Long>			plugin_map	= new HashMap<>();
	
	private boolean config_dirty;
	
	private CopyOnWriteList<MetaSearchListener> 	listeners 	= new CopyOnWriteList<>();
	
	private TimerEventPeriodic	update_check_timer;
	
	private static final int 	UPDATE_CHECK_PERIOD		= 15*60*1000;
	private static final int	MIN_UPDATE_CHECK_SECS	= 10*60;
	
	private Object				MS_UPDATE_CONSEC_FAIL_KEY = new Object();
	
	private AsyncDispatcher		update_dispatcher = new AsyncDispatcher();
	
	protected 
	MetaSearchImpl(
		MetaSearchManagerImpl		_manager )
	{
		manager	= _manager;
		
		loadConfig();
	}
	
	public MetaSearchManagerImpl
	getManager()
	{
		return( manager );
	}
	
	public Engine
	importFromBEncodedMap(
		Map<String,Object>		map )
	
		throws IOException
	{
		return( EngineImpl.importFromBEncodedMap( this, map )); 
	}
	
	public Engine
	importFromJSONString(
		int			type,
		long		id,
		long		last_updated,
		float		rank_bias,
		String		name,
		String		content )
	
		throws IOException
	{
		return( EngineImpl.importFromJSONString( this, type, id, last_updated, rank_bias, name, content ));
	}
	
	public EngineImpl
	importFromPlugin(
		String				_pid,
		SearchProvider		provider )
	
		throws IOException
	{
		synchronized( this ){

				// unfortunately pid can be internationalised and thus musn't be used as a key to
				// a bencoded-map as it can lead of nastyness. Delete any existing entries that have
				// got out of control

            for (String s : plugin_map.keySet()) {

                if (s.length() > 1024) {

                    Debug.out("plugin_map corrupted, resetting");

                    plugin_map.clear();

                    break;
                }
            }
			
			String pid = Base32.encode( _pid.getBytes(StandardCharsets.UTF_8));
			
			Long	l_id = plugin_map.get( pid );
			
			long	id;
			
			if ( l_id == null ){
				
				id = manager.getLocalTemplateID();
						
				plugin_map.put( pid, id);
						
				configDirty();

			}else{
				
				id = l_id;
			}
			
			EngineImpl engine = (EngineImpl)getEngine( id );
			
			if ( engine == null ){
				
				engine = new PluginEngine( this, id, provider );
				
				engine.setSource( Engine.ENGINE_SOURCE_LOCAL );
				
				engine.setSelectionState( Engine.SEL_STATE_MANUAL_SELECTED );
				
				addEngine( engine );
				
			}else{
				
				if ( engine instanceof PluginEngine ){
					
					((PluginEngine)engine).setProvider( provider );
					
				}else{
					
					Debug.out( "Inconsistent: plugin must be a PluginEngine!" );
					
					plugin_map.remove( pid );
					
					removeEngine( engine );
					
					throw( new IOException( "Inconsistent" ));
				}
			}
			
			return( engine );
		}	
	}
	
	public SearchProvider
	resolveProvider(
		PluginEngine	for_engine )
	{
		List<EngineImpl> l = engines.getList();

		for ( EngineImpl e: l ){
			
			if ( e instanceof PluginEngine ){
				
				PluginEngine pe = (PluginEngine)e;
				
				SearchProvider provider = pe.getProvider();
				
				if ( provider != null ){
					
					if ( pe.getName().equals( for_engine.getName())){
						
						return( provider );
					}
				}
			}
		}
		
		return( null );
	}
	
	public Engine 
	createRSSEngine(
		String		name,
		URL 		url )
	
		throws MetaSearchException 
	{
		EngineImpl engine = 
			new RSSEngine( 
					this, 
					manager.getLocalTemplateID(), 
					SystemTime.getCurrentTime(), 
					1.0f,
					name, 
					url.toExternalForm(), 
					false,
					WebEngine.AM_TRANSPARENT,
					null,
					new String[0] );
		
		engine.setSource( Engine.ENGINE_SOURCE_RSS );
		
		addEngine( engine, false );
				
		log( "Created RSS engine '" + url + "'" );
		
		return( engine );
	}
	
	protected void
	enableUpdateChecks()
	{
		synchronized( this ){
			
			if ( update_check_timer == null ){
				
				update_check_timer = SimpleTimer.addPeriodicEvent(
						"MS:updater",
						UPDATE_CHECK_PERIOD,
						new TimerEventPerformer()
						{
							public void 
							perform(
								TimerEvent event) 
							{
								checkUpdates();
							}
						});
			}
		}
	}
	private void
	checkUpdates()
	{
		update_dispatcher.dispatch(
			new AERunnable()
			{
				public void 
				runSupport() 
				{
					checkUpdatesSupport();
				}
			});
	}
	
	private void
	checkUpdatesSupport()
	{

        for (EngineImpl engine : engines) {

            String update_url = engine.getUpdateURL();

            if (update_url != null) {

                long now = SystemTime.getCurrentTime();

                long last_check = engine.getLastUpdateCheck();

                if (last_check > now) {

                    last_check = now;

                    engine.setLastUpdateCheck(now);
                }

                long check_secs = engine.getUpdateCheckSecs();

                if (check_secs < MIN_UPDATE_CHECK_SECS) {

                    log("Engine '" + engine.getName() + "': Update check period too small (" + check_secs + " secs) adjusting to " + MIN_UPDATE_CHECK_SECS + ": " + engine.getName());

                    check_secs = MIN_UPDATE_CHECK_SECS;
                }

                long check_millis = check_secs * 1000;

                long next_check = last_check + check_millis;

                Object consec_fails_o = engine.getUserData(MS_UPDATE_CONSEC_FAIL_KEY);

                int consec_fails = consec_fails_o == null ? 0 : (Integer) consec_fails_o;

                if (consec_fails > 0) {

                    next_check += (UPDATE_CHECK_PERIOD << consec_fails);
                }

                if (next_check < now) {

                    if (updateEngine(engine)) {

                        consec_fails = 0;

                        engine.setLastUpdateCheck(now);

                    } else {

                        consec_fails++;

                        if (consec_fails > 3) {

                            consec_fails = 0;

                            // skip to next scheduled update time

                            engine.setLastUpdateCheck(now);
                        }
                    }

                    engine.setUserData(MS_UPDATE_CONSEC_FAIL_KEY, consec_fails == 0 ? null : consec_fails);
                }
            }
        }
	}
	
	protected boolean
	updateEngine(
		EngineImpl		engine )
	{
		String	update_url = engine.getUpdateURL();

		int	pos = update_url.indexOf('?');
		
		if ( pos == -1 ){
			
			update_url += "?";
			
		}else{
			
			update_url += "&";
		}
		
		update_url += 	"az_template_uid=" + engine.getUID() + 
						"&az_template_version=" + engine.getVersion() +
						"&az_version=" + Constants.AZUREUS_VERSION +
					    "&az_locale=" + MessageText.getCurrentLocale().toString() +
					    "&az_rand=" + RandomUtils.nextAbsoluteLong();
		
		log( "Engine " + engine.getName() + ": auto-update check via " + update_url );
		
		try{
			ResourceDownloaderFactory rdf = StaticUtilities.getResourceDownloaderFactory();
			
			ResourceDownloader url_rd = rdf.create( new URL( update_url ));
			
			ResourceDownloader rd = rdf.getMetaRefreshDownloader( url_rd );

            try (InputStream is = rd.download()) {
                Map<String, Object> map = BDecoder.decode(new BufferedInputStream(is));

                log("    update check reply: " + map);

                // reply is either "response" meaning "no update" and giving possibly changed update secs
                // or Vuze file with updated template

                Map<String, Object> response = (Map<String, Object>) map.get("response");

                if (response != null) {

                    Long update_secs = (Long) response.get("update_url_check_secs");

                    if (update_secs == null) {

                        engine.setLocalUpdateCheckSecs(0);

                    } else {

                        int check_secs = update_secs.intValue();

                        if (check_secs < MIN_UPDATE_CHECK_SECS) {

                            log("    update check secs for to small, min is " + MIN_UPDATE_CHECK_SECS);

                            check_secs = MIN_UPDATE_CHECK_SECS;
                        }

                        engine.setLocalUpdateCheckSecs(check_secs);
                    }

                    return (true);

                } else {

                    VuzeFile vf = VuzeFileHandler.getSingleton().loadVuzeFile(map);

                    if (vf == null) {

                        log("    failed to decode vuze file");

                        return (false);
                    }

                    Engine[] updated_engines = manager.loadFromVuzeFile(vf);

                    if (updated_engines.length > 0) {

                        String existing_uid = engine.getUID();

                        boolean found = false;

                        String engine_str = "";

                        for (int i = 0; i < updated_engines.length; i++) {

                            Engine updated_engine = updated_engines[i];

                            engine_str += (i == 0 ? "" : ",") + updated_engine.getName() + ": uid=" + updated_engine.getUID() + ",version=" + updated_engine.getVersion();

                            if (updated_engine.getUID().equals(existing_uid)) {

                                found = true;
                            }
                        }

                        if (!found) {

                            log("    existing engine not found in updated set, deleting");

                            engine.delete();

                        }

                        log("    update complete: new engines=" + engine_str);

                    } else {

                        log("    no engines found in vuze file");
                    }

                    return (true);
                }
            }
		}catch( Throwable e ){
			
			log( "    update check failed", e );
			
			return( false );
		}
	}
	
	public void 
	addEngine(
		Engine 	engine )
	{
		addEngine( (EngineImpl)engine, false );
	}
	
	public Engine 
	addEngine(
		long 		id )
	
		throws MetaSearchException 
	{
		try{

			PlatformMetaSearchMessenger.templateDetails details = PlatformMetaSearchMessenger.getTemplate( manager.getExtensionKey(), id );
		
			log( "Downloading definition of template " + id );
			log( details.getValue());
		
			if ( details.isVisible()){
			
				Engine engine = 
					importFromJSONString( 
						details.getType()==PlatformMetaSearchMessenger.templateDetails.ENGINE_TYPE_JSON?Engine.ENGINE_TYPE_JSON:Engine.ENGINE_TYPE_REGEX,
						details.getId(),
						details.getModifiedDate(),
						details.getRankBias(),
						details.getName(),
						details.getValue());
				
				engine.setSource( Engine.ENGINE_SOURCE_VUZE );
				engine.setSelectionState( Engine.SEL_STATE_DESELECTED );
				
				addEngine( engine );
				
				return( engine );
				
			}else{
				
				throw( new MetaSearchException( "Search template is not visible" ));
			}
		}catch( MetaSearchException e ){
			
			throw( e );
			
		}catch( Throwable e ){
		
			throw( new MetaSearchException( "Template load failed", e ));
		}	
	}
		
	public void 
	addEngine(
		EngineImpl 	new_engine,
		boolean		loading )
	{		
		boolean	add_op = true;
		
		synchronized( this ){
			
			Iterator<EngineImpl>	it = engines.iterator();
			
			while( it.hasNext()){
				
				Engine existing_engine = it.next();
				
				if ( existing_engine.getId() == new_engine.getId()){
					
					log( "Updating engine with same ID " + existing_engine.getId() + ": " + existing_engine.getName() + "/" + existing_engine.getUID());
					
					it.remove();
					
					new_engine.setUID( existing_engine.getUID());
					
					if ( existing_engine.sameLogicAs( new_engine )){
						
						new_engine.setVersion( existing_engine.getVersion());

					}else{
						
						new_engine.setVersion( existing_engine.getVersion() + 1 );
						
						log( "    new version=" + new_engine.getVersion());
					}
					 
					add_op = false;
					
				}else if ( existing_engine.getUID().equals( new_engine.getUID())){
					
					log( "Removing engine with same UID " + existing_engine.getUID() + "(" + existing_engine.getName() + ")" );
					
					it.remove();
				}
			}
			
			engines.add( new_engine );
		}
		
		if ( new_engine.getUpdateURL() != null ){
			
			enableUpdateChecks();
		}
		
		if ( !loading ){
			
			log( "Engine '" + new_engine.getName() + "' added" );
			
			saveConfig();

            for (MetaSearchListener listener : listeners) {

                try {
                    if (add_op) {

                        listener.engineAdded(new_engine);

                    } else {

                        listener.engineUpdated(new_engine);
                    }
                } catch (Throwable e) {

                    Debug.printStackTrace(e);
                }
            }
		}
	}
	
	public void 
	removeEngine(
		Engine 	engine )
	{
		if ( engines.remove((EngineImpl)engine )){
		
			log( "Engine '" + engine.getName() + "' removed" );
			
			saveConfig();

            for (MetaSearchListener listener : listeners) {

                try {

                    listener.engineRemoved(engine);

                } catch (Throwable e) {

                    Debug.printStackTrace(e);
                }
            }
		}
	}
	
	protected void
	stateChanged(
		Engine		engine )
	{

        for (MetaSearchListener listener : listeners) {

            try {
                listener.engineStateChanged(engine);

            } catch (Throwable e) {

                Debug.printStackTrace(e);
            }
        }
	}
	
	public String
	getFUD()
	{
		List<EngineImpl> l = engines.getList();
		
		List<Long>	ids = new ArrayList<>();
		
		for ( EngineImpl engine: l ){
			
			if ( engine.getSource() == Engine.ENGINE_SOURCE_VUZE ){
				
				ids.add( engine.getId());
			}
		}
		
		Collections.sort( ids );
		
		String	fud = "";

		for ( Long id: ids ){
		
			fud += (fud.length()==0?"":",") +id; 
		}
		
		return( fud );
	}
	
	protected void
	addPotentialAssociation(
		EngineImpl		engine,
		String			key )
	{
		manager.addPotentialAssociation(engine, key);	
	}
	
	public Engine[] 
	getEngines(
		boolean		active_only,
		boolean		ensure_up_to_date )
	{
		if ( ensure_up_to_date ){
			
			manager.ensureEnginesUpToDate();
		}
		
		List<EngineImpl> l = engines.getList();
				
		List<EngineImpl> result;
		
		if ( active_only ){
			
			result = new ArrayList<>();

            for (EngineImpl e : l) {

                if (e.isActive()) {

                    result.add(e);
                }
            }
		}else{
			
			result = l;
		}
		
		return result.toArray(new Engine[0]);
	}
	
	public Engine
	getEngine(
		long		id )
	{
		List<EngineImpl> l = engines.getList();

        for (Engine e : l) {

            if (e.getId() == id) {

                return (e);
            }
        }

		return( null );
	}
	
	public Engine
	getEngineByUID(
		String	uid )
	{
		List<EngineImpl> l = engines.getList();

        for (Engine e : l) {

            if (e.getUID().equals(uid)) {

                return (e);
            }
        }

		return( null );
	}
	
	public int
	getEngineCount()
	{
		return( engines.size());
	}
	
	public Engine[] 
	search(
		final ResultListener 	original_listener,
		SearchParameter[] 		searchParameters,
		String					headers,
		int						max_results_per_engine )
	{
		return( search( original_listener, searchParameters, headers, new HashMap<>(), max_results_per_engine ));
	}
	
	public Engine[] 
  	search(
  		final ResultListener 	original_listener,
  		SearchParameter[] 		searchParameters,
  		String					headers,
  		Map<String,String>		context,
  		int						max_results_per_engine )
  	{
  		return( search( null, original_listener, searchParameters, headers, context, max_results_per_engine ));
  	}
	
	public Engine[] 
	search(
		Engine[]				engines,
		final ResultListener 	listener,
		SearchParameter[] 		search_parameters,
		String					headers,
		final int				max_results_per_engine )
	{
		return( search( engines, listener, search_parameters, headers, new HashMap<>(), max_results_per_engine ));
	}
	
	public void
	enginePreferred(
		Engine		engine )
	{
		Engine[] engines = getEngines( true, false );
		
		int	num_other_preferred = 0;
		
		for ( Engine e: engines ){
			
			if ( e.getId() == engine.getId()){
				
				e.setPreferredDelta( +1 );
				
			}else{
				
				if ( e.getPreferredWeighting() > 0 ){
					
					num_other_preferred++;
				}
			}
		}
				
		if ( num_other_preferred > 0 ){
			
			float negative_weighting = -1.0f / num_other_preferred;
		
			for ( Engine e: engines ){
			
				if ( e.getId() != engine.getId() && e.getPreferredWeighting() > 0 ){
				
					e.setPreferredDelta( negative_weighting );
				}
			}
		}
	}
	
	public Engine[] 
  	search(
  		Engine[]				engines,
  		final ResultListener 	original_listener,
  		SearchParameter[] 		searchParameters,
  		String					headers,
  		Map<String,String>		context,
  		final int				max_results_per_engine )
	{
		String batch_millis_str = context.get( Engine.SC_BATCH_PERIOD );
		
		final long batch_millis = batch_millis_str==null?0:Long.parseLong( batch_millis_str );
		
		String rem_dups_str = context.get( Engine.SC_REMOVE_DUP_HASH );
		
		final boolean rem_dups = rem_dups_str != null && rem_dups_str.equalsIgnoreCase("true");

		ResultListener	listener = 
			new ResultListener()
			{
					// 	single thread listener calls
			
				private AsyncDispatcher dispatcher = new AsyncDispatcher( 5000 );

				final private Map<Engine,List<Result[]>>	pending_results = new HashMap<>();
				
				final private Map<Engine,Set<String>>	result_hashes = new HashMap<>();
				
				public void
				contentReceived(
					final Engine engine, 
					final String content ) 
				{
					dispatcher.dispatch(
							new AERunnable()
							{
								public void
								runSupport()
								{
									original_listener.contentReceived( engine, content );
								}
							});
				}
				
				public void 
				matchFound(
					final Engine 	engine, 
					final String[] 	fields )
				{
					dispatcher.dispatch(
							new AERunnable()
							{
								public void
								runSupport()
								{
									original_listener.matchFound( engine, fields );
								}
							});	
				}
				
				public void 
				resultsReceived(
					final Engine 	engine,
					final Result[] 	results )
				{
					dispatcher.dispatch(
						new AERunnable()
						{
							public void
							runSupport()
							{
								Result[] results_to_return = null;
								
								if ( batch_millis > 0 ){
									
									List<Result[]> list = pending_results.get( engine );
																		
									if ( list == null ){
																			
										results_to_return = results;
										
										pending_results.put( engine, new ArrayList<>());
										
										new DelayedEvent(
											"SearchBatcher",
											batch_millis,
											new AERunnable()
											{
												public void 
												runSupport() 
												{
													dispatcher.dispatch(
														new AERunnable()
														{
															public void 
															runSupport() 
															{
																batchResultsComplete( engine );
															}
														});
												}
											});
									}else{
																															
										list.add( results );
									}
								}else{
								
									results_to_return = results;
								}
								
								if ( results_to_return != null ){
									
									results_to_return = truncateResults( engine, results_to_return, max_results_per_engine );
									
									original_listener.resultsReceived( engine, results_to_return );
								}
							}
						});
				}
			
				public void 
				resultsComplete(
					final Engine engine )
				{
					dispatcher.dispatch(
							new AERunnable()
							{
								public void
								runSupport()
								{
									if ( batch_millis > 0 ){

										batchResultsComplete( engine );
									}
									
									original_listener.resultsComplete( engine );
								}
							});
				}
			
				protected void
				batchResultsComplete(
					Engine engine )
				{
					List<Result[]> list = pending_results.remove( engine );
					
					if ( list != null ){
						
						List<Result> x = new ArrayList<>();
						
						for ( Result[] y: list ){
							
							x.addAll( Arrays.asList( y ));
						}
						
						Result[] results = x.toArray(new Result[0]);
					
						results = truncateResults( engine, results, max_results_per_engine );
					
						original_listener.resultsReceived( engine, results );
					}
				}
				
				protected Result[]
             	truncateResults(
             		Engine		engine,
             		Result[] 	a_results,
             		int			max )
             	{
					Set<String>	hash_set = result_hashes.get( engine );
					
					if ( hash_set == null ){
						
						hash_set = new HashSet<>();
						
						result_hashes.put( engine, hash_set );
					}
						
					List<Result>	results = new ArrayList<>(a_results.length);
					
					for ( Result r: a_results ){
							
						String name = r.getName();
							
						if ( name == null || name.trim().length() == 0 ){
								
							continue;
						}
					
						if ( rem_dups ){

							String hash = r.getHash();
							
							if ( 	hash == null ||
									hash.length() == 0 ){
								
								results.add( r );
								
							}else{
								
								if ( !hash_set.contains( hash )){
									
									results.add( r );
									
									hash_set.add( hash );
								}
							}
						}else{
							
							results.add( r );
						}
					}
					
             		if ( max < results.size() ){
             		
             			log( "Truncating search results for " + engine.getName() + " from " + results.size() + " to " + max );
             			
             			Collections.sort(
             				results,
             				new Comparator<Result>()
             				{
             					Map<Result,Float>	ranks = new HashMap<>();
             					
             					public int 
             					compare(
             						Result r1, 
             						Result r2) 
             					{						
             						Float	rank1 = ranks.get(r1);
             						
             						if ( rank1 == null ){	
             							rank1 = r1.getRank();
             							ranks.put( r1, rank1 );
             						}
             						
             						Float	rank2 = ranks.get(r2);
             						
             						if ( rank2 == null ){	
             							rank2 = r2.getRank();
             							ranks.put( r2, rank2 );
             						}
             						
             						return( rank2.compareTo( rank1 ));
             					}
             				});
             		
             			Result[] x = new Result[max];
             			
             			int	pos = 0;
             			
             			while( pos < max ){
             			
             				x[pos] = results.get( pos );
             				
             				pos++;
             			}
             			
             			return( x );
             			
             		}else{
             			
             			return( results.toArray(new Result[0]));
             		}
             	}
				
				public void 
				engineFailed(
					final Engine 	engine,
					final Throwable	e )
				{
					dispatcher.dispatch(
							new AERunnable()
							{
								public void
								runSupport()
								{
									original_listener.engineFailed( engine, e );
								}
							});
				}
				
				public void 
				engineRequiresLogin(
					final Engine 	engine,
					final Throwable	e )
				{
					dispatcher.dispatch(
							new AERunnable()
							{
								public void
								runSupport()
								{
									original_listener.engineRequiresLogin( engine, e );
								}
							});
				}
			};
			
		SearchExecuter se = new SearchExecuter( context, listener );
		
		if ( engines == null ){
			
			engines = getEngines( true, true );
		}
		
		String	engines_str = "";
		
		for (int i=0;i<engines.length;i++){
			
			engines_str += (i==0?"":",") + engines[i].getId();
		}
		
		log( "Search: engines=" + engines_str );

        for (Engine engine : engines) {

            se.search(engine, searchParameters, headers, max_results_per_engine);
        }
		
		return( engines );
	}
	
	public void
	exportEngines(
		File	target )
	
		throws MetaSearchException
	{
		Engine[] engines = getEngines( true, false );
		
		VuzeFile	vf = VuzeFileHandler.getSingleton().create();
		
		for ( Engine engine: engines ){
		
			try{
				vf.addComponent(
					VuzeFileComponent.COMP_TYPE_METASEARCH_TEMPLATE,
					engine.exportToBencodedMap());
				
			}catch( IOException e ){
				
				Debug.out( e );
			}
			
		}
		
		try{
			vf.write( target );
			
		}catch( IOException e ){
			
			throw( new MetaSearchException( "Failed to write file", e ));
		}
	}
	
	public void
	addListener(
		MetaSearchListener		listener )
	{
		listeners.add( listener );
	}
	
	public void
	removeListener(
		MetaSearchListener		listener )
	{
		listeners.remove( listener );
	}
	
	protected void
	loadConfig()
	{
		log( "Loading configuration" );
		
		synchronized( this ){
			
			Map<String,Object> map = FileUtil.readResilientConfigFile( CONFIG_FILE );
			
			List<Map<String,Object>>	l_engines = (List<Map<String,Object>>)map.get( "engines" );
			
			if( l_engines != null ){

                for (Map<String, Object> m : l_engines) {

                    try {
                        Engine e = importFromBEncodedMap(m);

                        addEngine((EngineImpl) e, true);

                        log("    loaded " + e.getString());

                    } catch (Throwable e) {

                        log("Failed to import engine from " + m, e);
                    }
                }
			}
			
			Map<String,Long>	p_map = (Map<String,Long>)map.get( "plugin_map" );
			
			if ( p_map != null ){
				
				plugin_map = p_map;
			}
		
			if ( update_check_timer != null ){
				
				checkUpdates();
			}
		}
	}
	
	public void
	configDirty()
	{
		synchronized( this ){
			
			if ( config_dirty ){
				
				return;
			}
			
			config_dirty = true;
		
			new DelayedEvent( 
				"MetaSearch:save", 5000,
				new AERunnable()
				{
					public void 
					runSupport() 
					{
						synchronized( MetaSearchImpl.this ){
							
							if ( !config_dirty ){

								return;
							}
							
							saveConfig();
						}	
					}
				});
		}
	}
	
	protected void
	saveConfig()
	{
		log( "Saving configuration" );
		
		synchronized( this ){
			
			config_dirty = false;
			
			Map<String,Object> map = new HashMap<>();
			
			List<Map<String,Object>>	l_engines = new ArrayList<>();
			
			map.put( "engines", l_engines );

            for (Engine e : engines) {

                try {

                    l_engines.add(e.exportToBencodedMap());

                } catch (Throwable f) {

                    log("Failed to export engine " + e.getName(), f);
                }
            }
			
			if ( plugin_map != null ){
				
				map.put( "plugin_map", plugin_map );
			}
			
			FileUtil.writeResilientConfigFile( CONFIG_FILE, map );
		}
	}
	
	protected void
	log(
		String	str )
	{
		manager.log( "search :"  + str );
	}
	
	protected void
	log(
		String		str,
		Throwable 	e )
	{
		manager.log( "search :"  +  str, e );
	}
	
	protected void
	generate(
		IndentWriter		writer )
	{

        for (EngineImpl e : engines) {

            writer.println(e.getString(true));
        }
	}
}
