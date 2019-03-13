/*
 * Created on Mar 20, 2013
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


package com.aelitis.azureus.core.tag;

public interface 
TagFeatureRateLimit 
	extends TagFeature
{
	int SR_ACTION_QUEUE	= 0;
	int SR_ACTION_PAUSE	= 1;
	int SR_ACTION_STOP	= 2;
	
	int SR_INDIVIDUAL_ACTION_DEFAULT = SR_ACTION_QUEUE;
	int SR_AGGREGATE_ACTION_DEFAULT	 = SR_ACTION_PAUSE;
	
	boolean AT_RATELIMIT_MAX_AGGREGATE_SR_PRIORITY_DEFAULT	= true;
	
	boolean
	supportsTagRates();
	
	boolean
	supportsTagUploadLimit();
	
	boolean
	supportsTagDownloadLimit();
	
	int
	getTagUploadLimit();
	
	void
	setTagUploadLimit(
            int bps);
	
	int
	getTagCurrentUploadRate();
	
	int
	getTagDownloadLimit();
	
	void
	setTagDownloadLimit(
            int bps);
	
	int
	getTagCurrentDownloadRate();
	
	long[]
	getTagSessionUploadTotal();
	
	void
	resetTagSessionUploadTotal();
	
	long[]
	getTagSessionDownloadTotal();

	void
	resetTagSessionDownloadTotal();
	
	long[]
	getTagUploadTotal();
	
	long[]
	getTagDownloadTotal();
	
	void
	setRecentHistoryRetention(
            boolean enable);
	
	int[][]
	getRecentHistory();
	
	int
	getTagUploadPriority();
	
	void
	setTagUploadPriority(
            int priority);
	
		// min share ratio
	
	int
	getTagMinShareRatio();
	
	void
	setTagMinShareRatio(
            int ratio_in_thousandths);
	
		// max share ratio
	
	int
	getTagMaxShareRatio();
	
	void
	setTagMaxShareRatio(
            int ratio_in_thousandths);
	
	int
	getTagMaxShareRatioAction();
	
	void
	setTagMaxShareRatioAction(
            int action);
	
		// aggregate share ratio
	
	int
	getTagAggregateShareRatio();
	
	int
	getTagMaxAggregateShareRatio();
	
	void
	setTagMaxAggregateShareRatio(
            int ratio_in_thousandths);
	
	int
	getTagMaxAggregateShareRatioAction();
	
	void
	setTagMaxAggregateShareRatioAction(
            int action);
	
	boolean
	getTagMaxAggregateShareRatioHasPriority();
	
	void
	setTagMaxAggregateShareRatioHasPriority(
            boolean has_priority);
}
