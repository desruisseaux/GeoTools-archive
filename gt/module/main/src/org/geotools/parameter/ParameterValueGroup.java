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

// OpenGIS dependencies
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.OperationParameterGroup;
import org.opengis.parameter.GeneralOperationParameter;
import org.opengis.parameter.ParameterNotFoundException;

// Geotools dependencies
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
     */
    public ParameterValueGroup(final OperationParameterGroup descriptor) {
        super(descriptor);
        ensureNonNull("values", values);
        final GeneralOperationParameter[] parameters = descriptor.getParameters();
        values = new GeneralParameterValue[parameters.length];
        for (int i=0; i<values.length; i++) {
            ensureNonNull("createValue", values[i] = parameters[i].createValue());
        }
    }

    /**
     * Construct a parameter group from the specified list of parameters.
     *
     * @param properties The properties for the
     *        {@linkplain org.geotools.parameter.OperationParameterGroup operation parameter group}
     *        to construct from the list of parameters.
     * @param values The list of parameters.
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
        final GeneralOperationParameter[] parameters = new GeneralOperationParameter[values.length];
        for (int i=0; i<parameters.length; i++) {
            ensureNonNull("values", values[i]);
            parameters[i] = values[i].getDescriptor();
        }
        return new org.geotools.parameter.OperationParameterGroup(properties, parameters);
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
     * Returns the first value in this group for the specified name. If no
     * {@linkplain org.geotools.parameter.ParameterValue parameter value} or
     * group is found for the given name, then this method search recursively
     * in subgroups (if any).
     *
     * @param  name The case insensitive name of the parameter to search for.
     * @return The parameter value for the given name.
     * @throws ParameterNotFoundException if there is no parameter for the given name.
     */
    public GeneralParameterValue getValue(String name) throws ParameterNotFoundException {
        ensureNonNull("name", name);
        name = name.trim();
        for (int i=0; i<values.length; i++) {
            final GeneralParameterValue value = values[i];
            if (name.equals(value.getDescriptor().getName(null))) {
                return value;
            }
        }
        for (int i=0; i<values.length; i++) {
            final GeneralParameterValue value = values[i];
            if (value instanceof ParameterValueGroup) try {
                return ((ParameterValueGroup) value).getValue(name);
            } catch (ParameterNotFoundException exception) {
                // Parameter not found. Ignore for now (search in others subgroups).
            }
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
