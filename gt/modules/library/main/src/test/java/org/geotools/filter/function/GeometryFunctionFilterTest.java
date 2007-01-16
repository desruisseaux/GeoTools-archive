package org.geotools.filter.function;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.Expression;
import org.geotools.filter.FunctionExpression;


public class GeometryFunctionFilterTest extends FunctionTestSupport {

    public GeometryFunctionFilterTest() {
        super("GeometryFunctionFilterTest");
    }
    
    public void testBasicTest() throws Exception {
        FunctionExpression exp = fac.createFunctionExpression("geometryType");
        exp.setArgs(new Expression[]{ fac.createAttributeExpression("geom") });
        FeatureIterator iter=fc.features();
        while( iter.hasNext() ){
            Feature feature = iter.next();
            assertEquals( "Point", exp.getValue(feature) );
        }
        
        iter.close();
    }
    
    public void testNullTest() throws Exception {
        FunctionExpression exp = fac.createFunctionExpression("geometryType");
        exp.setArgs(new Expression[]{ fac.createAttributeExpression("geom") });
        FeatureIterator iter=fc.features();
        while( iter.hasNext() ){
            Feature feature = iter.next();
            feature.setAttribute("geom",null);
            assertNull( exp.getValue(feature) );
        }
        
        iter.close();
    }
}
