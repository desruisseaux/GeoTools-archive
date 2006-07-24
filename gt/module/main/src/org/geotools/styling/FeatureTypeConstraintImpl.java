/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
import org.geotools.filter.Filter;
import org.geotools.resources.Utilities;


/**
 *
 * @source $URL$
 */
public class FeatureTypeConstraintImpl extends AbstractGTComponent
    implements FeatureTypeConstraint, Cloneable {
    /** the feature type name */
    String featureTypeName;

    /** the filter */
    Filter filter;

    /** the extents */
    Extent[] extents;

    public String getFeatureTypeName() {
        return featureTypeName;
    }

    public void setFeatureTypeName(String name) {
        String old = this.featureTypeName;
        this.featureTypeName = name;

        fireChildChanged("featureTypeName", this.featureTypeName, old);
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        Filter old = this.filter;
        this.filter = filter;

        fireChildChanged("filter", this.filter, old);
    }

    public Extent[] getExtents() {
        return extents;
    }

    public void setExtents(Extent[] extents) {
        Extent[] old = this.extents;
        this.extents = extents;

        fireChildChanged("extents", this.extents, old);
    }

    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }
    
    public int hashCode() {
    	final int PRIME = 1000003;
		int result = 0;
		
		if (featureTypeName != null) {
			result = (PRIME * result) + featureTypeName.hashCode();
		}
		
		if (filter != null) {
			result =  (PRIME * result) + filter.hashCode();
		}
		
		if (extents != null) {
			result =  (PRIME * result) + extents.hashCode();
	
		}
		return result;
    }
    
    public boolean equals(Object obj) {
    	if (this == obj) {
    		return true;
    	}
    	
    	if (obj instanceof FeatureTypeConstraintImpl) {
    		FeatureTypeConstraintImpl other = (FeatureTypeConstraintImpl)obj;
    		return Utilities.equals(featureTypeName,other.featureTypeName) && 
    			Utilities.equals(filter,other.filter) && 
    			Utilities.equals(extents,other.extents);
    	}
    	
    	return false;
    		
    }
}
