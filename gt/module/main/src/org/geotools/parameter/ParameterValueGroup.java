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
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.LinkedHashMap;
import java.util.Locale;

// OpenGIS dependencies
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.OperationParameterGroup;
import org.opengis.parameter.GeneralOperationParameter;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.InvalidParameterNameException;

// Geotools dependencies
import org.geotools.referencing.Info;
import org.geotools.referencing.Identifier;  // For javadoc
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * A group of related parameter values. The same group can be repeated more than once in an
 * {@linkplain org.geotools.referencing.operation.Operation operation} or higher level
 * <code>ParameterValueGroup</code>, if those instances contain different values of one
 * or more {@link org.geotools.parameter.ParameterValue}s which suitably distinquish among
 * those groups.
 *  
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see org.geotools.parameter.OperationParameterGroup
 * @see org.geotools.parameter.ParameterValue
 */
public class ParameterValueGroup extends org.geotools.parameter.GeneralParameterValue
                              implements org.opengis.parameter.ParameterValueGroup
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -1985309386356545126L;

    /**
     * The {@linkplain #getValues parameter values} for this group.
     * Note: consider as final. This field is not final only in order
     * to allows {@link #clone} to work.
     */
    private GeneralParameterValue[] values;

    /**
     * Construct a parameter group from the specified descriptor.
     * All {@linkplain #getValues parameter values} will be initialized
     * to their default value.
     *
     * @param descriptor The descriptor for this group.
     */
    public ParameterValueGroup(final OperationParameterGroup descriptor) {
        super(descriptor);
        final GeneralOperationParameter[] parameters = descriptor.getParameters();
        values = new GeneralParameterValue[parameters.length];
        for (int i=0; i<values.length; i++) {
            ensureNonNull("createValue", values[i] = parameters[i].createValue());
        }
    }

    /**
     * Construct a parameter group from the specified descriptor and list of parameters.
     *
     * @param descriptor The descriptor for this group.
     * @param values The list of parameter values.
     */
    public ParameterValueGroup(final OperationParameterGroup descriptor,
                                     GeneralParameterValue[] values)
    {
        super(descriptor);
        ensureNonNull("values", values);
        this.values = values = (GeneralParameterValue[]) values.clone();
        final GeneralOperationParameter[] parameters = descriptor.getParameters();
        final Map occurences = new LinkedHashMap(Math.round(parameters.length/0.75f)+1, 0.75f);
        for (int i=0; i<parameters.length; i++) {
            ensureNonNull("parameters", parameters, i);
            occurences.put(parameters[i], new int[1]);
        }
        ensureValidOccurs(values, occurences);
    }

    /**
     * Construct a parameter group from the specified list of parameters.
     *
     * @param properties The properties for the
     *        {@linkplain org.geotools.parameter.OperationParameterGroup operation parameter group}
     *        to construct from the list of parameters.
     * @param values The list of parameter values.
     */
    public ParameterValueGroup(final Map properties, final GeneralParameterValue[] values) {
        super(createDescriptor(properties, values));
        this.values = (GeneralParameterValue[]) values.clone();
    }

    /**
     * Work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private static OperationParameterGroup createDescriptor(final Map properties,
                                                            final GeneralParameterValue[] values)
    {
        ensureNonNull("values", values);
        final Map occurences = new LinkedHashMap(Math.round(values.length/0.75f)+1, 0.75f);
        for (int i=0; i<values.length; i++) {
            ensureNonNull("values", values, i);
            occurences.put(values[i].getDescriptor(), new int[1]);
        }
        ensureValidOccurs(values, occurences);
        final Set descriptors = occurences.keySet();
        return new org.geotools.parameter.OperationParameterGroup(properties,
                                          (GeneralOperationParameter[]) descriptors.toArray(
                                          new GeneralOperationParameter[descriptors.size()]));
    }

    /**
     * Make sure that the number of occurences of each values is inside the expected range.
     *
     * @param values The list of parameter values.
     * @param occurences A map of the number of occurences of a value for each descriptor.
     *        The key must be {@link GeneralOperationParameter} instances and the values
     *        must be <code>int[]</code> array of length 1 initialized with the 0 value.
     */
    private static void ensureValidOccurs(final GeneralParameterValue[] values,
                                          final Map occurences)
    {
        /*
         * Count the parameters occurences.
         */
        for (int i=0; i<values.length; i++) {
            ensureNonNull("values", values, i);
            final GeneralOperationParameter descriptor = values[i].getDescriptor();
            final int[] count = (int[]) occurences.get(descriptor);
            if (count == null) {
                throw new IllegalArgumentException(Resources.format(
                          ResourceKeys.ERROR_ILLEGAL_DESCRIPTOR_FOR_PARAMETER_$1,
                          descriptor.getName(Locale.getDefault())));
            }
            count[0]++;
        }
        /*
         * Now check if the occurences are in the expected ranges.
         */
        for (final Iterator it=occurences.entrySet().iterator(); it.hasNext();) {
            final Map.Entry entry = (Map.Entry) it.next();
            final GeneralOperationParameter descriptor = (GeneralOperationParameter) entry.getKey();
            final int count = ((int[]) entry.getValue())[0];
            final int min   = descriptor.getMinimumOccurs();
            final int max   = descriptor.getMaximumOccurs();
            if (!(count>=min && count<=max)) {
                throw new IllegalArgumentException(Resources.format(
                          ResourceKeys.ERROR_ILLEGAL_OCCURS_FOR_PARAMETER_$4,
                          descriptor.getName(Locale.getDefault()), new Integer(count),
                          new Integer(min), new Integer(max)));
            }
        }
    }

    /**
     * Returns the values in this group.
     *
     * @return The values.
     */
    public GeneralParameterValue[] getValues() {
        return (GeneralParameterValue[]) values.clone();
    }

    /**
     * Returns the value at the specified index.
     *
     * @param  The zero-based index.
     * @return The value at the specified index.
     * @throws IndexOutOfBoundsException if the specified index is out of bounds.
     */
    final GeneralParameterValue getValue(final int index) throws IndexOutOfBoundsException {
        return values[index];
    }

    /**
     * Returns the first value in this group for the specified {@linkplain Identifier#getCode
     * identifier code}. If no {@linkplain org.geotools.parameter.ParameterValue parameter value}
     * is found for the given code, then this method search recursively in subgroups (if any).
     * This convenience method provides a way to get and set parameter values by name. For example
     * the following idiom fetches a floating point value for the <code>"false_easting"</code>
     * parameter:
     * <br><br>
     * <blockquote><code>
     * double value = getValue("false_easting").{@linkplain
     * org.geotools.parameter.ParameterValue#doubleValue() doubleValue()};
     * </code></blockquote>
     *
     * @param  name The case insensitive {@linkplain Identifier#getCode identifier code} of the
     *              parameter to search for. If this string contains the <code>':'</code> character,
     *              then the part before <code>':'</code> is the {@linkplain Identifier#getCodeSpace
     *              code space}.
     * @return The parameter value for the given identifier code.
     * @throws ParameterNotFoundException if there is no parameter value for the given identifier code.
     */
    public ParameterValue getValue(String name) throws ParameterNotFoundException {
        ensureNonNull("name", name);
        name = name.trim();
        List subgroups = null;
        GeneralParameterValue[] values = this.values;
        while (values != null) {
            for (int i=0; i<values.length; i++) {
                final GeneralParameterValue value = values[i];
                if (value instanceof ParameterValue) {
                    if (Info.identifierMatches(value.getDescriptor(), name)) {
                        return (ParameterValue) value;
                    }
                } else if (value instanceof org.opengis.parameter.ParameterValueGroup) {
                    if (subgroups == null) {
                        subgroups = new LinkedList();
                    }
                    assert !subgroups.contains(value) : value;
                    subgroups.add(value);
                }
            }
            /*
             * Looks in subgroups only after all parameters in the current group have been verified.
             * Search in a "first in, first out" basis.
             */
            if (subgroups==null || subgroups.isEmpty()) {
                break;
            }
            values = ((org.opengis.parameter.ParameterValueGroup) subgroups.remove(0)).getValues();
        }
        throw new ParameterNotFoundException(Resources.format(
                  ResourceKeys.ERROR_MISSING_PARAMETER_$1, name), name);
    }
    
    /**
     * Compares the specified object with this parameter for equality.
     *
     * @param  object The object to compare to <code>this</code>.
     * @return <code>true</code> if both objects are equal.
     */
    public boolean equals(final Object object) {
        if (super.equals(object)) {
            final ParameterValueGroup that = (ParameterValueGroup) object;
            // TODO: We should use Arrays.deepEquals instead in J2SE 1.5.
            if (this.values.length == that.values.length) {
                for (int i=0; i<values.length; i++) {
                    if (!Utilities.equals(this.values[i], that.values[i])) {
                        return false;
                    }
                }
                return true;
            }
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
        int code = super.hashCode();
        // TODO: We should use Arrays.deepHashCode instead in J2SE 1.5.
        for (int i=0; i<values.length; i++) {
            code = code*37 + values[i].hashCode();
        }
        return code;
    }

    /**
     * Returns a copy of this group of parameter values.
     * Included parameter values and subgroups are cloned recursively.
     *
     * @return A copy of this group of parameter values.
     */
    public Object clone() {
        final ParameterValueGroup copy = (ParameterValueGroup) super.clone();
        copy.values = (GeneralParameterValue[]) copy.values.clone();
        for (int i=0; i<copy.values.length; i++) {
            copy.values[i] = (GeneralParameterValue) copy.values[i].clone();
        }
        return copy;
    }
}
