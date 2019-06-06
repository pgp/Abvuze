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

package org.gudy.azureus2.platform.macosx;

import org.gudy.azureus2.core3.logging.LogEvent;
import org.gudy.azureus2.core3.logging.Logger;
import org.gudy.azureus2.core3.util.SystemProperties;
import org.gudy.azureus2.platform.PlatformManager;
import org.gudy.azureus2.platform.common.PlatformManagerBaseUpdateChecker;
import org.gudy.azureus2.plugins.update.Update;
import org.gudy.azureus2.plugins.update.UpdateChecker;
import org.gudy.azureus2.plugins.update.UpdateInstaller;
import org.gudy.azureus2.plugins.utils.resourcedownloader.ResourceDownloader;

import java.io.File;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PlatformManagerUpdateChecker extends PlatformManagerBaseUpdateChecker {

	{
		PLATFORM_TYPE = PlatformManager.PT_MACOSX;
		OSTag = "OSX";
	}

	@Override
	protected void
	installUpdate(
		UpdateChecker		checker,
		Update 				update,
		ResourceDownloader	rd,
		InputStream			data ) {
		
		try {
			data = update.verifyData( data, true );

			rd.reportActivity( "Data verified successfully" );
			
			UpdateInstaller installer = checker.createInstaller();

			try(ZipInputStream zip = new ZipInputStream(data)) {
				ZipEntry entry;

				while ((entry = zip.getNextEntry()) != null) {

					String name = entry.getName();

					if (name.toLowerCase().startsWith("osx/")) {

						// OSX only files

						name = name.substring(4);

						// skip the directory entry

						if ( name.length() > 0 ){

							rd.reportActivity("Adding update action for '" + name + "'");

							if (Logger.isEnabled())
								Logger.log(new LogEvent(LOGID,
										"PlatformManager:"+OSTag+" adding action for '" + name + "'"));

							// handle sub-dirs

							String	resource_name = name.replaceAll( "/", "-" );

							installer.addResource( resource_name, zip, false );

							String appDir = installer.getInstallDir() +
									File.separator + SystemProperties.getApplicationName() + ".app";
							String contentsResourceJava = "Contents/Resources/Java/";
							if (name.startsWith(contentsResourceJava) &&
									!new File(appDir, "Contents").exists()) {
								// trying to install something into the "Java" dir
								// New installs have the "Java" dir as the Install dir.
								appDir = installer.getInstallDir();
								name = name.substring(contentsResourceJava.length());
							}

							String target = appDir + File.separator + name;

							installer.addMoveAction( resource_name, target );

							if ( name.endsWith( ".jnilib" ) || name.endsWith( "JavaApplicationStub" )){

								installer.addChangeRightsAction( "755", target );
							}
						}
					}
				}

				update.complete( true );
			}
		}
		catch (Throwable e) {

			update.complete( false );
			
			rd.reportActivity("Update install failed:" + e.getMessage());
			
		}
	}
}
