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

import org.geotools.geometry.JTS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Utility methods for working against the FeatureType interface.
 * <p>
 * Many methods from DataUtilities should be refractored here.
 * </p><p>
 * Responsibilities:
 * <ul>
 * <li>Schema construction from String spec
 * <li>Schema Force CRS
 * </ul>
 * @author Jody Garnett, Refractions Research
 * @since 2.1.M3
 */
public class FeatureTypes {

    public static FeatureType transform(FeatureType schema, CoordinateReferenceSystem crs) throws SchemaException{
    	FeatureTypeFactory factory = FeatureTypeFactory.newInstance( schema.getTypeName() );
        
        factory.setNamespace( schema.getNamespace() );
        factory.setName( schema.getTypeName() );
        
        GeometryAttributeType defaultGeometryType = null;
        for( int i=0; i<schema.getAttributeCount(); i++ ){
            AttributeType attributeType = schema.getAttributeType( i );
            if( attributeType instanceof GeometryAttributeType ){
                GeometryAttributeType geometryType = (GeometryAttributeType) attributeType;
                GeometryAttributeType geometry;
                
                geometry = (GeometryAttributeType) AttributeTypeFactory.newAttributeType(
                        geometryType.getName(),
                        geometryType.getType(),
                        geometryType.isNillable(),
                        0,
                        geometryType.createDefaultValue(),
                        crs
                	);
                
                if( defaultGeometryType == null || 
                    geometryType == schema.getDefaultGeometry() ){
                    defaultGeometryType = geometry;
                }
                factory.addType( geometry );                
            }
			else {
			    factory.addType( attributeType );
			}            
		}
		factory.setDefaultGeometry( defaultGeometryType );
		return factory.getFeatureType();
    }
    
	/**
	 * Applies transform to all geometry attribute.
	 * 
	 * @param feature Feature to be transformed
	 * @param schema Schema for target transformation - transform( schema, crs )
	 * @param transform MathTransform used to transform coordinates - reproject( crs, crs )
	 * @return transformed Feature of type schema
	 * @throws TransformException
	 * @throws MismatchedDimensionException
	 * @throws IllegalAttributeException
	 */
	public static Feature transform( Feature feature, FeatureType schema, MathTransform transform ) throws MismatchedDimensionException, TransformException, IllegalAttributeException{
	    feature = schema.create( feature.getAttributes( null ), feature.getID() );
	    
	    GeometryAttributeType geomType = schema.getDefaultGeometry();
	    Geometry geom = (Geometry) feature.getAttribute( geomType.getName() );
	    
	    geom = JTS.transform( geom, transform );
	    
	    try {	        
            feature.setAttribute( geomType.getName(), geom );
        } catch (IllegalAttributeException shouldNotHappen) {
            // we are expecting the transform to return the same geometry type
        }
	    return feature;
	}
    
}
