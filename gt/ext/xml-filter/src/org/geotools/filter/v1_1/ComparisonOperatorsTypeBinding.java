package org.geotools.filter.v1_1;


import org.geotools.xml.*;

import org.opengis.filter.FilterFactory;		

import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/ogc:ComparisonOperatorsType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:complexType name="ComparisonOperatorsType"&gt;
 *      &lt;xsd:sequence maxOccurs="unbounded"&gt;
 *          &lt;xsd:element name="ComparisonOperator" type="ogc:ComparisonOperatorType"/&gt;
 *      &lt;/xsd:sequence&gt;
 *  &lt;/xsd:complexType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class ComparisonOperatorsTypeBinding extends AbstractComplexBinding {

	FilterFactory filterfactory;		
	public ComparisonOperatorsTypeBinding( FilterFactory filterfactory ) {
		this.filterfactory = filterfactory;
	}

	/**
	 * @generated
	 */
	public QName getTarget() {
		return OGC.COMPARISONOPERATORSTYPE;
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