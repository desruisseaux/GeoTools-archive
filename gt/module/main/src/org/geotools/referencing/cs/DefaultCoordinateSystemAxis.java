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
package org.geotools.referencing.cs;

// J2SE dependencies and extensions
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.units.NonSI;
import javax.units.SI;
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.referencing.AbstractIdentifiedObject;
import org.geotools.referencing.wkt.Formatter;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.cts.Resources;
import org.geotools.util.NameFactory;


/**
 * Definition of a coordinate system axis. This is used to label axes, and indicate the orientation.
 * See {@linkplain org.opengis.referencing.cs#AxisNames axis name constraints}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @since 2.1
 *
 * @see AbstractCS
 * @see Unit
 */
public class DefaultCoordinateSystemAxis extends AbstractIdentifiedObject implements CoordinateSystemAxis {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 1449284160523432645L;
    
    /**
     * Default axis info for longitudes.
     * Increasing ordinates values go {@linkplain AxisDirection#EAST East}
     * and units are {@linkplain NonSI#DEGREE_ANGLE degrees}.
     * The abbreviation is "&phi;" (phi). This axis is usually part of a
     * {@link #LONGITUDE}, {@link #LATITUDE}, {@link #ALTITUDE} set.
     *
     * @see #GEODETIC_LONGITUDE
     * @see #SPHERICAL_LONGITUDE
     * @see #LATITUDE
     */
    public static final DefaultCoordinateSystemAxis LONGITUDE = new DefaultCoordinateSystemAxis(
            ResourceKeys.LONGITUDE, "\u03C6", AxisDirection.EAST, NonSI.DEGREE_ANGLE);
    
    /**
     * Default axis info for latitudes.
     * Increasing ordinates values go {@linkplain AxisDirection#NORTH North}
     * and units are {@linkplain NonSI#DEGREE_ANGLE degrees}.
     * The abbreviation is "&lambda;" (lambda). This axis is usually part of a
     * {@link #LONGITUDE}, {@link #LATITUDE}, {@link #ALTITUDE} set.
     *
     * @see #GEODETIC_LATITUDE
     * @see #SPHERICAL_LATITUDE
     * @see #LONGITUDE
     */
    public static final DefaultCoordinateSystemAxis LATITUDE = new DefaultCoordinateSystemAxis(
            ResourceKeys.LATITUDE, "\u03BB", AxisDirection.NORTH, NonSI.DEGREE_ANGLE);
    
    /**
     * The default axis for altitude values.
     * Increasing ordinates values go {@linkplain AxisDirection#UP up}
     * and units are {@linkplain SI#METER metres}.
     * The abbreviation is lower case "h". This axis is usually part of a
     * {@link #LONGITUDE}, {@link #LATITUDE}, {@link #ALTITUDE} set.
     *
     * @see #ELLIPSOIDAL_HEIGHT
     * @see #GEOCENTRIC_RADIUS
     * @see #GRAVITY_RELATED_HEIGHT
     * @see #DEPTH
     */
    public static final DefaultCoordinateSystemAxis ALTITUDE = new DefaultCoordinateSystemAxis(
            ResourceKeys.ALTITUDE, "h", AxisDirection.UP, SI.METER);
    
    /**
     * The default axis for depth.
     * Increasing ordinates values go {@linkplain AxisDirection#DOWN down}
     * and units are {@linkplain SI#METER metres}.
     *
     * @see #ALTITUDE
     * @see #ELLIPSOIDAL_HEIGHT
     * @see #GEOCENTRIC_RADIUS
     * @see #GRAVITY_RELATED_HEIGHT
     */
    public static final DefaultCoordinateSystemAxis DEPTH = new DefaultCoordinateSystemAxis(
            ResourceKeys.DEPTH, "d", AxisDirection.DOWN, SI.METER);
    
