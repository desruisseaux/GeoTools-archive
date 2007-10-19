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

import net.opengis.wfs.GetCapabilitiesType;
import org.w3c.dom.Document;
import org.geotools.wfs.WFS;
import org.geotools.wfs.WFSTestSupport;
import org.geotools.xml.Binding;


public class GetCapabilitiesTypeBindingTest extends WFSTestSupport {
    public void testType() throws Exception {
        assertEquals(GetCapabilitiesType.class, binding(WFS.GetCapabilitiesType).getType());
    }

    public void testExecutionMode() throws Exception {
        assertEquals(Binding.OVERRIDE, binding(WFS.GetCapabilitiesType).getExecutionMode());
    }

    public void testEncode() throws Exception {
        GetCapabilitiesType getCaps = factory.createGetCapabilitiesType();
        Document dom = encode(getCaps, WFS.GetCapabilities);
        print(dom);
    }
}
