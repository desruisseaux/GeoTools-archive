package org.geotools.filter;

import junit.framework.TestCase;

import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.FilterFactory;
/**
 * Confirms the functionality & usability of FilterBuilder.
 * 
 * @author Jody Garnett, Refractions Research Inc.
 */
public class FilterBuilderTest extends TestCase {
    FilterBuilder build;
    FilterFactory ff;

    protected void setUp() throws Exception {
        super.setUp();
        build = new FilterBuilder();

        ff = CommonFactoryFinder.getFilterFactory(null);
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
        Expression expression = build.literal(1).add( build.literal(2) );
        assertEquals( expression, ff.add( ff.literal(1), ff.literal(2)) );
    }
    
    public void testChained(){
        Expression expression = build.literal(1).literal( 2 ).add();
        assertEquals( expression, ff.add( ff.literal(1), ff.literal(2)) );
    }
    
}
