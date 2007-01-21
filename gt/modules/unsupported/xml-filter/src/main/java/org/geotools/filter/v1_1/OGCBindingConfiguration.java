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
package org.geotools.filter.v1_1;

import org.picocontainer.MutablePicoContainer;
import org.geotools.filter.v1_0.OGCBBOXTypeBinding;
import org.geotools.filter.v1_0.OGCDistanceTypeBinding;
import org.geotools.filter.v1_0.OGCExpressionTypeBinding;
import org.geotools.filter.v1_0.OGCFunctionTypeBinding;
import org.geotools.filter.v1_0.OGCLiteralTypeBinding;
import org.geotools.filter.v1_0.OGCLowerBoundaryTypeBinding;
import org.geotools.filter.v1_0.OGCPropertyIsBetweenTypeBinding;
import org.geotools.filter.v1_0.OGCPropertyIsNullTypeBinding;
import org.geotools.filter.v1_0.OGCPropertyNameTypeBinding;
import org.geotools.filter.v1_0.OGCUpperBoundaryTypeBinding;
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
        //Types
        //container.registerComponentImplementation(OGC.ABSTRACTIDTYPE,AbstractIdTypeBinding.class);
        container.registerComponentImplementation(OGC.ArithmeticOperatorsType,
            ArithmeticOperatorsTypeBinding.class);
        //container.registerComponentImplementation(OGC.BBOXTYPE,BBOXTypeBinding.class);
        container.registerComponentImplementation(OGC.BBOXType, OGCBBOXTypeBinding.class);
        container.registerComponentImplementation(OGC.BinaryComparisonOpType,
            BinaryComparisonOpTypeBinding.class);
        container.registerComponentImplementation(OGC.BinaryLogicOpType,
            BinaryLogicOpTypeBinding.class);
        container.registerComponentImplementation(OGC.BinaryOperatorType,
            BinaryOperatorTypeBinding.class);
        container.registerComponentImplementation(OGC.BinarySpatialOpType,
            BinarySpatialOpTypeBinding.class);
        container.registerComponentImplementation(OGC.ComparisonOperatorsType,
            ComparisonOperatorsTypeBinding.class);
        container.registerComponentImplementation(OGC.ComparisonOperatorType,
            ComparisonOperatorTypeBinding.class);
        //container.registerComponentImplementation(OGC.COMPARISONOPSTYPE,ComparisonOpsTypeBinding.class);
        container.registerComponentImplementation(OGC.DistanceBufferType,
            DistanceBufferTypeBinding.class);
        //container.registerComponentImplementation(OGC.DISTANCETYPE,DistanceTypeBinding.class);
        container.registerComponentImplementation(OGC.DistanceType, OGCDistanceTypeBinding.class);

        //container.registerComponentImplementation(OGC.EXPRESSIONTYPE, ExpressionTypeBinding.class);
        container.registerComponentImplementation(OGC.ExpressionType, OGCExpressionTypeBinding.class);

        container.registerComponentImplementation(OGC.FeatureIdType, FeatureIdTypeBinding.class);
        container.registerComponentImplementation(OGC.FilterType, FilterTypeBinding.class);
        //container.registerComponentImplementation(OGC.FILTERTYPE,FilterTypeBinding.class);
        container.registerComponentImplementation(OGC.FunctionNamesType,
            FunctionNamesTypeBinding.class);
        container.registerComponentImplementation(OGC.FunctionNameType,
            FunctionNameTypeBinding.class);
        container.registerComponentImplementation(OGC.FunctionsType, FunctionsTypeBinding.class);
        container.registerComponentImplementation(OGC.FunctionType, OGCFunctionTypeBinding.class);
        //container.registerComponentImplementation(OGC.FUNCTIONTYPE,FunctionTypeBinding.class);
        container.registerComponentImplementation(OGC.GeometryOperandsType,
            GeometryOperandsTypeBinding.class);
        container.registerComponentImplementation(OGC.GeometryOperandType,
            GeometryOperandTypeBinding.class);
        container.registerComponentImplementation(OGC.GmlObjectIdType, GmlObjectIdTypeBinding.class);
        container.registerComponentImplementation(OGC.Id_CapabilitiesType,
            Id_CapabilitiesTypeBinding.class);
        container.registerComponentImplementation(OGC.LiteralType, OGCLiteralTypeBinding.class);
        //container.registerComponentImplementation(OGC.LITERALTYPE,LiteralTypeBinding.class);
        //container.registerComponentImplementation(OGC.LOGICOPSTYPE,LogicOpsTypeBinding.class);
        container.registerComponentImplementation(OGC.LowerBoundaryType,
            OGCLowerBoundaryTypeBinding.class);
        container.registerComponentImplementation(OGC.PropertyIsBetweenType,
            OGCPropertyIsBetweenTypeBinding.class);
        //container.registerComponentImplementation(OGC.PROPERTYISBETWEENTYPE,PropertyIsBetweenTypeBinding.class);
        container.registerComponentImplementation(OGC.PropertyIsLikeType,
            PropertyIsLikeTypeBinding.class);
        //container.registerComponentImplementation(OGC.PROPERTYISNULLTYPE,PropertyIsNullTypeBinding.class);
        container.registerComponentImplementation(OGC.PropertyIsNullType,
            OGCPropertyIsNullTypeBinding.class);
        container.registerComponentImplementation(OGC.PropertyNameType,
            OGCPropertyNameTypeBinding.class);
        container.registerComponentImplementation(OGC.Scalar_CapabilitiesType,
            Scalar_CapabilitiesTypeBinding.class);
        container.registerComponentImplementation(OGC.SortByType, SortByTypeBinding.class);
        container.registerComponentImplementation(OGC.SortOrderType, SortOrderTypeBinding.class);
        container.registerComponentImplementation(OGC.SortPropertyType,
            SortPropertyTypeBinding.class);
        container.registerComponentImplementation(OGC.Spatial_CapabilitiesType,
            Spatial_CapabilitiesTypeBinding.class);
        container.registerComponentImplementation(OGC.SpatialOperatorNameType,
            SpatialOperatorNameTypeBinding.class);
        container.registerComponentImplementation(OGC.SpatialOperatorsType,
            SpatialOperatorsTypeBinding.class);
        container.registerComponentImplementation(OGC.SpatialOperatorType,
            SpatialOperatorTypeBinding.class);
        //container.registerComponentImplementation(OGC.SPATIALOPSTYPE,SpatialOpsTypeBinding.class);
        //container.registerComponentImplementation(OGC.UNARYLOGICOPTYPE,
        //    UnaryLogicOpTypeBinding.class);
        container.registerComponentImplementation(OGC.UpperBoundaryType,
            OGCUpperBoundaryTypeBinding.class);

        //Elements
        //container.registerComponentImplementation(OGC._ID,_IdBinding.class);
        container.registerComponentImplementation(OGC.Add, AddBinding.class);
        container.registerComponentImplementation(OGC.And, AndBinding.class);
        //container.registerComponentImplementation(OGC.BBOX,BBOXBinding.class);
        container.registerComponentImplementation(OGC.Beyond, BeyondBinding.class);
        //container.registerComponentImplementation(OGC.COMPARISONOPS,ComparisonOpsBinding.class);
        container.registerComponentImplementation(OGC.Contains, ContainsBinding.class);
        container.registerComponentImplementation(OGC.Crosses, CrossesBinding.class);
        container.registerComponentImplementation(OGC.Disjoint, DisjointBinding.class);
        container.registerComponentImplementation(OGC.Div, DivBinding.class);
        container.registerComponentImplementation(OGC.DWithin, DWithinBinding.class);
        container.registerComponentImplementation(OGC.EID, EIDBinding.class);
        container.registerComponentImplementation(OGC.Equals, EqualsBinding.class);
        //container.registerComponentImplementation(OGC.EXPRESSION, ExpressionBinding.class);
        //container.registerComponentImplementation(OGC.FEATUREID, FeatureIdBinding.class);
        container.registerComponentImplementation(OGC.FID, FIDBinding.class);
        //container.registerComponentImplementation(OGC.FILTER,FilterBinding.class);
        container.registerComponentImplementation(OGC.Filter_Capabilities,
            Filter_CapabilitiesBinding.class);
        //container.registerComponentImplementation(OGC.FUNCTION, FunctionBinding.class);
        //container.registerComponentImplementation(OGC.GMLOBJECTID, GmlObjectIdBinding.class);
        container.registerComponentImplementation(OGC.Intersects, IntersectsBinding.class);
        //container.registerComponentImplementation(OGC.LITERAL,LiteralBinding.class);
        container.registerComponentImplementation(OGC.LogicalOperators,
            LogicalOperatorsBinding.class);
        //container.registerComponentImplementation(OGC.LOGICOPS,LogicOpsBinding.class);
        container.registerComponentImplementation(OGC.Mul, MulBinding.class);
        container.registerComponentImplementation(OGC.Not, NotBinding.class);
        container.registerComponentImplementation(OGC.Or, OrBinding.class);
        container.registerComponentImplementation(OGC.Overlaps, OverlapsBinding.class);
        //container.registerComponentImplementation(OGC.PROPERTYISBETWEEN,PropertyIsBetweenBinding.class);
        container.registerComponentImplementation(OGC.PropertyIsEqualTo,
            PropertyIsEqualToBinding.class);
        container.registerComponentImplementation(OGC.PropertyIsGreaterThan,
            PropertyIsGreaterThanBinding.class);
        container.registerComponentImplementation(OGC.PropertyIsGreaterThanOrEqualTo,
            PropertyIsGreaterThanOrEqualToBinding.class);
        container.registerComponentImplementation(OGC.PropertyIsLessThan,
            PropertyIsLessThanBinding.class);
        container.registerComponentImplementation(OGC.PropertyIsLessThanOrEqualTo,
            PropertyIsLessThanOrEqualToBinding.class);
        //container.registerComponentImplementation(OGC.PROPERTYISLIKE, PropertyIsLikeBinding.class);
        container.registerComponentImplementation(OGC.PropertyIsNotEqualTo,
            PropertyIsNotEqualToBinding.class);
        //container.registerComponentImplementation(OGC.PROPERTYISNULL,PropertyIsNullBinding.class);
        //        container.registerComponentImplementation(OGC.PROPERTYNAME,
        //            PropertyNameBinding.class);
        container.registerComponentImplementation(OGC.SimpleArithmetic,
            SimpleArithmeticBinding.class);
        //container.registerComponentImplementation(OGC.SORTBY,SortByBinding.class);
        //container.registerComponentImplementation(OGC.SPATIALOPS,SpatialOpsBinding.class);
        container.registerComponentImplementation(OGC.Sub, SubBinding.class);
        container.registerComponentImplementation(OGC.Touches, TouchesBinding.class);
        container.registerComponentImplementation(OGC.Within, WithinBinding.class);
    }
}
