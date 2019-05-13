/*
 * Created on May 8, 2004
 * Created by Alon Rohter
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

package com.aelitis.azureus.core.networkmanager.impl.tcp;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.logging.*;
import org.gudy.azureus2.core3.util.*;

import com.aelitis.azureus.core.networkmanager.*;
import com.aelitis.azureus.core.networkmanager.impl.ProtocolDecoder;
import com.aelitis.azureus.core.networkmanager.impl.TransportHelperFilter;
import com.aelitis.azureus.core.networkmanager.impl.TransportCryptoManager;
import com.aelitis.azureus.core.networkmanager.impl.TransportHelper;
import com.aelitis.azureus.core.networkmanager.impl.TransportImpl;
import com.aelitis.azureus.core.proxy.AEProxyFactory;
import com.aelitis.azureus.core.proxy.AEProxyFactory.PluginProxy;



/**
 * Represents a peer TCP transport connection (eg. a network socket).
 */
public class TCPTransportImpl extends TransportImpl implements Transport {
	private static final LogIDs LOGID = LogIDs.NET;
  
  private final  ProtocolEndpointTCP		protocol_endpoint;


  
  private TCPConnectionManager.ConnectListener connect_request_key = null;
  private String description = "<disconnected>";
  private final boolean is_inbound_connection;
  
  private int transport_mode = TRANSPORT_MODE_NORMAL;

  public volatile boolean has_been_closed = false;
    
  private boolean 	connect_with_crypto;
  private byte[][]	shared_secrets;
  private int		fallback_count;
  private final boolean fallback_allowed;

  private boolean				is_socks;
  private volatile PluginProxy	plugin_proxy;
  
  /**
   * Constructor for disconnected (outbound) transport.
   */
  public 
  TCPTransportImpl( 
	ProtocolEndpointTCP endpoint, 
	boolean use_crypto, 
	boolean allow_fallback, 
	byte[][] _shared_secrets ) 
  {
	protocol_endpoint = endpoint;  
    is_inbound_connection = false;
    connect_with_crypto = use_crypto;
    shared_secrets		= _shared_secrets;
    fallback_allowed  = allow_fallback;
  }
  
  
  /**
   * Constructor for connected (inbound) transport.
   * @param channel connection
   * @param already_read bytes from the channel
   */
  
  public 
  TCPTransportImpl( 
	ProtocolEndpointTCP 	endpoint, 
	TransportHelperFilter	filter )
  {
	protocol_endpoint = endpoint;
   
	setFilter( filter );
        
    is_inbound_connection = true;
    connect_with_crypto = false;  //inbound connections will automatically be using crypto if necessary
    fallback_allowed = false;
    
    InetSocketAddress address = endpoint.getAddress();
    
    description = ( is_inbound_connection ? "R" : "L" ) + ": " + AddressUtils.getHostNameNoResolve( address ) + ": " + address.getPort();
 
  }
  
  /**
   * Get the socket channel used by the transport.
   * @return the socket channel
   */
  public SocketChannel getSocketChannel() {  
  	TransportHelperFilter filter = getFilter();
  	if (filter == null) {
  		return null;
  	}
  	
  	TCPTransportHelper helper = (TCPTransportHelper)filter.getHelper();
  	if (helper == null) {
  		return null;
  	}

  	return helper.getSocketChannel();  
  }
  
  public TransportEndpointTCP
  getTransportEndpoint()
  {
	  return( new TransportEndpointTCP( protocol_endpoint, getSocketChannel()));
  }
  
  public TransportStartpoint 
  getTransportStartpoint() 
  {
	  return( new TransportStartpointTCP( getTransportEndpoint()));
  }
  
  public int
  getMssSize()
  {
	  return( TCPNetworkManager.getTcpMssSize());
  }
  
  public boolean 
  isTCP()
  { 
	  return( true );
  }
	
  public boolean 
  isSOCKS()
  {
	  return( is_socks );
  }
  
  public String 
  getProtocol()
  {
	  if ( is_socks ){
		  
		  return( "TCP (SOCKS)" );
		  
	  }else{
		  
		  return "TCP";
	  }
  }
  
  /**
   * Get a textual description for this transport.
   * @return description
   */
  public String getDescription() {  return description;  }
  
 
 
