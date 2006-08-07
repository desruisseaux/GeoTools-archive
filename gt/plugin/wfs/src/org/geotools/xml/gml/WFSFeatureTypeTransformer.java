/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.xml.gml;

import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.SchemaException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * A sad hack class until the new Feature Model comes around. 
 * 
 * @see ChoiceAttributeType
 * @author Jesse
 * @since 1.1.0
 */
public class WFSFeatureTypeTransformer {

    public static FeatureType transform( FeatureType schema, CoordinateReferenceSystem crs ) throws SchemaException {
        FeatureTypeBuilder factory = FeatureTypeBuilder.newInstance(schema.getTypeName());

        factory.setNamespace(schema.getNamespace());
        factory.setName(schema.getTypeName());

        GeometryAttributeType defaultGeometryType = null;
        for( int i = 0; i < schema.getAttributeCount(); i++ ) {
            AttributeType attributeType = schema.getAttributeType(i);
            if( attributeType instanceof ChoiceAttributeType.Geometry ){
                defaultGeometryType = handleChoiceGeometryAttribute(schema, crs, factory, defaultGeometryType, attributeType);
            }else if (attributeType instanceof GeometryAttributeType) {
                defaultGeometryType = handleGeometryAttribute(schema, crs, factory, defaultGeometryType, attributeType);
            } else {
                factory.addType(attributeType);
            }
        }
        factory.setDefaultGeometry(defaultGeometryType);
        return factory.getFeatureType();
    }

    private static GeometryAttributeType handleGeometryAttribute( FeatureType schema, CoordinateReferenceSystem crs, FeatureTypeBuilder factory, GeometryAttributeType defaultGeometryType, AttributeType attributeType ) {
        GeometryAttributeType geometryType = (GeometryAttributeType) attributeType;
        GeometryAttributeType geometry;

        geometry = (GeometryAttributeType) AttributeTypeFactory.newAttributeType(
                geometryType.getName(), geometryType.getType(), geometryType.isNillable(),
                0, geometryType.createDefaultValue(), crs);

        if (defaultGeometryType == null || geometryType == schema.getDefaultGeometry()) {
            defaultGeometryType = geometry;
        }
        factory.addType(geometry);
        return defaultGeometryType;
    }

    private static GeometryAttributeType handleChoiceGeometryAttribute( FeatureType schema, CoordinateReferenceSystem crs, FeatureTypeBuilder factory, GeometryAttributeType defaultGeometryType, AttributeType attributeType ) {
        ChoiceAttributeType.Geometry geometryType = (ChoiceAttributeType.Geometry) attributeType;
        ChoiceAttributeType.Geometry geometry;

        geometry = new ChoiceAttributeTypeImpl.Geometry(
                geometryType.getName(), geometryType.getChoices(), geometryType.getType(), geometryType.isNillable(),
                geometryType.getMinOccurs(), geometryType.getMaxOccurs(), geometryType.createDefaultValue(), crs, geometryType.getRestriction());

        if (defaultGeometryType == null || geometryType == schema.getDefaultGeometry()) {
            defaultGeometryType = geometry;
        }
        factory.addType(geometry);
        return defaultGeometryType;
    }

}
