/**
 * Created on Jun 1, 2008
 *
 * Copyright (C) Azureus Software, Inc, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA 
 */

package com.aelitis.azureus.util;

import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.global.GlobalManager;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.util.*;
import org.gudy.azureus2.plugins.disk.DiskManagerFileInfo;
import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.download.DownloadException;
import org.gudy.azureus2.plugins.torrent.Torrent;
import org.gudy.azureus2.pluginsimpl.local.PluginCoreUtils;

import com.aelitis.azureus.activities.VuzeActivitiesEntry;
import com.aelitis.azureus.core.AzureusCoreFactory;
import com.aelitis.azureus.core.devices.DeviceOfflineDownload;
import com.aelitis.azureus.core.devices.TranscodeFile;
import com.aelitis.azureus.core.devices.TranscodeJob;
import com.aelitis.azureus.core.torrent.PlatformTorrentUtils;
import com.aelitis.azureus.ui.selectedcontent.DownloadUrlInfo;
import com.aelitis.azureus.ui.selectedcontent.ISelectedContent;

/**
 * @author TuxPaper
 * @created Jun 1, 2008
 *
 */
public class DataSourceUtils
{
	public static org.gudy.azureus2.core3.disk.DiskManagerFileInfo getFileInfo(
			Object ds) {
		try {
			if (ds instanceof DiskManagerFileInfo) {
				return PluginCoreUtils.unwrap((DiskManagerFileInfo) ds);
			} else if (ds instanceof org.gudy.azureus2.core3.disk.DiskManagerFileInfo) {
				return (org.gudy.azureus2.core3.disk.DiskManagerFileInfo) ds;
			} else if ((ds instanceof ISelectedContent)
					&& ((ISelectedContent) ds).getFileIndex() >= 0) {
				ISelectedContent sc = (ISelectedContent) ds;
				int idx = sc.getFileIndex();
				DownloadManager dm = sc.getDownloadManager();
				return dm.getDiskManagerFileInfoSet().getFiles()[idx];
			} else if (ds instanceof TranscodeJob) {
				TranscodeJob tj = (TranscodeJob) ds;
				try {
					return PluginCoreUtils.unwrap(tj.getFile());
				} catch (DownloadException e) {
				}
			} else if (ds instanceof TranscodeFile) {
				TranscodeFile tf = (TranscodeFile) ds;
				try {
					DiskManagerFileInfo file = tf.getSourceFile();
					return PluginCoreUtils.unwrap(file);
				} catch (DownloadException e) {
				}
			}

		} catch (Exception e) {
			Debug.printStackTrace(e);
		}
		return null;
	}

	public static DownloadManager getDM(Object ds) {
		try {
			if (ds instanceof DownloadManager) {
				return (DownloadManager) ds;
			} else if (ds instanceof VuzeActivitiesEntry) {
				VuzeActivitiesEntry entry = (VuzeActivitiesEntry) ds;
				DownloadManager dm = entry.getDownloadManger();
				if (dm == null) {
					String assetHash = entry.getAssetHash();
					if (assetHash != null && AzureusCoreFactory.isCoreRunning()) {
						GlobalManager gm = AzureusCoreFactory.getSingleton().getGlobalManager();
						dm = gm.getDownloadManager(new HashWrapper(Base32.decode(assetHash)));
						entry.setDownloadManager(dm);
					}
				}
				return dm;
			} else if ((ds instanceof TOTorrent) && AzureusCoreFactory.isCoreRunning()) {
				GlobalManager gm = AzureusCoreFactory.getSingleton().getGlobalManager();
				return gm.getDownloadManager((TOTorrent) ds);
			} else if (ds instanceof ISelectedContent) {
				return getDM(((ISelectedContent)ds).getDownloadManager()); 
			} else 	if (ds instanceof TranscodeJob) {
				TranscodeJob tj = (TranscodeJob) ds;
				try {
					DiskManagerFileInfo file = tj.getFile();
					if (file != null) {
						Download download = tj.getFile().getDownload();
						if (download != null) {
							return PluginCoreUtils.unwrap(download);
						}
					}
				} catch (DownloadException e) {
				}
			} else if (ds instanceof TranscodeFile) {
				TranscodeFile tf = (TranscodeFile) ds;
				try {
					DiskManagerFileInfo file = tf.getSourceFile();
					if (file != null) {
						Download download = file.getDownload();
						if (download != null) {
							return PluginCoreUtils.unwrap(download);
						}
					}
				} catch (DownloadException e) {
				}
			} else if (ds instanceof DeviceOfflineDownload ) {
				return( PluginCoreUtils.unwrap(((DeviceOfflineDownload)ds).getDownload()));
			} else if (ds instanceof Download) {
				return PluginCoreUtils.unwrap((Download) ds);
			} else if (ds instanceof byte[]) {
				byte[] hash = (byte[]) ds;
  			GlobalManager gm = AzureusCoreFactory.getSingleton().getGlobalManager();
  			DownloadManager dm = gm.getDownloadManager(new HashWrapper(hash));
  			if (dm != null) {
  				return dm;
  			}				
			} else if (ds instanceof Object[]) {
					Object[] o = (Object[]) ds;
					return getDM(o[0]);
			}	else if ((ds instanceof String)  && AzureusCoreFactory.isCoreRunning()) {
				String hash = (String) ds;
				try {
	  			GlobalManager gm = AzureusCoreFactory.getSingleton().getGlobalManager();
	  			DownloadManager dm = gm.getDownloadManager(new HashWrapper(Base32.decode(hash)));
	  			if (dm != null) {
	  				return dm;
	  			}
				} catch (Exception e) {
					// ignore
				}
			}
			
			org.gudy.azureus2.core3.disk.DiskManagerFileInfo fileInfo = getFileInfo(ds);
			if (fileInfo != null) {
				return fileInfo.getDownloadManager();
			}


		} catch (Exception e) {
			Debug.printStackTrace(e);
		}
		return null;
	}