  /**
   * Request the transport connection be established.
   * NOTE: Will automatically connect via configured proxy if necessary.
   * @param address remote peer address to connect to
   * @param listener establishment failure/success listener
   */
  public void connectOutbound( final ByteBuffer initial_data, final ConnectListener listener, final int priority ) {
	  
	if ( !TCPNetworkManager.TCP_OUTGOING_ENABLED ){
	
		listener.connectFailure( new Throwable( "Outbound TCP connections disabled" ));
		
		return;
	}
	
    if( has_been_closed ){
    	
		listener.connectFailure( new Throwable( "Connection already closed" ));

    	return;
    }
    
    if( getFilter() != null ) {  //already connected
      Debug.out( "socket_channel != null" );
      listener.connectSuccess( this, initial_data );
      return;
    }
    
    final InetSocketAddress	address = protocol_endpoint.getAddress();
    
    if ( !address.equals( ProxyLoginHandler.DEFAULT_SOCKS_SERVER_ADDRESS )){
   
     
    			// see if a plugin can handle this connection
    		
		if ( address.isUnresolved()){
			
			String host = address.getHostName();
			
			if (!AENetworkClassifier.AT_PUBLIC.equals(AENetworkClassifier.categoriseAddress(host))){
			
				Map<String,Object>	opts = new HashMap<>();
				
				Object peer_nets = listener.getConnectionProperty( AEProxyFactory.PO_PEER_NETWORKS );
				
				if ( peer_nets != null ){
					
					opts.put( AEProxyFactory.PO_PEER_NETWORKS, peer_nets );
				}
				
				PluginProxy pp = plugin_proxy;
				
				plugin_proxy = null;
				
				if ( pp != null ){
				
						// most likely crypto fallback connection so don't assume it is a bad
						// outcome
					
					pp.setOK( true );
				}
					
				plugin_proxy = AEProxyFactory.getPluginProxy( "outbound connection", host, address.getPort(), opts );
				    		
			}
		}
    	
		if ( plugin_proxy == null ){
			
		   	is_socks = COConfigurationManager.getBooleanParameter( "Proxy.Data.Enable" );
		}
    }
        
    final TCPTransportImpl transport_instance = this;    
    
     
    TCPConnectionManager.ConnectListener connect_listener = new TCPConnectionManager.ConnectListener() {
      public int connectAttemptStarted(
    		  int default_connect_timeout ) {
        return( listener.connectAttemptStarted( default_connect_timeout ));
      }
      
      public void connectSuccess( final SocketChannel channel ) {
      	if( channel == null ) {
      		String msg = "connectSuccess:: given channel == null";
      		Debug.out( msg );
      		setConnectResult( false );
      		listener.connectFailure( new Exception( msg ) );
      		return;
      	}
      	
        if( has_been_closed ) {  //closed between select ops
        	TCPNetworkManager.getSingleton().getConnectDisconnectManager().closeConnection( channel );  //just close it
          
        	setConnectResult( false );
        	
  			listener.connectFailure( new Throwable( "Connection has been closed" ));

          return;
        }
        
        connect_request_key = null;
        description = ( is_inbound_connection ? "R" : "L" ) + ": " + channel.socket().getInetAddress().getHostAddress() + ": " + channel.socket().getPort();

        PluginProxy pp = plugin_proxy;
        
        if ( is_socks ){  //proxy server connection established, login
        	if (Logger.isEnabled())
        		Logger.log(new LogEvent(LOGID,"Socket connection established to proxy server [" +description+ "], login initiated..."));

        	// set up a transparent filter for socks negotiation

        	setFilter( TCPTransportHelperFilterFactory.createTransparentFilter( channel ));

        	new ProxyLoginHandler( transport_instance, address, new ProxyLoginHandler.ProxyListener() {
        		public void connectSuccess() {
        			if (Logger.isEnabled())
        				Logger.log(new LogEvent(LOGID, "Proxy [" +description+ "] login successful." ));
        			handleCrypto( address, channel, initial_data, priority, listener );
        		}

        		public void connectFailure( Throwable failure_msg ) {
        			close( "Proxy login failed" );
        			listener.connectFailure( failure_msg );
        		}
        	});
        }else if ( pp != null ){

           	if (Logger.isEnabled()){
        		Logger.log(new LogEvent(LOGID,"Socket connection established via plugin proxy [" +description+ "], login initiated..."));
           	}
           	
           		// set up a transparent filter for socks negotiation

        	setFilter( TCPTransportHelperFilterFactory.createTransparentFilter( channel ));

        	String pp_host = pp.getHost();
        	
        	InetSocketAddress ia_address;
        	
        	if (AENetworkClassifier.AT_PUBLIC.equals(AENetworkClassifier.categoriseAddress(pp_host))){
        	
        		ia_address = new InetSocketAddress( pp.getHost(), pp.getPort());
        	
        	}else{
        		
        		ia_address = InetSocketAddress.createUnresolved( pp_host, pp.getPort());
        	}
        	
        	new ProxyLoginHandler( 
        		transport_instance, 
        		ia_address,
        		new ProxyLoginHandler.ProxyListener() 
        		{
        			public void 
        			connectSuccess() 
        			{
	        			if (Logger.isEnabled()){
	        				Logger.log(new LogEvent(LOGID, "Proxy [" +description+ "] login successful." ));
	        			}
	        			setConnectResult( true );
	        			
	        			handleCrypto( address, channel, initial_data, priority, listener );
        			}

        			public void 
        			connectFailure( 
        				Throwable failure_msg ) 
        			{
        				setConnectResult( false );
        				
        				close( "Proxy login failed" );
        				
        				listener.connectFailure( failure_msg );
        			}
        		},
        		"V4a", "", "" );
        	
        }else {  //direct connection established, notify
        	handleCrypto( address, channel, initial_data, priority, listener );
        }
      }

      public void connectFailure( Throwable failure_msg ) {
        connect_request_key = null;
        setConnectResult( false );
        listener.connectFailure( failure_msg );
      }
    };
    
    connect_request_key = connect_listener;
    
    InetSocketAddress to_connect;
    
    PluginProxy pp = plugin_proxy;
    
    if ( is_socks ){
    	
    	to_connect = ProxyLoginHandler.getProxyAddress( address );
    	
    }else if ( pp != null ){
    	
    	to_connect = (InetSocketAddress)pp.getProxy().address();
    	
    }else{
    	
    	to_connect = address;
    }
    
    TCPNetworkManager.getSingleton().getConnectDisconnectManager().requestNewConnection( to_connect, connect_listener, priority );
  }
  
    
  
  
  protected void 
  handleCrypto( 
	final InetSocketAddress 	address, 
	final SocketChannel 		channel, 
	final ByteBuffer 			initial_data, 
	final int	 				priority, 
	final ConnectListener 		listener ) 
  {  	
  	if( connect_with_crypto ) {
    	//attempt encrypted transport
  		
  		final TransportHelper	helper = new TCPTransportHelper( channel );
    	TransportCryptoManager.getSingleton().manageCrypto( helper, shared_secrets, false, initial_data, new TransportCryptoManager.HandshakeListener() {
    		public void handshakeSuccess( ProtocolDecoder decoder, ByteBuffer remaining_initial_data ) {    			
    			//System.out.println( description+ " | crypto handshake success [" +_filter.getName()+ "]" ); 
    			TransportHelperFilter filter = decoder.getFilter();
    			setFilter( filter ); 
    			if ( Logger.isEnabled()){
    		      Logger.log(new LogEvent(LOGID, "Outgoing TCP stream to " + channel.socket().getRemoteSocketAddress() + " established, type = " + filter.getName(false)));
    			}
    			
    			connectedOutbound( remaining_initial_data, listener );         
     		}

    		public void handshakeFailure( Throwable failure_msg ) {        	
        	if( fallback_allowed && NetworkManager.OUTGOING_HANDSHAKE_FALLBACK_ALLOWED && !has_been_closed ) {        		
        		if( Logger.isEnabled() ) Logger.log(new LogEvent(LOGID, description+ " | crypto handshake failure [" +failure_msg.getMessage()+ "], attempting non-crypto fallback." ));
        		connect_with_crypto = false;
        		fallback_count++;
         		close( helper, "Handshake failure and retry" );
        		has_been_closed = false;
        		if ( initial_data != null ){
        			
        			initial_data.position( 0 );
        		}
        		connectOutbound( initial_data, listener, priority );
        	}
        	else {
        		close( helper, "Handshake failure" );
        		listener.connectFailure( failure_msg );
        	}
        }
    		
    		public void
    		gotSecret(
				byte[]				session_secret )
    		{
    		}
    		
    		public int
    		getMaximumPlainHeaderLength()
    		{
    			throw( new RuntimeException());	// this is outgoing
    		}
		
    		public int
    		matchPlainHeader(
    				ByteBuffer			buffer )
    		{
    			throw( new RuntimeException());	// this is outgoing
    		}
    	});
  	}
  	else {  //no crypto
  		//if( fallback_count > 0 ) {
  		//	System.out.println( channel.socket()+ " | non-crypto fallback successful!" );
  		//}
  		setFilter( TCPTransportHelperFilterFactory.createTransparentFilter( channel ));
  		
		if ( Logger.isEnabled()){
		  Logger.log(new LogEvent(LOGID, "Outgoing TCP stream to " + channel.socket().getRemoteSocketAddress() + " established, type = " + getFilter().getName(false) + ", fallback = " + (fallback_count==0?"no":"yes" )));
		}
		
		connectedOutbound( initial_data, listener ); 
  	}
  }
  
  
  

