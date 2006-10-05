/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *    
 *    Created on Jan 24, 2004
 */
package org.geotools.validation.spatial;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.validation.DefaultFeatureValidation;
import org.geotools.validation.ValidationResults;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;


/**
 * PolygonNoGapsValidation purpose.
 * 
 * <p>
 * Ensures Polygon does not have gaps.
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: dmzwiers $ (last modification)
 * @source $URL$
 * @version $Id$
 */
public class PolygonNoGapsValidation extends DefaultFeatureValidation {
    /**
     * PolygonNoGapsValidation constructor.
     * 
     * <p>
     * Description
     * </p>
     */
    public PolygonNoGapsValidation() {
        super();
    }

    /**
     * Ensure Polygon does not have gaps.
     * 
     * <p></p>
     *
     * wasparam layers a HashMap of key="TypeName" value="FeatureSource"
     * wasparam envelope The bounding box of modified features
     * @param results Storage for the error and warning messages
     *
     * @return True if no features intersect. If they do then the validation
     *         failed.
     *
     * @throws Exception DOCUMENT ME!
     *
     * @see org.geotools.validation.IntegrityValidation#validate(java.util.Map,
     *      com.vividsolutions.jts.geom.Envelope,
     *      org.geotools.validation.ValidationResults)
     */
    public boolean validate(Feature feature, FeatureType type,
	ValidationResults results){
		
        if(feature != null){
        	Geometry layer = feature.getDefaultGeometry();
        	if(layer instanceof Polygon){
        		Polygon p = (Polygon)layer;
        		if(p.getNumInteriorRing()!=0){
                	results.error(feature,"The generated result was had gaps.");
                	return false;
        		}
        		return true;
        	}
        	results.error(feature,"The generated result was not of type polygon.");
        	return false;
        }

        return true;
    }
}
