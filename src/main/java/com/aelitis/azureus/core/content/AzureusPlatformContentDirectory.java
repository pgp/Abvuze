/*
 * Created on Dec 19, 2006
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


package com.aelitis.azureus.core.content;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentFactory;
import org.gudy.azureus2.core3.util.Base32;
import org.gudy.azureus2.plugins.disk.DiskManagerFileInfo;
import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.download.DownloadAttributeListener;
import org.gudy.azureus2.plugins.torrent.Torrent;
import org.gudy.azureus2.plugins.torrent.TorrentAttribute;
import org.gudy.azureus2.plugins.utils.resourcedownloader.ResourceDownloader;
import org.gudy.azureus2.plugins.utils.resourcedownloader.ResourceDownloaderFactory;
import org.gudy.azureus2.pluginsimpl.local.PluginCoreUtils;
import org.gudy.azureus2.pluginsimpl.local.PluginInitializer;
import org.gudy.azureus2.pluginsimpl.local.torrent.TorrentImpl;
import org.gudy.azureus2.pluginsimpl.local.utils.resourcedownloader.ResourceDownloaderFactoryImpl;

import com.aelitis.azureus.core.tag.Tag;
import com.aelitis.azureus.core.tag.TagListener;
import com.aelitis.azureus.core.tag.TagManagerFactory;
import com.aelitis.azureus.core.tag.TagType;
import com.aelitis.azureus.core.tag.Taggable;
import com.aelitis.azureus.core.torrent.PlatformTorrentUtils;
import com.aelitis.azureus.util.ConstantsVuze;

/**
 * Used in UPnP for something
 * 
 */
