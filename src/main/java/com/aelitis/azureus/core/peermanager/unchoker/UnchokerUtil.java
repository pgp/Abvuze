/*
 * Created on Apr 5, 2005
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

package com.aelitis.azureus.core.peermanager.unchoker;

import java.util.*;

import org.gudy.azureus2.core3.peer.PEPeer;
import org.gudy.azureus2.core3.peer.impl.PEPeerTransport;
import org.gudy.azureus2.core3.util.AENetworkClassifier;
import org.gudy.azureus2.core3.util.RandomUtils;

/**
 * Utility collection for unchokers.
 */
public class UnchokerUtil {
  
  /**
   * Test whether or not the given peer is allowed to be unchoked.
   * @param peer to test
   * @param allow_snubbed if true, ignore snubbed state
   * @return true if peer is allowed to be unchoked, false if not
   */
  public static boolean 
  isUnchokable( 
	PEPeer peer, 
	boolean allow_snubbed ) 
  {
	  return(	peer.getPeerState() == PEPeer.TRANSFERING && 
			  	!peer.isSeed() && 
			  	!peer.isRelativeSeed() && 
			  	peer.isInterested() && 
			  	!peer.isUploadDisabled() &&
			  	( !peer.isSnubbed() || allow_snubbed ));
  }
  

  /**
   * Update (if necessary) the given list with the given value while maintaining a largest-value-first (as seen so far) sort order.
   * NOTE: You will need to initialize the values array to Long.MIN_VALUE if you want to store negative values!
   * @param new_value to use
   * @param values existing values array
   * @param new_item to insert
   * @param items existing items
   * @param start_pos index at which to start compare
   */
  public static void updateLargestValueFirstSort( long new_value, long[] values, PEPeer new_item, ArrayList items, int start_pos ) {
	items.ensureCapacity( values.length );
    for( int i=start_pos; i < values.length; i++ ) {
      if( new_value >= values[ i ] ) {
	      // Potentially:
	      // System.arraycopy(values, i, values, i + 1, values.length - 2 + 1 - i);

		  //shift displaced values to the right
		  // @@@ REFACTORED BY IDEA
		  if (values.length - 1 - i >= 0) System.arraycopy(values, i, values, i + 1, values.length - 1 - i);
        
        if( items.size() == values.length ) {  //throw away last item if list too large 
            items.remove( values.length - 1 );
        }

        values[ i ] = new_value;
        items.add( i, new_item );
          
        return;
      }
    }
  }

  
  /**
   * Choose the next peer, optimistically, that should be unchoked.
   * @param all_peers list of peer to choose from
   * @param factor_reciprocated if true, factor in how much (if any) this peer has reciprocated when choosing
   * @param allow_snubbed allow the picking of snubbed-state peers as last resort
   * @return the next peer to optimistically unchoke, or null if there are no peers available
   */
  public static PEPeer getNextOptimisticPeer( ArrayList<PEPeer> all_peers, boolean factor_reciprocated, boolean allow_snubbed ) {
	  
	  ArrayList<PEPeer>	peers = getNextOptimisticPeers( all_peers, factor_reciprocated, allow_snubbed, 1 );
	  
	  if ( peers != null ){
		  
		  return peers.get(0);
	  }
	  
	  return( null );
  }

  public static ArrayList<PEPeer> getNextOptimisticPeers( ArrayList<PEPeer> all_peers, boolean factor_reciprocated, boolean allow_snubbed, int num_needed ) {
    //find all potential optimistic peers
    ArrayList<PEPeer> optimistics = new ArrayList<>();
	  for (PEPeer peer : all_peers) {
		  if (isUnchokable(peer, false) && peer.isChokedByMe()) {
			  optimistics.add(peer);
		  }
	  }
    
    if( optimistics.isEmpty() && allow_snubbed ) {  //try again, allowing snubbed peers as last resort
		for (PEPeer peer : all_peers) {
			if (isUnchokable(peer, true) && peer.isChokedByMe()) {
				optimistics.add(peer);
			}
		}
    }

    if( optimistics.isEmpty() )  return null;  //no unchokable peers avail
    
    //factor in peer reciprocation ratio when picking optimistic peers
    
    ArrayList<PEPeer>	result = new ArrayList<>(optimistics.size());
    
    if ( factor_reciprocated ){
    	
      ArrayList<PEPeerTransport> ratioed_peers = new ArrayList<>(optimistics.size());
      long[] ratios = new long[ optimistics.size() ];
      Arrays.fill( ratios, Long.MIN_VALUE );
        
      //order by upload ratio
		for (PEPeer peer : optimistics) {
			//score of >0 means we've uploaded more, <0 means we've downloaded more
			long score = peer.getStats().getTotalDataBytesSent() - peer.getStats().getTotalDataBytesReceived();

			UnchokerUtil.updateLargestValueFirstSort(score, ratios, peer, ratioed_peers, 0);  //higher value = worse score
		}
      
	  for (int i=0;i<num_needed && ratioed_peers.size() > 0;i++ ){

		  double factor = 1F / ( 0.8 + 0.2 * Math.pow( RandomUtils.nextFloat(), -1 ) );  //map to sorted list using a logistic curve 
      
		  int pos = (int)(factor * ratioed_peers.size());

		  result.add(ratioed_peers.remove( pos ));
	  }
    }else{

	    for (int i=0;i<num_needed && optimistics.size() > 0;i++ ){
	    	
		    int rand_pos = new Random().nextInt( optimistics.size() );
		    		    
		    result.add( optimistics.remove( rand_pos ));
	    }
    }
    
    return( result );
    
    //TODO:
    //in downloading mode, we would be better off optimistically unchoking just peers we are interested in ourselves,
    //as they could potentially reciprocate. however, new peers have no pieces to share, and are not interesting to
    //us, and would never be unchoked, and thus would never get any data.
    //we could use a deterministic method for new peers to get their very first piece from us
  }

  
  
