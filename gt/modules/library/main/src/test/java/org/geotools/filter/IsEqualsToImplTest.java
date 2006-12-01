package org.geotools.filter;

import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.Expression;

import junit.framework.TestCase;

public class IsEqualsToImplTest extends TestCase {

	org.opengis.filter.FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory( null );
	
	public void testOperandsSameType() {
		Expression e1 = filterFactory.literal( 1 );
		Expression e2 = filterFactory.literal( 1 );
		
		PropertyIsEqualTo equal = filterFactory.equals( e1, e2 );
		assertTrue( equal.evaluate( null ) );
	}
	
	public void testOperandsDifferentType() {
		Expression e1 = filterFactory.literal( 1 );
		Expression e2 = filterFactory.literal( "1"
				);
		
		PropertyIsEqualTo equal = filterFactory.equals( e1, e2 );
		assertTrue( equal.evaluate( null ) );
	}
	
	public void testCaseSensitivity() {
		Expression e1 = filterFactory.literal( "foo" );
		Expression e2 = filterFactory.literal( "FoO" );
		
		PropertyIsEqualTo caseSensitive = filterFactory.equal( e1, e2, true );
		assertFalse( caseSensitive. evaluate( null ) );
		
		PropertyIsEqualTo caseInsensitive = filterFactory.equal( e1, e2, false );
		assertTrue( caseInsensitive. evaluate( null ) );
		
	}
}
