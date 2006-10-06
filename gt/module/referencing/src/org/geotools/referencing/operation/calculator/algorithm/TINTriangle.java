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
package org.geotools.referencing.operation.calculator.algorithm;

import org.geotools.geometry.DirectPosition2D;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.DirectPosition;
import java.util.ArrayList;
import java.util.List;


/**
 * A triangle, with special methods for use with RubberSheetTransform.
 */
public class TINTriangle extends ExtendedPolygon {
    /** The first vertex. */
    public DirectPosition p0;

    /** The second vertex. */
    public DirectPosition p1;

    /** The third */
    public DirectPosition p2;

/**
     * Creates a Triangle.
     * @param p0 one vertex
     * @param p1 another vertex
     * @param p2 another vertex
     */
    protected TINTriangle(DirectPosition p0, DirectPosition p1,
        DirectPosition p2) {
        super(new DirectPosition[] { p0, p1, p2, p0 });
        this.p0 = p0;
        this.p1 = p1;
        this.p2 = p2;
    }

    /**
     * Returns the CircumCicle of the this TINTriangle.
     *
     * @return Returns the CircumCicle of the this TINTriangle
     */
    protected Circle getCircumCicle() {
        //DirectPosition center = new DirectPosition2D();
        List reducedVertices = reduce();

        CoordinateReferenceSystem crs = ((DirectPosition) reducedVertices
            .get(1)).getCoordinateReferenceSystem();

        double x1 = ((DirectPosition) reducedVertices.get(1)).getCoordinates()[0];
        double y1 = ((DirectPosition) reducedVertices.get(1)).getCoordinates()[1];

        double x2 = ((DirectPosition) reducedVertices.get(2)).getCoordinates()[0];
        double y2 = ((DirectPosition) reducedVertices.get(2)).getCoordinates()[1];

        // Calculation of Circumcicle center
        double t = (0.5 * (((x1 * x1) + (y1 * y1)) - (x1 * x2) - (y1 * y2))) / ((y1 * x2)
            - (x1 * y2));

        //t = Math.abs(t);
        DirectPosition2D center = new DirectPosition2D(crs,
                (x2 / 2) - (t * y2) + p0.getCoordinates()[0],
                (y2 / 2) + (t * x2) + p0.getCoordinates()[1]);

        return new Circle(center.getPosition(),
            center.distance(new DirectPosition2D(p0)));
    }

    /**
     * Returns the three triangles that are created by splitting this
     * TINTriangle at a newVertex.
     *
     * @param newVertex the split point (must be inside triangle).
     *
     * @return three Triangles created by splitting this TINTriangle at a
     *         newVertex.
     */
    public List subTriangles(DirectPosition newVertex) {
        ArrayList triangles = new ArrayList();
        triangles.add(new TINTriangle(p0, p1, newVertex));
        triangles.add(new TINTriangle(p1, p2, newVertex));
        triangles.add(new TINTriangle(p2, p0, newVertex));

        return triangles;
    }
}
