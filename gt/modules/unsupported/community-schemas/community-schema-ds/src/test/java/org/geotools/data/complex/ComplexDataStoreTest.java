package org.geotools.data.complex;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.geotools.data.Source;
import org.geotools.data.complex.config.ComplexDataStoreConfigurator;
import org.geotools.data.complex.config.ComplexDataStoreDTO;
import org.geotools.data.complex.config.XMLConfigDigester;
import org.geotools.data.feature.FeatureSource2;
import org.geotools.data.feature.memory.MemoryDataAccess;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.iso.Types;
import org.geotools.feature.iso.simple.SimpleFeatureFactoryImpl;
import org.geotools.feature.iso.simple.SimpleTypeBuilder;
import org.geotools.feature.iso.simple.SimpleTypeFactoryImpl;
import org.geotools.feature.iso.type.TypeFactoryImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.Attribute;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.simple.SimpleTypeFactory;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.TypeFactory;
import org.opengis.feature.type.TypeName;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.spatialschema.geometry.BoundingBox;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

public class ComplexDataStoreTest extends TestCase {

    private final static Logger LOGGER = Logger.getLogger(ComplexDataStoreTest.class.getPackage()
            .getName());

    Name targetName;

    FeatureType targetType;

    private ComplexDataStore dataStore;

    FeatureTypeMapping mapping;

