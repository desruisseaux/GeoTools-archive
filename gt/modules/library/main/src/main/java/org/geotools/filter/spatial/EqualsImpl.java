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
package org.geotools.filter.spatial;

import org.geotools.feature.Feature;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.GeometryFilterImpl;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.spatial.Equals;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class EqualsImpl extends GeometryFilterImpl implements Equals {

	public EqualsImpl(FilterFactory factory, Expression e1, Expression e2) {
		super(factory, e1, e2);

		// backwards compat with old type system
		this.filterType = GEOMETRY_EQUALS;
	}

	public boolean evaluate(Object feature) {
		if (feature instanceof Feature && !validate((Feature)feature))
			return false;

		Geometry left = getLeftGeometry(feature);
		Geometry right = getRightGeometry(feature);

		Envelope envLeft = left.getEnvelopeInternal();
		Envelope envRight = right.getEnvelopeInternal();

		if (envRight.equals(envLeft))
			return left.equals(right);
		else
			return false;
	}

	public Object accept(FilterVisitor visitor, Object extraData) {
		return visitor.visit(this, extraData);
	}
}
