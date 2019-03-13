/*
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
 
package org.gudy.azureus2.plugins.ui.tables;

/** 
 * This interface provides access to an Azureus table column.
 */
public interface TableColumn {
	int MENU_STYLE_HEADER = 1;

	int MENU_STYLE_COLUMN_DATA = 2;

  /** The cells in this column display textual information. */
  int TYPE_TEXT = 1;
  /** The graphic type, providing access to graphic specific functions in 
   * {@link TableCell}.
   */
  int TYPE_GRAPHIC = 2;
  /**
   * The cells in this column display only textual information, and does not
   * set any other visible properties of cell (background, foreground, icon, 
   * etc).
   * 
   * Using this type allows azureus to call refresh less, and saves on CPU.
   */
  int TYPE_TEXT_ONLY = 3;
  
  /** leading alignment */
  int ALIGN_LEAD = 1;
  /** trailing alignment */
  int ALIGN_TRAIL = 2;
  /** center alignment */
  int ALIGN_CENTER = 3;
  /** top align */
  int ALIGN_TOP = 4;
  /** bottom align */
  int ALIGN_BOTTOM = 8;

  /** For {@link #setPosition(int)}. Make column invisible initially. */
  int POSITION_INVISIBLE = -1;
  /** For {@link #setPosition(int)}. Make column the last column initially. */
  int POSITION_LAST = -2;
  
  /** Trigger refresh listeners every time a graphic cycle occurs (set by user) */
  int INTERVAL_GRAPHIC = -1;
  /** Trigger refresh listeners every time a GUI update cycle occurs (set by user) */
  int INTERVAL_LIVE = -2;
  /** Trigger refresh only when the cell/row becomes invalid */
  int INTERVAL_INVALID_ONLY = -3;
  
  String CAT_ESSENTIAL = "essential";
  String CAT_SHARING = "sharing";
  String CAT_TRACKER = "tracker";
  String CAT_TIME = "time";
  String CAT_SWARM = "swarm";
  String CAT_CONTENT = "content";
  String CAT_PEER_IDENTIFICATION = "identification";
  String CAT_PROTOCOL = "protocol";
  String CAT_BYTES = "bytes";
  String CAT_SETTINGS = "settings";
  String CAT_CONNECTION = "connection";
  String CAT_PROGRESS = "progress";

  	// user-data properties
  
  String UD_FORCE_VISIBLE = "ud_fv";	// Long
  
  /** Initialize a group of variables all at once.  Saves on individual setXxx.
   *
   * @param iAlignment See {@link #setAlignment(int)}
   * @param iPosition See {@link #setPosition(int)}
   * @param iWidth See {@link #setWidth(int)}
   * @param iInterval See {@link #setRefreshInterval(int)}
   * 
   * @since 2.1.0.0
   */
  void initialize(int iAlignment, int iPosition,
                  int iWidth, int iInterval);


  /** Initialize a group of variables all at once.  Saves on individual setXxx.
   *
   * @param iAlignment See {@link #setAlignment(int)}
   * @param iPosition See {@link #setPosition(int)}
   * @param iWidth See {@link #setWidth(int)}
   * 
   * @since 2.1.0.0
   */
  void initialize(int iAlignment, int iPosition, int iWidth);
  
  /**
   * The logical name of the column. This was set via
   * {@link TableManager#createColumn} and can not be changed.
   *
   * @return the column name (identification)
   * 
   * @since 2.1.0.0
   */
  String getName();

  /**
   * Returns the user's column name override if it exists
   * @return
   * @since 5.0.0.1
   */

  String getNameOverride();
  
  void setNameOverride(String name);
  
  /** Which table the column will be visible in.  This was set via
   * {@link TableManager#createColumn} and can not be changed.
   *
   * @return {@link TableManager}.TABLE_* constant(s)
   * 
   * @since 2.1.0.0
   */
  String getTableID();

  /** The type of the contained data.<br>
   * Current supported types are long, string, and graphic.
   * <P>
   * NOTE: This MUST be set BEFORE adding the column to a table.
   * <br>
   * The default type is {@link #TYPE_TEXT_ONLY}.
   *
   * @param type {@link #TYPE_TEXT}, {@link #TYPE_TEXT_ONLY}, {@link #TYPE_GRAPHIC}
   * 
   * @since 2.1.0.0
   */
  void setType(int type);

