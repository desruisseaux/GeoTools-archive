/*
 * Created on 21.01.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.geometry.iso.util.mesh2D;

import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Observer;
import java.util.TreeMap;

import org.geotools.geometry.iso.util.algorithm2D.AlgoLine2D;
import org.geotools.geometry.iso.util.algorithm2D.AlgoPoint2D;
import org.geotools.geometry.iso.util.algorithm2D.AlgoRectangle2D;
import org.geotools.geometry.iso.util.elem2D.Circle2D;
import org.geotools.geometry.iso.util.elem2D.Edge2D;
import org.geotools.geometry.iso.util.elem2D.Geo2DFactory;
import org.geotools.geometry.iso.util.elem2D.Node2D;
import org.geotools.geometry.iso.util.elem2D.Simplex2D;
import org.geotools.geometry.iso.util.elem2D.Triangle2D;
import org.geotools.geometry.iso.util.quadtree.QuadtreeCell;
import org.geotools.geometry.iso.util.quadtree.QuadtreeCellEnvelope;

/**
 * @author roehrig
 * 
 * MeshGenerator creates a pseudo delaunay triangulation if there are boundary
 * edges or breaklines, or it creates a delaunay triangulation if there are only
 * posts
 * 
 */
public class MeshGenerator extends Mesh {
	
	private abstract class EdgeObject {
		public Object object;
		EdgeObject(Object obj) {object = obj; }
		public abstract boolean isBoundary();
	}
	private class BoundaryEdgeObject extends EdgeObject {
		BoundaryEdgeObject(Object obj) {
			super(obj);
		}
		public boolean isBoundary() {
			return true;
		}
	}
	private class InternalEdgeObject extends EdgeObject {
		InternalEdgeObject(Object obj) {
			super(obj);
		}
		public boolean isBoundary() {
			return false;
		}
	}
	
	/**
	 * Triangulation's steps:
	 * 
	 * 1) create the boundary edges, internal edges and posts
	 * 
	 * 2) create the quadtree
	 * 
	 * 3) insert given nodes from boundary edges, internal edges and posts into

	 * the quadtree. If there are nodes with equal coordinates but which are not
	 * the same are substituted by the same node.
	 * 
	 * 4) if there are boundaries, discard internal edges and posts outside the
	 * boundaries. Checks whether there are internal edges or posts intersecting
	 * the boundary
	 * 
	 * 5) if the flag SPLITEDGES is set, split the boundary edges and internal edges
	 * 
	 * 6) 
	 * 
	 */
	/**
	 * List of Edge2D
	 */
	protected LinkedList<Edge2D> front;

	public boolean splitBoundary = true;
	
	public boolean triangulate = true;

	public double maxLength = Double.NaN;

	public MeshGenerator(ArrayList<ArrayList<Point2D>> bdryRings, 
						 ArrayList<ArrayList<Point2D>> breakLines, 
						 ArrayList<Point2D> post, 
						 double maxLength, 
						 ArrayList<Observer> observers) {
		
		super(bdryRings,breakLines,post,maxLength,observers);
		
		this.maxLength = maxLength;

		this.observable.sendMessage("insert edges into quadtree...");
		this.insertEdgesIntoQuadtree();
		
		this.observable.sendMessage("mark boundary and internal edges...");
		this.markEdges();
		
		this.observable.sendMessage("set quadtree cell colors...");
		this.setCellColors();	

		this.observable.sendMessage("");
	}
	
	protected MeshQuadtree createQuadtree(Rectangle2D r, double cellLength) {
		return new MeshGeneratorQuadtree(AlgoRectangle2D.createScale(r, 1.1), cellLength);
	}

	private void insertEdgesIntoQuadtree() {
		ArrayList<Edge2D> edges = this.getEdges();
		if (edges != null && !edges.isEmpty() ) 
			((MeshGeneratorQuadtree)this.quadtree).insertEdgesIntoQuadtree(edges);
	}
	
