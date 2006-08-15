package org.geotools.filter.v1_0;

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
		//expr.xsd
		container.registerComponentImplementation(OGC.BINARYOPERATORTYPE,OGCBinaryOperatorTypeBinding.class);
		container.registerComponentImplementation(OGC.EXPRESSIONTYPE,OGCExpressionTypeBinding.class);
		container.registerComponentImplementation(OGC.FUNCTIONTYPE,OGCFunctionTypeBinding.class);
		container.registerComponentImplementation(OGC.LITERALTYPE,OGCLiteralTypeBinding.class);
		container.registerComponentImplementation(OGC.PROPERTYNAMETYPE,OGCPropertyNameTypeBinding.class);
		
		//filter.xsd
		container.registerComponentImplementation(OGC.BBOXTYPE,OGCBBOXTypeBinding.class);
		container.registerComponentImplementation(OGC.BINARYCOMPARISONOPTYPE,OGCBinaryComparisonOpTypeBinding.class);
		container.registerComponentImplementation(OGC.BINARYLOGICOPTYPE,OGCBinaryLogicOpTypeBinding.class);
		container.registerComponentImplementation(OGC.BINARYSPATIALOPTYPE,OGCBinarySpatialOpTypeBinding.class);
		//container.registerComponentImplementation(OGC.COMPARISONOPSTYPE,OGCComparisonOpsTypeBinding.class);
		container.registerComponentImplementation(OGC.DISTANCEBUFFERTYPE,OGCDistanceBufferTypeBinding.class);
		container.registerComponentImplementation(OGC.DISTANCETYPE,OGCDistanceTypeBinding.class);
		container.registerComponentImplementation(OGC.FEATUREIDTYPE,OGCFeatureIdTypeBinding.class);
		container.registerComponentImplementation(OGC.FILTERTYPE,OGCFilterTypeBinding.class);
		//container.registerComponentImplementation(OGC.LOGICOPSTYPE,OGCLogicOpsTypeBinding.class);
		container.registerComponentImplementation(OGC.LOWERBOUNDARYTYPE,OGCLowerBoundaryTypeBinding.class);
		container.registerComponentImplementation(OGC.PROPERTYISBETWEENTYPE,OGCPropertyIsBetweenTypeBinding.class);
		container.registerComponentImplementation(OGC.PROPERTYISLIKETYPE,OGCPropertyIsLikeTypeBinding.class);
		container.registerComponentImplementation(OGC.PROPERTYISNULLTYPE,OGCPropertyIsNullTypeBinding.class);
		//container.registerComponentImplementation(OGC.SPATIALOPSTYPE,OGCSpatialOpsTypeBinding.class);
		container.registerComponentImplementation(OGC.UNARYLOGICOPTYPE,OGCUnaryLogicOpTypeBinding.class);
		container.registerComponentImplementation(OGC.UPPERBOUNDARYTYPE,OGCUpperBoundaryTypeBinding.class);

	}

}