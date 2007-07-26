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
import org.geotools.caching.spatialindex.grid.GridNode;


public class GridCacheNode extends GridNode {
    boolean valid = false;

    GridCacheNode(int id, GridCacheRootNode parent, Region mbr) {
        super(id, parent, mbr);
    }

    protected void clear() {
        super.clear();
    }
}
