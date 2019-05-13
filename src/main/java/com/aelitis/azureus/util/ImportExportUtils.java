/*
 * Copyright (C) Azureus Software, Inc, All Rights Reserved.
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

package com.aelitis.azureus.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.gudy.azureus2.core3.util.UrlUtils;
import org.json.simple.JSONArray;

/**
 * Note: There's a similarly defined map processing utility class called
 * {@link MapUtils}.  Since there are differences in implementation, both
 * have been kept until someone goes through each callee and check if it
 * can be switched to use just one of them.
 */
public final class ImportExportUtils {
	
	public static void
	exportString(
		Map		map,
		String	key,
		String	value )
	{
		if ( value != null ){

            map.put( key, value.getBytes(StandardCharsets.UTF_8));

        }
	}
	
	public static void
	exportJSONString(
		Map		map,
		String	key,
		String	value )
	{
		if ( value != null ){
	
			map.put( key, value );
		}
	}
	
	public static String
	importString(
		Map		map,
		String	key,
		String	def )
	{
		String	res = importString( map, key );
		
		if ( res == null ){
			
			res = def;
		}
		
		return( res );
	}
	
	public static String
	importString(
		Map		map,
		String	key )

	{
		if ( map == null ){
			
			return( null );
		}
		
		Object	obj = map.get( key );
		
		if ( obj instanceof String ){
			
			return((String)obj);
			
		}else if ( obj instanceof byte[]){

            return( new String((byte[])obj, StandardCharsets.UTF_8));

        }
		
		return( null );
	}
	
	public static long
	importLong(
		Map		map,
		String	key )
	{
		return( importLong( map, key, 0 ));
	}
	
	public static long
	importLong(
		Map		map,
		String	key,
		long	def )
	{
		if ( map == null ){
			
			return( def );
		}
		
		Object	obj = map.get( key );
		
		if ( obj instanceof Long){
			
			return((Long) obj);
			
		}else if ( obj instanceof String ){
			
			return( Long.parseLong((String)obj));
		}
		
		return( def );
	}

	public static void
	exportLong(
		Map		map,
		String	key,
		long	value )
	{
		map.put( key, value );
	}
	
	public static void
	exportInt(
		Map		map,
		String	key,
		int		value )
	{
		map.put( key, (long) value);
	}
	
	public static int
	importInt(
		Map		map,
		String	key )
	{
		return((int)importLong( map, key, 0 ));
	}
	
	public static int
	importInt(
		Map		map,
		String	key,
		int		def )

	{
		return((int)importLong( map, key, def ));
	}
	
	public static void
	exportFloat(
		Map		map,
		String	key,
		float	value )
	{
		exportString( map, key, String.valueOf( value ));
	}
	
	public static float
	importFloat(
		Map		map,
		String	key,
		float	def )
	{
		String	str = importString( map, key );
		
		if ( str == null ){
			
			return( def );
		}
		
		return( Float.parseFloat( str ));
	}
	
	public static void
	exportBoolean(
		Map		map,
		String	key,
		boolean	value )
	{
		map.put( key, (long) (value ? 1 : 0));
	}
	
	public static boolean
	importBoolean(
		Map		map,
		String	key )
	{
		return( importBoolean( map, key, false ));
	}
	
	public static boolean
	importBoolean(
		Map		map,
		String	key,
		boolean	def )
	{
		if ( map == null ){
			
			return( def );
		}
		
		Object	obj = map.get( key );
		
		if ( obj instanceof Long){
			
			return((Long) obj == 1 );
			
		}else if ( obj instanceof Boolean ){
			
			return((Boolean) obj);
		}
		
		return( def );
	}
	
	public static void
	exportJSONBoolean(
		Map		map,
		String	key,
		boolean	value )
	{
		map.put( key, value);
	}
	
	public static String
	importURL(
		Map		map,
		String	key )
	{
		String url = importString( map, key );
		
		if ( url != null ){
			
			url = url.trim();
			
			if ( url.length() == 0 ){
				
				url = null;
				
			}else{
				
				try{
					url = URLDecoder.decode( url, "UTF-8" );
					
				}catch( UnsupportedEncodingException e ){
					
					e.printStackTrace();
				}
			}
		}
		
		return( url );
	}
	
	public static void
	exportURL(
		Map		map,
		String	key,
		String	value )
	{
		exportString( map, key, value );
	}
	
	public static void
	exportJSONURL(
		Map		map,
		String	key,
		String	value )
	{
		exportJSONString( map, key, UrlUtils.encode( value ));
	}
	
	public static String[]
	importStringArray(
		Map		map,
		String	key )
	{
		List	list = (List)map.get( key );
		
		if ( list == null ){
			
			return( new String[0] );
		}
		
		String[]	res = new String[list.size()];
		
		for (int i=0;i<res.length;i++){
			
			Object obj = list.get(i);
			
			if ( obj instanceof String ){
				
				res[i] = (String)obj;
				
			}else if ( obj instanceof byte[] ){

                res[i] = new String((byte[])obj, StandardCharsets.UTF_8);

            }
		}
		
		return( res );
	}
	
	public static void
	exportStringArray(
		Map			map,
		String		key,
		String[]	data )
	{
		List	l = new ArrayList(data.length);
		
		map.put( key, l );

        for (String datum : data) {

            l.add(datum.getBytes(StandardCharsets.UTF_8));

        }
	}
	
	public static void
	exportJSONStringArray(
		Map			map,
		String		key,
		String[]	data )
	{
		List	l = new JSONArray(data.length);

		map.put( key, l );

		Collections.addAll(l, data);
	}
	
	public static void
	exportIntArray(
		Map			map,
		String		key,
		int[]		values )
	{
		if ( values == null ){
			
			return;
		}
		
		int	num = values.length;
		
		byte[]	bytes 	= new byte[num*4];
		int		pos		= 0;

        for (int v : values) {

            bytes[pos++] = (byte) (v >>> 24);
            bytes[pos++] = (byte) (v >>> 16);
            bytes[pos++] = (byte) (v >>> 8);
            bytes[pos++] = (byte) (v);
        }
		
		map.put( key, bytes );
	}
	
	public static int[]
	importIntArray(
		Map			map,
		String		key )
	{
		byte[]	bytes = (byte[])map.get( key );
		
		if ( bytes == null ){
			
			return( null );
		}
		
		int[]	values = new int[bytes.length/4];
		
		int	pos = 0;
		
		for (int i=0;i<values.length;i++){
			
			values[i] =  
				((bytes[pos++]&0xff) << 24) + 
				((bytes[pos++]&0xff) << 16) + 
				((bytes[pos++]&0xff) << 8) + 
				((bytes[pos++]&0xff)); 
		}
		
		return( values );
	}
}
