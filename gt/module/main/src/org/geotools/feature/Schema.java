/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.feature;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geotools.filter.Filter;

/**
 * This class contains utility methods focused on the schema represented by
 * the FeatureType data model.
 * <p>
 * These methods are often used for implementation the convience methods
 * such as FeatureType.getAttributeCount(), although they may be used directly
 * with any FeatureType.
 * </p>
 * <p>
 * These schema methods are based on the *complete* picture indicated by a FeatureType
 * and its ancestors. Many of these methods are focused on the derivation of AttribtueTypes
 * during an override.
 * </p>
 * @see FeatureTypes
 * @see FeatureType
 * @author Jody Garnett
 * @since 0.6.0
 */
public class Schema {
    /**
     * Walk the provided FeatureType and produce a count of distinct attribtues.
     * 
     * @param featureType
     * @return
     */
    public static int attribtueCount( FeatureType featureType ) {
//      used to detect duplicate attributes names (ie override)
        return names( featureType ).size();
    }
    /**
     * Does a quick walk to detect only a list of attribute names.
     * <p>
     * This method does not produce the complete schema (ie derrived restrictions based
     * on attribute facets). It is only used to get a list of the unique attribtues in
     * the resulting schema.
     * </p>
     * @param FeatureType
     * @return Set of unique attribute names
     */
    public static List names( FeatureType featureType ) {
        List names = new ArrayList();
        names( featureType, names );        
        return names;
    }
    
    public static List attributes( FeatureType featureType ){
    	List list = new ArrayList();
    	attributes( featureType, list );        
        return list;
    	
    }
    /**
     * This order is to be respected, based on Ancestors and so on.
     * <p>
     * This method is "faster" then actually constructing the merged
     * AttribtueTypes.
     * </p>
     */
    public static void names( FeatureType featureType, List names ){
        if( featureType == null || featureType.getAttributeTypes() == null ) return;

        FeatureType ancestors[] = featureType.getAncestors();
        if( ancestors != null && ancestors.length != 0 ){
            for( int i=0, length = ancestors.length; i<length; i++ ){
                FeatureType type = ancestors[i];
                names( ancestors[i], names );	           
	        }
        }
        AttributeType attributes[] = featureType.getAttributeTypes();
        if( attributes != null && attributes.length != 0 ){
            for( int i=0, length = attributes.length; i<length; i++ ){
                AttributeType type = attributes[i];
	            String name = type.getName();
	            if( !names.contains( name )){
	                names.add( name );
	            }
	        }
        }
    }
    
    /**
     * This order is to be respected, based on Ancestors and so on.
     * <p>
     * This method is "faster" then actually constructing the merged
     * AttribtueTypes.
     * </p>
     */
    public static void attributes( FeatureType featureType, List list ){
        if( featureType == null || featureType.getAttributeTypes() == null ) return;

        FeatureType ancestors[] = featureType.getAncestors();
        if( ancestors != null && ancestors.length != 0 ){
            for( int i=0, length = ancestors.length; i<length; i++ ){
                FeatureType type = ancestors[i];
                attributes( ancestors[i], list );	           
	        }
        }
        AttributeType attributes[] = featureType.getAttributeTypes();
        if( attributes != null && attributes.length != 0 ){
            for( int i=0, length = attributes.length; i<length; i++ ){
                AttributeType type = attributes[i];
	            String name = type.getName();
	            int index = indexOf( list, name );
	            if( index != -1 ){
	            	AttributeType origional = (AttributeType) list.get( index );
	            	list.remove( index );
	            	list.add( index, override( origional, type ));
	            }
	            else {
	            	list.add( type );
	            }
	        }
        }
    }
    
    private static int indexOf( List attributes, String name ){
    	int index = 0;
    	for( Iterator i=attributes.iterator(); i.hasNext(); index++){
    		AttributeType type = (AttributeType) i.next();
    		if( name.equals( type.getName() )) return index;
    	}
    	return -1;
    }
    private static AttributeType override(AttributeType type, AttributeType override ){
    	int max = override.getMaxOccurs();
    	if( max < 0 ) max = type.getMinOccurs();
    	
    	int min = override.getMinOccurs();
    	if( min < 0 ) min = type.getMinOccurs();
    	
    	String name = override.getName();
    	if( name == null ) name = type.getName();
    	
    	Filter restriction = override( type.getRestriction(), override.getRestriction() );
    	
    	Class javaType = override.getType();
    	if( javaType == null ) javaType = type.getType();
    	
    	boolean isNilable = override.isNillable();
    	
    	Object defaultValue = override.createDefaultValue();
    	if( defaultValue == null ) defaultValue = type.createDefaultValue();
    	
    	// WARNING cannot copy metadata!    	
    	return AttributeTypeFactory.newAttributeType( name, javaType, isNilable, restriction, defaultValue, null );    	
    }
    
    /**
     * Query featureType information the complete restrictions for the indicated name.
     * 
     * @param featureType
     * @param name
     * @return
     */
    public static Filter restriction( FeatureType featureType, String name ){
        if( featureType == null || featureType.getAttributeTypes() == null ) return Filter.ALL;
        
        return restriction( featureType, name, Filter.NONE );
    }
    private static Filter restriction( FeatureType featureType, String name, Filter filter ){
        FeatureType ancestors[] = featureType.getAncestors();
        if( ancestors != null && ancestors.length != 0 ){
            for( int i=0, length = ancestors.length; i<length; i++ ){
                FeatureType type = ancestors[i];
                filter = restriction( featureType, name, filter );                	          
	        }
        }
        AttributeType attributes[] = featureType.getAttributeTypes();
        if( attributes != null && attributes.length != 0 ){
            for( int i=0, length = attributes.length; i<length; i++ ){
                AttributeType type = attributes[i];
	            if( name.equals( type.getName() )){
	            	filter = override( filter, type.getRestriction() );	            	
	            }
	        }
        }
        return filter;
    }
    private static Filter override( Filter filter, Filter override ){
    	if( isNOP( override )){
    		// no override is needed
    		return filter;
    	}
    	else if( isNOP( filter )){
    		return override;
    	}
    	else {
    		return filter.and( override );
    	}
    }
    private static boolean isNOP( Filter filter ){
        return filter == null || filter.getFilterType() == Filter.NULL || Filter.NONE.equals( filter );
    }
    
    /**
     * Lookup can only really be by name.
     * 
     * @param type
     * @return
     */
    public static int find( FeatureType type, String name ) {
        List names = names( type );
        return names.indexOf( name );
    }
    /**
     * Look up based on name in the provided position.
     * 
     * @param position
     * @return
     */
    public static AttributeType attribute( FeatureType type, int index ) {
        String name = (String) names( type ).get( index );
        return xpath( type, name );
    }
    
    public static AttributeType attribute( FeatureType type, String name ){
    	List list = attributes( type );
    	int index = indexOf( list, name );
    	if( index == -1 ) return null;
    	return (AttributeType) list.get( index );
    }
    
    /**
     * Look up based on name in the provided position.
     * <p>
     * AttributeType needs a xpath based access
     * </p>
     * @param position
     * @return
     */
    public static AttributeType xpath( FeatureType type, String xpath) {
        return attribute( type, xpath ); // for now, use JXPath later
    }
}
