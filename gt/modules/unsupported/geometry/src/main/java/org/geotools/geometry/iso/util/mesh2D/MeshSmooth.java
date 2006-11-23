package org.geotools.geometry.iso.util.mesh2D;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.geotools.geometry.iso.util.elem2D.Edge2D;
import org.geotools.geometry.iso.util.elem2D.Node2D;
import org.geotools.geometry.iso.util.elem2D.Simplex2D;
import org.geotools.geometry.iso.util.elem2D.Triangle2D;

/**
 * @author roehrig
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class MeshSmooth {

	private class NodeAttribute {
		protected Object object;
		protected boolean fixed;
		protected ArrayList<Triangle2D> triangles = new ArrayList<Triangle2D>();
		
		protected NodeAttribute(Object obj, boolean fixed) {
			this.object = obj;
			this.fixed = fixed;
		}
	}

	enum SmoothType {
		LAPLACEAREA
	} 
	
	protected Mesh mesh;
	
	public SmoothType smoothType = SmoothType.LAPLACEAREA;

	/**
	 * @param triangles
	 * @param boundaryEdges
	 * @param points
	 */
	public MeshSmooth(Mesh mesh) {		
		this.mesh = mesh;
	}

	/**
	 * 
	 */

	public void smooth() {
		
		ArrayList<Node2D> nodeList = mesh.getNodes();
		
		setNodeAttributes(nodeList);
		
		if (smoothType==SmoothType.LAPLACEAREA) {
			smoothLaplaceArea(nodeList);
		}
		
		resetNodeAttributes(nodeList);
		

	}
	/**
	 * 
	 */
	private void resetNodeAttributes(ArrayList<Node2D> nodeList) {
		for ( Iterator<Node2D> it = nodeList.iterator(); it.hasNext();) {
			Node2D n = it.next();
			n.object = ((NodeAttribute)n.object).object;
		}
	}

	/**
	 * 
	 */
	private void setNodeAttributes(ArrayList<Node2D> nodeList) {

		// first set all nodes moveable
		for ( Iterator<Node2D> it = nodeList.iterator(); it.hasNext();) {
			Node2D n = it.next();
			n.object = new NodeAttribute(n.object,false);
		}

		// fix the posts 
		if ( mesh.posts!=null ) {
			for ( Iterator<Node2D> it = mesh.getPosts().iterator(); it.hasNext();) {
				Node2D n = it.next();
				((NodeAttribute)n.object).fixed = true;
			}			
		}

		// fix boundary nodes
		if ( mesh.boundaryEdges != null ) {
			for ( Iterator<ArrayList<Edge2D>> it = mesh.boundaryEdges.iterator(); it.hasNext();) {
				ArrayList<Edge2D> edges = it.next();
				for (Iterator<Edge2D> it1 = edges.iterator(); it1.hasNext();){
					Edge2D edge = it1.next();
					Node2D n1 = edge.getNode1();
					Node2D n2 = edge.getNode2();
					((NodeAttribute)n1.object).fixed = true;
					((NodeAttribute)n2.object).fixed = true;
				}
			}			
		}
		// fix breakline nodes
		if (mesh.internalEdges != null) {
			for ( Iterator<Edge2D> it = mesh.internalEdges.iterator(); it.hasNext();) {
				Edge2D edge = it.next();
				Node2D n1 = edge.getNode1();
				Node2D n2 = edge.getNode2();
				((NodeAttribute)n1.object).fixed = true;
				((NodeAttribute)n2.object).fixed = true;
			}			
		}
	}

	private void smoothLaplaceArea(ArrayList<Node2D> nodeList) {

		for (Iterator<Node2D> it0 = nodeList.iterator(); it0.hasNext();) {
			
			Node2D node = it0.next();
			
			if ( ((NodeAttribute)node.object).fixed ) continue;

			Set<Simplex2D> triangles = node.getSimplices();

			HashSet<Node2D> nodeSet = new HashSet<Node2D>();
			for (Iterator it1 = triangles.iterator(); it1.hasNext();) {
				Node2D nodes[] = ((Triangle2D) it1.next()).getPoints();
				if (nodes[0] != node)
					nodeSet.add(nodes[0]);
				if (nodes[1] != node)
					nodeSet.add(nodes[1]);
				if (nodes[2] != node)
					nodeSet.add(nodes[2]);
			}

			double xNew = 0.0;
			double yNew = 0.0;
			double summe = 0.0;
			double area[] = new double[triangles.size()];
			int i = 0;
			for (Iterator it1 = triangles.iterator(); it1.hasNext();) {
				Triangle2D triangle = (Triangle2D) it1.next();
				Point2D center = triangle.getCentroid();
				xNew += (triangle.getArea() * center.getX());
				yNew += (triangle.getArea() * center.getY());
				area[i] = triangle.getArea();
				summe += area[i++];
			}
			xNew /= summe;
			yNew /= summe;
			double xOld = node.getX();
			double yOld = node.getY();
			node.setLocation(xNew, yNew);
			i = 0;
			for (Iterator it1 = triangles.iterator(); it1.hasNext();) {
				Triangle2D triangle = (Triangle2D) it1.next();
				if (area[i++] * triangle.getArea() < 0.0) {
					node.setLocation(xOld, yOld);
					break;
				}
			}
		}
	}

}
