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


package com.aelitis.azureus.core.cnetwork;

import com.aelitis.azureus.core.vuzefile.VuzeFile;

public interface 
ContentNetwork 
{
	long	CONTENT_NETWORK_UNKNOWN		= -1;
	
	long	CONTENT_NETWORK_VUZE		= 1;
	
		/**
		 * You should normally NOT refer explicitly to this constant without consideration
		 * as we don't want network-specific code proliferating
		 */

        long	CONTENT_NETWORK_RFN			= 2;
	
	long	CONTENT_NETWORK_VHDNL		= 3;


	int		SERVICE_XSEARCH				= 2;	// String - query text; Boolean - toSubscribe
	int		SERVICE_RPC					= 3;
	int		SERVICE_RELAY_RPC			= 4;
	int		SERVICE_AUTH_RPC			= 5;
	int		SERVICE_BIG_BROWSE			= 6;
	int		SERVICE_PUBLISH				= 7;
	int		SERVICE_WELCOME				= 8;
	int		SERVICE_PUBLISH_NEW			= 9;
	int		SERVICE_PUBLISH_ABOUT		= 10;
	int		SERVICE_CONTENT_DETAILS		= 11;	// String - hash; String (can be null) - client ref
	int		SERVICE_COMMENT				= 12;	// String - hash
	int		SERVICE_PROFILE				= 13;	// String - login_id; String - client ref
	int		SERVICE_TORRENT_DOWNLOAD	= 14;	// String - hash; String (can be null) - client ref
	int		SERVICE_SITE				= 15;
	int		SERVICE_SUPPORT				= 16;
	int		SERVICE_FAQ					= 17;
	int		SERVICE_FAQ_TOPIC			= 18;	// String - topic entry
	int		SERVICE_BLOG				= 19;
	int		SERVICE_FORUMS				= 20;
	int		SERVICE_WIKI				= 21;
	int		SERVICE_LOGIN				= 22;	// String - message (can be null)
	int		SERVICE_LOGOUT				= 23;
	int		SERVICE_REGISTER			= 24;
	int		SERVICE_MY_PROFILE			= 25;
	int		SERVICE_MY_ACCOUNT			= 26;
	int		SERVICE_SITE_RELATIVE		= 27;	// String - relative URL
	int		SERVICE_ADD_FRIEND			= 28;	// String - bg colour
	int		SERVICE_SUBSCRIPTION		= 29;	// String - subscription ID
	int		SERVICE_GET_ICON			= 30;
	int		SERVICE_AUTHORIZE			= 31;
	int		SERVICE_PREPLAYBACK  		= 32;
	int		SERVICE_POSTPLAYBACK  		= 33;
	int		SERVICE_SIDEBAR_CLOSE  		= 34;
	int		SERVICE_ABOUT				= 35;
	int		SERVICE_IDENTIFY			= 36;
	int		SERVICE_EXT_SITE_RELATIVE		= 37;	// String - relative URL

		// content network properties
	
	int		PROPERTY_SITE_HOST			= 1;	// String
	int		PROPERTY_REMOVEABLE			= 2;	// Boolean
	int		PROPERTY_ORDER				= 3;	// String (Tux prefers to Integer)

		// persistent (and local) properties
	
	String	PP_AUTH_PAGE_SHOWN			= "auth_shown";			// Boolean, default false
	String	PP_IS_CUSTOMIZATION			= "is_cust";			// Boolean, default false
	String	PP_ACTIVE			        = "active";				// Boolean
	String	PP_SHOW_IN_MENU       		= "in_menu";			// Boolean
	String	PP_SOURCE_REF				= "source_ref"; 		// String

	
		/**
		 * Returns one of the above CONTENT_NETWORK constants
		 * @return
		 */

        long
	getID();
	
	String
	getName();
	
	Object
	getProperty(
            int property);
	
	boolean
	isStartupNetwork();
	
	void
	setStartupNetwork(
            boolean is_startup);
	
		/**
		 * Test if the network supports a particular service
		 * @param service_type
		 * @return
		 */

        boolean
	isServiceSupported(
                int service_type);
	
		/**
		 * Returns the base URL of the service. If not parameterised then this is sufficient to
		 * invoke the service
		 * @param service_type
		 * @return
		 */

        String
	getServiceURL(
                int service_type);
	
		/**
		 * Generic parameterised service method
		 * @param service_type
		 * @param params
		 * @return
		 */

        String
	getServiceURL(
                int service_type,
                Object[] params);
	
	String
	getXSearchService(
            String query,
            boolean to_subscribe);
	
	String
	getContentDetailsService(
            String hash,
            String client_ref);
	
	String
	getCommentService(
            String hash);
	
	String
	getProfileService(
            String login_id,
            String client_ref);
	
	String
	getTorrentDownloadService(
            String hash,
            String client_ref);
	
		/**
		 * @param topic The topic number or a pre-defined topic constant found in <code>FAQTopics</code>
		 */

        String
	getFAQTopicService(
                String topic);
	
	String
	getLoginService(
            String message);
	
	String
	getSiteRelativeURL(
            String relative_url,
            boolean append_suffix);
	
	String
	getExternalSiteRelativeURL(
            String relative_url,
            boolean append_suffix);
	
	String
	getAddFriendURL(
            String bg_colour);
	
	String
	getSubscriptionURL(
            String subs_id);
	
		/**
		 * I'd rather this function we embedded into the ContentNetwork service getting logic, but for
		 * the moment expose it for simplicity
		 * @param url_in			base URL onto which the suffix should be appended
		 * @param for_post			whether this is for an HTTP 'POST' operation, in which case the parameter
		 * 							separator is always an '&' (for 'GET' the first param uses a '?' sep)
		 * @param include_azid		whether or not we should include the azid in the suffix
		 * @return
		 */
        String
	appendURLSuffix(
                String url_in,
                boolean for_post,
                boolean include_azid);
	
		/**
		 * export to vuze file
		 * @return
		 */

        VuzeFile
	getVuzeFile();
	
		/**
		 * Sets a locally persistent property. Name should be from the PP_ names above so keep track
		 * of what attributes exist
		 * 
		 * @param name	PP_ constant
		 * @param value	must be bencodable!
		 */

        void
	setPersistentProperty(
                String name,
                Object value);
	
	Object
	getPersistentProperty(
            String name);
	
	void
	addPersistentPropertyChangeListener(
            ContentNetworkPropertyChangeListener listener);
	
	void
	removePersistentPropertyChangeListener(
            ContentNetworkPropertyChangeListener listener);
	
		/**
		 * Set a non-persistent property of the content network
		 * @param key
		 * @param value
		 */

        void
	setTransientProperty(
                Object key,
                Object value);
	
	Object
	getTransientProperty(
            Object key);
	
	void
	remove();
}
