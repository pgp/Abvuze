/*
 * Created on Jul 6, 2007
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;

import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.DisplayFormatters;
import org.gudy.azureus2.core3.util.FileUtil;
import org.gudy.azureus2.core3.util.IndentWriter;
import org.gudy.azureus2.core3.util.SystemTime;

import com.aelitis.azureus.core.speedmanager.SpeedManagerLimitEstimate;
import com.aelitis.azureus.core.speedmanager.SpeedManagerPingMapper;
import com.aelitis.azureus.core.speedmanager.SpeedManagerPingZone;

class
SpeedManagerPingMapperImpl
	implements SpeedManagerPingMapper
{
	static final int VARIANCE_GOOD_VALUE		= 50;
	static final int VARIANCE_BAD_VALUE			= 150;
	static final int VARIANCE_MAX				= VARIANCE_BAD_VALUE*10;
	
	static final int RTT_BAD_MIN				= 350;
	static final int RTT_BAD_MAX				= 500;
	
	static final int RTT_MAX					= 30*1000;

		// don't make this too large as we don't start considering capacity decreases until this
		// is full
	
	static final int MAX_BAD_LIMIT_HISTORY		= 16;
	
	static final int SPEED_DIVISOR = 256;
	
	private static final int SPEED_HISTORY_PERIOD	= 3*60*1000; // 3 min
	private static final int SPEED_HISTORY_COUNT	= SPEED_HISTORY_PERIOD / SpeedManagerImpl.UPDATE_PERIOD_MILLIS;
		
	private final SpeedManagerImpl	speed_manager;
	private final String				name;
	private final boolean				variance;
	private final boolean				trans;
			
	private int	ping_count;
	
	private pingValue[] pings;
	private final int max_pings;
	
	private pingValue prev_ping;
	
	private final int[] x_speeds = new int[ SPEED_HISTORY_COUNT ];
	private final int[] y_speeds = new int[ SPEED_HISTORY_COUNT ];
	
	private int speeds_next;
	
	private LinkedList	regions;
		
	private int last_x;
	private int	last_y;
	
	private final int[]	recent_metrics = new int[3];
	private int		recent_metrics_next;

	private final limitEstimate[] UD_estimate = new limitEstimate[2]; // up/down estimates

	private final LinkedList[] last_bad_UDs = new LinkedList[2]; // last bad ups/downs

	private static final int BAD_PROGRESS_COUNTDOWN	= 5;

	private final limitEstimate[] last_bad_UD = new limitEstimate[2]; // last bad up/down

	private final int[] bad_UD_in_progress_count = {0,0};

	private final limitEstimate[] best_good_UD = new limitEstimate[2];

	private final limitEstimate[] UD_capacity = new limitEstimate[]{getNullLimit(),getNullLimit()};

	private File history_file;
	
	protected
	SpeedManagerPingMapperImpl(
		SpeedManagerImpl		_speed_manager,
		String					_name,
		int						_entries ,
		boolean					_variance,
		boolean					_transient )
	{
		speed_manager	= _speed_manager;
		name			= _name;
		max_pings		= _entries;
		variance		= _variance;
		trans			= _transient;	
		
		init();
	}
	
	protected synchronized void
	init()
	{
		pings		= new pingValue[max_pings];
		ping_count	= 0;
		
		regions	= new LinkedList();

		UD_estimate[0]		= getNullLimit();
		UD_estimate[1]		= getNullLimit();

		last_bad_UDs[0]		= new LinkedList();
		last_bad_UDs[1]		= new LinkedList();

		last_bad_UD[0]				= null;
		bad_UD_in_progress_count[0]	= 0;

		last_bad_UD[1]				= null;
		bad_UD_in_progress_count[1]	= 0;

		best_good_UD[0] = null;
		best_good_UD[1] = null;
		
		UD_capacity[0]	= getNullLimit();
		UD_capacity[1]	= getNullLimit();

		prev_ping 			= null;
		recent_metrics_next	= 0;
	}
	
	protected synchronized void
	loadHistory(
		File		file )
	{
		try{
			if ( history_file != null && history_file.equals( file )){
				
				return;
			}
			
			if ( history_file != null ){
				
				saveHistory();
			}
			
			history_file = file;

			init();
			
			if ( history_file.exists()){
							
				// skip key intern to save CPU  as there are a lot of keys 
				// and we end up ditching the map after it's processed
				Map map = FileUtil.readResilientFile( history_file.getParentFile(), history_file.getName(), false, false );
				
				List	p = (List)map.get( "pings" );
				
				if ( p != null ){
					
					for (int i=0;i<p.size();i++){
						
						Map	m = (Map)p.get(i);
						
						int	x 		= ((Long)m.get( "x" )).intValue();
						int	y 		= ((Long)m.get( "y" )).intValue();
						int	metric 	= ((Long)m.get( "m" )).intValue();
						
						if ( i == 0 ){
							
							last_x	= 0;
							last_y	= 0;						
						}
						
						if ( variance ){
							
							if ( metric > VARIANCE_MAX ){
						
								metric = VARIANCE_MAX;
							}
						}else{
							
							if ( metric > RTT_MAX ){
								
								metric = RTT_MAX;
							}						
						}
						
						addPingSupport( x, y, -1, metric );
					}
				}
								
				last_bad_UDs[0] = loadLimits( map, "lbus" );
				last_bad_UDs[1] = loadLimits( map, "lbds" );
				
				if (last_bad_UDs[0].size() > 0)
					last_bad_UD[0]	= (limitEstimate)last_bad_UDs[0].get(last_bad_UDs[0].size()-1);
				
				if (last_bad_UDs[1].size() > 0)
					last_bad_UD[1] = (limitEstimate)last_bad_UDs[1].get(last_bad_UDs[1].size()-1);


				best_good_UD[0]	= loadLimit((Map)map.get( "bgu" ));
				best_good_UD[1]	= loadLimit((Map)map.get( "bgd" ));

				UD_capacity[0]	= loadLimit((Map)map.get( "upcap" ));
				UD_capacity[1]	= loadLimit((Map)map.get( "downcap" ));
				
				log( "Loaded " + ping_count + " entries from " + history_file + ": bad_up=" + getLimitString(last_bad_UDs[0]) + ", bad_down=" + getLimitString(last_bad_UDs[1]));
				
			}else{
				
				// first time with this ASN - removed auto speed test in 4813 so decided to increase
				// the initial estimated upload limit to avoid starting out too low
				
				setEstimatedUploadCapacityBytesPerSec( 75*1024, SpeedManagerLimitEstimate.TYPE_ESTIMATED );
			}
		
			prev_ping 			= null;
			recent_metrics_next	= 0;
			
			updateLimitEstimates();	
				
		}catch( Throwable e ){
			
			Debug.printStackTrace(e);
		}
	}
	
	protected synchronized void
	saveHistory()
	{
		try{
			if ( history_file == null ){
				
				return;
			}
			
			Map	map = new HashMap();
			
			List p = new ArrayList(ping_count);
			
				// NOTE: add to this you will need to modify the "reset" method appropriately
			
			map.put( "pings", p );
			
			for (int i=0;i<ping_count;i++){
				
				pingValue ping = pings[i];
				
				Map	m = new HashMap();
				
				p.add( m );
				
				m.put( "x", (long) ping.getX());
				m.put( "y", (long) ping.getY());
				m.put( "m", (long) ping.getMetric());
			}
			
			saveLimits( map, "lbus", last_bad_UDs[0]);
			saveLimits( map, "lbds", last_bad_UDs[1]);

			if ( best_good_UD[0] != null )
				map.put("bgu", saveLimit(best_good_UD[0]));
			
			if ( best_good_UD[1] != null )
				map.put("bgd", saveLimit(best_good_UD[1]));

			map.put( "upcap", 	saveLimit(UD_capacity[0]));
			map.put( "downcap", saveLimit(UD_capacity[1]));

			FileUtil.writeResilientFile( history_file, map );
			
			log( "Saved " + p.size() + " entries to " + history_file );
		
		}catch( Throwable e ){
			
			Debug.printStackTrace(e);
		}
	}
	
	protected LinkedList
	loadLimits(
		Map		map,
		String	name )
	{
		LinkedList	result = new LinkedList();
		
		List	l = (List)map.get(name);
		
		if ( l != null ){

            for (Object o : l) {

                Map m = (Map) o;

                result.add(loadLimit(m));
            }
		}
		
		return( result );
	}
	
	protected limitEstimate
	loadLimit(
		Map	m )
	{
		if ( m == null ){
			
			return( getNullLimit());
		}
		
		int	speed = ((Long)m.get( "s" )).intValue();
		
		double	metric = Double.parseDouble( new String((byte[])m.get("m")));
		
		int	hits = ((Long)m.get( "h" )).intValue();

		long	when = (Long) m.get("w");
		
		byte[]	t_bytes = (byte[])m.get("t");
		
		double type = t_bytes==null?SpeedManagerLimitEstimate.TYPE_ESTIMATED :Double.parseDouble( new String( t_bytes ));
		
		return( new limitEstimate( speed, type, metric, hits, when, new int[0][] ));
	}
	
	protected void
	saveLimits(
		Map				map,
		String			name,
		List			limits )
	{
		List	l = new ArrayList();

        for (Object limit1 : limits) {

            limitEstimate limit = (limitEstimate) limit1;

            Map m = saveLimit(limit);

            l.add(m);
        }
		
		map.put( name, l );
	}
	
	protected Map
	saveLimit(
		limitEstimate	limit )
	{
		if ( limit == null ){
			
			limit = getNullLimit();
		}
		
		Map	m = new HashMap();
		
		m.put( "s", (long) limit.getBytesPerSec());
		
		m.put( "m", String.valueOf( limit.getMetricRating()));
		
		m.put( "t", String.valueOf( limit.getEstimateType()));
		
		m.put( "h", (long) limit.getHits());
		
		m.put( "w", limit.getWhen());
		
		return( m );
	}
	
	public boolean
	isActive()
	{
		return( variance );
	}
	
	protected limitEstimate
	getNullLimit()
	{
		return( new limitEstimate( 0, SpeedManagerLimitEstimate.TYPE_UNKNOWN, 0, 0, 0, new int[0][] ));
	}
	
	protected String
	getLimitString(
		List	limits )
	{
		String	str = "";
		
		for (int i=0;i<limits.size();i++){
	
			str += (i==0?"":",") + ((limitEstimate)limits.get(i)).getString();
		}
		
		return( str );
	}
	
	protected void
	log(
		String	str )
	{
		if ( speed_manager != null ){
		
			speed_manager.log( str );
		}
	}
	
	public String
	getName()
	{
		return( name );
	}
	
	protected synchronized void
	addSpeed(
		int		x,
		int		y )
	{
		x = x/SPEED_DIVISOR;
		y = y/SPEED_DIVISOR;
		
		if ( x > 65535 )x = 65535;
		if ( y > 65535 )y = 65535;

		addSpeedSupport( x, y );
	}
	
	protected synchronized void
	addSpeedSupport(
		int		x,
		int		y )
	{
		x_speeds[speeds_next] = x;
		y_speeds[speeds_next] = y;
				
		speeds_next = (speeds_next+1)%SPEED_HISTORY_COUNT;
		
		int	min_x	= Integer.MAX_VALUE;
		int	min_y	= Integer.MAX_VALUE;
		
		for (int i=0;i<SPEED_HISTORY_COUNT;i++){
			
			min_x = Math.min( min_x, x_speeds[i] );
			min_y = Math.min( min_y, y_speeds[i] );
		}
		
		min_x *= SPEED_DIVISOR;
		min_y *= SPEED_DIVISOR;

		limitEstimate uE = UD_capacity[0];
		limitEstimate dE = UD_capacity[1];

		if ( uE.getEstimateType() != SpeedManagerLimitEstimate.TYPE_MANUAL){
			if ( min_x > uE.getBytesPerSec()){
				uE.setBytesPerSec(min_x);
				uE.setMetricRating(0);
				uE.setEstimateType( SpeedManagerLimitEstimate.TYPE_ESTIMATED);
				speed_manager.informUpCapChanged();
			}
		}

		if ( dE.getEstimateType() != SpeedManagerLimitEstimate.TYPE_MANUAL){
			if (min_y > dE.getBytesPerSec()){
				dE.setBytesPerSec(min_y);
				dE.setMetricRating(0);
				dE.setEstimateType( SpeedManagerLimitEstimate.TYPE_ESTIMATED);
				speed_manager.informDownCapChanged();
			}
		}
	}
	
	protected synchronized void
	addPing(
		int		x,
		int		y,
		int		rtt,
		boolean	re_base )
	{
		x = x/SPEED_DIVISOR;
		y = y/SPEED_DIVISOR;
		
		if ( x > 65535 )x = 65535;
		if ( y > 65535 )y = 65535;
		if ( rtt > 65535 )rtt = variance?VARIANCE_MAX:RTT_MAX;
		if ( rtt == 0 )rtt = 1;
		
			// ping time won't refer to current x+y due to latencies, apply to average between
			// current and previous
				
		int	average_x = (x + last_x )/2;
		int	average_y = (y + last_y )/2;
		
		last_x	= x;
		last_y	= y;
		
		x	= average_x;
		y	= average_y;
		
		int	metric;
		
		if ( variance ){
			
			if ( re_base ){
				
				log( "Re-based variance" );
				
				recent_metrics_next = 0;
			}

			recent_metrics[recent_metrics_next++%recent_metrics.length] = rtt;
			
			int var_metric = 0;
			int rtt_metric = 0;

			if ( recent_metrics_next > 1 ){
				
				int	entries = Math.min( recent_metrics_next, recent_metrics.length );
				
				int total = 0;
				
				for (int i=0;i<entries;i++){
					
					total += recent_metrics[i];
				}
				
				int	average = total/entries;
				
				int	total_deviation = 0;
				
				for (int i=0;i<entries;i++){

					int	deviation = recent_metrics[i] - average;
					
					total_deviation += deviation * deviation;
				}
				
					// we deliberately don't divide by num samples as this accentuates larger deviations
				
				var_metric = (int)Math.sqrt( total_deviation );
				
					// variance is a useful measure. however, under some conditions, in particular high
					// download speeds, we get elevated ping times with little variance
					// factor this in
				
				if ( entries == recent_metrics.length ){
					
					int	total_rtt = 0;
					
					for (int i=0;i<entries;i++){

						total_rtt += recent_metrics[i];
					}
					
					int	average_rtt = total_rtt / recent_metrics.length;
					
					if ( average_rtt >= RTT_BAD_MAX ){
						
						rtt_metric = VARIANCE_BAD_VALUE;
						
					}else if ( average_rtt > RTT_BAD_MIN ){
						
						int	rtt_diff 	= RTT_BAD_MAX - RTT_BAD_MIN;
						int	rtt_base	= average_rtt - RTT_BAD_MIN;
						
						rtt_metric = VARIANCE_GOOD_VALUE + (( VARIANCE_BAD_VALUE - VARIANCE_GOOD_VALUE ) * rtt_base ) / rtt_diff;
					}
				}
			}
						
			metric = Math.max( var_metric, rtt_metric );
			
			if ( metric < VARIANCE_BAD_VALUE ){
				
				addSpeedSupport( x, y );
				
			}else{
				
				addSpeedSupport( 0, 0 );
			}
		}else{
			
			metric = rtt;
		}
		
		region new_region = addPingSupport( x, y, rtt, metric );
		
		updateLimitEstimates();
		
		if ( variance ){
		
			String up_e 	= getShortString( getEstimatedUploadLimit( false )) + "," + 
								getShortString(getEstimatedUploadLimit( true )) + "," +
								getShortString(getEstimatedUploadCapacityBytesPerSec());
			
			String down_e 	= getShortString(getEstimatedDownloadLimit( false )) + "," + 
								getShortString(getEstimatedDownloadLimit( true )) + "," +
								getShortString(getEstimatedDownloadCapacityBytesPerSec());
			
			log( "Ping: rtt="+rtt+",x="+x+",y="+y+",m="+metric + 
					(new_region==null?"":(",region=" + new_region.getString())) + 
					",mr=" + getCurrentMetricRating() + 
					",up=[" + up_e + (best_good_UD[0]==null?"":(":"+getShortString(best_good_UD[0]))) +
						"],down=[" + down_e + (best_good_UD[1]==null?"":(":"+getShortString(best_good_UD[1]))) + "]" +
					",bu="+getLimitStr(last_bad_UDs[0],true)+",bd="+getLimitStr(last_bad_UDs[1],true));
		}
	}
	
	protected region
	addPingSupport(
		int		x,
		int		y,
		int		rtt,
		int		metric )
	{
		if ( ping_count == pings.length ){

				// discard oldest pings and reset 
							
			int	to_discard = pings.length/10;
			
			if ( to_discard < 3 ){
				
				to_discard = 3;
			}
			
			ping_count = pings.length - to_discard;

			System.arraycopy(pings, to_discard, pings, 0, ping_count);
			
			for (int i=0;i<to_discard;i++ ){
				
				regions.removeFirst();
			}
		}
				
		pingValue	ping = new pingValue( x, y, metric );

		pings[ping_count++] = ping;
		
		region	new_region = null;
		
		if ( prev_ping != null ){
			
			new_region = new region(prev_ping,ping);
			
			regions.add( new_region );
		}
		
		prev_ping = ping;

		return( new_region );
	}
	
	public synchronized int[][]
	getHistory()
	{
		int[][]	result = new int[ping_count][];

		for (int i=0;i<ping_count;i++){
			
			pingValue	ping = pings[i];
			
			result[i] = new int[]{ SPEED_DIVISOR*ping.getX(), SPEED_DIVISOR*ping.getY(), ping.getMetric()};
		}
		
		return( result );
	}
	
	public synchronized SpeedManagerPingZone[]
	getZones()
	{
		return((SpeedManagerPingZone[])regions.toArray(new SpeedManagerPingZone[0]));
	}
	
	public synchronized SpeedManagerLimitEstimate
	getEstimatedUploadLimit(
		boolean	persistent )
	{
		return( adjustForPersistence( UD_estimate[0], best_good_UD[0], last_bad_UD[0], persistent ));
	}
	
	public synchronized SpeedManagerLimitEstimate
	getEstimatedDownloadLimit(
		boolean	persistent )
	{
		return( adjustForPersistence( UD_estimate[1], best_good_UD[1], last_bad_UD[1], persistent ));
	}

	public SpeedManagerLimitEstimate
	getLastBadUploadLimit()
	{
		return last_bad_UD[0];
	}
	
	public SpeedManagerLimitEstimate
	getLastBadDownloadLimit()
	{
		return last_bad_UD[1];
	}
	
	public synchronized SpeedManagerLimitEstimate[]
	getBadUploadHistory()
	{
		return((SpeedManagerLimitEstimate[])last_bad_UDs[0].toArray(new SpeedManagerLimitEstimate[0]));
	}

	public synchronized SpeedManagerLimitEstimate[]
	getBadDownloadHistory()
	{
		return((SpeedManagerLimitEstimate[])last_bad_UDs[1].toArray(new SpeedManagerLimitEstimate[0]));
	}
	                             	
	protected SpeedManagerLimitEstimate
	adjustForPersistence(
		limitEstimate		estimate,
		limitEstimate		best_good,	
		limitEstimate		last_bad,	
		boolean				persistent )
	{
		if ( estimate == null ){
			
			return( null );
		}
		
		if ( persistent ){
			
				// if result is bad then we return this
			
			if ( estimate.getMetricRating() == -1 ){
				
				return( estimate );
			}
			
				// see if best good/last bad are relevant
			
			limitEstimate	persistent_limit = null;
			
			if ( best_good != null && last_bad != null ){
				
				if ( last_bad.getWhen() > best_good.getWhen()){
					
					persistent_limit = last_bad;
					
				}else{
					
					if ( best_good.getBytesPerSec() > last_bad.getBytesPerSec()){
						
						persistent_limit = best_good;
						
					}else{
						
						persistent_limit = last_bad;
					}
				}
			}else if ( best_good != null ){
				
				persistent_limit = best_good;
				
			}else if ( last_bad != null ){
				
				persistent_limit = last_bad;
			}
			
			if ( persistent_limit == null ){
				
				return( estimate );
			}

			if ( estimate.getBytesPerSec() > persistent_limit.getBytesPerSec()){
				
				return( estimate );
				
			}else{
									
				// need to convert this into a good rating to correspond to the 
				// actual estimate type we have
					
				limitEstimate res = estimate.getClone();
				
				res.setBytesPerSec(persistent_limit.getBytesPerSec());

				return( res );
			}
		}else{
			
			return( estimate );
		}
	}

	// up_estimate, last_bad_up, up_estimate, last_bad_ups, up_capacity
	// bad_up_in_progress_count

	protected void updateUDSingleLimitEstimates(int upOrDown, double cm) { // 0: up, 1: down
		UD_estimate[upOrDown] 	= getEstimatedLimit( true );
		if (UD_estimate[upOrDown] != null){
			double metric = UD_estimate[upOrDown].getMetricRating();
			if ( metric == -1 ){
				if ( bad_UD_in_progress_count[upOrDown] == 0 ){

					// don't count the duplicates we naturally get when sitting here with a bad limit
					// and nothing going on to change this situation

					if ( last_bad_UD[upOrDown] == null || last_bad_UD[upOrDown].getBytesPerSec() != UD_estimate[upOrDown].getBytesPerSec()){
						bad_UD_in_progress_count[upOrDown] = BAD_PROGRESS_COUNTDOWN;
						last_bad_UDs[upOrDown].addLast(UD_estimate[upOrDown]);
						if ( last_bad_UDs[upOrDown].size() > MAX_BAD_LIMIT_HISTORY ){
							last_bad_UDs[upOrDown].removeFirst();
						}
						checkCapacityDecrease( true, UD_capacity[upOrDown], last_bad_UDs[upOrDown]);
					}
				}
				last_bad_UD[upOrDown] = UD_estimate[upOrDown];
			}else if ( metric == 1 ){
				if ( best_good_UD[upOrDown] == null ){
					best_good_UD[upOrDown] = UD_estimate[upOrDown];
				}else{
					if ( best_good_UD[upOrDown].getBytesPerSec() < UD_estimate[upOrDown].getBytesPerSec()){
						best_good_UD[upOrDown] = UD_estimate[upOrDown];
					}
				}
			}

			if ( bad_UD_in_progress_count[upOrDown] > 0 ){
				if ( cm == -1 ){
					bad_UD_in_progress_count[upOrDown] = BAD_PROGRESS_COUNTDOWN;
				}
				else if ( cm == 1 ){
					bad_UD_in_progress_count[upOrDown]--;
				}
			}
		}
	}

	protected void updateLimitEstimates() {
		double cm = getCurrentMetricRating();
		updateUDSingleLimitEstimates(0,cm); // up
		updateUDSingleLimitEstimates(1,cm); // down
	}
	
	protected void
	checkCapacityDecrease(
		boolean			is_up,
		limitEstimate	capacity,
		LinkedList		bads )
	{
		if ( capacity.getEstimateType() == SpeedManagerLimitEstimate.TYPE_MANUAL){
			
			return;
		}
		
		if ( bads.size() < MAX_BAD_LIMIT_HISTORY ){
			
			return;
		}
		
			// remeber, 0 means UNLIMITED!!!
		
		int	cap = capacity.getBytesPerSec();
		
			// sanity check
		
		if ( cap > 0 && cap < 10*1024 ){
		
			return;
		}
		
		List b = new ArrayList( bads );
		
		Collections.sort(
			b,
			new Comparator()
			{
				public int 
				compare(
					Object o1, 
					Object o2 )
				{
					limitEstimate	l1 = (limitEstimate)o1;
					limitEstimate	l2 = (limitEstimate)o2;
					
					return( l1.getBytesPerSec() - l2.getBytesPerSec());
				}
			});
		
			// drop top bottom quarter of measurements
		
		int	start 	= MAX_BAD_LIMIT_HISTORY/4;
		int	end		= MAX_BAD_LIMIT_HISTORY - start;
		
		int	total 	= 0;
		int	num		= 0;
		
		for (int i=start;i<end;i++){
		
			int	s = ((limitEstimate)b.get(i)).getBytesPerSec();
			
			total += s;
			
			num++;
		}
		
		int	average = total/num;
		
			// only consider decreases!
		
		if ( cap > 0 && average >= cap ){
			
			log( "Not reducing " + (is_up?"up":"down") + " capacity - average=" + DisplayFormatters.formatByteCountToKiBEtcPerSec( average ) + ",capacity=" + DisplayFormatters.formatByteCountToKiBEtcPerSec( cap ));

			return;
		}
		
		int	total_deviation = 0;
		
		for (int i=start;i<end;i++){
			
			int	s = ((limitEstimate)b.get(i)).getBytesPerSec();

			int	deviation = s - average;
			
			total_deviation += deviation * deviation;
		}
		
		int	deviation = (int)Math.sqrt( ((double)total_deviation) / num );
		
			// adjust if deviation within 50% of capacity
		
		if ( cap <= 0 || ( deviation < cap/2 && average < cap )){
			
			log( "Reducing " + (is_up?"up":"down") + " capacity from " + cap + " to " + average + " due to frequent lower chokes (deviation=" + DisplayFormatters.formatByteCountToKiBEtcPerSec(deviation) + ")" );
			
			capacity.setBytesPerSec( average );
			
			capacity.setEstimateType( SpeedManagerLimitEstimate.TYPE_CHOKE_ESTIMATED);

				// remove the last 1/4 bad stats so we don't reconsider adjusting until more data collected
			
			for (int i=0;i<start;i++){
				
				bads.removeFirst();
			}
		}else{
			
			log( "Not reducing " + (is_up?"up":"down") + " capacity - deviation=" + DisplayFormatters.formatByteCountToKiBEtcPerSec( deviation ) + ",capacity=" + DisplayFormatters.formatByteCountToKiBEtcPerSec( cap ));

		}
	}
	
	protected synchronized limitEstimate
	getEstimatedLimit(
		boolean		up )
	{
		if ( !variance ){
			
			return( getNullLimit() );
		}
		
		int	num_samples = regions.size();
		
		if ( num_samples == 0 ){
			
			return( getNullLimit());
		}
		
		Iterator	it = regions.iterator();
		
		int	max_end = 0;
		
		while( it.hasNext()){
			
			region r = (region)it.next();
			
			int	end		= (up?r.getUploadEndBytesPerSec():r.getDownloadEndBytesPerSec())/SPEED_DIVISOR;
			
			if ( end > max_end ){
				
				max_end = end;
			}
		}
						
		int	sample_end = max_end + 1;
		
		int[]	totals 			= new int[sample_end];
		short[]	hits			= new short[sample_end];				
		short[]	worst_var_type	= new short[sample_end];
											
		ListIterator sample_it = regions.listIterator( 0 );
			
			// flatten out all observations into a single munged metric

		while( sample_it.hasNext()){
			
			region r = (region)sample_it.next();
		
			int	start 	= (up?r.getUploadStartBytesPerSec():r.getDownloadStartBytesPerSec())/SPEED_DIVISOR;
			int	end		= (up?r.getUploadEndBytesPerSec():r.getDownloadEndBytesPerSec())/SPEED_DIVISOR;
			int	metric	= r.getMetric();
		
			int	weighted_start;
			int	weighted_end;
			
			short	this_var_type;
			
			if ( metric < VARIANCE_GOOD_VALUE ){
			
					// a good variance applies to all speeds up to this one. This means
					// that previously occuring bad variance will get flattened out by
					// subsequent good variance
				
				weighted_start 	= 0;	
				weighted_end	= end;
				this_var_type 	= 0;
				
			}else if ( metric < VARIANCE_BAD_VALUE ){
				
					// medium values, treat at face value
				
				weighted_start 	= start;
				weighted_end	= end;
				this_var_type	= VARIANCE_GOOD_VALUE;

			}else{
				
					// bad ones, treat at face value
				
				weighted_start 	= start;
				weighted_end	= max_end;
				this_var_type	= VARIANCE_BAD_VALUE;
			}
			
			for (int j=weighted_start;j<=weighted_end;j++){
			
					// a bad variance resets totals as we have encountered this after (in time)
					// the existing data and this is more relevant and replaces any feel good
					// factor we might have accumulated via prior observations
				
				if ( this_var_type == VARIANCE_BAD_VALUE && worst_var_type[j] <= this_var_type ){
					
					totals[j]	= 0;
					hits[j]		= 0;
					
					worst_var_type[j] = this_var_type;
				}
				
				totals[j] += metric;
				hits[j]++;
			}
		}

			// now average out values based on history computed above
						
		for (int i=0;i<sample_end;i++){
			
			int	hit = hits[i];
			
			if ( hit > 0 ){
				
				int	average = totals[i]/hit;
				
				totals[i] = average;
				
				if ( average < VARIANCE_GOOD_VALUE ){

					worst_var_type[i] = 0;
				
				}else if ( average < VARIANCE_BAD_VALUE ){
				
					worst_var_type[i] = VARIANCE_GOOD_VALUE;

				}else{
					
					worst_var_type[i] = VARIANCE_BAD_VALUE;
				}
			}
		}
			
			// break history up into segments of same speed 
		
		int	last_average 			= -1;
		int	last_average_change		= 0;
		int last_average_worst_var	= 0;
		int	last_max_hits			= 0;
		
		int	worst_var	= 0;
		
		List segments = new ArrayList(totals.length);
		
		for (int i=0;i<sample_end;i++){
			
			int var		= worst_var_type[i];
			int	hit 	= hits[i];
			
			if ( var > worst_var ){
				
				worst_var = var;
			}
			
			int average = totals[i];
			
			if ( i == 0 ){
				
				last_average = average;
				
			}else if ( last_average != average ){
				
				segments.add( new int[]{ last_average, last_average_change*SPEED_DIVISOR, (i-1)*SPEED_DIVISOR, last_average_worst_var, last_max_hits });
				
				last_average 			= average;
				last_average_change		= i;
				last_average_worst_var	= var;
				last_max_hits			= hit;
			}else{
				
				last_average_worst_var 	= Math.max( var, last_average_worst_var );
				last_max_hits			= Math.max( hit, last_max_hits );
			}
		}
		
		if ( last_average_change != sample_end - 1 ){
		
			segments.add( new int[]{ last_average, last_average_change*SPEED_DIVISOR, (sample_end-1)*SPEED_DIVISOR, last_average_worst_var, last_max_hits });
		}
		
		int[]	estimate_seg 	= null;
		
		int estimate_var	= 0;

			// take smallest bad value and largest good
		
		if ( worst_var == VARIANCE_BAD_VALUE ){
			
			for (int i=segments.size()-1;i>=0;i-- ){
						
				int[]	seg = (int[])segments.get(i);
				
				int	var = seg[3];
				
				if ( var >= worst_var ){
						
					estimate_seg 	= seg;
					estimate_var	= var;
				}
			}
		}else{
            for (Object segment : segments) {

                int[] seg = (int[]) segment;

                int var = seg[3];

                if (var >= worst_var) {

                    estimate_seg = seg;
                    estimate_var = var;
                }
            }
		}
		
		int	estimate_speed;
		int	estimate_hits;
		
		if ( estimate_seg == null ){
			
			estimate_speed 	= -1;
			estimate_hits	= 0;

		}else{
			
			estimate_speed 	= -1;
			
			if ( worst_var == 0 ){
				
				estimate_speed = estimate_seg[2];
				
			}else if ( worst_var == VARIANCE_GOOD_VALUE ){
				
				estimate_speed = ( estimate_seg[1] + estimate_seg[2])/2;

			}else{
				
				estimate_speed = estimate_seg[1];
			}
			
			estimate_hits = estimate_seg[4];
		}
		
			// override any estimates < 5K to be OK ones as there's little point in recording negative
			// values lower than this
		
		if ( estimate_speed < 5*1024 ){
			
			estimate_var = VARIANCE_GOOD_VALUE;
			
				// value of 0 means unlimited
			
			if ( estimate_speed <= 0 ){
				
				estimate_speed = 1;
			}
		}
		
		limitEstimate result = 
			new limitEstimate(
					estimate_speed,
					SpeedManagerLimitEstimate.TYPE_ESTIMATED,
					convertMetricToRating( estimate_var ),
					estimate_hits, 
					SystemTime.getCurrentTime(),
					(int[][])segments.toArray(new int[segments.size()][]));
		
		return( result );
	}
	
	public synchronized double
	getCurrentMetricRating()
	{
		if ( ping_count == 0 ){
			
			return( 0 );
		}
		
		int	latest_metric = pings[ping_count-1].getMetric();
		
		if ( variance ){
			
			return( convertMetricToRating( latest_metric ));
			
		}else{
		
			return( 0 );
		}
	}
	
	public SpeedManagerLimitEstimate
	getEstimatedUploadCapacityBytesPerSec()
	{
		return UD_capacity[0];
	}
	
	public void
	setEstimatedDownloadCapacityBytesPerSec(
		int		bytes_per_sec,
		float	estimate_type )
	{
		if ( UD_capacity[1].getBytesPerSec() != bytes_per_sec || UD_capacity[1].getEstimateType() != estimate_type ){

			UD_capacity[1].setBytesPerSec( bytes_per_sec );
			UD_capacity[1].setEstimateType( estimate_type );
			
			speed_manager.informDownCapChanged();
		}
	}
	
	public SpeedManagerLimitEstimate
	getEstimatedDownloadCapacityBytesPerSec()
	{
		return UD_capacity[1];
	}
	
	public void
	setEstimatedUploadCapacityBytesPerSec(
		int		bytes_per_sec,
		float	estimate_type )
	{
		if ( UD_capacity[0].getBytesPerSec() != bytes_per_sec || UD_capacity[0].getEstimateType() != estimate_type ){

			UD_capacity[0].setBytesPerSec( bytes_per_sec );
			UD_capacity[0].setEstimateType( estimate_type );
			
			speed_manager.informUpCapChanged();
		}
	}
	
	protected synchronized void
	reset()
	{
		setEstimatedDownloadCapacityBytesPerSec( 0, SpeedManagerLimitEstimate.TYPE_UNKNOWN);
		setEstimatedUploadCapacityBytesPerSec( 0, SpeedManagerLimitEstimate.TYPE_UNKNOWN);
		
		ping_count	= 0;
		regions.clear();

		last_bad_UD[1]	= null;
		last_bad_UDs[1].clear();

		last_bad_UD[0]		= null;
		last_bad_UDs[0].clear();
		
		saveHistory();
	}
	
	protected double
	convertMetricToRating(
		int		metric )
	{
		if ( metric < VARIANCE_GOOD_VALUE ){
			
			return( +1 );
			
		}else if ( metric >= VARIANCE_BAD_VALUE ){
			
			return( -1 );
			
		}else{
			
			double val =  1 - ((double)metric - VARIANCE_GOOD_VALUE )/50;
			
				// sanitize
			
			if ( val < -1 ){
				
				val = -1;
				
			}else if ( val > 1 ){
				
				val = 1;
			}
			
			return( val );
		}
	}
	
	protected String
	getLimitStr(
		List	limits,
		boolean	short_form )
	{
		String	str = "";
		
		if ( limits != null ){

            for (Object limit : limits) {

                str += (str.length() == 0 ? "" : ",");

                limitEstimate l = (limitEstimate) limit;

                if (short_form) {
                    str += getShortString(l);
                } else {
                    str += l.getString();
                }
            }
		}
		
		return( str );
	}
	
	protected String
	getShortString(
		SpeedManagerLimitEstimate l )
	{
		return( DisplayFormatters.formatByteCountToKiBEtcPerSec( l.getBytesPerSec()));
	}
	
	protected void 
	generateEvidence(
		IndentWriter writer ) 
	{
		writer.println( "up_cap=" + UD_capacity[0].getString());
		writer.println( "down_cap=" + UD_capacity[1].getString());
				
		writer.println( "bad_up=" + getLimitStr( last_bad_UDs[0], false ));
		writer.println( "bad_down=" + getLimitStr( last_bad_UDs[1], false ));
		
		if ( best_good_UD[0] != null ){
			writer.println( "best_up=" + best_good_UD[0].getString());
		}
		if ( best_good_UD[1] != null ){
			writer.println( "best_down=" + best_good_UD[1].getString());
		}
	}
	
	public void
	destroy()
	{
		if ( trans ){
			
			speed_manager.destroy( this );
			
		}else{
	
			Debug.out( "Attempt to destroy non-transient mapper!" );
		}
	}
	
	private static class
	pingValue
	{
		private final short	x;
		private final short	y;
		private final short	metric;
		
		protected
		pingValue(
			int		_x,
			int		_y,
			int		_m )
		{
			x		= (short)_x;
			y		= (short)_y;
			metric	= (short)_m;
		}
		
		protected int
		getX()
		{
			return(((int)(x))&0xffff );
		}
		
		protected int
		getY()
		{
			return(((int)(y))&0xffff );
		}
		
		protected int
		getMetric()
		{
			return(((int)(metric))&0xffff );
		}
		
		protected String
		getString()
		{
			return("x=" + getX()+",y=" + getY() +",m=" + getMetric());
		}
	}

	private static class
	region
		implements SpeedManagerPingZone
	{
		private short	x1;
		private short	y1;
		private short	x2;
		private short	y2;
		private final short	metric;
		
		protected
		region(
			pingValue		p1,
			pingValue		p2 )
		{
			x1 = (short)p1.getX();
			y1 = (short)p1.getY();
			x2 = (short)p2.getX();
			y2 = (short)p2.getY();
			
			if ( x2 < x1 ){
				short t = x1;
				x1 = x2;
				x2 = t;
			}
			if ( y2 < y1 ){
				short t = y1;
				y1 = y2;
				y2 = t;
			}
			metric = (short)((p1.getMetric()+p2.getMetric())/2);
		}
		
		public int
		getX1()
		{
			return( x1 & 0x0000ffff );
		}
		
		public int
		getY1()
		{
			return( y1 & 0x0000ffff );
		}
		
		public int
		getX2()
		{
			return( x2 & 0x0000ffff );
		}
		
		public int
		getY2()
		{
			return( y2 & 0x0000ffff );
		}
					
		public int
		getUploadStartBytesPerSec()
		{
			return( getX1()*SPEED_DIVISOR );
		}
		
		public int
		getUploadEndBytesPerSec()
		{
			return( getX2()*SPEED_DIVISOR + (SPEED_DIVISOR-1));
		}
		
		public int
		getDownloadStartBytesPerSec()
		{
			return( getY1()*SPEED_DIVISOR );
		}
		
		public int
		getDownloadEndBytesPerSec()
		{
			return( getY2()*SPEED_DIVISOR + (SPEED_DIVISOR-1));
		}
		
		public int
		getMetric()
		{
			return( metric & 0x0000ffff );

		}
					
		public String
		getString()
		{				
			return( "x="+getX1() + ",y="+getY1()+",w=" + (getX2()-getX1()+1) +",h=" + (getY2()-getY1()+1));
		}
	}
	
	private static class
	limitEstimate
		implements SpeedManagerLimitEstimate, Cloneable
	{
		private int		speed;
		private float	estimate_type;
		private float	metric_rating;
		private final long	when;
		private final int		hits;
		
		private final int[][]	segs;
		
		protected
		limitEstimate(
			int			_speed,
			double		_estimate_type,
			double		_metric_rating,
			int			_hits,
			long		_when,
			int[][]		_segs )
		{
			speed				= _speed;
			estimate_type		= (float)_estimate_type;
			metric_rating		= (float)_metric_rating;
			hits				= _hits;
			when				= _when;
			segs				= _segs;
			
				// sanitize
			
			if ( metric_rating < -1 ){
				
				metric_rating = -1;
				
			}else if ( metric_rating > 1 ){
				
				metric_rating = 1;
			}
		}
		
		public int
		getBytesPerSec()
		{
			return( speed );
		}
		
		protected void
		setBytesPerSec(
			int		s )
		{
			speed	= s;
		}
		
		public float
		getEstimateType()
		{
			return( estimate_type );
		}
		
		public void
		setEstimateType(
			float	et )
		{
			estimate_type = et;
		}
		
		public float
		getMetricRating()
		{
			return( metric_rating );
		}
		
		protected void
		setMetricRating(
			float	mr )
		{
			metric_rating	= mr;
		}
		
		public int[][]
		getSegments()
		{
			return( segs );
		}
		
		protected int
		getHits()
		{
			return( hits );
		}
		
		public long
		getWhen()
		{
			return( when );
		}
		
		public limitEstimate
		getClone()
		{
			try{
				return((limitEstimate)clone());
				
			}catch( Throwable e ){
								
				return( null );
			}
		}
		
		public String
		getString()
		{
			return( "speed=" + DisplayFormatters.formatByteCountToKiBEtc( speed )+
					",metric=" + metric_rating + ",segs=" + segs.length + ",hits=" + hits + ",when=" + when );
		}
	}
	
	
	public static void
	main(
		String[]	args )
	{
		SpeedManagerPingMapperImpl pm = new SpeedManagerPingMapperImpl( null, "test", 100, true, false );
		
		Random rand = new Random();
		
		int[][] phases = { 
				{ 50, 0, 100000, 50 },
				{ 50, 100000, 200000, 200 },
				{ 50, 50000, 50000, 200 },
				{ 50, 0, 100000, 50 },

		};
		
		for (int i=0;i<phases.length;i++){
			
			int[]	phase = phases[i];
			
			System.out.println( "**** phase " + i );
			
			for (int j=0;j<phase[0];j++){
			
				int	x_base 	= phase[1];
				int	x_var	= phase[2];
				int r = phase[3];
				
				pm.addPing( x_base + rand.nextInt( x_var ), x_base + rand.nextInt( x_var ), rand.nextInt( r ), false);
			
				SpeedManagerLimitEstimate up 	= pm.getEstimatedUploadLimit( false );
				SpeedManagerLimitEstimate down 	= pm.getEstimatedDownloadLimit( false );
				
				if ( up != null && down != null ){
					
					System.out.println( up.getString() + "," + down.getString());
				}
			}
		}
	}
}