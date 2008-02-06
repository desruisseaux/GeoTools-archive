package org.geotools.gui.swing.toolbox.widgettool.svg2mif.element;

public class DPoint {

	public double x;
	public double y;
	
	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public DPoint(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public Object clone() {
		DPoint pt2 = new DPoint(this.x, this.y);
		return pt2;
	}
}
