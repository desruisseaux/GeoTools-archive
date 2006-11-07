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
import org.geotools.filter.expression.Value;
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
		Object[] values = eval( feature );
		Comparable lower = comparable( values[ 0 ] );
		Comparable upper = comparable( values[ 1 ] );
		
		Value value = new Value( eval( expression, feature ) );
		Object o = value.value( lower.getClass() );
		if ( o == null ) {
			o = value.value( upper.getClass() );
		}
		if ( o == null ) {
			o = value.getValue();
		}
		
		Comparable between = comparable( o );
		
		return lower.compareTo( between ) == -1 &&
		       upper.compareTo( between ) == 1;
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
	public final org.geotools.filter.Expression getMiddleValue() {
		return (org.geotools.filter.Expression) getExpression();
	}
	
	/**
	 * @deprecated use {@link #setExpression(Expression) }
	 */
	public void addMiddleValue(org.geotools.filter.Expression middleValue) {
		setExpression( middleValue );
	}
	
}
