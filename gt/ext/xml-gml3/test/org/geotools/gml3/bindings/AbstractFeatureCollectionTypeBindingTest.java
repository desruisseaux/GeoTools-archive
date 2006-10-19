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

import org.w3c.dom.Element;
import java.util.Iterator;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.gml3.GML3TestSupport;
import org.geotools.xml.Configuration;


public class AbstractFeatureCollectionTypeBindingTest extends GML3TestSupport {
    protected Configuration createConfiguration() {
        return new TestConfiguration();
    }

    protected void registerNamespaces(Element root) {
        super.registerNamespaces(root);
        root.setAttribute("xmlns:test", TEST.NAMESPACE);
    }

    public void testFeatureMember() throws Exception {
        Element featureCollection = GML3MockData.element(TEST.TestFeatureCollection, document,
                document);

        Element featureMember = GML3MockData.element(GML.featureMember, document, featureCollection);

        Element feature = GML3MockData.feature(document, featureMember);
        feature.setAttributeNS(GML.NAMESPACE, "id", "fid.1");

        featureMember = GML3MockData.element(GML.featureMember, document, featureCollection);

        feature = GML3MockData.feature(document, featureMember);
        feature.setAttributeNS(GML.NAMESPACE, "id", "fid.2");

        FeatureCollection fc = (FeatureCollection) parse();
        assertNotNull(fc);

        assertEquals(2, fc.size());

        Iterator i = fc.iterator();
        Feature f = (Feature) i.next();
        assertEquals("fid.1", f.getID());

        f = (Feature) i.next();
        assertEquals("fid.2", f.getID());
    }

    public void testFeatureMembers() throws Exception {
        Element featureCollection = GML3MockData.element(TEST.TestFeatureCollection, document,
                document);

        Element featureMember = GML3MockData.element(GML.featureMembers, document, featureCollection);

        Element feature = GML3MockData.feature(document, featureMember);
        feature.setAttributeNS(GML.NAMESPACE, "id", "fid.1");

        feature = GML3MockData.feature(document, featureMember);
        feature.setAttributeNS(GML.NAMESPACE, "id", "fid.2");

        FeatureCollection fc = (FeatureCollection) parse();
        assertNotNull(fc);

        assertEquals(2, fc.size());

        Iterator i = fc.iterator();
        Feature f = (Feature) i.next();
        assertEquals("fid.1", f.getID());

        f = (Feature) i.next();
        assertEquals("fid.2", f.getID());
    }
}
