package org.geotools.data.complex;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.data.memory.MemoryDataAccess;
import org.geotools.feature.impl.AttributeFactoryImpl;
import org.geotools.feature.schema.NodeImpl;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FunctionExpression;
import org.opengis.feature.AttributeFactory;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureCollection;
import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;

public class FeatureTypeMappingTest extends TestCase {
	public static final Logger LOGGER = Logger
			.getLogger(FeatureTypeMappingTest.class.getPackage().getName());

	private MemoryDataAccess simpleStore;

	protected void setUp() throws Exception {
		super.setUp();
		simpleStore = createWaterSampleTestFeatures();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		simpleStore = null;
	}

	/**
	 * Test the mapping of fid as <code>"_" +  ph + "." + "temp"</code>
	 * 
	 * @throws Exception
	 */
	public void testFidMapping() throws Exception {

		final FeatureSource wsSource = simpleStore
				.getFeatureSource(TestData.WATERSAMPLE_TYPENAME);
		final FeatureType sourceType = wsSource.getSchema();
		final FeatureType targetType = TestData.createComplexWaterSampleType();
		final AttributeDescriptor targetNode = new NodeImpl(targetType);
		final Expression fidMappingExpression;
		FilterFactory ff = FilterFactory.createFilterFactory();

		Expression prefix = ff.createLiteralExpression("_");
		Expression ph = ff.createAttributeExpression(sourceType, "ph");
		Expression dot = ff.createLiteralExpression(".");
		Expression temp = ff.createAttributeExpression(sourceType, "temp");

		FunctionExpression concat1 = ff.createFunctionExpression("strConcat");
		concat1.setArgs(new Expression[] { prefix, ph });

		FunctionExpression concat2 = ff.createFunctionExpression("strConcat");
		concat2.setArgs(new Expression[] { dot, temp });

		FunctionExpression concat3 = ff.createFunctionExpression("strConcat");
		concat3.setArgs(new Expression[] { concat1, concat2 });

		fidMappingExpression = concat3;

		List mappings = TestData.createMappingsColumnsAndValues(this.simpleStore);

		Map/*<String, Expression>*/idExpressions = new HashMap/*<String, Expression>*/();
		idExpressions.put(targetNode.getName().getLocalPart(), fidMappingExpression);
		FeatureTypeMapping mapper = new FeatureTypeMapping(wsSource,
				targetNode, mappings, idExpressions);

		FeatureCollection sourceFeatures = wsSource.getFeatures();
		for (Iterator it = sourceFeatures.features(); it.hasNext();) {
			SimpleFeature sourceFeature = (SimpleFeature) it.next();
			/*
			Feature mappedFeature = (Feature) mapper.map(sourceFeature);

			String expectedID = "_" + sourceFeature.get("ph") + "."
					+ sourceFeature.get("temp");

			assertEquals(expectedID, mappedFeature.getID());
			*/
		}
	}

	public void testMappingColumnsAndValues() throws Exception {
		final FeatureSource wsSource = simpleStore
				.getFeatureSource(TestData.WATERSAMPLE_TYPENAME);
		final FeatureType sourceType = wsSource.getSchema();
		final FeatureType targetType = TestData.createComplexWaterSampleType();
		final AttributeDescriptor targetNode = new NodeImpl(targetType);
		
		/*
		 * "ph" ---> sample/measurement[1]/parameter watersample/ph --->
		 * sample/measurement[1]/value "temp" --->
		 * sample/measurement[2]/parameter watersample/temp --->
		 * sample/measurement[2]/value "turbidity" --->
		 * sample/measurement[3]/parameter watersample/turbidity --->
		 * sample/measurement[3]/value
		 */

		List mappings = TestData.createMappingsColumnsAndValues(this.simpleStore);
		FeatureTypeMapping mapper = new FeatureTypeMapping(wsSource,
				targetNode, mappings, null);

		FeatureCollection sourceFeatures = wsSource.getFeatures();

		Iterator it = sourceFeatures.features();
		/**commented because mapping occurs only in MappingFeatureReader now.
		while (it.hasNext()) {
			SimpleFeature sourceFeature = (SimpleFeature) it.next();

			ComplexAttribute mappedObject = mapper.map(sourceFeature);
			assertNotNull(mappedObject);
			assertTrue(mappedObject instanceof Feature);
			Feature mappedFeature = (Feature) mappedObject;

			assertEquals(sourceFeature.getID(), mappedFeature.getID());

			Object measurements = XPath
					.get(mappedFeature, "sample/measurement");
			assertTrue(measurements instanceof List);
			assertEquals(3, ((List) measurements).size());

			Attribute att;
			Object expectedValue, obtainedValue;

			att = (Attribute) XPath.get(mappedFeature,
					"sample/measurement[1]/parameter");
			expectedValue = "ph";

			obtainedValue = att.get();
			assertEquals(expectedValue, obtainedValue);

			att = (Attribute) XPath.get(mappedFeature,
					"sample/measurement[1]/value");

			assertEquals(
					Double.valueOf(sourceFeature.get("ph").toString()),
					att.get()
				);

			att = (Attribute) XPath.get(mappedFeature,
					"sample/measurement[2]/parameter");
			assertEquals("temp", att.get());
			att = (Attribute) XPath.get(mappedFeature,
					"sample/measurement[2]/value");
			assertEquals(Double.valueOf(sourceFeature.get("temp")
					.toString()), att.get());

			att = (Attribute) XPath.get(mappedFeature,
					"sample/measurement[3]/parameter");
			assertEquals("turbidity", att.get());
			att = (Attribute) XPath.get(mappedFeature,
					"sample/measurement[3]/value");
			assertEquals(Double.valueOf(sourceFeature.get("turbidity")
					.toString()), att.get());
		}
		*/
	}
	
	
	public void testMappingGroupByStation()throws Exception{
		MemoryDataAccess dataStore = TestData.createDenormalizedWaterQualityResults();
		FeatureTypeMapping mapper = TestData.createMappingsGroupByStation(dataStore);
		assertNotNull(mapper);
		
		FeatureSource simpleSource = dataStore.getFeatureSource(TestData.WATERSAMPLE_TYPENAME);
		assertNotNull(simpleSource);
		
		FeatureCollection sourceFeatures = simpleSource.getFeatures();
		Iterator sourceIterator = sourceFeatures.features();
		assertNotNull(sourceIterator);
		
		Feature sourceFeature = (Feature) sourceIterator.next();
		assertNotNull(sourceFeature);
		/*
		Feature mapped = mapper.map(sourceFeature);
		assertNotNull(mapped);
		*/
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
	public static MemoryDataAccess createWaterSampleTestFeatures()
			throws Exception {
		MemoryDataAccess dataStore = new MemoryDataAccess();
		String typeString = "watersampleid:String,ph:int,temp:int,turbidity:float";
		SimpleFeatureType type = (SimpleFeatureType) DataUtilities.createType(
				TestData.WATERSAMPLE_TYPENAME, typeString);
		AttributeDescriptor attributeDescriptor = new NodeImpl(type);
		dataStore.createSchema(type);

		final int NUM_FEATURES = 10;
		AttributeFactory af = new AttributeFactoryImpl();

		for (int i = 0; i < NUM_FEATURES; i++) {
			SimpleFeature f = af.createSimpleFeature(attributeDescriptor, 
					type.getName().getLocalPart() + "." + i
			);
			f.set("watersampleid", "watersample." + i);
			f.set("ph", new Integer(i));
			f.set("temp", new Integer(10 + i));
			f.set("turbidity", new Float(i));
			dataStore.addFeature(f);
		}

		return dataStore;
	}
}