    protected void setUp() throws Exception {
        super.setUp();
        MemoryDataAccess ds = createWaterSampleTestFeatures();
        targetType = TestData.createComplexWaterSampleType();
        TypeFactory tf = new TypeFactoryImpl();
        AttributeDescriptor targetFeature = tf.createAttributeDescriptor(targetType, targetType
                .getName(), 0, Integer.MAX_VALUE, true);
        targetName = targetFeature.getName();
        List mappings = TestData.createMappingsColumnsAndValues(ds);

        TypeName sourceName = TestData.WATERSAMPLE_TYPENAME;
        FeatureSource2 source = (FeatureSource2) ds.access(sourceName);

        mapping = new FeatureTypeMapping(source, targetFeature, mappings);

        dataStore = new ComplexDataStore(Collections.singleton(mapping));

    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /*
     * Test method for
     * 'org.geotools.data.complex.ComplexDataStore.getTypeNames()'
     */
    public void testGetTypeNames() throws IOException {
        String[] typeNames = dataStore.getTypeNames();
        assertNotNull(typeNames);
        assertEquals(1, typeNames.length);
        assertEquals(targetName.getLocalPart(), typeNames[0]);

        // DataAccess interface:
        List names = dataStore.getNames();
        assertNotNull(names);
        assertEquals(1, names.size());
        assertEquals(targetName, names.get(0));
    }

    /*
     * Test method for
     * 'org.geotools.data.complex.ComplexDataStore.getSchema(String)'
     */
    public void testDescribeType() throws IOException {
        AttributeDescriptor descriptor = (AttributeDescriptor) dataStore.describe(targetName);
        assertNotNull(descriptor);
        assertEquals(targetType, descriptor.getType());
    }

    public void testGetBounds() throws IOException {
        final String namespaceUri = "http://online.socialchange.net.au";
        final String localName = "RoadSegment";
        final Name typeName = new org.geotools.feature.Name(namespaceUri, localName);

        URL configUrl = getClass().getResource("test-data/roadsegments.xml");

        ComplexDataStoreDTO config = new XMLConfigDigester().parse(configUrl);

        Set/* <FeatureTypeMapping> */mappings = ComplexDataStoreConfigurator.buildMappings(config);

        dataStore = new ComplexDataStore(mappings);
        FeatureSource2 source = (FeatureSource2) dataStore.access(typeName);

        AttributeDescriptor describe = (AttributeDescriptor) source.describe();
        FeatureType mappedType = (FeatureType) describe.getType();
        assertNotNull(mappedType.getDefaultGeometry());

        FeatureTypeMapping mapping = (FeatureTypeMapping) mappings.iterator().next();

        FeatureSource2 mappedSource = mapping.getSource();
        ReferencedEnvelope expected = getBounds(mappedSource);
        Envelope actual = getBounds(source);

        assertEquals(expected, actual);

    }

    private ReferencedEnvelope getBounds(FeatureSource2 source) {
        ReferencedEnvelope boundingBox = new ReferencedEnvelope(DefaultGeographicCRS.WGS84);
        Collection features = source.content();
        Iterator iterator = features.iterator();
        while (iterator.hasNext()) {
            Feature f = (Feature) iterator.next();
            BoundingBox bounds = f.getBounds();
            boundingBox.include(bounds);
        }
        return boundingBox;
    }

    /*
     * Test method for
     * 'org.geotools.data.complex.ComplexDataStore.getFeatureReader(String)'
     */
    public void testGetFeatureReader() throws IOException {
        Source access = dataStore.access(targetName);
        Object describe = access.describe();
        assertTrue(describe instanceof AttributeDescriptor);
        assertEquals(targetType, ((AttributeDescriptor) describe).getType());

        Collection reader = access.content();
        assertNotNull(reader);

        Iterator features = reader.iterator();
        assertTrue(features.hasNext());

        Feature complexFeature = (Feature) features.next();
        assertNotNull(complexFeature);
        assertEquals(targetType, complexFeature.getType());

        org.opengis.filter.FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
        PropertyName expr;
        Object value;

        expr = ff.property("measurement[1]");
        value = expr.evaluate(complexFeature);
        assertNotNull(value);

        expr = ff.property("measurement[1]/parameter");
        value = expr.evaluate(complexFeature);
        assertNotNull(value);

        expr = ff.property("measurement[1]/value");
        value = expr.evaluate(complexFeature);
        assertNotNull(value);

        expr = ff.property("measurement[2]/parameter");
        value = expr.evaluate(complexFeature);
        assertNotNull(value);

        expr = ff.property("measurement[2]/value");
        value = expr.evaluate(complexFeature);
        assertNotNull(value);

        expr = ff.property("measurement[3]/parameter");
        value = expr.evaluate(complexFeature);
        assertNotNull(value);

        expr = ff.property("measurement[3]/value");
        value = expr.evaluate(complexFeature);
        assertNotNull(value);

    }

    /*
     * Test method for
     * 'org.geotools.data.AbstractDataStore.getFeatureSource(String)'
     */
    public void testGetFeatureSource() throws IOException {
        Source complexSource = dataStore.access(targetName);
        assertNotNull(complexSource);
        Object describe = complexSource.describe();
        assertTrue(describe instanceof AttributeDescriptor);
        assertEquals(targetType, ((AttributeDescriptor) describe).getType());
    }

    /*
     * Test method for
     * 'org.geotools.data.AbstractDataStore.getFeatureReader(Query,
     * Transaction)'
     */
    public void testGetFeatureReaderQuery() throws Exception {
        FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);

        PropertyName property = ff.property("sample/measurement[1]/parameter");
        Literal literal = ff.literal("ph");
        Filter filterParameter = ff.equals(property, literal);

        property = ff.property("sample/measurement[1]/value");
        literal = ff.literal(new Integer(3));
        Filter filterValue = ff.equals(property, literal);

        Filter filter = ff.and(filterParameter, filterValue);

        Source complexSource = dataStore.access(targetName);
        Collection features = complexSource.content(filter);

        Iterator reader = features.iterator();

        PropertyIsEqualTo equivalentSourceFilter = ff.equals(ff.property("ph"), ff
                .literal(new Integer(3)));
        Collection collection = mapping.getSource().content(equivalentSourceFilter);

        int count = 0;
        int expectedCount = collection.size();

        Filter badFilter = ff.greater(ff.property("sample/measurement[1]/value"), ff
                .literal(new Integer(3)));

        while (reader.hasNext()) {
            Feature f = (Feature) reader.next();
            assertNotNull(f);
            assertTrue(filter.evaluate(f));
            assertFalse(badFilter.evaluate(f));
            count++;
        }
        assertEquals(expectedCount, count);
    }

