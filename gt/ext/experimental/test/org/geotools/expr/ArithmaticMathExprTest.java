package org.geotools.expr;

import junit.framework.TestCase;

public class ArithmaticMathExprTest extends TestCase {
	MathExpr one = new LiteralMathExpr( 1 );
	MathExpr two = new LiteralMathExpr( 2 );
	
	public void testAdd(){
		MathExpr add = one.add( two );
		Expr value = add.eval();
		assertTrue( "evaluated", value instanceof LiteralMathExpr );
		LiteralMathExpr sum = (LiteralMathExpr) value;
		assertEquals( 3, sum.toDouble(), 0 );
		
	}
}
