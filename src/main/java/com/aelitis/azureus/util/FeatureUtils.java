/*
 * Created on Jan 17, 2013
 * Created by Paul Gardner
 * 
 * Copyright 2013 Azureus Software, Inc.  All rights reserved.
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


package com.aelitis.azureus.util;

import java.util.Collections;
import java.util.Set;
import java.util.TreeMap;

import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.SystemTime;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.utils.FeatureManager;
import org.gudy.azureus2.plugins.utils.FeatureManager.FeatureDetails;
import org.gudy.azureus2.plugins.utils.FeatureManager.Licence;
import org.gudy.azureus2.pluginsimpl.local.PluginInitializer;
import org.gudy.azureus2.pluginsimpl.local.utils.UtilitiesImpl;

import com.aelitis.azureus.core.AzureusCore;
import com.aelitis.azureus.core.AzureusCoreFactory;
import com.aelitis.azureus.core.AzureusCoreRunningListener;

public class 
FeatureUtils 
{
	private static FeatureManager featman;

	static{
		AzureusCoreFactory.addCoreRunningListener(
			new AzureusCoreRunningListener() 
			{
				public void 
				azureusCoreRunning(
					AzureusCore core) 
				{
					PluginInterface pi = core.getPluginManager().getDefaultPluginInterface();
					featman = pi.getUtilities().getFeatureManager();
				}
			});
	}
		
	public static String getPlusMode() {
		boolean isFull = hasPlusLicence();
		boolean isTrial = hasFullBurn() && !isFull;
		return isFull ? "plus" : isTrial ? "trial" : "free";
	}
	
	public static String getNoAdsMode() {
		boolean isNoAds = hasNoAdLicence();
		return isNoAds ? "no_ads" : "free";
	}
	
	public static boolean hasPlusLicence() {
		if (featman == null) {
			//Debug.out("featman null");
			Set<String> featuresInstalled = UtilitiesImpl.getFeaturesInstalled();
			return featuresInstalled.contains("dvdburn");
		}
		licenceDetails fullFeatureDetails = getPlusFeatureDetails();
		long now = SystemTime.getCurrentTime();
		return fullFeatureDetails != null && fullFeatureDetails.expiry > now
				&& fullFeatureDetails.displayedExpiry > now;
	}

	/**
	 * Only used by old promo plugins (<=2.0)
	 * @deprecated Use {@link #hasPlusLicence()}
	 */
	public static boolean
	hasFullLicence()
	{
		return( hasPlusLicence());
	}
	
	public static boolean
	hasTrialLicence()
	{
		boolean isFull = hasPlusLicence();
		boolean isTrial = hasFullBurn() && !isFull;
		
		return( isTrial );
	}
	
	public static boolean
	hasNoAdLicence()
	{
		if ( featman == null ){
		
			Set<String> featuresInstalled = UtilitiesImpl.getFeaturesInstalled();
			
			return featuresInstalled.contains("no_ads");
		}
		
		licenceDetails details = getNoAdFeatureDetails();
		long now = SystemTime.getCurrentTime();
		return details != null && details.expiry > now
				&& details.displayedExpiry > now;
		
	}
	

	public static licenceDetails getPlusFeatureDetails() {
		return( getFeatureDetails( "dvdburn" ));
	}
	private static licenceDetails getNoAdFeatureDetails() {
		return( getFeatureDetails( "no_ads" ));
	}
	
	public static licenceDetails getPlusOrNoAdFeatureDetails() {
			// plus trumps no-ads
		licenceDetails plusDetails = getPlusFeatureDetails();
		
		long now = System.currentTimeMillis();
		
		if ( 	plusDetails != null &&
				plusDetails.licence.getState() == Licence.LS_AUTHENTICATED && 
				plusDetails.expiry >= now ){
			
			return( plusDetails );
		}
		
		licenceDetails noAdDetails = getNoAdFeatureDetails();
		
		if ( noAdDetails == null ){
			
			return( plusDetails );
			
		}else if ( plusDetails == null ){
			
			return( noAdDetails );
		}
		
		if ( 	noAdDetails.licence.getState() == Licence.LS_AUTHENTICATED && 
				noAdDetails.expiry >= now ){
			
			return( noAdDetails );
		}
		
		return( plusDetails );
	}
	
	private static licenceDetails getFeatureDetails( String feature ) {
		if (featman == null) {
			Debug.out("featman null");
			return null;
		}

		TreeMap<Long, Object[]> mapOrder = new TreeMap<>(
                Collections.reverseOrder());
		FeatureDetails[] featureDetails = featman.getFeatureDetails( feature );
		// if any of the feature details are still valid, we have a full
		for (FeatureDetails fd : featureDetails) {
			Licence licence = fd.getLicence();
			int state = licence.getState();
			if (state == Licence.LS_ACTIVATION_DENIED) {
				mapOrder.put(-1L, new Object[] { licence, 0L});
				continue;
			} else if (state == Licence.LS_CANCELLED) {
				mapOrder.put(-2L, new Object[] { licence, 0L});
				continue;
			} else if (state == Licence.LS_INVALID_KEY) {
				mapOrder.put(-3L, new Object[] { licence, 0L});
				continue;
			} else if (state == Licence.LS_REVOKED) {
				mapOrder.put(-4L, new Object[] { licence, 0L});
				continue;
			} else if (state == Licence.LS_PENDING_AUTHENTICATION) {
				mapOrder.put(-6L, new Object[] { licence, 0L});
				continue;
			}

			long now = SystemTime.getCurrentTime();
			Long lValidUntil = (Long) fd.getProperty(FeatureDetails.PR_VALID_UNTIL);
			Long lValidOfflineUntil = (Long) fd.getProperty(FeatureDetails.PR_OFFLINE_VALID_UNTIL);

			if (lValidUntil == null && lValidOfflineUntil == null) {
				continue;
			}

			long minValidUntil = -1;
			long maxValidUntil = -1;
			if (lValidUntil != null) {
				minValidUntil = maxValidUntil = lValidUntil;
				if (minValidUntil < now) {
					mapOrder.put(minValidUntil, new Object[] { licence, minValidUntil});
					continue;
				}
			}
			if (lValidOfflineUntil != null) {
				long validOfflineUntil = lValidOfflineUntil;
				if (validOfflineUntil < now) {
					mapOrder.put(validOfflineUntil, new Object[] { licence, maxValidUntil});
					continue;
				}
				if (maxValidUntil == -1 || validOfflineUntil > maxValidUntil) {
					maxValidUntil = validOfflineUntil;
				}
			}

			mapOrder.put(maxValidUntil, new Object[] { licence, minValidUntil });
		}

		if (mapOrder.size() == 0) {
			return null;
		}

		Long firstKey = mapOrder.firstKey();
		Object[] objects = mapOrder.get(firstKey);
		Licence licence = (Licence) objects[0];
		return new licenceDetails(firstKey, (Long) objects[1], licence, feature );
	}
	
	public static boolean isTrialLicence(Licence licence) {
		if (featman == null) {
			return false;
		}

		// if any of the FeatureDetails is a trial, return true

		boolean trial = false;
		FeatureDetails[] featureDetails = licence.getFeatures();
		for (FeatureDetails fd : featureDetails) {
			trial = isTrial(fd);
			if (trial) {
				break;
			}
		}

		return trial;
	}

	public static boolean isTrial(FeatureDetails fd) {
		Long lIsTrial = (Long) fd.getProperty(FeatureDetails.PR_IS_TRIAL);
		return lIsTrial != null && lIsTrial != 0;
	}
	
	public static long getRemaining() {
		FeatureDetails[] featureDetails = featman.getFeatureDetails("dvdburn_trial");
		if (featureDetails == null) {
			return 0;
		}
		for (FeatureDetails fd : featureDetails) {
			long remainingUses = getRemainingUses(fd);
			if (remainingUses >= 0) {
				return remainingUses;
			}
		}
		return 0;
	}

	private static long getRemainingUses(FeatureDetails fd) {
		if (fd == null) {
			return 0;
		}
		Long lRemainingUses = (Long) fd.getProperty(FeatureDetails.PR_TRIAL_USES_REMAINING);
		long remainingUses = lRemainingUses == null ? -1
				: lRemainingUses;
		return remainingUses;
	}

	/**
	 * @return
	 */
	public static boolean hasFullBurn() {
		
		PluginInterface pi = PluginInitializer.getDefaultInterface().getPluginState().isInitialisationComplete()
				? AzureusCoreFactory.getSingleton().getPluginManager().getPluginInterfaceByID(
						"azburn_v") : null;
		if (pi == null) {
			// maybe not added yet.. use featman
			Set<String> featuresInstalled = UtilitiesImpl.getFeaturesInstalled();
			return featuresInstalled.contains("dvdburn_trial") && !featuresInstalled.contains("dvdburn");
		}
		return pi.getPluginState().isOperational();
	}
	
	
	public static class licenceDetails {
		private final Licence licence;
		private final long expiry;
		private final long displayedExpiry;
		private final String	feature;
		
		public licenceDetails(long expiry, long displayedExpiry, Licence licence, String feature) {
			this.expiry = expiry;
			this.displayedExpiry = displayedExpiry;
			this.licence = licence;
			this.feature = feature;
		}
		
		public Licence
		getLicence()
		{
			return( licence );
		}
		
		public String 
		getRenewalKey()
		{
			FeatureDetails[] features = licence.getFeatures();
			if (features == null) {
				return null;
			}
			for (FeatureDetails fd : features) {
				Object property = fd.getProperty(FeatureDetails.PR_RENEWAL_KEY);
				if (property instanceof String) {
					return (String) property;
				}
			}
			return null;
		}
		
		public boolean
		isPlus()
		{
			return( feature.equals( "dvdburn" ));
		}
		
		public boolean
		isNoAds()
		{
			return( !isPlus());
		}
		
		public long getExpiryTimeStamp(){
			
			return( expiry );
		}

		public long getExpiryDisplayTimeStamp(){
			if ( expiry == 0 ){
				return 0;
			}
			return displayedExpiry;
		}

		public String getRenewalCode(){
			
			if ( expiry == 0) {
				return null;
			}

			return getRenewalKey();
		}
	}
}
