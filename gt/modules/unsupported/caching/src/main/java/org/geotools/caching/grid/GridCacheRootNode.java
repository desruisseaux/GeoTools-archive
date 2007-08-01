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

import org.geotools.caching.spatialindex.Region;
import org.geotools.caching.spatialindex.grid.GridData;
import org.geotools.caching.spatialindex.grid.GridNode;
import org.geotools.caching.spatialindex.grid.GridRootNode;


public class GridCacheRootNode extends GridRootNode {
    GridCacheRootNode(GridTracker grid, Region mbr, int capacity) {
        super(grid, mbr, capacity);
    }

    protected void split() {
        super.split();
    }

    int getCapacity() {
        return super.capacity;
    }

    protected GridNode createNode(Region reg) {
        return new GridCacheNode(this, reg);
    }

    @Override
    protected boolean insertData(int id, GridData data) {
        if (!getIdentifier().isValid()) { // FIXME: do not insert same data mutiple times

            return super.insertData(id, data);
        }

        return false;
    }
}
