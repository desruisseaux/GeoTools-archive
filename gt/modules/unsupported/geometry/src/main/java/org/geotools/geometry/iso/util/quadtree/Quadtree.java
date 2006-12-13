package org.geotools.geometry.iso.util.quadtree;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.geotools.geometry.iso.util.algorithm2D.AlgoRectangle2D;
import org.geotools.geometry.iso.util.elem2D.Edge2D;
import org.geotools.geometry.iso.util.quadtree.QuadtreeCell.CellNeighbour;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * a general grid of quadtrees, whereas each grid roots corresponds to a quadtree
 * 
 * @author roehrig
 *
 */
public abstract class Quadtree extends Rectangle2D.Double {

	public static class QUADRANT {
		public final static int NW = 1;
		public final static int NE = 2;
		public final static int SE = 4;
		public final static int SW = 8;
	}

	// This is a grid of objects, whereas each object corresponds to a roots.
	// QuadtreeCell width and height are equal. 
	// The limits of the grid are determined by the envelope.
	// The given envelope will be normally expanded in order to be filled completly 
	// with cells of the given cellSize 

	protected double cellSize;

	protected int nRows;

	protected int nCols;

	protected QuadtreeRoot[][] roots;

	public Quadtree(Rectangle2D env, double cellSize) {
		super(env.getMinX(), env.getMinY(), env.getWidth(), env.getHeight());
		this.createGrid(cellSize);
		this.createAttributes();
	}

	/**
	 * @param nextElement
	 */
	public Quadtree() {
		super();
		this.cellSize = java.lang.Double.NaN;
		this.nRows = 0;
		this.nCols = 0;
		this.roots = null;
	}
	
	public void createGrid(double cellSize) {
		double x0 = this.getX();
		double y0 = this.getY();
		double w = this.getWidth();
		double h = this.getHeight();
		double x1 = x0 + w;
		double y1 = y0 + h;
		if (cellSize <= 0.0 || java.lang.Double.isNaN(cellSize) ) {
			cellSize = Math.max(w, h);
		}
		this.cellSize = cellSize;
		this.nRows = (int) Math.ceil(h / cellSize);
		this.nCols = (int) Math.ceil(w / cellSize);
		double dx = (x0 + (this.nCols * cellSize) - x1) * 0.5;
		double dy = (y0 + (this.nRows * cellSize) - y1) * 0.5;
		double xmin = x0 - dx;
		double ymin = y0 - dy;
		double wmax = this.nCols * cellSize;
		double hmax = this.nRows * cellSize;
		this.setRect(xmin, ymin, wmax, hmax);

		// allocate the cells
		this.roots = new QuadtreeRoot[this.nRows][this.nCols];
		for (int i = 0; i < this.nRows; ++i) {
			for (int j = 0; j < this.nCols; ++j) {
				this.roots[i][j] = new QuadtreeRoot(this, i, j);
			}
		}

	}

	public abstract QuadtreeCellAttribute createCellAttribute();
	
