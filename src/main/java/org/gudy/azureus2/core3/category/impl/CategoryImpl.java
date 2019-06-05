/*
 * File    : CategoryImpl.java
 * Created : 09 feb. 2004
 * By      : TuxPaper
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

package org.gudy.azureus2.core3.category.impl;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.gudy.azureus2.core3.category.Category;
import org.gudy.azureus2.core3.category.CategoryListener;
import org.gudy.azureus2.core3.download.*;
import org.gudy.azureus2.core3.internat.MessageText;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.IndentWriter;
import org.gudy.azureus2.core3.util.ListenerManager;
import org.gudy.azureus2.core3.util.ListenerManagerDispatcher;

import com.aelitis.azureus.core.AzureusCore;
import com.aelitis.azureus.core.AzureusCoreFactory;
import com.aelitis.azureus.core.networkmanager.LimitedRateGroup;
import com.aelitis.azureus.core.tag.TagDownload;
import com.aelitis.azureus.core.tag.TagFeatureRunState;
import com.aelitis.azureus.core.tag.Taggable;
import com.aelitis.azureus.core.tag.impl.TagBase;
import com.aelitis.azureus.core.util.IdentityHashSet;

public class 
CategoryImpl 
	extends TagBase 
	implements Category, Comparable, TagDownload 
{
  final String sName;
  private final int type;
  private final List<DownloadManager> managers = new ArrayList<>();

  private int upload_speed;
  private int download_speed;

  private final Object UPLOAD_PRIORITY_KEY = new Object();
  
  private final Map<String,String>	attributes;
  
  private static final AtomicInteger	tag_ids = new AtomicInteger();
  
  private final LimitedRateGroup upload_limiter =
	  new LimitedRateGroup()
	  {
		  public String 
		  getName() 
		  {
			  return( "cat_up: " + sName);
		  }
		  public int 
		  getRateLimitBytesPerSecond()
		  {
			  return( upload_speed );
		  }
		  public boolean isDisabled() {
			  return( upload_speed == -1 );
		  }
		  
		  public void
		  updateBytesUsed(
				int	used )
		  {
			  
		  }
	  };
   
  private final LimitedRateGroup download_limiter =
	  new LimitedRateGroup()
  {
	  public String 
	  getName() 
	  {
		  return( "cat_down: " + sName);
	  }
	  public int 
	  getRateLimitBytesPerSecond()
	  {
		  return( download_speed );
	  }
	  public boolean isDisabled() {
		  return( download_speed == -1 );
	  }
	  public void
	  updateBytesUsed(
			int	used )
	  {
		  
	  }
  };  
  
  private boolean destroyed;
  
  private static final int LDT_CATEGORY_DMADDED     = 1;
  private static final int LDT_CATEGORY_DMREMOVED   = 2;
	private final ListenerManager<CategoryListener>	category_listeners = ListenerManager.createManager(
		"CatListenDispatcher",
		new ListenerManagerDispatcher<CategoryListener>()
		{
			public void
			dispatch(
				CategoryListener		target,
				int						type,
				Object					value )
			{
				if ( type == LDT_CATEGORY_DMADDED )
					target.downloadManagerAdded(CategoryImpl.this, (DownloadManager)value);
				else if ( type == LDT_CATEGORY_DMREMOVED )
					target.downloadManagerRemoved(CategoryImpl.this, (DownloadManager)value);
			}
		});

  public CategoryImpl(CategoryManagerImpl manager, String sName, int maxup, int maxdown, Map<String,String> _attributes ) {
	super( manager, tag_ids.incrementAndGet(), sName );
	addTag();
	
    this.sName = sName;
    this.type = Category.TYPE_USER;
    upload_speed	= maxup;
    download_speed	= maxdown;
    attributes = _attributes;
  }

  public CategoryImpl(CategoryManagerImpl manager, String sName, int type, Map<String,String> _attributes) {
	super( manager, tag_ids.incrementAndGet(), sName);
	addTag();
	
    this.sName = sName;
    this.type = type;
    attributes = _attributes;
  }

  public void addCategoryListener(CategoryListener l) {
  	if (!category_listeners.hasListener(l)) {
  		category_listeners.addListener( l );
  	}
  }

  public void removeCategoryListener(CategoryListener l) {
	  category_listeners.removeListener( l );
  }

  public boolean
  hasCategoryListener(
	 CategoryListener	l )
  {
	  return( category_listeners.hasListener( l ));
  }
  
  public String getName() {
    return sName;
  }
  
  public int getType() {
    return type;
  }
  
  public List<DownloadManager> getDownloadManagers(List<DownloadManager> all_dms) {
	  if ( type == Category.TYPE_USER ){
		  return managers;
	  }else if ( type == Category.TYPE_ALL || all_dms == null ){
		  return all_dms;
	  }else{
		  List<DownloadManager> result = new ArrayList<>();
          for (DownloadManager dm : all_dms) {
              Category cat = dm.getDownloadState().getCategory();
              if (cat == null || cat.getType() == Category.TYPE_UNCATEGORIZED) {
                  result.add(dm);
              }
          }
		  
		  return( result );
	  }
  }
  
  public void addManager(DownloadManagerState manager_state) {
  	Category manager_cat = manager_state.getCategory();
		if ((type != Category.TYPE_UNCATEGORIZED && manager_cat != this)
				|| (type == Category.TYPE_UNCATEGORIZED && manager_cat != null)) {
    	manager_state.setCategory(this);
      // we will be called again by CategoryManager.categoryChange
      return;
    }
    
    DownloadManager	manager = manager_state.getDownloadManager();
    
    	// can be null if called during downloadmanagerstate construction
    if ( manager == null ){
    	return;
    }
    
    addTaggable( manager );
    
    if (!managers.contains(manager)) {
    	if (type == Category.TYPE_USER) {
    		managers.add(manager);
    	}
      
      manager.addRateLimiter( upload_limiter, true );
      manager.addRateLimiter( download_limiter, false );
      
      int pri = getIntAttribute( AT_UPLOAD_PRIORITY, -1 );
      
      if ( pri > 0 ){
    	  
    	  	// another call-during-construction issue to avoid here
    	  
    	  if ( manager.getDownloadState() != null ){
    	  
    		  manager.updateAutoUploadPriority( UPLOAD_PRIORITY_KEY, true );
    	  }
      }
      
      category_listeners.dispatch(LDT_CATEGORY_DMADDED, manager);
    }
  }

  public void removeManager(DownloadManagerState manager_state) {
    if (manager_state.getCategory() == this) {
    	manager_state.setCategory(null);
      // we will be called again by CategoryManager.categoryChange
      return;
    }
    DownloadManager	manager = manager_state.getDownloadManager();

   	// can be null if called during downloadmanagerstate construction
    if ( manager == null ){
    	return;
    }
    
    removeTaggable( manager );
    
    if (type != Category.TYPE_USER || managers.contains(manager)) {
      managers.remove(manager);
      
      manager.removeRateLimiter( upload_limiter, true );
      manager.removeRateLimiter( download_limiter, false );
 
      int pri = getIntAttribute( AT_UPLOAD_PRIORITY, -1 );
      
      if ( pri > 0 ){
    	  
    	  	// another call-during-construction issue to avoid here
    	  
    	  if ( manager.getDownloadState() != null ){
    	  
    		  manager.updateAutoUploadPriority( UPLOAD_PRIORITY_KEY, false );
    	  }
      }
      
      category_listeners.dispatch( LDT_CATEGORY_DMREMOVED, manager );
    }
  }

  public void
  setDownloadSpeed(
	int		speed )
  {
	  if ( download_speed != speed ){
		  
		  download_speed = speed;
		  
		  CategoryManagerImpl.getInstance().saveCategories(this);
	  }
  }
  
  public int
  getDownloadSpeed()
  {
	  return( download_speed );
  }
  
  public void
  setUploadSpeed(
	int		speed )
  {
	  if ( upload_speed != speed ){
		  
		  upload_speed	= speed;
	  
		  CategoryManagerImpl.getInstance().saveCategories(this);
	  }
  }
  
  public int
  getUploadSpeed()
  {
	  return( upload_speed );
  }
  
  protected void
  setAttributes(
	Map<String,String> a )
  {
	  attributes.clear();
	  attributes.putAll( a );
  }
  
  protected Map<String,String>
  getAttributes()
  {
	  return( attributes );
  }
  
  public String
  getStringAttribute(
	String		name )
  {
	  return( attributes.get(name));
  }
  
  public void
  setStringAttribute(
	String		name,
	String		value )
  {
	  String old = attributes.put( name, value );
	  
	  if ( old == null || !old.equals( value )){
	  
		  CategoryManagerImpl.getInstance().saveCategories(this);
	  }

  }
  
  public int
  getIntAttribute(
	String		name )
  {
	  return( getIntAttribute( name, 0 ));
  }
  
  private int
  getIntAttribute(
	String		name,
	int			def )
  {
	 String str = getStringAttribute( name );
	 
	 if ( str == null ){
		 return( def );
	 }
	 return( Integer.parseInt( str ));
  }
  
  public void
  setIntAttribute(
	String		name,
	int			value )
  {
	  String	str_val = String.valueOf( value );
	  
	  String old = attributes.put( name, str_val );
	  
	  if ( old == null || !old.equals( str_val )){
	  
		  if ( name.equals( AT_UPLOAD_PRIORITY )){
			  
			  for ( DownloadManager dm: managers ){
				  
				  dm.updateAutoUploadPriority( UPLOAD_PRIORITY_KEY, value > 0 );
			  }
		  }
		  
		  CategoryManagerImpl.getInstance().saveCategories(this);
	  }

  }
  public boolean
  getBooleanAttribute(
	String		name )
  {
	 String str = getStringAttribute( name );
	 
	 return( str != null && str.equals( "true" ));
  }
  
  public void
  setBooleanAttribute(
	String		name,
	boolean		value )
  {
	  String str_val = value?"true":"false";
	  
	  String old = attributes.put( name, str_val );
	  
	  if ( old == null || !old.equals( str_val )){
	  
		  CategoryManagerImpl.getInstance().saveCategories(this);
	  }

  }
  
  public int 
  getTaggableTypes() 
  {
	  return( Taggable.TT_DOWNLOAD );
  }
  
  public String
  getTagName(
    boolean		localize )
  {
	  if ( localize ){
		  if ( type == Category.TYPE_ALL ||  type == Category.TYPE_UNCATEGORIZED){
			  return( MessageText.getString( getTagNameRaw()));
		  }
	  }
	  return( super.getTagName(localize));
  }
	
  public boolean
  supportsTagRates()
  {
	  return( false );
  }
	
  public boolean
  supportsTagUploadLimit()
  {
	  return( true );
  }

  public boolean
  supportsTagDownloadLimit()
  {
	  return( true );
  }
	
  public int
  getTagUploadLimit()
  {
	  return( getUploadSpeed());
  }

  public void
  setTagUploadLimit(
		  int		bps )
  {
	  setUploadSpeed( bps );
  }

  public int
  getTagCurrentUploadRate()
  {
	  return( -1 );
  }

  public int
  getTagDownloadLimit()
  {
	  return( getDownloadSpeed());
  }

  public void
  setTagDownloadLimit(
		  int		bps )
  {
	  setDownloadSpeed( bps );
  }

  public int
  getTagCurrentDownloadRate()
  {
	  return( -1 );
  }
  
  public int
  getTagUploadPriority()
  {
	  if ( type == Category.TYPE_USER ){
		  
		  return( getIntAttribute( AT_UPLOAD_PRIORITY ));
		  
	  }else{
		  
		  return( -1 );
	  }
  }

  public void
  setTagUploadPriority(
	  int		priority )
  {
	  setIntAttribute( AT_UPLOAD_PRIORITY, priority );
  }
  
  public boolean
  getCanBePublicDefault()
  {
	  return( type == Category.TYPE_USER );
  }
  
  public boolean
  supportsTagTranscode()
  {
	  return( false );
  }

  public String[]
  getTagTranscodeTarget()
  {
	  return( null );
  }

  public void
  setTagTranscodeTarget(
	  String		uid,
	  String		display_name )
  {
  }
	
  public Set<DownloadManager>
  getTaggedDownloads()
  {
  	AzureusCore core = AzureusCoreFactory.getSingleton();
  	
  	if ( !core.isStarted()){
  		
  		return new IdentityHashSet<>();
  	}
	return(new IdentityHashSet<>(getDownloadManagers(core.getGlobalManager().getDownloadManagers())));
  }
  
  public Set<Taggable> 
  getTagged() 
  {
	  return((Set<Taggable>)(Object)( getTaggedDownloads()));
  }
  
	public int 
	getTaggedCount() 
	{
		return( getTagged().size());
	}
		
	public boolean 
	hasTaggable(
		Taggable	t )
	{
		if ( !( t instanceof DownloadManager )){
			
			return( false );
		}
			  	
	  	if ( type == Category.TYPE_USER ){
	  		
	  		return( managers.contains( t ));
	  		
	  	}else if ( type == Category.TYPE_ALL ){
	  		
	  		return( true );
	  		
	  	}else{
	  		
	  		DownloadManager dm = (DownloadManager)t;
	  		
	  		Category cat = dm.getDownloadState().getCategory();

			return cat == null || cat.getType() == Category.TYPE_UNCATEGORIZED;
	  	}
	}
	
	public int
	getRunStateCapabilities()
	{
		return( TagFeatureRunState.RSC_NONE );
	}
	
	public boolean
	hasRunStateCapability(
		int		capability )
	{
		return( false );
	}
	
	public boolean[]
	getPerformableOperations(
   		int[]	ops )
	{
		return( new boolean[ ops.length]);
	}
	
	public void
	performOperation(
		int		op )
	{
		Debug.out( "derp" );
	}
	
  protected void
  destroy()
  {
	  if ( !destroyed ){
		  
		  destroyed = true;
	  
		  removeTag();
	  }
  }
  
  public int compareTo(Object b)
  {
    boolean aTypeIsUser = type == Category.TYPE_USER;
    boolean bTypeIsUser = ((Category)b).getType() == Category.TYPE_USER;
    if (aTypeIsUser == bTypeIsUser)
      return sName.compareToIgnoreCase(((Category)b).getName());
    if (aTypeIsUser)
      return 1;
    return -1;
  }
  
  public void dump(IndentWriter writer) {
	if ( upload_speed != 0 ){
		writer.println( "up=" + upload_speed );
	}
	if ( download_speed != 0 ){
		writer.println( "down=" + download_speed );
	}
	if ( attributes.size() > 0 ){
		
		writer.println( "attributes: " + attributes );
	}
	}
}
