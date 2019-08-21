/**
 * Copyright (C) Azureus Software, Inc, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */

package org.gudy.azureus2.core3.download.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.util.FileUtil;

import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.download.savelocation.DefaultSaveLocationManager;
import org.gudy.azureus2.plugins.download.savelocation.SaveLocationChange;
import org.gudy.azureus2.pluginsimpl.local.download.DownloadImpl;

import com.aelitis.azureus.core.tag.Tag;
import com.aelitis.azureus.core.tag.TagFeature;
import com.aelitis.azureus.core.tag.TagFeatureFileLocation;
import com.aelitis.azureus.core.tag.TagManagerFactory;


public class DownloadManagerDefaultPaths extends DownloadManagerMoveHandlerUtils {
	
	public final static DefaultSaveLocationManager DEFAULT_HANDLER = new DefaultSaveLocationManager() {
		public SaveLocationChange onInitialization(Download d, boolean for_move, boolean on_event) {
			
			/**
			 * This manager object isn't the sort of object which decides on
			 * an alternate initialisation place for a download - if a user
			 * has chosen a path for it, we don't interfere with it under any
			 * circumstances (though if plugins want to, then that's up to them). 
			 */
			if (on_event) {return null;}
			
			DownloadManager dm = ((DownloadImpl)d).getDownload();
			return determinePaths(dm, UPDATE_FOR_MOVE_DETAILS[1], for_move, false); // 1 - incomplete downloads
		}
		public SaveLocationChange onCompletion(Download d, boolean for_move, boolean on_event) {
			DownloadManager dm = ((DownloadImpl)d).getDownload();
			MovementInformation mi = getTagMovementInformation( dm, COMPLETION_DETAILS );
			return determinePaths(dm, mi, for_move, false);
		}
		public SaveLocationChange testOnCompletion(Download d, boolean for_move, boolean on_event) {
			DownloadManager dm = ((DownloadImpl)d).getDownload();
			MovementInformation mi = getTagMovementInformation( dm, COMPLETION_DETAILS );
			return determinePaths(dm, mi, for_move, true );
		}
		public SaveLocationChange onRemoval(Download d, boolean for_move, boolean on_event) {
			DownloadManager dm = ((DownloadImpl)d).getDownload();
			return determinePaths(dm, REMOVAL_DETAILS, for_move, false );
		}
		public boolean isInDefaultSaveDir(Download d) {
			DownloadManager dm = ((DownloadImpl)d).getDownload();
			return DownloadManagerDefaultPaths.isInDefaultDownloadDir(dm);
		}
	};
	
    private final static MovementInformation COMPLETION_DETAILS;
    private final static MovementInformation REMOVAL_DETAILS;
    private final static MovementInformation[] UPDATE_FOR_MOVE_DETAILS;
    private final static TargetSpecification[] DEFAULT_DIRS;

    private final static String SUBDIR_PARAM = "File.move.subdir_is_default";


