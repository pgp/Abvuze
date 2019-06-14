/*
 * Created on 13-Mar-2004
 * Created by James Yeh
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

import java.io.*;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.config.ParameterListener;
import org.gudy.azureus2.core3.logging.*;
import org.gudy.azureus2.core3.util.*;
import org.gudy.azureus2.platform.*;
import org.gudy.azureus2.platform.common.PlatformManagerBaseImpl;
import org.gudy.azureus2.platform.macosx.access.jnilib.OSXAccess;
import org.gudy.azureus2.plugins.platform.PlatformManagerException;

import com.aelitis.azureus.core.AzureusCore;
import com.aelitis.azureus.core.AzureusCoreLifecycleAdapter;


/**
 * Performs platform-specific operations with Mac OS X
 *
 * @author James Yeh
 * @version 1.0 Initial Version
 * @see PlatformManager
 */
public class PlatformManagerImpl extends PlatformManagerBaseImpl implements AEDiagnosticsEvidenceGenerator
{
	private static final LogIDs LOGID = LogIDs.CORE;

	private static final String BUNDLE_ID = "com.azureus.vuze"; // TODO: Pull from .plist if we want to accurate

	private static final String[] SCHEMES = new String[] {
		"magnet",
		"dht",
		"vuze",
		"bc",
		"bctp"
	};

	private static final String[] MIMETYPES = new String[] {
		"application/x-bittorrent",
		"application/x-vuze",
		"application/x-bctp-uri",
		"application/x-bc-uri"
	};

	private static final String[] EXTENSIONS = new String[] {
		"torrent",
		"tor",
		"vuze",
		"vuz",
		"bctpuri",
		"bcuri"
	};

    protected static PlatformManagerImpl singleton;
    protected static AEMonitor class_mon = new AEMonitor("PlatformManager");
    
    private static String fileBrowserName = "Finder";

    private volatile String		computer_name;
    private volatile boolean	computer_name_tried;

	private Class<?> claFileManager;
	
	private AzureusCore	azureus_core;
	
	private boolean		prevent_computer_sleep_pending	= false;
	private boolean 	prevent_computer_sleep			= false;
	private Process		prevent_computer_proc;
    
    /**
     * Gets the platform manager singleton, which was already initialized
     */
    public static PlatformManagerImpl getSingleton()
    {
        return singleton;
    }

    /**
     * Tries to enable cocoa-java access and instantiates the singleton
     */
    static
    {
      initializeSingleton();
    }

    /**
     * Instantiates the singleton
     */
    private static void initializeSingleton()
    {
        try
        {
            class_mon.enter();
            singleton = new PlatformManagerImpl();
        }
        catch (Throwable e)
        {
        	Logger.log(new LogEvent(LOGID, "Failed to initialize platform manager"
					+ " for Mac OS X", e));
        }
        finally
        {
            class_mon.exit();
        }

        COConfigurationManager.addAndFireParameterListener("FileBrowse.usePathFinder", new ParameterListener() {
					public void parameterChanged(String parameterName) {
						fileBrowserName = COConfigurationManager.getBooleanParameter("FileBrowse.usePathFinder")
	        		? "Path Finder" : "Finder";
					}
				});
    }

    /**
     * Creates a new PlatformManager and initializes its capabilities
     */
    public PlatformManagerImpl()
    {
        capabilitySet.add(PlatformManagerCapabilities.RecoverableFileDelete);
        capabilitySet.add(PlatformManagerCapabilities.ShowFileInBrowser);
        capabilitySet.add(PlatformManagerCapabilities.ShowPathInCommandLine);
        capabilitySet.add(PlatformManagerCapabilities.CreateCommandLineProcess);
        capabilitySet.add(PlatformManagerCapabilities.GetUserDataDirectory);
        capabilitySet.add(PlatformManagerCapabilities.UseNativeScripting);
        capabilitySet.add(PlatformManagerCapabilities.PlaySystemAlert);
        capabilitySet.add(PlatformManagerCapabilities.RequestUserAttention);
        
        if (OSXAccess.isLoaded()) {
	        capabilitySet.add(PlatformManagerCapabilities.GetVersion);
	        try {
  	        if (OSXAccess.canSetDefaultApp()) {
  		        capabilitySet.add(PlatformManagerCapabilities.RegisterFileAssociations);	        	
  	        }
	        } catch (Throwable t) {
	        	// likely java.lang.UnsatisfiedLinkError -- older version
	        }
        }
        
        if (hasVMOptions()) {
          capabilitySet.add(PlatformManagerCapabilities.AccessExplicitVMOptions);
        }
        
        capabilitySet.add(PlatformManagerCapabilities.RunAtLogin);
        capabilitySet.add(PlatformManagerCapabilities.GetMaxOpenFiles);
        
        if ( 	new File( "/usr/bin/pmset" ).canRead() ||
        		new File( "/usr/bin/caffeinate" ).canRead()){
        	
        	capabilitySet.add( PlatformManagerCapabilities.PreventComputerSleep );
        }
        
        try{
        	if ( new File( "/usr/bin/defaults" ).exists()){
     
				boolean	found = false;

				try{
					String[] read_command = { "/usr/bin/defaults", "read", BUNDLE_ID };
				  	
					Process p = Runtime.getRuntime().exec( read_command );
									
					if ( p.waitFor() == 0 ){
											
						InputStream is = p.getInputStream();
						
						LineNumberReader lnr = new LineNumberReader( new InputStreamReader( is, StandardCharsets.UTF_8));
						
						while( true ){
							
							String line = lnr.readLine();
							
							if ( line == null ){
								
								break;
							}
							
							if ( line.contains( "NSAppSleepDisabled" )){
								
								found = true;
								
								break;
							}
						}
					}
				}catch( Throwable e ){
					
					e.printStackTrace();
				}
				
        		if ( !found ){
        			
		        	String[] write_command = {
		        		"/usr/bin/defaults",
		        		"write",
		        		BUNDLE_ID,
		        		"NSAppSleepDisabled",
		        		"-bool",
		        		"YES"
		        	};
		        	
		        	Runtime.getRuntime().exec( write_command );
        		}	
        	}else{
        		
        		System.err.println( "/usr/bin/defaults missing" );
        	}
        }catch( Throwable e ){
        	
        	e.printStackTrace();
        }
        
        AEDiagnostics.addEvidenceGenerator(this);
    }

