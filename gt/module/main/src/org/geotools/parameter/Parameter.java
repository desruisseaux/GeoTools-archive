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
package org.geotools.parameter;

// J2SE dependencies
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collection;
import java.util.Set;
import java.util.Arrays;
import javax.units.Unit;
import javax.units.SI;
import javax.units.NonSI;
import javax.units.Converter;

// OpenGIS dependencies
import org.opengis.util.CodeList;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.InvalidParameterTypeException;
import org.opengis.parameter.InvalidParameterValueException;

// Geotools dependencies
import org.geotools.data.DataSourceException;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * A parameter value used by an operation method.
 * <p>
 * Most CRS parameter values are numeric, but other types of parameter values are possible. The parameter type can be fetch with the
 * <code>{@linkplain #getValue()}.{@linkplain Object#getClass() getClass()}</code> idiom.
 * The {@link #getValue()} and {@link #setValue(Object)} methods can be invoked at any time.
 * Others getters and setters are parameter-type dependents.
 * </p>
 * <p>
 * This implementation extends the capabilities of the geoapi interface with an additional
 * functionality proivded by parse( text ), and text() methods.
 * </p> 
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see org.geotools.parameter.ParameterDescriptor
 * @see org.geotools.parameter.ParameterGroup
 */
public class Parameter extends AbstractParameter
                         implements org.opengis.parameter.ParameterValue
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5837826787089486776L;

    /**
     * Frequently used values. <strong>Must</strong> be in increasing order.
     */
    private static final int[] CACHED_VALUES = {
        -360, -180, -90, -45, -30, -4, -3, -2, -1, 0, +1, +2, +3, +4, +30, +45, +90, +180, +360
    };

    /** Frequently used values as integers. */ private static final Integer[] CACHED_INTEGERS;
    /** Frequently used values as doubles.  */ private static final Double [] CACHED_DOUBLES;
    static {
        CACHED_INTEGERS = new Integer[CACHED_VALUES.length];
        CACHED_DOUBLES  = new Double [CACHED_VALUES.length];
        for (int i=0; i<CACHED_VALUES.length; i++) {
            CACHED_INTEGERS[i] = new Integer(CACHED_VALUES[i]);
            CACHED_DOUBLES [i] = new Double (CACHED_VALUES[i]);
        }
    }

    /**
     * The value.
     */
    private Object value;

    /**
     * The unit of measure for the value, or <code>null</code> if it doesn't apply.
     */
    private Unit unit;

    /**
     * Construct a parameter from the specified name and value. This convenience constructor
     * creates a default {@link org.geotools.parameter.ParameterDescriptor} object. But if such
     * an object was available, then the preferred way to get a <code>ParameterValue</code>
     * is to invokes {@link org.geotools.parameter.ParameterDescriptor#createValue}.
     *
     * @param name  The parameter name.
     * @param value The parameter value.
     */
    public Parameter(final String name, final int value) {
        this(new org.geotools.parameter.ParameterDescriptor(name,
                 0, Integer.MIN_VALUE, Integer.MAX_VALUE));
        this.value = wrap(value);
    }

    /**
     * Construct a parameter from the specified name and value. This convenience constructor
     * creates a default {@link org.geotools.parameter.ParameterDescriptor} object. But if such
     * an object was available, then the preferred way to get a <code>ParameterValue</code> is
     * to invokes {@link org.geotools.parameter.ParameterDescriptor#createValue}.
     *
     * @param name  The parameter name.
     * @param value The parameter value.
     * @param unit  The unit for the parameter value.
     */
    public Parameter(final String name, final double value, final Unit unit) {
        this(new org.geotools.parameter.ParameterDescriptor(name,
                 Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, normalize(unit)));
        this.value = wrap(value);
        this.unit  = unit;
    }

    /**
     * Construct a parameter from the specified enumeration. This convenience constructor
     * creates a default {@link org.geotools.parameter.ParameterDescriptor} object. But if
     * such an object was available, then the preferred way to get a <code>ParameterValue</code>
     * is to invokes {@link org.geotools.parameter.ParameterDescriptor#createValue}.
     *
     * @param name  The parameter name.
     * @param value The parameter value.
     */
    public Parameter(final String name, final CodeList value) {
        this(new org.geotools.parameter.ParameterDescriptor(name, value));
        this.value = value;
    }

    /**
     * Construct a parameter value from the specified descriptor.
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
     * Wrap the specified value in an {@link Integer} object.
     * This method try to avoid object creation if the value
     * is one of {@link #CACHED_VALUES frequently used values}.
     */
    static Integer wrap(final int value) {
        final int i = Arrays.binarySearch(CACHED_VALUES, value);
        return (i>=0) ? CACHED_INTEGERS[i] : new Integer(value);
    }

    /**
     * Wrap the specified value in an {@link Double} object.
     * This method try to avoid object creation if the value
     * is one of {@link #CACHED_VALUES frequently used values}.
     */
    static Double wrap(final double value) {
        final int integer = (int)value;
        if (integer == value) {
            final int i = Arrays.binarySearch(CACHED_VALUES, integer);
            if (i >= 0) {
                return CACHED_DOUBLES[i];
            }
        }
        return new Double(value);
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
     * This convenience method ensures that <code>value</code> is assignable to the
     * {@linkplain org.geotools.parameter.ParameterDescriptor#getValueClass expected class}, is between the
     * {@linkplain org.geotools.parameter.ParameterDescriptor#getMinimumValue minimum} and
     * {@linkplain org.geotools.parameter.ParameterDescriptor#getMaximumValue maximum} values and is one of the
     * {@linkplain org.geotools.parameter.ParameterDescriptor#getValidValues set of valid values}.
     * If the value fails any of those tests, then an exception is thrown.
     *
     * @param  descriptor The parameter descriptor to check against.
     * @param  value The value to check, or <code>null</code>.
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
            error = Resources.format(ResourceKeys.ERROR_ILLEGAL_OPERATION_FOR_VALUE_CLASS_$1,
                    Utilities.getShortClassName(value));
        } else {
            final Comparable minimum = descriptor.getMinimumValue();
            final Comparable maximum = descriptor.getMaximumValue();
            if ((minimum!=null && minimum.compareTo(value)>0) ||
                (maximum!=null && maximum.compareTo(value)<0))
            {
                error = Resources.format(ResourceKeys.ERROR_VALUE_OUT_OF_BOUNDS_$3,
                                         value, minimum, maximum);
            } else {
                final Set validValues = descriptor.getValidValues();
                if (validValues!=null && !validValues.contains(value)) {
                    error = Resources.format(ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2,
                                             getName(descriptor), value);
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
        return Resources.format(ResourceKeys.ERROR_ILLEGAL_OPERATION_FOR_VALUE_CLASS_$1,
               Utilities.getShortName(((ParameterDescriptor)descriptor).getValueClass()));
    }

    /**
     * Returns the parameter name in the default locale.
     */
    static String getName(final GeneralParameterDescriptor descriptor) {
        return descriptor.getName().toString();
    }

    /**
     * Returns the unit of measure of the {@linkplain #doubleValue() parameter value}.
     * If the parameter value has no unit (for example because it is a {@link String} type),
     * then this method returns <code>null</code>. Note that "no unit" doesn't means
     * "dimensionless".
     *
     * @return The unit of measure, or <code>null</code> if none.
     *
     * @see #doubleValue()
     * @see #doubleValueList()
     * @see #getValue
     */
    public Unit getUnit() {
        return unit;
    }

    /**
     * Returns the unit type as one of error message code. Used for
     * checking unit type are better error message formatting if needed.
     */
    static int getUnitMessageID(final Unit unit) {
        if (SI.METER .isCompatible(unit)) return ResourceKeys.ERROR_NON_LINEAR_UNIT_$1;
        if (SI.SECOND.isCompatible(unit)) return ResourceKeys.ERROR_NON_TEMPORAL_UNIT_$1;
        if (SI.RADIAN.isCompatible(unit)) return ResourceKeys.ERROR_NON_ANGULAR_UNIT_$1;
        return -1;
    }

    /**
     * Returns the numeric value of the coordinate operation parameter in the specified unit
     * of measure. This convenience method apply unit conversion on the fly as needed.
     *
     * @param  unit The unit of measure for the value to be returned.
     * @return The numeric value represented by this parameter after conversion to type
     *         <code>double</code> and conversion to <code>unit</code>.
     * @throws InvalidParameterTypeException if the value is not a numeric type.
     * @throws IllegalArgumentException if the specified unit is invalid for this parameter.
     *
     * @see #getUnit
     * @see #setValue(double,Unit)
     * @see #doubleValueList(Unit)
     */
    public double doubleValue(final Unit unit) throws InvalidParameterTypeException {
        if (this.unit == null) {
            throw new IllegalStateException(Resources.format(
                      ResourceKeys.ERROR_UNITLESS_PARAMETER_$1, getName(descriptor)));
        }
        ensureNonNull("unit", unit);
        final int expectedID = getUnitMessageID(this.unit);
        if (getUnitMessageID(unit) != expectedID) {
            throw new IllegalArgumentException(Resources.format(expectedID, unit));
        }
        return this.unit.getConverterTo(unit).convert(doubleValue());
    }

    /**
     * Returns the numeric value of the coordinate operation parameter with its
     * associated {@linkplain #getUnit unit of measure}.
     *
     * @return The numeric value represented by this parameter after conversion to type <code>double</code>.
     * @throws InvalidParameterTypeException if the value is not a numeric type.
     *
     * @see #getUnit
     * @see #setValue(double)
     * @see #doubleValueList()
     */
    public double doubleValue() throws InvalidParameterTypeException {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else {
            throw new InvalidParameterTypeException(getClassTypeError(), getName(descriptor));
        }
    }

    /**
     * Returns the positive integer value of an operation parameter, usually used
     * for a count. An integer value does not have an associated unit of measure.
     *
     * @return The numeric value represented by this parameter after conversion to type <code>int</code>.
     * @throws InvalidParameterTypeException if the value is not an integer type.
     *
     * @see #setValue(int)
     * @see #intValueList
     */
    public int intValue() throws InvalidParameterTypeException {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else {
            throw new InvalidParameterTypeException(getClassTypeError(), getName(descriptor));
        }
    }

    /**
     * Returns the boolean value of an operation parameter
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
        } else {
            throw new InvalidParameterTypeException(getClassTypeError(), getName(descriptor));
        }
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
        } else {
            throw new InvalidParameterTypeException(getClassTypeError(), getName(descriptor));
        }
    }

    /**
     * Returns an ordered sequence of numeric values in the specified unit of measure.
     * This convenience method apply unit conversion on the fly as needed.
     *
     * @param  unit The unit of measure for the value to be returned.
     * @return The sequence of values represented by this parameter after conversion to type
     *         <code>double</code> and conversion to <code>unit</code>.
     * @throws InvalidParameterTypeException if the value is not an array of <code>double</code>s.
     * @throws IllegalArgumentException if the specified unit is invalid for this parameter.
     *
     * @see #getUnit
     * @see #setValue(double[],Unit)
     * @see #doubleValue(Unit)
     */
    public double[] doubleValueList(final Unit unit) throws InvalidParameterTypeException {
        if (this.unit == null) {
            throw new IllegalStateException(Resources.format(
                      ResourceKeys.ERROR_UNITLESS_PARAMETER_$1, getName(descriptor)));
        }
        ensureNonNull("unit", unit);
        final int expectedID = getUnitMessageID(this.unit);
        if (getUnitMessageID(unit) != expectedID) {
            throw new IllegalArgumentException(Resources.format(expectedID, unit));
        }
        final Converter converter = this.unit.getConverterTo(unit);
        final double[] values = (double[]) doubleValueList().clone();
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
     * @throws InvalidParameterTypeException if the value is not an array of <code>double</code>s.
     *
     * @see #getUnit
     * @see #setValue(Object)
     * @see #doubleValue()
     */
    public double[] doubleValueList() throws InvalidParameterTypeException {
        if (value instanceof double[]) {
            return (double[]) value;
        } else {
            throw new InvalidParameterTypeException(getClassTypeError(), getName(descriptor));
        }
    }

    /**
     * Returns an ordered sequence of two or more integer values of an operation parameter list,
     * usually used for counts. These integer values do not have an associated unit of measure.
     *
     * @return The sequence of values represented by this parameter.
     * @throws InvalidParameterTypeException if the value is not an array of <code>int</code>s.
     *
     * @see #setValue(Object)
     * @see #intValue
     */
    public int[] intValueList() throws InvalidParameterTypeException {
        if (value instanceof int[]) {
            return (int[]) value;
        } else {
            throw new InvalidParameterTypeException(getClassTypeError(), getName(descriptor));
        }
    }

    /**
     * Returns a reference to a file or a part of a file containing one or more parameter
     * values. When referencing a part of a file, that file must contain multiple identified
     * parts, such as an XML encoded document. Furthermore, the referenced file or part of a
     * file can reference another part of the same or different files, as allowed in XML documents.
     *
     * @return The reference to a file containing parameter values.
     * @throws InvalidParameterTypeException if the value is not a reference to a file or an URL.
     *
     * @see #getValue
     * @see #setValue(Object)
     */
    public URL valueFile() throws InvalidParameterTypeException {
        if (value instanceof URL) {
            return (URL) value;
        } else {
            throw new InvalidParameterTypeException(getClassTypeError(), getName(descriptor));
        }
    }

    /**
     * Returns the parameter value as an object. The object type is typically a {@link Double},
     * {@link Integer}, {@link Boolean}, {@link String}, {@link URL}, <code>double[]</code> or
     * <code>int[]</code>.
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
            throw new IllegalStateException(Resources.format(
                      ResourceKeys.ERROR_UNITLESS_PARAMETER_$1, getName(descriptor)));
        }
        final int expectedID = getUnitMessageID(targetUnit);
        if (getUnitMessageID(unit) != expectedID) {
            throw new IllegalArgumentException(Resources.format(expectedID, unit));
        }
        final Double converted = wrap(unit.getConverterTo(targetUnit).convert(value));
        ensureValidValue((ParameterDescriptor) descriptor, converted);
        this.value = wrap(value);
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
        final Double check = wrap(value);
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
        final Integer check = wrap(value);
        ensureValidValue((ParameterDescriptor) descriptor, check);
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
     * {@link Integer}, {@link Boolean}, {@link String}, {@link URL}, <code>double[]</code>
     * or <code>int[]</code>.
     *
     * @param  value The parameter value.
     * @throws InvalidParameterValueException if the type of <code>value</code> is inappropriate
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
            throw new IllegalStateException(Resources.format(
                      ResourceKeys.ERROR_UNITLESS_PARAMETER_$1, getName(descriptor)));
        }
        final int expectedID = getUnitMessageID(targetUnit);
        if (getUnitMessageID(unit) != expectedID) {
            throw new IllegalArgumentException(Resources.format(expectedID, unit));
        }
        final double[] converted = (double[]) values.clone();
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
     * @param  object The object to compare to <code>this</code>.
     * @return <code>true</code> if both objects are equal.
     */
    public boolean equals(final Object object) {
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
    public int hashCode() {
        int code = super.hashCode()*37;
        if (value != null) code +=   value.hashCode();
        if (unit  != null) code += 37*unit.hashCode();
        return code;
    }
    
    /**
     * Set the Parameter value of the provided text.
     * 
     * @param text parsed using valueOf( text )
     */
    public void parse( String text ) throws IOException {
        setValue( valueOf( text ) );
    }
    /** Text representation of parameter value.
     * <p>
     * Should be suitable for use with parse( text )
     * @return
     */
    public String text(){
        Object obj = getValue();
        if( obj == null ){
            return null;
        }
        return obj.toString();
    }
    /**
     * Parses the text into a value for this Parameter.
     * <p>
     * Default implementation uses reflection to look for a constructor
     * that takes a single String.
     * </p>
     * @param text Text to parse
     * @return value value of type getDescriptor().getValueClass()
     * 
     * @throws IOException If text could not be parsed to getDescriptor().getValueClass()
     */
    protected Object valueOf( String text ) throws IOException {
        
        ParameterDescriptor descriptor =
            (ParameterDescriptor) this.getDescriptor();
        
        Class type = descriptor.getValueClass();
        
        if (text == null) {            
            return null;
        }
        if (type == String.class) {
            return text;
        }
        if (text.length() == 0) {
            return null;
        }

        Constructor constructor;

        try {
            constructor = type.getConstructor(new Class[] { String.class });
        } catch (SecurityException e) {
            //  type( String ) constructor is not public
            throw new IOException("Could not create " + type.getName()
                + " from text");
        } catch (NoSuchMethodException e) {
            // No type( String ) constructor
            throw new IOException("Could not create " + type.getName()
                + " from text");
        }

        try {
            return constructor.newInstance(new Object[] { text, });
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new DataSourceException("Could not create "
                + type.getName() + ": from '" + text + "'",
                illegalArgumentException);
        } catch (InstantiationException instantiaionException) {
            throw new DataSourceException("Could not create "
                + type.getName() + ": from '" + text + "'",
                instantiaionException);
        } catch (IllegalAccessException illegalAccessException) {
            throw new DataSourceException("Could not create "
                + type.getName() + ": from '" + text + "'",
                illegalAccessException);
        } catch (InvocationTargetException targetException) {
            Throwable cause = targetException.getCause();
            throw new DataSourceException( cause );
            
        }
    }
               
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String name = descriptor.getName().toString( null );
        Object value = getValue();
        
        StringBuffer buf = new StringBuffer();
        buf.append( "[<" );
        buf.append( descriptor.getName().toString( null ) );
        buf.append( "> " );
        if( value == null ){
            buf.append( "null" );
        }
        else if( value.getClass().isArray() ){
            int length = Array.getLength( value );
            if( length == 0 ){
                buf.append( "(,)" );
            }
            else {
                buf.append( "(");
                for( int i = 0; i< Math.min(5,length);i++){
                    buf.append( Array.get( value, 0 ) );
                    buf.append( "," );
                }
                if( length > 5 ){
                    buf.append( "..." );
                }
                buf.append(")");
            }
        }
        else {
            buf.append( value );            
        }        
        buf.append("]");
        return buf.toString();
    }
}

