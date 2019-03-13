/*
 * Created on Feb 4, 2009
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


package com.aelitis.azureus.core.devices;

import org.gudy.azureus2.plugins.disk.DiskManagerFileInfo;

public interface 
TranscodeJob 
{
	int	ST_QUEUED		= 0;
	int	ST_RUNNING		= 1;
	int	ST_PAUSED		= 2;
	int	ST_COMPLETE		= 3;
	int	ST_CANCELLED	= 4;
	int	ST_FAILED		= 5;
	int	ST_STOPPED		= 6;
	int	ST_REMOVED		= 7;

	String
	getName();
	
	TranscodeTarget
	getTarget();
	
	TranscodeProfile
	getProfile();
	
	DiskManagerFileInfo
	getFile();
	
	TranscodeFile
	getTranscodeFile();
	
	int
	getTranscodeRequirement();
	
	int
	getIndex();
	
	int
	getState();
	
	long
	getDownloadETA();
	
	int
	getPercentComplete();
	
	long
	getETASecs();
	
	String
	getETA();
	
	String
	getError();
	
	void
	setEnableAutoRetry(
            boolean enabled);
	
	boolean
	getEnableAutoRetry();
	
	void
	setPreferDirectInput(
            boolean prefer);
	
	boolean
	getPreferDirectInput();
	
	boolean
	canPause();
	
	void
	pause();
	
	void
	resume();
	
	void
	queue();
	
	void
	stop();
	
	void
	remove()
	
		throws TranscodeActionVetoException;
	
	void
	removeForce();
	
	void
	moveUp();
	
	void
	moveDown();

	long
	getProcessTime();
	
	void
	analyseNow(
            TranscodeAnalysisListener listener)
	
		throws TranscodeException;
}
