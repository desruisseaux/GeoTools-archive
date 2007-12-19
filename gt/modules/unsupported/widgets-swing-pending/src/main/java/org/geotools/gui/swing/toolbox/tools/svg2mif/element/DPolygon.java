package org.geotools.gui.swing.toolbox.tools.svg2mif.element;

import java.util.Vector;

public class DPolygon {

	private int numPoints;
	private Vector<DPoint> points;
	private DPoint center;

	public DPolygon() {
		this.numPoints = 0;
		this.points = new Vector<DPoint>();
		this.center = new DPoint(0, 0);
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
		DPolygon p2 = new DPolygon();
		for (i = 0; i < this.getNumPoints(); i++) {
			p2.addPoint((DPoint)this.points.elementAt(i));
		}
		p2.setCenter(this.getCenter());
		return p2;
	}

	public DPoint getCenter() {
		return center;
	}

	public void setCenter(DPoint center) {
		this.center = center;
	}

}
