/*
 * Created on Jun 11, 2009
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


package com.aelitis.azureus.core.util;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;


import com.aelitis.azureus.core.util.dns.DNSJavaImpl;
import org.gudy.azureus2.core3.util.Debug;


public class DNSUtils {
	private static DNSUtilsIntf impl;
	
	static {
//		String cla = System.getProperty( "az.factory.dnsutils.impl", "com.aelitis.azureus.core.util.dns.DNSUtilsImpl" );
//
//		try{
//			impl = (DNSUtilsIntf)Class.forName( cla ).newInstance();
//
//		}catch( Throwable e ){
//
//			Debug.out( "Failed to instantiate impl: " + cla, e );
//		}

		impl = new DNSJavaImpl(); // compatible with both Java and Android
	}
		
	public static DNSUtilsIntf
	getSingleton()
	{
		return( impl );
	}
	
	public interface DNSUtilsIntf {
		DNSDirContext
		getInitialDirContext()
		
			throws Exception;
		
		DNSDirContext
		getDirContextForServer(
				String dns_server_ip)
		
			throws Exception;
		
		Inet6Address
		getIPV6ByName(
				String host)
		
			throws UnknownHostException;
		
		List<InetAddress>
		getAllByName(
				String host)
			
			throws UnknownHostException;

		List<InetAddress>
		getAllByName(
				DNSUtils.DNSDirContext context,
				String host)
			
			throws UnknownHostException;
		
		List<String>
		getTXTRecords(
				String query);
		
		String
		getTXTRecord(
				String query)
		
			throws UnknownHostException;
	}
	
	public interface
	DNSDirContext
	{
		String
		getString();
	}
	
	public static String
	getInterestingHostSuffix(
		String		host )
	{
		if ( host == null ){
			
			return( null );
		}
		
		String[] bits = host.split( "\\." );
		
		int	num_bits = bits.length;
		
		if ( bits[num_bits-1].equals( "dht" )){
			
			return( null );
		}
		
		if ( bits.length > 2 ){
			
			// We want to find the most sensible name for the host, <prefix>+"."+ TLD
			// Unfortuantely the TLD list is large and ever growing (see http://data.iana.org/TLD/tlds-alpha-by-domain.txt)
			// and the rules for identifying valid ones even worse (see https://www.publicsuffix.org/list/public_suffix_list.dat)
			
			// so we assume that the host has its own dns prefix, e.g. tracker01.a.s.d.f, and want to drop the prefix
			// to turn tracker.a.com -> a.com
			// but also tracker.fred.org.uk -> fred.org.uk
			// so.... gonna assume that a 2/3 character components from the right are boring
			
			int	hit = -1;
			
			for ( int i=num_bits-1;i>=0;i--){
				
				String bit = bits[i];
				
				if ( bit.length() > 3 ){
					
					hit = i;
					
					break;
				}
			}
			
			if ( hit > 0 ){
				
				host = "";
				
				for ( int i=hit;i<num_bits;i++){
					
					host += (host==""?"":".") + bits[i];
				}
			}
		}
		
		return( host );
	}
}
