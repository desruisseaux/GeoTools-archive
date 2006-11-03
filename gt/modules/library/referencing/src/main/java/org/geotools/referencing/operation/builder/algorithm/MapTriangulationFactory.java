/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.referencing.operation.builder.algorithm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Class that implements methods for triangulation for RubberSheeting
 * transformation.
 *
 * @author Jan Jezek
 */
public class MapTriangulationFactory extends TriangulationFactory {
    /**
     * 
     * @param quad defines the area for transformation.
     * @param vectors represents pairs of identical points.
     * @throws TriangulationException thrown when the source points are outside the quad. 
     */
    public MapTriangulationFactory(Quadrilateral quad, MappedPosition[] vectors)
        throws TriangulationException {
        super(quad, vectors);
    }

    /**
     * Generates map of source and destination triangles.
     *
     * @return Map of a source and destination triangles.
     *
     * @throws TriangulationException thrown when the source points are outside
     *         the quad.
     */
    public Map getTriangleMap() throws TriangulationException {
        List taggedSourceTriangles = getTriangulation();
        final HashMap triangleMap = new HashMap();

        for (Iterator i = taggedSourceTriangles.iterator(); i.hasNext();) {
            final TINTriangle sourceTriangle = (TINTriangle) i.next();
            triangleMap.put(sourceTriangle,
                new TINTriangle(((MappedPosition) sourceTriangle.p0)
                    .getMappedposition(),
                    ((MappedPosition) sourceTriangle.p1).getMappedposition(),
                    ((MappedPosition) sourceTriangle.p2).getMappedposition()));
        }
        return triangleMap;
    }
}