    public void testGroupByFeatureReader() throws Exception {

        LOGGER.info("DATA TEST: testGroupByFeatureReader");

        // dataStore with denormalized wq_ir_results type
        MemoryDataAccess dataStore = TestData.createDenormalizedWaterQualityResults();
        // mapping definitions from simple wq_ir_results type to complex wq_plus
        // type
        FeatureTypeMapping mapper = TestData.createMappingsGroupByStation(dataStore);

        // for(Iterator it = mapper.getSource().content().iterator();
        // it.hasNext();){
        // SimpleFeature f = (SimpleFeature) it.next();
        // for(int i = 0; i < f.getNumberOfAttributes(); i++){
        // Object o = f.get(i);
        // System.out.print(o + ",\t");
        // }
        // System.out.println("");
        // }

        targetName = mapper.getTargetFeature().getName();

        Set/* <FeatureTypeMapping> */mappings = Collections.singleton(mapper);

        ComplexDataStore complexDataStore = new ComplexDataStore(mappings);

        Source complexSource = complexDataStore.access(targetName);
        assertNotNull(complexSource);

        AttributeDescriptor sourceDescriptor;
        sourceDescriptor = (AttributeDescriptor) complexSource.describe();
        targetType = (FeatureType) sourceDescriptor.getType();
        assertNotNull(targetType);

        Collection complexFeatures = complexSource.content();
        assertNotNull(complexFeatures);

        final int EXPECTED_FEATURE_COUNT = 10;// as results from applying the
        // mappings to the simple
        // FeatureSource

        int featureCount = 0;
        Iterator it = complexFeatures.iterator();
        Name measurementName = new org.geotools.feature.Name("measurement");
        while (it.hasNext()) {

            Feature currFeature = (Feature) it.next();
            featureCount++;

            assertNotNull(currFeature);

            // currFeature must have as many "measurement" complex attribute
            // instances as the current iteration number
            // This check relies on MemoryDataStore returning Features in the
            // same order they was inserted

            int expectedMeasurementInstances = featureCount;

            List/* <Attribute> */measurements = currFeature.get(measurementName);

            assertNotNull(measurements);

            try {
                for (Iterator itr = measurements.iterator(); itr.hasNext();) {
                    Attribute attribute = (Attribute) itr.next();
                    String measurementId = attribute.getID();
                    assertNotNull("expected not null id", measurementId);
                }
                assertEquals(expectedMeasurementInstances, measurements.size());
            } catch (AssertionFailedError e) {
                LOGGER.warning(currFeature.toString());
                throw e;
            }

        }
        assertEquals(EXPECTED_FEATURE_COUNT, featureCount);
    }

