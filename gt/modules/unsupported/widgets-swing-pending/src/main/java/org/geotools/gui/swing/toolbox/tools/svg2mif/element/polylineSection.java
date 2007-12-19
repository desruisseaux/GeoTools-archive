package org.geotools.gui.swing.toolbox.tools.svg2mif.element;

import java.util.Vector;

public class polylineSection {

	private int numPoints;
	private Vector<DPoint> points;

	public polylineSection() {
		this.numPoints = 0;
		this.points = new Vector<DPoint>();
	}
	
	public int getNumPoints() {
		return numPoints;
	}
	public void setNumPoints(int numPoints) {
		this.numPoints = numPoints;
	}
	public Vector<DPoint> getPoints() {
		return points;
	}
	public void setPoints(Vector<DPoint> points) {
		this.points = points;
	}
	
	public DPoint getPoint(int index) {
		return (DPoint)this.points.elementAt(index);
	}
	
	public void addPoint(double x, double y) {
		this.points.add(new DPoint(x, y));
		this.numPoints += 1;
	}
	
	public void addPoint(DPoint p) {
		this.points.add(p);
		this.numPoints += 1;
	}
	
	public Object clone() {
		int i = 0;
		polylineSection ps2 = new polylineSection();
		for (i = 0; i < this.getNumPoints(); i++) {
			ps2.addPoint((DPoint)this.points.elementAt(i));
		}
		return ps2;
	}
	
}
