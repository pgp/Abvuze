/*
 * File    : TorrentDownloader2Impl.java
 * Created : 27-Feb-2004
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

package org.gudy.azureus2.pluginsimpl.local.utils.resourcedownloader;

/**
 * @author parg
 *
 */

import java.util.*;
import java.io.*;

import org.gudy.azureus2.core3.internat.MessageText;
import org.gudy.azureus2.core3.util.AEMonitor;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.plugins.utils.resourcedownloader.*;

public abstract class 
ResourceDownloaderBaseImpl
	implements ResourceDownloader
{
	private static final String PR_PROPERTIES_SET = "!!!! properties set !!!!";
	
	private List			listeners		= new ArrayList();
	
	private boolean		result_informed;
	private Object		result_informed_data;
	
	private ResourceDownloaderBaseImpl			parent;
	private List<ResourceDownloaderBaseImpl>	children = new ArrayList<>();
	
	private boolean		download_cancelled;
	
	private Map			lc_key_properties	= new HashMap();
		
	protected AEMonitor		this_mon	= new AEMonitor( "ResourceDownloader" );

	protected
	ResourceDownloaderBaseImpl(
		ResourceDownloaderBaseImpl	_parent )
	{
		parent	= _parent;
		
		if ( parent != null ){
			
			parent.addChild(this);
		}
	}
	
	public ResourceDownloader
	getClone()
	{
		return( getClone( null ));
	}
	
	public abstract ResourceDownloaderBaseImpl
	getClone(
		ResourceDownloaderBaseImpl	_parent );

	protected abstract void
	setSize(
		long	size );
	
	public boolean
	getBooleanProperty(
		String		key )
	
		throws ResourceDownloaderException
	{
		return( getBooleanProperty( key, true ));
	}
	
	public boolean
	getBooleanProperty(
		String		key,
		boolean		maybe_delayed )
	
		throws ResourceDownloaderException
	{
		Object obj = getProperty( key, maybe_delayed );
	
		if ( obj instanceof Boolean ){
			
			return((Boolean) obj);
		}
		
		return( false );
	}
	
	public long
	getLongProperty(
		String		key )
	
		throws ResourceDownloaderException
	{
		Object obj = getProperty( key );
	
		if (!(obj instanceof Number)){
			
			return( -1 );
		}
		
		return(((Number)obj).longValue());
	}
	
	public String
	getStringProperty(
		String		key )
	
		throws ResourceDownloaderException
	{
		Object obj = getProperty( key );
		
		if ( obj == null || obj instanceof String ){
			
			return((String)obj);
		}
			
		if ( obj instanceof List ){
			
			List l = (List)obj;
			
			if ( l.size() == 0 ){
				
				return( null );
			}
			
			obj = l.get( 0 );
			
			if ( obj instanceof String ){
				
				return((String)obj);
			}
		}
		
		return( null );
	}
	
	public Object
	getProperty(
		String		name )
	
		throws ResourceDownloaderException
	{
		return( getProperty( name, true ));
	}
	
	protected Object
	getProperty(
		String		name,
		boolean		maybe_delayed )
	
		throws ResourceDownloaderException
	{
		Object res = getPropertySupport( name );
		
		if ( 	res != null || 
				getPropertySupport( PR_PROPERTIES_SET ) != null ||
				name.equalsIgnoreCase( "URL_Connection" ) ||
				name.equalsIgnoreCase( "URL_Connect_Timeout" ) ||
				name.equalsIgnoreCase( "URL_Read_Timeout" ) ||
				name.equalsIgnoreCase( "URL_Trust_Content_Length" ) ||
				name.equalsIgnoreCase( "URL_HTTP_VERB" )){
			
			return( res );
		}
		
		if ( maybe_delayed ){
		
				// hack this, properties are read during size acquisition - should treat size as a property
				// too....
			
			getSize();
			
			return( getPropertySupport( name ));
			
		}else{
			
			return( res );
		}
	}
	
	protected Object
	getPropertySupport(
		String	name )
	{
		return( lc_key_properties.get( name.toLowerCase(MessageText.LOCALE_ENGLISH)));
	}
	
	protected Map
	getLCKeyProperties()
	{
		return( lc_key_properties );
	}
	
	protected String
	getStringPropertySupport(
		String	name )
	{
		Object	 obj = lc_key_properties.get( name.toLowerCase(MessageText.LOCALE_ENGLISH));
		
		if ( obj instanceof String ){
			
			return((String)obj);
		}
		
		return( null );
	}
	
	protected void
	setPropertiesSet()
	
		throws ResourceDownloaderException
	{
		setProperty( PR_PROPERTIES_SET, "true" );
	}
	
	protected void
	setPropertySupport(
		String	name,
		Object	value )
	{
		boolean already_set = lc_key_properties.put( name.toLowerCase(MessageText.LOCALE_ENGLISH), value ) == value;
		
		if ( parent != null && !already_set ){
			
			try{
				parent.setProperty( name, value );
				
			}catch( Throwable e ){
				
				Debug.printStackTrace(e);
			}
		}
	}

	protected void
	setProperties(
		ResourceDownloaderBaseImpl	other )
	{
		Map p = other.lc_key_properties;

        for (Object o : p.keySet()) {

            String key = (String) o;

            try {
                setProperty(key, p.get(key));

            } catch (Throwable e) {

                Debug.printStackTrace(e);
            }
        }
	}

	protected void
	setPropertyRecursive(
		String		name, 
		Object		value )
		
		throws ResourceDownloaderException
	{
		setProperty(name, value);
		
		for ( ResourceDownloaderBaseImpl kid: getChildren()){
			
			kid.setPropertyRecursive(name, value);
		}
	}
	
	protected boolean
	isAnonymous()
	{
		try{
			return( getBooleanProperty( PR_BOOLEAN_ANONYMOUS, false ));
			
		}catch( Throwable e ){
			
			Debug.out( e );
		}
		
		return( false );
	}
	
	protected void
	setParent(
		ResourceDownloader		_parent )
	{
		ResourceDownloaderBaseImpl	old_parent	= parent;
		
		parent	= (ResourceDownloaderBaseImpl)_parent;
		
		if( old_parent != null ){
			
			old_parent.removeChild( this );
		}
		
		if ( parent != null ){
			
			parent.addChild( this );
		}
	}
	
	protected ResourceDownloaderBaseImpl
	getParent()
	{
		return( parent );
	}
	
	protected void
	addChild(
		ResourceDownloaderBaseImpl	kid )
	{
		children.add( kid );
	}
	
	protected void
	removeChild(
		ResourceDownloaderBaseImpl	kid )
	{
		children.remove( kid );
	}
	
	protected List<ResourceDownloaderBaseImpl>
	getChildren()
	{
		return( children );
	}
	
	protected String
	getLogIndent()
	{
		String	indent = "";
		
		ResourceDownloaderBaseImpl	pos = parent;
		
		while( pos != null ){
			
			indent += "  ";
			
			pos = pos.getParent();
		}
		
		return( indent );
	}
	
		// adds a listener that simply logs messages. used during size getting
	
	protected void
	addReportListener(
		ResourceDownloader	rd )
	{
		rd.addListener(
				new ResourceDownloaderAdapter()
				{
					public void
					reportActivity(
						ResourceDownloader	downloader,
						String				activity )
					{
						informActivity( activity );
					}
					
					public void
					failed(
						ResourceDownloader			downloader,
						ResourceDownloaderException e )
					{
						informActivity( downloader.getName() + ":" + e.getMessage());
					}
				});
	}
	
	protected void
	informPercentDone(
		int	percentage )
	{
		for (Object listener : listeners) {

			try {
				((ResourceDownloaderListener) listener).reportPercentComplete(this, percentage);

			} catch (Throwable e) {

				Debug.printStackTrace(e);
			}
		}
	}
	
	protected void
	informAmountComplete(
		long	amount )
	{
		for (Object listener : listeners) {

			try {
				((ResourceDownloaderListener) listener).reportAmountComplete(this, amount);

			} catch (NoSuchMethodError | AbstractMethodError e) {

				// handle addition of this new method with old impls
			} catch (Throwable e) {

				Debug.printStackTrace(e);
			}
		}
	}
	
	public void
	reportActivity(
		String	str )
	{
		informActivity( str );
	}
	
	protected void
	informActivity(
		String	activity )
	{
		for (Object listener : listeners) {

			try {
				((ResourceDownloaderListener) listener).reportActivity(this, activity);

			} catch (Throwable e) {

				Debug.printStackTrace(e);
			}
		}
	}
	
	protected boolean
	informComplete(
		InputStream	is )
	{
		if ( !result_informed ){

			for (Object listener : listeners) {

				try {
					if (!((ResourceDownloaderListener) listener).completed(this, is)) {

						return (false);
					}
				} catch (Throwable e) {

					Debug.printStackTrace(e);

					return (false);
				}
			}
			
			result_informed	= true;
			
			result_informed_data	= is;
		}
		
		return( true );
	}
	
	protected void
	informFailed(
		ResourceDownloaderException	e )
	{
		if ( !result_informed ){
			
			result_informed	= true;
		
			result_informed_data = e;

			for (Object listener : listeners) {

				try {
					((ResourceDownloaderListener) listener).failed(this, e);

				} catch (Throwable f) {

					Debug.printStackTrace(f);
				}
			}
		}
	}
	
	public void
	reportActivity(
		ResourceDownloader	downloader,
		String				activity )
	{
		informActivity( activity );
	}
	
	public void
	reportPercentComplete(
		ResourceDownloader	downloader,
		int					percentage )
	{
		informPercentDone( percentage );
	}
	
	public void
	reportAmountComplete(
		ResourceDownloader	downloader,
		long				amount )
	{
		informAmountComplete( amount );
	}
	
	protected void
	setCancelled()
	{
		download_cancelled	= true;
	}
	
	public boolean
	isCancelled()
	{
		return( download_cancelled );
	}
	
	public void
	addListener(
		ResourceDownloaderListener		l )
	{
		listeners.add( l );
		
		if ( result_informed ){
			
			if (result_informed_data instanceof InputStream ){
				
				l.completed( this, (InputStream)result_informed_data);
			}else{
				
				l.failed( this, (ResourceDownloaderException)result_informed_data);
			}
		}
	}
	
	public void
	removeListener(
		ResourceDownloaderListener		l )
	{
		listeners.remove(l);
	}
}
