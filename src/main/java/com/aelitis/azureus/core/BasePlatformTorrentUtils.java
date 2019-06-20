package com.aelitis.azureus.core;

import org.gudy.azureus2.core3.global.GlobalManager;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentException;
import org.gudy.azureus2.core3.util.Constants;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.TorrentUtils;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BasePlatformTorrentUtils {
    public static final String TOR_AZ_PROP_MAP = "Content";

    public static final String TOR_AZ_PROP_TITLE = "Title";

    public static final String TOR_AZ_PROP_DESCRIPTION = "Description";

    public static final String TOR_AZ_PROP_PRIMARY_FILE = "Primary File Index";

    public static final String TOR_AZ_PROP_THUMBNAIL = "Thumbnail";

    public static final String TOR_AZ_PROP_THUMBNAIL_URL = "Thumbnail.url";

    public static Map getContentMap(TOTorrent torrent) {
        if (torrent == null) {
            return Collections.EMPTY_MAP;
        }

        Map mapAZProps = torrent.getAdditionalMapProperty(TOTorrent.AZUREUS_PROPERTIES);

        if (mapAZProps == null) {
            mapAZProps = new HashMap();
            torrent.setAdditionalMapProperty(TOTorrent.AZUREUS_PROPERTIES, mapAZProps);
        }

        Object objExistingContentMap = mapAZProps.get(TOR_AZ_PROP_MAP);

        Map mapContent;
        if (objExistingContentMap instanceof Map) {
            mapContent = (Map) objExistingContentMap;
        } else {
            mapContent = new HashMap();
            mapAZProps.put(TOR_AZ_PROP_MAP, mapContent);
        }

        return mapContent;
    }

    public static Map getTempContentMap(TOTorrent torrent) {
        if (torrent == null) {
            return new HashMap();
        }

        Map mapAZProps = torrent.getAdditionalMapProperty("attributes");

        if (mapAZProps == null) {
            mapAZProps = new HashMap();
            torrent.setAdditionalMapProperty("attributes", mapAZProps);
        }

        Object objExistingContentMap = mapAZProps.get(TOR_AZ_PROP_MAP);

        Map mapContent;
        if (objExistingContentMap instanceof Map) {
            mapContent = (Map) objExistingContentMap;
        } else {
            mapContent = new HashMap();
            mapAZProps.put(TOR_AZ_PROP_MAP, mapContent);
        }

        return mapContent;
    }

    public static String getContentMapString(TOTorrent torrent, String key) {
        if (torrent == null) {
            return null;
        }

        Map mapContent = getContentMap(torrent);
        Object obj = mapContent.get(key);

        if (obj instanceof String) {
            return (String) obj;
        } else if (obj instanceof byte[]) {
            try {
                return new String((byte[]) obj, Constants.DEFAULT_ENCODING);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static long getContentMapLong(TOTorrent torrent, String key, long def) {
        if (torrent == null) {
            return def;
        }

        Map mapContent = getContentMap(torrent);
        Object obj = mapContent.get(key);

        try {
            if (obj instanceof Long) {
                return (Long) obj;
            } else if (obj instanceof Integer) {
                return ((Integer) obj).longValue();
            } else if (obj instanceof String) {
                return Long.parseLong((String) obj);
            } else if (obj instanceof byte[]) {
                return Long.parseLong(new String((byte[]) obj));
            }
        } catch (Exception e) {
        }

        return def;
    }

    public static Map getContentMapMap(TOTorrent torrent, String key ){
        if ( torrent == null ){
            return( null );
        }

        Map mapContent = getContentMap(torrent);
        Object obj = mapContent.get(key);

        if ( obj instanceof Map ){
            return((Map)obj);
        }

        return( null );
    }

    public static void putOrRemove(Map map, String key, Object obj) {
        if (obj == null) {
            map.remove(key);
        } else {
            map.put(key, obj);
        }
    }

    public static void writeTorrentIfExists(TOTorrent torrent) {
        if (!AzureusCoreFactory.isCoreRunning()) {
            return;
        }
        AzureusCore core = AzureusCoreFactory.getSingleton();
        if (core == null || !core.isStarted()) {
            return;
        }

        GlobalManager gm = core.getGlobalManager();
        if (gm == null || gm.getDownloadManager(torrent) == null) {
            return;
        }

        try {
            TorrentUtils.writeToFile(torrent);
        } catch (TOTorrentException e) {
            Debug.out(e);
        }
    }

    public static byte[] getContentThumbnail(TOTorrent torrent) {
        Map mapContent = getContentMap(torrent);
        Object obj = mapContent.get(TOR_AZ_PROP_THUMBNAIL);

        if (obj instanceof byte[]) {
            return (byte[]) obj;
        }

        return null;
    }

    public static String getContentThumbnailUrl(TOTorrent torrent) {
        return getContentMapString(torrent, TOR_AZ_PROP_THUMBNAIL_URL);
    }

    public static int getContentPrimaryFileIndex(TOTorrent torrent ){
        return (int)getContentMapLong(torrent, TOR_AZ_PROP_PRIMARY_FILE, -1 );
    }
}
