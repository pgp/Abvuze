/*
 * File    : TorrentDownloader2Listener.java
 * Created : 27-Feb-2004
 * By      : parg
 * 
 * Azureus - a Java Bittorrent client
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details ( see the LICENSE file ).
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.gudy.azureus2.plugins.utils.resourcedownloader;

/**
 * @author parg
 *
 */

import java.io.*;

public interface 
ResourceDownloaderListener 
{
	void
	reportPercentComplete(
            ResourceDownloader downloader,
            int percentage);
	
	void
	reportAmountComplete(
            ResourceDownloader downloader,
            long amount);
	
	void
	reportActivity(
            ResourceDownloader downloader,
            String activity);
	
		/**
		 * 
		 * @param downloader
		 * @param data
		 * @return return true if the completed download is OK. If false is returned then
		 * if there are alternative download sources they will be tried. If there are no
		 * other sources then the download will be "failed" 
		 */

        boolean
	completed(
                ResourceDownloader downloader,
                InputStream data);
	
	void
	failed(
            ResourceDownloader downloader,
            ResourceDownloaderException e);
}