  private void setTransportBuffersSize( int size_in_bytes ) {
  	if( getFilter() == null ) {
  		Debug.out( "socket_channel == null" );
  		return;
  	}
  	
    try{
    	SocketChannel	channel = getSocketChannel();
    	
    	channel.socket().setSendBufferSize( size_in_bytes );
    	channel.socket().setReceiveBufferSize( size_in_bytes );
      
      int snd_real = channel.socket().getSendBufferSize();
      int rcv_real = channel.socket().getReceiveBufferSize();
      
      if (Logger.isEnabled())
    	  Logger.log(new LogEvent(LOGID, "Setting new transport [" + description
					+ "] buffer sizes: SND=" + size_in_bytes + " [" + snd_real
					+ "] , RCV=" + size_in_bytes + " [" + rcv_real + "]"));
    }
    catch( Throwable t ) {
      Debug.out( t );
    }
  }
  
  
  /**
   * Set the transport to the given speed mode.
   * @param mode to change to
   */
  public void setTransportMode( int mode ) {
    if( mode == transport_mode )  return;  //already in mode
    
    switch( mode ) {
      case TRANSPORT_MODE_NORMAL:
        setTransportBuffersSize( 8 * 1024 );
        break;
        
      case TRANSPORT_MODE_FAST:
        setTransportBuffersSize( 64 * 1024 );
        break;
        
      case TRANSPORT_MODE_TURBO:
        setTransportBuffersSize( 512 * 1024 );
        break;
        
      default:
        Debug.out( "invalid transport mode given: " +mode );
    }
    
    transport_mode = mode;
  }
  
