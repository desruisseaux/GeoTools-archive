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
 */
package org.geotools.parameter;

// J2SE dependencies
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

// OpenGIS dependencies
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;

// Geotools dependencies
import org.geotools.referencing.IdentifiedObject;


/**
 * Utility class for methods helping implementing, and working with the
 * parameter API from {@link org.opengis.parameter} package.
 *
 * @version $Id$
 * @author Jody Garnett (Refractions Research)
 * @author Martin Desruisseaux
 */
public class Parameters {
    /**
     * An empty parameter group. This group contains no parameters.
     */
    public static ParameterDescriptorGroup EMPTY_GROUP =
            new org.geotools.parameter.ParameterDescriptorGroup("empty", // TODO: localize
            new GeneralParameterDescriptor[0]);
    
    /**
     * Used to handle code that expexted group.getValues().
     *
     * @deprecated This method was provided as a bridge between the old API (array-based) to the
     *             new API (list-based). New code should work directly on the list instead.
     */
    private static GeneralParameterValue[] array(ParameterValueGroup group) {
        List params = group.values(); 
        return (GeneralParameterValue[]) params.toArray( new GeneralParameterValue[ params.size()] );
    }

    /**
     * Number of type in group.
     * 
     * @param group
     * @param type
     * @return count of type in group
     *
     * @deprecated Use <code>ParameterValueGroup.groups(type.getName().getCode())</code>
     *             instead. It allows to fetch groups by name independently of the actual
     *             descriptor implementation. The returned list can be used for many operation,
     *             including fetching the count with <code>size()</code>.
     *
     * @see ParameterValueGroup#groups
     */ 
    public static int count(ParameterValueGroup group, GeneralParameterDescriptor type) {
        final GeneralParameterValue[] params = array( group );
        int count = 0;
        for (int i=0; i<params.length; i++) {
            final GeneralParameterValue param = params[i];
            if (param.getDescriptor() == type) {
                count++;
            }
        }
        return count;
    }

    /**
     * Check a parameter value against its {@linkplain ParameterDescriptor parameter descriptor}.
     * This method takes care of handling checking arrays and collections against parameter
     * descriptor.
     * <br><br>
     * When the {@linkplain ParameterDescriptor#getValueClass value class} is an array (like
     * {@code double[].class}) or a {@linkplain Collection collection} (like {@code List.class}),
     * the descriptor
     * {@linkplain ParameterDescriptor#getMinimumValue minimum value},
     * {@linkplain ParameterDescriptor#getMaxiumValue maximum value} and
     * {@linkplain ParameterDescriptor#getValidValues valid values}
     * will be used to check the elements.
     *
     * @param parameter The parameter to test.
     * @return true if parameter is valid.
     *
     * @see Parameter#ensureValidValue
     */
    public static boolean isValid(final ParameterValue parameter) {
        final ParameterDescriptor descriptor = (ParameterDescriptor) parameter.getDescriptor();
        final Object value = parameter.getValue();
        final Class  type  = (value == null) ? Void.TYPE : value.getClass();                
        final Class  kind  = descriptor.getValueClass();

        if (kind.isInstance(value)) {
            return false; // value not of the correct type
        }
        if (type.isArray()) {
            // handle checking elements in an aray
            final int length = Array.getLength(value);
            for (int i=0; i<length; i++) {
                if (!isValidValue(Array.get(value, i), descriptor)) {
                    return false;
                }
            }
        } else if (value instanceof Collection) {
            // handle checking elements in a collection
            final Collection collection = (Collection) value;
            for (final Iterator i=collection.iterator(); i.hasNext();) {
                if (!isValidValue(i.next(), descriptor)) {
                    return false;
                }
            }
        } else {
            isValidValue(value, descriptor);
        }
        return true;
    }

