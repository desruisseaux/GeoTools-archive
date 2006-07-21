package org.geotools.gce.geotiff.IIOMetadataAdpaters;

/**
 * 
 * @author Simone Giannecchini
 * 
 */
public final class TiePoint {
	private double[] values;

	public TiePoint(double i, double j, double k, double x, double y, double z) {
		values = new double[6];
		set(i, j, k, x, y, z);
	}

	public void set(double i, double j, double k, double x, double y, double z) {
		values[0] = i;
		values[1] = j;
		values[2] = k;
		values[3] = x;
		values[4] = y;
		values[5] = z;
	}

	public double getValueAt(int index) {
		if (index < 0 || index > 5)
			throw new IllegalArgumentException(
					"Provided index should be between 0 and 5");
		return values[index];
	}

	public double[] getData() {
		return values;
	}
}