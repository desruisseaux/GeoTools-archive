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

import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDFactory;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDSchemaLocationResolver;
import org.picocontainer.defaults.DefaultPicoContainer;
import javax.xml.namespace.QName;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.gml2.FeatureTypeCache;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;
import org.geotools.xml.SchemaIndex;
import org.geotools.xml.Schemas;
import org.geotools.xml.impl.ElementImpl;
import org.geotools.xml.impl.SchemaIndexImpl;
import org.geotools.xs.bindings.XS;


public class GMLAbstractFeatureTypeBindingTest extends AbstractGMLBindingTest {
    XSDSchema mySchema;
    SchemaIndex myIndex;

    protected void setUp() throws Exception {
        super.setUp();

        String loc = getClass().getResource("myFeature.xsd").toString();
        mySchema = Schemas.parse(loc, null,
                new XSDSchemaLocationResolver[] { new GMLSchemaLocationResolver() });
        container = new DefaultPicoContainer();

        container.registerComponentImplementation(FeatureTypeCache.class);

        container.registerComponentImplementation(GMLAbstractFeatureTypeBinding.class);
        myIndex = new SchemaIndexImpl(new XSDSchema[] { mySchema });
    }

    public void testSimpleFeature() throws Exception {
        QName qName = new QName("http://www.geotools.org/my", "MyFeatureType");

        XSDElementDeclaration decl = XSDFactory.eINSTANCE.createXSDElementDeclaration();
        decl.setName("myFeature");
        decl.setTargetNamespace("http://www.geotools.org/my");
        decl.setTypeDefinition(myIndex.getComplexTypeDefinition(qName));

        ElementInstance feature = new ElementImpl(decl);
        feature.setName("myFeature");
        feature.setNamespace("http://www.geotools.org/my");

        Node node = createNode(feature,
                new ElementInstance[] {
                    createElement("http://www.geotools.org/my", "geom", GML.POINTPROPERTYTYPE, null),
                    createElement("http://www.geotools.org/my", "count", XS.INTEGER, null)
                },
                new Object[] { new GeometryFactory().createPoint(new Coordinate(1, 1)), new Integer(
                        5) }, null, null);

        GMLAbstractFeatureTypeBinding s = (GMLAbstractFeatureTypeBinding) container
            .getComponentInstanceOfType(GMLAbstractFeatureTypeBinding.class);

        Feature f = (Feature) s.parse(feature, node, null);
        assertNotNull(f);

        FeatureType fType = f.getFeatureType();
        assertNotNull(fType.getAttributeType("geom"));
        assertNotNull(fType.getAttributeType("count"));

        assertEquals(((Point) f.getAttribute("geom")).getCoordinate(), new Coordinate(1, 1));
        assertEquals((Integer) f.getAttribute("count"), new Integer(5));
    }
}
