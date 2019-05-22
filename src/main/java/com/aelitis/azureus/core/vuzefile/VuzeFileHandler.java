/*
 * Created on May 16, 2008
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


package com.aelitis.azureus.core.vuzefile;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import org.gudy.azureus2.core3.util.BDecoder;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.FileUtil;
import org.gudy.azureus2.plugins.utils.StaticUtilities;
import org.gudy.azureus2.plugins.utils.resourcedownloader.ResourceDownloader;


public class 
VuzeFileHandler 
{
	private static final VuzeFileHandler singleton = new VuzeFileHandler();
	
	public static VuzeFileHandler
	getSingleton()
	{
		return( singleton );
	}
	
	private final List<VuzeFileProcessor>	processors = new CopyOnWriteArrayList<>();
	
	
	protected
	VuzeFileHandler()
	{
	}
	
	public VuzeFile
	loadVuzeFile(
		String	target  )
	{
		try{
			File test_file = new File( target );
	
			if ( test_file.isFile()){
					
				return( getVuzeFile( new FileInputStream( test_file )));
				
			}else{
				
				URL	url = new URI( target ).toURL();
				
				String	protocol = url.getProtocol().toLowerCase();
				
				if ( protocol.equals( "http" ) || protocol.equals( "https" )){
					
					ResourceDownloader rd = StaticUtilities.getResourceDownloaderFactory().create( url );
				
					return( getVuzeFile(rd.download()));
				}
			}
		}catch( Throwable e ){
		}
		
		return( null );
	}
	
	public VuzeFile
	loadVuzeFile(
		byte[]		bytes )
	{
		return( loadVuzeFile( new ByteArrayInputStream( bytes )));
	}
	
	public VuzeFile
	loadVuzeFile(
		InputStream 	is )
	{
		return( getVuzeFile( is ));
	}
	
	public VuzeFile
	loadVuzeFile(
		File 	file )
	{

        try (InputStream is = new FileInputStream(file)) {

            return (getVuzeFile(is));

        } catch (Throwable e) {

            return (null);

        }
	}
	
	protected VuzeFile
	getVuzeFile(
		InputStream		is )
	{
		try{
			BufferedInputStream bis = new BufferedInputStream( is );
			
			try{
				bis.mark(100);
				
				boolean is_json = false;
				
				while( true ){
				
					int next = bis.read();
				
					if ( next == -1 ){
						
						break;
					}
					
					char c = (char)next;
					
					if ( !Character.isWhitespace(c)){
						
						is_json = c == '{';
						
						break;
					}
				}
				
				bis.reset();
				
				Map map;
				
				if ( is_json ){
					
					byte[] bytes = FileUtil.readInputStreamAsByteArray( bis, 2*1024*1024 );
										
					map = BDecoder.decodeFromJSON( new String( bytes, StandardCharsets.UTF_8));
					
				}else{

					map = BDecoder.decode(bis);
				}
				
				return( loadVuzeFile( map ));
				
			}finally{
				
				is.close();
			}
		}catch( Throwable e ){
		}
		
		return( null );
	}
	
	public VuzeFile
	loadVuzeFile(
		Map	map )
	{
		if ( map.containsKey( "vuze" ) && !map.containsKey( "info" )){
					
			return( new VuzeFileImpl( this, (Map)map.get( "vuze" )));
		}
		
		return( null );
	}
	
	public VuzeFile
	loadAndHandleVuzeFile(
		String		target,
		int			expected_types )
	{
		VuzeFile vf = loadVuzeFile( target );
		
		if ( vf == null ){
			
			return( null );
		}
		
		handleFiles( new VuzeFile[]{ vf }, expected_types );
		
		return( vf );
	}
	
	public void
	handleFiles(
		VuzeFile[]		files,
		int				expected_types )
	{

        for (VuzeFileProcessor proc : processors) {

            try {
                proc.process(files, expected_types);

            } catch (Throwable e) {

                Debug.printStackTrace(e);
            }
        }

        for (VuzeFile vf : files) {

            VuzeFileComponent[] comps = vf.getComponents();

            for (VuzeFileComponent comp : comps) {

                if (!comp.isProcessed()) {

                    Debug.out("Failed to handle Vuze file component " + comp.getContent());
                }
            }
        }
	}
	
	public VuzeFile
	create()
	{
		return( new VuzeFileImpl( this ));
	}
			
	public void
	addProcessor(
		VuzeFileProcessor		proc )
	{
		processors.add( proc );
	}
}
