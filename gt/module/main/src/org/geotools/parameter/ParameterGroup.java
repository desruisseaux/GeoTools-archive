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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.LinkedHashMap;

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


/**
 * A group of related parameter values. The same group can be repeated more than once in an
 * {@linkplain org.geotools.referencing.operation.Operation operation} or higher level
 * <code>ParameterValueGroup</code>, if those instances contain different values of one
 * or more {@link org.geotools.parameter.Parameter}s which suitably distinquish among
 * those groups.
 *  
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see org.geotools.parameter.ParameterGroupDescriptor
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
     * Construct a parameter group from the specified descriptor.
     * All {@linkplain #getValues parameter values} will be initialized
     * to their default value.
     *
     * @param descriptor The descriptor for this group.
     */
    public ParameterGroup(final ParameterDescriptorGroup descriptor) {
        super(descriptor);
        final GeneralParameterDescriptor[] parameters = descriptor.getParameters();
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
    public ParameterGroup(final ParameterDescriptorGroup descriptor,
                                     GeneralParameterValue[] values)
    {
        super(descriptor);
        ensureNonNull("values", values);
        this.values = values = (GeneralParameterValue[]) values.clone();
        final GeneralParameterDescriptor[] parameters = descriptor.getParameters();
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
     *        {@linkplain org.geotools.parameter.ParameterGroupDescriptor operation parameter group}
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
    private static ParameterGroupDescriptor createDescriptor(final Map properties,
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
        return new org.geotools.parameter.ParameterGroupDescriptor(properties,
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
     * Returns the values in this group.
     *
     * @return The values.
     */
    public List values() {
        // don't need a clone because we will copy
        // still need synchronized incase we interupt add updating the values pointer
        // (usually only a problem on 64 bit multiprocessor machines, but with a 
        /// JIT all bets are off).
        GeneralParameterValue params[] = values;
        if( params == null ){
            return Collections.EMPTY_LIST;
        }
        List list = new ArrayList();
        for( int i=0; i<params.length; i++){
            list.add( params[i].clone() );
        }
        return list;        
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
     * identifier code}. If no {@linkplain org.geotools.parameter.Parameter parameter value}
     * is found for the given code, then this method search recursively in subgroups (if any).
     * This convenience method provides a way to get and set parameter values by name. For example
     * the following idiom fetches a floating point value for the <code>"false_easting"</code>
     * parameter:
     * <br><br>
     * <blockquote><code>
     * double value = getValue("false_easting").{@linkplain
     * org.geotools.parameter.Parameter#doubleValue() doubleValue()};
     * </code></blockquote>
     *
     * @param  name The case insensitive {@linkplain Identifier#getCode identifier code} of the
     *              parameter to search for. If this string contains the <code>':'</code> character,
     *              then the part before <code>':'</code> is the {@linkplain Identifier#getCodeSpace
     *              code space}.
     * @return The parameter value for the given identifier code.
     * @throws ParameterNotFoundException if there is no parameter value for the given identifier code.
     */
    public ParameterValue parameter(String name) throws ParameterNotFoundException {
        ensureNonNull("name", name);
        name = name.trim();
        List subgroups = null;
        GeneralParameterValue[] params = this.values;
        while (params != null) {
            for (int i=0; i<params.length; i++) {
                final GeneralParameterValue value = params[i];
                if (value instanceof ParameterValue) {
                    if (IdentifiedObject.identifierMatches(value.getDescriptor(), name)) {
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
            params = Parameters.array( (org.opengis.parameter.ParameterValueGroup) subgroups.remove(0));
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
            final ParameterGroup that = (ParameterGroup) object;
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
        final ParameterGroup copy = (ParameterGroup) super.clone();
        copy.values = (GeneralParameterValue[]) copy.values.clone();
        for (int i=0; i<copy.values.length; i++) {
            copy.values[i] = (GeneralParameterValue) copy.values[i].clone();
        }
        return copy;
    }

    /**
     * Adds a parameter to this group.
     * <p>
     * If an existing ParameterValue is already included:
     * <ul>
     * <li>For maxOccurs == 1, the new parameter will replace the existing parameter.
     * <li>For maxOccurs > 1, the new parameter will be added
     * <li>If adding the new parameter will increase the numbe past what
     * is allowable by maxOccurs an InvalidParameterTypeException will be thrown.
     * </p>
     * <p>
     * 
     * @param parameter New parameter to be added to this group
     * @throws InvalidParameterTypeException if adding this parameter
     *  would result in more parameters than allowed by maxOccurs, or if this
     *  parameter is not allowable by the groups descriptor 
     */
    public void add( ParameterValue parameter ) throws InvalidParameterTypeException{
        add( (GeneralParameterValue) parameter );
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
     */
    public void add( org.opengis.parameter.ParameterValueGroup group ) throws InvalidParameterTypeException {
        add( (GeneralParameterValue) group );
    }    
    private synchronized void add( GeneralParameterValue param ){
	    GeneralParameterDescriptor type = param.getDescriptor();        
	    if( !Parameters.allowed( (ParameterGroupDescriptor) this.getDescriptor(), type ) ){
	        throw new InvalidParameterTypeException(
                "Not allowed in ParameterValueGroup",
                type.getName().toString());
	    }
	    final int MIN = type.getMinimumOccurs();
	    final int MAX = type.getMaximumOccurs();
	    
	    if( MIN == 0 && MAX == 1 ){
	        // optional parameter group - we will simply replace what is there
	        int index = Parameters.indexOf( this, type ); 
	        if(  index == -1 ){
	            addImpl( param );                
	        }
	        else {
	            values[ index ] = param;
	        }
	    }
	    else if ( Parameters.count( this, type ) < MAX ){            
	        addImpl( param );
	    }        
	    else {
	        throw new InvalidParameterTypeException(
                "Cannot exceed maximum allowed in ParameterValueGroup",
                type.getName().toString()
                );
	    }
	}     
    // Actually perform the add
    private synchronized void addImpl( GeneralParameterValue param ){
        final int LENGTH = this.values == null ? 0 : this.values.length;
        GeneralParameterValue params[] = new GeneralParameterValue[ LENGTH+1 ];
        if( LENGTH > 0 ){
            System.arraycopy( this.values, 0, params, 0, LENGTH );
        }
        params[ LENGTH ] = param ;
        this.values = params;
    }
    
    /**
     * Convenience method used to locate ParameterValue(s) by descriptor.
     * <p>
     * This method does not search in subgroups.
     * </p>
     * @param type ParameterDescriptor used for lookup
     * @return Array of ParameterValuelength corasponding to cardinality of the descriptor
     */
    public ParameterValue[] parameter( ParameterDescriptor parameterType ){
        List found = Parameters.list( this, parameterType );
        return (ParameterValue[]) found.toArray( new ParameterValue[ found.size()] );
    }
    /**
     * Convenience method used to locate ParameterValueGroup(s) by descriptor.
     * <p>
     * This method does not search in subgroups.
     * </p>
     * @param groupType ParameterGroupDescriptor
     * @return Array of ParameterValueGroup length corasponding to cardinality of the descriptor
     */
    public org.opengis.parameter.ParameterValueGroup[] group( ParameterGroupDescriptor groupType ){
        List found = Parameters.list( this, groupType );
        return (org.opengis.parameter.ParameterValueGroup[]) found.toArray( new org.opengis.parameter.ParameterValueGroup[ found.size()] );
    }
}
