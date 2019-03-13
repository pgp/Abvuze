/*
 * Created on 14-Feb-2005
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

package org.gudy.azureus2.core3.tracker.client.impl;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.internat.MessageText;
import org.gudy.azureus2.core3.logging.*;
import org.gudy.azureus2.core3.peer.PEPeerSource;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.tracker.client.TRTrackerAnnouncer;
import org.gudy.azureus2.core3.tracker.client.TRTrackerAnnouncerException;
import org.gudy.azureus2.core3.tracker.client.TRTrackerAnnouncerListener;
import org.gudy.azureus2.core3.tracker.client.TRTrackerAnnouncerResponse;
import org.gudy.azureus2.core3.tracker.client.TRTrackerAnnouncerResponsePeer;
import org.gudy.azureus2.core3.util.AEMonitor;
import org.gudy.azureus2.core3.util.Constants;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.LightHashMap;
import org.gudy.azureus2.core3.util.ListenerManager;
import org.gudy.azureus2.core3.util.ListenerManagerDispatcher;
import org.gudy.azureus2.core3.util.RandomUtils;
import org.gudy.azureus2.plugins.clientid.ClientIDException;
import org.gudy.azureus2.plugins.download.DownloadAnnounceResultPeer;
import org.gudy.azureus2.pluginsimpl.local.clientid.ClientIDManagerImpl;

import com.aelitis.azureus.core.tracker.TrackerPeerSource;
import com.aelitis.azureus.core.tracker.TrackerPeerSourceAdapter;

/**
 * @author parg
 *
 */