  /** Returns the type of the contained data.
   *
   * @return type TYPE_TEXT, or TYPE_GRAPHIC
   * 
   * @since 2.1.0.0
   */
  int getType();
  
  /** The column size.
   * <P>
   * NOTE: This MUST be set BEFORE adding the column to a table.
   *
   * @param width the size in pixels, adjusting for DPI
   * 
   * @since 2.1.0.0
   */
  void setWidth(int unadjustedWidth);

  void setWidthPX(int realPixelWidth);

  /** Returns the column's size
   *
   * @return width in pixels
   * 
   * @since 2.1.0.0
   */
  int getWidth();
  
  /** Location to put the column.  When set before being added to the UI
   * (see {@link TableManager#addColumn}), the supplied value will be used
   * as the default position.  If the user has moved the column previously,
   * the new position will be used, and the default position will be ignored.
   *
   * This function cannot be called after you have added the column to a UI 
   * table.  In the future, setting the position after adding the column to the
   * UI table will result in the column being moved.
   *
   * @param position Column Number (0 based), POSITION_INVISIBLE or POSITION_LAST
   * 
   * @since 2.1.0.0
   */
  void setPosition(int position);
  
  
  /** Returns the position of the column
   *
   * @return Column Number (0 based), POSITION_INVISIBLE or POSITION_LAST
   * 
   * @since 2.1.0.0
   */
  int getPosition();

  /** Orientation of the columns text and header.
   * <P>
   * NOTE: This MUST be set BEFORE adding the column to a table.
   *
   * @param alignment ALIGN_TRAIL, ALIGN_LEAD, or ALIGN_CENTER
   * 
   * @since 2.1.0.0
   */
  void setAlignment(int alignment);

  /** Returns the alignment of the column 
   *
   * @return ALIGN_TRAIL, ALIGN_LEAD, or ALIGN_CENTER
   * 
   * @since 2.1.0.0
   */
  int getAlignment();
  
  /** Set how often the cell receives a refresh() trigger
   *
   * @param interval INTERVAL_GRAPHIC, INTERVAL_LIVE, INTERVAL_INVALID_ONLY
   *                 constants, or an integer based on the user-configurable
   *                 "GUI refresh interval".  For example, specifying 4 will 
   *                 result in a refresh trigger every 4 "GUI refresh intervals"
   * 
   * @since 2.1.0.0
   */
  void setRefreshInterval(int interval);

  /** Returns the refresh interval of the column.
   * The default is INTERVAL_INVALID_ONLY
   *
   * @return INTERVAL_* constant, or a number representing the # of GUI refresh
   *         cycles between each cell refresh call.
   * 
   * @since 2.1.0.0
   */
  int getRefreshInterval();
  
  /**
   * Sets the minimum width that the column can be before other columns
   * start collapsing.  This may not prevent the user from resizing the column
   * smaller than specified.
   * <p>
   * If not set, the width specified on initialize will be the minimum width
   * <p>
   * Not all UIs may have this feature implemented.
   * 
   * @param minwidth new minumum width
   * 
   * @since 3.0.0.7
   */
  void setMinWidth(int minwidth);

  /**
   * Gets the minimum width that the column can be before other columns
   * start collapsing.
   * <p>
   * If not set, the width specified on initialize will be the minimum width
   * <p>
   * Not all UIs may have this feature implemented.
   * 
   * @return minumum width of the column
   * 
   * @since 3.0.0.7
   */
  int getMinWidth();

  /**
   * Sets the maximum width that the column can be
   * <p>
   * Not all UIs may have this feature implemented.
   * 
   * @param maxwidth new maximum width
   *
   * @since 3.0.0.7
   */
  void setMaxWidth(int maxwidth);
  
  /**
   * Gets the maximum width the column can be
   * <p>
   * Not all UIs may have this feature implemented.
   * 
   * @return maximum width of column
   *
   * @since 3.0.0.7
   */
  int getMaxWidth();
  
