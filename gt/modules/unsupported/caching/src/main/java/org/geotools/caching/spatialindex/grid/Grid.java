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
package org.geotools.caching.spatialindex.grid;

import java.util.Iterator;
import org.geotools.caching.firstdraft.spatialindex.storagemanager.PropertySet;
import org.geotools.caching.spatialindex.AbstractSpatialIndex;
import org.geotools.caching.spatialindex.Node;
import org.geotools.caching.spatialindex.NodeIdentifier;
import org.geotools.caching.spatialindex.Region;
import org.geotools.caching.spatialindex.Shape;
import org.geotools.caching.spatialindex.Storage;
import org.geotools.caching.spatialindex.Visitor;
import org.geotools.caching.spatialindex.store.MemoryStorage;


/** A grid implementation of SpatialIndex.
 * A grid is a regular division of space, and is implemented as a very simple tree.
 * It has two levels, a top level consisting of one root node, and
 * a bottom level of nodes of the same size forming a grid.
 * Data is either inserted at the top level or at the bottom level,
 * and may be inserted more than once, if data intersects more than one node.
 * If data's shape is too big, it is inserted at the top level.
 * For the grid to be efficient, data should evenly distributed in size and in space,
 * and grid size should twice the mean size of data's shape.
 *
 * @author Christophe Rousson, SoC 2007, CRG-ULAVAL
 *
 */
public class Grid extends AbstractSpatialIndex {
    protected int root_insertions = 0;
    protected int MAX_INSERTION = 4;

    /** Constructor. Creates a new Grid covering space given by <code>mbr</code>
     * and with at least <code>capacity</code> nodes.
     *
     * @param mbr
     * @param capacity
     */
    public Grid(Region mbr, int capacity, Storage store) {
        this.store = store;
        this.dimension = mbr.getDimension();

        GridRootNode root = new GridRootNode(this, mbr, capacity);
        root.split();
        writeNode(root);
        this.stats.addToNodesCounter(root.capacity + 1); // root has root.capacity nodes, +1 for root itself :)
        this.root = root.getIdentifier();
    }

    protected Grid() {
    }

    protected void visitData(Node n, Visitor v, Shape query, int type) {
        GridNode node = (GridNode) n;

        for (int i = 0; i < node.num_data; i++) {
            GridData d = (GridData) node.data[i];

            if (((type == Grid.IntersectionQuery) && (query.intersects(d.getShape())))
                    || ((type == Grid.ContainmentQuery) && (query.contains(d.getShape())))) {
                v.visitData(d);
            }
        }
    }

    public void flush() throws IllegalStateException {
        // we drop all nodes and recreate grid ; GC will do the rest
        GridRootNode oldroot = (GridRootNode) readNode(this.root);
        int capacity = oldroot.capacity;
        Region mbr = new Region(oldroot.mbr);
        GridRootNode root = new GridRootNode(this, mbr, capacity);
        deleteNode(this.root);
        writeNode(root);
        this.root = root.getIdentifier();
        this.stats.reset();
        root.split();
        this.stats.addToNodesCounter(root.capacity + 1);
    }

    public PropertySet getIndexProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    protected boolean deleteDataRecursively(NodeIdentifier nodeid, Shape shape, int id) {
        int[] mins = new int[this.dimension];
        int[] maxs = new int[this.dimension];
        int[] cursor = new int[this.dimension];

        findMatchingTiles(shape, cursor, mins, maxs);

        boolean ret = false;
        GridRootNode node = (GridRootNode) readNode(nodeid);

        do {
            NodeIdentifier nextid = node.getChildIdentifier(node.gridIndexToNodeId(cursor));

            //GridNode nextnode = (GridNode) readNode(nextid) ;
            if (nextid.getShape().intersects(shape)) {
                ret = ret || deleteData(nextid, shape, id);
            }
        } while (increment(cursor, mins, maxs));

        return ret;
    }

    protected boolean deleteData(NodeIdentifier nodeid, Shape shape, int id) {
        GridNode node = (GridNode) readNode(nodeid);
        boolean ret = false;

        for (int i = 0; i < node.num_data; i++) {
            if (node.data_ids[i] == id) {
                node.deleteData(i);
                this.stats.addToDataCounter(-1);
                ret = true;
            }
        }

        if (node instanceof GridRootNode) {
            ret = ret || deleteDataRecursively(nodeid, shape, id); // if deleted before, we are done and do not visit children nodes.
        }

        return ret;
    }

