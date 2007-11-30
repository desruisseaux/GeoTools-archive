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

import javax.media.jai.util.Range;

import org.geotools.resources.ClassChanger;
import org.geotools.resources.Utilities;
import org.geotools.resources.XMath;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


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
        super(Byte.class, Byte.valueOf(minimum), isMinIncluded,
                          Byte.valueOf(maximum), isMaxIncluded);
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
        super(Short.class, Short.valueOf(minimum), isMinIncluded,
                           Short.valueOf(maximum), isMaxIncluded);
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
        super(Integer.class, Integer.valueOf(minimum), isMinIncluded,
                             Integer.valueOf(maximum), isMaxIncluded);
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
        super(Long.class, Long.valueOf(minimum), isMinIncluded,
                          Long.valueOf(maximum), isMaxIncluded);
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
        super(Float.class, Float.valueOf(minimum), isMinIncluded,
                           Float.valueOf(maximum), isMaxIncluded);
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
        super(Double.class, Double.valueOf(minimum), isMinIncluded,
                            Double.valueOf(maximum), isMaxIncluded);
    }

    /**
     * Constructs an inclusive range of {@link Comparable} objects.
     * This constructor is used by {@link RangeSet#newRange} only.
     *
     * @param type The element class, usually one of {@link Byte}, {@link Short},
     *             {@link Integer}, {@link Long}, {@link Float} or {@link Double}.
     * @param minimum The minimum value, inclusive.
     * @param maximum The maximum value, <strong>inclusive</strong>.
     */
    NumberRange(final Class<? extends Number> type, final Comparable minimum, final Comparable maximum) {
        super(type, minimum, maximum);
    }

    /**
     * Constructs an inclusive range of {@link Number} objects.
     *
     * @param type The element class, usually one of {@link Byte}, {@link Short},
     *             {@link Integer}, {@link Long}, {@link Float} or {@link Double}.
     * @param minimum The minimum value, inclusive.
     * @param maximum The maximum value, <strong>inclusive</strong>.
     */
    public <N extends Number> NumberRange(final Class<N> type, final N minimum, final N maximum) {
        super(type, (Comparable) minimum, (Comparable) maximum);
    }

    /**
     * Constructs a range of {@link Number} objects.
     *
     * @param type The element class, usually one of {@link Byte}, {@link Short},
     *             {@link Integer}, {@link Long}, {@link Float} or {@link Double}.
     * @param minimum The minimum value.
     * @param isMinIncluded Defines whether the minimum value is included in the Range.
     * @param maximum The maximum value.
     * @param isMaxIncluded Defines whether the maximum value is included in the Range.
     */
    public <N extends Number> NumberRange(final Class<N> type,
                                          final N minimum, final boolean isMinIncluded,
                                          final N maximum, final boolean isMaxIncluded)
    {
        super(type, (Comparable)minimum, isMinIncluded, (Comparable)maximum, isMaxIncluded);
    }

    /**
     * Constructs a range with the same values than the specified range,
     * casted to the specified type.
     *
     * @param type The element class, usually one of {@link Byte}, {@link Short},
     *             {@link Integer}, {@link Long}, {@link Float} or {@link Double}.
     * @param range The range to copy. The elements must be {@link Number} instances.
     * @throws ClassCastException if some elements are not instances of {@link Number}.
     */
    @SuppressWarnings("unchecked")  // Check will be performed by super-class constructor
    <N extends Number> NumberRange(final Class<N> type, final Range range) throws ClassCastException {
        this(type, ClassChanger.cast((N) range.getMinValue(), type), range.isMinIncluded(),
                   ClassChanger.cast((N) range.getMaxValue(), type), range.isMaxIncluded());
    }

    /**
     * Constructs a range with the same type and the same values than the specified range.
     * This is a copy constructor.
     *
     * @param range The range to copy. The elements must be {@link Number} instances.
     * @throws ClassCastException if some elements are not instances of {@link Number}.
     *
     * @since 2.4
     */
    @SuppressWarnings("unchecked")  // Check will be performed by super-class constructor
    public NumberRange(final Range range) throws ClassCastException {
        this((Class) getElementClass(range),
             (Number) range.getMinValue(), range.isMinIncluded(),
             (Number) range.getMaxValue(), range.isMaxIncluded());
    }

    /**
     * Wraps the specified {@link Range} in a {@code NumberRange} object. If the specified
     * range is already an instance of {@code NumberRange}, then it is returned unchanged.
     *
     * @param  range The range to wrap
     * @return The same range than {@code range} as a {@code NumberRange} object.
     */
    public static NumberRange wrap(final Range range) {
        if (range instanceof NumberRange) {
            return (NumberRange) range;
        }
        return new NumberRange(range);
    }

    /**
     * Returns the type of minimum and maximum values.
     */
    private static Class<? extends Number> getElementClass(final Range range)
            throws ClassCastException
    {
        @SuppressWarnings("unchecked")
        final Class<? extends Number> type = range.getElementClass();
        if (!Number.class.isAssignableFrom(type)) {
            throw new ClassCastException(Errors.format(ErrorKeys.ILLEGAL_CLASS_$2,
                    Utilities.getShortName(type), Utilities.getShortName(Number.class)));
        }
        return type;
    }

    /**
     * Returns the type of minimum and maximum values.
     */
    @Override
    public Class<? extends Number> getElementClass() {
        @SuppressWarnings("unchecked")
        final Class<? extends Number> type = super.getElementClass();
        assert Number.class.isAssignableFrom(type) : type;
        return type;
    }

    /**
     * Casts the specified range to the specified type. If this class is associated to a unit of
     * measurement, then this method convert the {@code range} units to the same units than this
     * instance. This method is overriden by {@link MeasurementRange} only in the way described
     * above.
     *
     * @param type The class to cast to. Must be one of {@link Byte}, {@link Short},
     *             {@link Integer}, {@link Long}, {@link Float} or {@link Double}.
     * @return The casted range, or {@code range} if no cast is needed.
     */
    NumberRange convertAndCast(final Range range, final Class<? extends Number> type) {
        if (type.equals(range.getElementClass())) {
            return wrap(range);
        }
        return new NumberRange(type, range);
    }

    /**
     * Casts this range to the specified type.
     *
     * @param  type The class to cast to. Must be one of {@link Byte}, {@link Short},
     *              {@link Integer}, {@link Long}, {@link Float} or {@link Double}.
     * @return The casted range, or {@code this} if this range already uses the specified type.
     */
    public NumberRange castTo(final Class<? extends Number> type) {
        return convertAndCast(this, type);
    }

    /**
     * Returns {@code true} if the specified value is within this range.
     */
    @Override
    public boolean contains(final Comparable value) {
        return contains((Number) value);
    }

    /**
     * Returns {@code true} if the specified value is within this range.
     */
    public boolean contains(final Number value) {
        final Class<? extends Number> type =
                ClassChanger.getWidestClass(getElementClass(), value.getClass());
        return castTo(type).containsImpl(ClassChanger.cast(value, type));
    }

    /**
     * Performs the contains test (no type check).
     */
    private boolean containsImpl(final Number value) {
        return super.contains((Comparable) value);
    }

    /**
     * Returns true if the supplied range is fully contained within this range.
     */
    @Override
    public boolean contains(final Range range) {
        final Class<? extends Number> type =
                ClassChanger.getWidestClass(getElementClass(), getElementClass(range));
        return castTo(type).containsImpl(convertAndCast(range, type));
    }

    /**
     * Performs the test (no type check).
     */
    private boolean containsImpl(final NumberRange range) {
        return super.contains(range);
    }

    /**
     * Returns true if this range intersects the given range.
     */
    @Override
    public boolean intersects(final Range range) {
        final Class<? extends Number> type =
                ClassChanger.getWidestClass(getElementClass(), getElementClass(range));
        return castTo(type).intersectsImpl(convertAndCast(range, type));
    }

    /**
     * Performs the test (no type check).
     */
    private boolean intersectsImpl(final NumberRange range) {
        return super.intersects(range);
    }

    /**
     * Returns the union of this range with the given range.
     * Widening conversions will be applied as needed.
     */
    @Override
    public NumberRange union(final Range range) {
        final Class<? extends Number> type =
                ClassChanger.getWidestClass(getElementClass(), getElementClass(range));
        return wrap(castTo(type).unionImpl(convertAndCast(range, type)));
    }

    /**
     * Performs the union (no type check).
     */
    private Range unionImpl(final NumberRange range) {
        return super.union(range);
    }

    /**
     * Returns the intersection of this range with the given range.
     * Widening conversions will be applied as needed.
     */
    @Override
    public NumberRange intersect(final Range range) {
        Class<? extends Number> type =
                ClassChanger.getWidestClass(getElementClass(), getElementClass(range));
        final Range result = castTo(type).intersectImpl(convertAndCast(range, type));
        /*
         * Use a finer type capable to holds the result (since the intersection may have
         * reduced the range), but not finer than the finest type of the ranges used in
         * the intersection calculation.
         */
        type = ClassChanger.getFinestClass(getElementClass(), getElementClass(range));
        return convertAndCast(result,
                ClassChanger.getWidestClass(type,
                ClassChanger.getWidestClass(
                ClassChanger.getFinestClass(((Number)result.getMinValue()).doubleValue()),
                ClassChanger.getFinestClass(((Number)result.getMaxValue()).doubleValue()))));
    }

    /**
     * Performs the intersection (no type check).
     */
    private Range intersectImpl(final NumberRange range) {
        return super.intersect(range);
    }

    /**
     * Returns the range of values that are in this range but not in the given range.
     */
    @Override
    public NumberRange[] subtract(final Range range) {
        Class<? extends Number> type =
                ClassChanger.getWidestClass(getElementClass(), getElementClass(range));
        final Range[] result = castTo(type).subtractImpl(convertAndCast(range, type));
        final NumberRange[] casted;
        if (result != null) {
            casted = new NumberRange[result.length];
            for (int i=0; i<result.length; i++) {
                casted[i] = wrap(result[i]);
            }
        } else {
            casted = null;
        }
        return casted;
    }

    /**
     * Performs the substraction (no type check).
     */
    private Range[] subtractImpl(final NumberRange range) {
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