  /**
   * Send choke/unchoke messages to the given peers.
   * @param peers_to_choke
   * @param peers_to_unchoke
   */
  public static void performChokes( ArrayList<PEPeer> peers_to_choke, ArrayList<PEPeer> peers_to_unchoke ) {
  	//do chokes
  	if( peers_to_choke != null ) {
		for (PEPeer pePeer : peers_to_choke) {
			final PEPeerTransport peer = (PEPeerTransport) pePeer;

			if (!peer.isChokedByMe()) {
				peer.sendChoke();
			}
		}
  	}
		
		//do unchokes
  	if( peers_to_unchoke != null ) {
		for (final PEPeer peer : peers_to_unchoke) {
			if (peer.isChokedByMe()) {   //TODO add UnchokerUtil.isUnchokable() test here to be safe?
				peer.sendUnChoke();
			}
		}
  	}
  }
  
  
  public static void performChokeUnchoke( PEPeer to_choke, PEPeer to_unchoke ) {
  	if( to_choke != null && !to_choke.isChokedByMe() ) {  
  		to_choke.sendChoke();   		
  	}
  	
  	if( to_unchoke != null && to_unchoke.isChokedByMe() ) {
  		to_unchoke.sendUnChoke();
		}
  }
  
  public static void 
  doHighLatencyPeers( 
	 ArrayList<PEPeer> 	peers_to_choke, 
	 ArrayList<PEPeer> 	peers_to_unchoke,
	 boolean			allow_snubbed )
  {
	
	  // when called we don't want to choke high-latency peers
	
	  if ( peers_to_choke.size() == 0 ){
		  
		  return;
	  }
	  
	  //System.out.println( "doHLP: " + peers_to_choke + ", " + peers_to_unchoke );
	  
	  Iterator<PEPeer> choke_it = peers_to_choke.iterator();
	  
	  int	to_remove = 0;
	  
	  while( choke_it.hasNext()){
		  
		  PEPeer peer = choke_it.next();
		  
		  if (!AENetworkClassifier.AT_PUBLIC.equals(AENetworkClassifier.categoriseAddress(peer.getIp()))){
			  
			  if ( isUnchokable( peer, allow_snubbed )){
			
				  //System.out.println( "   removed " + peer );
				  
				  choke_it.remove();
			
				  to_remove++;
				  
			  }else{
				  
				  // it isn't unchokable so we need to choke it whatever
			
			  }
		  }
	  }
	  
	  	// if we've removed any chokes then we need to balance things by removing an equal number
	  	// of unchokes
	  
	  if ( to_remove > 0 ){
		  
		  ListIterator<PEPeer> unchoke_it = peers_to_unchoke.listIterator( peers_to_unchoke.size());
		  
		  	// preferrably balance with high latency peers
		  
		  while( unchoke_it.hasPrevious()){
			 
			  PEPeer peer = unchoke_it.previous();
			  
			  if (!AENetworkClassifier.AT_PUBLIC.equals(AENetworkClassifier.categoriseAddress(peer.getIp()))){
				  
				  //System.out.println( "   balanced with " + peer );
				  
				  unchoke_it.remove();
				  
				  to_remove--;
				  
				  if ( to_remove == 0 ){
					  
					  return;
				  }
			  }
		  }
		  
		  if ( to_remove > 0 ){
			 
			  unchoke_it = peers_to_unchoke.listIterator( peers_to_unchoke.size());
			  
			  while( unchoke_it.hasPrevious()){
				 	
				  PEPeer peer = unchoke_it.previous();
				  
				  unchoke_it.remove();
				  
				  to_remove--;
				  
				  if ( to_remove == 0 ){
					  
					  return;
				  }
			  }
		  }
	  }
  }	  
}
