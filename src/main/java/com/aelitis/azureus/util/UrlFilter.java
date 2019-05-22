/**
 * Created on Dec 9, 2008
 *
 * Copyright (C) Azureus Software, Inc, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA 
 */

package com.aelitis.azureus.util;

import org.gudy.azureus2.core3.util.AEMonitor;
import org.gudy.azureus2.core3.util.Constants;
import org.gudy.azureus2.core3.util.Debug;

import com.aelitis.azureus.core.cnetwork.*;
import com.aelitis.azureus.core.messenger.PlatformMessenger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author TuxPaper
 * @created Dec 9, 2008
 *
 */
public class UrlFilter
{
	private static UrlFilter instance = null;

	//private String RPC_WHITELIST = "AZMSG%3B[0-9]+%3B.*";

	private String default_site_host = (String) ConstantsVuze.getDefaultContentNetwork().getProperty(ContentNetwork.PROPERTY_SITE_HOST);

	private String DEFAULT_RPC_WHITELIST = "https?://"
			+ default_site_host.replaceAll("\\.", "\\\\.") + ":?[0-9]*/" + ".*";

	private List<String> listUrlBlacklist = new CopyOnWriteArrayList<>();

	private List<String> listUrlWhitelist = new CopyOnWriteArrayList<>();

	private AEMonitor mon = new AEMonitor("UrlFilter");

	public static UrlFilter getInstance() {
		synchronized (UrlFilter.class) {
			if (instance == null) {
				instance = new UrlFilter();
			}
			return instance;
		}
	}

	public UrlFilter() {
		addUrlWhitelist(DEFAULT_RPC_WHITELIST);
		addUrlWhitelist("https?://([^.]+.?)?vuze.com:?[0-9]*/.*");
		addUrlWhitelist("https?://192\\.168\\.0\\.*:?[0-9]*/.*");
		addUrlWhitelist("https?://localhost:?[0-9]*/.*");
		// for +1 button
		addUrlWhitelist("https?://plusone\\.google\\.com/.*");
		addUrlWhitelist("https?://clients[0-9]\\.google\\.com/.*");

		ContentNetworkManager cmn = ContentNetworkManagerFactory.getSingleton();
		ContentNetwork[] contentNetworks = cmn.getContentNetworks();
		cmn.addListener(new ContentNetworkListener() {
			public void networkRemoved(ContentNetwork network) {
			}
		
			public void networkChanged(ContentNetwork network) {
			}
		
			public void networkAdded(ContentNetwork network) {
				addNetworkFilters(network);
			}

			public void networkAddFailed(long network_id, Throwable error) {
			}
		});
		
		for (ContentNetwork cn : contentNetworks) {
			addNetworkFilters(cn);
		}
	}

	/**
	 * Ensure whitelist has important network urls
	 * 
	 * @param network
	 *
	 * @since 4.0.0.5
	 */
	protected void addNetworkFilters(ContentNetwork network) {
		if (network == null) {
			return;
		}
		
		int[] whitelist_services = {
				ContentNetwork.SERVICE_SITE,
				ContentNetwork.SERVICE_AUTH_RPC,
				ContentNetwork.SERVICE_RELAY_RPC,
				ContentNetwork.SERVICE_RPC,	
		};
		
		for ( int service: whitelist_services ){

			if ( network.isServiceSupported( service )){
	
				String serviceUrl = network.getServiceURL( service );
				
				if (!isWhitelisted(serviceUrl)) {
				
					addUrlWhitelist( serviceUrl + ".*" );
				}
			}
		}
	}
	
	public void addUrlWhitelist(String string) {
		addUrlWhitelistSupport( string );
		
		if ( string.contains( "://localhost" )){
			
			addUrlWhitelistSupport( string.replace( "://localhost", "://127.0.0.1" ));
		}
	}

	private void addUrlWhitelistSupport(String string) {
		mon.enter();
		try {
			if (!listUrlWhitelist.contains(string)) {
				PlatformMessenger.debug("add whitelist of " + string);
				listUrlWhitelist.add(string);
			} else {
				PlatformMessenger.debug("whitelist already exists: " + string);
			}
		} finally {
			mon.exit();
		}
	}

	public void addUrlBlacklist(String string) {
		mon.enter();
		try {
			if (!listUrlBlacklist.contains(string)) {
				PlatformMessenger.debug("add blacklist of " + string);
				listUrlBlacklist.add(string);
			}
		} finally {
			mon.exit();
		}
	}

	public String[] getUrlWhitelist() {
		
		return listUrlWhitelist.toArray(new String[0]);
	}

	public boolean
	isWhitelisted(
		String		url )
	{

        for (String s : listUrlWhitelist) {
            if (url.matches(s)) {
                return true;
            }
        }
		return( false );
	}
	
	public boolean urlCanRPC(String url) {
		return urlCanRPC(url, false );//Constants.isCVSVersion());
	}

	public boolean urlCanRPC(String url,boolean showDebug) {
		if (url == null) {
			Debug.out("URL null and should be blocked");
			return false;
		}
		
		if (Constants.isCVSVersion() && url.startsWith("file://")) {
			return true;
		}

		if ( isWhitelisted( url )){
			
			return( true );
		}
		
		if(showDebug) {
			Debug.out("urlCanRPC: URL '" + url + "' " + " does not match one of the "
					+ listUrlWhitelist.size() + " whitelist entries");
		}
		return false;
	}
	
	public boolean urlIsBlocked(String url) {
		if (url == null) {
			Debug.out("URL null and should be blocked");
			return true;
		}

        for (String blackListed : listUrlBlacklist) {
            if (url.matches(blackListed)) {
                Debug.out("URL '" + url + "' " + " is blocked by " + blackListed);
                return true;
            }
        }
		return false;
	}

}
