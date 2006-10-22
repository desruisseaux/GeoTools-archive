/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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

import org.geotools.feature.type.FeatureAttributeType;
import org.geotools.feature.type.GeometricAttributeType;
import org.geotools.feature.type.NumericAttributeType;
import org.geotools.feature.type.TemporalAttributeType;
import org.geotools.feature.type.TextualAttributeType;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FilterType;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.LengthFunction;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Factory for creating DefaultAttributeTypes.
 *
 * @author Ian Schneider
 * @source $URL$
 * @version $Id$
 */
public class DefaultAttributeTypeFactory extends AttributeTypeFactory {
	private FilterFactory ff;
	public DefaultAttributeTypeFactory(){
		this( FilterFactoryFinder.createFilterFactory());
	}
	public DefaultAttributeTypeFactory( FilterFactory factory ){
		ff = factory;
	}
	public void setFilterFactory( FilterFactory factory ){
		ff = factory;
	}
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
            
        return new FeatureAttributeType(name, type, isNillable,1,1);
    }
    
    protected Filter length(int fieldLength){
        LengthFunction length = (LengthFunction)ff.createFunctionExpression("LengthFunction");
        if( length == null ) {
            // TODO: Help Richard! ff.createFunctionExpression cannot find Length!
            return null;
        }
        
        length.setArgs(new Expression[]{null});
        CompareFilter cf = null;
        try {
            cf = ff.createCompareFilter(FilterType.COMPARE_LESS_THAN_EQUAL);
            cf.addLeftValue(length);
            cf.addRightValue(ff.createLiteralExpression(fieldLength));
        } catch (IllegalFilterException e) {
            // TODO something
        }
        return cf == null ? Filter.ALL : cf;
    }
    
    /**
     * Implementation of AttributeType creation.
     */
    protected AttributeType createAttributeType(String name, Class clazz, 
        boolean isNillable, int fieldLength, Object defaultValue) {
        Filter f = length( fieldLength );
        
        if (Number.class.isAssignableFrom(clazz)) {
            return new NumericAttributeType(
                name, clazz, isNillable,1,1,defaultValue,f);
        } else if (CharSequence.class.isAssignableFrom(clazz)) {
            return new TextualAttributeType(name,isNillable,1,1,defaultValue,f);
        } else if (java.util.Date.class.isAssignableFrom(clazz)) {
            return new TemporalAttributeType(name,isNillable,1,1,defaultValue,f);
        } else if (Geometry.class.isAssignableFrom( clazz )){
            return new GeometricAttributeType(name,clazz,isNillable,1,1, defaultValue,null,f);
        }        
        return new DefaultAttributeType(name, clazz, isNillable,1,1,defaultValue, f);
    }
    
    /**
     * Implementation of AttributeType creation.
     */
    protected AttributeType createAttributeType(String name, Class clazz, 
        boolean isNillable, Filter filter, Object defaultValue, Object metadata) {

    	LengthFunction length = (LengthFunction)ff.createFunctionExpression("LengthFunction");
    	length.setArgs(new Expression[]{null});
    	if (Number.class.isAssignableFrom(clazz)) {
            return new NumericAttributeType(
                name, clazz, isNillable,1,1,defaultValue, filter);
        } else if (CharSequence.class.isAssignableFrom(clazz)) {
            return new TextualAttributeType(name,isNillable,1,1,defaultValue,filter);
        } else if (java.util.Date.class.isAssignableFrom(clazz)) {
            return new TemporalAttributeType(name,isNillable,1,1,defaultValue,filter);
        } else if (Geometry.class.isAssignableFrom( clazz )){
            if( metadata instanceof CoordinateReferenceSystem )
                return new GeometricAttributeType(name,clazz,isNillable,1,1, 
    	 	    defaultValue,(CoordinateReferenceSystem) metadata,filter);
	    else
		return new GeometricAttributeType(name,clazz,isNillable,1,1, defaultValue,null,filter);
	}        
        return new DefaultAttributeType(name, clazz, isNillable,1,1,defaultValue,filter);
    }
    
    protected AttributeType createAttributeType( String name, Class clazz, 
        boolean isNillable, int fieldLength, Object defaultValue, 
        Object metaData ){
            
        if( Geometry.class.isAssignableFrom( clazz) && metaData instanceof CoordinateReferenceSystem ){
            LengthFunction length = (LengthFunction)ff.createFunctionExpression("LengthFunction");
            length.setArgs(new Expression[]{null});
            CompareFilter cf = null;
            try {
                cf = ff.createCompareFilter(FilterType.COMPARE_LESS_THAN_EQUAL);
            cf.addLeftValue(length);
            cf.addRightValue(ff.createLiteralExpression(fieldLength));
            } catch (IllegalFilterException e) {
                // TODO something
            }
            Filter f = cf == null?Filter.ALL:cf;
            return new GeometricAttributeType(name,clazz,isNillable,1,1, defaultValue, (CoordinateReferenceSystem) metaData,f);
        }
        return createAttributeType( name, clazz, isNillable, fieldLength, defaultValue );
    }
}
