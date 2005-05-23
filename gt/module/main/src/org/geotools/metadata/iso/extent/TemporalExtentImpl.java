/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le Développement
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
package org.geotools.metadata.iso.extent;

// J2SE direct dependencies
import java.util.Date;

// OpenGIS dependencies
import org.opengis.metadata.extent.TemporalExtent;

// Geotools dependencies
import org.geotools.metadata.iso.MetadataEntity;


/**
 * Boundary enclosing the dataset, expressed as the closed set of
 * (<var>x</var>,<var>y</var>) coordinates of the polygon. The last
 * point replicates first point.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class TemporalExtentImpl extends MetadataEntity implements TemporalExtent {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 3668140516657118045L;

    /**
     * The start date and time for the content of the dataset,
     * in milliseconds ellapsed since January 1st, 1970. A value
     * of {@link Long#MIN_VALUE} means that this attribute is not set.
     */
    private long startTime = Long.MIN_VALUE;

    /**
     * The end date and time for the content of the dataset,
     * in milliseconds ellapsed since January 1st, 1970. A value
     * of {@link Long#MIN_VALUE} means that this attribute is not set.
     */
    private long endTime = Long.MIN_VALUE;
    
   /**
     * Constructs an initially empty temporal extent.
     */
    public TemporalExtentImpl() {
    }

    /**
     * Creates a temporal extent initialized to the specified values.
     */
    public TemporalExtentImpl(final Date startTime, final Date endTime) {
        setStartTime(startTime);
        setEndTime  (endTime);
    }

    /**
     * The start date and time for the content of the dataset.
     */
    public synchronized Date getStartTime() {
        return (startTime!=Long.MIN_VALUE) ? new Date(startTime) : null;
    }

    /**
     * Set the start date and time for the content of the dataset.
     */
    public synchronized void setStartTime(final Date newValue) {
        checkWritePermission();
        startTime = (newValue!=null) ? newValue.getTime() : Long.MIN_VALUE;
    }

    /**
     * Returns the end date and time for the content of the dataset.
     */
    public synchronized Date getEndTime() {
        return (endTime!=Long.MIN_VALUE) ? new Date(endTime) : null;
    }
    
    /**
     * Set the end date and time for the content of the dataset.
     */
    public synchronized void setEndTime(final Date newValue) {
        checkWritePermission();
        endTime = (newValue!=null) ? newValue.getTime() : Long.MIN_VALUE;
    }

    /**
     * Compare this TemporalExtent with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final TemporalExtentImpl that = (TemporalExtentImpl) object;
            return this.startTime == that.startTime &&
                   this.endTime   == that.endTime;
        }
        return false;
    }

    /**
     * Returns a hash code value for this series.
     */
    public synchronized int hashCode() {
        final long code = (startTime + 37*endTime);
        return (int)code ^ (int)(code >>> 32) ^ (int)serialVersionUID;
    }

    /**
     * Returns a string representation of this series.
     *
     * @todo Provides a more elaborated implementation.
     */
    public String toString() {
        return String.valueOf(startTime);
    }
}
