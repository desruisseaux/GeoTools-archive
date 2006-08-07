/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2004, Institut de Recherche pour le Développement
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
package org.geotools.referencing.cs;

// J2SE dependencies and extensions
import java.util.Map;
import javax.units.Converter;
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

// Geotools dependencies
import org.geotools.measure.Measure;
import org.geotools.resources.i18n.VocabularyKeys;


/**
 * A 1-, 2-, or 3-dimensional coordinate system. Gives the position of points relative to
 * orthogonal straight axes in the 2- and 3-dimensional cases. In the 1-dimensional case,
 * it contains a single straight coordinate axis. In the multi-dimensional case, all axes
 * shall have the same length unit of measure. A {@code CartesianCS} shall have one,
 * two, or three {@linkplain #getAxis axis}.
 *
 * <TABLE CELLPADDING='6' BORDER='1'>
 * <TR BGCOLOR="#EEEEFF"><TH NOWRAP>Used with CRS type(s)</TH></TR>
 * <TR><TD>
 *   {@link org.geotools.referencing.crs.DefaultGeocentricCRS  Geocentric},
 *   {@link org.geotools.referencing.crs.DefaultProjectedCRS   Projected},
 *   {@link org.geotools.referencing.crs.DefaultEngineeringCRS Engineering},
 *   {@link org.geotools.referencing.crs.DefaultImageCRS       Image}
 * </TD></TR></TABLE>
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see DefaultAffineCS
 */
public class DefaultCartesianCS extends DefaultAffineCS implements CartesianCS {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -6182037957705712945L;

    /**
     * A two-dimensional cartesian CS with
     * <var>{@linkplain DefaultCoordinateSystemAxis#EASTING Easting,}</var>,
     * <var>{@linkplain DefaultCoordinateSystemAxis#NORTHING Northing}</var>
     * axis in metres.
     */
    public static DefaultCartesianCS PROJECTED = new DefaultCartesianCS(
                    name(VocabularyKeys.PROJECTED),
                    DefaultCoordinateSystemAxis.EASTING,
                    DefaultCoordinateSystemAxis.NORTHING);

    /**
     * A three-dimensional cartesian CS with geocentric
     * <var>{@linkplain DefaultCoordinateSystemAxis#GEOCENTRIC_X x}</var>,
     * <var>{@linkplain DefaultCoordinateSystemAxis#GEOCENTRIC_Y y}</var>,
     * <var>{@linkplain DefaultCoordinateSystemAxis#GEOCENTRIC_Z z}</var>
     * axis in metres.
     *
     * @see DefaultSphericalCS#GEOCENTRIC
     */
    public static DefaultCartesianCS GEOCENTRIC = new DefaultCartesianCS(
                    name(VocabularyKeys.GEOCENTRIC),
                    DefaultCoordinateSystemAxis.GEOCENTRIC_X,
                    DefaultCoordinateSystemAxis.GEOCENTRIC_Y,
                    DefaultCoordinateSystemAxis.GEOCENTRIC_Z);

    /**
     * A two-dimensional cartesian CS with
     * <var>{@linkplain DefaultCoordinateSystemAxis#X x}</var>,
     * <var>{@linkplain DefaultCoordinateSystemAxis#Y y}</var>
     * axis in metres.
     */
    public static DefaultCartesianCS GENERIC_2D = new DefaultCartesianCS(
                    name(VocabularyKeys.CARTESIAN_2D),
                    DefaultCoordinateSystemAxis.X,
                    DefaultCoordinateSystemAxis.Y);

    /**
     * A three-dimensional cartesian CS with
     * <var>{@linkplain DefaultCoordinateSystemAxis#X x}</var>,
     * <var>{@linkplain DefaultCoordinateSystemAxis#Y y}</var>,
     * <var>{@linkplain DefaultCoordinateSystemAxis#Z z}</var>
     * axis in metres.
     */
    public static DefaultCartesianCS GENERIC_3D = new DefaultCartesianCS(
                    name(VocabularyKeys.CARTESIAN_3D),
                    DefaultCoordinateSystemAxis.X,
                    DefaultCoordinateSystemAxis.Y,
                    DefaultCoordinateSystemAxis.Z);

    /**
     * A two-dimensional cartesian CS with
     * <var>{@linkplain DefaultCoordinateSystemAxis#COLUMN column}</var>,
     * <var>{@linkplain DefaultCoordinateSystemAxis#ROW row}</var>
     * axis.
     */
    public static DefaultCartesianCS GRID = new DefaultCartesianCS(
                    name(VocabularyKeys.GRID),
                    DefaultCoordinateSystemAxis.COLUMN,
                    DefaultCoordinateSystemAxis.ROW);

