/*
 * File    : DiskManagerImpl.java
 * Created : 18-Oct-2003
 * By      : stuff
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
 
package org.gudy.azureus2.core3.disk;
 
 
import java.io.File;

import org.gudy.azureus2.core3.disk.impl.piecemapper.DMPieceList;
import org.gudy.azureus2.core3.disk.impl.piecemapper.DMPieceMap;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.util.DirectByteBuffer;
import org.gudy.azureus2.core3.util.IndentWriter;

/**
* @author MjrTom
*			2005/Oct/08: Priority, getPieces done, etc changes for new piece-picking
*/

public interface
DiskManager
{
	int INITIALIZING = 1;
	int ALLOCATING = 2;
	int CHECKING = 3;
	int READY = 4;
	int FAULTY = 10;
	
	int ET_NONE						= 0;
	int ET_OTHER					= 1;
	int ET_INSUFFICIENT_SPACE		= 2;
	
		// CHANGE THIS AND YOU MUST CHANGE NORMAL_REQUEST_SIZE in PeerReadRequest (plugin interface)
	
	int BLOCK_SIZE_KB 	= 16;
	int BLOCK_SIZE 		= BLOCK_SIZE_KB*1024;

	/**
	 * Start checking/allocating
	 */
    void
	start();
	
		/**
		 * Stop can go async if the download is in a 'starting' state - if so this method returns true
		 * @param closing
		 * @return 
		 */

        boolean
	stop(
                boolean closing);
	
	boolean
	isStopped();
	
	/**
	  * @return whether all files exist and sizes match
	  */

    boolean
	filesExist();

	DirectByteBuffer
	readBlock(
            int pieceNumber,
            int offset,
            int length);
	
	DiskManagerWriteRequest
	createWriteRequest(
            int pieceNumber,
            int offset,
            DirectByteBuffer data,
            Object user_data);

	
		/**
		 * enqueue an async write request
		 * @param pieceNumber
		 * @param offset
		 * @param data
		 * @param user_data	this will be provided to the listener when called back
		 * @param listener
		 */

        void
	enqueueWriteRequest(
                DiskManagerWriteRequest request,
                DiskManagerWriteRequestListener listener);

	boolean
	hasOutstandingWriteRequestForPiece(
            int piece_number);
	
	DiskManagerReadRequest
	createReadRequest(
            int pieceNumber,
            int offset,
            int length);
	
		  /**
		   * Enqueue an async disk read request.
		   * @param request
		   * @param listener
		   */

          void
	enqueueReadRequest(
                  DiskManagerReadRequest request,
                  DiskManagerReadRequestListener listener);

	boolean
	hasOutstandingReadRequestForPiece(
            int piece_number);
	
		/**
		 * Create a request to check a particular piece
		 * @param pieceNumber	-1 for a complete recheck request
		 * @param user_data
		 * @return
		 */

        DiskManagerCheckRequest
	createCheckRequest(
                int pieceNumber,
                Object user_data);
	
		/**
		 * enqueue an asynchronous single piece check
		 * @param pieceNumber
		 * @param listener
		 * @param user_data
		 */

        void
	enqueueCheckRequest(
                DiskManagerCheckRequest request,
                DiskManagerCheckRequestListener listener);
	
	boolean
	hasOutstandingCheckRequestForPiece(
            int piece_number);
	
		/**
		 * recheck the entire torrent asynchronously, reporting each piece to the listener
		 * @param listener
		 * @param user_data
		 */

        void
	enqueueCompleteRecheckRequest(
                DiskManagerCheckRequest request,
                DiskManagerCheckRequestListener listener);
	
	void
	setPieceCheckingEnabled(
            boolean enabled);
			
	void
    saveResumeData(
            boolean interim_save)
		
		throws Exception;

	
	DiskManagerPiece[]
	getPieces();
	
	int
	getNbPieces();

	DiskManagerFileInfo[] getFiles();
	DiskManagerFileInfoSet getFileSet();
	DiskManagerPiece getPiece(int PieceNumber);

		/**
		 * DON'T CACHE the DMPieceMap - as it is designed to be discarded when not in use
		 * @return
		 */

        DMPieceMap  getPieceMap();
	
	DMPieceList getPieceList(int pieceNumber);
	
	int
	getState();
	
	long
	getTotalLength();
	
	int
	getPieceLength();
	
	int
	getPieceLength(
            int piece_number);
	
	/**
	 * Get remaining bytes to completion *including* DND files
	 */
    long
	getRemaining();
	
	long
	getRemainingExcludingDND();
	
	/**
	 * Get the percentage done during checking, allocation or initializing
	 *  
	 * @return percent done * 1000 (1000 = 100%)
	 */
    int
	getPercentDone();
	
	String
	getErrorMessage();
  
	int
	getErrorType();
	
	void
	downloadEnded(
            OperationStatus op_status);

    void
    downloadRemoved();
	
	void
	moveDataFiles(
            File new_parent_dir,
            String dl_name,
            OperationStatus op_status);
	
		/**
		 * returns -1 if no recheck in progress, percentage complete in 1000 notation otherwise
		 * @return
		 */

        int
	getCompleteRecheckStatus();
  
		/**
		 * When a download's data is moving (for completion or removal events) this gives the progress in 1000 notation. -1 if nothing's going on
		 * @return
		 */

        int
	getMoveProgress();
	
		/**
		 * method for checking that the block details are sensible
		 * @param pieceNumber
		 * @param offset
		 * @param data
		 * @return
		 */

        boolean
	checkBlockConsistencyForWrite(
                String originator,
                int pieceNumber,
                int offset,
                DirectByteBuffer data);

		/**
		 * method for checking that the block details are sensible
		 * @param pieceNumber
		 * @param offset
		 * @param length
		 * @return
		 */

        boolean
	checkBlockConsistencyForRead(
                String originator,
                boolean peer_request,
                int pieceNumber,
                int offset,
                int length);
		
	boolean
	checkBlockConsistencyForHint(
            String originator,
            int pieceNumber,
            int offset,
            int length);
	
	TOTorrent
	getTorrent();
	
	File
	getSaveLocation();
	
	void
	addListener(
            DiskManagerListener l);
	
	void
	removeListener(
            DiskManagerListener l);
  
	boolean
	hasListener(
            DiskManagerListener l);
  
  /**
   * Save the individual file priorities map to
   * DownloadManager.getData( "file_priorities" ).
   */

  void
	saveState();

	/**
	 * @param pieceNumber
	 * @return true if the pieceNumber is Needed and not Done
	 */
    boolean isInteresting(int pieceNumber);
	
	boolean isDone(int pieceNumber);

	int getCacheMode();
	
	long[]
	getReadStats();
	
	void
	generateEvidence(
            IndentWriter writer);
	
	interface
	OperationStatus
	{
		void
		gonnaTakeAWhile(
                GettingThere gt);
	}
	
	interface
	GettingThere
	{
		boolean
		hasGotThere();
	}

	long getSizeExcludingDND();

	int getPercentDoneExcludingDND();
	
	long
	getPriorityChangeMarker();
}