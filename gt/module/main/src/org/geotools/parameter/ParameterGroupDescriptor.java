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
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

// OpenGIS dependencies
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterNotFoundException;

// Geotools dependencies
import org.geotools.referencing.IdentifiedObject;
import org.geotools.referencing.Identifier;  // For javadoc
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
 * @see org.geotools.parameter.ParameterDescriptor
 */
public class ParameterGroupDescriptor extends org.geotools.parameter.GeneralParameterDescriptor
                                  implements org.opengis.parameter.ParameterDescriptorGroup
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 3395510970737025480L;

    /**
     * The {@linkplain #getParameters operation parameters} for this group.
     */
    private final GeneralParameterDescriptor[] parameters;

    /**
     * Construct a parameter group from a name.
     * This parameter group will be required exactly once.
     *
     * @param name The parameter group name.
     * @param parameters The {@linkplain #getParameters operation parameters} for this group.
     */
    public ParameterGroupDescriptor(final String name,
                                   final GeneralParameterDescriptor[] parameters)
    {
        this(Collections.singletonMap("name", name), parameters);
    }

    /**
     * Construct a parameter group from a set of properties.
     * This parameter group will be required exactly once. The properties map is
     * given unchanged to the {@linkplain IdentifiedObject#IdentifiedObject(Map)
     * super-class constructor}.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param parameters The {@linkplain #getParameters operation parameters} for this group.
     */
    public ParameterGroupDescriptor(final Map properties,
                                   final GeneralParameterDescriptor[] parameters)
    {
        this(properties, 1, 1, parameters);
    }

    /**
     * Construct a parameter group from a set of properties. The properties map is
     * given unchanged to the {@linkplain IdentifiedObject#IdentifiedObject(Map)
     * super-class constructor}.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param minimumOccurs The {@linkplain #getMinimumOccurs minimum number of times}
     *        that values for this parameter group are required.
     * @param maximumOccurs The {@linkplain #getMaximumOccurs maximum number of times}
     *        that values for this parameter group are required.
     * @param parameters The {@linkplain #getParameters operation parameters} for this group.
     */
    public ParameterGroupDescriptor(final Map properties,
                                   final int minimumOccurs,
                                   final int maximumOccurs,
                                   final GeneralParameterDescriptor[] parameters)
    {
        super(properties, minimumOccurs, maximumOccurs);
        ensureNonNull("parameters", parameters);
        this.parameters = new GeneralParameterDescriptor[parameters.length];
        for (int i=0; i<parameters.length; i++) {
            this.parameters[i] = parameters[i];
            ensureNonNull("parameters", parameters, i);
        }
    }

    /**
     * Creates a new instance of
     * {@linkplain org.geotools.parameter.ParameterValueGroup parameter value group}
     * initialized with the
     * {@linkplain org.geotools.parameter.ParameterDescriptor#getDefaultValue default values}. The
     * {@linkplain org.geotools.parameter.ParameterValueGroup#getDescriptor parameter value descriptor}
     * for the created group will be <code>this</code> object.
     */
    public GeneralParameterValue createValue() {
        return new ParameterValueGroup(this);
    }

    /**
     * Returns the parameters in this group.
     *
     * @return The parameters.
     */
    public GeneralParameterDescriptor[] getParameters() {
        return (GeneralParameterDescriptor[]) parameters.clone();
    }

    /**
     * Returns the first parameter in this group for the specified {@linkplain Identifier#getCode
     * identifier code}. If no {@linkplain org.geotools.parameter.ParameterDescriptor operation
     * parameter} is found for the given code, then this method search recursively in subgroups
     * (if any). This convenience method provides a way to get and set parameter information by
     * name. For example the following idiom fetches the default value for the
     * <code>"false_easting"</code> parameter:
     * <br><br>
     * <blockquote><code>
     * Object defaultValue = getParameter("false_easting").{@linkplain
     * org.geotools.parameter.ParameterDescriptor#getDefaultValue() getDefaultValue()};
     * </code></blockquote>
     *
     * @param  name The case insensitive {@linkplain Identifier#getCode identifier code} of the
     *              parameter to search for. If this string contains the <code>':'</code> character,
     *              then the part before <code>':'</code> is the {@linkplain Identifier#getCodeSpace
     *              code space}.
     * @return The parameter for the given identifier code.
     * @throws ParameterNotFoundException if there is no parameter for the given identifier code.
     */
    public ParameterDescriptor getParameter(String name) throws ParameterNotFoundException {
        ensureNonNull("name", name);
        name = name.trim();
        List subgroups = null;
        GeneralParameterDescriptor[] parameters = this.parameters;
        while (parameters != null) {
            for (int i=0; i<parameters.length; i++) {
                final GeneralParameterDescriptor param = parameters[i];
                if (param instanceof ParameterDescriptor) {
                    if (identifierMatches(param, name)) {
                        return (ParameterDescriptor) param;
                    }
                } else if (param instanceof org.opengis.parameter.ParameterDescriptorGroup) {
                    if (subgroups == null) {
                        subgroups = new LinkedList();
                    }
                    assert !subgroups.contains(param) : param;
                    subgroups.add(param);
                }
            }
            /*
             * Looks in subgroups only after all parameters in the current group have been verified.
             * Search in a "first in, first out" basis.
             */
            if (subgroups==null || subgroups.isEmpty()) {
                break;
            }
            parameters = ((org.opengis.parameter.ParameterDescriptorGroup) subgroups.remove(0)).getParameters();
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
    public boolean equals(final IdentifiedObject object, final boolean compareMetadata) {
        if (super.equals(object, compareMetadata)) {
            final ParameterGroupDescriptor that = (ParameterGroupDescriptor) object;
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