    /**
     * A two-dimensional cartesian CS with
     * <var>{@linkplain DefaultCoordinateSystemAxis#DISPLAY_X display x}</var>,
     * <var>{@linkplain DefaultCoordinateSystemAxis#DISPLAY_Y display y}</var>
     * axis.
     *
     * @since 2.2
     */
    public static DefaultCartesianCS DISPLAY = new DefaultCartesianCS(
                    name(VocabularyKeys.DISPLAY),
                    DefaultCoordinateSystemAxis.DISPLAY_X,
                    DefaultCoordinateSystemAxis.DISPLAY_Y);

    /**
     * Converters from {@linkplain CoordinateSystemAxis#getUnit axis units} to
     * {@linkplain #getDistanceUnit distance unit}. Will be constructed only when
     * first needed.
     */
    private transient Converter[] converters;

    /**
     * Constructs a new coordinate system with the same values than the specified one.
     * This copy constructor provides a way to wrap an arbitrary implementation into a
     * Geotools one or a user-defined one (as a subclass), usually in order to leverage
     * some implementation-specific API. This constructor performs a shallow copy,
     * i.e. the properties are not cloned.
     *
     * @since 2.2
     */
    public DefaultCartesianCS(final CartesianCS cs) {
        super(cs);
    }

    /**
     * Constructs a two-dimensional coordinate system from a name.
     *
     * @param name  The coordinate system name.
     * @param axis0 The first axis.
     * @param axis1 The second axis.
     */
    public DefaultCartesianCS(final String               name,
                              final CoordinateSystemAxis axis0,
                              final CoordinateSystemAxis axis1)
    {
        super(name, axis0, axis1);
    }

    /**
     * Constructs a three-dimensional coordinate system from a name.
     *
     * @param name  The coordinate system name.
     * @param axis0 The first axis.
     * @param axis1 The second axis.
     * @param axis2 The third axis.
     */
    public DefaultCartesianCS(final String               name,
                              final CoordinateSystemAxis axis0,
                              final CoordinateSystemAxis axis1,
                              final CoordinateSystemAxis axis2)
    {
        super(name, axis0, axis1, axis2);
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
    public DefaultCartesianCS(final Map             properties,
                              final CoordinateSystemAxis axis0,
                              final CoordinateSystemAxis axis1)
    {
        super(properties, axis0, axis1);
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
    public DefaultCartesianCS(final Map             properties,
                              final CoordinateSystemAxis axis0,
                              final CoordinateSystemAxis axis1,
                              final CoordinateSystemAxis axis2)
    {
        super(properties, axis0, axis1, axis2);
    }

    /**
     * Computes the distance between two points.
     *
     * @param  coord1 Coordinates of the first point.
     * @param  coord2 Coordinates of the second point.
     * @return The distance between {@code coord1} and {@code coord2}.
     * @throws MismatchedDimensionException if a coordinate doesn't have the expected dimension.
     */
    public Measure distance(final double[] coord1, final double[] coord2)
            throws MismatchedDimensionException
    {
        ensureDimensionMatch("coord1", coord1);
        ensureDimensionMatch("coord2", coord2);
        final Unit unit = getDistanceUnit();
        Converter[] converters = this.converters; // Avoid the need for synchronization.
        if (converters == null) {
            converters = new Converter[getDimension()];
            for (int i=0; i<converters.length; i++) {
                converters[i] = getAxis(i).getUnit().getConverterTo(unit);
            }
            this.converters = converters;
        }
        double sum = 0;
        for (int i=0; i<converters.length; i++) {
            final Converter  c = converters[i];
            final double delta = c.convert(coord1[i]) - c.convert(coord2[i]);
            sum += delta*delta;
        }
        return new Measure(Math.sqrt(sum), unit);
    }

    /**
     * Returns a new coordinate system with the same properties than the current one except for
     * axis units.
     *
     * @param  unit The unit for the new axis.
     * @return A coordinate system with axis using the specified units.
     * @throws IllegalArgumentException If the specified unit is incompatible with the expected one.
     *
     * @since 2.2
     */
    public DefaultCartesianCS usingUnit(final Unit unit) throws IllegalArgumentException {
        final CoordinateSystemAxis[] axis = axisUsingUnit(unit);
        if (axis == null) {
            return this;
        }
        final Map properties = getProperties(this, null);
        switch (axis.length) {
            case 2:  return new DefaultCartesianCS(properties, axis[0], axis[1]);
            case 3:  return new DefaultCartesianCS(properties, axis[0], axis[1], axis[2]);
            default: throw new AssertionError(axis.length); // Should never happen.
        }
    }
}
