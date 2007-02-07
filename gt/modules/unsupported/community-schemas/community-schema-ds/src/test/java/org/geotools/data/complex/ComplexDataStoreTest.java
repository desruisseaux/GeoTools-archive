package org.geotools.data.complex;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.complex.config.ComplexDataStoreConfigurator;
import org.geotools.data.complex.config.ComplexDataStoreDTO;
import org.geotools.data.complex.config.XMLConfigDigester;
import org.geotools.data.memory.MemoryDataAccess;
import org.geotools.feature.Descriptors;
import org.geotools.feature.XPath;
import org.geotools.feature.schema.NodeImpl;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterType;
import org.geotools.util.AttributeName;
import org.opengis.feature.Attribute;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureCollection;
import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.schema.OrderedDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

public class ComplexDataStoreTest extends TestCase {

	private final static Logger LOGGER = Logger
			.getLogger(ComplexDataStoreTest.class.getPackage().getName());

	AttributeDescriptor targetDescriptor;
	FeatureType targetType;

	private ComplexDataStore dataStore;

	FeatureTypeMapping mapping;

	protected void setUp() throws Exception {
		super.setUp();
		MemoryDataAccess ds = FeatureTypeMappingTest
				.createWaterSampleTestFeatures();
		targetType = TestData.createComplexWaterSampleType();
		targetDescriptor = new NodeImpl(targetType);
		
		List mappings = TestData.createMappingsColumnsAndValues(ds);

		FeatureSource source = ds
				.getFeatureSource(TestData.WATERSAMPLE_TYPENAME);
		mapping = new FeatureTypeMapping(source, targetDescriptor, mappings, null);

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
		assertEquals(name(targetType), typeNames[0]);
	}

	/*
	 * Test method for
	 * 'org.geotools.data.complex.ComplexDataStore.getSchema(String)'
	 */
	public void testGetSchema() throws IOException {
		FeatureType schema = dataStore.getSchema(name(targetType));
		assertNotNull(schema);
		assertEquals(targetType, schema);
	}

	public void testGetBounds() throws IOException {
		URL configUrl = getClass().getResource("test-data/roadsegments.xml");
		
		ComplexDataStoreDTO config = new XMLConfigDigester().parse(configUrl);
		
		Set/*<FeatureTypeMapping>*/ mappings = ComplexDataStoreConfigurator.buildMappings(config);
		
		dataStore = new ComplexDataStore(mappings);
		FeatureSource source = dataStore.getFeatureSource("RoadSegmentType");
		FeatureType mappedType = source.getSchema();
		assertNotNull(mappedType.getDefaultGeometry());

		FeatureTypeMapping mapping = (FeatureTypeMapping) mappings.iterator().next();
		FeatureSource mappedSource = mapping.getSource();
		Envelope originalBounds = mappedSource.getBounds(Query.ALL);
		assertNotNull("mapped type bounds are not being fetched",
				originalBounds);
		FeatureSource fs = dataStore.getFeatureSource("RoadSegmentType");
		Envelope bounds = fs.getBounds();
		assertNotNull(bounds);

	}

	/*
	 * Test method for
	 * 'org.geotools.data.complex.ComplexDataStore.getFeatureReader(String)'
	 */
	public void testGetFeatureReader() throws IOException {
		FeatureReader reader = dataStore.getFeatureReader(
			name(targetType), Query.ALL);
		assertNotNull(reader);
		assertEquals(targetType, reader.getFeatureType());
		assertTrue(reader.hasNext());
		Feature complexFeature = reader.next();
		assertNotNull(complexFeature);
		assertEquals(targetType, complexFeature.getType());

		assertNotNull(XPath.get(complexFeature, "measurement[1]"));
		assertNotNull(XPath.get(complexFeature, "measurement[1]/parameter"));
		assertNotNull(XPath.get(complexFeature, "measurement[1]/value"));
		assertNotNull(XPath.get(complexFeature, "measurement[2]/parameter"));
		assertNotNull(XPath.get(complexFeature, "measurement[2]/value"));
		assertNotNull(XPath.get(complexFeature, "measurement[3]/parameter"));
		assertNotNull(XPath.get(complexFeature, "measurement[3]/value"));
	}