		/**
     * {@inheritDoc}
     */
    public int getPlatformType()
    {
        return PT_MACOSX;
    }

    /**
     * {@inheritDoc}
     */
    public String getVersion() throws PlatformManagerException
    {
    	if (!OSXAccess.isLoaded()) {
        throw new PlatformManagerException("Unsupported capability called on platform manager");
    	}
    	
    	return OSXAccess.getVersion();
    }

    protected PListEditor
    getPList()
    
    	throws IOException
    {
		String	plist = SystemProperties.getApplicationPath() +
			SystemProperties.getApplicationName() + ".app/Contents/Info.plist";

		File plist_file = new File( plist );
			
			// since 4800 the plist is read-only (as we sign it to play niceley with OSX and
			// updating it breaks the sig)
		
		if ( !plist_file.canWrite()){
			return( null );
		}
		
		PListEditor editor = new PListEditor( plist );
	
		return( editor );
    }
    
    protected boolean
    checkPList()
    {
    	try{
    		PListEditor editor = getPList();
    		
    		if ( editor == null) {
    			
    			return( false );
    		}
    		
    		editor.setFileTypeExtensions(EXTENSIONS);
    		editor.setSimpleStringValue("CFBundleName", "Vuze");
			editor.setSimpleStringValue("CFBundleTypeName", "Vuze Download");
			editor.setSimpleStringValue("CFBundleGetInfoString","Vuze");
			editor.setSimpleStringValue("CFBundleShortVersionString",Constants.AZUREUS_VERSION);
			editor.setSimpleStringValue("CFBundleVersion",Constants.AZUREUS_VERSION);
			editor.setArrayValues("CFBundleURLSchemes", "string", SCHEMES);
			
				// always touch it, see if it helps ensure we are registered as magnet
				// handler
			
			editor.touchFile();
			
			return( true );
			
    	}catch( Throwable e ){
    		
    			// don't use Debug.out - initialsiation recursion issue
    		
    		System.err.println( "Failed to update plist" );
    		e.printStackTrace();
    		
    		return( false );
    	}
    }
    
    protected void
    touchPList()
    {
       	try{
    		PListEditor editor = getPList();
  	
    		if ( editor != null ){
    		
    			editor.touchFile();
    		}
       	}catch( Throwable e ){
    		
       		System.err.println( "Failed to touch plist" );
       		e.printStackTrace();
    	}
    }

	@Override
	protected String getJVMOptionRedirect()
	{
		return ("-include-options ${HOME}/Library/Application Support/"
				+ SystemProperties.getApplicationName() + "/java.vmoptions");
	}
	
  private boolean hasVMOptions() {
		return FileUtil.getApplicationFile("java.vmoptions").exists();
	}


	@Override
	protected File[] getJVMOptionFiles()
	{
		try{
	
			File shared_options 		= FileUtil.getApplicationFile("java.vmoptions");
			// use LOC_USER_DATA instead of SystemProperties.getUserPath(),
			// since we assume in getJVMOptionRedirect that the shared_options'
			// include points to LOC_USER_DATA.
			File local_options 			= new File( getLocation(LOC_USER_DATA), "java.vmoptions" );
			
			return( new File[]{ shared_options, local_options });
			
		}catch( Throwable e ){
			
			return( new File[0] );
		}
	}

 
	public void
	startup(
		AzureusCore		_azureus_core )
	
		throws PlatformManagerException
	{	
		synchronized( this ){
			
			azureus_core = _azureus_core;
			
			if ( prevent_computer_sleep_pending ){
				
				prevent_computer_sleep_pending = false;
				
				setPreventComputerSleep( true );
			}
		}
		
		azureus_core.addLifecycleListener(
			new AzureusCoreLifecycleAdapter()
			{
				public void
				stopping(
					AzureusCore		core )
				{
					synchronized( PlatformManagerImpl.this ){
					
						try{
							setPreventComputerSleep( false );
							
						}catch( Throwable e ){
						}
						
						azureus_core = null;
					}
				}
			});
	}
	
	public int
	getShutdownTypes()
	{
		return( 0 );
	}
	
	public void
	shutdown(
		int			type )
	
		throws PlatformManagerException
	{	
		 throw new PlatformManagerException( "Unsupported capability called on platform manager" );
	}
	
	public void
	setPreventComputerSleep(
		boolean		prevent_it )
	
		throws PlatformManagerException
	{
		synchronized( this ){
			
			if ( azureus_core == null ){
				
				prevent_computer_sleep_pending = prevent_it;
				
				return;
			}
			
			if ( prevent_computer_sleep == prevent_it ){
				
				return;
			}
			
			prevent_computer_sleep = prevent_it;
			
			if ( prevent_it ){
				
				String[] command;
				
				File binary = new File( "/usr/bin/caffeinate" );
				
				if ( binary.canRead()){
					
					command = new String[]{ binary.getAbsolutePath(), "-i" };
					
				}else{
					
					binary = new File( "/usr/bin/pmset" );
				
					if ( binary.canRead()){
						
						command = new String[]{ binary.getAbsolutePath(), "noidle" };
						
					}else{
						
						 throw new PlatformManagerException("Unsupported capability called on platform manager");
					}
				}
			  
				if ( prevent_computer_proc != null ){
					
					Debug.out( "eh?" );
					
					prevent_computer_proc.destroy();
				}
				
				try{
					System.out.println( "Starting idle sleep preventer: " + command[0] );

					prevent_computer_proc = Runtime.getRuntime().exec( command );
					
				}catch( Throwable e ){
					
					Debug.out( e );
				}
			}else{
				
				if ( prevent_computer_proc != null ){
					
					System.out.println( "Stopping idle sleep preventer" );
					
					prevent_computer_proc.destroy();
					
					prevent_computer_proc = null;
				}
			}
		}	
	}
	
