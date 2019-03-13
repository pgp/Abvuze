/*
 * Created on 11-May-2004
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

package org.gudy.azureus2.plugins.update;

/**
 * @author parg
 *
 */

public interface 
UpdateCheckInstance 
{
	int	UCI_INSTALL			= 1;
	int	UCI_UPDATE			= 2;
	int	UCI_UNINSTALL		= 3;
	
	int	PT_UI_STYLE				= 1;	//Integer
	int	PT_UI_STYLE_DEFAULT		= 1;
	int	PT_UI_STYLE_SIMPLE		= 2;
	int	PT_UI_STYLE_NONE		= 3;
	
	int	PT_UI_PARENT_SWT_COMPOSITE					= 2;	// SWT Composite
	
	int	PT_UI_DISABLE_ON_SUCCESS_SLIDEY				= 3;	// Boolean
	int	PT_CLOSE_OR_RESTART_ALREADY_IN_PROGRESS		= 4;	// Boolean
	int	PT_UNINSTALL_RESTART_REQUIRED				= 5;	// Boolean

		/**
		 * returns one of the above UCI_ constants
		 * @return
		 */

        int
	getType();
	
		/**
		 * returns the name supplied when the instance was created (or "" if it wasn't)
		 * @return
		 */

        String
	getName();
	
	void
	start();
	
	void
	cancel();
	
	boolean
	isCancelled();
	
	UpdateChecker[]
	getCheckers();
	
	Update[]
	getUpdates();
	
	UpdateInstaller
	createInstaller()
		
		throws UpdateException;
	
		/**
		 * Add a further updatable component to this instance. Must be called before
		 * the check process is started
		 * @param component
		 * @param mandatory
		 */

        void
	addUpdatableComponent(
                UpdatableComponent component,
                boolean mandatory);
	
		/**
		 * Access to the update manager
		 * @return
		 */

        UpdateManager
	getManager();
	
	void
	setAutomatic(
            boolean automatic);
	
	boolean
	isAutomatic();
	
	void
	setLowNoise(
            boolean low_noise);
	
	boolean
	isLowNoise();
	
	boolean
	isCompleteOrCancelled();
	
	Object
	getProperty(
            int property_name);
	
	void
	setProperty(
            int property_name,
            Object value);
	
	void
	addDecisionListener(
            UpdateManagerDecisionListener l);
	
	void
	removeDecisionListener(
            UpdateManagerDecisionListener l);
	
	void
	addListener(
            UpdateCheckInstanceListener l);
	
	void
	removeListener(
            UpdateCheckInstanceListener l);
}
