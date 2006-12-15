package org.geotools.geometry.iso.util.quadtree;

import java.awt.geom.Rectangle2D;

/**
 * @author roehrig
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class QuadtreeCellEnvelope extends Rectangle2D.Double {

	protected QuadtreeCell cell;

	public QuadtreeCellEnvelope(Rectangle2D env, QuadtreeCell cell) {
		super(env.getX(), env.getY(), env.getWidth(), env.getHeight());
		this.cell = cell;
	}

	public QuadtreeCellEnvelope(double x, double y, double w, double h, QuadtreeCell cell) {
		super(x,y,w,h);
		this.cell = cell;
	}

	/**
	 * @return Returns the cell.
	 */
	public QuadtreeCell getCell() {
		return cell;
	}
	/**
	 * @param cell The cell to set.
	 */
	public void setCell(QuadtreeCell cell) {
		this.cell = cell;
	}
	
	public QuadtreeCellAttribute getCellAttribute() {
		return (this.cell==null) ? null : this.cell.attribute();
	}
}
