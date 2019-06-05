/*
 * File    : ShareManager.java
 * Created : 30-Dec-2003
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

package org.gudy.azureus2.plugins.sharing;

/**
 * @author parg
 *
 */

import java.io.File;
import java.util.Map;

public interface 
ShareManager 
{
	String	PR_PERSONAL		= "personal";		// "true"/"false"
	String	PR_NETWORKS		= "networks";		// String of nets, comma separated
	String	PR_TAGS			= "tags";			// String of long tag IDs, comma separated
	String	PR_USER_DATA	= "user_data";		// something distinct the 'creator' can recognise
	String	PR_PERSISTENT	= "persistent";		// "true"/"false"

	void
	initialise()
	
		throws ShareException;
	
	boolean
	isInitialising();
	
	ShareResource[]
	getShares();
	
		/**
		 * returns null if share not defined
		 * @param file_or_dir
		 * @return
		 */

        ShareResource
	getShare(
                File file_or_dir);
	
	ShareResourceFile
	addFile(
            File file)
	
		throws ShareException;
	
	ShareResourceFile
	addFile(
            File file,
            Map<String, String> properties)
	
		throws ShareException;

	ShareResourceDir
	addDir(
            File dir)
	
		throws ShareException;
	
	ShareResourceDir
	addDir(
            File dir,
            Map<String, String> properties)
	
		throws ShareException;
	
	ShareResourceDirContents
	addDirContents(
            File dir,
            boolean recursive)
	
		throws ShareException;
	
	ShareResourceDirContents
	addDirContents(
            File dir,
            boolean recursive,
            Map<String, String> properties)
	
		throws ShareException;
	
	/**
	 * adding shares can take a long time due to the torrent creation process. The current
	 * activity can be interrupted by calling this function, in which case the original 
	 * activity will fail with a ShareException
	 */

    void
	cancelOperation();
	
	void
	addListener(
            ShareManagerListener listener);
	
	void
	removeListener(
            ShareManagerListener listener);
}
