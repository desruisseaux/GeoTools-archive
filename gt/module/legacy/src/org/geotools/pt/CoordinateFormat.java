/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le D�veloppement
 * (C) 1998, P�ches et Oc�ans Canada
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
 */
package org.geotools.pt;

// J2SE dependencies
import java.text.FieldPosition;
import java.util.Locale;

import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.geometry.GeneralDirectPosition;


/**
 * Format a {@link CoordinatePoint} in an arbitrary {@link CoordinateSystem}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Replaced by {@link org.geotools.measure.CoordinateFormat}
 *             in the <code>org.geotools.measure</code> package.
 */
public class CoordinateFormat extends org.geotools.measure.CoordinateFormat {
    /**
     * The output coordinate system.
     */
    private CoordinateSystem coordinateSystem = GeographicCoordinateSystem.WGS84;

    /**
     * Construct a new coordinate format with default locale and
     * {@linkplain GeographicCoordinateSystem#WGS84 WGS 1984} coordinate system.
     */
    public CoordinateFormat() {
        super();
    }

    /**
     * Construct a new coordinate format for the specified locale and
     * {@linkplain GeographicCoordinateSystem#WGS84 WGS 1984} coordinate system.
     *
     * @param locale The locale for formatting coordinates and numbers.
     */
    public CoordinateFormat(final Locale locale) {
        super(locale);
    }

    /**
     * Construct a new coordinate format for the specified locale and coordinate system.
     *
     * @param locale The locale for formatting coordinates and numbers.
     * @param cs     The output coordinate system.
     */
    public CoordinateFormat(final Locale locale, final CoordinateSystem cs) {
        super(locale);
        setCoordinateSystem(cs);
    }

    /**
     * Returns the coordinate system for points to be formatted.
     *
     * @return The output coordinate system.
     */
    public CoordinateSystem getCoordinateSystem() {
        return coordinateSystem;
    }

    /**
     * Set the coordinate system for points to be formatted. The number
     * of dimensions must matched the dimension of points to be formatted.
     *
     * @param cs The new coordinate system.
     */
    public void setCoordinateSystem(final CoordinateSystem cs) {
        super.setCoordinateReferenceSystem(cs);
        coordinateSystem = cs;
    }

    /**
     * Formats a coordinate point.
     * The coordinate point dimension must matches the {@linkplain #getCoordinateSystem()
     * coordinate system} dimension.
     *
     * @param point      The {@link CoordinatePoint} to format.
     * @return           The formatted coordinate point.
     * @throws IllegalArgumentException if this <code>CoordinateFormat</code>
     *         cannot format the given object.
     */
    public String format(final CoordinatePoint point) {
        return super.format(new GeneralDirectPosition(point.ord));
    }
    
    /**
     * Formats a coordinate point and appends the resulting text to a given string buffer.
     * The coordinate point dimension must matches the {@linkplain #getCoordinateSystem()
     * coordinate system} dimension.
     *
     * @param point      The {@link CoordinatePoint} to format.
     * @param toAppendTo Where the text is to be appended.
     * @param position   A <code>FieldPosition</code> identifying a field in the formatted text,
     *                   or <code>null</code> if none.
     * @return           The string buffer passed in as <code>toAppendTo</code>, with formatted
     *                   text appended.
     * @throws IllegalArgumentException if this <code>CoordinateFormat</code>
     *         cannot format the given object.
     */
    public StringBuffer format(final CoordinatePoint point,
                               final StringBuffer    toAppendTo,
                               final FieldPosition   position)
            throws IllegalArgumentException
    {
        return super.format(new GeneralDirectPosition(point.ord), toAppendTo, position);
    }

    /**
     * Formats a coordinate point and appends the resulting text to a given string buffer.
     * The coordinate point dimension must matches the {@linkplain #getCoordinateSystem()
     * coordinate system} dimension.
     *
     * @param object     The {@link CoordinatePoint} to format.
     * @param toAppendTo Where the text is to be appended.
     * @param position   A <code>FieldPosition</code> identifying a field in the formatted text,
     *                   or <code>null</code> if none.
     * @return           The string buffer passed in as <code>toAppendTo</code>, with formatted
     *                   text appended.
     * @throws NullPointerException if <code>toAppendTo</code> is null.
     * @throws IllegalArgumentException if this <code>CoordinateFormat</code>
     *         cannot format the given object.
     */
    public StringBuffer format(final Object        object,
                               final StringBuffer  toAppendTo,
                               final FieldPosition position)
            throws IllegalArgumentException
    {
        if (object instanceof CoordinatePoint) {
            return format((CoordinatePoint) object, toAppendTo, position);
        } else {
            return super.format(object, toAppendTo, position);
        }
    }
}
