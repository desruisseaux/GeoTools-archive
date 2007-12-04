/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2004, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.parameter;

import java.io.File;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import javax.units.Converter;
import javax.units.NonSI;
import javax.units.SI;
import javax.units.Unit;

import org.opengis.parameter.InvalidParameterTypeException;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValue;
import org.opengis.util.CodeList;

import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.Utilities;
import org.geotools.resources.Classes;
import org.geotools.measure.Units;


/**
 * A parameter value used by an operation method.
 * Most CRS parameter values are numeric, but other types of parameter values are possible.
 * The parameter type can be fetch with the
 * <code>{@linkplain #getValue()}.{@linkplain Object#getClass() getClass()}</code> idiom.
 * The {@link #getValue()} and {@link #setValue(Object)} methods can be invoked at any time.
 * Others getters and setters are parameter-type dependents.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Jody Garnett (Refractions Research)
 *
 * @see DefaultParameterDescriptor
 * @see ParameterGroup
 */
public class Parameter extends AbstractParameter implements ParameterValue {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5837826787089486776L;

    /**
     * The value.
     */
    private Object value;

    /**
     * The unit of measure for the value, or {@code null} if it doesn't apply.
     */
    private Unit unit;

    /**
     * Constructs a parameter from the specified name and value. This convenience
     * constructor creates a {@link DefaultParameterDescriptor} object. But if such
     * an object was available, then the preferred way to get a {@code ParameterValue}
     * is to invokes {@link ParameterDescriptor#createValue}.
     *
     * @param name  The parameter name.
     * @param value The parameter value.
     */
    public Parameter(final String name, final int value) {
        this(new DefaultParameterDescriptor(name, 0, Integer.MIN_VALUE, Integer.MAX_VALUE));
        this.value = value;
    }

    /**
     * Constructs a parameter from the specified name and value. This convenience
     * constructor creates a {@link DefaultParameterDescriptor} object. But if such
     * an object was available, then the preferred way to get a {@code ParameterValue} is
     * to invokes {@link ParameterDescriptor#createValue}.
     *
     * @param name  The parameter name.
     * @param value The parameter value.
     * @param unit  The unit for the parameter value.
     */
    public Parameter(final String name, final double value, final Unit unit) {
        this(new DefaultParameterDescriptor(name, Double.NaN, Double.NEGATIVE_INFINITY,
                                            Double.POSITIVE_INFINITY, normalize(unit)));
        this.value = value;
        this.unit  = unit;
    }

    /**
     * Constructs a parameter from the specified enumeration. This convenience
     * constructor creates a {@link DefaultParameterDescriptor} object. But if
     * such an object was available, then the preferred way to get a {@code ParameterValue}
     * is to invokes {@link ParameterDescriptor#createValue}.
     *
     * @param name  The parameter name.
     * @param value The parameter value.
     */
    public Parameter(final String name, final CodeList value) {
        this(new DefaultParameterDescriptor(name, value.getClass(), (CodeList)null));
        this.value = value;
    }

    /**
     * Constructs a parameter value from the specified descriptor.
     * The value will be initialized to the default value, if any.
     *
     * @param descriptor The abstract definition of this parameter.
     */
    public Parameter(final ParameterDescriptor descriptor) {
        super(descriptor);
        value = descriptor.getDefaultValue();
        unit  = descriptor.getUnit();
    }

    /**
     * Constructs a parameter value from the specified descriptor and value.
     *
     * @param  descriptor The abstract definition of this parameter.
     * @param  value The parameter value.
     * @throws InvalidParameterValueException if the type of {@code value} is inappropriate
     *         for this parameter, or if the value is illegal for some other reason (for example
     *         the value is numeric and out of range).
     */
    public Parameter(final ParameterDescriptor descriptor, final Object value)
            throws InvalidParameterValueException
    {
        super(descriptor);
        unit = descriptor.getUnit();
        setValue(value);
    }

    /**
     * Normalize the specified unit into one of "standard" units used in projections.
     */
    private static Unit normalize(final Unit unit) {
        if (unit != null) {
            if (SI.METER          .isCompatible(unit)) return SI.METER;
            if (NonSI.DAY         .isCompatible(unit)) return NonSI.DAY;
            if (NonSI.DEGREE_ANGLE.isCompatible(unit)) return NonSI.DEGREE_ANGLE;
        }
        return unit;
    }

