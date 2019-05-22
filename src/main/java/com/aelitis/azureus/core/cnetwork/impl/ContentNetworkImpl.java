/*
 * Created on Nov 20, 2008
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


package com.aelitis.azureus.core.cnetwork.impl;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.util.BEncoder;
import org.gudy.azureus2.core3.util.Debug;

import com.aelitis.azureus.core.cnetwork.*;
import com.aelitis.azureus.core.vuzefile.VuzeFile;
import com.aelitis.azureus.core.vuzefile.VuzeFileComponent;
import com.aelitis.azureus.core.vuzefile.VuzeFileHandler;
import com.aelitis.azureus.util.ImportExportUtils;
import com.aelitis.azureus.util.MapUtils;

public abstract class 
ContentNetworkImpl
	implements ContentNetwork
{
	protected static final long	TYPE_VUZE_GENERIC		= 1;
	
	private static final String	PP_STARTUP_NETWORK		= "startup_network"; 	// Boolean

	protected static ContentNetworkImpl
	importFromBEncodedMapStatic(
		ContentNetworkManagerImpl	manager,
		Map							map )
	
		throws IOException
	{
		long type	= ImportExportUtils.importLong( map, "type" );
		
		if ( type == TYPE_VUZE_GENERIC ){
			
			return( new ContentNetworkVuzeGeneric( manager, map ));
			
		}else{
		
			throw( new IOException( "Unsupported network type: " + type ));
		}
	}
	
	private ContentNetworkManagerImpl	manager;
	private long						type;
	private long						version;
	private long						id;
	private String						name;
	
	private Map<String,Object>	pprop_defaults;
	
	private Map<Object,Object>	transient_properties = new ConcurrentHashMap<>();
	
	private List		persistent_listeners = new CopyOnWriteArrayList();
	
	protected
	ContentNetworkImpl(
		ContentNetworkManagerImpl	_manager,
		long						_type,
		long						_id,
		long						_version,
		String						_name,
		Map<String,Object>			_pprop_defaults )
	{
		manager		= _manager;
		type		= _type;
		version		= _version;
		id			= _id;
		name		= _name;
		
		pprop_defaults	= _pprop_defaults;
	}
	
	protected
	ContentNetworkImpl(
		ContentNetworkManagerImpl	_manager )
	{
		manager	= _manager;
	}
	
	protected void
	importFromBEncodedMap(
		Map<String,Object>		map )
	
		throws IOException
	{
		type	= ImportExportUtils.importLong( map, "type" );
		id		= ImportExportUtils.importLong( map, "id" );
		version	= ImportExportUtils.importLong( map, "version" );
		name 	= ImportExportUtils.importString( map, "name" );

		pprop_defaults = (Map<String,Object>)map.get( "pprop_defaults" );
	}
	
	protected void
	exportToBEncodedMap(
		Map<String,Object>			map )
	
		throws IOException
	{
		ImportExportUtils.exportLong( map, "type", type );
		ImportExportUtils.exportLong( map, "id", id );
		ImportExportUtils.exportLong( map, "version", version );
		ImportExportUtils.exportString( map, "name", name );
		
		if ( pprop_defaults != null ){
			
			map.put( "pprop_defaults", pprop_defaults );
		}
	}
	
	protected void
	updateFrom(
		ContentNetworkImpl	other )
	
		throws IOException
	{
		Map<String,Object>	map = new HashMap<>();
		
		other.exportToBEncodedMap(map);
		
		importFromBEncodedMap( map );
	}
	
	public long 
	getID() 
	{
		return( id );
	}
	
	protected long
	getVersion()
	{
		return( version );
	}
	
	public String
	getName()
	{
		return( name );
	}
	
	protected boolean
	isSameAs(
		ContentNetworkImpl		other )
	{
		try{
			Map<String,Object>	map1 = new HashMap<>();
			Map<String,Object>  map2 = new HashMap<>();
			
			exportToBEncodedMap( map1 );
			
			other.exportToBEncodedMap( map2 );
			
			return( BEncoder.mapsAreIdentical( map1, map2 ));
			
		}catch( Throwable e ){
			
			Debug.out( e );
			
			return( false );
		}
	}
		
	public String
	getXSearchService(
		String		query,
		boolean		to_subscribe )
	{
		return( getServiceURL( SERVICE_XSEARCH, new Object[]{ query, to_subscribe } ));
	}
	
	public String 
	getContentDetailsService(
		String 		hash, 
		String 		client_ref ) 
	{
		return( getServiceURL( SERVICE_CONTENT_DETAILS, new Object[]{ hash, client_ref }));
	}
	
	public String 
	getCommentService(
		String hash )
	{
		return( getServiceURL( SERVICE_COMMENT, new Object[]{ hash }));
	}
	
	public String 
	getProfileService(
		String 		login_id, 
		String 		client_ref ) 
	{
		return( getServiceURL( SERVICE_PROFILE, new Object[]{ login_id, client_ref }));
	}
	
	public String 
	getTorrentDownloadService(
		String 		hash, 
		String 		client_ref ) 
	{
		return( getServiceURL( SERVICE_TORRENT_DOWNLOAD, new Object[]{ hash, client_ref }));
	}
	
	public String 
	getFAQTopicService(
		String topic )
	{
		return( getServiceURL( SERVICE_FAQ_TOPIC, new Object[]{ topic }));
	}
	
	public String 
	getLoginService(
		String 	message )
	{
		return( getServiceURL( SERVICE_LOGIN, new Object[]{ message }));
	}
	
	public String 
	getSiteRelativeURL(
		String 		relative_url,
		boolean		append_suffix )
	{
		return( getServiceURL( SERVICE_SITE_RELATIVE, new Object[]{ relative_url, append_suffix }));
	}
	
	public String 
	getExternalSiteRelativeURL(
		String 		relative_url,
		boolean		append_suffix )
	{
		return( getServiceURL( SERVICE_EXT_SITE_RELATIVE, new Object[]{ relative_url, append_suffix }));
	}
	
	public String 
	getAddFriendURL(
		String 	colour )
	{
		return( getServiceURL( SERVICE_ADD_FRIEND, new Object[]{ colour }));
	}
	
	public String 
	getSubscriptionURL(
		String 	subs_id )
	{
		return( getServiceURL( SERVICE_SUBSCRIPTION, new Object[]{ subs_id }));
	}
	
	public VuzeFile
	getVuzeFile()
	{
		VuzeFile	vf = VuzeFileHandler.getSingleton().create();
		
		Map	map = new HashMap();
		
		try{
			exportToBEncodedMap( map );
		
			vf.addComponent( VuzeFileComponent.COMP_TYPE_CONTENT_NETWORK, map );
			
		}catch( Throwable e ){
			
			Debug.printStackTrace( e );
		}
		
		return( vf );
	}
	
	public boolean
	isStartupNetwork()
	{
		if ( hasPersistentProperty( PP_STARTUP_NETWORK )){
			
			return((Boolean)getPersistentProperty( PP_STARTUP_NETWORK ));
		}
		
		return((Boolean)getPersistentProperty( ContentNetwork.PP_IS_CUSTOMIZATION ));
	}
	
	public void
	setStartupNetwork(
		boolean		b )
	{
		setPersistentProperty( PP_STARTUP_NETWORK, b);
	}
	
	public void
	setTransientProperty(
		Object		key,
		Object		value )
	{
		transient_properties.put( key, value );
	}
	
	public Object
	getTransientProperty(
		Object		key )
	{
		return( transient_properties.get( key ));
	}
	
	protected String
	getPropertiesKey()
	{
		return( "cnetwork.net." + id + ".props" );
	}
	
	public void
	setPersistentProperty(
		String		name,
		Object		new_value )
	{
		synchronized( this ){
			
			String	key = getPropertiesKey();
			
			if ( new_value instanceof Boolean ){
				
				new_value = (long) (((Boolean) new_value) ? 1 : 0);
			}
			
			Map props = new HashMap( COConfigurationManager.getMapParameter( key , new HashMap()));
			
			Object old_value = props.get( key );
			
			if ( BEncoder.objectsAreIdentical( old_value, new_value )){
					
				return;
			}
			
			props.put( name, new_value );
			
			COConfigurationManager.setParameter( key, props );
		}

        for (Object persistent_listener : persistent_listeners) {

            try {

                ((ContentNetworkPropertyChangeListener) persistent_listener).propertyChanged(name);

            } catch (Throwable e) {

                Debug.printStackTrace(e);
            }
        }
	}
	
	public Object
	getPersistentProperty(
		String		name )
	{
		synchronized( this ){
			
			String	key = getPropertiesKey();
			
			Map props = COConfigurationManager.getMapParameter( key , new HashMap());
	
			if (PP_SOURCE_REF.equals(name)) {

				return MapUtils.getMapString(props, name, MapUtils.getMapString(
						pprop_defaults, name, null));
			}

			Object obj = props.get( name );
			
			if (PPSET.contains(name)){
				
				if ( obj == null && pprop_defaults != null ){
					
					obj = pprop_defaults.get( name );
				}
				
				if ( obj == null ){
					
					return( false );
					
				}else{
					
					return(((Long)obj)==1);
				}
			}
			
			return( obj );
		}
	}
	
	protected boolean
	hasPersistentProperty(
		String		name )
	{
		synchronized( this ){
			
			String	key = getPropertiesKey();
			
			Map props = COConfigurationManager.getMapParameter( key , new HashMap());
	
			return( props.containsKey( name ));
		}
	}
	
	protected Map<String,Object>
	getPersistentPropertyDefaults()
	{
		return( pprop_defaults );
	}
	
	public void
	addPersistentPropertyChangeListener(
		ContentNetworkPropertyChangeListener	listener )
	{
		persistent_listeners.add( listener );
	}
	
	public void
	removePersistentPropertyChangeListener(
		ContentNetworkPropertyChangeListener	listener )
	{
		persistent_listeners.remove( listener );
	}
	
	protected void
	destroy()
	{
		String	key = getPropertiesKey();

		COConfigurationManager.setParameter( key, new HashMap());
	}
	
	public void
	remove()
	{
		manager.removeNetwork( this );
	}
	
	protected void
	debug(
		String		str )
	{
		Debug.out( getString() + ": " + str );
	}
	
	protected String
	getString()
	{
		return( getID() + " - " + getName() + ": version=" + getVersion() + ", site=" + getProperty( PROPERTY_SITE_HOST ));
	}
}
