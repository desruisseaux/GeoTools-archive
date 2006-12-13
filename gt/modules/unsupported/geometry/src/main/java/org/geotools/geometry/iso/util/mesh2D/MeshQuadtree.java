/*
 * Created on 19.01.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.geometry.iso.util.mesh2D;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.geotools.geometry.iso.util.algorithm2D.AlgoRectangle2D;
import org.geotools.geometry.iso.util.elem2D.Edge2D;
import org.geotools.geometry.iso.util.elem2D.Geo2DFactory;
import org.geotools.geometry.iso.util.elem2D.Node2D;
import org.geotools.geometry.iso.util.elem2D.Simplex2D;
import org.geotools.geometry.iso.util.elem2D.Triangle2D;
import org.geotools.geometry.iso.util.quadtree.Quadtree;
import org.geotools.geometry.iso.util.quadtree.QuadtreeCell;
import org.geotools.geometry.iso.util.quadtree.QuadtreeCellAttribute;
import org.geotools.geometry.iso.util.quadtree.QuadtreeCellEnvelope;
import org.geotools.geometry.iso.util.quadtree.QuadtreeRoot;


/**
 * @author roehrig
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MeshQuadtree extends Quadtree {

	/**
	 * creates a grid composed of only one (quadtree) roots 
	 * @param env
	 * @param cellSize
	 */
	public MeshQuadtree(Rectangle2D env) {
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
	public MeshQuadtree(Rectangle2D env, double cellSize) {
		super(env, cellSize);
	}

	public QuadtreeCellAttribute createCellAttribute() {
		return new MeshCellAttribute();
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
        if ( this.contains(p) ) {
			for (int i = 0; i <= QuadtreeCell.maxQTLevel; ++i) {
				QuadtreeCellEnvelope qtCellEnv = this.findCell(p);
				if (qtCellEnv == null) {
					return null;
				}
				QuadtreeCell cell = qtCellEnv.getCell();
				MeshCellAttribute att = (MeshCellAttribute) cell.attribute();
				if (att.getNode() == null) {
					att.setNode(p);
					return qtCellEnv;
				}
				if (att.getNode().equals(p)) {
					return qtCellEnv;				
				}
				if (cell.isLeaf()) {
					cell.divide(qtCellEnv);
				}
			}
        }
		return null;
	}
    
	public void balanceRoot() {
		//while (balanceRootLevel());
	}
		
	private boolean balanceRootLevel() {
		for (int i = 0; i < this.getNRows(); ++i) {
			for (int j = 0; j < this.getNCols(); ++j) {
				QuadtreeCell cell = this.getRoot(i, j);
				if (cell.isLeaf()) {
					MeshCellAttribute att = (MeshCellAttribute) cell.attribute();
					if (att.node != null) {
						return false;						
					}
				}
			}
		}

		double size = this.getCellSize()*0.5;
		int nr = this.getNRows()* 2;
		int nc = this.getNCols()* 2;
		QuadtreeRoot[][] newRoots = new QuadtreeRoot[nr][nc];

		for (int i = 0; i < this.getNRows(); ++i) {
			for (int j = 0; j < this.getNCols(); ++j) {
				QuadtreeCell cell = this.getRoot(i, j);
				MeshCellAttribute att = (MeshCellAttribute) cell.attribute();
				if (cell.isLeaf()) {
					if (att.node==null) {
						newRoots[2*i][2*j] = new QuadtreeRoot(this, i, j);
						newRoots[2*i+1][2*j] = new QuadtreeRoot(this, i, j);
						newRoots[2*i][2*j+1] = new QuadtreeRoot(this, i, j);
						newRoots[2*i+1][2*j+1] = new QuadtreeRoot(this, i, j);

						newRoots[2*i][2*j].setAttribute(this.createCellAttribute());
						newRoots[2*i+1][2*j].setAttribute(this.createCellAttribute());
						newRoots[2*i][2*j+1].setAttribute(this.createCellAttribute());
						newRoots[2*i+1][2*j+1].setAttribute(this.createCellAttribute());
					} else {
						assert false; 
						return false;
					}
				} else {
					QuadtreeCell children[] = cell.getChildren();
					// cSW, cNW, cSE, cNE 
					newRoots[2*i][2*j] = this.makeRootFromCell(2*i, 2*j, children[0]);
					newRoots[2*i+1][2*j] = this.makeRootFromCell(2*i+1, 2*j, children[1]);
					newRoots[2*i][2*j+1] = this.makeRootFromCell(2*i, 2*j+1, children[2]);
					newRoots[2*i+1][2*j+1] = this.makeRootFromCell(2*i+1, 2*j+1, children[3]);
				}
			}
		}
		
		this.cellSize = size;
		this.nRows = nr;
		this.nCols = nc;
		this.roots = newRoots;
		return true;
	}
	
	public ArrayList<Node2D> getNodes() {
		ArrayList<QuadtreeCell> leafCells = new ArrayList<QuadtreeCell>();
		this.getLeafCells(leafCells);
		ArrayList<Node2D> nodes = new ArrayList<Node2D>();
		for (Iterator<QuadtreeCell> it = leafCells.iterator(); it.hasNext();) {
			QuadtreeCell cell = it.next();
			MeshCellAttribute att = (MeshCellAttribute)cell.attribute();
			if ( (att != null) && (att.node!=null) ) {
				nodes.add(att.node);
			}
		}
		return nodes;
	}
	
	public static Rectangle2D createRectangle(Collection<Node2D> nodes,
			Collection<Edge2D> edges, Collection<? extends Simplex2D> simplicies) {
		
		Rectangle2D rec = null;

		if ( (edges != null ) && !edges.isEmpty()) {
			Iterator<Edge2D> it = edges.iterator();
			if (rec==null) rec = it.next().getBounds2D();
			while (it.hasNext()) {
				rec.add(((Edge2D)it.next()).getNode1());
			}
		} 

		if ( ( nodes != null ) && !nodes.isEmpty() ) {
			Iterator<Node2D> it = nodes.iterator();
			if (rec==null) rec = AlgoRectangle2D.createRectangle(it.next());
			while ( it.hasNext() ) {
				rec.add((Node2D)it.next());
			}
		}
		if ( ( simplicies != null ) && !simplicies.isEmpty() ) {
			Iterator<? extends Simplex2D> it = simplicies.iterator();
			if (rec==null) rec = it.next().getRectangle();
			while ( it.hasNext() ) {
				rec.add(it.next().getRectangle());
			}
		}

		return rec;
	}

	/**
	 * @param name
	 */
	public void insertNodesRemoveDuplicates(ArrayList<Node2D> nodes) {
		if (nodes == null) return;
		// insert posts into the quadtree. If there are nodes with equal
		// coordinates but which are not the same, only the first node of them
		// will survive. The other equal nodes are discarded
		ArrayList<Node2D> nodesToRemove = new ArrayList<Node2D>();
		for (Iterator it = nodes.iterator(); it.hasNext();) {
			Node2D node = (Node2D)it.next();
			QuadtreeCellEnvelope ce = (QuadtreeCellEnvelope)this.insertNode(node);
			MeshCellAttribute att = (MeshCellAttribute)ce.getCellAttribute();
			if (att.node != node) {
				nodesToRemove.add(node);
			}
		}			
		nodes.removeAll(nodesToRemove);
	}

	/**
	 * @param name
	 */
	public void insertEdgesNodesRemoveDuplicates(ArrayList<Edge2D> edges) {
		
		if ( edges == null ) return;

		for (Iterator it = edges.iterator(); it.hasNext();) {
			
			Edge2D edge = (Edge2D) it.next();

			Node2D p1 = edge.getNode1();
			QuadtreeCellEnvelope ce1 = (QuadtreeCellEnvelope)this.insertNode(p1);
			MeshCellAttribute att1 = (MeshCellAttribute)ce1.getCellAttribute();
			if ( att1.node != p1  ) p1 = att1.node;

			Node2D p2 = edge.getNode2();
			QuadtreeCellEnvelope ce2 = (QuadtreeCellEnvelope)this.insertNode(p2);
			MeshCellAttribute att2 = (MeshCellAttribute)ce2.getCellAttribute();
			if ( att2.node != p2  ) p2 = att2.node;
			
			Geo2DFactory.setEdgeNodes(edge, p1,p2);
		}

	}

	/**
	 * @param name
	 */
	public void insertTrianglesNodesRemoveDuplicates(ArrayList<Triangle2D> triangles) {
		if ( triangles == null )  return;
			
		for (Iterator<Triangle2D> it = triangles.iterator(); it.hasNext();) {
			
			Triangle2D tri= it.next();
			
			Node2D p1 = tri.getPoint(0);
			QuadtreeCellEnvelope ce1 = (QuadtreeCellEnvelope)this.insertNode(p1);
			MeshCellAttribute att1 = (MeshCellAttribute)ce1.getCellAttribute();
			if ( att1.node != p1  ) p1 = att1.node;

			Node2D p2 = tri.getPoint(1);
			QuadtreeCellEnvelope ce2 = (QuadtreeCellEnvelope)this.insertNode(p2);
			MeshCellAttribute att2 = (MeshCellAttribute)ce2.getCellAttribute();
			if ( att2.node != p2  ) p2 = att2.node;
			
			Node2D p3 = tri.getPoint(2);
			QuadtreeCellEnvelope ce3 = (QuadtreeCellEnvelope)this.insertNode(p3);
			MeshCellAttribute att3 = (MeshCellAttribute)ce3.getCellAttribute();
			if ( att3.node != p3  ) p3 = att3.node;
			
			Geo2DFactory.setTriangleNodes(tri,p1,p2,p3);
		}
	}

}

