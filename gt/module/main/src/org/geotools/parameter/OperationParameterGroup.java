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
import java.util.Collections;

// OpenGIS dependencies
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.GeneralOperationParameter;
import org.opengis.parameter.ParameterNotFoundException;

// Geotools dependencies
import org.geotools.referencing.Info;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * The definition of a group of related parameters used by an operation method.
 *  
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see org.geotools.parameter.ParameterValueGroup
 * @see org.geotools.parameter.OperationParameter
 */
public class OperationParameterGroup extends org.geotools.parameter.GeneralOperationParameter
                                  implements org.opengis.parameter.OperationParameterGroup
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 3395510970737025480L;

    /**
     * The {@linkplain #getParameters operation parameters} for this group.
     */
    private final GeneralOperationParameter[] parameters;

    /**
     * Construct a parameter group from a name.
     * This parameter group will be required exactly once.
     *
     * @param name The parameter group name.
     * @param parameters The {@linkplain #getParameters operation parameters} for this group.
     */
    public OperationParameterGroup(final String name,
                                   final GeneralOperationParameter[] parameters)
    {
        this(Collections.singletonMap("name", name), parameters);
    }

    /**
     * Construct a parameter group from a set of properties.
     * This parameter group will be required exactly once. The properties map is
     * given unchanged to the {@linkplain Info#Info(Map) super-class constructor}.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param parameters The {@linkplain #getParameters operation parameters} for this group.
     */
    public OperationParameterGroup(final Map properties,
                                   final GeneralOperationParameter[] parameters)
    {
        this(properties, 1, 1, parameters);
    }

    /**
     * Construct a parameter group from a set of properties. The properties map is
     * given unchanged to the {@linkplain Info#Info(Map) super-class constructor}.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param minimumOccurs The {@linkplain #getMinimumOccurs minimum number of times}
     *        that values for this parameter group are required.
     * @param maximumOccurs The {@linkplain #getMaximumOccurs maximum number of times}
     *        that values for this parameter group are required.
     * @param parameters The {@linkplain #getParameters operation parameters} for this group.
     */
    public OperationParameterGroup(final Map properties,
                                   final int minimumOccurs,
                                   final int maximumOccurs,
                                   final GeneralOperationParameter[] parameters)
    {
        super(properties, minimumOccurs, maximumOccurs);
        ensureNonNull("parameters", parameters);
        this.parameters = new GeneralOperationParameter[parameters.length];
        for (int i=0; i<parameters.length; i++) {
            this.parameters[i] = parameters[i];
            ensureNonNull("parameters", parameters, i);
        }
    }

    /**
     * Creates a new instance of
     * {@linkplain org.geotools.parameter.ParameterValueGroup parameter value group}
     * initialized with the
     * {@linkplain org.geotools.parameter.OperationParameter#getDefaultValue default values}. The
     * {@linkplain org.geotools.parameter.ParameterValueGroup#getDescriptor parameter value descriptor}
     * for the created group will be <code>this</code> object.
     */
    public GeneralParameterValue createValue() {
        return new org.geotools.parameter.ParameterValueGroup(this);
    }

    /**
     * Returns the parameters in this group.
     *
     * @return The parameters.
     */
    public GeneralOperationParameter[] getParameters() {
        return (GeneralOperationParameter[]) parameters.clone();
    }

    /**
     * Returns the first parameter in this group for the specified name. If no
     * {@linkplain org.geotools.parameter.OperationParameter operation parameter}
     * or group is found for the given name, then this method search recursively
     * in subgroups (if any).
     *
     * @param  name The case insensitive name of the parameter to search for.
     * @return The parameter for the given name.
     * @throws ParameterNotFoundException if there is no parameter for the given name.
     */
    public GeneralOperationParameter getParameter(String name) throws ParameterNotFoundException {
        ensureNonNull("name", name);
        name = name.trim();
        for (int i=0; i<parameters.length; i++) {
            final GeneralOperationParameter param = parameters[i];
            if (name.equals(param.getName(null))) {
                return param;
            }
        }
        for (int i=0; i<parameters.length; i++) {
            final GeneralOperationParameter param = parameters[i];
            if (param instanceof OperationParameterGroup) try {
                return ((OperationParameterGroup) param).getParameter(name);
            } catch (ParameterNotFoundException exception) {
                // Parameter not found. Ignore for now (search in others subgroups).
            }
        }
        throw new ParameterNotFoundException(Resources.format(
                    ResourceKeys.ERROR_MISSING_PARAMETER_$1, name), name);
    }
    
    /**
     * Compares the specified object with this parameter group for equality.
     *
     * @param  object The object to compare to <code>this</code>.
     * @param  compareMetadata <code>true</code> for performing a strict comparaison, or
     *         <code>false</code> for comparing only properties relevant to transformations.
     * @return <code>true</code> if both objects are equal.
     */
    public boolean equals(final Info object, final boolean compareMetadata) {
        if (super.equals(object, compareMetadata)) {
            final OperationParameterGroup that = (OperationParameterGroup) object;
            // TODO: We should use Arrays.deepEquals instead in J2SE 1.5.
            if (this.parameters.length == that.parameters.length) {
                for (int i=0; i<parameters.length; i++) {
                    if (!Utilities.equals(this.parameters[i], that.parameters[i])) {
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
        for (int i=0; i<parameters.length; i++) {
            code = code*37 + parameters[i].hashCode();
        }
        return code;
    }
}
