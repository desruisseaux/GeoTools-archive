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

// opengis interfaces 
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.referencing.IdentifiedObject;

/**
 * Utility class for methods helping implementing, and working with the
 * Parameter API from org.opengis.parameter.
 * 
 * @author Jody Garnett (Refractions Research)
 */
public class Parameters {
    /** Empty ParameterGroup */
    public static ParameterDescriptorGroup EMPTY_GROUP =
            new org.geotools.parameter.ParameterDescriptorGroup("empty",
            new GeneralParameterDescriptor[0]);
    
    /** Locate by GeneralParameterDescriptor by ID (rather than name).
     * <p>
     * Name is localized - and not especially useful as a key to retrive
     * parameters. This lookup offers a bridge allowing you to lookup a
     * GeneralParameterDescriptor and use list or seach.
     * </p>
     * @param type
     * @return
     */
    public static GeneralParameterDescriptor id( ParameterDescriptorGroup type, String id ){
//        GeneralParameterDescriptor types[] = type.descriptors();
        
        return null;
    }
    public static IdentifiedObject id( IdentifiedObject objs[], String id ){
        for( int i=0; i<objs.length; i++){
            IdentifiedObject obj = objs[i];
            
        }
        return null;
    }
    public static boolean allowed( ParameterDescriptorGroup group, GeneralParameterDescriptor type){
        List types = group.descriptors();
        for(final Iterator it=types.iterator(); it.hasNext();) {
            if (it.next() == type) {
                return true;
            }
        }
        return false;
    }
    
    /** Used to handle code that expexted group.getValues() */
    public static GeneralParameterValue[] array( ParameterValueGroup group ){
        List params = group.values(); 
        return (GeneralParameterValue[]) params.toArray( new GeneralParameterValue[ params.size()] );
    }
    /**
     * Number of type in group.
     * 
     * @param group
     * @param type
     * @return count of type in group
     */ 
    public static int count( ParameterValueGroup group, GeneralParameterDescriptor type){
        GeneralParameterValue[] params = array( group );
        ParameterDescriptorGroup groupType = (ParameterDescriptorGroup) group.getDescriptor();        
        int count = 0;
        for (int i=0; i<params.length; i++) {
            final GeneralParameterValue param = params[i];
            if( param.getDescriptor() == type ){
                count++;
            }
        }
        return count;
    }
    /**
     * Check a ParameterValue against its ParameterDescriptor.
     * <p>
     * Method takes care of handling checking arrays and collections
     * against ParameterDescriptor.
     * </p>
     * <p>
     * Assumes that descriptor.getValueClass will be an Array (like double[].class)
     * or a Collection (like List.class) when working with those types, even 
     * though descriptor getMinimumValue(), getMaxiumValue and getValidValues
     * will be used to check the elements.
     * </p>
     * @return true if parameter is valid
     */
    public static boolean isValid( ParameterValue parameter ){
        ParameterDescriptor descriptor = (ParameterDescriptor) parameter.getDescriptor();
        Object value = parameter.getValue();
        Class type = value == null ? Void.TYPE : value.getClass();                
        Class kind = descriptor.getValueClass();
        
        if( kind.isInstance( value )){
            return false; // value not of the correct type
        }
        if( type.isArray() ){
            // handle checking elements in an aaray
            final int LENGTH = Array.getLength( value );
            for( int i=0; i<LENGTH; i++ ){
                if( !isValidValue( Array.get( value, i ), descriptor ) ){
                    return false;
                }
            }
        }
        else if( value instanceof Collection ){
            // handle checking elements in a collection
            Collection collection = (Collection) value;
            for( Iterator i=collection.iterator(); i.hasNext(); ){
                if( !isValidValue( i.next(), descriptor ) ){
                    return false;
                }
            }
        }
        else {
            isValidValue( value, descriptor );
        }
        return true;
    }    
    /**
     * Called on a single ParameterValue, or on elements of a ParameterValue
     * <p>
     * Ensures that descriptor.getValueValues(), getMinimumValue() and getMaximumValue()
     * all think the provided value is okay.
     * </p>
     * @return true if value is valid
     */
    private static boolean isValidValue( Object value, ParameterDescriptor descriptor ){
        Class type = value == null ? Void.TYPE : value.getClass();                
        Class expected = descriptor.getValueClass();
        
        Set validValues = descriptor.getValidValues();
        if( validValues != null && !validValues.contains( value )){
            return false;
        }
        Comparable min = descriptor.getMinimumValue();
        if( min != null && min.compareTo( value ) > 0 ){
            return false;
        }
        Comparable max = descriptor.getMinimumValue();
        if( max != null && max.compareTo( value ) < 0 ){
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
     */
    public static int indexOf( ParameterValueGroup group, GeneralParameterDescriptor type){
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
     * @return List (possibly empty of GeneralParameter
     */
    public static List list( ParameterValueGroup group, GeneralParameterDescriptor type){
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
     */
    public static List search( ParameterValueGroup group, GeneralParameterDescriptor type){
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
}
