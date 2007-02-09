package org.geotools.data.complex;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.geotools.data.feature.FeatureSource2;
import org.geotools.data.feature.memory.MemoryDataAccess;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.iso.simple.SimpleFeatureFactoryImpl;
import org.geotools.feature.iso.simple.SimpleTypeBuilder;
import org.geotools.feature.iso.simple.SimpleTypeFactoryImpl;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.simple.SimpleTypeFactory;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.TypeName;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;

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

		final FeatureSource2 wsSource = (FeatureSource2) simpleStore
				.access(TestData.WATERSAMPLE_TYPENAME);

		final FeatureType sourceType = (FeatureType) wsSource.describe();

		final FeatureType targetType = TestData.createComplexWaterSampleType();
		final TypeName targetName = targetType.getName();

		final Expression fidMappingExpression;
		FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);

		Expression prefix = ff.literal("_");
		Expression ph = ff.property("ph");
		Expression dot = ff.literal(".");
		Expression temp = ff.property("temp");

		Function concat1 = ff.function("strConcat", prefix, ph);

		Function concat2 = ff.function("strConcat", dot, temp);

		Function concat3 = ff.function("strConcat", concat1, concat2);

		fidMappingExpression = concat3;

		List mappings = TestData
				.createMappingsColumnsAndValues(this.simpleStore);

		Map/* <String, Expression> */idExpressions = new HashMap();
		idExpressions.put(targetName.getLocalPart(), fidMappingExpression);
		FeatureTypeMapping mapper = new FeatureTypeMapping(wsSource,
				targetName, mappings, idExpressions);

		Collection sourceFeatures = wsSource.content();
		for (Iterator it = sourceFeatures.iterator(); it.hasNext();) {
			SimpleFeature sourceFeature = (SimpleFeature) it.next();
			// TODO
		}
	}

	public void testMappingColumnsAndValues() throws Exception {
		final FeatureSource2 wsSource = (FeatureSource2) simpleStore
				.access(TestData.WATERSAMPLE_TYPENAME);
		final FeatureType sourceType = (FeatureType) wsSource.describe();
		final FeatureType targetType = TestData.createComplexWaterSampleType();
		// final AttributeDescriptor targetNode = new NodeImpl(targetType);

		/*
		 * "ph" ---> sample/measurement[1]/parameter watersample/ph --->
		 * sample/measurement[1]/value "temp" --->
		 * sample/measurement[2]/parameter watersample/temp --->
		 * sample/measurement[2]/value "turbidity" --->
		 * sample/measurement[3]/parameter watersample/turbidity --->
		 * sample/measurement[3]/value
		 */

		List mappings = TestData
				.createMappingsColumnsAndValues(this.simpleStore);

		FeatureTypeMapping mapper = new FeatureTypeMapping(wsSource, targetType
				.getName(), mappings, null);

		Collection sourceFeatures = wsSource.content();

		Iterator it = sourceFeatures.iterator();
		// TODO: test something!
	}

	public void testMappingGroupByStation() throws Exception {

		MemoryDataAccess dataStore = TestData
				.createDenormalizedWaterQualityResults();
		FeatureTypeMapping mapper = TestData
				.createMappingsGroupByStation(dataStore);
		assertNotNull(mapper);

		FeatureSource2 simpleSource = (FeatureSource2) dataStore
				.access(TestData.WATERSAMPLE_TYPENAME);
		assertNotNull(simpleSource);

		Collection sourceFeatures = simpleSource.content();
		Iterator sourceIterator = sourceFeatures.iterator();
		assertNotNull(sourceIterator);

		Feature sourceFeature = (Feature) sourceIterator.next();
		assertNotNull(sourceFeature);
		/*
		 * Feature mapped = mapper.map(sourceFeature); assertNotNull(mapped);
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
