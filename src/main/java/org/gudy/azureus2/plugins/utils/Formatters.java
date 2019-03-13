/*
 * File    : Formatter.java
 * Created : 30-Mar-2004
 * By      : parg
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

package org.gudy.azureus2.plugins.utils;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;

import org.gudy.azureus2.core3.util.Constants;

/**
 * @author parg
 *
 */
public interface 
Formatters 
{
	String BYTE_ENCODING = Constants.BYTE_ENCODING;
	String TEXT_ENCODING = Constants.DEFAULT_ENCODING;
	
	String
	formatByteCountToKiBEtc(
            long bytes);
	
	String
	formatByteCountToKiBEtcPerSec(
            long bytes);

	String
	formatPercentFromThousands(
            long thousands);
	
	String
	formatByteArray(
            byte[] data,
            boolean no_spaces);
	
	String
	encodeBytesToString(
            byte[] bytes);
	
	byte[]
	decodeBytesFromString(
            String str);
	
	String
	formatDate(
            long millis);
	
	/**
	 * @since 3.0.5.3
	 */
    String formatTimeOnly(long millis);
	
	/**
	 * @since 3.0.5.3
	 */
    String formatTimeOnly(long millis, boolean include_secs);

	/**
	 * @since 3.0.5.3
	 */
    String formatDateOnly(long millis);

	
	String
	formatTimeFromSeconds(
            long seconds);
	
	
	/**
	 * Format seconds remaining into an ETA value.
	 * @param seconds
	 * @return
	 * @since 2.4.0.3
	 */
    String formatETAFromSeconds(long seconds);
	
	
	
	byte[]
	bEncode(
            Map map)
	
		throws IOException;
	
	Map
	bDecode(
            byte[] data)
	
		throws IOException;
	
	String
	base32Encode(
            byte[] data);
	
	byte[]
	base32Decode(
            String data);
	
	Comparator
	getAlphanumericComparator(
            boolean ignore_case);
	
}
