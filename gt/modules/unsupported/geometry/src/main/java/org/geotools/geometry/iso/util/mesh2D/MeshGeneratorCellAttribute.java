package org.geotools.geometry.iso.util.mesh2D;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;

import org.geotools.geometry.iso.util.algorithm2D.AlgoRectangle2D;
import org.geotools.geometry.iso.util.elem2D.Edge2D;
import org.geotools.geometry.iso.util.quadtree.QuadtreeCell;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author roehrig
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MeshGeneratorCellAttribute extends MeshCellAttribute {

	public static final byte NOCOLOR = 0;
	
	public static final byte BLACK = 1;
	
	public static final byte GREY = 2;
	
	public static final byte WHITE = 3;

	protected byte color = NOCOLOR;
	
	private ArrayList<Edge2D> edgeList = new ArrayList<Edge2D>();

	public MeshGeneratorCellAttribute() {
		super();
		this.node = null;
	}

	public byte getColor() {
		return this.color;
	}

//	public void setColor(byte color ) {
//		this.color = color;
//	}
//
	public void resetColor() {
		this.color = NOCOLOR;
	}

	public void setGrey() {
		this.color = GREY;
	}

	public void setWhite() {
		this.color = WHITE;
	}

	public void setBlack() {
		this.color = BLACK;
	}

	public boolean isGrey() {
		return this.color == GREY;
	}

	public boolean isWhite() {
		return this.color == WHITE;
	}

	public boolean isBlack() {
		return this.color == BLACK;
	}

	public void divide(QuadtreeCell cell, final Rectangle2D env) {
		QuadtreeCell[] children = cell.getChildren();
		if (children == null)
			return;
		cell.setAttribute(null);
		if (this.node != null) {
			double cx = env.getCenterX();
			double cy = env.getCenterY();
			Point2D dp = this.node;
			if (dp.getX() < cx) { // West
				if (dp.getY() < cy) { // South
					cell.getChild(QuadtreeCell.SW).setAttribute(this);
				} else { // North
					cell.getChild(QuadtreeCell.NW).setAttribute(this);
				}
			} else { //' East
				if (dp.getY() < cy) { //' South
					cell.getChild(QuadtreeCell.SE).setAttribute(this);
				} else { //' North
					cell.getChild(QuadtreeCell.NE).setAttribute(this);
				}
			}
		}
		for (int i = 0; i < children.length; ++i) {
			QuadtreeCell cell0 = children[i];
			if (cell0.attribute() == null)
				cell0.setAttribute(new MeshGeneratorCellAttribute());
		}
		if (!this.edgeList.isEmpty()) {
			
			// children: this.cSW, this.cNW, this.cSE, this.cNE
			// Quadrants SW,NW,SE,NE 
			Rectangle2D.Double[] childRecs = cell.createQuadrantChildren(env);
			for (int i = 0; i < childRecs.length; ++i) {
				Rectangle2D.Double childRec = childRecs[i];
				AlgoRectangle2D.setScale(childRec,1.001);
			}
			ArrayList<Edge2D> edges = new ArrayList<Edge2D>(this.edgeList);
			for (Iterator<Edge2D> it = edges.iterator(); it.hasNext(); ) {
				Edge2D e = it.next();
				for (int i = 0; i < childRecs.length; ++i) {
					QuadtreeCell childCell = children[i];
					Rectangle2D.Double childRec = childRecs[i];
					if (childRec.intersectsLine(e)); {
						((MeshGeneratorCellAttribute)childCell.attribute()).insertEdge(e);
					}
				}
			}
		}
	}

	public void insertEdge(Edge2D e) {
		this.edgeList.add(e);
	}

	public void removeEdge(Edge2D e) {
		this.edgeList.remove(e);
	}

	public void hasEdge(Edge2D e) {
		this.edgeList.contains(e);
	}
	public ArrayList<Edge2D> getEdges() {
		return this.edgeList;	
	}

	public static int getColor(QuadtreeCell cell) {
		MeshGeneratorCellAttribute att = (MeshGeneratorCellAttribute) cell.attribute();
		return (att == null) ? 0 : att.getColor();
	}

	/**
	 * @return
	 */
	public boolean hasColor() {
		return this.color != NOCOLOR;
	}

	public Element getXML(Document document,String name) {
		Element element = document.createElement( name ); 
		element.setAttribute( "color", String.valueOf( this.color ) );
		Element superElement = super.getXML(document,"meshCellAttribute");
		element.appendChild(superElement);
		return element;
	}

}
