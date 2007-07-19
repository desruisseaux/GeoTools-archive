package org.geotools.caching.spatialindex.grid;

import org.geotools.caching.firstdraft.spatialindex.storagemanager.PropertySet;
import org.geotools.caching.spatialindex.AbstractSpatialIndex;
import org.geotools.caching.spatialindex.Node;
import org.geotools.caching.spatialindex.Region;
import org.geotools.caching.spatialindex.Shape;
import org.geotools.caching.spatialindex.Statistics;
import org.geotools.caching.spatialindex.Visitor;


public class Grid extends AbstractSpatialIndex {
    public Grid(Region mbr, int capacity) {
        this.dimension = mbr.getDimension();

        GridRootNode root = new GridRootNode(mbr, capacity);
        root.split();
        this.root = root;
    }

    protected void visitData(Node n, Visitor v) {
        // TODO Auto-generated method stub
    }

    public void flush() throws IllegalStateException {
        // we drop all nodes and recreate grid ; GC will do the rest
        GridRootNode oldroot = (GridRootNode) this.root;
        int capacity = oldroot.capacity;
        Region mbr = new Region(oldroot.mbr);
        GridRootNode root = new GridRootNode(mbr, capacity);
        this.root = root;
    }

    public PropertySet getIndexProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    public Statistics getStatistics() {
        // TODO Auto-generated method stub
        return null;
    }

    protected boolean deleteData(Node n, Shape shape, int id) {
        GridNode node = (GridNode) n;
        boolean ret = false;

        for (int i = 0; i < node.num_data; i++) {
            if (node.data_ids[i] == id) {
                node.deleteData(i);
                ret = true;
            }
        }

        return ret;
    }

    public void insertData(Object data, Shape shape, int id) {
        if (shape.getDimension() != dimension) {
            throw new IllegalArgumentException(
                "insertData: Shape has the wrong number of dimensions.");
        }

        if (this.root.getShape().contains(shape)) {
            GridRootNode node = (GridRootNode) this.root;
            insertData(node, data, shape, id);

            return;
        } else {
            insertDataOutOfBounds(data, shape, id);
        }
    }

    protected void insertData(Node n, Object data, Shape shape, int id) {
        GridNode node = (GridNode) n;
        node.insertData(id, data);
    }

    void insertData(GridRootNode node, Object data, Shape shape, int id) {
        //		 TODO: handle case where shape is bigger than one tile ...
        if (false) { // too big - replace by ad hoc condition
            insertData((Node) node, data, shape, id);

            return;
        }

        int[] minindexes = new int[this.dimension];
        int[] maxindexes = new int[this.dimension];
        int[] cursor = new int[this.dimension];

        for (int i = 0; i < this.dimension; i++) {
            /* minindexes[i] = 0 ;
            while (shape.getMBR().getLow(i) > node.mbr.getLow(i)+minindexes[i]*node.tiles_size[i]) {
                    minindexes[i]++ ;
            }
            maxindexes[i] = minindexes[i] ;
            while (shape.getMBR().getHigh(i) > node.mbr.getLow(i)+maxindexes[i]*node.tiles_size[i]) {
                    maxindexes[i]++ ;
            } */
            minindexes[i] = (int) ((shape.getMBR().getLow(i) -
                node.mbr.getLow(i)) / node.tiles_size[i]);
            cursor[i] = minindexes[i];
            maxindexes[i] = (int) ((shape.getMBR().getHigh(i) -
                node.mbr.getLow(i)) / node.tiles_size[i]) + 1;
        }

        do {
            int nextid = node.gridIndexToNodeId(cursor);
            Node nextnode = node.getSubNode(nextid);

            if (nextnode.getShape().contains(shape)) {
                insertData(nextnode, data, shape, id);
            }
        } while (increment(cursor, minindexes, maxindexes));
    }

    boolean increment(int[] cursor, int[] mins, int[] maxs) {
        int dims = cursor.length;
        int mono = cursor[dims - 1];

        for (int i = 0; i < dims; i++) {
            cursor[i]++;

            if (cursor[i] == maxs[i]) {
                cursor[i] = mins[i];
            } else {
                break;
            }
        }

        if (cursor[dims - 1] < mono) {
            return false;
        } else {
            return true;
        }
    }

    protected void insertDataOutOfBounds(Object data, Shape shape, int id) {
        throw new IllegalArgumentException(
            "Grids cannot expand : Shape out of grid : " + shape);
    }

    public boolean isIndexValid() {
        // TODO Auto-generated method stub
        return false;
    }
}
