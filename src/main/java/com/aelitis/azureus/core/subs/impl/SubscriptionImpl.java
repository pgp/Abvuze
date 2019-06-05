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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import org.gudy.azureus2.core3.internat.MessageText;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentCreator;
import org.gudy.azureus2.core3.torrent.TOTorrentFactory;
import org.gudy.azureus2.core3.util.AENetworkClassifier;
import org.gudy.azureus2.core3.util.AEThread2;
import org.gudy.azureus2.core3.util.BDecoder;
import org.gudy.azureus2.core3.util.BEncoder;
import org.gudy.azureus2.core3.util.Base32;
import org.gudy.azureus2.core3.util.ByteFormatter;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.FileUtil;
import org.gudy.azureus2.core3.util.HashWrapper;
import org.gudy.azureus2.core3.util.IndentWriter;
import org.gudy.azureus2.core3.util.LightHashMap;
import org.gudy.azureus2.core3.util.RandomUtils;
import org.gudy.azureus2.core3.util.SystemTime;
import org.gudy.azureus2.core3.util.TorrentUtils;
import org.gudy.azureus2.core3.util.UrlUtils;
import org.spongycastle.util.encoders.Base64;
import org.json.simple.JSONObject;

import com.aelitis.azureus.core.lws.LightWeightSeed;
import com.aelitis.azureus.core.lws.LightWeightSeedAdapter;
import com.aelitis.azureus.core.lws.LightWeightSeedManager;
import com.aelitis.azureus.core.metasearch.Engine;
import com.aelitis.azureus.core.metasearch.MetaSearchManagerFactory;
import com.aelitis.azureus.core.security.CryptoECCUtils;
import com.aelitis.azureus.core.subs.Subscription;
import com.aelitis.azureus.core.subs.SubscriptionException;
import com.aelitis.azureus.core.subs.SubscriptionHistory;
import com.aelitis.azureus.core.subs.SubscriptionListener;
import com.aelitis.azureus.core.subs.SubscriptionManager;
import com.aelitis.azureus.core.subs.SubscriptionPopularityListener;
import com.aelitis.azureus.core.subs.SubscriptionResult;
import com.aelitis.azureus.core.subs.SubscriptionResultFilter;
import com.aelitis.azureus.core.vuzefile.VuzeFile;
import com.aelitis.azureus.core.vuzefile.VuzeFileHandler;
import com.aelitis.azureus.util.ImportExportUtils;
import com.aelitis.azureus.util.JSONUtils;

