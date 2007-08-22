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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import org.geotools.caching.spatialindex.Node;
import org.geotools.caching.spatialindex.NodeIdentifier;


public class BufferedDiskStorage extends DiskStorage {
    LinkedHashMap<NodeIdentifier, BufferEntry> buffer;
    int buffer_size;

    public BufferedDiskStorage(File f, int page_size, int buffer_size)
        throws IOException {
        super(f, page_size);
        this.buffer_size = buffer_size;
        buffer = new LinkedHashMap<NodeIdentifier, BufferEntry>(buffer_size, .75f, true);
    }

    @Override
    public void clear() {
        buffer.clear();
        super.clear();
    }

    @Override
    public Node get(NodeIdentifier id) {
        BufferEntry entry = buffer.get(id);
        Node ret;

        if (entry == null) {
            ret = super.get(id);

            if (ret != null) {
                put(new BufferEntry(ret));
            }
        } else {
            ret = entry.node;
        }

        return ret;
    }

    void put(BufferEntry entry) {
        if (entry != null) {
            if (buffer.size() == buffer_size) {
                Iterator<NodeIdentifier> it = buffer.keySet().iterator();
                BufferEntry removed = buffer.remove(it.next());

                if (removed.dirty) {
                    super.put(removed.node);
                }

                buffer.put(entry.node.getIdentifier(), entry);
            } else {
                buffer.put(entry.node.getIdentifier(), entry);
            }
        }
    }

    @Override
    public void put(Node n) {
        if (!buffer.containsKey(n.getIdentifier())) {
            BufferEntry entry = new BufferEntry(n);
            entry.dirty = true;
            put(entry);
        } else {
            buffer.get(n.getIdentifier()).dirty = true;
        }
    }

    @Override
    public void remove(NodeIdentifier id) {
        if (buffer.containsKey(id)) {
            buffer.remove(id);
        } else {
            super.remove(id);
        }
    }

    class BufferEntry {
        Node node;
        boolean dirty = false;

        BufferEntry(Node node) {
            this.node = node;
        }
    }
}
