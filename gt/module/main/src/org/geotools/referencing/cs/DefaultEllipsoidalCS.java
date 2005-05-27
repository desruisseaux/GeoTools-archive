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

// J2SE dependencies and extensions
import java.util.Map;
import javax.units.Converter;
import javax.units.NonSI;
import javax.units.SI;
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.referencing.cs.EllipsoidalCS;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;


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
public class DefaultEllipsoidalCS extends AbstractCS implements EllipsoidalCS {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -1452492488902329211L;

    /**
     * A two-dimensional ellipsoidal CS with
     * <var>{@linkplain DefaultCoordinateSystemAxis#GEODETIC_LONGITUDE longitude}</var>,
     * <var>{@linkplain DefaultCoordinateSystemAxis#GEODETIC_LATITUDE latitude}</var>
     * axis in degrees.
     *
     * @todo Localize name.
     */
    public static DefaultEllipsoidalCS GEODETIC_2D = new DefaultEllipsoidalCS("Geodetic 2D",
                    DefaultCoordinateSystemAxis.GEODETIC_LONGITUDE,
                    DefaultCoordinateSystemAxis.GEODETIC_LATITUDE);

    /**
     * A three-dimensional ellipsoidal CS with
     * <var>{@linkplain DefaultCoordinateSystemAxis#GEODETIC_LONGITUDE longitude}</var>,
     * <var>{@linkplain DefaultCoordinateSystemAxis#GEODETIC_LATITUDE latitude}</var>,
     * <var>{@linkplain DefaultCoordinateSystemAxis#ELLIPSOIDAL_HEIGHT height}</var>
     * axis.
     *
     * @todo Localize name.
     */
    public static DefaultEllipsoidalCS GEODETIC_3D = new DefaultEllipsoidalCS("Geodetic 3D",
                    DefaultCoordinateSystemAxis.GEODETIC_LONGITUDE,
                    DefaultCoordinateSystemAxis.GEODETIC_LATITUDE,
                    DefaultCoordinateSystemAxis.ELLIPSOIDAL_HEIGHT);

    /**
     * The axis number for longitude, latitude and height.
     * Will be constructed only when first needed.
     */
    private transient int longitudeAxis, latitudeAxis, heightAxis;

    /**
     * The unit converters for longitude, latitude and height.
     * Will be constructed only when first needed.
     */
    private transient Converter longitudeConverter, latitudeConverter, heightConverter;

    /**
     * Constructs a two-dimensional coordinate system from a name.
     *
     * @param name  The coordinate system name.
     * @param axis0 The first axis.
     * @param axis1 The second axis.
     */
    public DefaultEllipsoidalCS(final String               name,
                                final CoordinateSystemAxis axis0,
                                final CoordinateSystemAxis axis1)
    {
        super(name, new CoordinateSystemAxis[] {axis0, axis1});
    }

    /**
     * Constructs a three-dimensional coordinate system from a name.
     *
     * @param name  The coordinate system name.
     * @param axis0 The first axis.
     * @param axis1 The second axis.
     * @param axis2 The third axis.
     */
    public DefaultEllipsoidalCS(final String               name,
                                final CoordinateSystemAxis axis0,
                                final CoordinateSystemAxis axis1,
                                final CoordinateSystemAxis axis2)
    {
        super(name, new CoordinateSystemAxis[] {axis0, axis1, axis2});
    }

    /**
     * Constructs a two-dimensional coordinate system from a set of properties.
     * The properties map is given unchanged to the
     * {@linkplain AbstractCS#AbstractCS(Map,CoordinateSystemAxis[]) super-class constructor}.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param axis0 The first axis.
     * @param axis1 The second axis.
     */
    public DefaultEllipsoidalCS(final Map             properties,
                                final CoordinateSystemAxis axis0,
                                final CoordinateSystemAxis axis1)
    {
        super(properties, new CoordinateSystemAxis[] {axis0, axis1});
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
    public DefaultEllipsoidalCS(final Map             properties,
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

    /**
     * Update the converters.
     */
    private void update() {
        for (int i=getDimension(); --i>=0;) {
            final CoordinateSystemAxis axis = getAxis(i);
            final AxisDirection   direction = axis.getDirection().absolute();
            final Unit                 unit = axis.getUnit();
            if (AxisDirection.EAST.equals(direction)) {
                longitudeAxis      = i;
                longitudeConverter = unit.getConverterTo(NonSI.DEGREE_ANGLE);
                continue;
            }
            if (AxisDirection.NORTH.equals(direction)) {
                latitudeAxis      = i;
                latitudeConverter = unit.getConverterTo(NonSI.DEGREE_ANGLE);
                continue;
            }
            if (AxisDirection.UP.equals(direction)) {
                heightAxis      = i;
                heightConverter = unit.getConverterTo(SI.METER);
                continue;
            }
            // Should not happen, since 'isCompatibleDirection'
            // has already checked axis directions.
            throw new AssertionError(direction);
        }
    }

    /**
     * Returns the longitude found in the specified coordinate point,
     * always in {@linkplain NonSI#DEGREE_ANGLE degrees}.
     *
     * @param  coordinates The coordinate point expressed in this coordinate system.
     * @return The longitude in the specified array, in {@linkplain NonSI#DEGREE_ANGLE degrees}.
     * @throws MismatchedDimensionException is the coordinate point doesn't have the expected
     *         dimension.
     */
    public double getLongitude(final double[] coordinates)
            throws MismatchedDimensionException
    {
        ensureDimensionMatch("coordinates", coordinates);
        if (longitudeConverter == null) {
            update();
        }
        return longitudeConverter.convert(coordinates[longitudeAxis]);
    }

    /**
     * Returns the latitude found in the specified coordinate point,
     * always in {@linkplain NonSI#DEGREE_ANGLE degrees}.
     *
     * @param  coordinates The coordinate point expressed in this coordinate system.
     * @return The latitude in the specified array, in {@linkplain NonSI#DEGREE_ANGLE degrees}.
     * @throws MismatchedDimensionException is the coordinate point doesn't have the expected
     *         dimension.
     */
    public double getLatitude(final double[] coordinates)
            throws MismatchedDimensionException
    {
        ensureDimensionMatch("coordinates", coordinates);
        if (latitudeConverter == null) {
            update();
        }
        return latitudeConverter.convert(coordinates[latitudeAxis]);
    }

    /**
     * Returns the height found in the specified coordinate point,
     * always in {@linkplain SI#METER meters}.
     *
     * @param  coordinates The coordinate point expressed in this coordinate system.
     * @return The height in the specified array, in {@linkplain SI#METER meters}.
     * @throws MismatchedDimensionException is the coordinate point doesn't have the expected
     *         dimension.
     */
    public double getHeight(final double[] coordinates)
            throws MismatchedDimensionException
    {
        ensureDimensionMatch("coordinates", coordinates);
        if (heightConverter == null) {
            update();
            if (heightConverter == null) {
                throw new IllegalStateException("Not a 3D coordinate system");
                // TODO: localize the error message.
            }
        }
        return heightConverter.convert(coordinates[heightAxis]);
    }
}
