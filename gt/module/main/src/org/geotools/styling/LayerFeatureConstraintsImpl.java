package org.geotools.styling;

import org.geotools.event.AbstractGTComponent;
import org.geotools.resources.Utilities;

public class LayerFeatureConstraintsImpl extends AbstractGTComponent
	implements LayerFeatureConstraints {

	private FeatureTypeConstraint[] constraints;
	
	public FeatureTypeConstraint[] getFeatureTypeConstraints() {
		return constraints;
	}

	public void setFeatrureTypeConstraints(FeatureTypeConstraint[] constraints) {
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
