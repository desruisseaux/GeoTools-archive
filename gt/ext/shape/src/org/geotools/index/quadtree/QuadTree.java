/*
 * Created on 19-ago-2004
 */
package org.geotools.index.quadtree;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Envelope;


/**
 * Java porting of mapserver quadtree implementation.<br><br>
 * Note that this implementation is <b>not thread safe</b>, so don't share the 
 * same instance across two or more threads.
 * 
 * TODO: example of typical use...
 * 
 * @author Tommaso Nolli
 * @source $URL$
 */
public class QuadTree {

    private static final double SPLITRATIO = 0.55d;
    
    private static final Logger LOGGER =
        Logger.getLogger("org.geotools.index.quadtree");
    
    private Node root;
    private int numShapes;
    private int maxDepth;
    
    /**
     * Constructor.
     * The maxDepth will be calculated.
     * @param numShapes The total number of shapes to index
     * @param maxBounds The bounds of all geometries to be indexed
     */
    public QuadTree(int numShapes, Envelope maxBounds) {
        this.numShapes = numShapes;
        this.maxDepth = 0;
        this.root = new Node(new Envelope(maxBounds));
        
        /* No max depth was defined, try to select a reasonable one
         * that implies approximately 8 shapes per node.
         */
        int numNodes = 1;
          
        while(numNodes * 4 < numShapes) {
            this.maxDepth += 1;
            numNodes = numNodes * 2;
        }
    }
    
    /**
     * Constructor.
     * @param numShapes The total number of shapes to index
     * @param maxDepth The max depth of the index, must be <= 65535
     * @param maxBounds The bounds of all geometries to be indexed
     */
    public QuadTree(int numShapes, int maxDepth, Envelope maxBounds) {
        this.numShapes = numShapes;
        this.maxDepth = maxDepth;
        this.root = new Node(new Envelope(maxBounds));
    }
    
    /**
     * Constructor.
     * WARNING: using this constructor, you have to manually set the root
     * @param numShapes The total number of shapes to index
     * @param maxDepth The max depth of the index, must be <= 65535
     */
    public QuadTree(int numShapes, int maxDepth) {
        if (maxDepth > 65535) {
            throw new IllegalArgumentException("maxDepth must be <= 65535");
        }
        
        this.numShapes = numShapes;
        this.maxDepth = maxDepth;
    }
    
    /**
     * Inserts a shape record id in the quadtree
     * @param recno The record number
     * @param bounds The bounding box
     */
    public void insert(int recno, Envelope bounds) throws StoreException {
        this.insert(this.root, recno, bounds, this.maxDepth);
    }
    
    /**
     * Inserts a shape record id in the quadtree
     * @param node
     * @param recno
     * @param bounds
     * @param md
     * @throws StoreException
     */
    private void insert(Node node, int recno, Envelope bounds, int md) 
    throws StoreException
    {

        if (md > 1 && node.getNumSubNodes() > 0) {
            /* If there are subnodes, then consider whether this object
             * will fit in them.
             */
            Node subNode = null;
            for(int i = 0; i < node.getNumSubNodes(); i++ ) {
                subNode = node.getSubNode(i);
                if (subNode.getBounds().contains(bounds)) {
                    this.insert(subNode, recno, bounds, md - 1);
                    return;
                }
            }
        } else if (md > 1 && node.getNumSubNodes() == 0) {
            /* Otherwise, consider creating four subnodes if could fit into
             * them, and adding to the appropriate subnode.
             */
            Envelope half1, half2, quad1, quad2, quad3, quad4;
            
            Envelope[] tmp = this.splitBounds(node.getBounds());
            half1 = tmp[0];
            half2 = tmp[1];
            
            tmp = this.splitBounds(half1);
            quad1 = tmp[0];
            quad2 = tmp[1];
            
            tmp = this.splitBounds(half2);
            quad3 = tmp[0];
            quad4 = tmp[1];
            
            if (quad1.contains(bounds) || quad2.contains(bounds) || 
                quad3.contains(bounds) || quad4.contains(bounds))
            {
                node.addSubNode(new Node(quad1));
                node.addSubNode(new Node(quad2));
                node.addSubNode(new Node(quad3));
                node.addSubNode(new Node(quad4));
                
                // recurse back on this node now that it has subnodes
                this.insert(node, recno, bounds, md);
                return;
            }
        }

        // If none of that worked, just add it to this nodes list.
        node.addShapeId(recno);
    }

