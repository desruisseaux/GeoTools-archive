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
package org.geotools.styling;

import org.geotools.event.AbstractGTComponent;
import org.geotools.resources.Utilities;

public class LayerFeatureConstraintsImpl extends AbstractGTComponent
	implements LayerFeatureConstraints {

	private FeatureTypeConstraint[] constraints;
	
	public FeatureTypeConstraint[] getFeatureTypeConstraints() {
		return constraints;
	}

	public void setFeatureTypeConstraints(FeatureTypeConstraint[] constraints) {
		FeatureTypeConstraint[] old = this.constraints;
		this.constraints = constraints;
		
		fireChildChanged("featureTypeConstraints", this.constraints, old);
	}
	
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (obj instanceof FeatureTypeConstraintImpl) {
			LayerFeatureConstraintsImpl other = (LayerFeatureConstraintsImpl)obj;
			return Utilities.equals(constraints,other.constraints);
		}
		
		return false;
	}
	
	public int hashCode() {
		final int PRIME = 1000003;
		int result = 0;
		
		if (constraints != null) {
			 result = (PRIME * result) + constraints.hashCode();
		}
		
		return result;
	}

}
