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

import org.geotools.feature.Feature;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.expression.Expression;

/**
 * Straight implementation of GeoAPI interface.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 */
public class IsBetweenImpl extends CompareFilterImpl implements BetweenFilter {

	private Expression expression;

	protected IsBetweenImpl(FilterFactory factory, Expression lower, Expression expression, Expression upper ){
		super( factory, lower, upper );
		this.expression = expression;
		
		//backwards compatability
		filterType = FilterType.BETWEEN;
	}
	
	public Expression getExpression() {
		return expression;
	}
	public void setExpression(Expression expression) {
		this.expression = expression;
	}
	
	//@Override
	public boolean evaluate(Feature feature) {
		Comparable lower = comparable( getExpression1(), feature );
		Comparable value = comparable( expression, feature );
		Comparable upper = comparable( getExpression2(), feature );

		return lower.compareTo( value ) == -1 &&
		       upper.compareTo( upper ) == 1;
	}

	public Object accept(FilterVisitor visitor, Object extraData) {
		return visitor.visit( this, extraData );
	}

	public Expression getLowerBoundary() {
		return getExpression1();
	}

	public void setLowerBoundary(Expression lowerBoundary) {
		setExpression1( lowerBoundary );
	}

	public Expression getUpperBoundary() {
		return getExpression2();
	}

	public void setUpperBoundary(Expression upperBoundary) {
		setExpression2( upperBoundary );
	}
	
	/**
	 * @deprecated use {@link #getExpression()}
	 */
	public final org.geotools.filter.expression.Expression getMiddleValue() {
		return (org.geotools.filter.expression.Expression) getExpression();
	}
	
	/**
	 * @deprecated use {@link #setExpression(Expression) }
	 */
	public void addMiddleValue(org.geotools.filter.expression.Expression middleValue) {
		setExpression( middleValue );
	}
	
}
