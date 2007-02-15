package org.geotools.data.complex.config;

import java.net.URL;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDElementDeclaration;
import org.geotools.feature.Name;
import org.geotools.feature.iso.Types;
import org.geotools.feature.type.TypeName;
import org.geotools.gml3.bindings.GML;
import org.geotools.test.TestData;
import org.geotools.xs.bindings.XS;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class EmfAppSchemaReaderTest extends TestCase {

    /**
     * Namespace URI of parsed types
     */
    private static final String NS_URI = "http://online.socialchange.net.au";

    private EmfAppSchemaReader schemaLoader;

    protected void setUp() throws Exception {
        super.setUp();
        schemaLoader = EmfAppSchemaReader.newInstance();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        schemaLoader = null;
    }

    public void testParseSimpleFeatureType() throws Exception {
        String res = "simpleFeature.xsd";
        URL resource = TestData.getResource(this, res);

        schemaLoader.parse(resource);

        Map parsedTypes = schemaLoader.getTypeRegistry();
        assertNotNull(parsedTypes);

        TypeName typeName = new TypeName(NS_URI, "simpleFeatureType");
        AttributeType type = (AttributeType) parsedTypes.get(typeName);
        assertNotNull(type);
        assertTrue(type.getClass().getName(), type instanceof SimpleFeatureType);
        assertTrue(type.getUserData(EmfAppSchemaReader.EMF_USERDATA_KEY) instanceof XSDComplexTypeDefinition);

        SimpleFeatureType ft = (SimpleFeatureType) type;
        String local = ft.getName().getLocalPart();
        String uri = ft.getName().getNamespaceURI();
        assertEquals("simpleFeatureType", local);
        assertEquals(NS_URI, uri);

        List/* <AttributeType> */attributes = (List) ft.getProperties();
        assertEquals(3, attributes.size());
        AttributeDescriptor descriptor;

        descriptor = (AttributeDescriptor) attributes.get(0);
        Name name = new Name(NS_URI, "the_geom");
        typeName = new TypeName(GML.NAMESPACE, "GeometryPropertyType");
        assertTrue(descriptor.getType() instanceof GeometryType);

        assertSimpleAttribute(descriptor, name, typeName, Geometry.class, 1, 1);

        descriptor = (AttributeDescriptor) attributes.get(1);
        name = new Name(NS_URI, "stringAtt");
        typeName = new TypeName(XS.NAMESPACE, XS.STRING.getLocalPart());

        assertSimpleAttribute(descriptor, name, typeName, String.class, 1, 1);

        descriptor = (AttributeDescriptor) attributes.get(2);
        name = new Name(NS_URI, "intAtt");
        typeName = new TypeName(XS.NAMESPACE, XS.INT.getLocalPart());
        assertSimpleAttribute(descriptor, name, typeName, Integer.class, 1, 1);
    }

    private void assertSimpleAttribute(AttributeDescriptor descriptor,
            org.opengis.feature.type.Name name, org.opengis.feature.type.TypeName typeName,
            Class binding, int minOccurs, int maxOccurs) {
        AttributeType type;
        assertEquals(name, descriptor.getName());
        assertEquals(minOccurs, descriptor.getMinOccurs());
        assertEquals(maxOccurs, descriptor.getMaxOccurs());
        assertTrue(descriptor.getUserData(EmfAppSchemaReader.EMF_USERDATA_KEY) instanceof XSDElementDeclaration);

        type = descriptor.getType();
        assertNotNull(type);
        assertFalse(type instanceof ComplexType);
        assertEquals(typeName, type.getName());
        assertEquals(binding, type.getBinding());
        // they're prebuilt types, does not contains the emf information
        // assertTrue(type.getUserData(EmfAppSchemaReader.EMF_USERDATA_KEY)
        // instanceof XSDTypeDefinition);
    }

    public void testComplexFeatureType() throws Exception {
        String res = "complexFeature.xsd";
        URL resource = TestData.getResource(this, res);
        schemaLoader.parse(resource);

        Map registry = schemaLoader.getTypeRegistry();
        assertNotNull(registry);

        TypeName typeName = new TypeName(NS_URI, "wq_plus_Type");
        AttributeType type = (AttributeType) registry.get(typeName);
        assertTrue(type instanceof FeatureType);
        assertFalse(type instanceof SimpleFeatureType);
        assertEquals(typeName, type.getName());
        assertTrue(type.getUserData(EmfAppSchemaReader.EMF_USERDATA_KEY) instanceof XSDComplexTypeDefinition);

        FeatureType wq_plus_Type = (FeatureType) type;

        assertNotNull(wq_plus_Type.getDefaultGeometry());
        assertNotNull(wq_plus_Type.getSuper());
        typeName = new TypeName(GML.NAMESPACE, GML.AbstractFeatureType.getLocalPart());
        assertEquals(typeName, wq_plus_Type.getSuper().getName());
        assertNotNull(wq_plus_Type.getProperties());
        assertEquals(3, wq_plus_Type.getProperties().size());

        Name name = new Name(NS_URI, "wq_plus");
        AttributeDescriptor wqPlusDescriptor = (AttributeDescriptor) registry.get(name);
        assertNotNull(wqPlusDescriptor);
        assertEquals(name, wqPlusDescriptor.getName());
        assertSame(wq_plus_Type, wqPlusDescriptor.getType());
        assertTrue(wqPlusDescriptor.getUserData(EmfAppSchemaReader.EMF_USERDATA_KEY) instanceof XSDElementDeclaration);

        typeName = new TypeName(NS_URI, "measurementType");
        type = (AttributeType) registry.get(typeName);
        assertTrue(type instanceof ComplexType);
        assertFalse(type instanceof FeatureType);
        assertTrue(type.getUserData(EmfAppSchemaReader.EMF_USERDATA_KEY) instanceof XSDComplexTypeDefinition);

        ComplexType measurementType = (ComplexType) type;
        assertEquals(typeName, measurementType.getName());
        assertTrue(measurementType.isIdentified());
        assertFalse(measurementType.isAbstract());
        assertEquals(2, measurementType.getProperties().size());

        name = new Name(NS_URI, "measurement");
        AttributeDescriptor descriptor;
        descriptor = (AttributeDescriptor) Types.descriptor(wq_plus_Type, name);
        assertNotNull(descriptor);
        assertEquals(name, descriptor.getName());
        assertNotNull(descriptor.getType());
        assertSame(measurementType, descriptor.getType());
        assertEquals(0, descriptor.getMinOccurs());
        assertEquals(Integer.MAX_VALUE, descriptor.getMaxOccurs());
        assertTrue(descriptor.getUserData(EmfAppSchemaReader.EMF_USERDATA_KEY) instanceof XSDElementDeclaration);

        name = new Name(NS_URI, "result");
        descriptor = (AttributeDescriptor) Types.descriptor(measurementType, name);
        typeName = new TypeName(XS.NAMESPACE, XS.FLOAT.getLocalPart());
        assertSimpleAttribute(descriptor, name, typeName, Float.class, 1, 1);

        name = new Name(NS_URI, "determinand_description");
        descriptor = (AttributeDescriptor) Types.descriptor(measurementType, name);
        typeName = new TypeName(XS.NAMESPACE, XS.STRING.getLocalPart());
        assertSimpleAttribute(descriptor, name, typeName, String.class, 1, 1);

        name = new Name(NS_URI, "the_geom");
        descriptor = (AttributeDescriptor) Types.descriptor(wq_plus_Type, name);
        typeName = new TypeName(GML.NAMESPACE, GML.PointPropertyType.getLocalPart());
        assertSimpleAttribute(descriptor, name, typeName, Point.class, 1, 1);

        name = new Name(NS_URI, "sitename");
        descriptor = (AttributeDescriptor) Types.descriptor(wq_plus_Type, name);
        typeName = new TypeName(XS.NAMESPACE, XS.STRING.getLocalPart());
        assertSimpleAttribute(descriptor, name, typeName, String.class, 1, Integer.MAX_VALUE);

    }

}
