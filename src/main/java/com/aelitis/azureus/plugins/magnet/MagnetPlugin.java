/*
 * Created on 03-Mar-2005
 * Created by Paul Gardner
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
 *
 */

package com.aelitis.azureus.plugins.magnet;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.NoRouteToHostException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.net.InetSocketAddress;
import java.util.concurrent.CopyOnWriteArrayList;

import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentAnnounceURLGroup;
import org.gudy.azureus2.core3.torrent.TOTorrentAnnounceURLSet;
import org.gudy.azureus2.core3.torrent.TOTorrentFactory;
import org.gudy.azureus2.core3.util.AEMonitor;
import org.gudy.azureus2.core3.util.AENetworkClassifier;
import org.gudy.azureus2.core3.util.AERunnable;
import org.gudy.azureus2.core3.util.AESemaphore;
import org.gudy.azureus2.core3.util.AEThread2;
import org.gudy.azureus2.core3.util.AsyncDispatcher;
import org.gudy.azureus2.core3.util.BEncoder;
import org.gudy.azureus2.core3.util.Base32;
import org.gudy.azureus2.core3.util.ByteArrayHashMap;
import org.gudy.azureus2.core3.util.ByteFormatter;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.DelayedEvent;
import org.gudy.azureus2.core3.util.SimpleTimer;
import org.gudy.azureus2.core3.util.SystemTime;
import org.gudy.azureus2.core3.util.TimerEvent;
import org.gudy.azureus2.core3.util.TimerEventPerformer;
import org.gudy.azureus2.core3.util.TorrentUtils;
import org.gudy.azureus2.core3.util.UrlUtils;
import org.gudy.azureus2.plugins.*;
import org.gudy.azureus2.plugins.ddb.DistributedDatabase;
import org.gudy.azureus2.plugins.ddb.DistributedDatabaseContact;
import org.gudy.azureus2.plugins.ddb.DistributedDatabaseEvent;
import org.gudy.azureus2.plugins.ddb.DistributedDatabaseListener;
import org.gudy.azureus2.plugins.ddb.DistributedDatabaseProgressListener;
import org.gudy.azureus2.plugins.ddb.DistributedDatabaseTransferType;
import org.gudy.azureus2.plugins.ddb.DistributedDatabaseValue;
import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.download.DownloadException;
import org.gudy.azureus2.plugins.sharing.ShareException;
import org.gudy.azureus2.plugins.sharing.ShareResourceDir;
import org.gudy.azureus2.plugins.sharing.ShareResourceFile;
import org.gudy.azureus2.plugins.torrent.Torrent;
import org.gudy.azureus2.plugins.ui.UIInstance;
import org.gudy.azureus2.plugins.ui.UIManagerListener;
import org.gudy.azureus2.plugins.ui.config.BooleanParameter;
import org.gudy.azureus2.plugins.ui.config.ConfigSection;
import org.gudy.azureus2.plugins.ui.config.IntParameter;
import org.gudy.azureus2.plugins.ui.config.Parameter;
import org.gudy.azureus2.plugins.ui.menus.MenuItem;
import org.gudy.azureus2.plugins.ui.menus.MenuItemListener;
import org.gudy.azureus2.plugins.ui.model.BasicPluginConfigModel;
import org.gudy.azureus2.plugins.ui.tables.TableContextMenuItem;
import org.gudy.azureus2.plugins.ui.tables.TableManager;
import org.gudy.azureus2.plugins.ui.tables.TableRow;
import org.gudy.azureus2.plugins.utils.resourcedownloader.ResourceDownloader;
import org.gudy.azureus2.plugins.utils.resourcedownloader.ResourceDownloaderAdapter;
import org.gudy.azureus2.plugins.utils.resourcedownloader.ResourceDownloaderException;
import org.gudy.azureus2.plugins.utils.resourcedownloader.ResourceDownloaderFactory;
import org.gudy.azureus2.pluginsimpl.local.PluginCoreUtils;

import com.aelitis.azureus.core.proxy.AEProxyFactory;
import com.aelitis.azureus.core.proxy.AEProxyFactory.PluginProxy;
import com.aelitis.azureus.core.tag.Tag;
import com.aelitis.azureus.core.tag.TagManagerFactory;
import com.aelitis.azureus.core.tag.TagType;
import com.aelitis.azureus.core.util.FeatureAvailability;
import com.aelitis.net.magneturi.*;

/**
 * @author parg
 *
 */