  protected void
  connectedOutbound(
	  ByteBuffer			remaining_initial_data,
	  ConnectListener		listener )
  {
	  if ( has_been_closed ){
		
		TransportHelperFilter	filter = getFilter();

	    if ( filter != null ){
	 
	      filter.getHelper().close( "Connection closed" );
	      
	      setFilter( null );
	    }
     		
    	listener.connectFailure( new Throwable( "Connection closed" ));

	  }else{
		
		connectedOutbound();
		  
		listener.connectSuccess( this, remaining_initial_data );
	  }
  }
  
  /**
   * Get the transport's speed mode.
   * @return current mode
   */
  public int getTransportMode() {  return transport_mode;  }
    
  protected void
  close(
	TransportHelper		helper,
	String				reason )
  {
	 helper.close( reason );
	 
	 close( reason );
  }
  
  private void
  setConnectResult(
	boolean		ok )
  {
	 PluginProxy pp = plugin_proxy;
	 if ( pp != null ){
		 plugin_proxy = null;
		 pp.setOK(ok);
	 }
  }
  
  /**
   * Close the transport connection.
   */
  public void close( String reason ) {
    has_been_closed = true;
    setConnectResult( false );
    if( connect_request_key != null ) {
    	TCPNetworkManager.getSingleton().getConnectDisconnectManager().cancelRequest( connect_request_key );
    }
    
    readyForRead( false );
    readyForWrite( false );

	TransportHelperFilter	filter = getFilter();

    if ( filter != null ){
 
      filter.getHelper().close( reason );
      
      setFilter( null );
    }
    
    	// we need to set it ready for reading so that the other scheduling thread wakes up
    	// and discovers that this connection has been closed
    
    setReadyForRead();
  }
}
