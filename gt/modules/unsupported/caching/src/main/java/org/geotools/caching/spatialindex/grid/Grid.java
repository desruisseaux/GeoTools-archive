package org.geotools.caching.spatialindex.grid;

import org.geotools.caching.firstdraft.spatialindex.storagemanager.PropertySet;
import org.geotools.caching.spatialindex.AbstractSpatialIndex;
import org.geotools.caching.spatialindex.Node;
import org.geotools.caching.spatialindex.Region;
import org.geotools.caching.spatialindex.Shape;
import org.geotools.caching.spatialindex.Visitor;


public class Grid extends AbstractSpatialIndex {
    int root_insertions = 0;

    /**
     * @param mbr
     * @param capacity
     */
    public Grid(Region mbr, int capacity) {
        this.dimension = mbr.getDimension();

        GridRootNode root = new GridRootNode(mbr, capacity);
        root.split();
        this.stats.addToNodesCounter(root.capacity + 1); // root has root.capacity nodes, +1 for root itself :)
        this.root = root;
    }

    protected void visitData(Node n, Visitor v, Shape query, int type) {
        GridNode node = (GridNode) n;

        for (int i = 0; i < node.num_data; i++) {
            GridData d = (GridData) node.data[i];

            if (((type == Grid.IntersectionQuery) &&
                    (query.intersects(d.getShape()))) ||
                    ((type == Grid.ContainmentQuery) &&
                    (query.contains(d.getShape())))) {
                v.visitData(d);
            }
        }
    }

    public void flush() throws IllegalStateException {
        // we drop all nodes and recreate grid ; GC will do the rest
        GridRootNode oldroot = (GridRootNode) this.root;
        int capacity = oldroot.capacity;
        Region mbr = new Region(oldroot.mbr);
        GridRootNode root = new GridRootNode(mbr, capacity);
        this.root = root;
        this.stats.reset();
        root.split();
        this.stats.addToNodesCounter(root.capacity + 1);
    }

    public PropertySet getIndexProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    protected boolean deleteData(GridRootNode node, Shape shape, int id) {
        int[] mins = new int[this.dimension];
        int[] maxs = new int[this.dimension];
        int[] cursor = new int[this.dimension];

        findMatchingTiles(shape, cursor, mins, maxs);

        boolean ret = false;

        do {
            int nextid = node.gridIndexToNodeId(cursor);
            Node nextnode = node.getSubNode(nextid);

            if (nextnode.getShape().contains(shape)) {
                ret = ret || deleteData(nextnode, shape, id);
            }
        } while (increment(cursor, mins, maxs));

        return ret;
    }

    protected boolean deleteData(Node n, Shape shape, int id) {
        GridNode node = (GridNode) n;
        boolean ret = false;

        for (int i = 0; i < node.num_data; i++) {
            if (node.data_ids[i] == id) {
                node.deleteData(i);
                this.stats.addToDataCounter(-1);
                ret = true;
            }
        }

        if (n instanceof GridRootNode) {
            ret = ret || deleteData((GridRootNode) n, shape, id); // if deleted before, we are done and do not visit children nodes.
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
        node.insertData(id, new GridData(id, shape, data));
        this.stats.addToDataCounter(1);
    }

    void insertData(GridRootNode node, Object data, Shape shape, int id) {
        /*int[] minindexes = new int[this.dimension];
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

        /*
        minindexes[i] = (int) ((shape.getMBR().getLow(i) -
        node.mbr.getLow(i)) / node.tiles_size) ;
        cursor[i] = minindexes[i] ;
        maxindexes[i] = (int) ((shape.getMBR().getHigh(i) -
        node.mbr.getLow(i)) / node.tiles_size) ;
        }

        boolean inserted = false ;

        do {
        int nextid = node.gridIndexToNodeId(cursor);
        Node nextnode = node.getSubNode(nextid);

        if (nextnode.getShape().contains(shape)) {
        insertData(nextnode, data, shape, id);
        inserted = true ;
        }
        } while (increment(cursor, minindexes, maxindexes));*/
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
    }

    void findMatchingTiles(Shape shape, int[] cursor, int[] mins, int[] maxs) {
        GridRootNode node = (GridRootNode) this.root;

        for (int i = 0; i < this.dimension; i++) {
            mins[i] = (int) ((shape.getMBR().getLow(i) - node.mbr.getLow(i)) / node.tiles_size);
            cursor[i] = mins[i];
            maxs[i] = (int) ((shape.getMBR().getHigh(i) - node.mbr.getLow(i)) / node.tiles_size);
        }
    }

    boolean increment(int[] cursor, int[] mins, int[] maxs) {
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
        throw new IllegalArgumentException(
            "Grids cannot expand : Shape out of grid : " + shape);
    }

    public boolean isIndexValid() {
        // TODO Auto-generated method stub
        return false;
    }
}
