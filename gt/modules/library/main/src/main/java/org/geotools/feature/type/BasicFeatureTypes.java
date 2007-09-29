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
package org.geotools.feature.type;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

/**
 * Defines required attributes for Annotations.
 *
 * <p>
 * Annotations represent a text based geographic feature.
 * The geometry stored in the feature indicates where the
 * text should be drawn and the attribute indicated by
 * the {@link #ANNOTATION_ATTRIBUTE_NAME} attribute holds
 * the text to be displayed for the feature.
 * </p>
 *
 * <p>Example:
 * <pre>
 *   if ( feature.getFeatureType().isDescendedFrom( AnnotationFeatureType.ANNOTATION ) )
 *   {
 *     String attributeName = (String)feature.getAttribute( AnnotationFeatureType.ANNOTATION_ATTRIBUTE_NAME );
 *     String annotationText = (String)feature.getAttribute( attributeName );
 *     ... // Do something with the annotation text and feature
 *   }
 * </pre>
 * </p>
 *
 * @author John Meagher
 * @source $URL$
 */
public class BasicFeatureTypes
{

    /**
     * The FeatureType reference that should be used for Polygons
     */
    public static final SimpleFeatureType POLYGON;
    
    /**
     * The FeatureType reference that should be used for Points
     */
    public static final SimpleFeatureType POINT;
    
    /**
     * The FeatureType reference that should be used for Lines
     */
    public static final SimpleFeatureType LINE;

    /**
     * The attribute name used to store the geometry
     */
    public static final String GEOMETRY_ATTRIBUTE_NAME = "the_geom";

    
    // Static initializer for the tyoe variables
    static {
        SimpleFeatureType tmpPoint = null;
        SimpleFeatureType tmpPolygon = null;
        SimpleFeatureType tmpLine = null;
        try {
            SimpleFeatureTypeBuilder build = new SimpleFeatureTypeBuilder();
            
            //AttributeDescriptor[] types =  new AttributeDescriptor[] {};
            
            build.setName( "pointFeature" );
            tmpPoint = build.name("pointFeature").buildFeatureType();            
            tmpLine = build.name("lineFeature").buildFeatureType();
            tmpPolygon  = build.name("polygonFeature").buildFeatureType();            
        } catch (Exception ex) {
            Logger.getLogger( "org.geotools.feature.type.BasicFeatureTypes" ).log(
               Level.SEVERE, "Error creating basic feature types", ex );
        }
        POINT = tmpPoint;
        LINE = tmpLine;
        POLYGON = tmpPolygon;
        
    }

    /**
     * Noone else should be able to build me.
     */
    private BasicFeatureTypes(){}
}
