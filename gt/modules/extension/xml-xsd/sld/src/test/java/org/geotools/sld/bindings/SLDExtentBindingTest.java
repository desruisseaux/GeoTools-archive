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

import org.geotools.styling.Extent;


public class SLDExtentBindingTest extends SLDTestSupport {
    public void testType() throws Exception {
        assertEquals(Extent.class, new SLDExtentBinding(null).getType());
    }

    public void testNormal() throws Exception {
        SLDMockData.extent(document, document);

        Extent extent = (Extent) parse();

        assertNotNull(extent);
        assertEquals("theName", extent.getName());
        assertEquals("theValue", extent.getValue());
    }
}
