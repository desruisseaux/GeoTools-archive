/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, Geotools Project Managment Committee (PMC)
 *    (C) 2007, Geomatys
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
import javax.units.ConversionException;
import javax.units.Converter;
import javax.units.Unit;

import org.geotools.resources.Utilities;
import org.geotools.resources.ClassChanger;


/**
 * A range of numbers associated with a unit of measurement. Unit conversions are applied as
 * needed by {@linkplain #union union} and {@linkplain #intersect intersection} operations.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class MeasurementRange extends NumberRange {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 3980319420337513745L;

    /**
     * The unit of measurement.
     */
    private final Unit units;

    /**
     * Constructs an inclusive range of {@code float} values.
     *
     * @param minimum The minimum value, inclusive.
     * @param maximum The maximum value, <strong>inclusive</strong>.
     * @param units   The units of measurement.
     */
    public MeasurementRange(final float minimum, final float maximum, final Unit units) {
        super(minimum, maximum);
        this.units = units;
    }

    /**
     * Constructs a range of {@code float} values.
     *
     * @param minimum The minimum value.
     * @param isMinIncluded Defines whether the minimum value is included in the Range.
     * @param maximum The maximum value.
     * @param isMaxIncluded Defines whether the maximum value is included in the Range.
     * @param units   The units of measurement.
     */
    public MeasurementRange(final float minimum, final boolean isMinIncluded,
                            final float maximum, final boolean isMaxIncluded, final Unit units)
    {
        super(minimum, isMinIncluded, maximum, isMaxIncluded);
        this.units = units;
    }

    /**
     * Constructs an inclusive range of {@code double} values.
     *
     * @param minimum The minimum value, inclusive.
     * @param maximum The maximum value, <strong>inclusive</strong>.
     * @param units   The units of measurement.
     */
    public MeasurementRange(final double minimum, final double maximum, final Unit units) {
        super(minimum, maximum);
        this.units = units;
    }

    /**
     * Constructs a range of {@code double} values.
     *
     * @param minimum The minimum value.
     * @param isMinIncluded Defines whether the minimum value is included in the Range.
     * @param maximum The maximum value.
     * @param isMaxIncluded Defines whether the maximum value is included in the Range.
     * @param units   The units of measurement.
     */
    public MeasurementRange(final double minimum, final boolean isMinIncluded,
                            final double maximum, final boolean isMaxIncluded, final Unit units)
    {
        super(minimum, isMinIncluded, maximum, isMaxIncluded);
        this.units = units;
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
     * @param units   The units of measurement.
     */
    public MeasurementRange(final Class type, final Number minimum, final boolean isMinIncluded,
                            final Number maximum, final boolean isMaxIncluded, final Unit units)
    {
        super(type, minimum, isMinIncluded, maximum, isMaxIncluded);
        this.units = units;
    }

    /**
     * Constructs a range with the same values than the specified range,
     * casted to the specified type.
     *
     * @param type The element class, usually one of {@link Byte}, {@link Short},
     *             {@link Integer}, {@link Long}, {@link Float} or {@link Double}.
     * @param range The range to copy. The elements must be {@link Number} instances.
     * @param units The units of measurement.
     */
    private MeasurementRange(final Class type, final Range range, final Unit units) {
        super(type, range);
        this.units = units;
    }

    /**
     * Returns the units of measurement.
     */
    public final Unit getUnits() {
        return units;
    }

    /**
     * Converts this range to the specified units.
     *
     * @param  targetUnit the target units.
     * @return The converted range, or {@code this} if no conversion is needed.
     * @throws ConversionException if the target units are not compatible with
     *         this {@linkplain #getUnits range units}.
     */
    public MeasurementRange convertTo(final Unit targetUnits) throws ConversionException {
        return convertAndCast(getElementClass(), targetUnits);
    }

    /**
     * Casts the specified range to the specified type. If this class is associated to a unit of
     * measurement, then this method convert the {@code range} units to the same units than this
     * instance.
     * 
     * @param type The class to cast to. Must be one of {@link Byte}, {@link Short},
     *             {@link Integer}, {@link Long}, {@link Float} or {@link Double}.
     * @return The casted range, or {@code range} if no cast is needed.
     */
    //@Override
    NumberRange convertAndCast(final Range range, final Class type) {
        if (range instanceof MeasurementRange) {
            return ((MeasurementRange) range).convertAndCast(type, units);
        } else {
            return super.convertAndCast(range, type);
        }
    }

    /**
     * Casts this range to the specified type and converts to the specified units.
     * 
     * @param  type The class to cast to. Must be one of {@link Byte}, {@link Short},
     *             {@link Integer}, {@link Long}, {@link Float} or {@link Double}.
     * @param  targetUnit the target units.
     * @return The casted range, or {@code this}.
     * @throws ConversionException if the target units are not compatible with
     *         this {@linkplain #getUnits range units}.
     */
    private MeasurementRange convertAndCast(final Class type, final Unit targetUnits)
            throws ConversionException
    {
        if (targetUnits == null || targetUnits.equals(units)) {
            if (type.equals(getElementClass())) {
                return this;
            } else {
                return new MeasurementRange(type, this, units);
            }
        }
        if (units == null) {
            return new MeasurementRange(type, this, targetUnits);
        }
        final Converter converter = units.getConverterTo(targetUnits);
        if (converter.equals(Converter.IDENTITY)) {
            return new MeasurementRange(type, this, targetUnits);
        }
        boolean isMinIncluded = isMinIncluded();
        boolean isMaxIncluded = isMaxIncluded();
        Double minimum = new Double(converter.convert(getMinimum()));
        Double maximum = new Double(converter.convert(getMaximum()));
        if (minimum.compareTo(maximum) > 0) {
            final Double td = minimum;
            minimum = maximum;
            maximum = td;
            final boolean tb = isMinIncluded;
            isMinIncluded = isMaxIncluded;
            isMaxIncluded = tb;
        }
        return new MeasurementRange(type,
                ClassChanger.cast(minimum, type), isMinIncluded,
                ClassChanger.cast(maximum, type), isMaxIncluded, targetUnits);
    }

    /**
     * Compares this range with the specified object for equality.
     */
    //@Override
    public boolean equals(final Object other) {
        if (super.equals(other)) {
            if (other instanceof MeasurementRange) {
                final MeasurementRange that = (MeasurementRange) other;
                return Utilities.equals(this.units, that.units);
            }
            return true;
        }
        return false;
    }
}
