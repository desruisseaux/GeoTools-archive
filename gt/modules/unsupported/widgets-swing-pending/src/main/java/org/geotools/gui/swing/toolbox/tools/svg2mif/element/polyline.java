package org.geotools.gui.swing.toolbox.tools.svg2mif.element;

import java.awt.Point;
import java.util.Vector;

public class polyline {
	
	private int numSections;
	private Point center;
	private Vector<polylineSection> sections;

	public int getNumSections() {
		return numSections;
	}
	public void setNumSections(int numSections) {
		this.numSections = numSections;
	}
	public Vector<polylineSection> getSections() {
		return sections;
	}
	public void setSections(Vector<polylineSection> sections) {
		this.sections = sections;
	}
	
	public polylineSection getSection(int index) {
		return (polylineSection)this.sections.elementAt(index);
	}
	
	public void addSection(polylineSection section) {
		this.sections.add(section);
		this.numSections += 1;
	}
	
	public polyline() {
		this.numSections = 0;
		this.sections = new Vector<polylineSection>();
		this.center = new Point(0, 0);
	}
}
