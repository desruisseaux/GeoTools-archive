package org.geotools.expr;

import java.io.IOException;

import org.geotools.feature.FeatureType;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.geotools.filter.IllegalFilterException;

class CompareExpr extends AbstractExpr {
	Expr expr1,expr2;
	short op;
	CompareExpr( Expr expr1, short op, Expr expr2 ){
		this.expr1 = expr1;
		this.expr2 = expr2;
	}
	static final Comparable compare( ResolvedExpr expr ){
		Object value = expr.getValue();
		if( value instanceof Comparable ){
			return (Comparable) value;
		}
		return null;
	}
	
	public Expr eval() {
		Expr eval1 = expr1.eval();
		Expr eval2 = expr2.eval();
				
		if( eval1 instanceof ResolvedExpr && eval2 instanceof ResolvedExpr ){
			Comparable value1 = compare( (ResolvedExpr) eval1 );
			Comparable value2 = compare( (ResolvedExpr) eval2 );
			
			int compare;
			if( value1 == null && value2 == null){
				compare = 0;
			}
			else if (value1 != null && value2 == null ){
				compare = 1;
			}
			else if (value1 == null && value2 != null ){
				compare = -1;
			}
			else {
				compare = value1.compareTo( value2 );
			}
			switch( op ){
			case Filter.COMPARE_EQUALS:
				return new LiteralExpr( compare == 0 );
			case Filter.COMPARE_GREATER_THAN:
				return new LiteralExpr( compare == 1 );
			case Filter.COMPARE_GREATER_THAN_EQUAL:
				return new LiteralExpr( compare == 1 || compare == 0 );
			case Filter.COMPARE_LESS_THAN:
				return new LiteralExpr( compare == -1 );
			case Filter.COMPARE_LESS_THAN_EQUAL:
				return new LiteralExpr( compare == -1 || compare == 1 );
			case Filter.COMPARE_NOT_EQUALS:
				return new LiteralExpr( compare == 1 || compare == -1 );
			}
		}
		if( eval1 == expr1 && eval2 == expr2 ){
			return this;
		}
		return new CompareExpr( eval1, op, eval2 );		
	}	
	public Filter filter(FeatureType schema) throws IOException {
		try {
			CompareFilter compare = factory.createCompareFilter( op );
			Expression left = expr1.expression( schema );
			Expression right = expr2.expression( schema );
			compare.addLeftValue( left );
			compare.addRightValue( right );
			return compare;
		} catch (IllegalFilterException e) {
			return null;
		}				
	}
}