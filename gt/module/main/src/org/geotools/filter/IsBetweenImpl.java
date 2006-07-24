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
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.expression.Expression;

/**
 * Straight implementation of GeoAPI interface.
 * 
 * @author jdeolive
 */
public class IsBetweenImpl extends FilterAbstract implements PropertyIsBetween {

	private Expression upperBoundary;
	private Expression lowerBoundary;
	private Expression expression;

	protected IsBetweenImpl(FilterFactory factory, Expression lower, Expression expression, Expression upper ){
		super( factory );
		this.expression = expression;
		this.lowerBoundary = lower;
		this.upperBoundary = upper;
	}
	
	public Expression getExpression() {
		return expression;
	}
	public void setExpression(Expression expression) {
		this.expression = expression;
	}
	public Expression getLowerBoundary() {
		return lowerBoundary;
	}
	public void setLowerBoundary(Expression lowerBounds) {
		this.lowerBoundary = lowerBounds;
	}
	public Expression getUpperBoundary() {
		return upperBoundary;
	}
	public void setUpperBoundary(Expression upperBounds) {
		this.upperBoundary = upperBounds;
	}
	
	//@Override
	public boolean evaluate(Feature feature) {
		Comparable lower = comparable( lowerBoundary, feature );
		Comparable value = comparable( expression, feature );
		Comparable upper = comparable( upperBoundary, feature );

		return lower.compareTo( value ) == -1 &&
		       upper.compareTo( upper ) == 1;
	}
	
	
}
