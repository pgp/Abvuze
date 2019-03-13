/*
 * File    : TrackerWebContext.java
 * Created : 23-Jan-2004
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

package org.gudy.azureus2.plugins.tracker.web;

/**
 * @author parg
 *
 */

import java.net.InetAddress;
import java.net.URL;

public interface 
TrackerWebContext 
{
	String
	getName();
	
	/**
	 * returns the context URLS (can be two for the tracker as http + https)
	 * @return
	 */

    URL[]
	getURLs();
	
	InetAddress
	getBindIP();
	
	void
	setEnableKeepAlive(
            boolean enable);
	
	void
	addPageGenerator(
            TrackerWebPageGenerator generator);
	
	void
	removePageGenerator(
            TrackerWebPageGenerator generator);
			
	TrackerWebPageGenerator[]
	getPageGenerators();
	
	void
	addAuthenticationListener(
            TrackerAuthenticationListener l);
	
	void
	removeAuthenticationListener(
            TrackerAuthenticationListener l);
	
		/**
		 * @since 3.0.2.3
		 */

        void
	destroy();
	
}
