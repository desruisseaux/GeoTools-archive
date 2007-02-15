package org.geotools.filter;

import junit.framework.TestCase;

import org.geotools.data.ComplexTestData;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.iso.AttributeBuilder;
import org.geotools.feature.iso.AttributeFactoryImpl;
import org.geotools.feature.iso.type.TypeFactoryImpl;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.TypeFactory;
import org.opengis.filter.expression.Function;

public class IDFunctionExpressionTest extends TestCase {

    Feature feature;

    Function idExpr;

    public IDFunctionExpressionTest() {
        super("IDFunctionExpressionTest");
    }

    protected void setUp() throws Exception {
        super.setUp();
        TypeFactory typeFactory = new TypeFactoryImpl();
        FeatureType type = ComplexTestData.createExample02MultipleMultivalued(typeFactory);
        AttributeBuilder ab = new AttributeBuilder(new AttributeFactoryImpl());
        ab.setType(type);
        feature = (Feature) ab.build("test-id");
        idExpr = CommonFactoryFinder.getFilterFactory(null).function("getID", new org.opengis.filter.expression.Expression[0]);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetValue() throws Exception {
        String fid = feature.getID();
        Object found = idExpr.evaluate(feature);
        assertNotNull(found);
        assertEquals(fid, found);
    }

    /*
     * Test method for 'org.geotools.filter.IDFunctionExpression.getName()'
     */
    public void testGetName() {
        assertEquals("getID", idExpr.getName());
    }

    /*
     * Test method for 'org.geotools.filter.IDFunctionExpression.getArgs()'
     */
    public void testGetArgs() {
        assertNotNull(idExpr.getParameters());
        assertEquals(0, idExpr.getParameters().size());
    }

}
