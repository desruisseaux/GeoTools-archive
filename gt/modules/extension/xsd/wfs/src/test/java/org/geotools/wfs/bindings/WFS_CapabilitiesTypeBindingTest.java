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

import net.opengis.wfs.WFSCapabilitiesType;
import org.geotools.wfs.WFS;
import org.geotools.wfs.WFSTestSupport;
import org.geotools.xml.Binding;


public class WFS_CapabilitiesTypeBindingTest extends WFSTestSupport {
    public void testType() throws Exception {
        assertEquals(WFSCapabilitiesType.class, binding(WFS.WFS_CapabilitiesType).getType());
    }

    public void testExecutionMode() throws Exception {
        assertEquals(Binding.OVERRIDE, binding(WFS.WFS_CapabilitiesType).getExecutionMode());
    }

    public void testParse() throws Exception {
        String xml = "<WFS_Capabilities version=\"1.1.0\">" + "<FeatureTypeList/>"
            + "</WFS_Capabilities>";
        buildDocument(xml);

        WFSCapabilitiesType caps = (WFSCapabilitiesType) parse();
        assertEquals("1.1.0", caps.getVersion());

        assertNotNull(caps.getFeatureTypeList());
    }
}
