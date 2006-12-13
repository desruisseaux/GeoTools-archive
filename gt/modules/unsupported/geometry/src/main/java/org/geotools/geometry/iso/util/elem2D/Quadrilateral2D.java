/*
 * Created on 21.01.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.geometry.iso.util.elem2D;

/**
 * @author roehrig
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Quadrilateral2D extends Simplex2D {

	/**
	 * @param p
	 */
	protected Quadrilateral2D(Node2D p0, Node2D p1, Node2D p2, Node2D p3) {
		super(new Node2D[] {p0,p1,p2,p3});
	}

	private static int SIDE[] = {
		  (1<<0) | (1<<1),		// v[0] && v[1]
		  (1<<1) | (1<<2),		// v[1] && v[2]
		  (1<<2) | (1<<3),		// v[2] && v[3]
		  (1<<3) | (1<<0)		// v[3] && v[0]
		};

	/* (non-Javadoc)
	 * @see org.arena.GeoSimplex2D#n()
	 */
	public int n() {
		return 4;
	}

	/* (non-Javadoc)
	 * @see org.arena.GeoSimplex2D#side(int)
	 */
	public int sideBits(int s) {
		return SIDE[s];
	}

}
