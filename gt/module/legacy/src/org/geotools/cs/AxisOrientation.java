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

// OpenGIS dependencies
import java.io.ObjectStreamException;
import java.util.Locale;
import java.util.NoSuchElementException;

import javax.media.jai.EnumeratedParameter;

import org.geotools.resources.i18n.VocabularyKeys;
import org.geotools.resources.i18n.Vocabulary;
import org.opengis.cs.CS_AxisOrientationEnum;
import org.opengis.referencing.cs.AxisDirection;


/**
 * Orientation of axis. Some coordinate systems use non-standard orientations.
 * For example, the first axis in South African grids usually points West,
 * instead of East. This information is obviously relevant for algorithms
 * converting South African grid coordinates into Lat/Long.
 * <br><br>
 * The <em>natural ordering</em> for axis orientations is defined
 * as (EAST-WEST), (NORTH-SOUTH), (UP-DOWN), (FUTURE-PAST) and OTHER, which is
 * the ordering for a (<var>x</var>,<var>y</var>,<var>z</var>,<var>t</var>)
 * coordinate system. This means that when an array of <code>AxisOrientation</code>s
 * is sorted using {@link java.util.Arrays#sort(Object[])}, EAST and WEST
 * orientations will appear first. NORTH and SOUTH will be next, followed
 * by UP and DOWN, etc.
 *
 * Care should be exercised if <code>AxisOrientation</code>s are to be used as
 * keys in a sorted map or elements in a sorted set, as
 * <code>AxisOrientation</code>'s natural ordering is inconsistent with equals.
 * See {@link java.lang.Comparable}, {@link java.util.SortedMap} or
 * {@link java.util.SortedSet} for more information.
 *
 * @source $URL$
 * @version $Id$
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 *
 * @see org.opengis.cs.CS_AxisOrientationEnum
 *
 * @deprecated Replaced by {@link org.opengis.referencing.cs.AxisDirection}.
 */
