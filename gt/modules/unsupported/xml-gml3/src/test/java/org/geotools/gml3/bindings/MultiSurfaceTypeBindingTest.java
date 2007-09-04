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
package org.geotools.gml3.bindings;

import org.w3c.dom.Document;
import org.geotools.gml3.GML;
import org.geotools.gml3.GML3TestSupport;


public class MultiSurfaceTypeBindingTest extends GML3TestSupport {
    public void testEncode() throws Exception {
        Document dom = encode(GML3MockData.multiPolygon(), GML.MultiSurface);
        assertEquals(2,
            dom.getElementsByTagNameNS(GML.NAMESPACE, GML.Polygon.getLocalPart()).getLength());
    }
}
