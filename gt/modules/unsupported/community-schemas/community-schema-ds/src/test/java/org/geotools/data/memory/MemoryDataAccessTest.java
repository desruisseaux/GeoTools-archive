package org.geotools.data.memory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.geotools.data.ComplexTestData;
import org.geotools.data.Query;
import org.geotools.data.Source;
import org.geotools.data.feature.FeatureSource2;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.Name;
import org.geotools.feature.iso.AttributeBuilder;
import org.geotools.feature.iso.AttributeFactoryImpl;
import org.geotools.feature.iso.FeatureImpl;
import org.geotools.feature.iso.TypeBuilder;
import org.geotools.feature.iso.Types;
import org.geotools.feature.iso.simple.SimpleTypeFactoryImpl;
import org.geotools.feature.iso.type.TypeFactoryImpl;
import org.geotools.feature.iso.xpath.AttributePropertyHandler;
import org.geotools.filter.IllegalFilterException;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.type.TypeFactory;
import org.opengis.feature.type.TypeName;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Expression;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;

public class MemoryDataAccessTest extends TestCase {

	private MemoryDataAccess dataStore;

	FeatureType wq_plusType;

	static List wq_plusFeatures;

	static final int NUM_FEATURES = 20;

	private TypeBuilder builder;

	private FilterFactory ff;

	protected void setUp() throws Exception {
		super.setUp();
		wq_plusType = ComplexTestData
				.createExample01MultiValuedComplexProperty(new TypeFactoryImpl());
		TypeFactory tf = new TypeFactoryImpl();
		builder = new TypeBuilder(tf);
		ff = CommonFactoryFinder.getFilterFactory(null);

		dataStore = complexFixture();

	}

	protected void tearDown() throws Exception {
		super.tearDown();
		wq_plusType = null;
		wq_plusFeatures = null;
	}

	public void testFilter() throws Exception {
		TypeFactory tf = new SimpleTypeFactoryImpl();
		builder = new TypeBuilder(tf);

		FeatureFactory af = new AttributeFactoryImpl();
		AttributeBuilder attBuilder = new AttributeBuilder(af);

		builder.setName("testType");
		builder.setBinding(Integer.class);
		AttributeType testType = builder.attribute();

		builder.addAttribute("test", testType);
		builder.setName("typeName");

		SimpleFeatureType t = (SimpleFeatureType) builder.feature();

		attBuilder.setType(t);
		attBuilder.add(null, "test");
		Attribute attribute = attBuilder.build();

		PropertyIsGreaterThan gtFilter = ff.greater(ff.property("test"), ff
				.literal(12));
		PropertyIsNull nullf = ff.isNull(ff.property("test"));

		Filter filter = ff.or(nullf, gtFilter);

		boolean contains = filter.evaluate(attribute);
		assertTrue(contains);
	}

	public void testDescribeType() throws IOException {
		FeatureType schema = (FeatureType) dataStore.describe(wq_plusType
				.getName());
		assertNotNull(schema);
		assertEquals(wq_plusType, schema);
	}

	public void testFeatureReader() throws IOException {
		TypeName typeName = wq_plusType.getName();
		FeatureSource2 source = (FeatureSource2) dataStore.access(typeName);

		Collection features = source.content();
		assertNotNull(features);

		int count = 0;
		for (Iterator reader = features.iterator(); reader.hasNext();) {
			Feature object = (Feature) reader.next();
			count++;
		}
		assertEquals(NUM_FEATURES, count);
	}

	public void testFeatureSource() throws IOException {
		FeatureSource2 fs = (FeatureSource2) dataStore.access(wq_plusType
				.getName());
		assertNotNull(fs);
		assertEquals(wq_plusType, fs.describe());

		assertNotNull(fs.getBounds());
		Envelope expected = new Envelope();
		for (Iterator it = wq_plusFeatures.iterator(); it.hasNext();) {
			expected.expandToInclude((Envelope) ((Feature) it.next())
					.getBounds());
		}
		assertTrue(expected.equals(fs.getBounds()));
		assertEquals(NUM_FEATURES, fs.getCount(Query.ALL));

		assertSame(dataStore, fs.getDataStore());
	}

