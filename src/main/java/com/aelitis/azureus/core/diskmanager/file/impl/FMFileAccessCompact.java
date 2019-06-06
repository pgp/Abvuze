/*
 * Created on 28-Sep-2005
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

package com.aelitis.azureus.core.diskmanager.file.impl;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentFile;
import org.gudy.azureus2.core3.util.DirectByteBuffer;
import org.gudy.azureus2.core3.util.FileUtil;

import com.aelitis.azureus.core.diskmanager.file.FMFileManagerException;

public class 
FMFileAccessCompact
	implements FMFileAccess
{
	private final static byte SS = DirectByteBuffer.SS_FILE;

	private final TOTorrentFile		torrent_file;
	private int					piece_size;
	
	private final File				controlFileDir;
	private final String				controlFileName;
	private final FMFileAccess		delegate;
	private final FMFileAccess.FMFileAccessIO delegateRead, delegateWrite;
	
	private volatile long		current_length;
	private static final long				version				= 0;
	
	private volatile boolean	write_required;
	
	private long	first_piece_start;
	private long	first_piece_length;
	private long	last_piece_start;
	private long	last_piece_length;
	
	protected
	FMFileAccessCompact(
		TOTorrentFile	_torrent_file,
		File			_controlFileDir,
		String			_controlFileName,
		FMFileAccess	_delegate )
	
		throws FMFileManagerException
	{
		torrent_file	= _torrent_file;
		controlFileDir	= _controlFileDir;
		controlFileName = _controlFileName;
		delegate		= _delegate;
		delegateRead = new FMFileAccessIO() {
			@Override
			public void io(RandomAccessFile raf, DirectByteBuffer[] buffers, long offset) throws FMFileManagerException {
				delegate.read(raf,buffers,offset);
			}
		};
		delegateWrite = new FMFileAccessIO() {
			@Override
			public void io(RandomAccessFile raf, DirectByteBuffer[] buffers, long offset) throws FMFileManagerException {
				delegate.write(raf,buffers,offset);
			}
		};

		try{
			piece_size = (int)torrent_file.getTorrent().getPieceLength();
			
			TOTorrent	torrent = torrent_file.getTorrent();
			
			long	file_length	= torrent_file.getLength();
			
			long	file_offset_in_torrent = 0;
			
			for (int i=0;i<torrent.getFiles().length;i++){
				
				TOTorrentFile	f = torrent.getFiles()[i];
				
				if ( f == torrent_file ){
					
					break;
				}
				
				file_offset_in_torrent	+= f.getLength();
			}
			
			int piece_offset	= piece_size - (int)( file_offset_in_torrent % piece_size );
			
			if ( piece_offset == piece_size ){
				
				piece_offset	= 0;
			}
			
			first_piece_length	= piece_offset;
			first_piece_start	= 0;
			
			if ( first_piece_length >= file_length ){
				
					// first piece takes up all the file
				
				first_piece_length 	= file_length;
				last_piece_start	= file_length;
				last_piece_length	= 0;
				
			}else{
			
				last_piece_length	= ( file_length - piece_offset ) % piece_size;
				last_piece_start	= file_length - last_piece_length;
			}
			
			/*
			System.out.println( 
					"file " + new String(torrent_file.getPathComponents()[0]) + ": " +
					"off = " + file_offset_in_torrent + ", len = " + file_length + ", fp = " + first_piece_start + "/" + first_piece_length +
					", lp = " + last_piece_start + "/" + last_piece_length );
			*/
			
			if ( !new File(controlFileDir,controlFileName).exists()){
				
				if (!controlFileDir.isDirectory() && !FileUtil.mkdirs(controlFileDir)) {
					throw new FMFileManagerException("Directory creation failed: "	+ controlFileDir);
				}
				
			}else{
				
				readState();
			}
		}catch( Throwable e ){
			
			throw( new FMFileManagerException( "Compact file init fail", e ));
		}
	}
	
	protected long
	getFirstPieceStart()
	{
		return( first_piece_start );
	}
	
	protected long
	getFirstPieceLength()
	{
		return( first_piece_length );
	}
	
	protected long
	getLastPieceStart()
	{
		return( last_piece_start );
	}
	
	protected long
	getLastPieceLength()
	{
		return( last_piece_length );
	}
	
	public void
	aboutToOpen()
	
		throws FMFileManagerException
	{
		delegate.aboutToOpen();
	}
	
	public long
	getLength(
		RandomAccessFile		raf )
	
		throws FMFileManagerException
	{
		return( current_length );
	}
	
	public void
	setLength(
		RandomAccessFile		raf,
		long					length )
	
		throws FMFileManagerException
	{
		if ( length != current_length ){
					
			current_length	= length;
			
			write_required = true;
		}
	}

	protected void
	io(
			RandomAccessFile	raf,
			DirectByteBuffer	buffer,
			long				position,
			FMFileAccessIO delegateIO)

			throws FMFileManagerException
	{
		int	original_limit	= buffer.limit(SS);

		try{
			int	len = original_limit - buffer.position(SS);

			// System.out.println( "compact: IO - " + position + "/" + len );

			// deal with any IO access to the first piece

			if ( position < first_piece_start + first_piece_length ){

				int	available = (int)( first_piece_start + first_piece_length - position );

				if ( available >= len ){

					// all they require is in the first piece

					// System.out.println( "    all in first piece" );

					delegateIO.io( raf, new DirectByteBuffer[]{ buffer }, position );

					position	+= len;
					len			= 0;
				}else{

					// io goes past end of first piece

					// System.out.println( "    part -- first piece" );

					buffer.limit( SS, buffer.position(SS) + available );

					delegateIO.io( raf, new DirectByteBuffer[]{ buffer }, position );

					buffer.limit( SS, original_limit );

					position	+= available;
					len			-= available;
				}
			}

			if ( len == 0 ){

				return;
			}

			// position is at start of gap between start and end - work out how much,
			// if any, space has been requested

			long	space = last_piece_start - position;

			if ( space > 0 ){

				if ( space >= len ){

					// System.out.println( "    all in space" );

					// all they require is space

					buffer.position( SS, original_limit );

					position	+= len;
					len			= 0;
				}else{

					// io goes past end of space

					// System.out.println( "    part in space" );

					buffer.position( SS, buffer.position(SS) + (int)space );

					position	+= space;
					len			-= space;
				}
			}

			if ( len == 0 ){

				return;
			}

			// lastly IO last piece

			// System.out.println( "    some in last piece" );

			delegateIO.io( raf, new DirectByteBuffer[]{buffer}, ( position - last_piece_start ) + first_piece_length );

		}finally{

			buffer.limit(SS,original_limit);
		}
	}


	protected void read(RandomAccessFile raf, DirectByteBuffer buffer, long position) throws FMFileManagerException {
		io(raf,buffer,position,delegateRead);
	}
	
	public void
	read(
		RandomAccessFile		raf,
		DirectByteBuffer[]		buffers,
		long					position )
	
		throws FMFileManagerException
	{
        for (DirectByteBuffer buffer : buffers) {

            int len = buffer.limit(SS) - buffer.position(SS);

            read(raf, buffer, position);

            int rem = buffer.remaining(SS);

            position += len - rem;

            if (rem > 0) {

                break;
            }
        }
		
		if ( position > current_length ){
			
			setLength( raf, position );
		}
	}
	
	protected void write(RandomAccessFile raf, DirectByteBuffer buffer, long position) throws FMFileManagerException {
		io(raf,buffer,position,delegateWrite);
	}
	
	
	public void
	write(
		RandomAccessFile		raf,
		DirectByteBuffer[]		buffers,
		long					position )
	
		throws FMFileManagerException
	{
        for (DirectByteBuffer buffer : buffers) {

            int len = buffer.limit(SS) - buffer.position(SS);

            write(raf, buffer, position);

            position += len;
        }
		
		if ( position > current_length ){
			
			setLength( raf, position );
		}
	}
	
	public void
	flush()
	
		throws FMFileManagerException
	{
		writeState();
	}
	
	public boolean
	isPieceCompleteProcessingNeeded(
		int					piece_number )
	{
		return( false );
	}
	
	public void
	setPieceComplete(
		RandomAccessFile	raf,
		int					piece_number,
		DirectByteBuffer	piece_data )
	
		throws FMFileManagerException
	{	
	}
	
	protected void
	readState()
	
		throws FMFileManagerException
	{
		try{
			Map	data = 			
				FileUtil.readResilientFile( controlFileDir, controlFileName, false );
			
			if ( data != null && data.size() > 0 ){
				
				Long	version = (Long)data.get( "version" );
				
				Long	length = (Long)data.get( "length" );
				
				current_length	= length;
			}
		}catch( Throwable e ){
			
			throw( new FMFileManagerException( "Failed to read control file state", e ));
		}
	}
	
	protected void
	writeState()
	
		throws FMFileManagerException
	{
		boolean	write = write_required;
		
		if ( write ){
		
			write_required	= false;
			
			try{
				Map	data = new HashMap();
				
				data.put( "version", version);
				
				data.put( "length", current_length);
				
				FileUtil.writeResilientFile(
						controlFileDir, controlFileName, data, false );
				
			}catch( Throwable e ){
				
				throw( new FMFileManagerException( "Failed to write control file state", e ));
			}
		}
	}
	
	public FMFileImpl 
	getFile()
	{
		return( delegate.getFile());
	}
	
	public String
	getString()
	{
		return( "compact" );
	}
}