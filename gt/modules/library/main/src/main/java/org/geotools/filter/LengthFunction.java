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

import org.opengis.filter.expression.Expression;

/**
 * Takes an AttributeExpression, and computes the length of the data for the attribute.
 * 
 * @author dzwiers
 *
 * @source $URL$
 */
public class LengthFunction extends FunctionExpressionImpl {

        public LengthFunction(){
            super("length");
        }
        
	/* (non-Javadoc)
	 * @see org.geotools.filter.FunctionExpressionImpl#getArgCount()
	 */
	public int getArgCount() {
		return 1;
	}

	/* (non-Javadoc)
	 * @see org.geotools.filter.Expression#getValue(org.geotools.feature.Feature)
	 */
	public Object evaluate(Object feature) {
	    Expression ae = (Expression)getParameters().get(0);
            return new Integer(ae.evaluate(feature).toString().length());
	}

}
