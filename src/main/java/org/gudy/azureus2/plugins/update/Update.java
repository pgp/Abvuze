/*
 * Created on 07-May-2004
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

import java.io.InputStream;

import org.gudy.azureus2.plugins.utils.resourcedownloader.*;

public interface 
Update 
{
	int	RESTART_REQUIRED_NO			= 1;
	int	RESTART_REQUIRED_YES		= 2;
	int	RESTART_REQUIRED_MAYBE		= 3;
	
	String
	getName();

	String[]
	getDescription();
	
	String
	getRelativeURLBase();
	
	void
	setRelativeURLBase(
            String base);

	/**
	 * 
	 * @param url URL to load instead of showing the description text)
	 *
	 * @since 3.0.1.7
	 */
    void
	setDescriptionURL(
            String url);
	
	/**
	 * @return null if no description url
	 *
	 * @since 3.0.1.7
	 */
    String
	getDesciptionURL();

	String
	getOldVersion();
	
	String
	getNewVersion();
	
	ResourceDownloader[]
	getDownloaders();
	
	boolean
	isMandatory();
	
	void
	setRestartRequired(
            int restart_required);
	
	int
	getRestartRequired();
	
	void
	setUserObject(
            Object obj);
	
	Object
	getUserObject();
	
		/**
		 * mark the update as complete and inform any listeners
		 */

        void
	complete(
                boolean success);
	
		/**
		 * cancel this update
		 */

        void
	cancel();
	
	boolean
	isCancelled();
	
	boolean
	isComplete();
	
	boolean
	wasSuccessful();
	
	UpdateCheckInstance
	getCheckInstance();
	
	Object
	getDecision(
            int decision_type,    // see UpdateManagerDecisionListener
            String decision_name,
            String decision_description,
            Object decision_data);
	
	InputStream
	verifyData(
            InputStream is,
            boolean force)
	
		throws UpdateException;
	
	void
	addListener(
            UpdateListener l);
	
	void
	removeListener(
            UpdateListener l);
}