public class 
AzureusPlatformContentDirectory
	implements AzureusContentDirectory
{
	private static boolean registered = false;
	
	private static TorrentAttribute	ta_category;
	
	public static synchronized void
	register()
	{
		if ( !registered ){
		
			registered = true;
			
			ta_category = PluginInitializer.getDefaultInterface().getTorrentManager().getAttribute( TorrentAttribute.TA_CATEGORY );
			
			AzureusContentDirectoryManager.registerDirectory( new AzureusPlatformContentDirectory());
		}
	}
	
	private static List<AzureusContentDirectoryListener>	listeners = new CopyOnWriteArrayList<>();
	
	public AzureusContent
	lookupContent(
		Map		attributes )
	{
		byte[]	hash = (byte[])attributes.get( AT_BTIH );
		
		if ( hash == null ){
			
			return( null );
		}
		
		String	url_str = ConstantsVuze.getDefaultContentNetwork().getTorrentDownloadService( Base32.encode( hash ), null );
		
		ResourceDownloaderFactory rdf = ResourceDownloaderFactoryImpl.getSingleton();
		
		try{
			ResourceDownloader rd = rdf.create( new URL( url_str ));

			try (InputStream is = rd.download()) {
				TOTorrent torrent = TOTorrentFactory.deserialiseFromBEncodedInputStream(is);

				return (new AzureusPlatformContent(new TorrentImpl(torrent)));

			}
			
		}catch( Throwable e ){
			
			e.printStackTrace();
			
			return( null );
		}
	}
	
	public AzureusContentDownload 
	lookupContentDownload(
		Map 		attributes ) 
	{
		byte[]	hash = (byte[])attributes.get( AT_BTIH );
				
		try{
			final Download download = PluginInitializer.getDefaultInterface().getDownloadManager().getDownload(hash);
		
			if ( download == null ){
				
				return( null );
			}
			
			return( 
				new AzureusContentDownload()
				{
					public Download
					getDownload()
					{
						return( download );
					}
					
					public Object
					getProperty(
						String		name )
					{
						return( null );
					}
				});
			
		}catch( Throwable e ){
			
			return( null );
		}
	}
	
	public AzureusContentFile 
	lookupContentFile(
		Map 		attributes) 
	{
		byte[]	hash 	= (byte[])attributes.get( AT_BTIH );
		int		index	= (Integer) attributes.get(AT_FILE_INDEX);
		
		try{

			Download download = PluginInitializer.getDefaultInterface().getDownloadManager().getDownload(hash);
		
			if ( download == null ){
				
				return( null );
			}
			
			Torrent	t_torrent = download.getTorrent();
			
			if ( t_torrent == null ){
				
				return( null );
			}

			String ud_key = "AzureusPlatformContentDirectory" + ":" + index;
			
			AzureusContentFile acf = (AzureusContentFile)download.getUserData( ud_key );
			
			if ( acf != null ){
				
				return( acf );
			}		
			
			final TOTorrent torrent = ((TorrentImpl)t_torrent).getTorrent();
			
			final DiskManagerFileInfo	file = download.getDiskManagerFileInfo(index);

			if ( PlatformTorrentUtils.isContent( torrent, false )){
			
				acf =
					new AzureusContentFile()
					{
						public DiskManagerFileInfo
						getFile()
						{
							return( file );
						}
						
						public Object
						getProperty(
							String		name )
						{
							try{
								switch (name) {
									case PT_DURATION:

										long duration = PlatformTorrentUtils.getContentVideoRunningTime(torrent);

										if (duration > 0) {

											// secs -> millis

											return (duration * 1000);
										}
										break;
									case PT_VIDEO_WIDTH: {

										int[] res = PlatformTorrentUtils.getContentVideoResolution(torrent);

										if (res != null) {

											return ((long) res[0]);
										}
										break;
									}
									case PT_VIDEO_HEIGHT: {

										int[] res = PlatformTorrentUtils.getContentVideoResolution(torrent);

										if (res != null) {

											return ((long) res[1]);
										}
										break;
									}
									case PT_DATE:

										return (file.getDownload().getCreationTime());

									case PT_CATEGORIES:

										try {
											String cat = file.getDownload().getCategoryName();

											if (cat != null && cat.length() > 0) {

												if (!cat.equalsIgnoreCase("Categories.uncategorized")) {

													return (new String[]{cat});
												}
											}
										} catch (Throwable e) {

										}

										return (new String[0]);

									case PT_TAGS:

										List<Tag> tags = TagManagerFactory.getTagManager().getTagsForTaggable(PluginCoreUtils.unwrap(file.getDownload()));

										List<String> tag_names = new ArrayList<>();

										for (Tag tag : tags) {

											if (tag.getTagType().getTagType() == TagType.TT_DOWNLOAD_MANUAL) {

												tag_names.add(tag.getTagName(true));
											}
										}

										return (tag_names.toArray(new String[0]));

									case PT_PERCENT_DONE:

										long size = file.getLength();

										return (size == 0 ? 100 : (1000 * file.getDownloaded() / size));

									case PT_ETA:

										return (getETA(file));
								}
							}catch( Throwable e ){							
							}
							
							return( null );
						}
					};
			}else{
				acf =
						new AzureusContentFile()
						{
							public DiskManagerFileInfo
							getFile()
							{
								return( file );
							}
							
							public Object
							getProperty(
								String		name )
							{
								try{
									switch (name) {
										case PT_DATE:

											return (file.getDownload().getCreationTime());

										case PT_CATEGORIES:

											try {
												String cat = file.getDownload().getCategoryName();

												if (cat != null && cat.length() > 0) {

													if (!cat.equalsIgnoreCase("Categories.uncategorized")) {

														return (new String[]{cat});
													}
												}
											} catch (Throwable e) {

											}

											return (new String[0]);

										case PT_TAGS:


											List<Tag> tags = TagManagerFactory.getTagManager().getTagsForTaggable(PluginCoreUtils.unwrap(file.getDownload()));

											List<String> tag_names = new ArrayList<>();

											for (Tag tag : tags) {

												if (tag.getTagType().getTagType() == TagType.TT_DOWNLOAD_MANUAL) {

													tag_names.add(tag.getTagName(true));
												}
											}

											return (tag_names.toArray(new String[0]));

										case PT_PERCENT_DONE:

											long size = file.getLength();

											return (size == 0 ? 100 : (1000 * file.getDownloaded() / size));

										case PT_ETA:

											return (getETA(file));
									}
								}catch( Throwable e ){							
								}
								
								return( null );
							}
						};
			}
			
			download.setUserData( ud_key, acf );
			
			final AzureusContentFile f_acf = acf;
			
			download.addAttributeListener(
				new DownloadAttributeListener()
				{
					public void 
					attributeEventOccurred(
						Download 			download,
						TorrentAttribute 	attribute, 
						int 				eventType ) 
					{
						fireCatsChanged( f_acf );
					}
				},
				ta_category,
				DownloadAttributeListener.WRITTEN );
			
			TagManagerFactory.getTagManager().getTagType( TagType.TT_DOWNLOAD_MANUAL ).addTagListener( 
					PluginCoreUtils.unwrap( download ),
					new TagListener()
					{	
						public void 
						taggableSync(
							Tag tag) 
						{
						}
						
						public void 
						taggableRemoved(
							Tag 		tag, 
							Taggable 	tagged ) 
						{
							update( tagged );
						}
						
						public void 
						taggableAdded(
							Tag 		tag, 
							Taggable 	tagged ) 
						{
							update( tagged );
						}
						
						private void
						update(
							Taggable	tagged )
						{
							fireTagsChanged( f_acf );
						}
					});
			
			return( acf );
			
		}catch( Throwable e ){
			
			return( null );
		}
	}
	
	protected long
	getETA(
		DiskManagerFileInfo		file )
	{
		try{
			if ( file.getDownloaded() == file.getLength()){
				
				return( 0 );
			}
			
			if ( file.isDeleted() || file.isSkipped()){
				
				return( Long.MAX_VALUE );
			}
		
			long eta = file.getDownload().getStats().getETASecs();
			
			if ( eta < 0 ){
				
				return( Long.MAX_VALUE );
			}
			
			return( eta );
			
		}catch( Throwable e ){
			
			return( Long.MAX_VALUE );
		}
	}
	
	public static void
	fireCatsChanged(
		AzureusContentFile	acf )
	{
		for ( AzureusContentDirectoryListener l: listeners ){
			
			l.contentChanged( acf, AzureusContentFile.PT_CATEGORIES );
		}
	}
	
	public static void
	fireTagsChanged(
		AzureusContentFile	acf )
	{
		for ( AzureusContentDirectoryListener l: listeners ){
			
			l.contentChanged( acf, AzureusContentFile.PT_TAGS );
		}
	}
	
	public void 
	addListener(
		AzureusContentDirectoryListener listener ) 
	{
		listeners.add( listener );
	}
	
	public void 
	removeListener(
		AzureusContentDirectoryListener listener )
	{
		listeners.remove( listener );
	}
	
	private static class
	AzureusPlatformContent
		implements AzureusContent
	{
		private Torrent	torrent;
		
		protected
		AzureusPlatformContent(
			Torrent		_torrent )
		{
			torrent	= _torrent;
		}
		
		public Torrent
		getTorrent()
		{
			return( torrent );
		}
	}
}
