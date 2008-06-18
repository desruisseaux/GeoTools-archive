/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.measure.unit.Unit;

import org.opengis.util.CodeList;
import org.opengis.metadata.citation.Citation;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValue;

import org.geotools.referencing.AbstractIdentifiedObject;
import org.geotools.referencing.NamedIdentifier;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.util.Utilities;


/**
 * The definition of a parameter used by an operation method.
 * For {@linkplain org.opengis.referencing.crs.CoordinateReferenceSystem Coordinate
 * Reference Systems} most parameter values are numeric, but other types
 * of parameter values are possible.
 * <P>
 * For numeric values, the {@linkplain #getValueClass value class} is usually
 * <code>{@linkplain Double}.class</code>, <code>{@linkplain Integer}.class</code> or
 * some other Java wrapper class.
 * <P>
 * This class contains numerous convenience constructors. But all of them ultimately invoke
 * {@linkplain #DefaultParameterDescriptor(Map,Class,Object[],Object,Comparable,Comparable,Unit,boolean)
 * a single, full-featured constructor}. All other constructors are just shortcuts.
 *
 * @param <T> The type of elements to be returned by {@link ParameterValue#getValue}.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux (IRD)
 *
 * @see Parameter
 * @see DefaultParameterDescriptorGroup
 */
