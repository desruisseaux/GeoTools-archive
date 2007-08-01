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

import java.util.HashMap;
import org.geotools.caching.spatialindex.Node;
import org.geotools.caching.spatialindex.NodeIdentifier;
import org.geotools.caching.spatialindex.Region;
import org.geotools.caching.spatialindex.RegionNodeIdentifier;
import org.geotools.caching.spatialindex.Shape;
import org.geotools.caching.spatialindex.grid.GridData;


/** A node in the grid.
 * Data objects are store in an array.
 * Extra data about the node may be stored in node_data, which is a HashMap.
 *
 * @author Christophe Rousson, SoC 2007, CRG-ULAVAL
 *
 */
public class GridNode implements Node {
    protected RegionNodeIdentifier id = null;
    Region mbr;
    boolean visited = false;
    GridNode parent;
    HashMap node_data;
    int num_data;
    int[] data_ids;
    GridData[] data;

    protected GridNode(GridNode parent, Region mbr) {
        this.mbr = new Region(mbr);
        this.parent = parent;
        this.node_data = new HashMap();
        this.num_data = 0;
        this.data = new GridData[10];
        this.data_ids = new int[10];
    }

    public NodeIdentifier getChildIdentifier(int index)
        throws IndexOutOfBoundsException {
        throw new UnsupportedOperationException("GridNode have no children.");
    }

    public Shape getChildShape(int index) throws IndexOutOfBoundsException {
        throw new UnsupportedOperationException("GridNode have no children.");
    }

    public int getChildrenCount() {
        return 0;
    }

    public int getLevel() {
        return 0;
    }

    public boolean isIndex() {
        return false;
    }

    public boolean isLeaf() {
        return true;
    }

    public NodeIdentifier getIdentifier() {
        if (id == null) {
            id = new RegionNodeIdentifier(this);
        }

        return id;
    }

    public Shape getShape() {
        return mbr;
    }

    /** Insert new data in this node.
     *
     * @param id of data
     * @param data
     */
    protected boolean insertData(int id, GridData data) {
        if (num_data == data_ids.length) {
            int[] n_data_ids = new int[data_ids.length * 2];
            GridData[] n_data = new GridData[this.data.length * 2];
            System.arraycopy(data_ids, 0, n_data_ids, 0, num_data);
            System.arraycopy(this.data, 0, n_data, 0, num_data);
            data_ids = n_data_ids;
            this.data = n_data;
        }

        data_ids[num_data] = id;
        this.data[num_data] = data;
        num_data++;

        return true;
    }

    /** Delete blindly data at the given index.
     * Index is not the id of the data, the search should be performed by the Grid class,
     * which determines the ids of data to delete, and then call this method.
     *
     * @param index
     */
    protected void deleteData(int index) {
        if ((index < 0) || (index > (num_data - 1))) {
            throw new IndexOutOfBoundsException();
        }

        if (index < (num_data - 1)) {
            data_ids[index] = data_ids[num_data - 1];
            data[index] = data[num_data - 1];
        }

        data[num_data - 1] = null;
        num_data--;
    }

    /** Erase all data referenced by this node.
     *
     */
    public void clear() {
        this.num_data = 0;
        this.data = new GridData[10];
        this.data_ids = new int[10];
    }

    public int getDataCount() {
        return this.num_data;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("GridNode: MBR:" + mbr);

        return sb.toString();
    }
}
