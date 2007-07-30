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
package org.geotools.caching.spatialindex.store;

import java.util.LinkedHashMap;
import org.geotools.caching.spatialindex.Node;
import org.geotools.caching.spatialindex.NodeIdentifier;
import org.geotools.caching.spatialindex.Storage;


/** A simple in-memory storage relying on LinkedHashMap.
 * This is NOT synchronised.
 *
 * @author crousson
 *
 */
public class MemoryStorage implements Storage {
    public LinkedHashMap map = new LinkedHashMap(); // TODO change visibility

    public Node get(NodeIdentifier id) {
        return (Node) map.get(id);
    }

    public void put(Node n) {
        map.put(n.getIdentifier(), n);
    }

    public void remove(NodeIdentifier id) {
        map.remove(id);
    }
}
