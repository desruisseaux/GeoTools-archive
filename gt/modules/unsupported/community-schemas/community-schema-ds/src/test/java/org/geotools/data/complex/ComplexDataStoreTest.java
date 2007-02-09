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

import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.Source;
import org.geotools.data.complex.config.ComplexDataStoreConfigurator;
import org.geotools.data.complex.config.ComplexDataStoreDTO;
import org.geotools.data.complex.config.XMLConfigDigester;
import org.geotools.data.feature.FeatureSource2;
import org.geotools.data.feature.memory.MemoryDataAccess;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.iso.Types;
import org.opengis.feature.Attribute;
import org.opengis.feature.Feature;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.TypeName;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

public class ComplexDataStoreTest extends TestCase {

	private final static Logger LOGGER = Logger
			.getLogger(ComplexDataStoreTest.class.getPackage().getName());

	TypeName targetName;

	FeatureType targetType;

	private ComplexDataStore dataStore;

	FeatureTypeMapping mapping;

	protected void setUp() throws Exception {
		super.setUp();
		MemoryDataAccess ds = FeatureTypeMappingTest
				.createWaterSampleTestFeatures();
		targetType = TestData.createComplexWaterSampleType();
		targetName = targetType.getName();

		List mappings = TestData.createMappingsColumnsAndValues(ds);

		TypeName sourceName = TestData.WATERSAMPLE_TYPENAME;
		FeatureSource2 source = (FeatureSource2) ds.access(sourceName);

		mapping = new FeatureTypeMapping(source, targetName, mappings, null);

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
		FeatureType schema = (FeatureType) dataStore.describe(targetName);
		assertNotNull(schema);
		assertEquals(targetType, schema);
	}

