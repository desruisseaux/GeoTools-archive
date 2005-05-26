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
package org.geotools.referencing.crs;

// J2SE dependencies and extensions
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import javax.units.Converter;
import javax.units.SI;
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.referencing.cs.TimeCS;
import org.opengis.referencing.datum.TemporalDatum;

// Geotools dependencies
import org.geotools.referencing.DefaultIdentifiedObject;


/**
 * A 1D coordinate reference system used for the recording of time.
 *
 * <TABLE CELLPADDING='6' BORDER='1'>
 * <TR BGCOLOR="#EEEEFF"><TH NOWRAP>Used with CS type(s)</TH></TR>
 * <TR><TD>
 *   {@link TimeCS Time}
 * </TD></TR></TABLE>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class TemporalCRS extends org.geotools.referencing.crs.SingleCRS
                      implements org.opengis.referencing.crs.TemporalCRS
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 3000119849197222007L;

    /**
     * Unit for milliseconds. Usefull for conversion from and to {@link Date} objects.
     */
    public static Unit MILLISECOND = SI.MILLI(SI.SECOND);

    /**
     * A converter from values in this CRS to values in milliseconds.
     * Will be constructed only when first needed.
     */
    private transient Converter toMillis;

    /**
     * The {@linkplain TemporalDatum#getOrigin origin} in milliseconds since January 1st, 1970.
     * This field could be implicit in the {@link #toMillis} converter, but we still handle it
     * explicitly in order to use integer arithmetic.
     */
    private transient long origin;

    /**
     * Constructs a temporal CRS from a name.
     *
     * @param name The name.
     * @param datum The datum.
     * @param cs The coordinate system.
     */
    public TemporalCRS(final String         name,
                       final TemporalDatum datum,
                       final TimeCS           cs)
    {
        this(Collections.singletonMap(NAME_PROPERTY, name), datum, cs);
    }

    /**
     * Constructs a temporal CRS from a set of properties. The properties are given unchanged to
     * the {@linkplain DefaultReferenceSystem#DefaultReferenceSystem(Map) super-class constructor}.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param cs The coordinate system.
     * @param datum The datum.
     */
    public TemporalCRS(final Map      properties,
                       final TemporalDatum datum,
                       final TimeCS           cs)
    {
        super(properties, datum, cs);
    }

    /**
     * Wraps an arbitrary temporal CRS into a Geotools implementation. This method is usefull
     * if the user wants to take advantage of {@link #toDate} and {@link #toValue} methods.
     */
    public static TemporalCRS wrap(final org.opengis.referencing.crs.TemporalCRS crs) {
        if (crs instanceof TemporalCRS) {
            return (TemporalCRS) crs;
        }
        // TODO: remove cast once we will be allowed to compile for J2SE 1.5.
        return new TemporalCRS(getProperties(crs),
                   (TemporalDatum) crs.getDatum(),
                   (TimeCS) crs.getCoordinateSystem());
    }

    /**
     * Initialize the fields required for {@link #toDate} and {@link #toValue} operations.
     */
    private void initializeConverter() {
        origin   = ((TemporalDatum)datum).getOrigin().getTime();
        toMillis = coordinateSystem.getAxis(0).getUnit().getConverterTo(MILLISECOND);
    }

    /**
     * Convert the given value into a {@link Date} object.
     * This method is the converse of {@link #toValue}.
     *
     * @param  value A value in this axis unit.
     * @return The value as a {@linkplain Date date}.
     */
    public Date toDate(final double value) {
        if (toMillis == null) {
            initializeConverter();
        }
        return new Date(Math.round(toMillis.convert(value)) + origin);
    }

    /**
     * Convert the given {@linkplain Date date} into a value in this axis unit.
     * This method is the converse of {@link #toDate}.
     *
     * @param  time The value as a {@linkplain Date date}.
     * @return value A value in this axis unit.
     */
    public double toValue(final Date time) {
        if (toMillis == null) {
            initializeConverter();
        }
        return toMillis.inverse().convert(time.getTime() - origin);
    }
    
    /**
     * Returns a hash value for this geographic CRS.
     *
     * @return The hash code value. This value doesn't need to be the same
     *         in past or future versions of this class.
     */
    public int hashCode() {
        return (int)serialVersionUID ^ super.hashCode();
    }
}
