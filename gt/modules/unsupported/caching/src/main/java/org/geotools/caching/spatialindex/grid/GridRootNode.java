package org.geotools.caching.spatialindex.grid;

import org.geotools.caching.spatialindex.Node;
import org.geotools.caching.spatialindex.Region;
import org.geotools.caching.spatialindex.Shape;

import java.util.ArrayList;


public class GridRootNode extends GridNode {
    int capacity;
    int[] tiles_number;
    double[] tiles_size;
    ArrayList children;

    GridRootNode(Region mbr, int capacity) {
        super(0, null, mbr);
        this.capacity = capacity;
        init();
    }

    void init() {
        int dims = mbr.getDimension();
        tiles_number = new int[dims];
        tiles_size = new double[dims];

        double length = 0;

        for (int i = 0; i < dims; i++) {
            length += (mbr.getHigh(i) - mbr.getLow(i));
        }

        int newcapacity = 0;

        for (int i = 0; i < dims; i++) {
            int tmp = ((int) ((capacity * (mbr.getHigh(i) - mbr.getLow(i))) / length)) +
                1;
            tiles_number[i] = tmp;
            tiles_size[i] = (mbr.getHigh(i) - mbr.getLow(i)) / tmp;
            newcapacity += tmp;
        }
        assert (newcapacity >= capacity);
        capacity = newcapacity;
        children = new ArrayList(capacity);
    }

    void split() {
        int dims = tiles_number.length;
        double[] pos = new double[dims];
        double[] nextpos = new double[dims];
        int id = 0;

        for (int i = 0; i < dims; i++) {
            pos[i] = mbr.getLow(i);
            nextpos[i] = pos[i] + tiles_size[i];
        }

        do {
            Region reg = new Region(pos, nextpos);
            GridNode child = new GridNode(id, this, reg);
            this.children.add(child);
            id++;
        } while (increment(pos, nextpos));
    }

    boolean increment(double[] pos, double[] nextpos) {
        int dims = pos.length;

        if ((dims != tiles_number.length) ||
                (nextpos.length != tiles_number.length)) {
            throw new IllegalArgumentException(
                "Cursor has not the same dimension as grid.");
        }

        int i;
        double mono = pos[dims - 1];

        for (i = 0; i < dims; i++) {
            if (nextpos[i] > mbr.getHigh(i)) {
                pos[i] = mbr.getLow(i);
                nextpos[i] = pos[i] + tiles_size[i];
            } else {
                double tmp = pos[i];
                pos[i] = nextpos[i];
                nextpos[i] = tmp + tiles_size[i];

                break;
            }
        }

        if (pos[dims - 1] < mono) {
            return false;
        }

        return true;
    }

    boolean increment(int[] cursor) {
        int dims = cursor.length;

        if (dims != tiles_number.length) {
            throw new IllegalArgumentException(
                "Cursor has not the same dimension as grid.");
        }

        int mono = cursor[dims - 1];

        for (int i = 0; i < dims; i++) {
            cursor[i]++;

            if (cursor[i] == tiles_number[i]) {
                cursor[i] = 0;
            } else {
                break;
            }
        }

        if (cursor[dims - 1] < mono) {
            return false;
        }

        return true;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("GridNode: capacity:" + capacity + ", MBR:" + mbr);

        return sb.toString();
    }

    public int getChildIdentifier(int index) throws IndexOutOfBoundsException {
        return getSubNode(index).getIdentifier();
    }

    public int getChildrenCount() {
        return children.size();
    }

    public Shape getChildShape(int index) throws IndexOutOfBoundsException {
        return getSubNode(index).getShape();
    }

    public int getLevel() {
        return 1;
    }

    public Node getSubNode(int index) throws IndexOutOfBoundsException {
        return (Node) children.get(index);
    }

    public boolean isIndex() {
        if (children.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isLeaf() {
        return !isIndex();
    }

    public int gridIndexToNodeId(int[] index) {
        if (index.length != tiles_number.length) {
            throw new IllegalArgumentException("Argument has " + index.length +
                " dimensions whereas grid has " + tiles_number.length);
        } else {
            int result = 0;
            int offset = 1;

            for (int i = 0; i < index.length; i++) {
                result += (offset * index[i]);
                offset *= tiles_number[i];
            }

            return result;
        }
    }
}