	private void markEdges() {
 		// insert the nodes of the external boundaries into the quadtree
		if ( this.boundaryEdges != null ) {
			for (Iterator it = this.boundaryEdges.iterator(); it.hasNext();) {
				Collection ring = (Collection)it.next();
				for (Iterator it1 = ring.iterator(); it1.hasNext();) {
					Edge2D edge = (Edge2D) it1.next();
					edge.object = new BoundaryEdgeObject(edge.object);
				}
			}
		}
		
		if (this.internalEdges != null) {
			for (Iterator it1 = this.internalEdges.iterator(); it1.hasNext();) {
				Edge2D edge = (Edge2D) it1.next();
				edge.object = new InternalEdgeObject(edge.object);
			}
		}
	}
	
	private void setCellColors() {

		((MeshGeneratorQuadtree)this.quadtree).setCellColors(getBoundaryArea(), this.getEdges(), this.posts);
		
		((MeshGeneratorQuadtree)this.quadtree).fillWhiteCellsWithNodes();

	}

	public void triangulate() {
		this.observable.sendMessage("spliting breaklines and boundaries...");
		this.splitInternalAndBoundaryEdges();
		this.observable.sendMessage("create front...");
		this.createFront();
		if (!triangulate) return;
		this.observable.sendMessage("advancing front...");
		
		this.advanceFront();
	}

	public void triangulateNodes() {
		if (this.boundaryEdges!=null && !this.boundaryEdges.isEmpty()) {
			assert false;
			return;
		}
		this.observable.sendMessage("create temporary boundary...");
		this.createTemporaryBoundaryEdges();
		this.observable.sendMessage("create front...");
		this.createFront();
		if (!triangulate) return;
		this.observable.sendMessage("advancing front...");
		this.advanceFront();
		this.observable.sendMessage("remove temporary boundary...");
		this.removeTemporaryBoundaryEdges();
		this.observable.sendMessage("");
	}

	/**
	 * 
	 */
	private void removeTemporaryBoundaryEdges() {
		// TODO
		assert false;
	}

	/**
	 * 
	 */
	private void createTemporaryBoundaryEdges() {
		ArrayList<Point2D> points = this.quadtree.getBoundaryPoints();
		
	}

	private void splitInternalAndBoundaryEdges() {
		
		if (!this.splitBoundary) return;

		if ( (this.boundaryEdges!=null) && !this.boundaryEdges.isEmpty()) {
			for (Iterator<ArrayList<Edge2D>> it = this.boundaryEdges.iterator(); it.hasNext(); ) {
				ArrayList<Edge2D> edges = it.next();
				this.splitEdgesIntersectingCells(edges);
			}			
		}

		if ( (this.internalEdges!=null) && !this.internalEdges.isEmpty()) {
			this.splitEdgesIntersectingCells(this.internalEdges);
		}
	}

	/**
	 * 
	 */
	private void createFront() {
		
		this.front = new LinkedList<Edge2D>();
		
		if (this.boundaryEdges != null) {
			for (Iterator it0 = this.boundaryEdges.iterator(); it0.hasNext();) {
				Collection ring = (Collection)it0.next();
				for (Iterator it1 = ring.iterator(); it1.hasNext();) {
					this.front.add((Edge2D)it1.next());
				}
			}
		}
		if (this.internalEdges != null) {
			for (Iterator it = this.internalEdges.iterator(); it.hasNext();) {
				this.front.add((Edge2D)it.next());
			}			
		}
		
	}

	private void advanceFront() {
		this.triangles = new ArrayList<Triangle2D>();
		long iniTime = System.currentTimeMillis();
		for (Triangle2D tri = this.createTriangle(); tri != null; tri = this.createTriangle()) {
			if ((front.size()%1000)==0) {
				this.observable.sendMessage("Remaining front: "+front.size()+". Generated triangles: "+this.triangles.size());
			}
			this.triangles.add(tri);
		}
		long cpuTime = (System.currentTimeMillis()-iniTime)/1000;
		this.observable.sendMessage(this.triangles.size()+" triangles created in "+cpuTime+" seconds.");

	}

//	private boolean checkNodes(ArrayList<Node2D> nodes) {
//		boolean check = true;
//		for (Node2D n : nodes) {
//			Edge2D[] e = n.getEdges();
//			if (e!=null) {
//				for (int i=0; i<e.length; ++i) {
//					if ( !e[i].hasPoint(n) ) {
//						System.out.println("EEEEE "+i+"  "+n);
//						check = false;
//					}
//				}
//			}
//		}
//		return check;
//	}

