/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2001, Institut de Recherche pour le Développement
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
import org.opengis.referencing.crs.TemporalCRS;
import org.opengis.referencing.datum.TemporalDatum;

// Geotools dependencies
import org.geotools.referencing.AbstractReferenceSystem;


/**
 * A 1D coordinate reference system used for the recording of time.
 *
 * <TABLE CELLPADDING='6' BORDER='1'>
 * <TR BGCOLOR="#EEEEFF"><TH NOWRAP>Used with CS type(s)</TH></TR>
 * <TR><TD>
 *   {@link TimeCS Time}
 * </TD></TR></TABLE>
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class DefaultTemporalCRS extends AbstractSingleCRS implements TemporalCRS {
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
     * Constructs a new temporal CRS with the same values than the specified one.
     * This copy constructor provides a way to wrap an arbitrary implementation into a
     * Geotools one or a user-defined one (as a subclass), usually in order to leverage
     * some implementation-specific API. This constructor performs a shallow copy,
     * i.e. the properties are not cloned.
     *
     * @since 2.2
     *
     * @see #wrap
     */
    public DefaultTemporalCRS(final TemporalCRS crs) {
        super(crs);
    }

    /**
     * Constructs a temporal CRS from a name.
     *
     * @param name The name.
     * @param datum The datum.
     * @param cs The coordinate system.
     */
    public DefaultTemporalCRS(final String         name,
                              final TemporalDatum datum,
                              final TimeCS           cs)
    {
        this(Collections.singletonMap(NAME_KEY, name), datum, cs);
    }

    /**
     * Constructs a temporal CRS from a set of properties. The properties are given unchanged to
     * the {@linkplain AbstractReferenceSystem#AbstractReferenceSystem(Map) super-class constructor}.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param cs The coordinate system.
     * @param datum The datum.
     */
    public DefaultTemporalCRS(final Map      properties,
                              final TemporalDatum datum,
                              final TimeCS           cs)
    {
        super(properties, datum, cs);
    }

    /**
     * Wraps an arbitrary temporal CRS into a Geotools implementation. This method is usefull
     * if the user wants to take advantage of {@link #toDate} and {@link #toValue} methods.
     * If the supplied CRS is already an instance of {@code DefaultTemporalCRS} or is {@code null},
     * then it is returned unchanged.
     */
    public static DefaultTemporalCRS wrap(final TemporalCRS crs) {
        if (crs==null || crs instanceof DefaultTemporalCRS) {
            return (DefaultTemporalCRS) crs;
        }
        return new DefaultTemporalCRS(crs);
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
