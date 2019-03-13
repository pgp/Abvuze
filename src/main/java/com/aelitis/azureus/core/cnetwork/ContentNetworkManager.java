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

public interface 
ContentNetworkManager 
{
	ContentNetwork[]
	getContentNetworks();
	
	ContentNetwork
	getContentNetwork(
            long id);

	ContentNetwork
	getStartupContentNetwork();

		/**
		 * Checks networks for those that have this URL as their site
		 * @return matching network, null if none
		 */

        ContentNetwork
	getContentNetworkForURL(
                String url);

	/**
	 * {@link ContentNetworkListener#networkAdded(ContentNetwork)} will be called
	 * once the network is added
	 */
    void
	addContentNetwork(
            long id)
	
		throws ContentNetworkException;
	
	void
	addListener(
            ContentNetworkListener listener);
	
	void
	removeListener(
            ContentNetworkListener listener);

}