	private Triangle2D createTriangle() {

		if (this.front.isEmpty()) return null;
		
		Edge2D front = (Edge2D)this.front.remove(this.front.size()-1);

		Triangle2D triangle = null;

		Point2D p0 = front.getP1();
		Point2D p1 = front.getP2();
		Point2D pm = AlgoPoint2D.evaluate(p0,p1,0.5);
		Point2D p2 = getOptimalTrianglePoint(p0,p1);
		Point2D pc = AlgoPoint2D.createCentroid(new Point2D[] {p0,p1,p2});
		Circle2D circle = new Circle2D(pc.getX(),pc.getY(),pc.distance(p0));
		
		TreeMap<Double,DelaunayHeight> candPoints = new TreeMap<Double,DelaunayHeight>();
		this.getCandidatePoints(front, circle, candPoints);
		int cnt = 0;
		do {
			for (Iterator itDelaunayPoints = candPoints.values().iterator(); itDelaunayPoints.hasNext();) {
				DelaunayHeight dh = (DelaunayHeight)itDelaunayPoints.next();
				p2 = dh.point;
				Line2D seg1 = new Line2D.Double(p1, p2);
				Line2D seg2 = new Line2D.Double(p2, p0);
				Node2D cp = containsPoint(p0,p1,p2,candPoints.values());
				if (cp!=null ) {
					while (cp!=null) {
						p2 = cp;
						cp = containsPoint(p0,p1,p2,candPoints.values());
					}
				}
				if (!intersectsWithFront(seg1) && !intersectsWithFront(seg2)) {
					triangle = new Triangle2D((Node2D)p0, (Node2D)p1, (Node2D)p2);
					if (!this.updateFronts(triangle,front)) {
						return null;
					}
					return triangle;
				}
			}
			pc = AlgoPoint2D.evaluate(pm,pc,2.0);
			circle.setValues(pc.getX(),pc.getY(),pc.distance(p0));
			this.getCandidatePoints(front, circle, candPoints);
		} while((cnt++)<20);
		return null;
	}
	
	private boolean intersectsWithFront(Line2D seg) {
		
		//ArrayList cells = this.getCellsIntersectingLine(seg,1.0001);
		ArrayList<QuadtreeCellEnvelope> cells = new ArrayList<QuadtreeCellEnvelope>();
		this.quadtree.getCellEnvelopesIntersectingRectangle(seg.getBounds2D(),cells);
		Point2D p0 = seg.getP1();
		Point2D p1 = seg.getP2();
		HashSet<Edge2D> edges = new HashSet<Edge2D>();
		for (Iterator it = cells.iterator(); it.hasNext();) {
			QuadtreeCellEnvelope cellEnv = (QuadtreeCellEnvelope)it.next();
			QuadtreeCell cell = cellEnv.getCell();
			MeshGeneratorCellAttribute att = (MeshGeneratorCellAttribute)cell.attribute();
			edges.addAll(att.getEdges());
		}
		for (Iterator it = edges.iterator(); it.hasNext();) {
			Edge2D edge = (Edge2D)it.next();
			if ( intersects(p0,p1,edge.getP1(),edge.getP2()) ) {
				 intersects(p0,p1,edge.getP1(),edge.getP2());
				return true;
			}
		}
		return false;
	}

