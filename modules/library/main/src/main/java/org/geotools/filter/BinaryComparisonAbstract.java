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

import org.geotools.filter.expression.Value;
import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.expression.Expression;

/**
 * Abstract implemention for binary filters.
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 */
public abstract class BinaryComparisonAbstract extends AbstractFilter 
	implements BinaryComparisonOperator {

	protected Expression expression1;
	protected Expression expression2;

	boolean matchingCase;
	
	protected BinaryComparisonAbstract(org.opengis.filter.FilterFactory factory) {
		this(factory,null,null);
	}
	
	protected BinaryComparisonAbstract(org.opengis.filter.FilterFactory factory, Expression expression1, Expression expression2 ) {
		this(factory,expression1,expression2,true);
	}
	
	protected BinaryComparisonAbstract(org.opengis.filter.FilterFactory factory, Expression expression1, Expression expression2, boolean matchingCase ) {
		super(factory);
		this.expression1 = expression1;
		this.expression2 = expression2;		
		this.matchingCase = matchingCase;
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
	
	public boolean isMatchingCase() {
		return matchingCase;
	}
	
	public Filter and(org.opengis.filter.Filter filter) {        
		return (Filter) factory.and(this, filter);
	}

	public Filter or(org.opengis.filter.Filter filter) {
		return (Filter) factory.or(this, filter);
	}

	public Filter not() {
		return (Filter) factory.not(this);
	}

	/**
	 * Convenience method which evaluates the expressions and trys to align the values
	 * to be of the same type. 
	 * <p>
	 * If the values can not be aligned, the original values are returned.
	 * </p>
	 *  
	 * @return
	 */
	protected Object[] eval( Object object ) {
		Value v1 = new Value( eval( getExpression1(), object ) );
		Value v2 = new Value( eval( getExpression2(), object ) );
		
		if ( v1.getValue() != null && v2.getValue() != null ) {
			//try to convert so that values are of same type
			if ( v1.getValue().getClass().equals( v2.getValue().getClass() ) ) {
				//nothing to do
				return new Object[] { v1.getValue(), v2.getValue() }; 
			}
			
			Object o = v2.value( v1.getValue().getClass() );
			if ( o != null ) {
				return new Object[] { v1.getValue(), o }; 
			}
			
			//try the other way
			o = v1.value( v2.getValue().getClass() );
			if ( o != null ) {
				return new Object[] { o, v2.getValue() };
			}
		}
		
		return new Object[] { v1.getValue(), v2.getValue() };
	}
	
	/**
	 * Wraps an object in a Comparable.
	 * @param value The original value.
	 * @return A comparable
	 */
	protected Comparable comparable( Object value ){
		if ( value == null ) {
			return null;
		}
		
		if( value instanceof Comparable ){
			return (Comparable) value;
		}
		else {
			return String.valueOf( value );
		}
	}
}
