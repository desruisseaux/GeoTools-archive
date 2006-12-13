package org.geotools.geometry.iso.util.quadtree;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Stack;

import org.geotools.geometry.iso.util.algorithm2D.AlgoRectangle2D;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author roehrig
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class QuadtreeCell {

	public static class CellNeighbour {
		public QuadtreeCell cell;
		public int level;
		public Rectangle2D env;
		public CellNeighbour(QuadtreeCell cell,int level,Rectangle2D env) {
			this.cell=cell;
			this.level=level;
			this.env = env;
		}
	}
	
	final public static int maxQTLevel = 100;
	
	final public static int NONE = 0;
	final public static int N = 1;
	final public static int S = 2;
	final public static int E = 4;
	final public static int W = 8;
	final public static int NE = 16;
	final public static int NW = 32;
	final public static int SW = 64;
	final public static int SE = 128;
	
	final public static int NOAxis = 0;
	final public static int NSAXIS = 1;
	final public static int EWAXIS = 2;

	public static int cellID = 0;

	protected QuadtreeCell parent;
	
	protected QuadtreeCell cNW;
	
	protected QuadtreeCell cNE;
	
	protected QuadtreeCell cSW;
	
	protected QuadtreeCell cSE;
	
	protected QuadtreeCellAttribute attribute;
	
	public int id;

	public static final String XMLElementName = "cell";
	
	public QuadtreeCell() {
		this.id = cellID++;
		this.parent = null;
		this.cNW = null;
		this.cNE = null;
		this.cSW = null;
		this.cSW = null;
		this.attribute = null;
	}
	
	public void reset() {
		if (this.cNE != null)
			this.cNE.reset();
		if (this.cNW != null)
			this.cNE.reset();
		if (this.cSE != null)
			this.cNE.reset();
		if (this.cSW != null)
			this.cNE.reset();
		this.cNE = null;
		this.cNW = null;
		this.cSE = null;
		this.cSW = null;
		this.setAttribute(null);
	}
	
	public void attribute(QuadtreeCellAttribute att ) {
		this.attribute = att;
	}
	public QuadtreeCell parent() {
		return this.parent;
	}
	
	public void parent(final QuadtreeCell Value) {
		this.parent = Value;
	}
	
	public QuadtreeCellAttribute attribute() {
		return this.attribute;
	}
	
	public void setAttribute(final QuadtreeCellAttribute Value) {
		this.attribute = Value;
	}
	
	public boolean isRoot() {
		return (this.parent == null);
	}
	
	public boolean isLeaf() {
		return (this.cNW == null);
	}
	
	public QuadtreeRoot getRoot() {
		QuadtreeCell cell = this;
		while (!cell.isRoot()) {
			cell = cell.parent();
		}
		return (QuadtreeRoot)cell;
	}
	
	public QuadtreeCell[] getChildren() {
		if (this.cNE == null)
			return null;
		return new QuadtreeCell[] { this.cSW, this.cNW, this.cSE, this.cNE };
	}
	
	public QuadtreeCell getChild(final int myQuadrant) {
		if (myQuadrant == NE) {
			return this.cNE;
		} else if (myQuadrant == NW) {
			return this.cNW;
		} else if (myQuadrant == SE) {
			return this.cSE;
		} else if (myQuadrant == SW) {
			return this.cSW;
		} else {
			return null;
		}
	}
	
//	public int getQuadrantBit() {
//		int myQuadrant;
//		if (this.parent.cNE == this) {
//			myQuadrant = NE;
//		} else if (this.parent.cNW == this) {
//			myQuadrant = NW;
//		} else if (this.parent.cSW == this) {
//			myQuadrant = SW;
//		} else if (this.parent.cSE == this) {
//			myQuadrant = SE;
//		} else {
//			myQuadrant = 0;
//		}
//		return myQuadrant;
//	}
//	
	public int getQuadrant() {
		int myQuadrant;
		if (this.parent == null) {
			myQuadrant = NONE;
		} else if (this.parent.cNE == this) {
			myQuadrant = NE;
		} else if (this.parent.cNW == this) {
			myQuadrant = NW;
		} else if (this.parent.cSW == this) {
			myQuadrant = SW;
		} else if (this.parent.cSE == this) {
			myQuadrant = SE;
		} else {
			myQuadrant = NONE;
		}
		return myQuadrant;
	}
	
	public QuadtreeCell getParent() {
		return this.parent;
	}
	
	public void divide(final Rectangle2D env) {
		this.cNW = this.createChild();
		this.cNE = this.createChild();
		this.cSW = this.createChild();
		this.cSE = this.createChild();
		this.cNW.parent(this);
		this.cNE.parent(this);
		this.cSW.parent(this);
		this.cSE.parent(this);
		if (this.attribute != null)
			this.attribute.divide(this, env);
	}
	
	protected QuadtreeCell createChild() {
		 return new QuadtreeCell();
	}

	public static int mirror(final int reflectAxis, final int quadBit) {
		int direction = NONE;
		if (reflectAxis==NSAXIS) {
			if (quadBit == NW) {
				direction = NE;
			} else if (quadBit == NE) {
				direction = NW;
			} else if (quadBit == SW) {
				direction = SE;
			} else if (quadBit == SE) {
				direction = SW;
			}
		} else if (reflectAxis==EWAXIS) {
			if (quadBit == NW) {
				direction = SW;
			} else if (quadBit == NE) {
				direction = SE;
			} else if (quadBit == SW) {
				direction = NW;
			} else if (quadBit == SE) {
				direction = NE;
			}
		}
		return direction;
	}
	

	public static int oppositeDirection(final int direction) {
		int oppositeDirection = NONE;
		if (direction==N)
			oppositeDirection = S;
		else if (direction==S)
			oppositeDirection = N;
		else if (direction==E)
			oppositeDirection = W;
		else if (direction==W)
			oppositeDirection = E;
		return oppositeDirection;
	}
	
	/**
	 * 
	 * @param ox
	 * @param oy
	 * @param width
	 * @param envelopeList: A list of CellEnvelopes
	 */
	public void getCellEnvelopes(final double ox, final double oy, double width,
			ArrayList<QuadtreeCellEnvelope> envelopeList) {
		if (envelopeList == null)
			return;
		if (this.isLeaf()) {
			envelopeList.add(new QuadtreeCellEnvelope(ox, oy, width, width, this));
		} else {
			width = width * 0.5;
			double cx = ox + width;
			double cy = oy + width;
			this.cSW.getCellEnvelopes(ox, oy, width, envelopeList);
			this.cNW.getCellEnvelopes(ox, cy, width, envelopeList);
			this.cSE.getCellEnvelopes(cx, oy, width, envelopeList);
			this.cNE.getCellEnvelopes(cx, cy, width, envelopeList);
		}
	}

	/**
	 * Returns the neighbor of the same size or larger then self in the given
	 * search direction, the neighbour level and, if the envelope of this object
	 * is given (not null), also the envelope of the neighbor
	 * 
	 * @param myLevel
	 * @param searchDirection
	 * @param myEnv
	 * @return
	 */
	public CellNeighbour findNeighbor(final int myLevel,
			final int searchDirection, final Rectangle2D myEnv) {

		if (this.isRoot()) {
			return ((QuadtreeRoot)this).findNeighborRoot(searchDirection);
		}
		
		int nbrQuad = NONE;
		int mirrorAxis = NONE;
		if (searchDirection == N) {
			nbrQuad = SW | SE;
			mirrorAxis = EWAXIS;
		} else if (searchDirection == S) {
			nbrQuad = NW | NE;
			mirrorAxis = EWAXIS;
		} else if (searchDirection == E) {
			nbrQuad = SW | NW;
			mirrorAxis = NSAXIS;
		} else if (searchDirection == W) {
			nbrQuad = NE | SE;
			mirrorAxis = NSAXIS;
		} else {
			//assert false;
		}
		
		/**
		 * Traverse up the tree from given roots until we find nearest common
		 * ancestor of this roots and its neighbor. The nearest common ancestor
		 * is the first roots which is reached via its nbrQuad. As we go,
		 * push each roots and the direction we had to go to get there onto a
		 * stack.
		 * 
		 */

		Stack<Integer> dirs = new Stack<Integer>();

		QuadtreeCell currCell = this;
		int currQuad = this.getQuadrant();
		dirs.add(new Integer(currQuad));		
		
		Rectangle2D parEnv = QuadtreeCell.getParentEnvelope(currQuad, myEnv);
		currCell = this.getParent();

		while ((currQuad & nbrQuad) == 0) {
			// currQuad is not one of nbrQuad
			if (currCell.isRoot()) {
				CellNeighbour cellNbr = ((QuadtreeRoot)currCell).findNeighborRoot(searchDirection);
				if ((cellNbr==null) || (cellNbr.cell==null)) return null; 
				currCell = cellNbr.cell;
				parEnv = cellNbr.env;
				break;
			}
			
			currQuad = currCell.getQuadrant();
			dirs.push(new Integer(currQuad));

			parEnv = QuadtreeCell.getParentEnvelope(currQuad, parEnv);
			currCell = currCell.getParent();			
		}
		
		// Traverse down following a path forms a mirror-image of the one
		// traversed up.
		while (!dirs.empty() && !currCell.isLeaf()) {
			currQuad = ((Integer)dirs.pop()).intValue();
			int mirrorDirection = mirror(mirrorAxis, currQuad);
			int mirrorQuadrant;;
			if (mirrorDirection==NW) {
				currCell = currCell.cNW;
				mirrorQuadrant = NW;
			} else if (mirrorDirection==NE) {
				currCell = currCell.cNE;
				mirrorQuadrant = NE;
			} else if (mirrorDirection==SW) {
				currCell = currCell.cSW;
				mirrorQuadrant = SW;
			} else if (mirrorDirection==SE) {
				currCell = currCell.cSE;
				mirrorQuadrant = SE;
			} else {
				currCell = null;
				mirrorQuadrant = NONE;
			}
			setChildEnvelope(mirrorQuadrant, parEnv);
		}
		return new CellNeighbour(currCell,myLevel - dirs.size(),parEnv);
	}
	
	public ArrayList<CellNeighbour> findLeafCellNeighbors(Rectangle2D env,final int searchDirection) {
		// return neighbors in given direction from given roots, whereas the
		// returned neighbors are leaf cells
		ArrayList<CellNeighbour> result = new ArrayList<CellNeighbour>();
		CellNeighbour cellNbr = this.findNeighbor(0, searchDirection, env);
		if (cellNbr==null) return result;
		QuadtreeCell cell = cellNbr.cell;
		if (cell != null) {
			result = new ArrayList<CellNeighbour>();
			//if (cellNbr.level > 0) {
			if (!cellNbr.cell.isLeaf()) {
				getLeafNeighbours(cell, cellNbr.level, cellNbr.env, result, oppositeDirection(searchDirection));
			} else {
				result.add(cellNbr);
			}
		}
		return result;
	}
	
	public void getLeafNeighbours(QuadtreeCell cell, int level, Rectangle2D env, ArrayList<CellNeighbour> leafCollection,
			int searchDirection) {
		level += 1;
		if (cell.isLeaf()) {
			CellNeighbour nbr = new CellNeighbour(cell,level,env);
			leafCollection.add(nbr);
		} else {
			/** returns Quadrants SW,NW,SE,NE */
			Rectangle2D rect[] = createQuadrantChildren(env);
			Rectangle2D envSW = null;
			Rectangle2D envNW = null;
			Rectangle2D envSE = null;
			Rectangle2D envNE = null;
			if (rect!=null) {
				envSW = rect[0];
				envNW = rect[1];
				envSE = rect[2];
				envNE = rect[3];				
			}
			if (searchDirection==NONE) {
				getLeafNeighbours(cell.cSW,level,envSW,leafCollection,NONE);
				getLeafNeighbours(cell.cSE,level,envSE,leafCollection,NONE);
				getLeafNeighbours(cell.cNW,level,envNW,leafCollection,NONE);
				getLeafNeighbours(cell.cNE,level,envNE,leafCollection,NONE);
			} else if (searchDirection==N) {
				getLeafNeighbours(cell.cNW,level,envNW,leafCollection, searchDirection);
				getLeafNeighbours(cell.cNE,level,envNE,leafCollection, searchDirection);
			} else if (searchDirection==S) {
				getLeafNeighbours(cell.cSW,level,envSW,leafCollection, searchDirection);
				getLeafNeighbours(cell.cSE,level,envSE,leafCollection, searchDirection);
			} else if (searchDirection==E) {
				getLeafNeighbours(cell.cNE,level,envNE,leafCollection, searchDirection);
				getLeafNeighbours(cell.cSE,level,envSE,leafCollection, searchDirection);
			} else if (searchDirection==W) {
				getLeafNeighbours(cell.cNW,level,envNW,leafCollection, searchDirection);
				getLeafNeighbours(cell.cSW,level,envSW,leafCollection, searchDirection);
			}
		}
	}
	
	public ArrayList<CellNeighbour> findLeafCellNeighbors(Rectangle2D env) {
		ArrayList<CellNeighbour> result = this.findLeafCellNeighbors(env,N);
		result.addAll(this.findLeafCellNeighbors(env,S));
		result.addAll(this.findLeafCellNeighbors(env,W));
		result.addAll(this.findLeafCellNeighbors(env,E));
		return result;
	}
	
	public void getLeafCells(final ArrayList<QuadtreeCell> leafCollection) {
		this.getLeafCells(leafCollection,NONE );
	}
	

	public void getLeafCells(ArrayList<QuadtreeCell> leafCollection,
			int searchDirection) {
		if (this.isLeaf()) {
			leafCollection.add(this);
		} else {
			if (searchDirection==NONE) {
				this.cSW.getLeafCells(leafCollection);
				this.cSE.getLeafCells(leafCollection);
				this.cNW.getLeafCells(leafCollection);
				this.cNE.getLeafCells(leafCollection);
			} else if (searchDirection==N) {
				this.cNW.getLeafCells(leafCollection, searchDirection);
				this.cNE.getLeafCells(leafCollection, searchDirection);
			} else if (searchDirection==S) {
				this.cSW.getLeafCells(leafCollection, searchDirection);
				this.cSE.getLeafCells(leafCollection, searchDirection);
			} else if (searchDirection==E) {
				this.cNE.getLeafCells(leafCollection, searchDirection);
				this.cSE.getLeafCells(leafCollection, searchDirection);
			} else if (searchDirection==W) {
				this.cNW.getLeafCells(leafCollection, searchDirection);
				this.cSW.getLeafCells(leafCollection, searchDirection);
			}
		}
	}
	
	public QuadtreeCellEnvelope findCell(final double px, final double py, final Rectangle2D unchangedEnvelope) {
		// return the smallest roots containing given p.
		// the origins and the length of the found roots are also returned ByRef
		QuadtreeCell cell = this;
		int quadrant;
		Rectangle2D env = new Rectangle2D.Double(unchangedEnvelope.getX(),unchangedEnvelope.getY(),unchangedEnvelope.getWidth(),unchangedEnvelope.getHeight());
		while (!cell.isLeaf()) {
			quadrant = getQuadrant(px, py, env);
			if (quadrant == NE) {
				cell = cell.cNE;
				setChildEnvelope(NE, env);
			} else if (quadrant == SE) {
				cell = cell.cSE;
				setChildEnvelope(SE, env);
			} else if (quadrant == NW) {
				cell = cell.cNW;
				setChildEnvelope(NW, env);
			} else if (quadrant == SW) {
				cell = cell.cSW;
				setChildEnvelope(SW, env);
			}
		}
		return new QuadtreeCellEnvelope(env,cell);
	}
	
	public  void divideToGivenSize(final double maximalSize, final Rectangle2D thisEnvelope) {
		if ((maximalSize>=thisEnvelope.getWidth()) || (maximalSize>=thisEnvelope.getHeight())) return;

		if (this.isLeaf()) this.divide(thisEnvelope);

		Rectangle2D rec[] = getChildEnvelopes(thisEnvelope);		

		QuadtreeCell cell[] = this.getChildren();
		
		for (int i=0; i<4; ++i) {
			cell[i].divideToGivenSize(maximalSize,rec[i]);
		}
		
	}	
	
	private void balanceLeaf(final int direction, final int myLevel, final Rectangle2D myEnvelope, final int levelDiff) {
		CellNeighbour cellNbr = this.findNeighbor(myLevel, direction, myEnvelope);
		if (cellNbr==null) {
			return;
		}
		QuadtreeCell nbr = cellNbr.cell;
		int lev =  cellNbr.level;
		Rectangle2D nNenv = cellNbr.env;
		if (nbr != null) {
			if (nbr.isLeaf() && ((myLevel - lev) > levelDiff)) {
				nbr.divide(nNenv);
				nbr.balance(lev + 1, nNenv, levelDiff);
			}
		}
	}
	
	public void balance(final int myLevel, final Rectangle2D myEnvelope, final int levelDiff) {
		// Refine the quadtree to enforce condition that (geometrically
		// neighboring) cells differ by no more than given number of levels.
		// Compare each roots to its neighbors, if they differ by more than given
		// number of levels, subdivide the larger roots as necessary.

		if (this.isLeaf()) {
			balanceLeaf(N, myLevel, myEnvelope,levelDiff);
			balanceLeaf(S, myLevel, myEnvelope,levelDiff);
			balanceLeaf(E, myLevel, myEnvelope,levelDiff);
			balanceLeaf(W, myLevel, myEnvelope,levelDiff);
		} else {

			// SW, NW, SE, NE
			Rectangle2D[] recs = getChildEnvelopes(myEnvelope);
			this.cSW.balance(myLevel + 1, recs[0], levelDiff);
			this.cNW.balance(myLevel + 1, recs[1], levelDiff);
			this.cSE.balance(myLevel + 1, recs[2], levelDiff);
			this.cNE.balance(myLevel + 1, recs[3], levelDiff);
		}
	}
	
	public static int getQuadrant(final double px, final double py, final Rectangle2D env) {
		if ((px > (env.getX() + env.getWidth() * 0.5))) {
			if ((py > (env.getY() + env.getHeight() * 0.5))) {
				return NE;
			} else {
				return SE;
			}
		} else {
			if ((py > (env.getY() + env.getHeight() * 0.5))) {
				return NW;
			} else {
				return SW;
			}
		}
	}
	
	public static void setChildEnvelope(final int parQuad, Rectangle2D parEnv) {
		if (parEnv == null)
			return;
		double x = parEnv.getMinX();
		double y = parEnv.getMinY();
		double w = parEnv.getWidth() * 0.5;
		double h = parEnv.getHeight() * 0.5;
		if (parQuad == SW)
			parEnv.setRect(x, y, w, h);
		else if (parQuad == NW)
			parEnv.setRect(x, y + h, w, h);
		else if (parQuad == SE)
			parEnv.setRect(x + w, y, w, h);
		else if (parQuad == NE)
			parEnv.setRect(x + w, y + h, w, h);
		// else
		// assert false;
	}

	public static Rectangle2D[] getChildEnvelopes(final Rectangle2D parEnv) {
		if (parEnv == null)
			return null;
		double x = parEnv.getX();
		double y = parEnv.getY();
		double w = parEnv.getWidth() * 0.5;
		double h = parEnv.getHeight() * 0.5;
		// SW, NW, SE, NE
		return new Rectangle2D[] {
				new Rectangle2D.Double(x, y, w, h),
				new Rectangle2D.Double(x, y + h, w, h),
				new Rectangle2D.Double(x + w, y, w, h),
				new Rectangle2D.Double(x + w, y + h, w, h)};
	}

	public static Rectangle2D getParentEnvelope(final int quadrantBit, final Rectangle2D env) {
		if (env == null)
			return null;
		double ox = Double.NaN;
		double oy = Double.NaN;
		if (quadrantBit == NW) {
			ox = env.getMinX();
			oy = env.getMinY() - env.getHeight();
		} else if (quadrantBit == NE) {
			ox = env.getMinX() - env.getWidth();
			oy = env.getMinY() - env.getHeight();
		} else if (quadrantBit == SW) {
			ox = env.getMinX();
			oy = env.getMinY();
		} else if (quadrantBit == SE) {
			ox = env.getMinX() - env.getWidth();
			oy = env.getMinY();
		}
		double width = env.getWidth() * 2.0;
		return new Rectangle2D.Double(ox, oy, width, width);
	}

	public ArrayList<QuadtreeCellEnvelope> getNextEnvelopes(Rectangle2D unchangedEnvelope) {
		ArrayList<QuadtreeCellEnvelope> result = new ArrayList<QuadtreeCellEnvelope>();
		QuadtreeCellEnvelope env = this.getNextEnvelope(unchangedEnvelope, NE);
		result.add(env);
		 env = this.getNextEnvelope(unchangedEnvelope, NW);
		 result.add(env);
		 env = this.getNextEnvelope(unchangedEnvelope, SE);
		 result.add(env);
		 env = this.getNextEnvelope(unchangedEnvelope, SW);
		result.add(env);
		
		return result;
		
	}
	
	public QuadtreeCellEnvelope getNextEnvelope(final Rectangle2D unchangedEnvelope, int quadrant) {
		QuadtreeCell cell = this;
		
		Rectangle2D env = new Rectangle2D.Double(unchangedEnvelope.getX(),unchangedEnvelope.getY(),unchangedEnvelope.getWidth(),unchangedEnvelope.getHeight());
		if (!cell.isLeaf()) {
			if (quadrant == NE) {
			cell = cell.cNE;
			setChildEnvelope(NE, env);
			}
			
			if (quadrant == SE){
			cell = cell.cSE;
			setChildEnvelope(SE, env);
			}
			
			if (quadrant == NW){
			cell = cell.cNW;
			setChildEnvelope(NW, env);
			}
			
			if (quadrant == SW){
			cell = cell.cSW;
			setChildEnvelope(SW, env);
			}
			
		}
		return new QuadtreeCellEnvelope(env,cell);
	}
	/** 
	 * returns Quadrants SW,NW,SE,NE 
	 */
    public Rectangle2D.Double[] createQuadrantChildren(final Rectangle2D env) {
    	/** returns Quadrants SW,NW,SE,NE */
    	if (env==null) return null;
    	double cenX = env.getCenterX();
    	double cenY = env.getCenterY();
    	double minX = env.getMinX();
    	double minY = env.getMinY();
    	double w = env.getWidth() * 0.5;
    	double h = env.getHeight() * 0.5;
    	return new Rectangle2D.Double[] {
				new Rectangle2D.Double(minX, minY, w, h),
				new Rectangle2D.Double(minX, cenY, w, h),
				new Rectangle2D.Double(cenX, minY, w, h),
				new Rectangle2D.Double(cenX, cenY, w, h) 
				};
    }
    
	/**
	 * @param rec
	 * @param cellEnv
	 * @param result
	 */
	public void getCellEnvelopesIntersectingRectangle(Rectangle2D rec, Rectangle2D cellEnv, ArrayList<QuadtreeCellEnvelope> result) {
		//if (!rec.intersects(cellEnv) && !rec.contains(cellEnv)) return;
		if (!AlgoRectangle2D.intersects(rec,cellEnv)) return;
		if (this.isLeaf()) {
			result.add(new QuadtreeCellEnvelope(cellEnv,this));
		} else {
			Rectangle2D[] cellEnvs = createQuadrantChildren(cellEnv);
			this.cSW.getCellEnvelopesIntersectingRectangle(rec,cellEnvs[0],result);
			this.cNW.getCellEnvelopesIntersectingRectangle(rec,cellEnvs[1],result);
			this.cSE.getCellEnvelopesIntersectingRectangle(rec,cellEnvs[2],result);
			this.cNE.getCellEnvelopesIntersectingRectangle(rec,cellEnvs[3],result);
		}
	}

	/**
	 * @param cellEnv
	 * @return
	 */
	public Node getXML(Document document, String name, Rectangle2D r) {
		Element element = document.createElement( name );
		element.setAttribute( "centerX", String.valueOf( r.getCenterX() ) );
		element.setAttribute( "centerY", String.valueOf( r.getCenterY() ) );
		element.appendChild( this.attribute.getXML(document,"attribute")  );
		return element;
	}


}
