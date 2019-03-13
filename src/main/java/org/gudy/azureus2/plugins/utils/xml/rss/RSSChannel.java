/*
 * Created on 02-Jan-2005
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

package org.gudy.azureus2.plugins.utils.xml.rss;

import java.net.URL;
import java.util.Date;

import org.gudy.azureus2.plugins.utils.xml.simpleparser.SimpleXMLParserDocumentNode;

/**
 * @author parg
 *
 */

public interface 
RSSChannel 
{
		/**
		 * Returns the mandatory title
		 * @return
		 */

        String
	getTitle();
	
		/**
		 * Returns the mandatory description
		 * @return
		 */

        String
	getDescription();
	
		/**
		 * Returns the mandatory link
		 * @return
		 */

        URL
	getLink();
	
		/**
		 * Returns the *optional* publication date
		 * @return
		 */

        Date
	getPublicationDate();
	
		/**
		 * returns the items in the channel
		 * @return
		 */

        RSSItem[]
	getItems();
	
		/**
		 * returns the underlying XML node for extraction of extensions
		 * @return
		 */

        SimpleXMLParserDocumentNode
	getNode();
}
