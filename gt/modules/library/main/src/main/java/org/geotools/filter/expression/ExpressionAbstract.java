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
package org.geotools.filter.expression;

import org.geotools.feature.Feature;
import org.geotools.filter.Expression;
import org.opengis.filter.expression.ExpressionVisitor;
//
/**
 * Abstract superclass of these Expression implementations.
 * <p>
 * Contains additional support for "Expression chaining". This allows
 * Expressions to be constructed as a chain of Java commands similar to the use
 * of the java collections api.
 * </p>
 * <p>
 * Note: Expression chaining is a simple developer convience, it has no effect
 * on the data model exposed by the GeoAPI interfaces.
 * </p>
 * <p>
 * Idea: We may also be able to teach this implementation to make use of
 * JXPath to extract "attribute values" from Java Beans, DOM, JDOM in addition
 * to the geotools & geoapi FeatureType models. It is a cunning plan - any
 * implementation will make use of this abstract base class.
 * </p>
 * 
 * @author Jody Garnett
 *
 */
public abstract class ExpressionAbstract implements org.opengis.filter.expression.Expression {
	
	/** Subclass should overide, default implementation returns null */
	public Object evaluate(Feature feature){
		return null;
	}
	/** Subclass should overide, default implementation returns null */
	public Object evaluate(Object object) {
		return null;
	}
	
	public final Object evaluate(Object object, Class context) {
		Value value = new Value( evaluate( object ) );
		return value.value( context );
	}
	
	/** Subclass should override, default implementation just returns extraData */
	public Object accept(ExpressionVisitor visitor, Object extraData) {
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
}
