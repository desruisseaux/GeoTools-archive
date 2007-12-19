package org.geotools.gui.swing.toolbox.tools.svg2mif.element;

import java.util.Vector;

public class mifmidLayer {

	private String fichier;
	private DPoint[] bounds = {new DPoint(Double.MAX_VALUE, Double.MAX_VALUE), new DPoint(Double.MIN_VALUE, Double.MIN_VALUE)};
	private String sizeUnit;
	private int nbObjets;
	private Vector<Object> objets;
	
	public mifmidLayer(String fichier) {
		this.fichier = fichier;
		this.objets = new Vector<Object>();
	}

	public String getFichier() {
		return fichier;
	}

	public void setFichier(String fichier) {
		this.fichier = fichier;
	}

	public DPoint getLBound() {
		return this.bounds[0];
	}
	
	public DPoint getHBound() {
		return this.bounds[1];
	}

	public void setBoundX(int index, double x) {
		this.bounds[index].x = x;
	}

	public void setBoundY(int index, double y) {
		this.bounds[index].y = y;
	}
	
	public String getSizeUnit() {
		return sizeUnit;
	}

	public void setSizeUnit(String sizeUnit) {
		this.sizeUnit = sizeUnit;
	}
	
	public void addObject(Object o) {
		this.objets.add(o);
		this.nbObjets += 1;
	}
	
	public Object getObjet(int index) {
		return this.objets.elementAt(index);
	}
	
	public int getNumObjets() {
		return this.nbObjets;
	}

}
