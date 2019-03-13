/*
 * Created on 15 Jun 2006
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

package com.aelitis.azureus.core.security;

import java.math.BigInteger;

public interface 
CryptoManager 
{
		// don't even THINK about changing this!!!!

	String	CRYPTO_CONFIG_PREFIX = "core.crypto.";
	
	int HANDLER_ECC	= 1;
	
	int[] HANDLERS = { HANDLER_ECC };
	
	byte[]
	getSecureID();
	
	CryptoHandler
	getECCHandler();
	
	byte[]
	obfuscate(
            byte[] data);
	
	byte[]
	deobfuscate(
            byte[] data);
	
	void
	clearPasswords();
	
	void
	clearPasswords(
            int password_handler_type);

	void
	addPasswordHandler(
            CryptoManagerPasswordHandler handler);
	
	void
	removePasswordHandler(
            CryptoManagerPasswordHandler handler);
	
	void
	addKeyListener(
            CryptoManagerKeyListener listener);
	
	void
	removeKeyListener(
            CryptoManagerKeyListener listener);
	
	void
	setSRPParameters(
            byte[] salt,
            BigInteger verifier);
		
	SRPParameters
	getSRPParameters();
	
	interface
	SRPParameters
	{		
		byte[]
		getSalt();
		
		BigInteger
		getVerifier();
	}
}
