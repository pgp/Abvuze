/*
 * Created on Aug 8, 2007
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


package com.aelitis.azureus.core.speedmanager.impl;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import org.gudy.azureus2.core3.util.SimpleTimer;
import org.gudy.azureus2.core3.util.TimerEvent;
import org.gudy.azureus2.core3.util.TimerEventPerformer;

import com.aelitis.azureus.core.dht.speed.DHTSpeedTester;
import com.aelitis.azureus.core.dht.speed.DHTSpeedTesterContact;
import com.aelitis.azureus.core.dht.speed.DHTSpeedTesterContactListener;
import com.aelitis.azureus.core.dht.speed.DHTSpeedTesterListener;

public abstract class 
TestPingSourceImpl 
	implements DHTSpeedTester
{
	private final SpeedManagerAlgorithmProviderAdapter		adapter;
	
	private volatile int		contact_num;
	
	private final List	listeners 	= new ArrayList();
	
	final List	sources		= new CopyOnWriteArrayList();
	
	private int		period;
	
	protected
	TestPingSourceImpl(
		SpeedManagerAlgorithmProviderAdapter		_adapter )
	{
		adapter	= _adapter;
		
		SimpleTimer.addPeriodicEvent(
			"TestPingSourceImpl",
			1000,
			new TimerEventPerformer()
			{
				private int	ticks;
				
				public void 
				perform(
					TimerEvent event ) 
				{
					ticks++;
					
					List	sources_to_update;
					
					synchronized( sources ){

						while( sources.size() < contact_num ){
							
							addContact( new testSource());
						}
						
						sources_to_update = sources;
					}
					
					if ( period > 0 ){
						
						if ( ticks % period == 0 ){
							
							testSource[]	contacts = new testSource[sources_to_update.size()];
							
							sources_to_update.toArray( contacts );

							update( contacts );
						}
					}
				}
			});
	}
	
	protected SpeedManagerAlgorithmProviderAdapter
	getAdapter()
	{
		return( adapter );
	}
	
	protected void 
	update(
		testSource[]		contacts )
	{
		int[]	round_trip_times = new int[contacts.length];

		updateSources( contacts );
	
		for (int i=0;i<round_trip_times.length;i++){
			
			round_trip_times[i] = contacts[i].getRTT();
		}

        for (Object listener : listeners) {

            ((DHTSpeedTesterListener) listener).resultGroup(contacts, round_trip_times);
        }
	}
	
	protected abstract void
	updateSources(
		testSource[]	sources );
	
	public int
	getContactNumber()
	{
		return( contact_num );
	}
	
	public void
	setContactNumber(
		int		number )
	{
		contact_num = number;
	}
	
	protected void
	addContact(
		testSource	contact )
	{
		synchronized( sources ){
			
			sources.add( contact );
		}

        for (Object listener : listeners) {

            ((DHTSpeedTesterListener) listener).contactAdded(contact);
        }
	}
	
	protected void
	removeContact(
		testSource	contact )
	{
		synchronized( sources ){
			
			sources.remove( contact );
		}
	}	
	
	
	public void 
	destroy() 
	{
        for (Object listener : listeners) {

            ((DHTSpeedTesterListener) listener).destroyed();
        }
	}
		
	public void
	addListener(
		DHTSpeedTesterListener	listener )
	{
		listeners.add( listener );
	}
	
	public void
	removeListener(
		DHTSpeedTesterListener	listener )
	{
		listeners.remove( listener );
	}
	
	protected class
	testSource
		implements DHTSpeedTesterContact
	{
		private final InetSocketAddress address = new InetSocketAddress( 1 );
		
		private final List	listeners = new ArrayList();
		
		private int		rtt;
		
		public InetSocketAddress 
		getAddress() 
		{
			return( address );
		}
		
		public String
		getString()
		{
			return( "test source" );
		}		
		
		public int
		getPingPeriod()
		{
			return( period );
		}
		
		public void
		setPingPeriod(
			int		period_secs )
		{
			period = period_secs;
		}

		protected int
		getRTT()
		{
			return( rtt );
		}
		
		protected void
		setRTT(
			int		_rtt )
		{
			rtt	= _rtt;
		}
		
		protected void
		failed()
		{
            for (Object listener : listeners) {

                ((DHTSpeedTesterContactListener) listener).contactDied(this);
            }
		}
		
		public void
		destroy()
		{
			removeContact( this );
		}
		
		public void
		addListener(
			DHTSpeedTesterContactListener	listener )
		{
			listeners.add( listener );
		}
		
		public void
		removeListener(
			DHTSpeedTesterContactListener	listener )
		{
			listeners.remove( listener );
		}
	}
}
