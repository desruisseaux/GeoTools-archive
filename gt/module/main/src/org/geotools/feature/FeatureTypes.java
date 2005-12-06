/*
 * Geotools2 - OpenSource mapping toolkit http://geotools.org (C) 2002, Geotools Project Managment
 * Committee (PMC) This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software Foundation;
 * version 2.1 of the License. This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.geotools.feature;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.geotools.factory.FactoryConfigurationError;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterType;
import org.geotools.filter.LengthFunction;
import org.geotools.filter.LiteralExpression;
import org.geotools.geometry.JTS;
import org.geotools.xml.gml.GMLSchema;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Utility methods for working against the FeatureType interface.
 * <p>
 * Many methods from DataUtilities should be refractored here.
 * </p>
 * <p>
 * Responsibilities:
 * <ul>
 * <li>Schema construction from String spec
 * <li>Schema Force CRS
 * </ul>
 * 
 * @author Jody Garnett, Refractions Research
 * @since 2.1.M3
 */
public class FeatureTypes {

    /** represent an unbounded field length */
    final public static int ANY_LENGTH = -1;

    /**
     * This is a 'suitable replacement for extracting the expected field length of an attribute
     * absed on its "facets" (ie Filter describing type restrictions);
     * <p>
     * This code is copied from the ShapefileDataStore where it was written (probably by dzwiers).
     * Cholmes is providing documentation.
     * </p>
     * 
     * @param attributeType
     * @return max length of field in characters, or ANY_LENGTH
     */
    public static int getFieldLength( AttributeType type ) {

        Class colType = type.getType();
        String colName = type.getName();

        int fieldLen = -1;
        Filter f = type.getRestriction();
        if (f != null
                && f != Filter.ALL
                && f != Filter.NONE
                && (f.getFilterType() == FilterType.COMPARE_LESS_THAN || f.getFilterType() == FilterType.COMPARE_LESS_THAN_EQUAL)) {
            try {
                CompareFilter cf = (CompareFilter) f;
                if (cf.getLeftValue() instanceof LengthFunction) {
                    return Integer.parseInt(((LiteralExpression) cf.getRightValue()).getLiteral()
                            .toString());
                } else if (cf.getRightValue() instanceof LengthFunction) {
                    return Integer.parseInt(((LiteralExpression) cf.getLeftValue()).getLiteral()
                            .toString());
                } else {
                    return ANY_LENGTH;
                }
            } catch (NumberFormatException e) {
                return ANY_LENGTH;
            }
        } else {
            return ANY_LENGTH;
        }
    }

