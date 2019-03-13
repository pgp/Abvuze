/*
 * File    : TrackersUtil.java
 * Created : 7 nov. 2003 12:09:56
 * By      : Olivier 
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
 
package org.gudy.azureus2.core3.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Olivier
 * 
 */
public class TrackersUtil {
  
  private List<String> trackers;
  private Map<String,List<List<String>>> multiTrackers; 
  private Map<String,Map> webseeds;
  
  private static TrackersUtil 	instance;
  private static final AEMonitor		class_mon 	= new AEMonitor( "TrackersUtil:class" );

  
  
  private TrackersUtil() {
    trackers = new ArrayList<>();
    multiTrackers = new HashMap<>();
    webseeds = new HashMap<>();
    loadList();
  }
  
  
  public static TrackersUtil getInstance() {
  	try{
  		class_mon.enter();
  	
  		if(instance == null)
  			instance = new TrackersUtil();
  		return instance;
  		
  	}finally{
  		
  		class_mon.exit();
  	}
  }
  
  public List<String> getTrackersList() {
    if(trackers != null)
      return new ArrayList<>(trackers);
    else
      return null;
  }
    
  public void addTracker(String trackerAnnounceUrl) {
    if(trackers.contains(trackerAnnounceUrl))
      return;
    trackers.add(0,trackerAnnounceUrl);
    saveList();
  }
  
  public void addMultiTracker(String configName, List<List<String>> groups) {
    multiTrackers.put(configName,groups);
    saveList();
  }
  
  public void removeMultiTracker(String configName) {
    multiTrackers.remove(configName);
    saveList();
  }
  
  public Map<String,List<List<String>>> getMultiTrackers() {
    return new HashMap<>(multiTrackers);
  }
  public void addWebSeed(String configName, Map ws) {
	  webseeds.put(configName,ws);
	  saveList();
  }

  public void removeWebSeed(String configName) {
	  webseeds.remove(configName);
	  saveList();
  }

  public Map<String,Map> getWebSeeds() {
	  return new HashMap<>(webseeds);
  }

  public void clearAllTrackers(boolean save) {
	  trackers = new ArrayList<>();
	  multiTrackers = new HashMap<>();
	  webseeds = new HashMap<>();
	  if (save) {saveList();}
  }
  
  private void loadList() {    
    File fTrackers = FileUtil.getUserFile("trackers.config");
    if(fTrackers.exists() && fTrackers.isFile()) {
        try (FileInputStream fin = new FileInputStream(fTrackers); BufferedInputStream bin = new BufferedInputStream(fin, 8192)) {
            Map map = BDecoder.decode(bin);
            List list = (List) map.get("trackers");
            if (list != null) {
                for (Object o : list) {
                    String tracker = new String((byte[]) o);
                    trackers.add(tracker);
                }
            }
            Map mapMT = (Map) map.get("multi-trackers");
            if (mapMT != null) {
                for (Object o : mapMT.keySet()) {
                    String configName = (String) o;
                    List groups = (List) mapMT.get(configName);
                    List resGroups = new ArrayList(groups.size());
                    for (Object group : groups) {
                        List theseTrackers = (List) group;
                        List resTrackers = new ArrayList(theseTrackers.size());
                        for (Object theseTracker : theseTrackers) {
                            String tracker = new String((byte[]) theseTracker);
                            resTrackers.add(tracker);
                        }
                        resGroups.add(resTrackers);
                    }
                    this.multiTrackers.put(configName, resGroups);
                }
            }
            webseeds = (Map) map.get("webseeds");

            if (webseeds == null) {
                webseeds = new HashMap();
            } else {
                BDecoder.decodeStrings(webseeds);
            }
        } catch (Exception e) {

            Debug.printStackTrace(e);

        }
    }
  }
  
  private void saveList() {
    Map map = new HashMap();
    map.put("trackers",trackers);
    map.put("multi-trackers",multiTrackers);
    map.put("webseeds",webseeds );
    FileOutputStream fos = null;
    try {
      //  Open the file
      File fTrackers = FileUtil.getUserFile("trackers.config");
      fos = new FileOutputStream(fTrackers);
      fos.write(BEncoder.encode(map));
      fos.close();     
    } catch (Exception e) {
    	Debug.printStackTrace( e );
    } finally{
		if ( fos != null ){
			try{
				fos.close();
			}catch( Throwable e ){
			}
		}   	
    }
  }

  
  

}
