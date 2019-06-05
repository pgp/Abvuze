/*
 * File    : ShareResource.java
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

import java.util.Map;

import org.gudy.azureus2.plugins.torrent.TorrentAttribute;

/**
 * @author parg
 *
 */
public interface 
ShareResource 
{
	int	ST_FILE			= 1;
	int	ST_DIR			= 2;
	int	ST_DIR_CONTENTS	= 3;
	
	int
	getType();
	
	String
	getName();
	
	void
	delete()
	
		throws ShareException;
	
	void
	delete(
            boolean force)
	
		throws ShareException;

	void
	setAttribute(
            TorrentAttribute attribute,
            String value);
	
		/**
		 * @param attribute
		 * @return	null if no value defined
		 */

        String
	getAttribute(
                TorrentAttribute attribute);
	
		/**
		 * get the defined attributes for this resource
		 * @return
		 */

        TorrentAttribute[]
	getAttributes();
	
	Map<String,String>
	getProperties();
	
	boolean
	isPersistent();
	
	boolean
	canBeDeleted()
	
		throws ShareResourceDeletionVetoException;
	
	ShareResourceDirContents
	getParent();
	
	void
	addChangeListener(
            ShareResourceListener l);
	
	void
	removeChangeListener(
            ShareResourceListener l);

	void
	addDeletionListener(
            ShareResourceWillBeDeletedListener l);
	
	void
	removeDeletionListener(
            ShareResourceWillBeDeletedListener l);
}