	private boolean intersects(Point2D p0, Point2D p1, Point2D q0, Point2D q1) {
		if (!AlgoRectangle2D.intersects(p0,p1,q0,q1)) return false;
		// BUG: we have to find a more robust intersection routine. With
		// TOL=0.0001 we find intersections where the points are equal
		//double TOL  = 0.0001;
		final double TOL  = 0.001;

		if (p0.equals(q0)) {
			if (p1.equals(q1)) {
				return false;
			}
			return intersectCommonPoint(p0,p1,q1);
		} else if (p1.equals(q1)) {
			return intersectCommonPoint(p1,p0,q0);
		} else if (p0.equals(q1)) {
			if (p1.equals(q0)) {
				return false;				
			}
			return intersectCommonPoint(p0,p1,q0);
		} else if (p1.equals(q0)) {
			return intersectCommonPoint(p1,p0,q1);			
		}
		
		Point2D p01 = AlgoPoint2D.subtract(p1,p0);
		Point2D q01 = AlgoPoint2D.subtract(q1,q0);
		double det = (float)(p01.getX() * q01.getY() - p01.getY() * q01.getX());
		if (det == 0.0) return false;
		Point2D pq = AlgoPoint2D.subtract(q0,p0);
		double r = (AlgoPoint2D.cross(pq,q01))/det;
		double s = (AlgoPoint2D.cross(pq,p01))/det;
		return r > TOL && r < (1-TOL) && s > TOL && s < (1-TOL);
	}
	
	/**
	 * @param p0
	 * @param p1
	 * @param q1
	 * @return
	 */
	private boolean intersectCommonPoint(Point2D p, Point2D p0, Point2D p1) {
		final double TOL  = 0.001;
		return (Line2D.ptSegDist(p.getX(),p.getY(),p0.getX(),p0.getY(),p1.getX(),p1.getY()) < TOL);
	}

	public class DelaunayHeight {//implements Comparable {

		public Node2D point;
		public double height;
		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		
		public DelaunayHeight(double h, Node2D p) {
			point = p;
			height = h;
		}
		public boolean equals(Object obj) {
			return (obj instanceof DelaunayHeight) && this.point.equals(((DelaunayHeight)obj).point);
		}
		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
//		public int compareTo(Object obj) {
//			return (this.height<((DelaunayHeight)obj).height) ? -1 : 1;
//		}
	}
	
	private Node2D containsPoint(Point2D p0, Point2D p1, Point2D p2, Collection candPoints) {
		for (Iterator it = candPoints.iterator();it.hasNext();) {
			DelaunayHeight dh1 = (DelaunayHeight)it.next();
			Node2D p = dh1.point;
			if (!p.equals(p2) &&
					!AlgoLine2D.rightSide(p0,p1,p) && 
					!AlgoLine2D.rightSide(p1,p2,p) && 
					!AlgoLine2D.rightSide(p2,p0,p)) return p;
		}
		return null;
	}
	
	/**
	 * returns a perpendicular line to (p0,p1) starting at the middle node of
	 * this segment (p0,p1). The first returned node is the origin and the
	 * second one the end of the vector
	 * 
	 * @param p0
	 * @param p1
	 * @return
	 */
	private AffineTransform at = new AffineTransform();
	private Point2D getOptimalTrianglePoint(Point2D p0, Point2D p1) {
		at.setToIdentity();
		at.setToRotation(1.0471975511965977461542144610932, p0.getX(), p0.getY());
		return at.transform(p1,null);

	}


