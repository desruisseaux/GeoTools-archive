/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004, Geotools Project Managment Committee (PMC)
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

package org.geotools.demo.referencing;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.CoordinateFilter;
import org.geotools.ct.MathTransform;
import org.geotools.pt.CoordinatePoint;

/**
 * A coordinate filter that can be used to tranform each coordinate 
 * in a geometry. This is applied to each individual coordinate,
 * so it does not work well when the polygon goes accross 180 + central lat 
 * or 90 + lat of origin. There are also some problems with projections that
 * have issues with 90 lat. 
 *
 * @version $Id:
 * @author rschulz
 */
public class TransformationCoordinateFilter implements CoordinateFilter{
    /* Transform to apply to each coordinate*/
    private MathTransform transform;
    
    /** Creates a new instance of TransformationCoordinateFilter */
    public TransformationCoordinateFilter(MathTransform transform) {
        this.transform = transform;
    }
    
    /*performs a transformation on a coordinate*/
    public void filter(com.vividsolutions.jts.geom.Coordinate coordinate) {
        CoordinatePoint point = new CoordinatePoint(coordinate.x, coordinate.y);
        try {
            point = transform.transform(point, point);
        }
        catch (org.opengis.referencing.operation.TransformException e) {
            System.out.println("Error in transformation: " + e);
        }
        
        coordinate.x = point.ord[0];
        coordinate.y = point.ord[1];
    }
    
}