	protected void createAttributes() {
		try {
			QuadtreeRoot[][] roots = this.getRoots();
			for (int i=0; i<this.getNRows();++i) {
				for (int j=0; j<this.getNCols();++j) {
					roots[i][j].setAttribute(this.createCellAttribute());
				}
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean equalType(Quadtree other) {
		return ((this.cellSize == other.cellSize)
				&& (this.nCols == other.nCols) && (this.nRows == other.nRows));
	}

	/**
	 * @return Returns the roots.
	 */
	public QuadtreeRoot[][] getRoots() {
		return roots;
	}

	/**
	 * @param roots The roots to set.
	 */
	public void setRoots(QuadtreeRoot[][] roots) {
		this.roots = roots;
	}

	/**
	 * @return Returns the cellSize.
	 */
	public double getCellSize() {
		return cellSize;
	}

	/**
	 * @return Returns the nCols.
	 */
	public int getNCols() {
		return nCols;
	}

	/**
	 * @return Returns the nRows.
	 */
	public int getNRows() {
		return nRows;
	}

	public int getNumberOfCells() {
		return this.nRows * this.nCols;
	}

	public QuadtreeRoot getRoot(int i, int j) {
		//assert(i < this.numberOfRows && j < this.numberOfColumns);
		return (i < 0 || i > this.nRows || j < 0 || j > this.nCols) ? null
				: this.roots[i][j];
	}

	public Point2D.Double getRootOrigin(int i, int j) {
		return new Point2D.Double(this.getX() + j * this.cellSize, this.getY()
				+ i * this.cellSize);
	}

	public Point2D.Double getRootCenter(int i, int j) {
		Point2D.Double p = this.getRootOrigin(i, j);
		p.x += this.cellSize * 0.5;
		p.y += this.cellSize * 0.5;
		return p;
	}

	public Rectangle2D getRootEnvelope(int i, int j) {
		Point2D.Double pmin = this.getRootOrigin(i, j);
		return new Rectangle2D.Double(pmin.getX(), pmin.getY(), this.cellSize,
				this.cellSize);
	}

	public QuadtreeRoot getRootCell(double x, double y) {
		int[] idx = getRootCellIndex(x,y);
		int i = idx[0];
		int j = idx[1];
		return ((i >= 0 && i < this.nRows) && (j >= 0 && j < this.nCols)) ? this.getRoot(i, j) : null;
	}

	public int[] getRootCellIndex(double x, double y) {
		return new int[] {(int) Math.floor((y - this.getMinY()) / this.cellSize),
						  (int) Math.floor((x - this.getMinX()) / this.cellSize)};
	}

	public int[] getRootCellIndexValid(double x, double y) {
		int i = (int) Math.floor((y - this.getMinY()) / this.cellSize);
		int j = (int) Math.floor((x - this.getMinX()) / this.cellSize);
		i = (i<0) ? 0 : ((i>=this.nRows) ? this.nRows-1 : i);
		j = (j<0) ? 0 : ((j>=this.nCols) ? this.nCols-1 : j);
		return new int[] {i,j};

	}

	public boolean equals(Quadtree g) {
		return super.equals(g) && this.nCols == this.nCols
				&& this.nRows == this.nRows;
	}

	public QuadtreeRoot getNeighbourCell(final int i, final int j, final int searchDirection) {
		if ((i == 0 && searchDirection == QuadtreeCell.S)
				|| (j == 0 && searchDirection == QuadtreeCell.W)
				|| (i == (this.nRows - 1) && searchDirection == QuadtreeCell.N)
				|| (j == (this.nCols - 1) && searchDirection == QuadtreeCell.E)) {
			return null;
		}

		if (searchDirection == QuadtreeCell.E) {
			return this.getRoot(i, j + 1);
		} else if (searchDirection == QuadtreeCell.W) {
			return this.getRoot(i, j - 1);
		} else if (searchDirection == QuadtreeCell.N) {
			return this.getRoot(i + 1, j);
		} else if (searchDirection == QuadtreeCell.S) {
			return this.getRoot(i - 1, j);
		} else if (searchDirection == QuadtreeCell.NE) {
			return this.getRoot(i + 1, j + 1);
		} else if (searchDirection == QuadtreeCell.NW) {
			return this.getRoot(i + 1, j - 1);
		} else if (searchDirection == QuadtreeCell.SE) {
			return this.getRoot(i - 1, j + 1);
		} else if (searchDirection == QuadtreeCell.SW) {
			return this.getRoot(i - 1, j - 1);
		} else {
			return null;
		}
	}

	public Rectangle2D[][] getRootEnvelopes() {
		Rectangle2D[][] result = new Rectangle2D[this.nRows][this.nCols];
		for (int i = 0; i < this.nRows; ++i) {
			for (int j = 0; j < this.nCols; ++j) {
				result[i][j] = this.getRootEnvelope(i, j);
			}
		}
		return result;
	}

	public ArrayList<QuadtreeRoot> getIntersectedRoots(Rectangle2D env) {
		ArrayList<QuadtreeRoot> result = new ArrayList<QuadtreeRoot>();
		for (int i = 0; i < this.nRows; ++i) {
			for (int j = 0; j < this.nCols; ++j) {
				if (AlgoRectangle2D.intersects(env,this.getRootEnvelope(i,j))) {
					result.add(this.roots[i][j]);
				}
			}
		}
		return result;
	}

	public void getLeafCells(ArrayList<QuadtreeCell> leafCells) {
		if (leafCells == null)
			return;
		for (int i = 0; i < this.getNRows(); ++i) {
			for (int j = 0; j < this.getNCols(); ++j) {
				QuadtreeRoot root = this.roots[i][j];
				root.getLeafCells(leafCells);
			}
		}
	}

	public QuadtreeCellEnvelope findCell(final Point2D p) {
		// env will be overwrited and becomes neu values
		QuadtreeRoot root = this.getRootCell(p.getX(),p.getY());
		if (root == null) return null;
		return root.findCell(p.getX(), p.getY(), root.envelope());
	}

	public void divideCell(Point2D p) {
		QuadtreeRoot root = this.getRootCell(p.getX(),p.getY());
		if (root == null) return;
		QuadtreeCellEnvelope qtEnv = this.findCell(p);
		QuadtreeCell cell = qtEnv.getCell();
		if (qtEnv != null)
			cell.divide(qtEnv);
	}

	public ArrayList<QuadtreeCellEnvelope> getCellEnvelopes() {
		ArrayList<QuadtreeCellEnvelope> envelopeList = new ArrayList<QuadtreeCellEnvelope>();
		this.getCellEnvelopes(envelopeList);
		return envelopeList;
	}

	/**
	 * 
	 * @param envelopeList: A list of CellEnvelopes
	 */
	public void getCellEnvelopes(ArrayList<QuadtreeCellEnvelope> envelopeList) {
		if (envelopeList == null)
			return;
		double width = this.getCellSize();
		for (int i = 0; i < this.getNRows(); ++i) {
			for (int j = 0; j < this.getNCols(); ++j) {
				QuadtreeRoot root = this.roots[i][j];
				Point2D p = this.getRootOrigin(i, j);
				root.getCellEnvelopes(p.getX(), p.getY(), width, envelopeList);
			}
		}
	}

	public void balance(int level) {
		for (int i = 0, nr = this.getNRows(); i < nr; ++i) {
			for (int j = 0, nc = this.getNCols(); j < nc; ++j) {
				this.getRoot(i, j).balance(level);
			}
		}
	}

	public void balanceRoot() {
		for (int i = 0; i < this.getNRows(); ++i) {
			for (int j = 0; j < this.getNCols(); ++j) {
				QuadtreeCell cell = this.getRoot(i, j);
				if (cell.isLeaf()) {
					return;
				}
			}
		}
		// TODO
//		cellSize *= 0.5;
//		nRows *= 2;
//		nCols *= 2;
//		for (int i = 0; i < this.getNRows(); ++i) {
//			for (int j = 0; j < this.getNCols(); ++j) {
//				if (this.getRoot(i, j).isLeaf()) return;
//			}
//		}
		
	}
	
	public QuadtreeRoot makeRootFromCell(int i, int j, QuadtreeCell cell) {
		QuadtreeRoot root = new QuadtreeRoot(this,i,j);
		root.cSW = cell.cSW;
		root.cNW = cell.cNW;
		root.cSE = cell.cSE;
		root.cNE = cell.cNE;
		root.attribute = cell.attribute;
		return root;
	}


	public void divideToGivenSize(double maximalSize) {
		if (maximalSize<=0.0 || maximalSize==java.lang.Double.NaN) return;
		for (int i = 0; i < this.getNRows(); ++i) {
			for (int j = 0; j < this.getNCols(); ++j) {
				this.roots[i][j].divideToGivenSize(maximalSize, roots[i][j].envelope());
			}
		}
		
	}
	
	public void getCellEnvelopesIntersectingRectangle(Rectangle2D rec, ArrayList<QuadtreeCellEnvelope> result) {
		
		if (!AlgoRectangle2D.intersects(this,rec)) {
			return;
		}
		int idxMin[] = getRootCellIndexValid(rec.getMinX(),rec.getMinY());
		int idxMax[] = getRootCellIndexValid(rec.getMaxX(),rec.getMaxY());
		
		for (int i = idxMin[0]; i <= idxMax[0]; ++i) {
			for (int j = idxMin[1]; j <= idxMax[1]; ++j) {
				QuadtreeRoot root = this.getRoot(i, j);
				root.getCellEnvelopesIntersectingRectangle(rec,root.envelope(),result);
			}
		}
	}

	/**
	 * Returns ArrayList<QuadtreeCellEnvelope>
	 * @param line
	 * @return
	 */
	public ArrayList<QuadtreeCellEnvelope> getCellsIntersectingLine(Line2D line) {
		
		Rectangle2D rec = AlgoRectangle2D.createRectangle(line.getP1(),line.getP2());
		AlgoRectangle2D.setMinSize(rec,1.001);
		AlgoRectangle2D.setScale(rec,1.001);
		ArrayList<QuadtreeCellEnvelope> cells = new ArrayList<QuadtreeCellEnvelope>();
		this.getCellEnvelopesIntersectingRectangle(rec,cells);			
		ArrayList<QuadtreeCellEnvelope> result = new ArrayList<QuadtreeCellEnvelope>();
		for (Iterator<QuadtreeCellEnvelope> it=cells.iterator(); it.hasNext();) {
			QuadtreeCellEnvelope cellEnv = it.next();
			if (AlgoRectangle2D.createScale(cellEnv,1.001).intersectsLine(line)) result.add(cellEnv);
		}
		return result;
	}

	public void getCellsIntersectingLine(Edge2D line, HashSet<QuadtreeCellEnvelope> cells) {
		ArrayList<QuadtreeCellEnvelope> list = this.getCellsIntersectingLine(line);
		cells.addAll(list);
	}
	
	public static void addNeighbourCells(Set<QuadtreeCell> cells) {
		if (cells==null) return;
		ArrayList<QuadtreeCell> nbrCells = new ArrayList<QuadtreeCell>();
		for (Iterator<QuadtreeCell> it = cells.iterator(); it.hasNext(); ) {
			QuadtreeCell cell = it.next();
			ArrayList<CellNeighbour> cellNbr = cell.findLeafCellNeighbors(null); //Collection<CellNeighbour>
			for (Iterator<CellNeighbour> it1 = cellNbr.iterator(); it1.hasNext();) {
				nbrCells.add(((QuadtreeCell.CellNeighbour)it1.next()).cell);				
			}
		}
		cells.addAll(nbrCells);
	}
	
	public Element getXML( Document document, String name ) {
		Element element = document.createElement( name );
		element.setAttribute( "rows", String.valueOf(this.nRows) );
		element.setAttribute( "cols", String.valueOf(this.nCols) );
		element.setAttribute( "cellSize", String.valueOf(this.cellSize) );
		element.setAttribute( "xMin", String.valueOf(this.getMinX()) );
		element.setAttribute( "yMin", String.valueOf(this.getMinY()) );
		element.setAttribute( "xMax", String.valueOf(this.getMaxX()) );
		element.setAttribute( "yMax", String.valueOf(this.getMaxY()) );

		ArrayList<QuadtreeCellEnvelope> cellEnvelopes = this.getCellEnvelopes();
		for (Iterator<QuadtreeCellEnvelope> it = cellEnvelopes.iterator(); it.hasNext(); ) {
			QuadtreeCellEnvelope cellEnv = it.next();
			element.appendChild( cellEnv.cell.getXML(document, "cell", cellEnv) );
		}
		return element;
	}

	public void setXML(Element element) {
		
		assert element.getNodeName().equals(this.getClass().getName());

		String cs = element.getAttribute( "cellSize" );
		String xMin = element.getAttribute( "xMin" );
		String yMin = element.getAttribute( "yMin" );
		String xMax = element.getAttribute( "xMax" );
		String yMax = element.getAttribute( "yMax" );

		Rectangle2D r = AlgoRectangle2D.createRectangle(
				java.lang.Double.valueOf(xMin),
				java.lang.Double.valueOf(yMin),
				java.lang.Double.valueOf(xMax),
				java.lang.Double.valueOf(yMax) );
		
		this.setRect(r);
		this.createGrid(java.lang.Double.valueOf(cs));

		ArrayList<Object[]> pointAndAttributes = new ArrayList<Object[]>();
		ArrayList<QuadtreeCellAttribute> cellAtts = new ArrayList<QuadtreeCellAttribute>();
		
		NodeList cellNodes = element.getChildNodes();
		for (int i=0; i<cellNodes.getLength(); ++i) {
			Node cellNode = cellNodes.item(i);
			if (cellNode.getNodeType()==Node.ELEMENT_NODE) {
				Element cellElement = (Element)cellNode;
				if (cellElement.getNodeName().equals(QuadtreeCell.class.getName())) {
					double cx = java.lang.Double.valueOf(cellElement.getAttribute( "centerX" ) );
					double cy = java.lang.Double.valueOf(cellElement.getAttribute( "centerY" ) );
					Point2D p = new Point2D.Double(cx,cy);
					QuadtreeCell cell = this.insertPoint(p);
					Node n = cellElement.getFirstChild();
					pointAndAttributes.add(new Object[] {p,n});
				} else {
					assert false;
					break;
				}
			} 
		}

		for ( Object[] obj : pointAndAttributes ) {
			Point2D p = (Point2D)obj[0];
			Node n = (Node)obj[1];
			QuadtreeCellEnvelope cellEnv = this.findCell(p);
			QuadtreeCell cell = cellEnv.cell;
			QuadtreeCellAttribute att = this.createCellAttribute();
			if (n.getNodeType()==Node.ELEMENT_NODE) {
				att.setXML((Element)n);
			} else {
				assert false;
			}
		}

	}

	/**
	 * used only to create a quadtree from given cell centers
	 * @param p
	 * @return
	 */
	private QuadtreeCell insertPoint(Point2D p) {
        if ( this.contains(p) ) {
			for (int i = 0; i <= QuadtreeCell.maxQTLevel; ++i) {
				QuadtreeCellEnvelope qtCellEnv = this.findCell(p);
				if (qtCellEnv == null) {
					return null;
				}
				QuadtreeCell cell = qtCellEnv.getCell();
				CellPoint att = (CellPoint) cell.attribute();
				if (att.p == null) {
					att.p = p;
					return qtCellEnv.cell;
				}
				if (att.p.equals(p)) {
					return qtCellEnv.cell;				
				}
				if (cell.isLeaf()) {
					cell.divide(qtCellEnv);
				}
			}
        }
		return null;
	}

	/**
	 * return the boundary points counterclockwise, whereas each point is an
	 * outer corner of the quadtree grid. The first and the last points have the same coordinate.
	 * For a NxM grid returns 2*(N+M) + 1 points  
	 */
	public ArrayList<Point2D> getBoundaryPoints() {
		int nr = this.getNRows();
		int nc = this.getNCols();
		ArrayList<Point2D> result = new ArrayList<Point2D>(2*(nc+nr)+1);
		double xMin = this.getMinX();
		double yMin = this.getMinY();
		double xMax = this.getMaxX();
		double yMax = this.getMaxY();
		double cs = this.cellSize;
		// bottom
		double xx = xMin;
		double yy = yMin;
		for (int i=0; i<nc-1; ++i, xx += cs) {
			result.add(new Point2D.Double(xx,yy));
		}
		// right
		xx = xMax;
		yy = yMin;
		for (int i=0; i<nr-1; ++i, yy += cs) {
			result.add(new Point2D.Double(xx,yy));
		}
		// top
		xx = xMax;
		yy = yMax;
		for (int i=0; i<nc-1; ++i, xx -= cs) {
			result.add(new Point2D.Double(xx,yy));
		}
		// left
		xx = xMin;
		yy = yMax;
		for (int i=0; i<nr-1; ++i, yy -= cs) {
			result.add(new Point2D.Double(xx,yy));
		}
		result.add(new Point2D.Double(xMin,yMin));
		
		return result;
	}

	public class CellPoint implements QuadtreeCellAttribute {
		QuadtreeCellAttribute att = null;
		public Point2D p = null;
		public void divide(QuadtreeCell cell, final Rectangle2D env) {
			QuadtreeCell[] children = cell.getChildren();
			if (children == null) return;
			cell.setAttribute(null);
			if (p != null) {
				double cx = env.getCenterX();
				double cy = env.getCenterY();
				if (p.getX() < cx) { // West
					if (p.getY() < cy) { // South
						cell.getChild(QuadtreeCell.SW).setAttribute(this);
					} else { // North
						cell.getChild(QuadtreeCell.NW).setAttribute(this);
					}
				} else { //' East
					if (p.getY() < cy) { //' South
						cell.getChild(QuadtreeCell.SE).setAttribute(this);
					} else { //' North
						cell.getChild(QuadtreeCell.NE).setAttribute(this);
					}
				}
			}
			for (int i = 0; i < children.length; ++i) {
				QuadtreeCell cell0 = children[i];
				if (cell0.attribute() == null)
					cell0.setAttribute( new CellPoint() );
			}
		}
		public Element getXML(Document document,String name) {return null;}
		public void setXML(Element element) {}
	}


}
