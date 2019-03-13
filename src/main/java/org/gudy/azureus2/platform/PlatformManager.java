/*
 * Created on 18-Apr-2004
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

package org.gudy.azureus2.platform;

import java.io.File;
import java.net.InetAddress;

import org.gudy.azureus2.plugins.platform.PlatformManagerException;

import com.aelitis.azureus.core.AzureusCore;

/**
 * @author parg
 *
 */
public interface 
PlatformManager
	extends org.gudy.azureus2.plugins.platform.PlatformManager
{
	// TODO PGP named enum instead of sparse constants (adjust org.gudy.azureus2.platform.PlatformManagerFactory as well)
    int	PT_WINDOWS		= 1;
	int PT_OTHER		= 2;
    int PT_MACOSX 		= 3;
  	int PT_UNIX			= 4;
  	int PT_ANDROID		= 5;

  	int USER_REQUEST_INFO 		= 1;
  	int USER_REQUEST_WARNING 	= 2;
  	int USER_REQUEST_QUESTION 	= 3;
  	
 	int	SD_SHUTDOWN		= 0x00000001;	// don't change these as used to index msgs
 	int	SD_HIBERNATE	= 0x00000002;
 	int	SD_SLEEP		= 0x00000004;
  	
 	int[] SD_ALL = { SD_SHUTDOWN, SD_HIBERNATE, SD_SLEEP };
 	
	int
	getPlatformType();
	
	String
	getVersion()
	
		throws PlatformManagerException;
	
	void
	startup(
            AzureusCore azureus_core)
	
		throws PlatformManagerException;
	
	String
	getUserDataDirectory()
	
		throws PlatformManagerException;
	
	boolean
	isApplicationRegistered()
	
		throws PlatformManagerException;
	
	void
	registerApplication()
	
		throws PlatformManagerException;
	
	String
	getApplicationCommandLine() 
		
		throws PlatformManagerException;

	File
	getVMOptionFile()
	
		throws PlatformManagerException;
	
	String[]
	getExplicitVMOptions()
	
		throws PlatformManagerException;
	
	void
	setExplicitVMOptions(
            String[] options)
	
		throws PlatformManagerException;

	boolean
	getRunAtLogin()
	          	
	 	throws PlatformManagerException;

	void
	setRunAtLogin(
            boolean run)
	          	
	 	throws PlatformManagerException;

	int
	getShutdownTypes();
	
	void
	shutdown(
            int type)
	
		throws PlatformManagerException;
	
	void
	setPreventComputerSleep(
            boolean prevent_it)
	
		throws PlatformManagerException;
	
	boolean
	getPreventComputerSleep();
	
	void
	createProcess(
            String command_line,
            boolean inherit_handles)
	
		throws PlatformManagerException;
	
	void
    performRecoverableFileDelete(
            String file_name)
	
		throws PlatformManagerException;

		/**
		 * enable or disable the platforms support for TCP TOS
		 * @param enabled
		 * @throws PlatformManagerException
		 */

        void
	setTCPTOSEnabled(
                boolean enabled)
		
		throws PlatformManagerException;

	void
    copyFilePermissions(
            String from_file_name,
            String to_file_name)
	
		throws PlatformManagerException;

	boolean
	testNativeAvailability(
            String name)
	
		throws PlatformManagerException;
	
	void
	traceRoute(
            InetAddress interface_address,
            InetAddress target,
            PlatformManagerPingCallback callback)
	
		throws PlatformManagerException;
	
	void
	ping(
            InetAddress interface_address,
            InetAddress target,
            PlatformManagerPingCallback callback)
	
		throws PlatformManagerException;
	
		/**
		 * This max-open-files concept here is from linux/osx where network connections are treated as 'files' 
		 * @return
		 * @throws PlatformManagerException
		 */

        int
	getMaxOpenFiles()
	
		throws PlatformManagerException;
	
		/**
		 * Gives a particular platform the ability to alter the class-loading method
		 * @param loader
		 * @param class_name
		 * @return
		 * @throws PlatformManagerException
		 */

        Class<?>
	loadClass(
                ClassLoader loader,
                String class_name)
		
		throws PlatformManagerException;
		
    /**
     * <p>Gets whether the platform manager supports a capability</p>
     * <p>Users of PlatformManager should check for supported capabilities before calling
     * the corresponding methods</p>
     * <p>Note that support for a particular capability may change arbitrarily in
     * the duration of the application session, but the manager will cache where
     * necessary.</p>
     * @param capability A platform manager capability
     * @return True if the manager supports the capability
     */
    boolean
	hasCapability(
            PlatformManagerCapabilities capability);

    /**
     * Disposes system resources. This method is optional.
     */
    void
    dispose();
    
    void
    addListener(
            PlatformManagerListener listener);
    
    void
    removeListener(
            PlatformManagerListener listener);

		/**
		 * Requests the user's attention such as bouncing the application icon on OSX
		 *  
		 * @param type
		 * @param data
		 * @throws PlatformManagerException
		 */
        void
		requestUserAttention(
                int type,
                Object data)
		
			throws PlatformManagerException;
}
