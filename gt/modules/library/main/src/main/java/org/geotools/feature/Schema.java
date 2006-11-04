/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.feature;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.Hints;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.PropertyIsNull;

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
 * @since 2.1.0
 * @source $URL$
 */
public class Schema {
    private static Schema DEFAULT = new Schema();
    private FilterFactory ff;
    
    public Schema(){
        this( (Hints) null );
    }
    public Schema( Hints hints ){
        this( CommonFactoryFinder.getFilterFactory( hints ));
    }
    public Schema( FilterFactory filterFactory ){
        ff = filterFactory;
    }

    /**
     * Walk the provided FeatureType and produce a count of distinct attribtues.
     * <p>
     * used to detect duplicate attributes names (ie override)
     * </p>
     *  
     * @param featureType
     */
    public int getAttributeCount( FeatureType featureType ) {
        return getNames( featureType ).size();
    }
    
    /**
     * Does a quick walk to detect only a list of attribute names.
     * <p>
     * This method does not produce the complete schema (ie derrived restrictions based
     * on attribute facets). It is only used to get a list of the unique attribtues in
     * the resulting schema.
     * </p>
     * @param featureType
     * 
     * @return Set of unique attribute names
     */
    public List getNames( FeatureType featureType ) {
        return getNames( featureType, new ArrayList() );        
    }
    
        
    /**
     * This order is to be respected, based on Ancestors and so on.
     * <p>
     * This method is "faster" then actually constructing the merged
     * AttribtueTypes.
     * </p>
     */
    public List getNames( FeatureType featureType, List names ){
        if( featureType == null || featureType.getAttributeTypes() == null ){
            return names;
        }

        FeatureType ancestors[] = featureType.getAncestors();
        if( ancestors != null && ancestors.length != 0 ){
            for( int i=0, length = ancestors.length; i<length; i++ ){
                FeatureType superType = ancestors[i];
                getNames( superType, names );	           
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
        return names;
    }

    public List getAttributes( FeatureType featureType ){
        return getAttributes( featureType, new ArrayList() );                       
    }
    
    
    /**
     * This order is to be respected, based on Ancestors and so on.
     * <p>
     * This method is "faster" then actually constructing the merged
     * AttribtueTypes.
     * </p>
     */
    public List getAttributes( FeatureType featureType, List list ){
        if( featureType == null || featureType.getAttributeTypes() == null ) {
            return list;
        }

        FeatureType ancestors[] = featureType.getAncestors();
        if( ancestors != null && ancestors.length != 0 ){
            for( int i=0, length = ancestors.length; i<length; i++ ){
                //eatureType type = ancestors[i];
                getAttributes( ancestors[i], list );	           
	        }
        }
        AttributeType attributes[] = featureType.getAttributeTypes();
        if( attributes != null && attributes.length != 0 ){
            for( int i=0, length = attributes.length; i<length; i++ ){
                AttributeType type = attributes[i];
	            String name = type.getName();
	            int index = getIndexOf( list, name );
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
        return list;
    }
    /**
     * Query featureType information the complete restrictions for the indicated name.
     * 
     * @param featureType
     * @param name
     */
    public Filter getRestrictions( FeatureType featureType, String name ){
        if( featureType == null || featureType.getAttributeTypes() == null ) return Filter.EXCLUDE;
        
        return restriction( featureType, name, Filter.INCLUDE );
    }
    
    /**
     * Lookup can only really be by name.
     * 
     * @param type
     */
    public int getIndexOf( FeatureType type, String name ) {
        List names = getNames( type );
        return names.indexOf( name );
    }
    
    /**
     * Look up based on name in the provided position.
     * 
     * @param type  the FeatureType
     * @param index the position
     * 
     */
    public AttributeType getAttribute( FeatureType type, int index ) {
        String name = (String) getNames( type ).get( index );
        return getXPath( type, name );
    }
    
    public AttributeType getAttribute( FeatureType type, String name ){
    	List list = getAttributes( type );
    	int index = getIndexOf( list, name );
    	if( index == -1 ) return null;
    	return (AttributeType) list.get( index );
    }

    /**
     * Look up based on name in the provided position.
     * <p>
     * AttributeType needs a xpath based access
     * </p>
     * @param type
     * @param xpath
     * 
     */
    public AttributeType getXPath( FeatureType type, String xpath) {
        return getAttribute( type, xpath ); // for now, use JXPath later
    }
    
    // Utility Methods
    //
    private int getIndexOf( List attributes, String name ){
        int index = 0;
        for( Iterator i=attributes.iterator(); i.hasNext(); index++){
            AttributeType type = (AttributeType) i.next();
            if( name.equals( type.getName() )) return index;
        }
        return -1;
    }
    
    private AttributeType override(AttributeType type, AttributeType override ){
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
    
    private Filter restriction( FeatureType featureType, String name, Filter filter ){
        FeatureType ancestors[] = featureType.getAncestors();
        if( ancestors != null && ancestors.length != 0 ){
            for( int i=0, length = ancestors.length; i<length; i++ ){
                FeatureType superType = ancestors[i];
                filter = restriction( superType, name, filter );                              
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
    
    private Filter override( Filter filter, Filter override ){
        if( isNOP( override )){
            // no override is needed
            return filter;
        }
        else if( isNOP( filter )){
            return override;
        }
        else {            
            return ff.and( filter, override );
        }
    }
    
    private boolean isNOP( Filter filter ){
        return filter == null || filter instanceof PropertyIsNull || filter == Filter.INCLUDE;
    }
    
    // Utiltie Methods
    // (make use of DEFAULT Schema)
    //
    
    /**
     * Walk the provided FeatureType and produce a count of distinct attribtues.
     * <p>
     * used to detect duplicate attributes names (ie override)
     * </p>
     *  
     * @param featureType
     */
    public static int attributeCount( FeatureType featureType ){
        return DEFAULT.getAttributeCount(featureType);
    }    
    
    /**
     * @deprecated use getAttribute( type, index )
     */
    public static AttributeType attribute( FeatureType type, int index ) {
        return DEFAULT.getAttribute(type, index);
    }
    /** @deprecated use getAttribute( type, name ) */
    public static AttributeType attribute( FeatureType type, String name ){
        return DEFAULT.getAttribute(type, name );
    }
    /** @deprecated use getAttributes( featureType ) */
    public static List attributes( FeatureType featureType ){
        return DEFAULT.getAttributes(featureType);
    }    
    /** @deprecated use getAttributes( featureType, list ) */
    public static List attributes( FeatureType featureType, List list ){
        return DEFAULT.getAttributes(featureType, list);
    }
     
    /**
     * @deprecated please use getIndexOf( type, name )
     */
    public static int find( FeatureType type, String name ) {
        return DEFAULT.getIndexOf(type, name);
    }
    
    
    /**
     * @deprecated use getNames( featureType )
     */
    public static List names( FeatureType featureType ) {
        return DEFAULT.getNames(featureType);
    }
    
    /**
     * @deprecated use getNames( featureType, List )
     */
    public static List names( FeatureType featureType, List names ){
        return DEFAULT.getNames( featureType, names );
    }
    
    /**
     * @deprecated please use getRestriction( featureType, name )
     */
    public static Filter restriction( FeatureType featureType, String name ){
        return DEFAULT.getRestrictions(featureType, name);
    }
    
    /**
     * @deprecated use getXPath( type, xpath );
     */
    public static AttributeType xpath( FeatureType type, String xpath) {
        return DEFAULT.getAttribute( type, xpath ); // for now, use JXPath later
    }
}