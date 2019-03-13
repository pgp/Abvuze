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

package org.gudy.azureus2.plugins.ui.toolbar;

public interface UIToolBarItem
{
	long STATE_ENABLED = 0x1;

	long STATE_DOWN = 0x2;

	/**
	 * Retrieve the ID of the toolbar item
	 * 
	 * @since 4.6.0.5
	 */
    String getID();

	/**
	 * Return the message bundle ID for the button text
	 * 
	 * @since 4.6.0.5
	 */
    String getTextID();

	/**
	 * Sets the button's text to a messagebundle value looked up using the id
	 * 
	 * @param id
	 * @since 4.6.0.5
	 */
    void setTextID(String id);

	/**
	 * Get the ID of the image used
	 *
	 * @since 4.6.0.5
	 */
    String getImageID();

	/**
	 * Sets the toolbar item to use the specified image
	 *
	 * @since 4.6.0.5
	 */
    void setImageID(String id);

	/**
	 * Returns if the toolbar item is always available (enabled)
	 *
	 * @since 4.6.0.5
	 */
    boolean isAlwaysAvailable();

	long getState();
	
	void setState(long state);

	boolean triggerToolBarItem(long activationType, Object datasource);

	void setDefaultActivationListener(
            UIToolBarActivationListener defaultActivation);
	
	/**
	 * @return Group that item belongs to
	 *
	 * @since 5.0.0.1
	 */
    String getGroupID();

	/**
	 * @param groupID
	 *
	 * @since 5.0.0.1
	 */
    void setGroupID(String groupID);

	/**
	 * @param string
	 *
	 * @since 5.0.0.1
	 */
    void setToolTip(String text);

	/**
	 * @return
	 *
	 * @since 5.0.0.1
	 */
	String getToolTip();
}