    static {
        SourceSpecification source;
        TargetSpecification dest;
        TransferSpecification trans;
        MovementInformation mi_1, mi_2;

        /**
         * There are three sets of directories that we consider a "default"
         * directory (perhaps it should just be two):
         *
         * - default save dir
         * - completed save dir
         * - removed save dir
         */
        DEFAULT_DIRS = new TargetSpecification[3];
        dest = new TargetSpecification();
        dest.setBoolean("enabled", true);
        dest.setString("target", "Default save path");
        dest.setContext("default save dir");
        DEFAULT_DIRS[0] = dest;

        // First - download completion details.
		source = new SourceSpecification();
		source.setBoolean("default dir", "Move Only When In Default Save Dir");
		source.setBoolean("default subdir", SUBDIR_PARAM);
		source.setBoolean("incomplete dl", false);

		dest = new TargetSpecification();
		dest.setBoolean("enabled", "Move Completed When Done");
		dest.setString("target", "Completed Files Directory");
		dest.setContext("completed files dir");
		dest.setBoolean("torrent", "Move Torrent When Done");
		dest.setString("torrent_path", "Move Torrent When Done Directory");

		trans = new TransferSpecification();
		
		mi_1 = new MovementInformation(source, dest, trans, "Move on completion");
		COMPLETION_DETAILS = mi_1;
		DEFAULT_DIRS[1] = dest;

		// Next - download removal details.
		source = new SourceSpecification();
		source.setBoolean("default dir", "File.move.download.removed.only_in_default");
		source.setBoolean("default subdir", SUBDIR_PARAM);
		source.setBoolean("incomplete dl", false);

		dest = new TargetSpecification();
		dest.setBoolean("enabled", "File.move.download.removed.enabled");
		dest.setString("target", "File.move.download.removed.path");
		dest.setContext("removed files dir");
		dest.setBoolean("torrent", "File.move.download.removed.move_torrent");
		dest.setString("torrent_path", "File.move.download.removed.move_torrent_path");

		trans = new TransferSpecification();

		mi_1 = new MovementInformation(source, dest, trans, "Move on removal");
		REMOVAL_DETAILS = mi_1;
		DEFAULT_DIRS[2] = dest;

	    /**
	     * Next - updating the current path (complete dl's first)
	     * 
	     * We instantiate the "update incomplete download" source first, and then
	     * we instantiate the "update complete download", but when we process, we
	     * will do the complete download bit first.
	     * 
	     * We do this, because in the "update incomplete download" section, completed
	     * downloads are enabled for it. And the reason it is, is because this will
	     * allow the code to behave properly if move on completion is not enabled.
	     *
	     * Complete downloads apply to this bit, just in case the "move on completion"
	     * section isn't active.
	     */
		source = new SourceSpecification();
		source.updateSettings(COMPLETION_DETAILS.source.getSettings());
		source.setBoolean("default dir", true);

		mi_1 = new MovementInformation(source, COMPLETION_DETAILS.target,
				COMPLETION_DETAILS.transfer, "Update completed download");
		
		// Now incomplete downloads. We have to define completely new settings for
		// it, since we've never defined it before.
		source = new SourceSpecification();
		source.setBoolean("default dir", true); // Must be in default directory to update.
		source.setBoolean("default subdir", SUBDIR_PARAM);
		source.setBoolean("incomplete dl", true);

		dest = new TargetSpecification();
		dest.setBoolean("enabled", true);
		dest.setString("target", "Default save path");
		dest.setBoolean("torrent", false);

		trans = new TransferSpecification();

        // Rest of the settings are the same.
		mi_2 = new MovementInformation(source, dest, trans, "Update incomplete download");
		UPDATE_FOR_MOVE_DETAILS = new MovementInformation[] {mi_1, mi_2};

    }
    