    /**
     * Default axis info for geodetic longitudes in a
     * {@linkplain org.geotools.referencing.crs.DefaultGeographicCRS geographic CRS}.
     * Increasing ordinates values go {@linkplain AxisDirection#EAST East}
     * and units are {@linkplain NonSI#DEGREE_ANGLE degrees}.
     * The abbreviation is "&phi;" (phi). This axis is usually part of a
     * {@link #GEODETIC_LONGITUDE}, {@link #GEODETIC_LATITUDE}, {@link #ELLIPSOIDAL_HEIGHT} set.
     *
     * @see #LONGITUDE
     * @see #SPHERICAL_LONGITUDE
     * @see #GEODETIC_LATITUDE
     */
    public static final DefaultCoordinateSystemAxis GEODETIC_LONGITUDE = new DefaultCoordinateSystemAxis(
            ResourceKeys.GEODETIC_LONGITUDE, "\u03C6", AxisDirection.EAST, NonSI.DEGREE_ANGLE);
    
    /**
     * Default axis info for geodetic latitudes in a
     * {@linkplain org.geotools.referencing.crs.DefaultGeographicCRS geographic CRS}.
     * Increasing ordinates values go {@linkplain AxisDirection#NORTH North}
     * and units are {@linkplain NonSI#DEGREE_ANGLE degrees}.
     * The abbreviation is "&lambda;" (lambda). This axis is usually part of a
     * {@link #GEODETIC_LONGITUDE}, {@link #GEODETIC_LATITUDE}, {@link #ELLIPSOIDAL_HEIGHT} set.
     *
     * @see #LATITUDE
     * @see #SPHERICAL_LATITUDE
     * @see #GEODETIC_LONGITUDE
     */
    public static final DefaultCoordinateSystemAxis GEODETIC_LATITUDE = new DefaultCoordinateSystemAxis(
            ResourceKeys.GEODETIC_LATITUDE, "\u03BB", AxisDirection.NORTH, NonSI.DEGREE_ANGLE);
    
    /**
     * The default axis for height values above the ellipsoid in a
     * {@linkplain org.geotools.referencing.crs.DefaultGeographicCRS geographic CRS}.
     * Increasing ordinates values go {@linkplain AxisDirection#UP up}
     * and units are {@linkplain SI#METER metres}.
     * The abbreviation is lower case "h". This axis is usually part of a
     * {@link #GEODETIC_LONGITUDE}, {@link #GEODETIC_LATITUDE}, {@link #ELLIPSOIDAL_HEIGHT} set.
     *
     * @see #ALTITUDE
     * @see #GEOCENTRIC_RADIUS
     * @see #GRAVITY_RELATED_HEIGHT
     * @see #DEPTH
     */
    public static final DefaultCoordinateSystemAxis ELLIPSOIDAL_HEIGHT = new DefaultCoordinateSystemAxis(
            ResourceKeys.ELLIPSOIDAL_HEIGHT, "h", AxisDirection.UP, SI.METER);
    
    /**
     * The default axis for height values measured from gravity.
     * Increasing ordinates values go {@linkplain AxisDirection#UP up}
     * and units are {@linkplain SI#METER metres}.
     * The abbreviation is lower case "h".
     *
     * @see #ALTITUDE
     * @see #ELLIPSOIDAL_HEIGHT
     * @see #GEOCENTRIC_RADIUS
     * @see #DEPTH
     */
    public static final DefaultCoordinateSystemAxis GRAVITY_RELATED_HEIGHT = new DefaultCoordinateSystemAxis(
            ResourceKeys.GRAVITY_RELATED_HEIGHT, "h", AxisDirection.UP, SI.METER);
    
