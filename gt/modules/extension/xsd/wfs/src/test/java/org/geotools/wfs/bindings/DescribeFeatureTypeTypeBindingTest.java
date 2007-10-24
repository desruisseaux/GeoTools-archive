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
package org.geotools.wfs.bindings;

import net.opengis.wfs.DescribeFeatureTypeType;
import org.w3c.dom.Document;
import org.geotools.wfs.WFS;
import org.geotools.wfs.WFSTestSupport;
import org.geotools.xml.Binding;


public class DescribeFeatureTypeTypeBindingTest extends WFSTestSupport {
    public void testType() throws Exception {
        assertEquals(DescribeFeatureTypeType.class, binding(WFS.DescribeFeatureTypeType).getType());
    }

    public void testExecutionMode() throws Exception {
        assertEquals(Binding.OVERRIDE, binding(WFS.DescribeFeatureTypeType).getExecutionMode());
    }

    public void testEncode() throws Exception {
        DescribeFeatureTypeType dft = factory.createDescribeFeatureTypeType();
        dft.setService("WFS");
        dft.setVersion("1.1.0");
        dft.setOutputFormat("foo");
        dft.setHandle("bar");

        Document dom = encode(dft, WFS.DescribeFeatureType);
        assertEquals("WFS", dom.getDocumentElement().getAttribute("service"));
        assertEquals("1.1.0", dom.getDocumentElement().getAttribute("version"));
        assertEquals("foo", dom.getDocumentElement().getAttribute("outputFormat"));
        assertEquals("bar", dom.getDocumentElement().getAttribute("handle"));
    }
}
