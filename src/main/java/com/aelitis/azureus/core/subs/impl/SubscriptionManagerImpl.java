/*
 * Created on Jul 11, 2008
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


package com.aelitis.azureus.core.subs.impl;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.config.ParameterListener;
import org.gudy.azureus2.core3.download.DownloadManagerState;
import org.gudy.azureus2.core3.internat.MessageText;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentFactory;
import org.gudy.azureus2.core3.util.*;
import org.gudy.azureus2.plugins.PluginException;
import org.gudy.azureus2.plugins.utils.Utilities;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.ddb.DistributedDatabase;
import org.gudy.azureus2.plugins.download.*;
import org.gudy.azureus2.plugins.peers.PeerManager;
import org.gudy.azureus2.plugins.torrent.Torrent;
import org.gudy.azureus2.plugins.torrent.TorrentAttribute;
import org.gudy.azureus2.plugins.torrent.TorrentManager;
import org.gudy.azureus2.plugins.ui.UIManager;
import org.gudy.azureus2.plugins.ui.UIManagerEvent;
import org.gudy.azureus2.plugins.utils.DelayedTask;
import org.gudy.azureus2.plugins.utils.StaticUtilities;
import org.gudy.azureus2.plugins.utils.search.SearchException;
import org.gudy.azureus2.plugins.utils.search.SearchInstance;
import org.gudy.azureus2.plugins.utils.search.SearchObserver;
import org.gudy.azureus2.plugins.utils.search.SearchProvider;
import org.gudy.azureus2.plugins.utils.search.SearchResult;
import org.gudy.azureus2.pluginsimpl.local.PluginCoreUtils;
import org.gudy.azureus2.pluginsimpl.local.PluginInitializer;
import org.gudy.azureus2.pluginsimpl.local.torrent.TorrentImpl;
import org.gudy.azureus2.pluginsimpl.local.utils.UtilitiesImpl;
import org.spongycastle.util.encoders.Base64;

import com.aelitis.azureus.core.AzureusCore;
import com.aelitis.azureus.core.AzureusCoreRunningListener;
import com.aelitis.azureus.core.AzureusCoreFactory;
import com.aelitis.azureus.core.custom.Customization;
import com.aelitis.azureus.core.custom.CustomizationManager;
import com.aelitis.azureus.core.custom.CustomizationManagerFactory;
import com.aelitis.azureus.core.lws.LightWeightSeed;
import com.aelitis.azureus.core.lws.LightWeightSeedManager;
import com.aelitis.azureus.core.messenger.config.PlatformSubscriptionsMessenger;
import com.aelitis.azureus.core.metasearch.Engine;
import com.aelitis.azureus.core.metasearch.MetaSearchListener;
import com.aelitis.azureus.core.metasearch.MetaSearchManagerFactory;
import com.aelitis.azureus.core.metasearch.impl.web.WebEngine;
import com.aelitis.azureus.core.metasearch.impl.web.rss.RSSEngine;
import com.aelitis.azureus.core.security.CryptoECCUtils;
import com.aelitis.azureus.core.subs.*;
import com.aelitis.azureus.core.subs.SubscriptionUtils.SubscriptionDownloadDetails;
import com.aelitis.azureus.core.tag.Tag;
import com.aelitis.azureus.core.tag.TagManagerFactory;
import com.aelitis.azureus.core.torrent.PlatformTorrentUtils;
import com.aelitis.azureus.core.util.CopyOnWriteList;
import com.aelitis.azureus.core.vuzefile.*;
import com.aelitis.azureus.plugins.dht.*;
import com.aelitis.azureus.plugins.magnet.MagnetPlugin;
import com.aelitis.azureus.plugins.magnet.MagnetPluginProgressListener;
import com.aelitis.azureus.plugins.net.buddy.BuddyPluginBeta;
import com.aelitis.azureus.plugins.net.buddy.BuddyPluginUtils;
import com.aelitis.azureus.plugins.net.buddy.BuddyPluginBeta.ChatInstance;
import com.aelitis.azureus.plugins.net.buddy.BuddyPluginBeta.ChatMessage;
import com.aelitis.azureus.util.ImportExportUtils;
import com.aelitis.azureus.util.UrlFilter;
import com.aelitis.net.magneturi.MagnetURIHandler;


public class 
SubscriptionManagerImpl 
	implements SubscriptionManager, AEDiagnosticsEvidenceGenerator
{	
	private static final String	CONFIG_FILE = "subscriptions.config";
	private static final String	LOGGER_NAME = "Subscriptions";

	private static final String CONFIG_MAX_RESULTS 			= "subscriptions.max.non.deleted.results";
	private static final String CONFIG_AUTO_START_DLS 		= "subscriptions.auto.start.downloads";
	private static final String CONFIG_AUTO_START_MIN_MB 	= "subscriptions.auto.start.min.mb";
	private static final String CONFIG_AUTO_START_MAX_MB 	= "subscriptions.auto.start.max.mb";
	private static final String CONFIG_AUTO_MARK_READ	 	= "subscriptions.auto.dl.mark.read.days";
	
	private static final String	CONFIG_RSS_ENABLE			= "subscriptions.config.rss_enable";

	private static final String	CONFIG_ENABLE_SEARCH			= "subscriptions.config.search_enable";
	
	private static final String	CONFIG_HIDE_SEARCH_TEMPLATES	= "subscriptions.config.hide_search_templates";
	
	private static final String	CONFIG_DL_SUBS_ENABLE		= "subscriptions.config.dl_subs_enable";
	private static final String	CONFIG_DL_RATE_LIMITS		= "subscriptions.config.rate_limits";

	private static final String CONFIG_ACTIVATE_ON_CHANGE	= "subscriptions.config.activate.sub.on.change";
	
	private static final int DELETE_UNUSED_AFTER_MILLIS = 2*7*24*60*60*1000;
	
	private static final int PUB_ASSOC_CONC_MAX;
	private static final int PUB_SLEEPING_ASSOC_CONC_MAX	= 1;

	static{
		int max_conc_assoc_pub = 3;
		
		try{
			
			max_conc_assoc_pub = Integer.parseInt( System.getProperty( "azureus.subs.max.concurrent.assoc.publish", ""+max_conc_assoc_pub));
			
		}catch( Throwable e ){
			Debug.out( e );
		}
		
		PUB_ASSOC_CONC_MAX = max_conc_assoc_pub;
	}
	
	private static SubscriptionManagerImpl		singleton;
	private static boolean						pre_initialised;
	
	private static final int random_seed = RandomUtils.nextInt( 256 );
	
	public static void
	preInitialise()
	{
		synchronized( SubscriptionManagerImpl.class ){
			
			if ( pre_initialised ){
				
				return;
			}
			
			pre_initialised = true;
		}
		
		VuzeFileHandler.getSingleton().addProcessor(
			new VuzeFileProcessor()
			{
				public void
				process(
					VuzeFile[]		files,
					int				expected_types )
				{
                    for (VuzeFile vf : files) {

                        VuzeFileComponent[] comps = vf.getComponents();

                        for (VuzeFileComponent comp : comps) {

                            int type = comp.getType();

                            if (type == VuzeFileComponent.COMP_TYPE_SUBSCRIPTION ||
                                    type == VuzeFileComponent.COMP_TYPE_SUBSCRIPTION_SINGLETON) {

                                try {
                                    Subscription subs = ((SubscriptionManagerImpl) getSingleton(false)).importSubscription(
                                            type,
                                            comp.getContent(),
                                            (expected_types &
                                                    (VuzeFileComponent.COMP_TYPE_SUBSCRIPTION | VuzeFileComponent.COMP_TYPE_SUBSCRIPTION_SINGLETON)) == 0);

                                    comp.setProcessed();

                                    comp.setData(Subscription.VUZE_FILE_COMPONENT_SUBSCRIPTION_KEY, subs);

                                } catch (Throwable e) {

                                    Debug.printStackTrace(e);
                                }
                            }
                        }
                    }
				}
			});		
	}
		
	public static SubscriptionManager
	getSingleton(
		boolean		stand_alone )
	{
		preInitialise();
		
		synchronized( SubscriptionManagerImpl.class ){
			
			if ( singleton != null ){
			
				return( singleton );
			}
			
			singleton = new SubscriptionManagerImpl( stand_alone );
		}
		
			// saw deadlock here when adding core listener while synced on class - rework
			// to avoid 
		
		if ( !stand_alone ){
			
			singleton.initialise();
		}
		
		return( singleton );
	}
	
	
	private boolean		started;
		
	private static final int	TIMER_PERIOD		= 30*1000;
	
	private static final int	ASSOC_CHECK_PERIOD	= 5*60*1000;
	private static final int	ASSOC_CHECK_TICKS	= ASSOC_CHECK_PERIOD/TIMER_PERIOD;
	
	private static final int	ASSOC_PUBLISH_PERIOD	= 5*60*1000;
	private static final int	ASSOC_PUBLISH_TICKS		= ASSOC_PUBLISH_PERIOD/TIMER_PERIOD;

	private static final int	CHAT_CHECK_PERIOD	= 3*60*1000;
	private static final int	CHAT_CHECK_TICKS	= CHAT_CHECK_PERIOD/TIMER_PERIOD;

	private static final int	SERVER_PUB_CHECK_PERIOD	= 10*60*1000;
	private static final int	SERVER_PUB_CHECK_TICKS	= SERVER_PUB_CHECK_PERIOD/TIMER_PERIOD;
	
	private static final int	TIDY_POT_ASSOC_PERIOD	= 30*60*1000;
	private static final int	TIDY_POT_ASSOC_TICKS	= TIDY_POT_ASSOC_PERIOD/TIMER_PERIOD;

	private static final int	SET_SELECTED_PERIOD		= 23*60*60*1000;
	private static final int	SET_SELECTED_FIRST_TICK	= 3*60*1000 /TIMER_PERIOD;
	private static final int	SET_SELECTED_TICKS		= SET_SELECTED_PERIOD/TIMER_PERIOD;

	private static final Object	SP_LAST_ATTEMPTED	= new Object();
	private static final Object	SP_CONSEC_FAIL		= new Object();
	
	private AzureusCore		azureus_core;
	
	private volatile DHTPluginInterface	dht_plugin_public;
	
	private List<SubscriptionImpl>		subscriptions	= new ArrayList<>();
	
	private boolean	config_dirty;
		
	private int		publish_associations_active;
	private boolean	publish_next_asyc_pending;
	
	private boolean publish_subscription_active;
	
	private TorrentAttribute		ta_subs_download;
	private TorrentAttribute		ta_subs_download_rd;
	private TorrentAttribute		ta_subscription_info;
	private TorrentAttribute		ta_category;
	private TorrentAttribute		ta_networks;
	
	private boolean					periodic_lookup_in_progress;
	private int						priority_lookup_pending;
	
	private CopyOnWriteList<SubscriptionManagerListener>			listeners = new CopyOnWriteList<>();
	
	private SubscriptionSchedulerImpl	scheduler;
	
	private List<Object[]>					potential_associations	= new ArrayList<>();
	private Map<HashWrapper,Object[]>		potential_associations2	= new HashMap<>();
	private Map<HashWrapper,Object[]>		potential_associations3	= new HashMap<>();
	
	private boolean					meta_search_listener_added;
	
	private Pattern					exclusion_pattern = Pattern.compile( "azdev[0-9]+\\.azureus\\.com" );
	
	private SubscriptionRSSFeed		rss_publisher;
	
	private AEDiagnosticsLogger		logger;
	
	private Map<SubscriptionImpl,Object[]>		result_cache = new HashMap<>();
		
	
	protected
	SubscriptionManagerImpl(
		boolean	stand_alone )
	{
		if ( !stand_alone ){
			
			loadConfig();
	
			AEDiagnostics.addEvidenceGenerator( this );
			
			CustomizationManager cust_man = CustomizationManagerFactory.getSingleton();
			
			Customization cust = cust_man.getActiveCustomization();
			
			if ( cust != null ){
				
				String cust_name 	= COConfigurationManager.getStringParameter( "subscriptions.custom.name", "" );
				String cust_version = COConfigurationManager.getStringParameter( "subscriptions.custom.version", "0" );
				
				boolean	new_name 	= !cust_name.equals( cust.getName());
				boolean	new_version = org.gudy.azureus2.core3.util.Constants.compareVersions( cust_version, cust.getVersion() ) < 0;
				
				if ( new_name || new_version ){

					log( "Customization: checking templates for " + cust.getName() + "/" + cust.getVersion());
					
					try{
						InputStream[] streams = cust.getResources( Customization.RT_SUBSCRIPTIONS );

                        for (InputStream is : streams) {

                            try {
                                VuzeFile vf = VuzeFileHandler.getSingleton().loadVuzeFile(is);

                                if (vf != null) {

                                    VuzeFileComponent[] comps = vf.getComponents();

                                    for (VuzeFileComponent comp : comps) {

                                        int type = comp.getType();

                                        if (type == VuzeFileComponent.COMP_TYPE_SUBSCRIPTION ||
                                                type == VuzeFileComponent.COMP_TYPE_SUBSCRIPTION_SINGLETON) {

                                            try {
                                                importSubscription(
                                                        type,
                                                        comp.getContent(),
                                                        false);

                                                comp.setProcessed();

                                            } catch (Throwable e) {

                                                Debug.printStackTrace(e);
                                            }
                                        }
                                    }
                                }
                            }
                            finally {
                                try{ is.close();} catch (IOException e) {}
                            }
                        }
					}finally{
						
						COConfigurationManager.setParameter( "subscriptions.custom.name", cust.getName());
						COConfigurationManager.setParameter( "subscriptions.custom.version", cust.getVersion());
					}
				}
			}
			
			scheduler = new SubscriptionSchedulerImpl( this );
		}
		
	SimpleTimer.addPeriodicEvent(
			"SubscriptionCacheCheck",
			10*1000,
			new TimerEventPerformer()
			{
				public void 
				perform(TimerEvent event) {
				
					long now = SystemTime.getMonotonousTime();
					
					synchronized( result_cache ){
					
						Iterator<Object[]> it = result_cache.values().iterator();
						
						while( it.hasNext()){
							
							long time = (Long)it.next()[1];
							
							if ( now - time > 15*1000 ){
								
								it.remove();
							}
						}
					}
				}
			});
	}
	
	protected void
	initialise()
	{
		AzureusCoreFactory.addCoreRunningListener(new AzureusCoreRunningListener() {
			public void azureusCoreRunning(final AzureusCore core) {
				initWithCore(core);
			}
		});
	}

	protected void
	initWithCore(
		AzureusCore 	_core )
	{
		synchronized( this ){
			
			if ( started ){
				
				return;
			}
			
			started	= true;
		}

		azureus_core = _core;
		
		final PluginInterface default_pi = PluginInitializer.getDefaultInterface();

		rss_publisher = new SubscriptionRSSFeed( this, default_pi );
		
		TorrentManager  tm = default_pi.getTorrentManager();
		
		ta_subs_download 		= tm.getPluginAttribute( "azsubs.subs_dl" );
		ta_subs_download_rd 	= tm.getPluginAttribute( "azsubs.subs_dl_rd" );
		ta_subscription_info 	= tm.getPluginAttribute( "azsubs.subs_info" );
		ta_category				= tm.getAttribute( TorrentAttribute.TA_CATEGORY );
		ta_networks 			= tm.getAttribute( TorrentAttribute.TA_NETWORKS );

		PluginInterface  dht_plugin_pi  = AzureusCoreFactory.getSingleton().getPluginManager().getPluginInterfaceByClass( DHTPlugin.class );
				
		if ( dht_plugin_pi != null ){
			
			dht_plugin_public = (DHTPlugin)dht_plugin_pi.getPlugin();

			/*
			if ( Constants.isCVSVersion()){
				
				addListener(
						new SubscriptionManagerListener()
						{
							public void 
							subscriptionAdded(
								Subscription subscription ) 
							{
							}
				
							public void
							subscriptionChanged(
								Subscription		subscription )
							{
							}
							
							public void 
							subscriptionRemoved(
								Subscription subscription ) 
							{
							}
							
							public void 
							associationsChanged(
								byte[] hash )
							{
								System.out.println( "Subscriptions changed: " + ByteFormatter.encodeString( hash ));
								
								Subscription[] subs = getKnownSubscriptions( hash );
							
								for (int i=0;i<subs.length;i++){
									
									System.out.println( "    " + subs[i].getString());
								}
							}
						});	
			}
			*/
			
			default_pi.getDownloadManager().addListener(
				new DownloadManagerListener()
				{
					public void
					downloadAdded(
						Download	download )
					{
						Torrent	torrent = download.getTorrent();
						
						if ( torrent != null ){
							
							byte[]	hash = torrent.getHash();
							
							Object[] entry;
							
							synchronized( potential_associations2 ){
								
								entry = potential_associations2.remove( new HashWrapper( hash ));
							}
							
							if ( entry != null ){
								
								SubscriptionImpl[] subs = (SubscriptionImpl[])entry[0];
								
								String	subs_str = "";
								for (int i=0;i<subs.length;i++){
									subs_str += (i==0?"":",") + subs[i].getName();
								}
								
								log( "Applying deferred asocciation for " + ByteFormatter.encodeString( hash ) + " -> " + subs_str );
								
								recordAssociationsSupport(
									hash,
									subs,
                                        (Boolean) entry[1]);
							}
						}
					}
					
					public void
					downloadRemoved(
						Download	download )
					{	
					}
				},
				false );
			
			default_pi.getDownloadManager().addDownloadWillBeAddedListener(
				new DownloadWillBeAddedListener() {
					
					public void 
					initialised(
						Download download )
					{
						Torrent	torrent = download.getTorrent();
						
						if ( torrent != null ){
							
							byte[]	hash = torrent.getHash();
							
							HashWrapper hw = new HashWrapper( hash );
							
							Object[] entry;
							
							synchronized( potential_associations2 ){
								
								entry = potential_associations2.get( hw );
							}
							
							if ( entry != null ){
								
								SubscriptionImpl[] subs = (SubscriptionImpl[])entry[0];
																
								prepareDownload( download, subs, null );
								
							}else{
																
								synchronized( potential_associations3 ){
									
									entry = potential_associations3.get( hw );
								}
								
								if ( entry != null ){
									
									Subscription[] subs = (Subscription[])entry[0];

									SubscriptionResult[] results = (SubscriptionResult[])entry[1];
									
									prepareDownload( download, subs, results );
								}
							}
						}
					}
				});
			
			TorrentUtils.addTorrentAttributeListener(
				new TorrentUtils.torrentAttributeListener()
				{
					public void 
					attributeSet(
						TOTorrent 	torrent,
						String 		attribute, 
						Object 		value )
					{
						if (TorrentUtils.TORRENT_AZ_PROP_OBTAINED_FROM.equals(attribute)){
							
							try{
								checkPotentialAssociations( torrent.getHash(), (String)value );
								
							}catch( Throwable e ){
								
								Debug.printStackTrace(e);
							}
						}
					}
				});
				
			DelayedTask delayed_task = UtilitiesImpl.addDelayedTask( "Subscriptions", 
					new Runnable()
					{
						public void 
						run() 
						{
							new AEThread2( "Subscriptions:delayInit", true )
							{
								public void
								run()
								{
									asyncInit();
								}
							}.start();
							
						}
						
						protected void
						asyncInit()
						{
							Download[] downloads = default_pi.getDownloadManager().getDownloads();

                            for (Download download : downloads) {

                                if (download.getBooleanAttribute(ta_subs_download)) {

                                    Map rd = download.getMapAttribute(ta_subs_download_rd);

                                    boolean delete_it;

                                    if (rd == null) {

                                        delete_it = true;

                                    } else {

                                        delete_it = !recoverSubscriptionUpdate(download, rd);
                                    }

                                    if (delete_it) {

                                        removeDownload(download, true);
                                    }
                                }
                            }
								
							default_pi.getDownloadManager().addListener(
								new DownloadManagerListener()
								{
									public void
									downloadAdded(
										final Download	download )
									{
											// if ever changed to handle non-persistent then you need to fix init deadlock
											// potential with share-hoster plugin
										
										if ( !downloadIsIgnored( download )){
											
											if ( !dht_plugin_public.isInitialising()){
								
													// if new download then we want to check out its subscription status 
												
												lookupAssociations( download.getMapAttribute( ta_subscription_info ) == null );
												
											}else{
												
												new AEThread2( "Subscriptions:delayInit", true )
												{
													public void
													run()
													{
														lookupAssociations( download.getMapAttribute( ta_subscription_info ) == null );
													}
												}.start();
											}
										}
									}
									
									public void
									downloadRemoved(
										Download	download )
									{
									}
								},
								false );
							
							for (int i=0;i<PUB_ASSOC_CONC_MAX;i++){
							
								if ( publishAssociations()){
									
									break;
								}
							}
							
							publishSubscriptions();
							
							COConfigurationManager.addParameterListener(
									CONFIG_MAX_RESULTS,
									new ParameterListener()
									{
										public void 
										parameterChanged(
											String	 name )
										{
											final int	max_results = COConfigurationManager.getIntParameter( CONFIG_MAX_RESULTS );
											
											new AEThread2( "Subs:max results changer", true )
											{
												public void
												run()
												{
													checkMaxResults( max_results );
												}
											}.start();
										}
									});
							
							SimpleTimer.addPeriodicEvent(
									"SubscriptionChecker",
									TIMER_PERIOD,
									new TimerEventPerformer()
									{
										private int	ticks;
										
										public void 
										perform(
											TimerEvent event )
										{
											ticks++;
											
											checkStuff( ticks );
										}
									});
						}
					});
		
			delayed_task.queue();
		}
		
		if ( isSearchEnabled()){
			
			try{
				default_pi.getUtilities().registerSearchProvider(
					new SearchProvider()
					{
						private Map<Integer,Object>	properties = new HashMap<>();
						
						{
							properties.put( PR_NAME, MessageText.getString( "ConfigView.section.Subscriptions" ));
							
							try{
								URL url = 
									MagnetURIHandler.getSingleton().registerResource(
										new MagnetURIHandler.ResourceProvider()
										{
											public String
											getUID()
											{
												return( SubscriptionManager.class.getName() + ".2" );
											}
											
											public String
											getFileType()
											{
												return( "png" );
											}
													
											public byte[]
											getData()
											{
												InputStream is = getClass().getClassLoader().getResourceAsStream( "com/aelitis/azureus/ui/images/subscription_icon_1616.png" );

												if ( is == null ){
													
													return( null );
												}
												
												try{
													ByteArrayOutputStream	baos = new ByteArrayOutputStream();
													
													try{
														byte[]	buffer = new byte[8192];
														
														while( true ){
								
															int	len = is.read( buffer );
											
															if ( len <= 0 ){
																
																break;
															}
									
															baos.write( buffer, 0, len );
														}
													}finally{
														
														is.close();
													}
													
													return( baos.toByteArray());
													
												}catch( Throwable e ){
													
													return( null );
												}
											}
										});
																		
								properties.put( PR_ICON_URL, url.toExternalForm());
								
							}catch( Throwable e ){
								
								Debug.out( e );
							}
						}
						
						public SearchInstance
						search(
							Map<String,Object>	search_parameters,
							SearchObserver		observer )
						
							throws SearchException
						{		
							try{
								return( searchSubscriptions( search_parameters, observer ));
								
							}catch( Throwable e ){
								
								throw( new SearchException( "Search failed", e ));
							}
						}
						
						public Object
						getProperty(
							int			property )
						{
							return( properties.get( property ));
						}
						
						public void
						setProperty(
							int			property,
							Object		value )
						{
							properties.put( property, value );
						}
					});
				
			}catch( Throwable e ){
				
				Debug.out( "Failed to register search provider" );
			}
		}
		
		default_pi.getUtilities().registerJSONRPCServer(
			new Utilities.JSONServer()
			{
				private List<String>	methods = new ArrayList<>();
				
				{
					methods.add( "vuze-subs-list" );
				}
				
				public String
				getName()
				{
					return( "Subscriptions" );
				}
				
				public List<String> 
				getSupportedMethods() 
				{
					return( methods );
				}
				
				public Map 
				call(
					String 		method, 
					Map		 	args )
						
					throws PluginException 
				{
					throw( new PluginException( "derp" ));
				}
			});
	}

	protected Object[]
	getSearchTemplateVuzeFile(
		SubscriptionImpl	sub )
	{
		try{
			String subs_url_str = ((RSSEngine)sub.getEngine()).getSearchUrl( true );
			
			URL subs_url = new URL( subs_url_str );
			
			final byte[] vf_bytes = FileUtil.readInputStreamAsByteArray(subs_url.openConnection().getInputStream());

			VuzeFile vf = VuzeFileHandler.getSingleton().loadVuzeFile( vf_bytes );
			
			if ( MetaSearchManagerFactory.getSingleton().isImportable( vf )){
	
				return( new Object[]{ vf, vf_bytes });
			}
		}catch( Throwable e ){
			
			Debug.out( e );
		}
		
		return( null );
	}
	
	public boolean 
	isSearchTemplateImportable(
		SubscriptionImpl	sub )
	{
		try{
			String subs_url_str = ((RSSEngine)sub.getEngine()).getSearchUrl( true );
			
			URL subs_url = new URL( subs_url_str );
			
			final byte[] vf_bytes = FileUtil.readInputStreamAsByteArray(subs_url.openConnection().getInputStream());

			VuzeFile vf = VuzeFileHandler.getSingleton().loadVuzeFile( vf_bytes );
			
			return( MetaSearchManagerFactory.getSingleton().isImportable( vf ));
	
		}catch( Throwable e ){
			
			Debug.out( e );
		}
		
		return( false );
	}
	
	public SearchInstance
	searchSubscriptions(
		Map<String,Object>		search_parameters,
		final SearchObserver	observer )
	
		throws SearchException
	{
		final String	term = (String)search_parameters.get( SearchProvider.SP_SEARCH_TERM );
		
		final SearchInstance si = 
			new SearchInstance()
			{
				public void
				cancel()
				{
					Debug.out( "Cancelled" );
				}
			};
			
		if ( term == null ){
		
			try{
				observer.complete();
				
			}catch( Throwable e ){
				
				Debug.out( e );
			}
		}else{
		
			new AEThread2( "Subscriptions:search", true )
			{
				public void
				run()
				{
					final Set<String>	hashes = new HashSet<>();
					
					searchMatcher	matcher = new searchMatcher( term );

					try{				
						List<SubscriptionResult>	matches = matchSubscriptionResults( matcher );
							
						for ( final SubscriptionResult result: matches ){
							
							final Map result_properties = result.toPropertyMap();
							
							byte[] hash = (byte[])result_properties.get( SearchResult.PR_HASH );
														
							if ( hash != null ){
															
								String hash_str = Base32.encode( hash );
							
								if ( hashes.contains( hash_str )){
								
									continue;
								}
							
								hashes.add( hash_str );
							}
							
							SearchResult search_result = 
								new SearchResult()
								{
									public Object
									getProperty(
										int		property_name )
									{
										return( result_properties.get( property_name ));
									}
								};
						
							try{
								observer.resultReceived( si, search_result );
								
							}catch( Throwable e ){
								
								Debug.out( e );
							}
						}
												
						Map<String,Object[]> template_matches = new HashMap<>();
								
						Engine[] engines = MetaSearchManagerFactory.getSingleton().getMetaSearch().getEngines( false, false );
						
						Map<Subscription,List<String>>	sub_dl_name_map = null;
						
						for ( Subscription sub: getSubscriptions( false )){
							
							if ( !sub.isSearchTemplate()){
								
								continue;
							}
							
							String	sub_name = sub.getName(false);
														
							Engine sub_engine = sub.getEngine();
							
							if ( sub_engine.isActive() || !(sub_engine instanceof RSSEngine )){
								
								continue;
							}
							
							int	pos = sub_name.indexOf( ":" );
							
							String t_name = sub_name.substring( pos+1 );
							
							pos	= t_name.indexOf( "(v" );
							
							int t_ver;
							
							if ( pos == -1 ){
								
								t_ver = 1;
								
							}else{
								
								String s = t_name.substring( pos+2, t_name.length()-1);
								
								t_name = t_name.substring( 0, pos );
								
								try{
							
									t_ver = Integer.parseInt(s);
									
								}catch( Throwable e ){
									
									t_ver = 1;
								}
							}
							
							t_name = t_name.trim();
							
							boolean skip = false;
							
							for ( Engine e: engines ){
								
								if ( e != sub_engine && e.sameLogicAs( sub_engine )){
									
									skip = true;
									
									break;
								}
								
								if ( e.getName().equalsIgnoreCase( t_name )){
									
									if ( e.getVersion() >= t_ver ){
										
										skip = true;
									}
								}
							}
							
							if ( skip ){
								
								continue;
							}
							
							if ( sub_dl_name_map == null ){
								
								sub_dl_name_map = new HashMap<>();
								
								SubscriptionDownloadDetails[] sdds = SubscriptionUtils.getAllCachedDownloadDetails( azureus_core );
								
								for ( SubscriptionDownloadDetails sdd: sdds ){
									
									String name = sdd.getDownload().getDisplayName();
									
									if ( matcher.matches( name )){
										
										Subscription[] x = sdd.getSubscriptions();
										
										for ( Subscription s: x ){
											
											List<String> g = sub_dl_name_map.get( s );
											
											if ( g == null ){
												
												g = new ArrayList<>();
												
												sub_dl_name_map.put( s, g );
											}
											
											g.add( name );
										}
									}
								}
							}
							
							List<String> names = sub_dl_name_map.get( sub );
							
							if ( names == null ){
								
								continue;
							}
													
							String key = t_name.toLowerCase();
							
							Object[] entry = template_matches.get( key );
							
							if ( entry == null ){
								
								entry = new Object[]{ sub, t_ver };
								
								template_matches.put( key, entry );
								
							}else{
								
								if ( t_ver > (Integer)entry[1]){
									
									entry[0]	= sub;
									entry[1]	= t_ver;
								}
							}
						}
						
						List<SubscriptionImpl>	interesting = new ArrayList<>();
						
						for ( Object[] entry: template_matches.values()){
						
							interesting.add((SubscriptionImpl)entry[0]);
						}
						
						Collections.sort(
							interesting,
							new Comparator<Subscription>()
							{
								public int 
								compare(
									Subscription o1,
									Subscription o2) 
								{
									long res = o2.getCachedPopularity() - o1.getCachedPopularity();
									
									if ( res < 0 ){
										return( -1 );
									}else if ( res > 0 ){
										return( 1 );
									}else{
										return( 0 );
									}
								}
							});
						
						int	added = 0;
						
						for ( final SubscriptionImpl sub: interesting ){
								
							if ( added >= 3 ){
								
								break;
							}
							
							try{
								Object[] vf_entry = getSearchTemplateVuzeFile( sub );
								
								if ( vf_entry != null ){
									
									final byte[] vf_bytes = (byte[])vf_entry[1];
									
									final URL url = 
										MagnetURIHandler.getSingleton().registerResource(
											new MagnetURIHandler.ResourceProvider()
											{
												public String
												getUID()
												{
													return( SubscriptionManager.class.getName() + ".sid." + sub.getID() );
												}
												
												public String
												getFileType()
												{
													return( "vuze" );
												}
														
												public byte[]
												getData()
												{
													return( vf_bytes );
												}
											});
											
									SearchResult search_result = 
										new SearchResult()
										{
											public Object
											getProperty(
												int		property_name )
											{
												if ( property_name == SearchResult.PR_NAME ){
													
													return( sub.getName());
													
												}else if ( 	property_name == SearchResult.PR_DOWNLOAD_LINK ||
															property_name == SearchResult.PR_DOWNLOAD_BUTTON_LINK ){
													
													return( url.toExternalForm());
													
												}else if ( property_name == SearchResult.PR_PUB_DATE ){
													
													return( new Date(sub.getAddTime()));
													
												}else if ( property_name == SearchResult.PR_SIZE ){
													
													return( 1024L );
													
												}else if ( 	property_name == SearchResult.PR_SEED_COUNT ||
															property_name == SearchResult.PR_VOTES ){
																							
													return sub.getCachedPopularity();
													
												}else if ( property_name == SearchResult.PR_RANK ){
												
													return( 100L );
												}
												
												return( null );
											}
										};
							
									added++;
										
									try{
										observer.resultReceived( si, search_result );
										
									}catch( Throwable e ){
										
										Debug.out( e );
									}
								}
							}catch( Throwable e ){
								
								Debug.out( e );
							}
						}
					}catch( Throwable e ){
						
						Debug.out( e );
						
					}finally{
						
						observer.complete();
					}
				}
			}.start();
		}
		
		return( si );
	}
	
	private List<SubscriptionResult>
	matchSubscriptionResults(
		searchMatcher	matcher  )
	{
		List<SubscriptionResult> result = new ArrayList<>();
				
		for ( Subscription sub: getSubscriptions( true )){
			
			SubscriptionResult[] results = sub.getResults( false );
			
			for ( SubscriptionResult r: results ){
				
				Map properties = r.toPropertyMap();
				
				String name = (String)properties.get( SearchResult.PR_NAME );
				
				if ( name == null ){
					
					continue;
				}
				
				if ( matcher.matches( name )){
					
					result.add( r );
				}
			}
		}
		
		return( result );
	}
	
	protected void
	checkMaxResults(
		int		max )
	{
		Subscription[] subs = getSubscriptions();

        for (Subscription sub : subs) {

            ((SubscriptionHistoryImpl) sub.getHistory()).checkMaxResults(max);
        }
	}
	
	public SubscriptionScheduler
	getScheduler()
	{
		return( scheduler );
	}
	
	public boolean
	isRSSPublishEnabled()
	{
		return( COConfigurationManager.getBooleanParameter( CONFIG_RSS_ENABLE, false ));
	}
	
	public void
	setRSSPublishEnabled(
		boolean		enabled )
	{
		COConfigurationManager.setParameter( CONFIG_RSS_ENABLE, enabled );
	}
	
	public boolean
	isSearchEnabled()
	{
		return( COConfigurationManager.getBooleanParameter( CONFIG_ENABLE_SEARCH, true ));
	}
	
	public void
	setSearchEnabled(
		boolean		enabled )
	{
		COConfigurationManager.setParameter( CONFIG_ENABLE_SEARCH, enabled );
	}
	
	public boolean
	hideSearchTemplates()
	{
		return( COConfigurationManager.getBooleanParameter( CONFIG_HIDE_SEARCH_TEMPLATES, true ));
	}
	
	public boolean
	isSubsDownloadEnabled()
	{
		return( COConfigurationManager.getBooleanParameter( CONFIG_DL_SUBS_ENABLE, true ));
	}
	
	public void
	setSubsDownloadEnabled(
		boolean		enabled )
	{
		COConfigurationManager.setParameter( CONFIG_DL_SUBS_ENABLE, enabled );
	}
	
	public void
	setRateLimits(
		String		limits )
	{
		COConfigurationManager.setParameter( CONFIG_DL_RATE_LIMITS, limits );

	}
	
	public String
	getRateLimits()
	{
		return( COConfigurationManager.getStringParameter( CONFIG_DL_RATE_LIMITS, "" ));
	}
	
	public void
	setActivateSubscriptionOnChange(
		boolean		b )
	{
		COConfigurationManager.setParameter( CONFIG_ACTIVATE_ON_CHANGE, b );

	}
	
	public boolean
	getActivateSubscriptionOnChange()
	{
		return( COConfigurationManager.getBooleanParameter( CONFIG_ACTIVATE_ON_CHANGE, false ));
	}
	
	public String
	getRSSLink()
	{
		return( rss_publisher.getFeedURL());
	}
	
	public Subscription 
	create(
		String			name,
		boolean			public_subs,
		String			json )
	
		throws SubscriptionException 
	{
		name = getUniqueName( name );
		
		boolean is_anonymous = false;
		
		SubscriptionImpl subs = new SubscriptionImpl( this, name, public_subs, is_anonymous, null, json, SubscriptionImpl.ADD_TYPE_CREATE );
		
		log( "Created new subscription: " + subs.getString());
		
		if ( subs.isPublic()){
			
			updatePublicSubscription( subs );
		}
		
		return( addSubscription( subs ));
	}
	
	public Subscription
	createSingletonRSS(
		String		name,
		URL			url,
		int			check_interval_mins,
		boolean		is_anon )
		
		throws SubscriptionException
	{
		return( createSingletonRSSSupport( name, url, true, check_interval_mins, is_anon, SubscriptionImpl.ADD_TYPE_CREATE, true ));
	}
	
	public Subscription
	createFromURI(
		String		uri )
		
		throws SubscriptionException
	{
		final AESemaphore sem = new AESemaphore( "subswait" );
		
		final Object[] result = { null };
		
		byte[] 	sid 		= null;
		int		version		= -1;
		boolean	is_anon		= false;
		
		int	pos = uri.indexOf( '?' );
		
		String[] bits = uri.substring(pos+1).split( "&" );
		
		for ( String bit: bits ){
			
			String[] temp = bit.split( "=" );
			
			if ( temp.length != 2 ){
				
				continue;
			}
			
			String lhs 	= temp[0].toLowerCase( Locale.US );
			String	rhs	= temp[1];

			switch (lhs) {
				case "id":

					sid = Base32.decode(rhs);

					break;
				case "v":

					version = Integer.parseInt(rhs);

					break;
				case "a":

					is_anon = rhs.equals("1");
					break;
			}
		}
		
		if ( sid == null || version == -1 ){
			
			throw( new SubscriptionException( "Invalid URI" ));
		}
		
		lookupSubscription( 
			new byte[20], 
			sid, 
			version, 
			is_anon,
			new subsLookupListener() {
				
				public void 
				found(
					byte[] 			hash, 
					Subscription 	subscription ) 
				{
				}
				
				public void 
				failed(
					byte[] 					hash, 
					SubscriptionException 	error ) 
					
				{	
					synchronized( result ){
						
						result[0] = error;
					}
					
					sem.release();
				}
				
				public void 
				complete(
					byte[] 			hash, 
					Subscription[] 	subscriptions )
				{
					synchronized( result ){
						
						if ( subscriptions.length > 0 ){
						
							result[0] = subscriptions[0];
							
						}else{
							
							result[0] = new SubscriptionException( "Subscription not found" );
						}
					}
					
					sem.release();
				}
				
				public boolean 
				isCancelled() 
				{
					return( false );
				}
			});
		
		sem.reserve();
		
		if ( result[0] instanceof Subscription ){
			
			return((Subscription)result[0]);
			
		}else{
			
			throw((SubscriptionException)result[0]);
		}
	}
	
	protected SubscriptionImpl
	lookupSingletonRSS(
		String		name,
		URL			url,
		boolean		is_public,
		int			check_interval_mins,
		boolean		is_anon )
		
		throws SubscriptionException
	{
		checkURL( url );
		
		Map	singleton_details = getSingletonMap(name, url, is_public, check_interval_mins, is_anon);
		
		byte[] sid = SubscriptionBodyImpl.deriveSingletonShortID(singleton_details);
		
		return( getSubscriptionFromSID( sid ));
	}
	
			
	protected Subscription
	createSingletonRSSSupport(
		String		name,
		URL			url,
		boolean		is_public,
		int			check_interval_mins,
		boolean		is_anon,
		int			add_type,
		boolean		subscribe )
		
		throws SubscriptionException
	{
		checkURL( url );
		
		try{
			Subscription existing = lookupSingletonRSS( name, url, is_public, check_interval_mins, is_anon );
			
			if ( existing != null ){
				
				return( existing );
			}
			
			Engine engine = MetaSearchManagerFactory.getSingleton().getMetaSearch().createRSSEngine( name, url );
			
			String	json = SubscriptionImpl.getSkeletonJSON( engine, check_interval_mins );
			
			Map	singleton_details = getSingletonMap(name, url, is_public, check_interval_mins, is_anon );
						
			SubscriptionImpl subs = new SubscriptionImpl( this, name, is_public, is_anon, singleton_details, json, add_type );
			
			subs.setSubscribed( subscribe );
			
			log( "Created new singleton subscription: " + subs.getString());
							
			subs = addSubscription( subs );
			
			if ( subs.isPublic() && subs.isMine() && subs.isSearchTemplate()){
				
				updatePublicSubscription( subs );
			}
			
			return( subs );
			
		}catch( SubscriptionException e ){
			
			throw e;
			
		}catch( Throwable e ){
			
			throw( new SubscriptionException( "Failed to create subscription", e ));
		}
	}
	
	protected String
	getUniqueName(
		String	name )
	{
		for ( int i=0;i<1024;i++){
			
			String	test_name = name + (i==0?"":(" (" + i + ")"));
			
			if ( getSubscriptionFromName( test_name ) == null ){

				return( test_name );
			}
		}
		
		return( name );
	}
	
	protected Map
	getSingletonMap(
		String		name,
		URL			url,
		boolean		is_public,
		int			check_interval_mins,
		boolean		is_anon )
	
		throws SubscriptionException
	{
		try{
			Map	singleton_details = new HashMap();
			
			if ( url.getProtocol().equalsIgnoreCase( "vuze" )){
				
					// hack to minimise encoded url length for our own urls
				
				singleton_details.put( "key", url.toExternalForm().getBytes( Constants.BYTE_ENCODING ));

			}else{
				singleton_details.put( "key", url.toExternalForm().getBytes(StandardCharsets.UTF_8));
			}
						
			String	name2 = name.length() > 64?name.substring(0,64):name;
			
			singleton_details.put( "name", name2 );
			
			if ( check_interval_mins != SubscriptionHistoryImpl.DEFAULT_CHECK_INTERVAL_MINS ){
				
				singleton_details.put( "ci", (long) check_interval_mins);
			}
			
			if ( is_anon ){
				
				singleton_details.put( "a", 1L);
			}
			
			return( singleton_details );
		
		}catch( Throwable e ){
			
			throw( new SubscriptionException( "Failed to create subscription", e ));
		}
	}
	
	protected SubscriptionImpl
	createSingletonSubscription(
		Map			singleton_details,
		int			add_type,
		boolean		subscribe )
	
		throws SubscriptionException 
	{
		try{
			String name = ImportExportUtils.importString( singleton_details, "name", "(Anonymous)" );
			
			URL	url = new URL( ImportExportUtils.importString( singleton_details, "key" ));
			
			int	check_interval_mins = (int)ImportExportUtils.importLong( singleton_details, "ci", SubscriptionHistoryImpl.DEFAULT_CHECK_INTERVAL_MINS );
			
			boolean	is_anon = ImportExportUtils.importLong( singleton_details, "a", 0 ) != 0;
			
				// only defined type is singleton rss
			
			SubscriptionImpl s = (SubscriptionImpl)createSingletonRSSSupport( name, url, true, check_interval_mins, is_anon, add_type, subscribe );
			
			return( s );
			
		}catch( Throwable e ){
			
			log( "Creation of singleton from " + singleton_details + " failed", e );
			
			throw( new SubscriptionException( "Creation of singleton from " + singleton_details + " failed", e ));
		}
	}
	
	public void
	requestSubscription(
		URL						url,
		Map<String, Object> 	options )
	{
		for ( SubscriptionManagerListener listener: listeners ){
			
			try{
				listener.subscriptionRequested( url, options );
				
			}catch( Throwable e ){
				
				Debug.out( e );
			}
		}
	}
	
	public void 
	requestSubscription(
		SearchProvider 			sp,
		Map<String, Object> 	search_parameters) 
		
		throws org.gudy.azureus2.plugins.utils.subscriptions.SubscriptionException
	{		
		try{
			Engine engine = MetaSearchManagerFactory.getSingleton().getEngine( sp );

			if ( engine == null ){
				
				throw( new SubscriptionException( "Engine not found "));
			}
			
			Boolean		anonymous	= (Boolean)search_parameters.get( "_anonymous_" );

			String		term 		= (String)search_parameters.get( SearchProvider.SP_SEARCH_TERM );
			String[]	networks 	= (String[])search_parameters.get( SearchProvider.SP_NETWORKS );
			
			String networks_str = null;
			
			if ( networks != null && networks.length > 0 ){
				
				networks_str = "";
				
				for ( String network: networks ){
					
					networks_str += (networks_str.length()==0?"":",") + network;
				}
			}
			
			String	json = SubscriptionImpl.getSkeletonJSON( engine, term, networks_str, 60 );
				
			String	name 	= (String)search_parameters.get( SearchProvider.SP_SEARCH_NAME );

			if ( name == null || name.length() == 0 ){
			
				name = engine.getName() + ": " + search_parameters.get( SearchProvider.SP_SEARCH_TERM );
			}
			
			boolean anon = anonymous!=null&&anonymous;
			
			SubscriptionImpl subs = new SubscriptionImpl( this, name, engine.isPublic(), anon, null, json, SubscriptionImpl.ADD_TYPE_CREATE );
			
			if ( anon ){
				
				subs.getHistory().setDownloadNetworks( new String[]{ AENetworkClassifier.AT_I2P });
			}
			
			log( "Created new subscription: " + subs.getString());
					
			subs = addSubscription( subs );
			
			Number		freq	= (Number)search_parameters.get( "_frequency_" );

			if ( freq != null ){
			
				subs.getHistory().setCheckFrequencyMins( freq.intValue());
			}
			
			if ( subs.isPublic()){
			
				updatePublicSubscription( subs );
			}
		
			Boolean		silent	= (Boolean)search_parameters.get( "_silent_" );

			if ( silent == null || !silent ){
			
				subs.requestAttention();
			}
		}catch( Throwable e ){
			
			throw( new org.gudy.azureus2.plugins.utils.subscriptions.SubscriptionException( "Failed to create subscription", e ));
		}
	}
	
	public Subscription 
	createRSS(
		String		name,
		URL			url,
		int			check_interval_mins,
		Map			user_data )
	
		throws SubscriptionException 
	{
		return( createRSS( name, url, check_interval_mins, false, user_data ));
	}
	
	public Subscription 
	createRSS(
		String		name,
		URL			url,
		int			check_interval_mins,
		boolean		is_anonymous,
		Map			user_data )
	
		throws SubscriptionException 
	{
		checkURL( url );
		
		try{
			name = getUniqueName(name);
		
			Engine engine = MetaSearchManagerFactory.getSingleton().getMetaSearch().createRSSEngine( name, url );
			
			String	json = SubscriptionImpl.getSkeletonJSON( engine, check_interval_mins );
			
				// engine name may have been modified so re-read it for subscription default
						
			SubscriptionImpl subs = new SubscriptionImpl( this, engine.getName(), engine.isPublic(), is_anonymous, null, json, SubscriptionImpl.ADD_TYPE_CREATE );
			
			if ( user_data != null ){

                for (Object o : user_data.entrySet()) {

                    Map.Entry entry = (Map.Entry) o;

                    subs.setUserData(entry.getKey(), entry.getValue());
                }
			}
			
			log( "Created new subscription: " + subs.getString());
					
			subs = addSubscription( subs );
			
			if ( subs.isPublic()){
			
				updatePublicSubscription( subs );
			}
			
			return( subs );
			
		}catch( Throwable e ){
			
			throw( new SubscriptionException( "Failed to create subscription", e ));
		}
	}

	protected void
	checkURL(
		URL		url )
	
		throws SubscriptionException
	{
		if ( url.getHost().trim().length() == 0 ){
			
			String protocol = url.getProtocol().toLowerCase();
			
			if ( ! ( protocol.equals( "azplug" ) || protocol.equals( "file" ) || protocol.equals( "vuze" ))){
			
				throw( new SubscriptionException( "Invalid URL '" + url + "'" ));
			}
		}
	}
	
	protected SubscriptionImpl
	addSubscription(
		SubscriptionImpl		subs )
	{
		SubscriptionImpl existing;
		
		synchronized( this ){

			int index = Collections.binarySearch(subscriptions, subs, new Comparator<Subscription>() {
				public int compare(Subscription arg0, Subscription arg1) {
					return arg0.getID().compareTo(arg1.getID());
				}
			});
			if (index < 0) {
				existing = null;
				index = -1 * index - 1; // best guess

				subscriptions.add( index, subs );
			
				saveConfig();
			} else {
				existing = subscriptions.get(index);
			}
		}
		
		if ( existing != null ){
			
			log( "Attempted to add subscription when already present: " + subs.getString());
			
			subs.destroy();
			
			return( existing );
		}
		
		if ( subs.isMine()){
			
			addMetaSearchListener();
		}
		
		if ( subs.getCachedPopularity() == -1 ){
			
			try{
				subs.getPopularity(
					new SubscriptionPopularityListener()
					{
						public void
						gotPopularity(
							long						popularity )
						{
						}
						
						public void
						failed(
							SubscriptionException		error )
						{
						}
					});
				
			}catch( Throwable e ){
				
				log( "", e );
			}
		}

        for (SubscriptionManagerListener listener : listeners) {

            try {
                (listener).subscriptionAdded(subs);

            } catch (Throwable e) {

                Debug.printStackTrace(e);
            }
        }
		
		if ( subs.isSubscribed() && subs.isPublic()){
			
			setSelected( subs );
		}
		
		if ( dht_plugin_public != null ){
				
			new AEThread2( "Publish check", true )
			{
				public void
				run()
				{
					publishSubscriptions();
				}
			}.start();
		}
		
		return( subs );
	}
	
	protected void
	addMetaSearchListener()
	{
		synchronized( this ){
			
			if ( meta_search_listener_added ){
				
				return;
			}
			
			meta_search_listener_added = true;
		}
		
		MetaSearchManagerFactory.getSingleton().getMetaSearch().addListener(
			new MetaSearchListener()
			{
				public void
				engineAdded(
					Engine		engine )
				{					
				}
				
				public void
				engineUpdated(
					Engine		engine )
				{
					synchronized( SubscriptionManagerImpl.this ){

                        for (SubscriptionImpl subs : subscriptions) {

                            if (subs.isMine()) {

                                subs.engineUpdated(engine);
                            }
                        }
					}
				}
				
				public void
				engineRemoved(
					Engine		engine )
				{					
				}
				
				public void 
				engineStateChanged(
					Engine engine) 
				{
				}
			});
	}
	
	protected void
	changeSubscription(
		SubscriptionImpl		subs )
	{
		if ( !subs.isRemoved()){

            for (SubscriptionManagerListener listener : listeners) {

                try {
                    (listener).subscriptionChanged(subs);

                } catch (Throwable e) {

                    Debug.printStackTrace(e);
                }
            }
		}
	}
	
	protected void
	selectSubscription(
		SubscriptionImpl		subs )
	{
		if ( !subs.isRemoved()){

            for (SubscriptionManagerListener listener : listeners) {

                try {
                    (listener).subscriptionSelected(subs);

                } catch (Throwable e) {

                    Debug.printStackTrace(e);
                }
            }
		}
	}
	
	protected void
	removeSubscription(
		SubscriptionImpl		subs )
	{
		synchronized( this ){
			
			if ( subscriptions.remove( subs )){
			
				saveConfig();
				
			}else{
			
				return;
			}
		}
		
		try{
			Engine engine = subs.getEngine( true );
			
			if ( engine.getType() == Engine.ENGINE_TYPE_RSS ){
				
				engine.delete();
				
				log( "Removed engine " + engine.getName() + " due to subscription removal" );
			}
			
		}catch( Throwable e ){
			
			log( "Failed to check for engine deletion", e );
		}

        for (SubscriptionManagerListener listener : listeners) {

            try {
                listener.subscriptionRemoved(subs);

            } catch (Throwable e) {

                Debug.printStackTrace(e);
            }
        }
		
		try{
			FileUtil.deleteResilientFile( getResultsFile( subs ));
			
			synchronized( result_cache ){
				
				result_cache.remove( subs );
			}
			
			File vuze_file = getVuzeFile( subs );
			
			vuze_file.delete();
			
			new File( vuze_file.getParent(), vuze_file.getName() + ".bak" ).delete();
			
		}catch( Throwable e ){
			
			log( "Failed to delete results/vuze file", e );
		}
	}
	
	private AsyncDispatcher	async_dispatcher = new AsyncDispatcher( "SubsManDispatcher");
	
	protected void
	updatePublicSubscription(
		final SubscriptionImpl		subs )
	{		
		if ( subs.isSingleton() && !( subs.isMine() && subs.isSearchTemplate())){
			
				// never update singletons
			
			subs.setServerPublished();
			
		}else{
				// the update is blocking on the messenger server and this method can be called
				// from the UI thread so back things off for the publishing
			
			final AESemaphore sem = new AESemaphore( "pub:async" );
			
			async_dispatcher.dispatch(
				new AERunnable()
				{	
					@Override
					public void 
					runSupport() 
					{
						try{
							Long	l_last_pub 	= (Long)subs.getUserData( SP_LAST_ATTEMPTED );
							Long	l_consec_fail = (Long)subs.getUserData( SP_CONSEC_FAIL );
							
							if ( l_last_pub != null && l_consec_fail != null ){
								
								long	delay = SERVER_PUB_CHECK_PERIOD;
								
								for (int i = 0; i< l_consec_fail; i++){
									
									delay <<= 1;
									
									if ( delay > 24*60*60*1000 ){
										
										break;
									}
								}
								
								if (l_last_pub + delay > SystemTime.getMonotonousTime()){
									
									return;
								}
							}
	
							try{		
								File vf = getVuzeFile( subs );
					
								byte[] bytes = FileUtil.readFileAsByteArray( vf );
								
								byte[]	encoded_subs = Base64.encode( bytes );
					
								PlatformSubscriptionsMessenger.updateSubscription(
										!subs.getServerPublished(),
										subs.getName(false),
										subs.getPublicKey(),
										subs.getPrivateKey(),
										subs.getShortID(),
										subs.getVersion(),
										subs.isAnonymous(),
										new String( encoded_subs ));
								
								subs.setUserData( SP_LAST_ATTEMPTED, null );
								subs.setUserData( SP_CONSEC_FAIL, null );
				
								subs.setServerPublished();
								
								log( "    Updated public subscription " + subs.getString());
								
							}catch( Throwable e ){
								
								log( "    Failed to update public subscription " + subs.getString(), e );
								
								subs.setUserData( SP_LAST_ATTEMPTED, SystemTime.getMonotonousTime());
								
								subs.setUserData( SP_CONSEC_FAIL, l_consec_fail == null ? 1 : (l_consec_fail + 1));
				
								subs.setServerPublicationOutstanding();
							}
						}finally{
							
							sem.release();
						}
					}
				});
			
			sem.reserve( 5000 );	// give it a chance to work synchronously
		}
	}
	
	protected void
	checkSingletonPublish(
		SubscriptionImpl		subs )
	
		throws SubscriptionException
	{
		if ( subs.getSingletonPublishAttempted()){
			
			throw( new SubscriptionException( "Singleton publish already attempted" ));
		}
		
		subs.setSingletonPublishAttempted();
		
		try{
			File vf = getVuzeFile( subs );
	
			byte[] bytes = FileUtil.readFileAsByteArray( vf );
			
			byte[]	encoded_subs = Base64.encode( bytes );
			
				// use a transient key-pair as we won't have the private key in general
			
			KeyPair	kp = CryptoECCUtils.createKeys();
			
			byte[] public_key 		= CryptoECCUtils.keyToRawdata( kp.getPublic());
			byte[] private_key 		= CryptoECCUtils.keyToRawdata( kp.getPrivate());
	
			PlatformSubscriptionsMessenger.updateSubscription(
					true,
					subs.getName(false),
					public_key,
					private_key,
					subs.getShortID(),
					1,
					subs.isAnonymous(),
					new String( encoded_subs ));
			
			log( "    created singleton public subscription " + subs.getString());
			
		}catch( Throwable e ){
			
			throw( new SubscriptionException( "Failed to publish singleton", e ));
		}
	}
	
	protected void
	checkServerPublications(
		List		subs )
	{
        for (Object sub1 : subs) {

            SubscriptionImpl sub = (SubscriptionImpl) sub1;

            if (sub.getServerPublicationOutstanding()) {

                updatePublicSubscription(sub);
            }
        }
	}
	
	private static final Object	SUBS_CHAT_KEY	= new Object();
	
	protected void
	checkStuff(
		int		ticks )
	{
		long now = SystemTime.getCurrentTime();
		
		List<SubscriptionImpl> subs;
		
		synchronized( this ){
			
			subs = new ArrayList<>(subscriptions);
		}
		
		SubscriptionImpl	expired_subs = null;

        for (SubscriptionImpl sub : subs) {

            if (!(sub.isMine() || sub.isSubscribed())) {

                long age = now - sub.getAddTime();

                if (age > DELETE_UNUSED_AFTER_MILLIS) {

                    if (expired_subs == null ||
                            (sub.getAddTime() < expired_subs.getAddTime())) {

                        expired_subs = sub;
                    }

                    continue;
                }
            }


            sub.checkPublish();
        }
		
		if ( expired_subs != null ){
			
			log( "Removing unsubscribed subscription '" + expired_subs.getName() + "' as expired" );
					
			expired_subs.remove();
		}
		
		if ( ticks % CHAT_CHECK_TICKS == 0 ){
			
			List<SubscriptionImpl> subs_copy = new ArrayList<>(subs);
			
			Collections.shuffle( subs_copy );
			
			long mono_now = SystemTime.getMonotonousTime();
			
			for ( final SubscriptionImpl sub: subs_copy ){
				
				if ( !sub.isSubscribed()){
					
					continue;
				}
				
				if ( sub.isSearchTemplate()){
					
					continue;
				}
				
				Long data = (Long)sub.getUserData( SUBS_CHAT_KEY );
				
				if ( data != null ){
					
					if ( data < 0 || mono_now - data < 4*60*60*1000 ){
						
						continue;
					}
				}
								
				String chat_key = SubscriptionUtils.getSubscriptionChatKey( sub );
				
				if ( chat_key != null ){
															
					sub.setUserData( SUBS_CHAT_KEY, -1L );
					
					SubscriptionUtils.peekChatAsync(
						sub.isAnonymous()?AENetworkClassifier.AT_I2P:AENetworkClassifier.AT_PUBLIC,
						chat_key,
						new Runnable()
						{
							public void 
							run() 
							{
								sub.setUserData( SUBS_CHAT_KEY, SystemTime.getMonotonousTime());
							}
						});
						
						// just fire off one at a time
					
					break;
					
				}else{
					
						// prevent future checks as no chat for this subs 
					
					sub.setUserData( SUBS_CHAT_KEY, -2L );
				}
			}
		}
		
		if ( ticks % ASSOC_CHECK_TICKS == 0 ){
			
			lookupAssociations( false );
		}
		
		if ( ticks % ASSOC_PUBLISH_TICKS == 0 ){
			
			int rem = getPublishRemainingCount();
			
			if ( rem == 0 ){
				
				log( "No associations to publish" );
				
			}else{
				
				log( rem + " associations remaining to publish" );
				
				publishAssociations();
			}
		}
		
		if ( ticks % SERVER_PUB_CHECK_TICKS == 0 ){
			
			checkServerPublications( subs );
		}
		
		if ( ticks % TIDY_POT_ASSOC_TICKS == 0 ){
			
			tidyPotentialAssociations();
		}
		
		if ( 	ticks == SET_SELECTED_FIRST_TICK ||
				ticks % SET_SELECTED_TICKS == 0 ){
			
			setSelected( subs );
		}
	}
	
	public Subscription
	importSubscription(
		int			type,
		Map			map,
		boolean		warn_user )
	
		throws SubscriptionException
	{
		boolean	log_errors = true;
		
		try{
			try{
				if ( type == VuzeFileComponent.COMP_TYPE_SUBSCRIPTION_SINGLETON ){
					
					String	name = new String((byte[])map.get( "name" ), StandardCharsets.UTF_8);
					
					URL	url = new URL( new String((byte[])map.get( "url" ), StandardCharsets.UTF_8));
					
					Long	l_interval = (Long)map.get( "check_interval_mins" );
					
					int	check_interval_mins = l_interval==null?SubscriptionHistoryImpl.DEFAULT_CHECK_INTERVAL_MINS:l_interval.intValue();
					
					Long	l_public = (Long)map.get( "public" );
					
					boolean is_public = l_public == null || l_public == 1;
					
					Long	l_anon = (Long)map.get( "anon" );
					
					boolean is_anon = l_anon != null && l_anon == 1;
					
					SubscriptionImpl existing = lookupSingletonRSS(name, url, is_public, check_interval_mins, is_anon );
					
					if ( UrlFilter.getInstance().urlCanRPC( url.toExternalForm())){
						
						warn_user = false;
					}
					
					if ( existing != null && existing.isSubscribed()){
						
						if ( warn_user ){
							
							UIManager ui_manager = StaticUtilities.getUIManager( 120*1000 );
							
							String details = MessageText.getString(
									"subscript.add.dup.desc",
									new String[]{ existing.getName()});
							
							ui_manager.showMessageBox(
									"subscript.add.dup.title",
									"!" + details + "!",
									UIManagerEvent.MT_OK );
						}
						
						selectSubscription( existing );
						
						return( existing );
						
					}else{
						
						if ( warn_user ){
							
							UIManager ui_manager = StaticUtilities.getUIManager( 120*1000 );
				
							String details = MessageText.getString(
									"subscript.add.desc",
									new String[]{ name });
							
							long res = ui_manager.showMessageBox(
									"subscript.add.title",
									"!" + details + "!",
									UIManagerEvent.MT_YES | UIManagerEvent.MT_NO );
							
							if ( res != UIManagerEvent.MT_YES ){	
							
								log_errors = false;
								
								throw( new SubscriptionException( "User declined addition" ));
							}
						}
						
						if ( existing == null ){
					
							SubscriptionImpl new_subs = (SubscriptionImpl)createSingletonRSSSupport( name, url, is_public, check_interval_mins, is_anon, SubscriptionImpl.ADD_TYPE_IMPORT, true );
																	
							log( "Imported new singleton subscription: " + new_subs.getString());
									
							return( new_subs );
							
						}else{
							
							existing.setSubscribed( true );
							
							selectSubscription( existing );
							
							return( existing );
						}
					}
				}else{
					
					SubscriptionBodyImpl body = new SubscriptionBodyImpl( this, map );
							
					SubscriptionImpl existing = getSubscriptionFromSID( body.getShortID());
					
					if ( existing != null && existing.isSubscribed()){
					
						if ( existing.getVersion() >= body.getVersion()){
							
							log( "Not upgrading subscription: " + existing.getString() + " as supplied (" +  body.getVersion() + ") is not more recent than existing (" + existing.getVersion() + ")");
							
							if ( warn_user ){
								
								UIManager ui_manager = StaticUtilities.getUIManager( 120*1000 );
								
								String details = MessageText.getString(
										"subscript.add.dup.desc",
										new String[]{ existing.getName()});
								
								ui_manager.showMessageBox(
										"subscript.add.dup.title",
										"!" + details + "!",
										UIManagerEvent.MT_OK );
							}
								// we have a newer one, ignore
							
							selectSubscription( existing );
							
							return( existing );
							
						}else{
							
							if ( warn_user ){
								
								UIManager ui_manager = StaticUtilities.getUIManager( 120*1000 );
				
								String details = MessageText.getString(
										"subscript.add.upgrade.desc",
										new String[]{ existing.getName()});
								
								long res = ui_manager.showMessageBox(
										"subscript.add.upgrade.title",
										"!" + details + "!",
										UIManagerEvent.MT_YES | UIManagerEvent.MT_NO );
								
								if ( res != UIManagerEvent.MT_YES ){	
								
									throw( new SubscriptionException( "User declined upgrade" ));
								}
							}
							
							log( "Upgrading subscription: " + existing.getString());
			
							existing.upgrade( body );
							
							saveConfig();
							
							subscriptionUpdated();
							
							return( existing );
						}
					}else{
						
						SubscriptionImpl new_subs = null;
						
						String	subs_name;
						
						if ( existing == null ){
							
							new_subs = new SubscriptionImpl( this, body, SubscriptionImpl.ADD_TYPE_IMPORT, true );
							
							subs_name = new_subs.getName();
							
						}else{
							
							subs_name = existing.getName();
						}
						
						if ( warn_user ){
							
							UIManager ui_manager = StaticUtilities.getUIManager( 120*1000 );
				
							String details = MessageText.getString(
									"subscript.add.desc",
									new String[]{ subs_name });
							
							long res = ui_manager.showMessageBox(
									"subscript.add.title",
									"!" + details + "!",
									UIManagerEvent.MT_YES | UIManagerEvent.MT_NO );
							
							if ( res != UIManagerEvent.MT_YES ){	
							
								throw( new SubscriptionException( "User declined addition" ));
							}
						}
						
						if ( new_subs == null ){
							
							existing.setSubscribed( true );
							
							selectSubscription( existing );
							
							return( existing );
							
						}else{
							
							log( "Imported new subscription: " + new_subs.getString());
						
							new_subs = addSubscription( new_subs );
						
							return( new_subs );
						}
					}
				}
			}catch( Throwable e ){
				
				throw( new SubscriptionException( "Subscription import failed", e ));
			}
		}catch( SubscriptionException e ){
			
			if ( warn_user && log_errors ){
				
				UIManager ui_manager = StaticUtilities.getUIManager( 120*1000 );
				
				String details = MessageText.getString(
						"subscript.import.fail.desc",
						new String[]{ Debug.getNestedExceptionMessage(e)});
				
				ui_manager.showMessageBox(
						"subscript.import.fail.title",
						"!" + details + "!",
						UIManagerEvent.MT_OK );
			}
			
			throw( e );
		}
	}
	
	public Subscription[]
	getSubscriptions()
	{
		synchronized( this ){
			
			return subscriptions.toArray(new SubscriptionImpl[0]);
		}
	}
	
	public Subscription[]
	getSubscriptions(
		boolean	subscribed_only )
	{
		if ( !subscribed_only ){
			
			return( getSubscriptions());
		}
		
		List	result = new ArrayList();
				
		synchronized( this ){

            for (SubscriptionImpl subs : subscriptions) {

                if (subs.isSubscribed()) {

                    result.add(subs);
                }
            }
		}
		
		return((SubscriptionImpl[])result.toArray(new SubscriptionImpl[0]));
	}
	
	public int
	getSubscriptionCount(
		boolean	subscribed_only )
	{
		if ( subscribed_only ){
	
			int total = 0;
			
			synchronized( this ){
				
				for ( Subscription subs: subscriptions ){
					
					if ( subs.isSubscribed()){
						
						total++;
					}
				}
			}
			
			return( total );
			
		}else{
			
			synchronized( this ){
				
				return( subscriptions.size());
			}
		}
	}

	protected SubscriptionImpl
	getSubscriptionFromName(
		String		name )
	{
		synchronized( this ){

            for (SubscriptionImpl s : subscriptions) {

                if (s.getName().equalsIgnoreCase(name)) {

                    return (s);
                }
            }
		}
		
		return( null );
	}
	
	public Subscription
	getSubscriptionByID(
		String		id )
	{
		synchronized( this ){
			
  		int index = Collections.binarySearch(subscriptions, id, new Comparator() {
  			public int compare(Object o1, Object o2) {
  				String id1 = (o1 instanceof Subscription) ? ((Subscription) o1).getID() : o1.toString();
  				String id2 = (o2 instanceof Subscription) ? ((Subscription) o2).getID() : o2.toString();
  				return id1.compareTo(id2);
  			}
  		});
  		
  		if (index >= 0) {
  			return subscriptions.get(index);
  		}
		}
		
		return null;
	}
	
	protected SubscriptionImpl
	getSubscriptionFromSID(
		byte[]		sid )
	{
		return (SubscriptionImpl) getSubscriptionByID( Base32.encode(sid));
	}
	
	protected File
	getSubsDir()
	
		throws IOException
	{
		File dir = new File(SystemProperties.getUserPath());

		dir = new File( dir, "subs" );
 		
 		if ( !dir.exists()){
 			
 			if ( !dir.mkdirs()){
 				
 				throw( new IOException( "Failed to create '" + dir + "'" ));
 			}
 		}	
 		
 		return( dir );
	}
	
	protected File
	getVuzeFile(
		SubscriptionImpl 		subs )
	
		throws IOException
	{
 		File dir = getSubsDir();
 		
 		return( new File( dir, ByteFormatter.encodeString( subs.getShortID()) + ".vuze" ));
	}
	
	protected File
	getResultsFile(
		SubscriptionImpl 		subs )
	
		throws IOException
	{
 		File dir = getSubsDir();
 		
 		return( new File( dir, ByteFormatter.encodeString( subs.getShortID()) + ".results" ));
	}
	
	public int
	getKnownSubscriptionCount()
	{
		PluginInterface pi = PluginInitializer.getDefaultInterface();

		Download[] downloads = pi.getDownloadManager().getDownloads();

		ByteArrayHashMap<String> results = new ByteArrayHashMap<>(Math.max(16, downloads.length * 2));
		
		try{			
			for ( Download download: downloads ){
				
				Map	m = download.getMapAttribute( ta_subscription_info );
				
				if ( m != null ){
					
					List s = (List)m.get("s");
					
					if ( s != null && s.size() > 0 ){

                        for (Object o : s) {

                            byte[] sid = (byte[]) o;

                            results.put(sid, "");
                        }
					}
				}
			}
		}catch( Throwable e ){
			
			log( "Failed to get known subscriptions", e );
		}
		
		return( results.size());
	}
	
	public Subscription[]
	getKnownSubscriptions(
		byte[]						hash )
	{
		PluginInterface pi = PluginInitializer.getDefaultInterface();

		try{
			Download download = pi.getDownloadManager().getDownload( hash );
			
			if ( download != null ){
				
				Map	m = download.getMapAttribute( ta_subscription_info );
				
				if ( m != null ){
					
					List s = (List)m.get("s");
					
					if ( s != null && s.size() > 0 ){
						
						List	result = new ArrayList( s.size());
						
						boolean hide_search = hideSearchTemplates();

                        for (Object o : s) {

                            byte[] sid = (byte[]) o;

                            SubscriptionImpl subs = getSubscriptionFromSID(sid);

                            if (subs != null) {

                                if (isVisible(subs)) {

                                    if (hide_search && subs.isSearchTemplate()) {

                                    } else {

                                        result.add(subs);
                                    }
                                }
                            }
                        }
						
						return((Subscription[])result.toArray(new Subscription[0]));
					}
				}
			}
		}catch( Throwable e ){
			
			log( "Failed to get known subscriptions", e );
		}
		
		return( new Subscription[0] );
	}
	
	protected boolean
	subscriptionExists(
		Download			download,
		SubscriptionImpl	subs )
	{
		byte[]	sid = subs.getShortID();
	
		Map	m = download.getMapAttribute( ta_subscription_info );
		
		if ( m != null ){
			
			List s = (List)m.get("s");
			
			if ( s != null && s.size() > 0 ){

                for (Object o : s) {

                    byte[] x = (byte[]) o;

                    if (Arrays.equals(x, sid)) {

                        return (true);
                    }
                }
			}
		}
		
		return( false );
	}
	
	private boolean
	downloadIsIgnored(
		Download		download )
	{
		if ( download.getTorrent() == null || !download.isPersistent()){
			
			return( true );
		}
		
		return( false );
	}
	
	protected boolean
	isVisible(
		SubscriptionImpl		subs )
	{
			// to avoid development links polluting production we filter out such subscriptions
		
		if ( Constants.isCVSVersion() || subs.isSubscribed()){
			
			return( true );
		}
		
		try{
			Engine engine = subs.getEngine( true );
			
			if ( engine instanceof WebEngine ){
				
				String url = ((WebEngine)engine).getSearchUrl();
				
				try{
					String host = new URL( url ).getHost();
					
					return( !exclusion_pattern.matcher( host ).matches());
					
				}catch( Throwable e ){
				}
			}
			
			return( true );
			
		}catch( Throwable e ){
			
			log( "isVisible failed for " + subs.getString(), e );
			
			return( false );
		}
	}
		
	public Subscription[]
	getLinkedSubscriptions(
		byte[]						hash )
	{
		PluginInterface pi = PluginInitializer.getDefaultInterface();

		try{
			Download download = pi.getDownloadManager().getDownload( hash );
			
			if ( download != null ){
				
				Map	m = download.getMapAttribute( ta_subscription_info );
				
				if ( m != null ){
					
					List s = (List)m.get("s");
					
					if ( s != null && s.size() > 0 ){
						
						List	result = new ArrayList( s.size());

                        for (Object o : s) {

                            byte[] sid = (byte[]) o;

                            SubscriptionImpl subs = getSubscriptionFromSID(sid);

                            if (subs != null) {

                                if (subs.hasAssociation(hash)) {

                                    result.add(subs);
                                }
                            }
                        }
						
						return((Subscription[])result.toArray(new Subscription[0]));
					}
				}
			}
		}catch( Throwable e ){
			
			log( "Failed to get known subscriptions", e );
		}
		
		return( new Subscription[0] );
	}
	
	protected void
	lookupAssociations(
		boolean		high_priority )
	{
		synchronized( this ){
			
			if ( periodic_lookup_in_progress ){
				
				if ( high_priority ){
				
					priority_lookup_pending++;
				}
				
				return;
			}
			
			periodic_lookup_in_progress  = true;
		}
		
		try{
			PluginInterface pi = PluginInitializer.getDefaultInterface();
			
			Download[] downloads = pi.getDownloadManager().getDownloads();
					
			long	now = SystemTime.getCurrentTime();
			
			long		newest_time		= 0;
			Download	newest_download	= null;


            for (Download download : downloads) {

                if (downloadIsIgnored(download)) {

                    continue;
                }

                Map map = download.getMapAttribute(ta_subscription_info);

                if (map == null) {

                    map = new LightHashMap();

                } else {

                    map = new LightHashMap(map);
                }

                Long l_last_check = (Long) map.get("lc");

                long last_check = l_last_check == null ? 0 : l_last_check;

                if (last_check > now) {

                    last_check = now;

                    map.put("lc", last_check);

                    download.setMapAttribute(ta_subscription_info, map);
                }

                List subs = (List) map.get("s");

                int sub_count = subs == null ? 0 : subs.size();

                if (sub_count > 8) {

                    continue;
                }

                long create_time = download.getCreationTime();

                int time_between_checks = (sub_count + 1) * 24 * 60 * 60 * 1000 + (int) (create_time % 4 * 60 * 60 * 1000);

                if (now - last_check >= time_between_checks) {

                    if (create_time > newest_time) {

                        newest_time = create_time;
                        newest_download = download;
                    }
                }
            }
			
			if ( newest_download != null ){
			
				DHTPluginInterface dht_plugin = selectDHTPlugin( newest_download );
				
				if ( dht_plugin != null ){
					
					byte[] hash = newest_download.getTorrent().getHash();
					
					log( "Association lookup starts for " + newest_download.getName() + "/" + ByteFormatter.encodeString( hash ));
	
					lookupAssociationsSupport( 
						dht_plugin,
						hash,
						new	SubscriptionLookupListener()
						{
							public void
							found(
								byte[]					hash,
								Subscription			subscription )
							{							
							}
							
							public void
							failed(
								byte[]					hash,
								SubscriptionException	error )
							{
								log( "Association lookup failed for " + ByteFormatter.encodeString( hash ), error );
	
								associationLookupComplete();
							}
							
							public void 
							complete(
								byte[] 			hash,
								Subscription[]	subs )
							{
								log( "Association lookup complete for " + ByteFormatter.encodeString( hash ));
								
								associationLookupComplete();
							}
						});
				}else{	
					
					associationLookupComplete();
				}
			}else{
				
				associationLookupComplete();
			}
		}catch( Throwable e ){
			
			log( "Association lookup check failed", e );

			associationLookupComplete();			
		}
	}
	
	protected void
	associationLookupComplete()
	{
		boolean	recheck;
		
		synchronized( this ){
			
			periodic_lookup_in_progress = false;
			
			recheck = priority_lookup_pending > 0;
				
			if ( recheck ){
				
				priority_lookup_pending--;
			}
		}
		
		if ( recheck ){
			
			new AEThread2( "SM:priAssLookup", true )
			{
				public void run() 
				{
					lookupAssociations( false );
				}
			}.start();
		}
	}
	
	protected void
	setSelected(
		List		subs )
	{
		List<byte[]>			sids 		= new ArrayList<>();
		List<SubscriptionImpl>	used_subs	= new ArrayList<>();
		
		final List<SubscriptionImpl> dht_pops = new ArrayList<>();

        for (Object sub1 : subs) {

            SubscriptionImpl sub = (SubscriptionImpl) sub1;

            if (sub.isSubscribed()) {

                if (sub.isPublic()) {

                    if (!sub.isAnonymous()) {

                        used_subs.add(sub);

                        sids.add(sub.getShortID());

                    } else {

                        dht_pops.add(sub);
                    }
                } else {

                    checkInitialDownload(sub);
                }
            }
        }
		
		if ( sids.size() > 0 ){
			
			try{
				List[] result = PlatformSubscriptionsMessenger.setSelected( sids );
				
				List<Long>	versions 		= result[0];
				List<Long>	popularities	= result[1];
				
				log( "Popularity update: updated " + sids.size());
								
				for (int i=0;i<sids.size();i++){
					
					SubscriptionImpl sub = used_subs.get(i);
					
					int	latest_version = versions.get(i).intValue();
					
					if ( latest_version > sub.getVersion()){
						
						updateSubscription( sub, latest_version );
						
					}else{
						
						checkInitialDownload( sub );
					}
					
					if ( latest_version > 0 ){
						
						try{
							long	pop = popularities.get(i);
							
							if ( pop >= 0 && pop != sub.getCachedPopularity()){
								
								sub.setCachedPopularity( pop );
							}
						}catch( Throwable e ){
							
							log( "Popularity update: Failed to extract popularity", e );
						}
					}else{
						
						dht_pops.add( sub );
					}
				}
				
			}catch( Throwable e ){
				
				log( "Popularity update: Failed to record selected subscriptions", e );
			}
		}else{
			
			log( "Popularity update: No selected, public subscriptions" );
		}
		
		if ( dht_pops.size() <= 3 ){

            for (SubscriptionImpl dht_pop : dht_pops) {

                updatePopularityFromDHT(dht_pop, false);
            }
		}else{
			
			new AEThread2( "SM:asyncPop", true )
			{
				public void
				run()
				{
                    for (SubscriptionImpl dht_pop : dht_pops) {

                        updatePopularityFromDHT(dht_pop, true);
                    }
				}
			}.start();
		}
	}
	
	protected void
	checkUpgrade(
		SubscriptionImpl		sub )
	{
		setSelected( sub );
	}
	
	protected void
	setSelected(
		final SubscriptionImpl	sub )
	{
		if ( sub.isSubscribed()){
			
			if ( sub.isPublic()){
			
				new DelayedEvent( 
					"SM:setSelected",
					0,
					new AERunnable()
					{
						public void
						runSupport()
						{
							try{
								if ( !sub.isAnonymous()){
									
									List	sids = new ArrayList();
									
									sids.add( sub.getShortID());
								
									List[] result = PlatformSubscriptionsMessenger.setSelected( sids );
									
									log( "setSelected: " + sub.getName());
									
									int	latest_version = ((Long)result[0].get(0)).intValue();
									
									if ( latest_version == 0 ){
										
										if ( sub.isSingleton()){
										
											checkSingletonPublish( sub );
										}
									}else if ( latest_version > sub.getVersion()){
										
										updateSubscription( sub, latest_version );										
									}
									
									if ( latest_version > 0 ){
										
										try{
											long	pop = (Long) result[1].get(0);
											
											if ( pop >= 0 && pop != sub.getCachedPopularity()){
												
												sub.setCachedPopularity( pop );
											}
										}catch( Throwable e ){
											
											log( "Popularity update: Failed to extract popularity", e );
										}
									}else{
										
										updatePopularityFromDHT( sub, true );
									}
								}else{
									
									updatePopularityFromDHT( sub, true );
								}
							}catch( Throwable e ){
								
								log( "setSelected: failed for " + sub.getName(), e );
								
							}finally{
								
								checkInitialDownload( sub );
							}
						}
					});
			}else{
				
				checkInitialDownload( sub );
			}
		}
	}
	
	protected void
	checkInitialDownload(
		SubscriptionImpl		subs )
	{
		if ( subs.getHistory().getLastScanTime() == 0 ){
			
			scheduler.download( 
				subs, 
				true,
				new SubscriptionDownloadListener()
				{
					public void
					complete(
						Subscription		subs )
					{
						log( "Initial download of " + subs.getName() + " complete" );
					}
					
					public void
					failed(
						Subscription			subs,
						SubscriptionException	error )
					{
						log( "Initial download of " + subs.getName() + " failed", error );
					}
				});
		}
	}
	
	public SubscriptionAssociationLookup
	lookupAssociations(
		byte[]						hash,
		String[]					networks,
		SubscriptionLookupListener	listener )
	
		throws SubscriptionException
	{
		return( lookupAssociations( selectDHTPlugin(networks), hash, listener ));
	}
	
	public SubscriptionAssociationLookup
	lookupAssociations(
		final byte[] 							hash,
		final SubscriptionLookupListener		listener )
	
		throws SubscriptionException
	{
		DHTPluginInterface	dht_plugin;
		
		try{
			Download download = PluginInitializer.getDefaultInterface().getDownloadManager().getDownload( hash );
			
			if ( download != null ){
			
				dht_plugin = selectDHTPlugin(download);
	
			}else{
				
				dht_plugin = dht_plugin_public;
			}
		}catch( Throwable e ){
			
			dht_plugin = dht_plugin_public;
		}
		
		return( lookupAssociations( dht_plugin, hash, listener ));
	}
	
	private SubscriptionAssociationLookup
	lookupAssociations(
		DHTPluginInterface						dht_plugin,
		final byte[] 							hash,
		final SubscriptionLookupListener		listener )
	
		throws SubscriptionException
	{	
		if ( dht_plugin != null ){
			
			if ( !dht_plugin.isInitialising()){
				
				return( lookupAssociationsSupport( dht_plugin, hash, listener ));
			}
			
			final boolean[]	cancelled 	= { false };
			final long[] 	timeout		= { 0 };
			
			final SubscriptionAssociationLookup[]	actual_res = { null };
			
			final SubscriptionAssociationLookup res = 
				new SubscriptionAssociationLookup()
				{
					public void 
					cancel() 
					{
						log( "    Association lookup cancelled" );
		
						synchronized( actual_res ){
							
							cancelled[0] = true;
							
							if ( actual_res[0] != null ){
								
								actual_res[0].cancel();
							}
						}
					}
					
					public void setTimeout(long millis){
						
						synchronized( actual_res ){
							
							timeout[0] = millis;
							
							if ( actual_res[0] != null ){
								
								actual_res[0].setTimeout( millis );
							}
						}
					}
				};
			
			final DHTPluginInterface f_dht_plugin = dht_plugin;
				
			new AEThread2( "SM:initwait", true )
			{
				public void
				run()
				{
					try{
						SubscriptionAssociationLookup x = lookupAssociationsSupport( f_dht_plugin, hash, listener );
						
						synchronized( actual_res ){
	
							actual_res[0] = x;
							
							if ( cancelled[0] ){
								
								x.cancel();
							}
							
							if ( timeout[0] != 0 ){
								
								x.setTimeout( timeout[0] );
							}
						}
						
					}catch( SubscriptionException e ){
						
						listener.failed( hash, e );
					}
					
				}
			}.start();
			
			return( res );
			
		}else{
			
			throw( new SubscriptionException( "No DHT available" ));
		}
	}
	
	protected SubscriptionAssociationLookup
	lookupAssociationsSupport(
		final DHTPluginInterface				dht_plugin,
		final byte[] 							hash,
		final SubscriptionLookupListener		_listener )
	
		throws SubscriptionException 
	{
		log( "Looking up associations for '" + ByteFormatter.encodeString( hash ));
		
		final String	key = "subscription:assoc:" + ByteFormatter.encodeString( hash ); 
			
		final boolean[]	cancelled = { false };
		
		final SubscriptionException	timeout_exception = new SubscriptionException( "Timeout" );
				
		final SubscriptionLookupListener listener = new
			SubscriptionLookupListener()
			{
				private boolean	done = false;
				
				private List<Subscription>	subs = new ArrayList<>();
						
				public void
				found(
					byte[]					hash,
					Subscription			subscription )
				{
					synchronized( this ){
						if ( done ){
							return;
						}
						subs.add( subscription );
					}
					
					_listener.found(hash, subscription);
				}	
				
				public void
				complete(
					byte[]					hash,
					Subscription[]			subscriptions )
				{
					synchronized( this ){
						if ( done ){
							return;
						}
						
						done = true;
					}
					
					_listener.complete(hash, subscriptions);
				}
				
				public void
				failed(
					byte[]					hash,
					SubscriptionException	error )
				{
					Subscription[]	subscriptions;
					
					synchronized( this ){
						if ( done ){
							return;
						}
						
						done = true;
						
						subscriptions = subs.toArray(new Subscription[0]);
					}
					
					if ( error == timeout_exception ){
						
						_listener.complete(hash, subscriptions);
						
					}else{
					
						_listener.failed(hash, error);
					}
				}
			};
		
		dht_plugin.get(
			getKeyBytes(key),
			"Subs assoc read: " + Base32.encode( hash ).substring( 0, 16 ),
			DHTPlugin.FLAG_SINGLE_VALUE,
			30,
			60*1000*(dht_plugin!=dht_plugin_public?2:1),
			true,
			true,
			new DHTPluginOperationListener()
			{
				private Map<HashWrapper,Integer>	hits 					= new HashMap<>();
				private AESemaphore					hits_sem				= new AESemaphore( "Subs:lookup" );
				private List<Subscription>			found_subscriptions 	= new ArrayList<>();
				
				private boolean	complete;
				
				private AsyncDispatcher 	dispatcher = new AsyncDispatcher( "SubsMan:AL");
				
				public boolean
				diversified()
				{
					return( true );
				}
				
				public void 
				starts(
					byte[] 				key ) 
				{
				}
				
				public void
				valueRead(
					DHTPluginContact	originator,
					DHTPluginValue		value )
				{
					if ( isCancelled2()){
						
						return;
					}
					
					byte[]	val = value.getValue();
					
					if ( val.length > 4 ){
						
						final int	ver = ((val[0]<<16)&0xff0000) | ((val[1]<<8)&0xff00) | (val[2]&0xff);

							// val[3] is fixed-random
						
						final byte[]	sid = new byte[ val.length - 4 ];
						
						System.arraycopy( val, 4, sid, 0, sid.length );
												
						HashWrapper hw = new HashWrapper( sid );
						
						boolean	new_sid = false;
						
						synchronized( hits ){
							
							if ( complete ){
								
								return;
							}
							
							Integer v = hits.get(hw);
							
							if ( v != null ){
								
								if ( ver > v){
									
									hits.put( hw, ver);
								}
							}else{
								
								new_sid = true;
																
								hits.put( hw, ver);
							}
						}
						
						if ( new_sid ){
							
							log( "    Found subscription " + ByteFormatter.encodeString( sid ) + " version " + ver );

								// check if already subscribed
							
							SubscriptionImpl subs = getSubscriptionFromSID( sid );
							
							if ( subs != null ){
								
								synchronized( hits ){
								
									found_subscriptions.add( subs );
								}
								
								try{
									listener.found( hash, subs );
									
								}catch( Throwable e ){
									
									Debug.printStackTrace(e);
								}
																
								hits_sem.release();
								
							}else{
								
									// don't want to block the DHT processing
								
								dispatcher.dispatch(
									new AERunnable()
									{
										public void
										runSupport()
										{
											boolean is_anon = dht_plugin!=dht_plugin_public;
											
											lookupSubscription( 
												hash, 
												sid, 
												ver,
												is_anon,
												new subsLookupListener()
												{
													private boolean sem_done = false;
													
													public void
													found(
														byte[]					hash,
														Subscription			subscription )
													{
													}
													
													public void
													complete(
														byte[]					hash,
														Subscription[]			subscriptions )
													{
														done( subscriptions );
													}
													
													public void
													failed(
														byte[]					hash,
														SubscriptionException	error )
													{
														done( new Subscription[0]);
													}
													
													protected void
													done(
														Subscription[]			subs )
													{
														synchronized( this ){
															
															if ( sem_done ){
																
																return;
															}
															
															sem_done = true;
														}
														
														try{
															if ( isCancelled()){
																																
																return;
															}
															
															if ( subs.length > 0 ){
																
																synchronized( hits ){
																
																	found_subscriptions.add( subs[0] );
																}
																
																try{
																	listener.found( hash, subs[0] );
																	
																}catch( Throwable e ){
																	
																	Debug.printStackTrace(e);
																}
															}
														}finally{
															
															hits_sem.release();
														}
													}
													
													public boolean 
													isCancelled() 
													{
														return( isCancelled2());
													}
												});
										}
									});
							}
						}
					}
				}
				
				public void
				valueWritten(
					DHTPluginContact	target,
					DHTPluginValue		value )
				{
				}
				
				public void
				complete(
					byte[]				original_key,
					boolean				timeout_occurred )
				{
					// can't use the dispatcher here as the complete processing might get scheduled before one of the
					// subs lookups and then deadlock as it is waiting on hit_sem
					
					new AEThread2( "SubsManAL:comp" )
					{
						public void
						run()
						{
							int	num_hits;
							
							synchronized( hits ){
								
								if ( complete ){
									
									return;
								}
								
								complete = true;
								
								num_hits = hits.size();
							}
							
							for (int i=0;i<num_hits;i++){
								
								if ( isCancelled2()){
									
									listener.failed( hash, new SubscriptionException( "Cancelled" ));
									
									return;
								}

								hits_sem.reserve();
							}

							SubscriptionImpl[] s;
							
							synchronized( hits ){
								
								s = found_subscriptions.toArray(new SubscriptionImpl[0]);
							}
							
							log( "    Association lookup complete - " + s.length + " found" );

							try{	
									// record zero assoc here for completeness
								
								recordAssociations( hash, s, true );
								
							}finally{
								
								listener.complete( hash, s );
							}
						}
					}.start();
				}
				
				protected boolean
				isCancelled2()
				{
					synchronized( cancelled ){
						
						return( cancelled[0] );
					}
				}
			});
		
		return( 
			new SubscriptionAssociationLookup()
			{
				public void 
				cancel() 
				{
					log( "    Association lookup cancelled" );

					synchronized( cancelled ){
						
						cancelled[0] = true;
					}
				}
				
				public void setTimeout(long millis) {
					SimpleTimer.addEvent( 
						"subs:timeout", 
						SystemTime.getOffsetTime( millis ),
						new TimerEventPerformer() {
							
							public void perform(TimerEvent event) {
								listener.failed( hash, timeout_exception );	
							}
						});
				}
			});
	}
	
	interface 
	subsLookupListener
		extends SubscriptionLookupListener
	{
		boolean
		isCancelled();
	}
	
	protected void
	getPopularity(
		final SubscriptionImpl					subs,
		final SubscriptionPopularityListener	listener )
	
		throws SubscriptionException
	{
		if ( !subs.isAnonymous()){
			
			try{
				long pop = PlatformSubscriptionsMessenger.getPopularityBySID( subs.getShortID());
	
				if ( pop >= 0 ){	
					
					log( "Got popularity of " + subs.getName() + " from platform: " + pop );
					
					listener.gotPopularity( pop );
	
					return;
					
				}else{
					
						// unknown sid - if singleton try to register for popularity tracking purposes
					
					if ( subs.isSingleton()){
						
						try{
							checkSingletonPublish( subs );
							
						}catch( Throwable e ){						
						}
						
						listener.gotPopularity( subs.isSubscribed()?1:0 );
							
						return;
					}
				}
				
			}catch( Throwable e ){
				
				log( "Subscription lookup via platform failed", e );
			}
		}
		
		getPopularityFromDHT( subs, listener, true );
	}
	
	protected void
	getPopularityFromDHT(
		final SubscriptionImpl					subs,
		final SubscriptionPopularityListener	listener,
		final boolean							sync )

	{
		final DHTPluginInterface	dht_plugin = selectDHTPlugin( subs );
		
		if ( dht_plugin != null ){
			
			if ( !dht_plugin.isInitialising()){

				getPopularitySupport( dht_plugin, subs, listener, sync );
			
			}else{
				
				new AEThread2( "SM:popwait", true )
				{
					public void
					run()
					{
						getPopularitySupport( dht_plugin, subs, listener, sync );
					}
				}.start();
			}
		}else{
			
			listener.failed( new SubscriptionException( "DHT unavailable" ));
		}
	}
	
	protected void
	updatePopularityFromDHT(
		final SubscriptionImpl		subs,
		boolean						sync )
	{
		getPopularityFromDHT(
			subs,
			new SubscriptionPopularityListener()
			{
				public void
				gotPopularity(
					long						popularity )
				{
					subs.setCachedPopularity( popularity );
				}
				
				public void
				failed(
					SubscriptionException		error )
				{
					log( "Failed to update subscription popularity from DHT", error );
				}
			},
			sync );
	}
	
	protected void
	getPopularitySupport(
		final DHTPluginInterface				dht_plugin,
		final SubscriptionImpl					subs,
		final SubscriptionPopularityListener	_listener,
		final boolean							sync )
	{
		log( "Getting popularity of " + subs.getName() + " from DHT (" + dht_plugin.getNetwork() + ")" );
			
		byte[]	sub_id 		= subs.getShortID();
		int		sub_version	= subs.getVersion();
		
		String	key = "subscription:publish:" + ByteFormatter.encodeString( sub_id ) + ":" + sub_version; 
		
			// check both torrent hash and pub hash
		
		byte[][]	keys = { subs.getPublicationHash(), getKeyBytes(key) };
		
		final AESemaphore sem = new AESemaphore( "SM:pop" );
		
		final long[] result = { -1 };
		
		final int timeout = 15*1000 * (subs.isAnonymous()?3:1);
		
		final SubscriptionPopularityListener listener = 
			new SubscriptionPopularityListener()
			{
				private boolean	done;
				
				public void
				gotPopularity(
					long						popularity )
				{
					synchronized( this ){
						if ( done ){
							return;
						}
						done = true;
					}
					_listener.gotPopularity( popularity );
				}
				
				public void
				failed(
					SubscriptionException		error )
				{
					synchronized( this ){
						if ( done ){
							return;
						}
						done = true;
					}
					_listener.failed( error );
				}
			};
			
		for ( byte[] hash: keys ){
			
			dht_plugin.get(
				hash,
				"Popularity lookup for subscription " + subs.getName(),
				DHTPlugin.FLAG_STATS,
				5,
				timeout,
				false,
				true,
				new DHTPluginOperationListener()
				{
					private boolean	diversified;
					
					private int	hits = 0;
					
					public boolean
					diversified()
					{
						diversified = true;
						
						return( false );
					}
					
					public void 
					starts(
						byte[] 				key ) 
					{
					}
					
					public void
					valueRead(
						DHTPluginContact	originator,
						DHTPluginValue		value )
					{
						DHTPluginKeyStats stats = dht_plugin.decodeStats( value );
						
						if ( stats != null ){
							
							result[0] = Math.max( result[0], stats.getEntryCount());
						
							hits++;
							
							if ( hits >= 3 ){
								
								done();
							}
						}
					}
					
					public void
					valueWritten(
						DHTPluginContact	target,
						DHTPluginValue		value )
					{
						
					}
					
					public void
					complete(
						byte[]				key,
						boolean				timeout_occurred )
					{
						if ( diversified ){
							
								// TODO: fix?
							
							result[0] *= 11;
							
							if ( result[0] == 0 ){
								
								result[0] = 10;
							}
						}
						
						done();
					}
					
					protected void
					done()
					{						
						if ( sync ){
			
							sem.release();

						}else{
							
							if ( result[0] == -1 ){
								
								log( "Failed to get popularity of " + subs.getName() + " from DHT" );
					
								listener.failed( new SubscriptionException( "Timeout" ));
								
							}else{
							
								log( "Get popularity of " + subs.getName() + " from DHT: " + result[0] );
					
								listener.gotPopularity( result[0] );
							}
						}
					}
				});
		}
		
		if ( sync ){
			
			sem.reserve( timeout );
			
			if ( result[0] == -1 ){
				
				log( "Failed to get popularity of " + subs.getName() + " from DHT" );
	
				listener.failed( new SubscriptionException( "Timeout" ));
				
			}else{
			
				log( "Get popularity of " + subs.getName() + " from DHT: " + result[0] );
	
				listener.gotPopularity( result[0] );
			}
		}
	}
	
	protected void
	lookupSubscription(
		final byte[]						association_hash,
		final byte[]						sid,
		final int							version,
		boolean								is_anon,		
		final subsLookupListener			listener )
	{
		try{
			SubscriptionImpl subs = getSubscriptionFromPlatform( sid, is_anon, SubscriptionImpl.ADD_TYPE_LOOKUP );

			log( "Added temporary subscription: " + subs.getString());
			
			subs = addSubscription( subs );
			
			listener.complete( association_hash, new Subscription[]{ subs });
			
			return;
			
		}catch( Throwable e ){
			
			if ( listener.isCancelled()){
				
				listener.failed( association_hash, new SubscriptionException( "Cancelled" ));
				
				return;
			}
			
			final String sid_str = ByteFormatter.encodeString( sid );
			
			log( "Subscription lookup via platform for " + sid_str + " failed", e );
			
			if ( getSubscriptionDownloadCount() > 8 ){
				
				log( "Too many existing subscription downloads" );
				
				listener.complete( association_hash, new Subscription[0]);

				return;
			}
			
				// fall back to DHT
			
			log( "Subscription lookup via DHT starts for " + sid_str );

			final String	key = "subscription:publish:" + ByteFormatter.encodeString( sid ) + ":" + version; 
			
			dht_plugin_public.get(
				getKeyBytes(key),
				"Subs lookup read: " + ByteFormatter.encodeString( sid ) + ":" + version,
				DHTPlugin.FLAG_SINGLE_VALUE,
				12,
				60*1000,
				false,
				true,
				new DHTPluginOperationListener()
				{
					private boolean listener_handled;
					
					public boolean
					diversified()
					{
						return( true );
					}
					
					public void 
					starts(
						byte[] 				key ) 
					{
					}
					
					public void
					valueRead(
						DHTPluginContact	originator,
						DHTPluginValue		value )
					{
						byte[]	data = value.getValue();
								
						try{
							final Map	details = decodeSubscriptionDetails( data );
							
							if ( SubscriptionImpl.getPublicationVersion( details ) == version ){
																
								Map	singleton_details = (Map)details.get( "x" );
								
								if ( singleton_details == null ){
									
									synchronized( this ){
										
										if ( listener_handled  ){
											
											return;
										}
										
										listener_handled = true;
									}
									
									log( "    found " + sid_str + ", non-singleton" );

									new AEThread2( "Subs:lookup download", true )
									{
										public void
										run()
										{
											downloadSubscription( 
												association_hash,
												SubscriptionImpl.getPublicationHash( details ),
												sid,
												version,
												SubscriptionImpl.getPublicationSize( details ),
												listener );
										}
									}.start();
									
								}else{
																			
									synchronized( this ){
											
										if ( listener_handled  ){
												
											return;
										}
											
										listener_handled = true;
									}
									
									log( "    found " + sid_str + ", singleton" );

									try{
										SubscriptionImpl subs = createSingletonSubscription( singleton_details, SubscriptionImpl.ADD_TYPE_LOOKUP, false );
																			
										listener.complete( association_hash, new Subscription[]{ subs });
																			
									}catch( Throwable e ){
																					
										listener.failed( association_hash, new SubscriptionException( "Subscription creation failed", e ));
									}
								}
							}else{
								
								log( "    found " + sid_str + " but version mismatch" );

							}
						}catch( Throwable e ){
							
							log( "    found " + sid_str + " but verification failed", e );

						}
					}
					
					public void
					valueWritten(
						DHTPluginContact	target,
						DHTPluginValue		value )
					{
					}
					
					public void
					complete(
						byte[]				original_key,
						boolean				timeout_occurred )
					{
						log( "    " + sid_str + " complete" );
						
						synchronized( this ){
							
							if ( listener_handled ){
						
								return;
							}
							
							listener_handled = true;
							
						}
						listener.complete( association_hash, new Subscription[0] );
					}
				});
		}
	}
	
	protected SubscriptionImpl
	getSubscriptionFromPlatform(
		byte[]		sid,
		boolean		is_anon,
		int			add_type )
	
		throws SubscriptionException
	{
		try{
			PlatformSubscriptionsMessenger.subscriptionDetails details = PlatformSubscriptionsMessenger.getSubscriptionBySID( sid, is_anon );
			
			SubscriptionImpl res = getSubscriptionFromVuzeFileContent( sid, add_type, details.getContent());
			
			int	pop = details.getPopularity();
			
			if ( pop >= 0 ){
				
				res.setCachedPopularity( pop );
			}
			
			return( res );
			
		}catch( SubscriptionException e ){
			
			throw( e );
			
		}catch( Throwable e ){
			
			throw( new SubscriptionException( "Failed to read subscription from platform", e ));
		}
	}
	
	protected SubscriptionImpl
	getSubscriptionFromVuzeFile(
		byte[]		sid,
		int			add_type,
		File		file )
	
		throws SubscriptionException
	{
		VuzeFileHandler vfh = VuzeFileHandler.getSingleton();
		
		String	file_str = file.getAbsolutePath();
		
		VuzeFile vf = vfh.loadVuzeFile( file_str );

		if ( vf == null ){
			
			log( "Failed to load vuze file from " + file_str );
			
			throw( new SubscriptionException( "Failed to load vuze file from " + file_str ));
		}
		
		return( getSubscriptionFromVuzeFile( sid, add_type, vf ));
	}
	
	protected SubscriptionImpl
	getSubscriptionFromVuzeFileContent(
		byte[]		sid,
		int			add_type,
		String		content )
	
		throws SubscriptionException
	{
		VuzeFileHandler vfh = VuzeFileHandler.getSingleton();
		
		VuzeFile vf = vfh.loadVuzeFile( Base64.decode( content ));

		if ( vf == null ){
			
			log( "Failed to load vuze file from " + content );
			
			throw( new SubscriptionException( "Failed to load vuze file from content" ));
		}
	
		return( getSubscriptionFromVuzeFile( sid, add_type, vf ));
	}
	
	protected SubscriptionImpl
	getSubscriptionFromVuzeFile(
		byte[]		sid,
		int			add_type,
		VuzeFile	vf )
	
		throws SubscriptionException
	{
		VuzeFileComponent[] comps = vf.getComponents();

        for (VuzeFileComponent comp : comps) {

            if (comp.getType() == VuzeFileComponent.COMP_TYPE_SUBSCRIPTION) {

                Map map = comp.getContent();

                try {
                    SubscriptionBodyImpl body = new SubscriptionBodyImpl(SubscriptionManagerImpl.this, map);

                    SubscriptionImpl new_subs = new SubscriptionImpl(SubscriptionManagerImpl.this, body, add_type, false);

                    if (Arrays.equals(new_subs.getShortID(), sid)) {

                        return (new_subs);
                    }
                } catch (Throwable e) {

                    log("Subscription decode failed", e);
                }
            }
        }
		
		throw( new SubscriptionException( "Subscription not found" ));
	}
	
	protected void 
	downloadSubscription(
		final byte[]						association_hash,
		byte[]								torrent_hash,
		final byte[]						sid,
		int									version,
		int									size,
		final subsLookupListener		 	listener )
	{
		try{
			Object[] res = downloadTorrent( torrent_hash, size );
			
			if ( listener.isCancelled()){
				
				listener.failed( association_hash, new SubscriptionException( "Cancelled" ));
				
				return;
			}
			
			if ( res == null ){
				
				listener.complete( association_hash, new Subscription[0] );
				
				return;
			}

			downloadSubscription(
				(TOTorrent)res[0], 
				(InetSocketAddress)res[1],
				sid,
				version,
				"Subscription " + ByteFormatter.encodeString( sid ) + " for " + ByteFormatter.encodeString( association_hash ),
				new downloadListener()
				{
					public void
					complete(
						File		data_file )
					{
						boolean	reported = false;
						
						try{
							if ( listener.isCancelled()){
								
								listener.failed( association_hash, new SubscriptionException( "Cancelled" ));

								return;
							}
							
							SubscriptionImpl subs = getSubscriptionFromVuzeFile( sid, SubscriptionImpl.ADD_TYPE_LOOKUP, data_file );
						
							log( "Added temporary subscription: " + subs.getString());
							
							subs = addSubscription( subs );
							
							listener.complete( association_hash, new Subscription[]{ subs });
							
							reported = true;
	
						}catch( Throwable e ){
							
							log( "Subscription decode failed", e );
							
						}finally{
														
							if ( !reported ){
								
								listener.complete( association_hash, new Subscription[0] );
							}
						}
					}
					
					public void
					complete(
						Download	download,	
						File		torrent_file )
					{
						File	data_file = new File( download.getSavePath());
						
						try{
							removeDownload( download, false );

							complete( data_file );
							
						}catch( Throwable e ){
							
							log( "Failed to remove download", e );
							
							listener.complete( association_hash, new Subscription[0] );
							
						}finally{
							
							torrent_file.delete();
							
							data_file.delete();
						}
					}
						
					public void
					failed(
						Throwable	error )
					{
						listener.complete( association_hash, new Subscription[0] );
					}
					
					public Map
					getRecoveryData()
					{
						return( null );
					}
					
					public boolean
					isCancelled()
					{
						return( listener.isCancelled());
					}
				});
				
		}catch( Throwable e ){
			
			log( "Subscription download failed",e );
			
			listener.complete( association_hash, new Subscription[0] );
		}
	}
	
	protected int
	getSubscriptionDownloadCount()
	{
		PluginInterface pi = PluginInitializer.getDefaultInterface();
		
		Download[] downloads = pi.getDownloadManager().getDownloads();
		
		int	res = 0;

        for (Download download : downloads) {

            if (download.getBooleanAttribute(ta_subs_download)) {

                res++;
            }
        }
		
		return( res );
	}
	
	protected void
	associationAdded(
		SubscriptionImpl			subscription,
		byte[]						association_hash )
	{
		recordAssociations( association_hash, new SubscriptionImpl[]{ subscription }, false );

		DHTPluginInterface	dht_plugin = selectDHTPlugin( subscription );
		
		if ( dht_plugin != null ){
			
			publishAssociations();
		}
	}
	
	protected void
	addPotentialAssociation(
		SubscriptionImpl			subs,
		String						result_id,
		String						key )
	{
		if ( key == null ){
			
			Debug.out( "Attempt to add null key!" );
			
			return;
		}
		
		log( "Added potential association: " + subs.getName() + "/" + result_id + " -> " + key );
		
		synchronized( potential_associations ){
			
			potential_associations.add( new Object[]{ subs, result_id, key, System.currentTimeMillis()} );
			
			if ( potential_associations.size() > 512 ){
				
				potential_associations.remove(0);
			}
		}
	}
	
	protected void
	checkPotentialAssociations(
		byte[]				hash,
		String				key )
	{
		log( "Checking potential association: " + key + " -> " + ByteFormatter.encodeString( hash ));
		
		SubscriptionImpl 	subs 		= null;
		String				result_id	= null;
		
		synchronized( potential_associations ){

			Iterator<Object[]> it = potential_associations.iterator();
			
			while( it.hasNext()){
				
				Object[]	entry = it.next();
				
				String	this_key = (String)entry[2];
				
					// startswith as actual URL may have had additional parameters added such as azid
				
				if ( key.startsWith( this_key )){
					
					subs		= (SubscriptionImpl)entry[0];
					result_id	= (String)entry[1];
					
					log( "    key matched to subscription " + subs.getName() + "/" + result_id);

					it.remove();
					
					break;
				}
			}
			
			if ( subs == null ){
				
					// try again, this time by hash in case the initial download failed and was
					// auto-convereted to a magnet download attempt
				
				it = potential_associations.iterator();
				
				while( it.hasNext()){
					
					Object[]	entry = it.next();
											
					SubscriptionImpl 	subs_temp		= (SubscriptionImpl)entry[0];
					String				result_id_temp	= (String)entry[1];
					
					SubscriptionResult result = subs_temp.getHistory().getResult( result_id_temp );
					
					if ( result != null ){
						
						Map<Integer,Object>	props = result.toPropertyMap();
						
						byte[] result_hash = (byte[])props.get( SearchResult.PR_HASH );
						
						if ( result_hash == null ){
							
							String url = (String)props.get( SearchResult.PR_TORRENT_LINK );
							
							if ( url == null ){
								
								url = (String)props.get( SearchResult.PR_DOWNLOAD_LINK );
							}
							
							if ( url != null ){
								
								String lc_url = url.toLowerCase( Locale.US );
							
								if ( lc_url.startsWith( "http" )){
  	  							
									String alt_url = UrlUtils.parseTextForURL( url.substring( 5 ), true );
									
									if ( key.startsWith( alt_url )){
										
										result_hash = hash;	// force match below
									}
								}else if ( lc_url.startsWith( "magnet" )){
									
									result_hash = UrlUtils.extractHash( lc_url );
								}
  							}
						}
						
						if ( result_hash != null && Arrays.equals( result_hash, hash )){
							
							subs		= subs_temp;
							result_id	= result_id_temp;
							
							log( "    hash matched to subscription " + subs.getName() + "/" + result_id);

							it.remove();
							
							break;
						}
					}
				}
			}
		}
		
		if ( subs == null ){
			
			log( "    no potential associations found" );
			
		}else{
			
			SubscriptionResult	result = subs.getHistory().getResult( result_id );
			
			if ( result != null ){
				
				log( "    result found, marking as read" );

				result.setRead( true );
				
			}else{
				
				log( "    result not found" );
			}
			
			log( "    adding association" );
			
			subs.addAssociation( hash );
		}
	}
	
	protected void
	tidyPotentialAssociations()
	{
		long	now = SystemTime.getCurrentTime();
		
		synchronized( potential_associations ){
			
			Iterator it = potential_associations.iterator();
			
			while( it.hasNext() && potential_associations.size() > 16 ){
				
				Object[]	entry = (Object[])it.next();
				
				long	created = (Long) entry[3];
				
				if ( created > now ){
					
					entry[3] = now;
					
				}else if ( now - created > 60*60*1000 ){
					
					SubscriptionImpl 	subs = (SubscriptionImpl)entry[0];

					String	result_id	= (String)entry[1];
					String	key			= (String)entry[2];

					log( "Removing expired potential association: " + subs.getName() + "/" + result_id + " -> " + key );
					
					it.remove();
				}
			}
		}
		
		synchronized( potential_associations2 ){
			
			Iterator it = potential_associations2.entrySet().iterator();
			
			while( it.hasNext() && potential_associations2.size() > 16 ){
				
				Map.Entry	map_entry = (Map.Entry)it.next();
				
				byte[]		hash = ((HashWrapper)map_entry.getKey()).getBytes();
				
				Object[]	entry = (Object[])map_entry.getValue();
				
				long	created = (Long) entry[2];
				
				if ( created > now ){
					
					entry[2] = now;
					
				}else if ( now - created > 60*60*1000 ){
					
					SubscriptionImpl[] 	subs = (SubscriptionImpl[])entry[0];

					String	subs_str = "";
					
					for (int i=0;i<subs.length;i++){
						subs_str += (i==0?"":",") + subs[i].getName();
					}
					
					log( "Removing expired potential association: " + ByteFormatter.encodeString(hash) + " -> " + subs_str );
					
					it.remove();
				}
			}
		}
	}
	
	protected void
	recordAssociations(
		byte[]						association_hash,
		SubscriptionImpl[]			subscriptions,
		boolean						full_lookup )
	{
		HashWrapper	hw = new HashWrapper( association_hash );
		
		synchronized( potential_associations2 ){
			
			potential_associations2.put( hw, new Object[]{ subscriptions, full_lookup, SystemTime.getCurrentTime()});
		}
			
		if ( recordAssociationsSupport( association_hash, subscriptions, full_lookup )){
			
			synchronized( potential_associations2 ){

				potential_associations2.remove( hw );
			}
		}else{
			
			log( "Deferring association for " + ByteFormatter.encodeString( association_hash ));
		}
	}
	
	protected void
	addPrepareTrigger(
		byte[]					hash,
		Subscription[]			subs,
		SubscriptionResult[]	results )
	{
		synchronized( potential_associations3 ){
			
			potential_associations3.put( new HashWrapper( hash ), new Object[]{ subs, results } );
		}
	}
	
	protected void
	removePrepareTrigger(
		byte[]				hash )
	{
		synchronized( potential_associations3 ){
			
			potential_associations3.remove( new HashWrapper( hash ));
		}
	}
	
	protected void
	prepareDownload(
		Download 				download, 
		Subscription[]			subscriptions,
		SubscriptionResult[]	results )
	{		
		try{			
			if ( subscriptions.length > 0 ){
				
				Subscription subs = subscriptions[0];	// deal with first only for cat/tag/nets as will always be just one when called from downloadAdded
				
				if ( results != null && results.length > 0 ){
					
					try{
						SubscriptionResult result = results[0];
						
						Map<Integer,Object> props = result.toPropertyMap();
						
						Long	leechers 	= (Long)props.get( SearchResult.PR_LEECHER_COUNT );
						Long	seeds 		= (Long)props.get( SearchResult.PR_SEED_COUNT );
						
						if ( leechers != null && seeds != null && leechers >= 0 && seeds >= 0 ){
							
							org.gudy.azureus2.core3.download.DownloadManager core_dm = PluginCoreUtils.unwrap( download );
							
							DownloadManagerState state = core_dm.getDownloadState();
	
							long cache = ((seeds&0x00ffffffL)<<32)|(leechers&0x00ffffffL);
							
							state.setLongAttribute( DownloadManagerState.AT_SCRAPE_CACHE_SOURCE, 1 );
							state.setLongAttribute( DownloadManagerState.AT_SCRAPE_CACHE, cache );
						}
					}catch( Throwable e ){
						
					}
				}
				
				String	category = subs.getCategory();
				
				if ( category != null ){
					
					String existing = download.getAttribute( ta_category );
							
					if ( existing == null ){
								
						download.setAttribute( ta_category, category );
					}
				}
				
				long	tag_id = subs.getTagID();
				
				if ( tag_id >= 0 ){
					
					Tag tag = TagManagerFactory.getTagManager().lookupTagByUID( tag_id );

					if ( tag != null ){
						
						org.gudy.azureus2.core3.download.DownloadManager core_dm = PluginCoreUtils.unwrap( download );
						
						if ( !tag.hasTaggable( core_dm )){
							
							tag.addTaggable( core_dm );
						}
					}
				}
				
				String[] nets = subs.getHistory().getDownloadNetworks();
				
				if ( nets != null ){
									
					org.gudy.azureus2.core3.download.DownloadManager core_dm = PluginCoreUtils.unwrap( download );
					
					DownloadManagerState state = core_dm.getDownloadState();
					
					state.setNetworks( nets );
					
						// ensure that other cide (e.g. the open-torrent stuff) doesn't over-write this
					
					state.setFlag( DownloadManagerState.FLAG_INITIAL_NETWORKS_SET, true );
				}
			}
		
		}catch( Throwable e ){
			
			log( "Failed to prepare association", e );
		}
	}
	
	protected boolean
	recordAssociationsSupport(
		byte[]						association_hash,
		SubscriptionImpl[]			subscriptions,
		boolean						full_lookup )
	{
		PluginInterface pi = PluginInitializer.getDefaultInterface();

		boolean	download_found	= false;
		boolean	changed 		= false;
		boolean	assoc_added		= false;
		
		try{
			Download download = pi.getDownloadManager().getDownload( association_hash );
			
			if ( download != null ){
				
				download_found = true;
				
				Map<String,Object>	map = (Map<String,Object>)download.getMapAttribute( ta_subscription_info );
				
				if ( map == null ){
					
					map = new LightHashMap<>();
					
				}else{
					
					map = new LightHashMap<>(map);
				}
				
				List<byte[]>	s = (List<byte[]>)map.get( "s" );

                for (SubscriptionImpl subscription : subscriptions) {

                    byte[] sid = subscription.getShortID();

                    if (s == null) {

                        s = new ArrayList<>();

                        s.add(sid);

                        changed = true;

                        map.put("s", s);

                    } else {

                        boolean found = false;

                        for (byte[] existing : s) {

                            if (Arrays.equals(sid, existing)) {

                                found = true;

                                break;
                            }
                        }

                        if (!found) {

                            s.add(sid);

                            if (subscription.isSubscribed() &&
                                    subscription.isPublic() &&
                                    !subscription.isSearchTemplate()) {

                                // pick up alternative subscriptions for same download

                                if (subscription.addAssociationSupport(association_hash, true)) {

                                    assoc_added = true;
                                }
                            }

                            changed = true;
                        }
                    }
                }
				
				if ( full_lookup ){
				
					map.put( "lc", SystemTime.getCurrentTime());
					
					changed	= true;
				}
				
				if ( changed ){
				
					download.setMapAttribute( ta_subscription_info, map );
				}
				
				if ( subscriptions.length == 1 && subscriptions[0].isSearchTemplate() && !full_lookup ){
					
					searchTemplateOK( subscriptions[0], download );
				}
			}
		}catch( Throwable e ){
			
			log( "Failed to record associations", e );
		}
		
		if ( changed ){

            for (SubscriptionManagerListener listener : listeners) {

                try {
                    (listener).associationsChanged(association_hash);

                } catch (Throwable e) {

                    Debug.printStackTrace(e);
                }
            }
		}
		
		if ( assoc_added ){
			
			publishAssociations();
		}
		
		return( download_found );
	}
	
	private AsyncDispatcher				chat_write_dispatcher 	= new AsyncDispatcher( "Subscriptions:cwd" );
	private Set<String>					chat_st_done = new HashSet<>();
	private LinkedList<ChatInstance>	chat_assoc_done	= new LinkedList<>();
	
	private void
	searchTemplateOK(
		final SubscriptionImpl	subs,
		final Download			download )
	{
		if ( BuddyPluginUtils.isBetaChatAvailable()){
			
			chat_write_dispatcher.dispatch(
				new AERunnable() {
					
					@Override
					public void 
					runSupport() 
					{
						DHTPluginInterface dht = selectDHTPlugin( download );
						
						if ( dht == null ){
							
							return;
						}
						
						String target_net = dht.getNetwork();
						
						if (!AENetworkClassifier.AT_PUBLIC.equals(target_net)){
							
							if ( !BuddyPluginUtils.isBetaChatAnonAvailable()){
								
								return;
							}
							
							target_net = AENetworkClassifier.AT_I2P;
						}
						
						String name = subs.getName();
						
						int pos = name.indexOf( ':' );
						
						if ( pos != -1 ){
							
							name = name.substring( pos+1 ).trim();
						}
						
						if ( chat_st_done.contains( name )){
							
							return;
						}

						chat_st_done.add( name );
						
						final BuddyPluginBeta.ChatInstance chat = BuddyPluginUtils.getChat( target_net, "Search Templates" );
							
						if ( chat != null ){
								
							chat.setSharedNickname( false );
								
							chat.setSaveMessages( false );
							
							final String f_msg = subs.getURI() + "[[" + UrlUtils.encode( name ) + "]]";
							
							final Runnable do_write = 
								new Runnable()
								{
									public void
									run()
									{		
										Map<String,Object>	flags 	= new HashMap<>();
										
										flags.put( BuddyPluginBeta.FLAGS_MSG_ORIGIN_KEY, BuddyPluginBeta.FLAGS_MSG_ORIGIN_SUBS );
										
										Map<String,Object>	options = new HashMap<>();
										
										chat.sendMessage( f_msg, flags, options );
									}
								};
																
							waitForChat(
								chat, 
								new AERunnable()
								{
									public void 
									runSupport() 
									{
										List<ChatMessage>	messages = chat.getMessages();
																				
										for ( ChatMessage message: messages ){
																									
											if ( message.getMessage().equals( f_msg )){
																												
												return;
											}
										}
										
										do_write.run();
									}
                                });
						}
					}
				});
		}
	}
	
	private void
	assocOK(
		final SubscriptionImpl					subs,
		final SubscriptionImpl.association		assoc )
	{
		if ( BuddyPluginUtils.isBetaChatAvailable()){
				
			chat_write_dispatcher.dispatch(
				new AERunnable() {
					
					@Override
					public void 
					runSupport() 
					{
						try{
							Download download = azureus_core.getPluginManager().getDefaultPluginInterface().getDownloadManager().getDownload( assoc.getHash());
							
							if ( download != null ){
								
								if ( TorrentUtils.isReallyPrivate( PluginCoreUtils.unwrap( download.getTorrent()))){
									
									return;
								}
								
								final ChatInstance chat = BuddyPluginUtils.getChat( download );
								
								if ( chat != null ){
									
									String net = chat.getNetwork();
									
									if (AENetworkClassifier.AT_PUBLIC.equals(net) || subs.isAnonymous()){
																			
										synchronized( chat_assoc_done ){
											
											if ( !chat_assoc_done.contains( chat )){
												
												chat_assoc_done.add( chat );
																								
												if ( chat_assoc_done.size() > 50 ){
														
													ChatInstance c = chat_assoc_done.removeFirst();
														
													c.setInteresting( false );
													
													c.destroy();
												}
											}
										}
										
										String name = subs.getName();
										
										if ( subs.isSearchTemplate()){
										
											int pos = name.indexOf( ':' );
										
											if ( pos != -1 ){
												
												name = name.substring( pos+1 ).trim();
											}
										}
										
										final String f_msg = (subs.isSearchTemplate()?"Search Template":"Subscription" ) + " " + subs.getURI() + "[[" + UrlUtils.encode( name ) + "]]";
																																						
										waitForChat(
											chat, 
											new AERunnable()
											{
												public void 
												runSupport() 
												{
													List<ChatMessage>	messages = chat.getMessages();
																							
													for ( ChatMessage message: messages ){
																												
														if ( message.getMessage().equals( f_msg )){
																
															synchronized( chat_assoc_done ){
																
																if ( chat_assoc_done.remove( chat )){
															
																	chat.destroy();
																}
															}
															
															return;
														}
													}
													
													Map<String,Object>	flags 	= new HashMap<>();
													
													flags.put( BuddyPluginBeta.FLAGS_MSG_ORIGIN_KEY, BuddyPluginBeta.FLAGS_MSG_ORIGIN_SUBS );
													
													Map<String,Object>	options = new HashMap<>();
													
													chat.sendMessage( f_msg, flags, options );
	
												}
                                            });
									}else{
										
										chat.destroy();
									}
								}
							}
						}catch( Throwable e ){
							
						}
					}
				});
		}
	}
	
	private void
	waitForChat(
			final ChatInstance		chat,
			final AERunnable		runnable )
	{
			// wait for chat to synchronize and then run 

		final TimerEventPeriodic[] event = { null };

		synchronized( event ){

			event[0] = 
				SimpleTimer.addPeriodicEvent(
					"Subs:chat:checker",
					30*1000,
					new TimerEventPerformer()
					{
						private int elapsed_time;

						public void 
						perform(
							TimerEvent e ) 
						{
							elapsed_time += 30*1000;

							if ( chat.isDestroyed()){

								synchronized( event ){

									event[0].cancel();
								}

							}else{

								if ( 	chat.getIncomingSyncState() == 0 ||
										elapsed_time >= 5*60*1000 ){

									synchronized( event ){

										event[0].cancel();
									}

									SimpleTimer.addEvent(
										"Subs:chat:checker",
										SystemTime.getOffsetTime( 5*60*1000 ),
										new TimerEventPerformer()
										{	
											public void 
											perform(
												TimerEvent event ) 
											{
												if ( !chat.isDestroyed()){

													chat_write_dispatcher.dispatch( 
														new AERunnable() {

															@Override
															public void 
															runSupport() 
															{
																if ( !chat.isDestroyed()){

																	runnable.runSupport();
																}
															}
														});
												}
											}
										});
								}
							}
						}
					});	
		}
	}

	private boolean
	publishAssociations()
	{
		SubscriptionImpl 				subs_to_publish		= null;
		SubscriptionImpl.association	assoc_to_publish 	= null;

		synchronized( this ){
			
			if ( publish_associations_active >= ( dht_plugin_public.isSleeping()?PUB_SLEEPING_ASSOC_CONC_MAX:PUB_ASSOC_CONC_MAX )){
				
				return( false );
			}			
			
			publish_associations_active++;
			
			log( "Publishing Associations Starts (conc=" + publish_associations_active + ")" );
			
			List<SubscriptionImpl> shuffled_subs = new ArrayList<>(subscriptions);

			Collections.shuffle( shuffled_subs );

            for (SubscriptionImpl sub : shuffled_subs) {

                if (sub.isSubscribed() && sub.isPublic()) {

                    assoc_to_publish = sub.getAssociationForPublish();

                    if (assoc_to_publish != null) {

                        subs_to_publish = sub;

                        break;
                    }
                }
            }
		}
		
		if ( assoc_to_publish != null ){
		
			publishAssociation( subs_to_publish, assoc_to_publish );
			
			return( false );
			
		}else{
					
			log( "Publishing Associations Complete" );
					
			synchronized( this ){

				publish_associations_active--;
			}
			
			return( true );
		}
	}
	
	private int
	getPublishRemainingCount()
	{
		synchronized( this ){
			
			int	result = 0;
			
			for ( SubscriptionImpl sub: subscriptions ){
				
				if ( sub.isSubscribed() && sub.isPublic()){
					
					result += sub.getAssociationsRemainingForPublish();
				}
			}
			
			return( result );
		}
	}
	
	private void
	publishAssociation(
		final SubscriptionImpl					subs,
		final SubscriptionImpl.association		assoc )
	{
		log( "Checking association '" + subs.getString() + "' -> '" + assoc.getString() + "'" );
		
		byte[]	sub_id 		= subs.getShortID();
		int		sub_version	= subs.getVersion();
		
		byte[]	assoc_hash	= assoc.getHash();
		
		final String	key = "subscription:assoc:" + ByteFormatter.encodeString( assoc_hash ); 
				
		final byte[]	put_value = new byte[sub_id.length + 4];
		
		System.arraycopy( sub_id, 0, put_value, 4, sub_id.length );
		
		put_value[0]	= (byte)(sub_version>>16);
		put_value[1]	= (byte)(sub_version>>8);
		put_value[2]	= (byte)sub_version;
		put_value[3]	= (byte)subs.getFixedRandom();
		
		final DHTPluginInterface	dht_plugin = selectDHTPlugin( subs );
		
		if ( dht_plugin == null ){
			
			synchronized( this ){

				publish_associations_active--;
			}
			
			return;
		}
		
		dht_plugin.get(
			getKeyBytes(key),
			"Subs assoc read: " + Base32.encode( assoc_hash ).substring( 0, 16 ),
			DHTPlugin.FLAG_SINGLE_VALUE,
			30,
			60*1000*(subs.isAnonymous()?2:1),
			false,
			false,
			new DHTPluginOperationListener()
			{
				private int			hits;
				private boolean		diversified;
				private int			max_ver;
				
				public boolean
				diversified()
				{
					diversified = true;
					
					return( false );
				}
				
				public void 
				starts(
					byte[] 				key ) 
				{
				}
				
				public void
				valueRead(
					DHTPluginContact	originator,
					DHTPluginValue		value )
				{
					byte[]	val = value.getValue();
					
					if ( val.length == put_value.length ){
						
						boolean	diff = false;
						
						for (int i=4;i<val.length;i++){
							
							if ( val[i] != put_value[i] ){
								
								diff = true;
								
								break;
							}
						}
						
						if ( !diff ){
							
							hits++;
							
							int	ver = ((val[0]<<16)&0xff0000) | ((val[1]<<8)&0xff00) | (val[2]&0xff);
							
							if ( ver > max_ver ){
								
								max_ver = ver;
							}
						}
					}
				}
				
				public void
				valueWritten(
					DHTPluginContact	target,
					DHTPluginValue		value )
				{
				}
				
				public void
				complete(
					byte[]				original_key,
					boolean				timeout_occurred )
				{
					log( "Checked association '" + subs.getString() + "' -> '" + assoc.getString() + "' - max_ver=" + max_ver + ",hits=" + hits + ",div=" + diversified );

					if ( max_ver > subs.getVersion()){
						
						if ( !subs.isMine()){
						
							updateSubscription( subs, max_ver );
						}
					}
					
					if ( hits < 10 && !diversified ){			
			
						log( "    Publishing association '" + subs.getString() + "' -> '" + assoc.getString() + "', existing=" + hits + ", net=" + dht_plugin.getNetwork());

						byte flags = DHTPlugin.FLAG_ANON;
						
						if ( hits < 3 && !diversified ){
							
							flags |= DHTPlugin.FLAG_PRECIOUS;
						}
												
						if ( subs.isAnonymous()){
							
							flags |= DHTPlugin.FLAG_BRIDGED;
						}
						
						dht_plugin.put(
							getKeyBytes(key),
							"Subs assoc write: " + Base32.encode( assoc.getHash()).substring( 0, 16 ) + " -> " + Base32.encode( subs.getShortID() ) + ":" + subs.getVersion(),
							put_value,
							flags,
							new DHTPluginOperationListener()
							{
								public boolean
								diversified()
								{
									return( true );
								}
								
								public void 
								starts(
									byte[] 				key ) 
								{
								}
								
								public void
								valueRead(
									DHTPluginContact	originator,
									DHTPluginValue		value )
								{
								}
								
								public void
								valueWritten(
									DHTPluginContact	target,
									DHTPluginValue		value )
								{
								}
								
								public void
								complete(
									byte[]				key,
									boolean				timeout_occurred )
								{
									log( "        completed '" + subs.getString() + "' -> '" + assoc.getString() + "'" );
				
									publishNext();
								}
							});
						
						assocOK( subs, assoc );
								
					}else{
						
						log( "    Not publishing association '" + subs.getString() + "' -> '" + assoc.getString() + "', existing =" + hits );

						publishNext();
					}
				}
				
				protected void
				publishNext()
				{
					synchronized( SubscriptionManagerImpl.this ){
						
						publish_associations_active--;
					}
					
					publishNextAssociation();
				}
			});
	}
	
	private void
	publishNextAssociation()
	{
		boolean	dht_sleeping = dht_plugin_public.isSleeping();
		
		if ( dht_sleeping ){
			
			synchronized( this ){
				
				if ( publish_next_asyc_pending ){
					
					return;
				}
				
				publish_next_asyc_pending = true;
			}
			
			SimpleTimer.addEvent(
				"subs:pn:async",
				SystemTime.getCurrentTime() + 60*1000,
				new TimerEventPerformer()
				{
					public void 
					perform(
						TimerEvent event) 
					{
						synchronized( SubscriptionManagerImpl.this ){
							
							publish_next_asyc_pending = false;
						}
						
						publishAssociations();
					}
				});
			
			return;
		}
		
		publishAssociations();
	}
	
	protected void
	subscriptionUpdated()
	{
		if ( dht_plugin_public != null ){
			
			publishSubscriptions();
		}
	}
	
	protected void
	publishSubscriptions()
	{
		List	 shuffled_subs;

		synchronized( this ){
			
			if ( publish_subscription_active ){
				
				return;
			}			
			
			shuffled_subs = new ArrayList( subscriptions );

			publish_subscription_active = true;
		}
	
		boolean	publish_initiated = false;
		
		try{
			Collections.shuffle( shuffled_subs );

            for (Object shuffled_sub : shuffled_subs) {

                SubscriptionImpl sub = (SubscriptionImpl) shuffled_sub;

                if (sub.isSubscribed() && sub.isPublic() && !sub.getPublished()) {

                    sub.setPublished(true);

                    publishSubscription(sub);

                    publish_initiated = true;

                    break;
                }
            }
		}finally{
			
			if ( !publish_initiated ){
				
				log( "Publishing Subscriptions Complete" );
				
				synchronized( this ){
	
					publish_subscription_active = false;
				}
			}
		}
	}
	
	protected void
	publishSubscription(
		final SubscriptionImpl					subs )
	{
		log( "Checking subscription publication '" + subs.getString() + "'" );
		
		byte[]	sub_id 		= subs.getShortID();
		int		sub_version	= subs.getVersion();
				
		final String	key = "subscription:publish:" + ByteFormatter.encodeString( sub_id ) + ":" + sub_version; 
			
		final DHTPluginInterface dht_plugin = selectDHTPlugin( subs );
		
		if ( dht_plugin == null ){
			
			return;
		}
		
		dht_plugin.get(
			getKeyBytes(key),
			"Subs presence read: " + ByteFormatter.encodeString( sub_id ) + ":" + sub_version,
			DHTPlugin.FLAG_SINGLE_VALUE,
			24,
			60*1000*(subs.isAnonymous()?2:1),
			false,
			false,
			new DHTPluginOperationListener()
			{
				private int		hits;
				private boolean	diversified;
				
				public boolean
				diversified()
				{					
					diversified = true;
					
					return( false );
				}
				
				public void 
				starts(
					byte[] 				key ) 
				{
				}
				
				public void
				valueRead(
					DHTPluginContact	originator,
					DHTPluginValue		value )
				{					
					byte[]	data = value.getValue();
						
					try{
						Map	details = decodeSubscriptionDetails( data );

						if ( subs.getVerifiedPublicationVersion( details ) == subs.getVersion()){
						
							hits++;
						}
					}catch( Throwable e ){
						
					}
				}
				
				public void
				valueWritten(
					DHTPluginContact	target,
					DHTPluginValue		value )
				{
				}
				
				public void
				complete(
					byte[]				original_key,
					boolean				timeout_occurred )
				{
					log( "Checked subscription publication '" + subs.getString() + "' - hits=" + hits + ",div=" + diversified );

					if ( hits < 10 && !diversified ){			
			
						log( "    Publishing subscription '" + subs.getString() + ", existing=" + hits );

						try{
							byte[]	put_value = encodeSubscriptionDetails( subs );						
							
							if ( put_value.length < DHTPlugin.MAX_VALUE_SIZE ){
								
								byte	flags = DHTPlugin.FLAG_SINGLE_VALUE;
								
								if ( hits < 3 && !diversified ){
									
									flags |= DHTPlugin.FLAG_PRECIOUS;
								}
								
								if ( subs.isAnonymous()){
									
									flags |= DHTPlugin.FLAG_BRIDGED;
								}
																
								dht_plugin.put(
									getKeyBytes(key),
									"Subs presence write: " + Base32.encode( subs.getShortID() ) + ":" + subs.getVersion(),
									put_value,
									flags,
									new DHTPluginOperationListener()
									{
										public boolean
										diversified()
										{
											return( true );
										}
										
										public void 
										starts(
											byte[] 				key ) 
										{
										}
										
										public void
										valueRead(
											DHTPluginContact	originator,
											DHTPluginValue		value )
										{
										}
										
										public void
										valueWritten(
											DHTPluginContact	target,
											DHTPluginValue		value )
										{
										}
										
										public void
										complete(
											byte[]				key,
											boolean				timeout_occurred )
										{
											log( "        completed '" + subs.getString() + "'" );
						
											publishNext();
										}
									});
								
							}else{
								
								publishNext();
							}
						}catch( Throwable e ){
							
							Debug.printStackTrace( e );
							
							publishNext();
						}
						
					}else{
						
						log( "    Not publishing subscription '" + subs.getString() + "', existing =" + hits );

						publishNext();
					}
				}
				
				protected void
				publishNext()
				{
					synchronized( SubscriptionManagerImpl.this ){
						
						publish_subscription_active = false;
					}
					
					publishSubscriptions();
				}
			});
	}
	
	protected void
	updateSubscription(
		final SubscriptionImpl		subs,
		final int					new_version )
	{
		log( "Subscription " + subs.getString() + " - higher version found: " + new_version );
		
		if ( !subs.canAutoUpgradeCheck()){
			
			log( "    Checked too recently or not updateable, ignoring" );
			
			return;
		}
		
		if ( subs.getHighestUserPromptedVersion() >= new_version ){
			
			log( "    User has already been prompted for version " + new_version + " so ignoring" );
			
			return;
		}
					
		byte[]	sub_id 		= subs.getShortID();
			
		if ( !subs.isAnonymous()){
			
			try{
				PlatformSubscriptionsMessenger.subscriptionDetails details = PlatformSubscriptionsMessenger.getSubscriptionBySID( sub_id, false );
				
				if ( !askIfCanUpgrade( subs, new_version )){
					
					return;
				}
				
				VuzeFileHandler vfh = VuzeFileHandler.getSingleton();
				
				VuzeFile vf = vfh.loadVuzeFile( Base64.decode( details.getContent()));
								
				vfh.handleFiles( new VuzeFile[]{ vf }, VuzeFileComponent.COMP_TYPE_SUBSCRIPTION );
				
				return;
				
			}catch( Throwable e ){
				
				log( "Failed to read subscription from platform, trying DHT" );
			}
		}
		
		log( "Checking subscription '" + subs.getString() + "' upgrade to version " + new_version );

		final String	key = "subscription:publish:" + ByteFormatter.encodeString( sub_id ) + ":" + new_version; 
			
		DHTPluginInterface dht_plugin = selectDHTPlugin( subs );

		dht_plugin.get(
			getKeyBytes(key),
			"Subs update read: " + Base32.encode( sub_id ) + ":" + new_version,
			DHTPlugin.FLAG_SINGLE_VALUE,
			12,
			60*1000*(subs.isAnonymous()?2:1),
			false,
			false,
			new DHTPluginOperationListener()
			{
				private byte[]	verified_hash;
				private int		verified_size;
				
				public boolean
				diversified()
				{
					return( true );
				}
				
				public void 
				starts(
					byte[] 				key ) 
				{
				}
				
				public void
				valueRead(
					DHTPluginContact	originator,
					DHTPluginValue		value )
				{
					byte[]	data = value.getValue();
							
					try{
						Map	details = decodeSubscriptionDetails( data );
						
						if ( 	verified_hash == null && 
								subs.getVerifiedPublicationVersion( details ) == new_version ){
							
							verified_hash 	= SubscriptionImpl.getPublicationHash( details );
							verified_size	= SubscriptionImpl.getPublicationSize( details );
						}
						
					}catch( Throwable e ){
						
					}
				}
				
				public void
				valueWritten(
					DHTPluginContact	target,
					DHTPluginValue		value )
				{
				}
				
				public void
				complete(
					byte[]				original_key,
					boolean				timeout_occurred )
				{
					if ( verified_hash != null ){			
			
						log( "    Subscription '" + subs.getString() + " upgrade verified as authentic" );

						updateSubscription( subs, new_version, verified_hash, verified_size );
						
					}else{
						
						log( "    Subscription '" + subs.getString() + " upgrade not verified" );
					}
				}
			});
	}
	
	protected byte[]
	encodeSubscriptionDetails(
		SubscriptionImpl		subs )
	
		throws IOException
	{
		Map		details = subs.getPublicationDetails();					
		
			// inject a random element so we can count occurrences properly (as the DHT logic
			// removes duplicates)
		
		details.put( "!", (long) random_seed);
		
		byte[] encoded = BEncoder.encode( details );
				
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
				
		GZIPOutputStream os = new GZIPOutputStream( baos );

		os.write( encoded );
		
		os.close();
		
		byte[] compressed = baos.toByteArray();
		
		byte	header;
		byte[]	data;
		
		if ( compressed.length < encoded.length ){
			
			header 	= 1;
			data	= compressed; 
		}else{
			
			header	= 0;
			data	= encoded;
		}
				
		byte[] result = new byte[data.length+1];
		
		result[0] = header;
		
		System.arraycopy( data, 0, result, 1, data.length );
		
		return( result );
	}
	
	protected Map
	decodeSubscriptionDetails(
		byte[]			data )
	
		throws IOException
	{
		byte[]	to_decode;
		
		if ( data[0] == 0 ){
			
			to_decode = new byte[ data.length-1 ];
			
			System.arraycopy( data, 1, to_decode, 0, data.length - 1 );
			
		}else{
			
			GZIPInputStream is = new GZIPInputStream(new ByteArrayInputStream( data, 1, data.length - 1 ));
			
			to_decode = FileUtil.readInputStreamAsByteArray( is );
			
			is.close();
		}
		
		Map res = BDecoder.decode( to_decode );
		
			// remove any injected random seed
		
		res.remove( "!" );
		
		return( res );
	}
	
	protected void
	updateSubscription(
		final SubscriptionImpl			subs,
		final int						update_version,
		final byte[]					update_hash,
		final int						update_size )
	{
		log( "Subscription " + subs.getString() + " - update hash=" + ByteFormatter.encodeString( update_hash ) + ", size=" + update_size );

		new AEThread2( "SubsUpdate", true )
		{
			public void
			run()
			{
				try{
					Object[] res = downloadTorrent( update_hash, update_size );
					
					if ( res != null ){
					
						updateSubscription( subs, update_version, (TOTorrent)res[0], (InetSocketAddress)res[1] );
					}
				}catch( Throwable e ){
					
					log( "    update failed", e );
				}
			}
		}.start();
	}
	
	protected Object[]
	downloadTorrent(
		byte[]		hash,
		int			update_size )
	{
		if ( !isSubsDownloadEnabled()){
			
			log( "    Can't download subscription " + Base32.encode( hash ) + " as feature disabled" );
			
			return( null );
		}
		
		final MagnetPlugin	magnet_plugin = getMagnetPlugin();
	
		if ( magnet_plugin == null ){
		
			log( "    Can't download, no magnet plugin" );
		
			return( null );
		}

		try{
			final InetSocketAddress[] sender = { null };
			
			byte[] torrent_data = magnet_plugin.download(
				new MagnetPluginProgressListener()
				{
					public void
					reportSize(
						long	size )
					{
					}
					
					public void
					reportActivity(
						String	str )
					{
						log( "    MagnetDownload: " + str );
					}
					
					public void
					reportCompleteness(
						int		percent )
					{
					}
					
					public void
					reportContributor(
						InetSocketAddress	address )
					{
						synchronized( sender ){
						
							sender[0] = address;
						}
					}

					public boolean 
					verbose() 
					{
						return( false );
					}
					
					public boolean 
					cancelled() 
					{
						return( false );
					}
				},
				hash,
				"",
				new InetSocketAddress[0],
				300*1000,
				MagnetPlugin.FL_DISABLE_MD_LOOKUP );
			
			if ( torrent_data == null ){
				
				log( "    download failed - timeout" );
				
				return( null );
			}
			
			log( "Subscription torrent downloaded" );
			
			TOTorrent torrent = TOTorrentFactory.deserialiseFromBEncodedByteArray( torrent_data );
		
				// update size is just that of signed content, torrent itself is .vuze file
				// so take this into account
			
			if ( torrent.getSize() > update_size + 10*1024 ){
			
				log( "Subscription download abandoned, torrent size is " + torrent.getSize() + ", underlying data size is " + update_size );
				
				return( null );
			}
			
			if ( torrent.getSize() > 4*1024*1024 ){
				
				log( "Subscription download abandoned, torrent size is too large (" + torrent.getSize() + ")" );
				
				return( null );
			}
			
			synchronized( sender ){
			
				return( new Object[]{ torrent, sender[0] });
			}
			
		}catch( Throwable e ){
			
			log( "    download failed", e );
			
			return( null );
		}
	}
	
	protected void
	downloadSubscription(
		final TOTorrent			torrent,
		final InetSocketAddress	peer,
		byte[]					subs_id,
		int						version,
		String					name,
		final downloadListener	listener )
	{
		try{
				// testing purposes, see if local exists
			
			LightWeightSeed lws = LightWeightSeedManager.getSingleton().get( new HashWrapper( torrent.getHash()));
	
			if ( lws != null ){
				
				log( "Light weight seed found" );
				
				listener.complete( lws.getDataLocation());
				
			}else{
				String	sid = ByteFormatter.encodeString( subs_id );
				
				File	dir = getSubsDir();
				
				dir = new File( dir, "temp" );
				
				if ( !dir.exists()){
					
					if ( !dir.mkdirs()){
						
						throw( new IOException( "Failed to create dir '" + dir + "'" ));
					}
				}
				
				final File	torrent_file 	= new File( dir, sid + "_" + version + ".torrent" );
				final File	data_file 		= new File( dir, sid + "_" + version + ".vuze" );
	
				PluginInterface pi = PluginInitializer.getDefaultInterface();
			
				final DownloadManager dm = pi.getDownloadManager();
				
				Download download = dm.getDownload( torrent.getHash());
				
				if ( download == null ){
					
					log( "Adding download for subscription '" + new String(torrent.getName()) + "'" );
					
					boolean is_update = getSubscriptionFromSID( subs_id ) != null;
					
					PlatformTorrentUtils.setContentTitle(torrent, (is_update?"Update":"Download") + " for subscription '" + name + "'" );
					
						// PlatformTorrentUtils.setContentThumbnail(torrent, thumbnail);
						
					TorrentUtils.setFlag( torrent, TorrentUtils.TORRENT_FLAG_LOW_NOISE, true );
					
					Torrent t = new TorrentImpl( torrent );
					
					t.setDefaultEncoding();
					
					t.writeToFile( torrent_file );
					
					download = dm.addDownload( t, torrent_file, data_file );
					
					download.setFlag( Download.FLAG_DISABLE_AUTO_FILE_MOVE, true );

					download.setBooleanAttribute( ta_subs_download, true );
					
					Map rd = listener.getRecoveryData();
					
					if ( rd != null ){
						
						download.setMapAttribute( ta_subs_download_rd, rd );
					}
				}else{
					
					log( "Existing download found for subscription '" + new String(torrent.getName()) + "'" );
				}
				
				final Download f_download = download;
				
				final TimerEventPeriodic[] event = { null };
				
				event[0] = 
					SimpleTimer.addPeriodicEvent(
						"SM:cancelTimer",
						10*1000,
						new TimerEventPerformer()
						{
							private long	start_time = SystemTime.getMonotonousTime();
							
							public void 
							perform(
								TimerEvent ev ) 
							{
								boolean	kill = false;
								
								try{	
									Download download = dm.getDownload( torrent.getHash());
									
									if ( listener.isCancelled() || download == null ){
										
										kill = true;
										
									}else{
										
										int	state = download.getState();
										
										if ( state == Download.ST_ERROR ){
											
											log( "Download entered error state, removing" );
											
											kill = true;
											
										}else{
											
											long	now = SystemTime.getMonotonousTime();
											
											long	running_for = now - start_time;
											
											if ( running_for > 10*60*1000 ){
												
												log( "Download hasn't completed in permitted time, removing" );
												
												kill = true;
												
											}else if ( running_for > 4*60*1000 ){
												
												if ( download.getStats().getDownloaded() == 0 ){
													
													log( "Download has zero downloaded, removing" );
													
													kill = true;
												}
											}else if ( running_for > 2*60*1000 ){
													
												DownloadScrapeResult scrape = download.getLastScrapeResult();
												
												if ( scrape == null || scrape.getSeedCount() <= 0 ){
													
													log( "Download has no seeds, removing" );
													
													kill = true;
												}
											}
										}
									}
								}catch( Throwable e ){
									
									log( "Download failed", e );

									kill = true;
								}
								
								if ( kill && event[0] != null ){
									
									try{
										event[0].cancel();
										
										if ( !listener.isCancelled()){
																					
											listener.failed( new SubscriptionException( "Download abandoned" ));
										}
									}finally{
										
										removeDownload( f_download, true );
									
										torrent_file.delete();
									}
								}
							}
						});
				
				download.addCompletionListener(
					new DownloadCompletionListener()
					{
						public void 
						onCompletion(
							Download d ) 
						{
							listener.complete( d, torrent_file );
						}
					});
				
				if ( download.isComplete()){
					
					listener.complete( download, torrent_file  );
					
				}else{
								
					download.setForceStart( true );
					
					if ( peer != null ){
					
						download.addPeerListener(
							new DownloadPeerListener()
							{
								public void
								peerManagerAdded(
									Download		download,
									PeerManager		peer_manager )
								{									
									InetSocketAddress tcp = AddressUtils.adjustTCPAddress( peer, true );
									InetSocketAddress udp = AddressUtils.adjustUDPAddress( peer, true );
									
									log( "    Injecting peer into download: " + tcp );

									peer_manager.addPeer( tcp.getAddress().getHostAddress(), tcp.getPort(), udp.getPort(), true );
								}
								
								public void
								peerManagerRemoved(
									Download		download,
									PeerManager		peer_manager )
								{							
								}
							});
					}
				}
			}
		}catch( Throwable e ){
			
			log( "Failed to add download", e );
			
			listener.failed( e );
		}
	}
	
	protected interface
	downloadListener
	{
		void
		complete(
                File data_file);
		
		void
		complete(
                Download download,
                File torrent_file);
			
		void
		failed(
                Throwable error);
		
		Map
		getRecoveryData();
		
		boolean
		isCancelled();
	}
	
	protected void
	updateSubscription(
		final SubscriptionImpl		subs,
		final int					new_version,
		TOTorrent					torrent,
		InetSocketAddress			peer )
	{
		log( "Subscription " + subs.getString() + " - update torrent: " + new String( torrent.getName()));

		if ( !askIfCanUpgrade( subs, new_version )){
			
			return;
		}
			
		downloadSubscription(
			torrent,
			peer,
			subs.getShortID(),
			new_version,
			subs.getName(false),
			new downloadListener()
			{
				public void
				complete(
					File		data_file )
				{
					updateSubscription( subs, data_file );
				}
				
				public void
				complete(
					Download	download,	
					File		torrent_file )
				{
					updateSubscription( subs, download, torrent_file, new File( download.getSavePath()));
				}
					
				public void
				failed(
					Throwable	error )
				{
					log( "Failed to download subscription", error );
				}
				
				public Map
				getRecoveryData()
				{
					Map	rd = new HashMap();
					
					rd.put( "sid", subs.getShortID());
					rd.put( "ver", (long) new_version);
					
					return( rd );
				}
				
				public boolean
				isCancelled()
				{
					return( false );
				}
			});
	}

	protected boolean
	askIfCanUpgrade(
		SubscriptionImpl		subs,
		int						new_version )
	{
		subs.setHighestUserPromptedVersion( new_version );
		
		UIManager ui_manager = StaticUtilities.getUIManager( 120*1000 );
		
		String details = MessageText.getString(
				"subscript.add.upgradeto.desc",
				new String[]{ String.valueOf(new_version), subs.getName()});
		
		long res = ui_manager.showMessageBox(
				"subscript.add.upgrade.title",
				"!" + details + "!",
				UIManagerEvent.MT_YES | UIManagerEvent.MT_NO );
		
		if ( res != UIManagerEvent.MT_YES ){	
		
			log( "    User declined upgrade" );
			
			return( false );
		}
		
		return( true );
	}
	
	protected boolean
	recoverSubscriptionUpdate(
		Download				download,
		final Map				rd )
	{
		byte[]	sid 	= (byte[])rd.get( "sid" );
		int		version = ((Long)rd.get( "ver" )).intValue();
		
		final SubscriptionImpl subs = getSubscriptionFromSID( sid );
		
		if ( subs == null ){
		
			log( "Can't recover '" + download.getName() + "' - subscription " + ByteFormatter.encodeString( sid ) +  " not found" );
			
			return( false );
		}
		
		downloadSubscription(
				((TorrentImpl)download.getTorrent()).getTorrent(),
				null,
				subs.getShortID(),
				version,
				subs.getName(false),
				new downloadListener()
				{
					public void
					complete(
						File		data_file )
					{
						updateSubscription( subs, data_file );
					}
					
					public void
					complete(
						Download	download,	
						File		torrent_file )
					{
						updateSubscription( subs, download, torrent_file, new File( download.getSavePath()));
					}
						
					public void
					failed(
						Throwable	error )
					{
						log( "Failed to download subscription", error );
					}
					
					public Map
					getRecoveryData()
					{
						return( rd );
					}
					
					public boolean
					isCancelled()
					{
						return( false );
					}
				});
		
		return( true );
	}
	
	protected void
	updateSubscription(
		SubscriptionImpl		subs,
		Download				download,
		File					torrent_file,
		File					data_file )
	{
		try{
			removeDownload( download, false );
		
			try{				
				updateSubscription( subs, data_file );
											
			}finally{
				
				if ( !data_file.delete()){
					
					log( "Failed to delete update file '" + data_file + "'" );
				}
				
				if ( !torrent_file.delete()){
					
					log( "Failed to delete update torrent '" + torrent_file + "'" );
				}
			}
		}catch( Throwable e ){
			
			log( "Failed to remove update download", e );
		}
	}
	
	protected void
	removeDownload(
		Download		download,
		boolean			remove_data )
	{
		try{
			download.stop();
			
		}catch( Throwable e ){
		}
		
		try{
			download.remove( true, remove_data );
			
			log( "Removed download '" + download.getName() + "'" );
			
		}catch( Throwable e ){
			
			log( "Failed to remove download '" + download.getName() + "'", e );
		}
	}
	
	protected void
	updateSubscription(
		SubscriptionImpl		subs,
		File					data_location )
	{
		log( "Updating subscription '" + subs.getString() + " using '" + data_location + "'" );
		
		VuzeFileHandler vfh = VuzeFileHandler.getSingleton();
			
		VuzeFile vf = vfh.loadVuzeFile( data_location.getAbsolutePath());
						
		vfh.handleFiles( new VuzeFile[]{ vf }, VuzeFileComponent.COMP_TYPE_SUBSCRIPTION );
	}
	
	protected MagnetPlugin
	getMagnetPlugin()
	{
		PluginInterface  pi  = AzureusCoreFactory.getSingleton().getPluginManager().getPluginInterfaceByClass( MagnetPlugin.class );
	
		if ( pi == null ){
			
			return( null );
		}
		
		return((MagnetPlugin)pi.getPlugin());
	}
	
	protected Engine
	getEngine(
		SubscriptionImpl		subs,
		Map						json_map,
		boolean					local_only )
	
		throws SubscriptionException
	{
		long id = (Long) json_map.get("engine_id");
		
		Engine engine = MetaSearchManagerFactory.getSingleton().getMetaSearch().getEngine( id );
		
		if ( engine != null ){
			
			return( engine );
		}

		if ( !local_only ){
			
			try{
				if ( id >= 0 && id < Integer.MAX_VALUE ){
								
					log( "Engine " + id + " not present, loading" );
						
						// vuze template but user hasn't yet loaded it
						
					try{
						engine = MetaSearchManagerFactory.getSingleton().getMetaSearch().addEngine( id );
									
						return( engine );
						
					}catch( Throwable e ){
					
						throw( new SubscriptionException( "Failed to load engine '" + id + "'", e ));
					}
				}
			}catch( Throwable e ){
				
				log( "Failed to load search template", e );
			}
		}
		
		engine = subs.extractEngine( json_map, id );
		
		if ( engine != null ){
			
			return( engine );
		}
		
		throw( new SubscriptionException( "Failed to extract engine id " + id ));
	}
	
	protected LinkedHashMap<String,SubscriptionResultImpl>
	loadResults(
		SubscriptionImpl			subs )
	{
		synchronized( result_cache ){
			
			Object[]	entry = result_cache.get( subs );
			
			if ( entry != null ){
				
				entry[1] = SystemTime.getMonotonousTime();
				
				return((LinkedHashMap<String,SubscriptionResultImpl>)entry[0]);
			}
		
			LinkedHashMap	results = new LinkedHashMap<String,SubscriptionResultImpl>(1024);
			
			try{
				File	f = getResultsFile( subs );
								
				Map	map = FileUtil.readResilientFile( f );
				
				List	list = (List)map.get( "results" );
				
				if ( list != null ){
				
					SubscriptionHistoryImpl	history = (SubscriptionHistoryImpl)subs.getHistory();

                    for (Object o : list) {

                        Map result_map = (Map) o;

                        try {
                            SubscriptionResultImpl result = new SubscriptionResultImpl(history, result_map);

                            results.put(result.getID(), result);

                        } catch (Throwable e) {

                            log("Failed to decode result '" + result_map + "'", e);
                        }
                    }
				}
				
			}catch( Throwable e ){
				
				log( "Failed to load results for '" + subs.getName() + "' - continuing with empty result set", e );
			}
			
			result_cache.put( subs, new Object[]{ results, SystemTime.getMonotonousTime() });
			
			if ( result_cache.size() > 5 ){
				
				SubscriptionImpl	oldest_sub 	= null;
				long				oldest_time	= Long.MAX_VALUE;
				
				for ( Map.Entry<SubscriptionImpl,Object[]> x: result_cache.entrySet()){
					
					long time = (Long)x.getValue()[1];
					
					if ( time < oldest_time ){
						
						oldest_time	= time;
						oldest_sub	= x.getKey();
					}
				}
				
				result_cache.remove( oldest_sub );
			}
			
			return( results );
		}
	}
	
	protected void
  	setCategoryOnExisting(
  		SubscriptionImpl	subscription,
  		String				old_category,
  		String				new_category )
  	{
		PluginInterface default_pi = PluginInitializer.getDefaultInterface();

  		Download[] downloads 	= default_pi.getDownloadManager().getDownloads();
  		 		 		
  		for ( Download d: downloads ){
  			 			
  			if ( subscriptionExists( d, subscription )){
  					
				String existing = d.getAttribute( ta_category );

				if ( existing == null || existing.equals( old_category )){
					
					d.setAttribute( ta_category, new_category );
				}
  			}
  		}
  	}
	
	public int
	getMaxNonDeletedResults()
	{
		return( COConfigurationManager.getIntParameter( CONFIG_MAX_RESULTS ));
	}
	
	public void
	setMaxNonDeletedResults(
		int		max )
	{
		if ( max != getMaxNonDeletedResults()){
			
			COConfigurationManager.setParameter( CONFIG_MAX_RESULTS, max );
		}
	}
	
	public boolean
	getAutoStartDownloads()
	{
		return( COConfigurationManager.getBooleanParameter( CONFIG_AUTO_START_DLS ));		
	}
	
	public void
	setAutoStartDownloads(
		boolean		auto_start )
	{
		if ( auto_start != getAutoStartDownloads()){
			
			COConfigurationManager.setParameter( CONFIG_AUTO_START_DLS, auto_start );
		}		
	}
	
	public int
	getAutoStartMinMB()
	{
		return( COConfigurationManager.getIntParameter( CONFIG_AUTO_START_MIN_MB ));
	}
	
	public void
	setAutoStartMinMB(
		int			mb )
	{
		if ( mb != getAutoStartMinMB()){
			
			COConfigurationManager.setParameter( CONFIG_AUTO_START_MIN_MB, mb );
		}
	}

	public int
	getAutoStartMaxMB()
	{
		return( COConfigurationManager.getIntParameter( CONFIG_AUTO_START_MAX_MB ));
	}
	
	public void
	setAutoStartMaxMB(
		int			mb )
	{
		if ( mb != getAutoStartMaxMB()){
			
			COConfigurationManager.setParameter( CONFIG_AUTO_START_MAX_MB, mb );
		}
	}
	
	public int
	getAutoDownloadMarkReadAfterDays()
	{
		return( COConfigurationManager.getIntParameter( CONFIG_AUTO_MARK_READ ));
	}
	
	public void
	setAutoDownloadMarkReadAfterDays(
		int		days )
	{
		if ( days != getAutoDownloadMarkReadAfterDays()){
			
			COConfigurationManager.setParameter( CONFIG_AUTO_MARK_READ, days );
		}
	}
	
	protected boolean
	shouldAutoStart(
		Torrent		torrent )
	{
		if ( getAutoStartDownloads()){
			
			long	min = getAutoStartMinMB()*1024*1024L;
			long	max = getAutoStartMaxMB()*1024*1024L;
			
			if ( min <= 0 && max <= 0 ){
				
				return( true );
			}
			
			long size = torrent.getSize();
			
			if ( min > 0 && size < min ){
				
				return( false );
			}
			
			if ( max > 0 && size > max ){
				
				return( false );
			}
			
			return( true );
			
		}else{
			
			return( false );
		}
	}
	
	protected void
 	saveResults(
 		SubscriptionImpl			subs,
 		SubscriptionResultImpl[]	results )
 	{
		synchronized( result_cache ){
			
			result_cache.remove( subs );
			
			try{
				File	f = getResultsFile( subs );
		
				Map	map = new HashMap();
				
				List	list = new ArrayList( results.length );
				
				map.put( "results", list );

                for (SubscriptionResultImpl result : results) {

                    list.add(result.toBEncodedMap());
                }
				
				FileUtil.writeResilientFile( f, map );
				
			}catch( Throwable e ){
				
				log( "Failed to save results for '" + subs.getName(), e );
			}
		}
 	}
	
	private void
	loadConfig()
	{
		if ( !FileUtil.resilientConfigFileExists( CONFIG_FILE )){
			
			return;
		}
		
		log( "Loading configuration" );
		
		boolean	some_are_mine = false;
		
		synchronized( this ){
			
			Map map = FileUtil.readResilientConfigFile( CONFIG_FILE );
			
			List	l_subs = (List)map.get( "subs" );
			
			if ( l_subs != null ){

                for (Object l_sub : l_subs) {

                    Map m = (Map) l_sub;

                    try {
                        SubscriptionImpl sub = new SubscriptionImpl(this, m);

                        int index = Collections.binarySearch(subscriptions, sub, new Comparator<Subscription>() {
                            public int compare(Subscription arg0, Subscription arg1) {
                                return arg0.getID().compareTo(arg1.getID());
                            }
                        });
                        if (index < 0) {
                            index = -1 * index - 1; // best guess

                            subscriptions.add(index, sub);
                        }

                        if (sub.isMine()) {

                            some_are_mine = true;
                        }

                        log("    loaded " + sub.getString());

                    } catch (Throwable e) {

                        log("Failed to import subscription from " + m, e);
                    }
                }
			}
		}
		
		if ( some_are_mine ){
							
			addMetaSearchListener();
		}
	}
	
	protected void
	configDirty(
		SubscriptionImpl		subs )
	{
		changeSubscription( subs );
		
		configDirty();
	}
	
	protected void
	configDirty()
	{
		synchronized( this ){
			
			if ( config_dirty ){
				
				return;
			}
			
			config_dirty = true;
		
			new DelayedEvent( 
				"Subscriptions:save", 5000,
				new AERunnable()
				{
					public void 
					runSupport() 
					{
						synchronized( SubscriptionManagerImpl.this ){
							
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
			
			if ( subscriptions.size() == 0 ){
				
				FileUtil.deleteResilientConfigFile( CONFIG_FILE );
				
			}else{
				
				Map map = new HashMap();
				
				List	l_subs = new ArrayList();
				
				map.put( "subs", l_subs );

                for (SubscriptionImpl sub : subscriptions) {

                    try {
                        l_subs.add(sub.toMap());

                    } catch (Throwable e) {

                        log("Failed to save subscription " + sub.getString(), e);
                    }
                }
				
				FileUtil.writeResilientConfigFile( CONFIG_FILE, map );
			}
		}
	}
	
	private byte[]
	getKeyBytes( 
		String		key )
	{
        return( key.getBytes(StandardCharsets.UTF_8));

    }
	private AEDiagnosticsLogger
	getLogger()
	{
			// sync not required (and has caused deadlock) as AEDiagnostics handles singleton
		
		if ( logger == null ){
			
			logger = AEDiagnostics.getLogger( LOGGER_NAME );
		}
		
		return( logger );
	}
	
	public void 
	log(
		String 		s,
		Throwable 	e )
	{
		AEDiagnosticsLogger diag_logger = getLogger();
		
		diag_logger.log( s );
		diag_logger.log( e );
	}
	
	public void 
	log(
		String 	s )
	{
		AEDiagnosticsLogger diag_logger = getLogger();
		
		diag_logger.log( s );
	}
	
	public void
	addListener(
		SubscriptionManagerListener	listener )
	{
		listeners.add( listener );
	}
	
	public void
	removeListener(
		SubscriptionManagerListener	listener )
	{
		listeners.remove( listener );
	}
	
	public void
	generate(
		IndentWriter		writer )
	{
		writer.println( "Subscriptions" );
			
		try{
			writer.indent();

			Subscription[] subs = getSubscriptions();

            for (Subscription sub1 : subs) {

                SubscriptionImpl sub = (SubscriptionImpl) sub1;

                sub.generate(writer);
            }
			
		}finally{
			
			writer.exdent();
		}
	}
	
	private static class
	searchMatcher
	{
		private String[]	bits;
		private int[]		bit_types;
		private Pattern[]	bit_patterns;

		protected 
		searchMatcher(
			String		term )
		{
			bits = Constants.PAT_SPLIT_SPACE.split(term.toLowerCase() );
			
			bit_types 		= new int[bits.length];
			bit_patterns 	= new Pattern[bits.length];
			
			for (int i=0;i<bits.length;i++){
				
				String bit = bits[i] = bits[i].trim();
				
				if ( bit.length() > 0 ){
					
					char	c = bit.charAt(0);
					
					if ( c == '+' ){
						
						bit_types[i] = 1;
						
						bit = bits[i] = bit.substring(1);
						
					}else if ( c == '-' ){
						
						bit_types[i] = 2;
						
						bit = bits[i] = bit.substring(1);
					}
					
					if ( bit.startsWith( "(" ) && bit.endsWith((")"))){
						
						bit = bit.substring( 1, bit.length()-1 );
						
						try{
							bit_patterns[i] = Pattern.compile( bit, Pattern.CASE_INSENSITIVE );
							
						}catch( Throwable e ){
						}
					}else if ( bit.contains( "|" )){
						
						try{
							bit_patterns[i] = Pattern.compile( bit, Pattern.CASE_INSENSITIVE );
							
						}catch( Throwable e ){
						}
					}
				}
			}
		}
		
		public boolean
		matches(
			String		str )
		{			
			// term is made up of space separated bits - all bits must match
			// each bit can be prefixed by + or -, a leading - means 'bit doesn't match'. + doesn't mean anything
			// each bit (with prefix removed) can be "(" regexp ")"
			// if bit isn't regexp but has "|" in it it is turned into a regexp so a|b means 'a or b'
									
			str = str.toLowerCase();
			
			boolean	match 			= true;
			boolean	at_least_one 	= false;
			
			for (int i=0;i<bits.length;i++){
				
				String bit = bits[i];
				
				if ( bit.length() > 0 ){
					
					boolean	hit;
					
					if ( bit_patterns[i] == null ){
					
						hit = str.contains( bit );
						
					}else{
					
						hit = bit_patterns[i].matcher( str ).find();
					}
					
					int	type = bit_types[i];
					
					if ( hit ){
												
						if ( type == 2 ){
							
							match = false;
							
							break;
							
						}else{
							
							at_least_one = true;

						}
					}else{
						
						if ( type == 2 ){
						
							at_least_one = true;
							
						}else{
							
							match = false;
						
							break;
						}
					}
				}
			}
			
			boolean res = match && at_least_one;
				
			return( res );
		}
	}
	
	private DHTPluginInterface
	selectDHTPlugin(
		SubscriptionImpl		subs )
	{
		if ( subs.isAnonymous()){
			
			List<DistributedDatabase> ddbs = AzureusCoreFactory.getSingleton().getPluginManager().getDefaultPluginInterface().getUtilities().getDistributedDatabases( new String[]{ AENetworkClassifier.AT_I2P });
			
			if ( ddbs.size() > 0 ){
				
				return( ddbs.get(0).getDHTPlugin());
			}
			
			return( null );
			
		}else{
			
			return( dht_plugin_public );
		}
	}
	
	private DHTPluginInterface
	selectDHTPlugin(
		Download		download )
	{
		String[]	networks = download.getListAttribute( ta_networks );

		return( selectDHTPlugin( networks ));
	}
	
	private DHTPluginInterface
	selectDHTPlugin(
		String[]		networks )
	{
		if ( networks.length > 0 ){
			
			for ( String net: networks ){
				
				if (AENetworkClassifier.AT_PUBLIC.equals(net)){
					
					return( dht_plugin_public );
				}
			}
						
			List<DistributedDatabase> ddbs = AzureusCoreFactory.getSingleton().getPluginManager().getDefaultPluginInterface().getUtilities().getDistributedDatabases( new String[]{ AENetworkClassifier.AT_I2P });
			
			if ( ddbs.size() > 0 ){
				
				return( ddbs.get(0).getDHTPlugin());
			}
		}
		
		return( null );
	}
	
	public static void
	main(
		String[]	args )
	{
		final String 	NAME 	= "lalalal";
		final String	URL_STR	= "http://www.vuze.com/feed/publisher/ALL/1";
		
		try{
			//AzureusCoreFactory.create();
			/*
			Subscription subs = 
				getSingleton(true).createSingletonRSS(
						NAME,
						new URL( URL_STR ),
						240 );
			
			subs.getVuzeFile().write( new File( "C:\\temp\\srss.vuze" ));
			
			subs.remove();
			*/
			
			VuzeFile	vf = VuzeFileHandler.getSingleton().create();
			
			Map	map = new HashMap();
			
			map.put( "name", NAME );
			map.put( "url", URL_STR );
			map.put( "public", 0L);
			map.put( "check_interval_mins", 345L);
			
			vf.addComponent( VuzeFileComponent.COMP_TYPE_SUBSCRIPTION_SINGLETON, map );
			
			vf.write( new File( "C:\\temp\\srss_2.vuze" ) );

		}catch( Throwable e ){
			
			e.printStackTrace();
		}
	}
}