    /**
     * Called on a single {@linkplain ParameterValue parameter value}, or on elements of a
     * parameter value. This method ensures that
     * {@linkplain ParameterDescriptor#getMinimumValue minimum value},
     * {@linkplain ParameterDescriptor#getMaxiumValue maximum value} and
     * {@linkplain ParameterDescriptor#getValidValues valid values}
     * all think the provided value is okay.
     *
     * @param  value The value to test.
     * @param  descriptor The descriptor for the value.
     * @return true if parameter is valid.
     *
     * @see Parameter#ensureValidValue
     */
    private static boolean isValidValue(final Object value, final ParameterDescriptor descriptor) {
        final Class  type = (value == null) ? Void.TYPE : value.getClass();                
        final Class  expected = descriptor.getValueClass();
        final Set validValues = descriptor.getValidValues();
        if (validValues!=null && !validValues.contains(value)) {
            return false;
        }
        final Comparable min = descriptor.getMinimumValue();
        if (min!=null && min.compareTo(value) > 0) {
            return false;
        }
        final Comparable max = descriptor.getMaximumValue();
        if (max!=null && max.compareTo(value) < 0) {
            return false;
        }
        return true;
    }

    /**
     * Search for an exact match for the provided GeneralParameterDescriptor.
     * <p>
     * This method does not search in subgroups.
     * </p>
     * @param type GeneralParameterDescriptor to search for
     * @return List (possibly empty of GeneralParameter
     *
     * @deprecated Use {@link ParameterValue#parameter} or {@link ParameterValue#groups} instead.
     *  We would like to encourage peoples to use parameter name as key instead of parameter
     *  descriptor, because the parameter descriptor may not be always available. For example
     *  if a user know he is looking for the "semi_major" axis length (because it is documented
     *  in OpenGIS specification under that name) but don't know and don't care about who is
     *  providing the implementation, then he doesn't have the parameter descriptor; he only
     *  have the parameter name. Furthermore, parameter descriptor is implementation dependent.
     *  For example if we search for the "semi_major" axis length using the Geotools descriptor
     *  for this parameter, we will fail to find this parameter in a {@link ParameterValueGroup}
     *  provided by an other implementation. This is against GeoAPI goal, which is
     *  inter-operability. This doesn't mean that parameter descriptor should not be used.
     *  They are used for inspecting meta-data about parameters, not for searching parameters.
     *  Since each parameter name should be unique in a given parameter group (because
     *  {@link ParameterDescriptor#getMaximumOccurs} is always 1 for single parameter),
     *  the parameter name is a suffisient key for searching.
     */
    public static int indexOf(ParameterValueGroup group, GeneralParameterDescriptor type) {
        GeneralParameterValue[] params = array( group );
        if( params == null ){
            return -1;
        }        
        for (int i=0; i<params.length; i++) {
            final GeneralParameterValue param = params[i];
            if( param.getDescriptor() == type ){
                return i;
            }
        }
        return -1;
    }

    /**
     * Search for an exact match for the provided GeneralParameterDescriptor.
     * <p>
     * This method does not search in subgroups.
     * </p>
     * @param type GeneralParameterDescriptor to search for
     * @return List (possibly empty) of GeneralParameter
     *
     * @deprecated Use <code>{@linkplain #search(GeneralParameterValue,String,int) search}(group,
     * type.getName().getCode(), 1)</code> instead. See comments in {@link #indexOf} for a rational
     * about why name should be used as key instead of parameter descriptor.
     */
    public static List list(ParameterValueGroup group, GeneralParameterDescriptor type) {
        GeneralParameterValue[] params = array( group );
        if( params == null ){
            return Collections.EMPTY_LIST;
        }
        List list = new ArrayList(1);
        for (int i=0; i<params.length; i++) {
            final GeneralParameterValue param = params[i];
            if( param.getDescriptor() == type ){
                list.add( param );
            }
        }
        return list;
    }

