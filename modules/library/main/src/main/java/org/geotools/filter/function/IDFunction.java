/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.filter.function;

import org.geotools.filter.FunctionExpression;
import org.geotools.filter.FunctionExpressionImpl;
import org.opengis.feature.Feature;

/**
 * Allow access to the value of Feature.getID() as an expression
 * 
 * @author Jody Garnett
 * @since 2.2, 2.5
 */
public class IDFunction extends FunctionExpressionImpl {

	public IDFunction() {
	    super("id");
	}

	public int getArgCount() {
		return 0;
	}

	public String toString() {
		return "ID()";
	}

	public Object getValue(Object obj) {
	    if( obj instanceof Feature){
	        Feature feature = (Feature) obj;
	        return feature.getID();
	    }
		return ""; // no ID
	}

}