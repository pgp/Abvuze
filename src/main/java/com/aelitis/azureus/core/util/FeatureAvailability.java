/*
 * Created on Nov 6, 2007
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


package com.aelitis.azureus.core.util;

import com.aelitis.azureus.core.versioncheck.VersionCheckClient;

public class 
FeatureAvailability 
{
	private static final long	FT_DISABLE_REQUEST_LIMITING			= 0x0000000000000001;
	private static final long	FT_DISABLE_PEER_GENERAL_RECONNECT	= 0x0000000000000002;
	private static final long	FT_DISABLE_PEER_UDP_RECONNECT		= 0x0000000000000004;
	private static final long	FT_AUTO_SPEED_DEFAULT_CLASSIC		= 0x0000000000000008;
	private static final long	FT_DISABLE_RCM						= 0x0000000000000010;
	private static final long	FT_DISABLE_DHT_REP_V2				= 0x0000000000000020;
	private static final long	FT_DISABLE_MAGNET_SL				= 0x0000000000000040;
	private static final long	FT_ENABLE_ALL_FE_CLIENTS			= 0x0000000000000080;
	
	private static final long	FT_ENABLE_INTERNAL_FEATURES			= 0x0000000000000100;
	
	private static final long	FT_TRIGGER_SPEED_TEST_V1			= 0x0000000000000200;
	private static final long	FT_DISABLE_GAMES					= 0x0000000000000400;
	private static final long	FT_DISABLE_MAGNET_MD				= 0x0000000000000800;
	
	private static final VersionCheckClient vcc = VersionCheckClient.getSingleton();
	
	/*
	public static final boolean 
	ENABLE_PLUS()
	{
		return( true );
	}
	*/
	
	public static boolean
	areInternalFeaturesEnabled()
	{

		return(vcc.getFeatureFlags() & FT_ENABLE_INTERNAL_FEATURES) != 0;
	}
	
	public static boolean
	isRequestLimitingEnabled()
	{

		return (vcc.getFeatureFlags() & FT_DISABLE_REQUEST_LIMITING) == 0;
	}
	
	public static boolean
	isGeneralPeerReconnectEnabled()
	{

		return (vcc.getFeatureFlags() & FT_DISABLE_PEER_GENERAL_RECONNECT) == 0;
	}
	
	public static boolean
	isUDPPeerReconnectEnabled()
	{

		return (vcc.getFeatureFlags() & FT_DISABLE_PEER_UDP_RECONNECT) == 0;
	}
	
	public static boolean
	isAutoSpeedDefaultClassic()
	{

		return (vcc.getFeatureFlags() & FT_AUTO_SPEED_DEFAULT_CLASSIC) != 0;
	}
	
	public static boolean
	isRCMEnabled()
	{

		return (vcc.getFeatureFlags() & FT_DISABLE_RCM) == 0;
	}
	
	public static boolean
	isDHTRepV2Enabled()
	{

		return (vcc.getFeatureFlags() & FT_DISABLE_DHT_REP_V2) == 0;
	}
	
	public static boolean
	isMagnetSLEnabled()
	{

		return (vcc.getFeatureFlags() & FT_DISABLE_MAGNET_SL) == 0;
	}
	
	public static boolean
	isMagnetMDEnabled()
	{

		return (vcc.getFeatureFlags() & FT_DISABLE_MAGNET_MD) == 0;
	}
	
	public static boolean
	allowAllFEClients()
	{

		return (vcc.getFeatureFlags() & FT_ENABLE_ALL_FE_CLIENTS) != 0;
	}
	
	public static boolean
	triggerSpeedTestV1()
	{
		return (vcc.getFeatureFlags() & FT_TRIGGER_SPEED_TEST_V1) != 0;
	}

	public static boolean
	isGamesEnabled()
	{
		return (vcc.getFeatureFlags() & FT_DISABLE_GAMES) == 0;
	}
}