    /**
     * Ensures that the given value is valid according the specified parameter descriptor.
     * This convenience method ensures that {@code value} is assignable to the
     * {@linkplain ParameterDescriptor#getValueClass expected class}, is between the
     * {@linkplain ParameterDescriptor#getMinimumValue minimum} and
     * {@linkplain ParameterDescriptor#getMaximumValue maximum} values and is one of the
     * {@linkplain ParameterDescriptor#getValidValues set of valid values}.
     * If the value fails any of those tests, then an exception is thrown.
     *
     * @param  descriptor The parameter descriptor to check against.
     * @param  value The value to check, or {@code null}.
     * @throws InvalidParameterValueException if the parameter value is invalid.
     */
    public static void ensureValidValue(final ParameterDescriptor descriptor, final Object value)
            throws InvalidParameterValueException
    {
        if (value == null) {
            return;
        }
        final String error;
        if (!descriptor.getValueClass().isAssignableFrom(value.getClass())) {
            error = Errors.format(ErrorKeys.ILLEGAL_OPERATION_FOR_VALUE_CLASS_$1, Classes.getClass(value));
        } else {
            final Comparable minimum = descriptor.getMinimumValue();
            final Comparable maximum = descriptor.getMaximumValue();
            if ((minimum!=null && minimum.compareTo(value)>0) ||
                (maximum!=null && maximum.compareTo(value)<0))
            {
                error = Errors.format(ErrorKeys.VALUE_OUT_OF_BOUNDS_$3, value, minimum, maximum);
            } else {
                final Set validValues = descriptor.getValidValues();
                if (validValues!=null && !validValues.contains(value)) {
                    error = Errors.format(ErrorKeys.ILLEGAL_ARGUMENT_$2, getName(descriptor), value);
                } else {
                    return;
                }
            }
        }
        throw new InvalidParameterValueException(error, getName(descriptor), value);
    }

    /**
     * Format an error message for illegal method call for the current value type.
     */
    private String getClassTypeError() {
        return Errors.format(ErrorKeys.ILLEGAL_OPERATION_FOR_VALUE_CLASS_$1,
               ((ParameterDescriptor) descriptor).getValueClass());
    }

    /**
     * Returns the unit of measure of the {@linkplain #doubleValue() parameter value}.
     * If the parameter value has no unit (for example because it is a {@link String} type),
     * then this method returns {@code null}. Note that "no unit" doesn't means
     * "dimensionless".
     *
     * @return The unit of measure, or {@code null} if none.
     *
     * @see #doubleValue()
     * @see #doubleValueList()
     * @see #getValue
     */
    public Unit getUnit() {
        return unit;
    }

    /**
     * Returns the unit type as one of error message code. Used for checking unit with a better
     * error message formatting if needed.
     * <p>
     * Note: It is difficult to differentiate scale and angular units, since both of them are
     *       dimensionless. However, in EPSG database version 6.7, there is only 3 scale units
     *       and all of them maps to {@link Unit#ONE} or {@link Units#PPM}. Consequently, they
     *       are hard-coded and treated especially by this method.
     *
     * @todo Provides a better way to differentiate scale units (currently Unit.ONE)
     *       and angular units. Both are dimensionless...
     */
    static int getUnitMessageID(final Unit unit) {
        // Note: ONE must be tested before RADIAN.
        if (Unit.ONE .equals      (unit) ||
            Units.PPM.equals      (unit)) return ErrorKeys.NON_SCALE_UNIT_$1;
        if (SI.METER .isCompatible(unit)) return ErrorKeys.NON_LINEAR_UNIT_$1;
        if (SI.SECOND.isCompatible(unit)) return ErrorKeys.NON_TEMPORAL_UNIT_$1;
        if (SI.RADIAN.isCompatible(unit)) return ErrorKeys.NON_ANGULAR_UNIT_$1;
        return ErrorKeys.INCOMPATIBLE_UNIT_$1;
    }