public class 
MagnetPlugin
	implements Plugin
{	
	public static final int	FL_NONE					= 0x00000000;
	public static final int	FL_DISABLE_MD_LOOKUP	= 0x00000001;
	
	private static final String	SECONDARY_LOOKUP 			= "http://magnet.vuze.com/";
	private static final int	SECONDARY_LOOKUP_DELAY		= 20*1000;
	private static final int	SECONDARY_LOOKUP_MAX_TIME	= 2*60*1000;
	
	private static final int	MD_LOOKUP_DELAY_SECS_DEFAULT		= 20;


	private static final String	PLUGIN_NAME				= "Magnet URI Handler";
	private static final String PLUGIN_CONFIGSECTION_ID = "plugins.magnetplugin";

	private PluginInterface		plugin_interface;
		
	private List listeners = new CopyOnWriteArrayList();
	
	private boolean			first_download	= true;
	
	private static final int	PLUGIN_DOWNLOAD_TIMEOUT_SECS_DEFAULT 	= 10*60;	// needs to be fairly large as non-public downloads can take a while...
	
	private BooleanParameter secondary_lookup;
	private BooleanParameter md_lookup;
	private IntParameter	 md_lookup_delay;
	private IntParameter	 timeout_param;
	
	private Map<String,BooleanParameter> net_params = new HashMap<>();
	
	
	public static void
	load(
		PluginInterface		plugin_interface )
	{
		plugin_interface.getPluginProperties().setProperty( "plugin.version", 	"1.0" );
		plugin_interface.getPluginProperties().setProperty( "plugin.name", PLUGIN_NAME );
	}
	
	public void
	initialize(
		PluginInterface	_plugin_interface )
	{
		plugin_interface	= _plugin_interface;
		
		MagnetURIHandler uri_handler = MagnetURIHandler.getSingleton();
		
		BasicPluginConfigModel	config = 
			plugin_interface.getUIManager().createBasicPluginConfigModel( ConfigSection.SECTION_PLUGINS, 
					PLUGIN_CONFIGSECTION_ID);
		
		config.addInfoParameter2("MagnetPlugin.current.port", String.valueOf( uri_handler.getPort()));
		
		secondary_lookup 	= config.addBooleanParameter2( "MagnetPlugin.use.lookup.service", "MagnetPlugin.use.lookup.service", true );
		md_lookup 			= config.addBooleanParameter2( "MagnetPlugin.use.md.download", "MagnetPlugin.use.md.download", true );
		md_lookup_delay		= config.addIntParameter2( "MagnetPlugin.use.md.download.delay", "MagnetPlugin.use.md.download.delay", MD_LOOKUP_DELAY_SECS_DEFAULT );
		
		md_lookup.addEnabledOnSelection( md_lookup_delay );
			
		timeout_param		= config.addIntParameter2( "MagnetPlugin.timeout.secs", "MagnetPlugin.timeout.secs", PLUGIN_DOWNLOAD_TIMEOUT_SECS_DEFAULT );

		Parameter[] nps = new Parameter[ AENetworkClassifier.AT_NETWORKS.length ];
		
		for ( int i=0; i<nps.length; i++ ){

			String nn = AENetworkClassifier.AT_NETWORKS[i];

			String config_name = "Network Selection Default." + nn;
			
			String msg_text = "ConfigView.section.connection.networks." + nn;

			final BooleanParameter param 	= 
				config.addBooleanParameter2( 
						config_name, 
						msg_text, 
						COConfigurationManager.getBooleanParameter( config_name ));
			
			COConfigurationManager.addParameterListener(
					config_name,
					new org.gudy.azureus2.core3.config.ParameterListener()
					{	
						public void 
						parameterChanged(
							String name ) 
						{
							param.setDefaultValue( COConfigurationManager.getBooleanParameter( name ));
						}
					});
			
			nps[i] = param;
			
			net_params.put( nn, param );
		}
				
		config.createGroup( "label.default.nets", nps );
		
		MenuItemListener	listener = 
			new MenuItemListener()
			{
				public void
				selected(
					MenuItem		_menu,
					Object			_target )
				{
					TableRow[] rows = (TableRow[])_target;
					
					String cb_all_data = "";
					
					for ( TableRow row: rows ){
						Torrent torrent;
						String name;
						Object ds = row.getDataSource();
						
						Download download = null;
						
						if (ds instanceof ShareResourceFile) {
							try {
								torrent = ((ShareResourceFile) ds).getItem().getTorrent();
							} catch (ShareException e) {
								continue;
							}
							name = ((ShareResourceFile) ds).getName();
						}else if (ds instanceof ShareResourceDir) {
								try {
									torrent = ((ShareResourceDir) ds).getItem().getTorrent();
								} catch (ShareException e) {
									continue;
								}
								name = ((ShareResourceDir) ds).getName();
						} else if (ds instanceof Download) {
							download = (Download)ds;
							torrent = download.getTorrent();
							name = download.getName();
						} else {
							continue;
						}
					  
						
						String cb_data = download==null?UrlUtils.getMagnetURI( name, torrent ):UrlUtils.getMagnetURI( download);
						
						if ( download != null ){
							
							List<Tag> tags = TagManagerFactory.getTagManager().getTagsForTaggable( TagType.TT_DOWNLOAD_MANUAL, PluginCoreUtils.unwrap( download ));
							
							for ( Tag tag: tags ){
								
								if ( tag.isPublic()){
									
									cb_data += "&tag=" + UrlUtils.encode( tag.getTagName( true ));
								}
							}
						}
						
						// removed this as well - nothing wrong with allowing magnet copy
						// for private torrents - they still can't be tracked if you don't
						// have permission
						
						
						/*if ( torrent.isPrivate()){
							
							cb_data = getMessageText( "private_torrent" );
							
						}else if ( torrent.isDecentralised()){
						*/	
							// ok
							
							/* relaxed this as we allow such torrents to be downloaded via magnet links
							 * (as opposed to tracked in the DHT)
							 
						}else if ( torrent.isDecentralisedBackupEnabled()){
								
							TorrentAttribute ta_peer_sources 	= plugin_interface.getTorrentManager().getAttribute( TorrentAttribute.TA_PEER_SOURCES );
	
							String[]	sources = download.getListAttribute( ta_peer_sources );
			
							boolean	ok = false;
									
							for (int i=0;i<sources.length;i++){
										
								if ( sources[i].equalsIgnoreCase( "DHT")){
											
									ok	= true;
											
									break;
								}
							}
			
							if ( !ok ){
								
								cb_data = getMessageText( "decentral_disabled" );
							}
						}else{
							
							cb_data = getMessageText( "decentral_backup_disabled" );
							*/
						// }
						
						// System.out.println( "MagnetPlugin: export = " + url );
						
						cb_all_data += (cb_all_data.length()==0?"":"\n") + cb_data;
					}
					
					try{
						plugin_interface.getUIManager().copyToClipBoard( cb_all_data );
						
					}catch( Throwable  e ){
						
						e.printStackTrace();
					}
				}
			};
		
		final TableContextMenuItem menu1 = plugin_interface.getUIManager().getTableManager().addContextMenuItem(TableManager.TABLE_MYTORRENTS_INCOMPLETE, "MagnetPlugin.contextmenu.exporturi" );
		final TableContextMenuItem menu2 = plugin_interface.getUIManager().getTableManager().addContextMenuItem(TableManager.TABLE_MYTORRENTS_COMPLETE, 	"MagnetPlugin.contextmenu.exporturi" );
		final TableContextMenuItem menu3 = plugin_interface.getUIManager().getTableManager().addContextMenuItem(TableManager.TABLE_MYSHARES, 	"MagnetPlugin.contextmenu.exporturi" );
			
		menu1.addMultiListener( listener );
		menu2.addMultiListener( listener );
		menu3.addMultiListener( listener );

		uri_handler.addListener(
			new MagnetURIHandlerListener()
			{
				public byte[]
				badge()
				{
					InputStream is = getClass().getClassLoader().getResourceAsStream( "com/aelitis/azureus/plugins/magnet/Magnet.gif" );
					
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
						
						Debug.printStackTrace(e);
						
						return( null );
					}
				}
							
				public byte[]
				download(
					final MagnetURIHandlerProgressListener		muh_listener,
					final byte[]								hash,
					final String								args,
					final InetSocketAddress[]					sources,
					final long									timeout )
				
					throws MagnetURIHandlerException
				{
						// see if we've already got it!
					
					try{
						Download	dl = plugin_interface.getDownloadManager().getDownload( hash );
					
						if ( dl != null ){
							
							Torrent	torrent = dl.getTorrent();
							
							if ( torrent != null ){
								
								byte[] torrent_data = torrent.writeToBEncodedData();
								
								torrent_data = addTrackersAndWebSeedsEtc( torrent_data, args, new HashSet<>());
								
								return( torrent_data);
							}
						}
					}catch( Throwable e ){
					
						Debug.printStackTrace(e);
					}
					
					return( MagnetPlugin.this.download(
							muh_listener == null ? null : new MagnetPluginProgressListener()
							{
								public void
								reportSize(
									long	size )
								{
									muh_listener.reportSize( size );
								}
								
								public void
								reportActivity(
									String	str )
								{
									muh_listener.reportActivity( str );
								}
								
								public void
								reportCompleteness(
									int		percent )
								{
									muh_listener.reportCompleteness( percent );
								}
								
								public void
								reportContributor(
									InetSocketAddress	address )
								{
								}
								
								public boolean 
								cancelled() 
								{
									return( muh_listener.cancelled());
								}
								
								public boolean 
								verbose() 
								{
									return( muh_listener.verbose());
								}
							},
							hash,
							args,
							sources,
							timeout,
							0 ));
				}
				
				public boolean
				download(
					URL		url )
				
					throws MagnetURIHandlerException
				{
					try{
						
						plugin_interface.getDownloadManager().addDownload( url, false );
						
						return( true );
						
					}catch( DownloadException e ){
						
						throw( new MagnetURIHandlerException( "Operation failed", e ));
					}
				}
				
				public boolean
				set(
					String		name,
					Map		values )
				{
					List	l = listeners;

                    for (Object o : l) {

                        if (((MagnetPluginListener) o).set(name, values)) {

                            return (true);
                        }
                    }
					
					return( false );
				}
				
				public int
				get(
					String		name,
					Map			values )
				{
					List	l = listeners;

                    for (Object o : l) {

                        int res = ((MagnetPluginListener) o).get(name, values);

                        if (res != Integer.MIN_VALUE) {

                            return (res);
                        }
                    }
					
					return( Integer.MIN_VALUE );
				}
			});
		
		plugin_interface.addListener(
			new PluginListener()
			{
				public void
				initializationComplete()
				{
						// make sure DDB is initialised as we need it to register its
						// transfer types
					
					AEThread2 t = 
						new AEThread2( "MagnetPlugin:init", true )
						{
							public void
							run()
							{
								plugin_interface.getDistributedDatabase();
							}
						};
										
					t.start();
				}
				
				public void
				closedownInitiated(){}
				
				public void
				closedownComplete(){}			
			});
		
		plugin_interface.getUIManager().addUIListener(
				new UIManagerListener()
				{
					public void
					UIAttached(
						UIInstance		instance )
					{
						if ( instance.getUIType() == UIInstance.UIT_SWT ){
							
							try{
								Class.forName( "com.aelitis.azureus.plugins.magnet.swt.MagnetPluginUISWT" ).getConstructor(
									new Class[]{ UIInstance.class, TableContextMenuItem[].class }).newInstance(
                                        instance, new TableContextMenuItem[]{ menu1, menu2, menu3 });
								
							}catch( Throwable e ){
								
								e.printStackTrace();
							}
						}
					}
					
					public void
					UIDetached(
						UIInstance		instance )
					{
						
					}
				});
		
		final List<Download>	to_delete = new ArrayList<>();
		
		Download[] downloads = plugin_interface.getDownloadManager().getDownloads();
		
		for ( Download download: downloads ){
			
			if ( download.getFlag( Download.FLAG_METADATA_DOWNLOAD )){
				
				to_delete.add( download );
			}
		}
		
		if ( to_delete.size() > 0 ){
			
			AEThread2 t = 
				new AEThread2( "MagnetPlugin:delmds", true )
				{
					public void
					run()
					{
						for ( Download download: to_delete ){
							
							try{
								download.stop();
								
							}catch( Throwable e ){
							}
							
							try{
								download.remove( true, true );
								
							}catch( Throwable e ){
								
								Debug.out( e );
							}
						}
					}
				};
								
			t.start();
		}
	}
	
	public boolean
	isNetworkEnabled(
		String		net )
	{
		return( net_params.get( net ).getValue());
	}
	
	public URL
	getMagnetURL(
		Download		d )
	{
		Torrent	torrent = d.getTorrent();
		
		if ( torrent == null ){
			
			return( null );
		}
		
		return( getMagnetURL( torrent.getHash()));
	}
	
	public URL
	getMagnetURL(
		byte[]		hash )
	{
		try{
			return( new URL( "magnet:?xt=urn:btih:" + Base32.encode(hash)));
		
		}catch( Throwable e ){
		
			Debug.printStackTrace(e);
		
			return( null );
		}
	}
	
	public byte[]
	badge()
	{
		return( null );
	}
	
	public byte[]
	download(
		MagnetPluginProgressListener		listener,
		byte[]								hash,
		String								args,
		InetSocketAddress[]					sources,
		long								timeout,
		int									flags )
	
		throws MagnetURIHandlerException
	{
		DownloadResult result = downloadSupport( listener, hash, args, sources, timeout, flags );
		
		if ( result == null ){
		
			return( null );
		}
		
		return( addTrackersAndWebSeedsEtc( result, args  ));
	}
	
	private byte[]
	addTrackersAndWebSeedsEtc(
		DownloadResult		result,
		String				args )
	{
		byte[]		torrent_data 	= result.getTorrentData();
		Set<String>	networks		= result.getNetworks();
		
		return( addTrackersAndWebSeedsEtc( torrent_data, args, networks ));
	}
	
	private byte[]
	addTrackersAndWebSeedsEtc(
		byte[]			torrent_data,
		String			args,
		Set<String>		networks )
	{
		List<String>	new_web_seeds 	= new ArrayList<>();
		List<String>	new_trackers 	= new ArrayList<>();

		Set<String>	tags			= new HashSet<>();
		
		if ( args != null ){
			
			String[] bits = args.split( "&" );
			

			for ( String bit: bits ){
				
				String[] x = bit.split( "=" );
				
				if ( x.length == 2 ){
					
					String	lhs = x[0].toLowerCase();

                    switch (lhs) {
                        case "ws":

                            try {
                                new_web_seeds.add(new URL(UrlUtils.decode(x[1])).toExternalForm());

                            } catch (Throwable e) {
                            }
                            break;
                        case "tr":

                            try {
                                new_trackers.add(new URL(UrlUtils.decode(x[1])).toExternalForm());

                            } catch (Throwable e) {
                            }
                            break;
                        case "tag":

                            tags.add(UrlUtils.decode(x[1]));
                            break;
                    }
				}
			}
		}
			
		if ( new_web_seeds.size() > 0 || new_trackers.size() > 0 || networks.size() > 0 ){
			
			try{
				TOTorrent torrent = TOTorrentFactory.deserialiseFromBEncodedByteArray( torrent_data );

				boolean	update_torrent = false;
				
				if ( new_web_seeds.size() > 0 ){
					
					Object obj = torrent.getAdditionalProperty( "url-list" );
					
					List<String> existing = new ArrayList<>();
					
					if ( obj instanceof byte[] ){
		                
						try{
							new_web_seeds.remove( new URL( new String((byte[])obj, StandardCharsets.UTF_8)).toExternalForm());
							
						}catch( Throwable e ){							
						}
					}else if ( obj instanceof List ){
						
						List<byte[]> l = (List<byte[]>)obj;
						
						for ( byte[] b: l ){
							
							try{
								existing.add( new URL( new String(b, StandardCharsets.UTF_8)).toExternalForm());
								
							}catch( Throwable e ){							
							}
						}
					}
					
					boolean update_ws = false;
					
					for ( String e: new_web_seeds ){
						
						if ( !existing.contains( e )){
							
							existing.add( e );
							
							update_ws = true;
						}
					}
					
					if ( update_ws ){
					
						List<byte[]>	l = new ArrayList<>();
						
						for ( String s: existing ){
							
							l.add( s.getBytes(StandardCharsets.UTF_8));
						}
						
						torrent.setAdditionalProperty( "url-list", l );
						
						update_torrent = true;
					}
				}
				
				if ( new_trackers.size() > 0 ){
											
					URL announce_url = torrent.getAnnounceURL();
												
					new_trackers.remove( announce_url.toExternalForm());
					
					TOTorrentAnnounceURLGroup group = torrent.getAnnounceURLGroup();
					
					TOTorrentAnnounceURLSet[] sets = group.getAnnounceURLSets();
					
					for ( TOTorrentAnnounceURLSet set: sets ){
						
						URL[] set_urls = set.getAnnounceURLs();
						
						for( URL set_url: set_urls ){
																																	
							new_trackers.remove( set_url.toExternalForm());
						}
					}
					
					if ( new_trackers.size() > 0 ){
						
						TOTorrentAnnounceURLSet[]	new_sets = new TOTorrentAnnounceURLSet[ sets.length + new_trackers.size()];

						System.arraycopy(sets, 0, new_sets, 0, sets.length);
						
						for ( int i=0;i<new_trackers.size();i++){
							
							TOTorrentAnnounceURLSet new_set = group.createAnnounceURLSet( new URL[]{ new URL( new_trackers.get(i))});
							
							new_sets[i+sets.length] = new_set;
						}
						
						group.setAnnounceURLSets( new_sets );
						
						update_torrent = true;
					}
				}
				
				if ( networks.size() > 0 ){
						
					TorrentUtils.setNetworkCache( torrent, new ArrayList<>(networks));

					update_torrent = true;
				}
				
				if ( tags.size() > 0 ){
					
					TorrentUtils.setTagCache( torrent, new ArrayList<>(tags));

					update_torrent = true;
				}
				
				if ( update_torrent ){
					
					torrent_data = BEncoder.encode( torrent.serialiseToMap());
				}
			}catch( Throwable e ){
			}
		}
		
		return( torrent_data );
	}
	
	private static final ByteArrayHashMap<DownloadActivity>	download_activities = new ByteArrayHashMap<>();
	
	private static class
	DownloadActivity
	{
		private volatile DownloadResult				result;
		private volatile MagnetURIHandlerException	error;
		
		private AESemaphore		sem = new AESemaphore( "MP:DA" );
		
		public void
		setResult(
			DownloadResult	_result )
		{
			result	= _result;
			
			sem.releaseForever();
		}
		
		public void
		setResult(
			Throwable _error  )
		{
			if ( _error instanceof MagnetURIHandlerException ){
				
				error = (MagnetURIHandlerException)_error;
				
			}else{
				
				error = new MagnetURIHandlerException( "Download failed", _error );
			}
			
			sem.releaseForever();
		}
		
		public DownloadResult
		getResult()
		
			throws MagnetURIHandlerException
		{
			sem.reserve();
			
			if ( error != null ){
				
				throw( error );
			}
			
			return( result );
		}
	}
	
	private DownloadResult
 	downloadSupport(
 		MagnetPluginProgressListener	listener,
 		byte[]							hash,
 		String							args,
 		InetSocketAddress[]				sources,
 		long							timeout,
 		int								flags )
 	
 		throws MagnetURIHandlerException
 	{
		DownloadActivity	activity;
		boolean				new_activity = false;
		
 		synchronized( download_activities ){
 			
 				// single-thread per hash to avoid madness ensuing if we get multiple concurrent hits
 			
 			activity = download_activities.get( hash );
 			
 			if ( activity == null ){
 				
 				activity = new DownloadActivity();
 				
 				download_activities.put( hash, activity );
 				
 				new_activity = true;
 			}
 		}
 		 		
 		if ( new_activity ){
 		
	 		try{
	 			
	 			activity.setResult( _downloadSupport( listener, hash, args, sources, timeout, flags ));
	 			
	 		}catch( Throwable e ){
	 			
	 			activity.setResult( e );
	 			
	 		}finally{
	 			
	 			synchronized( download_activities ){
	 				
	 				download_activities.remove( hash );
	 			}
	 		}
 		}
 			
 		return( activity.getResult());

 	}
	
	private DownloadResult
	_downloadSupport(
		final MagnetPluginProgressListener		listener,
		final byte[]							hash,
		final String							args,
		final InetSocketAddress[]				sources,
		long									_timeout,
		int										flags )
	
		throws MagnetURIHandlerException
	{
		final long	timeout;
		
		if ( _timeout < 0 ){
			
			// use plugin defined value
			
			int secs = timeout_param.getValue();
			
			if ( secs <= 0 ){
				
				timeout = Integer.MAX_VALUE;
				
			}else{
				
				timeout = secs*1000L;
			}
			
		}else{
			
			timeout = _timeout;
		}
		
		boolean	md_enabled;
		
		final boolean	dummy_hash = Arrays.equals( hash, new byte[20] );
		
		if ((flags & FL_DISABLE_MD_LOOKUP) != 0 ){
			
			md_enabled = false;
			
		}else{
			
			md_enabled = md_lookup.getValue() && FeatureAvailability.isMagnetMDEnabled();
		}

		final byte[][]		result_holder 	= { null };
		final Throwable[] 	result_error 	= { null };

		TimerEvent							md_delay_event = null;
		final MagnetPluginMDDownloader[]	md_downloader = { null };
		
		boolean	net_pub_default = isNetworkEnabled( AENetworkClassifier.AT_PUBLIC );
		
		final Set<String>	networks_enabled;
		
		final Set<String>	additional_networks = new HashSet<>();

		if ( args != null ){
			
			String[] bits = args.split( "&" );
			
			List<URL>	fl_args 	= new ArrayList<>();
			
			Set<String>	tr_networks 		= new HashSet<>();
			Set<String>	explicit_networks 	= new HashSet<>();

			for ( String bit: bits ){
				
				if ( bit.startsWith( "maggot_sha1" )){

					tr_networks.clear();
					
					explicit_networks.clear();
					
					fl_args.clear();
					
					explicit_networks.add( AENetworkClassifier.AT_I2P  );
					
					break;
				}
					
				String[] x = bit.split( "=" );
				
				if ( x.length == 2 ){
					
					String	lhs = x[0].toLowerCase();

                    switch (lhs) {
                        case "fl":
                        case "xs":
                        case "as":

                            try {
                                URL url = new URL(UrlUtils.decode(x[1]));

                                fl_args.add(url);

                                tr_networks.add(AENetworkClassifier.categoriseAddress(url.getHost()));

                            } catch (Throwable e) {
                            }
                            break;
                        case "tr":

                            try {
                                tr_networks.add(AENetworkClassifier.categoriseAddress(new URL(UrlUtils.decode(x[1])).getHost()));

                            } catch (Throwable e) {
                            }
                            break;
                        case "net":

                            String network = AENetworkClassifier.internalise(x[1]);

                            if (network != null) {

                                explicit_networks.add(network);
                            }
                            break;
                    }
				}
			}
			
			if ( explicit_networks.size() > 0 ){
				
				networks_enabled = explicit_networks;
				
			}else{
				
				networks_enabled = tr_networks;
				
				if ( net_pub_default ){
					
					if ( networks_enabled.size() == 0 ){
						
						networks_enabled.add( AENetworkClassifier.AT_PUBLIC );
					}
				}else{
					
					networks_enabled.remove( AENetworkClassifier.AT_PUBLIC );
				}
			}
			
			if ( fl_args.size() > 0 ){
				
				final AESemaphore fl_sem = new AESemaphore( "fl_sem" );
				
				int	fl_run = 0;
				
				for ( int i=0;i<fl_args.size() && i < 3; i++ ){
					
					final URL fl_url = fl_args.get( i );
					
					String url_net = AENetworkClassifier.categoriseAddress( fl_url.getHost());
					
					if ( networks_enabled.contains( url_net )){
						
						new AEThread2( "Magnet:fldl", true )
						{
							public void 
							run() 
							{
								try{
									TOTorrent torrent = TorrentUtils.download( fl_url, timeout );
									
									if ( torrent != null ){
										
										if ( dummy_hash || Arrays.equals( torrent.getHash(), hash )){
											
											synchronized( result_holder ){
												
												result_holder[0] = BEncoder.encode( torrent.serialiseToMap());
											}
										}
									}
								}catch( Throwable e ){
									
									Debug.out( e );
									
								}finally{
									
									fl_sem.release();
								}
							}
						}.start();
						
						fl_run++;
					}
				}
			
				if ( dummy_hash ){
						
					long	remaining = timeout;
					
					for ( int i=0; i<fl_run && remaining>0; i++ ){
					
						long	start = SystemTime.getMonotonousTime();
						
						if ( !fl_sem.reserve( remaining )){
							
							break;
						}
					
						remaining -= (SystemTime.getMonotonousTime() - start );
						
						synchronized( result_holder ){
						
							if ( result_holder[0] != null ){
							
								return( new DownloadResult( result_holder[0], networks_enabled, additional_networks ));
							}
						}
					}	
				}
			}
		}else{
				
			networks_enabled = new HashSet<>();
			
			if ( net_pub_default ){
									
				networks_enabled.add( AENetworkClassifier.AT_PUBLIC );
			}
		}
		
		if ( dummy_hash ){
		
			return( null );
		}
		
			// networks-enabled has either the networks inferrable from the magnet set up
			// or, if none, then public (but only if public is enabled by default )
				
		if ( md_enabled ){
			
			int	delay_millis = md_lookup_delay.getValue()*1000;
			
			md_delay_event = 
				SimpleTimer.addEvent(
					"MagnetPlugin:md_delay",
					delay_millis<=0?0:(SystemTime.getCurrentTime() + delay_millis ),
					new TimerEventPerformer()
					{
						public void 
						perform(
							TimerEvent event ) 
						{
							MagnetPluginMDDownloader mdd;
							
							synchronized( md_downloader ){
								
								if ( event.isCancelled()){
									
									return;
								}
								
								md_downloader[0] = mdd = new MagnetPluginMDDownloader( MagnetPlugin.this, plugin_interface, hash, networks_enabled, sources, args );
							}
							
							if ( listener != null ){
								listener.reportActivity( getMessageText( "report.md.starts" ));
							}
							
							mdd.start(
								new MagnetPluginMDDownloader.DownloadListener()
								{
									public void
									reportProgress(
										int		downloaded,
										int		total_size )
									{
										if ( listener != null ){
  										listener.reportActivity( getMessageText( "report.md.progress", downloaded + "/" + total_size));
  										
  										listener.reportCompleteness( 100*downloaded/total_size );
										}
									}
									
									public void
									complete(
										TOTorrent		torrent,
										Set<String>		peer_networks )
									{
										if ( listener != null ){
											listener.reportActivity( getMessageText( "report.md.done" ));
										}
										
										synchronized( result_holder ){
										
											additional_networks.addAll( peer_networks );
											
											try{
												result_holder[0] = BEncoder.encode( torrent.serialiseToMap());
												
											}catch( Throwable e ){
												
												Debug.out( e );
											}
										}
									}
									
									public void
									failed(
										Throwable e )
									{
										if ( listener != null ){
											listener.reportActivity( getMessageText( "report.error", Debug.getNestedExceptionMessage(e)));
										}
										
										synchronized( result_holder ){
											
											result_error[0] = e;
										}
									}
								});
						}
					});
		}
		

		try{
			try{
				long	remaining	= timeout;
								
				boolean	sl_enabled				= secondary_lookup.getValue() && FeatureAvailability.isMagnetSLEnabled();
				boolean	sl_failed				= false;
				long secondary_lookup_time 	= -1;

				final Object[] secondary_result = { null };

					// public DHT lookup
				
				if ( networks_enabled.contains( AENetworkClassifier.AT_PUBLIC )){
					
					boolean	is_first_download = first_download;
					
					if ( is_first_download ){
					
						if ( listener != null ){
							listener.reportActivity( getMessageText( "report.waiting_ddb" ));
						}
						
						first_download = false;
					}
					
					final DistributedDatabase db = plugin_interface.getDistributedDatabase();
				
					if ( db.isAvailable()){
						
						final List			potential_contacts 		= new ArrayList();
						final AESemaphore	potential_contacts_sem 	= new AESemaphore( "MagnetPlugin:liveones" );
						final AEMonitor		potential_contacts_mon	= new AEMonitor( "MagnetPlugin:liveones" );
						
						final int[]			outstanding		= {0};
						final boolean[]		lookup_complete	= {false};
						
						if ( listener != null ){
							listener.reportActivity(  getMessageText( "report.searching" ));
						}
						
						DistributedDatabaseListener	ddb_listener = 
							new DistributedDatabaseListener()
							{
								private final Set found_set = new HashSet();
								
								public void
								event(
									DistributedDatabaseEvent 		event )
								{
									int	type = event.getType();
				
									if ( type == DistributedDatabaseEvent.ET_OPERATION_STARTS ){
			
											// give live results a chance before kicking in explicit ones
										
										if ( sources.length > 0 ){
											
											new DelayedEvent(
												"MP:sourceAdd",
												10*1000,
												new AERunnable()
												{
													public void
													runSupport()
													{
														addExplicitSources();
													}
												});
										}
										
									}else if ( type == DistributedDatabaseEvent.ET_VALUE_READ ){
																
										contactFound( event.getValue().getContact());
						
									}else if (	type == DistributedDatabaseEvent.ET_OPERATION_COMPLETE ||
												type == DistributedDatabaseEvent.ET_OPERATION_TIMEOUT ){
											
										if ( listener != null ){
											listener.reportActivity( getMessageText( "report.found", String.valueOf( found_set.size())));
										}
										
											// now inject any explicit sources
			
										addExplicitSources();
										
										try{
											potential_contacts_mon.enter();													
			
											lookup_complete[0] = true;
											
										}finally{
											
											potential_contacts_mon.exit();
										}
										
										potential_contacts_sem.release();
									}
								}
								
								protected void
								addExplicitSources()
								{
                                    for (InetSocketAddress source : sources) {

                                        try {
                                            contactFound(db.importContact(source));

                                        } catch (Throwable e) {

                                            Debug.printStackTrace(e);
                                        }
                                    }
								}
								
								public void
								contactFound(
									final DistributedDatabaseContact	contact )
								{
									String	key = contact.getAddress().toString();
									
									synchronized( found_set ){
										
										if ( found_set.contains( key )){
											
											return;
										}
										
										found_set.add( key );
									}
									
									if ( listener != null && listener.verbose()){
									
										listener.reportActivity( getMessageText( "report.found", contact.getName()));
									}
									
									try{
										potential_contacts_mon.enter();													
			
										outstanding[0]++;
										
									}finally{
										
										potential_contacts_mon.exit();
									}
									
									contact.isAlive(
										20*1000,
										new DistributedDatabaseListener()
										{
											public void 
											event(
												DistributedDatabaseEvent event) 
											{
												try{
													boolean	alive = event.getType() == DistributedDatabaseEvent.ET_OPERATION_COMPLETE;
														
													if ( listener != null && listener.verbose()){
													
														listener.reportActivity( 
															getMessageText( alive?"report.alive":"report.dead",	contact.getName()));
													}
													
													try{
														potential_contacts_mon.enter();
														
														Object[]	entry = new Object[]{alive, contact};
														
														boolean	added = false;
														
														if ( alive ){
															
																// try and place before first dead entry 
													
															for (int i=0;i<potential_contacts.size();i++){
																
																if (!(Boolean) ((Object[]) potential_contacts.get(i))[0]){
																	
																	potential_contacts.add(i, entry );
																	
																	added = true;
																	
																	break;
																}
															}
														}
														
														if ( !added ){
															
															potential_contacts.add( entry );	// dead at end
														}
															
													}finally{
															
														potential_contacts_mon.exit();
													}
												}finally{
													
													try{
														potential_contacts_mon.enter();													
			
														outstanding[0]--;
														
													}finally{
														
														potential_contacts_mon.exit();
													}
													
													potential_contacts_sem.release();
												}
											}
										});
								}
							};
							
						db.read(
							ddb_listener,
							db.createKey( hash, "Torrent download lookup for '" + ByteFormatter.encodeString( hash ) + "'" ),
							timeout,
							DistributedDatabase.OP_EXHAUSTIVE_READ | DistributedDatabase.OP_PRIORITY_HIGH );
											
						long 	overall_start 			= SystemTime.getMonotonousTime();					
						long 	last_found 				= -1;
											
						AsyncDispatcher	dispatcher = new AsyncDispatcher();
						
						while( remaining > 0 ){
							
							try{
								potential_contacts_mon.enter();
			
								if ( 	lookup_complete[0] && 
										potential_contacts.size() == 0 &&
										outstanding[0] == 0 ){
									
									break;
								}
							}finally{
								
								potential_contacts_mon.exit();
							}
											
							
							while( remaining > 0 ){
							
								if ( listener != null && listener.cancelled()){
									
									return( null );
								}
								
								synchronized( result_holder ){
									
									if ( result_holder[0] != null ){
										
										return( new DownloadResult( result_holder[0], networks_enabled, additional_networks ));
									}
								}
								
								long wait_start = SystemTime.getMonotonousTime();
			
								boolean got_sem = potential_contacts_sem.reserve( 1000 );
					
								long now = SystemTime.getMonotonousTime();
								
								remaining -= ( now - wait_start );
							
								if ( got_sem ){
								
									last_found = now;
									
									break;
									
								}else{
									
									if ( sl_enabled ){
										
										if ( secondary_lookup_time == -1 ){
										
											long	base_time;
											
											if ( last_found == -1 || now - overall_start > 60*1000 ){
												
												base_time = overall_start;
												
											}else{
												
												base_time = last_found;
											}
											
											long	time_so_far = now - base_time;
											
											if ( time_so_far > SECONDARY_LOOKUP_DELAY ){
												
												secondary_lookup_time = SystemTime.getMonotonousTime();
												
												doSecondaryLookup( listener, secondary_result, hash, networks_enabled, args );
											}
										}else{
											
											try{
												byte[] torrent = getSecondaryLookupResult( secondary_result );
												
												if ( torrent != null ){
													
													return( new DownloadResult( torrent, networks_enabled, additional_networks ));
												}
											}catch( ResourceDownloaderException e ){
												
												sl_failed = true;
												
												// ignore, we just continue processing
											}
										}
									}
								}
							}
							
							if ( sl_enabled ){
								
									// check before we try another DHT contact
								
								try{
									byte[] torrent = getSecondaryLookupResult( secondary_result );
									
									if ( torrent != null ){
										
										return( new DownloadResult( torrent, networks_enabled, additional_networks ));
									}
								}catch( ResourceDownloaderException e ){
									
									sl_failed = true;
								}
							}
							
							final DistributedDatabaseContact	contact;
							final boolean						live_contact;
							
							try{
								potential_contacts_mon.enter();
								
								// System.out.println( "rem=" + remaining + ",pot=" + potential_contacts.size() + ",out=" + outstanding[0] );
								
								if ( potential_contacts.size() == 0 ){
									
									if ( outstanding[0] == 0 ){
									
										break;
										
									}else{
										
										continue;
									}
								}else{
								
									Object[]	entry = (Object[])potential_contacts.remove(0);
									
									live_contact 	= (Boolean) entry[0];
									contact 		= (DistributedDatabaseContact)entry[1];
								}
								
							}finally{
								
								potential_contacts_mon.exit();
							}
								
							// System.out.println( "magnetDownload: " + contact.getName() + ", live = " + live_contact );
							
							final AESemaphore	contact_sem 	= new AESemaphore( "MD:contact" );
							
							dispatcher.dispatch(
								new AERunnable()
								{
									public void
									runSupport()
									{
										try{
											if ( !live_contact ){
												
												if ( listener != null ){
													listener.reportActivity( getMessageText( "report.tunnel", contact.getName()));
												}
							
												contact.openTunnel();
											}
											
											try{
												if ( listener != null ){
													listener.reportActivity( getMessageText( "report.downloading", contact.getName()));
												}
												
												DistributedDatabaseValue	value = 
													contact.read( 
															listener == null ? null : new DistributedDatabaseProgressListener()
															{
																public void
																reportSize(
																	long	size )
																{
																	listener.reportSize( size );
																}
																public void
																reportActivity(
																	String	str )
																{
																	listener.reportActivity( str );
																}
																
																public void
																reportCompleteness(
																	int		percent )
																{
																	listener.reportCompleteness( percent );
																}
															},
															db.getStandardTransferType( DistributedDatabaseTransferType.ST_TORRENT ),
															db.createKey ( hash , "Torrent download content for '" + ByteFormatter.encodeString( hash ) + "'"),
															timeout );
																	
												if ( value != null ){
													
														// let's verify the torrent
													
													byte[]	data = (byte[])value.getValue(byte[].class);
							
													try{
														TOTorrent torrent = TOTorrentFactory.deserialiseFromBEncodedByteArray( data );
														
														if ( Arrays.equals( hash, torrent.getHash())){
														
															if ( listener != null ){
																listener.reportContributor( contact.getAddress());
															}
													
															synchronized( result_holder ){
																
																result_holder[0] = data;
															}												
														}else{
															
															if ( listener != null ){
																listener.reportActivity( getMessageText( "report.error", "torrent invalid (hash mismatch)" ));
															}
														}
													}catch( Throwable e ){
														
														if ( listener != null ){
															listener.reportActivity( getMessageText( "report.error", "torrent invalid (decode failed)" ));
														}
													}
												}
											}catch( Throwable e ){
												
												if ( listener != null ){
													listener.reportActivity( getMessageText( "report.error", Debug.getNestedExceptionMessage(e)));
												}
												
												Debug.printStackTrace(e);
											}
										}finally{
											
											contact_sem.release();
										}
									}
								});
							
							while( true ){
								
								if ( listener != null && listener.cancelled()){
									
									return( null );
								}
								
								boolean got_sem = contact_sem.reserve( 500 );
																							
								synchronized( result_holder ){
									
									if ( result_holder[0] != null ){
										
										return( new DownloadResult( result_holder[0], networks_enabled, additional_networks ));
									}
								}
								
								if ( got_sem ){
									
									break;
								}
							}
						}
					}else{
						
						if ( is_first_download ){
						
							if ( listener != null ){
								listener.reportActivity( getMessageText( "report.ddb_disabled" ));
							}
						}
					}
				}
			
					// DDB lookup process is complete or skipped
					// If secondary lookup is active/doable then hang around until it completes
				
				if ( sl_enabled && !sl_failed ){
					
					if ( secondary_lookup_time == -1 ){
						
						secondary_lookup_time = SystemTime.getMonotonousTime();
						
						doSecondaryLookup(listener, secondary_result, hash, networks_enabled, args );
					}
					
					while( SystemTime.getMonotonousTime() - secondary_lookup_time < SECONDARY_LOOKUP_MAX_TIME ){
						
						if ( listener != null && listener.cancelled()){
							
							return( null );
						}
						
						try{
							byte[] torrent = getSecondaryLookupResult( secondary_result );
							
							if ( torrent != null ){
								
								return( new DownloadResult( torrent, networks_enabled, additional_networks ));
							}
							
							synchronized( result_holder ){
								
								if ( result_holder[0] != null ){
									
									return( new DownloadResult( result_holder[0], networks_enabled, additional_networks ));
								}
							}
														
							Thread.sleep( 500 );
							
						}catch( ResourceDownloaderException e ){
							
								// get here when secondary lookup completes with fail
							
							sl_failed = true;
							
							break;
						}
					}
				}
				
					// lastly hang around until metadata download completes
				
				if ( md_enabled ){
				
					while( remaining > 0 ){

						if ( listener != null && listener.cancelled()){
							
							return( null );
						}
						
						Thread.sleep( 500 );
						
						remaining -= 500;
						
						if ( !sl_failed ){
							
							try{
								byte[] torrent = getSecondaryLookupResult( secondary_result );
							
								if ( torrent != null ){
								
									return( new DownloadResult( torrent, networks_enabled, additional_networks ));
								}
							}catch( ResourceDownloaderException e ){
								
									// get here when secondary lookup completes with fail
								
								sl_failed = true;
							}
						}
						
						synchronized( result_holder ){
							
							if ( result_holder[0] != null ){
								
								return( new DownloadResult( result_holder[0], networks_enabled, additional_networks ));
							}
							
							if ( result_error[0] != null ){
								
								break;
							}
						}
					}
				}
				
				return( null );		// nothing found
				
			}catch( Throwable e ){
				
				Debug.printStackTrace(e);
				
				if ( listener != null ){
					listener.reportActivity( getMessageText( "report.error", Debug.getNestedExceptionMessage(e)));
				}
	
				throw( new MagnetURIHandlerException( "MagnetURIHandler failed", e ));
			}
		}finally{
			
			synchronized( md_downloader ){

				if ( md_delay_event != null ){
					
					md_delay_event.cancel();
					
					if ( md_downloader[0] != null ){
						
						 md_downloader[0].cancel();
					}
				}
			}
		}
	}
	
	protected void
	doSecondaryLookup(
		final MagnetPluginProgressListener		listener,
		final Object[]							result,
		byte[]									hash,
		Set<String>								networks_enabled,
		String									args )	
	{
		if ( listener != null ){
			listener.reportActivity( getMessageText( "report.secondarylookup", null ));
		}
		
		PluginProxy	plugin_proxy = null;

		try{
			URL original_sl_url = new URL( SECONDARY_LOOKUP + "magnetLookup?hash=" + Base32.encode( hash ) + (args.length()==0?"":("&args=" + UrlUtils.encode( args ))));

			URL 	sl_url	= original_sl_url;
			Proxy	proxy	= null;
												
			if ( !networks_enabled.contains( AENetworkClassifier.AT_PUBLIC )){
			
				plugin_proxy = AEProxyFactory.getPluginProxy( "secondary magnet lookup", sl_url );
				
				if ( plugin_proxy == null ){
					
					throw( new NoRouteToHostException( "plugin proxy unavailable" ));
					
				}else{
					
					proxy 	= plugin_proxy.getProxy();
					sl_url	= plugin_proxy.getURL();							
				}
			}
			
			ResourceDownloaderFactory rdf = plugin_interface.getUtilities().getResourceDownloaderFactory();
					
			ResourceDownloader rd;
			
			if ( proxy == null ){
				
				rd = rdf.create( sl_url );
				
			}else{
				
				rd = rdf.create( sl_url, proxy );
				
				rd.setProperty( "URL_HOST", original_sl_url.getHost());
			}
			
			final PluginProxy f_pp = plugin_proxy;
			
			rd.addListener(
				new ResourceDownloaderAdapter()
				{
					public boolean
					completed(
						ResourceDownloader	downloader,
						InputStream			data )
					{
						try{
							if ( listener != null ){
								listener.reportActivity( getMessageText( "report.secondarylookup.ok", null ));
							}
	
							synchronized( result ){
							
								result[0] = data;
							}
							
							return( true );
							
						}finally{
							
							complete();
						}
					}
					
					public void
					failed(
						ResourceDownloader			downloader,
						ResourceDownloaderException e )
					{
						try{
							synchronized( result ){
								
								result[0] = e;
							}
							
							if ( listener != null ){
								listener.reportActivity( getMessageText( "report.secondarylookup.fail" ));
							}
							
						}finally{
							
							complete();
						}
					}
					
					private void
					complete()
					{
						if ( f_pp != null ){
							
							f_pp.setOK( true );		// outcome doesn't really indicate whether the result was wholesome
						}
					}
				});
			
			rd.asyncDownload();
				
		}catch( Throwable e ){
			
			if ( plugin_proxy != null ){
				
				plugin_proxy.setOK( true );		// tidy up, no indication of proxy badness here so say its ok
			}
			
			if ( listener != null ){
				listener.reportActivity( getMessageText( "report.secondarylookup.fail", Debug.getNestedExceptionMessage( e ) ));
			}
		}
	}
	
	protected byte[]
	getSecondaryLookupResult(
		final Object[]	result )
	
		throws ResourceDownloaderException
	{
		if ( result == null ){
			
			return( null );
		}
		
		Object x;
		
		synchronized( result ){
			
			x = result[0];
			
			result[0] = null;
		}
			
		if ( x instanceof InputStream ){
			
			InputStream is = (InputStream)x;
				
			try{
				TOTorrent t = TOTorrentFactory.deserialiseFromBEncodedInputStream( is );
				
				TorrentUtils.setPeerCacheValid( t );
		
				return( BEncoder.encode( t.serialiseToMap()));
				
			}catch( Throwable e ){							
			}
		}else if ( x instanceof ResourceDownloaderException ){
			
			throw((ResourceDownloaderException)x);
		}
		
		return( null );
	}
	
	protected String
	getMessageText(
		String	resource,
		String...	params )
	{
		return( plugin_interface.getUtilities().getLocaleUtilities().getLocalisedMessageText( 
				"MagnetPlugin." + resource, params ));
	}
	
	public void
	addListener(
		MagnetPluginListener		listener )
	{
		listeners.add( listener );
	}
	
	public void
	removeListener(
		MagnetPluginListener		listener )
	{
		listeners.remove( listener );
	}
	
	private static class
	DownloadResult
	{
		private byte[]		data;
		private Set<String>	networks;
		
		private
		DownloadResult(
			byte[]			torrent_data,
			Set<String>		networks_enabled,
			Set<String>		additional_networks )
		{
			data		= torrent_data;
			
			networks = new HashSet<>();
			
			networks.addAll( networks_enabled );
			networks.addAll( additional_networks );
		}
		
		private byte[]
		getTorrentData()
		{
			return( data );
		}
		
		private Set<String>
		getNetworks()
		{
			return( networks );
		}
	}
}
