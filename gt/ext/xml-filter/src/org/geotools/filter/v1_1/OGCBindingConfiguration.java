package org.geotools.filter.v1_1;


import org.geotools.filter.v1_0.OGCBBOXTypeBinding;
import org.geotools.filter.v1_0.OGCDistanceTypeBinding;
import org.geotools.filter.v1_0.OGCFilterTypeBinding;
import org.geotools.filter.v1_0.OGCFunctionTypeBinding;
import org.geotools.filter.v1_0.OGCLiteralTypeBinding;
import org.geotools.filter.v1_0.OGCPropertyIsBetweenTypeBinding;
import org.geotools.filter.v1_0.OGCPropertyIsNullTypeBinding;
import org.geotools.xml.BindingConfiguration;
import org.picocontainer.MutablePicoContainer;

/**
 * Binding configuration for the http://www.opengis.net/ogc schema.
 *
 * @generated
 */
public final class OGCBindingConfiguration
	implements BindingConfiguration {


	/**
	 * @generated modifiable
	 */
	public void configure(MutablePicoContainer container) {
	
		//Types
		//container.registerComponentImplementation(OGC.ABSTRACTIDTYPE,AbstractIdTypeBinding.class);
		container.registerComponentImplementation(OGC.ARITHMETICOPERATORSTYPE,ArithmeticOperatorsTypeBinding.class);
		//container.registerComponentImplementation(OGC.BBOXTYPE,BBOXTypeBinding.class);
		container.registerComponentImplementation(OGC.BBOXTYPE,OGCBBOXTypeBinding.class);
		container.registerComponentImplementation(OGC.BINARYCOMPARISONOPTYPE,BinaryComparisonOpTypeBinding.class);
		container.registerComponentImplementation(OGC.BINARYLOGICOPTYPE,BinaryLogicOpTypeBinding.class);
		container.registerComponentImplementation(OGC.BINARYOPERATORTYPE,BinaryOperatorTypeBinding.class);
		container.registerComponentImplementation(OGC.BINARYSPATIALOPTYPE,BinarySpatialOpTypeBinding.class);
		container.registerComponentImplementation(OGC.COMPARISONOPERATORSTYPE,ComparisonOperatorsTypeBinding.class);
		container.registerComponentImplementation(OGC.COMPARISONOPERATORTYPE,ComparisonOperatorTypeBinding.class);
		//container.registerComponentImplementation(OGC.COMPARISONOPSTYPE,ComparisonOpsTypeBinding.class);
		container.registerComponentImplementation(OGC.DISTANCEBUFFERTYPE,DistanceBufferTypeBinding.class);
		//container.registerComponentImplementation(OGC.DISTANCETYPE,DistanceTypeBinding.class);
		container.registerComponentImplementation(OGC.DISTANCETYPE,OGCDistanceTypeBinding.class);
		container.registerComponentImplementation(OGC.EXPRESSIONTYPE,ExpressionTypeBinding.class);
		container.registerComponentImplementation(OGC.FEATUREIDTYPE,FeatureIdTypeBinding.class);
		container.registerComponentImplementation(OGC.FILTERTYPE,OGCFilterTypeBinding.class);
		//container.registerComponentImplementation(OGC.FILTERTYPE,FilterTypeBinding.class);
		container.registerComponentImplementation(OGC.FUNCTIONNAMESTYPE,FunctionNamesTypeBinding.class);
		container.registerComponentImplementation(OGC.FUNCTIONNAMETYPE,FunctionNameTypeBinding.class);
		container.registerComponentImplementation(OGC.FUNCTIONSTYPE,FunctionsTypeBinding.class);
		container.registerComponentImplementation(OGC.FUNCTIONTYPE,OGCFunctionTypeBinding.class);
		//container.registerComponentImplementation(OGC.FUNCTIONTYPE,FunctionTypeBinding.class);
		container.registerComponentImplementation(OGC.GEOMETRYOPERANDSTYPE,GeometryOperandsTypeBinding.class);
		container.registerComponentImplementation(OGC.GEOMETRYOPERANDTYPE,GeometryOperandTypeBinding.class);
		container.registerComponentImplementation(OGC.GMLOBJECTIDTYPE,GmlObjectIdTypeBinding.class);
		container.registerComponentImplementation(OGC.ID_CAPABILITIESTYPE,Id_CapabilitiesTypeBinding.class);
		container.registerComponentImplementation(OGC.LITERALTYPE,OGCLiteralTypeBinding.class);
		//container.registerComponentImplementation(OGC.LITERALTYPE,LiteralTypeBinding.class);
		//container.registerComponentImplementation(OGC.LOGICOPSTYPE,LogicOpsTypeBinding.class);
		container.registerComponentImplementation(OGC.LOWERBOUNDARYTYPE,LowerBoundaryTypeBinding.class);
		container.registerComponentImplementation(OGC.PROPERTYISBETWEENTYPE,OGCPropertyIsBetweenTypeBinding.class);
		//container.registerComponentImplementation(OGC.PROPERTYISBETWEENTYPE,PropertyIsBetweenTypeBinding.class);
		container.registerComponentImplementation(OGC.PROPERTYISLIKETYPE,PropertyIsLikeTypeBinding.class);
		//container.registerComponentImplementation(OGC.PROPERTYISNULLTYPE,PropertyIsNullTypeBinding.class);
		container.registerComponentImplementation(OGC.PROPERTYISNULLTYPE,OGCPropertyIsNullTypeBinding.class);
		container.registerComponentImplementation(OGC.PROPERTYNAMETYPE,PropertyNameTypeBinding.class);
		container.registerComponentImplementation(OGC.SCALAR_CAPABILITIESTYPE,Scalar_CapabilitiesTypeBinding.class);
		container.registerComponentImplementation(OGC.SORTBYTYPE,SortByTypeBinding.class);
		container.registerComponentImplementation(OGC.SORTORDERTYPE,SortOrderTypeBinding.class);
		container.registerComponentImplementation(OGC.SORTPROPERTYTYPE,SortPropertyTypeBinding.class);
		container.registerComponentImplementation(OGC.SPATIAL_CAPABILITIESTYPE,Spatial_CapabilitiesTypeBinding.class);
		container.registerComponentImplementation(OGC.SPATIALOPERATORNAMETYPE,SpatialOperatorNameTypeBinding.class);
		container.registerComponentImplementation(OGC.SPATIALOPERATORSTYPE,SpatialOperatorsTypeBinding.class);
		container.registerComponentImplementation(OGC.SPATIALOPERATORTYPE,SpatialOperatorTypeBinding.class);
		//container.registerComponentImplementation(OGC.SPATIALOPSTYPE,SpatialOpsTypeBinding.class);
		container.registerComponentImplementation(OGC.UNARYLOGICOPTYPE,UnaryLogicOpTypeBinding.class);
		container.registerComponentImplementation(OGC.UPPERBOUNDARYTYPE,UpperBoundaryTypeBinding.class);

		//Elements
		//container.registerComponentImplementation(OGC._ID,_IdBinding.class);
		container.registerComponentImplementation(OGC.ADD,AddBinding.class);
		container.registerComponentImplementation(OGC.AND,AndBinding.class);
		//container.registerComponentImplementation(OGC.BBOX,BBOXBinding.class);
		container.registerComponentImplementation(OGC.BEYOND,BeyondBinding.class);
		//container.registerComponentImplementation(OGC.COMPARISONOPS,ComparisonOpsBinding.class);
		container.registerComponentImplementation(OGC.CONTAINS,ContainsBinding.class);
		container.registerComponentImplementation(OGC.CROSSES,CrossesBinding.class);
		container.registerComponentImplementation(OGC.DISJOINT,DisjointBinding.class);
		container.registerComponentImplementation(OGC.DIV,DivBinding.class);
		container.registerComponentImplementation(OGC.DWITHIN,DWithinBinding.class);
		container.registerComponentImplementation(OGC.EID,EIDBinding.class);
		container.registerComponentImplementation(OGC.EQUALS,EqualsBinding.class);
		container.registerComponentImplementation(OGC.EXPRESSION,ExpressionBinding.class);
		container.registerComponentImplementation(OGC.FEATUREID,FeatureIdBinding.class);
		container.registerComponentImplementation(OGC.FID,FIDBinding.class);
		//container.registerComponentImplementation(OGC.FILTER,FilterBinding.class);
		container.registerComponentImplementation(OGC.FILTER_CAPABILITIES,Filter_CapabilitiesBinding.class);
		container.registerComponentImplementation(OGC.FUNCTION,FunctionBinding.class);
		container.registerComponentImplementation(OGC.GMLOBJECTID,GmlObjectIdBinding.class);
		container.registerComponentImplementation(OGC.INTERSECTS,IntersectsBinding.class);
		//container.registerComponentImplementation(OGC.LITERAL,LiteralBinding.class);
		container.registerComponentImplementation(OGC.LOGICALOPERATORS,LogicalOperatorsBinding.class);
		//container.registerComponentImplementation(OGC.LOGICOPS,LogicOpsBinding.class);
		container.registerComponentImplementation(OGC.MUL,MulBinding.class);
		container.registerComponentImplementation(OGC.NOT,NotBinding.class);
		container.registerComponentImplementation(OGC.OR,OrBinding.class);
		container.registerComponentImplementation(OGC.OVERLAPS,OverlapsBinding.class);
		//container.registerComponentImplementation(OGC.PROPERTYISBETWEEN,PropertyIsBetweenBinding.class);
		container.registerComponentImplementation(OGC.PROPERTYISEQUALTO,PropertyIsEqualToBinding.class);
		container.registerComponentImplementation(OGC.PROPERTYISGREATERTHAN,PropertyIsGreaterThanBinding.class);
		container.registerComponentImplementation(OGC.PROPERTYISGREATERTHANOREQUALTO,PropertyIsGreaterThanOrEqualToBinding.class);
		container.registerComponentImplementation(OGC.PROPERTYISLESSTHAN,PropertyIsLessThanBinding.class);
		container.registerComponentImplementation(OGC.PROPERTYISLESSTHANOREQUALTO,PropertyIsLessThanOrEqualToBinding.class);
		container.registerComponentImplementation(OGC.PROPERTYISLIKE,PropertyIsLikeBinding.class);
		container.registerComponentImplementation(OGC.PROPERTYISNOTEQUALTO,PropertyIsNotEqualToBinding.class);
		//container.registerComponentImplementation(OGC.PROPERTYISNULL,PropertyIsNullBinding.class);
		container.registerComponentImplementation(OGC.PROPERTYNAME,PropertyNameBinding.class);
		container.registerComponentImplementation(OGC.SIMPLEARITHMETIC,SimpleArithmeticBinding.class);
		//container.registerComponentImplementation(OGC.SORTBY,SortByBinding.class);
		//container.registerComponentImplementation(OGC.SPATIALOPS,SpatialOpsBinding.class);
		container.registerComponentImplementation(OGC.SUB,SubBinding.class);
		container.registerComponentImplementation(OGC.TOUCHES,TouchesBinding.class);
		container.registerComponentImplementation(OGC.WITHIN,WithinBinding.class);

	}

}