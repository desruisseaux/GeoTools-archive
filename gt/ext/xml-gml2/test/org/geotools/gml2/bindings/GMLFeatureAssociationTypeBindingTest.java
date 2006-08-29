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
package org.geotools.gml2.bindings;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.geotools.feature.Feature;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;


public class GMLFeatureAssociationTypeBindingTest extends AbstractGMLBindingTest {
    ElementInstance featureAssociation;
    ElementInstance feature;

    protected void setUp() throws Exception {
        super.setUp();

        featureAssociation = createElement(GML.NAMESPACE,
                "myFeatureAssociation", GML.FEATUREASSOCIATIONTYPE, null);
        feature = createElement(GML.NAMESPACE, "myFeature",
                GML.ABSTRACTFEATURETYPE, null);
    }

    //	public void testWithFeature() throws Exception {
    //		Feature f = createFeature(
    //			new String[]{"geom","count"},new Class[]{Point.class,Integer.class},
    //			new Object[]{new GeometryFactory().createPoint(new Coordinate(1,1)), new Integer(2)}
    //		);
    //		
    //		Node node = createNode(
    //			featureAssociation,new ElementInstance[]{feature},new Object[]{f},
    //			null,null
    //		);
    //		
    //		GMLFeatureAssociationTypeBinding s = 
    //			(GMLFeatureAssociationTypeBinding)getBinding(GML.FEATUREASSOCIATIONTYPE);
    //		Feature f1 = (Feature) s.parse(featureAssociation,node,null);
    //		assertNotNull(f1);
    //		assertEquals(f1,f);
    //	}
    public void testWithoutFeature() throws Exception {
        Node node = createNode(featureAssociation, null, null, null, null);

        GMLFeatureAssociationTypeBinding s = (GMLFeatureAssociationTypeBinding) getBinding(GML.FEATUREASSOCIATIONTYPE);

        try {
            s.parse(featureAssociation, node, null);
            fail("Parse with no feature should throw an exception");
        } catch (Exception e) {
            //ok
        }
    }
}