public abstract class 
TRTrackerAnnouncerImpl
	implements TRTrackerAnnouncer
{
  // Used to be componentID 2
	public final static LogIDs LOGID = LogIDs.TRACKER;

	// 	listener
	
	protected static final int LDT_TRACKER_RESPONSE		= 1;
	protected static final int LDT_URL_CHANGED			= 2;
	protected static final int LDT_URL_REFRESH			= 3;
	
	private static final String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	private static final int	   	key_id_length	= 8;

	private static String
	createKeyID()
	{
		String	key_id = "";
		
		for (int i = 0; i < key_id_length; i++) {
			int pos = RandomUtils.nextInt( chars.length());
		    key_id +=  chars.charAt(pos);
		}
		
		return( key_id );
	}
	
	protected final ListenerManager<TRTrackerAnnouncerListener>	listeners 	= ListenerManager.createManager(
			"TrackerClient:ListenDispatcher",
			new ListenerManagerDispatcher<TRTrackerAnnouncerListener>()
			{
				public void
				dispatch(
					TRTrackerAnnouncerListener		listener,
					int								type,
					Object							value )
				{
					if ( type == LDT_TRACKER_RESPONSE ){
						
						listener.receivedTrackerResponse((TRTrackerAnnouncerResponse)value);
						
					}else if ( type == LDT_URL_CHANGED ){
						
						Object[]	x = (Object[])value;
						
						URL			old_url 	= (URL)x[0];
						URL			new_url 	= (URL)x[1];
						boolean		explicit	= (Boolean) x[2];
						
						listener.urlChanged( TRTrackerAnnouncerImpl.this, old_url, new_url, explicit );
						
					}else{
						
						listener.urlRefresh();
					}
				}
			});

	final Map	tracker_peer_cache		= new LinkedHashMap();	// insertion order - most recent at end
	private final AEMonitor tracker_peer_cache_mon 	= new AEMonitor( "TRTrackerClientClassic:PC" );
	private int	cache_peers_used;
	
	final private TOTorrent						torrent;
	final private byte[]						peer_id;
	final private String						tracker_key;
	final private int							udp_key;
		
	
	protected
	TRTrackerAnnouncerImpl(
		TOTorrent	_torrent )
	
		throws TRTrackerAnnouncerException
	{
		torrent	= _torrent;
		
		tracker_key	= createKeyID();
	    
		udp_key	= RandomUtils.nextInt();
		
		try{
			byte[] 	hash = null;
			
			try{
				hash = torrent.getHash();
				
			}catch( Throwable e ){	
			}
			
			peer_id		= ClientIDManagerImpl.getSingleton().generatePeerID( hash, false );
		
		}catch( ClientIDException e ){

			 throw( new TRTrackerAnnouncerException( "TRTrackerAnnouncer: Peer ID generation fails", e ));
		}
	}

	public TOTorrent
	getTorrent()
	{
		return( torrent );
	}
	
	public Helper
	getHelper()
	{
		return(
			new Helper()
			{
				public byte[]
				getPeerID()
				{
					return( peer_id );
				}
				
				public String
				getTrackerKey()
				{
					return( tracker_key );
				}
				
				public int
				getUDPKey()
				{
					return( udp_key );
				}
				
				public void
				addToTrackerCache(
					TRTrackerAnnouncerResponsePeerImpl[]		peers )
				{
					TRTrackerAnnouncerImpl.this.addToTrackerCache( peers );
				}

				public TRTrackerAnnouncerResponsePeer[]
		      	getPeersFromCache(
		      		int			num_want )
				{
					return( TRTrackerAnnouncerImpl.this.getPeersFromCache(num_want));
				}
				
				public void
				setTrackerResponseCache(
					Map 		map	)
				{
					TRTrackerAnnouncerImpl.this.setTrackerResponseCache( map );
				}
					
				public void 
				removeFromTrackerResponseCache(
					String ip, int tcpPort )
				{
					TRTrackerAnnouncerImpl.this.removeFromTrackerResponseCache( ip,tcpPort );
				}
					
				public Map 
				getTrackerResponseCache()
				{
					return( TRTrackerAnnouncerImpl.this.getTrackerResponseCache());
				}
					
				public void
				informResponse(
					TRTrackerAnnouncerHelper		helper,
					TRTrackerAnnouncerResponse		response )
				{
					TRTrackerAnnouncerImpl.this.informResponse( helper, response );
				}
				
				public void
				informURLChange(
					URL		old_url,
					URL		new_url,
					boolean	explicit )
				{
					listeners.dispatch(	LDT_URL_CHANGED,
							new Object[]{old_url, new_url, explicit});
				}
				
				public void
				informURLRefresh()
				{
					TRTrackerAnnouncerImpl.this.informURLRefresh();
				}
				
			 	public void
				addListener(
					TRTrackerAnnouncerListener	l )
			 	{
			 		TRTrackerAnnouncerImpl.this.addListener( l );
			 	}
					
				public void
				removeListener(
					TRTrackerAnnouncerListener	l )
				{
					TRTrackerAnnouncerImpl.this.removeListener( l );
				}
			});
	}
	
	public byte[]
	getPeerId()
	{
		return( peer_id );
	}
	
 	public static byte[]
	getAnonymousPeerId(
		String	my_ip,
		int		my_port )
	{
  		byte[] anon_peer_id = new byte[20];
	
  		// unique initial two bytes to identify this as fake

  		anon_peer_id[0] = (byte)'[';
  		anon_peer_id[1] = (byte)']';

  		try{
	  		byte[]	ip_bytes 	= my_ip.getBytes( Constants.DEFAULT_ENCODING );
	  		int		ip_len		= ip_bytes.length;
	
	  		if ( ip_len > 18 ){
		
	  			ip_len = 18;
	  		}
	
	  		System.arraycopy( ip_bytes, 0, anon_peer_id, 2, ip_len );
									
	  		int	port_copy = my_port;
		
	  		for (int j=2+ip_len;j<20;j++){
			
	  			anon_peer_id[j] = (byte)(port_copy&0xff);
			
	  			port_copy >>= 8;
	  		}
  		}catch( UnsupportedEncodingException e ){
  			
  			Debug.printStackTrace( e );
  		}
  		
  		return( anon_peer_id );
   }
					
		// NOTE: tracker_cache is cleared out in DownloadManager when opening a torrent for the
		// first time as a DOS prevention measure
	
	public Map
	getTrackerResponseCache()
	{				
		return( exportTrackerCache());
	}
	
	
	public void
	setTrackerResponseCache(
		Map		map )
	{
		int	num = importTrackerCache( map );
		
		if (Logger.isEnabled())
			Logger.log(new LogEvent(getTorrent(), LOGID, "TRTrackerClient: imported "
					+ num + " cached peers"));
	}
	
	protected Map
	exportTrackerCache()
	{
		Map	res = new LightHashMap(1);
		
		List	peers = new ArrayList();
		
		res.put( "tracker_peers", peers );
		
		try{
			tracker_peer_cache_mon.enter();

			for (Object o : tracker_peer_cache.values()) {

				TRTrackerAnnouncerResponsePeer peer = (TRTrackerAnnouncerResponsePeer) o;

				LightHashMap entry = new LightHashMap();

				entry.put("ip", peer.getAddress().getBytes());
				entry.put("src", peer.getSource().getBytes());
				entry.put("port", (long) peer.getPort());

				int udp_port = peer.getUDPPort();
				if (udp_port != 0) {
					entry.put("udpport", (long) udp_port);
				}
				int http_port = peer.getHTTPPort();
				if (http_port != 0) {
					entry.put("httpport", (long) http_port);
				}

				entry.put("prot", (long) peer.getProtocol());

				byte az_ver = peer.getAZVersion();

				if (az_ver != TRTrackerAnnouncer.AZ_TRACKER_VERSION_1) {
					entry.put("azver", (long) az_ver);
				}

				entry.compactify(0.9f);

				peers.add(entry);
			}
		
			if (Logger.isEnabled())
				Logger.log(new LogEvent(getTorrent(), LOGID,
						"TRTrackerClient: exported " + tracker_peer_cache.size()
								+ " cached peers"));
		}finally{
			
			tracker_peer_cache_mon.exit();
		}
		
		return( res );
	}
	
	protected int
	importTrackerCache(
		Map		map )
	{
		if ( !COConfigurationManager.getBooleanParameter("File.save.peers.enable")){
			
			return( 0 );
		}
		
		try{
			if ( map == null ){
				
				return( 0 );
			}
			
			List	peers = (List)map.get( "tracker_peers" );
	
			if ( peers == null ){
				
				return( 0 );
			}
			
			try{
				tracker_peer_cache_mon.enter();

				for (Object peer1 : peers) {

					Map peer = (Map) peer1;

					byte[] src_bytes = (byte[]) peer.get("src");
					String peer_source = src_bytes == null ? PEPeerSource.PS_BT_TRACKER : new String(src_bytes);
					String peer_ip_address = new String((byte[]) peer.get("ip"));
					int peer_tcp_port = ((Long) peer.get("port")).intValue();
					byte[] peer_peer_id = getAnonymousPeerId(peer_ip_address, peer_tcp_port);
					Long l_protocol = (Long) peer.get("prot");
					short protocol = l_protocol == null ? DownloadAnnounceResultPeer.PROTOCOL_NORMAL : l_protocol.shortValue();
					Long l_udp_port = (Long) peer.get("udpport");
					int peer_udp_port = l_udp_port == null ? 0 : l_udp_port.intValue();
					Long l_http_port = (Long) peer.get("httpport");
					int peer_http_port = l_http_port == null ? 0 : l_http_port.intValue();
					Long l_az_ver = (Long) peer.get("azver");
					byte az_ver = l_az_ver == null ? TRTrackerAnnouncer.AZ_TRACKER_VERSION_1 : l_az_ver.byteValue();

					//System.out.println( "recovered " + ip_address + ":" + port );

					TRTrackerAnnouncerResponsePeerImpl entry =
							new TRTrackerAnnouncerResponsePeerImpl(
									peer_source,
									peer_peer_id,
									peer_ip_address,
									peer_tcp_port,
									peer_udp_port,
									peer_http_port,
									protocol,
									az_ver,
									(short) 0);

					tracker_peer_cache.put(entry.getKey(), entry);
				}
				
				return( tracker_peer_cache.size());
				
			}finally{
				
				tracker_peer_cache_mon.exit();
			}
		}catch( Throwable e ){
			
			Debug.printStackTrace( e );
			
			return( tracker_peer_cache.size());
		}
	}
	
	protected void
	addToTrackerCache(
		TRTrackerAnnouncerResponsePeerImpl[]		peers )
	{
		if ( !COConfigurationManager.getBooleanParameter("File.save.peers.enable")){
			
			return;
		}
		
		int	max = COConfigurationManager.getIntParameter( "File.save.peers.max", DEFAULT_PEERS_TO_CACHE );
		
		// System.out.println( "max peers= " + max );
		
		try{
			tracker_peer_cache_mon.enter();

			for (TRTrackerAnnouncerResponsePeerImpl peer : peers) {

				// remove and reinsert to maintain most recent last

				tracker_peer_cache.remove(peer.getKey());

				tracker_peer_cache.put(peer.getKey(), peer);
			}
			
			Iterator	it = tracker_peer_cache.keySet().iterator();
			
			if ( max > 0 ){
					
				while ( tracker_peer_cache.size() > max ){
						
					it.next();
					
					it.remove();
				}
			}
		}finally{
			
			tracker_peer_cache_mon.exit();
		}
	}
	
	public void
	removeFromTrackerResponseCache(
		String		ip,
		int			tcp_port )
	{
		try{
			tracker_peer_cache_mon.enter();
		
				// create a fake peer so we can get the key
			
			TRTrackerAnnouncerResponsePeerImpl peer = 
				new TRTrackerAnnouncerResponsePeerImpl( "", new byte[0], ip, tcp_port, 0, 0, (short)0, (byte)0, (short)0 );
			
			if ( tracker_peer_cache.remove( peer.getKey()) != null ){
				
				if (Logger.isEnabled())
					Logger.log(new LogEvent( getTorrent(), LOGID, "Explicit removal of peer cache for " + ip + ":" + tcp_port ));
			}
			
		}finally{
			
			tracker_peer_cache_mon.exit();
		}
	}
	
	public static Map
	mergeResponseCache(
		Map		map1,
		Map		map2 )
	{
		if ( map1 == null && map2 == null ){
			return( new HashMap());
		}else if ( map1 == null ){
			return( map2 );
		}else if ( map2 == null ){
			return( map1 );
		}
		
		Map	res = new HashMap();
				
		List	peers = (List)map1.get( "tracker_peers" );
		
		if ( peers == null ){
			
			peers = new ArrayList();
		}
		
		List	p2 = (List)map2.get( "tracker_peers" );
		
		if ( p2 != null ){
			
			if (Logger.isEnabled())
				Logger.log(new LogEvent(LOGID,
						"TRTrackerClient: merged peer sets: p1 = " + peers.size()
								+ ", p2 = " + p2.size()));

			peers.addAll(p2);
		}
		
		res.put( "tracker_peers", peers );
		
		return( res );
	}
	
	protected abstract int
	getPeerCacheLimit();
	
	protected TRTrackerAnnouncerResponsePeer[]
	getPeersFromCache(
		int	num_want )
	{
		int	limit = getPeerCacheLimit();
		
		if ( limit <= 0 ){
			
			return( new TRTrackerAnnouncerResponsePeer[0] );
		}
		
			// limit peers returned to avoid multi-tracker torrents from getting swamped
			// by out-of-date peers from a failed tracker
		
		num_want = Math.min( limit, num_want );
		
		try{
			tracker_peer_cache_mon.enter();
	
			TRTrackerAnnouncerResponsePeerImpl[]	res;
			
			if ( tracker_peer_cache.size() <= num_want ){
				
				res = new TRTrackerAnnouncerResponsePeerImpl[tracker_peer_cache.size()];
				
				tracker_peer_cache.values().toArray( res );
				
			}else{
			
				res = new TRTrackerAnnouncerResponsePeerImpl[num_want];
				
				Iterator	it = tracker_peer_cache.keySet().iterator();
				
					// take 'em out and put them back in so we cycle through the peers
					// over time
				
				for (int i=0;i<num_want;i++){
					
					String	key = (String)it.next();
					
					res[i] = (TRTrackerAnnouncerResponsePeerImpl)tracker_peer_cache.get(key);
				
					it.remove();
				}
				
				for (int i=0;i<num_want;i++){
					
					tracker_peer_cache.put( res[i].getKey(), res[i] );
				}
			}
			
			if (Logger.isEnabled()){

				for (TRTrackerAnnouncerResponsePeerImpl re : res) {

					Logger.log(new LogEvent(getTorrent(), LOGID, "CACHED PEER: " + re.getString()));
				}
			
				Logger.log(new LogEvent(getTorrent(), LOGID,
						"TRTrackerClient: returned " + res.length + " cached peers"));
			}
		    
			cache_peers_used += res.length;
			
			return( res );
			
		}finally{
			
			tracker_peer_cache_mon.exit();
		}
	} 
	
	public TrackerPeerSource 
	getCacheTrackerPeerSource()
	{
		return(
			new TrackerPeerSourceAdapter()
			{
				public String
				getName()
				{
					return( MessageText.getString( "tps.tracker.cache1", new String[]{ String.valueOf( cache_peers_used )}));
				}
				
				public int
				getPeers()
				{
					return( tracker_peer_cache.size() );
				}
			});
	}

	protected void
	informResponse(
		TRTrackerAnnouncerHelper		helper,
		TRTrackerAnnouncerResponse		response )
	{
		listeners.dispatch( LDT_TRACKER_RESPONSE, response );
	}

	protected void
	informURLRefresh()
	{
		listeners.dispatch( LDT_URL_REFRESH, null );
	}
	
 	public void
	addListener(
		TRTrackerAnnouncerListener	l )
	{
		listeners.addListener( l );
	}
		
	public void
	removeListener(
		TRTrackerAnnouncerListener	l )
	{
		listeners.removeListener(l);
	}

	public interface
	Helper
	{
		byte[]
		getPeerID();
		
		String
		getTrackerKey();
		
		int
		getUDPKey();
		
		void
		addToTrackerCache(
				TRTrackerAnnouncerResponsePeerImpl[] peers);

		TRTrackerAnnouncerResponsePeer[]
      	getPeersFromCache(
				int num_want);
		
		void
		setTrackerResponseCache(
				Map map);
		
		void
		removeFromTrackerResponseCache(
				String ip, int tcpPort);
		
		Map
		getTrackerResponseCache();
		
		void
		informResponse(
				TRTrackerAnnouncerHelper helper,
				TRTrackerAnnouncerResponse response);
		
		void
		informURLChange(
				URL old_url,
				URL new_url,
				boolean explicit);
		
		void
		informURLRefresh();
		
	 	void
		addListener(
				TRTrackerAnnouncerListener l);
			
		void
		removeListener(
				TRTrackerAnnouncerListener l);
	}
}
