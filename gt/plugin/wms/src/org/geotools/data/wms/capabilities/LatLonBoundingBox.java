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
package org.geotools.data.wms.capabilities;

/**
 * @author rgould
 *
 * The LatLonBoundingBox attributes indicate the edges of the 
 * enclosing rectangle in latitude/longitude decimal degrees 
 * (as in SRS EPSG:4326 [WGS1984 lat/lon]
 */
public class LatLonBoundingBox {
    private String minX;
    private String minY;
    private String maxX;
    private String maxY;
    
    
    /**
     * @param minX
     * @param minY
     * @param maxX
     * @param maxY
     */
    public LatLonBoundingBox(String minX, String minY, String maxX, String maxY) {
        super();
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }
    public String getMaxX() {
        return maxX;
    }
    public void setMaxX(String maxX) {
        this.maxX = maxX;
    }
    public String getMaxY() {
        return maxY;
    }
    public void setMaxY(String maxY) {
        this.maxY = maxY;
    }
    public String getMinX() {
        return minX;
    }
    public void setMinX(String minX) {
        this.minX = minX;
    }
    public String getMinY() {
        return minY;
    }
    public void setMinY(String minY) {
        this.minY = minY;
    }
}
