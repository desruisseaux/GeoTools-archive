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

// J2SE direct dependencies
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.Collections;
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.util.CodeList;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.referencing.IdentifiedObject;
import org.geotools.resources.Utilities;
import org.geotools.resources.ClassChanger;
import org.geotools.resources.rsc.Resources;
import org.geotools.resources.rsc.ResourceKeys;
import org.geotools.util.SimpleInternationalString;


/**
 * The definition of a parameter used by an operation method.
 * For {@linkplain org.geotools.referencing.crs.CoordinateReferenceSystem Coordinate
 * Reference Systems} most parameter values are numeric, but other types
 * of parameter values are possible.
 *
 * <P>For numeric values, the {@linkplain #getValueClass value class} is usually
 * <code>{@linkplain Double}.class</code>, <code>{@linkplain Integer}.class</code> or
 * some other Java wrapper class.</P>
 *  
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see org.geotools.parameter.Parameter
 * @see org.geotools.parameter.ParameterDescriptorGroup
 */
public class ParameterDescriptor extends AbstractParameterDescriptor
             implements org.opengis.parameter.ParameterDescriptor
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -295668622297737705L;

    /**
     * The class that describe the type of the parameter.
     * This class can never be a primitive.
     */
    private final Class valueClass;

    /**
     * The class that describe the type of the parameter, maybe as a primitive. This is the
     * value class that the user specified at construction time.  This is usually identical
     * to <code>valueClass</code>. However, some optimization may be done for some primitive
     * types, for example a special implementation of {@link Parameter} for the
     * <code>double</code> type.
     */
    private final Class primitiveClass;

    /**
     * A immutable, finite set of valid values (usually from a {linkplain org.opengis.util.CodeList
     * code list}) or <code>null</code> if it doesn't apply. This set is immutable.
     */
    private final Set validValues;

    /**
     * The default value for the parameter, or <code>null</code>.
     */
    private final Object defaultValue;

    /**
     * The minimum parameter value, or <code>null</code>.
     */
    private final Comparable minimum;

    /**
     * The maximum parameter value, or <code>null</code>.
     */
    private final Comparable maximum;

    /**
     * The unit for default, minimum and maximum values, or <code>null</code>.
     */
    private final Unit unit;

    /**
     * Constructs a parameter for a range of integer values.
     *
     * @param name The parameter name.
     * @param defaultValue The default value for the parameter.
     * @param minimum The minimum parameter value, or {@link Integer#MIN_VALUE} if none.
     * @param maximum The maximum parameter value, or {@link Integer#MAX_VALUE} if none.
     */
    public ParameterDescriptor(final String name,
                               final int defaultValue,
                               final int minimum,
                               final int maximum)
    {
        this(name, Integer.class, Parameter.wrap(defaultValue),
             minimum == Integer.MIN_VALUE ? null : Parameter.wrap(minimum),
             maximum == Integer.MAX_VALUE ? null : Parameter.wrap(maximum), null);
    }

    /**
     * Constructs a parameter for a range of floating point values. The parameter is mandatory
     * if no default value is specified (i.e. <code>defaultValue</code> is <code>NaN</code>).
     * Otherwise, the parameter will be optional.
     *
     * @param name The parameter name.
     * @param defaultValue The default value for the parameter, or {@link Double#NaN} if none.
     * @param minimum The minimum parameter value, or {@link Double#NEGATIVE_INFINITY} if none.
     * @param maximum The maximum parameter value, or {@link Double#POSITIVE_INFINITY} if none.
     * @param unit    The unit for default, minimum and maximum values.
     */
    public ParameterDescriptor(final String name,
                               final double defaultValue,
                               final double minimum,
                               final double maximum,
                               final Unit   unit)
    {
        this(name, Double.class,
             Double.isNaN(defaultValue)          ? null : Parameter.wrap(defaultValue),
             minimum == Double.NEGATIVE_INFINITY ? null : Parameter.wrap(minimum),
             maximum == Double.POSITIVE_INFINITY ? null : Parameter.wrap(maximum), unit);
    }

    /**
     * Constructs a parameter from a range of comparable objects. The parameter is mandatory
     * if no default value is specified (i.e. <code>defaultValue</code> is <code>null</code>).
     * Otherwise, the parameter will be optional.
     *
     * @param name The parameter name.
     * @param valueClass The class that describe the type of the parameter.
     * @param defaultValue The default value for the parameter, or <code>null</code>.
     * @param minimum The minimum parameter value, or <code>null</code>.
     * @param maximum The maximum parameter value, or <code>null</code>.
     * @param unit    The unit for default, minimum and maximum values, or <code>null</code>.
     */
    public ParameterDescriptor(final String     name,
                               final Class      valueClass,
                               final Comparable defaultValue,
                               final Comparable minimum,
                               final Comparable maximum,
                               final Unit       unit)
    {
        this(Collections.singletonMap(NAME_PROPERTY, name),
          (defaultValue!=null) ? 0 : 1, 1, valueClass, null, defaultValue, minimum, maximum, unit);
    }

    /**
     * Construct a parameter for a {@linkplain CodeList code list} (or enumeration).
     *
     * @param name         The parameter name.
     * @param defaultValue The default value.
     */
    public ParameterDescriptor(final String   name,
                               final CodeList defaultValue)
    {
        this(name, defaultValue.getClass(), defaultValue);
    }

    /**
     * Construct a parameter for a {@linkplain CodeList code list} (or enumeration).
     *
     * @param name         The parameter name.
     * @param valueClass   The class that describe the type of the parameter.
     *                     Must be a subclass of {@link CodeList}.
     * @param defaultValue The default value, or <code>null</code>.
     */
    ParameterDescriptor(final String   name,
                        final Class    valueClass,
                        final CodeList defaultValue)
    {
        this(name, valueClass, getCodeLists(valueClass), defaultValue);
    }

    /**
     * Returns the enumeration found in the specified <code>CodeList</code> class.
     * Returns <code>null</code> if no values were found.
     */
    private static CodeList[] getCodeLists(final Class type) {
        try {
            return (CodeList[]) type.getMethod("values", (Class[])null)
                                    .invoke(null, (Object[]) null);
        } catch (Exception exception) {
            // No code list defined. Not a problem; we will just
            // not provided any set of code to check against.
            return null;
        }
    }

    /**
     * Construct a parameter for a name and a default value. The parameter type will
     * be assumed the same than the default value class.
     * 
     * @param name The parameter name.
     * @param description An optional description.
     * @param defaultValue The default value.
     * @param required <code>true</code> if this parameter is required,
     *        <code>false</code> otherwise.
     *
     * @deprecated Use the constructor with an {@link InternationalString} argument instead.
     */
    public ParameterDescriptor(final String   name,
                               final String   description,
                               final Object   defaultValue,
                               final boolean  required )
    {
        this(name, new SimpleInternationalString(description), defaultValue, required);
    }

    /**
     * Construct a parameter for a name and a default value. The parameter type will
     * be assumed the same than the default value class.
     * 
     * @param name The parameter name.
     * @param description An optional description.
     * @param defaultValue The default value.
     * @param required <code>true</code> if this parameter is required,
     *        <code>false</code> otherwise.
     */
    public ParameterDescriptor(final String              name,
                               final InternationalString description,
                               final Object              defaultValue,
                               final boolean             required )
    {
        this(toMap(name, description),
             required ? 1 : 0,
             1,
             defaultValue.getClass(),
             (defaultValue instanceof CodeList) ? getCodeLists(defaultValue.getClass()) : null,
             defaultValue,
             null,
             null,
             null);
    }

    /**
     * Work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private static final Map toMap(final String name, final InternationalString remarks) {
        if (remarks == null ){
            return Collections.singletonMap(NAME_PROPERTY, name);
        }
        final Map properties = new HashMap(4);
        properties.put(NAME_PROPERTY,   name);
        properties.put(REMARKS_PROPERTY, remarks);
        return properties;        
    }

    /**
     * Construct a parameter for a set of predefined values.
     *
     * @param name The parameter name.
     * @param valueClass The class that describe the type of the parameter.
     * @param validValues A finite set of valid values (usually from a
     *        {linkplain org.opengis.util.CodeList code list}) or <code>null</code>
     *        if it doesn't apply.
     * @param defaultValue The default value for the parameter, or <code>null</code>.
     */
    public ParameterDescriptor(final String   name,
                               final Class    valueClass,
                               final Object[] validValues,
                               final Object   defaultValue)
    {
        this(Collections.singletonMap("name", name),
             (defaultValue!=null) ? 0 : 1, 1, valueClass, validValues, defaultValue, null, null, null);
    }

    /**
     * Construct a parameter from a set of properties. The properties map is
     * given unchanged to the {@linkplain IdentifiedObject#IdentifiedObject(Map)
     * super-class constructor}.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param minimumOccurs The {@linkplain #getMinimumOccurs minimum number of times}
     *        that values for this parameter group or parameter are required.
     * @param maximumOccurs The {@linkplain #getMaximumOccurs maximum number of times}
     *        that values for this parameter group or parameter are required.
     * @param valueClass The class that describe the type of the parameter.
     * @param validValues A finite set of valid values (usually from a
     *        {linkplain org.opengis.util.CodeList code list}) or <code>null</code>
     *        if it doesn't apply.
     * @param defaultValue The default value for the parameter, or <code>null</code>.
     * @param minimum The minimum parameter value, or <code>null</code>.
     * @param maximum The maximum parameter value, or <code>null</code>.
     * @param unit    The unit for default, minimum and maximum values.
     */
    public ParameterDescriptor(final Map        properties,
                               final int        minimumOccurs,
                               final int        maximumOccurs,
                                     Class      valueClass,
                               final Object[]   validValues,
                               final Object     defaultValue,
                               final Comparable minimum,
                               final Comparable maximum,
                               final Unit       unit)
    {
        super(properties, minimumOccurs, maximumOccurs);
        this.primitiveClass = valueClass;
        this.defaultValue   = defaultValue;
        this.minimum        = minimum;
        this.maximum        = maximum;
        this.unit           = unit;
        ensureNonNull("valueClass", valueClass);
        if (valueClass.isPrimitive()) {
            valueClass = ClassChanger.toWrapper(valueClass);
        }
        this.valueClass = valueClass;
        AbstractParameter.ensureValidClass(valueClass, defaultValue);
        AbstractParameter.ensureValidClass(valueClass, minimum);
        AbstractParameter.ensureValidClass(valueClass, maximum);
        if (minimum!=null && maximum!=null) {
            if (minimum.compareTo(maximum) > 0) {
                throw new IllegalArgumentException(Resources.format(
                          ResourceKeys.ERROR_BAD_RANGE_$2, minimum, maximum));
            }
        }
        if (validValues != null) {
            final Set valids = new HashSet(Math.max(validValues.length*4/3 + 1, 8), 0.75f);
            for (int i=0; i<validValues.length; i++) {
                final Object value = validValues[i];
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
     * Creates a new instance of {@linkplain org.geotools.parameter.Parameter parameter value}
     * initialized with the {@linkplain #getDefaultValue default value}.
     * The {@linkplain org.geotools.parameter.Parameter#getDescriptor parameter value
     * descriptor} for the created parameter value will be <code>this</code> object.
     * <br><br>
     * If the {@linkplain #getValueClass value class} specified at construction time was
     * a primitive type (e.g. <code>Double.{@linkplain Double#TYPE TYPE}</code> instead
     * of <code>{@linkplain Double}.class</code>), then this method may returns a specialized
     * parameter value implementation for this primitive type. Specialized implementations may
     * use less storage space and be more flexible during conversions, but this flexibility is
     * not always wanted.
     */
    public org.opengis.parameter.GeneralParameterValue createValue() {
        if (Double.TYPE.equals(primitiveClass)) {
            return new ParameterReal(this);
        }
        return new Parameter(this);
    }

    /**
     * Returns the class that describe the type of the parameter. If the value class specified
     * at construction time was a primitive type (e.g. <code>double</code>), it is converted to
     * the corresponding wrapper class (e.g. {@link Double}).
     *
     * @return The parameter value class (never a primitive type).
     */
    public Class getValueClass() {
        return valueClass;
    }

    /**
     * If this parameter allows only a finite set of values, returns this set.
     * This set is usually a {linkplain org.opengis.util.CodeList code list} or
     * enumerations. This method returns <code>null</code> if this parameter
     * doesn't limits values to a finite set.
     *
     * @return A finite set of valid values (usually from a
     *         {linkplain org.opengis.util.CodeList code list}),
     *         or <code>null</code> if it doesn't apply.
     */
    public Set getValidValues() {
        return validValues;
    }

    /**
     * Returns the default value for the parameter. The return type can be any type
     * including a {@link Number} or a {@link String}. If there is no default value,
     * then this method returns <code>null</code>.
     *
     * @return The default value, or <code>null</code> in none.
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * Returns the minimum parameter value. If there is no minimum value, or if minimum
     * value is inappropriate for the {@linkplain #getValueClass parameter type}, then
     * this method returns <code>null</code>.
     *
     * @return The minimum parameter value (often an instance of {@link Double}),
     *         or <code>null</code>.
     */
    public Comparable getMinimumValue() {
        return minimum;
    }

    /**
     * Returns the maximum parameter value. If there is no maximum value, or if maximum
     * value is inappropriate for the {@linkplain #getValueClass parameter type}, then
     * this method returns <code>null</code>.
     *
     * @return The minimum parameter value (often an instance of {@link Double}),
     *         or <code>null</code>.
     */
    public Comparable getMaximumValue() {
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
     * @return The unit for numeric value, or <code>null</code> if it
     *         doesn't apply to the value type.
     */
    public Unit getUnit() {
        return unit;
    }
    
    /**
     * Compares the specified object with this parameter for equality.
     *
     * @param  object The object to compare to <code>this</code>.
     * @param  compareMetadata <code>true</code> for performing a strict comparaison, or
     *         <code>false</code> for comparing only properties relevant to transformations.
     * @return <code>true</code> if both objects are equal.
     */
    public boolean equals(final IdentifiedObject object, final boolean compareMetadata) {
        if (object == this) {
            return true;
        }
        if (super.equals(object, compareMetadata)) {
            final ParameterDescriptor that = (ParameterDescriptor) object;
            return Utilities.equals(this.primitiveClass, that.primitiveClass)   &&
                   Utilities.equals(this.validValues,    that.validValues)  &&
                   Utilities.equals(this.defaultValue,   that.defaultValue) &&
                   Utilities.equals(this.minimum,        that.minimum)      &&
                   Utilities.equals(this.maximum,        that.maximum)      &&
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
    public int hashCode() {
        int code = super.hashCode()*37 + valueClass.hashCode();
        if (defaultValue != null) code += (37)      *defaultValue.hashCode();
        if (minimum      != null) code += (37*37)   *minimum     .hashCode();
        if (maximum      != null) code += (37*37*37)*maximum     .hashCode();
        if (unit         != null) code +=            unit        .hashCode();
        return code;
    }
}
