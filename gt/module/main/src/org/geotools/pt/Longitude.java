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


/**
 * A longitude angle. Positive longitudes are East, while
 * negative longitudes are West. This class has no direct
 * OpenGIS equivalent.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see Latitude
 * @see AngleFormat
 *
 * @deprecated Replaced by {@link org.geotools.measure.Longitude}
 *             in the <code>org.geotools.measure</code> package.
 */
public final class Longitude extends Angle {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -8614900608052762636L;
    
    /**
     * Minimum legal value for longitude (-180°).
     */
    public static final double MIN_VALUE = -180;
    
    /**
     * Maximum legal value for longitude (+180°).
     */
    public static final double MAX_VALUE = +180;
    
    /**
     * Contruct a new longitude with the specified value.
     *
     * @param theta Angle in degrees.
     */
    public Longitude(final double theta) {
        super(theta);
    }
    
    /**
     * Constructs a newly allocated <code>Longitude</code> object that
     * represents the longitude value represented by the string.   The
     * string should represents an angle in either fractional degrees
     * (e.g. 45.5°) or degrees with minutes and seconds (e.g. 45°30').
     * The hemisphere (E or W) is optional (default to East).
     *
     * @param  theta A string to be converted to a <code>Longitude</code>.
     * @throws NumberFormatException if the string does not contain a parsable longitude.
     */
    public Longitude(final String theta) throws NumberFormatException {
        super(theta);
    }
}
