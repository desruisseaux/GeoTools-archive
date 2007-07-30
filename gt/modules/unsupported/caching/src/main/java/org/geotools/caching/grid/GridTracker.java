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
package org.geotools.caching.grid;

import java.util.Stack;
import org.geotools.caching.spatialindex.NodeIdentifier;
import org.geotools.caching.spatialindex.Region;
import org.geotools.caching.spatialindex.Storage;
import org.geotools.caching.spatialindex.grid.Grid;


public class GridTracker extends Grid {
    public GridTracker(Region mbr, int capacity, Storage store) {
        this.dimension = mbr.getDimension();
        this.store = store;

        GridCacheRootNode root = new GridCacheRootNode(this, mbr, capacity);
        root.split();
        writeNode(root);
        this.stats.addToNodesCounter(root.getCapacity() + 1); // root has root.capacity nodes, +1 for root itself :)
        this.root = root.getIdentifier();
    }

    NodeIdentifier getRoot() {
        return this.root;
    }

    Stack searchMissingTiles(Region search) { // search must be within root mbr !

        Stack missing = new Stack();
        boolean foundValid = false;

        if (!this.root.isValid()) {
            int[] cursor = new int[this.dimension];
            int[] mins = new int[this.dimension];
            int[] maxs = new int[this.dimension];
            findMatchingTiles(search, cursor, mins, maxs);

            GridCacheRootNode root = (GridCacheRootNode) readNode(this.root);

            do {
                int nextid = root.gridIndexToNodeId(cursor);
                NodeIdentifier nextnode = root.getChildIdentifier(nextid);

                if (!nextnode.isValid()) {
                    missing.add(nextnode.getShape());
                } else if (!foundValid) {
                    foundValid = true;
                }
            } while (increment(cursor, mins, maxs));
        }

        if ((missing.size() > 1) && !foundValid) {
            Region r1 = (Region) missing.pop();
            Region r2 = (Region) missing.get(0);
            missing = new Stack();
            missing.add(r1.combinedRegion(r2));
        }

        return missing;
    }

    public void flush() {
        GridCacheRootNode oldroot = (GridCacheRootNode) readNode(this.root);
        int capacity = oldroot.getCapacity();
        Region mbr = new Region((Region) oldroot.getShape());
        GridCacheRootNode root = new GridCacheRootNode(this, mbr, capacity);
        deleteNode(this.root);
        writeNode(root);
        this.root = root.getIdentifier();
        this.stats.reset();
        root.split();
        this.stats.addToNodesCounter(root.getCapacity() + 1);
    }
}