	public void testGetBounds() throws IOException {
		final String namespaceUri = "http://online.socialchange.net.au";
		final String localName = "RoadSegmentType";
		final TypeName typeName = new org.geotools.feature.type.TypeName(
				namespaceUri, localName);

		URL configUrl = getClass().getResource("test-data/roadsegments.xml");

		ComplexDataStoreDTO config = new XMLConfigDigester().parse(configUrl);

		Set/* <FeatureTypeMapping> */mappings = ComplexDataStoreConfigurator
				.buildMappings(config);

		dataStore = new ComplexDataStore(mappings);
		FeatureSource2 source = (FeatureSource2) dataStore.access(typeName);

		FeatureType mappedType = (FeatureType) source.describe();
		assertNotNull(mappedType.getDefaultGeometry());

		FeatureTypeMapping mapping = (FeatureTypeMapping) mappings.iterator()
				.next();

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
		Source access = dataStore.access(targetName);
		assertEquals(targetType, access.describe());

		Collection reader = access.content();
		assertNotNull(reader);

		Iterator features = reader.iterator();
		assertTrue(features.hasNext());

		Feature complexFeature = (Feature) features.next();
		assertNotNull(complexFeature);
		assertEquals(targetType, complexFeature.getType());

		org.opengis.filter.FilterFactory ff = CommonFactoryFinder
				.getFilterFactory(null);
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
		assertEquals(targetType, complexSource.describe());
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

		int count = 0;
		int expectedCount = 4;

		Filter badFilter = ff.greater(ff
				.property("sample/measurement[1]/value"), ff
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
		MemoryDataAccess dataStore = TestData
				.createDenormalizedWaterQualityResults();
		// mapping definitions from simple wq_ir_results type to complex wq_plus
		// type
		FeatureTypeMapping mapper = TestData
				.createMappingsGroupByStation(dataStore);

		targetName = mapper.getTargetFeature();

		Set/* <FeatureTypeMapping> */mappings = Collections.singleton(mapper);

		ComplexDataStore complexDataStore = new ComplexDataStore(mappings);

		Source complexSource = complexDataStore.access(targetName);
		assertNotNull(complexSource);

		targetType = (FeatureType) complexSource.describe();
		assertNotNull(targetType);

		Collection complexFeatures = complexSource.content();
		assertNotNull(complexFeatures);

		final int EXPECTED_FEATURE_COUNT = 10;// as results from applying the
		// mappings to the simple
		// FeatureSource

		int featureCount = 0;
		Iterator it = complexFeatures.iterator();
		TypeName measurementName = new org.geotools.feature.type.TypeName(
				"measurement");
		while (it.hasNext()) {

			Feature currFeature = (Feature) it.next();
			featureCount++;

			assertNotNull(currFeature);
			LOGGER.info(currFeature.toString());

			// currFeature must have as many "measurement" complex attribute
			// instances as the current iteration number
			// This check relies on MemoryDataStore returning Features in the
			// same order they was inserted

			int expectedMeasurementInstances = featureCount;

			List/* <Attribute> */measurements = currFeature
					.get(measurementName);

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
	public void testGroupByFeatureReaderRespectsAttributeOrder()
			throws Exception {

		LOGGER
				.info("DATA TEST: testGroupByFeatureReaderRespectsAttributeOrder");

		// dataStore with denormalized wq_ir_results type
		MemoryDataAccess dataStore;
		dataStore = TestData.createDenormalizedWaterQualityResults();
		// mapping definitions from simple wq_ir_results type to complex wq_plus
		// type
		FeatureTypeMapping mapper;
		mapper = TestData.createMappingsGroupByStation(dataStore);

		targetName = mapper.getTargetFeature();

		Set/* <FeatureTypeMapping> */mappings = Collections.singleton(mapper);

		ComplexDataStore complexDataStore = new ComplexDataStore(mappings);
		Source complexSource = complexDataStore.access(targetName);
		assertNotNull(complexSource);

		targetType = (FeatureType) complexSource.describe();

		Collection complexFeatures = complexSource.content();
		assertNotNull(complexFeatures);

		Iterator it = complexFeatures.iterator();
		Feature currFeature = (Feature) it.next();

		Collection/* <? extends StructuralDescriptor> */sequence = targetType
				.getProperties();

		List/* <Attribute> */atts = (List) currFeature.get();
		int idx = 0;
		for (Iterator itr = sequence.iterator(); itr.hasNext();) {
			AttributeDescriptor node = (AttributeDescriptor) itr.next();
			AttributeType attType = node.getType();
			Attribute attribute = (Attribute) atts.get(idx);
			String msg = "Expected " + attType.getName() + " at index " + idx
					+ " but got " + attribute.getType().getName();
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
		final String nsUri = "http://online.socialchange.net.au";
		final String localName = "RoadSegmentType";
		final TypeName typeName = new org.geotools.feature.type.TypeName(nsUri,
				localName);

		final URL configUrl = getClass().getResource(
				"test-data/roadsegments.xml");

		ComplexDataStoreDTO config = new XMLConfigDigester().parse(configUrl);

		Set/* <FeatureTypeMapping> */mappings = ComplexDataStoreConfigurator
				.buildMappings(config);

		dataStore = new ComplexDataStore(mappings);
		Source source = dataStore.access(typeName);

		FeatureType type = (FeatureType) source.describe();

		AttributeDescriptor node;
		node = (AttributeDescriptor) Types.descriptor(type, Types.typeName(
				nsUri, "the_geom"));
		assertNotNull(node);
		assertEquals("LineStringPropertyType", node.getType().getName()
				.getLocalPart());

		assertNotNull(type.getDefaultGeometry());
		assertEquals(node.getType(), type.getDefaultGeometry());

		assertNotNull(Types.descriptor(type, Types.typeName(nsUri, "name")));

		TypeName ftNodeName = Types.typeName(nsUri, "fromToNodes");
		assertNotNull(Types.descriptor(type, ftNodeName));

		AttributeDescriptor descriptor = (AttributeDescriptor) Types
				.descriptor(type, ftNodeName);

		ComplexType fromToNodes = (ComplexType) descriptor.getType();

		assertFalse(descriptor.isNillable());
		assertTrue(fromToNodes.isIdentified());

		TypeName fromNodeName = Types.typeName(nsUri, "fromNode");
		AttributeDescriptor fromNode = (AttributeDescriptor) Types.descriptor(
				fromToNodes, fromNodeName);
		assertNotNull(fromNode);

		TypeName toNodeName = Types.typeName(nsUri, "toNode");
		AttributeDescriptor toNode = (AttributeDescriptor) Types.descriptor(
				fromToNodes, toNodeName);
		assertNotNull(fromNode);

		assertEquals(Point.class, fromNode.getType().getBinding());
		assertEquals(Point.class, toNode.getType().getBinding());

		Iterator features = source.content().iterator();
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
}
