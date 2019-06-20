/**
 * Copyright (C) Azureus Software, Inc, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */

package com.aelitis.azureus.core.util;

import com.aelitis.azureus.core.BasePlatformTorrentUtils;
import org.gudy.azureus2.core3.torrent.TOTorrent;

import java.util.Map;


public class PlatformTorrentUtils extends BasePlatformTorrentUtils {
	// duplicate of some azureus3 project PlatformTorrentUtils features needed in azureus2 ;(
	
	private static final String TOR_AZ_PROP_CVERSION = "_Version_";

	private static final String TOR_AZ_PROP_THUMBNAIL_TYPE = "Thumbnail.type";

	private static void setContentMapString(TOTorrent torrent, String key,
			String value) {
		if (torrent == null) {
			return;
		}

		Map mapContent = getContentMap(torrent);
		mapContent.put(key, value);
		incVersion(mapContent);
	}
	
	private static void setContentMapLong(TOTorrent torrent, String key,
			long value) {
		if (torrent == null) {
			return;
		}

		Map mapContent = getContentMap(torrent);
		mapContent.put(key, value);
		incVersion(mapContent);
	}

	public static void setContentMapMap(TOTorrent torrent, String key,
			Map value) {
		if (torrent == null) {
			return;
		}

		Map mapContent = getContentMap(torrent);
		mapContent.put(key, value);
		incVersion(mapContent);
	}

	private static void
	incVersion(
		Map mapContent )
	{
		Long v = (Long)mapContent.get( TOR_AZ_PROP_CVERSION );
		mapContent.put( TOR_AZ_PROP_CVERSION, v==null?0:v+1 );
	}
	
	public static int getContentVersion(TOTorrent torrent) {
		Map mapContent = getContentMap(torrent);
		Long v = (Long)mapContent.get( TOR_AZ_PROP_CVERSION );
		return(v==null?0:v.intValue());
	}
	
	public static String getContentTitle(TOTorrent torrent) {
		return getContentMapString(torrent, TOR_AZ_PROP_TITLE);
	}
	public static void setContentTitle(TOTorrent torrent, String title) {
		setContentMapString(torrent, TOR_AZ_PROP_TITLE, title);
	}

	public static String getContentDescription(TOTorrent torrent) {
		return getContentMapString(torrent, TOR_AZ_PROP_DESCRIPTION);
	}
	
	public static void setContentDescription(TOTorrent torrent, String desc) {
		setContentMapString(torrent, TOR_AZ_PROP_DESCRIPTION,desc);
		writeTorrentIfExists(torrent);
	}

	public static void setContentThumbnailUrl(TOTorrent torrent, String url) {
		setContentMapString(torrent, TOR_AZ_PROP_THUMBNAIL_URL, url);
	}

	public static void setContentThumbnail(TOTorrent torrent, byte[] thumbnail) {
		Map mapContent = getContentMap(torrent);
		putOrRemove(mapContent, TOR_AZ_PROP_THUMBNAIL, thumbnail);
		incVersion(mapContent);
		writeTorrentIfExists(torrent);
	}

	public static void setContentThumbnail(TOTorrent torrent, byte[] thumbnail, String type ){
		Map mapContent = getContentMap(torrent);
		putOrRemove(mapContent, TOR_AZ_PROP_THUMBNAIL, thumbnail);
		incVersion(mapContent);
		setContentMapString(torrent, TOR_AZ_PROP_THUMBNAIL_TYPE, type);
		writeTorrentIfExists(torrent);
	}
	
	public static String getContentThumbnailType(TOTorrent torrent) {
		return getContentMapString(torrent, TOR_AZ_PROP_THUMBNAIL_TYPE);
	}

	public static void setContentPrimaryFileIndex(TOTorrent torrent, int index ) {
		setContentMapLong(torrent, TOR_AZ_PROP_PRIMARY_FILE, index );
	}
}
