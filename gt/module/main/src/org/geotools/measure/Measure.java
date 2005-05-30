/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
 * (C) 1999, Fisheries and Oceans Canada
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
package org.geotools.measure;

// Miscellaneous
import javax.units.Unit;

import org.geotools.resources.Utilities;


/**
 * A scalar with an unit.
 *
 * @author Martin Desruisseaux
 *
 * @since 2.1
 */
public final class Measure extends Number {
    /**
     * For compatibility with different versions.
     */
    private static final long serialVersionUID = 6917234039472328164L;

    /**
     * The scalar value.
     */
    private final double value;

    /**
     * The unit.
     */
    private final Unit unit;

    /**
     * Creates a new measure with the specified value and unit.
     */
    public Measure(final double value, final Unit unit) {
        this.value = value;
        this.unit  = unit;
    }

    /** Returns the scalar value. */ public double doubleValue() {return (double) value;}
    /** Returns the scalar value. */ public float   floatValue() {return (float)  value;}
    /** Returns the scalar value. */ public long     longValue() {return (long)   value;}
    /** Returns the scalar value. */ public int       intValue() {return (int)    value;}
    /** Returns the scalar value. */ public short   shortValue() {return (short)  value;}
    /** Returns the scalar value. */ public byte     byteValue() {return (byte)   value;}

    /**
     * Returns the unit.
     */
    public Unit getUnit() {
        return unit;
    }

    /**
     * Returns a hash code value for this measure.
     */
    public int hashCode() {
        long code = Double.doubleToLongBits(value);
        return (int)code ^ (int)(code >>> 32) ^ unit.hashCode();
    }

    /**
     * Compares this measure with the specified object for equality.
     */
    public boolean equals(final Object object) {
        if (object instanceof Measure) {
            final Measure that = (Measure) object;
            return Double.doubleToLongBits(value) == Double.doubleToLongBits(that.value) &&
                   Utilities.equals(unit, that.unit);
        }
        return false;
    }

    /**
     * Returns a string representation of this measure.
     */
    public String toString() {
        final StringBuffer buffer = new StringBuffer();
        buffer.append(value);
        buffer.append(' ');
        buffer.append(unit);
        return buffer.toString();
    }
}
