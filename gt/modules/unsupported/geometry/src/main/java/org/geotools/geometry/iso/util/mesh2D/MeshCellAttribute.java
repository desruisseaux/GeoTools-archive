package org.geotools.geometry.iso.util.mesh2D;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.geotools.geometry.iso.util.elem2D.Node2D;
import org.geotools.geometry.iso.util.quadtree.QuadtreeCell;
import org.geotools.geometry.iso.util.quadtree.QuadtreeCellAttribute;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author roehrig
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MeshCellAttribute implements QuadtreeCellAttribute {

	protected Node2D node;

	public MeshCellAttribute() {
		super();
		this.node = null;
	}

	/**
	 * utility static method to get the cell node. Given a quastree cell,
	 * returns the node or null if the cell attribute is null
	 * 
	 * @param cell
	 * @return
	 */
	public static Point2D getNode(QuadtreeCell cell) {
		MeshCellAttribute att = (MeshCellAttribute) cell.attribute();
		return (att == null) ? null : att.getNode();
	}

	public Node2D getNode() {
		return this.node;
	}

	public void setNode(Node2D p) {
		this.node = p;
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
				cell0.setAttribute(new MeshCellAttribute());
		}
	}

	/* (non-Javadoc)
	 * @see org.arena.geo2D.quadtree.QuadtreeCellAttribute#getXML(org.w3c.dom.Document)
	 */
	public Element getXML(Document document,String name) {
		Element element = document.createElement( name ); 
		element.setAttribute( "nodeX", (this.node==null) ? "" : String.valueOf( this.node.x ) );
		element.setAttribute( "nodeY", (this.node==null) ? "" : String.valueOf( this.node.y ) );
		return element;

	}

	/* (non-Javadoc)
	 * @see org.arena.geo2D.quadtree.QuadtreeCellAttribute#setXML(org.w3c.dom.Element)
	 */
	public void setXML(Element element) {
		assert element.getNodeName().equals( this.getClass().getName() );
		double x = Double.valueOf( element.getAttribute("nodeX") );
		double y = Double.valueOf( element.getAttribute("nodeY") );
		this.node = new Node2D(x,y);
	}

}