	/*
	 * Test method for
	 * 'org.geotools.data.AbstractDataStore.getFeatureSource(String)'
	 */
	public void testGetFeatureSource() throws IOException {
		FeatureSource complexSource = dataStore.getFeatureSource(
			name(targetType));
		assertNotNull(complexSource);
	}

	/*
	 * Test method for
	 * 'org.geotools.data.AbstractDataStore.getFeatureReader(Query,
	 * Transaction)'
	 */
	public void testGetFeatureReaderQuery() throws Exception {
		FilterFactory ff = FilterFactory.createFilterFactory();
		CompareFilter ph = ff.createCompareFilter(FilterType.COMPARE_EQUALS);
		ph.addLeftValue(ff.createAttributeExpression(targetType,
				"sample/measurement[1]/parameter"));
		ph.addRightValue(ff.createLiteralExpression("ph"));

		CompareFilter phValue = ff
				.createCompareFilter(FilterType.COMPARE_LESS_THAN_EQUAL);
		phValue.addLeftValue(ff.createAttributeExpression(targetType,
				"sample/measurement[1]/value"));
		phValue.addRightValue(ff.createLiteralExpression(new Integer(3)));

		Filter filter = ph.and(phValue);

		Query query = new DefaultQuery(name(targetType), filter);
		FeatureSource complexSource = dataStore.getFeatureSource(name(targetType));
		FeatureCollection features = complexSource.getFeatures(query);
		Iterator reader = features.features();

		int count = 0;
		int expectedCount = 4;
		CompareFilter badFilter = ff
				.createCompareFilter(FilterType.COMPARE_GREATER_THAN);
		badFilter.addLeftValue(ff.createAttributeExpression(targetType,
				"sample/measurement[1]/value"));
		badFilter.addRightValue(ff.createLiteralExpression(new Integer(3)));

		while (reader.hasNext()) {
			Feature f = (Feature) reader.next();
			assertNotNull(f);
			assertTrue(filter.contains(f));
			assertFalse(badFilter.contains(f));
			count++;
		}
		assertEquals(expectedCount, count);

		/*
		 * UnmappingFilterVisitor visitor = new UnmappingFilterVisitor(mapping);
		 * visitor.visit(filter);
		 * 
		 * Filter unrolled = visitor.getUnrolledFilter();
		 */
	}

