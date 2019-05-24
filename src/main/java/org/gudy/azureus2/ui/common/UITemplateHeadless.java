/**
 * Copyright (C) Azureus Software, Inc, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */

package org.gudy.azureus2.ui.common;

import org.gudy.azureus2.ui.common.util.LGLogger2Log4j;

/**
 * @author tobi
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class UITemplateHeadless
        extends UITemplate
        implements IUserInterface {

    /* (non-Javadoc)
     * @see org.gudy.azureus2.ui.common.IUserInterface#init(boolean, boolean)
     */
    public void init(boolean first, boolean others) {
        super.init(first, others);
        if (first)
            LGLogger2Log4j.set();

    }
}