  /**
   * Sets the minimum and maximum widths in one call
   * <p>
   * Not all UIs may have this min and max limits implemented.
   * 
   * @param min New minimum column width
   * @param max New maximum column width
   *
   * @since 3.0.0.7
   */
  void setWidthLimits(int min, int max);
  
  /**
   * Sets whether the max width is automatically set.  Depending on the UI,
   * automatically setting the max width usually results in the maximum width
   * being grown to fit the largest text set for any cell (past or present).
   * Therefore, the column will never grow larger than the largest text it
   * contains or contained.
   * 
   * @param automaxwidth
   *
   * @since 3.0.0.7
   */
  void setMaxWidthAuto(boolean automaxwidth);
  
  /**
   * Retrieve whether the max width is automatically being set.
   * 
   * @return max width auto setting state
   *
   * @since 3.0.0.7
   */
  boolean isMaxWidthAuto();
  
  /**
   * Sets whether the min width of the column is automatically set.  Depending
   * on the UI, automatically setting the min width usually results in the
   * column never shrinking below the maximum text width ever encountered.
   * 
   * @param autowidth
   *
   * @since 3.0.0.7
   */
  void setMinWidthAuto(boolean autowidth);
  
  /**
   * Retrieve whether the min width is automatically being set
   * 
   * @return min width auto setting state
   *
   * @since 3.0.0.7
   */
  boolean isMinWidthAuto();
  
  /**
   * Sets the preferred width of the column.  When the UI is in auto-expand
   * mode and space is made available, the columns will first fill to their
   * preferred width, then to their maximum width.
   * 
   * @param width New preferred width
   *
   * @since 3.0.0.7
   */
  void setPreferredWidth(int width);
  
  /**
   * Gets the preferred width of the coloumn.
   * 
   * @return preferred width
   *
   * @since 3.0.0.7
   */
  int getPreferredWidth();
  
  /**
   * Retrieves whether the preferred width is automatically calculated.
   * 
   * @return preferred width auto calculation state
   *
   * @since 3.0.0.7
   */
  boolean isPreferredWidthAuto();
  
  /**
   * Sets whether the preferred with is automatically calculated.  An
   * automatically calculated preferred width will be set to the largest
   * text width known to that column
   * 
   * @param auto Preferred Width Auto State
   *
   * @since 3.0.0.7
   */
  void setPreferredWidthAuto(boolean auto);

  /**
   * Gets the visibility of the column 
   * <p>
   * Not all UIs may have this feature implemented.
   * 
   * @return Column visibility
   *
   * @since 3.0.0.7
   */
  boolean isVisible();
  
  /**
   * Associates custom data with the column, usually meant for column-specific settings and stores it across sessions
   * @param key the key under which the value will be stored and serialized
   * @param value should be BEncodable, otherwise it won't be serialized
   */
  void setUserData(String key, Object value);
  void removeUserData(String key);
  
  
  	/**
	 * implement this method if you want to be notified when the stored column
	 * configuration such as user data or GUI-adjustable properties have been
	 * loaded
	 */
    void postConfigLoad();

	/**
	 * implement this method if you want to be notified when the column
	 * configuration is about to be serialized
	 */
    void preConfigSave();
  
  /**
   * 
   * @param key
   * @return data set via setUserData()
   */
  Object getUserData(String key);
  

  String getUserDataString(String key);

  /**
   * Sets the visibility of the column
   * 
   * @param visible New visibility state
   *
   * @since 3.0.0.7
   */
  void setVisible(boolean visible);

  /** Adds a listener that triggers when a TableCell that belongs to this column
   * needs refreshing.
   *
   * @param listener Listener Object to be called when refresh is needed.
   * 
   * @since 2.1.0.0
   */
  void addCellRefreshListener(TableCellRefreshListener listener);
  /** Removed a previously added TableCellRefreshListener
   *
   * @param listener Previously added listener
   * 
   * @since 2.1.0.0
   */
  void removeCellRefreshListener(TableCellRefreshListener listener);


