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
package org.geotools.filter.v1_0;

import javax.xml.namespace.QName;


/**
 * This interface contains the qualified names of all the types in the
 * http://www.opengis.net/ogc schema.
 *
 * @generated
 */
public interface OGC {
    /** @generated */
    public static final String NAMESPACE = "http://www.opengis.net/ogc";

    //expr.xsd
    /** @generated */
    public static final QName BINARYOPERATORTYPE = new QName("http://www.opengis.net/ogc",
            "BinaryOperatorType");

    /** @generated */
    public static final QName EXPRESSIONTYPE = new QName("http://www.opengis.net/ogc",
            "ExpressionType");

    /** @generated */
    public static final QName FUNCTIONTYPE = new QName("http://www.opengis.net/ogc",
            "FunctionType");

    /** @generated */
    public static final QName LITERALTYPE = new QName("http://www.opengis.net/ogc",
            "LiteralType");

    /** @generated */
    public static final QName PROPERTYNAMETYPE = new QName("http://www.opengis.net/ogc",
            "PropertyNameType");

    //filter.xsd
    /** @generated */
    public static final QName BBOXTYPE = new QName("http://www.opengis.net/ogc",
            "BBOXType");

    /** @generated */
    public static final QName BINARYCOMPARISONOPTYPE = new QName("http://www.opengis.net/ogc",
            "BinaryComparisonOpType");

    /** @generated */
    public static final QName BINARYLOGICOPTYPE = new QName("http://www.opengis.net/ogc",
            "BinaryLogicOpType");

    /** @generated */
    public static final QName BINARYSPATIALOPTYPE = new QName("http://www.opengis.net/ogc",
            "BinarySpatialOpType");

    /** @generated */
    public static final QName COMPARISONOPSTYPE = new QName("http://www.opengis.net/ogc",
            "ComparisonOpsType");

    /** @generated */
    public static final QName DISTANCEBUFFERTYPE = new QName("http://www.opengis.net/ogc",
            "DistanceBufferType");

    /** @generated */
    public static final QName DISTANCETYPE = new QName("http://www.opengis.net/ogc",
            "DistanceType");

    /** @generated */
    public static final QName FEATUREIDTYPE = new QName("http://www.opengis.net/ogc",
            "FeatureIdType");

    /** @generated */
    public static final QName FILTERTYPE = new QName("http://www.opengis.net/ogc",
            "FilterType");

    /** @generated */
    public static final QName LOGICOPSTYPE = new QName("http://www.opengis.net/ogc",
            "LogicOpsType");

    /** @generated */
    public static final QName LOWERBOUNDARYTYPE = new QName("http://www.opengis.net/ogc",
            "LowerBoundaryType");

    /** @generated */
    public static final QName PROPERTYISBETWEENTYPE = new QName("http://www.opengis.net/ogc",
            "PropertyIsBetweenType");

    /** @generated */
    public static final QName PROPERTYISLIKETYPE = new QName("http://www.opengis.net/ogc",
            "PropertyIsLikeType");

    /** @generated */
    public static final QName PROPERTYISNULLTYPE = new QName("http://www.opengis.net/ogc",
            "PropertyIsNullType");

    /** @generated */
    public static final QName SPATIALOPSTYPE = new QName("http://www.opengis.net/ogc",
            "SpatialOpsType");

    /** @generated */
    public static final QName UNARYLOGICOPTYPE = new QName("http://www.opengis.net/ogc",
            "UnaryLogicOpType");

    /** @generated */
    public static final QName UPPERBOUNDARYTYPE = new QName("http://www.opengis.net/ogc",
            "UpperBoundaryType");
}
