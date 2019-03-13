/*
 * Created on Aug 6, 2008
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


package com.aelitis.azureus.core.subs;

public interface 
SubscriptionHistory 
{
	int	DEFAULT_CHECK_INTERVAL_MINS		= 120;

	boolean
	isEnabled();
	
	void
	setEnabled(
            boolean enabled);
	
	boolean
	isAutoDownload();
	
	void
	setAutoDownload(
            boolean auto_dl);

	void
	setDetails(
            boolean enabled,
            boolean auto_dl);
	
	void
	deleteResults(
            String[] result_ids);
	
	void
	deleteAllResults();
	
	void
	markAllResultsRead();
	
	void
	markAllResultsUnread();
	
	void
	markResults(
            String[] result_ids,
            boolean[] read);
	
	void
	reset();
	
	long
	getLastScanTime();
	
	long
	getLastNewResultTime();
	
	long
	getNextScanTime();
	
	int
	getNumUnread();
	
	int
	getNumRead();
	
	int
	getCheckFrequencyMins();
	
	void
	setCheckFrequencyMins(
            int mins);
	
	String
	getLastError();
	
	boolean
	isAuthFail();
	
	int
	getConsecFails();
	
	SubscriptionResult[]
	getResults(
            boolean include_deleted);
	
	SubscriptionResult
	getResult(
            String result_id);
	
	boolean
	getDownloadWithReferer();
	
	void
	setDownloadWithReferer(
            boolean b);
	
	int
	getMaxNonDeletedResults();
	
	void
	setMaxNonDeletedResults(
            int max);
	
	String[]
	getDownloadNetworks();
	
	void
	setDownloadNetworks(
            String[] nets);
	
	boolean
	getNotificationPostEnabled();
	
	void
	setNotificationPostEnabled(
            boolean enabled);
}
