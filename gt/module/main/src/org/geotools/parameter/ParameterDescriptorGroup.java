/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le D�veloppement
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.referencing.IdentifiedObject;
import org.geotools.resources.UnmodifiableArrayList;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.cts.Resources;
import org.opengis.metadata.Identifier;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.InvalidParameterNameException;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;


/**
 * The definition of a group of related parameters used by an operation method.
 *  
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see org.geotools.parameter.ParameterGroup
 * @see org.geotools.parameter.ParameterDescriptor
 */
public class ParameterDescriptorGroup extends org.geotools.parameter.AbstractParameterDescriptor
                                   implements org.opengis.parameter.ParameterDescriptorGroup
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -4613190550542423839L;
    
    /**
     * The maximum number of times that values for this parameter group or
     * parameter are required.
     */
    private final int maximumOccurs;

    /**
     * The {@linkplain #descriptors() parameter descriptors} for this group.
     */
    private final GeneralParameterDescriptor[] parameters;

    /**
     * A view of {@link #parameters} as an immutable list. Will be constructed
     * only when first needed.
     */
    private transient List asList;

    /**
     * Construct a parameter group from a name.
     * This parameter group will be required exactly once.
     *
     * @param name The parameter group name.
     * @param parameters The {@linkplain #descriptors() parameter descriptors} for this group.
     */
    public ParameterDescriptorGroup(final String name,
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
     * @param parameters The {@linkplain #descriptors() parameter descriptors} for this group.
     */
    public ParameterDescriptorGroup(final Map properties,
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
     * @param parameters The {@linkplain #descriptors() parameter descriptors} for this group.
     */
    public ParameterDescriptorGroup(final Map properties,
                                    final int minimumOccurs,
                                    final int maximumOccurs,
                                    GeneralParameterDescriptor[] parameters)
    {
        super(properties, minimumOccurs, maximumOccurs);
        this.maximumOccurs = maximumOccurs;
        ensureNonNull("parameters", parameters);
        this.parameters = new GeneralParameterDescriptor[parameters.length];
        for (int i=0; i<parameters.length; i++) {
            this.parameters[i] = parameters[i];
            ensureNonNull("parameters", parameters, i);
        }
        /*
         * Ensure there is no conflict in parameter names.
         */
        parameters = this.parameters;
        for (int i=0; i<parameters.length; i++) {
            final String name = parameters[i].getName().getCode();
            for (int j=0; j<parameters.length; j++) {
                if (i != j) {
                    if (nameMatches(parameters[j], name)) {
                        throw new InvalidParameterNameException(Resources.format(
                            ResourceKeys.ERROR_PARAMETER_NAME_CLASH_$4,
                            parameters[j].getName().getCode(), new Integer(j),
                            name, new Integer(i)), name);
                    }
                }
            }
        }
    }

    /**
     * The maximum number of times that values for this parameter group or
     * parameter are required.
     *
     * @see #getMaximumOccurs
     */
    public int getMaximumOccurs() {
        return maximumOccurs;
    }

    /**
     * Creates a new instance of
     * {@linkplain org.geotools.parameter.ParameterGroup parameter value group}
     * initialized with the
     * {@linkplain ParameterDescriptor#getDefaultValue default values}. The
     * {@linkplain ParameterValueGroup#getDescriptor parameter value descriptor}
     * for the created group will be <code>this</code> object.
     */
    public GeneralParameterValue createValue() {
        return new ParameterGroup(this);
    }

    /**
     * A view of {@link #parameters} as an unmodifiable list. This class overides
     * {@link #contains} with a faster implementation based on {@link HashSet}.
     * It can help for map projection implementations (among other), which test
     * often for a parameter validity.
     */
    private static final class AsList extends UnmodifiableArrayList {
        /** For compatibility with different versions. */
        private static final long serialVersionUID = -2116304004367396735L;

        /** The element as a set. Will be constructed only when first needed. */
        private transient Set asSet;

        /** Construct a list for the specified array. */
        public AsList(final GeneralParameterDescriptor[] array) {
            super(array);
        }

        /** Test for the inclusion of the specified descriptor. */
        public boolean contains(final Object object) {
            if (asSet == null) {
                asSet = new HashSet(this);
            }
            return asSet.contains(object);
        }
    }

    /**
     * Returns the parameters in this group.
     */
    public List descriptors() {
        if (asList == null) {
            if (parameters == null) {
                asList = Collections.EMPTY_LIST;
            } else switch (parameters.length) {
                case 0:  asList = Collections.EMPTY_LIST;                   break;
                case 1:  asList = Collections.singletonList(parameters[0]); break;
                case 2:  // fall through
                case 3:  asList = new UnmodifiableArrayList(parameters);    break;
                default: asList = new AsList(parameters);                   break;
            }
        }
        return asList;
    }

    /**
     * Returns the first parameter in this group for the specified {@linkplain Identifier#getCode
     * identifier code}.
     *
     * @param  name The case insensitive {@linkplain Identifier#getCode identifier code} of the
     *              parameter to search for.
     * @return The parameter for the given identifier code.
     * @throws ParameterNotFoundException if there is no parameter for the given identifier code.
     */
    public GeneralParameterDescriptor descriptor(String name) throws ParameterNotFoundException {
        ensureNonNull("name", name);
        name = name.trim();
        List subgroups = null;
        List/*<GeneralParameterDescriptor>*/ parameters = descriptors();
        while (parameters != null) {
            for (final Iterator it=parameters.iterator(); it.hasNext();) {
                final GeneralParameterDescriptor param = (GeneralParameterDescriptor) it.next();
                if (param instanceof ParameterDescriptor) {
                    if (nameMatches(param, name)) {
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
            parameters = ((org.opengis.parameter.ParameterDescriptorGroup) subgroups.remove(0))
                         .descriptors();
        }
        throw new ParameterNotFoundException(Resources.format(
                  ResourceKeys.ERROR_MISSING_PARAMETER_$1, name), name);
    }

    /**
     * Returns the first parameter in this group for the specified {@linkplain Identifier#getCode
     * identifier code}.
     *
     * @deprecated Use {@link #descriptor} instead.
     */
    public ParameterDescriptor getParameter(String name) throws ParameterNotFoundException {
        return (ParameterDescriptor) descriptor(name);
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
            final ParameterDescriptorGroup that = (ParameterDescriptorGroup) object;
            return Arrays.equals(this.parameters, that.parameters);
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
