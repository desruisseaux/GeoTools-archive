/*
 * Created on Jun 30, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.getCapabilities;

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
