/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
 * (C) 2005, Institut de Recherche pour le Développement
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
package org.geotools.metadata.citation;

// J2SE direct dependencies
import java.util.Date;

// OpenGIS dependencies
import org.opengis.metadata.citation.DateType;

// Geotools dependencies
import org.geotools.metadata.MetadataEntity;
import org.geotools.resources.Utilities;


/**
 * Reference date and event used to describe it.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @deprecated Renamed as {@code CitationDateImpl} in {@code org.geotools.metadata.iso} subpackage.
 */
public class CitationDate extends MetadataEntity
        implements org.opengis.metadata.citation.CitationDate
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -2884791484254008454L;

    /**
     * Reference date for the cited resource in millisecondes ellapsed sine January 1st, 1970,
     * or {@link Long#MIN_VALUE} if none.
     */
    private long date = Long.MIN_VALUE;

    /**
     * Event used for reference date.
     */
    private DateType dateType;

    /**
     * Constructs an initially empty citation date.
     */
    public CitationDate() {
    }

    /**
     * Constructs a citation date initialized to the given date.
     */
    public CitationDate(final Date date, final DateType dateType) {
        setDate    (date);
        setDateType(dateType);
    }

    /**
     * Returns the reference date for the cited resource.
     */
    public synchronized Date getDate() {
        return (date!=Long.MIN_VALUE) ? new Date(date) : null;
    }

    /**
     * Set the reference date for the cited resource.
     */
    public synchronized void setDate(final Date newValue) {
        checkWritePermission();
        date = (newValue!=null) ? newValue.getTime() : Long.MIN_VALUE;
    }

    /**
     * Returns the event used for reference date.
     */
    public DateType getDateType() {
        return dateType;
    }

    /**
     * Set the event used for reference date.
     */
    public synchronized void setDateType(final DateType newValue) {
        checkWritePermission();
        dateType = newValue;
    }

    /**
     * Declares this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
    }

    /**
     * Compares this citation with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final CitationDate that = (CitationDate) object;
            return this.date == that.date && Utilities.equals(this.dateType, that.dateType);
        }
        return false;
    }

    /**
     * Returns a hash code value for this citation. For performance reason, this method do
     * not uses all attributes for computing the hash code. Instead, it uses the attributes
     * that are the most likely to be unique.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        code ^= (int)date ^ (int) (date >>> 32);
        if (dateType != null) code ^= dateType.hashCode();
        return code;
    }

    /**
     * Returns a string representation of this citation.
     *
     * @todo Provides a more elaborated implementation.
     */
    public String toString() {
        return String.valueOf(getDate());
    }
}
