/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.gce.arcgrid;

import org.geotools.resources.TestData;
import java.net.URL;


/**
 * DOCUMENT ME!
 *
 * @author Christiaan ten Klooster
 * @source $URL$
 */
public class ArcGridHeaderTest extends TestCaseSupport {
    public ArcGridHeaderTest(String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite(ArcGridHeaderTest.class));
    }

    public void testHeaderSource() throws Exception {
        URL url = TestData.getResource(this, "ArcGrid.asc");
        ArcGridRaster header = new ArcGridRaster(url);

        header.parseHeader();
        assertEquals("ncols", header.getNCols(), 233);
        assertEquals("nrows", header.getNRows(), 3);
        assertEquals("xllcorner", header.getXlCorner(), 122222.0, 0);
        assertEquals("yllcorner", header.getYlCorner(), 45001.0, 0);
        assertEquals("cellsize", header.getCellSize(), 250.0, 0);
        assertEquals("NODATA_value", header.getNoData(), 1.70141E38, 0);
    }
}
