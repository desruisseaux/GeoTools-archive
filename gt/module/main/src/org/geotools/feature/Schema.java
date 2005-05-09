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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * This order is matched by  is to be respected, based on Ancestors and so on.
     * <p>
     * This method is "faster" then actually constructing the merged
     * AttribtueTypes.
     * </p>
     */
    private static void map( FeatureType featureType, Map map ){
        if( featureType == null || featureType.getAttributeTypes() == null ) return;

        FeatureType ancestors[] = featureType.getAncestors();
        if( ancestors != null && ancestors.length != 0 ){
            for( int i=0, length = ancestors.length; i<length; i++ ){
                FeatureType type = ancestors[i];
                map( ancestors[i], map );	           
	        }
        }
        AttributeType attributes[] = featureType.getAttributeTypes();
        if( attributes != null && attributes.length != 0 ){
            for( int i=0, length = attributes.length; i<length; i++ ){
                AttributeType type = attributes[i];
	            String name = type.getName();
	            if( map.containsKey( name )){
	                AttribtueSchema attribute = (AttribtueSchema) map.get( name );
	                attribute.override( type );
	            }
	            else {
	                map.put( name, new AttribtueSchema( type ) );
	            }
	        }
        }
    }
    
    private static Filter merge( Filter origional, Filter override ){
        if( isNOP( origional )) return override;
        if( isNOP( override )) return origional;
        return origional.and( override );
    }
    private static boolean isNOP( Filter filter ){
        return filter == null || filter.getFilterType() == Filter.NULL || Filter.NONE.equals( filter );
    }

    /**
     * Captures the internal model of an Attribute type based on all
     * its overrides.
     * <p>
     * This matches client's codes idea of what an attribtue type is.
     * </p>
     */
    static class AttribtueSchema {
        private Filter filter; // merged restrictions
        private AttributeType type;
        AttribtueSchema( AttributeType type ){
            this.type = type;
            this.filter = type.getRestriction();
        }
        /**
         * Compute the resulting schema from the merge of two attribtue types.
         * <p>
         * The facets are both considered as restrictions and are combined using
         * AND. AttributeType type narrowing is permitted.
         * </p>
         * @param origional
         * @param type
         * @return an AttributeType capturing the information of both orginal and type
         */
        public void override( AttributeType override ){
            filter = merge( filter, override.getRestriction() );
            type = override;
        }
        /**
         * We need to create a derrived type that includes merged content.
         * <p>
         * This should be done in a manner similar FeatureTypeBuilder.
         * </p>
         * @return AttributeType reflecting this entry;
         */
        public AttributeType type(){
            if( type.getRestriction() == filter ){
                // no overrides!
                return type;
            }
            return type; // can't do better then this right now (two many subclasses)
        }
    }
    
    /**
     * This is the internal model of the FeatureType "merged" with
     * all its ancestors.
     * <p>
     * This is lazy construction at its finest, the minimum is done upfront.
     * It may be used by implmentors as required.
     * </p>
     * 
     * @author Jody Garnett
     * @since 0.6.0
     */
    public static class FeatureSchema {
        private List names = null;
        private Map attribtues = null; // merged!
        private FeatureType type;
        
        FeatureSchema( FeatureType featureType ){
            type = featureType;
        }
        
        synchronized int count(){
            if( attribtues != null ) return attribtues.size();
            if( names != null ) return names.size();            
            return names().size();
        }
        synchronized List names(){
            if( names == null){
                names = new ArrayList();
                Schema.names( type, names );        
            }
            return names; 
        }
        synchronized Map attributes(){
            if( attribtues == null ){
                attribtues = new HashMap();
                Schema.map( type, attribtues );
            }
            return attribtues;
        }

        public synchronized AttributeType attribute( String name ) {
            AttribtueSchema attribute = (AttribtueSchema) attributes().get( name );
            return attribute.type();
        }        
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
        FeatureSchema schema = new FeatureSchema( type );
        String name = (String) schema.names().get( index );
        return schema.attribute( name );
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
        FeatureSchema schema = new FeatureSchema( type );
        return schema.attribute( xpath );
    }
}
