/*
 * Created by Joseph Bridgewater
 * Created on Jan 2, 2006
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

package com.aelitis.azureus.core.peermanager.piecepicker;

import java.util.List;

import org.gudy.azureus2.core3.peer.PEPeer;
import org.gudy.azureus2.core3.peer.PEPiece;
import org.gudy.azureus2.core3.util.IndentWriter;


/**
 * @author MjrTom
 *
 */

public interface PiecePicker
{
	int REQUEST_HINT_MAX_LIFE	= 120*1000;
	
    boolean  hasDownloadablePiece();
    /** @return long value indicated serial number of current count of changes
     * to hasNeededUndonePiece.
     * A method interesting in tracking changes can compare this with a locally stored
     * value to determine if the hasNeededUndonePiece status has changed since the last check. 
     */
    long     getNeededUndonePieceChange();
    

    void     addHavePiece(PEPeer peer, int pieceNumber);
    
    /** This is called periodically by the peer control scheduler.
     * It should not normally be called by other methods.
     * It will update the global availability if neccesary
     * and then update the derived information 
     */
    void     updateAvailability();
    int[]    getAvailability();
    int      getAvailability(final int pieceNumber);
    
    float    getMinAvailability();
    int		getMaxAvailability();
    float    getAvgAvail();
	long 	getAvailWentBadTime();
	float    getMinAvailability(int fileIndex);
	   
	long 	getBytesUnavailable();
	
	void		allocateRequests();

	boolean	isInEndGameMode();
	boolean	hasEndGameModeBeenAbandoned();
	void		clearEndGameChunks();
	/** adds all blocks in the piece to endGameModeChunks
	 * @param pePiece
	 */
    void		addEndGameChunks(final PEPiece pePiece);

	void		removeFromEndGameModeChunks(final int pieceNumber, final int offset);
	
	int	getNumberOfPieces();
	
	
	int		getNbPiecesDone();
	
	void
	setForcePiece(
            int pieceNumber,
            boolean forced);
	
	boolean
	isForcePiece(
            int pieceNumber);
	
	void
	setGlobalRequestHint(
            int piece_number,
            int start_bytes,
            int byte_count);
	
	int[]
	getGlobalRequestHint();
	
	void
	setReverseBlockOrder(
            boolean is_reverse);
	
	boolean
	getReverseBlockOrder();
	
	void
	addRTAProvider(
            PieceRTAProvider shaper);
	
	void
	removeRTAProvider(
            PieceRTAProvider shaper);
	
	List
	getRTAProviders();
	
	void
	addPriorityProvider(
            PiecePriorityProvider shaper);
	
	void
	removePriorityProvider(
            PiecePriorityProvider shaper);
	
	List
	getPriorityProviders();
	
	void
	addListener(
            PiecePickerListener listener);
	
	void
	removeListener(
            PiecePickerListener listener);

	void
	destroy();
	
	void
	generateEvidence(
            IndentWriter writer);
	
	String
	getPieceString(
            int piece_number);
}