    /**
     * Default axis info for longitudes in a
     * {@linkplain org.geotools.referencing.crs.DefaultGeocentricCRS geocentric CRS} using
     * {@linkplain DefaultSphericalCS spherical CS}.
     * Increasing ordinates values go {@linkplain AxisDirection#EAST East}
     * and units are {@linkplain NonSI#DEGREE_ANGLE degrees}.
     * The abbreviation is "&phi;" (phi). This axis is usually part of a
     * {@link #SPHERICAL_LONGITUDE}, {@link #SPHERICAL_LATITUDE}, {@link #GEOCENTRIC_RADIUS} set.
     *
     * @see #LONGITUDE
     * @see #GEODETIC_LONGITUDE
     * @see #SPHERICAL_LATITUDE
     */
    public static final DefaultCoordinateSystemAxis SPHERICAL_LONGITUDE = new DefaultCoordinateSystemAxis(
            ResourceKeys.SPHERICAL_LONGITUDE, "\u03C6", AxisDirection.EAST, NonSI.DEGREE_ANGLE);
    
    /**
     * Default axis info for latitudes in a
     * {@linkplain org.geotools.referencing.crs.DefaultGeocentricCRS geocentric CRS} using
     * {@linkplain DefaultSphericalCS spherical CS}.
     * Increasing ordinates values go {@linkplain AxisDirection#NORTH North}
     * and units are {@linkplain NonSI#DEGREE_ANGLE degrees}.
     * The abbreviation is "&lambda;" (lambda). This axis is usually part of a
     * {@link #SPHERICAL_LONGITUDE}, {@link #SPHERICAL_LATITUDE}, {@link #GEOCENTRIC_RADIUS} set.
     *
     * @see #LATITUDE
     * @see #GEODETIC_LATITUDE
     * @see #SPHERICAL_LONGITUDE
     */
    public static final DefaultCoordinateSystemAxis SPHERICAL_LATITUDE = new DefaultCoordinateSystemAxis(
            ResourceKeys.SPHERICAL_LATITUDE, "\u03BB", AxisDirection.NORTH, NonSI.DEGREE_ANGLE);
    
    /**
     * Default axis info for radius in a
     * {@linkplain org.geotools.referencing.crs.DefaultGeocentricCRS geocentric CRS} using
     * {@linkplain DefaultSphericalCS spherical CS}.
     * Increasing ordinates values go {@linkplain AxisDirection#UP up}
     * and units are {@linkplain SI#METER metres}.
     * The abbreviation is lower case "h". This axis is usually part of a
     * {@link #SPHERICAL_LONGITUDE}, {@link #SPHERICAL_LATITUDE}, {@link #GEOCENTRIC_RADIUS} set.
     *
     * @see #ALTITUDE
     * @see #ELLIPSOIDAL_HEIGHT
     * @see #GRAVITY_RELATED_HEIGHT
     * @see #DEPTH
     */
    public static final DefaultCoordinateSystemAxis GEOCENTRIC_RADIUS = new DefaultCoordinateSystemAxis(
            ResourceKeys.GEOCENTRIC_RADIUS, "r", AxisDirection.UP, SI.METER);
    
    /**
     * Default axis info for <var>x</var> values in a {@linkplain DefaultCartesianCS cartesian CS}.
     * Increasing ordinates values go {@linkplain AxisDirection#EAST East}
     * and units are {@linkplain SI#METER metres}.
     * The abbreviation is lower case "x". This axis is usually part of a
     * {@link #X}, {@link #Y}, {@link #Z} set.
     *
     * @see #EASTING
     * @see #WESTING
     */
    public static final DefaultCoordinateSystemAxis X = new DefaultCoordinateSystemAxis(
                        "x", AxisDirection.EAST, SI.METER);
    
    /**
     * Default axis info for <var>y</var> values in a {@linkplain DefaultCartesianCS cartesian CS}.
     * Increasing ordinates values go {@linkplain AxisDirection#NORTH North}
     * and units are {@linkplain SI#METER metres}.
     * The abbreviation is lower case "y". This axis is usually part of a
     * {@link #X}, {@link #Y}, {@link #Z} set.
     *
     * @see #NORTHING
     * @see #SOUTHING
     */
    public static final DefaultCoordinateSystemAxis Y = new DefaultCoordinateSystemAxis(
                        "y", AxisDirection.NORTH, SI.METER);
    
