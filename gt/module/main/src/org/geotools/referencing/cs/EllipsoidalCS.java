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
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;


/**
 * A two- or three-dimensional coordinate system in which position is specified by geodetic
 * latitude, geodetic longitude, and (in the three-dimensional case) ellipsoidal height. An
 * <code>EllipsoidalCS</code> shall have two or three {@linkplain #getAxis axis}.
 *
 * <TABLE CELLPADDING='6' BORDER='1'>
 * <TR BGCOLOR="#EEEEFF"><TH NOWRAP>Used with CRS type(s)</TH></TR>
 * <TR><TD>
 *   {@link org.geotools.referencing.crs.GeographicCRS  Geographic},
 *   {@link org.geotools.referencing.crs.EngineeringCRS Engineering}
 * </TD></TR></TABLE>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class EllipsoidalCS extends org.geotools.referencing.cs.CoordinateSystem
                        implements org.opengis.referencing.cs.EllipsoidalCS
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -1452492488902329211L;

    /**
     * A two-dimensional ellipsoidal CS with
     * <var>{@linkplain org.geotools.referencing.cs.CoordinateSystemAxis#GEODETIC_LONGITUDE longitude}</var>,
     * <var>{@linkplain org.geotools.referencing.cs.CoordinateSystemAxis#GEODETIC_LATITUDE latitude}</var>
     * axis in degrees.
     *
     * @todo Localize name.
     */
    public static EllipsoidalCS GEODETIC_2D = new EllipsoidalCS("Geodetic 2D",
                    org.geotools.referencing.cs.CoordinateSystemAxis.GEODETIC_LONGITUDE,
                    org.geotools.referencing.cs.CoordinateSystemAxis.GEODETIC_LATITUDE);

    /**
     * A three-dimensional ellipsoidal CS with
     * <var>{@linkplain org.geotools.referencing.cs.CoordinateSystemAxis#GEODETIC_LONGITUDE longitude}</var>,
     * <var>{@linkplain org.geotools.referencing.cs.CoordinateSystemAxis#GEODETIC_LATITUDE latitude}</var>,
     * <var>{@linkplain org.geotools.referencing.cs.CoordinateSystemAxis#ELLIPSOIDAL_HEIGHT height}</var>
     * axis.
     *
     * @todo Localize name.
     */
    public static EllipsoidalCS GEODETIC_3D = new EllipsoidalCS("Geodetic 3D",
                    org.geotools.referencing.cs.CoordinateSystemAxis.GEODETIC_LONGITUDE,
                    org.geotools.referencing.cs.CoordinateSystemAxis.GEODETIC_LATITUDE,
                    org.geotools.referencing.cs.CoordinateSystemAxis.ELLIPSOIDAL_HEIGHT);

    /**
     * Construct a two-dimensional coordinate system from a name.
     *
     * @param name  The coordinate system name.
     * @param axis0 The first axis.
     * @param axis1 The second axis.
     */
    public EllipsoidalCS(final String               name,
                         final CoordinateSystemAxis axis0,
                         final CoordinateSystemAxis axis1)
    {
        super(name, new CoordinateSystemAxis[] {axis0, axis1});
    }

    /**
     * Construct a three-dimensional coordinate system from a name.
     *
     * @param name  The coordinate system name.
     * @param axis0 The first axis.
     * @param axis1 The second axis.
     * @param axis2 The third axis.
     */
    public EllipsoidalCS(final String               name,
                         final CoordinateSystemAxis axis0,
                         final CoordinateSystemAxis axis1,
                         final CoordinateSystemAxis axis2)
    {
        super(name, new CoordinateSystemAxis[] {axis0, axis1, axis2});
    }

    /**
     * Construct a two-dimensional coordinate system from a set of properties. The properties map is
     * given unchanged to the {@linkplain CoordinateSystem#CoordinateSystem(Map,CoordinateSystemAxis[])
     * super-class constructor}.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param axis0 The first axis.
     * @param axis1 The second axis.
     */
    public EllipsoidalCS(final Map             properties,
                         final CoordinateSystemAxis axis0,
                         final CoordinateSystemAxis axis1)
    {
        super(properties, new CoordinateSystemAxis[] {axis0, axis1});
    }

    /**
     * Construct a three-dimensional coordinate system from a set of properties. The properties map is
     * given unchanged to the {@linkplain CoordinateSystem#CoordinateSystem(Map,CoordinateSystemAxis[])
     * super-class constructor}.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param axis0 The first axis.
     * @param axis1 The second axis.
     * @param axis2 The third axis.
     */
    public EllipsoidalCS(final Map             properties,
                         final CoordinateSystemAxis axis0,
                         final CoordinateSystemAxis axis1,
                         final CoordinateSystemAxis axis2)
    {
        super(properties, new CoordinateSystemAxis[] {axis0, axis1, axis2});
    }

    /**
     * Returns <code>true</code> if the specified axis direction is allowed for this coordinate
     * system. The default implementation accepts only the following directions:
     * {@link AxisDirection#NORTH NORTH}, {@link AxisDirection#SOUTH SOUTH},
     * {@link AxisDirection#EAST  EAST},  {@link AxisDirection#WEST  WEST},
     * {@link AxisDirection#UP    UP} and {@link AxisDirection#DOWN  DOWN}.
     */
    protected boolean isCompatibleDirection(AxisDirection direction) {
        direction = direction.absolute();
        return AxisDirection.NORTH.equals(direction) ||
               AxisDirection.EAST .equals(direction) ||
               AxisDirection.UP   .equals(direction);
    }
}
