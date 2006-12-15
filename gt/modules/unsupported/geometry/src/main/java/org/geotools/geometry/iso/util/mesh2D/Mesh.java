/*
 * Created on 11.08.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.geometry.iso.util.mesh2D;

import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.geotools.geometry.iso.util.algorithm2D.AlgoArea;
import org.geotools.geometry.iso.util.algorithm2D.AlgoLine2D;
import org.geotools.geometry.iso.util.algorithm2D.AlgoPoint2D;
import org.geotools.geometry.iso.util.algorithm2D.AlgoRectangle2D;
import org.geotools.geometry.iso.util.elem2D.Edge2D;
import org.geotools.geometry.iso.util.elem2D.Geo2DFactory;
import org.geotools.geometry.iso.util.elem2D.Node2D;
import org.geotools.geometry.iso.util.elem2D.Triangle2D;
import org.geotools.geometry.iso.util.quadtree.QuadtreeCellEnvelope;

/**
 * @author roehrig
 *
 */
public class Mesh {

	public class MeshObservable extends Observable {
		
		public Mesh mesh = null;
		
		/**
		 * @param generator
		 */
		public MeshObservable(Mesh mesh) {
			this.mesh = mesh;
		}

		public void sendMessage(String message) {
			this.setChanged();
			this.notifyObservers(message);
		}
	}
	
	protected MeshObservable observable;

	/**
	 * posts are the constraint points, which are not part of the rings and
	 * breaklines. Posts will be triangle nodes
	 * 
	 * boundary are splitted according to maxLength 
	 * 
	 * ArrayList containing lists of Point2D, whereas each list corresponds to a
	 * ring. The first ring is the external ring and shall be oriented
	 * clockwise. The further collections of Points2D are the internal rings,
	 * which shall be oriented counterclockwise
	 * 
	 * breaklines are contraint lines, which will be part of the front, and
	 * hence, sides of triangles. Examples are rivers, streets, canalization,
	 * etc. ArrayList <ArrayList <Point2D>>
	 */	
	protected MeshQuadtree quadtree = null;

	protected ArrayList<Triangle2D> triangles = null;
	
	protected ArrayList<ArrayList<Edge2D>> boundaryEdges = null;

	protected ArrayList<Edge2D> internalEdges = null;

	protected ArrayList<Node2D> posts = null;

	/**
	 * inserts the mesh of triangles, edges and points. Edges, if present, must
	 * concide with triangle sides. If no edge was given, edges will be created
	 * for the boundary of triangles
	 * 
	 * @param triangles
	 * @param edges
	 * @param posts
	 */
	public Mesh(ArrayList<Point2D[]> triangles, ArrayList<Point2D[]> edges, ArrayList<Point2D> constraintPoints) {
		
		this.observable = new MeshObservable(this);
		
		this.posts = new ArrayList<Node2D>(constraintPoints.size());
		for (Point2D p : constraintPoints) {
			Node2D n= Geo2DFactory.createNode(p);
			if (n!=null) this.posts.add(n);
		}
		
		if (edges != null) {
			while (edges.remove(null));
			this.internalEdges = new ArrayList<Edge2D>(edges.size());
			for (Point2D[] p :  edges) {
				if (p!=null && p.length>1) {
					Edge2D e = Geo2DFactory.createEdgeAndNodes(p[0].getX(),p[0].getY(),p[1].getX(),p[1].getY());
					if (e!=null) this.internalEdges.add(e);
				}
			}
		}
	
		if (triangles != null) {
			while (triangles.remove(null));
			this.triangles = new ArrayList<Triangle2D> (triangles.size());
			for (Point2D[] p :  triangles) {
				if (p!=null && p.length>2) {
					Triangle2D t = Geo2DFactory.createTriangleAndNodes(p[0].getX(),p[0].getY(),p[1].getX(),p[1].getY(),p[2].getX(),p[2].getY());
					if (t!=null) this.triangles.add(t);
				}
			}

		}
		
		this.createQuadtree(-1.0);
		
		this.insertNodesIntoQuadtree();
		
		this.setTopology();
		
		this.balanceQuadtree();

	}

