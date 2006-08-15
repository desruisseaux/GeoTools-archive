package org.geotools.filter.v1_1;


import org.geotools.xml.*;

import org.opengis.filter.FilterFactory;		

import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/ogc:Scalar_CapabilitiesType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:complexType name="Scalar_CapabilitiesType"&gt;
 *      &lt;xsd:sequence&gt;
 *          &lt;xsd:element maxOccurs="1" minOccurs="0" ref="ogc:LogicalOperators"/&gt;
 *          &lt;xsd:element maxOccurs="1" minOccurs="0"
 *              name="ComparisonOperators" type="ogc:ComparisonOperatorsType"/&gt;
 *          &lt;xsd:element maxOccurs="1" minOccurs="0"
 *              name="ArithmeticOperators" type="ogc:ArithmeticOperatorsType"/&gt;
 *      &lt;/xsd:sequence&gt;
 *  &lt;/xsd:complexType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class Scalar_CapabilitiesTypeBinding extends AbstractComplexBinding {

	FilterFactory filterfactory;		
	public Scalar_CapabilitiesTypeBinding( FilterFactory filterfactory ) {
		this.filterfactory = filterfactory;
	}

	/**
	 * @generated
	 */
	public QName getTarget() {
		return OGC.SCALAR_CAPABILITIESTYPE;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Class getType() {
		return null;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {
		
		//TODO: implement
		return null;
	}

}