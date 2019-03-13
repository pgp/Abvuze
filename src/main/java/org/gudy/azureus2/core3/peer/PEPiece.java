/*
 * File    : PEPiece
 * Created : 15-Oct-2003
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

package org.gudy.azureus2.core3.peer;

import org.gudy.azureus2.core3.disk.DiskManagerPiece;


/**
 * Represents a Peer Piece and the status of its different blocks (un-requested, requested, downloaded, written).
 * 
 * @author Olivier
 * @author MjrTom
 *			2005/Oct/08: various changes to support new piece-picking
 *			2006/Jan/2: refactoring, mostly to base Piece interface
 */

public interface 
PEPiece
{  
	PEPeerManager	getManager();
    DiskManagerPiece getDMPiece();
    int         		getPieceNumber();
	int				getLength();
	int				getNbBlocks();
    /**
     * @param offset int bytes into piece 
     * @return block int number corresponding to given offset
     */
    int          getBlockNumber(int offset);
	int			getBlockSize(int block_index);

    /** The time the pePiece was [re]created
     */
    long         getCreationTime();

    /** How many ms since a write to the piece, or since the piece
     * was created if no last write time is known.
     * The return value will be 0 when there's no writes and the piece is new.
     * @return long
     */
    long         getTimeSinceLastActivity();

    long         getLastDownloadTime(long now);
    
	/**
	 * record details of a piece's blocks that have been completed for bad peer detection purposes
	 * @param blockNumber
	 * @param sender
	 * @param hash
	 * @param correct
	 */
    void
	addWrite(
            int blockNumber,
            String sender,
            byte[] hash,
            boolean correct);

	int			getNbWritten();

	int			getAvailability();

	boolean		hasUnrequestedBlock();
	int[]		getAndMarkBlocks(PEPeer peer, int nbWanted, int[] request_hint, boolean reverse_order);
	
	void 		getAndMarkBlock(PEPeer peer, int index);
	Object		getRealTimeData();
	void			setRealTimeData(Object o);
	
	boolean		setRequested(PEPeer peer, int blockNumber);
	void			clearRequested(int blocNumber);
    boolean      isRequested(int blockNumber);
    
    boolean      isRequested();
    void			setRequested();
    boolean		isRequestable();
    
	int			getNbRequests();
	int			getNbUnrequested();
//	public int			checkRequests();

	boolean		isDownloaded(int blockNumber);
    void         setDownloaded(int offset);
    void         clearDownloaded(int offset);
	boolean		isDownloaded();
	boolean[]	getDownloaded();
	boolean		hasUndownloadedBlock();

	//A Piece can be reserved by a peer, so that only s/he can
	//contribute to it.
    String		getReservedBy();
	void			setReservedBy(String peer);

	/**
	 * @return int ResumePriority (startPriority + resuming adjustments)
	 */
    int			getResumePriority();
	/**
	 * @param p the Resume Priority to set, for display purposes
	 */
    void			setResumePriority(int p);

	String[] 	getWriters();
	void			setWritten(String peer, int blockNumber);
	boolean 		isWritten();
	boolean 		isWritten(int blockNumber);
	
	int 			getSpeed();
	void			setSpeed(int speed);

	void
	setLastRequestedPeerSpeed(
            int speed);
	
	void			reset();
	
	String
	getString();
}