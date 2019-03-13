/**
 * File    : TableCellCore.java
 * Created : 2004/May/14
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details ( see the LICENSE file ).
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.aelitis.azureus.ui.common.table;

import org.gudy.azureus2.plugins.ui.tables.TableCell;
import org.gudy.azureus2.plugins.ui.tables.TableCellMouseEvent;
import org.gudy.azureus2.plugins.ui.tables.TableCellVisibilityListener;

/** 
 * Core Table Cell functions are those available to plugins plus
 * some core-only functions.  The core-only functions are listed here.
 *
 * @see org.gudy.azureus2.ui.swt.views.table.impl.TableCellImpl
 */
public interface TableCellCore extends TableCell, Comparable
{
	int TOOLTIPLISTENER_HOVER = 0;

	int TOOLTIPLISTENER_HOVERCOMPLETE = 1;

	void invalidate(boolean bMustRefresh);

	/** 
	 * Refresh the cell
	 * 
	 * @param bDoGraphics Whether to update graphic cells 
	 */
    boolean refresh(boolean bDoGraphics);

	/** 
	 * Refresh the cell, including graphic types 
	 */
    boolean refresh();

	/**
	 * Refresh the cell.  This method overide takes a bRowVisible paramater
	 * and a bCellVisible parameter in order to reduce the number of calls to 
	 * TableRow.isVisible() and calculations of cell visibility.
	 * 
	 * @param bDoGraphics Whether to update graphic cells
	 * @param bRowVisible Assumed visibility state of row
	 * @param bCellVisible Assumed visibility state of the cell
	 */
    boolean refresh(boolean bDoGraphics, boolean bRowVisible,
                    boolean bCellVisible);

	/**
	 * Refresh the cell.  This method override takes a bRowVisible parameter in
	 * order to reduce the number of calls to TableRow.isVisible() in cases where
	 * multiple cells on the same row are being refreshed.
	 * 
	 * @param bDoGraphics Whether to update graphic cells
	 * @param bRowVisible Visibility state of row
	 */
    boolean refresh(boolean bDoGraphics, boolean bRowVisible);

	/** 
	 * dispose of the cell 
	 */
    void dispose();

	/** 
	 * Retrieve whether the cell need any paint calls (graphic)
	 *
	 * @return whether the cell needs painting
	 */
    boolean needsPainting();

	/** 
	 * Location of the cell has changed 
	 */
    void locationChanged();

	/** 
	 * Retrieve the row that this cell belongs to
	 *
	 * @return the row that this cell belongs to
	 */
    TableRowCore getTableRowCore();
	
	TableColumnCore getTableColumnCore();

	/**
	 * Trigger all the tooltip listeners that have been added to this cell
	 * 
	 * @param type {@link #TOOLTIPLISTENER_HOVER}, {@link #TOOLTIPLISTENER_HOVERCOMPLETE}
	 */
    void invokeToolTipListeners(int type);

	/**
	 * Trigger all the mouse listeners that have been addded to this cell
	 * 
	 * @param event event to trigger
	 */
    void invokeMouseListeners(TableCellMouseEvent event);

	/**
	 * Trigger all the visibility listeners that have been added to this cell.<BR>
	 * 
	 * @param visibility See {@link TableCellVisibilityListener}.VISIBILITY_* constants
	 */
    void invokeVisibilityListeners(int visibility, boolean invokeColumnListeners);

	/**
	 * Sets whether the cell will need updating when it's visible again
	 * 
	 * @param upToDate
	 */
    void setUpToDate(boolean upToDate);

	/**
	 * Returns whether the cell will need updating when it's visible again
	 * 
	 * @return
	 */
	boolean isUpToDate();

	/**
	 * Return the text used when generating diagnostics
	 * 
	 * @return
	 */
	String getObfusticatedText();

	/**
	 * Get the cursor ID we are currently using
	 * 
	 * XXX Should NOT be SWT.CURSOR_ constants!
	 * 
	 * @return
	 */
    int getCursorID();

	/**
	 * Set the cursor ID that should be used for the cell
	 * 
	 * @param cursor_hand
	 * @return changed
	 */
    boolean setCursorID(int cursorID);
	
	/**
	 * 
	 * @since 3.0.1.7
	 */
    boolean isMouseOver();

	/**
	 * Returns whether the cell has visually changed since the last refresh call.
	 * Could be used to prevent a refresh, or refresh early.
	 * 
	 * @return visually changed since refresh state
	 */
	boolean getVisuallyChangedSinceRefresh();

	/**
	 * 
	 *
	 * @since 3.0.5.3
	 */
	void refreshAsync();

	/**
	 * 
	 *
	 * @since 3.1.1.1
	 */
	void redraw();

	/**
	 * Sets tooltip to be shown in absence of an explicit one
	 * @param str
	 */
    void setDefaultToolTip(Object tt);

	Object getDefaultToolTip();
}
