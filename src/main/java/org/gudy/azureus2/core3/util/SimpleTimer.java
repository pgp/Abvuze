/*
 * Created on 12-Jul-2004
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

package org.gudy.azureus2.core3.util;

import com.aelitis.azureus.core.util.CopyOnWriteList;

/**
 * @author parg
 *
 */

public class 
SimpleTimer 
{
		/**
		 * A simple timer class for use by application components that want to schedule 
		 * low-overhead events (i.e. when fired the event shouldn't take significant processing
		 * time as there is a limited thread pool to service it 
		 */
	
	protected static final Timer	timer;
	
	static final CopyOnWriteList<TimerTickReceiver>		tick_receivers = new CopyOnWriteList<>(true);
	
	static{
		timer = new Timer("Simple Timer",32);
		
		timer.setIndestructable();
		
		timer.setWarnWhenFull();
		
		// timer.setLogCPU();
		
		// timer.setLogging(true);
		
		addPeriodicEvent(
			"SimpleTimer:ticker",
			1000,
			new TimerEventPerformer()
			{
				private int tick_count;
				
				public void 
				perform(
					TimerEvent event ) 
				{
					tick_count++;
					
					if ( tick_receivers.size() > 0 ){
						
						long mono_now = SystemTime.getMonotonousTime();
						
						for ( TimerTickReceiver ttr: tick_receivers ){
							
							try{
								ttr.tick( mono_now, tick_count );
								
							}catch( Throwable e ){
								
								Debug.out( e );
							}
						}
					}
				}
			});
	}
	
	public static TimerEvent
	addEvent(
		String				name,
		long				when,
		TimerEventPerformer	performer )
	{
		return timer.addEvent(name, when, performer);
	}
	
	public static TimerEvent
	addEvent(
		String				name,
		long				when,
		boolean				absolute,
		TimerEventPerformer	performer )
	{
		return timer.addEvent(name, when, absolute, performer);
	}
	
	public static TimerEventPeriodic
	addPeriodicEvent(
		String				name,
		long				frequency,
		TimerEventPerformer	performer )
	{
		return timer.addPeriodicEvent(name, frequency, performer);
	}
	
	public static TimerEventPeriodic
	addPeriodicEvent(
		String				name,
		long				frequency,
		boolean				absolute,
		TimerEventPerformer	performer )
	{
		return timer.addPeriodicEvent(name, frequency, absolute, performer);
	}
	
	public static void
	addTickReceiver(
		TimerTickReceiver	receiver )
	{
		tick_receivers.add( receiver );
	}
	
	public static void
	removeTickReceiver(
		TimerTickReceiver	receiver )
	{
		tick_receivers.remove( receiver );
	}
	
	public interface
	TimerTickReceiver
	{
		void
		tick(
                long mono_now,
                int tick_ount);
	}
}
