/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
 *    
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.filter;

import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.expression.Expression;

/**
 * Abstract implemention for binary filters.
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
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
