/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le D�veloppement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
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
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.metadata.extent;

// J2SE direct dependencies
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.geotools.resources.Utilities;
import org.geotools.util.CheckedHashSet;
import org.opengis.metadata.extent.GeographicExtent;


/**
 * Boundary enclosing the dataset, expressed as the closed set of
 * (<var>x</var>,<var>y</var>) coordinates of the polygon. The last
 * point replicates first point.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Toura�vane
 */
public class SpatialTemporalExtent extends TemporalExtent
       implements org.opengis.metadata.extent.SpatialTemporalExtent
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 821702768255546660L;

    /**
     * The spatial extent component of composite
     * spatial and temporal extent.
     */
    private Set spatialExtent;
    
    /**
     * Construct an initially empty spatial-temporal extent.
     */
    public SpatialTemporalExtent() {
    }

    /**
     * Creates a spatial-temporal extent initialized to the specified values.
     */
    public SpatialTemporalExtent(final Date startTime,
                                 final Date endTime,
                                 final Set  spatialExtent)
    {
        super(startTime, endTime);
        setSpatialExtent(spatialExtent);
    }
    
    /**
     * Returns the spatial extent component of composite
     * spatial and temporal extent.
     *
     * @return The list of geographic extents (never <code>null</code>).
     */
    public Set getSpatialExtent() {
        final Set spatialExtent = this.spatialExtent; // Avoid synchronization
        return (spatialExtent!=null) ? spatialExtent : Collections.EMPTY_SET;
    }

    /**
     * Set the spatial extent component of composite
     * spatial and temporal extent.
     */
    public synchronized void setSpatialExtent(final Set newValues) {
        checkWritePermission();
        if (spatialExtent == null) {
            spatialExtent = new CheckedHashSet(GeographicExtent.class);
        } else {
            spatialExtent.clear();
        }
        spatialExtent.addAll(newValues);
    }

    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        spatialExtent = (Set) unmodifiable(spatialExtent);
    }

    /**
     * Compare this spatial-temporal extent with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final SpatialTemporalExtent that = (SpatialTemporalExtent) object;
            return Utilities.equals(this.spatialExtent, that.spatialExtent);
        }
        return false;
    }

    /**
     * Returns a hash code value for this extent.
     */
    public synchronized int hashCode() {
        int code = super.hashCode() ^ (int)serialVersionUID;
        if (spatialExtent != null) code ^= spatialExtent.hashCode();
        return code;
    }

    /**
     * Returns a string representation of this extent.
     *
     * @todo Provides a more elaborated implementation.
     */
    public String toString() {
        return String.valueOf(spatialExtent);
    }    
}
