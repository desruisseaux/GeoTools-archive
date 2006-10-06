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

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import org.geotools.geometry.DirectPosition2D;
import org.opengis.spatialschema.geometry.DirectPosition;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Simple polygons like three - sided (triangle) or four - sided
 * (qadrilateral), that are used for triangulation.
 *
 * @author Jan Jezek
 */
class ExtendedPolygon {
    /** Vertices of this polygon. */
    private DirectPosition[] vertices;

/**
     * Creates a polygon using specified vertices.
     * 
     * @param coordinates of vertices
     */
    ExtendedPolygon(DirectPosition[] coordinates) {
        //  super(factory.getCoordinateSequenceFactory().create(coordinates), factory);
        //      super(factory.createLinearRing(coordinates), null, factory);
        this.vertices = coordinates;
    }

    /**
     * Sets the vertices of this polygon.
     *
     * @param coordinates
     */
    public void setCoordinates(DirectPosition[] coordinates) {
        this.vertices = coordinates;
    }

    /**
     * Returns vertices of this polygon.
     *
     * @return vertices of this polygon.
     */
    public DirectPosition[] getPoints() {
        return vertices;
    }

    /**
     * Converts the DirectPosition into the JTS Coordinate.
     *
     * @return JTS Coordinate array.
     */
    private Coordinate[] getJTSCoordinates() {
        Coordinate[] jtsCoordinates = new Coordinate[vertices.length];

        for (int i = 0; i < vertices.length; i++) {
            jtsCoordinates[i] = new Coordinate(vertices[i].getCoordinates()[0],
                    vertices[i].getCoordinates()[1]);
        }

        return jtsCoordinates;
    }

    /**
     * Returns the LINESTRING representation in WKT.
     *
     * @return WKT format.
     */
    public String toString() {
        String wkt = "";
        Coordinate[] coords = getJTSCoordinates();

        for (int i = 0; i < coords.length; i++) {
            wkt = wkt + coords[i].x + " " + coords[i].y;

            if (i != (coords.length - 1)) {
                wkt = wkt + ", ";
            }
        }

        return "LINESTRING (" + wkt + ")";
    }

    /**
     * Test whether the coordinate is inside (or on a side) of ploygon.
     * This method is used insted of  super class method to speed up the
     * procces.
     *
     * @param dp Coordinate to be tested whether is inside of triangle.
     *
     * @return True if the point is inside (or is the vertex lies on the side,
     *         or is a vertex), false if not.
     */
    protected boolean containsOrIntersects(DirectPosition dp) {
        Coordinate p = new Coordinate(dp.getCoordinates()[0],
                dp.getCoordinates()[1]);

        //this is much  faster then simple geom.contains method.
        if (CGAlgorithms.isOnLine(p, this.getJTSCoordinates())) {
            return true;
        }

        if (CGAlgorithms.isPointInRing(p, this.getJTSCoordinates())) {
            return true;
        }

        return false;
    }

    /**
     * Returns whether v is one of the vertices of this polygon.
     *
     * @param p the candidate point
     *
     * @return whether v is equal to one of the vertices of this Triangle
     */
    public boolean hasVertex(DirectPosition p) {
        for (int i = 0; i < vertices.length; i++) {
            if (p == vertices[i]) {
                return true;
            }
        }

        return false;
    }

    /**
     * Enlarge the polygon using homothetic transformation method.
     *
     * @param scale of enlargement (when scale = 1 then polygon stays
     *        unchanged)
     */
    protected void enlarge(double scale) {
        double sumX = 0;
        double sumY = 0;

        for (int i = 0; i < vertices.length; i++) {
            sumX = sumX + vertices[i].getCoordinates()[0];
            sumY = sumY + vertices[i].getCoordinates()[1];
        }

        // The center of polygon is calculated.
        sumX = sumX / vertices.length;
        sumY = sumY / vertices.length;

        // The homothetic transformation is made.
        for (int i = 0; i < vertices.length; i++) {
            vertices[i].getCoordinates()[0] = (scale * (vertices[i]
                .getCoordinates()[0] - sumX)) + sumX;
            vertices[i].getCoordinates()[1] = (scale * (vertices[i]
                .getCoordinates()[1] - sumY)) + sumY;
        }
    }

    /**
     * Returns reduced coordinates of vertices so the first vertex has
     * [0,0] coordinats.
     *
     * @return The List of reduced vertives
     */
    protected List reduce() {
        //Coordinate[] redCoords = new Coordinate[coordinates.length];
        ArrayList redCoords = new ArrayList();

        for (int i = 0; i < vertices.length; i++) {
            redCoords.add(new DirectPosition2D(
                    vertices[i].getCoordinateReferenceSystem(),
                    vertices[i].getCoordinates()[0]
                    - vertices[0].getCoordinates()[0],
                    vertices[i].getCoordinates()[1]
                    - vertices[0].getCoordinates()[1]));
        }

        return redCoords;
    }

    /**
     * Returns whether this Polygon contains all of the the given
     * coordinates.
     *
     * @param coordinate of coordinates
     *
     * @return whether this Polygon contains the all of the given coordinates
     */
    protected boolean containsAll(List coordinate) {
        for (Iterator i = coordinate.iterator(); i.hasNext();) {
            if (!this.containsOrIntersects((DirectPosition) i.next())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns a copy of this.
     *
     * @return copy of this object.
     */
    public Object clone() {
        return new ExtendedPolygon(vertices);
    }
}
