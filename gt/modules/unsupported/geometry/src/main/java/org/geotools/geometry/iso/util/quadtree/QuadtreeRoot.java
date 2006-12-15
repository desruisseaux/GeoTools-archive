package org.geotools.geometry.iso.util.quadtree;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 * @author roehrig
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class QuadtreeRoot extends QuadtreeCell {
	// We do not define here an envelope because this roots cell may be part of a quadtree. In this case 
	// the envelope of this roots is implicitly defined by the quadtree structure. Hence, in a quadtree with 
	// mxn cells we save the storage of mxn envelopes

	private int row;

	private int col;

	private Quadtree quadtree;

	public QuadtreeRoot(Quadtree g, int i, int j) {
		super();
		this.row = i;
		this.col = j;
		this.quadtree = g;
	}

	public int getRowNumber() {
		return this.row;
	}

	public int getColNumber() {
		return this.col;
	}

	public double length() {
		return this.quadtree.getCellSize();
	}

	public Point2D.Double origin() {
		return this.quadtree.getRootOrigin(this.row, this.col);
	}

	public ArrayList getCellEnvelopes() {
		Rectangle2D env = this.envelope();
		double x = env.getX();
		double y = env.getY();
		double w = env.getWidth();
		ArrayList<QuadtreeCellEnvelope> list = new ArrayList<QuadtreeCellEnvelope>();
		this.getCellEnvelopes(x, y, w, list);
		if (list.isEmpty())
			return null;
		return list;
	}

	public void divideRoot() {
		this.divide(this.envelope());
	}

	public void balance(int level) {
		this.balance(0, this.envelope(), level);
	}

	public QuadtreeCellEnvelope findCell(double px, double py) {
		return super.findCell(px, py, this.envelope());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fhkoeln.tt.core.raster.QuadtreeCellRoot#findNeighborRootCell(int)
	 */
	public CellNeighbour findNeighborRoot(int searchDirection) {
		QuadtreeRoot nbrRoot = this.quadtree.getNeighbourCell(this.row,
				this.col, searchDirection);
		return (nbrRoot == null) ? null : new CellNeighbour(nbrRoot, 0,
				nbrRoot.envelope());
	}

	public CellNeighbour[] findNeighborRoots() {
		return new CellNeighbour[] { this.findNeighborRoot(QuadtreeCell.NW),
				this.findNeighborRoot(QuadtreeCell.N), this.findNeighborRoot(QuadtreeCell.NE),
				this.findNeighborRoot(QuadtreeCell.E), this.findNeighborRoot(QuadtreeCell.SE),
				this.findNeighborRoot(QuadtreeCell.S), this.findNeighborRoot(QuadtreeCell.SW),
				this.findNeighborRoot(QuadtreeCell.W) };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fhkoeln.tt.core.raster.QuadtreeCellRoot#envelope()
	 */
	public Rectangle2D envelope() {
		return (Rectangle2D) this.quadtree.getRootEnvelope(this.row, this.col);
	}

}
