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

import java.util.HashMap;
import java.util.Properties;
import org.geotools.caching.spatialindex.Node;
import org.geotools.caching.spatialindex.NodeIdentifier;
import org.geotools.caching.spatialindex.SpatialIndex;
import org.geotools.caching.spatialindex.Storage;


/** A simple in-memory storage relying on LinkedHashMap.
 * This is NOT synchronised.
 *
 * @author crousson
 *
 */
public class MemoryStorage implements Storage {
    HashMap<NodeIdentifier, NodeEntry> map;

    private MemoryStorage() {
        this.map = new HashMap<NodeIdentifier, NodeEntry>();
    }

    public static Storage createInstance(Properties pset) {
        return new MemoryStorage();
    }

    public static Storage createInstance() {
        return new MemoryStorage();
    }

    public Node get(NodeIdentifier id) {
        return map.get(id).node;
    }

    public void put(Node n) {
        if (!map.containsKey(n.getIdentifier())) {
            map.put(n.getIdentifier(), new NodeEntry(n.getIdentifier(), n));
        }
    }

    public void remove(NodeIdentifier id) {
        map.remove(id);
    }

    public void clear() {
        map.clear();
    }

    public void setParent(SpatialIndex index) {
        // do nothing - we do not need back link for this storage
    }

    public void flush() {
        // do nothing
    }

    public Properties getPropertySet() {
        Properties pset = new Properties();
        pset.setProperty(STORAGE_TYPE_PROPERTY, MemoryStorage.class.getCanonicalName());

        return pset;
    }

    public NodeIdentifier findUniqueInstance(NodeIdentifier id) {
        if (map.containsKey(id)) {
            return map.get(id).id;
        } else {
            return id;
        }
    }
}


class NodeEntry {
    NodeIdentifier id;
    Node node;

    NodeEntry(NodeIdentifier id, Node node) {
        this.id = id;
        this.node = node;
    }
}
