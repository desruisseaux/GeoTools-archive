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
import java.util.Collections;

// OpenGIS dependencies
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystemAxis;

// Geotools dependencies
import org.geotools.referencing.Info;
import org.geotools.referencing.wkt.Formatter;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * The set of coordinate system axes that spans a given coordinate space. A coordinate system (CS)
 * is derived from a set of (mathematical) rules for specifying how coordinates in a given space
 * are to be assigned to points. The coordinate values in a coordinate tuple shall be recorded in
 * the order in which the coordinate system axes are recorded, whenever those
 * coordinates use a coordinate reference system that uses this coordinate system.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see CoordinateSystemAxis
 * @see javax.units.Unit
 * @see org.geotools.referencing.datum.Datum
 * @see org.geotools.referencing.crs.CoordinateReferenceSystem
 */
public class CoordinateSystem extends Info implements org.opengis.referencing.cs.CoordinateSystem {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 6757665252533744744L;
    
    /**
     * The axis for this coordinate system at the specified dimension.
     */
    private final CoordinateSystemAxis[] axis;

    /**
     * Construct a coordinate system from a name.
     *
     * @param name  The coordinate system name.
     * @param axis  The set of axis.
     */
    public CoordinateSystem(final String name, final CoordinateSystemAxis[] axis) {
        this(Collections.singletonMap("name", name), axis);
    }

    /**
     * Construct a coordinate system from a set of properties.
     * The properties map is given unchanged to the superclass constructor.
     *
     * @param properties   Set of properties. Should contains at least <code>"name"</code>.
     * @param axis         The set of axis.
     */
    public CoordinateSystem(final Map properties, final CoordinateSystemAxis[] axis) {
        super(properties);
        ensureNonNull("axis", axis);
        this.axis = (CoordinateSystemAxis[]) axis.clone();
        /*
         * Makes sure there is no axis along the same direction
         * (e.g. two North axis, or an East and a West axis).
         */
        for (int i=0; i<axis.length; i++) {
            ensureNonNull("axis", axis, i);
            AxisDirection check = axis[i].getDirection();
            ensureNonNull("direction", check);
            if (!isCompatibleDirection(check)) {
                // TOOD: localize name()
                throw new IllegalArgumentException(Resources.format(
                            ResourceKeys.ERROR_ILLEGAL_AXIS_ORIENTATION_$2,
                            check.name(), Utilities.getShortClassName(this)));
            }
            check = check.absolute();
            if (!check.equals(AxisDirection.OTHER)) {
                for (int j=i; --j>=0;) {
                    if (check.equals(axis[j].getDirection().absolute())) {
                        // TODO: localize name()
                        final String nameI = axis[i].getDirection().name();
                        final String nameJ = axis[j].getDirection().name();
                        throw new IllegalArgumentException(Resources.format(
                                    ResourceKeys.ERROR_COLINEAR_AXIS_$2, nameI, nameJ));
                    }
                }
            }
        }
    }

    /**
     * Returns <code>true</code> if the specified axis direction is allowed for this coordinate
     * system. This method is invoked at construction time for checking argument validity. The
     * default implementation returns <code>true</code> for all axis directions. Subclass will
     * overrides this method in order to put more restrictions on allowed axis directions.
     */
    protected boolean isCompatibleDirection(final AxisDirection direction) {
        return true;
    }

    /**
     * Returns the dimension of the coordinate system.
     * This is the number of axis.
     *
     * @return The dimension of the coordinate system.
     */
    public int getDimension() {
        return axis.length;
    }

    /**
     * Returns the axis for this coordinate system at the specified dimension.
     *
     * @param  dimension The zero based index of axis.
     * @return The axis at the specified dimension.
     * @throws IndexOutOfBoundsException if <code>dimension</code> is out of bounds.
     */
    public CoordinateSystemAxis getAxis(final int dimension) throws IndexOutOfBoundsException {
        return axis[dimension];
    }
    
    /**
     * Compares the specified object with this coordinate system for equality.
     *
     * @param  object The object to compare to <code>this</code>.
     * @param  compareMetadata <code>true</code> for performing a strict comparaison, or
     *         <code>false</code> for comparing only properties relevant to transformations.
     * @return <code>true</code> if both objects are equal.
     */
    public boolean equals(final Info object, final boolean compareMetadata) {
        if (object == this) {
            return true; // Slight optimization.
        }
        if (super.equals(object, compareMetadata)) {
            final CoordinateSystem that = (CoordinateSystem) object;
            return equals(this.axis, that.axis, compareMetadata);
        }
        return false;
    }
    
    /**
     * Returns a hash value for this coordinate system.
     *
     * @return The hash code value. This value doesn't need to be the same
     *         in past or future versions of this class.
     */
    public int hashCode() {
        int code = (int)serialVersionUID;
        for (int i=0; i<axis.length; i++) {
            code = code*37 + axis[i].hashCode();
        }
        return code;
    }
    
    /**
     * Format the inner part of a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> (WKT)</A> element. Note that WKT is not yet defined for coordinate system.
     * Current implementation list the axis contained in this CS.
     *
     * @param  formatter The formatter to use.
     * @return The WKT element name. Current implementation default to the class name.
     */
    protected String formatWKT(final Formatter formatter) {
        for (int i=0; i<axis.length; i++) {
            formatter.append(axis[i]);
        }
        formatter.setInvalidWKT();
        return super.formatWKT(formatter);
    }
}
