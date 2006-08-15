package org.geotools.filter.v1_0;


import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.xml.*;

import org.picocontainer.MutablePicoContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/ogc:UnaryLogicOpType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:complexType name="UnaryLogicOpType"&gt;
 *      &lt;xsd:complexContent&gt;
 *          &lt;xsd:extension base="ogc:LogicOpsType"&gt;
 *              &lt;xsd:sequence&gt;
 *                  &lt;xsd:choice&gt;
 *                      &lt;xsd:element ref="ogc:comparisonOps"/&gt;
 *                      &lt;xsd:element ref="ogc:spatialOps"/&gt;
 *                      &lt;xsd:element ref="ogc:logicOps"/&gt;
 *                  &lt;/xsd:choice&gt;
 *              &lt;/xsd:sequence&gt;
 *          &lt;/xsd:extension&gt;
 *      &lt;/xsd:complexContent&gt;
 *  &lt;/xsd:complexType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class OGCUnaryLogicOpTypeBinding implements ComplexBinding {
	/**
	 * @generated
	 */
	public QName getTarget() {
		return OGC.UNARYLOGICOPTYPE;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public int getExecutionMode() {
		return OVERRIDE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Class getType() {
		return Filter.class;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public void initialize(ElementInstance instance, Node node, MutablePicoContainer context) {
	
	}
	/**
	 * <!-- begin-user-doc -->
	 * Werid lillte API only filter creation that does not requre a factory...
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {
		
		//TODO: replace with element binding
		String name = instance.getName();
// 	<xsd:element name="Not" substitutionGroup="ogc:logicOps" type="ogc:UnaryLogicOpType"/>		
		if( "not".equalsIgnoreCase( name )){
			Filter filter = (Filter) node.getChildValue( 0 );		
			return filter.not();
		}
		else {
			throw new IllegalStateException(name);
		}
	}
	
}