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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.cs;

// J2SE dependencies
import java.util.Locale;
import java.io.Serializable;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;

// OpenGIS dependencies
import org.opengis.referencing.Identifier;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystemAxis;


/**
 * Details of axis. This is used to label axes,
 * and indicate the orientation.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see org.opengis.cs.CS_AxisInfo
 *
 * @deprecated Replaced by {@link org.geotools.referencing.cs.CoordinateSystemAxis}.
 */
public class AxisInfo implements CoordinateSystemAxis, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 6799874182734710227L;
    
    /**
     * Default axis info for <var>x</var> values.
     * Increasing ordinates values go East. This
     * is usually used with projected coordinate
     * systems.
     *
     * @deprecated Replaced by {@link org.geotools.referencing.cs.CoordinateSystemAxis#X}.
     */
    public static final AxisInfo X = new AxisInfo("x", AxisOrientation.EAST);
    
    /**
     * Default axis info for <var>y</var> values.
     * Increasing ordinates values go North. This
     * is usually used with projected coordinate
     * systems.
     *
     * @deprecated Replaced by {@link org.geotools.referencing.cs.CoordinateSystemAxis#Y}.
     */
    public static final AxisInfo Y = new AxisInfo("y", AxisOrientation.NORTH);
    
    /**
     * Default axis info for longitudes.
     * Increasing ordinates values go East.
     * This is usually used with geographic
     * coordinate systems.
     *
     * @deprecated Replaced by {@link org.geotools.referencing.cs.CoordinateSystemAxis#LONGITUDE}.
     */
    public static final AxisInfo LONGITUDE = new AxisInfo.Localized("Longitude", ResourceKeys.LONGITUDE, AxisOrientation.EAST);
    
    /**
     * Default axis info for latitudes.
     * Increasing ordinates values go North.
     * This is usually used with geographic
     * coordinate systems.
     *
     * @deprecated Replaced by {@link org.geotools.referencing.cs.CoordinateSystemAxis#LATITUDE}.
     */
    public static final AxisInfo LATITUDE = new AxisInfo.Localized("Latitude", ResourceKeys.LATITUDE, AxisOrientation.NORTH);
    
    /**
     * The default axis for altitude values.
     * Increasing ordinates values go up.
     *
     * @deprecated Replaced by {@link org.geotools.referencing.cs.CoordinateSystemAxis#ALTITUDE}.
     */
    public static final AxisInfo ALTITUDE = new AxisInfo.Localized("Altitude", ResourceKeys.ALTITUDE, AxisOrientation.UP);
    
    /**
     * A default axis for time values.
     * Increasing time go toward future.
     *
     * @deprecated Replaced by {@link org.geotools.referencing.cs.CoordinateSystemAxis#TIME}.
     */
    public static final AxisInfo TIME = new AxisInfo.Localized("Time", ResourceKeys.TIME, AxisOrientation.FUTURE);
    
    /**
     * Human readable name for axis.
     * Possible values are <code>X</code>, <code>Y</code>,
     * <code>Long</code>, <code>Lat</code> or any other
     * short string.
     *
     * @see org.opengis.cs.CS_AxisInfo#name
     *
     * @deprecated Replaced by {@link org.geotools.referencing.cs.CoordinateSystemAxis#getName}.
     */
    public final String name;
    
    /**
     * Enumerated value for orientation.
     *
     * @see org.opengis.cs.CS_AxisInfo#orientation
     *
     * @deprecated Replaced by {@link org.geotools.referencing.cs.CoordinateSystemAxis#getDirection}.
     */
    public final AxisOrientation orientation;

    /** For compatibility with GeoAPI interfaces. */
    private final Unit unit;
    
    /**
     * Construct an AxisInfo.
     *
     * @param name The axis name. Possible values are <code>X</code>,
     *             <code>Y</code>, <code>Long</code>, <code>Lat</code>
     *             or any other short string.
     * @param orientation The axis orientation.
     */
    public AxisInfo(final String name, final AxisOrientation orientation) {
        this.name        = name;
        this.orientation = orientation;
        this.unit        = null;
        Info.ensureNonNull("name",        name);
        Info.ensureNonNull("orientation", orientation);
    }
    
    /**
     * Construct an AxisInfo.
     */
    AxisInfo(final AxisInfo toCopy, final Unit unit) {
        name        = toCopy.name;
        orientation = toCopy.orientation;
        this.unit   = unit;
    }
    
    /**
     * Returns the localized name of this axis.
     * The default implementation returns {@link #name}.
     *
     * @param locale The locale, or <code>null</code> for the default locale.
     * @return The localized string.
     */
    public String getName(final Locale locale) {
        return name;
    }
    
    /**
     * Returns a hash value for this axis.
     *
     * @return The hash code value. This value doesn't need to be the same
     *         in past or future versions of this class.
     */
    public int hashCode() {
        int code = (int)serialVersionUID;
        if (orientation!=null) code = code*37 + orientation.hashCode();
        if (name       !=null) code = code*37 + name.hashCode();
        return code;
    }
    
    /**
     * Compares the specified object
     * with this axis for equality.
     */
    public boolean equals(final Object object) {
        if (object!=null && object.getClass().equals(getClass())) {
            final AxisInfo that = (AxisInfo) object;
            return Utilities.equals(this.orientation, that.orientation) &&
                   Utilities.equals(this.name       , that.name);
        } else {
            return false;
        }
    }
    
    /**
     * Returns the Well Known Text (WKT) for this axis.
     * The WKT is part of OpenGIS's specification and
     * looks like <code>AXIS["name",NORTH]</code>.
     */
    public String toString() {
        final StringBuffer buffer=new StringBuffer("AXIS[\"");
        buffer.append(name);
        buffer.append('"');
        if (orientation!=null) {
            buffer.append(',');
            buffer.append(orientation.getName());
        }
        buffer.append(']');
        return buffer.toString();
    }

    /** For compatibility with GeoAPI interfaces. */
    public String getAbbreviation() {
        return name;
    }
    
    /** For compatibility with GeoAPI interfaces. */
    public AxisDirection getDirection() {
        return orientation.direction;
    }
    
    /** For compatibility with GeoAPI interfaces. */
    public Identifier[] getIdentifiers() {
        return Info.EMPTY_IDENTIFIERS;
    }
    
    /** For compatibility with GeoAPI interfaces. */
    public String getRemarks(Locale locale) {
        return null;
    }
    
    /** For compatibility with GeoAPI interfaces. */
    public javax.units.Unit getUnit() {
        if (unit != null) {
            return unit.toJSR108();
        }
        throw new UnsupportedOperationException();
    }
    
    /**
     * Localized {@link AxisInfo}.
     */
    private static final class Localized extends AxisInfo {
        /**
         * Serial number for interoperability with different versions.
         */
        private static final long serialVersionUID = 7625005531024599865L;
        
        /**
         * The key for localization.
         */
        private final int key;
        
        /**
         * Constructs a localized axis info.
         */
        public Localized(final String name, final int key, final AxisOrientation orientation) {
            super(name, orientation);
            this.key = key;
        }
        
        /**
         * Returns a localized string.
         */
        public String getName(final Locale locale) {
            return Resources.getResources(locale).getString(key);
        }
    }
}
