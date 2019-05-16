/*
 * File    : PluginPEPeerWrapper.java
 * Created : 01-Dec-2003
 * By      : parg
 * 
 * Azureus - a Java Bittorrent client
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details ( see the LICENSE file ).
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.gudy.azureus2.pluginsimpl.local.peers;

/**
 * @author parg
 *
 */

import java.util.*;

import org.gudy.azureus2.core3.logging.LogRelation;
import org.gudy.azureus2.core3.peer.*;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.plugins.messaging.Message;
import org.gudy.azureus2.plugins.network.Connection;
import org.gudy.azureus2.plugins.network.ConnectionStub;
import org.gudy.azureus2.plugins.network.RateLimiter;
import org.gudy.azureus2.plugins.peers.*;
import org.gudy.azureus2.pluginsimpl.local.messaging.MessageAdapter;
import org.gudy.azureus2.pluginsimpl.local.utils.UtilitiesImpl;
import org.gudy.azureus2.pluginsimpl.local.utils.UtilitiesImpl.PluginLimitedRateGroup;

import com.aelitis.azureus.core.networkmanager.LimitedRateGroup;
import com.aelitis.azureus.core.peermanager.piecepicker.util.BitFlags;


public class 
PeerImpl 
	extends LogRelation
	implements Peer
{
	protected PeerManagerImpl	manager;
	protected PEPeer			delegate;
  	
	private Map<Object,PEPeerListener> peer_listeners;
  	
	private UtilitiesImpl.PluginLimitedRateGroupListener	up_rg_listener;
	private UtilitiesImpl.PluginLimitedRateGroupListener	down_rg_listener;
					
	private volatile boolean closed;
	
		/**
		 * don't use me, use PeerManagerImpl.getPeerForPEPeer
		 * @param _delegate
		 */
	
	protected
	PeerImpl(
		PEPeer	_delegate )
	{
		delegate	= _delegate;
		
		manager = PeerManagerImpl.getPeerManager( delegate.getManager());
	}

	public void
	bindConnection(
		ConnectionStub		stub )
	{
	}
	
	public PeerManager
	getManager()
	{
		return( manager );
	}
	
	public PEPeer
	getDelegate()
	{
		return( delegate );
	}
  
  public Connection getConnection() {
    return delegate.getPluginConnection();
  }
  
  
  public boolean supportsMessaging() {
    return delegate.supportsMessaging();
  }
  
  
  public Message[] getSupportedMessages() {
    com.aelitis.azureus.core.peermanager.messaging.Message[] core_msgs = delegate.getSupportedMessages();
    
    Message[] plug_msgs = new Message[ core_msgs.length ];
    
    for( int i=0; i < core_msgs.length; i++ ) {
      plug_msgs[i] = new MessageAdapter( core_msgs[i] );
    }
    
    return plug_msgs;
  }
  
  
  
	public int 
	getState()
	{
		int	state = delegate.getPeerState();
		
		switch( state ){
			
			case PEPeer.CONNECTING:
			{
				return( Peer.CONNECTING );
			}
			case PEPeer.DISCONNECTED:
			{
				return( Peer.DISCONNECTED );
			}
			case PEPeer.HANDSHAKING:
			{
				return( Peer.HANDSHAKING );
			}
			case PEPeer.TRANSFERING:
			{
				return( Peer.TRANSFERING );
			}
		}
		
		return( -1 );
	}

	public byte[] getId()
	{
			// we *really* don't want a plugin to accidentally change our peerid (e.g. the Stuffer plugin did this)
			// as this screws stuff up bigtime
		
		byte[]	id = delegate.getId();
		
		if ( id == null ){
			
			return( new byte[0] );
		}
		
		byte[]	copy = new byte[id.length];
		
		System.arraycopy( id, 0, copy, 0, copy.length );
		
		return( copy );
	}

	public String getIp()
	{
		return( delegate.getIp());
	}
 
	public int getPort()
	{
		return( delegate.getPort());
	}
  
  public int getTCPListenPort() {  return delegate.getTCPListenPort();  }
  public int getUDPListenPort() {  return delegate.getUDPListenPort();  }
  public int getUDPNonDataListenPort() { return delegate.getUDPNonDataListenPort(); }
	
  @Override
  public boolean isLANLocal() {
	  return( delegate.isLANLocal());
  }
	public final boolean[] getAvailable()
	{
		BitFlags bf = delegate.getAvailable();
		if ( bf == null ){
			return( null );
		}
		return( bf.flags );
	}
   
	public boolean isPieceAvailable(int pieceNumber)
	{
		return delegate.isPieceAvailable(pieceNumber);
	}
   
	public boolean
	isTransferAvailable()
	{
		return( delegate.transferAvailable());
	}
	
	public boolean isDownloadPossible()
	{
		return delegate.isDownloadPossible();
	}
	
	public boolean isChoked()
	{
		return( delegate.isChokingMe());
	}

	public boolean isChoking()
	{
		return( delegate.isChokedByMe());
	}

	public boolean isInterested()
	{
		return( delegate.isInteresting());
	}

	public boolean isInteresting()
	{
		return( delegate.isInterested());
	}

	public boolean isSeed()
	{
		return( delegate.isSeed());
	}
 
	public boolean isSnubbed()
	{
		return( delegate.isSnubbed());
	}
 
	public long getSnubbedTime()
	{
		return delegate.getSnubbedTime();
	}
	
	public void
	setSnubbed(
		boolean	b )
	{
		delegate.setSnubbed(b);
	}
	
	public PeerStats getStats()
	{
		return( new PeerStatsImpl(manager, this, delegate.getStats()));
	}
 	

	public boolean isIncoming()
	{
		return( delegate.isIncoming());
	}

	public int getPercentDone()
	{
		return( delegate.getPercentDoneInThousandNotation());
	}

	public int getOutgoingRequestCount() {
		return( delegate.getOutgoingRequestCount());
	}
	
	public int[] 
	getOutgoingRequestedPieceNumbers(){
		return( delegate.getOutgoingRequestedPieceNumbers());
	}
	
	public int getPercentDoneInThousandNotation()
	{
		return( delegate.getPercentDoneInThousandNotation());
	}
	
	public String getClient()
	{
		return( delegate.getClient());
	}

	public boolean isOptimisticUnchoke()
	{
		return( delegate.isOptimisticUnchoke());
	}
	
	public void setOptimisticUnchoke( boolean is_optimistic ) {
		delegate.setOptimisticUnchoke( is_optimistic );
	}
	
	public void
	initialize()
	{
		throw( new RuntimeException( "not supported"));
	}
	
	public List
	getExpiredRequests()
	{
		throw( new RuntimeException( "not supported"));
	}	
  		
	public List
	getRequests()
	{
		throw( new RuntimeException( "not supported"));
	}
	
	public int
	getNumberOfRequests()
	{
		throw( new RuntimeException( "not supported"));
	}

	public int
	getMaximumNumberOfRequests()
	{
		throw( new RuntimeException( "not supported"));
	}

	public int[]
	getPriorityOffsets()
	{
		throw( new RuntimeException( "not supported"));
	}
	
	public boolean
	requestAllocationStarts(
		int[]	base_priorities )
	{
		throw( new RuntimeException( "not supported"));
	}
	
	public void
	requestAllocationComplete()
	{
		throw( new RuntimeException( "not supported"));		
	}
	
	public void
	cancelRequest(
		PeerReadRequest	request )
	{
		throw( new RuntimeException( "not supported"));
	}

 
	public boolean 
	addRequest(
		PeerReadRequest	request )
	{
		throw( new RuntimeException( "not supported"));
	}

	private void
	createRGListeners()
	{
		up_rg_listener = 
				new UtilitiesImpl.PluginLimitedRateGroupListener()
				{
					public void
					disabledChanged(
						PluginLimitedRateGroup		group,
						boolean						is_disabled )
					{
						if ( closed ){
							
							group.removeListener( this );					
						}
							
						delegate.setUploadDisabled( group, is_disabled );
					}
					
					public void
					sync(
						PluginLimitedRateGroup		group,
						boolean						is_disabled )
					{
						if ( closed ){
							
							group.removeListener( this );					
						}
					}
				};

		down_rg_listener = 
				new UtilitiesImpl.PluginLimitedRateGroupListener()
				{
					public void
					disabledChanged(
						PluginLimitedRateGroup		group,
						boolean						is_disabled )
					{
						if ( closed ){
							
							group.removeListener( this );
						}	
					
						delegate.setDownloadDisabled( group, is_disabled );
					}
					
					public void
					sync(
						PluginLimitedRateGroup		group,
						boolean						is_disabled )
					{
						if ( closed ){
							
							group.removeListener( this );					
						}
					}
				};	
	}
	public void
	addRateLimiter(
	  RateLimiter		limiter,
	  boolean			is_upload )
	{
		synchronized( this ){

			if ( closed ){
				
				return;
			}
			
			PluginLimitedRateGroup wrapped_limiter = UtilitiesImpl.wrapLimiter( limiter, true );
	
			if ( up_rg_listener == null ){
				
				createRGListeners();
			}
			
			if ( is_upload ){
				
				wrapped_limiter.addListener( up_rg_listener );
				
			}else{
				
				wrapped_limiter.addListener( down_rg_listener );
			}
			
			delegate.addRateLimiter( wrapped_limiter, is_upload );
		}
	}

	public void
	removeRateLimiter(
	  RateLimiter		limiter,
	  boolean			is_upload )
	{
		synchronized( this ){

			PluginLimitedRateGroup wrapped_limiter = UtilitiesImpl.wrapLimiter( limiter, true );
	
			if ( up_rg_listener != null ){
							
				if ( is_upload ){
					
					wrapped_limiter.removeListener( up_rg_listener );
					
				}else{
					
					wrapped_limiter.removeListener( down_rg_listener );
				}
			}
			
			delegate.removeRateLimiter(wrapped_limiter, is_upload );
		}
	}
	
	public RateLimiter[]
	getRateLimiters(
		boolean	is_upload )
	{
		LimitedRateGroup[] limiters = delegate.getRateLimiters( is_upload );
		
		RateLimiter[]	result = new RateLimiter[limiters.length];
		
		int	pos = 0;
		
		for ( LimitedRateGroup l: limiters ){
			
			if ( l instanceof PluginLimitedRateGroup  ){
				
				result[pos++] = UtilitiesImpl.unwrapLmiter((PluginLimitedRateGroup)l);
			}
		}
		
		if ( pos == result.length ){
			
			return( result );
		}
		
		RateLimiter[]	result_mod = new RateLimiter[pos];
		
		System.arraycopy( result, 0, result_mod, 0, pos );
		
		return( result_mod );
	}
	
	public void
	close(
		String 		reason,
		boolean 	closedOnError,
		boolean 	attemptReconnect )
	{
		manager.removePeer( this, reason );
	}
	
	public int
	readBytes(
		int	max )
	{
		throw( new RuntimeException( "not supported"));
	}
	
	public int
	writeBytes(
		int	max )
	{
		throw( new RuntimeException( "not supported"));
	}
	
	protected void
	closed()
	{
		synchronized( this ){

			closed	= true;
			
			if ( up_rg_listener != null ){
				
					// tidy up
				
				LimitedRateGroup[] limiters = delegate.getRateLimiters( true );
						
				for ( LimitedRateGroup l: limiters ){
					
					if ( l instanceof PluginLimitedRateGroup  ){
		
						((PluginLimitedRateGroup)l).removeListener( up_rg_listener );
					}
					
					delegate.removeRateLimiter( l,  true );
				}
				
				limiters = delegate.getRateLimiters( false );
				
				for ( LimitedRateGroup l: limiters ){
					
					if ( l instanceof PluginLimitedRateGroup  ){
		
						((PluginLimitedRateGroup)l).removeListener( down_rg_listener );
					}
					
					delegate.removeRateLimiter( l,  false );
				}
			}
		}
		
		if ( delegate instanceof PeerForeignDelegate ){
			
			((PeerForeignDelegate)delegate).stop();
		}
	}
	
	public int
	getPercentDoneOfCurrentIncomingRequest()
	{
		return( delegate.getPercentDoneOfCurrentIncomingRequest());
	}
		  
	public int
	getPercentDoneOfCurrentOutgoingRequest()
	{
		return( delegate.getPercentDoneOfCurrentOutgoingRequest());
	}

	public void 
	addListener( 
		final PeerListener	l ) 
	{
		PEPeerListener core_listener = 
			new PEPeerListener() 
			{
				public void 
				stateChanged(
					final PEPeer peer,	// seems don't need this here
					int new_state ) 
				{
					try{
						l.stateChanged( new_state );
						
					}catch( Throwable e ){
						Debug.printStackTrace(e);
					}
				}
      
				public void 
				sentBadChunk( 
					final PEPeer peer,	// seems don't need this here
					int piece_num, 
					int total_bad_chunks )
				{
					try{
						l.sentBadChunk( piece_num, total_bad_chunks );
					
					}catch( Throwable e ){
						Debug.printStackTrace(e);
					}
				}
				
				public void addAvailability(final PEPeer peer, BitFlags peerHavePieces)
				{
				}

				public void removeAvailability(final PEPeer peer, BitFlags peerHavePieces)
				{
				}
			};
    
		delegate.addListener( core_listener );
    
		synchronized( this ){
			
			if ( peer_listeners == null ){
				
				peer_listeners = new HashMap<>();
			}
			
			peer_listeners.put( l, core_listener );
		}
	}
	

	public void	
	removeListener( 
		PeerListener	l ) 
	{
		PEPeerListener core_listener = null;
		
		synchronized( this ){
			
			if ( peer_listeners != null ){
			
				core_listener = peer_listeners.remove( l );
			}
		}
    
		if ( core_listener != null ) {
      
			delegate.removeListener( core_listener );
		}
	}
	
	public void 
	addListener( 
		final PeerListener2	l ) 
	{
		PEPeerListener core_listener = 
			new PEPeerListener() 
			{
				public void 
				stateChanged(
					final PEPeer peer,	// seems don't need this here
					int new_state ) 
				{
					fireEvent( PeerEvent.ET_STATE_CHANGED, new_state);
				}
      
				public void 
				sentBadChunk( 
					final PEPeer peer,	// seems don't need this here
					int piece_num, 
					int total_bad_chunks )
				{
					fireEvent( PeerEvent.ET_BAD_CHUNK, new Integer[]{piece_num, total_bad_chunks});
				}
				
				public void addAvailability(final PEPeer peer, BitFlags peerHavePieces)
				{
					fireEvent( PeerEvent.ET_ADD_AVAILABILITY,peerHavePieces.flags );
				}

				public void removeAvailability(final PEPeer peer, BitFlags peerHavePieces)
				{
					fireEvent( PeerEvent.ET_REMOVE_AVAILABILITY,peerHavePieces.flags );
				}
				protected void
				fireEvent(
					final int		type,
					final Object	data )
				{
					try{
						l.eventOccurred(
							new PeerEvent()
							{
								public int getType(){ return( type );}
								public Object getData(){ return( data );}
							});
					}catch( Throwable e ){
						
						Debug.printStackTrace(e);
					}
				}
			};
    
		delegate.addListener( core_listener );
    
		synchronized( this ){
			
			if ( peer_listeners == null ){
				
				peer_listeners = new HashMap<>();
			}
			
			peer_listeners.put( l, core_listener );
		}
	}
	

	public void	
	removeListener( 
		PeerListener2	l ) 
	{
		PEPeerListener core_listener = null;
		
		synchronized( this ){
			
			if ( peer_listeners != null ){
			
				core_listener = peer_listeners.remove( l );
			}
		}
		
		if ( core_listener != null ) {
      
			delegate.removeListener( core_listener );
		}
	}
	
	public boolean 
	isPriorityConnection() 
	{
		return( delegate.isPriorityConnection());
	}
	
	public void 
	setPriorityConnection(
		boolean is_priority ) 
	{
		delegate.setPriorityConnection ( is_priority );
	}
	
	public void
	setUserData(
		Object		key,
		Object		value )
	{
		delegate.setUserData( key, value );
	}
	
	public Object
	getUserData(
		Object	key )
	{
		return( delegate.getUserData( key ));
	}
	
		// as we don't maintain a 1-1 mapping between these and delegates make sure
		// that "equals" etc works sensibly
	
	public boolean
	equals(
		Object	other )
	{
		if ( other instanceof PeerImpl ){
			
			return( delegate == ((PeerImpl)other).delegate );
		}
		
		return( false );
	}
	
	public int
	hashCode()
	{
		return( delegate.hashCode());
	}
	
	/** Core use only.  This is not propogated to the plugin interface
	 * 
	 * @return PEPeer object associated with the plugin Peer object
	 */
	public PEPeer getPEPeer() {
		return delegate;
	}

  // Pass LogRelation off to core objects

	/* (non-Javadoc)
	 * @see org.gudy.azureus2.core3.logging.LogRelation#getLogRelationText()
	 */
	public String getRelationText() {
		return propogatedRelationText(delegate);
	}

	/* (non-Javadoc)
	 * @see org.gudy.azureus2.core3.logging.LogRelation#getQueryableInterfaces()
	 */
	public Object[] getQueryableInterfaces() {
		return new Object[] { delegate };
	}
	
	public byte[] getHandshakeReservedBytes() {
		return delegate.getHandshakeReservedBytes();
	}
}
