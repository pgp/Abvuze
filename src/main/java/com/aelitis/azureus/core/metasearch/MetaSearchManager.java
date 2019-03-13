/*
 * Created on May 6, 2008
 * Created by Paul Gardner
 * 
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
 */


package com.aelitis.azureus.core.metasearch;

import java.util.Map;

import org.gudy.azureus2.plugins.utils.search.SearchProvider;

import com.aelitis.azureus.core.vuzefile.VuzeFile;

public interface 
MetaSearchManager 
{
	MetaSearch
	getMetaSearch();
	
	boolean
	isAutoMode();
			
	void
	setSelectedEngines(
            long[] ids,
            boolean auto)
	
		throws MetaSearchException;
	
	Engine
	addEngine(
            long id,
            int type,
            String name,
            String json_value)
	
		throws MetaSearchException;
	
	boolean
	isImportable(
            VuzeFile vf);
	
	Engine
	importEngine(
            Map map,
            boolean warn_user)
	
		throws MetaSearchException;
	
	Engine
	getEngine(
            SearchProvider sp);
	
	boolean
	getProxyRequestsEnabled();
	
	void
	setProxyRequestsEnabled(
            boolean enabled);
	
	void
	addListener(
            MetaSearchManagerListener listener);
	
	void
	removeListener(
            MetaSearchManagerListener listener);
	
	void
	log(
            String str);
}
