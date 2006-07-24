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
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Expression;

public class IsNullImpl extends FilterAbstract implements
		PropertyIsNull {

	private org.opengis.filter.expression.Expression expression;

	public IsNullImpl(FilterFactory factory, org.opengis.filter.expression.Expression expression) {
		super(factory);
		this.expression = expression;
	}
	
	public Expression getExpression() {
		return expression;
	}
	
	public void setExpression(Expression expression) {
		this.expression = expression;
	}
	
	//@Override
	public boolean evaluate(Feature feature) {
		return expression == null ||
	       expression.evaluate( feature ) == null;
	}	
}
