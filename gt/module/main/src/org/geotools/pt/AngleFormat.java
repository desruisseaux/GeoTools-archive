/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
 * (C) 1999, Fisheries and Oceans Canada
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

// Text format
import java.util.Locale;
import java.text.FieldPosition;
import java.text.DecimalFormatSymbols;


/**
 * Parse and format angle according a specified pattern.
 *
 * @see Angle
 * @see Latitude
 * @see Longitude
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Replaced by {@link org.geotools.measure.AngleFormat}
 *             in the <code>org.geotools.measure</code> package.
 */
public class AngleFormat extends org.geotools.measure.AngleFormat {
    /**
     * Construct a new <code>AngleFormat</code> using
     * the current default locale and a default pattern.
     */
    public AngleFormat() {
        super();
    }
    
    /**
     * Construct a new <code>AngleFormat</code> using the
     * current default locale and the specified pattern.
     *
     * @param  pattern Pattern to use for parsing and formatting angle.
     *         See class description for an explanation of how this pattern work.
     * @throws IllegalArgumentException If the specified pattern is not legal.
     */
    public AngleFormat(final String pattern) throws IllegalArgumentException {
        super(pattern);
    }
    
    /**
     * Construct a new <code>AngleFormat</code>
     * using the specified pattern and locale.
     *
     * @param  pattern Pattern to use for parsing and formatting angle.
     *         See class description for an explanation of how this pattern work.
     * @param  locale Locale to use.
     * @throws IllegalArgumentException If the specified pattern is not legal.
     */
    public AngleFormat(final String pattern, final Locale locale) throws IllegalArgumentException {
        super(pattern, locale);
    }
    
    /**
     * Construct a new <code>AngleFormat</code>
     * using the specified pattern and decimal symbols.
     *
     * @param  pattern Pattern to use for parsing and formatting angle.
     *         See class description for an explanation of how this pattern work.
     * @param  symbols The symbols to use for parsing and formatting numbers.
     * @throws IllegalArgumentException If the specified pattern is not legal.
     */
    public AngleFormat(final String pattern, final DecimalFormatSymbols symbols) {
        super(pattern, symbols);
    }
    
    /**
     * Formats an angle, a latitude or a longitude and appends the resulting text
     * to a given string buffer.
     */
    public StringBuffer format(Object obj,
                               StringBuffer toAppendTo,
                               final FieldPosition pos)
        throws IllegalArgumentException
    {
        if (obj instanceof Latitude) {
            obj = new org.geotools.measure.Latitude(((Latitude) obj).degrees());
        }
        if (obj instanceof Longitude) {
            obj = new org.geotools.measure.Longitude(((Longitude) obj).degrees());
        }
        return super.format(obj, toAppendTo, pos);
    }
}