	private boolean updateFronts(Triangle2D tri, Edge2D front) {
		
		Node2D p0 = tri.getPoint(0);
		Node2D p1 = tri.getPoint(1);
		Node2D p2 = tri.getPoint(2);
		
		if (!updateFront(p0, p2, tri)) return false;
		
		if (!updateFront(p2, p1, tri)) return false;
		
		if ( !Geo2DFactory.connectEdgeSimplex(front,tri,Edge2D.LEFTSIDE) ) return false;
		
		if ( this.isBoundaryEdge(front) ) {
			// the front was already removed from the front list. removes it
			// from the quadtree cells
			((MeshGeneratorQuadtree)this.quadtree).removeEdgeFromQuadtree(front);			
		} else if ( this.isInternalEdge(front) ) {
			if ( front.getRightSimplex()!=null) {
				// the front was already removed from the front list. Removes it
				// from the quadtree cells
				((MeshGeneratorQuadtree)this.quadtree).removeEdgeFromQuadtree(front);
			} else {
				// The right side of this internal front was not yet
				// triangulated. The front was already removed from the list, so
				// we have to insert it again and invert it
				front.reverse();
				this.front.add(front);				
			} 
		} else {
			// The front is not an internal or a boundary edge. The right side
			// must already be triangulated. The front was already eliminated
			// from the front list. We remove it from the quadtree cells and
			// connect the simplices direct with each other
			((MeshGeneratorQuadtree)this.quadtree).removeEdgeFromQuadtree(front);
			Simplex2D rs = front.getRightSimplex();
			if (!Geo2DFactory.connectSimplexSimplex(rs,tri)) return false;
		}
		return true;
		
	}

	private boolean updateFront(Node2D p0, Node2D p1,Triangle2D tri) {
		// the new triangle will be connected at the right side of the front
		// (po,p1). If the front (p0,p1) do not exist, create one. If there is
		// such a front, but it has other direction (p1,p0) then invert it.
		Edge2D frontSeg = ((MeshGeneratorQuadtree)this.quadtree).findEdge(p0, p1);
		if (frontSeg==null) {
			// there is no front with (p0,p1). We create one, insert it into the
			// quadtree cells and to the front
			Edge2D edge = Geo2DFactory.createEdge(p0,p1,tri,null);
			edge.object = null;
			this.front.add(edge);
			((MeshGeneratorQuadtree)this.quadtree).insertEdgeIntoQuadtree(edge);
			return edge != null;
		
		} else {
			// there is already a front segment between p0 and p1. We check
			// whether the existing front has the same direction as the side of
			// the new created triangle (p0,p1). 
			Point2D p2 = frontSeg.getP1();
			Point2D p3 = frontSeg.getP2();
			if (p0.equals(p2) && p1.equals(p3)) {
				// (p0,p1) and the found front have the same direction. The
				// right side of the front may not be already set.
				if (frontSeg.getRightSimplex()!=null) {
					System.out.println("error 1: updateFrontEdge(Point2D p0, Point2D p1, Edge2D front, Triangle2D tri)");
					return false;
				}
				Geo2DFactory.connectEdgeSimplex(frontSeg,tri,Edge2D.RIGHTSIDE);
				
				if (frontSeg.getLeftSimplex()!=null) {
				
					this.front.remove(frontSeg);

					((MeshGeneratorQuadtree)this.quadtree).removeEdgeFromQuadtree(frontSeg);
					
					if (frontSeg.object==null) {
						// temporary front edge, connect right and left triangles to each other
						Geo2DFactory.connectSimplexSimplex(frontSeg.getRightSimplex(),frontSeg.getLeftSimplex());
					}
				}
				return true;
			
			} else if (p0.equals(p3) && p1.equals(p2)) {
				// (p0,p1) and the found front have opposite directions. As we
				// should connect the triangle (tri) to the right side of
				// (p0,p1), this corresponds to connect to the left of the found
				// front. The left side of the found front may not be set
				if (frontSeg.getLeftSimplex()!=null) {
					System.out.println("error 2: updateFrontEdge(Point2D p0, Point2D p1, Edge2D front, Triangle2D tri)");
					return false;
				}
				Geo2DFactory.connectEdgeSimplex(frontSeg,tri,Edge2D.LEFTSIDE);
				if ( this.isBoundaryEdge(frontSeg) || 
				    (this.isInternalEdge(frontSeg) && frontSeg.getRightSimplex()!=null) ) {
					// this is a internal or a boundary edge. If it is a
					// boundary edge of an internal and both edge sides are set,
					// then we remove the edge from the front
					((MeshGeneratorQuadtree)this.quadtree).removeEdgeFromQuadtree(frontSeg);
					this.front.remove(frontSeg);
				} else if (frontSeg.getRightSimplex()!=null) {
					// the left side of the front edge is set. If the right side is
					// also set, then we can remove the edge and connect the
					// simplices directly with each other
					Geo2DFactory.connectSimplexSimplex(frontSeg.getRightSimplex(),frontSeg.getLeftSimplex());
					((MeshGeneratorQuadtree)this.quadtree).removeEdgeFromQuadtree(frontSeg);
					this.front.remove(frontSeg);
				} else {
					// one side is not set and it is not an internal or boundary
					// edge. Invert it, in order to triangulate the left side of
					// the front and keep it in the front list
					frontSeg.reverse();
				}
				return true;
			} else {
				System.out.println("error 3: updateFrontEdge(Point2D p0, Point2D p1, Edge2D front, Triangle2D tri)");
				return false;
			}
		}
	}