    /**
     * Default axis info for <var>z</var> values in a {@linkplain DefaultCartesianCS cartesian CS}.
     * Increasing ordinates values go {@linkplain AxisDirection#UP up}
     * and units are {@linkplain SI#METER metres}.
     * The abbreviation is lower case "z". This axis is usually part of a
     * {@link #X}, {@link #Y}, {@link #Z} set.
     */
    public static final DefaultCoordinateSystemAxis Z = new DefaultCoordinateSystemAxis(
                        "z", AxisDirection.UP, SI.METER);
    
    /**
     * Default axis info for <var>x</var> values in a
     * {@linkplain org.geotools.referencing.crs.DefaultGeocentricCRS geocentric CRS} using
     * {@linkplain DefaultCartesianCS cartesian CS}.
     * Increasing ordinates values go toward prime meridian
     * and units are {@linkplain SI#METER metres}.
     * The abbreviation is upper case "X". This axis is usually part of a
     * {@link #GEOCENTRIC_X}, {@link #GEOCENTRIC_Y}, {@link #GEOCENTRIC_Z} set.
     */
    public static final DefaultCoordinateSystemAxis GEOCENTRIC_X = new DefaultCoordinateSystemAxis(
            ResourceKeys.GEOCENTRIC_X, "X", AxisDirection.OTHER, SI.METER);
    
    /**
     * Default axis info for <var>y</var> values in a
     * {@linkplain org.geotools.referencing.crs.DefaultGeocentricCRS geocentric CRS} using
     * {@linkplain DefaultCartesianCS cartesian CS}.
     * Increasing ordinates values go {@linkplain AxisDirection#EAST East}
     * and units are {@linkplain SI#METER metres}.
     * The abbreviation is upper case "Y". This axis is usually part of a
     * {@link #GEOCENTRIC_X}, {@link #GEOCENTRIC_Y}, {@link #GEOCENTRIC_Z} set.
     */
    public static final DefaultCoordinateSystemAxis GEOCENTRIC_Y = new DefaultCoordinateSystemAxis(
            ResourceKeys.GEOCENTRIC_Y, "Y", AxisDirection.EAST, SI.METER);
    
    /**
     * Default axis info for <var>z</var> values in a
     * {@linkplain org.geotools.referencing.crs.DefaultGeocentricCRS geocentric CRS} using
     * {@linkplain DefaultCartesianCS cartesian CS}.
     * Increasing ordinates values go {@linkplain AxisDirection#NORTH North}
     * and units are {@linkplain SI#METER metres}.
     * The abbreviation is upper case "Z". This axis is usually part of a
     * {@link #GEOCENTRIC_X}, {@link #GEOCENTRIC_Y}, {@link #GEOCENTRIC_Z} set.
     */
    public static final DefaultCoordinateSystemAxis GEOCENTRIC_Z = new DefaultCoordinateSystemAxis(
            ResourceKeys.GEOCENTRIC_Z, "Z", AxisDirection.NORTH, SI.METER);
    
    /**
     * Default axis info for Easting values in a
     * {@linkplain org.geotools.referencing.crs.DefaultProjectedCRS projected CRS}.
     * Increasing ordinates values go {@linkplain AxisDirection#EAST East}
     * and units are {@linkplain SI#METER metres}.
     * The abbreviation is upper case "E". This axis is usually part of a
     * {@link #EASTING}, {@link #NORTHING} set.
     *
     * @see #X
     * @see #EASTING
     * @see #WESTING
     */
    public static final DefaultCoordinateSystemAxis EASTING = new DefaultCoordinateSystemAxis(
            ResourceKeys.EASTING, "E", AxisDirection.EAST, SI.METER);
    
    /**
     * Default axis info for Westing values in a
     * {@linkplain org.geotools.referencing.crs.DefaultProjectedCRS projected CRS}.
     * Increasing ordinates values go {@linkplain AxisDirection#WEST West}
     * and units are {@linkplain SI#METER metres}.
     * The abbreviation is upper case "W".
     *
     * @see #X
     * @see #EASTING
     * @see #WESTING
     */
    public static final DefaultCoordinateSystemAxis WESTING = new DefaultCoordinateSystemAxis(
            ResourceKeys.WESTING, "W", AxisDirection.WEST, SI.METER);
    
