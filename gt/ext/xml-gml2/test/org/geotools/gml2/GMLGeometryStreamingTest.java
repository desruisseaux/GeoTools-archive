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
package org.geotools.gml2;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import junit.framework.TestCase;
import org.geotools.xml.Configuration;
import org.geotools.xml.StreamingParser;


public class GMLGeometryStreamingTest extends TestCase {
    StreamingParser parser;

    protected void setUp() throws Exception {
        Configuration configuration = new GMLConfiguration();
        parser = new StreamingParser(configuration,
                getClass().getResourceAsStream("geometry.xml"), "/child::*");
    }

    public void test() throws Exception {
        Object o = parser.parse();
        assertNotNull(o);
        assertTrue(o instanceof Point);

        o = parser.parse();
        assertNotNull(o);
        assertTrue(o instanceof LineString);

        o = parser.parse();
        assertNotNull(o);
        assertTrue(o instanceof Polygon);

        o = parser.parse();
        assertNull(o);
    }
}
