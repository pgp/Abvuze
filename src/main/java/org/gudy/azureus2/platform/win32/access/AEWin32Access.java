/*
 * Created on Apr 16, 2004
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

package org.gudy.azureus2.platform.win32.access;

import java.io.File;
import java.net.InetAddress;
import java.util.Map;

import org.gudy.azureus2.platform.PlatformManagerPingCallback;

/**
 * @author parg
 *
 */


public interface 
AEWin32Access 
{
	int	HKEY_CLASSES_ROOT		= 1;
	int	HKEY_CURRENT_CONFIG		= 2;
	int	HKEY_LOCAL_MACHINE		= 3;
	int	HKEY_CURRENT_USER		= 4;
	
	int SW_HIDE = 0;
	int SW_NORMAL = 1;
	int SW_SHOWNORMAL = 1;
	int SW_SHOWMINIMIZED = 2;
	int SW_SHOWMAXIMIZED = 3;
	int SW_MAXIMIZE = 3;
	int SW_SHOWNOACTIVATE = 4;
	int SW_SHOW = 5;
	int SW_MINIMIZE = 6;
	int SW_SHOWMINNOACTIVE = 7;
	int SW_SHOWNA = 8;
	int SW_RESTORE = 9;
	int SW_SHOWDEFAULT = 10;
	int SW_FORCEMINIMIZE = 11;
	int SW_MAX = 11;

	boolean
	isEnabled();
	
	String
	getVersion();
	
	String
	readStringValue(
            int type,        // HKEY type from above
            String subkey,
            String value_name)
	
		throws AEWin32AccessException;
	
	void
	writeStringValue(
            int type,        // HKEY type from above
            String subkey,
            String value_name,
            String value_value)
	
		throws AEWin32AccessException;
	
	int
	readWordValue(
            int type,        // HKEY type from above
            String subkey,
            String value_name)
	
		throws AEWin32AccessException;
	
	void
	writeWordValue(
            int type,        // HKEY type from above
            String subkey,
            String value_name,
            int value_value)
	
		throws AEWin32AccessException;
	

	void
	deleteKey(
            int type,
            String subkey)
	
		throws AEWin32AccessException;
	
	void
	deleteKey(
            int type,
            String subkey,
            boolean recursuve)
	
		throws AEWin32AccessException;
	
	void
	deleteValue(
            int type,
            String subkey,
            String value_name)
	
		throws AEWin32AccessException;
	
	String
	getUserAppData()
	
		throws AEWin32AccessException;
	
	String
	getProgramFilesDir()
	
		throws AEWin32AccessException;
	
	String
	getApplicationInstallDir(
            String app_name)
		
		throws AEWin32AccessException;
	
	void
	createProcess(
            String command_line,
            boolean inherit_handles)
	
		throws AEWin32AccessException;
	
	void
	moveToRecycleBin(
            String file_name)
	
		throws AEWin32AccessException;
	
	void
    copyFilePermissions(
            String from_file_name,
            String to_file_name)
	
		throws AEWin32AccessException;
	
	boolean
	testNativeAvailability(
            String name)
		
		throws AEWin32AccessException;
	
	void
	traceRoute(
            InetAddress source_address,
            InetAddress target_address,
            PlatformManagerPingCallback callback)
	
		throws AEWin32AccessException;
	
	void
	ping(
            InetAddress source_address,
            InetAddress target_address,
            PlatformManagerPingCallback callback)
	
		throws AEWin32AccessException;
	
	void
	addListener(
            AEWin32AccessListener listener);
	
	void
	removeListener(
            AEWin32AccessListener listener);

	/**
	 * @return
	 * @throws AEWin32AccessException
	 */
	String getUserDocumentsDir() throws AEWin32AccessException;

	/**
	 * @return
	 * @throws AEWin32AccessException
	 */
	String getUserMusicDir() throws AEWin32AccessException;

	/**
	 * @return
	 * @throws AEWin32AccessException
	 */
	String getUserVideoDir() throws AEWin32AccessException;

	/**
	 * @return
	 * @throws AEWin32AccessException
	 */
	String getCommonAppData() throws AEWin32AccessException;
	
	int
	shellExecute(
            String operation,
            String file,
            String parameters,
            String directory,
            int SW_const)

		throws AEWin32AccessException;
	
	int
	shellExecuteAndWait(
            String file,
            String params)

		throws AEWin32AccessException;

	/**
	 * @return
	 *
	 * @since 4.1.0.5
	 */
    Map<File, Map>
		getAllDrives();

 	boolean isUSBDrive(Map driveInfo);

	/**
	 * @return
	 * @throws AEWin32AccessException
	 *
	 * @since 4.5.0.3
	 */
	String getLocalAppData() throws AEWin32AccessException;
	
	/**
	 * @since 4713
	 * @param state
	 */

    void
	setThreadExecutionState(
            int state);
}
