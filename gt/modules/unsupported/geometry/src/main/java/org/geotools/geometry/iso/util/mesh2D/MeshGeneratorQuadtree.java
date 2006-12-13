/*
 * Created on 19.01.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.geometry.iso.util.mesh2D;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;

import org.geotools.geometry.iso.util.elem2D.Edge2D;
import org.geotools.geometry.iso.util.elem2D.Node2D;
import org.geotools.geometry.iso.util.quadtree.QuadtreeCell;
import org.geotools.geometry.iso.util.quadtree.QuadtreeCellAttribute;
import org.geotools.geometry.iso.util.quadtree.QuadtreeCellEnvelope;
import org.geotools.geometry.iso.util.quadtree.QuadtreeCell.CellNeighbour;


/**
 * @author roehrig
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MeshGeneratorQuadtree extends  MeshQuadtree {

	/**
	 * creates a grid composed of only one (quadtree) roots 
	 * @param env
	 * @param cellSize
	 */
	public MeshGeneratorQuadtree(Rectangle2D env) {
		super(env, -1.0);
	}

	/**
	 * creates a grid, which number of rows and columns will be calculated from
	 * the envelope and the given roots size. The envelope can (and will be
	 * mostly) expanded to fit to the roots size
	 * 
	 * @param env
	 * @param cellSize
	 */
	public MeshGeneratorQuadtree(Rectangle2D env, double cellSize) {
		super(env, cellSize);
	}

	public QuadtreeCellAttribute createCellAttribute() {
		return new MeshGeneratorCellAttribute();
	}

	
	/**
	 * inserts the directPosition into the roots and set the roots color grey
	 * Divides the roots if it already contains a directPosition.
	 * 
	 * returns the QuadtreeCellEnvelope with containing the node 
	 * 
	 * Dividing a roots implies: 1) relocation of the node to the new child
	 * roots 2) setting this.point = null and me.color = none
	 * 
	 */
    public QuadtreeCellEnvelope insertNode(Node2D p) {
    	QuadtreeCellEnvelope qtCellEnv = super.insertNode(p);
		if (qtCellEnv != null) {
			MeshGeneratorCellAttribute att = (MeshGeneratorCellAttribute) qtCellEnv.getCell().attribute();
			att.setGrey();
		}
		return qtCellEnv;
	}
    
	public Edge2D findEdge(Node2D p0, Node2D p1) {
		return p0.getEdge(p1);
//		QuadtreeCellEnvelope cellEnvelope = this.findCell(p0);
//		QuadtreeCell cell = (QuadtreeCell)cellEnvelope.getCell();
//		MeshGeneratorCellAttribute att = (MeshGeneratorCellAttribute)cell.attribute();
//		for (Iterator it = att.getEdges().iterator(); it.hasNext();) {
//			Edge2D edge = (Edge2D)it.next();
//			Point2D p2 = edge.getP1();
//			Point2D p3 = edge.getP2();
//			if ((p0.equals(p2) && p1.equals(p3)) || (p0.equals(p3) && p1.equals(p2))) return edge;
//		}
//		return null;
	}

	public void insertEdgeIntoQuadtree(Edge2D edge) {
		ArrayList cells = this.getCellsIntersectingLine(edge);
		for (Iterator it = cells.iterator(); it.hasNext();) {
			QuadtreeCellEnvelope cellEnv = (QuadtreeCellEnvelope)it.next();
			QuadtreeCell cell = cellEnv.getCell();
			MeshGeneratorCellAttribute att = (MeshGeneratorCellAttribute)cell.attribute();
			att.insertEdge(edge);
		}		
	}
 
	public void removeEdgeFromQuadtree(Edge2D edge) {
		// remove the edge from the edge lists of each cell intersecting the front
		ArrayList cells = this.getCellsIntersectingLine(edge);
		for (Iterator it = cells.iterator(); it.hasNext();) {
			QuadtreeCellEnvelope cellEnv = (QuadtreeCellEnvelope)it.next();
			QuadtreeCell cell = cellEnv.getCell();
			MeshGeneratorCellAttribute att = (MeshGeneratorCellAttribute)cell.attribute();
			att.removeEdge(edge);
		}			
	}

	public void insertEdgesIntoQuadtree(ArrayList<Edge2D> edges) {
		if (edges != null) {
			for (Iterator it1 = edges.iterator(); it1.hasNext();) {
				Edge2D edge = (Edge2D) it1.next();
				insertEdgeIntoQuadtree(edge);
			}
		}
	}

	/**
	 * @param edges
	 */
	public void setEdgeIntersectingCellsGrey(ArrayList<Edge2D> edges) {
		if ( edges==null ) return;
		for (Iterator it = edges.iterator(); it.hasNext(); ) {
			Edge2D edge = (Edge2D)it.next();
			ArrayList cells = this.getCellsIntersectingLine(edge);
			for (Iterator it1 = cells.iterator(); it1.hasNext(); ) {
				QuadtreeCellEnvelope cellEnv = (QuadtreeCellEnvelope)it1.next();
				QuadtreeCell cell = (QuadtreeCell)cellEnv.getCell();
				MeshGeneratorCellAttribute att = (MeshGeneratorCellAttribute) cell.attribute();
				att.setGrey();
			}
		}
	}

	/**
	 * @param name
	 */
	public void setNodeContainingCellsGrey(ArrayList<Node2D> nodes) {
		if ( nodes==null ) return;
		for (Iterator<Node2D>  it = nodes.iterator(); it.hasNext(); ) {
			Node2D post = it.next();
			QuadtreeCellEnvelope cellEnv = this.findCell(post);
			QuadtreeCell cell = (QuadtreeCell)cellEnv.getCell();
			MeshGeneratorCellAttribute att = (MeshGeneratorCellAttribute) cell.attribute();
			att.setGrey();
		}
	}

	/**
	 * 
	 */
	public void fillWhiteCellsWithNodes() {
		ArrayList<QuadtreeCellEnvelope> qtEnvs = new ArrayList<QuadtreeCellEnvelope>();
		this.getCellEnvelopes(qtEnvs);
		for (int i=0; i<qtEnvs.size();++i) {
			QuadtreeCellEnvelope qtEnv = (QuadtreeCellEnvelope)qtEnvs.get(i);
			MeshGeneratorCellAttribute cellAtt = (MeshGeneratorCellAttribute) qtEnv.getCell().attribute();
			if (cellAtt.isWhite()) {
				cellAtt.node = new Node2D(qtEnv.getCenterX(),qtEnv.getCenterY());
			}
		}
	}

	/**
	 * 
	 */
	public void resetColors(ArrayList<QuadtreeCellEnvelope> qtEnvs) {
		if (qtEnvs==null) {
			qtEnvs = new ArrayList<QuadtreeCellEnvelope>();
			this.getCellEnvelopes(qtEnvs);
		}
		for (int i=0, n=qtEnvs.size(); i<n; ++i) {
			QuadtreeCellEnvelope qtEnv = (QuadtreeCellEnvelope)qtEnvs.get(i);
			MeshGeneratorCellAttribute att = (MeshGeneratorCellAttribute) qtEnv.getCell().attribute();
			att.resetColor();
		}
		
	}

	/**
	 * 
	 */
	public void setCellColors(Area area, ArrayList<Edge2D> edges, ArrayList<Node2D> nodes) {

		ArrayList<QuadtreeCellEnvelope> qtEnvs = new ArrayList<QuadtreeCellEnvelope>();
		this.getCellEnvelopes(qtEnvs);

		this.resetColors(qtEnvs);

		this.setEdgeIntersectingCellsGrey(edges);

		this.setNodeContainingCellsGrey(nodes);

		for (int i=0, n=qtEnvs.size(); i<n; ++i) {
			QuadtreeCellEnvelope qtEnv = (QuadtreeCellEnvelope)qtEnvs.get(i);
			MeshGeneratorCellAttribute att = (MeshGeneratorCellAttribute) qtEnv.getCell().attribute();
			if (!att.hasColor()) {
				if (area.contains(qtEnv)) {
					/** cells completly within are set white */
					this.setWhiteCellColor(qtEnv.getCell(),qtEnv);
				} else {
					/** cells completly outside are set white */
					this.setBlackCellColor(qtEnv.getCell(),qtEnv);
				}
			}
		}
	}
	
	private void setWhiteCellColor(QuadtreeCell cell, Rectangle2D env) {
		
		MeshGeneratorCellAttribute catt = (MeshGeneratorCellAttribute) cell.attribute();
		
		if (catt.isWhite()) return;
		
		catt.setWhite();
		
		ArrayList<CellNeighbour> cellNeighbors = cell.findLeafCellNeighbors(env);
		
		ArrayList<CellNeighbour> newCellNeighbors = new ArrayList<CellNeighbour>();
		
		while(!cellNeighbors.isEmpty()) {
			
			newCellNeighbors.clear(); 
			
			for (Iterator<CellNeighbour> it = cellNeighbors.iterator(); it.hasNext(); ) {

				CellNeighbour cn = it.next();
				
				MeshGeneratorCellAttribute att = (MeshGeneratorCellAttribute) cn.cell.attribute();
				
				if (!att.hasColor()) {
					att.setWhite();
					newCellNeighbors.addAll( cn.cell.findLeafCellNeighbors(cn.env) );
				}
				

			}
			
			cellNeighbors.clear();
			
			for (Iterator<CellNeighbour> it = newCellNeighbors.iterator(); it.hasNext();  ) {
				
				CellNeighbour cn = it.next();

				MeshGeneratorCellAttribute att = (MeshGeneratorCellAttribute) cn.cell.attribute();
				
				if (!att.hasColor()) {
					cellNeighbors.add(cn);					
				}
			}
		}
	}
	
	private void setBlackCellColor(QuadtreeCell cell, Rectangle2D env) {
		
		MeshGeneratorCellAttribute catt = (MeshGeneratorCellAttribute) cell.attribute();
		
		if (catt.isBlack()) return;
		
		catt.setBlack();
		
		ArrayList<CellNeighbour> cellNeighbors = cell.findLeafCellNeighbors(env);
		
		ArrayList<CellNeighbour> newCellNeighbors = new ArrayList<CellNeighbour>();
		
		while(!cellNeighbors.isEmpty()) {
			
			newCellNeighbors.clear(); 
			
			for (Iterator<CellNeighbour> it = cellNeighbors.iterator(); it.hasNext(); ) {

				CellNeighbour cn = it.next();
				
				MeshGeneratorCellAttribute att = (MeshGeneratorCellAttribute) cn.cell.attribute();
				
				if (!att.hasColor()) {
					att.setBlack();
					newCellNeighbors.addAll( cn.cell.findLeafCellNeighbors(cn.env) );
				}
				

			}
			
			cellNeighbors.clear();
			
			for (Iterator<CellNeighbour> it = newCellNeighbors.iterator(); it.hasNext();  ) {
				
				CellNeighbour cn = it.next();

				MeshGeneratorCellAttribute att = (MeshGeneratorCellAttribute) cn.cell.attribute();
				
				if ( !att.hasColor() ) {
					cellNeighbors.add(cn);					
				}
			}
		}
	}

}

