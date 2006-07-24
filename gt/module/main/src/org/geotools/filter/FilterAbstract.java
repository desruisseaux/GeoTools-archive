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
import org.geotools.filter.expression.Expression;

import org.opengis.filter.FilterVisitor;

/**
 * Abstract implementation for Filter.
 *
 * @author Jody Garnett
 */
public class FilterAbstract implements org.opengis.filter.Filter 
	 {
	
	/** filter factory **/
	FilterFactory factory;
	
	/**
	 * @param factory FilterFactory injected into the filter.
	 */
	protected FilterAbstract(FilterFactory factory) {
		this.factory = factory;
	}
	
	/**
	 * Subclass should overrride.
	 * 
	 * Default value is false
	 */
	public boolean evaluate(Feature feature) {
		return false;
	}
	
	/**
	 * Subclass should overrride.
	 *
	 * Default value is false
	 */
	public boolean evaluate(Object object) {
		return false;
	}
	/**
	 * Straight call throught to: evaulate( feature )
	 */
	public boolean accepts(Feature feature) {
		return evaluate( feature );
	}
	
	
	/** Subclass should override, default implementation just returns extraData */
	public Object accept(FilterVisitor visitor, Object extraData) {
		return extraData;
	}
	
	/**
	 * Helper method for subclasses to reduce null checks
	 * @param expression
	 * @param feature
	 * @return value or null
	 */
	protected Object eval( Expression expression, Feature feature ){
		if( expression == null || feature == null ) return null;
		return expression.evaluate( feature );
	}
	
	/**
	 * Helper method for subclasses to reduce null checks
	 * @param expression
	 * @param object
	 * @return value or null
	 */
	protected Object eval(org.opengis.filter.expression.Expression expression, Object object) {
		if( expression == null || object == null ) return null;
		return expression.evaluate( object );
	}
	
	/**
	 * Subclass convenience method for turning an expression + feature into something
	 * comparable.
	 * <p>
	 * If the result of the expression is something that implements 
	 * {@link java.lang.Comparable} it is returned directly, otherwise the 
	 * object is transformed to a string and returned.
	 * </p>
	 * 
	 * @param expr The expression
	 * @param feature The feature.
	 * 
	 * @return Something comparable.
	 */
	protected Comparable comparable( org.opengis.filter.expression.Expression expr, Feature feature ){
		Object value = eval(expr,feature);
		if( value instanceof Comparable ){
			return (Comparable) value;
		}
		else {
			return String.valueOf( value );
		}
	}
}
