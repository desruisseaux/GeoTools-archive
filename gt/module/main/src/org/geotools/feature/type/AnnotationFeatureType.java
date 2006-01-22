/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004, Geotools Project Managment Committee (PMC)
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
package org.geotools.feature.type;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;

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
 */
public class AnnotationFeatureType
{

    /**
     * The FeatureType reference that should be used for Anotations.
     */
    public static final FeatureType ANNOTATION;

    /**
     * The attribute name used to store the attribute name containing the annotation text.
     * This is basically just a level of redirection.  
     */
    public static final String ANNOTATION_ATTRIBUTE_NAME = "annotation_attribute_name";

    
    // Static initializer for the ANNOTATION variable
    static {
        FeatureType tmp = null;
        try {
            tmp = FeatureTypeFactory.newFeatureType( new AttributeType[] {
                AttributeTypeFactory.newAttributeType( ANNOTATION_ATTRIBUTE_NAME,
                                                       String.class,
                                                       true,
                                                       -1,
                                                       null  ) },
                                               "annotation" );
        } catch (Exception ex) {
            Logger.getLogger( "org.geotools.data.vpf.AnnotationFeatureType" ).log(
               Level.SEVERE, "Error creating ANNOTATION feature type", ex );
        }
        ANNOTATION = tmp;
    }

    /**
     * Noone else should be able to build me.
     */
    private AnnotationFeatureType(){}
}
