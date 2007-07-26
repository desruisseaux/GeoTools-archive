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
import org.geotools.caching.spatialindex.Region;
import org.geotools.caching.spatialindex.grid.Grid;
import org.geotools.caching.spatialindex.grid.GridRootNode;


public class GridTracker extends Grid {
    public GridTracker(Region mbr, int capacity) {
        this.dimension = mbr.getDimension();

        GridCacheRootNode root = new GridCacheRootNode(mbr, capacity);
        root.split();
        this.stats.addToNodesCounter(root.getCapacity() + 1); // root has root.capacity nodes, +1 for root itself :)
        this.root = root;
    }

    GridCacheRootNode getRoot() {
        return (GridCacheRootNode) this.root;
    }

    Stack searchMissingTiles(Region search) { // search must be within root mbr !

        Stack missing = new Stack();
        boolean foundValid = false;

        if (!getRoot().valid) {
            int[] cursor = new int[this.dimension];
            int[] mins = new int[this.dimension];
            int[] maxs = new int[this.dimension];
            findMatchingTiles(search, cursor, mins, maxs);

            do {
                int nextid = getRoot().gridIndexToNodeId(cursor);
                GridCacheNode nextnode = (GridCacheNode) getRoot().getSubNode(nextid);

                if (!nextnode.valid) {
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
        GridCacheRootNode oldroot = (GridCacheRootNode) this.root;
        int capacity = oldroot.getCapacity();
        Region mbr = new Region((Region) oldroot.getShape());
        GridCacheRootNode root = new GridCacheRootNode(mbr, capacity);
        this.root = root;
        this.stats.reset();
        root.split();
        this.stats.addToNodesCounter(root.getCapacity() + 1);
    }
}
