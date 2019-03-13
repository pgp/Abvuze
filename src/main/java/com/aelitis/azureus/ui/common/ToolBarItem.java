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

package com.aelitis.azureus.ui.common;

import org.gudy.azureus2.plugins.ui.toolbar.UIToolBarActivationListener;
import org.gudy.azureus2.plugins.ui.toolbar.UIToolBarItem;

public interface ToolBarItem
	extends UIToolBarItem
{
	interface ToolBarItemListener
	{
		void uiFieldChanged(ToolBarItem item);

		boolean triggerToolBarItem(ToolBarItem item, long activationType,
                                   Object datasource);
	}

	boolean triggerToolBarItem(long activationType, Object datasource);

	void setDefaultActivationListener(
            UIToolBarActivationListener toolBarActivation);

	void setAlwaysAvailable(boolean b);

	String getTooltipID();

	UIToolBarActivationListener getDefaultActivationListener();

	void addToolBarItemListener(ToolBarItemListener l);
	
	void removeToolBarItemListener(ToolBarItemListener l);
}
