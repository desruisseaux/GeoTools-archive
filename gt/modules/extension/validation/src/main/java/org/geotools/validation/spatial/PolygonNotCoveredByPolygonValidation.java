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
import org.geotools.validation.ValidationResults;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;


/**
 * PolygonBoundaryCoveredByPolygonValidation purpose.
 * 
 * <p>
 * Ensures Polygon is not covered by the Polygon.
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: dmzwiers $ (last modification)
 * @source $URL$
 * @version $Id$
 */
public class PolygonNotCoveredByPolygonValidation
    extends PolygonPolygonAbstractValidation {
    /**
     * PolygonBoundaryCoveredByPolygonValidation constructor.
     * 
     * <p>
     * Description
     * </p>
     */
    public PolygonNotCoveredByPolygonValidation() {
        super();
    }

    /**
     * Ensure Polygon is not covered by the Polygon.
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
        FeatureSource<SimpleFeatureType, SimpleFeature> polySource1 = (FeatureSource) layers.get(getPolygonTypeRef());
        FeatureSource<SimpleFeatureType, SimpleFeature> polySource2 = (FeatureSource) layers.get(getRestrictedPolygonTypeRef());

        Object[] poly1 = polySource1.getFeatures().toArray();
        Object[] poly2 = polySource2.getFeatures().toArray();

        if (!envelope.contains(polySource1.getBounds())) {
            results.error((SimpleFeature) poly1[0],
                "Polygon Feature Source is not contained within the Envelope provided.");

            return false;
        }

        if (!envelope.contains(polySource2.getBounds())) {
            results.error((SimpleFeature) poly1[0],
                "Restricted Polygon Feature Source is not contained within the Envelope provided.");

            return false;
        }

        for (int i = 0; i < poly2.length; i++) {
            SimpleFeature tmp = (SimpleFeature) poly2[i];
            Geometry gt = (Geometry) tmp.getDefaultGeometry();

            for (int j = 0; j < poly1.length; j++) {
                SimpleFeature tmp2 = (SimpleFeature) poly1[j];
                Geometry gt2 = (Geometry) tmp2.getDefaultGeometry();

                if (gt2.within(gt)) {
                    return false;
                }
            }
        }

        return true;
    }
}