    /**
     * Tests that GroupByFeatureReader respects attribute order when creating
     * the mapped Features. That is, mapped attributes appear in the schema
     * declared order.
     * 
     * @throws Exception
     */
    // Commented out since it is no longer needed for the featuresource to
    // produce
    // content in the schema specified order. That has to be handled at encoding
    // time with a schema assisted encoder
    // public void testGroupByFeatureReaderRespectsAttributeOrder() throws
    // Exception {
    //
    // LOGGER.info("DATA TEST: testGroupByFeatureReaderRespectsAttributeOrder");
    //
    // // dataStore with denormalized wq_ir_results type
    // MemoryDataAccess dataStore;
    // dataStore = TestData.createDenormalizedWaterQualityResults();
    // // mapping definitions from simple wq_ir_results type to complex wq_plus
    // // type
    // FeatureTypeMapping mapper;
    // mapper = TestData.createMappingsGroupByStation(dataStore);
    //
    // AttributeDescriptor targetFeature = mapper.getTargetFeature();
    // targetType = (FeatureType) targetFeature.getType();
    // targetName = targetFeature.getName();
    //
    // Set/* <FeatureTypeMapping> */mappings = Collections.singleton(mapper);
    //
    // ComplexDataStore complexDataStore = new ComplexDataStore(mappings);
    // Source complexSource = complexDataStore.access(targetName);
    // assertNotNull(complexSource);
    //
    // Collection complexFeatures = complexSource.content();
    // assertNotNull(complexFeatures);
    //
    // Iterator it = complexFeatures.iterator();
    // Feature currFeature = (Feature) it.next();
    //
    // Collection/* <? extends StructuralDescriptor> */sequence =
    // targetType.getProperties();
    //
    // List/* <Attribute> */atts = (List) currFeature.get();
    // int idx = 0;
    // for (Iterator itr = sequence.iterator(); itr.hasNext();) {
    // AttributeDescriptor node = (AttributeDescriptor) itr.next();
    // AttributeType attType = node.getType();
    // Attribute attribute = (Attribute) atts.get(idx);
    // String msg = "Expected " + attType.getName() + " at index " + idx + " but
    // got "
    // + attribute.getType().getName();
    // assertEquals(msg, attType, attribute.getType());
    // idx++;
    // }
    // LOGGER.info(currFeature.toString());
    // }
    /**
     * Loads config from an xml config file which uses a property datastore as
     * source of features.
     * 
     * @throws IOException
     */
    public void testWithConfig() throws Exception {
        final String nsUri = "http://online.socialchange.net.au";
        final String localName = "RoadSegment";
        final TypeName typeName = new org.geotools.feature.type.TypeName(nsUri, localName);

        final URL configUrl = getClass().getResource("test-data/roadsegments.xml");

        ComplexDataStoreDTO config = new XMLConfigDigester().parse(configUrl);

        Set/* <FeatureTypeMapping> */mappings = ComplexDataStoreConfigurator.buildMappings(config);

        dataStore = new ComplexDataStore(mappings);
        Source source = dataStore.access(typeName);

        AttributeDescriptor sdesc = (AttributeDescriptor) source.describe();
        FeatureType type = (FeatureType) sdesc.getType();

        AttributeDescriptor node;
        node = (AttributeDescriptor) Types.descriptor(type, new org.geotools.feature.Name(nsUri,
                "the_geom"));
        assertNotNull(node);
        assertEquals("LineStringPropertyType", node.getType().getName().getLocalPart());

        assertNotNull(type.getDefaultGeometry());
        assertEquals(node.getType(), type.getDefaultGeometry().getType());

        assertNotNull(Types.descriptor(type, Types.typeName(nsUri, "name")));

        TypeName ftNodeName = Types.typeName(nsUri, "fromToNodes");
        assertNotNull(Types.descriptor(type, ftNodeName));

        AttributeDescriptor descriptor = (AttributeDescriptor) Types.descriptor(type, ftNodeName);

        ComplexType fromToNodes = (ComplexType) descriptor.getType();

        assertFalse(descriptor.isNillable());
        assertTrue(fromToNodes.isIdentified());

        TypeName fromNodeName = Types.typeName(nsUri, "fromNode");
        AttributeDescriptor fromNode = (AttributeDescriptor) Types.descriptor(fromToNodes,
                fromNodeName);
        assertNotNull(fromNode);

        TypeName toNodeName = Types.typeName(nsUri, "toNode");
        AttributeDescriptor toNode = (AttributeDescriptor) Types
                .descriptor(fromToNodes, toNodeName);
        assertNotNull(fromNode);

        assertEquals(Point.class, fromNode.getType().getBinding());
        assertEquals(Point.class, toNode.getType().getBinding());

        Iterator features = source.content().iterator();
        int count = 0;
        final int expectedCount = 5;
        try {
            while (features.hasNext()) {
                Feature f = (Feature) features.next();
                LOGGER.finest(String.valueOf(f));
                ++count;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        assertEquals("feature count", expectedCount, count);
    }

    /**
     * Creates a MemoryDataStore contaning a simple FeatureType with test data
     * for the "Multiple columns could be mapped to a multi-value property"
     * mapping case.
     * <p>
     * The structure of the "WaterSample" FeatureType is as follows: <table>
     * <tr>
     * <th>watersampleid</th>
     * <th>ph</th>
     * <th>temp</th>
     * <th>turbidity</th>
     * </tr>
     * <tr>
     * <td>watersample.1</td>
     * <td>7</td>
     * <td>21</td>
     * <td>0.6</td>
     * </tr>
     * </table>
     * </p>
     */
    public static MemoryDataAccess createWaterSampleTestFeatures() throws Exception {
        MemoryDataAccess dataStore = new MemoryDataAccess();
        SimpleTypeFactory tf = new SimpleTypeFactoryImpl();
        SimpleTypeBuilder tb = new SimpleTypeBuilder(tf);

        tb.setName(TestData.WATERSAMPLE_TYPENAME.getLocalPart());
        tb.addAttribute("watersampleid", String.class);
        tb.addAttribute("ph", Integer.class);
        tb.addAttribute("temp", Integer.class);
        tb.addAttribute("turbidity", Float.class);

        SimpleFeatureType type = tb.feature();

        dataStore.createSchemaInternal(type);

        final int NUM_FEATURES = 10;
        SimpleFeatureFactory af = new SimpleFeatureFactoryImpl();

        for (int i = 0; i < NUM_FEATURES; i++) {
            String fid = type.getName().getLocalPart() + "." + i;
            SimpleFeature f = af.createSimpleFeature(type, fid, null);

            f.set("watersampleid", "watersample." + i);
            f.set("ph", new Integer(i));
            f.set("temp", new Integer(10 + i));
            f.set("turbidity", new Float(i));

            dataStore.addFeatureInternal(f);
        }
        return dataStore;
    }
}