    /**
     * Returns the numeric value of the coordinate operation parameter in the specified unit
     * of measure. This convenience method apply unit conversion on the fly as needed.
     *
     * @param  unit The unit of measure for the value to be returned.
     * @return The numeric value represented by this parameter after conversion to type
     *         {@code double} and conversion to {@code unit}.
     * @throws InvalidParameterTypeException if the value is not a numeric type.
     * @throws IllegalArgumentException if the specified unit is invalid for this parameter.
     *
     * @see #getUnit
     * @see #setValue(double,Unit)
     * @see #doubleValueList(Unit)
     */
    public double doubleValue(final Unit unit) throws InvalidParameterTypeException {
        if (this.unit == null) {
            throw unitlessParameter(descriptor);
        }
        ensureNonNull("unit", unit);
        final int expectedID = getUnitMessageID(this.unit);
        if (getUnitMessageID(unit) != expectedID) {
            throw new IllegalArgumentException(Errors.format(expectedID, unit));
        }
        return this.unit.getConverterTo(unit).convert(doubleValue());
    }

    /**
     * Returns the numeric value of the coordinate operation parameter with its
     * associated {@linkplain #getUnit unit of measure}.
     *
     * @return The numeric value represented by this parameter after conversion to type {@code double}.
     * @throws InvalidParameterTypeException if the value is not a numeric type.
     *
     * @see #getUnit
     * @see #setValue(double)
     * @see #doubleValueList()
     */
    public double doubleValue() throws InvalidParameterTypeException {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        final String name = getName(descriptor);
        if (value == null) {
            // This is the kind of exception expected by org.geotools.referencing.wkt.Formatter.
            throw new IllegalStateException(Errors.format(ErrorKeys.MISSING_PARAMETER_$1, name));
        }
        // Reminder: the following is a specialization of IllegalStateException.
        throw new InvalidParameterTypeException(getClassTypeError(), name);
    }

    /**
     * Returns the positive integer value of an operation parameter, usually used
     * for a count. An integer value does not have an associated unit of measure.
     *
     * @return The numeric value represented by this parameter after conversion to type {@code int}.
     * @throws InvalidParameterTypeException if the value is not an integer type.
     *
     * @see #setValue(int)
     * @see #intValueList
     */
    public int intValue() throws InvalidParameterTypeException {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        final String name = getName(descriptor);
        if (value == null) {
            throw new IllegalStateException(Errors.format(ErrorKeys.MISSING_PARAMETER_$1, name));
        }
        throw new InvalidParameterTypeException(getClassTypeError(), name);
    }

