/*
 * File    : Utilities.java
 * Created : 24-Mar-2004
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

package org.gudy.azureus2.plugins.utils;

/**
 * @author parg
 *
 */

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import org.gudy.azureus2.plugins.PluginException;
import org.gudy.azureus2.plugins.ddb.DistributedDatabase;
import org.gudy.azureus2.plugins.tag.Tag;
import org.gudy.azureus2.plugins.tag.TagManager;
import org.gudy.azureus2.plugins.utils.ScriptProvider.ScriptProviderListener;
import org.gudy.azureus2.plugins.utils.resourcedownloader.*;
import org.gudy.azureus2.plugins.utils.resourceuploader.ResourceUploaderFactory;
import org.gudy.azureus2.plugins.utils.search.SearchException;
import org.gudy.azureus2.plugins.utils.search.SearchInitiator;
import org.gudy.azureus2.plugins.utils.search.SearchProvider;
import org.gudy.azureus2.plugins.utils.security.*;
import org.gudy.azureus2.plugins.utils.subscriptions.SubscriptionException;
import org.gudy.azureus2.plugins.utils.subscriptions.SubscriptionManager;
import org.gudy.azureus2.plugins.utils.xml.simpleparser.*;
import org.gudy.azureus2.plugins.utils.xml.rss.*;

