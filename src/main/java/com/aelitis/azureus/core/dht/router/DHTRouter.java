/*
 * Created on 11-Jan-2005
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

package com.aelitis.azureus.core.dht.router;

/**
 * @author parg
 *
 */

import java.util.*;

public interface 
DHTRouter 
{
	int
	getK();
	
	byte[]
	getID();
	
	boolean
	isID(
            byte[] node_id);
	
	DHTRouterContact
	getLocalContact();
	
	void
	setAdapter(
            DHTRouterAdapter _adapter);
	
		/**
		 * Tells the router to perform its "start of day" functions required to integrate
		 * it into the DHT (search for itself, refresh buckets)
		 */

        void
	seed();
	
		/**
		 * Adds a contact to the router. The contact is not known to be alive (e.g.
		 * we've been returned the contact by someone but we've not either got a reply
		 * from it, nor has it invoked us.
		 * @param node_id
		 * @param attachment
		 * @return
		 */

        void
	contactKnown(
                byte[] node_id,
                DHTRouterContactAttachment attachment,
                boolean force);
	
		/**
		 * Adds a contact to the router and marks it as "known to be alive"
		 * @param node_id
		 * @param attachment
		 * @return
		 */

        void
	contactAlive(
                byte[] node_id,
                DHTRouterContactAttachment attachment);

		/**
		 * Informs the router that an attempt to interact with the contact failed 
		 * @param node_id
		 * @param attachment
		 * @return
		 */

        DHTRouterContact
	contactDead(
                byte[] node_id,
                boolean force);
	
	DHTRouterContact
	findContact(
            byte[] node_id);

		/**
		 * Returns num_to_return or a few more closest contacts, unordered
		 */

        List<DHTRouterContact>
	findClosestContacts(
                byte[] node_id,
                int num_to_return,
                boolean live_only);
		
	void
	recordLookup(
            byte[] node_id);
	
	boolean
	requestPing(
            byte[] node_id);
	
	void
	refreshIdleLeaves(
            long idle_max);
	
	byte[]
	refreshRandom();
	
		/**
		 * returns a list of best contacts in terms of uptime, best first
		 * @param max
		 * @return
		 */

        List<DHTRouterContact>
	findBestContacts(
                int max);
	
		/**
		 * Returns a list of DHTRouterContact objects
		 * @return
		 */

        List<DHTRouterContact>
	getAllContacts();
	
	DHTRouterStats
	getStats();
	
	void
	setSleeping(
            boolean sleeping);
	
	void
	setSuspended(
            boolean susp);
	
	void
	destroy();
	
	void
	print();
	
	/**
	 * Adds a routing table observer if it is not already observing.
	 * 
	 * @param rto
	 * the observer to add
	 * @return <code>true</code> if now observing, <code>false</code> otherwise
	 */
    boolean addObserver(DHTRouterObserver rto);
	
	/**
	 * Returns whether the given observer is already observing.
	 * 
	 * @param rto
	 * the observer to query as observing
	 * @return <code>true</code> if observing, <code>false</code> otherwise
	 */
    boolean containsObserver(DHTRouterObserver rto);
	
	/**
	 * Removes the observer if it is already observing.
	 * 
	 * @param rto
	 * the observer to remove
	 * @return <code>true</code> if no longer observing, <code>false</code> otherwise
	 */
    boolean removeObserver(DHTRouterObserver rto);
}
