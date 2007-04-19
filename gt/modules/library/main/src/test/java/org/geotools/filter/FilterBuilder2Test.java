package org.geotools.filter;

import junit.framework.TestCase;

import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.expression.Add;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.FilterFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
/**
 * Confirms the functionality & usability of FilterBuilder2.
 * <p>
 * The main difference here is the support of prefix and infix notation.
 * </p>
 * @author Jody Garnett, Refractions Research Inc.
 */
public class FilterBuilder2Test extends TestCase {
    FilterBuilder2 build;
    FilterFactory ff;
	GeometryFactory gf;

    protected void setUp() throws Exception {
        super.setUp();
        build = new FilterBuilder2();

        ff = CommonFactoryFinder.getFilterFactory(null);
        gf = new GeometryFactory();        
    }

    protected void tearDown() throws Exception {
        build = null;
        super.tearDown();
    }
    public void testLiteral(){
        // 1
        Expression expression = build.literal( 1 ).expr();
        assertEquals( expression, ff.literal( 1 ));
    }
    public void testTransition(){
    	Add expected = ff.add( ff.literal(1), ff.literal(1));
    	
    	build.setLeft( ff.literal(1) );
    	build.setRight( ff.literal(1) );
    	build.add();    	
    	
    	assertEquals( "1+1", expected, build.expr() );
    }
    public void testSingleLevel(){
    	Add expected = ff.add( ff.literal(1), ff.literal(2));        
    	
    	assertEquals( "infix", expected, build.literal(1).add().literal(2).expr() );
    	assertEquals( "postfix", expected, build.literal(1).literal(2).add().expr() );
		assertEquals( "prefix", expected, build.add().literal(1).literal(2).expr() );		
    }
    
    public void testTwoLevels(){
    	Add expected = ff.add( ff.add( ff.literal(1), ff.literal(2)), ff.literal(3) );
    	Expression expression = build.literal(1).add().literal(2).add().literal(3).expr();
    	assertEquals( "(1+2)+3", expected, expression );
    	
    	build.init();
    	expression = build.add().add().literal(1).literal(2).literal(3).expr();
    	assertEquals( "(1+2)+3", expected, expression );
    	
    	build.init();
    	expression = build.literal(1).add( 2 ).add( 3 ).expr();
    	assertEquals( "(1+2)+3", expected, expression );
    	
    	build.init();
    	try {
    		build.add().literal(1).literal(2).literal(3).expr();
    		fail("Expected illegal state");
    	}
    	catch (IllegalStateException yep){    		
    	}
    }

    public void testFunction(){
    	Expression expected = ff.function( "length", new Expression[]{ ff.literal(123) } );
    	Expression expression = build.function("length").literal(123).expr();
    	
    	assertEquals( "length(123)", expected, expression );
    }
    public void testGeometry(){
    	build.literal( gf.createPoint( new Coordinate( 1, 1 )));    
    }
    
}
