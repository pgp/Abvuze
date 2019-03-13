/*
 * Created on 08-Oct-2004
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

package org.gudy.azureus2.core3.disk;


/**
 * Represents a DiskManager Piece
 *
 * @author parg
 * @author MjrTom
 *			2005/Oct/08: priority handling
 *			2006/Jan/2: refactoring, mostly to base Piece interface
 */

public interface 
DiskManagerPiece
{
	DiskManager	getManager();

	int			getLength();
	int         	getPieceNumber();
	int			getNbBlocks();
	int			getBlockSize(int block_index);

	short		getReadCount();
	void			setReadCount(short c);

	boolean		calcNeeded();
	void			clearNeeded();
	
	/** @return true if any file the piece covers is neither Do Not Download nor Delete.
	 * This is not a real-time indicator.  Also, the results are not reliable for pieces that are Done.
	 * Use calcNeeded() for guaranteed correct and up to date results
	 * @see calcNeeded(), clearNeeded(), setNeeded(), setNeeded(boolean)
	 */
    boolean		isNeeded();
	void			setNeeded();
	void			setNeeded(boolean b);

	// a piece is Written if data has been written to storage for every block (without concern for if it's checked)  
    boolean      isWritten();
	int			getNbWritten();
	boolean[] 	getWritten();
	
	/**
	 * @param blockNumber int
	 * @return true if the given blockNumber has already been written to disk
	 */
    boolean		isWritten(int blockNumber);
	void			setWritten(int blockNumber);

	// a piece is Checking if a hash check has been setup and the hash check hasn't finalized the result yet
	// this flag is asynch, so be careful, and it's also transitory (comapared to most of the others being kinda sticky)

	void			setChecking();
    boolean 		isChecking();
    
    boolean		isNeedsCheck();

    boolean		spansFiles();
    
	boolean		calcDone();
	/** @return true when the hash check has passed and the DiskManager has asyncronously updated the Done status.
	 * There is nothing further to be done regarding downloading for pieces that are Done.
	 */
    boolean		isDone();
	void			setDone(boolean b);

    /**
     * @return true if a piece is Needed and not Done
     */
    boolean      isInteresting();

    /** This must not be used to qualify pieces in End Game Mode.
	 * @return true if a piece is Needed but is not fully; Requested, Downloaded, Written, Checking, or Done.
	 */
    boolean		isDownloadable();
	void 		setDownloadable();
	    
     /**
     * returns true if all the files that the piece spans are skipped
     * @return
     */
     boolean	    isSkipped();
    
    void 		reDownloadBlock(int blockNumber);
    void			reset();
    
    String
    getString();
}
