/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package org.geotools.index.rtree.memory;

import org.geotools.index.TreeException;
import org.geotools.index.rtree.Entry;
import org.geotools.index.rtree.Node;


/**
 * DOCUMENT ME!
 *
 * @author Tommaso Nolli
 * @source $URL$
 */
public class MemoryNode extends Node {
    private Node parent;

    /**
     * DOCUMENT ME!
     *
     * @param maxNodeEntries
     */
    public MemoryNode(int maxNodeEntries) {
        super(maxNodeEntries);
    }

    /**
     * @see org.geotools.index.rtree.Node#getParent()
     */
    public Node getParent() throws TreeException {
        return this.parent;
    }

    /**
     * @see org.geotools.index.rtree.Node#setParent(org.geotools.index.rtree.Node)
     */
    public void setParent(Node node) {
        this.parent = node;
    }

    /**
     * @see org.geotools.index.rtree.Node#getEntry(org.geotools.index.rtree.Node)
     */
    protected Entry getEntry(Node node) {
        Entry ret = null;
        Node n = null;

        for (int i = 0; i < this.entries.length; i++) {
            n = (Node) this.entries[i].getData();

            if (n == node) {
                ret = this.entries[i];

                break;
            }
        }

        return ret;
    }

    /**
     * @see org.geotools.index.rtree.Node#doSave()
     */
    protected void doSave() throws TreeException {
        // does nothing....
    }
}
