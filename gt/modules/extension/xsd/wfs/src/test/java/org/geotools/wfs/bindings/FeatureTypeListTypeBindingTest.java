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

import net.opengis.wfs.FeatureTypeListType;
import org.geotools.wfs.WFS;
import org.geotools.wfs.WFSTestSupport;
import org.geotools.xml.Binding;


public class FeatureTypeListTypeBindingTest extends WFSTestSupport {
    public void testType() throws Exception {
        assertEquals(FeatureTypeListType.class, binding(WFS.FeatureTypeListType).getType());
    }

    public void testExecutionMode() throws Exception {
        assertEquals(Binding.OVERRIDE, binding(WFS.FeatureTypeListType).getExecutionMode());
    }

    public void testParse() throws Exception {
        String xml = "<FeatureTypeList>" + "<Operations/>" + "<FeatureType/>" + "<FeatureType/>"
            + "</FeatureTypeList>";

        buildDocument(xml);

        FeatureTypeListType featureTypeList = (FeatureTypeListType) parse();
        assertNotNull(featureTypeList.getOperations());
        assertEquals(2, featureTypeList.getFeatureType().size());
    }
}
