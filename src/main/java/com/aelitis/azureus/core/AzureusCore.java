/*
 * Created on 13-Jul-2004
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

package com.aelitis.azureus.core;

/**
 * @author parg
 *
 */

import java.io.File;

import org.gudy.azureus2.core3.tracker.host.TRHost;
import org.gudy.azureus2.core3.global.GlobalManager;
import org.gudy.azureus2.core3.ipfilter.IpFilterManager;
import org.gudy.azureus2.core3.internat.LocaleUtil;
import org.gudy.azureus2.plugins.*;
import org.gudy.azureus2.plugins.utils.PowerManagementListener;

import com.aelitis.azureus.core.instancemanager.AZInstanceManager;
import com.aelitis.azureus.core.nat.NATTraverser;
import com.aelitis.azureus.core.security.CryptoManager;
import com.aelitis.azureus.core.speedmanager.SpeedManager;

public interface 
AzureusCore 
{	
	String	CA_QUIT_VUZE	= PluginManager.CA_QUIT_VUZE;
	String	CA_SLEEP		= PluginManager.CA_SLEEP;
	String	CA_HIBERNATE	= PluginManager.CA_HIBERNATE;
	String	CA_SHUTDOWN		= PluginManager.CA_SHUTDOWN;
	
	long
	getCreateTime();

	boolean
	canStart();
	
	void
	start()
	
		throws AzureusCoreException;
		
	boolean
	isStarted();
	
	boolean
	isInitThread();
	
		/**
		 * stop the core and inform lifecycle listeners of stopping
		 * @throws AzureusCoreException
		 */

        void
	stop()
	
		throws AzureusCoreException;
	
		/**
		 * ask lifecycle listeners to perform a stop. they may veto this by throwing an exception, or do nothing
		 * if nothing is done then it will be stopped as per "stop" above
		 * @throws AzureusCoreException
		 */

        void
	requestStop()
	
		throws AzureusCoreException;
	
		/**
		 * checks if restart operation is supported - if not an alert will be raised and an exception thrown
		 * @throws AzureusCoreException
		 */

        void
	checkRestartSupported()
	
		throws AzureusCoreException;
	
		/**
		 * restart the system
		 */

        void
	restart();
	
		/**
		 * request a restart of the system - currently only available for swt based systems 
		 * @throws AzureusCoreException
		 */

        void
	requestRestart()
	
		throws AzureusCoreException;
	
	/**
	 * 
	 * @return
	 * @since 3053
	 */

    boolean
	isRestarting();
	
	void
	executeCloseAction(
            String action,            // see CA_ constants above
            String reason);
	
	void
	saveState();
	
	LocaleUtil
	getLocaleUtil();
	
	GlobalManager
	getGlobalManager()
	
		throws AzureusCoreException;
	
	PluginManagerDefaults
	getPluginManagerDefaults()
	
		throws AzureusCoreException;
	
	PluginManager
	getPluginManager()
	
		throws AzureusCoreException;
	
	TRHost
	getTrackerHost()
	
		throws AzureusCoreException;
	
	IpFilterManager
	getIpFilterManager()
	
		throws AzureusCoreException;
	
	AZInstanceManager
	getInstanceManager();
	
	SpeedManager
	getSpeedManager();
	
	CryptoManager
	getCryptoManager();
	
	NATTraverser
	getNATTraverser();
	
	File
	getLockFile();
	
	void
	createOperation(
            int type,
            AzureusCoreOperationTask task);

	void
	addLifecycleListener(
            AzureusCoreLifecycleListener l);
	
	void
	removeLifecycleListener(
            AzureusCoreLifecycleListener l);
	
	void
	addOperationListener(
            AzureusCoreOperationListener l);
	
	void
	removeOperationListener(
            AzureusCoreOperationListener l);
	
	/**
	 * @param component
	 */
	void triggerLifeCycleComponentCreated(AzureusCoreComponent component);
	
	void
	addPowerManagementListener(
            PowerManagementListener listener);
		
	void
	removePowerManagementListener(
            PowerManagementListener listener);
}
