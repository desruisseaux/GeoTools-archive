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

import java.util.Map;

import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.validation.ValidationResults;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;


/**
 * PointCoveredByLineValidation purpose.
 * 
 * <p>
 * Checks to ensure the Line is covered by the Polygon Boundary.
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: dmzwiers $ (last modification)
 * @source $URL$
 * @version $Id$
 */
public class LineCoveredByPolygonBoundaryValidation
    extends LinePolygonAbstractValidation {
    /**
     * PointCoveredByLineValidation constructor.
     * 
     * <p>
     * Super
     * </p>
     */
    public LineCoveredByPolygonBoundaryValidation() {
        super();
    }

    /**
     * Ensure Line is covered by the Polygon Boundary.
     * 
     * <p></p>
     *
     * @param layers a HashMap of key="TypeName" value="FeatureSource"
     * @param envelope The bounding box of modified features
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
    public boolean validate(Map layers, Envelope envelope,
        ValidationResults results) throws Exception {

    	boolean r = true;
    	
        FeatureSource<SimpleFeatureType, SimpleFeature> fsLine = (FeatureSource) layers.get(getLineTypeRef());
        
        FeatureCollection<SimpleFeatureType, SimpleFeature> fcLine = fsLine.getFeatures();
        FeatureIterator<SimpleFeature> fLine = fcLine.features();
        
        FeatureSource<SimpleFeatureType, SimpleFeature> fsPoly = (FeatureSource) layers.get(getRestrictedPolygonTypeRef());
         
        FeatureCollection<SimpleFeatureType, SimpleFeature> fcPoly = fsPoly.getFeatures();
                
        while(fLine.hasNext()){
        	SimpleFeature line = fLine.next();
            FeatureIterator<SimpleFeature> fPoly = fcPoly.features();
            Geometry lineGeom = (Geometry) line.getDefaultGeometry();
            if(envelope.contains(lineGeom.getEnvelopeInternal())){
            	// 	check for valid comparison
            	if(LineString.class.isAssignableFrom(lineGeom.getClass())){
            		while(fPoly.hasNext()){
            			SimpleFeature poly = fPoly.next();
            			Geometry polyGeom = (Geometry) poly.getDefaultGeometry(); 
                        if(envelope.contains(polyGeom.getEnvelopeInternal())){
                        	if(Polygon.class.isAssignableFrom(polyGeom.getClass())){
                        		Geometry polyGeomBoundary = polyGeom.getBoundary();
                        		if(!polyGeomBoundary.contains(lineGeom)){
                        			results.error(poly,"Boundary does not contain the specified Line.");
                        			r = false;
                        		}
                        		// do next.
                        	}else{
                        		fcPoly.remove(poly);
                        		results.warning(poly,"Invalid type: this feature is not a derivative of a Polygon");
                        	}
                        }else{
                    		fcPoly.remove(poly);
                        }
            		}
            	}else{
            		results.warning(line,"Invalid type: this feature is not a derivative of a LineString");
            	}
            }
        }
        return r;
    }

    /**
     * The priority level used to schedule this Validation.
     *
     * @return PRORITY_SIMPLE
     *
     * @see org.geotools.validation.Validation#getPriority()
     */
    public int getPriority() {
        return PRIORITY_SIMPLE;
    }
}
