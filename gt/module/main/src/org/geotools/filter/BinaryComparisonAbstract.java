package org.geotools.filter;

import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.expression.Expression;

/**
 * Abstract implemention for binary filters.
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class BinaryComparisonAbstract extends AbstractFilter 
	implements BinaryComparisonOperator {

	protected Expression expression1;
	protected Expression expression2;

	protected BinaryComparisonAbstract(FilterFactory factory) {
		this(factory,null,null);
	}
	
	protected BinaryComparisonAbstract(FilterFactory factory, Expression expression1, Expression expression2 ) {
		super(factory);
		this.expression1 = expression1;
		this.expression2 = expression2;		
	}
	
	public Expression getExpression1() {
		return expression1;
	}

	public void setExpression1(Expression expression) {
		this.expression1 = expression;
	}
	
	public Expression getExpression2() {
		return expression2;
	}
	
	public void setExpression2(Expression expression) {
		this.expression2 = expression;
	}
	
	public Filter and(Filter filter) {
		return factory.and(this,filter);
	}

	public Filter or(Filter filter) {
		return factory.or(this,filter);
	}

	public Filter not() {
		return factory.not(this);
	}

}
