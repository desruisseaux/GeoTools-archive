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
package org.geotools.kml;

import junit.framework.TestCase;
import java.util.Collection;
import org.geotools.feature.Feature;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Symbolizer;
import org.geotools.xml.Parser;
import org.geotools.xml.StreamingParser;


public class KMLParsingTest extends TestCase {
    public void testParse() throws Exception {
        Parser parser = new Parser(new KMLConfiguration());
        Feature f = (Feature) parser.parse(getClass().getResourceAsStream("states.kml"));
        assertNotNull(f);

        assertEquals("topp:states", f.getAttribute("name"));

        Collection placemarks = (Collection) f.getAttribute("Feature");
        assertEquals(49, placemarks.size());
    }

    public void testStream() throws Exception {
        StreamingParser parser = new StreamingParser(new KMLConfiguration(),
                getClass().getResourceAsStream("states.kml"), KML.Placemark);
        int count = 0;
        Feature f = null;

        while ((f = (Feature) parser.parse()) != null) {
            FeatureTypeStyle style = (FeatureTypeStyle) f.getAttribute("Style");
            assertNotNull(style);

            Symbolizer[] syms = style.getRules()[0].getSymbolizers();
            assertEquals(3, syms.length);

            count++;
        }

        assertEquals(49, count);
    }
}