    /**
     * Default axis info for Northing values in a
     * {@linkplain org.geotools.referencing.crs.DefaultProjectedCRS projected CRS}.
     * Increasing ordinates values go {@linkplain AxisDirection#NORTH North}
     * and units are {@linkplain SI#METER metres}.
     * The abbreviation is upper case "N". This axis is usually part of a
     * {@link #EASTING}, {@link #NORTHING} set.
     *
     * @see #Y
     * @see #NORTHING
     * @see #SOUTHING
     */
    public static final DefaultCoordinateSystemAxis NORTHING = new DefaultCoordinateSystemAxis(
            ResourceKeys.NORTHING, "N", AxisDirection.NORTH, SI.METER);
    
    /**
     * Default axis info for Southing values in a
     * {@linkplain org.geotools.referencing.crs.DefaultProjectedCRS projected CRS}.
     * Increasing ordinates values go {@linkplain AxisDirection#SOUTH South}
     * and units are {@linkplain SI#METER metres}.
     * The abbreviation is upper case "S".
     *
     * @see #Y
     * @see #NORTHING
     * @see #SOUTHING
     */
    public static final DefaultCoordinateSystemAxis SOUTHING = new DefaultCoordinateSystemAxis(
            ResourceKeys.SOUTHING, "S", AxisDirection.SOUTH, SI.METER);
    
    /**
     * A default axis for time values in a {@linkplain DefaultTimeCS time CS}.
     * Increasing time go toward {@linkplain AxisDirection#FUTURE future}
     * and units are {@linkplain NonSI#DAY days}.
     * The abbreviation is lower case "t".
     */
    public static final DefaultCoordinateSystemAxis TIME = new DefaultCoordinateSystemAxis(
            ResourceKeys.TIME, "t", AxisDirection.FUTURE, NonSI.DAY);

    /**
     * A default axis for column indices in a {@linkplain org.opengis.coverage.grid.GridCoverage
     * grid coverage}. Increasing values go toward {@linkplain AxisDirection#COLUMN_POSITIVE
     * positive column number}. The abbreviation is lower case "i".
     */
    public static final DefaultCoordinateSystemAxis COLUMN = new DefaultCoordinateSystemAxis(
            ResourceKeys.COLUMN, "i", AxisDirection.COLUMN_POSITIVE, Unit.ONE);

    /**
     * A default axis for row indices in a {@linkplain org.opengis.coverage.grid.GridCoverage grid
     * coverage}. Increasing values go toward {@linkplain AxisDirection#ROW_POSITIVE positive row
     * number}. The abbreviation is lower case "j".
     */
    public static final DefaultCoordinateSystemAxis ROW = new DefaultCoordinateSystemAxis(
            ResourceKeys.ROW, "j", AxisDirection.ROW_POSITIVE, Unit.ONE);

    /**
     * The abbreviation used for this coordinate system axes. This abbreviation is also
     * used to identify the ordinates in coordinate tuple. Examples are "<var>X</var>"
     * and "<var>Y</var>".
     */
    private final String abbreviation;

    /**
     * Direction of this coordinate system axis. In the case of Cartesian projected
     * coordinates, this is the direction of this coordinate system axis locally.
     */
    private final AxisDirection direction;

    /**
     * The unit of measure used for this coordinate system axis.
     */
    private final Unit unit;

