/*
 * Created on Feb 4, 2010
 * Created by Paul Gardner
 * 
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


package org.gudy.azureus2.plugins.utils;

import org.gudy.azureus2.plugins.PluginException;


public interface 
FeatureManager 
{
	Licence[]
	getLicences();
	
	Licence[]
	createLicences(
            String[] feature_ids)
	
		throws PluginException;
	
	Licence
	addLicence(
            String licence_key)
	
		throws PluginException;
	
	FeatureDetails[]
	getFeatureDetails(
            String feature_id);
	
		// feature present and not expired
	
	boolean
	isFeatureInstalled(
            String feature_id);
	
	void
	refreshLicences();
	
	void
	registerFeatureEnabler(
            FeatureEnabler enabler);
	
	void
	unregisterFeatureEnabler(
            FeatureEnabler enabler);
	
	void
	addListener(
            FeatureManagerListener listener);
	
	void
	removeListener(
            FeatureManagerListener listener);

	
	interface
	Licence
	{
		int LS_PENDING_AUTHENTICATION	= 1;
		int LS_AUTHENTICATED			= 2;
		int LS_INVALID_KEY				= 3;
		int LS_CANCELLED				= 4;
		int LS_REVOKED					= 5;
		int LS_ACTIVATION_DENIED		= 6;
		
		int
		getState();
		
		String
		getKey();
		
		String
		getShortID();
		
		FeatureDetails[]
		getFeatures();
		
		boolean
		isFullyInstalled();
		
		void
		retryInstallation();
		
		void
		addInstallationListener(
                LicenceInstallationListener listener);
		
		void
		removeInstallationListener(
                LicenceInstallationListener listener);
		
		void
		remove();
		
		interface
		LicenceInstallationListener
		{
			void
			start(
                    String licence_key);
			
			void
			reportActivity(
                    String licence_key,
                    String install,
                    String activity);
			
			void
			reportProgress(
                    String licence_key,
                    String install,
                    int percent);
			
			void
			complete(
                    String licence_key);
			
			void
			failed(
                    String licence_key,
                    PluginException error);
		}
	}
	
	interface
	FeatureEnabler
	{
		Licence[]
       	getLicences();
		
		Licence[]
		createLicences(
                String[] feature_ids)
		
			throws PluginException;
		
       	Licence
       	addLicence(
                String licence_key);
       	
       	void
       	refreshLicences();
       	
    	void
    	addListener(
                FeatureManagerListener listener);
    	
    	void
    	removeListener(
                FeatureManagerListener listener);
	}
	
	interface
	FeatureDetails
	{
		String	PR_PUBLIC_KEY				= "PublicKey";				// String
		String	PR_VALID_UNTIL				= "ValidUntil";				// Long
		String	PR_OFFLINE_VALID_UNTIL		= "OfflineValidUntil";		// Long
		String	PR_IS_INSTALL_TIME			= "IsInstallTime";			// Long (0=false)
		String	PR_IS_TRIAL					= "IsTrial";				// Long (0=false)
		String	PR_TRIAL_USES_LIMIT			= "TrialUsesLimit";			// Long
		String	PR_TRIAL_USES_FAIL_COUNT	= "TrialUsesFailCount";		// Long
		String	PR_TRIAL_USES_REMAINING		= "TrialUsesRemaining";		// Long
		String	PR_REQUIRED_PLUGINS			= "Plugins";				// String: comma separated plugin ids
		String	PR_FINGERPRINT				= "Fingerprint";			// String
		String	PR_RENEWAL_KEY				= "RenewalKey";				// String
		
		Licence
		getLicence();
		
		String
		getID();
		
			/**
			 * Returns true if offline expired or overall expired. 
			 * NOT to be used by verified plugins, they must do the check explicitly using the
			 * signed properties
			 * @return
			 */

            boolean
		hasExpired();
		
		byte[]
		getEncodedProperties();
		
		byte[]
		getSignature();
		
		Object
		getProperty(
                String propery_name);
		
		void
		setProperty(
                String property_name,
                Object property_value);
	}
	
	interface
	FeatureManagerListener
	{
		void
		licenceAdded(
                Licence licence);
		
		void
		licenceChanged(
                Licence licence);
		
		void
		licenceRemoved(
                Licence licence);
	}
}
