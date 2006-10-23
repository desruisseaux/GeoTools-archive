/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2003, Institut de Recherche pour le Développement
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
 */
package org.geotools.util;

// JAI dependencies
import javax.media.jai.util.Range;

// Geotools dependencies
import org.geotools.resources.ClassChanger;
import org.geotools.resources.XMath;


/**
 * A range of numbers. {@linkplain #union Union} and {@linkplain #intersect intersection}
 * are computed as usual, except that widening conversions will be applied as needed.
 *
 * @since 2.0
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class NumberRange extends Range {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -818167965963008231L;

    /**
     * Constructs an inclusive range of {@code byte} values.
     *
     * @param minimum The minimum value, inclusive.
     * @param maximum The maximum value, <strong>inclusive</strong>.
     */
    public NumberRange(final byte minimum, final byte maximum) {
        this(minimum, true, maximum, true);
    }

    /**
     * Constructs a range of {@code byte} values.
     *
     * @param minimum The minimum value.
     * @param isMinIncluded Defines whether the minimum value is included in the Range.
     * @param maximum The maximum value.
     * @param isMaxIncluded Defines whether the maximum value is included in the Range.
     */
    public NumberRange(final byte minimum, final boolean isMinIncluded,
                       final byte maximum, final boolean isMaxIncluded)
    {
        super(Byte.class, new Byte(minimum), isMinIncluded,
                          new Byte(maximum), isMaxIncluded);
    }

    /**
     * Constructs an inclusive range of {@code short} values.
     *
     * @param minimum The minimum value, inclusive.
     * @param maximum The maximum value, <strong>inclusive</strong>.
     */
    public NumberRange(final short minimum, final short maximum) {
        this(minimum, true, maximum, true);
    }

    /**
     * Constructs a range of {@code short} values.
     *
     * @param minimum The minimum value.
     * @param isMinIncluded Defines whether the minimum value is included in the Range.
     * @param maximum The maximum value.
     * @param isMaxIncluded Defines whether the maximum value is included in the Range.
     */
    public NumberRange(final short minimum, final boolean isMinIncluded,
                       final short maximum, final boolean isMaxIncluded)
    {
        super(Short.class, new Short(minimum), isMinIncluded,
                           new Short(maximum), isMaxIncluded);
    }

    /**
     * Constructs an inclusive range of {@code int} values.
     *
     * @param minimum The minimum value, inclusive.
     * @param maximum The maximum value, <strong>inclusive</strong>.
     */
    public NumberRange(final int minimum, final int maximum) {
        this(minimum, true, maximum, true);
    }

    /**
     * Constructs a range of {@code int} values.
     *
     * @param minimum The minimum value.
     * @param isMinIncluded Defines whether the minimum value is included in the Range.
     * @param maximum The maximum value.
     * @param isMaxIncluded Defines whether the maximum value is included in the Range.
     */
    public NumberRange(final int minimum, final boolean isMinIncluded,
                       final int maximum, final boolean isMaxIncluded)
    {
        super(Integer.class, new Integer(minimum), isMinIncluded,
                             new Integer(maximum), isMaxIncluded);
    }

    /**
     * Constructs an inclusive range of {@code long} values.
     *
     * @param minimum The minimum value, inclusive.
     * @param maximum The maximum value, <strong>inclusive</strong>.
     */
    public NumberRange(final long minimum, final long maximum) {
        this(minimum, true, maximum, true);
    }

    /**
     * Constructs a range of {@code long} values.
     *
     * @param minimum The minimum value.
     * @param isMinIncluded Defines whether the minimum value is included in the Range.
     * @param maximum The maximum value.
     * @param isMaxIncluded Defines whether the maximum value is included in the Range.
     */
    public NumberRange(final long minimum, final boolean isMinIncluded,
                       final long maximum, final boolean isMaxIncluded)
    {
        super(Long.class, new Long(minimum), isMinIncluded,
                          new Long(maximum), isMaxIncluded);
    }

    /**
     * Constructs an inclusive range of {@code float} values.
     *
     * @param minimum The minimum value, inclusive.
     * @param maximum The maximum value, <strong>inclusive</strong>.
     */
    public NumberRange(final float minimum, final float maximum) {
        this(minimum, true, maximum, true);
    }

    /**
     * Constructs a range of {@code float} values.
     *
     * @param minimum The minimum value.
     * @param isMinIncluded Defines whether the minimum value is included in the Range.
     * @param maximum The maximum value.
     * @param isMaxIncluded Defines whether the maximum value is included in the Range.
     */
    public NumberRange(final float minimum, final boolean isMinIncluded,
                       final float maximum, final boolean isMaxIncluded)
    {
        super(Float.class, new Float(minimum), isMinIncluded,
                           new Float(maximum), isMaxIncluded);
    }

    /**
     * Constructs an inclusive range of {@code double} values.
     *
     * @param minimum The minimum value, inclusive.
     * @param maximum The maximum value, <strong>inclusive</strong>.
     */
    public NumberRange(final double minimum, final double maximum) {
        this(minimum, true, maximum, true);
    }

    /**
     * Constructs a range of {@code double} values.
     *
     * @param minimum The minimum value.
     * @param isMinIncluded Defines whether the minimum value is included in the Range.
     * @param maximum The maximum value.
     * @param isMaxIncluded Defines whether the maximum value is included in the Range.
     */
    public NumberRange(final double minimum, final boolean isMinIncluded,
                       final double maximum, final boolean isMaxIncluded)
    {
        super(Double.class, new Double(minimum), isMinIncluded,
                            new Double(maximum), isMaxIncluded);
    }

    /**
     * Constructs an inclusive range of {@link Comparable} objects.
     * This constructor is used by {@link RangeSet#newRange} only.
     *
     * @param classe The element class, usually one of {@link Byte}, {@link Short},
     *               {@link Integer}, {@link Long}, {@link Float} or {@link Double}.
     * @param minimum The minimum value, inclusive.
     * @param maximum The maximum value, <strong>inclusive</strong>.
     */
    NumberRange(final Class classe, final Comparable minimum, final Comparable maximum) {
        super(classe, minimum, maximum);
    }

    /**
     * Constructs an inclusive range of {@link Number} objects.
     *
     * @param classe The element class, usually one of {@link Byte}, {@link Short},
     *               {@link Integer}, {@link Long}, {@link Float} or {@link Double}.
     * @param minimum The minimum value, inclusive.
     * @param maximum The maximum value, <strong>inclusive</strong>.
     */
    public NumberRange(final Class classe, final Number minimum, final Number maximum) {
        super(classe, (Comparable)minimum, (Comparable)maximum);
    }

    /**
     * Constructs a range of {@link Number} objects.
     *
     * @param classe The element class, usually one of {@link Byte}, {@link Short},
     *               {@link Integer}, {@link Long}, {@link Float} or {@link Double}.
     * @param minimum The minimum value.
     * @param isMinIncluded Defines whether the minimum value is included in the Range.
     * @param maximum The maximum value.
     * @param isMaxIncluded Defines whether the maximum value is included in the Range.
     */
    public NumberRange(final Class classe, final Number minimum, final boolean isMinIncluded,
                                           final Number maximum, final boolean isMaxIncluded)
    {
        super(classe, (Comparable)minimum, isMinIncluded, (Comparable)maximum, isMaxIncluded);
    }

    /**
     * Wrap the specified {@link Range} in a {@code NumberRange} object. If the specified
     * range is already an instance of {@code NumberRange}, then it is returned unchanged.
     *
     * @param  range The range to wrap
     * @return The same range than {@code range} as a {@code NumberRange} object.
     */
    public static NumberRange wrap(final Range range) {
        if (range instanceof NumberRange) {
            return (NumberRange) range;
        }
        return new NumberRange(range.getElementClass(),
                               (Number)range.getMinValue(), range.isMinIncluded(),
                               (Number)range.getMaxValue(), range.isMaxIncluded());
    }

    /**
     * Cast this range to the specified type.
     *
     * @param  type The class to cast to. Must be one of {@link Byte}, {@link Short},
     *              {@link Integer}, {@link Long}, {@link Float} or {@link Double}.
     * @return The casted range, or {@code this}.
     */
    private static NumberRange cast(final Range r, final Class type) {
        if (r==null || type.equals(r.getElementClass())) {
            return wrap(r);
        }
        return new NumberRange(type,
                               ClassChanger.cast((Number)r.getMinValue(), type), r.isMinIncluded(),
                               ClassChanger.cast((Number)r.getMaxValue(), type), r.isMaxIncluded());
    }

    /**
     * Cast this range to the specified type.
     *
     * @param  type The class to cast to. Must be one of {@link Byte}, {@link Short},
     *              {@link Integer}, {@link Long}, {@link Float} or {@link Double}.
     * @return The casted range, or {@code this} if this range already uses
     *         the specified type.
     */
    public NumberRange castTo(final Class type) {
        return cast(this, type);
    }

    /**
     * Returns {@code true} if the specified value is within this range.
     */
    public boolean contains(final Comparable value) {
        return contains((Number) value);
    }

    /**
     * Returns {@code true} if the specified value is within this range.
     */
    public boolean contains(final Number value) {
        final Class type = ClassChanger.getWidestClass(getElementClass(), value.getClass());
        return castTo(type)._contains(ClassChanger.cast(value, type));
    }

    /**
     * Performs the contains test (no type check).
     */
    private boolean _contains(final Number value) {
        return super.contains((Comparable) value);
    }

    /**
     * Returns true if the supplied range is fully contained within this range.
     */
    public boolean contains(final Range range) {
        final Class type = ClassChanger.getWidestClass(getElementClass(), range.getElementClass());
        return castTo(type)._contains(cast(range, type));
    }

    /**
     * Performs the test (no type check).
     */
    private boolean _contains(final Range range) {
        return super.contains(range);
    }

    /**
     * Returns true if this range intersects the given range.
     */
    public boolean intersects(final Range range) {
        final Class type = ClassChanger.getWidestClass(getElementClass(), range.getElementClass());
        return castTo(type)._intersects(cast(range, type));
    }

    /**
     * Performs the test (no type check).
     */
    private boolean _intersects(final Range range) {
        return super.intersects(range);
    }

    /**
     * Returns the union of this range with the given range.
     * Widening conversions will be applied as needed.
     *
     * @todo The return type will be changed to {@code NumberRange} when J2SE 1.5
     *       will be available. We should then search for NumberRange.warp(...) in all
     *       client classes; some 'warp' may no longer be needed.
     */
    public Range union(final Range range) {
        final Class type = ClassChanger.getWidestClass(getElementClass(), range.getElementClass());
        return wrap(castTo(type)._union(cast(range, type)));
    }

    /**
     * Performs the union (no type check).
     */
    private Range _union(final Range range) {
        return super.union(range);
    }

    /**
     * Returns the intersection of this range with the given range.
     * Widening conversions will be applied as needed.
     *
     * @todo The return type will be changed to {@code NumberRange} when J2SE 1.5
     *       will be available. We should then search for NumberRange.warp(...) in all
     *       client classes; some 'warp' may no longer be needed.
     */
    public Range intersect(final Range range) {
        Class type = ClassChanger.getWidestClass(getElementClass(), range.getElementClass());
        final Range result = castTo(type)._intersect(cast(range, type));
        /*
         * Use a finer type capable to holds the result (since the intersection may have
         * reduced the range), but not finer than the finest type of the ranges used in
         * the intersection calculation.
         */
        type = ClassChanger.getFinestClass(getElementClass(), range.getElementClass());
        return cast(result,
                    ClassChanger.getWidestClass(type, 
                    ClassChanger.getWidestClass(
                    ClassChanger.getFinestClass(((Number)result.getMinValue()).doubleValue()),
                    ClassChanger.getFinestClass(((Number)result.getMaxValue()).doubleValue()))));
    }

    /**
     * Performs the intersection (no type check).
     */
    private Range _intersect(final Range range) {
        return super.intersect(range);
    }

    /**
     * Returns the range of values that are in this range but not in the given range.
     */
    public Range[] subtract(final Range range) {
        Class type = ClassChanger.getWidestClass(getElementClass(), range.getElementClass());
        final Range[] result = castTo(type)._subtract(cast(range, type));
        if (result!=null) {
            for (int i=0; i<result.length; i++) {
                result[i] = wrap(result[i]);
            }
        }
        return result;
    }

    /**
     * Performs the substraction (no type check).
     */
    private Range[] _subtract(final Range range) {
        return super.subtract(range);
    }

    /**
     * Returns the {@linkplain #getMinValue minimum value} as a {@code double}.
     * If this range is unbounded, then {@link Double#NEGATIVE_INFINITY} is returned.
     */
    public double getMinimum() {
        final Number value = (Number) getMinValue();
        return (value!=null) ? value.doubleValue() : Double.NEGATIVE_INFINITY;
    }

    /**
     * Returns the {@linkplain #getMinimum() minimum value} with the specified inclusive or
     * exclusive state. If this range is unbounded, then {@link Double#NEGATIVE_INFINITY} is
     * returned.
     *
     * @param  inclusive {@code true} for the minimum value inclusive,
     *         or {@code false} for the minimum value exclusive.
     * @return The minimum value, inclusive or exclusive as requested.
     */
    public double getMinimum(final boolean inclusive) {
        double value = getMinimum();
        if (inclusive != isMinIncluded()) {
            value = XMath.rool(getElementClass(), value, inclusive ? +1 : -1);
        }
        return value;
    }

    /**
     * Returns the {@linkplain #getMaxValue maximum value} as a {@code double}.
     * If this range is unbounded, then {@link Double#POSITIVE_INFINITY} is returned.
     */
    public double getMaximum() {
        final Number value = (Number) getMaxValue();
        return (value!=null) ? value.doubleValue() : Double.POSITIVE_INFINITY;
    }

    /**
     * Returns the {@linkplain #getMaximum() maximum value} with the specified inclusive or
     * exclusive state. If this range is unbounded, then {@link Double#POSITIVE_INFINITY} is
     * returned.
     *
     * @param  inclusive {@code true} for the maximum value inclusive,
     *         or {@code false} for the maximum value exclusive.
     * @return The maximum value, inclusive or exclusive as requested.
     */
    public double getMaximum(final boolean inclusive) {
        double value = getMaximum();
        if (inclusive != isMaxIncluded()) {
            value = XMath.rool(getElementClass(), value, inclusive ? -1 : +1);
        }
        return value;
    }
}