    /**
     * 
     * @param bounds
     * @return A List of Integer
     */
    public List search(Envelope bounds) throws StoreException {
        long start = System.currentTimeMillis();

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST,
                       "Querying " + bounds);            
        }

        List ret = new ArrayList();
        this.collectShapeIds(this.root, bounds, ret);
        
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST,
                       ret.size() + " ids retrieved in " +
                       (System.currentTimeMillis() - start) + "ms.");            
        }

        return ret;
    }

    /**
     * 
     * @param node
     * @param bounds
     * @param ids
     */
    private void collectShapeIds(Node node, Envelope bounds, List ids) 
    throws StoreException 
    {
        if (!node.getBounds().intersects(bounds)) {
            return;
        }
        
        // Add the local nodes shapeids to the list.
        for(int i = 0; i < node.getNumShapeIds(); i++) {
            ids.add(new Integer(node.getShapeId(i)));
        }
        
        Node subNode = null;
        for(int i = 0; i < node.getNumSubNodes(); i++) {
            subNode = node.getSubNode(i);
            collectShapeIds(subNode, bounds, ids);
        }
    }
    
    /**
     * Closes this QuadTree after use...
     * @throws StoreException
     */
    public void close() throws StoreException {
        /* This does nothing, the IndexStore need to
         * override this if something needs to be closed...
         * 
         */
    }
    
    /**
     * 
     * @return
     */
    public boolean trim() throws StoreException {
        LOGGER.fine("Trimming the tree...");
        return this.trim(this.root);
    }
    
    /**
     * Trim subtrees, and free subnodes that come back empty.
     * @param node The node to trim
     * @return true if this node has been trimmed
     */
    private boolean trim(Node node) throws StoreException {
        Node[] dummy = new Node[node.getNumSubNodes()];
        for (int i = 0; i < node.getNumSubNodes(); i++) {
            dummy[i] = node.getSubNode(i);
        }

        for(int i = 0; i < dummy.length; i++ ) {
            if(this.trim(dummy[i])) {
                node.removeSubNode(dummy[i]);
            }
        }
        
        /* If I have only 1 subnode and no shape records, promote that
         * subnode to my position.
         */
        if (node.getNumSubNodes() == 1 && node.getNumShapeIds() == 0) {
            Node subNode = node.getSubNode(0);
            
            node.clearSubNodes();
            for (int i = 0; i < subNode.getNumSubNodes(); i++) {
                node.addSubNode(subNode.getSubNode(i));
            }
            
            node.setShapesId(subNode.getShapesId());
            node.setBounds(subNode.getBounds());
        }
        
        return (node.getNumSubNodes() == 0 && node.getNumShapeIds() == 0);
    }
    
    /**
     * Splits the specified Envelope
     * @param in an Envelope to split
     * @return an array of 2 Envelopes
     */
    private Envelope[] splitBounds(Envelope in) {
        Envelope[] ret = new Envelope[2];
        double range, calc;
        
        if ((in.getMaxX() - in.getMinX()) > (in.getMaxY() - in.getMinY())) {
            // Split in X direction
            range = in.getMaxX() - in.getMinX();
            
            calc = in.getMinX() + range * SPLITRATIO;
            ret[0] = new Envelope(in.getMinX(), calc,
                                  in.getMinY(), in.getMaxY());

            calc = in.getMaxX() - range * SPLITRATIO;
            ret[1] = new Envelope(calc, in.getMaxX(),
                                  in.getMinY(), in.getMaxY());
        } else {
            // Split in Y direction
            range = in.getMaxY() - in.getMinY();
            
            calc = in.getMinY() + range * SPLITRATIO;
            ret[0] = new Envelope(in.getMinX(), in.getMaxX(),
                                  in.getMinY(), calc);

            calc = in.getMaxY() - range * SPLITRATIO;
            ret[1] = new Envelope(in.getMinX(), in.getMaxX(),
                                  calc, in.getMaxY());
        }

        return ret;
    }
    
    /**
     * @return Returns the maxDepth.
     */
    public int getMaxDepth() {
        return this.maxDepth;
    }

    /**
     * @param maxDepth The maxDepth to set.
     */
    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    /**
     * @return Returns the numShapes.
     */
    public int getNumShapes() {
        return this.numShapes;
    }

    /**
     * @param numShapes The numShapes to set.
     */
    public void setNumShapes(int numShapes) {
        this.numShapes = numShapes;
    }
    
    /**
     * @return Returns the root.
     */
    public Node getRoot() {
        return this.root;
    }
    
    /**
     * @param root The root to set.
     */
    public void setRoot(Node root) {
        this.root = root;
    }
}