    private static MovementInformation
    getTagMovementInformation(
    	DownloadManager			dm,
    	MovementInformation		def_mi )
    {
    	List<Tag> dm_tags = TagManagerFactory.getTagManager().getTagsForTaggable( dm );
    	
    	if ( dm_tags == null || dm_tags.size() == 0 ){
    		
    		return( def_mi );
    	}
    	
    	List<Tag>	applicable_tags = new ArrayList<>();
    	
    	for ( Tag tag: dm_tags ){
    		
    		if ( tag.getTagType().hasTagTypeFeature( TagFeature.TF_FILE_LOCATION )){
    	
    			TagFeatureFileLocation fl = (TagFeatureFileLocation)tag;
    			
    			if ( fl.supportsTagMoveOnComplete()){
    				
	    			File move_to = fl.getTagMoveOnCompleteFolder();
	    			
	    			if ( move_to != null ){
	    				
	    				if ( !move_to.exists()){
	    					
	    					move_to.mkdirs();
	    				}
	    				
	    				if ( move_to.isDirectory() && move_to.canWrite()){
	    			
	    					applicable_tags.add( tag );
	    					
	    				}else{
	    				
	    					logInfo( "Ignoring invalid tag move-to location: " + move_to, dm );
	    				}
	    			}
    			}
    		}
    	}
    	
    	if ( applicable_tags.size() == 0 ){
    		
    		return( def_mi );
    		
    	}else if ( applicable_tags.size() > 1 ){
    		
    		Collections.sort(
    			applicable_tags,
    			new Comparator<Tag>()
    			{
    				public int 
    				compare(
    					Tag o1, 
    					Tag o2) 
    				{
    					return( o1.getTagID() - o2.getTagID());
    				}
    			});
    		
    		String str = "";
    		
    		for ( Tag tag: applicable_tags ){
    			
    			str += (str.length()==0?"":", ") + tag.getTagName( true );
    		}
    		
    		logInfo( "Multiple applicable tags found: " + str + " - selecting first", dm );
    	}
    	
    	Tag tag_target = applicable_tags.get(0);
    	
		TagFeatureFileLocation fl = (TagFeatureFileLocation)tag_target;
			
		File move_to = fl.getTagMoveOnCompleteFolder();

		if ( move_to != null ){
			
			long	options = fl.getTagMoveOnCompleteOptions();
			
			boolean	move_data 		= ( options&TagFeatureFileLocation.FL_DATA ) != 0;
			boolean	move_torrent 	= ( options&TagFeatureFileLocation.FL_TORRENT ) != 0;
			
	    	SourceSpecification source = new SourceSpecification();
	    	
	    		// we want to ignore the 'move only in def folder' constraint if the user hasn't
	    		// enabled overall move-on-complete otherwise this is confusing  
	    	
	    	if ( def_mi.target.getBoolean( "enabled", false ) ){
	    		source.setBoolean( "default dir", "Move Only When In Default Save Dir" );
	    		source.setBoolean( "default subdir", SUBDIR_PARAM );
	    	}else{
	    		source.setBoolean( "default dir", false );
	    	}
	    	
			source.setBoolean( "incomplete dl", false );
				
			TargetSpecification dest = new TargetSpecification();
			
			if ( move_data ){
				
				dest.setBoolean( "enabled", true );
				dest.setString( "target_raw", move_to.getAbsolutePath());
				
			}else{
				
				dest.setBoolean( "enabled", def_mi.target.getBoolean( "enabled", false ));	
			}
			
			dest.setContext( "Tag '" + tag_target.getTagName( true ) + "' move-on-complete directory" );
				
			if ( move_torrent ){
				
				dest.setBoolean("torrent", true );
				dest.setString("torrent_path_raw", move_to.getAbsolutePath());

			}else{
				
				dest.setBoolean("torrent", "Move Torrent When Done");
				dest.setString("torrent_path", "Move Torrent When Done Directory");
			}
	
			TransferSpecification trans = new TransferSpecification();

			return new MovementInformation(source, dest, trans, "Tag Move on Completion");
		}
		
		return def_mi;
    }
    
    private interface ContextDescriptor {
    	String getContext();
    }
    
    private static String normaliseRelativePathPart(String name) {
    	name = name.trim();
    	if (name.length() == 0) {return "";}
    	if (name.equals(".") || name.equals("..")) {
    		return null;
    	}
    	return FileUtil.convertOSSpecificChars(name, false).trim();
    }
    
    public static File normaliseRelativePath(File path) {
    	if (path.isAbsolute()) {return null;}
    	
    	File parent = path.getParentFile();
    	String child_name = normaliseRelativePathPart(path.getName());
    	if (child_name == null) {
    		return null;
    	}
    	
    	//  Simple one-level path.
    	if (parent == null) {
    		return new File(child_name);
    	}
    	
    	ArrayList parts = new ArrayList();
    	parts.add(child_name);
    	
    	String filepart = null;
    	while (parent != null) {
    		filepart = normaliseRelativePathPart(parent.getName());
    		if (filepart == null) {return null;}
    		else if (filepart.length()==0) {/* continue */}
    		else {parts.add(0, filepart);} 
    		parent = parent.getParentFile();
    	}
    	
    	StringBuilder sb = new StringBuilder((String)parts.get(0));
    	for (int i=1; i<parts.size(); i++) {
    		sb.append(File.separatorChar);
    		sb.append(parts.get(i));
    	}
    	
    	return new File(sb.toString());
    }
    	
