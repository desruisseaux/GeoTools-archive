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
import org.opengis.metadata.Identifier;  // For javadoc

// Geotools dependencies
import org.geotools.referencing.IdentifiedObject;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.UnmodifiableArrayList;
import org.geotools.io.TableWriter;


/**
 * A group of related parameter values. The same group can be repeated more than once in an
 * {@linkplain org.opengis.referencing.operation.Operation operation} or higher level
 * {@link org.opengis.parameter.ParameterValueGroup}, if those instances contain different
 * values of one or more {@link ParameterValue}s which suitably distinquish among
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
    private ArrayList/*<GeneralParameterValue>*/ values;

    /**
     * A view of {@link #values} as an immutable list. Will be constructed only when first
     * needed. Note that while this list may be immutable, <strong>elements</strong> in this
     * list stay modifiable. The goal is to allows the following idiom:
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
        values = new ArrayList/*<GeneralParameterValue>*/(parameters.size());
        for (final Iterator it=parameters.iterator(); it.hasNext();) {
            final GeneralParameterDescriptor element = (GeneralParameterDescriptor) it.next();
            for (int count=element.getMinimumOccurs(); --count>=0;) {
                final GeneralParameterValue value = element.createValue();
                ensureNonNull("createValue", value);
                values.add(value);
            }
        }
    }

    /**
     * Construct a parameter group from the specified descriptor and list of parameters.
     *
     * @param descriptor The descriptor for this group.
     * @param values The list of parameter values.
     */
    public ParameterGroup(final ParameterDescriptorGroup descriptor,
                          final GeneralParameterValue[]  values)
    {
        super(descriptor);
        ensureNonNull("values", values);
        this.values = new ArrayList(values.length);
        for (int i=0; i<values.length; i++) {
            this.values.add(values[i]);
        }
        final List/*<GeneralParameterDescriptor>*/ parameters = descriptor.descriptors();
        final Map occurences = new LinkedHashMap(Math.round(parameters.size()/0.75f)+1, 0.75f);
        for (final Iterator it=parameters.iterator(); it.hasNext();) {
            final GeneralParameterDescriptor param = (GeneralParameterDescriptor) it.next();
            ensureNonNull("parameters", param);
            occurences.put(param, new int[1]);
            // The value 'int[1]' will be used by 'ensureValidOccurs'
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
        this.values = new ArrayList(values.length);
        for (int i=0; i<values.length; i++) {
            this.values.add(values[i]);
        }
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
            // The value 'int[1]' will be used by 'ensureValidOccurs'
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
                asList = Collections.unmodifiableList(values);
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
        return (GeneralParameterValue) values.get(index); // Remove cast with J2SE 1.5.
    }

    /**
     * Returns the value in this group for the specified
     * {@linkplain Identifier#getCode identifier code}.
     * If no {@linkplain ParameterValue parameter value} is found but
     * a {@linkplain ParameterDescriptor parameter descriptor} is found
     * (which may occurs if the parameter is optional, i.e.
     * <code>{@linkplain ParameterDescriptor#getMinimumOccurs minimumOccurs} == 0</code>),
     * then a {@linkplain ParameterValue parameter value} is
     * automatically created and initialized to its
     * {@linkplain ParameterDescriptor#getDefaultValue default value} (if any).
     *
     * <P>This convenience method provides a way to get and set parameter values by name. For
     * example the following idiom fetches a floating point value for the
     * <code>"false_easting"</code> parameter:</P>
     *
     * <blockquote><code>
     * double value =
     * parameter("false_easting").{@linkplain ParameterValue#doubleValue() doubleValue()};
     * </code></blockquote>
     *
     * <P>This method do not search recursively in subgroups. This is because more than one
     * subgroup may exist for the same {@linkplain ParameterDescriptorGroup descriptor}. The
     * user must {@linkplain #groups query all subgroups} and select explicitly
     * the appropriate one to use.</P>
     *
     * @param  name The case insensitive {@linkplain Identifier#getCode identifier code} of the
     *              parameter to search for.
     * @return The parameter value for the given identifier code.
     * @throws ParameterNotFoundException if there is no parameter value for the given identifier
     *         code.
     */
    public ParameterValue parameter(String name) throws ParameterNotFoundException {
        ensureNonNull("name", name);
        name = name.trim();
        while (values != null) {
            for (final Iterator it=values.iterator(); it.hasNext();) {
                final GeneralParameterValue value = (GeneralParameterValue) it.next();
                if (value instanceof ParameterValue) {
                    if (IdentifiedObject.nameMatches(value.getDescriptor(), name)) {
                        return (ParameterValue) value;
                    }
                }
            }
            /*
             * No existing parameter found. Check if an optional parameter exists.
             * If such a descriptor is found, create it, add it to the list of values
             * and returns it.
             */
            // TODO: The following lines should be considerably shorter with J2SE 1.5:
            // for (GeneralParameterDescriptor descriptor : getDescriptor()) {
            for (final Iterator it=((ParameterDescriptorGroup)
                 getDescriptor()).descriptors().iterator(); it.hasNext();)
            {
                final GeneralParameterDescriptor descriptor = (GeneralParameterDescriptor) it.next();
                if (descriptor instanceof ParameterDescriptor) {
                    if (IdentifiedObject.nameMatches(descriptor, name)) {
                        // TODO: remove the first cast with J2SE 1.5.
                        final ParameterValue value = (ParameterValue)
                                ((ParameterDescriptor) descriptor).createValue();
                        values.add(value);
                        return value;
                    }
                }
            }
        }
        throw new ParameterNotFoundException(Resources.format(
                  ResourceKeys.ERROR_MISSING_PARAMETER_$1, name), name);
    }
    
    /**
     * Returns all subgroups with the specified name. This method do not create new groups.
     * If the requested group is optional (i.e.
     * <code>{@linkplain ParameterDescriptor#getMinimumOccurs minimumOccurs} == 0</code>)
     * and no value were set, then this method returns an empty set.
     *
     * @param  name The case insensitive {@linkplain Identifier#getCode identifier code}
     *         of the parameter group to search for.
     * @return The set of all parameter group for the given identifier code.
     * @throws ParameterNotFoundException if no {@linkplain ParameterDescriptorGroup descriptor}
     *         was found for the given name.
     */
    public List/*<org.opengis.parameter.ParameterValueGroup>*/ groups(String name)
            throws ParameterNotFoundException
    {
        ensureNonNull("name", name);
        name = name.trim();
        final List groups = new ArrayList(Math.min(values.size(), 10));
        for (final Iterator it=values.iterator(); it.hasNext();) {
            final GeneralParameterValue value = (GeneralParameterValue) it.next();
            if (value instanceof org.opengis.parameter.ParameterValueGroup) {
                if (IdentifiedObject.nameMatches(value.getDescriptor(), name)) {
                    groups.add(value);
                }
            }
        }
        /*
         * No groups were found. Check if the group actually exists (i.e. is declared in the
         * descriptor). If it doesn't exists, then an exception is thrown. If it exists (i.e.
         * it is simply an optional group not yet defined), then returns an empty list.
         */
        if (groups.isEmpty()) {
            final GeneralParameterDescriptor check = 
                    ((ParameterDescriptorGroup) descriptor).descriptor(name);
            if (!(check instanceof ParameterDescriptorGroup)) {
                throw new ParameterNotFoundException(Resources.format(
                          ResourceKeys.ERROR_MISSING_PARAMETER_$1, name), name);
            }
        }
        return groups;
    }

    /**
     * Create a new group of the specified name. The specified name must be the
     * {@linkplain Identifier#getCode identifier code} of a {@linkplain ParameterDescriptorGroup
     * descriptor group}.
     *
     * @param  name The case insensitive {@linkplain Identifier#getCode identifier code} of the
     *              parameter group to create.
     * @return A newly created parameter group for the given identifier code.
     * @throws ParameterNotFoundException if no {@linkplain ParameterDescriptorGroup descriptor}
     *         was found for the given name.
     * @throws IllegalStateException if this parameter group already contains the
     *         {@linkplain ParameterDescriptorGroup#getMaximumOccurs maximum number of occurences}
     *         of subgroups of the given name.
     */
    public org.opengis.parameter.ParameterValueGroup addGroup(String name)
            throws ParameterNotFoundException, IllegalStateException
    {
        final GeneralParameterDescriptor check = 
                ((ParameterDescriptorGroup) descriptor).descriptor(name);
        if (!(check instanceof ParameterDescriptorGroup)) {
            throw new ParameterNotFoundException(Resources.format(
                      ResourceKeys.ERROR_MISSING_PARAMETER_$1, name), name);
        }
        int occurs = 0;
        for (final Iterator it=values.iterator(); it.hasNext();) {
            final GeneralParameterValue value = (GeneralParameterValue) it.next();
            if (IdentifiedObject.nameMatches(value.getDescriptor(), name)) {
                occurs++;
            }
        }
        if (occurs >= check.getMaximumOccurs()) {
            throw new IllegalStateException(Resources.format(
                ResourceKeys.ERROR_TOO_MANY_OCCURENCES_$2, name, new Integer(occurs)));
        }
        final org.opengis.parameter.ParameterValueGroup value =
            (org.opengis.parameter.ParameterValueGroup) // Remove this cast for J2SE 1.5.
            ((ParameterDescriptorGroup) check).createValue();
        values.add(value);
        return value;
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
                values.add(param);                
            } else {
                values.set(index, param);
            }
        } else if (Parameters.count(this, type) < MAX) {            
            values.add(param);
        } else {
            throw new InvalidParameterTypeException(
            "Cannot exceed maximum allowed in ParameterValueGroup",
            type.getName().toString()
            );
        }
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
            return Utilities.equals(this.values, that.values);
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
        return super.hashCode() ^ values.hashCode();
    }

    /**
     * Returns a deep copy of this group of parameter values.
     * Included parameter values and subgroups are cloned recursively.
     *
     * @return A copy of this group of parameter values.
     */
    public Object clone() {
        final ParameterGroup copy = (ParameterGroup) super.clone();
        copy.values = (ArrayList/*<GeneralParameterValue>*/) copy.values.clone();
        for (int i=copy.values.size(); --i>=0;) {
            // TODO: remove cast with J2SE 1.5
            copy.values.set(i, ((GeneralParameterValue) copy.values.get(i)).clone());
        }
        copy.asList = null;
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
        for (final Iterator it=values.iterator(); it.hasNext();) {
            final GeneralParameterValue value = (GeneralParameterValue) it.next();
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
