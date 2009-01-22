/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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

import org.geotools.caching.spatialindex.Data;
import org.geotools.caching.spatialindex.Node;
import org.geotools.caching.spatialindex.Region;
import org.geotools.caching.spatialindex.Visitor;

/**
 * 
 * Visitor that invalidates cache nodes.
 * 
 * <p>Will also try to clear all items cached in node.</p>
 */
public class InvalidatingVisitor implements Visitor {
	
    private Region region;

    /**
     * Creates a new Invalidating Visitor.
     * 
     * @param r the region to invalid nodes within
     */
    public InvalidatingVisitor(Region r) {
        this.region = r;
    }
    
    /**
     * Creates a new Invalidating Visitor that will invalidate
     * all nodes visited (no matter where they are).
     */
    public InvalidatingVisitor() {
    }
    
    public boolean isDataVisitor() {
        return false;
    }

    public void visitData(Data d) {
        // do nothing
    }
    
    public void visitNode(Node n) {
		if (this.region == null || this.region.contains(n.getShape())) {
			n.getIdentifier().setValid(false);
			if (n instanceof GridCacheNode) {
				GridCacheNode node = (GridCacheNode) n;
				node.clear();
			}
		}
	}
}
