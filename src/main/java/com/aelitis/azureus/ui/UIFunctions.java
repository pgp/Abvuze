/*
 * Created on Jun 14, 2006 9:02:55 PM
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
package com.aelitis.azureus.ui;


import java.util.Map;

import org.gudy.azureus2.core3.torrent.impl.TorrentOpenOptions;
import org.gudy.azureus2.core3.util.Constants;

import com.aelitis.azureus.core.AzureusCoreComponent;
import com.aelitis.azureus.core.tag.Tag;
import com.aelitis.azureus.ui.common.updater.UIUpdater;
import com.aelitis.azureus.ui.mdi.MultipleDocumentInterface;

/**
 * @author TuxPaper
 * @created Jun 14, 2006
 *
 */
public interface UIFunctions
	extends AzureusCoreComponent
{
	String	MAIN_WINDOW_NAME 		= System.getProperty( "azureus.window.title", Constants.APP_NAME 		+ " Bittorrent Client" );
	String	MAIN_WINDOW_NAME_PLUS 	= System.getProperty( "azureus.window.title", Constants.APP_PLUS_NAME 	+ " Bittorrent Client" );
		
	int STATUSICON_NONE = 0;

	int STATUSICON_WARNING = 1;

	int STATUSICON_ERROR = 2;

	/** @deprecated Used by azburn */
    int VIEW_MYTORRENTS = 8;
	/** @deprecated Used by rcm */
    int VIEW_CONFIG = 4;
	/** @deprecated Used by rcm */
    int VIEW_DM_DETAILS = 5;

	
	int ACTION_FULL_UPDATE				= 1;	// arg: String - url; response Boolean - ok
	int ACTION_UPDATE_RESTART_REQUEST	= 2;	// arg: Boolean - true->no auto-select response Boolean - ok
	
	int VS_TRAY_ONLY				= 1;		// low-resource minimized state
	int VS_MINIMIZED_TO_TRAY		= 2;		// minimized to tray only
	int VS_MINIMIZED				= 3;		// normal minimized
	int VS_ACTIVE					= 4;		// active

	int
	getUIType();

	/**
	 * Bring main window to the front
	 */
	void bringToFront();

	/**
	 * Bring main window to the front
	 * 
	 * @param tryTricks: try tricks to force it to the top
	 *
	 * @since 3.0.1.7
	 */
	void bringToFront(boolean tryTricks);
	
	int getVisibilityState();
	
	/**
	 * Change/Refresh the language of the UI
	 */
	void refreshLanguage();

	/**
	 * 
	 */
	void refreshIconBar();

	
	/**
	 * @param string
	 */
	void setStatusText(String string);

	void setStatusText(int statustype, String string, UIStatusTextClickListener l);

	/**
	 * Request the UI be shut down.
	 * 
	 * @return true - request granted, UI is being shut down
	 *         false - request denied (example: password entry failed)
	 */
	boolean dispose(boolean for_restart, boolean close_already_in_progress);

	boolean viewURL(String url, String target, int w, int h, boolean allowResize,
			boolean isModal);

	boolean viewURL(String url, String target, double wPct, double hPct,
			boolean allowResize, boolean isModal);

	void viewURL(String url, String target, String sourceRef);


	UIFunctionsUserPrompter getUserPrompter(String title, String text,
                                            String[] buttons, int defaultOption);

	void promptUser(String title, String text, String[] buttons,
                    int defaultOption, String rememberID, String rememberText,
                    boolean bRememberByDefault, int autoCloseInMS, UserPrompterResultListener l);
	
	/**
	 * Retrieves the class that handles periodically updating the UI
	 * 
	 * @return
	 *
	 * @since 3.1.1.1
	 */
    UIUpdater getUIUpdater();

	/**
	 * @deprecated Use {@link #getMDI()}
	 */
	void openView(int viewID, Object datasource);
	
	void doSearch(String searchText );
	
	void doSearch(String searchText, boolean toSubscribe );

	void
	installPlugin(
            String plugin_id,
            String resource_prefix,
            actionListener listener);
	
	/**
	 * 
	 * @param action_id
	 * @param args
	 * @param listener
	 */
    void
	performAction(
            int action_id,
            Object args,
            actionListener listener);
	
	interface 
	actionListener
	{
		void
		actionComplete(
                Object result);
	}

	/**
	 * Retrieve the MDI (Sidebar, TabbedMDI)
	 * @return
	 */
    MultipleDocumentInterface getMDI();

	/**
	 * Might launch the old-school Mr Slidey
	 */
	void forceNotify(int iconID, String title, String text, String details,
			Object[] relatedObjects, int timeoutSecs);	
	
	void
	runOnUIThread(
            int ui_type,
            Runnable runnable);
	
	boolean
	isProgramInstalled(
            String extension,
            String name);
	
	void
	openRemotePairingWindow();
	
	void
	playOrStreamDataSource(
            Object ds,
            String referal,
            boolean launch_already_checked,
            boolean complete_only);

	String 	OTO_DEFAULT_TO_STOPPED			= "defaultStopped";		// Boolean
	boolean OTO_DEFAULT_TO_STOPPED_DEFAULT	= false;
	
	String 	OTO_FORCE_OPEN					= "forceOpen";			// Boolean
	boolean OTO_FORCE_OPEN_DEFAULT			= false;
	
	String 	OTO_SILENT						= "silent";				// Boolean
	boolean OTO_SILENT_DEFAULT				= false;
	
	String 	OTO_HIDE_ERRORS					= "hideErrors";			// Boolean
	boolean OTO_HIDE_ERRORS_DEFAULT			= false;
	
	/**
	 * Opens the Torrent Add Options Window, if configured to
	 * 
	 * @param force  Override configuration, show it!
	 * @return true if torrent was added
	 */
    boolean addTorrentWithOptions(boolean force, TorrentOpenOptions torrentOptions);

	boolean addTorrentWithOptions(TorrentOpenOptions torrentOptions, Map<String, Object> addOptions);

	void showErrorMessage(String keyPrefix, String details, String[] textParams);
	
	void showCreateTagDialog(TagReturner tagReturner);
	
	int adjustPXForDPI(int px);
	
	interface
	TagReturner 
	{
		void returnedTags(Tag[] tags);
	}
}