	public Mesh(ArrayList<ArrayList<Point2D>> bdryRings, ArrayList<ArrayList<Point2D>> breakLines, 
			ArrayList<Point2D> constraintPoints, double maxLength, Collection<Observer> observers) {
		
		this.observable = new MeshObservable(this);
		if (observers!=null) {
			for (Iterator<Observer> it = observers.iterator(); it.hasNext(); ) {
				this.observable.addObserver(it.next());
			}
		}

		this.observable.sendMessage("building boundary...");
		if ( (bdryRings != null) && !bdryRings.isEmpty() ) {
			
			// check whether the external ring is the first one
			// check the orientation of the external ring (clockwise)
			// check the orientation of the internal rings (counterclockwise)
			// tranform rings into front
			createBoundaryEdges(bdryRings);
		}
		
		this.observable.sendMessage("building breaklines...");
		if ( (breakLines != null) && !breakLines.isEmpty() ) {
			
			for (Iterator<ArrayList<Point2D>> it0 = breakLines.iterator(); it0.hasNext();) {
				ArrayList<Point2D> coll = it0.next();
				// remove all null values from the list
				while (coll.remove(null)) {}
			}
			
			// createInternalFront
			this.internalEdges = new ArrayList<Edge2D>();
			for (Iterator<ArrayList<Point2D>> it0 = breakLines.iterator(); it0.hasNext();) {
				ArrayList<Point2D> coll = it0.next();
				Iterator it1 = coll.iterator();
				Point2D p0 = (it1.hasNext()) ? (Point2D) it1.next() : null;
				while (it1.hasNext()) {
					Point2D p1 = (Point2D) it1.next();
					Edge2D edge = Geo2DFactory.createEdgeAndNodes(p0.getX(),p0.getY(), p1.getX(), p1.getY());
					this.internalEdges.add(edge);
					p0 = p1;
				}
			}
		}
		

		if (constraintPoints != null) {
			// remove all null values from the list
			while(constraintPoints.remove(null)) {}
			this.posts = new ArrayList<Node2D>(constraintPoints.size());
			for (Point2D p : constraintPoints) {
				Node2D n= Geo2DFactory.createNode(p);
				if (n!=null) this.posts.add(n);
			}
		}
		
		this.observable.sendMessage("creating quadtree...");
		this.createQuadtree(maxLength);
		
		this.observable.sendMessage("insert nodes into quadtree...");
		this.insertNodesIntoQuadtree();
		
		this.observable.sendMessage("divide to given size...");
		this.quadtree.divideToGivenSize(maxLength);
		
		this.observable.sendMessage("balancing quadtree...");
		this.balanceQuadtree();
		
		this.observable.sendMessage("");

	}

	/**
	 * @param bdryRings
	 */
	protected void createBoundaryEdges(ArrayList<ArrayList<Point2D>> bdryRings) {
		if  (checkExternalRingSize(bdryRings) && checkBoundaryOrientation(bdryRings)) {
			this.boundaryEdges = new ArrayList<ArrayList<Edge2D>>();
			for (Iterator<ArrayList<Point2D>> it0 = bdryRings.iterator(); it0.hasNext();) {
				ArrayList<Point2D> coll = it0.next();
				ArrayList<Edge2D> ring = createRing(coll);
				if (ring!=null) this.boundaryEdges.add(ring);
			}
		}
	}

	protected ArrayList<Edge2D> createRing(ArrayList<Point2D> points) {
		while(points.remove(null)) {}
		if (points == null || points.size()<3) return null;
		ArrayList<Edge2D> ring = new ArrayList<Edge2D>();
		Iterator<Point2D> it = points.iterator();
		Point2D p0 = it.next();
		while(it.hasNext()) {
			Point2D p1 = it.next();
			Edge2D edge = Geo2DFactory.createEdgeAndNodes(p0.getX(),p0.getY(), p1.getX(),p1.getY());
			ring.add(edge);
			p0 = p1;
		}
		// assure that the first and last nodes are identical
		Geo2DFactory.mergeNodes(ring.get(0).getNode1(),ring.get(ring.size()-1).getNode2());
		return ring;		
	}

