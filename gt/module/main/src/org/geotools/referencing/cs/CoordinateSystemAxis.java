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

// J2SE dependencies
import java.util.Map;
import java.util.Locale;
import java.util.Collections;
import javax.units.NonSI;
import javax.units.SI;
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.referencing.cs.AxisDirection;

// Geotools dependencies
import org.geotools.referencing.IdentifiedObject;
import org.geotools.referencing.wkt.Formatter;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Definition of a coordinate system axis. This is used to label axes, and indicate the orientation.
 * See {@linkplain org.opengis.referencing.cs#AxisNames axis name constraints}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see CoordinateSystem
 * @see Unit
 */
public class CoordinateSystemAxis extends IdentifiedObject
                               implements org.opengis.referencing.cs.CoordinateSystemAxis
{
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
     * @see #LONGITUDE
     * @see #GEODETIC_LONGITUDE
     * @see #SPHERICAL_LONGITUDE
     */
    public static final CoordinateSystemAxis LONGITUDE =
                        new Localized("\u03C6", AxisDirection.EAST, NonSI.DEGREE_ANGLE,
                                      ResourceKeys.LONGITUDE);
    
    /**
     * Default axis info for latitudes.
     * Increasing ordinates values go {@linkplain AxisDirection#NORTH North}
     * and units are {@linkplain NonSI#DEGREE_ANGLE degrees}.
     * The abbreviation is "&lambda;" (lambda). This axis is usually part of a
     * {@link #LONGITUDE}, {@link #LATITUDE}, {@link #ALTITUDE} set.
     *
     * @see #LATITUDE
     * @see #GEODETIC_LATITUDE
     * @see #SPHERICAL_LATITUDE
     */
    public static final CoordinateSystemAxis LATITUDE =
                        new Localized("\u03BB", AxisDirection.NORTH, NonSI.DEGREE_ANGLE,
                                      ResourceKeys.LATITUDE);
    
    /**
     * The default axis for altitude values.
     * Increasing ordinates values go {@linkplain AxisDirection#UP up}
     * and units are {@linkplain SI#METER metres}.
     * The abbreviation is lower case "h". This axis is usually part of a
     * {@link #LONGITUDE}, {@link #LATITUDE}, {@link #ALTITUDE} set.
     *
     * @see #ALTITUDE
     * @see #ELLIPSOIDAL_HEIGHT
     * @see #GEOCENTRIC_RADIUS
     * @see #GRAVITY_RELATED_HEIGHT
     * @see #DEPTH
     */
    public static final CoordinateSystemAxis ALTITUDE =
                        new Localized("h", AxisDirection.UP, SI.METER,
                                      ResourceKeys.ALTITUDE);
    
    /**
     * The default axis for depth.
     * Increasing ordinates values go {@linkplain AxisDirection#DOWN down}
     * and units are {@linkplain SI#METER metres}.
     *
     * @see #ALTITUDE
     * @see #ELLIPSOIDAL_HEIGHT
     * @see #GEOCENTRIC_RADIUS
     * @see #GRAVITY_RELATED_HEIGHT
     * @see #DEPTH
     */
    public static final CoordinateSystemAxis DEPTH =
                        new Localized("d", AxisDirection.DOWN, SI.METER,
                                      ResourceKeys.DEPTH);
    
    /**
     * Default axis info for geodetic longitudes in a
     * {@linkplain org.geotools.referencing.crs.GeographicCRS geographic CRS}.
     * Increasing ordinates values go {@linkplain AxisDirection#EAST East}
     * and units are {@linkplain NonSI#DEGREE_ANGLE degrees}.
     * The abbreviation is "&phi;" (phi). This axis is usually part of a
     * {@link #GEODETIC_LONGITUDE}, {@link #GEODETIC_LATITUDE}, {@link #ELLIPSOIDAL_HEIGHT} set.
     *
     * @see #LONGITUDE
     * @see #GEODETIC_LONGITUDE
     * @see #SPHERICAL_LONGITUDE
     */
    public static final CoordinateSystemAxis GEODETIC_LONGITUDE =
                        new Localized("\u03C6", AxisDirection.EAST, NonSI.DEGREE_ANGLE,
                                      ResourceKeys.GEODETIC_LONGITUDE);
    
    /**
     * Default axis info for geodetic latitudes in a
     * {@linkplain org.geotools.referencing.crs.GeographicCRS geographic CRS}.
     * Increasing ordinates values go {@linkplain AxisDirection#NORTH North}
     * and units are {@linkplain NonSI#DEGREE_ANGLE degrees}.
     * The abbreviation is "&lambda;" (lambda). This axis is usually part of a
     * {@link #GEODETIC_LONGITUDE}, {@link #GEODETIC_LATITUDE}, {@link #ELLIPSOIDAL_HEIGHT} set.
     *
     * @see #LATITUDE
     * @see #GEODETIC_LATITUDE
     * @see #SPHERICAL_LATITUDE
     */
    public static final CoordinateSystemAxis GEODETIC_LATITUDE =
                        new Localized("\u03BB", AxisDirection.NORTH, NonSI.DEGREE_ANGLE,
                                      ResourceKeys.GEODETIC_LATITUDE);
    
    /**
     * The default axis for height values above the ellipsoid in a
     * {@linkplain org.geotools.referencing.crs.GeographicCRS geographic CRS}.
     * Increasing ordinates values go {@linkplain AxisDirection#UP up}
     * and units are {@linkplain SI#METER metres}.
     * The abbreviation is lower case "h". This axis is usually part of a
     * {@link #GEODETIC_LONGITUDE}, {@link #GEODETIC_LATITUDE}, {@link #ELLIPSOIDAL_HEIGHT} set.
     *
     * @see #ALTITUDE
     * @see #ELLIPSOIDAL_HEIGHT
     * @see #GEOCENTRIC_RADIUS
     * @see #GRAVITY_RELATED_HEIGHT
     * @see #DEPTH
     */
    public static final CoordinateSystemAxis ELLIPSOIDAL_HEIGHT =
                        new Localized("h", AxisDirection.UP, SI.METER,
                                      ResourceKeys.ELLIPSOIDAL_HEIGHT);
    
    /**
     * The default axis for height values measured from gravity.
     * Increasing ordinates values go {@linkplain AxisDirection#UP up}
     * and units are {@linkplain SI#METER metres}.
     * The abbreviation is lower case "h".
     *
     * @see #ALTITUDE
     * @see #ELLIPSOIDAL_HEIGHT
     * @see #GEOCENTRIC_RADIUS
     * @see #GRAVITY_RELATED_HEIGHT
     * @see #DEPTH
     */
    public static final CoordinateSystemAxis GRAVITY_RELATED_HEIGHT =
                        new Localized("h", AxisDirection.UP, SI.METER,
                                      ResourceKeys.GRAVITY_RELATED_HEIGHT);
    
    /**
     * Default axis info for longitudes in a
     * {@linkplain org.geotools.referencing.crs.GeocentricCRS geocentric CRS} using
     * {@linkplain SphericalCS spherical CS}.
     * Increasing ordinates values go {@linkplain AxisDirection#EAST East}
     * and units are {@linkplain NonSI#DEGREE_ANGLE degrees}.
     * The abbreviation is "&phi;" (phi). This axis is usually part of a
     * {@link #SPHERICAL_LONGITUDE}, {@link #SPHERICAL_LATITUDE}, {@link #GEOCENTRIC_RADIUS} set.
     *
     * @see #LONGITUDE
     * @see #GEODETIC_LONGITUDE
     * @see #SPHERICAL_LONGITUDE
     */
    public static final CoordinateSystemAxis SPHERICAL_LONGITUDE =
                        new Localized("\u03C6", AxisDirection.EAST, NonSI.DEGREE_ANGLE,
                                      ResourceKeys.SPHERICAL_LONGITUDE);
    
    /**
     * Default axis info for latitudes in a
     * {@linkplain org.geotools.referencing.crs.GeocentricCRS geocentric CRS} using
     * {@linkplain SphericalCS spherical CS}.
     * Increasing ordinates values go {@linkplain AxisDirection#NORTH North}
     * and units are {@linkplain NonSI#DEGREE_ANGLE degrees}.
     * The abbreviation is "&lambda;" (lambda). This axis is usually part of a
     * {@link #SPHERICAL_LONGITUDE}, {@link #SPHERICAL_LATITUDE}, {@link #GEOCENTRIC_RADIUS} set.
     *
     * @see #LATITUDE
     * @see #GEODETIC_LATITUDE
     * @see #SPHERICAL_LATITUDE
     */
    public static final CoordinateSystemAxis SPHERICAL_LATITUDE =
                        new Localized("\u03BB", AxisDirection.NORTH, NonSI.DEGREE_ANGLE,
                                      ResourceKeys.SPHERICAL_LATITUDE);
    
    /**
     * Default axis info for radius in a
     * {@linkplain org.geotools.referencing.crs.GeocentricCRS geocentric CRS} using
     * {@linkplain SphericalCS spherical CS}.
     * Increasing ordinates values go {@linkplain AxisDirection#UP up}
     * and units are {@linkplain SI#METER metres}.
     * The abbreviation is lower case "h". This axis is usually part of a
     * {@link #SPHERICAL_LONGITUDE}, {@link #SPHERICAL_LATITUDE}, {@link #GEOCENTRIC_RADIUS} set.
     *
     * @see #ALTITUDE
     * @see #ELLIPSOIDAL_HEIGHT
     * @see #GEOCENTRIC_RADIUS
     * @see #GRAVITY_RELATED_HEIGHT
     * @see #DEPTH
     */
    public static final CoordinateSystemAxis GEOCENTRIC_RADIUS =
                        new Localized("r", AxisDirection.UP, SI.METER,
                                      ResourceKeys.GEOCENTRIC_RADIUS);
    
    /**
     * Default axis info for <var>x</var> values in a {@linkplain CartesianCS cartesian CS}.
     * Increasing ordinates values go {@linkplain AxisDirection#EAST East}
     * and units are {@linkplain SI#METER metres}.
     * The abbreviation is lower case "x". This axis is usually part of a
     * {@link #X}, {@link #Y}, {@link #Z} set.
     *
     * @see #X
     * @see #GEOCENTRIC_X
     * @see #EASTING
     * @see #WESTING
     */
    public static final CoordinateSystemAxis X =
                        new CoordinateSystemAxis("x", AxisDirection.EAST, SI.METER);
    
    /**
     * Default axis info for <var>y</var> values in a {@linkplain CartesianCS cartesian CS}.
     * Increasing ordinates values go {@linkplain AxisDirection#NORTH North}
     * and units are {@linkplain SI#METER metres}.
     * The abbreviation is lower case "y". This axis is usually part of a
     * {@link #X}, {@link #Y}, {@link #Z} set.
     *
     * @see #Y
     * @see #GEOCENTRIC_Y
     * @see #NORTHING
     * @see #SOUTHING
     */
    public static final CoordinateSystemAxis Y =
                        new CoordinateSystemAxis("y", AxisDirection.NORTH, SI.METER);
    
    /**
     * Default axis info for <var>z</var> values in a {@linkplain CartesianCS cartesian CS}.
     * Increasing ordinates values go {@linkplain AxisDirection#UP up}
     * and units are {@linkplain SI#METER metres}.
     * The abbreviation is lower case "z". This axis is usually part of a
     * {@link #X}, {@link #Y}, {@link #Z} set.
     *
     * @see #Z
     * @see #GEOCENTRIC_Z
     */
    public static final CoordinateSystemAxis Z =
                        new CoordinateSystemAxis("z", AxisDirection.UP, SI.METER);
    
    /**
     * Default axis info for <var>x</var> values in a
     * {@linkplain org.geotools.referencing.crs.GeocentricCRS geocentric CRS} using
     * {@linkplain CartesianCS cartesian CS}.
     * Increasing ordinates values go {@linkplain AxisDirection#EAST East}
     * and units are {@linkplain SI#METER metres}.
     * The abbreviation is upper case "X". This axis is usually part of a
     * {@link #GEOCENTRIC_X}, {@link #GEOCENTRIC_Y}, {@link #GEOCENTRIC_Z} set.
     *
     * @see #X
     * @see #GEOCENTRIC_X
     * @see #EASTING
     * @see #WESTING
     */
    public static final CoordinateSystemAxis GEOCENTRIC_X =
                        new Localized("X", AxisDirection.EAST, SI.METER,
                                      ResourceKeys.GEOCENTRIC_X);
    
    /**
     * Default axis info for <var>y</var> values in a
     * {@linkplain org.geotools.referencing.crs.GeocentricCRS geocentric CRS} using
     * {@linkplain CartesianCS cartesian CS}.
     * Increasing ordinates values go {@linkplain AxisDirection#NORTH North}
     * and units are {@linkplain SI#METER metres}.
     * The abbreviation is upper case "Y". This axis is usually part of a
     * {@link #GEOCENTRIC_X}, {@link #GEOCENTRIC_Y}, {@link #GEOCENTRIC_Z} set.
     *
     * @see #Y
     * @see #GEOCENTRIC_Y
     * @see #NORTHING
     * @see #SOUTHING
     */
    public static final CoordinateSystemAxis GEOCENTRIC_Y =
                        new Localized("Y", AxisDirection.NORTH, SI.METER,
                                      ResourceKeys.GEOCENTRIC_Y);
    
    /**
     * Default axis info for <var>z</var> values in a
     * {@linkplain org.geotools.referencing.crs.GeocentricCRS geocentric CRS} using
     * {@linkplain CartesianCS cartesian CS}.
     * Increasing ordinates values go {@linkplain AxisDirection#UP up}
     * and units are {@linkplain SI#METER metres}.
     * The abbreviation is upper case "Z". This axis is usually part of a
     * {@link #GEOCENTRIC_X}, {@link #GEOCENTRIC_Y}, {@link #GEOCENTRIC_Z} set.
     *
     * @see #Z
     * @see #GEOCENTRIC_Z
     */
    public static final CoordinateSystemAxis GEOCENTRIC_Z =
                        new Localized("Z", AxisDirection.UP, SI.METER,
                                      ResourceKeys.GEOCENTRIC_Z);
    
    /**
     * Default axis info for Easting values in a
     * {@linkplain org.geotools.referencing.crs.ProjectedCRS projected CRS}.
     * Increasing ordinates values go {@linkplain AxisDirection#EAST East}
     * and units are {@linkplain SI#METER metres}.
     * The abbreviation is upper case "E". This axis is usually part of a
     * {@link #EASTING}, {@link #NORTHING} set.
     *
     * @see #X
     * @see #GEOCENTRIC_X
     * @see #EASTING
     * @see #WESTING
     */
    public static final CoordinateSystemAxis EASTING =
                        new Localized("E", AxisDirection.EAST, SI.METER,
                                      ResourceKeys.EASTING);
    
    /**
     * Default axis info for Westing values in a
     * {@linkplain org.geotools.referencing.crs.ProjectedCRS projected CRS}.
     * Increasing ordinates values go {@linkplain AxisDirection#WEST West}
     * and units are {@linkplain SI#METER metres}.
     * The abbreviation is upper case "W".
     *
     * @see #X
     * @see #GEOCENTRIC_X
     * @see #EASTING
     * @see #WESTING
     */
    public static final CoordinateSystemAxis WESTING =
                        new Localized("W", AxisDirection.WEST, SI.METER,
                                      ResourceKeys.WESTING);
    
    /**
     * Default axis info for Northing values in a
     * {@linkplain org.geotools.referencing.crs.ProjectedCRS projected CRS}.
     * Increasing ordinates values go {@linkplain AxisDirection#NORTH North}
     * and units are {@linkplain SI#METER metres}.
     * The abbreviation is upper case "N". This axis is usually part of a
     * {@link #EASTING}, {@link #NORTHING} set.
     *
     * @see #Y
     * @see #GEOCENTRIC_Y
     * @see #NORTHING
     * @see #SOUTHING
     */
    public static final CoordinateSystemAxis NORTHING =
                        new Localized("N", AxisDirection.NORTH, SI.METER,
                                      ResourceKeys.NORTHING);
    
    /**
     * Default axis info for Southing values in a
     * {@linkplain org.geotools.referencing.crs.ProjectedCRS projected CRS}.
     * Increasing ordinates values go {@linkplain AxisDirection#SOUTH South}
     * and units are {@linkplain SI#METER metres}.
     * The abbreviation is upper case "S".
     *
     * @see #Y
     * @see #GEOCENTRIC_Y
     * @see #NORTHING
     * @see #SOUTHING
     */
    public static final CoordinateSystemAxis SOUTHING =
                        new Localized("S", AxisDirection.SOUTH, SI.METER,
                                      ResourceKeys.SOUTHING);
    
    /**
     * A default axis for time values in a {@linkplain TemporalCS temporal CS}.
     * Increasing time go toward {@linkplain AxisDirection#FUTURE future}
     * and units are {@linkplain NonSI#DAY days}.
     * The abbreviation is lower case "t".
     */
    public static final CoordinateSystemAxis TIME =
                        new Localized("t", AxisDirection.FUTURE, NonSI.DAY, ResourceKeys.TIME);

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
     * Construct an axis with the same {@linkplain #getName name} than the abbreviation.
     *
     * @param abbreviation The {@linkplain #getAbbreviation abbreviation} used for this
     *                     coordinate system axes.
     * @param direction    The {@linkplain #getDirection direction} of this coordinate system axis.
     * @param unit         The {@linkplain #getUnit unit of measure} used for this coordinate
     *                     system axis.
     */
    public CoordinateSystemAxis(final String        abbreviation,
                                final AxisDirection direction,
                                final Unit          unit)
    {
        this(Collections.singletonMap("name", abbreviation), abbreviation, direction, unit);
    }

    /**
     * Construct an axis from a set of properties. The properties map is given unchanged
     * to the {@linkplain IdentifiedObject#IdentifiedObject(Map) super-class constructor}.
     *
     * @param properties   Set of properties. Should contains at least <code>"name"</code>.
     * @param abbreviation The {@linkplain #getAbbreviation abbreviation} used for this
     *                     coordinate system axes.
     * @param direction    The {@linkplain #getDirection direction} of this coordinate system axis.
     * @param unit         The {@linkplain #getUnit unit of measure} used for this coordinate
     *                     system axis.
     */
    public CoordinateSystemAxis(final Map           properties,
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
     * Within any set of coordinate system axes, only one of each pair of terms
     * can be used. For earth-fixed coordinate reference systems, this direction is often
     * approximate and intended to provide a human interpretable meaning to the axis. When a
     * geodetic datum is used, the precise directions of the axes may therefore vary slightly
     * from this approximate direction.
     *
     * Note that an {@link org.geotools.referencing.crs.EngineeringCRS} often requires
     * specific descriptions of the directions of its coordinate system axes.
     *
     * @return The coordinate system axis direction.
     */
    public AxisDirection getDirection() {
        return direction;
    }

    /**
     * The unit of measure used for this coordinate system axis. The value of this
     * coordinate in a coordinate tuple shall be recorded using this unit of measure,
     * whenever those coordinates use a coordinate reference system that uses a
     * coordinate system that uses this axis.
     *
     * @return  The coordinate system axis unit.
     */
    public Unit getUnit() {
        return unit;
    }
    
    /**
     * Compares the specified object with this axis for equality.
     *
     * @param  object The object to compare to <code>this</code>.
     * @param  compareMetadata <code>true</code> for performing a strict comparaison, or
     *         <code>false</code> for comparing only properties relevant to transformations.
     * @return <code>true</code> if both objects are equal.
     */
    public boolean equals(final IdentifiedObject object, final boolean compareMetadata) {
        if (object == this) {
            return true; // Slight optimization.
        }
        if (super.equals(object, compareMetadata)) {
            final CoordinateSystemAxis that = (CoordinateSystemAxis) object;
            return equals(this.abbreviation, that.abbreviation) &&
                   equals(this.direction,    that.direction)    &&
                   equals(this.unit,         that.unit);
        }
        return false;
    }
    
    /**
     * Returns a hash value for this axis.
     *
     * @return The hash code value. This value doesn't need to be the same
     *         in past or future versions of this class.
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
    
    /**
     * Localized {@link CoordinateSystemAxis}.
     * Used for providing localized version of {@link #getName}.
     *
     * @author Martin Desruisseaux
     * @version $Id$
     */
    private static final class Localized extends CoordinateSystemAxis {
        /**
         * Serial number for interoperability with different versions.
         */
        private static final long serialVersionUID = 3447702143250807839L;
        
        /**
         * The key for localization.
         */
        private final int key;
        
        /**
         * Constructs a localized axis.
         */
        public Localized(final String        abbreviation,
                         final AxisDirection direction,
                         final Unit          unit,
                         final int           key)
        {
            super(abbreviation, direction, unit);
            this.key = key;
        }
        
        /**
         * Returns a localized string. If <code>locale</code> is <code>null</code>, then the
         * {@linkplain Locale#ENGLISH English} locale is used in order to allows proper WKT
         * formatting. English resources should always been available for this implementation.
         */
        public String getName(Locale locale) {
            if (locale == null) {
                locale = Locale.ENGLISH;
            }
            return Resources.getResources(locale).getString(key);
        }
    
        /**
         * Compares the specified object with this axis for equality.
         *
         * @param  object The object to compare to <code>this</code>.
         * @param  compareMetadata <code>true</code> for performing a strict comparaison, or
         *         <code>false</code> for comparing only properties relevant to transformations.
         * @return <code>true</code> if both objects are equal.
         */
        public boolean equals(final IdentifiedObject object, final boolean compareMetadata) {
            if (super.equals(object, compareMetadata)) {
                // Always compare the key, since we use it for differentiating axis
                // with the same abbreviation (e.g. LATITUDE and GEODETIC_LATITUDE).
                final Localized that = (Localized) object;
                return this.key == that.key;
            }
            return false;
        }
    }
}
