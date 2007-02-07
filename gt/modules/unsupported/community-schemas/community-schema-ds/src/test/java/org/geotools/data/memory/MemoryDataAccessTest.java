package org.geotools.data.memory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.geotools.data.ComplexTestData;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.iso.AttributeBuilder;
import org.geotools.feature.iso.AttributeFactoryImpl;
import org.geotools.feature.iso.Descriptors;
import org.geotools.feature.iso.TypeBuilder;
import org.geotools.feature.iso.type.FeatureTypeImpl;
import org.geotools.feature.iso.type.TypeFactoryImpl;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.FilterType;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.NullFilter;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureCollection;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.TypeFactory;
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
        
        PropertyIsGreaterThan gtFilter = ff.greater(ff.property("test"), ff.literal(12));
        PropertyIsNull nullf = ff.isNull(ff.property("test"));
        
        Filter filter = ff.or(nullf, gtFilter);
        
        System.out.println(filter);
        
        boolean contains = filter.evaluate(attribute);
        assertTrue(contains);
    }

    public void testGetSchema() throws IOException {
        FeatureType schema = dataStore.getSchema("wq_plus");
        assertNotNull(schema);
        assertEquals(wq_plusType, schema);
    }

    public void testFeatureReader() throws IOException {
        FeatureReader reader = dataStore.getFeatureReader("wq_plus");
        FeatureType type = reader.getFeatureType();
        assertEquals(wq_plusType, type);

        int count = 0;
        while (reader.hasNext()) {
            reader.next();
            count++;
        }
        assertEquals(NUM_FEATURES, count);
    }

    public void testFeatureSource() throws IOException {
        FeatureSource fs = dataStore.getFeatureSource("wq_plus");
        assertNotNull(fs);
        assertEquals(wq_plusType, fs.getSchema());

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
     *     (measurement/determinand_description = 'determinand_description_5_0')
     *      OR
     *     ( length(project_no) &gt; 12) //at least ending with two digits
     * </pre>
     */
    public void testComplexQuery() throws IllegalFilterException, IOException {
        FilterFactory ff = FilterFactory.createFilterFactory();

        CompareFilter determinand = ff
                .createCompareFilter(FilterType.COMPARE_EQUALS);
        Expression left = ff.createAttributeExpression(wq_plusType,
                "measurement/determinand_description");
        Expression literal = ff
                .createLiteralExpression("determinand_description_5_0");
        determinand.addLeftValue(left);
        determinand.addRightValue(literal);

        CompareFilter project_no = ff
                .createCompareFilter(FilterType.COMPARE_GREATER_THAN);
        FunctionExpression length = ff
                .createFunctionExpression("LengthFunction");
        length.setArgs(new Expression[] { ff.createAttributeExpression(
                wq_plusType, "project_no") });
        literal = ff.createLiteralExpression(13);

        project_no.addLeftValue(length);
        project_no.addRightValue(literal);

        Filter filter = determinand.or(project_no);

        FeatureSource source = dataStore.getFeatureSource("wq_plus");
        FeatureCollection result = source.getFeatures(filter);
        assertNotNull(result);

        int expected = 1 + (NUM_FEATURES - 10);
        assertEquals(expected, result.size());
    }

    /**
     * Creates an in memory datastore containing a complex FeatureType:
     * <p>
     * Schema:
     * 
     * <pre>
     *     wq_plus 
     *     	 sitename (1..1)
     *     	 anzlic_no (0..1)
     *     	 location (0..1)
     *       measurement (0..*)
     *       	determinand_description (1..1)
     *       	result (1..1)
     *       project_no (0..1)
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

        final AttributeFactory attf = new AttributeFactoryImpl();
        final GeometryFactory gf = new GeometryFactory();

        FeatureTypeImpl ftype = (FeatureTypeImpl) ComplexTestData
                .createExample01MultiValuedComplexProperty(tf, df);
        md.createSchema(ftype);

        AttributeDescriptor type;
        wq_plusFeatures = new LinkedList();
        for (int i = 0; i < NUM_FEATURES; i++) {
            Feature f = attf.createFeature(new NodeImpl(ftype), ftype.name()
                    + "." + i);
            List/* <Attribute> */values = new ArrayList/* <Attribute> */();

            type = Descriptors.node(ftype.getDescriptor(), "sitename");
            values.add(attf.create(type, null, "sitename_" + i));

            type = Descriptors.node(ftype.getDescriptor(), "anzlic_no");
            values.add(attf.create(type, null, "anzilc_no_" + i));

            type = Descriptors.node(ftype.getDescriptor(), "location");
            values.add(attf.create(type, null, gf.createPoint(new Coordinate(i,
                    i))));

            AttributeDescriptor measurementDescriptor = Descriptors.node(ftype
                    .getDescriptor(), "measurement");
            ComplexType mtype = (ComplexType) measurementDescriptor.getType();
            for (int mcount = 0; mcount < i; mcount++) {
                ComplexAttribute measurement = attf.createComplex(
                        measurementDescriptor, null);
                List/* <Attribute> */mcontents = new ArrayList/* <Attribute> */();

                type = Descriptors.node(mtype.getDescriptor(),
                        "determinand_description");
                mcontents.add(attf.create(type, null,
                        "determinand_description_" + i + "_" + mcount));
                type = Descriptors.node(mtype.getDescriptor(), "result");
                mcontents.add(attf.create(type, null, "result_" + i + "_"
                        + mcount));

                measurement.set(mcontents);

                values.add(measurement);
            }

            type = Descriptors.node(ftype.getDescriptor(), "project_no");
            values.add(attf.create(type, null, "project_no_ " + i));

            f.set(values);
            wq_plusFeatures.add(f);
        }

        md.addFeatures(wq_plusFeatures);
        return md;
    }
}
