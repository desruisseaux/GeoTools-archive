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
package org.geotools.sld;

import junit.framework.TestCase;
import org.geotools.styling.StyledLayerDescriptor;
import org.geotools.xml.Parser;


public class SLDTest extends TestCase {
    public void test() throws Exception {
        Parser parser = new Parser(new SLDConfiguration());
        Object o = parser.parse(getClass().getResourceAsStream("example-sld.xml"));

        assertTrue(o instanceof StyledLayerDescriptor);
    }
}
