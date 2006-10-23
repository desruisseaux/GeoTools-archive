/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.gce.arcgrid;

import java.util.Iterator;

import junit.framework.TestCase;

import org.geotools.data.coverage.grid.GridFormatFactorySpi;
import org.geotools.data.coverage.grid.GridFormatFinder;

/**
 * DOCUMENT ME!
 * 
 * @author ian
 * @source $URL$
 */
public class ServiceTest extends TestCase {
	final String TEST_FILE = "ArcGrid.asc";

	public ServiceTest(java.lang.String testName) {
		super(testName);
	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(ServiceTest.class);
	}

	public void testIsAvailable() {
		Iterator list = GridFormatFinder.getAvailableFormats().iterator();
		boolean found = false;

		while (list.hasNext()) {
			GridFormatFactorySpi fac = (GridFormatFactorySpi) list.next();

			if (fac instanceof ArcGridFormatFactory) {
				found = true;

				break;
			}
		}

		assertTrue("ArcGridFormatFactory not registered", found);
	}
}
