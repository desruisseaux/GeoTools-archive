/*
 * Created on Sep 10, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.ows;

/**
 * @author Richard Gould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LatLonBoundingBox {

	protected double minX;
	protected double minY;
	protected double maxX;
	protected double maxY;
	
	public LatLonBoundingBox() {
		
	}

	public LatLonBoundingBox(double minX, double minY, double maxX, double maxY) {
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
	}
	
	/**
	 * The maxX value is the higher X coordinate value
	 * @return  the bounding box's maxX value
	 */
	public double getMaxX() {
	    return maxX;
	}

	/**
	 * The maxX value is the higher X coordinate value
	 * @param maxX  the new value for maxX. Should be greater than minX.
	 */
	public void setMaxX(double maxX) {
	    this.maxX = maxX;
	}

	/**
	 * The maxY value is the higher Y coordinate value
	 * @return  the bounding box's maxY value
	 */
	public double getMaxY() {
	    return maxY;
	}

	/**
	 * The maxY value is the higher Y coordinate value
	 * @param maxY  the new value for maxY. Should be greater than minY.
	 */
	public void setMaxY(double maxY) {
	    this.maxY = maxY;
	}

	/**
	 * The minX value is the lower X coordinate value
	 * @return  the bounding box's minX value
	 */
	public double getMinX() {
	    return minX;
	}

	/**
	 * The minX value is the lower X coordinate value
	 * @param minX  the new value for minX. Should be less than maxX.
	 */
	public void setMinX(double minX) {
	    this.minX = minX;
	}

	/**
	 * The minY value is the lower Y coordinate value
	 * @return  the bounding box's minY value
	 */
	public double getMinY() {
	    return minY;
	}

	/**
	 * The minY value is the lower Y coordinate value
	 * @param minY  the new value for minY. Should be less than maxY.
	 */
	public void setMinY(double minY) {
	    this.minY = minY;
	}

}
