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


package com.aelitis.azureus.core.subs;

import java.net.URL;
import java.util.Map;

import org.gudy.azureus2.pluginsimpl.local.utils.UtilitiesImpl;

public interface 
SubscriptionManager
	extends UtilitiesImpl.PluginSubscriptionManager
{
	Subscription
	create(
            String name,
            boolean is_public,
            String json)
		
		throws SubscriptionException;
	
	Subscription
	createRSS(
            String name,
            URL url,
            int check_interval_mins,
            Map user_data)
		
		throws SubscriptionException;
	
	Subscription
	createRSS(
            String name,
            URL url,
            int check_interval_mins,
            boolean anonymous,
            Map user_data)
		
		throws SubscriptionException;
	
		// creates a subscription that will always have the same identity for the given parameters
		// and can't be updated
	
	Subscription
	createSingletonRSS(
            String name,
            URL url,
            int check_interval_mins,
            boolean is_anon)
	
		throws SubscriptionException;
	
	Subscription
	createFromURI(
            String uri)
	
		throws SubscriptionException;
	
	int
	getKnownSubscriptionCount();
	
	int
	getSubscriptionCount(
            boolean subscribed_only);
	
	Subscription[]
	getSubscriptions();
	
	Subscription[]
   	getSubscriptions(
            boolean subscribed_only);

	Subscription
	getSubscriptionByID(
            String id);
	
		/**
		 * Full lookup
		 * @param hash
		 * @param listener
		 * @return
		 * @throws SubscriptionException
		 */

        SubscriptionAssociationLookup
	lookupAssociations(
                byte[] hash,
                SubscriptionLookupListener listener)
	
		throws SubscriptionException;
	
	SubscriptionAssociationLookup
	lookupAssociations(
            byte[] hash,
            String[] networks,
            SubscriptionLookupListener listener)
	
		throws SubscriptionException;
	
		/**
		 * Cached view of hash's subs
		 * @param hash
		 * @return
		 */

        Subscription[]
	getKnownSubscriptions(
                byte[] hash);
	
	Subscription[]
	getLinkedSubscriptions(
            byte[] hash);
	
	SubscriptionScheduler
	getScheduler();
	
	int
	getMaxNonDeletedResults();
	
	void
	setMaxNonDeletedResults(
            int max);
	
	boolean
	getAutoStartDownloads();
	
	void
	setAutoStartDownloads(
            boolean auto_start);

	int
	getAutoStartMinMB();
	
	void
	setAutoStartMinMB(
            int mb);

	int
	getAutoStartMaxMB();
	
	void
	setAutoStartMaxMB(
            int mb);

	int
	getAutoDownloadMarkReadAfterDays();
	
	void
	setAutoDownloadMarkReadAfterDays(
            int days);
	
	boolean
	isRSSPublishEnabled();
	
	void
	setRSSPublishEnabled(
            boolean enabled);
	
	boolean
	isSearchEnabled();
	
	void
	setSearchEnabled(
            boolean enabled);
	
	boolean
	isSubsDownloadEnabled();
	
	void
	setSubsDownloadEnabled(
            boolean enabled);
	
	boolean
	hideSearchTemplates();
	
	void
	setActivateSubscriptionOnChange(
            boolean b);
	
	boolean
	getActivateSubscriptionOnChange();
	
	String
	getRSSLink();
	
	void
	setRateLimits(
            String limits);
	
	String
	getRateLimits();
	
	void
	addListener(
            SubscriptionManagerListener listener);
	
	void
	removeListener(
            SubscriptionManagerListener listener);
}
