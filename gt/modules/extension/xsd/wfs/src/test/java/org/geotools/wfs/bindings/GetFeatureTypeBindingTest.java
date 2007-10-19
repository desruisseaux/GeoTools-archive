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

import net.opengis.wfs.GetFeatureType;
import org.w3c.dom.Document;
import java.math.BigInteger;
import org.geotools.wfs.WFS;
import org.geotools.wfs.WFSTestSupport;
import org.geotools.xml.Binding;


public class GetFeatureTypeBindingTest extends WFSTestSupport {
    public void testType() throws Exception {
        assertEquals(GetFeatureType.class, binding(WFS.GetFeatureType).getType());
    }

    public void testExecutionMode() throws Exception {
        assertEquals(Binding.OVERRIDE, binding(WFS.GetFeatureType).getExecutionMode());
    }

    public void testEncode() throws Exception {
        GetFeatureType getFeature = factory.createGetFeatureType();
        getFeature.setHandle("handle");
        getFeature.setMaxFeatures(BigInteger.valueOf(10));
        getFeature.getQuery().add(factory.createQueryType());
        getFeature.getQuery().add(factory.createQueryType());

        Document dom = encode(getFeature, WFS.GetFeature);
        assertEquals("handle", dom.getDocumentElement().getAttribute("handle"));
        assertEquals("10", dom.getDocumentElement().getAttribute("maxFeatures"));
        assertEquals(2, getElementsByQName(dom, WFS.Query).getLength());
    }
}
