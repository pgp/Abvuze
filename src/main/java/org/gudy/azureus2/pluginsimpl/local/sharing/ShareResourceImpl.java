/*
 * File    : ShareResourceImpl.java
 * Created : 31-Dec-2003
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

package org.gudy.azureus2.pluginsimpl.local.sharing;

/**
 * @author parg
 *
 */

import java.util.*;
import java.io.*;

import org.gudy.azureus2.plugins.sharing.*;
import org.gudy.azureus2.plugins.torrent.TorrentAttribute;
import org.gudy.azureus2.pluginsimpl.local.torrent.TorrentManagerImpl;
import org.gudy.azureus2.core3.util.*;
import org.gudy.azureus2.core3.internat.*;

public abstract class 
ShareResourceImpl
	implements ShareResource
{
	protected static BrokenMd5Hasher	hasher = new BrokenMd5Hasher();
	
	protected ShareManagerImpl				manager;
	protected int							type;
	protected ShareResourceDirContents		parent;
	
	protected Map	attributes			= new HashMap();
	
	protected List	change_listeners 	= new ArrayList();
	protected List	deletion_listeners 	= new ArrayList();
	
		// new constructor
	
	protected
	ShareResourceImpl(
		ShareManagerImpl	_manager,
		int					_type )
	{
		manager	= _manager;
		type 	= _type;
	}

		// deserialised constructor
	
	protected
	ShareResourceImpl(
		ShareManagerImpl	_manager,
		int					_type,
		Map					_map )
	{
		manager	= _manager;
		type 	= _type;
		
		Map	attrs = (Map)_map.get( "attributes" );
		
		if ( attrs != null ){

            for (Object o : attrs.keySet()) {

                String key = (String) o;

                try {
                    String value = new String((byte[]) attrs.get(key), Constants.DEFAULT_ENCODING);

                    TorrentAttribute ta = TorrentManagerImpl.getSingleton().getAttribute(key);

                    if (ta == null) {

                        Debug.out("Invalid attribute '" + key);
                    } else {

                        attributes.put(ta, value);
                    }
                } catch (Throwable e) {

                    Debug.printStackTrace(e);
                }
            }
		}
	}
	
	protected void
	serialiseResource(
		Map		map )
	{
		Iterator	it = attributes.keySet().iterator();
		
		Map	attrs = new HashMap();
		
		map.put( "attributes", attrs );
		
		while( it.hasNext()){
			
			TorrentAttribute	ta = (TorrentAttribute)it.next();
			
			String	value = (String)attributes.get(ta);
			
			try{
				if ( value != null ){
					
					attrs.put( ta.getName(), value.getBytes( Constants.DEFAULT_ENCODING ));
					
				}
			}catch( Throwable e ){
				
				Debug.printStackTrace(e);
			}
		}
	}
	
	public ShareResourceDirContents
	getParent()
	{
		return( parent );
	}
	
	protected void
	setParent(
		ShareResourceDirContents	_parent )
	{
		parent	= _parent;
	}
	
	public ShareResource[]
	getChildren()
	{
		return( new ShareResource[0] );
	}
	
	public int
	getType()
	{
		return( type );
	}
	
	public void
	setAttribute(
		final TorrentAttribute		attribute,
		String						value )
	{
		ShareConfigImpl	config = manager.getShareConfig();
				
		try{
			config.suspendSaving();
		
			ShareResource[]	kids = getChildren();

            for (ShareResource kid : kids) {

                kid.setAttribute(attribute, value);
            }
			
			String	old_value = (String)attributes.get( attribute );
			
			if( old_value == null && value == null ){
				
				return;
			}
			
			if (old_value != null && old_value.equals(value)){
				
				return;
			}
			
			attributes.put( attribute, value );
			
			try{
				config.saveConfig();
				
			}catch( ShareException e ){
				
				Debug.printStackTrace( e );
			}
			
		}finally{
				
			try{
				config.resumeSaving();
					
			}catch( ShareException e ){
					
				Debug.printStackTrace( e );
			}
		}

        for (Object change_listener : change_listeners) {

            try {
                ((ShareResourceListener) change_listener).shareResourceChanged(
                        this,
                        new ShareResourceEvent() {
                            public int
                            getType() {
                                return (ShareResourceEvent.ET_ATTRIBUTE_CHANGED);
                            }

                            public Object
                            getData() {
                                return (attribute);
                            }
                        });

            } catch (Throwable e) {

                Debug.printStackTrace(e);
            }
        }
	}
	
	public String
	getAttribute(
		TorrentAttribute		attribute )
	{
		return((String)attributes.get( attribute ));
	}
	
	public TorrentAttribute[]
	getAttributes()
	{
		TorrentAttribute[]	res = new TorrentAttribute[attributes.size()];
		
		attributes.keySet().toArray( res );
		
		return( res );
	}
	
	protected void
	inheritAttributes(
		ShareResourceImpl	source )
	{
		TorrentAttribute[]	attrs = source.getAttributes();

        for (TorrentAttribute attr : attrs) {

            setAttribute(attr, source.getAttribute(attr));
        }
	}
	
	public void
	delete()
	
		throws ShareException, ShareResourceDeletionVetoException
	{
		if ( getParent() != null ){
			
		
			throw( new ShareResourceDeletionVetoException( MessageText.getString("plugin.sharing.remove.veto")));
		}
		
		delete( false );
	}
	
	public void
	delete(
		boolean	force )
	
		throws ShareException, ShareResourceDeletionVetoException
	{
		delete( force, true );
	}
	
	public void
	delete(
		boolean	force,
		boolean	fire_listeners )
	
		throws ShareException, ShareResourceDeletionVetoException
	{
		if ( !force ){
	
			canBeDeleted();
		}
		
		manager.delete( this, fire_listeners );
	}
	
	public abstract boolean
	canBeDeleted()
	
		throws ShareResourceDeletionVetoException;
	
	public boolean
	isPersistent()
	{
		Map<String,String>	properties = getProperties();
		
		if ( properties == null ){
			
			return( false );
		}
		
		String persistent_str = properties.get( ShareManager.PR_PERSISTENT );
	
		boolean	persistent = persistent_str!=null && persistent_str.equalsIgnoreCase( "true" );
		
		return( persistent );
	}
	
	protected abstract void
	deleteInternal();
	
	protected byte[]
	getFingerPrint(
		File		file )
	
		throws ShareException
	{
		try{
			StringBuffer	buffer = new StringBuffer();
			
			getFingerPrintSupport( buffer, file, TorrentUtils.getIgnoreSet());
							
			return( hasher.calculateHash(buffer.toString().getBytes()));
			
		}catch( ShareException e ){
			
			throw( e );
			
		}catch( Throwable e ){
			
			throw( new ShareException( "ShareResource::getFingerPrint: fails", e ));
		}
	}
	
	protected void
	getFingerPrintSupport(
		StringBuffer	buffer,
		File			file,
		Set				ignore_set )
	
		throws ShareException
	{
		try{
			if ( file.isFile()){
				
				long	mod 	= file.lastModified();
				long	size	= file.length();
			
				String	file_name = file.getName();
				
				if  ( ignore_set.contains( file_name.toLowerCase())){
										
				}else{
					
					buffer.append( file_name ).append( ":" ).append( mod ).append( ":" ).append( size );
				}	
			}else if ( file.isDirectory()){
								
				File[]	dir_file_list = file.listFiles();
										
				List file_list = new ArrayList(Arrays.asList(dir_file_list));
				
				Collections.sort(file_list);

                for (Object o : file_list) {

                    File f = (File) o;

                    String file_name = f.getName();

                    if (!(file_name.equals(".") || file_name.equals(".."))) {

                        StringBuffer sub_print = new StringBuffer();

                        getFingerPrintSupport(sub_print, f, ignore_set);

                        if (sub_print.length() > 0) {

                            buffer.append(":").append(sub_print);
                        }
                    }
                }
			}else{
				
				throw( new ShareException( "ShareResource::getFingerPrint: '" + file.toString() + "' doesn't exist" ));
			}
			
		}catch( Throwable e ){
			
			if ( e instanceof ShareException ){
				
				throw((ShareException)e);
			}
			
			Debug.printStackTrace( e );
			
			throw( new ShareException( "ShareResource::getFingerPrint: fails", e ));
		}
	}
	protected String
	getNewTorrentLocation()
	
		throws ShareException
	{
		return( manager.getNewTorrentLocation());
	}
	
	protected void
	writeTorrent(
		ShareItemImpl		item )
	
		throws ShareException
	{
		manager.writeTorrent( item );
	}
	
	protected void
	readTorrent(
		ShareItemImpl		item )
	
		throws ShareException
	{
		manager.readTorrent( item );
	}	
	
	protected void
	deleteTorrent(
		ShareItemImpl		item )
	{
		manager.deleteTorrent( item );
	}
	
	public File
	getTorrentFile(
		ShareItemImpl		item )
	{
		return( manager.getTorrentFile(item));
	}
	
	protected abstract void
	checkConsistency()
	
		throws ShareException;
	
	public void
	addChangeListener(
		ShareResourceListener	l )
	{
		change_listeners.add( l );
	}
	
	public void
	removeChangeListener(
		ShareResourceListener	l )
	{
		change_listeners.remove( l );
	}
	
	public void
	addDeletionListener(
		ShareResourceWillBeDeletedListener	l )
	{
		deletion_listeners.add( l );
	}
	
	public void
	removeDeletionListener(
		ShareResourceWillBeDeletedListener	l )
	{
		deletion_listeners.remove( l );
	}
}
