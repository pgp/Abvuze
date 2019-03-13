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

import org.gudy.azureus2.pluginsimpl.local.utils.UtilitiesImpl;

import com.aelitis.azureus.core.metasearch.Engine;
import com.aelitis.azureus.core.vuzefile.VuzeFile;

public interface 
Subscription 
	extends UtilitiesImpl.PluginSubscription
{
	int AZ_VERSION	= 1;
	
	Object	VUZE_FILE_COMPONENT_SUBSCRIPTION_KEY = new Object();

	int	ADD_TYPE_CREATE		= 1;
	int	ADD_TYPE_IMPORT		= 2;
	int	ADD_TYPE_LOOKUP		= 3;

		/**
		 * Returns local name if set
		 * @return
		 */

        String
	getName();
	
	String
	getName(
            boolean use_local);
	
	void
	setLocalName(
            String str);
	
	void
	setName(
            String str)
	
		throws SubscriptionException;
	
	String
	getNameEx();
	
	String
	getQueryKey();
	
	String
	getID();
	
	byte[]
	getPublicKey();
	
	int
	getVersion();

	long
	getAddTime();
	
	int
	getAddType();
	
	int
	getHighestVersion();
	
	void
	resetHighestVersion();

	int
	getAZVersion();
	
	boolean
	isMine();
	
	boolean
	isPublic();
	
	void
	setPublic(
            boolean is_public)
	
		throws SubscriptionException;
	
	boolean
	isAnonymous();
	
	boolean
	isUpdateable();
	
	boolean
	isShareable();
	
	boolean
	isSearchTemplate();
	
	boolean
	isSearchTemplateImportable();
	
	VuzeFile
	getSearchTemplateVuzeFile();
	
	String
	getJSON()
	
		throws SubscriptionException;
	
	boolean
	setJSON(
            String json)
	
		throws SubscriptionException;
	
	boolean
	isSubscribed();
	
	void
	setSubscribed(
            boolean subscribed);
	
	void
	getPopularity(
            SubscriptionPopularityListener listener)
	
		throws SubscriptionException;
	
	boolean
	setDetails(
            String name,
            boolean is_public,
            String json)
	
		throws SubscriptionException;
	
	String
	getReferer();
	
	long
	getCachedPopularity();
	
	void
	addAssociation(
            byte[] hash);
	
	void
	addPotentialAssociation(
            String result_id,
            String key);

	int
	getAssociationCount();
	
	boolean
	hasAssociation(
            byte[] hash);
	
	String
	getCategory();
	
	void
	setCategory(
            String category);
	
	/**
	 * Tag UID
	 */
    long
	getTagID();
	
	void
	setTagID(
            long tag_id);
	
	String
	getParent();
	
	void
	setParent(
            String parent);
	
	Engine
	getEngine()
	
		throws SubscriptionException;
	
	Subscription
	cloneWithNewEngine(
            Engine engine)
	
		throws SubscriptionException;
	
	boolean
	isAutoDownloadSupported();
	
	VuzeFile
	getVuzeFile()
	
		throws SubscriptionException;
		
	void
	setCreatorRef(
            String str);
	
	String
	getCreatorRef();
	
	void
	reset();
	
	void
	remove();
	
	SubscriptionManager
	getManager();
	
	SubscriptionHistory
	getHistory();
	
		/**
		 * shortcut to help plugin interface
		 * @param l
		 */

        SubscriptionResult[]
	getResults(
                boolean include_deleted);
	
	String
	getURI();
	
	SubscriptionResultFilter
	getFilters()
	
		throws SubscriptionException;
	
	void
	requestAttention();
	
	void
	addListener(
            SubscriptionListener l);
	
	void
	removeListener(
            SubscriptionListener l);
	
	void
	setUserData(
            Object key,
            Object data);
	
	Object
	getUserData(
            Object key);
	
	String
	getString();
	
		// fil
}