	//	private boolean checkEdges(ArrayList<Edge2D> edges) {
//		boolean check = true;
//		for (Edge2D e : edges) {
//			Node2D n1 = e.getNode1();
//			Node2D n2 = e.getNode2();
//			Edge2D[] e1 = n1.getEdges();
//			for (int i=0, cnt=0; i<e1.length; ++i) {
//				if (e1[i]==e) cnt++;
//				if (cnt>1) {
//					System.out.println("checkEdges(ArrayList<Edge2D> edges) found an error");
//					check = false;
//				}
//			}
//			Edge2D[] e2 = n1.getEdges();
//			for (int i=0, cnt=0; i<e2.length; ++i) {
//				if (e2[i]==e) cnt++;
//				if (cnt>1) {
//					System.out.println("checkEdges(ArrayList<Edge2D> edges) found an error");
//					check = false;
//				}
//			}
//		}
//		return check;
//	}
	/**
	 * 
	 */

	public ArrayList<Node2D> getPosts() {
		return this.posts;
	}

	public ArrayList<Node2D> getNodes() {
		return this.quadtree.getNodes();
	}
	
	public ArrayList<Edge2D> getEdges() {
		ArrayList<Edge2D> result = new ArrayList<Edge2D>();
		ArrayList<Edge2D> bdrEdges = getBoundaryEdges();
		ArrayList<Edge2D> intEdges = getInternalEdges();
		if (bdrEdges!=null) result.addAll(bdrEdges);
		if (intEdges!=null) result.addAll(intEdges);
		return result;
	}

	public ArrayList<ArrayList<Edge2D>> getBoundaryRings() {
		return this.boundaryEdges;
	}
	/**
	 * @return
	 */
	public ArrayList<Edge2D> getBoundaryEdges() {
		ArrayList<Edge2D> result = new ArrayList<Edge2D>();
		if ( ( this.boundaryEdges != null ) && !this.boundaryEdges.isEmpty() ) {
			for (Iterator<ArrayList<Edge2D>> it = this.boundaryEdges.iterator(); it.hasNext(); ) {
				ArrayList<Edge2D> ring = it.next();
				result.addAll(ring);
			}			
		}
		return result;
	}

	/**
	 * @return Returns the internalEdges.
	 */
	public ArrayList<Edge2D> getInternalEdges() {
		return internalEdges;
	}
	
	/**
	 * @param envelopeList
	 */
	public ArrayList<QuadtreeCellEnvelope> getCellEnvelopes() {
		return (this.quadtree==null) ? null : this.quadtree.getCellEnvelopes();
	}

	public ArrayList<Triangle2D> getTriangles() {
		return this.triangles;
	}

	public void enumerateElements() {
		this.enumeratePoints();
		this.enumerateEdges();
		this.enumerateTriangles();
	}
	/**
	 * 
	 */
	private void setTopology() {
		
		Geo2DFactory.setTopology(this.triangles, this.internalEdges);
		
		// create edges for boundary trangles
		ArrayList<Edge2D> edges = new ArrayList<Edge2D>();
		for (Triangle2D t : this.triangles) {
			Object obj[] = t.getNeighbours();
			for (int i=0; i<3; ++i) {
				if (obj[i]==null) {
					edges.add( Geo2DFactory.createEdge(t.getPoint(i), t.getPoint((i+1)%3), null, t) );
				}
			}
		}

		ArrayList<ArrayList<Edge2D>> rings = new ArrayList<ArrayList<Edge2D>>();
		Rectangle2D rMax = null;
		ArrayList<Edge2D> ringMax = null;
		int size = edges.size();
		int cnt = 0;
		while (!edges.isEmpty()) {
			assert (cnt++) < size;
			Edge2D e = edges.remove(edges.size()-1);
			assert e.getLeftSimplex()!=null & e.getRightSimplex()==null;
			ArrayList<Edge2D> ring = new ArrayList<Edge2D>();
			rings.add(ring);
			ring.add(e);
			Node2D n1 = e.getNode1();
			Node2D n2 = e.getNode2();
			Node2D nBeg = n1;
			Rectangle2D r = AlgoRectangle2D.createRectangle(n1,n2);
			while (n2!=nBeg) {
				Edge2D ne[] = n2.getEdges();
				for (int i=0; i<ne.length; ++i) {
					Edge2D e1 = ne[i];
					if (e1!=e && e1.getRightSimplex()==null) {
						ring.add(e1);
						edges.remove(e1);
						n2 = e1.getNode2();
						r.add(n2);
						e = e1;
						break;
					}
				}
			}
			if (rMax==null ||  r.contains(rMax) ) {
				rMax = r;
				ringMax = ring;
			}
		}
		this.boundaryEdges = new ArrayList<ArrayList<Edge2D>>();
		this.boundaryEdges.add(ringMax);
		for (ArrayList<Edge2D> ring :  rings) {
			if (ring != ringMax) {
				this.boundaryEdges.add(ring);
			}
		}
	}