  /** Adds a listener that triggers when a TableCell that belongs to this column
   * is being added.
   *
   * @param listener Listener Object to be called when refresh is needed.
   * 
   * @since 2.1.0.0
   */
  void addCellAddedListener(TableCellAddedListener listener);
  void removeCellAddedListener(TableCellAddedListener listener);

  /** Adds a listener that triggers when a TableCell that belongs to this column
   * is being disposed.
   *
   * @param listener Listener Object to be called when refresh is needed.
   * 
   * @since 2.1.0.0
   */
  void addCellDisposeListener(TableCellDisposeListener listener);
  void removeCellDisposeListener(TableCellDisposeListener listener);

  /** Adds a listener that triggers when a TableCell that belongs to this column
   * has a tooltip action
   *
   * @param listener Listener Object to be called when refresh is needed.
   * 
   * @since 2.1.0.2
   */
  void addCellToolTipListener(TableCellToolTipListener listener);
  void removeCellToolTipListener(TableCellToolTipListener listener);

  /**
   * Adds a listener that triggers when a TableCell that belongs to this column
   * has a mouse event.
   * 
   * @param listener
   * 
   * @since 2.3.0.7
   */
  void addCellMouseListener(TableCellMouseListener listener);
  /** Remove a previously added TableCellMouseListener
  *
  * @param listener Previously added listener
   * @since 2.3.0.7
  */
  void removeCellMouseListener(TableCellMouseListener listener);

  /**
   * A listener is added for every type of cell listener the supplied object 
   * implements
   *  
   * @param listenerObject Object implementing some cell listeneters
   * 
   * @since 2.4.0.0
   */
  void addListeners(Object listenerObject);

  
  /** Invalidate all cells in this column.  The cells will be forced to
   * update on the next refresh.
   * 
   * @since 2.1.0.0
   */
  void invalidateCells();
  
  /**
   * Invalidates any cells which are linked to the given data source object.
   * 
   * @since 3.0.1.5
   */
  void invalidateCell(Object data_source);


  /** Adds a Context Menu item to the column
	 *
	 * @param resourceKey ID of the context menu, which is also used to retreieve
	 *                    the textual name from the plugin language file.
	 * @param menuStyle See MENU_STYLE_* constants (header or data)
	 *
	 * @return a newly created menu item
	 * 
	 * @since 4.2.0.5
	 */
  TableContextMenuItem addContextMenuItem(String resourceKey,
                                          int menuStyle);

	/** Adds a Context Menu item to data section of the column
	 *
	 * @param resourceKey ID of the context menu, which is also used to retreieve
	 *                    the textual name from the plugin language file.
	 *
	 * @return a newly created menu item
	 * 
	 * @since 2.4.0.0
	 */
    TableContextMenuItem addContextMenuItem(String resourceKey);


	/**
	 * Returns whether the column's data will be obfusticated when screen
	 * capturing (for bug reports, etc).
	 * <p>
	 * Currently not fully implemented for plugins  
	 * 
	 * @return Obfusticated value
	 * 
	 * @since 2.4.0.3 
	 */
	boolean isObfusticated();

	/**
	 * Sets whether the column's data will be obfusticated during a screen
	 * capture (for bug reports, etc).
	 *  
	 * @param hideData new state of obfustication
	 * 
	 * @since 2.4.0.3 
	 */
	void setObfustication(boolean hideData);
	
	/**
	 * @since 4005
	 */

    void
	remove();


	/**
	 * @param listener
	 *
	 * @since 4.0.0.5
	 */
	void addColumnExtraInfoListener(TableColumnExtraInfoListener listener);


	/**
	 * @param listener
	 *
	 * @since 4.0.0.5
	 */
	void removeColumnExtraInfoListener(TableColumnExtraInfoListener listener);


	/**
	 * @return
	 *
	 * @since 4.0.0.5
	 */
	Class getForDataSourceType();

	/**
	 * 
	 * @since 4.4.0.7
	 */
    void setIconReference(String iconID, boolean showOnlyIcon);
	
	/**
	 * 
	 * @since 4.4.0.7
	 */
    String getIconReference();
	
	/**
	 * *since 4501
	 * @param mode from Parameter. constants
	 */

    void
	setMinimumRequiredUserMode(
            int mode);
}