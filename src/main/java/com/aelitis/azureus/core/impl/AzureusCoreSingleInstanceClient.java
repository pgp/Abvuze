/*
 * Created on 11-Sep-2006
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

package com.aelitis.azureus.core.impl;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

import org.gudy.azureus2.core3.util.Constants;


/**
 * Single instance management is a bit of a mess. For some reason the UIs have their own implementations of clients and servers.
 * We also have a more generic plugin-accessible single instance service that can be used by launchable plugins but don't give
 * a generic mechanism for dealing with the basic mechanism used by the UIs (that run on the instance port).
 * I have introduced this class to give a programmatic way of passing arguments using the existing instance port. Perhaps one day
 * the various UI implementations will be rewritten to use this...
 * @author Parg
 */

public class 
AzureusCoreSingleInstanceClient 
{
	public static final String ACCESS_STRING = "Azureus Start Server Access";
	
	private static final int CONNECT_TIMEOUT	= 500;
	private static final int READ_TIMEOUT		= 5000;
	
	public boolean
	sendArgs(
		String[]	args,
		int			max_millis_to_wait )
	{
			// limit the subset here as we're looping waiting for something to be alive and we can't afford to take ages getting back to the start
		
		long	start = System.currentTimeMillis();
		
		while( true ){
			
			long	connect_start = System.currentTimeMillis();
			
			if ( connect_start < start ){
				
				start  = connect_start;
			}
			
			if ( connect_start - start > max_millis_to_wait ){
				
				return( false );
			}

			try (Socket sock = new Socket()) {

				sock.connect(new InetSocketAddress("127.0.0.1", Constants.INSTANCE_PORT), CONNECT_TIMEOUT);

				sock.setSoTimeout(READ_TIMEOUT);

				PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream(), StandardCharsets.UTF_8));

				StringBuilder buffer = new StringBuilder(ACCESS_STRING + ";args;");

				for (String arg1 : args) {

					String arg = arg1.replaceAll("&", "&&").replaceAll(";", "&;");

					buffer.append(arg);

					buffer.append(';');
				}

				pw.println(buffer.toString());

				pw.flush();

                return receiveReply(sock);

            } catch (Throwable e) {

				long connect_end = System.currentTimeMillis();

				long time_taken = connect_end - connect_start;

				if (time_taken < CONNECT_TIMEOUT) {

					try {
						Thread.sleep(CONNECT_TIMEOUT - time_taken);

					} catch (Throwable f) {
					}
				}
			}
			
	
		}
	}
	
	public static boolean
	sendReply(
		Socket		socket )
	{
       try{
        		// added reply from 5613_b16+
    	   
        	OutputStream os = socket.getOutputStream();
        	
        	os.write( ( ACCESS_STRING + ";" ).getBytes(StandardCharsets.UTF_8));
        	
        	os.flush();
        	
        	return( true );
        	
        }catch( Throwable e ){
        }
       
       return( false );
	}
	
	public static boolean
	receiveReply(
		Socket		socket )
	{
		try{
			InputStream	is = socket.getInputStream();
			
			socket.setSoTimeout( 15*1000 );
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			while( true ){
				
				int data = is.read();
				
				if ( data == -1 ){
					
					break;
				}
				
				byte b = (byte)data;
				
				if ( b == ';' ){
					
					String str = new String( baos.toByteArray(), StandardCharsets.UTF_8);
					
					return( str.equals( ACCESS_STRING ));
					
				}else{
					
					baos.write( b );
				}
			}
		}catch( Throwable e ){
		}
		
		return( false );
	}
	
	public static void
	main(
		String[]	args )
	{
		new AzureusCoreSingleInstanceClient().sendArgs( new String[]{ "6C0B39D9897AF42F624AC2DE010CF33F55CB45EC" }, 30000 );
	}
}