    /**
     * Constructs an axis from a set of properties. The properties map is given unchanged to the
     * {@linkplain AbstractIdentifiedObject#AbstractIdentifiedObject(Map) super-class constructor}.
     *
     * @param properties   Set of properties. Should contains at least <code>"name"</code>.
     * @param abbreviation The {@linkplain #getAbbreviation abbreviation} used for this
     *                     coordinate system axes.
     * @param direction    The {@linkplain #getDirection direction} of this coordinate system axis.
     * @param unit         The {@linkplain #getUnit unit of measure} used for this coordinate
     *                     system axis.
     */
    public DefaultCoordinateSystemAxis(final Map           properties,
                                       final String        abbreviation,
                                       final AxisDirection direction,
                                       final Unit          unit)
    {
        super(properties);
        this.abbreviation = abbreviation;
        this.direction    = direction;
        this.unit         = unit;
        ensureNonNull("abbreviation", abbreviation);
        ensureNonNull("direction",    direction);
        ensureNonNull("unit",         unit);
    }

    /**
     * Constructs an axis with the same {@linkplain #getName name} as the abbreviation.
     *
     * @param abbreviation The {@linkplain #getAbbreviation abbreviation} used for this
     *                     coordinate system axes.
     * @param direction    The {@linkplain #getDirection direction} of this coordinate system axis.
     * @param unit         The {@linkplain #getUnit unit of measure} used for this coordinate
     *                     system axis.
     */
    public DefaultCoordinateSystemAxis(final String        abbreviation,
                                       final AxisDirection direction,
                                       final Unit          unit)
    {
        this(Collections.singletonMap(NAME_PROPERTY, abbreviation), abbreviation, direction, unit);
    }

    /**
     * Constructs an axis with a name as an {@linkplain InternationalString international string}
     * and an abbreviation. The {@linkplain #getName name of this identified object} is set to the
     * unlocalized version of the {@code name} argument, as given by
     * <code>name.{@linkplain InternationalString#toString(Locale) toString}(null)</code>. The
     * same {@code name} argument is also stored as an {@linkplain #getAlias alias}, which
     * allows fetching localized versions of the name.
     *
     * @param name         The name of this axis. Also stored as an alias for localization purpose.
     * @param abbreviation The {@linkplain #getAbbreviation abbreviation} used for this
     *                     coordinate system axis.
     * @param direction    The {@linkplain #getDirection direction} of this coordinate system axis.
     * @param unit         The {@linkplain #getUnit unit of measure} used for this coordinate
     *                     system axis.
     */    
    public DefaultCoordinateSystemAxis(final InternationalString name,
                                       final String        abbreviation,
                                       final AxisDirection direction,
                                       final Unit          unit)
    {
        this(toMap(name), abbreviation, direction, unit);
    }

    /**
     * Work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private static Map toMap(final InternationalString name) {
        final Map properties = new HashMap(4);
        if (name != null) {
            properties.put(NAME_PROPERTY,  name.toString(Locale.US));
            properties.put(ALIAS_PROPERTY, NameFactory.create(new InternationalString[] {name}));
        }
        return properties;
    }

    /**
     * Constructs an axis with a name and an abbreviation as a resource bundle key.
     *
     * @param name         The resource bundle key for the name.
     * @param abbreviation The {@linkplain #getAbbreviation abbreviation} used for this
     *                     coordinate system axes.
     * @param direction    The {@linkplain #getDirection direction} of this coordinate system axis.
     * @param unit         The {@linkplain #getUnit unit of measure} used for this coordinate
     *                     system axis.
     */    
    private DefaultCoordinateSystemAxis(final int           name,
                                        final String        abbreviation,
                                        final AxisDirection direction,
                                        final Unit          unit)
    {
        this(Resources.formatInternational(name), abbreviation, direction, unit);
    }

    /**
     * Returns an axis direction constants from its name.
     *
     * @param  direction The direction name (e.g. "north", "east", etc.).
     * @return The axis direction for the given name/.
     * @throws NoSuchElementException if the given name is not a know axis direction.
     */
    public static AxisDirection getDirection(final String direction) throws NoSuchElementException {
        final String search = direction.trim();
        final AxisDirection[] values = AxisDirection.values();
        for (int i=0; i<values.length; i++) {
            final AxisDirection candidate = values[i];
            final String name = candidate.name();
            if (search.equalsIgnoreCase(name)) {
                return candidate;
            }
        }
        // TODO: localize
        throw new NoSuchElementException("Unknow axis direction: \""+direction+"\".");
    }