public interface 
Utilities 
{
	String
	getAzureusUserDir();
	
	String
	getAzureusProgramDir();
	
	boolean
	isCVSVersion();
	
	boolean
	isWindows();
	
	boolean
	isLinux();
	
	boolean
	isSolaris();
	
	boolean
	isOSX();
	

	/**
	 * @return Whether the OS is a unix flavor (linux, bsd, aix, etc)
	 * 
	 * @since 2.4.0.3
	 */
	boolean isUnix();

	/**
	 * @return Whether the OS is FreeBSD
	 * 
	 * @note Up to at least v2.4.0.2, the FreeBSD port has identified itself
	 *       to azureus as Linux and not FreeBSD
	 * 
	 * @since 2.4.0.3
	 */
	boolean isFreeBSD();
	

	InputStream
	getImageAsStream(
            String image_name);
	
	Semaphore
	getSemaphore();
  
    Monitor getMonitor();
	
	ByteBuffer
	allocateDirectByteBuffer(
            int size);
	
	void
	freeDirectByteBuffer(
            ByteBuffer buffer);
	
	PooledByteBuffer
	allocatePooledByteBuffer(
            int size);
	
	PooledByteBuffer
	allocatePooledByteBuffer(
            byte[] data);
	
		/**
		 * 
		 * @param data	must be b-encodable
		 * @return
		 */

        PooledByteBuffer
	allocatePooledByteBuffer(
                Map data)
	
		throws IOException;
	
	Formatters
	getFormatters();
	
	LocaleUtilities
	getLocaleUtilities();
	
	/**
	 * Creates a <code>UTTimer</code> instance. It will be configured for non-lightweight
	 * tasks by default.
	 * 
	 * @param name Name for the UTTimer object.
	 * @return A UTTimer instance.
	 */
    UTTimer
	createTimer(
            String name);

	/**
	 * Creates a <code>UTTimer</code> instance.
	 * 
	 * @param name Name for the UTTimer object.
	 * @param lightweight If <code>true</code>, it indicates that this timer will be used to
	 *   perform small lightweight tasks. If <code>false</code>, it indicates that
	 *   this timer will be used to perform expensive tasks. This allows Azureus to create
	 *   the appropriate amount of resources to manage this timer.
	 * @return A UTTimer instance.
	 */
    UTTimer
	createTimer(
            String name,
            boolean lightweight);

	/**
	 * Creates a <code>UTTimer</code> instance.
	 * 
	 * @param name Name for the UTTimer object.
	 * @param priority The Thread.<i>XXX_</i>PRIORITY value to use.
	 * @return A UTTimer instance.
	 */
    UTTimer createTimer(String name, int priority);

		/**
		 * create and run a thread for the target. This will be a daemon thread so that
		 * its existence doesn't interfere with Azureus closedown
		 * @param name
		 * @param target
		 */

        void
	createThread(
                String name,
                Runnable target);
	
		/**
		 * create a child process and executes the supplied command line. The child process
		 * will not inherit any open handles on Windows, which does happen if Runtime is
		 * used directly. This relies on the Platform plugin, if this is not installed then
		 * this will fall back to using Runtime.exec 
		 * @param command_line
		 */

        void
	createProcess(
                String command_line)
	
		throws PluginException;
	
	ResourceDownloaderFactory
	getResourceDownloaderFactory();
	
	ResourceUploaderFactory
	getResourceUploaderFactory();

	SESecurityManager
	getSecurityManager();
	
	SimpleXMLParserDocumentFactory
	getSimpleXMLParserDocumentFactory();
	
	/**
	 * @deprecated
	 * @param is
	 * @return
	 * @throws SimpleXMLParserDocumentException
	 */

    RSSFeed
	getRSSFeed(
            InputStream is)
	
		throws SimpleXMLParserDocumentException;
	
	RSSFeed
	getRSSFeed(
            URL source_url,
            InputStream is)
	
		throws SimpleXMLParserDocumentException;

	RSSFeed
	getRSSFeed(
            URL feed_location)
	
		throws ResourceDownloaderException, SimpleXMLParserDocumentException;
	
	/**
	 * @deprecated
	 * @param feed_location
	 * @return
	 * @throws ResourceDownloaderException
	 * @throws SimpleXMLParserDocumentException
	 */

    RSSFeed
	getRSSFeed(
            ResourceDownloader feed_location)
	
		throws ResourceDownloaderException, SimpleXMLParserDocumentException;

	RSSFeed
	getRSSFeed(
            URL source_url,
            ResourceDownloader feed_location)
	
		throws ResourceDownloaderException, SimpleXMLParserDocumentException;
	
		/**
		 * Returns a public IP address of the machine or null if it can't be determined
		 */

        InetAddress
	getPublicAddress();
	
	InetAddress
	getPublicAddress(
            boolean ipv6);
	
		/**
		 * attempts a reverse DNS lookup of an address, null if it fails
		 * @param address
		 * @return
		 */

        String
	reverseDNSLookup(
                InetAddress address);
  
  
  /**
   * Get the current system time, like System.currentTimeMillis(),
   * only the time lookup is cached for performance reasons.
   * @return current system time
   */
  long getCurrentSystemTime();
  
  	ByteArrayWrapper
	createWrapper(
            byte[] data);
  	
  	
  	/**
  	 * create a dispatcher that will queue runnable items until either the limit
  	 * is reached or the dispatcher hasn't had an entry added for the defined idle time
  	 * @param idle_dispatch_time	milliseconds
  	 * @param max_queue_size		0 -> infinite
  	 * @return
  	 */

    AggregatedDispatcher
	createAggregatedDispatcher(
            long idle_dispatch_time,
            long max_queue_size);
  	
 	AggregatedList
	createAggregatedList(
            AggregatedListAcceptor acceptor,
            long idle_dispatch_time,
            long max_queue_size);
 	
	/**
	 * 
 	 * @return Map read from config file, or empty HashMap if error
	 */
    Map
 	readResilientBEncodedFile(
            File parent_dir,
            String file_name,
            boolean use_backup);
 	
	void
 	writeResilientBEncodedFile(
            File parent_dir,
            String file_name,
            Map data,
            boolean use_backup);
	
	void
 	deleteResilientBEncodedFile(
            File parent_dir,
            String file_name,
            boolean use_backup);
	
	/**
	 * Compares two version strings for order. 
	 * Returns a negative integer, zero, or a positive integer as the first 
	 * argument is less than, equal to, or greater than the second.
	 * <p>
	 * Example:<pre>
	 * compareVersions("1.1.0.0", "1.1.2.0"); // -
	 * compareVersions("1.1.0.0", "1.1.0"); // 0
	 * compareVersions("1.1.1.1", "1.1.1"); // +
	 * </pre>
	 * 
	 * @param v1 the first version string to be compared 
	 * @param v2 the second version string to be compared
	 * @return a negative integer, zero, or a positive integer as the first 
	 *          argument is less than, equal to, or greater than the second.
	 * 
	 * @since 2.3.0.7
	 */
    int compareVersions(String v1, String v2);
	
	/**
	 * Converts a file name so that all characters in the file name are
	 * compatible with the underlying filesystem. This includes quote
	 * characters, back and forwarded slashes, newline characters and so on.
	 * 
	 * <p>
	 * 
	 * Note - this is only intended for file names, rather than file paths.
	 * 
	 * @param f_name File name to convert.
	 * @return Converted file name.
	 */
    String normaliseFileName(String f_name);
	
	/**
	 * Adds a low priority task that will be scheduled at some point after existing tasks have
	 * completed. In particular a system task exists that will block subsequent ones until after
	 * UI initialisation is complete. Plugins can therefore use this to schedule initialisation 
	 * actions to occur after UI init is complete.
	 * 
	 * @since 3.0.5.3
	 * @return
	 */

    DelayedTask createDelayedTask(Runnable r);
	
	void
	registerSearchProvider(
            SearchProvider provider)
	
		throws SearchException;

	void
	unregisterSearchProvider(
            SearchProvider provider)
	
		throws SearchException;
	
	SearchInitiator
	getSearchInitiator()
	
		throws SearchException;
	
	SubscriptionManager
	getSubscriptionManager()
	
		throws SubscriptionException;
	
	FeatureManager
	getFeatureManager();
	
	boolean
	supportsPowerStateControl(
            int state);
	
	void
	addPowerManagementListener(
            PowerManagementListener listener);
		
	void
	removePowerManagementListener(
            PowerManagementListener listener);
	
	List<LocationProvider>
	getLocationProviders();
	
	void
	addLocationProvider(
            LocationProvider provider);
	
	void
	removeLocationProvider(
            LocationProvider provider);
	
	void
	addLocationProviderListener(
            LocationProviderListener listener);
	
	void
	removeLocationProviderListener(
            LocationProviderListener listener);
	
	
	void
	registerJSONRPCServer(
            JSONServer server);
	
	void
	unregisterJSONRPCServer(
            JSONServer server);

	void
	registerJSONRPCClient(
            JSONClient client);
	
	void
	unregisterJSONRPCClient(
            JSONClient client);

	List<DistributedDatabase>
	getDistributedDatabases(
            String[] networks);
	
	List<DistributedDatabase>
	getDistributedDatabases(
            String[] networks,
            Map<String, Object> options);
	
	List<ScriptProvider>
	getScriptProviders();
	
	void
	registerScriptProvider(
            ScriptProvider provider);
	
	void
	unregisterScriptProvider(
            ScriptProvider provider);
	
	void
	addScriptProviderListener(
            ScriptProviderListener provider);
	
	void
	removeScriptProviderListener(
            ScriptProviderListener provider);
	
	TagManager
	getTagManager();
	
	Tag
	lookupTag(
            String name);
	
	interface
	JSONServer
	{
		String
		getName();
		
		List<String>
		getSupportedMethods();
		
		Map
		call(
                String method,
                Map args)
		
			throws PluginException;
	}
	
	interface
	JSONClient
	{
		void
		serverRegistered(
                JSONServer server);
		
		void
		serverUnregistered(
                JSONServer server);
	}

}


