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

import org.picocontainer.MutablePicoContainer;
import org.geotools.xml.BindingConfiguration;


/**
 * Binding configuration for the http://www.opengis.net/ogc schema.
 *
 * @generated
 */
public final class OGCBindingConfiguration implements BindingConfiguration {
    /**
     * @generated modifiable
     */
    public void configure(MutablePicoContainer container) {
        //expr.xsd
        container.registerComponentImplementation(OGC.BinaryOperatorType,
            OGCBinaryOperatorTypeBinding.class);
        container.registerComponentImplementation(OGC.ExpressionType, OGCExpressionTypeBinding.class);
        container.registerComponentImplementation(OGC.FunctionType, OGCFunctionTypeBinding.class);
        container.registerComponentImplementation(OGC.LiteralType, OGCLiteralTypeBinding.class);
        container.registerComponentImplementation(OGC.PropertyNameType,
            OGCPropertyNameTypeBinding.class);

        //filter.xsd
        container.registerComponentImplementation(OGC.BBOXType, OGCBBOXTypeBinding.class);
        container.registerComponentImplementation(OGC.BinaryComparisonOpType,
            OGCBinaryComparisonOpTypeBinding.class);
        container.registerComponentImplementation(OGC.BinaryLogicOpType,
            OGCBinaryLogicOpTypeBinding.class);
        container.registerComponentImplementation(OGC.BinarySpatialOpType,
            OGCBinarySpatialOpTypeBinding.class);
        //container.registerComponentImplementation(OGC.COMPARISONOPSTYPE,OGCComparisonOpsTypeBinding.class);
        container.registerComponentImplementation(OGC.DistanceBufferType,
            OGCDistanceBufferTypeBinding.class);
        container.registerComponentImplementation(OGC.DistanceType, OGCDistanceTypeBinding.class);
        container.registerComponentImplementation(OGC.FeatureIdType, OGCFeatureIdTypeBinding.class);
        container.registerComponentImplementation(OGC.FilterType, OGCFilterTypeBinding.class);
        //container.registerComponentImplementation(OGC.LOGICOPSTYPE,OGCLogicOpsTypeBinding.class);
        container.registerComponentImplementation(OGC.LowerBoundaryType,
            OGCLowerBoundaryTypeBinding.class);
        container.registerComponentImplementation(OGC.PropertyIsBetweenType,
            OGCPropertyIsBetweenTypeBinding.class);
        container.registerComponentImplementation(OGC.PropertyIsLikeType,
            OGCPropertyIsLikeTypeBinding.class);
        container.registerComponentImplementation(OGC.PropertyIsNullType,
            OGCPropertyIsNullTypeBinding.class);
        //container.registerComponentImplementation(OGC.SPATIALOPSTYPE,OGCSpatialOpsTypeBinding.class);
        container.registerComponentImplementation(OGC.UnaryLogicOpType,
            OGCUnaryLogicOpTypeBinding.class);
        container.registerComponentImplementation(OGC.UpperBoundaryType,
            OGCUpperBoundaryTypeBinding.class);
    }
}
