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

import java.io.*;
import java.util.Map;

import com.aelitis.azureus.core.subs.Subscription;
import com.aelitis.azureus.core.vuzefile.VuzeFile;


public interface 
Engine 
{	
	/**
	 * AZ_VERSION:
	 * 1: Original
	 * 2: field value substitution in JSON engine type using ${FIELD_ID}
	 * 3: field value substitution in REGEX engine type using ${FIELD_NO}
	 * 4: JSON engine now supports [x] in Entry Path, where x is the array index
	 * 5: JSON and REGEX support functions in ${FIELD_ID} or ${FIELD_NO}, such as
	 *      ${1,ucase}
	 *      ${1,lcase}
	 *      ${1,urldecode}
	 *      ${1,replace,fromRegex,toText}
	 *      ${1,lcase+urldecode}
	 */
    int	AZ_VERSION		= 5;
	
	Object	VUZE_FILE_COMPONENT_ENGINE_KEY = new Object();
	
		// Don't change these values as they get persisted
	
	int FIELD_NAME 			= 1;
	int FIELD_DATE 			= 2;
	int FIELD_SIZE 			= 3;
	int FIELD_PEERS 		= 4;
	int FIELD_SEEDS 		= 5;
	int FIELD_CATEGORY 		= 6;
	int FIELD_COMMENTS 		= 7;
	int FIELD_CONTENT_TYPE 	= 8;
	int FIELD_DISCARD	 	= 9;
	int FIELD_VOTES		 	= 10;
	int FIELD_SUPERSEEDS 	= 11;
	int FIELD_PRIVATE	 	= 12;
	int FIELD_DRMKEY        = 13;
	int FIELD_VOTES_DOWN    = 14;
	
	
	int FIELD_TORRENTLINK 	= 102;
	int FIELD_CDPLINK 		= 103;
	int FIELD_PLAYLINK 		= 104;
	int FIELD_DOWNLOADBTNLINK 		= 105;
	
	int FIELD_HASH			= 200;
	int FIELD_RANK    = 201;
	
	int[] FIELD_IDS = {
		FIELD_NAME, FIELD_DATE, FIELD_SIZE, FIELD_PEERS, FIELD_SEEDS, FIELD_CATEGORY,
		FIELD_COMMENTS, FIELD_CONTENT_TYPE, FIELD_DISCARD, 
		FIELD_TORRENTLINK, FIELD_CDPLINK, FIELD_PLAYLINK,FIELD_DOWNLOADBTNLINK, FIELD_VOTES, FIELD_SUPERSEEDS,
		FIELD_PRIVATE, FIELD_DRMKEY, FIELD_VOTES_DOWN, FIELD_HASH, FIELD_RANK
	};
		
	String[] FIELD_NAMES = {
		"TITLE", "DATE", "SIZE", "PEERS", "SEEDS", "CAT",
		"COMMENTS", "CONTENT_TYPE", "DISCARD",
		"TORRENT", "CDP", "PLAY","DLBTN", "VOTES", "XSEEDS",
		"PRIVATE", "DRMKEY", "VOTESDOWN", "HASH", "RANK"
	};
	
	int ENGINE_TYPE_REGEX		= 1;
	int ENGINE_TYPE_JSON		= 2;
	int ENGINE_TYPE_PLUGIN		= 3;
	int ENGINE_TYPE_RSS			= 4;
	
	
	int	ENGINE_SOURCE_UNKNOWN				= 0;
	int	ENGINE_SOURCE_VUZE					= 1;
	int	ENGINE_SOURCE_LOCAL					= 2;
	int	ENGINE_SOURCE_RSS					= 3;
	
	int	SEL_STATE_DESELECTED			= 0;
	int	SEL_STATE_AUTO_SELECTED			= 1;
	int	SEL_STATE_MANUAL_SELECTED		= 2;
	int	SEL_STATE_FORCE_DESELECTED		= 3;

	int AUTO_DL_SUPPORTED_UNKNOWN		= 0;
	int AUTO_DL_SUPPORTED_YES			= 1;
	int AUTO_DL_SUPPORTED_NO			= 2;
	
		/**
		 * don't change these as they are externalised
		 */
        String[] ENGINE_SOURCE_STRS = { "unknown","vuze","local","rss","unused" };
	String[] SEL_STATE_STRINGS	= { "no", "auto", "manual", "force_no" };
	String[] ENGINE_TYPE_STRS 	= { "unknown","regexp","json", "plugin" };
	
	String	SC_SOURCE			= "azsrc";
	String	SC_AZID				= "azid";
	String	SC_FORCE_FULL		= "force_full";	// ignore if-modified stuff and force a full search
	String	SC_BATCH_PERIOD		= "batch_millis";
	String	SC_REMOVE_DUP_HASH	= "remove_dup_hash";

	
	String	CT_VIDEO	= "video";
	String	CT_AUDIO	= "audio";
	String	CT_GAME		= "game";
	
	int getType();
		
	Result[]
	search(
            SearchParameter[] searchParameters,
            Map context,
            int desired_max_matches,
            int absolute_max_matches,
            String headers,
            ResultListener listener)
	
		throws SearchException;
	
	String
	getName();
	
	String
	getNameEx();
	
	long
	getId();
	
	String
	getUID();
	
	int
	getVersion();
	            
	long
	getLastUpdated();
	
	String
	getIcon();

	String
	getDownloadLinkCSS();
	
	boolean
	isActive();
	
	boolean
	isMine();
	
	boolean
	isPublic();
	
	void
	setMine(
            boolean mine);
	
	int
	getSelectionState();
	
	void
	setSelectionState(
            int state);
	
	void
	recordSelectionState();
	
	void
	checkSelectionStateRecorded();
		
	int
	getSource();
	
	void
	setSource(
            int source);
	
	String
	getReferer();
	
	float
	getRankBias();
	
	void
	setRankBias(
            float bias);
	
	void
	setPreferredDelta(
            float delta);
	
	float
	getPreferredWeighting();
	
	float
	applyRankBias(
            float rank);
	
	boolean
	supportsField(
            int field_id);
	
	boolean
	supportsContext(
            String context_key);
	
	boolean
	isShareable();
	
	boolean
	isAnonymous();
	
	boolean
	isAuthenticated();
	
		/**
		 * @return one of AUTO_DL constants above
		 */

        int
	getAutoDownloadSupported();
	
	int
	getAZVersion();
	
	void
	addPotentialAssociation(
            String key);
	
	Subscription
	getSubscription();
	
	Map<String,Object>
	exportToBencodedMap() 
	
		throws IOException;
	
	Map<String,Object>
	exportToBencodedMap(
            boolean generic)
	
		throws IOException;

	String
	exportToJSONString()
	
		throws IOException;
	
	void
	exportToVuzeFile(
            File target)
	
		throws IOException;
	
	VuzeFile
	exportToVuzeFile()
	
		throws IOException;
	
		/**
		 * Tests for sameness in terms of function (ignores id, selection state etc)
		 * @param other
		 * @return
		 */

        boolean
	sameLogicAs(
                Engine other);
	
		/**
		 * resets to initial state (e.g. if the engine has state pertaining to what has/hasn't been downloaded
		 * such as etags then this will be cleared)
		 */

        void
	reset();
	
	void
	delete();
	
	String
	getString();
}