    public void insertData(Object data, Shape shape, int id) {
        if (shape.getDimension() != dimension) {
            throw new IllegalArgumentException(
                "insertData: Shape has the wrong number of dimensions.");
        }

        if (this.root.getShape().contains(shape)) {
            insertData(this.root, data, shape, id);

            return;
        } else {
            insertDataOutOfBounds(data, shape, id);
        }
    }

    protected void _insertData(NodeIdentifier n, Object data, Shape shape, int id) {
        GridNode node = (GridNode) readNode(n);
        node.insertData(id, new GridData(id, shape, data));
        this.stats.addToDataCounter(1);
    }

    protected void insertData(NodeIdentifier n, Object data, Shape shape, int id) {
        /*
         * This version inserts data in tile if tile contains data's MBR (ie shape),
         * otherwise inserts data at root node.
         * This is not optimal, since most data of size about the size of a tile
         * are inserted at root node, because they are likely to fall between two tiles,
         * rather thant in one and only one tile.
         *
               int[] cursor = new int[this.dimension];
               for (int i = 0; i < this.dimension; i++) {
                   cursor[i] = (int) ((shape.getMBR().getLow(i) - node.mbr.getLow(i)) / node.tiles_size);
               }
               int nextid = node.gridIndexToNodeId(cursor);
               Node nextnode = node.getSubNode(nextid);
               if (nextnode.getShape().contains(shape)) {
                   insertData(nextnode, data, shape, id);
               } else {
                   insertData(this.root, data, shape, id);
                   root_insertions++;
               }
         */

        /* so we prefer this version :
         * data may be inserted more than one time, in each tile intersecting data's MBR.
         * However, very big MBR will cause data to be inserted in a large number of tiles :
         * given a threshold, data is inserted at root node.
         * */
        int[] mins = new int[this.dimension];
        int[] maxs = new int[this.dimension];
        int[] cursor = new int[this.dimension];

        findMatchingTiles(shape, cursor, mins, maxs);

        int tiles = 1;

        for (int i = 0; i < this.dimension; i++) {
            tiles *= (maxs[i] - mins[i] + 1);
        }

        if (tiles > MAX_INSERTION) {
            _insertData(this.root, data, shape, id);
            root_insertions++;
        } else {
            GridRootNode node = (GridRootNode) readNode(n);

            do {
                int nextid = node.gridIndexToNodeId(cursor);
                NodeIdentifier nextnode = node.getChildIdentifier(nextid);
                _insertData(nextnode, data, shape, id);
            } while (increment(cursor, mins, maxs));
        }
    }

    /** Computes min and max indexes of grid nodes in order to cover all tiles intersecting shape,
     * and initializes cursor with mins values.
     *
     * @param shape
     * @param cursor
     * @param mins
     * @param maxs
     */
    protected void findMatchingTiles(Shape shape, int[] cursor, final int[] mins, final int[] maxs) {
        GridRootNode node = (GridRootNode) readNode(this.root);

        for (int i = 0; i < this.dimension; i++) {
            mins[i] = (int) ((shape.getMBR().getLow(i) - node.mbr.getLow(i)) / node.tiles_size);
            cursor[i] = mins[i];
            maxs[i] = (int) ((shape.getMBR().getHigh(i) - node.mbr.getLow(i)) / node.tiles_size);
        }
    }

    /** Moves cursor between mins and maxs, from mins to maxs.
     *
     * @param cursor
     * @param mins
     * @param maxs
     * @return <code>false</code> if cursor has reached maxs, <code>true</code> otherwise.
     */
    protected static boolean increment(int[] cursor, int[] mins, int[] maxs) {
        int dims = cursor.length;
        boolean cont = true;

        for (int i = 0; i < dims; i++) {
            cursor[i]++;

            if (cursor[i] > maxs[i]) {
                cursor[i] = mins[i];

                if (i == (dims - 1)) {
                    cont = false;
                }
            } else {
                break;
            }
        }

        return cont;
    }

    protected void insertDataOutOfBounds(Object data, Shape shape, int id) {
        throw new IllegalArgumentException("Grids cannot expand : Shape out of grid : " + shape);
    }

    public boolean isIndexValid() {
        // TODO Auto-generated method stub
        return true;
    }

    protected Node readNode(NodeIdentifier id) {
        return super.readNode(id);
    }

    protected void writeNode(Node node) {
        super.writeNode(node);
    }
}