    /**
     * Search for an exact match for the provided GeneralParameterDescriptor.
     * <p>
     * This method does search in subgroups.
     * </p>
     * @param type GeneralParameterDescriptor to search for
     * @return List (possibly empty of GeneralParameter
     *
     * @deprecated Use <code>{@linkplain #search(GeneralParameterValue,String,int) search}(group,
     * type.getName().getCode(), 100)</code> instead. See comments in {@link #indexOf} for a
     * rational about why name should be used as key instead of parameter descriptor.
     */
    public static List search(ParameterValueGroup group, GeneralParameterDescriptor type) {
        GeneralParameterValue[] params = array( group );
        if( params == null ){
            return Collections.EMPTY_LIST;
        }
        List list = new ArrayList(1);
        for (int i=0; i<params.length; i++) {
            final GeneralParameterValue param = params[i];
            if( param.getDescriptor() == type ){
                list.add( param );
            }
            if( param instanceof ParameterValueGroup ){
                List found = search( (ParameterValueGroup) param, type );
                list.addAll( found );
            }
        }
        return list;
    }

    /**
     * Search all parameters with the specified name. The given {@code name} is compared against
     * parameter {@link GeneralParameterDescriptor#getName name} and
     * {@link GeneralParameterDescriptor#getAlias alias}. This method search recursively
     * in subgroups up to the specified depth:
     * <br><br>
     * <ul>
     *   <li>If {@code maxDepth} is equals to 0, then this method returns {@code param}
     *       if and only if it matches the specified name.</li>
     *   <li>If {@code maxDepth} is equals to 1 and {@code param} is an instance of
     *       {@link ParameterDescriptorGroup}, then this method checks all elements
     *       in this group but not in subgroups.</li>
     *   <li>...</li>
     *   <li>If {@code maxDepth} is a high number (e.g. 100), then this method checks all elements
     *       in all subgroups up to the specified depth, which is likely to be never reached. In
     *       this case, {@code maxDepth} can be seen as a safeguard against never ending loops, for
     *       example if parameters graph contains cyclic entries.</li>
     * </ul>
     *
     * @param  param The parameter to inspect.
     * @param  name  The name of the parameter to search for.
     * @return The set (possibly empty) of parameters with the given name.
     */
    public static List search(final GeneralParameterValue param, final String name, int maxDepth) {
        final List list = new ArrayList();
        search(param, name, maxDepth, list);
        return list;
    }

    /**
     * Implementation of the search algorithm. The result is stored in the supplied set.
     */
    private static void search(final GeneralParameterValue param, final String name,
                               final int maxDepth, final Collection list)
    {
        if (maxDepth >= 0) {
            if (IdentifiedObject.nameMatches(param.getDescriptor(), name)) {
                list.add(param);
            }
            if (maxDepth!=0 && param instanceof ParameterValueGroup) {
                for (Iterator it=((ParameterValueGroup)param).values().iterator(); it.hasNext();) {
                    search((GeneralParameterValue) it.next(), name, maxDepth-1, list);
                }
            }
        }
    }

    /**
     * Gets a flat view of
     * {@linkplain ParameterDescriptor#getName name}-{@linkplain ParameterValue#getValue value}
     * pairs. This method copies all parameter values into the supplied {@code destination} map.
     * Keys are parameter names as {@link String} objects, and values are parameter values as
     * arbitrary objects. All subgroups (if any) are extracted recursively.
     *
     * @param  parameters  The parameters to extract values from.
     * @param  destination The destination map, or {@code null} for a default one.
     * @return [@code destination}, or a new map if {@code destination} was null.
     */
    public static Map toNameValueMap(final GeneralParameterValue parameters, Map destination) {
        if (destination == null) {
            destination = new LinkedHashMap();
        }
        if (parameters instanceof ParameterValue) {
            final ParameterValue param = (ParameterValue) parameters;
            final Object value = param.getValue();
            final Object old = destination.put(param.getDescriptor().getName().getCode(), value);
            if (old!=null && !old.equals(value)) {
                // TODO: This code will fails to detect if a null value was explicitly supplied
                //       previously. We assume that this case should be uncommon and not a big deal.
                throw new IllegalArgumentException("Inconsistent value"); // TODO: localize.
            }
        }
        if (parameters instanceof ParameterValueGroup) {
            final ParameterValueGroup group = (ParameterValueGroup) parameters;
            for (final Iterator it=group.values().iterator(); it.hasNext();) {
                destination = toNameValueMap((GeneralParameterValue) it.next(), destination);
            }
        }
        return destination;
    }
}
