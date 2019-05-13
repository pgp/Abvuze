package com.aelitis.azureus.core.subs.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;

import com.aelitis.azureus.core.metasearch.Result;
import com.aelitis.azureus.core.subs.SubscriptionException;
import com.aelitis.azureus.core.subs.SubscriptionResultFilter;
import com.aelitis.azureus.util.ImportExportUtils;
import com.aelitis.azureus.util.JSONUtils;

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

public class 
SubscriptionResultFilterImpl
	implements SubscriptionResultFilter
{
	private final SubscriptionImpl		subs;
	
	private String[] 	textFilters;
	private Pattern[]	textFilterPatterns;
	
	private String[] 	excludeTextFilters;
	private Pattern[]	excludeTextFilterPatterns;
	
	private String regexFilter;	// unused
	
	private long minSeeds = -1;
	private long minSize = -1;
	private long maxSize = -1;
	private String categoryFilter = null;
	
	public SubscriptionResultFilterImpl(SubscriptionImpl	_subs, Map filters) {
		subs	= _subs;
		
		try {
			textFilters = importStrings(filters,"text_filter"," ");
			
			textFilterPatterns = getPatterns( textFilters );
			
			excludeTextFilters = importStrings(filters,"text_filter_out"," ");
			
			excludeTextFilterPatterns = getPatterns( excludeTextFilters );

			
			regexFilter = ImportExportUtils.importString(filters, "text_filter_regex");
			
			minSize = ImportExportUtils.importLong(filters,"min_size",-1L);
			
			maxSize = ImportExportUtils.importLong(filters,"max_size",-1L);
			
			minSeeds = ImportExportUtils.importLong(filters, "min_seeds",-1L);
			
			String rawCategory = ImportExportUtils.importString(filters,"category");
			if(rawCategory != null) {
				categoryFilter = rawCategory.toLowerCase();
			}
			
		} catch(Exception e) {
			//Invalid filters array
		}
	}

	
	public long
	getMinSze()
	{
		return( minSize );
	}
	
	public long
	getMaxSize()
	{
		return( maxSize );
	}
	
	public String[]
	getWithWords()
	{
		return( textFilters );
	}
	
	public String[]
	getWithoutWords()
	{
		return( excludeTextFilters );
	}
	
	@Override
	public void 
	update(
		String[] with_words, 
		String[] without_words,
		long min_size, long max_size )
		
		throws SubscriptionException
	{
		Map map = JSONUtils.decodeJSON( subs.getJSON());

		Map filters = new JSONObject();
		
		map.put( "filters", filters );
		
		exportStrings( filters, "text_filter", with_words );
		exportStrings( filters, "text_filter_out", without_words );
		
		if ( min_size <= 0 ){
			min_size = -1;
		}
		
		if ( max_size <= 0 ){
			max_size = -1;
		}
		
		filters.put( "min_size", min_size );
		filters.put( "max_size", max_size );
		
		subs.setDetails( subs.getName( false ), subs.isPublic(), map.toString());
		
		textFilters	= with_words;
		
		textFilterPatterns = getPatterns( textFilters );

		excludeTextFilters = without_words;
		
		excludeTextFilterPatterns = getPatterns( excludeTextFilters );

		minSize = min_size;
		maxSize = max_size;
	}
	
	public String
	getString()
	{
		String	res = addString( "", "+", getString(textFilters));
	
		res = addString( res, "-", getString(excludeTextFilters));
		
		res = addString( res, "regex=", regexFilter );

		res = addString( res, "cat=", categoryFilter );

		return( res );
	}
	
	private String
	addString(
		String	existing,
		String	key,
		String	rest )
	{
		if ( rest == null || rest.length() == 0 ){
			
			return( existing );
		}
		
		String str = key + rest;
		
		if ( existing == null || existing.length() == 0){
			
			return( str );
		}
		
		return( existing + "," + str );
	}
		
	private String
	getString(
		String[]		strs )
	{
		String	res = "";
		
		for( int i=0;i<strs.length;i++){
			res += (i==0?"":"&") + strs[i]; 
		}
		
		return( res );
	}
	

	private static Pattern[] NO_PATTERNS = {};
	
	private Pattern[]
	getPatterns(
		String[]	strs )
	{
		if ( strs.length == 0 ){
			
			return( NO_PATTERNS );
		}
		
		Pattern[] pats = new Pattern[strs.length];
		
		for (int i=0;i<strs.length;i++){
		
			try{
				pats[i] = Pattern.compile( strs[i].trim());
				
			}catch( Throwable e ){
				
				System.out.println( "Failed to compile pattern '" + strs[i] );
			}
		}		
		
		return( pats );
	}
	
	private String[] importStrings(Map filters,String key,String separator) throws IOException {
		String rawStringFilter = ImportExportUtils.importString(filters,key);
		if(rawStringFilter != null) {
			StringTokenizer st = new StringTokenizer(rawStringFilter,separator);
			String[] stringFilter = new String[st.countTokens()];
			for(int i = 0 ; i < stringFilter.length ; i++) {
				stringFilter[i] = st.nextToken().toLowerCase();
			}
			return stringFilter;
		}
		return new String[0];
	}
	
	private void
	exportStrings(
		Map			map,
		String		key,
		String[]	values )
	{
		if ( values == null || values.length == 0 ){
			
			return;
		}
		
		String encoded = "";
		
		for ( String value: values ){
			
			encoded += ("".equals(encoded) ?"":" ") + value;
		}
		
		map.put( key, encoded );
	}
	
	public Result[] filter(Result[] results) {
		List<Result> filteredResults = new ArrayList<>(results.length);
        for (Result result : results) {
            String name = result.getName();
            //Results need a name, or they are by default invalid
            if (name == null) {
                continue;
            }
            name = name.toLowerCase();

            boolean valid = true;
            for (int j = 0; j < textFilters.length; j++) {

                //If one of the text filters do not match, let's not keep testing the others
                // and mark the result as not valid
                if (!name.contains(textFilters[j])) {

                    // double check against reg-expr if exists

                    Pattern p = textFilterPatterns[j];

                    if (p == null || !p.matcher(name).find()) {

                        valid = false;

                        break;
                    }
                }
            }

            //if invalid after name check, let's get to the next result
            if (!valid) {
                continue;
            }

            for (int j = 0; j < excludeTextFilters.length; j++) {

                //If one of the text filters do not match, let's not keep testing the others
                // and mark the result as not valid
                if (name.contains(excludeTextFilters[j])) {
                    valid = false;
                    break;
                } else {
                    Pattern p = excludeTextFilterPatterns[j];

                    if (p != null && p.matcher(name).find()) {
                        valid = false;
                        break;
                    }
                }
            }

            //if invalid after name check, let's get to the next result
            if (!valid) {
                continue;
            }

            long size = result.getSize();

            if (minSize > -1) {
                if (minSize > size) {
                    continue;
                }
            }

            if (maxSize > -1) {
                if (maxSize < size) {
                    continue;
                }
            }

            if (minSeeds > -1) {
                if (minSeeds < result.getNbSeeds()) {
                    continue;
                }
            }

            if (categoryFilter != null) {
                String category = result.getCategory();
                if (category == null || !category.equalsIgnoreCase(categoryFilter)) {
                    continue;
                }
            }


            //All filters are ok, let's add the results to the filtered results
            filteredResults.add(result);

        }
		
		Result[] fResults = filteredResults.toArray(new Result[0]);
		
		return fResults;
	}
}