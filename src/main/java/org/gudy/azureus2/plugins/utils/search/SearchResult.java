/*
 * Created on Jun 20, 2008
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


package org.gudy.azureus2.plugins.utils.search;

public interface 
SearchResult 
{
	int	PR_NAME					= 1;	// String
	int	PR_PUB_DATE				= 2;	// Date
	int	PR_SIZE					= 3;	// Long
	int	PR_LEECHER_COUNT		= 4;	// Long	(-1 unknown)
	int	PR_SEED_COUNT			= 5;	// Long	(-1 unknown)
	int	PR_SUPER_SEED_COUNT		= 6;	// Long	(-1 unknown)
	int	PR_CATEGORY				= 7;	// String
	int	PR_COMMENTS				= 8;	// Long	(-1 unknown)
	int	PR_VOTES				= 9;	// Long	(-1 unknown)
	int	PR_CONTENT_TYPE			= 10;	// String  ("video","audio","game")
	int	PR_DETAILS_LINK			= 11;	// URL as String
	int	PR_DOWNLOAD_LINK		= 12;	// URL as String
	int	PR_PLAY_LINK			= 13;	// URL as String
	int	PR_PRIVATE				= 14;	// Boolean
	int	PR_DRM_KEY				= 15;	// String
	int	PR_DOWNLOAD_BUTTON_LINK = 16;	// URL as String
	int	PR_RANK					= 17;	// Long value, 0->100 (-1 unknown)
	int	PR_ACCURACY				= 18;	// Long value, 0->100 (-1 unknown)
	int	PR_VOTES_DOWN			= 19;	// Long	(-1 unknown)
	int	PR_UID					= 20;	// String
	int	PR_HASH					= 21;	// byte[] torrent hash
	int	PR_VERSION				= 22;	// Long (-1 n/a)
	int	PR_TORRENT_LINK			= 23;	// URL as String - for whatever reason, PR_DOWNLOAD_LINK will be populated with the 'download button link'
															// from a result in preference to the 'download link' - sometimes these are different... so added this one to 
															// always return teh 'download link'


	
		// if you add more properties make sure you amend the mapping in PluginEngine appropriately
		// AND the reverse mapping in MetaSearchManagerImpl
		// AND generic XML attributes in xml-http plugin
	
	Object
	getProperty(
            int property_name);
}