    public static FeatureType transform( FeatureType schema, CoordinateReferenceSystem crs )
            throws SchemaException {
        FeatureTypeFactory factory = FeatureTypeFactory.newInstance(schema.getTypeName());

        factory.setNamespace(schema.getNamespace());
        factory.setName(schema.getTypeName());

        GeometryAttributeType defaultGeometryType = null;
        for( int i = 0; i < schema.getAttributeCount(); i++ ) {
            AttributeType attributeType = schema.getAttributeType(i);
            if (attributeType instanceof GeometryAttributeType) {
                GeometryAttributeType geometryType = (GeometryAttributeType) attributeType;
                GeometryAttributeType geometry;

                geometry = (GeometryAttributeType) AttributeTypeFactory.newAttributeType(
                        geometryType.getName(), geometryType.getType(), geometryType.isNillable(),
                        0, geometryType.createDefaultValue(), crs);

                if (defaultGeometryType == null || geometryType == schema.getDefaultGeometry()) {
                    defaultGeometryType = geometry;
                }
                factory.addType(geometry);
            } else {
                factory.addType(attributeType);
            }
        }
        factory.setDefaultGeometry(defaultGeometryType);
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
    public static Feature transform( Feature feature, FeatureType schema, MathTransform transform )
            throws MismatchedDimensionException, TransformException, IllegalAttributeException {
        feature = schema.create(feature.getAttributes(null), feature.getID());

        GeometryAttributeType geomType = schema.getDefaultGeometry();
        Geometry geom = (Geometry) feature.getAttribute(geomType.getName());

        geom = JTS.transform(geom, transform);

        try {
            feature.setAttribute(geomType.getName(), geom);
        } catch (IllegalAttributeException shouldNotHappen) {
            // we are expecting the transform to return the same geometry type
        }
        return feature;
    }

    /**
     * The most specific way to create a new FeatureType.
     * 
     * @param types The AttributeTypes to create the FeatureType with.
     * @param name The typeName of the FeatureType. Required, may not be null.
     * @param ns The namespace of the FeatureType. Optional, may be null.
     * @param isAbstract True if this created type should be abstract.
     * @param superTypes A Collection of types the FeatureType will inherit from. Currently, all
     *        types inherit from feature in the opengis namespace.
     * @return A new FeatureType created from the given arguments.
     * @throws FactoryConfigurationError If there are problems creating a factory.
     * @throws SchemaException If the AttributeTypes provided are invalid in some way.
     */
    public static FeatureType newFeatureType( AttributeType[] types, String name, URI ns,
            boolean isAbstract, FeatureType[] superTypes ) throws FactoryConfigurationError,
            SchemaException {
        return newFeatureType(types, name, ns, isAbstract, superTypes, null);
    }

    /**
     * The most specific way to create a new FeatureType.
     * 
     * @param types The AttributeTypes to create the FeatureType with.
     * @param name The typeName of the FeatureType. Required, may not be null.
     * @param ns The namespace of the FeatureType. Optional, may be null.
     * @param isAbstract True if this created type should be abstract.
     * @param superTypes A Collection of types the FeatureType will inherit from. Currently, all
     *        types inherit from feature in the opengis namespace.
     * @return A new FeatureType created from the given arguments.
     * @throws FactoryConfigurationError If there are problems creating a factory.
     * @throws SchemaException If the AttributeTypes provided are invalid in some way.
     */
    public static FeatureType newFeatureType( AttributeType[] types, String name, URI ns,
            boolean isAbstract, FeatureType[] superTypes, AttributeType defaultGeometry )
            throws FactoryConfigurationError, SchemaException {
        FeatureTypeFactory factory = FeatureTypeFactory.newInstance(name);
        factory.addTypes(types);
        factory.setNamespace(ns);
        factory.setAbstract(isAbstract);
        if (defaultGeometry != null)
            factory.setDefaultGeometry((GeometryAttributeType) defaultGeometry);

        if (superTypes != null) {
            factory.setSuperTypes(Arrays.asList(superTypes));
        }

        return factory.getFeatureType();
    }

    /**
     * The most specific way to create a new FeatureType.
     * 
     * @param types The AttributeTypes to create the FeatureType with.
     * @param name The typeName of the FeatureType. Required, may not be null.
     * @param ns The namespace of the FeatureType. Optional, may be null.
     * @param isAbstract True if this created type should be abstract.
     * @param superTypes A Collection of types the FeatureType will inherit from. Currently, all
     *        types inherit from feature in the opengis namespace.
     * @return A new FeatureType created from the given arguments.
     * @throws FactoryConfigurationError If there are problems creating a factory.
     * @throws SchemaException If the AttributeTypes provided are invalid in some way.
     */
    public static FeatureType newFeatureType( AttributeType[] types, String name, URI ns,
            boolean isAbstract, FeatureType[] superTypes, GeometryAttributeType defaultGeometry )
            throws FactoryConfigurationError, SchemaException {
        FeatureTypeFactory factory = FeatureTypeFactory.newInstance(name);
        factory.addTypes(types);
        factory.setNamespace(ns);
        factory.setAbstract(isAbstract);

        if (superTypes != null) {
            factory.setSuperTypes(Arrays.asList(superTypes));
        }

        if (defaultGeometry != null) {
            factory.setDefaultGeometry(defaultGeometry);
        }

        return factory.getFeatureType();
    }

    /**
     * Create a new FeatureType with the given AttributeTypes. A short cut for calling
     * <code>newFeatureType(types,name,ns,isAbstract,null)</code>.
     * 
     * @param types The AttributeTypes to create the FeatureType with.
     * @param name The typeName of the FeatureType. Required, may not be null.
     * @param ns The namespace of the FeatureType. Optional, may be null.
     * @param isAbstract True if this created type should be abstract.
     * @return A new FeatureType created from the given arguments.
     * @throws FactoryConfigurationError If there are problems creating a factory.
     * @throws SchemaException If the AttributeTypes provided are invalid in some way.
     */
    public static FeatureType newFeatureType( AttributeType[] types, String name, URI ns,
            boolean isAbstract ) throws FactoryConfigurationError, SchemaException {
        return newFeatureType(types, name, ns, isAbstract, null);
    }

    /**
     * Create a new FeatureType with the given AttributeTypes. A short cut for calling
     * <code>newFeatureType(types,name,ns,false,null)</code>.
     * 
     * @param types The AttributeTypes to create the FeatureType with.
     * @param name The typeName of the FeatureType. Required, may not be null.
     * @param ns The namespace of the FeatureType. Optional, may be null.
     * @return A new FeatureType created from the given arguments.
     * @throws FactoryConfigurationError If there are problems creating a factory.
     * @throws SchemaException If the AttributeTypes provided are invalid in some way.
     */
    public static FeatureType newFeatureType( AttributeType[] types, String name, URI ns )
            throws FactoryConfigurationError, SchemaException {
        return newFeatureType(types, name, ns, false);
    }

    /**
     * Create a new FeatureType with the given AttributeTypes. A short cut for calling
     * <code>newFeatureType(types,name,null,false,null)</code>. Useful for test cases or
     * datasources which may not allow a namespace.
     * 
     * @param types The AttributeTypes to create the FeatureType with.
     * @param name The typeName of the FeatureType. Required, may not be null.
     * @return A new FeatureType created from the given arguments.
     * @throws FactoryConfigurationError If there are problems creating a factory.
     * @throws SchemaException If the AttributeTypes provided are invalid in some way.
     */
    public static FeatureType newFeatureType( AttributeType[] types, String name )
            throws FactoryConfigurationError, SchemaException {
        return newFeatureType(types, name, GMLSchema.NAMESPACE, false);
    }

    /**
     * A query of the the types ancestor information.
     * <p>
     * This utility method may be used as common implementation for
     * <code>FeatureType.isDecendedFrom( namespace, typeName )</code>, however for specific uses,
     * such as GML, an implementor may be able to provide a more efficient implemenation based on
     * prior knolwege.
     * </p>
     * <p>
     * This is a proper check, if the provided FeatureType matches the given namespace and typename
     * it is <b>not </b> considered to be decended from itself.
     * </p>
     * 
     * @param featureType typeName with parentage in question
     * @param namespace namespace to match against, or null for a "wildcard"
     * @param typeName typename to match against, or null for a "wildcard"
     * @return true if featureType is a decendent of the indicated namespace & typeName
     */
    public static boolean isDecendedFrom( FeatureType featureType, URI namespace, String typeName ) {
        if (featureType == null)
            return false;
        FeatureType ancestors[] = featureType.getAncestors();
        if (ancestors != null) {
            TEST: for( int i = 0; i < ancestors.length; i++ ) {
                FeatureType ancestor = ancestors[i];
                if (namespace != null && !namespace.equals(ancestor.getNamespace())) {
                    continue TEST;
                }
                if (typeName != null && !namespace.equals(ancestor.getTypeName())) {
                    continue TEST;
                }
                return true; // we have a match
            }
        }
        return false;
    }
    public static boolean isDecendedFrom( FeatureType featureType, FeatureType isParentType ) {
        if (featureType == null || isParentType == null)
            return false;
        FeatureType ancestors[] = featureType.getAncestors();
        if (ancestors != null) {
            TEST: for( int i = 0; i < ancestors.length; i++ ) {
                FeatureType ancestor = ancestors[i];
                if (isParentType == ancestor)
                    return true;
                if (false) {
                    // hack idea #1?
                    if (isParentType.getNamespace().equals(ancestor.getNamespace())) {
                        continue TEST;
                    }
                    if (isParentType.equals(ancestor.getTypeName())) {
                        continue TEST;
                    }
                    return true; // match based on namespace, typeName
                }
            }
        }
        return false;
    }

    /** Exact equality based on typeNames, namespace, attributes and ancestors */
    public static boolean equals( FeatureType typeA, FeatureType typeB ) {
        if (typeA == typeB)
            return true;

        if (typeA == null || typeB == null) {
            return false;
        }
        return equalsId(typeA, typeB)
                && equals(typeA.getAttributeTypes(), typeB.getAttributeTypes()) &&
                equalsAncestors( typeA, typeB );
    }
    
    public static boolean equals( AttributeType attributesA[], AttributeType attributesB[] ) {
        if (attributesA.length != attributesB.length)
            return false;

        for( int i = 0, length = attributesA.length; i < length; i++ ) {
            if (!equals(attributesA[i], attributesB[i]))
                return false;
        }
        return true;
    }
    /**
     * This method depends on the correct implementation of FeatureType equals
     * <p>
     * We may need to write an implementation that can detect cycles,
     * </p>
     * 
     * @param typeA
     * @param typeB
     * @return
     */
    public static boolean equalsAncestors( FeatureType typeA, FeatureType typeB ) {
        return ancestors( typeA ).equals( typeB );
    }
    
    public static Set ancestors( FeatureType featureType ) {
        if (featureType == null || featureType.getAncestors() == null
                || featureType.getAncestors().length == 0) {
            return Collections.EMPTY_SET;
        }
        return new HashSet(Arrays.asList(featureType.getAncestors()));
    }
    
    public static boolean equals( AttributeType a, AttributeType b ) {
        return a == b || (a != null && a.equals(b));
    }
    /** Quick check of namespace and typename */
    public static boolean equalsId( FeatureType typeA, FeatureType typeB ) {
        if (typeA == typeB)
            return true;

        if (typeA == null || typeB == null) {
            return false;
        }

        String typeNameA = typeA.getTypeName();
        String typeNameB = typeB.getTypeName();
        if (typeNameA == null && typeNameB != null)
            return false;
        else if (!typeNameA.equals(typeNameB))
            return false;

        URI namespaceA = typeA.getNamespace();
        URI namespaceB = typeB.getNamespace();
        if (namespaceA == null && namespaceB != null)
            return false;
        else if (!namespaceA.equals(namespaceB))
            return false;

        return true;
    }
    
}
