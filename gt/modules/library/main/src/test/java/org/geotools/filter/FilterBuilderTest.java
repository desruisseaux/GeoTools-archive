package org.geotools.filter;

import junit.framework.TestCase;

import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.FilterFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
/**
 * Confirms the functionality & usability of FilterBuilder.
 * 
 * @author Jody Garnett, Refractions Research Inc.
 */
public class FilterBuilderTest extends TestCase {
    FilterBuilder build;
    FilterFactory ff;
	GeometryFactory gf;

    protected void setUp() throws Exception {
        super.setUp();
        build = new FilterBuilder();

        ff = CommonFactoryFinder.getFilterFactory(null);
        gf = new GeometryFactory();        
    }

    protected void tearDown() throws Exception {
        build = null;
        super.tearDown();
    }
    public void testLiteral(){
        // 1 + 1
        Expression expression = build.literal( 1 ).expr();
        assertEquals( expression, ff.literal( 1 ));
    }
    public void testNested(){
        Expression expression = build.literal(1).literal(2).add();
        assertEquals( expression, ff.add( ff.literal(1), ff.literal(2)) );
    }
    
    public void testChained(){
        Expression expression = build.literal(1).literal( 2 ).add();
        assertEquals( expression, ff.add( ff.literal(1), ff.literal(2)) );
    }
    
    public void testGeometry(){
    	build.literal( gf.createPoint( new Coordinate( 1, 1 )));    	
    }
    
}