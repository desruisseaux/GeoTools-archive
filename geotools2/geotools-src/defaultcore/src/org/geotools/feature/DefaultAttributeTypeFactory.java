/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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

import org.opengis.sc.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Factory for creating DefaultAttributeTypes.
 *
 * @author Ian Schneider
 * @version $Id: DefaultAttributeTypeFactory.java,v 1.10 2004/02/11 21:31:18 ianschneider Exp $
 */
public class DefaultAttributeTypeFactory extends AttributeTypeFactory {
    /**
     * Create an AttributeType with the given name, Class, nillability, and
     * fieldLength meta-data. This method will itself call <code>
     * createAttributeType(String,Class,boolean,int,Object) </code> with null
     * as the default value. To use your own default value, use the above
     * method, providing your default value.
     *
     * @param name The name of the AttributeType to create.
     * @param clazz the class of the AttributeType to create.
     * @param isNillable whether the AttributeType should allow nulls.
     *
     * @return the newly created AttributeType
     */
    protected AttributeType createAttributeType(String name, Class clazz, 
        boolean isNillable, int fieldLength) {

        return createAttributeType(name,clazz,isNillable,fieldLength,null);
    }

    /**
     * Creates the DefaultAttributeType.Feature
     *
     * @param name The name of the AttributeType to create.
     * @param type To use for validation.
     * @param isNillable whether the AttributeType should allow nulls.
     *
     * @return the newly created feature AttributeType.
     */
    protected AttributeType createAttributeType(String name, FeatureType type,
        boolean isNillable) {
            
        return new DefaultAttributeType.Feature(name, type, isNillable,null);
    }
    
    /**
     * Implementation of AttributeType creation.
     */
    protected AttributeType createAttributeType(String name, Class clazz, 
        boolean isNillable, int fieldLength, Object defaultValue) {
            
        if (Number.class.isAssignableFrom(clazz)) {
            return new DefaultAttributeType.Numeric(
                name, clazz, isNillable,fieldLength,defaultValue);
        } else if (CharSequence.class.isAssignableFrom(clazz)) {
            return new DefaultAttributeType.Textual(name,isNillable,fieldLength,defaultValue);
        } else if (java.util.Date.class.isAssignableFrom(clazz)) {
            return new DefaultAttributeType.Temporal(name,isNillable,fieldLength,defaultValue);
        } else if (Geometry.class.isAssignableFrom( clazz )){
            return new DefaultAttributeType.Geometric(name,clazz,isNillable, fieldLength,defaultValue,null);
        }
        
        return new DefaultAttributeType(name, clazz, isNillable,fieldLength,defaultValue);
    }
    
    
    protected AttributeType createAttributeType( String name, Class clazz, 
        boolean isNillable, int fieldLength, Object defaultValue, 
        Object metaData ){
            
        if( Geometry.class.isAssignableFrom( clazz) && metaData instanceof CoordinateReferenceSystem ){
            return createAttributeType( name, clazz, isNillable, fieldLength, defaultValue, (CoordinateReferenceSystem) metaData );
        }
        else {
            return createAttributeType( name, clazz, isNillable, fieldLength, defaultValue );
        }
    }
}
