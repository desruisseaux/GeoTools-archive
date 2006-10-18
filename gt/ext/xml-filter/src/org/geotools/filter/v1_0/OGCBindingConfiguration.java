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
        container.registerComponentImplementation(OGC.BINARYOPERATORTYPE,
            OGCBinaryOperatorTypeBinding.class);
        container.registerComponentImplementation(OGC.EXPRESSIONTYPE, OGCExpressionTypeBinding.class);
        container.registerComponentImplementation(OGC.FUNCTIONTYPE, OGCFunctionTypeBinding.class);
        container.registerComponentImplementation(OGC.LITERALTYPE, OGCLiteralTypeBinding.class);
        container.registerComponentImplementation(OGC.PROPERTYNAMETYPE,
            OGCPropertyNameTypeBinding.class);

        //filter.xsd
        container.registerComponentImplementation(OGC.BBOXTYPE, OGCBBOXTypeBinding.class);
        container.registerComponentImplementation(OGC.BINARYCOMPARISONOPTYPE,
            OGCBinaryComparisonOpTypeBinding.class);
        container.registerComponentImplementation(OGC.BINARYLOGICOPTYPE,
            OGCBinaryLogicOpTypeBinding.class);
        container.registerComponentImplementation(OGC.BINARYSPATIALOPTYPE,
            OGCBinarySpatialOpTypeBinding.class);
        //container.registerComponentImplementation(OGC.COMPARISONOPSTYPE,OGCComparisonOpsTypeBinding.class);
        container.registerComponentImplementation(OGC.DISTANCEBUFFERTYPE,
            OGCDistanceBufferTypeBinding.class);
        container.registerComponentImplementation(OGC.DISTANCETYPE, OGCDistanceTypeBinding.class);
        container.registerComponentImplementation(OGC.FEATUREIDTYPE, OGCFeatureIdTypeBinding.class);
        container.registerComponentImplementation(OGC.FILTERTYPE, OGCFilterTypeBinding.class);
        //container.registerComponentImplementation(OGC.LOGICOPSTYPE,OGCLogicOpsTypeBinding.class);
        container.registerComponentImplementation(OGC.LOWERBOUNDARYTYPE,
            OGCLowerBoundaryTypeBinding.class);
        container.registerComponentImplementation(OGC.PROPERTYISBETWEENTYPE,
            OGCPropertyIsBetweenTypeBinding.class);
        container.registerComponentImplementation(OGC.PROPERTYISLIKETYPE,
            OGCPropertyIsLikeTypeBinding.class);
        container.registerComponentImplementation(OGC.PROPERTYISNULLTYPE,
            OGCPropertyIsNullTypeBinding.class);
        //container.registerComponentImplementation(OGC.SPATIALOPSTYPE,OGCSpatialOpsTypeBinding.class);
        container.registerComponentImplementation(OGC.UNARYLOGICOPTYPE,
            OGCUnaryLogicOpTypeBinding.class);
        container.registerComponentImplementation(OGC.UPPERBOUNDARYTYPE,
            OGCUpperBoundaryTypeBinding.class);
    }
}
