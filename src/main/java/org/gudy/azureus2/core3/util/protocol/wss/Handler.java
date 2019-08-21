/*
 * File    : Handler.java
 * Created : 19-Jan-2004
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

package org.gudy.azureus2.core3.util.protocol.wss;

/**
 * @author parg
 *
 */

import java.io.IOException;
import java.net.*;

import org.gudy.azureus2.core3.internat.MessageText;
import org.gudy.azureus2.core3.util.AEThread2;
import org.gudy.azureus2.core3.util.Constants;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.ipc.IPCException;
import org.gudy.azureus2.plugins.ipc.IPCInterface;

import com.aelitis.azureus.core.AzureusCoreFactory;
import com.aelitis.azureus.ui.UIFunctions;
import com.aelitis.azureus.ui.UIFunctionsManager;
import com.aelitis.azureus.ui.UIFunctionsUserPrompter;

public class 
Handler 
	extends URLStreamHandler 
{
	private static boolean	install_prompted	= false;
	
	public URLConnection 
	openConnection(URL u)
	
		throws IOException
	{	
		return( getProxy(u).openConnection());
	}

	private URL
	getProxy(
		URL		u )
		
		throws IOException
	{
		PluginInterface pi = AzureusCoreFactory.getSingleton().getPluginManager().getPluginInterfaceByID( "azwebtorrent" );
		
		if ( pi == null ){
			
			installPlugin();
			
			throw( new IOException( "'WebTorrent Support Plugin' is required - go to 'Tools->Plugins->Installation Wizard' to install." ));
		}
		
		IPCInterface ipc = pi.getIPC();
		
		try {
			return (URL)ipc.invoke("getProxyURL", new Object[]{u});
		}catch( IPCException ipce ){
			
			Throwable e = ipce;
			
			if ( e.getCause() != null ){
				
				e = e.getCause();
			}
			
			throw( new IOException( "Communication error with WebTorrent Support Plugin: " + Debug.getNestedExceptionMessage(e)));
		}
	}
	
	private static void
	installPlugin()
	{
		synchronized( Handler.class ){
			
			if ( install_prompted ){
								
				return;
			}
			
			install_prompted = true;
		}
		
		new AEThread2( "install::async" )
		{
			public void
			run()
			{
				boolean	installing = false;
				
				try{
					UIFunctions uif = UIFunctionsManager.getUIFunctions();
					
					if ( uif == null ){
										
						return;
					}
									
					String title = MessageText.getString("azwebtorrent.install");
					
					String text = MessageText.getString("azwebtorrent.install.text" );
					
					UIFunctionsUserPrompter prompter = uif.getUserPrompter(title, text, new String[] {
						MessageText.getString("Button.yes"),
						MessageText.getString("Button.no")
					}, 0);
					
					String remember_id = "azwebtorrent.install.remember.id";
					
					if ( remember_id != null ){
						
						prompter.setRemember( 
							remember_id, 
							false,
							MessageText.getString("MessageBoxWindow.nomoreprompting"));
					}
					
					prompter.setAutoCloseInMS(0);
					
					prompter.open(null);
					
					boolean	install = prompter.waitUntilClosed() == 0;
					
					if ( install ){
							
						if ( !Constants.isJava7OrHigher ){
							
							title = MessageText.getString("azwebtorrent.install.fail.jver");
							
							text = MessageText.getString("azwebtorrent.install.fail.jver.text" );
							
							prompter = uif.getUserPrompter(title, text, new String[]{
								MessageText.getString("Button.ok"),
							}, 0);
			
							prompter.setAutoCloseInMS(0);
							
							prompter.open(null);
							
						}else{

							uif.installPlugin(
								"azwebtorrent",
								"azwebtorrent.install",
								new UIFunctions.actionListener()
								{
									public void
									actionComplete(
										Object		result )
									{
									}
								});
							
							installing = true;
						}
					}

				}finally{
					
					if ( !installing ){
					
					}
				}
			}
		}.start();
	}
}
