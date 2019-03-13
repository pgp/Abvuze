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

import java.io.File;


public interface 
TranscodeTarget 
{
	int TRANSCODE_UNKNOWN			= -1;
	int TRANSCODE_NEVER				= 1;
	int TRANSCODE_WHEN_REQUIRED		= 2;
	int TRANSCODE_ALWAYS			= 3;
	
	String
	getID();
	
	Device
	getDevice();
	
	TranscodeFile[]
	getFiles();
	
	File
	getWorkingDirectory();
	
	void
	setWorkingDirectory(
            File directory);
	
	TranscodeProfile[]
	getTranscodeProfiles();
		
	TranscodeProfile
	getDefaultTranscodeProfile()
	
		throws TranscodeException;
	
	void
	setDefaultTranscodeProfile(
            TranscodeProfile profile);
	
	TranscodeProfile
	getBlankProfile();
	
	int
	getTranscodeRequirement();
	
	void
	setTranscodeRequirement(
            int req);
	
	boolean
	getAlwaysCacheFiles();
	
	void
	setAlwaysCacheFiles(
            boolean always_cache);

	boolean
	isTranscoding();
	
	boolean
	isNonSimple();
	
	boolean
	isAudioCompatible(
            TranscodeFile file);
	
	void
	addListener(
            TranscodeTargetListener listener);
	
	void
	removeListener(
            TranscodeTargetListener listener);
}
