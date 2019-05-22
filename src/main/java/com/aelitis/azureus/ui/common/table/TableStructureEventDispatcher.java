/**
 * File    : TableStructureEventDispatcher.java
 * Created : 27 nov. 2003
 * By      : Olivier
 *
 * Copyright (C) Azureus Software, Inc, All Rights Reserved.
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

package com.aelitis.azureus.ui.common.table;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.gudy.azureus2.core3.util.AEMonitor;
import org.gudy.azureus2.core3.util.Debug;

/**
 * @author Olivier
 *
 */
public class TableStructureEventDispatcher implements
		TableStructureModificationListener
{

	private static Map<String, TableStructureEventDispatcher> instances = new HashMap<>();

	private static AEMonitor class_mon = new AEMonitor(
			"TableStructureEventDispatcher:class");

	private List listeners;

	private AEMonitor listeners_mon = new AEMonitor(
			"TableStructureEventDispatcher:L");

	/**
	 * 
	 */
	private TableStructureEventDispatcher() {
		listeners = new CopyOnWriteArrayList();
	}

	public static TableStructureEventDispatcher getInstance(String tableID) {
		try {
			class_mon.enter();

			TableStructureEventDispatcher instance = instances.get(tableID);
			if (instance == null) {
				instance = new TableStructureEventDispatcher();
				instances.put(tableID, instance);
			}
			return instance;
		} finally {

			class_mon.exit();
		}
	}

	public void addListener(TableStructureModificationListener listener) {
		try {
			listeners_mon.enter();

			if (!listeners.contains(listener)) {
				listeners.add(listener);
			}

		} finally {

			listeners_mon.exit();
		}
	}

	public void removeListener(TableStructureModificationListener listener) {
		try {
			listeners_mon.enter();

			listeners.remove(listener);
		} finally {

			listeners_mon.exit();
		}
	}

	public void tableStructureChanged( boolean columnAddedOrRemoved, Class forPluginDataSourceType ) {

        for (Object listener1 : listeners) {
            TableStructureModificationListener listener = (TableStructureModificationListener) listener1;
            try {
                listener.tableStructureChanged(columnAddedOrRemoved, forPluginDataSourceType);
            } catch (Throwable e) {
                Debug.printStackTrace(e);
            }
        }
	}

	public void columnSizeChanged(TableColumnCore tableColumn, int diff) {
        for (Object listener1 : listeners) {
            TableStructureModificationListener listener = (TableStructureModificationListener) listener1;
            listener.columnSizeChanged(tableColumn, diff);
        }
	}

	public void columnInvalidate(TableColumnCore tableColumn) {

        for (Object listener1 : listeners) {
            TableStructureModificationListener listener = (TableStructureModificationListener) listener1;
            listener.columnInvalidate(tableColumn);
        }

	}

	public void cellInvalidate(TableColumnCore tableColumn, Object data_source) {

        for (Object listener1 : listeners) {
            TableStructureModificationListener listener = (TableStructureModificationListener) listener1;
            listener.cellInvalidate(tableColumn, data_source);
        }

	}

	
	public void columnOrderChanged(int[] iPositions) {

        for (Object listener1 : listeners) {
            TableStructureModificationListener listener = (TableStructureModificationListener) listener1;
            listener.columnOrderChanged(iPositions);
        }
	}
}
