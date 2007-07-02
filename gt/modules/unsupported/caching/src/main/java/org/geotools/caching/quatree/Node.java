package org.geotools.caching.quatree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.geotools.caching.spatialindex.spatialindex.INode;
import org.geotools.caching.spatialindex.spatialindex.IShape;
import org.geotools.caching.spatialindex.spatialindex.Region;

public class Node implements INode {
	
	private Region bounds;
    protected int numShapes;
    protected int[] shapesId;
    protected byte[][] shapesData ;
    protected List subNodes;
    protected Node parent;
    protected boolean visited=false;
    protected boolean childrenVisited=false;
	protected int id;
	protected int level ;
	
	// Constructors
	
	public Node(Region s, int id, Node parent) {
		this.bounds = s ;
		this.id = id ;
		this.parent = parent ;
		this.subNodes = new ArrayList() ;
		this.numShapes = 0 ;
		this.shapesId = new int[4] ;
		Arrays.fill(shapesId, -1) ;
		this.shapesData = new byte[4][] ;
		if (parent == null) {
			this.level = 0 ;
		} else {
			this.level = parent.level-1 ;
		}
	}
	
	// Interface

	public int getChildIdentifier(int index) throws IndexOutOfBoundsException {
		return ((Node) subNodes.get(index)).getIdentifier() ;
	}

	public IShape getChildShape(int index) throws IndexOutOfBoundsException {
		return ((Node) subNodes.get(index)).getShape() ;
	}

	public int getChildrenCount() {
		return subNodes.size() ;
	}

	public int getLevel() {
		return level ;
	}

	public boolean isIndex() {
		return !isLeaf() ;
	}

	public boolean isLeaf() {
		return subNodes.isEmpty() ;
	}

	public int getIdentifier() {
		return id ;
	}

	public IShape getShape() {
		return bounds ;
	}
	
	// Internal
	
	protected Node getSubNode(int index) {
		return (Node) subNodes.get(index) ;
	}
	
	protected void addSubNode(Node n) {
		subNodes.add(n) ;
	}
	
	protected void split(double SPLITRATIO) {
		assert(isLeaf()) ;
		Region half1, half2 ;
		Region[] quads = new Region[4] ;
		Region[] tmp = splitBounds(bounds, SPLITRATIO) ;
		half1 = tmp[0] ;
		half2 = tmp[1] ;
		tmp = splitBounds(half1, SPLITRATIO) ;
		quads[0] = tmp[0] ;
		quads[1] = tmp[1] ;
		tmp = splitBounds(half2, SPLITRATIO) ;
		quads[2] = tmp[0] ;
		quads[3] = tmp[1] ;
		for (int i = 0 ; i < 4 ; i++) {
			addSubNode(new Node(quads[i], i, this)) ;
		}
	}
	
	/**
     * Splits the specified Envelope
     * @param in an Envelope to split
     * @return an array of 2 Envelopes
     */
    protected Region[] splitBounds(Region in, double SPLITRATIO) {
        Region[] ret = new Region[2];
        double range, calc;
        
        if ((in.m_pHigh[0] - in.m_pLow[0]) > (in.m_pHigh[1] - in.m_pLow[1])) {
            // Split in X direction
            range = in.m_pHigh[0] - in.m_pLow[0] ;
            
            calc = in.m_pLow[0] + range * SPLITRATIO;
            ret[0] = new Region(in) ;
            ret[0].m_pHigh[0] = calc ;
            
            calc = in.m_pHigh[0] - range * SPLITRATIO;
            ret[1] = new Region(in) ;
            ret[1].m_pLow[0] = calc ;
            
        } else {
            // Split in Y direction
            range = in.m_pHigh[1] - in.m_pLow[1] ;
            
            calc = in.m_pLow[1] + range * SPLITRATIO;
            ret[0] = new Region(in) ;
            ret[0].m_pHigh[1] = calc ;
            
            calc = in.m_pHigh[1] - range * SPLITRATIO;
            ret[1] = new Region(in) ;
            ret[1].m_pLow[1] = calc ;
        }

        return ret;
    }
    
    protected void insertData(byte[] data, int id) {
    	if (shapesId.length == numShapes) {
    		// increases storage size
    		int[] newIds = new int[shapesId.length*2] ;
    		byte[][] newData = new byte[shapesData.length*2][] ;
    		System.arraycopy(this.shapesId, 0, newIds, 0, this.numShapes) ;
    		System.arraycopy(shapesData, 0, newData, 0, this.numShapes) ;
    		this.shapesId = newIds ;
    		this.shapesData = newData ;
    	}
    	this.shapesId[numShapes] = id ;
    	this.shapesData[numShapes] = data ;
    	numShapes++ ;
    }
    
    protected void deleteData(int index) throws IndexOutOfBoundsException {
    	if ((index < 0) || (index > numShapes-1)) {
    		throw new IndexOutOfBoundsException("" + index) ;
    	}
    	if (index < numShapes-1) {
    		this.shapesId[index] = this.shapesId[numShapes-1] ;
    		this.shapesData[index] = this.shapesData[numShapes-1] ;
    	}
    	this.shapesData[numShapes-1] = null ;
    	numShapes-- ;
    	//if (numShapes == 0) {
    		// do we have to to something ?
    	//}
    	
    }

}
