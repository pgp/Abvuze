/*
 * Created on 15-Dec-2005
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

package com.aelitis.azureus.plugins.extseed.impl.webseed;

import java.net.URL;
import java.util.*;

import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.torrent.Torrent;

import com.aelitis.azureus.plugins.extseed.ExternalSeedPlugin;
import com.aelitis.azureus.plugins.extseed.ExternalSeedReader;
import com.aelitis.azureus.plugins.extseed.ExternalSeedReaderFactory;

public class 
ExternalSeedReaderFactoryWebSeed
	implements ExternalSeedReaderFactory
{
	public
	ExternalSeedReaderFactoryWebSeed()
	{
	}
	
	public ExternalSeedReader[]
	getSeedReaders(
		ExternalSeedPlugin		plugin,
		Download				download )
	{		
		return( getSeedReaders( plugin, download.getName(), download.getTorrent()));
	}
	
	public ExternalSeedReader[]
   	getSeedReaders(
   		ExternalSeedPlugin		plugin,
   		Torrent					torrent )
  	{	
		return( getSeedReaders( plugin, torrent.getName(), torrent ));
  	}
	
	private ExternalSeedReader[]
   	getSeedReaders(
   		ExternalSeedPlugin		plugin,
   		String					name,
   		Torrent					torrent )
	{				
		try{
			Map	config = new HashMap();
			
			Object	obj = torrent.getAdditionalProperty( "httpseeds" );
			
			if ( obj != null ){
				
				config.put( "httpseeds", obj );
			}
			
			return( getSeedReaders( plugin, name, torrent, config ));
			
		}catch( Throwable e ){
		
			e.printStackTrace();
		}
	
		return( new ExternalSeedReader[0] );  	
	}
	
	public ExternalSeedReader[]
  	getSeedReaders(
  		ExternalSeedPlugin		plugin,
  		Download				download,
  		Map						config )
  	{
		return( getSeedReaders( plugin, download.getName(), download.getTorrent(), config ));
  	}
	
	private ExternalSeedReader[]
  	getSeedReaders(
  		ExternalSeedPlugin		plugin,
  		String					name,
  		Torrent					torrent,
  		Map						config )
	{				
		try{
			Object	obj = config.get( "httpseeds" );
			
				// might as well handle case where there's a single entry rather than a list
			
			if ( obj instanceof byte[] ){
				
                List l = new ArrayList();
                
		        l.add(obj);
		        
		        obj = l;
			}
			
			if ( obj instanceof List ){
				
				List	urls = (List)obj;

				List	readers = new ArrayList();
				
				Object	_params = config.get( "httpseeds-params" );
				
				Map	params = _params instanceof Map?(Map)_params:new HashMap();
				
				Collections.shuffle( urls );

                for (Object url1 : urls) {

                    if (readers.size() > 10) {

                        break;
                    }

                    try {
                        String url_str = new String((byte[]) url1);

                        // avoid java encoding ' ' as '+' as this is not conformant with Apache (for example)

                        url_str = url_str.replaceAll(" ", "%20");

                        URL url = new URL(url_str);

                        String protocol = url.getProtocol().toLowerCase();

                        if (protocol.equals("http")) {

                            readers.add(new ExternalSeedReaderWebSeed(plugin, torrent, url, params));

                        } else {

                            plugin.log(name + ": WS unsupported protocol: " + url);
                        }
                    } catch (Throwable e) {

                        String str = (url1 instanceof byte[]) ? new String((byte[]) url1) : String.valueOf(url1);

                        Debug.out("WS seed invalid: " + str, e);
                    }
                }
				
				ExternalSeedReader[]	res = new ExternalSeedReader[ readers.size() ];
				
				readers.toArray( res );
				
				return( res );
			}
		}catch( Throwable e ){
			
			e.printStackTrace();
		}
		
		return( new ExternalSeedReader[0] );
	}
}
