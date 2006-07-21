package org.geotools.gce.geotiff.IIOMetadataAdpaters;

/**
 * 
 * @author Simone Giannecchini
 * 
 */
public final class PixelScale {

	private double scaleX;

	private double scaleY;

	private double scaleZ;

	public PixelScale(double scaleX, double scaleY, double scaleZ) {
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.scaleZ = scaleZ;
	}

	public PixelScale() {
		this.scaleX = 0;
		this.scaleY = 0;
		this.scaleZ = 0;
	}

	public double getScaleX() {
		return scaleX;
	}

	public void setScaleX(double scaleX) {
		this.scaleX = scaleX;
	}

	public double getScaleY() {
		return scaleY;
	}

	public void setScaleY(double scaleY) {
		this.scaleY = scaleY;
	}

	public double getScaleZ() {
		return scaleZ;
	}

	public void setScaleZ(double scaleZ) {
		this.scaleZ = scaleZ;
	}

	public double[] getValues() {
		return new double[] { scaleX, scaleY, scaleZ };
	}

}
