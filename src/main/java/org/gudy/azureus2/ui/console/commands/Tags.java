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

package org.gudy.azureus2.ui.console.commands;

import java.io.PrintStream;
import java.util.*;

import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.ui.console.ConsoleInput;

import com.aelitis.azureus.core.tag.*;


public class Tags extends IConsoleCommand {

	private List<Tag> 				current_tags;
	private Tag						current_tag;
	
	public Tags()
	{
		super("tag", "tag");
	}
	
	public String getCommandDescriptions()
	{
		return("tags\t\tAccess to tags.");
	}
	
	public void printHelpExtra(PrintStream out, List<String> args) {
		out.println("> -----");
		out.println("Subcommands:");
		out.println("\tlist\t: List tags");
		out.println("\tcreate <name>\t: Create a new tag");
		out.println("\tselect <number>\t: Select tag <number> for further operations");
		out.println("The following commands operate on a selected tag" );
		out.println("\ttorrents\t: List the tag's torrents" );
		out.println("\tshow\t: Show tag properties" );
		out.println("\tset_rssenable [yes|no]\t: Enable/disable RSS feed generation for the tag" );
		out.println("\tdelete\t: Delete the tag" );
		
		out.println("> -----");
	}
	
	public void 
	execute(
		String commandName, ConsoleInput ci, List<String> args) {
				
		if( args.isEmpty()){
			
			printHelp(ci.out, args);
			
			return;
		}
					
		String cmd = args.get(0);
			
		TagManager tm = TagManagerFactory.getTagManager();
		
		TagType tt = tm.getTagType( TagType.TT_DOWNLOAD_MANUAL );

        switch (cmd) {
            case "list":

                ci.out.println("> -----");

                current_tags = tt.getTags();

                int index = 1;

                for (Tag tag : current_tags) {

                    String index_str = "" + index++;

                    while (index_str.length() < 3) {
                        index_str += " ";
                    }

                    String str = index_str + tag.getTagName(true) + ", downloads=" + tag.getTaggedCount();

                    ci.out.println(str);
                }

                if (current_tags.size() == 0) {

                    ci.out.println("No Tags");
                }
                break;
            case "create":

                if (args.size() < 2) {

                    ci.out.println("Usage: tag create <name>");

                } else {

                    String tag_name = args.get(1);

                    if (tt.getTag(tag_name, true) != null) {

                        ci.out.println("Tag already exists");

                    } else {
                        try {
                            tt.createTag(tag_name, true);

                            ci.out.println("Tag created");

                        } catch (Throwable e) {

                            ci.out.println("Failed to create tag: " + Debug.getNestedExceptionMessage(e));
                        }
                    }
                }
                break;
            case "select":

                if (args.size() < 2) {

                    ci.out.println("Usage: tag select <number>");

                } else {

                    try {
                        index = Integer.parseInt(args.get(1));

                        if (current_tags == null) {

                            throw (new Exception("tags must be listed prior to being selected"));

                        } else if (current_tags.size() == 0) {

                            throw (new Exception("no tags exist"));

                        } else if (index < 0 || index > current_tags.size()) {

                            throw (new Exception("tag index '" + index + "' is out of range"));

                        } else {
                            current_tag = current_tags.get(index - 1);

                            ci.out.println("Selected tag '" + current_tag.getTagName(true) + "'");
                        }
                    } catch (Throwable e) {

                        ci.out.println("Failed to select tag: " + Debug.getNestedExceptionMessage(e));

                    }
                }

                break;
            case "torrents":
            case "show":
            case "delete":
            case "set_rssenable":

                if (current_tag == null) {

                    ci.out.println("No current tag - select one!");

                } else {

                    switch (cmd) {
                        case "torrents":

                            ci.out.println("Torrents for tag '" + current_tag.getTagName(true) + "'");

                            List<DownloadManager> downloads = new ArrayList<>((java.util.Set<DownloadManager>) (Object) current_tag.getTagged());

                            Collections.sort(downloads, new TorrentComparator());

                            for (DownloadManager dm : downloads) {

                                ci.out.println(getTorrentSummary(dm));
                            }

                            break;
                        case "show":

                            ci.out.println("Details for tag '" + current_tag.getTagName(true) + "'");

                            ci.out.println("\tRSS Enable: " + ((TagFeatureRSSFeed) current_tag).isTagRSSFeedEnabled());

                            break;
                        case "set_rssenable":

                            if (args.size() < 2) {

                                ci.out.println("Usage: " + cmd + " [yes|no]");

                            } else {

                                String temp = args.get(1);

                                if (temp.equals("yes") || temp.equals("no")) {

                                    ((TagFeatureRSSFeed) current_tag).setTagRSSFeedEnabled(temp.equals("yes"));

                                } else {

                                    ci.out.println("Usage: " + cmd + " [yes|no]");
                                }
                            }
                            break;
                        case "delete":

                            current_tag.removeTag();

                            current_tag = null;
                            current_tags = null;
                            break;
                    }
                }
                break;
            default:

                ci.out.println("Unsupported sub-command: " + cmd);
                break;
        }
		
		ci.out.println("> -----");
	}
}
