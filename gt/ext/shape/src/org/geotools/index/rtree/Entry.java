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
package org.geotools.index.rtree;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.index.Data;


/**
 * DOCUMENT ME!
 *
 * @author Tommaso Nolli
 * @source $URL$
 */
public class Entry implements Cloneable {
    private Envelope bounds;
    private Object data;
    private EntryBoundsChangeListener listener;

    public Entry(Envelope e, Object data) {
        this.bounds = e;
        this.data = data;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public Envelope getBounds() {
        return bounds;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public Object getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        Entry e = (Entry) obj;

        return this.bounds.equals(e.getBounds())
        && this.data.equals(e.getData());
    }

    /**
     * DOCUMENT ME!
     *
     * @param envelope
     */
    void setBounds(Envelope envelope) {
        bounds = envelope;

        if (this.listener != null) {
            this.listener.boundsChanged(this);
        }
    }

    /**
     * @see java.lang.Object#clone()
     */
    protected Object clone() {
        Entry ret = new Entry(new Envelope(this.bounds), this.data);
        ret.setListener(this.listener);

        return ret;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "Entry --> " + this.bounds + " - key: " + this.data;
    }

    /**
     * DOCUMENT ME!
     *
     * @param listener
     */
    public void setListener(EntryBoundsChangeListener listener) {
        this.listener = listener;
    }
}
