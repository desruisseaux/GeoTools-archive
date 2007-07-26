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

import java.util.ArrayList;
import org.geotools.caching.spatialindex.AbstractSpatialIndex;
import org.geotools.caching.spatialindex.Node;
import org.geotools.caching.spatialindex.Region;
import org.geotools.caching.spatialindex.Shape;


/** The root node of a grid, which has n GridNodes as children.
 * As GridNodes do, it can store data too.
 *
 * @author Christophe Rousson, SoC 2007, CRG-ULAVAL
 *
 */
public class GridRootNode extends GridNode {
    protected int capacity;
    int[] tiles_number;
    double tiles_size;
    ArrayList children;

    protected GridRootNode(Region mbr, int capacity) {
        super(0, null, mbr);
        this.capacity = capacity;
        init();
    }

    void init() {
        int dims = mbr.getDimension();
        tiles_number = new int[dims];

        double area = 1;

        for (int i = 0; i < dims; i++) {
            area *= (mbr.getHigh(i) - mbr.getLow(i));
        }

        tiles_size = Math.pow(area / capacity, 1d / dims);

        int newcapacity = 1;

        for (int i = 0; i < dims; i++) {
            int tmp;
            double dtmp = (mbr.getHigh(i) - mbr.getLow(i)) / tiles_size;
            tmp = (int) dtmp;

            if (tmp < dtmp) {
                tmp += 1;
            }

            tiles_number[i] = tmp;
            newcapacity *= tmp;
        }
        assert (newcapacity >= capacity);
        capacity = newcapacity;
        children = new ArrayList(capacity);
    }

    /** Creates the grid by appending children to this node.
     *
     */
    protected void split() {
        int dims = tiles_number.length;
        double[] pos = new double[dims];
        double[] nextpos = new double[dims];
        int id = 0;

        for (int i = 0; i < dims; i++) {
            pos[i] = mbr.getLow(i);
            nextpos[i] = pos[i] + tiles_size;
        }

        do {
            Region reg = new Region(pos, nextpos);
            GridNode child = createNode(id, reg);
            this.children.add(child);
            id++;
        } while (increment(pos, nextpos));
    }

    protected GridNode createNode(int id, Region reg) {
        return new GridNode(id, this, reg);
    }

    /** Computes sequentially the corner position of each tile in the grid.
     *
     * @param pos
     * @param nextpos
     * @return false if the upperright corner of the grid has been reached, true otherwise
     */
    boolean increment(double[] pos, double[] nextpos) {
        int dims = pos.length;

        if ((dims != tiles_number.length) || (nextpos.length != tiles_number.length)) {
            throw new IllegalArgumentException("Cursor has not the same dimension as grid.");
        }

        for (int i = 0; i < dims; i++) {
            if (((nextpos[i] - mbr.getHigh(i)) > 0)
                    || (Math.abs(nextpos[i] - mbr.getHigh(i)) < AbstractSpatialIndex.EPSILON)) {
                pos[i] = mbr.getLow(i);
                nextpos[i] = pos[i] + tiles_size;

                if (i == (dims - 1)) {
                    return false;
                }
            } else {
                pos[i] = nextpos[i];
                nextpos[i] = pos[i] + tiles_size;

                break;
            }
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

    /** Converts an array of indexes into the id of a node.
     *
     * @param index
     * @return
     */
    public int gridIndexToNodeId(int[] index) {
        if (index.length != tiles_number.length) {
            throw new IllegalArgumentException("Argument has " + index.length
                + " dimensions whereas grid has " + tiles_number.length);
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
