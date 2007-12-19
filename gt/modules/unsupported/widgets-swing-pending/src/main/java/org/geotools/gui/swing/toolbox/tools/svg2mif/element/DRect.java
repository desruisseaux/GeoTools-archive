package org.geotools.gui.swing.toolbox.tools.svg2mif.element;

public class DRect {

	private double x1;
	private double y1;
	private double x2;
	private double y2;
	
	public DRect() {
		this.x1 = 0;
		this.y1 = 0;
		this.x2 = 0;
		this.y2 = 0;
	}
	
	public DRect(double a1, double b1, double a2, double b2) {
		this.x1 = a1;
		this.y1 = b1;
		this.x2 = a2;
		this.y2 = b2;
	}

	public double getX1() {
		return x1;
	}

	public void setX1(double x1) {
		this.x1 = x1;
	}

	public double getX2() {
		return x2;
	}

	public void setX2(double x2) {
		this.x2 = x2;
	}

	public double getY1() {
		return y1;
	}

	public void setY1(double y1) {
		this.y1 = y1;
	}

	public double getY2() {
		return y2;
	}

	public void setY2(double y2) {
		this.y2 = y2;
	}
}
