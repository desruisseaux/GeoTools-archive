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
import org.opengis.filter.PropertyIsNotEqualTo;
import org.opengis.filter.expression.Expression;

public class IsNotEqualToImpl extends CompareFilterImpl
	implements PropertyIsNotEqualTo {

	protected IsNotEqualToImpl(FilterFactory factory) {
		this(factory,null,null);
	}
	
	protected IsNotEqualToImpl(FilterFactory factory, Expression e1, Expression e2) {
		super(factory, e1, e2);
		
		//backwards compat with old type system
		this.filterType = COMPARE_NOT_EQUALS;
	}

	//@Override
	public boolean evaluate(Feature feature) {
		Object[] values = eval( feature );
		Object value1 = values[ 0 ];
		Object value2 = values[ 1 ];
		
		return (value1 == null && value2 != null) ||
			(value1 != null && value2 == null) ||
		    (value1 != null && !value1.equals( value2 ));
	}
	
	public Object accept(FilterVisitor visitor, Object extraData) {
		return visitor.visit( this, extraData );
	}

}