	protected static boolean checkExternalRingSize(ArrayList rings) {
		Iterator it = rings.iterator();
		if (!it.hasNext()) return false;
		Rectangle2D extEnv = AlgoPoint2D.getEnvelope((Collection)it.next());
		while(it.hasNext()) {
			// internal rings
			Rectangle2D intEnv = AlgoPoint2D.getEnvelope((Collection)it.next());
			if (intEnv==null || !extEnv.contains(intEnv)) return false;
		}
		return true;
	}

	protected static boolean checkBoundaryOrientation(ArrayList rings) {
		Iterator it = rings.iterator();
		if (!it.hasNext()) return false;
		List ring = (List)it.next();
		Boolean orientation = AlgoPoint2D.pointsOrientation(ring);
		if (!orientation.booleanValue()) {
			Collections.reverse(ring);
		}
		while(it.hasNext()) {
			// internal rings
			ring = (List)it.next();
			orientation = AlgoPoint2D.pointsOrientation(ring);
			if (orientation.booleanValue()) {
				Collections.reverse(ring);
			}
		}
		return true;
	}

	/**
	 * 
	 */
	protected void enumeratePoints() {
		int maxID = -1;
		for (Iterator it = this.getNodes().iterator(); it.hasNext(); ) {
			Node2D n = (Node2D)it.next();
			if (n.hasID() && (n.id > maxID)) maxID = n.id;
		}
		for (Iterator it = this.getNodes().iterator(); it.hasNext(); ) {
			Node2D n = (Node2D)it.next();
			if (!n.hasID()) n.id = ++maxID;
		}
		
	}

	/**
	 * 
	 */
	protected void enumerateEdges() {
		int id = 0;
		for (Iterator it = this.boundaryEdges.iterator(); it.hasNext(); ) {
			Collection ring = (Collection)it.next();
			for (Iterator it1 = ring.iterator(); it1.hasNext(); ) {
	 			Edge2D e = (Edge2D)it1.next();
				e.id = id++;				
			}
		}
	}

	/**
	 * 
	 */
	protected void enumerateTriangles() {
		int id = 0;
		for (Iterator it = this.triangles.iterator(); it.hasNext(); ) {
			Triangle2D t = (Triangle2D)it.next();
			t.id = id++;
		}
	}

	protected Area getBoundaryArea() {
		
		if ( (this.boundaryEdges == null) || this.boundaryEdges.isEmpty()) return null;
		
		GeneralPath path = AlgoArea.createGeneralPathFromEdges((ArrayList)this.boundaryEdges.get(0));

		Area area = new Area(path);
		
		for (int i=1, n=this.boundaryEdges.size(); i<n;++i) {
			ArrayList ring = (ArrayList)this.boundaryEdges.get(i);
			Area internalArea = new Area(AlgoArea.createGeneralPathFromEdges(ring));
			area.subtract(internalArea);
		}
		return area;
	}

	protected MeshQuadtree createQuadtree(Rectangle2D r, double cellLength) {
		return new MeshQuadtree(AlgoRectangle2D.createScale(r, 1.1),cellLength);
	}

