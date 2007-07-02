package org.geotools.caching.quatree;

import java.util.Stack;
import java.util.logging.Logger;

import org.geotools.caching.spatialindex.spatialindex.IData;
import org.geotools.caching.spatialindex.spatialindex.INearestNeighborComparator;
import org.geotools.caching.spatialindex.spatialindex.INodeCommand;
import org.geotools.caching.spatialindex.spatialindex.IQueryStrategy;
import org.geotools.caching.spatialindex.spatialindex.IShape;
import org.geotools.caching.spatialindex.spatialindex.ISpatialIndex;
import org.geotools.caching.spatialindex.spatialindex.IStatistics;
import org.geotools.caching.spatialindex.spatialindex.IVisitor;
import org.geotools.caching.spatialindex.spatialindex.Region;
import org.geotools.caching.spatialindex.storagemanager.PropertySet;

import com.vividsolutions.jts.geom.Envelope;

public class QuadTree implements ISpatialIndex {
	
    private static final double SPLITRATIO = 0.55d;
    
    private static final Logger LOGGER =
        Logger.getLogger("org.geotools.caching.quadtree");
    
	private Node root ;
	
	// Constructors
	
	public QuadTree(Region bounds) {
		this.root = new Node(new Region(bounds), 0, null) ;
	}
	
	// Interface

	public void addDeleteNodeCommand(INodeCommand nc) {
		// TODO Auto-generated method stub

	}

	public void addReadNodeCommand(INodeCommand nc) {
		// TODO Auto-generated method stub

	}

	public void addWriteNodeCommand(INodeCommand nc) {
		// TODO Auto-generated method stub

	}

	public void containmentQuery(IShape query, IVisitor v) {
		Node current = this.root ;
		current.visited = false ;
		Stack nodes = new Stack() ;
		if (current.getShape().contains(query)) {
			nodes.push(current) ;
		}
		while (!nodes.isEmpty()) {
			current = (Node) nodes.pop() ;
			if (!current.visited) {
				v.visitNode(current) ;
				for (int i = 0 ; i < current.getChildrenCount() ; i++) {
					current.getSubNode(i).visited = false ;
				}
				for (int i = 0 ; i < current.numShapes ; i++) {
					v.visitData(new Data(current.shapesData[i], null, current.shapesId[i])) ;
				}
				current.visited = true ;
			}
			for (int i = 0 ; i < current.getChildrenCount() ; i++) {
				Node child = current.getSubNode(i) ;
				if (!child.visited) {
					if (child.getShape().contains(query)) {
						// we will go back to this one to examine other children
						nodes.push(current) ;
						nodes.push(child) ;
						break ;
					} else {
						child.visited = true ;
					}
				}
			}
		}
	}

	public boolean deleteData(IShape shape, int id) {
		// TODO Auto-generated method stub
		return false;
	}

	public void flush() throws IllegalStateException {
		// TODO Auto-generated method stub

	}

	public PropertySet getIndexProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	public IStatistics getStatistics() {
		// TODO Auto-generated method stub
		return null;
	}

	public void insertData(byte[] data, IShape shape, int id) {
		insertData(this.root, data, shape, id) ;
	}

	public void intersectionQuery(IShape query, IVisitor v) {
		Node current = this.root ;
		current.visited = false ;
		Stack nodes = new Stack() ;
		if (current.getShape().intersects(query)) {
			nodes.push(current) ;
		}
		while (!nodes.isEmpty()) {
			current = (Node) nodes.pop() ;
			if (!current.visited) {
				v.visitNode(current) ;
				for (int i = 0 ; i < current.getChildrenCount() ; i++) {
					current.getSubNode(i).visited = false ;
				}
				for (int i = 0 ; i < current.numShapes ; i++) {
					v.visitData(new Data(current.shapesData[i], null, current.shapesId[i])) ;
				}
				current.visited = true ;
			}
			for (int i = 0 ; i < current.getChildrenCount() ; i++) {
				Node child = current.getSubNode(i) ;
				if (!child.visited) {
					if (child.getShape().intersects(query)) {
						// we will go back to this one later to examine other children
						nodes.push(current) ;
						nodes.push(child) ;
						break ;
					} else {
						child.visited = true ;
					}
				}
			}
		}
	}

	public boolean isIndexValid() {
		// TODO Auto-generated method stub
		return false;
	}

	public void nearestNeighborQuery(int k, IShape query, IVisitor v,
			INearestNeighborComparator nnc) {
		// TODO Auto-generated method stub

	}

	public void nearestNeighborQuery(int k, IShape query, IVisitor v) {
		// TODO Auto-generated method stub

	}

	public void pointLocationQuery(IShape query, IVisitor v) {
		// TODO Auto-generated method stub

	}

	public void queryStrategy(IQueryStrategy qs) {
		int[] next = new int[] { this.root.id } ;
		
		Node current = this.root ;
		
		while (true) {
			boolean[] hasNext = new boolean[] { false } ;
			qs.getNextEntry(current, next, hasNext) ;
			if (!hasNext[0]) {
				break ;
			} else {
				if (next[0] < 0) {
					current = current.parent ;
				} else {
					current = current.getSubNode(next[0]) ;
				}
			}
		}
		
	}
	
	public void queryStrategy(QueryStrategy qs) {
		Node current = this.root ;
		while (true) {
			boolean[] hasNext = new boolean[] { false } ;
			current = qs.getNextNode(current, hasNext) ;
			if (hasNext[0] == false) {
				break ;
			}
		}
	}
	
	// Internals
	
	protected void insertData(Node n, byte[] data, IShape shape, int id) {
		// TODO consider using maximum Depth
		if (n.isIndex()) {
            /* If there are subnodes, then consider whether this object
             * will fit in them.
             */
			for (int i = 0; i < n.getChildrenCount() ; i++) {
				Node subNode = n.getSubNode(i) ;
				if (subNode.getShape().contains(shape)) {
					insertData(subNode, data, shape, id) ;
					return ;
				}
			}
		} else {
			/* Otherwise, consider creating four subnodes if could fit into
             * them, and adding to the appropriate subnode.
             */
			n.split(SPLITRATIO) ;
			// recurse
			insertData(n, data, shape, id) ;
		}
		// If none of that worked, just add it to this nodes list.
		n.insertData(data, id) ;
	}
	
	class Data implements IData {
		
		private byte[] data ;
		private int id ;
		private IShape shape ;

		public Data(byte[] data, Region mbr, int id) {
			this.data = data ;
			this.shape = mbr ;
			this.id = id ;
		}
		
		public byte[] getData() {
			return data ;
		}

		public int getIdentifier() {
			return id ;
		}

		public IShape getShape() {
			return shape ;
		}
		
	}


}
