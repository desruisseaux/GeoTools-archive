package org.geotools.expr;

import junit.framework.TestCase;

public class LiteralExprTest extends TestCase {
	public void testHelloWorld(){
		LiteralExpr literal;
		
		literal = new LiteralExpr( "Hello World" );
		assertEquals( "Hello World", literal.getValue() );
	}
	
}