	public void testGroupByFeatureReader() throws Exception {

		LOGGER.info("DATA TEST: testGroupByFeatureReader");

		// dataStore with denormalized wq_ir_results type
		MemoryDataAccess dataStore = TestData
				.createDenormalizedWaterQualityResults();
		// mapping definitions from simple wq_ir_results type to complex wq_plus
		// type
		FeatureTypeMapping mapper = TestData
				.createMappingsGroupByStation(dataStore);

		targetDescriptor = mapper.getTargetFeature();
		targetType = (FeatureType)targetDescriptor.getType();

		Set/*<FeatureTypeMapping>*/ mappings = Collections.singleton(mapper);

		ComplexDataStore complexDataStore = new ComplexDataStore(mappings);

		FeatureSource complexSource = complexDataStore
				.getFeatureSource(name(targetType));
		assertNotNull(complexSource);
		FeatureCollection complexFeatures = complexSource.getFeatures();
		assertNotNull(complexFeatures);

		final int EXPECTED_FEATURE_COUNT = 10;// as results from applying the
		// mappings to the simple
		// FeatureSource

		int featureCount = 0;
		Iterator it = complexFeatures.features();
		while (it.hasNext()) {

			Feature currFeature = (Feature) it.next();
			featureCount++;

			assertNotNull(currFeature);
			LOGGER.info(currFeature.toString());

			// currFeature must have as many "measurement" complex attribute
			// instances as
			// the current iteration number
			// This check relies on MemoryDataStore returning Features in the
			// same order they
			// was inserted
			int expectedMeasurementInstances = featureCount;
			List/*<Attribute>*/ measurements = currFeature
					.getAttributes(new AttributeName("measurement"));
			assertNotNull(measurements);
			try {
				for (Iterator itr = measurements.iterator(); itr.hasNext();) {
					Attribute attribute = (Attribute) itr.next();
					assertNotNull("expected not null id: " + attribute.getID(),
							attribute.getID());
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
	public void testGroupByFeatureReaderRespectsAttributeOrder()
			throws Exception {

		LOGGER.info("DATA TEST: testGroupByFeatureReaderRespectsAttributeOrder");

		// dataStore with denormalized wq_ir_results type
		MemoryDataAccess dataStore = TestData
				.createDenormalizedWaterQualityResults();
		// mapping definitions from simple wq_ir_results type to complex wq_plus
		// type
		FeatureTypeMapping mapper = TestData.createMappingsGroupByStation(dataStore);

		targetDescriptor = mapper.getTargetFeature();
		targetType = (FeatureType)targetDescriptor.getType();

		Set/*<FeatureTypeMapping>*/ mappings = Collections.singleton(mapper);

		ComplexDataStore complexDataStore = new ComplexDataStore(mappings);

		FeatureSource complexSource = complexDataStore
				.getFeatureSource(name(targetType));
		assertNotNull(complexSource);
		FeatureCollection complexFeatures = complexSource.getFeatures();
		assertNotNull(complexFeatures);

		Iterator it = complexFeatures.features();
		Feature currFeature = (Feature) it.next();
		List/*<? extends Descriptor>*/ sequence = ((OrderedDescriptor) targetType
				.getDescriptor()).sequence();
		List/*<Attribute>*/ atts = currFeature.getAttributes();
		int idx = 0;
		for (Iterator itr = sequence.iterator(); itr.hasNext();) {
			Descriptor d = (Descriptor) itr.next();
			AttributeDescriptor node = (AttributeDescriptor) d;
			AttributeType attType = node.getType();
			Attribute attribute = (Attribute) atts.get(idx);
			String msg = "Expected " + attType.getName()
					+ " at index " + idx + " but got "
					+ attribute.getType().getName();
			assertEquals(msg, attType, attribute.getType());
			idx++;
		}
		LOGGER.info(currFeature.toString());
	}

	/**
	 * Loads config from an xml config file which uses a property datastore as
	 * source of features.
	 * 
	 * @throws IOException
	 */
	public void testWithConfig() throws Exception {
		URL configUrl = getClass().getResource("test-data/roadsegments.xml");
		
		ComplexDataStoreDTO config = new XMLConfigDigester().parse(configUrl);
		
		Set/*<FeatureTypeMapping>*/ mappings = ComplexDataStoreConfigurator.buildMappings(config); 
		
		dataStore = new ComplexDataStore(mappings);
		FeatureSource source = dataStore.getFeatureSource("RoadSegmentType");

		AttributeDescriptor node;

		FeatureType type = source.getSchema();
		Descriptor scheme = type.getDescriptor();


		node = Descriptors.node(scheme, "the_geom");
		assertNotNull(node);
		assertEquals("LineStringPropertyType", node.getType().getName().getLocalPart());

		assertNotNull(type.getDefaultGeometry());
		assertEquals(node.getType(), type.getDefaultGeometry());;
		
		assertNotNull(type.type("name"));
		assertNotNull(type.type("fromToNodes"));

		ComplexType fromToNodes = (ComplexType) type.type("fromToNodes");
		assertFalse(fromToNodes.isNillable().booleanValue());
		assertTrue(fromToNodes.isIdentified());
		//assertEquals(2, fromToNodes.getDescriptor());
		assertNotNull(fromToNodes.type("fromNode"));
		assertEquals(Point.class, fromToNodes.type("fromNode").getBinding());
		assertEquals(Point.class, fromToNodes.type("toNode").getBinding());

		Iterator features = source.getFeatures().features();
		int count = 0;
		final int expectedCount = 5;
		try {
			while (features.hasNext()) {
				Feature f = (Feature) features.next();
				++count;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		assertEquals("feature count", expectedCount, count);
	}
	
	protected String name(FeatureType type) {
		return type.getName().getLocalPart();
	}
}
