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
package org.geotools.referencing.cs;

// J2SE dependencies
import java.util.Map;

// OpenGIS dependencies
import org.opengis.referencing.cs.SphericalCS;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystemAxis;


/**
 * A three-dimensional coordinate system with one distance measured from the origin and two angular
 * coordinates. Not to be confused with an {@linkplain DefaultEllipsoidalCS ellipsoidal coordinate
 * system} based on an ellipsoid "degenerated" into a sphere. A <code>SphericalCS</code> shall have
 * three {@linkplain #getAxis axis}.
 *
 * <TABLE CELLPADDING='6' BORDER='1'>
 * <TR BGCOLOR="#EEEEFF"><TH NOWRAP>Used with CRS type(s)</TH></TR>
 * <TR><TD>
 *   {@link org.geotools.referencing.crs.DefaultGeocentricCRS  Geocentric},
 *   {@link org.geotools.referencing.crs.DefaultEngineeringCRS Engineering}
 * </TD></TR></TABLE>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class DefaultSphericalCS extends AbstractCS implements SphericalCS {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 196295996465774477L;

    /**
     * A three-dimensional spherical CS with
     * <var>{@linkplain CoordinateSystemAxis#SPHERICAL_LONGITUDE longitude}</var>,
     * <var>{@linkplain CoordinateSystemAxis#SPHERICAL_LATITUDE latitude}</var>,
     * <var>{@linkplain CoordinateSystemAxis#GEOCENTRIC_RADIUS radius}</var>
     * axis.
     *
     * @see DefaultCartesianCS#GEOCENTRIC
     *
     * @todo Localize name.
     */
    public static DefaultSphericalCS GEOCENTRIC = new DefaultSphericalCS("Geocentric",
                    DefaultCoordinateSystemAxis.SPHERICAL_LONGITUDE,
                    DefaultCoordinateSystemAxis.SPHERICAL_LATITUDE,
                    DefaultCoordinateSystemAxis.GEOCENTRIC_RADIUS);

    /**
     * Constructs a three-dimensional coordinate system from a name.
     *
     * @param name  The coordinate system name.
     * @param axis0 The first axis.
     * @param axis1 The second axis.
     * @param axis2 The third axis.
     */
    public DefaultSphericalCS(final String               name,
                              final CoordinateSystemAxis axis0,
                              final CoordinateSystemAxis axis1,
                              final CoordinateSystemAxis axis2)
    {
        super(name, new CoordinateSystemAxis[] {axis0, axis1, axis2});
    }

    /**
     * Constructs a three-dimensional coordinate system from a set of properties.
     * The properties map is given unchanged to the
     * {@linkplain AbstractCS#AbstractCS(Map,CoordinateSystemAxis[]) super-class constructor}.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param axis0 The first axis.
     * @param axis1 The second axis.
     * @param axis2 The third axis.
     */
    public DefaultSphericalCS(final Map             properties,
                              final CoordinateSystemAxis axis0,
                              final CoordinateSystemAxis axis1,
                              final CoordinateSystemAxis axis2)
    {
        super(properties, new CoordinateSystemAxis[] {axis0, axis1, axis2});
    }

    /**
     * Returns <code>true</code> if the specified axis direction is allowed for this coordinate
     * system. The default implementation accepts all directions except temporal ones (i.e.
     * {@link AxisDirection#FUTURE FUTURE} and {@link AxisDirection#PAST PAST}).
     */
    protected boolean isCompatibleDirection(final AxisDirection direction) {
        return !AxisDirection.FUTURE.equals(direction.absolute());
    }
}
