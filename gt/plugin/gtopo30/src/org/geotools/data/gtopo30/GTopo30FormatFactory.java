/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
/*
 * Created on Apr 13, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.gtopo30;

import org.geotools.data.coverage.grid.GridFormatFactorySpi;
import org.opengis.coverage.grid.Format;
import java.util.Collections;
import java.util.Map;


/**
 * DOCUMENT ME!
 *
 * @author giannecchini TODO To change the template for this generated type
 *         comment go to Window - Preferences - Java - Code Style - Code
 *         Templates
 */
public class GTopo30FormatFactory implements GridFormatFactorySpi {
    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridFormatFactorySpi#createFormat()
     */
    public Format createFormat() {
        // TODO Auto-generated method stub
        return new GTopo30Format();
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridFormatFactorySpi#isAvailable()
     */
    public boolean isAvailable() {
        // TODO Auto-generated method stub
        return true;
    }

    /* (non-Javadoc)
     * @see org.geotools.factory.Factory#getImplementationHints()
     */
    public Map getImplementationHints() {
        // TODO Auto-generated method stub
        return Collections.EMPTY_MAP;
    }
}
