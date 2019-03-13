/*
 * File    : StatsWriterImpl.java
 * Created : 30-Oct-2003
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

package org.gudy.azureus2.core3.stats.impl;

/**
 * @author parg
 *
 */

import java.util.*;
import java.io.*;

import org.gudy.azureus2.core3.util.*;
import org.gudy.azureus2.core3.config.*;
import org.gudy.azureus2.core3.xml.util.*;
import org.gudy.azureus2.core3.global.*;
import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;
import org.gudy.azureus2.core3.download.*;
import org.gudy.azureus2.core3.peer.*;
import org.gudy.azureus2.core3.torrent.*;

import com.aelitis.azureus.core.AzureusCore;
import com.aelitis.azureus.core.peermanager.utils.PeerClassifier;


public class 
StatsWriterImpl
	extends XUXmlWriter
{
	final AzureusCore		core;
	
	protected
	StatsWriterImpl(
		AzureusCore		_core )
	{			
		core		= _core;
	}
	
	protected void
	write(
		String		file_name )
	
		throws IOException
	{
		try{
			setOutputStream( new FileOutputStream( file_name ));
			
			writeSupport();
		}finally{
			
			closeOutputStream();
		}
	}	
	
	protected void
	write(
		OutputStream		os )
	
		throws IOException
	{
		try{
			setOutputStream( os );
			
			writeSupport();
		}finally{
			
			flushOutputStream();
		}
	}
	
	protected void
	writeSupport()
	{
		writeLineRaw( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );

		boolean	export_peer_stats = COConfigurationManager.getBooleanParameter("Stats Export Peer Details");
		boolean	export_file_stats = COConfigurationManager.getBooleanParameter("Stats Export File Details");
		
		String xsl = COConfigurationManager.getStringParameter( "Stats XSL File" );
		
		if ( xsl.length() > 0 ){
			
			writeLineRaw( "<?xml-stylesheet type=\"text/xsl\" href=\"" + xsl + "\"?>" );
		}
		
		writeLineRaw( "<STATS>");
	
		GlobalManager	global = core.getGlobalManager();
		
		try{
			indent();
		
			writeTag( "AZUREUS_VERSION", Constants.AZUREUS_VERSION );
			
			writeLineRaw( "<GLOBAL>" );
			
			try{
				indent();
				
				GlobalManagerStats	gm_stats = global.getStats();
									
				writeRawCookedAverageTag( "DOWNLOAD_SPEED", gm_stats.getDataReceiveRate() + gm_stats.getProtocolReceiveRate() );
				writeRawCookedAverageTag( "UPLOAD_SPEED", 	gm_stats.getDataSendRate() + gm_stats.getProtocolSendRate() );
				
			}finally{
				
				exdent();
			}
		
			writeLineRaw( "</GLOBAL>" );
			
			writeLineRaw( "<DOWNLOADS>");
			
			try{
				
				indent();
				
				List	_dms = global.getDownloadManagers();
			
				DownloadManager[]	dms = new DownloadManager[_dms.size()];
				
					// sort by position, downloads then seeders
				
				_dms.toArray( dms );
				
				Arrays.sort(
					dms,
					new Comparator()
					{
						public int 
						compare(
							Object o1, 
							Object o2)
						{
							DownloadManager	d1 = (DownloadManager)o1;
							DownloadManager	d2 = (DownloadManager)o2;
							
							int	d1_index 	= d1.getPosition();
							int d2_index	= d2.getPosition();
							
							if ( d1.isDownloadComplete(false) ){
								
								d1_index	+= 1000000;
							}
							
							if ( d2.isDownloadComplete(false) ){
								
								d2_index	+= 1000000;
							}

							return( d1_index - d2_index );
						}
					});

                for (DownloadManager dm : dms) {

                    DownloadManagerStats dm_stats = dm.getStats();

                    writeLineRaw("<DOWNLOAD>");

                    try {
                        indent();

                        writeLineRaw("<TORRENT>");

                        // torrent can be null if broken torrent!

                        TOTorrent torrent = dm.getTorrent();

                        try {
                            indent();

                            writeTag("NAME", dm.getDisplayName());

                            writeTag("TORRENT_FILE", dm.getTorrentFileName());

                            if (torrent != null) {

                                writeTag("HASH", TorrentUtils.nicePrintTorrentHash(torrent, true));

                                writeRawCookedTag("SIZE", torrent.getSize());

                                writeTag("PIECE_LENGTH", torrent.getPieceLength());

                                writeTag("PIECE_COUNT", torrent.getNumberOfPieces());

                                writeTag("FILE_COUNT", torrent.getFiles().length);

                                writeTag("COMMENT", dm.getTorrentComment());

                                writeTag("CREATED_BY", dm.getTorrentCreatedBy());

                                writeTag("CREATION_DATE", torrent.getCreationDate());
                            }

                        } finally {

                            exdent();
                        }

                        writeLineRaw("</TORRENT>");

                        writeTag("DOWNLOAD_STATUS", DisplayFormatters.formatDownloadStatusDefaultLocale(dm));

                        writeTag("DOWNLOAD_DIR", dm.getSaveLocation().toString());

                        if (torrent != null) {

                            if (torrent.isSimpleTorrent()) {

                                writeTag("TARGET_FILE", dm.getSaveLocation().toString());

                            } else {

                                writeTag("TARGET_DIR", dm.getSaveLocation().toString());
                            }
                        }

                        writeTag("TRACKER_STATUS", dm.getTrackerStatus());

                        writeTag("COMPLETED", dm_stats.getCompleted());
                        writeTag("NON_DND_COMPLETED", dm.isDownloadComplete(false));

                        writeRawCookedTag("DOWNLOADED", dm_stats.getTotalDataBytesReceived());
                        writeRawCookedTag("UPLOADED", dm_stats.getTotalDataBytesSent());
                        writeRawCookedTag("DISCARDED", dm_stats.getDiscarded());

                        writeRawCookedAverageTag("DOWNLOAD_SPEED", dm_stats.getDataReceiveRate());
                        writeRawCookedAverageTag("UPLOAD_SPEED", dm_stats.getDataSendRate());
                        writeRawCookedAverageTag("TOTAL_SPEED", dm_stats.getTotalAverage());

                        writeRawCookedAverageTag("DOWNLOAD_SPEED_SMOOTH", dm_stats.getSmoothedDataReceiveRate());
                        writeRawCookedAverageTag("UPLOAD_SPEED_SMOOTH", dm_stats.getSmoothedDataSendRate());

                        writeTag("ELAPSED", dm_stats.getElapsedTime());
                        writeTag("ETA", DisplayFormatters.formatETA(dm_stats.getSmoothedETA()));
                        writeTag("HASH_FAILS", dm_stats.getHashFailCount());
                        writeTag("SHARE_RATIO", dm_stats.getShareRatio());

                        writeTag("TOTAL_SEEDS", dm.getNbSeeds());
                        writeTag("TOTAL_LEECHERS", dm.getNbPeers());

                        if (export_file_stats) {

                            try {
                                writeLineRaw("<FILES>");

                                indent();

                                DiskManagerFileInfo[] files = dm.getDiskManagerFileInfo();

                                for (DiskManagerFileInfo file : files) {

                                    try {
                                        writeLineRaw("<FILE>");

                                        indent();

                                        writeTag("NAME", file.getTorrentFile().getRelativePath());

                                        writeTag("DND", file.isSkipped());

                                        writeRawCookedTag("SIZE", file.getLength());

                                        writeRawCookedTag("DOWNLOADED", file.getDownloaded());

                                    } finally {

                                        exdent();

                                        writeLineRaw("</FILE>");
                                    }
                                }

                            } finally {

                                exdent();

                                writeLineRaw("</FILES>");
                            }
                        }
                        if (export_peer_stats) {

                            try {
                                writeLineRaw("<PEERS>");

                                indent();

                                PEPeerManager pm = dm.getPeerManager();

                                if (pm != null) {

                                    List peers = pm.getPeers();

                                    for (Object peer1 : peers) {

                                        PEPeer peer = (PEPeer) peer1;

                                        PEPeerStats peer_stats = peer.getStats();

                                        byte[] id = peer.getId();

                                        if (id == null) {

                                            continue;
                                        }

                                        try {
                                            String peer_id = PeerClassifier.getPrintablePeerID(id);

                                            peer_id = escapeXML(peer_id);

                                            String type = escapeXML(peer.getClient());

                                            writeLineRaw("<PEER hex_id=\"" + ByteFormatter.encodeString(id) + "\" printable_id=\"" + peer_id + "\" type=\"" + type + "\">");

                                            indent();

                                            writeTag("IP", peer.getIp());

                                            writeTag("IS_SEED", peer.isSeed());

                                            writeRawCookedTag("DOWNLOADED", peer_stats.getTotalDataBytesReceived());
                                            writeRawCookedTag("UPLOADED", peer_stats.getTotalDataBytesSent());

                                            writeRawCookedAverageTag("DOWNLOAD_SPEED", peer_stats.getDataReceiveRate());
                                            writeRawCookedAverageTag("UPLOAD_SPEED", peer_stats.getDataSendRate());

                                        } catch (Throwable e) {

                                            Debug.printStackTrace(e);

                                        } finally {

                                            exdent();

                                            writeLineRaw("</PEER>");
                                        }
                                    }
                                }
                            } finally {

                                exdent();

                                writeLineRaw("</PEERS>");
                            }
                        }
                    } finally {

                        exdent();
                    }

                    writeLineRaw("</DOWNLOAD>");
                }
				
			}finally{
				
				exdent();
			}
				
			writeLineRaw( "</DOWNLOADS>" );
			
		}finally{
			
			exdent();
		}
		writeLineRaw( "</STATS>");	

	}
	
	protected void
	writeRawCookedTag(
		String	tag,
		long	raw )
	{
		writeLineRaw( "<" + tag + ">");
						
		try{
			indent();
							
			writeTag( "TEXT",	DisplayFormatters.formatByteCountToKiBEtc( raw ));
			writeTag( "RAW",	raw);
							
		}finally{
							
			exdent();
		}
						
		writeLineRaw( "</" + tag + ">");
	}
	
	protected void
	writeRawCookedAverageTag(
		String	tag,
		long	raw )
	{
		writeLineRaw( "<" + tag + ">");
						
		try{
			indent();
							
			writeTag( "TEXT",	DisplayFormatters.formatByteCountToKiBEtcPerSec( raw ));
			writeTag( "RAW",	raw);
							
		}finally{
							
			exdent();
		}
								
		writeLineRaw( "</" + tag + ">");
	}
}
