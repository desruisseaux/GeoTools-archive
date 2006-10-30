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
package org.geotools.sld.bindings;

import java.awt.Color;
import org.geotools.styling.Fill;


public class SLDFillBindingTest extends SLDTestSupport {
    public void testType() throws Exception {
        assertEquals(Fill.class, new SLDFillBinding(null).getType());
    }

    public void test() throws Exception {
        SLDMockData.fill(document, document);

        Fill fill = (Fill) parse();
        assertNotNull(fill);
        assertEquals(org.geotools.styling.SLD.opacity(fill), 1, 0d);

        Color c = org.geotools.styling.SLD.color(fill.getColor());
        assertEquals(c.getRed(), integer("12"));
        assertEquals(c.getGreen(), integer("34"));
        assertEquals(c.getBlue(), integer("56"));

        c = org.geotools.styling.SLD.color(fill.getBackgroundColor());
        assertEquals(c.getRed(), integer("65"));
        assertEquals(c.getGreen(), integer("43"));
        assertEquals(c.getBlue(), integer("21"));
    }

    public int integer(String hex) {
        int integer = 0;

        for (int i = 0; i < hex.length(); i++) {
            int k = Integer.parseInt(hex.charAt(hex.length() - i - 1) + "");
            integer += (k * Math.pow(16, i));
        }

        return integer;
    }
}