    /**
     * The abbreviation used for this coordinate system axes. This abbreviation is also
     * used to identify the ordinates in coordinate tuple. Examples are "<var>X</var>"
     * and "<var>Y</var>".
     *
     * @return The coordinate system axis abbreviation.
     */
    public String getAbbreviation() {
        return abbreviation;
    }

    /**
     * Direction of this coordinate system axis. In the case of Cartesian projected
     * coordinates, this is the direction of this coordinate system axis locally.
     * Examples:
     * {@linkplain AxisDirection#NORTH north} or {@linkplain AxisDirection#SOUTH south},
     * {@linkplain AxisDirection#EAST  east}  or {@linkplain AxisDirection#WEST  west},
     * {@linkplain AxisDirection#UP    up}    or {@linkplain AxisDirection#DOWN  down}.
     *
     * <P>Within any set of coordinate system axes, only one of each pair of terms
     * can be used. For earth-fixed coordinate reference systems, this direction is often
     * approximate and intended to provide a human interpretable meaning to the axis. When a
     * geodetic datum is used, the precise directions of the axes may therefore vary slightly
     * from this approximate direction.</P>
     *
     * <P>Note that an {@link org.geotools.referencing.crs.DefaultEngineeringCRS} often requires
     * specific descriptions of the directions of its coordinate system axes.</P>
     */
    public AxisDirection getDirection() {
        return direction;
    }

    /**
     * The unit of measure used for this coordinate system axis. The value of this
     * coordinate in a coordinate tuple shall be recorded using this unit of measure,
     * whenever those coordinates use a coordinate reference system that uses a
     * coordinate system that uses this axis.
     */
    public Unit getUnit() {
        return unit;
    }
    
    /**
     * Compares the specified object with this axis for equality.
     *
     * @param  object The object to compare to {@code this}.
     * @param  compareMetadata {@code true} for performing a strict comparaison, or
     *         {@code false} for comparing only properties relevant to transformations.
     * @return {@code true} if both objects are equal.
     */
    public boolean equals(final AbstractIdentifiedObject object, final boolean compareMetadata) {
        if (object == this) {
            return true; // Slight optimization.
        }
        if (super.equals(object, compareMetadata)) {
            final DefaultCoordinateSystemAxis that = (DefaultCoordinateSystemAxis) object;
            if (!compareMetadata) {
                /*
                 * We compare the code since it is the only way to differentiate axes with the same
                 * abbreviation, for example LONGITUDE and GEODETIC_LONGITUDE constants. However,
                 * we don't need to perform this check if 'compareMetadata' is true since in such
                 * case, the comparaison will already have been done in the super-class.
                 */
                if (!Utilities.equals(this.getName().getCode(), that.getName().getCode())) {
                    return false;
                }
            }
            return Utilities.equals(this.abbreviation, that.abbreviation) &&
                   Utilities.equals(this.direction,    that.direction)    &&
                   Utilities.equals(this.unit,         that.unit);
        }
        return false;
    }
    
    /**
     * Returns a hash value for this axis. This value doesn't need to be the same
     * in past or future versions of this class.
     */
    public int hashCode() {
        int code = (int)serialVersionUID;
        code = code*37 + abbreviation.hashCode();
        code = code*37 + direction   .hashCode();
        code = code*37 + unit        .hashCode();
        return code;
    }
    
    /**
     * Format the inner part of a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> (WKT)</A> element. WKT is returned by the {@link #toString toString} method
     * and looks like <code>AXIS["name",NORTH]</code>.
     *
     * @param  formatter The formatter to use.
     * @return The WKT element name, which is "AXIS".
     */
    protected String formatWKT(final Formatter formatter) {
        formatter.append(direction);
        return "AXIS";
    }
}
