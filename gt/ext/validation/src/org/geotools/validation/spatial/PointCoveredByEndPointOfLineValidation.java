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
import org.geotools.feature.Feature;
import org.geotools.validation.ValidationResults;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;


/**
 * PointCoveredByEndPointOfLineValidation purpose.
 * 
 * <p>
 * Checks to ensure the Point is covered by an endpoint of the Line.
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: dmzwiers $ (last modification)
 * @source $URL$
 * @version $Id$
 */
public class PointCoveredByEndPointOfLineValidation
    extends PointLineAbstractValidation {
    /**
     * PointCoveredByEndPointOfLineValidation constructor.
     * 
     * <p>
     * Description
     * </p>
     */
    public PointCoveredByEndPointOfLineValidation() {
        super();
    }

    /**
     * Ensure Point is covered by a Line end point.
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
        FeatureSource lineSource = (FeatureSource) layers.get(getRestrictedLineTypeRef());
        FeatureSource pointSource = (FeatureSource) layers.get(getPointTypeRef());

        Object[] points = pointSource.getFeatures().collection().toArray();
        Object[] lines = lineSource.getFeatures().collection().toArray();

        if (!envelope.contains(pointSource.getBounds())) {
            results.error((Feature) points[0],
                "Point Feature Source is not contained within the Envelope provided.");

            return false;
        }

        if (!envelope.contains(lineSource.getBounds())) {
            results.error((Feature) lines[0],
                "Line Feature Source is not contained within the Envelope provided.");

            return false;
        }

        for (int i = 0; i < lines.length; i++) {
            Feature tmp = (Feature) lines[i];
            Geometry gt = tmp.getDefaultGeometry();

            if (gt instanceof LineString) {
                LineString ls = (LineString) gt;
                Point str = ls.getStartPoint();
                Point end = ls.getEndPoint();

                for (int j = 0; j < points.length; j++) {
                    Feature tmp2 = (Feature) points[j];
                    Geometry gt2 = tmp2.getDefaultGeometry();

                    if (gt2 instanceof Point) {
                        Point pt = (Point) gt2;

                        if (pt.equalsExact(str) || pt.equalsExact(end)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}
