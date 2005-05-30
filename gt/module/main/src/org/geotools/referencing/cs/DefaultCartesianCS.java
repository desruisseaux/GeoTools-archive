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
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

// Geotools dependencies
import org.geotools.measure.Measure;


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
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @since 2.1
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
     *
     * @todo Localize name.
     */
    public static DefaultCartesianCS PROJECTED = new DefaultCartesianCS("Projected",
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
     *
     * @todo Localize name.
     */
    public static DefaultCartesianCS GEOCENTRIC = new DefaultCartesianCS("Geocentric",
                    DefaultCoordinateSystemAxis.GEOCENTRIC_X,
                    DefaultCoordinateSystemAxis.GEOCENTRIC_Y,
                    DefaultCoordinateSystemAxis.GEOCENTRIC_Z);

    /**
     * A two-dimensional cartesian CS with
     * <var>{@linkplain DefaultCoordinateSystemAxis#X x}</var>,
     * <var>{@linkplain DefaultCoordinateSystemAxis#Y y}</var>
     * axis in metres.
     *
     * @todo Localize name.
     */
    public static DefaultCartesianCS GENERIC_2D = new DefaultCartesianCS("Cartesian",
                    DefaultCoordinateSystemAxis.X,
                    DefaultCoordinateSystemAxis.Y);

    /**
     * A three-dimensional cartesian CS with
     * <var>{@linkplain DefaultCoordinateSystemAxis#X x}</var>,
     * <var>{@linkplain DefaultCoordinateSystemAxis#Y y}</var>,
     * <var>{@linkplain DefaultCoordinateSystemAxis#Z z}</var>
     * axis in metres.
     *
     * @todo Localize name.
     */
    public static DefaultCartesianCS GENERIC_3D = new DefaultCartesianCS("Cartesian",
                    DefaultCoordinateSystemAxis.X,
                    DefaultCoordinateSystemAxis.Y,
                    DefaultCoordinateSystemAxis.Z);

    /**
     * A two-dimensional cartesian CS with
     * <var>{@linkplain DefaultCoordinateSystemAxis#COLUMN column}</var>,
     * <var>{@linkplain DefaultCoordinateSystemAxis#ROW row}</var>
     * axis.
     *
     * @todo Localize name.
     */
    public static DefaultCartesianCS GRID = new DefaultCartesianCS("Grid",
                    DefaultCoordinateSystemAxis.COLUMN,
                    DefaultCoordinateSystemAxis.ROW);

    /**
     * Converters from {@linkplain CoordinateSystemAxis#getUnit axis units} to
     * {@linkplain #getDistanceUnit distance unit}. Will be constructed only when
     * first needed.
     */
    private transient Converter[] converters;

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
}
