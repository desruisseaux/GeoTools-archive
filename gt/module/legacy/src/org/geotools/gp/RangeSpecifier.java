/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2001, Institut de Recherche pour le D�veloppement
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
 */
package org.geotools.gp;

// J2SE dependencies
import java.awt.Color;
import java.io.Serializable;
import java.util.Arrays;

import org.geotools.ct.MathTransform1D;
import org.geotools.cv.Category;
import org.geotools.gc.GridCoverage;
import org.geotools.resources.Utilities;
import org.geotools.units.Unit;
import org.geotools.util.NumberRange;
import org.opengis.util.Cloneable;


/**
 * Argument type for {@link GridCoverageProcessor} operations for specifying the range,
 * colors and units of a computation result. <code>RangeSpecifier</code> are used for
 * tuning the {@link Category} object to be constructed. For example the "GradientMagnitude"
 * operation will produces new {@link GridCoverage} with sample values ranging from 0 to some
 * maximal value which may be very different from the source {@link GridCoverage} range. By
 * default, most {@linkplain Operation operations} try to guess a raisonable range for output
 * values. This default behavior can be overriden with an explicit <code>RangeSpecifier</code>
 * argument.
 * <br><br>
 * All <code>RangeSpecifier</code>'s properties are optional; it is up to processor's
 * {@linkplain Operation operation} to replace <code>null</code> values by a default
 * one. <code>RangeSpecifier</code> argument is used by the
 *
 * "GradientMagnitude"
 *
 * operation.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class RangeSpecifier implements Serializable, Cloneable {
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = 8436500582161136302L;

    /**
     * The target range, or <code>null</code> if none.
     */
    private NumberRange range;

    /**
     * The target "sample to geophysics" transform, or <code>null</code> if none.
     */
    private MathTransform1D transform;

    /**
     * The target range units, or <code>null</code> if none.
     */
    private Unit unit;

    /**
     * The target colors, or <code>null</code> if none.
     */
    private Color[] colors;

    /**
     * Construct a default <code>RangeSpecifier</code> with no value set.
     */
    public RangeSpecifier() {
    }

    /**
     * Construct a <code>RangeSpecifier</code> initialised to the sp�cified range.
     */
    public RangeSpecifier(final NumberRange range) {
        this.range = range;
    }

    /**
     * Construct a <code>RangeSpecifier</code> initialised to the specified
     * "sample to geophysics" transform.
     */
    public RangeSpecifier(final MathTransform1D transform) {
        this.transform = transform;
    }

    /**
     * Returns the target range, or <code>null</code> if none.
     */
    public NumberRange getRange() {
        return range;
    }

    /**
     * Set the target range to the specified values. Setting this property will clear
     * the {@linkplain #getSampleToGeophysics sample to geophysics transform}, since
     * those properties are mutually exclusive.
     *
     * @param range The target range.
     */
    public void setRange(final NumberRange range) {
        this.range = range;
        transform  = null;
    }

    /**
     * Returns the target "sample to geophysics" transform, or <code>null</code> if none.
     */
    public MathTransform1D getSampleToGeophysics() {
        return transform;
    }

    /**
     * Set the target "sample to geophysics" transform to the specified value.
     * Setting this property will clear the {@linkplain #getRange range} property,
     * since those properties are mutually exclusive.
     */
    public void setSampleToGeophysics(final MathTransform1D transform) {
        this.transform = transform;
        range = null;
    }

    /**
     * Returns the target range units, or <code>null</code> if none.
     */
    public Unit getUnit() {
        return unit;
    }

    /**
     * Set the target range units to the specified value.
     */
    public void setUnit(final Unit unit) {
        this.unit = unit;
    }

    /**
     * Returns the target colors, or <code>null</code> if none.
     */
    public Color[] getColors() {
        return (colors!=null) ? (Color[])colors.clone() : null;
    }

    /**
     * Set the target colors to the specified value.
     */
    public void setColors(final Color[] colors) {
        this.colors = (colors!=null) ? (Color[])colors.clone() : null;
    }

    /**
     * Returns a clone of this object.
     */
    public Object clone() {
        try {
             return super.clone();
        } catch (CloneNotSupportedException exception) {
            // Should not happen, since we are cloneable.
            throw new AssertionError(exception);
        }
    }

    /**
     * Returns a hash code value for this range specifier.
     */
    public int hashCode() {
        int code = (int)serialVersionUID;
        if (range != null) {
            code += range.hashCode();
        }
        if (transform != null) {
            code += transform.hashCode();
        }
        return code;
    }

    /**
     * Compare this range specifier with the specified object for equality.
     */
    public boolean equals(final Object object) {
        if (object!=null && object.getClass().equals(getClass())) {
            final RangeSpecifier that = (RangeSpecifier) object;
            return Utilities.equals(this.range,     that.range    ) &&
                   Utilities.equals(this.transform, that.transform) &&
                   Utilities.equals(this.unit,      that.unit     ) &&
                      Arrays.equals(this.colors,    that.colors   );
        }
        return false;
    }

    /**
     * Returns a string representation of this range specifier.
     */
    public String toString() {
        final StringBuffer buffer = new StringBuffer(Utilities.getShortClassName(this));
        buffer.append('[');
        if (range != null) {
            buffer.append(range);
        } else if (transform != null) {
            buffer.append(transform);
        }
        buffer.append(']');
        return buffer.toString();
    }
}