	/**
	 * Query:
	 * 
	 * <pre>
	 *       (measurement/determinand_description = 'determinand_description_5_0')
	 *        OR
	 *       ( length(project_no) &gt; 12) //at least ending with two digits
	 * </pre>
	 */
	public void testComplexQuery() throws IllegalFilterException, IOException {
		FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);

		Filter determinand = ff.equals(ff
				.property("measurement/determinand_description"), ff
				.literal("determinand_description_5_0"));

		Expression length = ff.function("LengthFunction", ff
				.property("project_no"));
		Filter project_no = ff.greater(length, ff.literal(13));

		Filter filter = ff.or(determinand, project_no);

		Source source = dataStore.access(wq_plusType.getName());
		Collection result = source.content(filter);
		
		assertNotNull(result);

		int expected = 1 + (NUM_FEATURES - 10);
		int actual = result.size();
		assertEquals(expected, actual);
	}

	/**
	 * Creates an in memory datastore containing a complex FeatureType:
	 * <p>
	 * Schema:
	 * 
	 * <pre>
	 *           wq_plus 
	 *           	 sitename (1..1)
	 *           	 anzlic_no (0..1)
	 *           	 location (0..1)
	 *             measurement (0..*)
	 *             	determinand_description (1..1)
	 *             	result (1..1)
	 *             project_no (0..1)
	 * </pre>
	 * 
	 * </p>
	 * <p>
	 * The features created has a variable number of measurement attribute
	 * instances. {@link #NUM_FEATURES} features are created inside an iteration
	 * from 0 to <code>NUM_FEATURES - 1</code>. The iteration number is the
	 * number of measurement instances each feature has.
	 * </p>
	 * 
	 * @return
	 */
	private static MemoryDataAccess complexFixture() throws IOException {
		MemoryDataAccess md = new MemoryDataAccess();
		final TypeFactory tf = new TypeFactoryImpl();

		final FeatureFactory attf = new AttributeFactoryImpl();
		final GeometryFactory gf = new GeometryFactory();
		final AttributeBuilder builder = new AttributeBuilder(attf);

		FeatureType ftype = ComplexTestData
				.createExample01MultiValuedComplexProperty(tf);

		// md.createSchema(ftype);

		wq_plusFeatures = new LinkedList();
		final String namespaceURI = ftype.getName().getNamespaceURI();

		builder.setType(ftype);
		for (int i = 0; i < NUM_FEATURES; i++) {

			builder.add("sitename_" + i, new Name(namespaceURI, "sitename"));

			builder.add("anzlic_no_" + i, new Name(namespaceURI, "anzlic_no"));

			builder.add(gf.createPoint(new Coordinate(i, i)), new Name(
					namespaceURI, "location"));

			PropertyDescriptor measurementDescriptor = Types.descriptor(ftype,
					new Name(namespaceURI, "measurement"));

			ComplexType mtype = (ComplexType) measurementDescriptor.type();

			AttributeBuilder mbuilder = new AttributeBuilder(attf);

			Collection measurements = new ArrayList();

			for (int mcount = 0; mcount < i; mcount++) {
				mbuilder.setType(mtype);

				mbuilder.add("determinand_description_" + i + "_" + mcount,
						new Name(namespaceURI, "determinand_description"));

				mbuilder.add("result_" + i + "_" + mcount, new Name(
						namespaceURI, "result"));

				ComplexAttribute measurement = (ComplexAttribute) mbuilder
						.build();
				//measurements.add(measurement);
				
				builder.add(measurement.get(), new Name(namespaceURI, "measurement"));
			}
			/*
			if (measurements.size() > 0) {
				builder
						.add(measurements,
								new Name(namespaceURI, "measurement"));
			}
			*/

			builder.add("project_no_ " + i,
					new Name(namespaceURI, "project_no"));

			String fid = ftype.getName().getLocalPart() + "." + i;
			Feature f = (Feature) builder.build(fid);

			wq_plusFeatures.add(f);
		}

		md.addFeatures(wq_plusFeatures);
		return md;
	}
}
