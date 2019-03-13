/*
 * Created on 05-Sep-2005
 * Created by Paul Gardner
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
 *
 */

package org.gudy.azureus2.plugins.ui;

/**
 * 
 * @see UIManager#addUIListener(UIManagerListener)
 * @see org.gudy.azureus2.ui.swt.plugins.UISWTInstance
 */
public interface 
UIManagerListener 
{
	/**
	 * Triggered when an UI is attached
	 * 
	 * @param instance Check with <code>instanceof</code> to see what UI is being
	 *                  attached
	 */
    void
	UIAttached(
            UIInstance instance);
	
	/**
	 * Triggered when an UI is detached
	 * 
	 * @param instance Check with <code>instanceof</code> to see what UI is being
	 *                  dettached
	 */
    void
	UIDetached(
            UIInstance instance);
}