    private static File[] getDefaultDirs() {
		List results = new ArrayList();
		File location = null;
		TargetSpecification ts = null;
        for (TargetSpecification defaultDir : DEFAULT_DIRS) {
            ts = defaultDir;
            File[] targets = ts.getTargets(null, ts);
            if (targets[0] != null) {
                results.add(targets[0]);
            }
        }
		return (File[])results.toArray(new File[0]);
	}



    /**
     * This does the guts of determining appropriate file paths.
     */
    private static SaveLocationChange determinePaths(DownloadManager dm, MovementInformation mi, boolean check_source, boolean is_test) {
		boolean proceed = !check_source || mi.source.matchesDownload(dm, mi, is_test );
		if (!proceed) {
			logInfo("Cannot consider " + describe(dm, mi) +
			    " - does not match source criteria.", dm);
			return null;
		}

		File[] target_paths = mi.target.getTargets(dm, mi);
		if (target_paths[0] == null && target_paths[1] == null) {
			logInfo("Unable to determine an appropriate target for " +
			    describe(dm, mi) + ".", dm);
			return null;
		}

        logInfo("Determined path for " + describe(dm, mi) + ".", dm);
		return mi.transfer.getTransferDetails(dm, mi, target_paths);
	}

	static boolean isInDefaultDownloadDir(DownloadManager dm) {
		// We don't create this object properly, but just enough to get it
		// to be usable.
		SourceSpecification source = new SourceSpecification();
		source.setBoolean("default subdir", SUBDIR_PARAM);
		return source.checkDefaultDir(dm.getSaveLocation().getParentFile(), getDefaultDirs());
	}
    
    private static class MovementInformation implements ContextDescriptor {
        final SourceSpecification source;
        final TargetSpecification target;
        final TransferSpecification transfer;
        final String title;

        MovementInformation(SourceSpecification source, TargetSpecification target,
            TransferSpecification transfer, String title) {
            this.source = source;
            this.target = target;
            this.transfer = transfer;
            this.title = title;
        }
        
        public String getContext() {return title;}
    }

    private abstract static class ParameterHelper implements ContextDescriptor {
		private final Map settings = new HashMap();
		private String context = null;

		protected boolean getBoolean(String key, boolean def) {
			Object result = this.settings.get(key);
			if (result == null) {return( def );}
			if (result instanceof Boolean) {return (Boolean) result;}
            return COConfigurationManager.getBooleanParameter((String)result);
        }

        protected void setBoolean(String key, boolean value) {
        	settings.put(key, value);
        }

        protected void setBoolean(String key, String param) {
        	settings.put(key, param);
        }

        protected void setString(String key, String param) {
        	settings.put(key, param);
        }
        
        protected String getStringRaw(String key) {
			return((String)this.settings.get(key));
        }
        
        protected String getString(String key, String def) {
			String result = (String)this.settings.get(key);
			if (result == null) { return( def );}
			
        	return COConfigurationManager.getStringParameter(result);
        }
        
        public Map getSettings() {return this.settings;}
        public void updateSettings(Map settings) {this.settings.putAll(settings);}
        
        public String getContext() {return this.context;}
        public void setContext(String context) {this.context = context;}
    }

    private static class SourceSpecification extends ParameterHelper {

		public boolean matchesDownload(DownloadManager dm, ContextDescriptor context, boolean ignore_completeness ) {
			if (this.getBoolean("default dir", false)) {
				logInfo("Checking if " + describe(dm, context) + " is inside default dirs.", dm);
				File[] default_dirs = getDefaultDirs();
				File current_location = dm.getSaveLocation().getParentFile();
				
				/**
                 * Very very rare, but I have seen this on fscked up downloads which don't appear
                 * to have a blank / malformed download path.
				 */ 
				if (current_location == null) {
					logWarn(describe(dm, context) + " appears to have a malformed save directory, skipping.", dm);
					return false;
				}
				
				if (!this.checkDefaultDir(current_location, default_dirs)) {
					logWarn(describe(dm, context) +
					    " doesn't exist in any of the following default directories" +
					    " (current dir: " + current_location + ", subdirectories checked: " +
					    this.getBoolean("default subdir", false) + ") - " + Arrays.asList(default_dirs), dm);
					return false;
				}
				logInfo(describe(dm, context) + " does exist inside default dirs.", dm);
			}

			// Does it work for incomplete downloads?
  			if (!dm.isDownloadComplete(false)) {
  				boolean can_move = ignore_completeness || this.getBoolean("incomplete dl", false);
  				String log_message = describe(dm, context) + " is incomplete which is " +
  			    	((can_move) ? "" : "not ") + "an appropriate state.";
  				if (!can_move) {
  					logInfo(log_message, dm);
  					return false;
  				}
  			}
  			
			return true;
		}
		
