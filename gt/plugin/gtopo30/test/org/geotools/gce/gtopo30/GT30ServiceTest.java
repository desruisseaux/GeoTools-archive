/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2005, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.gce.gtopo30;

import java.util.Iterator;

import junit.framework.TestCase;

import org.geotools.data.coverage.grid.GridFormatFactorySpi;
import org.geotools.data.coverage.grid.GridFormatFinder;


/**
 * DOCUMENT ME!
 *
 * @author Simone Giannecchini
 */
public class GT30ServiceTest extends TestCase {
    public GT30ServiceTest() {
        super();

        // TODO Auto-generated constructor stub
    }

    /**
     * DOCUMENT ME!
     *
     * @param arg0
     */
    public GT30ServiceTest(String arg0) {
        super(arg0);

        // TODO Auto-generated constructor stub
    }

    public void testIsAvailable() {
        Iterator list = GridFormatFinder.getAvailableFormats();
        boolean found = false;

        while (list.hasNext()) {
            GridFormatFactorySpi fac = (GridFormatFactorySpi) list.next();

            if (fac instanceof GTopo30FormatFactory) {
                found = true;

                break;
            }
        }

        assertTrue("GTopo30FormatFactory not registered", found);
    }
}
