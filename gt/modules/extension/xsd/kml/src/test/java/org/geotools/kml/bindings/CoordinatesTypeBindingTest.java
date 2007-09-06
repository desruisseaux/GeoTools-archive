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
package org.geotools.kml.bindings;

import com.vividsolutions.jts.geom.Coordinate;
import org.geotools.kml.KML;
import org.geotools.kml.KMLTestSupport;
import org.geotools.xml.Binding;


public class CoordinatesTypeBindingTest extends KMLTestSupport {
    public void testType() {
        assertEquals(Coordinate[].class, binding(KML.CoordinatesType).getType());
    }

    public void testExecutionMode() {
        assertEquals(Binding.AFTER, binding(KML.CoordinatesType).getExecutionMode());
    }

    public void testParse() throws Exception {
        buildDocument("<coordinates>1,1 2,2</coordinates>");

        Coordinate[] c = (Coordinate[]) parse();

        assertEquals(2, c.length);
        assertEquals(new Coordinate(1, 1), c[0]);
        assertEquals(new Coordinate(2, 2), c[1]);

        buildDocument("<coordinates>1,1,1" + " 2,2,2" + " </coordinates>");
        c = (Coordinate[]) parse();

        assertEquals(2, c.length);
        assertEquals(new Coordinate(1, 1), c[0]);
        assertEquals(1d, c[0].z, 0.1);
        assertEquals(new Coordinate(2, 2), c[1]);
        assertEquals(2d, c[1].z, 0.1);
    }
}
