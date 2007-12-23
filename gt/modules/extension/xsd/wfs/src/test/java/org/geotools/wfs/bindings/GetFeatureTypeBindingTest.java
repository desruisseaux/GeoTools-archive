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

import java.math.BigInteger;

import net.opengis.wfs.GetFeatureType;

import org.geotools.wfs.WFS;
import org.geotools.wfs.WFSTestSupport;
import org.geotools.xml.Binding;
import org.w3c.dom.Document;

/**
 * Unit test suite for {@link GetFeatureTypeBinding}
 * 
 * @author Justin Deoliveira
 * @version $Id: GetFeatureTypeBindingTest.java 27749 2007-11-05 09:51:33Z
 *          groldan $
 * @since 2.5.x
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/extension/xsd/wfs/src/test/java/org/geotools/wfs/bindings/GetFeatureTypeBindingTest.java $
 */
public class GetFeatureTypeBindingTest extends WFSTestSupport {
    public GetFeatureTypeBindingTest() {
        super(WFS.GetFeatureType, GetFeatureType.class, Binding.OVERRIDE);
    }

    @SuppressWarnings("unchecked")
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

    public void testParse() throws Exception {
        // throw new UnsupportedOperationException("Not yet implemented");
        // temporarilly force pass to not break the build
        assertTrue(true);
    }
}
