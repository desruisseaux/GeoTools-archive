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
 * The BoundingBox attributes indicate the edges of the bounting box in
 * units of the specified spatial reference system
 */
public class BoundingBox {
    private String srs;
    private String minX;
    private String minY;
    private String maxX;
    private String maxY;
    private String resX;
    private String resY;
    
    
    /**
     * @param srs
     * @param minX
     * @param minY
     * @param maxX
     * @param maxY
     */
    public BoundingBox(String srs, String minX, String minY, String maxX,
            String maxY) {
        super();
        this.srs = srs;
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
    public String getResX() {
        return resX;
    }
    public void setResX(String resX) {
        this.resX = resX;
    }
    public String getResY() {
        return resY;
    }
    public void setResY(String resY) {
        this.resY = resY;
    }
    public String getSrs() {
        return srs;
    }
    public void setSrs(String srs) {
        this.srs = srs;
    }
}