public class DefaultParameterDescriptor<T> extends AbstractParameterDescriptor
        implements ParameterDescriptor<T>
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -295668622297737705L;

    /**
     * The class that describe the type of the parameter.
     * This is the value class that the user specified at construction time.
     */
    private final Class<T> valueClass;

    /**
     * A immutable, finite set of valid values (usually from a {linkplain org.opengis.util.CodeList
     * code list}) or {@code null} if it doesn't apply. This set is immutable.
     */
    private final Set<T> validValues;

    /**
     * The default value for the parameter, or {@code null}.
     */
    private final T defaultValue;

    /**
     * The minimum parameter value, or {@code null}.
     */
    private final Comparable<T> minimum;

    /**
     * The maximum parameter value, or {@code null}.
     */
    private final Comparable<T> maximum;

    /**
     * The unit for default, minimum and maximum values, or {@code null}.
     */
    private final Unit<?> unit;

    /**
     * Constructs a descriptor with the same values than the specified one. This copy constructor
     * may be used in order to wraps an arbitrary implementation into a Geotools one.
     *
     * @param descriptor The descriptor to copy.
     *
     * @since 2.2
     */
    public DefaultParameterDescriptor(final ParameterDescriptor<T> descriptor) {
        super(descriptor);
        valueClass   = descriptor.getValueClass();
        validValues  = descriptor.getValidValues();
        defaultValue = descriptor.getDefaultValue();
        minimum      = descriptor.getMinimumValue();
        maximum      = descriptor.getMaximumValue();
        unit         = descriptor.getUnit();
    }

    /**
     * Constructs a mandatory parameter for a range of integer values.
     *
     * @param name The parameter name.
     * @param defaultValue The default value for the parameter.
     * @param minimum The minimum parameter value, or {@link Integer#MIN_VALUE} if none.
     * @param maximum The maximum parameter value, or {@link Integer#MAX_VALUE} if none.
     *
     * @deprecated Needs to move in a factory class.
     */
    @Deprecated
    public DefaultParameterDescriptor(final String name,
                                      final int defaultValue,
                                      final int minimum,
                                      final int maximum)
    {
        this(Collections.singletonMap(NAME_KEY, name),
             defaultValue, minimum, maximum, true);
    }

    /**
     * Constructs a parameter for a range of integer values.
     *
     * @param properties The parameter properties (name, identifiers, alias...).
     * @param defaultValue The default value for the parameter.
     * @param minimum The minimum parameter value, or {@link Integer#MIN_VALUE} if none.
     * @param maximum The maximum parameter value, or {@link Integer#MAX_VALUE} if none.
     * @param required {@code true} if this parameter is required, {@code false} otherwise.
     *
     * @deprecated Should move to a static factory method (required for getting ride of warnings).
     */
    @Deprecated
    public DefaultParameterDescriptor(final Map<String,?> properties,
                                      final int defaultValue,
                                      final int minimum,
                                      final int maximum,
                                      final boolean required)
    {
        this(properties, required, (Class<T>) Integer.class, null, (T) (Object) defaultValue,
             (Comparable<T>) (minimum == Integer.MIN_VALUE ? null : minimum),
             (Comparable<T>) (maximum == Integer.MAX_VALUE ? null : maximum), null);
    }

    /**
     * Constructs a mandatory parameter for a range of floating point values.
     *
     * @param name    The parameter name.
     * @param defaultValue The default value for the parameter, or {@link Double#NaN} if none.
     * @param minimum The minimum parameter value, or {@link Double#NEGATIVE_INFINITY} if none.
     * @param maximum The maximum parameter value, or {@link Double#POSITIVE_INFINITY} if none.
     * @param unit    The unit for default, minimum and maximum values.
     *
     * @deprecated Should move to a static factory method (required for getting ride of warnings).
     */
    @Deprecated
    public DefaultParameterDescriptor(final String name,
                                      final double defaultValue,
                                      final double minimum,
                                      final double maximum,
                                      final Unit<?> unit)
    {
        this(Collections.singletonMap(NAME_KEY, name),
             defaultValue, minimum, maximum, unit, true);
    }

    /**
     * Constructs a parameter for a range of floating point values.
     *
     * @param properties The parameter properties (name, identifiers, alias...).
     * @param defaultValue The default value for the parameter, or {@link Double#NaN} if none.
     * @param minimum The minimum parameter value, or {@link Double#NEGATIVE_INFINITY} if none.
     * @param maximum The maximum parameter value, or {@link Double#POSITIVE_INFINITY} if none.
     * @param unit    The unit for default, minimum and maximum values.
     * @param required {@code true} if this parameter is required, {@code false} otherwise.
     *
     * @deprecated Should move to a static factory method (required for getting ride of warnings).
     */
    @Deprecated
    public DefaultParameterDescriptor(final Map<String,?> properties,
                                      final double  defaultValue,
                                      final double  minimum,
                                      final double  maximum,
                                      final Unit<?> unit,
                                      final boolean required)
    {
        this(properties, required, (Class<T>) Double.class, null,
             (T) (Double.isNaN(defaultValue)          ? null : defaultValue),
             (Comparable<T>) (minimum == Double.NEGATIVE_INFINITY ? null : minimum),
             (Comparable<T>) (maximum == Double.POSITIVE_INFINITY ? null : maximum), unit);
    }

    /**
     * Constructs a parameter for a name and a default value. The parameter type will
     * be assumed the same than the default value class.
     *
     * @param name         The parameter name.
     * @param remarks      An optional description as a {@link String} or an
     *                     {@link org.opengis.util.InternationalString}, or {@code null} if none.
     * @param defaultValue The default value.
     * @param required     {@code true} if this parameter is required, {@code false} otherwise.
     *
     * @deprecated Should move to a static factory method (required for getting ride of warnings).
     */
    @Deprecated
    public DefaultParameterDescriptor(final String       name,
                                      final CharSequence remarks,
                                      final T            defaultValue,
                                      final boolean      required)
    {
        this(toMap(name, remarks),
             (Class<T>) defaultValue.getClass(),
             (T[]) ((defaultValue instanceof CodeList) ? getCodeLists((Class) defaultValue.getClass()) : null),
             defaultValue,
             null,
             null,
             null,
             required);
    }

    /**
     * Work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private static final Map<String,?> toMap(final String name, final CharSequence remarks) {
        if (remarks == null ){
            return Collections.singletonMap(NAME_KEY, name);
        }
        final Map<String,Object> properties = new HashMap<String,Object>(4);
        properties.put(NAME_KEY,    name);
        properties.put(REMARKS_KEY, remarks);
        return properties;
    }

    /**
     * Constructs a parameter for a {@linkplain CodeList code list} (or enumeration).
     *
     * @param name         The parameter name.
     * @param defaultValue The default value.
     *
     * @deprecated Should move to a static factory method (required for getting ride of warnings).
     */
    @Deprecated
    public DefaultParameterDescriptor(final String   name,
                                      final CodeList defaultValue)
    {
        this(name, (Class<T>) defaultValue.getClass(), defaultValue);
    }

    /**
     * Constructs a parameter for a {@linkplain CodeList code list} (or enumeration). This
     * constructor is used by the {@link #DefaultParameterDescriptor(String,CodeList)} constructor.
     *
     * @param name         The parameter name.
     * @param valueClass   The class that describe the type of the parameter.
     *                     Must be a subclass of {@link CodeList}.
     * @param defaultValue The default value, or {@code null}.
     *
     * @deprecated Should move to a static factory method (required for getting ride of warnings).
     */
    @Deprecated
    DefaultParameterDescriptor(final String   name,
                               final Class<T> valueClass,
                               final CodeList defaultValue)
    {
        this(name, valueClass, (T[]) getCodeLists(valueClass.asSubclass(CodeList.class)), (T) defaultValue);
    }

    /**
     * Returns the enumeration found in the specified {@code CodeList} class.
     * Returns {@code null} if no values were found.
     */
    @SuppressWarnings("unchecked")
    private static <T extends CodeList> T[] getCodeLists(final Class<T> type) {
        try {
            return (T[]) type.getMethod("values", (Class<?>[]) null).invoke(null, (Object[]) null);
        } catch (Exception exception) {
            // No code list defined. Not a problem; we will just
            // not provide any set of code to check against.
            return null;
        }
    }

    /**
     * Constructs a mandatory parameter for a set of predefined values.
     *
     * @param name The parameter name.
     * @param valueClass The class that describe the type of the parameter.
     * @param validValues A finite set of valid values (usually from a
     *        {linkplain org.opengis.util.CodeList code list}) or {@code null}
     *        if it doesn't apply.
     * @param defaultValue The default value for the parameter, or {@code null}.
     */
    public DefaultParameterDescriptor(final String   name,
                                      final Class<T> valueClass,
                                      final T[]      validValues,
                                      final T        defaultValue)
    {
        this(Collections.singletonMap(NAME_KEY, name),
             valueClass, validValues, defaultValue, null, null, null, true);
    }

    /**
     * Constructs a parameter from an authority and a name.
     *
     * @param authority The authority (e.g.
     *        {@link org.geotools.metadata.iso.citation.Citations#OGC OGC}).
     * @param name The parameter name.
     * @param valueClass The class that describe the type of the parameter.
     * @param validValues A finite set of valid values (usually from a
     *        {linkplain org.opengis.util.CodeList code list}) or {@code null}
     *        if it doesn't apply.
     * @param defaultValue The default value for the parameter, or {@code null}.
     * @param minimum  The minimum parameter value, or {@code null}.
     * @param maximum  The maximum parameter value, or {@code null}.
     * @param unit     The unit for default, minimum and maximum values.
     * @param required {@code true} if this parameter is required,
     *                 or {@code false} if it is optional.
     *
     * @since 2.2
     */
    public DefaultParameterDescriptor(final Citation      authority,
                                      final String        name,
                                      final Class<T>      valueClass,
                                      final T[]           validValues,
                                      final T             defaultValue,
                                      final Comparable<T> minimum,
                                      final Comparable<T> maximum,
                                      final Unit<?>       unit,
                                      final boolean       required)
    {
        this(Collections.singletonMap(NAME_KEY, new NamedIdentifier(authority, name)),
             valueClass, validValues, defaultValue, minimum, maximum, unit, required);
    }

    /**
     * Constructs a parameter from a set of properties. The properties map is
     * given unchanged to the {@linkplain AbstractIdentifiedObject#AbstractIdentifiedObject(Map)
     * super-class constructor}.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param valueClass The class that describe the type of the parameter.
     * @param validValues A finite set of valid values (usually from a
     *        {linkplain org.opengis.util.CodeList code list}) or {@code null}
     *        if it doesn't apply.
     * @param defaultValue The default value for the parameter, or {@code null}.
     * @param minimum  The minimum parameter value, or {@code null}.
     * @param maximum  The maximum parameter value, or {@code null}.
     * @param unit     The unit for default, minimum and maximum values.
     * @param required {@code true} if this parameter is required,
     *                 or {@code false} if it is optional.
     */
    public DefaultParameterDescriptor(final Map<String,?> properties,
                                      final Class<T>      valueClass,
                                      final T[]           validValues,
                                      final T             defaultValue,
                                      final Comparable<T> minimum,
                                      final Comparable<T> maximum,
                                      final Unit<?>       unit,
                                      final boolean       required)
    {
        this(properties, required, valueClass, validValues, defaultValue, minimum, maximum, unit);
    }

    /**
     * Constructs a parameter from a set of properties. The properties map is given unchanged to the
     * {@linkplain AbstractIdentifiedObject#AbstractIdentifiedObject(Map) super-class constructor}.
     * <p>
     * This constructor assumes that minimum, maximum and default values are
     * already replaced by their cached values, if available.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param required {@code true} if this parameter is required, or {@code false}
     *        if it is optional.
     * @param valueClass The class that describe the type of the parameter.
     * @param validValues A finite set of valid values (usually from a
     *        {linkplain org.opengis.util.CodeList code list}) or {@code null}
     *        if it doesn't apply.
     * @param defaultValue The default value for the parameter, or {@code null}.
     * @param minimum The minimum parameter value, or {@code null}.
     * @param maximum The maximum parameter value, or {@code null}.
     * @param unit    The unit for default, minimum and maximum values.
     */
    private DefaultParameterDescriptor(final Map<String,?> properties,
                                       final boolean       required,
                                       final Class<T>      valueClass,
                                       final T[]           validValues,
                                       final T             defaultValue,
                                       final Comparable<T> minimum,
                                       final Comparable<T> maximum,
                                       final Unit<?>       unit)
    {
        super(properties, required ? 1 : 0, 1);
        this.valueClass     = valueClass;
        this.defaultValue   = defaultValue;
        this.minimum        = minimum;
        this.maximum        = maximum;
        this.unit           = unit;
        ensureNonNull("valueClass", valueClass);
        AbstractParameter.ensureValidClass(valueClass, defaultValue);
        AbstractParameter.ensureValidClass(valueClass, minimum);
        AbstractParameter.ensureValidClass(valueClass, maximum);
        if (minimum!=null && maximum!=null) {
            if (minimum.compareTo(valueClass.cast(maximum)) > 0) {
                throw new IllegalArgumentException(Errors.format(
                          ErrorKeys.BAD_RANGE_$2, minimum, maximum));
            }
        }
        if (validValues != null) {
            final Set<T> valids = new HashSet<T>(Math.max(validValues.length*4/3 + 1, 8), 0.75f);
            for (int i=0; i<validValues.length; i++) {
                final T value = validValues[i];
                AbstractParameter.ensureValidClass(valueClass, value);
                valids.add(value);
            }
            this.validValues = Collections.unmodifiableSet(valids);
        } else {
            this.validValues = null;
        }
        if (defaultValue != null) {
            Parameter.ensureValidValue(this, defaultValue);
        }
    }

    /**
     * The maximum number of times that values for this parameter group or
     * parameter can be included. For a {@linkplain DefaultParameterDescriptor
     * single parameter}, the value is always 1.
     *
     * @return The maximum occurence.
     *
     * @see #getMinimumOccurs
     */
    public int getMaximumOccurs() {
        return 1;
    }

    /**
     * Creates a new instance of {@linkplain org.geotools.parameter.Parameter parameter value}
     * initialized with the {@linkplain #getDefaultValue default value}.
     * The {@linkplain org.geotools.parameter.Parameter#getDescriptor parameter value
     * descriptor} for the created parameter value will be {@code this} object.
     *
     * @return A parameter initialized to the default value.
     */
    @SuppressWarnings("unchecked")
    public ParameterValue<T> createValue() {
        if (Double.class.equals(valueClass) && unit == null) {
            return (ParameterValue) new FloatParameter((ParameterDescriptor) this);
        }
        return new Parameter<T>(this);
    }

    /**
     * Returns the class that describe the type of the parameter.
     *
     * @return The parameter value class.
     */
    public Class<T> getValueClass() {
        return valueClass;
    }

    /**
     * If this parameter allows only a finite set of values, returns this set.
     * This set is usually a {linkplain org.opengis.util.CodeList code list} or
     * enumerations. This method returns {@code null} if this parameter
     * doesn't limits values to a finite set.
     *
     * @return A finite set of valid values (usually from a
     *         {linkplain org.opengis.util.CodeList code list}),
     *         or {@code null} if it doesn't apply.
     */
    public Set<T> getValidValues() {
        return validValues;
    }

    /**
     * Returns the default value for the parameter. The return type can be any type
     * including a {@link Number} or a {@link String}. If there is no default value,
     * then this method returns {@code null}.
     *
     * @return The default value, or {@code null} in none.
     */
    public T getDefaultValue() {
        return defaultValue;
    }

    /**
     * Returns the minimum parameter value. If there is no minimum value, or if minimum
     * value is inappropriate for the {@linkplain #getValueClass parameter type}, then
     * this method returns {@code null}.
     *
     * @return The minimum parameter value (often an instance of {@link Double}), or {@code null}.
     */
    public Comparable<T> getMinimumValue() {
        return minimum;
    }

    /**
     * Returns the maximum parameter value. If there is no maximum value, or if maximum
     * value is inappropriate for the {@linkplain #getValueClass parameter type}, then
     * this method returns {@code null}.
     *
     * @return The minimum parameter value (often an instance of {@link Double}), or {@code null}.
     */
    public Comparable<T> getMaximumValue() {
        return maximum;
    }

    /**
     * Returns the unit for
     * {@linkplain #getDefaultValue default},
     * {@linkplain #getMinimumValue minimum} and
     * {@linkplain #getMaximumValue maximum} values.
     * This attribute apply only if the values is of numeric type (usually an instance
     * of {@link Double}).
     *
     * @return The unit for numeric value, or {@code null} if it
     *         doesn't apply to the value type.
     */
    public Unit<?> getUnit() {
        return unit;
    }

    /**
     * Compares the specified object with this parameter for equality.
     *
     * @param  object The object to compare to {@code this}.
     * @param  compareMetadata {@code true} for performing a strict comparaison, or
     *         {@code false} for comparing only properties relevant to transformations.
     * @return {@code true} if both objects are equal.
     */
    @Override
    public boolean equals(final AbstractIdentifiedObject object, final boolean compareMetadata) {
        if (object == this) {
            return true;
        }
        if (super.equals(object, compareMetadata)) {
            if (!compareMetadata) {
                /*
                 * Tests for name, since parameters with different name have
                 * completly different meaning. For example there is no difference
                 * between "semi_major" and "semi_minor" parameters except the name.
                 * We don't perform this comparaison if the user asked for metadata
                 * comparaison, because in such case the names have already been
                 * compared by the subclass.
                 */
                if (!nameMatches(object. getName().getCode()) &&
                    !nameMatches(object, getName().getCode()))
                {
                    return false;
                }
            }
            final DefaultParameterDescriptor that = (DefaultParameterDescriptor) object;
            return Utilities.equals(this.validValues,    that.validValues)    &&
                   Utilities.equals(this.defaultValue,   that.defaultValue)   &&
                   Utilities.equals(this.minimum,        that.minimum)        &&
                   Utilities.equals(this.maximum,        that.maximum)        &&
                   Utilities.equals(this.unit,           that.unit);
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
        int code = super.hashCode()*37 + valueClass.hashCode();
        if (defaultValue != null) code += (37)      *defaultValue.hashCode();
        if (minimum      != null) code += (37*37)   *minimum     .hashCode();
        if (maximum      != null) code += (37*37*37)*maximum     .hashCode();
        if (unit         != null) code +=            unit        .hashCode();
        return code;
    }
}
