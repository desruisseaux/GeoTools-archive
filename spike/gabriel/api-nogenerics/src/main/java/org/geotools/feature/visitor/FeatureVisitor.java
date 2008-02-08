/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.feature.visitor;

import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;


/**
 * FeatureVisitor interface (for reviewing feature content).
 *
 * @author Cory Horner, Refractions
 *
 * @since 2.2.M2
 * @source $URL$
 */
public abstract class FeatureVisitor implements org.opengis.feature.FeatureVisitor {
	public Object visit(SimpleFeature feature, Object extraData) {
		visit( feature );
		return extraData;
	}	
    public abstract void visit(Feature feature);
}