	public static TOTorrent getTorrent(Object ds) {
		if (ds instanceof TOTorrent) {
			return (TOTorrent) ds;
		}

		if (ds instanceof DownloadManager) {
			TOTorrent torrent = ((DownloadManager) ds).getTorrent();
			if (torrent != null) {
				return torrent;
			}
		}
		if (ds instanceof VuzeActivitiesEntry) {
			TOTorrent torrent = ((VuzeActivitiesEntry) ds).getTorrent();
			if (torrent == null) {
				// getDM will check hash as well
				DownloadManager dm = getDM(ds);
				if (dm != null) {
					torrent = dm.getTorrent();
				}
			}
			return torrent;
		}

		if (ds instanceof TranscodeFile) {
			TranscodeFile tf = (TranscodeFile) ds;
			try {
				DiskManagerFileInfo file = tf.getSourceFile();
				if (file != null) {
					Download download = file.getDownload();
					if (download != null) {
						Torrent torrent = download.getTorrent();
						if (torrent != null) {
							return PluginCoreUtils.unwrap(torrent);
						}
					}
				}
			} catch (Throwable e) {
			}
		}

		if (ds instanceof TranscodeJob) {
			TranscodeJob tj = (TranscodeJob) ds;
			try {
				DiskManagerFileInfo file = tj.getFile();
				if (file != null) {
					Download download = tj.getFile().getDownload();
					
					if (download != null) {
						Torrent torrent = download.getTorrent();
						if (torrent != null) {
							return PluginCoreUtils.unwrap(torrent);
						}
					}
				}
			} catch (DownloadException e) {
			}
		}
		
		if (ds instanceof DeviceOfflineDownload ){
			Torrent torrent = ((DeviceOfflineDownload) ds).getDownload().getTorrent();
			if (torrent != null) {
				return PluginCoreUtils.unwrap(torrent);
			}
		}

		if (ds instanceof ISelectedContent) {
			return ((ISelectedContent)ds).getTorrent();
		}
		
		if (ds instanceof String) {
			String hash = (String) ds;
			try {
  			GlobalManager gm = AzureusCoreFactory.getSingleton().getGlobalManager();
  			DownloadManager dm = gm.getDownloadManager(new HashWrapper(Base32.decode(hash)));
  			if (dm != null) {
  				return dm.getTorrent();
  			}
			} catch (Exception e) {
				// ignore
			}
		}
		
		DownloadManager dm = getDM(ds);
		if (dm != null) {
			return dm.getTorrent();
		}

		return null;
	}

	/**
	 * @return
	 *
	 * @since 3.0.5.3
	 */
	public static boolean isPlatformContent(Object ds) {
		TOTorrent torrent = getTorrent(ds);
		if (torrent != null) {
			return PlatformTorrentUtils.isContent(torrent, true);
		}
		return ds instanceof VuzeActivitiesEntry;
	}

	public static String getHash(Object ds) {
		try {
			if (ds instanceof VuzeActivitiesEntry) {
				VuzeActivitiesEntry entry = (VuzeActivitiesEntry) ds;
				return entry.getAssetHash();
			} else if (ds instanceof ISelectedContent) {
				return ((ISelectedContent)ds).getHash();
			} else if (ds instanceof byte[]) {
				return Base32.encode((byte[]) ds);
			} else if (ds instanceof String) {
				// XXX Check validility
				return (String) ds;
			}
			
			TOTorrent torrent = getTorrent(ds);
			if (torrent != null) {
				return torrent.getHashWrapper().toBase32String();
			}
		} catch (Exception e) {
			Debug.printStackTrace(e);
		}
		return null;
	}

	/**
	 * @param ds
	 *
	 * @since 3.1.1.1
	 */
	public static DownloadUrlInfo getDownloadInfo(Object ds) {
		if (ds instanceof ISelectedContent) {
			return ((ISelectedContent)ds).getDownloadInfo();
		}
		return null;
	}

}
