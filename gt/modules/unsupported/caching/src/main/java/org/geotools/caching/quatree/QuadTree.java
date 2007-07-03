/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
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


/** A QuadTree implementation, inspired by the shapefile quadtree in org.geotools.index.quadtree,
 * but using visitors and query strategies to customize how the tree is visited or run specialized queries.
 *
 * Other noticeable changes from original QuadTree :
 * <ul><li>tree delegates splitting to nodes
 * </ul>
 *
 * @see org.geotools.index.quadtree.QuadTree
 * @see http://research.att.com/~marioh/spatialindex
 *
 * @author Christophe Rousson, SoC 2007, CRG-ULAVAL
 *
 * TODO: implement maximum depth : allow to specify that the tree must not grow more than n levels
 * TODO: implement full interface
 * TODO: allow to extend the tree from top, by changing root node
 * TODO: make tree serializable or loadable from disk
 *
 */
public class QuadTree implements ISpatialIndex {
    /**
     * Control how much sub-quadrants do overlap.
     * if ratio = 0.5, quadrants will not overlap at all.
     * I guess that we want quadrants to overlap a bit, due to roundoff errors.
     * Defaults to orginial value picked in org.geotools.index.quadtree.QuadTree
     */
    private static final double SPLITRATIO = 0.55d;
    private static final Logger LOGGER = Logger.getLogger("org.geotools.caching.quadtree");

    /**
     * First node of the tree, pointing recursively to all other nodes.
     */
    private Node root;

    // Constructors

    /** Create a new QuadTree with first node with given bounds.
     *
     * @param bounds root node bounds.
     */
    public QuadTree(Region bounds) {
        this.root = new Node(new Region(bounds), 0, null);
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
        Node current = this.root;
        current.visited = false;

        Stack nodes = new Stack();

        if (current.getShape().contains(query)) {
            nodes.push(current);
        }

        while (!nodes.isEmpty()) {
            current = (Node) nodes.pop();

            if (!current.visited) {
                v.visitNode(current);

                for (int i = 0; i < current.getChildrenCount(); i++) {
                    current.getSubNode(i).visited = false;
                }

                for (int i = 0; i < current.numShapes; i++) {
                    v.visitData(new Data(current.shapesData[i], null, current.shapesId[i]));
                }

                current.visited = true;
            }

            for (int i = 0; i < current.getChildrenCount(); i++) {
                Node child = current.getSubNode(i);

                if (!child.visited) {
                    if (child.getShape().contains(query)) {
                        // we will go back to this one to examine other children
                        nodes.push(current);
                        nodes.push(child);

                        break;
                    } else {
                        child.visited = true;
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
        insertData(this.root, data, shape, id);
    }

    public void intersectionQuery(IShape query, IVisitor v) {
        Node current = this.root;
        current.visited = false;

        Stack nodes = new Stack();

        if (current.getShape().intersects(query)) {
            nodes.push(current);
        }

        while (!nodes.isEmpty()) {
            current = (Node) nodes.pop();

            if (!current.visited) {
                v.visitNode(current);

                for (int i = 0; i < current.getChildrenCount(); i++) {
                    current.getSubNode(i).visited = false;
                }

                for (int i = 0; i < current.numShapes; i++) {
                    v.visitData(new Data(current.shapesData[i], null, current.shapesId[i]));
                }

                current.visited = true;
            }

            for (int i = 0; i < current.getChildrenCount(); i++) {
                Node child = current.getSubNode(i);

                if (!child.visited) {
                    if (child.getShape().intersects(query)) {
                        // we will go back to this one later to examine other children
                        nodes.push(current);
                        // meanwhile, we put one child at a time into stack, so we do not waste space
                        nodes.push(child);

                        break;
                    } else {
                        // we won't have to compute intersection again and again
                        child.visited = true;
                    }
                }
            }
        }
    }

    public boolean isIndexValid() {
        // TODO Auto-generated method stub
        return false;
    }

    public void nearestNeighborQuery(int k, IShape query, IVisitor v, INearestNeighborComparator nnc) {
        // TODO Auto-generated method stub
    }

    public void nearestNeighborQuery(int k, IShape query, IVisitor v) {
        // TODO Auto-generated method stub
    }

    public void pointLocationQuery(IShape query, IVisitor v) {
        // TODO Auto-generated method stub
    }

    public void queryStrategy(IQueryStrategy qs) {
        int[] next = new int[] { this.root.id };

        Node current = this.root;

        while (true) {
            boolean[] hasNext = new boolean[] { false };
            qs.getNextEntry(current, next, hasNext);

            if (!hasNext[0]) {
                break;
            } else {
                if (next[0] < 0) {
                    current = current.parent;
                } else {
                    current = current.getSubNode(next[0]);
                }
            }
        }
    }

    /** This a variant of the original interface method, using nodes directly rather than references to nodes using ids,
     * as in this implementation nodes does not have a unique ID in the tree, they have a unique ID in their quadrant.
     *
     * @see org.geotools.caching.spatialindex.spatialindex.ISpatialIndex#queryStrategy(IQueryStrategy) ;
     *
     * @param qs
     */
    public void queryStrategy(QueryStrategy qs) {
        Node current = this.root;

        while (true) {
            boolean[] hasNext = new boolean[] { false };
            current = qs.getNextNode(current, hasNext);

            if (hasNext[0] == false) {
                break;
            }
        }
    }

    // Internals

    /** Insert new data into node.
     * Does not check data MBR fits into node MBR,
     * but this is what is expected. This why method is kept private.
     *
     * @param n target node
     * @param data data to insert
     * @param shape MBR of new data
     * @param id id of data
     */
    private void insertData(Node n, byte[] data, IShape shape, int id) {
        // TODO consider using maximum Depth
        if (n.isIndex()) {
            /* If there are subnodes, then consider whether this object
             * will fit in them.
             */
            for (int i = 0; i < n.getChildrenCount(); i++) {
                Node subNode = n.getSubNode(i);

                if (subNode.getShape().contains(shape)) {
                    insertData(subNode, data, shape, id);

                    return;
                }
            }
        } else {
            /* Otherwise, consider creating four subnodes if could fit into
             * them, and adding to the appropriate subnode.
             */
            n.split(SPLITRATIO);
            // recurse
            insertData(n, data, shape, id);
        }

        // If none of that worked, just add it to this nodes list.
        n.insertData(data, id);
    }

    /** Utility class to expose data records outside of the tree.
     *
     * @author Christophe Rousson, SoC 2007, CRG-ULAVAL
     *
     */
    class Data implements IData {
        private byte[] data;
        private int id;
        private IShape shape;

        public Data(byte[] data, Region mbr, int id) {
            this.data = data;
            this.shape = mbr;
            this.id = id;
        }

        public byte[] getData() {
            return data;
        }

        public int getIdentifier() {
            return id;
        }

        public IShape getShape() {
            return shape;
        }
    }
}
