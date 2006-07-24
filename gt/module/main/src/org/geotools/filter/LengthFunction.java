/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
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
import org.geotools.filter.expression.AttributeExpression;
import org.geotools.filter.expression.Expression;

/**
 * Takes an AttributeExpression, and computes the length of the data for the attribute.
 * 
 * @author dzwiers
 *
 * @source $URL$
 */
public class LengthFunction extends FunctionExpressionImpl {

	public String getName() {
		return "length";
	}

	private AttributeExpression ae;
	public void setArgs(Expression[] args) {
		ae = (AttributeExpression)args[0];
	}

	/* (non-Javadoc)
	 * @see org.geotools.filter.FunctionExpressionImpl#getArgCount()
	 */
	public int getArgCount() {
		return ae == null?0:1;
	}

	/* (non-Javadoc)
	 * @see org.geotools.filter.FunctionExpression#getArgs()
	 */
	public Expression[] getArgs() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.filter.Expression#getValue(org.geotools.feature.Feature)
	 */
	public Object evaluate(Feature feature) {
		return new Integer(ae.getValue(feature).toString().length());
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Length ["+ae.toString()+"]";
	}
}