	public boolean
	getPreventComputerSleep()
	{
		synchronized( this ){
			
			return( prevent_computer_sleep );
		}
	}
	
  	public boolean 
  	getRunAtLogin() 
  	
  		throws PlatformManagerException 
  	{
  		if ( Constants.isOSX_10_8_OrHigher ){
  			
  			String item_name = SystemProperties.getApplicationName();
  			
  			try{
  				 
                 StringBuffer sb = new StringBuffer();
                 sb.append("tell application \"");
                 sb.append("System Events");
                 sb.append("\" to get the name of every login item");

                 String[] items = performOSAScript(sb).split( "," );
                 
                 for ( String item: items ){
                	 
                	 if ( item.trim().equalsIgnoreCase( item_name )){
                		 
                		 return( true );
                	 }
                 }
                 
                 return( false );
                 
             }catch (Throwable e){
            	 
                 throw new PlatformManagerException("Failed to get login items", e);
             }
  		}
  		
  		File f = getLoginPList();
  		
  		if ( !f.exists()){
  			
  			return( false );
  		}
  		
  		File	bundle_file = getAbsoluteBundleFile();
  		
  		if ( !bundle_file.exists()){
  			
  			return( false );
  		}
  		
  		try{
  			convertToXML( f );

            try (LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
                int state = 0;
                String target = bundle_file.getAbsolutePath();
                while (true) {

                    String line = lnr.readLine();

                    if (line == null) {

                        break;
                    }

                    if (state == 0) {

                        if (containsTag(line, "AutoLaunchedApplicationDictionary")) {

                            state = 1;
                        }
                    } else {

                        if (line.contains(target)) {

                            return (true);
                        }
                    }
                }

                return (false);

            }
  		}catch( Throwable e ){
  			
  			throw( new PlatformManagerException( "Failed to read input file", e ));
  		}
  	}
  	
  	public void 
  	setRunAtLogin(
  		boolean run ) 
  	