	private boolean isBoundaryEdge(Edge2D edge) {
		return (edge.object==null) ? false : ((EdgeObject)edge.object).isBoundary();
	}
	
	private boolean isInternalEdge(Edge2D edge) {
		return (edge.object==null) ? false : !((EdgeObject)edge.object).isBoundary();
	}
	
	private void getCandidatePoints(Line2D front, Circle2D circle, TreeMap<Double,DelaunayHeight> candidatePoints) {

		Rectangle2D rec = circle.getRectangle();
		ArrayList<QuadtreeCellEnvelope> cellEnvelopes = new ArrayList<QuadtreeCellEnvelope>();
		//cellEnvelopes = this.mshGrid.getCellEnvelopes();
		this.quadtree.getCellEnvelopesIntersectingRectangle(rec,cellEnvelopes);
		// It may happen that not all points are in the list 
		for (Iterator it=cellEnvelopes.iterator();it.hasNext();) {
			QuadtreeCellEnvelope cellEnvelope = (QuadtreeCellEnvelope)it.next();
			QuadtreeCell cell = (QuadtreeCell)cellEnvelope.getCell();
			MeshGeneratorCellAttribute att = (MeshGeneratorCellAttribute)cell.attribute();
			Node2D p = att.node;
			 if ((p != null) && (AlgoLine2D.leftSide(front,p)) && circle.contains(p)) {
			 	Double dh = new Double(this.getDelaunayHeight(front.getP2(),front.getP1(), p));
			 	// the key may be already in use
			 	while (candidatePoints.containsKey(dh)) {
			 		dh = new Double(dh.doubleValue()+ 0.0000000001);
			 	}
			 	candidatePoints.put(dh,new DelaunayHeight(dh.doubleValue(),p));
			}			
		}
	}
	
	private double getDelaunayHeight(Point2D p0, Point2D p1, Point2D p2) {
		// declaration startpoint(dp0), endpoint(dp1) and the middle (pm) of
		// the segment
		double p0x = p0.getX();
		double p0y = p0.getY();
		double p2x = p2.getX();
		double p2y = p2.getY();
		Point2D pm = new Point2D.Double(((0.5 * p1.getX())+(0.5 * p0x)),((0.5 * p1.getY())+(0.5* p0y)));
		double pmx = pm.getX();
		double pmy = pm.getY();
		// length of vector pm to p2
		double centerSquare = Math.abs(
				(pmx * pmx) - (2 * (pmx * p2x)) + (p2x * p2x)+ 
				(pmy * pmy) - (2 * (pmy * p2y)) + (p2y * p2y));
		// length of vector p0 to p1
		double length = 2 * Math.sqrt(
				(pmx * pmx) - (2 * (pmx * p0x)) + (p0x * p0x) + 
				(pmy * pmy) - (2 * (pmy * p0y)) + (p0y * p0y));		
		// Transfer p0 to p0_1
		Point2D p0_1 = AlgoPoint2D.subtract(p0,pm);
		// Transfer p1 to p1_1
		Point2D p1_1 = AlgoPoint2D.subtract(p1,pm);
		// Transfer p2 to p2_1
		Point2D p2_1 = AlgoPoint2D.subtract(p2,pm);		
		// Point dp0__ = dp0 after shifting and rotating
		Point2D p0_2 = new Point2D.Double((-0.5 * length), 0);
		Point2D p1_2 = new Point2D.Double(( 0.5 * length), 0);		
		// to get the rotating angle phi
		double angle = Math.acos(
				((p0_1.getX()  * p0_2.getX()) / (Math.sqrt(
						Math.pow(p0_1.getX(), 2) +	Math.pow(p0_1.getY(), 2)) * Math.sqrt((p0_2.getX() * p0_2.getX())))));
		if (p1_1.getY() >0) {
			angle = ((2 * Math.PI) - angle);
		}
		Point2D p2_2 = new Point2D.Double(
				(Math.cos(angle) * (p2x - pmx)) - (Math.sin(angle) * (p2y - pmy)),
				(Math.sin(angle) * (p2x - pmx))	+ (Math.cos(angle) * (p2y - pmy)));
		double b = (1 / (2 * p2_2.getY())) * (centerSquare - ( (length * length)/4));
		double r = ((Math.sqrt((4*(b*b))+ (length*length)))/2);
		double dh = r - b;
		return ( dh <= 0.0) ? Double.MAX_VALUE : dh;
	}
	
