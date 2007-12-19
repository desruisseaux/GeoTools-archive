package org.geotools.gui.swing.toolbox.tools.svg2mif.element;

public class DDimension {
	
	private double width;
	private double height;
	
	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public DDimension() {
		this.width = 0;
		this.height = 0;
	}
	
	public DDimension(double l, double h) {
		this.width = l;
		this.height = h;
	}
	
	public String toString() {
		String s = new String();
		s = s + "Width : " + this.width + ", Height : " + height;
		return s;
	}
}
