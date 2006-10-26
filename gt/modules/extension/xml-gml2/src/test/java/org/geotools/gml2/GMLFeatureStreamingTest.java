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

import junit.framework.TestCase;
import java.io.InputStream;
import com.vividsolutions.jts.geom.Point;
import org.geotools.feature.Feature;
import org.geotools.xml.StreamingParser;


public class GMLFeatureStreamingTest extends TestCase {
    public void test() throws Exception {
        InputStream in = getClass().getResourceAsStream("feature.xml");
        StreamingParser parser = new StreamingParser(new TestConfiguration(), in, "//TestFeature");

        for (int i = 0; i < 3; i++) {
            Feature f = (Feature) parser.parse();
            assertNotNull(f);

            //assertEquals(i + "", f.getID());
            assertEquals(i, ((Point) f.getDefaultGeometry()).getX(), 0d);
            assertEquals(i, ((Point) f.getDefaultGeometry()).getY(), 0d);
            assertEquals(i, ((Integer) f.getAttribute("count")).intValue());
        }

        assertNull(parser.parse());
        in.close();
    }
}
