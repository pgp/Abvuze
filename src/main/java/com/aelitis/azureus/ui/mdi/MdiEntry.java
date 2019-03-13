/**
 * Created on Aug 13, 2008
 *
 * Copyright (C) Azureus Software, Inc, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA 
 */
 
package com.aelitis.azureus.ui.mdi;

import java.util.Map;

import org.gudy.azureus2.plugins.ui.UIPluginView;
import org.gudy.azureus2.plugins.ui.toolbar.UIToolBarEnablerBase;

import com.aelitis.azureus.ui.common.viewtitleinfo.ViewTitleInfo;


/**
 * @author TuxPaper
 * @created Aug 13, 2008
 *
 */
public interface MdiEntry extends UIPluginView
{

	String getParentID();

	Object getDatasource();

	String getExportableDatasource();

	boolean isCloseable();

	String getId();

	MdiEntryVitalityImage addVitalityImage(String imageID);

	void addListeners(Object objectWithListeners);

	/**
	 * @param l
	 *
	 * @since 4.1.0.3
	 */
	void addListener(MdiCloseListener l);

	void addListener(MdiChildCloseListener l);

	/**
	 * @param l
	 *
	 * @since 4.1.0.3
	 */
	void removeListener(MdiCloseListener l);

	void removeListener(MdiChildCloseListener l);

	/**
	 * @param l
	 *
	 * @since 4.1.0.3
	 */
	void addListener(MdiEntryOpenListener l);

	/**
	 * @param l
	 *
	 * @since 4.1.0.3
	 */
	void removeListener(MdiEntryOpenListener l);

	void addListener(MdiEntryDatasourceListener l);

	void removeListener(MdiEntryDatasourceListener l);

	void setImageLeftID(String string);

	void setCollapseDisabled(boolean b);

	void addListener(MdiEntryDropListener listener);
	
	void removeListener(MdiEntryDropListener listener);

	void setDatasource(Object ds);

	void setLogID(String logID);

	boolean isAdded();

	boolean isDisposed();

	ViewTitleInfo getViewTitleInfo();

	void setViewTitleInfo(ViewTitleInfo viewTitleInfo);

	String getLogID();

	MultipleDocumentInterface getMDI();

	MdiEntryVitalityImage[] getVitalityImages();

	boolean close(boolean forceClose);

	void updateUI();

	void redraw();

	void addListener(MdiEntryLogIdListener l);

	void removeListener(MdiEntryLogIdListener l);

	void hide();

	void requestAttention();
	
	String getTitle();
	
	void setTitle(String title);

	void setTitleID(String titleID);

	String getImageLeftID();

	boolean isExpanded();

	void setExpanded(boolean expanded);
	
	void setDefaultExpanded(boolean defaultExpanded);
	
	void expandTo();

	void setParentID(String id);

	UIToolBarEnablerBase[] getToolbarEnablers();

	void addToolbarEnabler(UIToolBarEnablerBase enabler);

	void removeToolbarEnabler(UIToolBarEnablerBase enabler);

	boolean isSelectable();

	void setSelectable(boolean selectable);

	void setPreferredAfterID(String preferredAfterID);

	String getPreferredAfterID();
	
	void
	setUserData(
            Object key,
            Object value);
	
	Object
	getUserData(
            Object key);

	Map<String, Object> getAutoOpenInfo();
}
