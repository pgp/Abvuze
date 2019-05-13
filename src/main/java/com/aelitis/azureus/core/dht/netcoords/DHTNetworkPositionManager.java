/*
 * Created on 24-Apr-2006
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

package com.aelitis.azureus.core.dht.netcoords;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.gudy.azureus2.core3.util.Debug;

import com.aelitis.azureus.core.dht.DHTStorageAdapter;
import com.aelitis.azureus.core.dht.impl.DHTLog;
import com.aelitis.azureus.core.util.CopyOnWriteList;

public class 
DHTNetworkPositionManager 
{
	private static DHTNetworkPositionProvider[]	providers = new DHTNetworkPositionProvider[0];
	private static final Object providers_lock = new Object();
	
	private static DHTStorageAdapter	storage_adapter = null;
	
	private static final CopyOnWriteList<DHTNetworkPositionProviderListener>		provider_listeners = new CopyOnWriteList<>();
	private static volatile CopyOnWriteList<DHTNetworkPositionListener>		position_listeners;
	
	private static final DHTNetworkPosition[] NP_EMPTY_ARRAY = {};
	
	public static void
	initialise(
		DHTStorageAdapter		adapter )
	{
		synchronized( providers_lock ){
			
			if ( storage_adapter == null ){
				
				storage_adapter	= adapter;

                for (DHTNetworkPositionProvider provider : providers) {

                    try {
                        startUp(provider);

                    } catch (Throwable e) {

                        Debug.printStackTrace(e);
                    }
                }
			}
		}
	}
	
	private static void
	startUp(
		DHTNetworkPositionProvider	provider )
	{
		byte[] data = null;
		
		if ( storage_adapter != null ){
			
			data = storage_adapter.getStorageForKey( "NPP:" + provider.getPositionType());
		}
		
		if ( data == null ){
			
			data = new byte[0];
		}
		
		try{
			provider.startUp( new DataInputStream( new ByteArrayInputStream( data )));
			
		}catch( Throwable e ){
			
			Debug.printStackTrace( e );
		}
	}
	
	private static void
	shutDown(
		DHTNetworkPositionProvider	provider )
	{
		try{			
			ByteArrayOutputStream	baos = new ByteArrayOutputStream();
			
			DataOutputStream	dos = new DataOutputStream( baos );
			
			provider.shutDown( dos );
			
			dos.flush();
			
			byte[]	data = baos.toByteArray();
			
			storage_adapter.setStorageForKey( "NPP:" + provider.getPositionType(), data );
			
		}catch( Throwable e ){
			
			Debug.printStackTrace( e );
		}
	}
	
	public static void
	destroy(
		DHTStorageAdapter		adapter )
	{
		synchronized( providers_lock ){
			
			if ( storage_adapter == adapter ){

                for (DHTNetworkPositionProvider provider : providers) {

                    shutDown(provider);
                }
				
				storage_adapter	= null;
			}
		}
	}
	
	public static DHTNetworkPositionProviderInstance
	registerProvider(
		final DHTNetworkPositionProvider	provider )
	{
		boolean	fire_added = false;
		
		synchronized( providers_lock ){
	
			boolean						found 		= false;
			DHTNetworkPositionProvider	type_found	= null;
			
			for ( DHTNetworkPositionProvider p: providers ){
				
				if ( p == provider ){
					
					found = true;
					
					break;
					
				}else if ( p.getPositionType() == provider.getPositionType()){
					
					type_found = p;
				}
			}
			
			if ( !found ){
				
				if ( type_found != null ){
					
					Debug.out( "Registration of " + provider + " found previous provider for same position type, removing it" );
					
					unregisterProviderSupport( type_found );
				}
				
				DHTNetworkPositionProvider[]	new_providers = new DHTNetworkPositionProvider[providers.length + 1 ];
				
				System.arraycopy( providers, 0, new_providers, 0, providers.length );
				
				new_providers[providers.length] = provider;
				
				providers	= new_providers;
				
				if ( storage_adapter != null ){
				
					startUp( provider );
				}
				
				fire_added = true;
			}
		}
		
		if ( fire_added ){
			
			for ( DHTNetworkPositionProviderListener l: provider_listeners ){
				
				try{
					
					l.providerAdded( provider );
					
				}catch( Throwable e ){
					
					Debug.out( e );
				}
			}
		}
		
		return( new DHTNetworkPositionProviderInstance()
				{	
					public void
					log(
						String		log )
					{
						DHTLog.log("NetPos " + provider.getPositionType() + ": " + log );
					}
				});
	}
	
	public static void
	unregisterProvider(
		DHTNetworkPositionProvider	provider )
	{
		if ( unregisterProviderSupport( provider )){
			
			for ( DHTNetworkPositionProviderListener l: provider_listeners ){
				
				try{
					
					l.providerRemoved( provider );
					
				}catch( Throwable e ){
					
					Debug.out( e );
				}
			}
		}
	}
	
	private static boolean
	unregisterProviderSupport(
		DHTNetworkPositionProvider	provider )
	{
		boolean	removed = false;
		
		synchronized( providers_lock ){
	
			if ( providers.length == 0 ){
				
				return( false );
			}
			
			DHTNetworkPositionProvider[]	new_providers = new DHTNetworkPositionProvider[providers.length - 1 ];

			int	pos = 0;

            for (DHTNetworkPositionProvider provider1 : providers) {

                if (provider1 == provider) {

                    if (storage_adapter != null) {

                        shutDown(provider);
                    }
                } else {

                    new_providers[pos++] = provider1;
                }
            }
			if ( pos == new_providers.length ){
			
				providers = new_providers;
				
				removed = true;
			}
		}

		return( removed );
	}
	
	public static DHTNetworkPositionProvider
	getProvider(
		byte		type )
	{
		synchronized( providers_lock ){

            for (DHTNetworkPositionProvider provider : providers) {

                if (provider.getPositionType() == type) {

                    return (provider);
                }
            }
		}
		
		return( null );
	}
	
	public static DHTNetworkPosition[]
	getLocalPositions()
	{
		DHTNetworkPositionProvider[]	prov = providers;
		
		List<DHTNetworkPosition> res = new ArrayList<>();

        for (DHTNetworkPositionProvider dhtNetworkPositionProvider : prov) {

            try {
                DHTNetworkPosition pos = dhtNetworkPositionProvider.getLocalPosition();

                if (pos != null) {

                    res.add(pos);
                }
            } catch (Throwable e) {

                Debug.printStackTrace(e);
            }
        }
		
		return( res.toArray(new DHTNetworkPosition[0]));

	}
	
	public static DHTNetworkPosition
	getBestLocalPosition()
	{
		DHTNetworkPosition	best_position = null;
		
		DHTNetworkPosition[]	positions = getLocalPositions();
		
		byte	best_provider = DHTNetworkPosition.POSITION_TYPE_NONE;

        for (DHTNetworkPosition position : positions) {

            int type = position.getPositionType();

            if (type > best_provider) {

                best_position = position;
            }
        }
		
		return( best_position );
	}
	
	public static DHTNetworkPosition[]
	createPositions(
		byte[]		ID,
		boolean		is_local )
	{
		DHTNetworkPositionProvider[]	prov = providers;
		
		if ( prov.length == 0 ){
		
			return( NP_EMPTY_ARRAY );
			
		}else{
			DHTNetworkPosition[]	res = new DHTNetworkPosition[prov.length];
			
			int	skipped	= 0;
			
			for (int i=0;i<res.length;i++){
				
				try{
					res[i] = prov[i].create( ID, is_local );
					
				}catch( Throwable e ){
					
					Debug.printStackTrace(e);
					
					skipped++;
				}
			}
			
			if  ( skipped > 0 ){
				
				DHTNetworkPosition[] x	= new DHTNetworkPosition[ res.length - skipped ];
				
				int	pos = 0;

                for (DHTNetworkPosition re : res) {

                    if (re != null) {

                        x[pos++] = re;
                    }
                }
				
				res	= x;
				
				if ( res.length == 0 ){
					
					Debug.out( "hmm" );
				}
			}
			
			return( res );
		}
	}
	
	public static float
	estimateRTT(
		DHTNetworkPosition[]		p1s,
		DHTNetworkPosition[]		p2s )
	{
		byte	best_provider = DHTNetworkPosition.POSITION_TYPE_NONE;
		
		float	best_result	= Float.NaN;

        for (DHTNetworkPosition p1 : p1s) {

            byte p1_type = p1.getPositionType();

            for (DHTNetworkPosition p2 : p2s) {

                if (p1_type == p2.getPositionType()) {

                    try {
                        float f = p1.estimateRTT(p2);

                        if (!Float.isNaN(f)) {

                            if (p1_type > best_provider) {

                                best_result = f;
                                best_provider = p1_type;
                            }
                        }
                    } catch (Throwable e) {

                        Debug.printStackTrace(e);
                    }

                    break;
                }
            }
        }
		
		return( best_result );
	}
	
	public static void
	update(
		DHTNetworkPosition[]	local_positions,
		byte[]					remote_id,
		DHTNetworkPosition[]	remote_positions,
		float					rtt )
	{
        for (DHTNetworkPosition p1 : local_positions) {

            for (DHTNetworkPosition p2 : remote_positions) {

                if (p1.getPositionType() == p2.getPositionType()) {

                    try {
                        p1.update(remote_id, p2, rtt);

                    } catch (Throwable e) {

                        Debug.printStackTrace(e);
                    }

                    break;
                }
            }
        }
	}
	
	public static byte[]
	serialisePosition(
		DHTNetworkPosition	pos )
	
		throws IOException
	{
		ByteArrayOutputStream	baos = new ByteArrayOutputStream();
		
		DataOutputStream	dos = new DataOutputStream( baos );
	
		dos.writeByte( 1 );	// version
		dos.writeByte( pos.getPositionType());
		
		pos.serialise( dos );
		
		dos.close();
		
		return( baos.toByteArray());
	}
	
	public static DHTNetworkPosition
   	deserialisePosition(
   		InetAddress				originator,
   		byte[]					bytes )
   	
   		throws IOException
   	{
   		ByteArrayInputStream	bais = new ByteArrayInputStream( bytes );
   		
   		DataInputStream	dis = new DataInputStream( bais );
   	
   		dis.readByte();	// version
   		
   		byte	position_type = dis.readByte();
   		
   		return( deserialise( originator, position_type, dis ));
   	}
	
	public static DHTNetworkPosition
	deserialise(
		InetAddress				originator,
		byte					position_type,
		DataInputStream			is )
	
		throws IOException
	{
		DHTNetworkPositionProvider[]	prov = providers;

		is.mark( 512 );

        for (DHTNetworkPositionProvider dhtNetworkPositionProvider : prov) {

            if (dhtNetworkPositionProvider.getPositionType() == position_type) {

                DHTNetworkPositionProvider provider = dhtNetworkPositionProvider;

                try {
                    DHTNetworkPosition np = provider.deserialisePosition(is);

                    CopyOnWriteList<DHTNetworkPositionListener> listeners = position_listeners;

                    if (listeners != null) {

                        for (DHTNetworkPositionListener listener : listeners) {

                            try {
                                listener.positionFound(provider, originator, np);

                            } catch (Throwable e) {

                                Debug.printStackTrace(e);
                            }
                        }
                    }

                    return (np);

                } catch (Throwable e) {

                    Debug.printStackTrace(e);

                    is.reset();
                }

                break;
            }
        }
		
		return( null );
	}
	
	public static void
	addPositionListener(
		DHTNetworkPositionListener		listener )
	{
		synchronized( DHTNetworkPositionManager.class ){
		
			if ( position_listeners == null ){
				
				position_listeners = new CopyOnWriteList<>();
			}
			
			position_listeners.add( listener );
		}
	}
	
	public static void
	removePositionListener(
		DHTNetworkPositionListener		listener )
	{
		synchronized( DHTNetworkPositionManager.class ){
		
			if ( position_listeners != null ){
				
				position_listeners.remove( listener );
				
				if ( position_listeners.size() == 0 ){
					
					position_listeners = null;
				}
			}
		}
	}
	
	public static void
	addProviderListener(
		DHTNetworkPositionProviderListener		listener )
	{
		provider_listeners.add( listener );
	}
	
	public static void
	removeProviderListener(
		DHTNetworkPositionProviderListener		listener )
	{
		provider_listeners.remove( listener );
	}
}