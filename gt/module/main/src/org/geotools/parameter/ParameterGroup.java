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
import java.util.Arrays;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collections;
import java.util.AbstractList;
import java.util.LinkedHashMap;
import java.util.RandomAccess;
import java.io.Serializable;
import java.io.IOException;

// OpenGIS dependencies
import org.opengis.parameter.InvalidParameterTypeException;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterNotFoundException;

// Geotools dependencies
import org.geotools.referencing.IdentifiedObject;
import org.geotools.referencing.Identifier;  // For javadoc
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.UnmodifiableArrayList;
import org.geotools.io.TableWriter;


/**
 * A group of related parameter values. The same group can be repeated more than once in an
 * {@linkplain org.geotools.referencing.operation.Operation operation} or higher level
 * <code>ParameterValueGroup</code>, if those instances contain different values of one
 * or more {@link org.geotools.parameter.Parameter}s which suitably distinquish among
 * those groups.
 *  
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Jody Garnett (Refractions Research)
 *
 * @see org.geotools.parameter.ParameterDescriptorGroup
 * @see org.geotools.parameter.Parameter
 */
public class ParameterGroup extends org.geotools.parameter.AbstractParameter
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
     * A view of {@link #values} as a list. Will be constructed only when first needed.
     * Note that while this list may be immutable, <strong>elements</strong> in this list
     * stay modifiable. The goal is to allows the following idiom:
     *
     * <blockquote><pre>
     * values().get(i).setValue(myValue);
     * </pre></blockquote>
     */
    private transient List asList;

    /**
     * Construct a parameter group from the specified descriptor.
     * All {@linkplain #getValues parameter values} will be initialized
     * to their default value.
     *
     * @param descriptor The descriptor for this group.
     */
    public ParameterGroup(final ParameterDescriptorGroup descriptor) {
        super(descriptor);
        final List/*<GeneralParameterDescriptor>*/ parameters = descriptor.descriptors();
        final List/*<GeneralParameterValue>*/ values = new ArrayList(parameters.size());
        for (final Iterator it=parameters.iterator(); it.hasNext();) {
            final GeneralParameterDescriptor element = (GeneralParameterDescriptor) it.next();
            for (int i=element.getMinimumOccurs(); --i>=0;) {
                final GeneralParameterValue value = element.createValue();
                ensureNonNull("createValue", value);
                values.add(value);
            }
        }
        this.values = (GeneralParameterValue[]) values.toArray(new GeneralParameterValue[values.size()]);
    }

    /**
     * Construct a parameter group from the specified descriptor and list of parameters.
     *
     * @param descriptor The descriptor for this group.
     * @param values The list of parameter values.
     */
    public ParameterGroup(final ParameterDescriptorGroup descriptor,
                                     GeneralParameterValue[] values)
    {
        super(descriptor);
        ensureNonNull("values", values);
        this.values = values = (GeneralParameterValue[]) values.clone();
        final List/*<GeneralParameterDescriptor>*/ parameters = descriptor.descriptors();
        final Map occurences = new LinkedHashMap(Math.round(parameters.size()/0.75f)+1, 0.75f);
        for (final Iterator it=parameters.iterator(); it.hasNext();) {
            final GeneralParameterDescriptor param = (GeneralParameterDescriptor) it.next();
            ensureNonNull("parameters", param);
            occurences.put(param, new int[1]);
            // The value 'int[1}' will be used by 'ensureValidOccurs'
        }
        ensureValidOccurs(values, occurences);
    }

    /**
     * Construct a parameter group from the specified list of parameters.
     *
     * @param properties The properties for the
     *        {@linkplain org.geotools.parameter.ParameterDescriptorGroup operation parameter group}
     *        to construct from the list of parameters.
     * @param values The list of parameter values.
     */
    public ParameterGroup(final Map properties, final GeneralParameterValue[] values) {
        super(createDescriptor(properties, values));
        this.values = (GeneralParameterValue[]) values.clone();
    }

    /**
     * Work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private static ParameterDescriptorGroup createDescriptor(final Map properties,
                                                             final GeneralParameterValue[] values)
    {
        ensureNonNull("values", values);
        final Map occurences = new LinkedHashMap(Math.round(values.length/0.75f)+1, 0.75f);
        for (int i=0; i<values.length; i++) {
            ensureNonNull("values", values, i);
            occurences.put(values[i].getDescriptor(), new int[1]);
            // The value 'int[1}' will be used by 'ensureValidOccurs'
        }
        ensureValidOccurs(values, occurences);
        final Set descriptors = occurences.keySet();
        return new org.geotools.parameter.ParameterDescriptorGroup(properties,
                                          (GeneralParameterDescriptor[]) descriptors.toArray(
                                          new GeneralParameterDescriptor[descriptors.size()]));
    }

    /**
     * Make sure that the number of occurences of each values is inside the expected range.
     *
     * @param values The list of parameter values.
     * @param occurences A map of the number of occurences of a value for each descriptor.
     *        The key must be {@link GeneralParameterDescriptor} instances and the values
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
            final GeneralParameterDescriptor descriptor = values[i].getDescriptor();
            final int[] count = (int[]) occurences.get(descriptor);
            if (count == null) {
                throw new IllegalArgumentException(Resources.format(
                          ResourceKeys.ERROR_ILLEGAL_DESCRIPTOR_FOR_PARAMETER_$1,
                          descriptor.getName().toString()));
            }
            count[0]++;
        }
        /*
         * Now check if the occurences are in the expected ranges.
         */
        for (final Iterator it=occurences.entrySet().iterator(); it.hasNext();) {
            final Map.Entry entry = (Map.Entry) it.next();
            final GeneralParameterDescriptor descriptor = (GeneralParameterDescriptor) entry.getKey();
            final int count = ((int[]) entry.getValue())[0];
            final int min   = descriptor.getMinimumOccurs();
            final int max   = descriptor.getMaximumOccurs();
            if (!(count>=min && count<=max)) {
                throw new IllegalArgumentException(Resources.format(
                          ResourceKeys.ERROR_ILLEGAL_OCCURS_FOR_PARAMETER_$4,
                          descriptor.getName().toString(), new Integer(count),
                          new Integer(min), new Integer(max)));
            }
        }
    }

    /**
     * Returns the values in this group. The returned list may be unmodifiable. However,
     * <strong>elements</strong> in this list (i.e. {@link GeneralParameterValue} objects)
     * are modifiable. Concequently, a user can iterate through all parameters in this group
     * and update some of them based on arbitrary condition (for example remplacing all URL
     * by an other one).
     */
    public List values() {
        if (asList == null) {
            if (values == null){
                asList = Collections.EMPTY_LIST;
            } else {
                asList = new UnmodifiableArrayList(values);
            }
        }
        return asList;
    }

    /**
     * Returns the parameter value at the specified index.
     *
     * @param  The zero-based index.
     * @return The parameter value at the specified index.
     * @throws IndexOutOfBoundsException if the specified index is out of bounds.
     */
    final GeneralParameterValue parameter(final int index) throws IndexOutOfBoundsException {
        return values[index];
    }

    /**
     * Returns the first value in this group for the specified {@linkplain Identifier#getCode
     * identifier code}. If no {@linkplain org.geotools.parameter.ParameterDescriptor parameter
     * descriptor} is found for the given code, then this method search recursively in subgroups
     * (if any). If a parameter descriptor is found but there is no
     * {@linkplain org.geotools.parameter.ParameterValue value} for it (because it is an optional
     * parameter), then a {@linkplain org.geotools.parameter.ParameterValue parameter value} is
     * automatically created and initialized to its default value (if any).
     *
     * <P>This convenience method provides a way to get and set parameter values by name. For
     * example the following idiom fetches a floating point value for the
     * <code>"false_easting"</code> parameter:</P>
     *
     * <blockquote><code>
     * double value = parameter("false_easting").{@linkplain
     * org.geotools.parameter.ParameterValue#doubleValue() doubleValue()};
     * </code></blockquote>
     *
     * @param  name The case insensitive {@linkplain Identifier#getCode identifier code} of the
     *              parameter to search for.
     * @return The parameter value for the given identifier code.
     * @throws ParameterNotFoundException if there is no parameter value for the given identifier
     *         code.
     *
     * @todo Creation of new {@link org.geotools.parameter.ParameterValue} object not yet
     *       implemented.
     */
    public ParameterValue parameter(String name) throws ParameterNotFoundException {
        ensureNonNull("name", name);
        name = name.trim();
        List subgroups = null;
        List/*<GeneralParameterValue>*/ values = values();
        while (values != null) {
            for (final Iterator it=values.iterator(); it.hasNext();) {
                final GeneralParameterValue value = (GeneralParameterValue) it.next();
                if (value instanceof ParameterValue) {
                    if (IdentifiedObject.nameMatches(value.getDescriptor(), name)) {
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
            values = ((org.opengis.parameter.ParameterValueGroup) subgroups.remove(0)).values();
        }
        throw new ParameterNotFoundException(Resources.format(
                  ResourceKeys.ERROR_MISSING_PARAMETER_$1, name), name);
    }

    /**
     * Adds a parameter to this group.
     * <p>
     * If an existing ParameterValue is already included:
     * <ul>
     * <li>For maxOccurs == 1, the new parameter will replace the existing parameter.
     * <li>For maxOccurs > 1, the new parameter will be added
     * <li>If adding the new parameter will increase the number past what
     * is allowable by maxOccurs an InvalidParameterTypeException will be thrown.
     * </p>
     * <p>
     * 
     * @param parameter New parameter to be added to this group
     * @throws InvalidParameterTypeException if adding this parameter
     *  would result in more parameters than allowed by maxOccurs, or if this
     *  parameter is not allowable by the groups descriptor 
     *
     * @deprecated User should never add {@link ParameterValue} himself. Parameter value
     *             creation should be controlled by this class. We should probably add a
     *             <code>add(String)</code> method instead, which create and returns a
     *             <code>ParameterValue</code> object.
     */
    public void add(ParameterValue parameter) throws InvalidParameterTypeException {
        add((GeneralParameterValue) parameter);
    }
    
    /**
     * Adds new parameter group to this group.
     * <p>
     * If an existing ParameterValueGroup is already included:
     * <ul>
     * <li>For maxOccurs == 1, the new group will replace the existing group.
     * <li>For maxOccurs > 1, the new group will be added
     * <li>If adding the new group will increase the number past what
     * is allowable by maxOccurs an InvalidParameterTypeException will be thrown.
     * </p>
     * <p>
     * 
     * @param group New ParameterValueGroup to be added to this group
     * @throws InvalidParameterTypeException if adding this parameter
     *  would result in more parameters than allowed by maxOccurs, or if this
     *  parameter is not allowable by the groups descriptor 
     *
     * @deprecated User should never add {@link ParameterValueGroup} himself. Parameter value
     *             creation should be controlled by this class. We should probably add a
     *             <code>addGroup(String)</code> method instead, which create and returns a
     *             <code>ParameterValueGroup</code> object.
     */
    public void add(org.opengis.parameter.ParameterValueGroup group) throws InvalidParameterTypeException {
        add((GeneralParameterValue) group);
    }    

    /**
     * Implementation of add methods.
     *
     * @deprecated User should never add {@link ParameterValue} himself. Parameter value
     *             creation should be controlled by this class. We should probably add a
     *             <code>add(String)</code> method instead, which create and returns a
     *             <code>ParameterValue</code> object.
     */
    private synchronized void add(GeneralParameterValue param) {
        GeneralParameterDescriptor type = param.getDescriptor();        
        if (!Parameters.allowed((ParameterDescriptorGroup) this.getDescriptor(), type )) {
            throw new InvalidParameterTypeException(
                "Not allowed in ParameterValueGroup:"+type.getName(),
                type.getName().toString());
        }
        final int MIN = type.getMinimumOccurs();
        final int MAX = type.getMaximumOccurs();
        if (MIN == 0 && MAX == 1) {
            // optional parameter group - we will simply replace what is there
            int index = Parameters.indexOf( this, type ); 
            if (index == -1) {
                addImpl(param);                
            } else {
                values[ index ] = param;
            }
        } else if (Parameters.count(this, type) < MAX) {            
            addImpl(param);
        } else {
            throw new InvalidParameterTypeException(
            "Cannot exceed maximum allowed in ParameterValueGroup",
            type.getName().toString()
            );
        }
    }

    /**
     * Implementation of add methods.
     *
     * @deprecated User should never add {@link ParameterValue} himself.
     */
    private synchronized void addImpl(GeneralParameterValue param) {
        final int LENGTH = this.values == null ? 0 : this.values.length;
        GeneralParameterValue params[] = new GeneralParameterValue[ LENGTH+1 ];
        if (LENGTH > 0) {
            System.arraycopy(this.values, 0, params, 0, LENGTH);
        }
        params[LENGTH] = param;
        this.values = params;
    }
    
    /**
     * Compares the specified object with this parameter for equality.
     *
     * @param  object The object to compare to <code>this</code>.
     * @return <code>true</code> if both objects are equal.
     */
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final ParameterGroup that = (ParameterGroup) object;
            Arrays.equals(this.values, that.values);
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
        // TODO: We should use Arrays.hashCode instead in J2SE 1.5.
        for (int i=0; i<values.length; i++) {
            code = code*37 + values[i].hashCode();
        }
        return code;
    }

    /**
     * Returns a deep copy of this group of parameter values.
     * Included parameter values and subgroups are cloned recursively.
     *
     * @return A copy of this group of parameter values.
     */
    public Object clone() {
        final ParameterGroup copy = (ParameterGroup) super.clone();
        copy.values = (GeneralParameterValue[]) copy.values.clone();
        for (int i=0; i<copy.values.length; i++) {
            copy.values[i] = (GeneralParameterValue) copy.values[i].clone();
        }
        return copy;
    }

    /**
     * Write the content of this parameter to the specified table.
     *
     * @param  table The table where to format the parameter value.
     * @throws IOException if an error occurs during output operation.
     */
    protected void write(final TableWriter table) throws IOException {
        table.write(descriptor.getName().getCode());
        table.nextColumn();
        table.write(':');
        table.nextColumn();
        TableWriter inner = null;
        for (int i=0; i<values.length; i++) {
            final GeneralParameterValue value = values[i];
            if (value instanceof AbstractParameter) {
                if (inner == null) {
                    inner = new TableWriter(table, 1);
                }
                ((AbstractParameter) value).write(inner);
            } else {
                // Unknow implementation. It will break the formatting. Too bad...
                if (inner != null) {
                    inner.flush();
                    inner = null;
                }
                table.write(value.toString());
                table.write(System.getProperty("line.separator", "\r"));
            }
        }
        if (inner != null) {
            inner.flush();
        }
        table.nextLine();
    }
}