	private void createQuadtree(double cellLength) {

		Rectangle2D env = MeshQuadtree.createRectangle(this.posts, this.getEdges(), this.triangles);
		// expands the bounding box of the region
		// by changing this value consider that the artificial front created by
		// the subclass MeshGenerator uses the boundary of the quadtree.
		AlgoRectangle2D.setScale(env,1.05);
		this.quadtree = createQuadtree(env,cellLength);

	}

	private boolean insertNodesIntoQuadtree() {

		if ( this.quadtree == null ) return false;
		
		this.quadtree.insertNodesRemoveDuplicates(this.posts);


		this.quadtree.insertEdgesNodesRemoveDuplicates(this.getEdges());

		this.quadtree.insertTrianglesNodesRemoveDuplicates(this.triangles);

		return true;
	}

	private void balanceQuadtree() {		
		
		this.quadtree.balance(1);
		
		this.quadtree.balanceRoot();
	
	}

	public void validate() {

		// remove breaklines coincident with boundary edges
		for (Iterator<ArrayList<Edge2D>> it = this.boundaryEdges.iterator(); it.hasNext(); ) {
			ArrayList<Edge2D> ring = it.next();
			for (Iterator<Edge2D> it1 = ring.iterator(); it1.hasNext(); ) {
				Edge2D edge = it1.next();
				Node2D n1 = edge.getNode1();
				Node2D n2 = edge.getNode2();
				Edge2D[] edges = n1.getEdges();
				for (int i=0; i<edges.length; ++i) {
					if ( (edges[i]!=edge) && edges[i].hasPoint(n2)) {
						boolean removed = this.internalEdges.remove(edges[i]);
						assert removed;
					}
				}
			}
		}
	
		// split edges containing interior nodes
		for (Iterator<ArrayList<Edge2D>> it = this.boundaryEdges.iterator(); it.hasNext(); ) {
			splitEdgesContainingNodes( it.next() );			
		}
		
		splitEdgesContainingNodes( this.getInternalEdges() );
		

		
	}
	
	private void splitEdgesContainingNodes(ArrayList<Edge2D> edges1) {
		if (edges1==null) return;
		ArrayList<Edge2D> edges = new ArrayList<Edge2D>(edges1);
		// validate point edge intersection
		for (Iterator<Edge2D> it1 = edges1.iterator(); it1.hasNext(); ) {
			Edge2D edge = it1.next();
			Node2D n1 = edge.getNode1();
			Node2D n2 = edge.getNode2();
			double lenSq = edge.getLengthSq() * 0.001;
			Rectangle2D r = edge.getBounds2D();
			ArrayList<QuadtreeCellEnvelope>  cel = this.quadtree.getCellsIntersectingLine(edge);
			for (Iterator<QuadtreeCellEnvelope> it2 = cel.iterator(); it2.hasNext(); ) {
				QuadtreeCellEnvelope ce = it2.next();
				MeshGeneratorCellAttribute att = (MeshGeneratorCellAttribute)ce.getCellAttribute();
				Node2D n = att.getNode();
				if ( (n!=null) && (n!=n1) && (n!=n2)) {
					if (edge.ptLineDistSq(n) < lenSq) {
						double par = AlgoLine2D.constrParamForPoint(n1,n2,n);
						if (!Double.isNaN(par)) {
							Point2D p = AlgoLine2D.evaluate(edge,par);
							Object obj[] = this.splitEdge( edge, p );
							Edge2D newEdge = (Edge2D)obj[0]; 
							Node2D newNode = (Node2D)obj[1];
							int index = edges.indexOf(edge);
							edges.add(index+1,newEdge);
						} else {
							if (n.distanceSq(n1) < n.distanceSq(n2)) {
								Geo2DFactory.mergeNodes(n1,n);
							} else {
								Geo2DFactory.mergeNodes(n2,n);								
							}
						}
					}
				}
			}
		}
		edges1.clear();
		edges1.addAll(edges);
	}
	
	protected Object[] splitEdge(Edge2D edge, Point2D p) {
		return Geo2DFactory.splitEdge(edge,p);
	}

}
