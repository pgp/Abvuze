/*
 * Copyright (C) Azureus Software, Inc, All Rights Reserved.
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

package com.aelitis.azureus.ui.mdi;

import java.util.List;
import java.util.Map;

import com.aelitis.azureus.ui.common.viewtitleinfo.ViewTitleInfo;

public interface MultipleDocumentInterface
{
	String SIDEBAR_POS_FIRST = "";

	String SIDEBAR_HEADER_VUZE = "header.vuze";

	String SIDEBAR_HEADER_TRANSFERS = "header.transfers";

	String SIDEBAR_HEADER_DISCOVERY = "header.discovery";

	String SIDEBAR_HEADER_DEVICES = "header.devices";

	String SIDEBAR_HEADER_DVD = "header.dvd";

	String SIDEBAR_HEADER_PLUGINS = "header.plugins";

	String SIDEBAR_SECTION_PLUGINS = "Plugins";

	String SIDEBAR_SECTION_ABOUTPLUGINS = "About.Plugins";

	String SIDEBAR_SECTION_LIBRARY = "Library";

	String SIDEBAR_SECTION_GAMES = "Games";

	String SIDEBAR_SECTION_BETAPROGRAM = "BetaProgramme";

	String SIDEBAR_SECTION_LIBRARY_DL = "LibraryDL";

	String SIDEBAR_SECTION_LIBRARY_CD = "LibraryCD";

	String SIDEBAR_SECTION_TAGS = "TagsOverview";

	String SIDEBAR_SECTION_TAG_DISCOVERY = "TagDiscovery";

	String SIDEBAR_SECTION_CHAT = "ChatOverview";

	String SIDEBAR_SECTION_LIBRARY_UNOPENED = "LibraryUnopened";
	
	String SIDEBAR_SECTION_TORRENT_DETAILS = "DMDetails";

	String SIDEBAR_SECTION_WELCOME = "Welcome";

	String SIDEBAR_SECTION_PLUS = "Plus";

	String SIDEBAR_SECTION_SUBSCRIPTIONS = "Subscriptions";

	String SIDEBAR_SECTION_DEVICES = "Devices";

	String SIDEBAR_SECTION_BURN_INFO = "BurnInfo";

	String SIDEBAR_SECTION_ACTIVITIES = "Activity";

	String SIDEBAR_SECTION_SEARCH = "Search";
	
	String SIDEBAR_SECTION_ALLPEERS = "AllPeersView";
	
	String SIDEBAR_SECTION_TORRENT_OPTIONS = "TorrentOptionsView";

	String SIDEBAR_SECTION_MY_SHARES = "MySharesView";

	String SIDEBAR_SECTION_MY_TRACKER = "MyTrackerView";
	
	String SIDEBAR_SECTION_CLIENT_STATS = "ClientStatsView";

	String SIDEBAR_SECTION_LOGGER = "LoggerView";

	String SIDEBAR_SECTION_CONFIG = "ConfigView";

	String SIDEBAR_SECTION_ARCHIVED_DOWNLOADS = "ArchivedDownloads";
	
	String SIDEBAR_SECTION_DOWNLOAD_HISTORY = "DownloadHistory";

	boolean showEntryByID(String id);

	boolean showEntryByID(String id, Object datasource);

		/**
		 * If you prefix the 'preferedAfterID' string with '~' then the operation will actually
		 * switch to 'preferedBeforeID'
		 * @param parentID
		 * @param id
		 * @param configID
		 * @param title
		 * @param titleInfo
		 * @param params
		 * @param closeable
		 * @param preferedAfterID
		 * @return
		 */
        MdiEntry createEntryFromSkinRef(String parentID, String id,
                                        String configID, String title, ViewTitleInfo titleInfo, Object params,
                                        boolean closeable, String preferedAfterID);

	MdiEntry getCurrentEntry();

	MdiEntry getEntry(String id);

	void addListener(MdiListener l);

	void removeListener(MdiListener l);

	void addListener(MdiEntryLoadedListener l);

	void removeListener(MdiEntryLoadedListener l);

	boolean isVisible();

	void closeEntry(String id);

	MdiEntry[] getEntries();

	void registerEntry(String id, MdiEntryCreationListener l);
	
	void registerEntry(String id,
                       MdiEntryCreationListener2 mdiEntryCreationListener2);

	void deregisterEntry(String id, MdiEntryCreationListener l);
	
	void deregisterEntry(String id,
                         MdiEntryCreationListener2 mdiEntryCreationListener2);

	boolean entryExists(String id);

	void removeItem(MdiEntry entry);

	/**
	 * When an entry can not be opened (ie. creation listener isn't registered yet),
	 * call this to store your open request
	 */
    void setEntryAutoOpen(String id, Object datasource);
	
	void removeEntryAutoOpen(String id);

	void showEntry(MdiEntry newEntry);

	void informAutoOpenSet(MdiEntry entry, Map<String, Object> autoOpenInfo);

	boolean loadEntryByID(String id, boolean activate);

	void setPreferredOrder(String[] preferredOrder);

	String[] getPreferredOrder();

	MdiEntry createHeader(String id, String title, String preferredAfterID);

	List<MdiEntry> getChildrenOf(String id);

	boolean loadEntryByID(String id, boolean activate,
                          boolean onlyLoadOnce, Object datasource);

	/**
	 * @return
	 * @since 5.6.0.1
	 */
    int getEntriesCount();
	
	boolean isDisposed();

}
