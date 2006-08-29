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

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;


public class GMLBoundingShapeTypeBindingTest extends AbstractGMLBindingTest {
    ElementInstance boundingShape;
    ElementInstance box;
    ElementInstance nil;

    protected void setUp() throws Exception {
        super.setUp();

        boundingShape = createElement(GML.NAMESPACE, "myBoundingShape",
                GML.BOUNDINGSHAPETYPE, null);
        box = createElement(GML.NAMESPACE, "Box", GML.BOXTYPE, null);
        nil = createElement(GML.NAMESPACE, "null", GML.NULLTYPE, null);
    }

    public void testWithBox() throws Exception {
        Envelope e = new Envelope();
        e.expandToInclude(1, 2);

        Node node = createNode(boundingShape, new ElementInstance[] { box },
                new Object[] { e }, null, null);

        GMLBoundingShapeTypeBinding s = (GMLBoundingShapeTypeBinding) getBinding(GML.BOUNDINGSHAPETYPE);

        Envelope e1 = (Envelope) s.parse(boundingShape, node, null);
        assertNotNull(e1);

        assertEquals(e1, e);
    }

    public void testWithNull() throws Exception {
        Node node = createNode(boundingShape, new ElementInstance[] { nil },
                new Object[] { "unknown" }, null, null);

        GMLBoundingShapeTypeBinding s = (GMLBoundingShapeTypeBinding) getBinding(GML.BOUNDINGSHAPETYPE);

        Envelope e1 = (Envelope) s.parse(boundingShape, node, null);
        assertNotNull(e1);
        assertTrue(e1.isNull());
    }
}