		public boolean checkDefaultDir(File location, File[] default_dirs) {
			location = FileUtil.canonise(location);
			boolean subdir = this.getBoolean("default subdir", false);
            for (File default_dir : default_dirs) {
                if (subdir) {
                    if (FileUtil.isAncestorOf(default_dir, location)) {
                        return true;
                    }
                } else {
                    if (default_dir.equals(location)) {
                        return true;
                    }
                }
            }
			return false;
		}
		
    }
			
	private static class TargetSpecification extends ParameterHelper {

		public File[] getTargets(DownloadManager dm, ContextDescriptor cd) {
			
			File	data_target;
			File	torrent_target;

			boolean	data_enabled = this.getBoolean("enabled", false);
			
			if ( !data_enabled ){
				
				logInfo("Data target for " + describe(dm, cd) + " is not enabled.", dm);
			}
			
			//logInfo("Calculating target location for " + describe(dm, cd), lr);

				// need the following logic to default the torrent location to the data location
			
			String location = getStringRaw( "target_raw" );
			if ( location == null ){
				location = this.getString("target", null );
				if ( location != null ){
					location = location.trim();
				}
			}
			if ( location == null || location.length() == 0) {
				logInfo("No explicit data target for " + describe(dm, cd) + ".", dm);
				data_target = null;
			}else{

				data_target = new File(FileUtil.getCanonicalFileName(location));
				String relative_path = null;
	
				if( dm != null && dm.getDownloadState() != null ) {
					relative_path = dm.getDownloadState().getRelativeSavePath();
				}
	
				if (relative_path != null && relative_path.length() > 0) {
					logInfo("Consider relative save path: " + relative_path, dm);
	
					// Doesn't matter if File.separator is required or not, it seems to
					// remove duplicate file separators.
					data_target = new File(data_target.getPath() + File.separator + relative_path);
				}
			}
						
			boolean	torrent_enabled = this.getBoolean("torrent", false);
				
			if ( !torrent_enabled ){
				
				logInfo("Torrent target for " + describe(dm, cd) + " is not enabled.", dm);
				
				torrent_target = null;
				
			}else{
							
				torrent_target = data_target;
									
					// update if needed
				
				String torrent_path = this.getStringRaw( "torrent_path_raw" );
				
				if ( torrent_path == null ){
					
					torrent_path = this.getString( "torrent_path", null );
				}
				
				if ( torrent_path != null && torrent_path.trim().length() > 0 ){
					
					File temp = new File( torrent_path );
					
					if ( temp.isDirectory()){
					
						torrent_target = temp;
						
					}else if ( !temp.exists()){
						
						if ( temp.mkdirs()){
							
							torrent_target = temp;
						}
					}
				}
			}
			
			return( new File[]{ data_enabled?data_target:null, torrent_enabled?torrent_target:null });
		}

	}

	private static class TransferSpecification{

		public SaveLocationChange getTransferDetails(DownloadManager dm, 
				ContextDescriptor cd, File[] target_paths ){
			
			SaveLocationChange result = new SaveLocationChange();
			
			File	data_target		= target_paths[0];
			File	torrent_target	= target_paths[1];
			
			if ( data_target != null ){
			
				result.download_location = data_target;
			}
			
			if ( torrent_target != null ){
				
				result.torrent_location = torrent_target;
			}
			
			return result;
		}
	}
	
	static String describe(DownloadManager dm, ContextDescriptor cs) {
		if (cs == null) {return describe(dm);}
		if (dm == null) {
			return "\"" + cs.getContext() + "\"";
		}
		return "\"" + dm.getDisplayName() + "\" with regard to \"" + cs.getContext() + "\"";
	}

}