    /**
     * Returns the boolean value of an operation parameter.
     * A boolean value does not have an associated unit of measure.
     *
     * @return The boolean value represented by this parameter.
     * @throws InvalidParameterTypeException if the value is not a boolean type.
     *
     * @see #setValue(boolean)
     */
    public boolean booleanValue() throws InvalidParameterTypeException {
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }
        final String name = getName(descriptor);
        if (value == null) {
            throw new IllegalStateException(Errors.format(ErrorKeys.MISSING_PARAMETER_$1, name));
        }
        throw new InvalidParameterTypeException(getClassTypeError(), name);
    }

    /**
     * Returns the string value of an operation parameter.
     * A string value does not have an associated unit of measure.
     *
     * @return The string value represented by this parameter.
     * @throws InvalidParameterTypeException if the value is not a string.
     *
     * @see #getValue
     * @see #setValue(Object)
     */
    public String stringValue() throws InvalidParameterTypeException {
        if (value instanceof CharSequence) {
            return value.toString();
        }
        final String name = getName(descriptor);
        if (value == null) {
            throw new IllegalStateException(Errors.format(ErrorKeys.MISSING_PARAMETER_$1, name));
        }
        throw new InvalidParameterTypeException(getClassTypeError(), name);
    }

    /**
     * Returns an ordered sequence of numeric values in the specified unit of measure.
     * This convenience method apply unit conversion on the fly as needed.
     *
     * @param  unit The unit of measure for the value to be returned.
     * @return The sequence of values represented by this parameter after conversion to type
     *         {@code double} and conversion to {@code unit}.
     * @throws InvalidParameterTypeException if the value is not an array of {@code double}s.
     * @throws IllegalArgumentException if the specified unit is invalid for this parameter.
     *
     * @see #getUnit
     * @see #setValue(double[],Unit)
     * @see #doubleValue(Unit)
     */
    public double[] doubleValueList(final Unit unit) throws InvalidParameterTypeException {
        if (this.unit == null) {
            throw unitlessParameter(descriptor);
        }
        ensureNonNull("unit", unit);
        final int expectedID = getUnitMessageID(this.unit);
        if (getUnitMessageID(unit) != expectedID) {
            throw new IllegalArgumentException(Errors.format(expectedID, unit));
        }
        final Converter converter = this.unit.getConverterTo(unit);
        final double[] values = doubleValueList().clone();
        for (int i=0; i<values.length; i++) {
            values[i] = converter.convert(values[i]);
        }
        return values;
    }

    /**
     * Returns an ordered sequence of two or more numeric values of an operation parameter
     * list, where each value has the same associated {@linkplain Unit unit of measure}.
     *
     * @return The sequence of values represented by this parameter.
     * @throws InvalidParameterTypeException if the value is not an array of {@code double}s.
     *
     * @see #getUnit
     * @see #setValue(Object)
     * @see #doubleValue()
     */
    public double[] doubleValueList() throws InvalidParameterTypeException {
        if (value instanceof double[]) {
            return (double[]) value;
        }
        final String name = getName(descriptor);
        if (value == null) {
            throw new IllegalStateException(Errors.format(ErrorKeys.MISSING_PARAMETER_$1, name));
        }
        throw new InvalidParameterTypeException(getClassTypeError(), name);
    }

    /**
     * Returns an ordered sequence of two or more integer values of an operation parameter list,
     * usually used for counts. These integer values do not have an associated unit of measure.
     *
     * @return The sequence of values represented by this parameter.
     * @throws InvalidParameterTypeException if the value is not an array of {@code int}s.
     *
     * @see #setValue(Object)
     * @see #intValue
     */
    public int[] intValueList() throws InvalidParameterTypeException {
        if (value instanceof int[]) {
            return (int[]) value;
        }
        final String name = getName(descriptor);
        if (value == null) {
            throw new IllegalStateException(Errors.format(ErrorKeys.MISSING_PARAMETER_$1, name));
        }
        throw new InvalidParameterTypeException(getClassTypeError(), name);
    }

    /**
     * Returns a reference to a file or a part of a file containing one or more parameter
     * values. When referencing a part of a file, that file must contain multiple identified
     * parts, such as an XML encoded document. Furthermore, the referenced file or part of a
     * file can reference another part of the same or different files, as allowed in XML documents.
     *
     * @return The reference to a file containing parameter values.
     * @throws InvalidParameterTypeException if the value is not a reference to a file or an URI.
     *
     * @see #getValue
     * @see #setValue(Object)
     */
    public URI valueFile() throws InvalidParameterTypeException {
        if (value instanceof URI) {
            return (URI) value;
        }
        if (value instanceof File) {
            return ((File) value).toURI();
        }
        Exception cause = null;
        try {
            if (value instanceof URL) {
                return ((URL) value).toURI();
            }
            if (value instanceof String) {
                return new URI((String) value);
            }
        } catch (URISyntaxException exception) {
            cause = exception;
        }
        /*
         * Value can't be converted.
         */
        final String name = getName(descriptor);
        if (value == null) {
            throw new IllegalStateException(Errors.format(ErrorKeys.MISSING_PARAMETER_$1, name));
        }
        final InvalidParameterTypeException exception =
                new InvalidParameterTypeException(getClassTypeError(), name);
        if (cause != null) {
            exception.initCause(cause);
        }
        throw exception;
    }

    /**
     * Returns the parameter value as an object. The object type is typically a {@link Double},
     * {@link Integer}, {@link Boolean}, {@link String}, {@link URI}, {@code double[]} or
     * {@code int[]}.
     *
     * @return The parameter value as an object.
     *
     * @see #setValue(Object)
     */
    public Object getValue() {
        return value;
    }

    /**
     * Set the parameter value as a floating point and its associated unit.
     *
     * @param  value The parameter value.
     * @param  unit The unit for the specified value.
     * @throws InvalidParameterValueException if the floating point type is inappropriate for this
     *         parameter, or if the value is illegal for some other reason (for example a value out
     *         of range).
     *
     * @see #setValue(double)
     * @see #doubleValue(Unit)
     */
    public void setValue(final double value, final Unit unit) throws InvalidParameterValueException {
        ensureNonNull("unit", unit);
        final Unit targetUnit = ((ParameterDescriptor) descriptor).getUnit();
        if (targetUnit == null) {
            throw unitlessParameter(descriptor);
        }
        final int expectedID = getUnitMessageID(targetUnit);
        if (getUnitMessageID(unit) != expectedID) {
            throw new InvalidParameterValueException(Errors.format(expectedID, unit),
                      descriptor.getName().getCode(), value);
        }
        final Double converted = unit.getConverterTo(targetUnit).convert(value);
        ensureValidValue((ParameterDescriptor) descriptor, converted);
        this.value = value;
        this.unit  = unit;
    }

    /**
     * Set the parameter value as a floating point.
     * The unit, if any, stay unchanged.
     *
     * @param value The parameter value.
     * @throws InvalidParameterValueException if the floating point type is inappropriate for this
     *         parameter, or if the value is illegal for some other reason (for example a value out
     *         of range).
     *
     * @see #setValue(double,Unit)
     * @see #doubleValue()
     */
    public void setValue(final double value) throws InvalidParameterValueException {
        final Double check = value;
        ensureValidValue((ParameterDescriptor) descriptor, check);
        this.value = check;
    }

    /**
     * Set the parameter value as an integer.
     *
     * @param  value The parameter value.
     * @throws InvalidParameterValueException if the integer type is inappropriate for this parameter,
     *         or if the value is illegal for some other reason (for example a value out of range).
     *
     * @see #intValue
     */
    public void setValue(final int value) throws InvalidParameterValueException {
        final ParameterDescriptor descriptor = (ParameterDescriptor) this.descriptor;
        final Class type = descriptor.getValueClass();
        if (Double.class.equals(type) || Double.TYPE.equals(type)) {
            setValue((double) value);
            return;
        }
        final Integer check = value;
        ensureValidValue(descriptor, check);
        this.value = check;
    }

    /**
     * Set the parameter value as a boolean.
     *
     * @param  value The parameter value.
     * @throws InvalidParameterValueException if the boolean type is inappropriate for this parameter.
     *
     * @see #booleanValue
     */
    public void setValue(final boolean value) throws InvalidParameterValueException {
        final Boolean check = Boolean.valueOf(value);
        ensureValidValue((ParameterDescriptor) descriptor, check);
        this.value = check;
    }

    /**
     * Set the parameter value as an object. The object type is typically a {@link Double},
     * {@link Integer}, {@link Boolean}, {@link String}, {@link URI}, {@code double[]}
     * or {@code int[]}.
     *
     * @param  value The parameter value.
     * @throws InvalidParameterValueException if the type of {@code value} is inappropriate
     *         for this parameter, or if the value is illegal for some other reason (for example
     *         the value is numeric and out of range).
     *
     * @see #getValue
     */
    public void setValue(final Object value) throws InvalidParameterValueException {
        ensureValidValue((ParameterDescriptor) descriptor, value);
        this.value = value;
    }

    /**
     * Set the parameter value as an array of floating point and their associated unit.
     *
     * @param  values The parameter values.
     * @param  unit The unit for the specified value.
     * @throws InvalidParameterValueException if the floating point type is inappropriate for this
     *         parameter, or if the value is illegal for some other reason (for example a value out
     *         of range).
     */
    public void setValue(double[] values, final Unit unit) throws InvalidParameterValueException {
        ensureNonNull("unit", unit);
        final Unit targetUnit = ((ParameterDescriptor) descriptor).getUnit();
        if (targetUnit == null) {
            throw unitlessParameter(descriptor);
        }
        final int expectedID = getUnitMessageID(targetUnit);
        if (getUnitMessageID(unit) != expectedID) {
            throw new IllegalArgumentException(Errors.format(expectedID, unit));
        }
        final double[] converted = values.clone();
        final Converter converter = unit.getConverterTo(targetUnit);
        for (int i=0; i<converted.length; i++) {
            converted[i] = converter.convert(converted[i]);
        }
        ensureValidValue((ParameterDescriptor) descriptor, converted);
        this.value = values;
        this.unit  = unit;
    }

    /**
     * Compares the specified object with this parameter for equality.
     *
     * @param  object The object to compare to {@code this}.
     * @return {@code true} if both objects are equal.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            // Slight optimization
            return true;
        }
        if (super.equals(object)) {
            final Parameter that = (Parameter) object;
            return Utilities.equals(this.value, that.value) &&
                   Utilities.equals(this.unit,  that.unit);
        }
        return false;
    }

    /**
     * Returns a hash value for this parameter.
     *
     * @return The hash code value. This value doesn't need to be the same
     *         in past or future versions of this class.
     */
    @Override
    public int hashCode() {
        int code = super.hashCode()*37;
        if (value != null) code +=   value.hashCode();
        if (unit  != null) code += 37*unit.hashCode();
        return code ^ (int)serialVersionUID;
    }
}
