/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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
package org.geotools.referencing.datum;

// J2SE direct dependencies
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.geotools.referencing.IdentifiedObject;
import org.opengis.util.InternationalString;


/**
 * A temporal datum defines the origin of a temporal coordinate reference system.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class TemporalDatum extends Datum implements org.opengis.referencing.datum.TemporalDatum {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 3357241732140076884L;
    
    /**
     * Default datum for time measured since January 1st, 1970 at 00:00 UTC.
     */
    public static final TemporalDatum UNIX = new TemporalDatum("UNIX", new Date(0));

    /**
     * The date and time origin of this temporal datum.
     */
    private final long origin;

    /**
     * Construct a temporal datum from a name.
     *
     * @param name   The datum name.
     * @param origin The date and time origin of this temporal datum.
     */
    public TemporalDatum(final String name, final Date origin) {
        this(Collections.singletonMap(NAME_PROPERTY, name), origin);
    }

    /**
     * Construct a temporal datum from a set of properties. The properties map is
     * given unchanged to the {@linkplain Datum#Datum(Map) super-class constructor}.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param origin The date and time origin of this temporal datum.
     */
    public TemporalDatum(final Map properties, final Date origin) {
        super(properties);
        ensureNonNull("origin", origin);
        this.origin = origin.getTime();
    }

    /**
     * The date and time origin of this temporal datum.
     *
     * @return The date and time origin of this temporal datum.
     */
    public Date getOrigin() {
        return new Date(origin);
    }

    /**
     * Description of the point or points used to anchor the datum to the Earth.
     *
     * @deprecated This attribute is defined in the {@link Datum} parent class,
     *             but is not used by a temporal datum.
     */
    public InternationalString getAnchorPoint() {
        return super.getAnchorPoint();
    }

    /**
     * The time after which this datum definition is valid.
     *
     * @deprecated This attribute is defined in the {@link Datum} parent class,
     *             but is not used by a temporal datum.
     */
    public Date getRealizationEpoch() {
        return super.getRealizationEpoch();
    }
    
    /**
     * Compare this temporal datum with the specified object for equality.
     *
     * @param  object The object to compare to <code>this</code>.
     * @param  compareMetadata <code>true</code> for performing a strict comparaison, or
     *         <code>false</code> for comparing only properties relevant to transformations.
     * @return <code>true</code> if both objects are equal.
     */
    public boolean equals(final IdentifiedObject object, final boolean compareMetadata) {
        if (object == this) {
            return true; // Slight optimization.
        }
        if (super.equals(object, compareMetadata)) {
            final TemporalDatum that = (TemporalDatum) object;
            return this.origin == that.origin;
        }
        return false;
    }

    /**
     * Returns a hash value for this temporal datum. {@linkplain #getName Name},
     * {@linkplain #getRemarks remarks} and the like are not taken in account. In
     * other words, two temporal datums will return the same hash value if they
     * are equal in the sense of
     * <code>{@link #equals equals}(IdentifiedObject, <strong>false</strong>)</code>.
     *
     * @return The hash code value. This value doesn't need to be the same
     *         in past or future versions of this class.
     */
    public int hashCode() {
        return super.hashCode() ^ (int)origin ^ (int)(origin >>> 32);
    }
}
