/*
 * Created on 19-Jan-2006
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

package com.aelitis.azureus.core.networkmanager.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.gudy.azureus2.core3.util.Debug;

public class 
TransportCipher 
{
	private Cipher		cipher;

	public
	TransportCipher(
		String					algorithm,
		int						mode,
		SecretKeySpec			key_spec,
		AlgorithmParameterSpec	params )
	
		throws Exception
	{
    	cipher = Cipher.getInstance( algorithm );
    
    	cipher.init( mode, key_spec, params );
	}

	TransportCipher(String algorithm, int mode, SecretKeySpec key_spec ) throws Exception {
		if (algorithm.equals("RC4")){
			try{
				cipher = Cipher.getInstance( algorithm );
				cipher.init( mode, key_spec );
			}
			catch( Exception e ){
				throw new RuntimeException(e);
			}

			//System.out.println( "RC4 key: " + ByteFormatter.encodeString( key_spec.getEncoded()));
			// skip first 1024 bytes of stream to protected against a Fluhrer, Mantin and Shamir attack
			update(new byte[1024]);

			//System.out.println( "RC4: first discard = " + ByteFormatter.encodeString( temp, 0, 4 ));
		}
		else {
	    	cipher = Cipher.getInstance( algorithm );
	    	cipher.init( mode, key_spec );
	    }
	}
	
	protected byte[]
	update(
		byte[]	data )
	{
		return( update( data, 0, data.length ));
	}
	
	protected byte[]
   	update(
   		byte[]	data,
   		int		offset,
   		int		length )
	{
		byte[]	result;
		if(length == 0){
			// watch out, cipher.update returns null with 0 length input
			result = new byte[0];
		}
		else if ( cipher != null ){
			result = cipher.update( data, offset, length );
		}
		else throw new RuntimeException("No RC4 implementation available");
		return result;
   	}
	           	
	protected void
	update(
		ByteBuffer	source_buffer,
		ByteBuffer	target_buffer )
	
		throws IOException
	{
		try{
			// TODO: 1.5 supports update( ByteBuffer, ByteBuffer )
			
			byte[]	source_bytes;
			int		offset;
			int		length	= source_buffer.remaining();
			
			if ( source_buffer.hasArray()){
				
				source_bytes 	= source_buffer.array();
				
				offset			= source_buffer.arrayOffset() + source_buffer.position();
				
			}else{
				
				source_bytes 	= new byte[length];
				
				offset			= 0;
				
				source_buffer.get( source_bytes );
			}
			
			byte[]	target_bytes = update( source_bytes, offset, length ); 
			
			source_buffer.position( source_buffer.limit());
			
			target_buffer.put( target_bytes );
			
		}catch( Throwable e ){
						
			throw( new IOException( Debug.getNestedExceptionMessage( e )));
		}
	}
	
	public String
	getName()
	{
		if ( cipher != null ){
			
			String	s = cipher.getAlgorithm();
			
			int	pos = s.indexOf("/");
			
			if ( pos != -1 ){
				
				s = s.substring(0,pos);
			}
			
			if ( s.equals( "RC4" )){
				
				s = "RC4-160";
				
			}else{
				
				s += "-" + cipher.getBlockSize()*8;
			}
			
			return( s );
		}else{
			
			return( "RC4-160" );
		}
	}
}
