/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
package org.geotools.data.ows;

/**
 * A pair of coordinates and a reference system that represents a section of
 * the Earth
 *
 * @author Richard Gould
 */
public class BoundingBox extends LatLonBoundingBox {
    /** Represents the Coordinate Reference System this bounding box is in */
    private String crs;

    /**
     * Construct an empty BoundingBox
     */
    public BoundingBox() {
        super();
    }

    /**
     * Create a bounding box with the specified properties
     *
     * @param crs The Coordinate Reference System this bounding box is in
     * @param minX
     * @param minY
     * @param maxX
     * @param maxY
     */
    public BoundingBox(String crs, double minX, double minY, double maxX,
        double maxY) {
        super(minX, minY, maxX, maxY);
        this.crs = crs;
    }

    /**
     * The CRS is bounding box's Coordinate Reference System
     *
     * @return the CRS/SRS value
     */
    public String getCrs() {
        return crs;
    }

    /**
     * The CRS is bounding box's Coordinate Reference System
     *
     * @param crs the new value for the CRS/SRS
     */
    public void setCrs(String crs) {
        this.crs = crs;
    }
}