	protected Object[] splitEdge(Edge2D edge, Point2D p) {
		((MeshGeneratorQuadtree) this.quadtree).removeEdgeFromQuadtree(edge);
		Object[] obj = Geo2DFactory.splitEdge(edge,p);
		((MeshGeneratorQuadtree) this.quadtree).insertEdgeIntoQuadtree(edge);
		((MeshGeneratorQuadtree) this.quadtree).insertEdgeIntoQuadtree((Edge2D)obj[0]);
		return obj;
	}


	/**
	 * split the edges intersecting cell without nodes. The new edges will be
	 * inserted w.r.t. the edges sequence
	 * 
	 * @param edges
	 */
	protected void splitEdgesIntersectingCells(ArrayList<Edge2D> edges) {
		
		ArrayList<Edge2D> tmpFront1 = new ArrayList<Edge2D>(edges);
		
		while (!tmpFront1.isEmpty()) {
			Edge2D edge = (Edge2D)tmpFront1.remove(tmpFront1.size()-1);
			Point2D p1 = edge.getP1();
			Point2D p2 = edge.getP2();
			double edgeLengthSq = p1.distanceSq(p2);
			
			ArrayList cells = this.quadtree.getCellsIntersectingLine(edge);
			for (Iterator it1 = cells.iterator(); it1.hasNext();) {
				
				QuadtreeCellEnvelope cellEnv = (QuadtreeCellEnvelope) it1.next();
				QuadtreeCell cell = cellEnv.getCell();
				MeshGeneratorCellAttribute att = (MeshGeneratorCellAttribute) cell.attribute();
				
				if (att.node != null) continue;
				
				double cellSizeSq = (cellEnv.getWidth() > cellEnv.getHeight()) ? 
							cellEnv.getWidth() : cellEnv.getHeight();
				cellSizeSq *= cellSizeSq;
				if (cellSizeSq > edgeLengthSq) continue;
				
				Point2D points[] = AlgoRectangle2D.intersectionRectangleLine(cellEnv, edge);
				if ((points != null) && (points.length == 2) && (!points[0].equals(points[1]))) {
					Point2D p = AlgoPoint2D.evaluate(points[0], points[1], 0.5);
					
					if ((p.distanceSq(p1)>cellSizeSq) && (p.distanceSq(p2)>cellSizeSq)) {
						
						Object obj[] = splitEdge(edge, p);
						Edge2D newEdge = (Edge2D)obj[0];
						Node2D newNode = (Node2D)obj[1];
						int index = edges.indexOf(edge);
						edges.add(index+1,newEdge);
						
						tmpFront1.add(edge);
						tmpFront1.add(newEdge);
						
						att.setGrey();
						att.setNode(newNode);
						
						break;
					}
				}
			}
		}
		
	}
	
}