  		throws PlatformManagerException 
  	{
  		if ( getRunAtLogin() == run ){
  			
  			return;
  		}
  		
		File	bundle_file = getAbsoluteBundleFile();
  		
  		if ( !bundle_file.exists()){
  			
 			throw( new PlatformManagerException( "Failed to write set run-at-login, bundle not found" ));
  		}
  		
		String	abs_target = bundle_file.getAbsolutePath();

  		if ( Constants.isOSX_10_8_OrHigher ){
  		
  			if ( run ){
  				
  	 			 try{
  	  				 
  	                 StringBuffer sb = new StringBuffer();
  	                 sb.append("tell application \"");
  	                 sb.append("System Events");
  	                 sb.append("\" to make login item at end with properties {path:\"" );
  	                 sb.append(abs_target);
  	                 sb.append("\", hidden:false}" );
  	                 
  	                 System.out.println( performOSAScript(sb));
  	                 
  	                 return;
  	                 
  	             }catch (Throwable e){
  	            	 
  	                 throw new PlatformManagerException("Failed to add login item", e);
  	             }	
  			  		
  			}else{
  				
  				
 	 			 try{
  	  				 
  	                 StringBuffer sb = new StringBuffer();
  	                 sb.append("tell application \"");
  	                 sb.append("System Events");
  	                 sb.append("\" to delete login item \"" );
  	                 sb.append(SystemProperties.getApplicationName());
  	                 sb.append("\"" );
  	                 
  	                 System.out.println( performOSAScript(sb));
  	                 
  	                 return;
  	                 
  	             }catch (Throwable e){
  	            	 
  	                 throw new PlatformManagerException("Failed to delete login item", e);
  	             }	  		
  			}
  		}
  		

  		
  		File f = getLoginPList();
  		
  		if ( f.exists()){
  			
  			convertToXML( f );
  			
  		}else{
  			
  			try{

                try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {

                    pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                    pw.println("<!DOCTYPE plist PUBLIC \"-//Apple Computer//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">");
                    pw.println("<plist version=\"1.0\">");
                    pw.println("<dict>");

                    pw.println("</dict>");
                    pw.println("</plist>");

                }
  			}catch( Throwable e ){
  				
  				throw( new PlatformManagerException( "Failed to write output file", e ));
  			}
  		}
  		
  
  		try{
  			List<String>	lines = new ArrayList<>();

            int dict_line = -1;
            int auto_launch_line = -1;
            int target_index = -1;
            try (LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
                while (true) {

                    String line = lnr.readLine();

                    if (line == null) {

                        break;
                    }

                    lines.add(line);

                    if (dict_line == -1 && containsTag(line, "<dict>")) {

                        dict_line = lines.size();
                    }

                    if (auto_launch_line == -1 && containsTag(line, "AutoLaunchedApplicationDictionary")) {

                        auto_launch_line = lines.size();
                    }

                    if (line.contains(abs_target)) {

                        target_index = lines.size();
                    }
                }

                if (dict_line == -1) {

                    throw (new PlatformManagerException("Malformed plist - no 'dict' entry"));
                }

                if (auto_launch_line == -1) {

                    lines.add(dict_line, "\t<key>AutoLaunchedApplicationDictionary</key>");

                    auto_launch_line = dict_line + 1;

                    lines.add(auto_launch_line, "\t<array>");
                    lines.add(auto_launch_line + 1, "\t</array>");
                }
            }
  			
  			if ( run ){
  				
  				if ( target_index != -1 || auto_launch_line == -1 ){
  					
  					return;
  				}
  				
  				target_index = auto_launch_line+1;
  				
 				lines.add( target_index++, "\t\t<dict>" );
				lines.add( target_index++, "\t\t\t<key>Path</key>" );
				lines.add( target_index++, "\t\t\t<string>" + abs_target + "</string>" );
 				lines.add( target_index++, "\t\t</dict>" );
  				
  			}else{
  				
  				if ( target_index == -1 ){
  					
  					return;
  				}
  				
  				while( !containsTag( lines.get( target_index ), "</dict>" )){
  					
  					lines.remove( target_index );
  				}
  				
  				lines.remove( target_index );
  				
  				target_index--;
  				
  				while( !containsTag( lines.get( target_index ), "<dict>" )){
  					
  					lines.remove( target_index );
  					
  					target_index--;
  				}
  				
  				lines.remove( target_index );
  			}
  			
  			File	backup = new File( f.getParentFile(), f.getName() + ".bak" );
  			
  			if ( backup.exists()){
  				
  				backup.delete();
  			}
  			
  			if ( !f.renameTo( backup )){
  				
  				throw( new PlatformManagerException( "Failed to backup " + f ));
  			}
  			
			boolean	ok = false;
			
			try{
				PrintWriter pw = new PrintWriter( new OutputStreamWriter( new FileOutputStream( f ), StandardCharsets.UTF_8));
				
				try{
				
					for ( String line: lines ){
						
						pw.println( line );
					}
				}finally{
					
					pw.close();
					
					if ( pw.checkError()){
						
						throw( new PlatformManagerException( "Failed to write output file" ));
					}
					
					ok = true;
				}
			}finally{
				
				if ( !ok ){
					
					backup.renameTo( f );
				}
			}
 	
  		}catch( PlatformManagerException e ){
  			
  			throw( e );
  			
  		}catch( Throwable e ){
  			
  			throw( new PlatformManagerException( "Failed to write output file", e ));
  		}
   	}
    
  	private void
  	convertToXML(
  		File		file )
  	
  		throws PlatformManagerException
  	{
 		try{

            try (LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                String line = lnr.readLine();

                if (line == null) {

                    return;
                }

                if (line.trim().toLowerCase().startsWith("<?xml")) {

                    return;
                }

                Runtime.getRuntime().exec(
                        new String[]{
                                findCommand("plutil"),
                                "-convert",
                                "xml1",
                                file.getAbsolutePath()
                        }).waitFor();

            }
  		}catch( Throwable e ){
  			
  			throw( new PlatformManagerException( "Failed to convert plist to xml" ));
  		}
  	}
  	
  	private String
  	findCommand(
  		String	name )
  	{
  		final String[]  locations = { "/bin", "/usr/bin" };

  		for ( String s: locations ){

  			File f = new File( s, name );

  			if ( f.exists() && f.canRead()){

  				return( f.getAbsolutePath());
  			}
  		}

  		return( name );
  	}
  	
  	private boolean
  	containsTag(
  		String	line,
  		String	tag )
  	{
  		line 	= line.trim().toLowerCase( Locale.US );
  		tag		= tag.toLowerCase( Locale.US );
  		
  		StringBuilder line2 = new StringBuilder( line.length());
  		
  		for (char c:line.toCharArray()){
  			
  			if ( !Character.isWhitespace( c )){
  				
  				line2.append( c );
  			}
  		}
  		
  		return( line2.toString().contains( tag ));
  	}
  	
    private File 
    getLoginPList() 
    
    	throws PlatformManagerException
    {
    	return( new File(System.getProperty("user.home"), "/Library/Preferences/loginwindow.plist" )); 
    }
    
    /**
     * {@inheritDoc}
     * @see org.gudy.azureus2.core3.util.SystemProperties#getUserPath()
     */
    public String getUserDataDirectory() throws PlatformManagerException
    {
    	return new File(System.getProperty("user.home")
    			+ "/Library/Application Support/" 
    			+ SystemProperties.APPLICATION_NAME).getPath()
    			+ SystemProperties.SEP;
    }

	public String 
	getComputerName() 
	{
		if ( computer_name_tried ){
			
			return( computer_name );
		}
		
		try{
			String result = null;
			
			String	hostname = System.getenv( "HOSTNAME" );
			
			if ( hostname != null && hostname.length() > 0 ){
				
				result = hostname;
			}
			
			if ( result == null ){
				
				String	host = System.getenv( "HOST" );
				
				if ( host != null && host.length() > 0 ){
					
					result = host;
				}
			}
			
			if ( result == null ){
			
				try{				
					String[] to_run = new String[3];
					
				  	to_run[0] = "/bin/sh";
				  	to_run[1] = "-c";
				  	to_run[2] = "echo $HOSTNAME";
				  	
					Process p = Runtime.getRuntime().exec( to_run );
					
					if ( p.waitFor() == 0 ){
						
						String	output = "";
						
						InputStream is = p.getInputStream();
						
						while( true ){
							
							byte[] buffer = new byte[1024];
							
							int len = is.read( buffer );
							
							if ( len <= 0 ){
								
								break;
							}
							
							output += new String( buffer, 0, len );
							
							if ( output.length() > 64 ){
								
								break;
							}
						}
						
						if ( output.length() > 0 ){
							
							result = output.trim();
							
							int pos = result.indexOf(' ');
							
							if ( pos != -1 ){
								
								result = result.substring( 0, pos ).trim();
							}
						}
					}
				}catch( Throwable e ){
				}
			}
			
			if ( result != null ){
			
				int	pos = result.lastIndexOf( '.' );
				
				if ( pos != -1 ){
					
					result = result.substring( 0, pos );
				}
				
				if ( result.length() > 0 ){
					
					if ( result.length() > 32 ){
						
						result = result.substring( 0, 32 );
					}
					
					computer_name = result;
				}
			}
						
			return( computer_name );
			
		}finally{
			
			computer_name_tried = true;
		}
	}
	
	public File
	getLocation(
		long	location_id )
	
		throws PlatformManagerException
	{
		switch ((int)location_id) {
			case LOC_USER_DATA:
				return new File(getUserDataDirectory());
				
			case LOC_DOCUMENTS:
				try {
					return new File(OSXAccess.getDocDir());
				} catch (Throwable e) {
					// throws UnsatisfiedLinkError if no osxaccess
					// Sometimes throws NullPointerException

					// Usually in user.home + Documents
					return new File(System.getProperty("user.home"), "Documents");
				}
				
			case LOC_MUSIC:
				
			case LOC_VIDEO:

			default:
				return( null );
		}
		
	}

	/* (non-Javadoc)
	 * @see org.gudy.azureus2.platform.PlatformManager#isApplicationRegistered()
	 */
	public boolean isApplicationRegistered()
			throws PlatformManagerException {
		try {
			if (OSXAccess.canSetDefaultApp()) {
				for (String ext : EXTENSIONS) {
					if (!isOurExt(ext)) {
						return false;
					}
				}
				for (String mimeType : MIMETYPES) {
					if (!isOurMimeType(mimeType)) {
						return false;
					}
				}
				for (String scheme : SCHEMES) {
					if (!isOurScheme(scheme)) {
						return false;
					}
				}
			}
		} catch (Throwable e) {

		}
		return true;
	}
	
	private boolean isOurExt(String ext) {
		try {
			String appForExt = OSXAccess.getDefaultAppForExt(ext);
			//System.out.println("app for ext:" + ext + ": " + appForExt);
			return BUNDLE_ID.equals(appForExt);
		} catch (Throwable e) {
			return true; // fake it
		}
	}

	private boolean isOurScheme(String scheme) {
		try {
			String appForScheme = OSXAccess.getDefaultAppForScheme(scheme);
			//System.out.println("app for scheme:" + scheme + ": " + appForScheme);
			return BUNDLE_ID.equals(appForScheme);
		} catch (Throwable e) {
			return true; // fake it
		}
	}

	private boolean isOurMimeType(String mimetype) {
		try {
			String appForMimeType = OSXAccess.getDefaultAppForMime(mimetype);
			//System.out.println("app for mime:" + mimetype + ": " + appForMimeType);
			return BUNDLE_ID.equals(appForMimeType);
		} catch (Throwable e) {
			return true; // fake it
		}
	}

	/**
	 * Bundle Path is the .app file that launched vuze, usually /Applications/Vuze.app
	 */
    private String
    getBundlePath()
    {
  		String mod_name = System.getProperty( "exe4j.moduleName", null );
  		if (mod_name != null && mod_name.endsWith(".app")) {
  			return mod_name;
  		}
		return( SystemProperties.getApplicationPath() + SystemProperties.getApplicationName() + ".app" );
    }
    
  	/**
  	 * Bundle Path is the .app file that launched vuze, usually /Applications/Vuze.app
  	 */
    private File
    getAbsoluteBundleFile()
    {
    	return( new File( getBundlePath()).getAbsoluteFile());
    }
    
  	/**
  	 * command to launch Vuze
  	 */
	public String
	getApplicationCommandLine()
		throws PlatformManagerException
	{
		try{	    
			File osx_app_bundle = getAbsoluteBundleFile();
			
			if( !osx_app_bundle.exists() ) {
				String msg = "OSX app bundle not found: [" +osx_app_bundle.toString()+ "]";
				System.out.println( msg );
				if (Logger.isEnabled())
					Logger.log(new LogEvent(LOGID, msg));		
				throw new PlatformManagerException( msg );
			}
			
			return "open -a \"" +osx_app_bundle.toString()+ "\"";
			//return osx_app_bundle.toString() +"/Contents/MacOS/JavaApplicationStub";
			
		}
		catch( Throwable t ){	
			t.printStackTrace();
			return null;
		}
	}
	
	
	public boolean
	isAdditionalFileTypeRegistered(
		String		name,				// e.g. "BitTorrent"
		String		type )				// e.g. ".torrent"
	
		throws PlatformManagerException
	{
		String osxType = type.startsWith(".") ? type.substring(1) : type;
		return isOurExt(osxType);
	}
	
	public void
	unregisterAdditionalFileType(
		String		name,				// e.g. "BitTorrent"
		String		type )				// e.g. ".torrent"
		
		throws PlatformManagerException
	{
		throw new PlatformManagerException("Unsupported capability called on platform manager");
	}
	
	public void
	registerAdditionalFileType(
		String		name,				// e.g. "BitTorrent"
		String		description,		// e.g. "BitTorrent File"
		String		type,				// e.g. ".torrent"
		String		content_type )		// e.g. "application/x-bittorrent"
	
		throws PlatformManagerException
	{
		try {
			if (OSXAccess.canSetDefaultApp()) {
				if (type != null) {
  				String osxType = type.startsWith(".") ? type.substring(1) : type;
  				OSXAccess.setDefaultAppForExt(BUNDLE_ID, osxType);
				}
				if (content_type != null) {
					OSXAccess.setDefaultAppForMime(BUNDLE_ID, content_type);
				}
			}
		} catch (Throwable t) {
			throw new PlatformManagerException(
					"registerAdditionalFileType failed on platform manager", t);
		}
	}
	
    public void registerApplication() throws PlatformManagerException
    {
    	touchPList();

  		try {
  			if (OSXAccess.canSetDefaultApp()) {
  				for (String ext : EXTENSIONS) {
  					OSXAccess.setDefaultAppForExt(BUNDLE_ID, ext);
  				}
  				for (String mimeType : MIMETYPES) {
  					OSXAccess.setDefaultAppForMime(BUNDLE_ID, mimeType);
  				}
  				for (String scheme : SCHEMES) {
  					OSXAccess.setDefaultAppForScheme(BUNDLE_ID, scheme);
  				}


  				// TODO: Remove
					isApplicationRegistered();
  			}
  		} catch (Throwable t) {
  			throw new PlatformManagerException(
  					"registerApplication failed on platform manager", t);
  		}

    }

    /**
     * {@inheritDoc}
     */
    public void createProcess(String cmd, boolean inheritsHandles) throws PlatformManagerException
    {
        try
        {
            performRuntimeExec(cmd.split(" "));
        }
        catch (Throwable e)
        {
            throw new PlatformManagerException("Failed to create process", e);
        }
    }
    
    private Class<?> getFileManagerClass() {
    	if (claFileManager != null) {
    		return claFileManager;
    	}
    	
			try {
				// We can only use FileManager after CocoaUIEnhancer has been initialized
				// because refering to FileManager earlier will prevent our main menu from
				// working
				Class<?> claCocoaUIEnhancer = Class.forName("org.gudy.azureus2.ui.swt.osx.CocoaUIEnhancer");
				if ((Boolean) claCocoaUIEnhancer.getMethod("isInitialized").invoke(null)) {
					claFileManager = Class.forName("com.apple.eio.FileManager");
				}
			} catch (Exception e) {
			}
			return claFileManager;
    }

    /**
     * {@inheritDoc}
     */
    public void performRecoverableFileDelete(String path) throws PlatformManagerException
    {
        File file = new File(path);
        if(!file.exists())
        {
	        	if (Logger.isEnabled())
							Logger.log(new LogEvent(LOGID, LogEvent.LT_WARNING, "Cannot find "
									+ file.getName()));
            return;
        }

				
				try {
					Class<?> claFileManager = getFileManagerClass();

					if (claFileManager != null) {
  					Method methMoveToTrash = claFileManager.getMethod("moveToTrash",
                            File.class);
    				if (methMoveToTrash != null) {
  						Object result = methMoveToTrash.invoke(null, file);
  						if (result instanceof Boolean) {
  							if ((Boolean) result) {
  								return;
  							}
  						}
    				}
					}
 				} catch (Throwable e) {
				}

        boolean useOSA = !NativeInvocationBridge.sharedInstance().isEnabled() || !NativeInvocationBridge.sharedInstance().performRecoverableFileDelete(file);

        if(useOSA)
        {
            try
            {
                StringBuffer sb = new StringBuffer();
                sb.append("tell application \"");
                sb.append("Finder");
                sb.append("\" to move (posix file \"");
                sb.append(path);
                sb.append("\" as alias) to the trash");

                performOSAScript(sb);
            }
            catch (Throwable e)
            {
                throw new PlatformManagerException("Failed to move file", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void dispose()
    {
    	try {
    		if (NativeInvocationBridge.hasSharedInstance()) {
    			NativeInvocationBridge.sharedInstance().dispose();
    		}
    	} catch (Throwable t) {
    		Debug.out("Problem disposing NativeInvocationBridge", t);
    	}
    }

    /**
     * {@inheritDoc}
     */
    public void setTCPTOSEnabled(boolean enabled) throws PlatformManagerException
    {
        throw new PlatformManagerException("Unsupported capability called on platform manager");
    }

	public void
    copyFilePermissions(
		String	from_file_name,
		String	to_file_name )
	
		throws PlatformManagerException
	{
	    throw new PlatformManagerException("Unsupported capability called on platform manager");		
	}
	
    /**
     * {@inheritDoc}
     */
    public void showFile(String path) throws PlatformManagerException
    {
        File file = new File(path);
        if(!file.exists())
        {
        	if (Logger.isEnabled())
        		Logger.log(new LogEvent(LOGID, LogEvent.LT_WARNING, "Cannot find "
        				+ file.getName()));
            throw new PlatformManagerException("File not found");
        }

        showInFinder(file);
    }

    // Public utility methods not shared across the interface

    /**
     * Plays the system alert (the jingle is specified by the user in System Preferences)
     */
    public void playSystemAlert()
    {
        try
        {
            performRuntimeExec(new String[]{"beep"});
        }
        catch (IOException e)
        {
        	if (Logger.isEnabled())
        		Logger.log(new LogEvent(LOGID, LogEvent.LT_WARNING,
						"Cannot play system alert"));
        	Logger.log(new LogEvent(LOGID, "", e));
        }
    }

    /**
     * <p>Shows the given file or directory in Finder</p>
     * @param path Absolute path to the file or directory
     */
    public void showInFinder(File path)
    {
			
			try {
				Class<?> claFileManager = getFileManagerClass();
				if (claFileManager != null && getFileBrowserName().equals("Finder")) {
  				Method methRevealInFinder = claFileManager.getMethod("revealInFinder",
                        File.class);
  				if (methRevealInFinder != null) {
						Object result = methRevealInFinder.invoke(null, path);
						if (result instanceof Boolean) {
							if ((Boolean) result) {
								return;
							}
						}
  				}
				}
			} catch (Throwable e) {
			}

        boolean useOSA = !NativeInvocationBridge.sharedInstance().isEnabled() || !NativeInvocationBridge.sharedInstance().showInFinder(path,fileBrowserName);

        if(useOSA)
        {
            StringBuffer sb = new StringBuffer();
            sb.append("tell application \"");
            sb.append(getFileBrowserName());
            sb.append("\"\n");
            sb.append("reveal (posix file \"");
            sb.append(path);
            sb.append("\" as alias)\n");
            sb.append("activate\n");
            sb.append("end tell\n");

            try
            {
                performOSAScript(sb);
            }
            catch (IOException e)
            {
                Logger.log(new LogAlert(LogAlert.UNREPEATABLE, LogAlert.AT_ERROR, e
						.getMessage()));
            }
        }
    }

    /**
     * <p>Shows the given file or directory in Terminal by executing cd /absolute/path/to</p>
     * @param path Absolute path to the file or directory
     */
    public void showInTerminal(String path)
    {
        showInTerminal(new File(path));
    }

    /**
     * <p>Shows the given file or directory in Terminal by executing cd /absolute/path/to</p>
     * @param path Absolute path to the file or directory
     */
    public void showInTerminal(File path)
    {
        if (path.isFile())
        {
            path = path.getParentFile();
        }

        if (path != null && path.isDirectory())
        {
            StringBuffer sb = new StringBuffer();
            sb.append("tell application \"");
            sb.append("Terminal");
            sb.append("\" to do script \"cd ");
            sb.append(path.getAbsolutePath().replaceAll(" ", "\\ "));
            sb.append("\"");

            try
            {
                performOSAScript(sb);
            }
            catch (IOException e)
            {
                Logger.log(new LogAlert(LogAlert.UNREPEATABLE, LogAlert.AT_ERROR, e
						.getMessage()));
            }
        }
        else
        {
        	if (Logger.isEnabled())
        		Logger.log(new LogEvent(LOGID, LogEvent.LT_WARNING, "Cannot find "
        				+ (path==null?"null":path.getName())));
        }
    }

    // Internal utility methods

    /**
     * Compiles a new AppleScript instance and runs it
     * @param cmd AppleScript command to execute; do not surround command with extra quotation marks
     * @return Output of the script
     * @throws IOException If the script failed to execute
     */
    protected static String performOSAScript(CharSequence cmd) throws IOException
    {
        return performOSAScript(new CharSequence[]{cmd});
    }

    /**
     * Compiles a new AppleScript instance and runs it
     * @param cmds AppleScript Sequence of commands to execute; do not surround command with extra quotation marks
     * @return Output of the script
     * @throws IOException If the script failed to execute
     */
    protected static String performOSAScript(CharSequence[] cmds) throws IOException
    {
    	/*
        long start = System.currentTimeMillis();
       
        Debug.outNoStack("Executing OSAScript: ");
        for (int i = 0; i < cmds.length; i++)
        {
            Debug.outNoStack("\t" + cmds[i]);
        }
		*/
        
        String[] cmdargs = new String[2 * cmds.length + 1];
        cmdargs[0] = "osascript";
        for (int i = 0; i < cmds.length; i++)
        {
            cmdargs[i * 2 + 1] = "-e";
            cmdargs[i * 2 + 2] = String.valueOf(cmds[i]);
        }

        Process osaProcess = performRuntimeExec(cmdargs);
        BufferedReader reader = new BufferedReader(new InputStreamReader(osaProcess.getInputStream()));
        String line = reader.readLine();
        reader.close();
        
        //Debug.outNoStack("OSAScript Output: " + line);

        reader = new BufferedReader(new InputStreamReader(osaProcess.getErrorStream()));
        String errorMsg = reader.readLine();
        reader.close();

        //Debug.outNoStack("OSAScript Error (if any): " + errorMsg);

        //Debug.outNoStack(MessageFormat.format("OSAScript execution ended ({0}ms)", new Object[]{String.valueOf(System.currentTimeMillis() - start)}));

        try {
        	osaProcess.destroy();
        } catch (Throwable t) {
        	//ignore
        }

        if (errorMsg != null)
        {
            throw new IOException(errorMsg);
        }

        return line;
    }

    /**
     * Compiles a new AppleScript instance and runs it
     * @param script AppleScript file (.scpt) to execute
     * @return Output of the script
     * @throws IOException If the script failed to execute
     */
    protected static String performOSAScript(File script) throws IOException
    {
    	/*
        long start = System.currentTimeMillis();
        Debug.outNoStack("Executing OSAScript from file: " + script.getPath());
		*/
    	
        Process osaProcess = performRuntimeExec(new String[]{"osascript", script.getPath()});
        BufferedReader reader = new BufferedReader(new InputStreamReader(osaProcess.getInputStream()));
        String line = reader.readLine();
        reader.close();
        //Debug.outNoStack("OSAScript Output: " + line);

        reader = new BufferedReader(new InputStreamReader(osaProcess.getErrorStream()));
        String errorMsg = reader.readLine();
        reader.close();

        //Debug.outNoStack("OSAScript Error (if any): " + errorMsg);

        //Debug.outNoStack(MessageFormat.format("OSAScript execution ended ({0}ms)", new Object[]{String.valueOf(System.currentTimeMillis() - start)}));

        try {
        	osaProcess.destroy();
        } catch (Throwable t) {
        	//ignore
        }
        if (errorMsg != null)
        {
            throw new IOException(errorMsg);
        }

        return line;
    }

    /**
     * Compiles a new AppleScript instance to the specified location
     * @param cmd         Command to compile; do not surround command with extra quotation marks
     * @param destination Destination location of the AppleScript file
     * @return True if compiled successfully
     */
    protected static boolean compileOSAScript(CharSequence cmd, File destination)
    {
        return compileOSAScript(new CharSequence[]{cmd}, destination);
    }

    /**
     * Compiles a new AppleScript instance to the specified location
     * @param cmds Sequence of commands to compile; do not surround command with extra quotation marks
     * @param destination Destination location of the AppleScript file
     * @return True if compiled successfully
     */
    protected static boolean compileOSAScript(CharSequence[] cmds, File destination)
    {
    	/*
        long start = System.currentTimeMillis();
        Debug.outNoStack("Compiling OSAScript: " + destination.getPath());
        for (int i = 0; i < cmds.length; i++)
        {
            Debug.outNoStack("\t" + cmds[i]);
        }
		*/
    	
        String[] cmdargs = new String[2 * cmds.length + 3];
        cmdargs[0] = "osacompile";
        for (int i = 0; i < cmds.length; i++)
        {
            cmdargs[i * 2 + 1] = "-e";
            cmdargs[i * 2 + 2] = String.valueOf(cmds[i]);
        }

        cmdargs[cmdargs.length - 2] = "-o";
        cmdargs[cmdargs.length - 1] = destination.getPath();

        String errorMsg;
        try
        {
            Process osaProcess = performRuntimeExec(cmdargs);

            BufferedReader reader = new BufferedReader(new InputStreamReader(osaProcess.getErrorStream()));
            errorMsg = reader.readLine();
            reader.close();
        }
        catch (IOException e)
        {
            Debug.outNoStack("OSACompile Execution Failed: " + e.getMessage());
            Debug.printStackTrace(e);
            return false;
        }

        //Debug.outNoStack("OSACompile Error (if any): " + errorMsg);

        //Debug.outNoStack(MessageFormat.format("OSACompile execution ended ({0}ms)", new Object[]{String.valueOf(System.currentTimeMillis() - start)}));

        return (errorMsg == null);
    }

    /**
     * @see Runtime#exec(String[])
     */
    protected static Process performRuntimeExec(String[] cmdargs) throws IOException
    {
        try
        {
            return Runtime.getRuntime().exec(cmdargs);
        }
        catch (IOException e)
        {
            Logger.log(new LogAlert(LogAlert.UNREPEATABLE, e.getMessage(), e));
            throw e;
        }
    }

    /**
     * <p>Gets the preferred file browser name</p>
     * <p>Currently supported browsers are Path Finder and Finder. If Path Finder is currently running
     * (not just installed), then "Path Finder is returned; else, "Finder" is returned.</p>
     * @return "Path Finder" if it is currently running; else "Finder"
     */
    private static String getFileBrowserName()
    {
    	return fileBrowserName;
    }
    
	public boolean
	testNativeAvailability(
		String	name )
	
		throws PlatformManagerException
	{
	    throw new PlatformManagerException("Unsupported capability called on platform manager");		
	}
    
	public void
	traceRoute(
		InetAddress							interface_address,
		InetAddress							target,
		PlatformManagerPingCallback			callback )
	
		throws PlatformManagerException
	{
	    throw new PlatformManagerException("Unsupported capability called on platform manager");		
	}
	
	public void
	ping(
		InetAddress							interface_address,
		InetAddress							target,
		PlatformManagerPingCallback			callback )
	
		throws PlatformManagerException
	{
	    throw new PlatformManagerException("Unsupported capability called on platform manager");		
	}
	
	public int
	getMaxOpenFiles()
	
		throws PlatformManagerException
	{
        LineNumberReader lnr = null;
        
	    try{
	        Process p = 
	        	Runtime.getRuntime().exec( 
	        		new String[]{
	        				"/bin/sh",
	        				"-c",
	        				"ulimit -a" });
          
	        lnr = new LineNumberReader( new InputStreamReader( p.getInputStream()));
	        		    	        
	        Map<String,String>	map = new HashMap<>();
	        
	        while( true ){
	        	
	        	String	line = lnr.readLine();
	        	
	        	if ( line == null ){
	        		
	        		break;
	        	}
	        	
	        	int	pos1 = line.indexOf( '(' );
	        	int pos2 = line.indexOf( ')', pos1+1 );
	        	
	        	String keyword 	= line.substring( 0, pos1 ).trim().toLowerCase();
	        	String value	= line.substring( pos2+1 ).trim();
	        	
	        	map.put( keyword, value );
	        }
	        
	        String open_files = map.get( "open files" );
	        
	        if ( open_files != null ){
	        	
	        	if ( open_files.equalsIgnoreCase( "unlimited" )){
	        		
	        		return( 0 );
	        	}else{
	        		try{
	        			return( Integer.parseInt( open_files ));
	        			
	        		}catch( Throwable e ){
	        			
	        			Debug.out( "open files invalid: " + open_files );
	        		}
	        	}
	        }
	    }catch( Throwable e ){
	    	
	    }finally{
	    	
            if ( lnr != null ){
            	
                try{
                	lnr.close();
                    
                }catch( Throwable e ){
                }
            }
        }

	    return( -1 );
	}
	
    public void
    addListener(
    	PlatformManagerListener		listener )
    {
    }
    
    public void
    removeListener(
    	PlatformManagerListener		listener )
    {
    }

    public void generate(IndentWriter writer) {
    	writer.println("PlatformManager: MacOSX");
    	try {
    		writer.indent();

    		if (OSXAccess.isLoaded()) {
    			try {
    				writer.println("Version " + getVersion());
    				writer.println("User Data Dir: " + getLocation(LOC_USER_DATA));
    				writer.println("User Doc Dir: " + getLocation(LOC_DOCUMENTS));
    			} catch (PlatformManagerException e) {
    			}
    		} else {
    			writer.println("Not loaded");
    		}

    		writer.println("Computer Name: " + getComputerName());
    		
    		try{
    			writer.println("Max Open Files: " + getMaxOpenFiles());
    		}catch( Throwable e ){
    			writer.println("Max Open Files: " + Debug.getNestedExceptionMessage( e ));
    		}
    	} finally {
    		writer.exdent();
    	}
    }

	// @see org.gudy.azureus2.platform.PlatformManager#getAzComputerID()
	public String getAzComputerID() throws PlatformManagerException {
		throw new PlatformManagerException(
				"Unsupported capability called on platform manager");
	}

	/**
	 * If the application is not active causes the application icon at the bottom to bounce until the application becomes active
	 * If the application is already active then this method does nothing.
	 * 
	 * Note: This is an undocumented feature from Apple so it's behavior may change without warning
	 * 
	 * @param type one of USER_REQUEST_INFO, USER_REQUEST_WARNING
	 */
	public void requestUserAttention(int type, Object data)
			throws PlatformManagerException {
		if (type == USER_REQUEST_QUESTION) {
			return;
		}
		try {
			Class<?> claNSApplication = Class.forName("com.apple.eawt.Application");
			Method methGetApplication = claNSApplication.getMethod("getApplication");
			Object app = methGetApplication.invoke(null);
			
			Method methRequestUserAttention = claNSApplication.getMethod(
					"requestUserAttention", Boolean.class);
			if (type == USER_REQUEST_INFO) {
				methRequestUserAttention.invoke(app, false);
			} else if (type == USER_REQUEST_WARNING) {
				methRequestUserAttention.invoke(app, true);
			}

		} catch (Exception e) {
			throw new PlatformManagerException("Failed to request user attention", e);
		}

	}
	
	public static void
	main(
		String[]	args )
	{
		try{
			SystemProperties.setApplicationName( "Vuze" );
			
			// System.out.println( new PlatformManagerImpl().getMaxOpenFiles());
			
			PlatformManagerImpl pm = new PlatformManagerImpl();
			
			pm.getRunAtLogin();
			
			pm.setRunAtLogin( false );
		}catch( Throwable e ){
			
			e.printStackTrace();
		}
	}
}
