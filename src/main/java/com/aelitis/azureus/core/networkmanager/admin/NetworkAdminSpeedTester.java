/**
* Created on Apr 17, 2007
* Created by Alan Snyder
* Copyright (C) Azureus Software, Inc, All Rights Reserved.
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
*
*/


package com.aelitis.azureus.core.networkmanager.admin;


public interface NetworkAdminSpeedTester 
{

    int TEST_TYPE_UPLOAD_ONLY 			= 0;
    int TEST_TYPE_DOWNLOAD_ONLY 		= 1;

    int[] TEST_TYPES = { TEST_TYPE_UPLOAD_ONLY, TEST_TYPE_DOWNLOAD_ONLY };
    
    int getTestType();

    void setMode(int mode);

    int getMode();
    
    void
    setUseCrypto(
            boolean use_crypto);
    
    boolean
    getUseCrypto();
    
    void addListener(NetworkAdminSpeedTesterListener listener);
    
    void removeListener(NetworkAdminSpeedTesterListener listener);
}
