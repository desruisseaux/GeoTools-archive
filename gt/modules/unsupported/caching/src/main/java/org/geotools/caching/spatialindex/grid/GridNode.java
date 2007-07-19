package org.geotools.caching.spatialindex.grid;

import java.util.HashMap;

import org.geotools.caching.spatialindex.Region;
import org.geotools.caching.spatialindex.Shape;
import org.geotools.caching.spatialindex.Node ;

public class GridNode implements Node {
	
	int id ;
	Region mbr ;
	boolean visited = false ;
	GridNode parent ;
	HashMap node_data ;
	int num_data ;
	int[] data_ids ;
	Object[] data ;
	
	GridNode(int id, GridNode parent, Region mbr) {
		this.id = id ;
		this.mbr = mbr ;
		this.parent = parent ;
		this.node_data = new HashMap() ;
		this.num_data = 0 ;
		this.data = new Object[10] ;
		this.data_ids = new int[10] ;
	}

	public int getChildIdentifier(int index) throws IndexOutOfBoundsException {
		throw new UnsupportedOperationException("GridNode have no children.") ;
	}

	public Shape getChildShape(int index) throws IndexOutOfBoundsException {
		throw new UnsupportedOperationException("GridNode have no children.") ;
	}

	public int getChildrenCount() {
		return 0;
	}

	public int getLevel() {
		return 0;
	}

	public Node getSubNode(int index) throws IndexOutOfBoundsException {
		throw new UnsupportedOperationException("GridNode have no children.") ;
	}

	public boolean isIndex() {
		return false;
	}

	public boolean isLeaf() {
		return true;
	}

	public boolean isVisited() {
		return visited ;
	}

	public void setVisited(boolean visited) {
		this.visited = visited ;
	}

	public int getIdentifier() {
		return id ;
	}

	public Shape getShape() {
		return mbr ;
	}
	
	protected void insertData(int id, Object data) {
		if (num_data == data_ids.length) {
			int[] n_data_ids = new int[data_ids.length*2] ;
			Object[] n_data = new Object[this.data.length*2] ;
			System.arraycopy(data_ids, 0, n_data_ids, 0, num_data) ;
			System.arraycopy(this.data, 0, n_data, 0, num_data) ;
			data_ids = n_data_ids ;
			this.data = n_data ;
		}
		data_ids[num_data] = id ;
		this.data[num_data] = data ;
		num_data++ ;
	}
	
	protected void deleteData(int index) {
		if ((index < 0) || (index > num_data-1)) {
			throw new IndexOutOfBoundsException() ;
		}
		if (index < num_data-1) {
			data_ids[index] = data_ids[num_data-1] ;
			data[index] = data[num_data-1] ;
		}
		data[num_data-1] = null ;
		num_data-- ;
	}

}