public final class AxisOrientation extends EnumeratedParameter implements Comparable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 4649182002820021468L;
    
    // NOTE: The following enum values are from the OpenGIS specification.
    //       IF THOSE VALUES CHANGE, THEN inverse() AND absolute() MUST BE
    //       UPDATED.
    
    /**
     * Unknown or unspecified axis orientation.
     * This can be used for local or fitted coordinate systems.
     *
     * @see org.opengis.cs.CS_AxisOrientationEnum#CS_AO_Other
     *
     * @deprecated Replaced by {@link org.opengis.referencing.cs.AxisDirection#OTHER}.
     */
    public static final AxisOrientation OTHER = new AxisOrientation("OTHER",
        CS_AxisOrientationEnum.CS_AO_Other, VocabularyKeys.OTHER, AxisDirection.OTHER);
    
    /**
     * Increasing ordinates values go North.
     * This is usually used for Grid Y coordinates and Latitude.
     *
     * @see org.opengis.cs.CS_AxisOrientationEnum#CS_AO_North
     *
     * @deprecated Replaced by {@link org.opengis.referencing.cs.AxisDirection#NORTH}.
     */
    public static final AxisOrientation NORTH = new AxisOrientation("NORTH",
            CS_AxisOrientationEnum.CS_AO_North, VocabularyKeys.NORTH, AxisDirection.NORTH);
    
    /**
     * Increasing ordinates values go South.
     *
     * @see org.opengis.cs.CS_AxisOrientationEnum#CS_AO_South
     *
     * @deprecated Replaced by {@link org.opengis.referencing.cs.AxisDirection#SOUTH}.
     */
    public static final AxisOrientation SOUTH = new AxisOrientation("SOUTH",
            CS_AxisOrientationEnum.CS_AO_South, VocabularyKeys.SOUTH, AxisDirection.SOUTH);
    
    /**
     * Increasing ordinates values go East.
     * This is usually used for Grid X coordinates and Longitude.
     *
     * @see org.opengis.cs.CS_AxisOrientationEnum#CS_AO_East
     *
     * @deprecated Replaced by {@link org.opengis.referencing.cs.AxisDirection#EAST}.
     */
    public static final AxisOrientation EAST = new AxisOrientation("EAST",
            CS_AxisOrientationEnum.CS_AO_East, VocabularyKeys.EAST, AxisDirection.EAST);
    
    /**
     * Increasing ordinates values go West.
     *
     * @see org.opengis.cs.CS_AxisOrientationEnum#CS_AO_West
     *
     * @deprecated Replaced by {@link org.opengis.referencing.cs.AxisDirection#WEST}.
     */
    public static final AxisOrientation WEST = new AxisOrientation("WEST",
            CS_AxisOrientationEnum.CS_AO_West, VocabularyKeys.WEST, AxisDirection.WEST);
    
    /**
     * Increasing ordinates values go up.
     * This is used for vertical coordinate systems.
     *
     * @see org.opengis.cs.CS_AxisOrientationEnum#CS_AO_Up
     *
     * @deprecated Replaced by {@link org.opengis.referencing.cs.AxisDirection#UP}.
     */
    public static final AxisOrientation UP = new AxisOrientation("UP",
            CS_AxisOrientationEnum.CS_AO_Up, VocabularyKeys.UP, AxisDirection.UP);
    
    /**
     * Increasing ordinates values go down.
     * This is used for vertical coordinate systems.
     *
     * @see org.opengis.cs.CS_AxisOrientationEnum#CS_AO_Down
     *
     * @deprecated Replaced by {@link org.opengis.referencing.cs.AxisDirection#DOWN}.
     */
    public static final AxisOrientation DOWN = new AxisOrientation("DOWN",
            CS_AxisOrientationEnum.CS_AO_Down, VocabularyKeys.DOWN, AxisDirection.DOWN);
    
    /**
     * Increasing time go toward future.
     * This is used for temporal axis.
     *
     * @deprecated Replaced by {@link org.opengis.referencing.cs.AxisDirection#FUTURE}.
     */
    public static final AxisOrientation FUTURE = new AxisOrientation("FUTURE",
            7, VocabularyKeys.FUTURE, AxisDirection.FUTURE);
    
    /**
     * Increasing time go toward past.
     * This is used for temporal axis.
     *
     * @deprecated Replaced by {@link org.opengis.referencing.cs.AxisDirection#PAST}.
     */
    public static final AxisOrientation PAST = new AxisOrientation("PAST",
            8, VocabularyKeys.PAST, AxisDirection.PAST);
    
    /**
     * The last paired value. Paired values are NORTH-SOUTH, EAST-WEST,
     * UP-DOWN, FUTURE-PAST.
     */
    private static final int LAST_PAIRED_VALUE = 8;
    
    /**
     * Axis orientations by value. Used to
     * canonicalize after deserialization.
     */
    private static final AxisOrientation[] ENUMS = {OTHER,NORTH,SOUTH,EAST,WEST,UP,DOWN,FUTURE,PAST};
    static {
        for (int i=0; i<ENUMS.length; i++) {
            if (ENUMS[i].getValue()!=i) {
                throw new AssertionError(ENUMS[i]);
            }
        }
    }
    
    /**
     * The axis order. Used for {@link #compareTo} implementation.
     */
    private static final AxisOrientation[] ORDER = {EAST, NORTH, UP, FUTURE};

    /**
     * The direction according new (GeoAPI) constants, or <code>null</code> if unknow.
     * This field is provided for interoperability with new GeoAPI interfaces.
     */
    public final AxisDirection direction;
    
    /**
     * Resource key, used for building localized name. This key doesn't need to
     * be serialized, since {@link #readResolve} canonicalizes enums according
     * to their {@link #value}. Furthermore, its value is
     * implementation-dependent (which is another raison why it should not be
     * serialized).
     */
    private transient final int key;
    
    /**
     * Constructs a new enum with the specified value.
     */
    private AxisOrientation(final String name, final int value, final int key, final AxisDirection direction) {
        super(name, value);
        this.key       = key;
        this.direction = direction;
    }
    
    /**
     * Returns the enum for the specified value.
     * This method is provided for compatibility with
     * {@link org.opengis.cs.CS_AxisOrientationEnum}.
     *
     * @param value The enum value.
     * @return The enum for the specified value.
     * @throws NoSuchElementException if there is no enum for the specified value.
     */
    public static AxisOrientation getEnum(final int value) throws NoSuchElementException {
        if (value>=0 && value<ENUMS.length) return ENUMS[value];
        throw new NoSuchElementException(String.valueOf(value));
    }

    /**
     * Returns the enum for the specified name.
     * Search is case and locale insensitive.
     *
     * @param name One of the constant values ({@link #NORTH}, {@link #SOUTH}, etc.)
     * @return The enum for the specified name.
     * @throws NoSuchElementException if there is no enum for the specified name.
     */
    public static AxisOrientation getEnum(String name) {
        name = name.trim();
        for (int i=0; i<ENUMS.length; i++) {
            final AxisOrientation candidate = ENUMS[i];
            if (name.equalsIgnoreCase(candidate.getName())) {
                return candidate;
            }
        }
        throw new NoSuchElementException(name);
    }

    /**
     * Returns the enum for the specified localized name.
     * Search is case-insensitive.
     *
     * @param name The localized name (e.g. "Nord", "Sud", "Est", "Ouest", etc.)
     * @param locale The locale, or <code>null</code> for the default locale.
     * @return The enum for the specified localized name.
     * @throws NoSuchElementException if there is no enum for the specified name.
     */
    public static AxisOrientation getEnum(String name, final Locale locale) {
        name = name.trim();
        final Vocabulary resources = Vocabulary.getResources(locale);
        for (int i=0; i<ENUMS.length; i++) {
            final AxisOrientation candidate = ENUMS[i];
            if (name.equalsIgnoreCase(resources.getString(candidate.key))) {
                return candidate;
            }
        }
        throw new NoSuchElementException(name);
    }
    
    /**
     * Returns this enum's name in the specified locale.
     * If no name is available for the specified locale, a default one will
     * be used.
     *
     * @param locale The locale, or <code>null</code> for the default locale.
     * @return Enum's name in the specified locale.
     */
    public String getName(final Locale locale) {
        return Vocabulary.getResources(locale).getString(key);
    }
    
    /**
     * Returns the opposite orientation of this axis.
     * The opposite of North is South, and the opposite of South is North.
     * The same applies to East-West, Up-Down and Future-Past.
     * Other axis orientations are returned unchanged.
     */
    public AxisOrientation inverse() {
        final int value=getValue()-1;
        if (value>=0 && value<LAST_PAIRED_VALUE) {
            return ENUMS[(value ^ 1)+1];
        } else {
            return this;
        }
    }
    
    /**
     * Returns the "absolute" orientation of this axis.
     * This "absolute" operation is similar to the <code>Math.abs(int)</code>
     * method in that "negative" orientations (<code>SOUTH</code>,
     * <code>WEST</code>, <code>DOWN</code>, <code>PAST</code>) are changed
     * for their positive counterparts (<code>NORTH</code>, <code>EAST</code>,
     * <code>UP</code>, <code>FUTURE</code>). More specifically, the
     * following conversion table is applied.
     * <br>&nbsp;
     * <table align="center" cellpadding="3" border="1" bgcolor="F4F8FF">
     *   <tr bgcolor="#B9DCFF">
     *     <th>&nbsp;&nbsp;Orientation&nbsp;&nbsp;</th>
     *     <th>&nbsp;&nbsp;Absolute value&nbsp;&nbsp;</th>
     *   </tr>
     *   <tr align="center"><td>NORTH</td> <td>NORTH</td> </tr>
     *   <tr align="center"><td>SOUTH</td> <td>NORTH</td> </tr>
     *   <tr align="center"><td>EAST</td>  <td>EAST</td>  </tr>
     *   <tr align="center"><td>WEST</td>  <td>EAST</td>  </tr>
     *   <tr align="center"><td>UP</td>    <td>UP</td>    </tr>
     *   <tr align="center"><td>DOWN</td>  <td>UP</td>    </tr>
     *   <tr align="center"><td>FUTURE</td><td>FUTURE</td></tr>
     *   <tr align="center"><td>PAST</td>  <td>FUTURE</td></tr>
     *   <tr align="center"><td>OTHER</td> <td>OTHER</td> </tr>
     * </table>
     */
    public AxisOrientation absolute() {
        final int value=getValue()-1;
        if (value>=0 && value<LAST_PAIRED_VALUE) {
            return ENUMS[(value & ~1)+1];
        } else {
            return this;
        }
    }
    
    /**
     * Compares this <code>AxisOrientation</code> with the specified
     * orientation.  The <em>natural ordering</em> is defined as
     * (EAST-WEST), (NORTH-SOUTH), (UP-DOWN), (FUTURE-PAST) and OTHER,
     * which is the ordering for a
     * (<var>x</var>,<var>y</var>,<var>z</var>,<var>t</var>) coordinate system.
     * Two <code>AxisOrientation</code>s that are along the same axis but with
     * an opposite direction (e.g. EAST vs WEST) are considered equal by this
     * method.
     *
     * @param  ao An <code>AxisOrientation</code> object to be compared with.
     * @throws ClassCastException if <code>ao</code> is not an
     *         <code>AxisOrientation</code> object.
     */
    public int compareTo(final Object ao) {
        final AxisOrientation that = (AxisOrientation)ao;
        final int thisOrder = this.absolute().getOrder();
        final int thatOrder = that.absolute().getOrder();
        if (thisOrder > thatOrder) return +1;
        if (thisOrder < thatOrder) return -1;
        return 0;
    }
    
    /**
     * Returns the order for this axis orientation
     * (i.e. the index in the {@link #ORDER} table).
     */
    private int getOrder() {
        int i;
        for (i=0; i<ORDER.length; i++) {
            if (equals(ORDER[i])) {
                break;
            }
        }
        return i;
    }
    
    /**
     * Uses a single instance of {@link AxisOrientation} after deserialization.
     * It allows client code to test <code>enum1==enum2</code> instead of
     * <code>enum1.equals(enum2)</code>.
     *
     * @return A single instance of this enum.
     * @throws ObjectStreamException if deserialization failed.
     */
    private Object readResolve() throws ObjectStreamException {
        final int value = getValue();
        if (value>=0 && value<ENUMS.length) {
            // Canonicalize
            return ENUMS[value];
        } else {
            // Collapse unknown value to a single canonical one
            return ENUMS[0]; 
        }
    }
}
