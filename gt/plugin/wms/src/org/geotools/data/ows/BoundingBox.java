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
package org.geotools.data.ows;

/**
 * A pair of coordinates and a reference system that represents a section of the Earth
 * 
 * @author Richard Gould
 */
public class BoundingBox {
    /**
     * Represents the Coordinate Reference System this bounding box is in
     */
    private String crs;
    
    private double minX;
    private double minY;
    private double maxX;
    private double maxY;
    
    public BoundingBox() {
    }
    
    /**
     * Create a bounding box with the specified properties
     * @param crs The Coordinate Reference System this bounding box is in
     * @param minX
     * @param minY
     * @param maxX
     * @param maxY
     */
    public BoundingBox(String crs, double minX, double minY, double maxX,
            double maxY) {
        this.crs = crs;
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }
    public String getCrs() {
        return crs;
    }
    public void setCrs(String crs) {
        this.crs = crs;
    }
    public double getMaxX() {
        return maxX;
    }
    public void setMaxX(double maxX) {
        this.maxX = maxX;
    }
    public double getMaxY() {
        return maxY;
    }
    public void setMaxY(double maxY) {
        this.maxY = maxY;
    }
    public double getMinX() {
        return minX;
    }
    public void setMinX(double minX) {
        this.minX = minX;
    }
    public double getMinY() {
        return minY;
    }
    public void setMinY(double minY) {
        this.minY = minY;
    }
}