public class 
SubscriptionImpl 
	implements Subscription 
{
		
	private static final int MAX_ASSOCIATIONS;
	
	static{
		int max_assoc = 256;
		
		try{
			max_assoc = Integer.parseInt( System.getProperty( "azureus.subs.max.associations", ""+max_assoc));
			
		}catch( Throwable e ){
			Debug.out( e );
		}
		
		MAX_ASSOCIATIONS = max_assoc;
	}
	
	private static final int MIN_RECENT_ASSOC_TO_RETAIN		= 16;
		
	//private static final byte[] GENERIC_PUBLIC_KEY 		= {(byte)0x04,(byte)0xd0,(byte)0x1a,(byte)0xd9,(byte)0xb9,(byte)0x99,(byte)0xd8,(byte)0x49,(byte)0x15,(byte)0x5f,(byte)0xe9,(byte)0x6b,(byte)0x3c,(byte)0xd8,(byte)0x18,(byte)0x81,(byte)0xf7,(byte)0x92,(byte)0x15,(byte)0x3f,(byte)0x24,(byte)0xaa,(byte)0x35,(byte)0x6f,(byte)0x52,(byte)0x01,(byte)0x79,(byte)0x2e,(byte)0x93,(byte)0xf6,(byte)0xf1,(byte)0x57,(byte)0x13,(byte)0x2a,(byte)0x3c,(byte)0x31,(byte)0x66,(byte)0xa5,(byte)0x34,(byte)0x9f,(byte)0x79,(byte)0x62,(byte)0x04,(byte)0x31,(byte)0x68,(byte)0x37,(byte)0x8f,(byte)0x77,(byte)0x5c};
	// private static final byte[] GENERIC_PRIVATE_KEY 	= {(byte)0x71,(byte)0xc3,(byte)0xe8,(byte)0x6c,(byte)0x56,(byte)0xbb,(byte)0x30,(byte)0x14,(byte)0x9e,(byte)0x19,(byte)0xa5,(byte)0x3d,(byte)0xcb,(byte)0x47,(byte)0xbb,(byte)0x6d,(byte)0x57,(byte)0x57,(byte)0xd3,(byte)0x59,(byte)0xce,(byte)0x8f,(byte)0x79,(byte)0xe5};

	protected static byte[]
	intToBytes(
		int		version )
	{
		return( new byte[]{ (byte)(version>>24), (byte)(version>>16),(byte)(version>>8),(byte)version } );
	}
	
	protected static int
	bytesToInt(
		byte[]		bytes )
	{
		return( (bytes[0]<<24)&0xff000000 | (bytes[1] << 16)&0x00ff0000 | (bytes[2] << 8)&0x0000ff00 | bytes[3]&0x000000ff );
	}
		
	private SubscriptionManagerImpl		manager;
	
	private byte[]			public_key;
	private byte[]			private_key;
	
	private String			name;
	private String			name_ex;
	
	private int				version;
	private int				az_version;
	
	private boolean			is_public;		// whether or not we publish associations
	private boolean			is_anonymous;	// whether or not the subscription is anon
	
	private Map				singleton_details;
	
	private byte[]			hash;
	private byte[]			sig;
	private int				sig_data_size;
	
	private int				add_type;
	private long			add_time;
	
	private boolean			is_subscribed;
	
	private int				highest_prompted_version;
	
	private byte[]			short_id;

	private String			id;

	private final List<association> associations = new ArrayList<>();
	
	private int				fixed_random;
	
	private long			popularity				= -1;
	
	private long			last_auto_upgrade_check	= -1;
	private boolean			published;
	
	private boolean			server_published;
	private boolean			server_publication_outstanding;
	
	private boolean			singleton_sp_attempted;
	private String			local_name;
	
	private LightWeightSeed	lws;
	private int				lws_skip_check;
	
	private boolean			destroyed;
	
	private Map				history_map;
	private Map				schedule_map;
	
	private final Map		user_data = new LightHashMap();
	
	private final 			SubscriptionHistoryImpl	history;
	
	private String			referer;
	
	private final List		listeners = new CopyOnWriteArrayList();
	
	private Map				verify_cache_details;
	private boolean			verify_cache_result;
	
	private String			creator_ref;
	private String			category;
	private long			tag_id = -1;
	private String			parent;
	
	protected static String
	getSkeletonJSON(
		Engine		engine,
		int			check_interval_mins )
	{
		JSONObject	map = new JSONObject();
		
		map.put( "engine_id", engine.getId());
		
		map.put( "search_term", "" );

		map.put( "filters", new HashMap());
		
		map.put( "options", new HashMap());
		
		Map schedule = new HashMap();
		
		schedule.put( "interval", (long) check_interval_mins);
		
		List	days = new ArrayList();
		
		for (int i=1;i<=7;i++){
			
			days.add( String.valueOf(i));
		}
		
		schedule.put( "days", days );
		
		map.put( "schedule", schedule );
		
		embedEngines( map, engine );
		
		return( JSONUtils.encodeToJSON( map ));
	}
	
	protected static String
	getSkeletonJSON(
		Engine		engine,
		String		term,
		String		networks,
		int			check_interval_mins )
	{
		JSONObject	map = new JSONObject();
		
		map.put( "engine_id", engine.getId());
		
		map.put( "search_term", term );
		
		if ( networks != null ){
			
			map.put( "networks", networks );
		}

		map.put( "filters", new HashMap());
		
		map.put( "options", new HashMap());
		
		Map schedule = new HashMap();
		
		schedule.put( "interval", (long) check_interval_mins);
		
		List	days = new ArrayList();
		
		for (int i=1;i<=7;i++){
			
			days.add( String.valueOf(i));
		}
		
		schedule.put( "days", days );
		
		map.put( "schedule", schedule );
		
		embedEngines( map, engine );
		
		return( JSONUtils.encodeToJSON( map ));
	}
	
	
		// new subs constructor
	
	protected
	SubscriptionImpl(
		SubscriptionManagerImpl		_manager,
		String						_name,
		boolean						_public,
		boolean						_anonymous,
		Map							_singleton_details,
		String						_json_content,
		int							_add_type )
	
		throws SubscriptionException
	{
		manager	= _manager;
		
		history_map	= new HashMap();

		history = new SubscriptionHistoryImpl( manager, this );
		
		name				= _name;
		is_public			= _public;
		is_anonymous		= _anonymous;
		singleton_details	= _singleton_details;
		
		version				= 1;
		az_version			= AZ_VERSION;
		
		add_type			= _add_type;
		add_time			= SystemTime.getCurrentTime();
		
		is_subscribed		= true;

		try{
			KeyPair	kp = CryptoECCUtils.createKeys();
				
			public_key 			= CryptoECCUtils.keyToRawdata( kp.getPublic());
			private_key 		= CryptoECCUtils.keyToRawdata( kp.getPrivate());
						
			
			fixed_random	= RandomUtils.nextInt();
			
			init();
			
			String json_content = embedEngines( _json_content );
			
			SubscriptionBodyImpl body = new SubscriptionBodyImpl( manager, name, is_public, is_anonymous, json_content, public_key, version, az_version, singleton_details );
						
			syncToBody( body );
			
		}catch( Throwable e ){
			
			throw( new SubscriptionException( "Failed to create subscription", e ));
		}
	}
	
		// cache detail constructor
	
	protected
	SubscriptionImpl(
		SubscriptionManagerImpl		_manager,
		Map							map )
	
		throws IOException
	{
		manager	= _manager;
				
		fromMap( map );
		
		history = new SubscriptionHistoryImpl( manager, this );

		init();
	}

		// import constructor
	
	protected
	SubscriptionImpl(
		SubscriptionManagerImpl		_manager,
		SubscriptionBodyImpl		_body,
		int							_add_type,
		boolean						_is_subscribed )
	
		throws SubscriptionException
	{
		manager	= _manager;
			
		history_map	= new HashMap();
		
		history = new SubscriptionHistoryImpl( manager, this );
		
		syncFromBody( _body );
		
		add_type		= _add_type;
		add_time		= SystemTime.getCurrentTime();
		
		is_subscribed	= _is_subscribed;
		
		fixed_random	= RandomUtils.nextInt();
		
		init();
				
		syncToBody( _body );
	}
	
	protected void
	syncFromBody(
		SubscriptionBodyImpl	body )
	
		throws SubscriptionException
	{
		public_key			= body.getPublicKey();
		version				= body.getVersion();
		az_version			= body.getAZVersion();
				
		name				= body.getName();
		is_public			= body.isPublic();
		is_anonymous		= body.isAnonymous();
		singleton_details	= body.getSingletonDetails();
		
		if ( az_version > AZ_VERSION ){
			
			throw( new SubscriptionException( MessageText.getString( "subscription.version.bad", new String[]{ name })));
		}
	}
	
	protected void
	syncToBody(
		SubscriptionBodyImpl		body )
	
		throws SubscriptionException
	{
			// this picks up latest values of version, name + is_public from here
		
		body.writeVuzeFile( this );
		
		hash 			= body.getHash();
		sig				= body.getSig();
		sig_data_size	= body.getSigDataSize();
	}
	
	protected Map
	toMap()
	
		throws IOException
	{
		synchronized( this ){
			
			Map	map = new HashMap();
			
			map.put( "name", name.getBytes(StandardCharsets.UTF_8));
			
			map.put( "public_key", public_key );
						
			map.put( "version", (long) version);
			
			map.put( "az_version", (long) az_version);
			
			map.put( "is_public", (long) (is_public ? 1 : 0));
			
			map.put( "is_anonymous", (long) (is_anonymous ? 1 : 0));
			
			if ( singleton_details != null ){
				
				map.put( "sin_details", singleton_details );
				map.put( "spa", (long) (singleton_sp_attempted ? 1 : 0));
			}
			
			if ( local_name != null ){
				
				map.put( "local_name", local_name );
			}
				// body data
			
			map.put( "hash", hash );
			map.put( "sig", sig );
			map.put( "sig_data_size", (long) sig_data_size);
			
				// local data
			
			if ( private_key != null ){
				
				map.put( "private_key", private_key );
			}

			map.put( "add_type", (long) add_type);
			map.put( "add_time", add_time);
			
			map.put( "subscribed", (long) (is_subscribed ? 1 : 0));
			
			map.put( "pop", popularity);
			
			map.put( "rand", (long) fixed_random);
			
			map.put( "hupv", (long) highest_prompted_version);
			
			map.put( "sp", (long) (server_published ? 1 : 0));
			map.put( "spo", (long) (server_publication_outstanding ? 1 : 0));
						
			if ( associations.size() > 0 ){
				
				List	l_assoc = new ArrayList();
				
				map.put( "assoc", l_assoc );

                for (association assoc : associations) {

                    Map m = new HashMap();

                    l_assoc.add(m);

                    m.put("h", assoc.getHash());
                    m.put("w", assoc.getWhen());
                }
			}
			
			map.put( "history", history_map );
			
			if ( creator_ref != null ){
				
				map.put( "cref", creator_ref.getBytes(StandardCharsets.UTF_8));
			}
			
			if ( category != null ){
				
				map.put( "cat", category.getBytes(StandardCharsets.UTF_8));
			}
			
			if ( tag_id != -1 ){
				
				map.put( "tag", tag_id );
			}
			
			if ( parent != null ){
				
				map.put( "par", parent.getBytes(StandardCharsets.UTF_8));
			}
			
			return( map );
		}
	}
	
	protected void
	fromMap(
		Map		map )
	
		throws IOException
	{
		name				= new String((byte[])map.get( "name"), StandardCharsets.UTF_8);
		public_key			= (byte[])map.get( "public_key" );
		private_key			= (byte[])map.get( "private_key" );
		version				= ((Long)map.get( "version" )).intValue();
		az_version			= (int)ImportExportUtils.importLong( map, "az_version", AZ_VERSION );
		is_public			= ((Long)map.get( "is_public")).intValue() == 1;
		Long anon			= (Long)map.get( "is_anonymous" );
		is_anonymous		= anon!=null&&anon==1;
		singleton_details	= (Map)map.get( "sin_details" );
		
		hash			= (byte[])map.get( "hash" );
		sig				= (byte[])map.get( "sig" );
		sig_data_size	= ((Long)map.get( "sig_data_size" )).intValue();
		
		fixed_random	= ((Long)map.get( "rand" )).intValue();
		
		add_type		= ((Long)map.get( "add_type" )).intValue();		
		add_time		= (Long) map.get("add_time");
		
		is_subscribed	= ((Long)map.get( "subscribed" )).intValue()==1;
				
		popularity		= (Long) map.get("pop");
		
		highest_prompted_version = ((Long)map.get( "hupv" )).intValue();
		
		server_published = ((Long)map.get( "sp" )).intValue()==1;
		server_publication_outstanding = ((Long)map.get( "spo" )).intValue()==1;
		
		Long	l_spa = (Long)map.get( "spa" );
		
		if ( l_spa != null ){
			singleton_sp_attempted = l_spa ==1;
		}
		
		byte[]	b_local_name = (byte[])map.get( "local_name" );
		
		if ( b_local_name != null ){
			
			local_name = new String( b_local_name, StandardCharsets.UTF_8);
		}
		
		List	l_assoc = (List)map.get( "assoc" );
		
		if ( l_assoc != null ){

            for (Object o : l_assoc) {

                Map m = (Map) o;

                byte[] hash = (byte[]) m.get("h");
                long when = (Long) m.get("w");

                associations.add(new association(hash, when));
            }
		}
		
		history_map = (Map)map.get( "history" );
		
		if ( history_map == null ){
			
			history_map = new HashMap();
		}
		
		byte[] b_cref = (byte[])map.get( "cref" );
		
		if ( b_cref != null ){
			
			creator_ref = new String( b_cref, StandardCharsets.UTF_8);
		}
		
		byte[] b_cat = (byte[])map.get( "cat" );
		
		if ( b_cat != null ){
			
			category = new String( b_cat, StandardCharsets.UTF_8);
		}
		
		Long l_tag_id = (Long)map.get( "tag" );
		
		if ( l_tag_id != null ){
			
			tag_id = l_tag_id;
		}
		
		byte[] b_parent = (byte[])map.get( "par" );
		
		if ( b_parent != null ){
			
			parent = new String( b_parent, StandardCharsets.UTF_8);
		}
	}
	
	protected Map
	getScheduleConfig()
	{
		if ( schedule_map == null ){
			
			try{		
				Map map = JSONUtils.decodeJSON( getJSON());

				schedule_map = (Map)map.get( "schedule" );
				
				if ( schedule_map == null ){
					
					schedule_map = new HashMap();
				}
			}catch( Throwable e ){
				
				log( "Failed to load schedule", e );
				
				schedule_map = new HashMap();
			}
		}
		
		return( schedule_map );
	}
	
	protected Map
	getHistoryConfig()
	{
		return( history_map );
	}
	
	protected void
	updateHistoryConfig(
		Map		_history_map,
		int		reason )
	{
		history_map = _history_map;
		
		fireChanged( reason );
	}
	
	protected void
	upgrade(
		SubscriptionBodyImpl		body )
	
		throws SubscriptionException
	{
			// pick up details from the body (excluding json that is maintained in body only)
		
		syncFromBody( body );
		
			// write to file
		
		syncToBody(body);
		
		fireChanged( SubscriptionListener.CR_METADATA );
	}
	
	protected void
	init()
	{
		short_id = SubscriptionBodyImpl.deriveShortID( public_key, singleton_details );
		id = null;
	}
	
	public boolean
	isSingleton()
	{
		return( singleton_details != null );
	}
	
	public boolean
	isShareable()
	{
		try{
			return( getEngine().isShareable() && !isSingleton());
			
		}catch( Throwable e ){
			
			Debug.printStackTrace(e);
			
			return( false );
		}
	}
	
	public boolean
	isSearchTemplate()
	{
		return( getName(false).startsWith( "Search Template:" ));
	}
	
	protected Map
	getSingletonDetails()
	{
		return( singleton_details );
	}
	
	protected boolean
	getSingletonPublishAttempted()
	{
		return( singleton_sp_attempted );
	}
	
	protected void
	setSingletonPublishAttempted()
	{
		if ( !singleton_sp_attempted ){
			
			singleton_sp_attempted = true;
		
			manager.configDirty( this );
		}
	}
	
	public String
	getName()
	{
		return( getName( true ));
	}
	
	public String
	getName(
		boolean	use_local )
	{
		return( local_name==null?name:local_name );
	}
	
	public String
	getURI()
	{
		String str = "sub:?name=" + UrlUtils.encode(getName()) + "&id=" + Base32.encode(getShortID()) + "&v=" + getVersion();
		
		if ( is_anonymous ){
			
			str += "&a=1";
		}
		
		return( "azplug:?id=subscription&arg=" + UrlUtils.encode( str ));
	}
	
	public void 
	requestAttention() 
	{
		manager.selectSubscription( this );
	}
	
	public void
	setLocalName(
		String		str )
	{
		local_name = str;
		
		manager.configDirty( this );
		
		fireChanged(SubscriptionListener.CR_METADATA);
	}
	
	public void
	setName(
		String		_name )
	
		throws SubscriptionException
	{
		if ( !name.equals( _name )){
			
			boolean	ok = false;
			
			String	old_name 	= name;
			int		old_version	= version;
			
			try{
				name	= _name;
				
				version++;
				
				SubscriptionBodyImpl body = new SubscriptionBodyImpl( manager, this );
					
				syncToBody( body );
				
				versionUpdated( body, false );
				
				ok	= true;
				
			}finally{
				
				if ( !ok ){
					
					name 	= old_name;
					version	= old_version;
				}
			}
			
			fireChanged(SubscriptionListener.CR_METADATA);
		}
	}
	
	public String
	getNameEx()
	{
		if ( name_ex == null ){
			
			try{
				Map map = JSONUtils.decodeJSON( getJSON());
				
				String	search_term	= (String)map.get( "search_term" );
				Map		filters		= (Map)map.get( "filters" );
	
				Engine engine = manager.getEngine( this, map, true );

				String	engine_name = engine.getNameEx();
				
				if ( name.startsWith( engine_name )){
					
					name_ex = name;
					
				}else if ( engine_name.startsWith( name )){
					
					name_ex = engine_name;
					
				}else{
					
					name_ex = name + ": " + engine.getNameEx();
				}
				
				if ( search_term != null && search_term.length() > 0 ){
					
					name_ex += ", query=" + search_term;
				}
				
				if ( filters != null && filters.size() > 0 ){
					
					name_ex += ", filters=" + new SubscriptionResultFilterImpl(this,filters).getString();
				}
				
			}catch( Throwable e ){
				
				name_ex = name + ": " + Debug.getNestedExceptionMessage(e);
			}
		}
		
		return( name_ex );
	}
	
	public String
	getQueryKey()
	{
		try{
			Map map = JSONUtils.decodeJSON( getJSON());
			
			String	search_term	= (String)map.get( "search_term" );
			Map		filters		= (Map)map.get( "filters" );

			Engine engine = manager.getEngine( this, map, true );

			String	name = engine.getNameEx();
					
			if ( search_term != null && search_term.length() > 0 ){
				
				name += ", query=" + search_term;
			}
			
			if ( filters != null && filters.size() > 0 ){
				
				name += ", filters=" + new SubscriptionResultFilterImpl(this,filters).getString();
			}
			
			return( name );
			
		}catch( Throwable e ){
			
			return( null );
		}
	}
	
	public long
	getAddTime()
	{
		return( add_time );
	}
	
	public int
	getAddType()
	{
		return( add_type );
	}
	
	public boolean
	isPublic()
	{
		return( is_public );
	}
	
	public boolean
	isAnonymous()
	{
		return( is_anonymous );
	}
	
	public void
	setPublic(
		boolean		_is_public )
	
		throws SubscriptionException
	{
		if ( is_public != _is_public ){
				
			boolean	ok = false;
			
			boolean	old_public	= is_public;
			int		old_version	= version;
			
			try{
				is_public	= _is_public;
				
				version++;
								
				SubscriptionBodyImpl body = new SubscriptionBodyImpl( manager, this );
				
				syncToBody( body );
				
				versionUpdated( body, false );

				ok = true;
				
			}finally{
				
				if ( !ok ){
				
					version		= old_version;
					is_public	= old_public;
				}
			}
			
			fireChanged(SubscriptionListener.CR_METADATA);
		}
	}
	
	protected boolean
	getServerPublicationOutstanding()
	{
		return( server_publication_outstanding );
	}
	
	protected void
	setServerPublicationOutstanding()	
	{
		if ( !server_publication_outstanding ){
			
			server_publication_outstanding = true;
		
			fireChanged(SubscriptionListener.CR_METADATA);
		}
	}
	
	protected void
	setServerPublished()
	{
		if ( server_publication_outstanding || !server_published ){
			
			server_published 				= true;
			server_publication_outstanding	= false;
			
			fireChanged(SubscriptionListener.CR_METADATA);
		}
	}
	
	protected boolean
	getServerPublished()
	{
		return( server_published );
	}
	
	public String
	getJSON()
	
		throws SubscriptionException
	{
		try{
			SubscriptionBodyImpl body = new SubscriptionBodyImpl( manager, this );

			return( body.getJSON());
			
		}catch( Throwable e ){
			
			history.setFatalError( Debug.getNestedExceptionMessage(e));
			
			if ( e instanceof SubscriptionException ){
				
				throw((SubscriptionException)e );
			}
			
			throw( new SubscriptionException( "Failed to read subscription", e ));
		}
	}
	
	public boolean
	setJSON(
		String		_json )
	
		throws SubscriptionException
	{
		String json = embedEngines( _json );
		
		SubscriptionBodyImpl body = new SubscriptionBodyImpl( manager, this );		
		
		String	old_json = body.getJSON();
		
		if ( !json.equals( old_json )){
			
			boolean	ok = false;
			
			int		old_version	= version;
			
			try{				
				version++;
													
				body.setJSON( json );
				
				syncToBody( body );
				
				versionUpdated( body, true );

				referer = null;
				
				ok	= true;
				
			}finally{
				
				if ( !ok ){
					
					version	= old_version;
				}
			}
			
			fireChanged(SubscriptionListener.CR_METADATA);
			
			return( true );
		}
		
		return( false );
	}
	
	protected String
	embedEngines(
		String		json_in )
	{
			// see if we need to embed private search templates
		
		Map map = JSONUtils.decodeJSON( json_in );
		
		long 	engine_id 	= (Long) map.get("engine_id");

		String	json_out	= json_in;
		
		if ( engine_id >= Integer.MAX_VALUE || engine_id < 0 ){
			
			Engine engine = MetaSearchManagerFactory.getSingleton().getMetaSearch().getEngine( engine_id );

			if ( engine == null ){
				
				log( "Private search template with id '" + engine_id + "' not found!!!!" );
				
			}else{
				
				try{								
					embedEngines( map, engine );
					
					json_out = JSONUtils.encodeToJSON( map );
					

					log( "Embedded private search template '" + engine.getName() + "'" );
					
				}catch( Throwable e ){
					
					log( "Failed to embed private search template", e );
				}
			}
		}
		
		return( json_out );
	}
	
	protected static void
	embedEngines(
		Map			map,
		Engine		engine )
	{
		Map	engines = new HashMap();
		
		map.put( "engines", engines );
		
		Map	engine_map = new HashMap();
		
		try{
		
			String	engine_str = new String( Base64.encode( BEncoder.encode( engine.exportToBencodedMap())), StandardCharsets.UTF_8);
		
			engine_map.put( "content", engine_str );
		
			engines.put( String.valueOf( engine.getId()), engine_map );
			
		}catch( Throwable e ){
			
			Debug.out( e );
		}
	}

	protected Engine
	extractEngine(
		Map		json_map,
		long	id )
	{
		Map engines = (Map)json_map.get( "engines" );
		
		if ( engines != null ){
			
			Map	engine_map = (Map)engines.get( String.valueOf( id ));
			
			if ( engine_map != null ){
				
				String	engine_str = (String)engine_map.get( "content" );
				
				try{
				
					Map map = BDecoder.decode( Base64.decode( engine_str.getBytes(StandardCharsets.UTF_8)));
						
					return( MetaSearchManagerFactory.getSingleton().getMetaSearch().importFromBEncodedMap(map));
					
				}catch( Throwable e ){
					
					log( "failed to import engine", e );
				}
			}
		}
		
		return( null );
	}
	
	public Subscription
	cloneWithNewEngine(
		Engine		engine )
	
		throws SubscriptionException
	{
		try{	
			String	json = getJSON();
			
			Map map = JSONUtils.decodeJSON( json );

			long	id = (Long) map.get("engine_id");
	
			if ( id == engine.getId()){
					
				embedEngines(map, engine);
				
				SubscriptionImpl subs = new SubscriptionImpl( manager, getName(), engine.isPublic(), isAnonymous(), null, JSONUtils.encodeToJSON(map), SubscriptionImpl.ADD_TYPE_CREATE );

				subs = manager.addSubscription( subs );
				
				setLocalName( getName( false ) + " (old)" );
				
				return( subs );
		
			}else{
				
				throw( new SubscriptionException( "Engine mismatch" ));
			}
		}catch( Throwable e ){
			
			throw( new SubscriptionException( "Failed to export engine", e ));
		}
	}
	
	public Engine
	getEngine()
	
		throws SubscriptionException
	{
		return( getEngine( true ));
	}
	
	protected Engine
	getEngine(
		boolean		local_only )
	
		throws SubscriptionException
	{
		Map map = JSONUtils.decodeJSON( getJSON());
					
		return( manager.getEngine( this, map, local_only ));
	}
	
	protected void
	engineUpdated(
		Engine		engine )
	{
		try{
			String	json = getJSON();
			
			Map map = JSONUtils.decodeJSON( json );

			long	id = (Long) map.get("engine_id");
			
			if ( id == engine.getId()){
								
				if ( setJSON( json )){
					
					log( "Engine has been updated, saved" );
				}
			}
		}catch( Throwable e ){
			
			log( "Engine update failed", e );
		}
	}
	
	public boolean
	setDetails(
		String		_name,
		boolean		_is_public,
		String		_json )
	
		throws SubscriptionException
	{
		_json = embedEngines( _json );
		
		SubscriptionBodyImpl body = new SubscriptionBodyImpl( manager, this );		
		
		String	old_json = body.getJSON();
		
		boolean	json_changed = !_json.equals( old_json );
		
		if ( 	!_name.equals( name ) ||
				_is_public != is_public ||
				json_changed ){
			
			boolean	ok = false;
			
			String	old_name	= name;
			boolean	old_public	= is_public;
			int		old_version	= version;
			
			try{
				is_public	= _is_public;			
				name		= _name;

				body.setJSON( _json );
				
				version++;
												
				syncToBody( body );
				
				versionUpdated( body, json_changed );

				ok = true;
				
			}finally{
				
				if ( !ok ){
				
					version		= old_version;
					is_public	= old_public;
					name		= old_name;
				}
			}
			
			fireChanged(SubscriptionListener.CR_METADATA);
			
			return( true );
		}
		
		return( false );
	}
	
	protected void
	versionUpdated(
		SubscriptionBodyImpl		body,
		boolean						json_changed )
	{
		if ( json_changed ){
			
			try{		
				Map map = JSONUtils.decodeJSON( body.getJSON());

				schedule_map = (Map)map.get( "schedule" );
				
			}catch( Throwable e ){
			}
		}
		
		name_ex = null;
		
		if ( is_public ){
			
			manager.updatePublicSubscription( this );
			
			setPublished( false );
			
			synchronized( this ){

                for (SubscriptionImpl.association association : associations) {

                    association.setPublished(false);
                }
			}
		}	
	}
	
	public byte[]
	getPublicKey()
	{
		return( public_key );
	}
	
	public byte[]
	getShortID()
	{
		return( short_id );
	}
	
	public String
	getID()
	{
		if (id == null) {
			id = Base32.encode(getShortID());
		}
		return( id );
	}
	
	protected byte[]
	getPrivateKey()
	{
		return( private_key );
	}
	
	protected int
	getFixedRandom()
	{
		return( fixed_random );
	}
	
	public int
	getVersion()
	{
		return( version );
	}
	
	public int
	getAZVersion()
	{
		return( az_version );
	}
	
	protected void
	setHighestUserPromptedVersion(
		int		v )
	{
		if ( v < version ){
			
			v  = version;
		}
		
		if ( highest_prompted_version != v ){
			
			highest_prompted_version = v;
			
			fireChanged(SubscriptionListener.CR_METADATA);
		}
	}
	
	protected int
	getHighestUserPromptedVersion()
	{
		return( highest_prompted_version );
	}
	
	public int
	getHighestVersion()
	{
		return( Math.max( version, highest_prompted_version ));
	}
	
	public void
	resetHighestVersion()
	{
		if ( highest_prompted_version > 0 ){
			
			highest_prompted_version = 0;
			
			fireChanged(SubscriptionListener.CR_METADATA);
			
			manager.checkUpgrade(this);
		}
	}
	
	public boolean
	isMine()
	{
		if ( private_key == null ){
			
			return( false );
		}

		return !isSingleton() || add_type == ADD_TYPE_CREATE;
	}
	
	public boolean
	isUpdateable()
	{
		return( private_key != null );
	}
	
	public boolean
	isSubscribed()
	{
		return( is_subscribed );
	}
	
	public void
	setSubscribed(
		boolean			s )
	{
		if ( is_subscribed != s ){
			
			is_subscribed = s;
			
			if ( is_subscribed ){
				
				manager.setSelected( this );
				
			}else{
				
				reset();
			}
			
			fireChanged(SubscriptionListener.CR_METADATA);
		}
	}
	
	public boolean 
	isAutoDownloadSupported() 
	{
		return( history.isAutoDownloadSupported());
	}
	
	public void
	getPopularity(
		final SubscriptionPopularityListener	listener )
	
		throws SubscriptionException
	{
		new AEThread2( "subs:popwait", true )
		{
			public void
			run()
			{		
				try{
					manager.getPopularity( 
						SubscriptionImpl.this,
						new SubscriptionPopularityListener()
						{
							public void
							gotPopularity(
								long						pop )
							{
								if ( pop != popularity ){
									
									popularity = pop;
									
									fireChanged(SubscriptionListener.CR_METADATA);
								}
								
								listener.gotPopularity( popularity );
							}
							
							public void
							failed(
								SubscriptionException		e )
							{
								if ( popularity == -1 ){
									
									listener.failed( new SubscriptionException( "Failed to read popularity", e ));
									
								}else{
									
									listener.gotPopularity( popularity );
								}
							}
						});
					
				}catch( Throwable e ){
					
					if ( popularity == -1 ){
					
						listener.failed( new SubscriptionException( "Failed to read popularity", e ));
						
					}else{
						
						listener.gotPopularity( popularity );
					}
				}
			}
		}.start();
	}
	
	public long 
	getCachedPopularity() 
	{
		return( popularity );
	}
	
	protected void
	setCachedPopularity(
		long		pop )
	{
		if ( pop != popularity ){
			
			popularity		= pop;
			
			fireChanged(SubscriptionListener.CR_METADATA);
		}
	}
	
	public String
	getReferer()
	{
		if ( referer == null ){
			
			try{
				Map map = JSONUtils.decodeJSON( getJSON());
						
				Engine engine = manager.getEngine( this, map, false );
				
				if ( engine != null ){
										
					referer = engine.getReferer();
				}
			}catch( Throwable e ){
				
				log( "Failed to get referer", e );
			}
			
			if ( referer == null ){
				
				referer = "";
			}
		}
		
		return( referer );
	}
	
	protected void
	checkPublish()
	{
		synchronized( this ){
			
			if ( destroyed ){
				
				return;
			}
				
				// singleton's not available for upgrade
			
			if ( isSingleton()){
				
				return;
			}
			
				// nothing to do for unsubscribed ones
			
			if ( !isSubscribed()){
				
				return;
			}
			
			if ( popularity > 100 ){
			
					// one off test on whether to track so we have around 100 active
				
				if ( lws_skip_check == 2 ){
					
					return;
					
				}else if ( lws_skip_check == 0 ){
									
					if ( RandomUtils.nextInt((int)(( popularity + 99 ) / 100 )) == 0 ){
						
						lws_skip_check = 1;
						
					}else{
						
						lws_skip_check = 2;
						
						return;
					}
				}
			}
			
			if ( hash != null ){
				
				boolean	create = false;

				if ( lws == null ){
					
					create = true;
					
				}else{
					
					if ( !Arrays.equals( lws.getHash().getBytes(), hash )){
			
						lws.remove();
						
						create = true;
					}
				}
				
				if ( create ){
										
					try{
						File original_data_location = manager.getVuzeFile( this );

						if ( original_data_location.exists()){
							
								// make a version based filename to avoid issues regarding multiple
								// versions
							
							final File	versioned_data_location = new File( original_data_location.getParent(), original_data_location.getName() + "." + getVersion());
							
							if ( !versioned_data_location.exists()){
								
								if ( !FileUtil.copyFile( original_data_location, versioned_data_location )){
									
									throw( new Exception( "Failed to copy file to '" + versioned_data_location + "'" ));
								}
							}
							
							lws = LightWeightSeedManager.getSingleton().add(
									getName(),
									new HashWrapper( hash ),
									TorrentUtils.getDecentralisedEmptyURL(),
									versioned_data_location,
									isAnonymous()?AENetworkClassifier.AT_I2P:AENetworkClassifier.AT_PUBLIC,
									new LightWeightSeedAdapter()
									{
										public TOTorrent 
										getTorrent(
											byte[] 		hash,
											URL 		announce_url, 
											File 		data_location) 
										
											throws Exception
										{
											log( "Generating light-weight torrent: hash=" + ByteFormatter.encodeString( hash ));
											
											TOTorrentCreator creator = 
												TOTorrentFactory.createFromFileOrDirWithFixedPieceLength( 
														data_location, 
														announce_url,
														256*1024 );
									
											TOTorrent t = creator.create();
											
											t.setHashOverride( hash );
											
											return( t );
										}
									});
						}
								
					}catch( Throwable e ){
						
						log( "Failed to create light-weight-seed", e );
					}
				}
			}
		}
	}
	
	protected synchronized boolean
	canAutoUpgradeCheck()
	{
		if ( isSingleton()){
			
			return( false );
		}
		
		long	now = SystemTime.getMonotonousTime();
		
		if ( last_auto_upgrade_check == -1 || now - last_auto_upgrade_check > 4*60*60*1000 ){
			
			last_auto_upgrade_check = now;
			
			return( true );
		}
		
		return( false );
	}
	
	public void
	addAssociation(
		byte[]		hash )
	{
		if ( hash.length != 20 ){
			
			Debug.out( "Invalid hash: " + ByteFormatter.encodeString( hash ));
			
			return;
		}
		
		addAssociationSupport( hash, false );
	}
	
	protected boolean
	addAssociationSupport(
		byte[]		hash,
		boolean		internal )
	{
		synchronized( this ){

            for (association assoc : associations) {

                if (Arrays.equals(assoc.getHash(), hash)) {

                    return (false);
                }
            }
			
			associations.add( new association( hash, SystemTime.getCurrentTime()));
			
			if ( MAX_ASSOCIATIONS > 0 && associations.size() > MAX_ASSOCIATIONS ){
				
				associations.remove( RandomUtils.nextInt( MAX_ASSOCIATIONS - MIN_RECENT_ASSOC_TO_RETAIN ));
			}
		}
		
		if ( !internal ){
			
			fireChanged(SubscriptionListener.CR_METADATA);
			
			manager.associationAdded( this, hash);
		}
		
		return( true );
	}
	
	public boolean
	hasAssociation(
		byte[]		hash )
	{
		synchronized( this ){

            for (association assoc : associations) {

                if (Arrays.equals(assoc.getHash(), hash)) {

                    return (true);
                }
            }
		}
			
		return( false );	
	}
	
	public void
	addPotentialAssociation(
		String		result_id,
		String		key )
	{
		manager.addPotentialAssociation( this, result_id, key );
	}
	
	public int
	getAssociationCount()
	{
		synchronized( this ){
			
			return( associations.size());
		}
	}
	
	protected association
	getAssociationForPublish()
	{
		synchronized( this ){
			
			int	num_assoc = associations.size();
			
				// first set in order of most recent
			
			for (int i=num_assoc-1;i>=Math.max( 0, num_assoc-MIN_RECENT_ASSOC_TO_RETAIN);i--){
				
				association assoc = associations.get(i);
				
				if ( !assoc.getPublished()){
					
					assoc.setPublished( true );
					
					return( assoc );
				}
			}
			
				// remaining randomised
			
			int	rem = associations.size() - MIN_RECENT_ASSOC_TO_RETAIN;
			
			if ( rem > 0 ){
				
				List<association> l = new ArrayList<>(associations.subList(0, rem));
				
				Collections.shuffle( l );

                for (association assoc : l) {

                    if (!assoc.getPublished()) {

                        assoc.setPublished(true);

                        return (assoc);
                    }
                }
			}
		}
		
		return( null );
	}
	
	protected int
	getAssociationsRemainingForPublish()
	{
		synchronized( this ){
			
			int	result = 0;
			
			for ( association a: associations ){
				
				if ( !a.getPublished()){
					
					result++;
				}
			}
			
			return( result );
		}
	}
	
	protected boolean
	getPublished()
	{
		return( published );
	}
	
	protected void
	setPublished(
		boolean		b )
	{
		published = b;
	}
	
	protected int
	getVerifiedPublicationVersion(
		Map		details )
	{
			// singleton versions always 1 and each instance has separate private key so
			// verification will always fail so save to just return current version
		
		if ( isSingleton()){
			
			return( getVersion());
		}
		
		if ( !verifyPublicationDetails( details )){
			
			return( -1 );
		}

		return( getPublicationVersion( details ));
	}
	
	protected static int
	getPublicationVersion(
		Map		details )
	{
		return(((Long)details.get("v")).intValue());
	}
	
	protected byte[]
	getPublicationHash()
	{
		return( hash );
	}
	
	protected static byte[]
	getPublicationHash(
		Map		details )
	{
		return((byte[])details.get( "h" ));
	}
	
	protected static int
	getPublicationSize(
		Map		details )
	{
		return(((Long)details.get("z")).intValue());
	}
	
	protected Map
	getPublicationDetails()
	{
		Map	result = new HashMap();
		
		result.put( "v", (long) version);
			
		if ( singleton_details == null ){
			
			result.put( "h", hash );
			result.put( "z", (long) sig_data_size);
			result.put( "s", sig );

		}else{
			
			result.put( "x", singleton_details );
		}
		
		return( result );
	}
	
	protected boolean
	verifyPublicationDetails(
		Map		details )
	{
		synchronized( this ){
			
			if ( BEncoder.mapsAreIdentical( verify_cache_details, details )){
								
				return( verify_cache_result );
			}
		}
				
		byte[]	hash 	= (byte[])details.get( "h" );
		int		version	= ((Long)details.get( "v" )).intValue();
		int		size	= ((Long)details.get( "z" )).intValue();
		byte[]	sig		= (byte[])details.get( "s" );
		
		boolean	result = SubscriptionBodyImpl.verify( public_key, hash, version, size, sig );
		
		synchronized( this ){
			
			verify_cache_details 	= details;
			verify_cache_result		= result;
		}
		
		return( result );
	}
	
	public void
	setCreatorRef(
		String	ref )
	{
		creator_ref = ref;
		
		fireChanged(SubscriptionListener.CR_METADATA);
	}
	
	public String
	getCreatorRef()
	{
		return( creator_ref );
	}
	
	public void
	setCategory(
		String	_category )
	{
		if ( _category == null && category == null ){
			
			return;
		}
		
		if (_category != null && _category.equals(category)){
			
			return;
		}
				
		manager.setCategoryOnExisting( this, category, _category );
		
		category = _category;

		fireChanged(SubscriptionListener.CR_METADATA);
	}
	
	public String
	getCategory()
	{
		return( category );
	}

	public void
	setTagID(
		long	_tag_id )
	{

		if ( _tag_id == tag_id ){
			
			return;
		}
			
		// don't update existing download tagging at the moment
		//manager.setTagOnExisting( this, tag, _tag );
		
		tag_id = _tag_id;

		fireChanged(SubscriptionListener.CR_METADATA);
	}
	
	public long
	getTagID()
	{
		return( tag_id );
	}
	
	public String
	getParent()
	{
		return( parent );
	}
	
	public void
	setParent(
		String		_parent )
	{
		if ( _parent == null && parent == null ){
			
			return;
		}
		
		if (_parent != null && _parent.equals(parent)){
			
			return;
		}
		
		parent = _parent;

		fireChanged(SubscriptionListener.CR_METADATA);
	}
	
	protected void
	fireChanged(
		int		reason )
	{
		manager.configDirty( this );

        for (Object listener : listeners) {

            try {
                ((SubscriptionListener) listener).subscriptionChanged(this, reason);

            } catch (Throwable e) {

                Debug.printStackTrace(e);
            }
        }
	}

	protected void
	fireDownloaded(
		boolean	was_auto )
	{

        for (Object listener : listeners) {

            try {
                ((SubscriptionListener) listener).subscriptionDownloaded(this, was_auto);

            } catch (Throwable e) {

                Debug.printStackTrace(e);
            }
        }
	}
	
	public void
	addListener(
		SubscriptionListener	l )
	{
		listeners.add( l );
	}
	
	public void
	removeListener(
		SubscriptionListener	l )
	{
		listeners.remove( l );
	}
	
	public SubscriptionHistory 
	getHistory() 
	{
		return( history );
	}
	
	public SubscriptionManager
	getManager()
	{
		return( manager );
	}
	
	public VuzeFile 
	getVuzeFile() 
	
		throws SubscriptionException
	{
		try{
			return( VuzeFileHandler.getSingleton().loadVuzeFile( manager.getVuzeFile( this ).getAbsolutePath()));
			
		}catch( Throwable e ){
			
			throw( new SubscriptionException( "Failed to get Vuze file", e ));
		}
	}
	
	public VuzeFile 
	getSearchTemplateVuzeFile()
	{
		if ( !isSearchTemplate()){
			
			return( null );
		}
		
		Object[] details = manager.getSearchTemplateVuzeFile( this );
		
		if ( details != null ){
			
			return((VuzeFile)details[0]);
		}
		
		return( null );
	}
	
	public boolean 
	isSearchTemplateImportable() 
	{
		return( manager.isSearchTemplateImportable( this ));
	}
	
	protected void
	destroy()
	{
		LightWeightSeed l;
		
		synchronized( this ){
			
			destroyed	= true;
			
			l = lws;
		}
		
		if ( l != null ){
			
			l.remove();
		}
	}
	
	public void
	reset()
	{
		getHistory().reset();
		
		try{
			getEngine().reset();
			
		}catch( Throwable e ){
			
			Debug.printStackTrace(e);
		}
	}
	
	public void
	remove()
	{
		destroy();
		
		manager.removeSubscription( this );
	}
	
	protected boolean
	isRemoved()
	{
		synchronized( this ){

			return( destroyed );
		}
	}
	
	public SubscriptionResult[]
  	getResults(
  		boolean		include_deleted )
	{
		return( getHistory().getResults( include_deleted ));
	}
	
	@Override
	public SubscriptionResultFilter 
	getFilters() 
	
		throws SubscriptionException
	{
		Map map = JSONUtils.decodeJSON( getJSON());

		Map		filters		= (Map)map.get( "filters" );

		return( new SubscriptionResultFilterImpl( this, filters ));
	}
	
	public void
	setUserData(
		Object		key,
		Object		data )
	{
		synchronized( user_data ){
			
			if ( data == null ){
				
				user_data.remove( key );
				
			}else{
				
				user_data.put( key, data );
			}
		}
	}
	
	public Object
	getUserData(
		Object		key )
	{
		synchronized( user_data ){

			return( user_data.get( key ));
		}
	}
	
	protected void
	log(
		String		str )
	{
		manager.log( getString() + ": " + str );
	}
	
	protected void
	log(
		String		str,
		Throwable	e )
	{
		manager.log( getString() + ": " + str, e );
	}
	
	public String
	getString()
	{
		return( "name=" + name + 
					",sid=" + ByteFormatter.encodeString( short_id ) + 
					",ver=" + version + 
					",pub=" + is_public +
					",anon=" + is_anonymous +
					",mine=" + isMine() +
					",sub=" + is_subscribed +
					(is_subscribed?(",hist={" + history.getString() + "}"):"") +
					",pop=" + popularity + 
					(server_publication_outstanding?",spo=true":""));
	}
	
	protected void
	generate(
		IndentWriter		writer )
	{
		String	engine_str;
		
		try{
			
			engine_str = "" + getEngine().getId();
			
		}catch( Throwable e ){
			
			engine_str = Debug.getNestedExceptionMessage(e);
		}
		
		writer.println( getString() + ": engine=" + engine_str );
			
		try{
			writer.indent();
			
			synchronized( this ){

                for (SubscriptionImpl.association association : associations) {

                    association.generate(writer);
                }
			}
		}finally{
			
			writer.exdent();
		}
	}
	
	protected static class
	association
	{
		private byte[]	hash;
		private long	when;
		private boolean	published;
		
		protected
		association(
			byte[]		_hash,
			long		_when )
		{
			hash		= _hash;
			when		= _when;
		}
		
		protected byte[]
		getHash()
		{
			return( hash );
		}
		
		protected long
		getWhen()
		{
			return( when );
		}
		
		protected boolean
		getPublished()
		{
			return( published );
		}
		
		protected void
		setPublished(
			boolean		b )
		{
			published = b;
		}
		
		protected String
		getString()
		{
			return( ByteFormatter.encodeString( hash ) + ", pub=" + published );
		}
		
		protected void
		generate(
			IndentWriter		writer )
		{
			writer.println( getString());
		}
	}
}
