/*
 * Created on 19-ago-2004
 */
package org.geotools.index.quadtree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;


/**
 * @author Tommaso Nolli
 */
public class Node {

    private Envelope bounds;
    private int numShapesId;
    private int[] shapesId;
    
    protected List subNodes;
    
    public Node(Envelope bounds) {
        this.bounds = new Envelope(bounds);
        this.subNodes = new ArrayList(4);
        this.shapesId = new int[4];
        Arrays.fill(this.shapesId, -1);
    }
    
    /**
     * @return Returns the bounds.
     */
    public Envelope getBounds() {
        return this.bounds;
    }
    /**
     * @param bounds The bounds to set.
     */
    public void setBounds(Envelope bounds) {
        this.bounds = bounds;
    }
    
    /**
     * @return Returns the numSubNodes.
     */
    public int getNumSubNodes() {
        return this.subNodes.size();
    }

    /**
     * @return Returns the number of records stored.
     */
    public int getNumShapeIds() {
        return this.numShapesId;
    }

    /**
     * 
     * @param node
     */
    public void addSubNode(Node node) {
        if (node == null) {
            throw new NullPointerException("Cannot add null to subnodes");
        }
        this.subNodes.add(node);
    }
    
    /**
     * Removes a subnode
     * @param node The subnode to remove
     * @return true if the subnode has been removed
     */
    public boolean removeSubNode(Node node) {
        return this.subNodes.remove(node);
    }
    
    /**
     * 
     *
     */
    public void clearSubNodes() {
        this.subNodes.clear();
    }
    
    /**
     * Gets the Node at the requested position
     * @param pos The position
     * @return A Node
     */
    public Node getSubNode(int pos) throws StoreException {
        return (Node)this.subNodes.get(pos);
    }
    
    /**
     * Add a shape id
     * @param id
     */
    public void addShapeId(int id) {
        if (this.shapesId.length == this.numShapesId) {
            // Increase the array
            int[] newIds = new int[this.numShapesId * 2];
            Arrays.fill(newIds, -1);
            System.arraycopy(this.shapesId, 0, newIds, 0, this.numShapesId);
            this.shapesId = newIds;
        }
        
        this.shapesId[this.numShapesId] = id;
        this.numShapesId++;
    }
    
    /**
     * Gets a shape id
     * @param pos The position
     * @return The shape id (or recno) at the requested position
     */
    public int getShapeId(int pos) {
       if (pos >= this.numShapesId) {
           throw new ArrayIndexOutOfBoundsException("Requsted " + pos + 
                                                    " but size = " + 
                                                    this.numShapesId);
       }
       
       return this.shapesId[pos];
    }
    
    /**
     * Sets the shape ids
     * @param ids
     */
    public void setShapesId(int[] ids) {
        if (ids == null) {
            this.numShapesId = 0;
        } else {
            this.shapesId = ids;
            this.numShapesId = 0;
            for (int i = 0; i < ids.length; i++) {
                if (ids[i] == -1) {
                    break;
                }
                this.numShapesId++;
            }
        }
    }
    
    /**
     * @return Returns the shapesId.
     */
    public int[] getShapesId() {
        return this.shapesId;
    }
}
