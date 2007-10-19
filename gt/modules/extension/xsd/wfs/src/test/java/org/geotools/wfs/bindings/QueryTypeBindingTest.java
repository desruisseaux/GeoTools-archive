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

import net.opengis.wfs.QueryType;
import org.w3c.dom.Document;
import java.util.Collections;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.sort.SortOrder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.v1_1.OGC;
import org.geotools.wfs.WFS;
import org.geotools.wfs.WFSTestSupport;
import org.geotools.xml.Binding;


public class QueryTypeBindingTest extends WFSTestSupport {
    public void testType() throws Exception {
        assertEquals(QueryType.class, binding(WFS.QueryType).getType());
    }

    public void testExecutionMode() throws Exception {
        assertEquals(Binding.OVERRIDE, binding(WFS.QueryType).getExecutionMode());
    }

    public void testEncode() throws Exception {
        FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);

        QueryType query = factory.createQueryType();
        query.getPropertyName().add("property1");
        query.getPropertyName().add("property2");

        query.setFilter(ff.id(Collections.singleton(ff.featureId("fid"))));
        query.getSortBy().add(ff.sort("sortProperty", SortOrder.DESCENDING));

        Document dom = encode(query, WFS.Query);

        assertEquals(WFS.Query.getLocalPart(), dom.getDocumentElement().getLocalName());
        assertEquals(2, getElementsByQName(dom, WFS.PropertyName).getLength());
        assertEquals("property1",
            getElementByQName(dom, WFS.PropertyName).getFirstChild().getNodeValue());

        assertNotNull(getElementByQName(dom, OGC.Filter));
        assertNotNull(getElementByQName(dom, OGC.FeatureId));

        assertNotNull(getElementByQName(dom, OGC.SortBy));
    }
}
