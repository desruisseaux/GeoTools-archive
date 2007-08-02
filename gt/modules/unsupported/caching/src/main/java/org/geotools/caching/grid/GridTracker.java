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
import org.geotools.caching.EvictableTree;
import org.geotools.caching.EvictionPolicy;
import org.geotools.caching.LRUEvictionPolicy;
import org.geotools.caching.spatialindex.Node;
import org.geotools.caching.spatialindex.NodeIdentifier;
import org.geotools.caching.spatialindex.Region;
import org.geotools.caching.spatialindex.Shape;
import org.geotools.caching.spatialindex.Storage;
import org.geotools.caching.spatialindex.grid.Grid;
import org.geotools.caching.spatialindex.grid.GridNode;
import org.geotools.caching.spatialindex.store.MemoryStorage;


public class GridTracker extends Grid implements EvictableTree {
    GridTrackerStatistics stats;
    EvictionPolicy policy;

    public GridTracker(Region mbr, int capacity, Storage store) {
        this.dimension = mbr.getDimension();
        this.store = store;
        this.policy = new LRUEvictionPolicy(this);

        GridCacheRootNode root = new GridCacheRootNode(this, mbr, capacity);
        root.split();
        writeNode(root);
        this.stats = new GridTrackerStatistics();
        super.stats = this.stats;
        this.stats.addToNodesCounter(root.getCapacity() + 1); // root has root.capacity nodes, +1 for root itself :)
        this.root = root.getIdentifier();
    }

    NodeIdentifier getRoot() {
        return this.root;
    }

    Stack searchMissingTiles(Region search) { // search must be within root mbr !

        Stack<Shape> missing = new Stack<Shape>();
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

        if (!foundValid && (missing.size() > 1)) {
            Region r1 = (Region) missing.pop();
            Region r2 = (Region) missing.get(0);
            missing = new Stack<Shape>();
            missing.add(r1.combinedRegion(r2));
        }

        return missing;
    }

    public void flush() {
        GridCacheRootNode oldroot = (GridCacheRootNode) readNode(this.root);
        int capacity = oldroot.getCapacity();
        Region mbr = new Region((Region) oldroot.getShape());
        GridCacheRootNode root = new GridCacheRootNode(this, mbr, capacity);
        this.store.clear();
        writeNode(root);
        this.root = root.getIdentifier();
        this.stats.reset();
        root.split();
        this.stats.addToNodesCounter(root.getCapacity() + 1);
    }

    public int getEvictions() {
        return stats.getEvictions();
    }

    public void evict(NodeIdentifier node) {
        GridNode nodeToEvict = (GridNode) readNode(node); // FIXME: avoid to read node before eviction
        int ret = nodeToEvict.getDataCount();
        nodeToEvict.clear();
        nodeToEvict.getIdentifier().setValid(false);
        this.stats.addToDataCounter(-ret);
        this.stats.addToEvictionCounter(1);
    }

    @Override
    protected Node readNode(NodeIdentifier id) {
        policy.access(id);

        return super.readNode(id);
    }

    @Override
    protected void writeNode(Node node) {
        super.writeNode(node);
        policy.access(node.getIdentifier());
    }

    class GridTrackerStatistics extends ThisStatistics {
        int stats_evictions = 0;

        public void addToEvictionCounter(int count) {
            stats_evictions += count;
        }

        public int getEvictions() {
            return stats_evictions;
        }

        @Override
        public void reset() {
            stats_evictions = 0;
            super.reset();
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer(super.toString());
            sb.append(" ; Evictions = " + stats_evictions);

            return sb.toString();
        }
    }
}
