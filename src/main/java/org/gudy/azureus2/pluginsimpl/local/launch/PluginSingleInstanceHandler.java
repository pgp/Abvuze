/*
 * Created on 12-Sep-2005
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

package org.gudy.azureus2.pluginsimpl.local.launch;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.gudy.azureus2.core3.util.RandomUtils;
import org.gudy.azureus2.core3.util.SystemProperties;
import org.gudy.azureus2.plugins.PluginManagerArgumentHandler;
import org.gudy.azureus2.plugins.logging.LoggerChannel;
import org.gudy.azureus2.plugins.logging.LoggerChannelListener;


public class 
PluginSingleInstanceHandler 
{
	private static boolean		active;
	
	private static int								port;
	private static PluginManagerArgumentHandler		handler;
	
	public static void
	initialise(
		int									_port,
		PluginManagerArgumentHandler		_handler )
	{
		port		= _port;
		handler		= _handler;
		
		String	multi_instance = System.getProperty( "MULTI_INSTANCE");
		
		if ( multi_instance != null && multi_instance.equalsIgnoreCase( "true" )){
			
			return;
		}
		
		active = true;
	}
	
	public static boolean
	initialiseAndProcess(
		int									_port,
		PluginManagerArgumentHandler		_handler,
		String[]							_args )
	{
		initialise( _port, _handler );
		
		return( process( null, _args ));
	}
	
	protected static boolean
	process(
		LoggerChannelListener	log,
		String[]				args )
	{
		if ( active ){
			
			if ( startListener( log )){
				
				return( false );
				
			}else{
				
				sendArguments( log, args );
				
				return( true );
			}
		}else{
			
			return( false );
		}
	}

	  
	protected static boolean
	startListener(
		final LoggerChannelListener	log )
	{
		try{
			final ServerSocket server_socket = new ServerSocket( port, 50, InetAddress.getByName("127.0.0.1"));
	         
			if ( log != null ){
				log.messageLogged( 
					  LoggerChannel.LT_INFORMATION,
					  "SingleInstanceHandler: listening on 127.0.0.1:" + port + " for passed arguments");
			}
			
			Thread t = 
				new Thread("Single Instance Handler")
				{
		    		public void 
		    		run()
					{
						for(;;) {
		    		    	
		    		    	Socket socket			= null;
		    		    	ObjectInputStream	ois	= null;
		    		    	
		    		    	try{
		    		    		socket = server_socket.accept();
		    		    		
		    		    		String address = socket.getInetAddress().getHostAddress();
		    		    		
		    		    		if ( !( address.equals("localhost") || address.equals("127.0.0.1"))){
		    		    			
		    		    			socket.close();
		    		    			
		    		    			continue;
		    		    		}
		    		    		
		    		    		ois = new ObjectInputStream( socket.getInputStream());
		    		    		
		    		    		ois.readInt();	// version
		    		    		
		    		    		String	header = (String)ois.readObject();
		    		    		
		    		    		if ( !header.equals( getHeader())){
		    		    			
		    		    			if ( log != null ){
		    		    				log.messageLogged( 
		    		    					LoggerChannel.LT_ERROR,
		    		    					"SingleInstanceHandler: invalid header - " + header );
		    		    			}
		    		    			
		    		    			continue;
		    		    		}

		    		    		String[]	args = (String[])ois.readObject();
		    		    		
		    					String config_dir = System.getProperty( SystemProperties.SYS_PROP_CONFIG_OVERRIDE, null );
		    					
		    					if ( config_dir != null ){
		    		    			
		    							// caller will have written args to a file
		    						
		    		    			String config_path 	= (String)ois.readObject();
		    		    			String file_name	= (String)ois.readObject();
		    		    			
		    		    			if ( !config_path.equals( config_dir )){
		    		    				
		    		    				throw( new Exception( "Called supplied incorrect config path: " + config_path ));
		    		    			}
		    		    			
		    		    			File cmd_file = new File( new File( config_dir, "tmp" ), file_name ).getCanonicalFile();
		    		    			
		    		    			if ( !cmd_file.getParentFile().getParentFile().equals( new File( config_dir ))){
		    		    				
		    		    				throw( new Exception( "Called supplied invalid file name: " + file_name ));
		    		    			}

                                    try (ObjectInputStream ois2 = new ObjectInputStream(new FileInputStream(cmd_file))) {

                                        args = (String[]) ois2.readObject();

                                    } finally {

                                        cmd_file.delete();
                                    }
		    					}
		    					
		    		    		handler.processArguments( args );
		    		    		
		    		    	}catch( Throwable e ){
		    		    		
		    		    		if ( log != null ){
		    		    			log.messageLogged( "SingleInstanceHandler: receive error", e );
		    		    		}
		    		    		
		    		    	}finally{
		    		    		
		    		    		if ( ois != null ){
		    		    			try{
		    		    				ois.close();
		    		    				
		    		    			}catch( Throwable e ){
		    		    			}
		    		    		}
		    		    		
		    		    		if ( socket != null ){
		    		    			try{
		    		    				socket.close();
		    		    				
		    		    			}catch( Throwable e ){
		    		    			}	
		    		    		}
		    		    	}
		    		    }
					}
				};
		    
			t.setDaemon( true );
				
			t.start(); 
			
			return( true );
			
		}catch( Throwable e ){
		
			return( false );
		}
	}
	   
	protected static void
	sendArguments(
		LoggerChannelListener	log,
		String[]				args )
	{

        try (Socket socket = new Socket("127.0.0.1", port)) {

            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

            oos.writeInt(0);

            oos.writeObject(getHeader());

            oos.writeObject(args);

            // if we know the config dir then use more secure mechanism to pass args by writing
            // to a file (this proving we have write access to the directory at least)

            String config_dir = System.getProperty(SystemProperties.SYS_PROP_CONFIG_OVERRIDE, null);

            if (config_dir != null) {

                File file = new File(config_dir, "tmp");

                file.mkdirs();

                file = File.createTempFile("AZU" + RandomUtils.nextSecureAbsoluteLong(), ".tmp", file);

                try (ObjectOutputStream oos2 = new ObjectOutputStream(new FileOutputStream(file))) {
                    oos2.writeObject(args);

                }

                oos.writeObject(config_dir);

                oos.writeObject(file.getName());
            }

            oos.flush();

            if (log != null) {

                log.messageLogged(LoggerChannel.LT_INFORMATION, "SingleInstanceHandler: arguments passed to existing process");
            }
        } catch (Throwable e) {

            if (log != null) {

                log.messageLogged("SingleInstanceHandler: send error", e);
            }
        }
	}
	
	protected static String
	getHeader()
	{
		return( SystemProperties.getApplicationName() + " Single Instance Handler" );
	}
}
