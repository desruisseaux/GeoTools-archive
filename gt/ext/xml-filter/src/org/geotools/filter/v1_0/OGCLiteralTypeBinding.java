package org.geotools.filter.v1_0;


import org.geotools.xml.*;

import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Literal;
import org.picocontainer.MutablePicoContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/ogc:LiteralType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:complexType name="LiteralType"&gt;
 *      &lt;xsd:complexContent mixed="true"&gt;
 *          &lt;xsd:extension base="ogc:ExpressionType"&gt;
 *              &lt;xsd:sequence&gt;
 *                  &lt;xsd:any minOccurs="0"/&gt;
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
public class OGCLiteralTypeBinding implements ComplexBinding {
	
	private FilterFactory factory;
	
	public OGCLiteralTypeBinding( FilterFactory factory ){
		this.factory = factory;
	}
	/**
	 * @generated
	 */
	public QName getTarget() {
		return OGC.LITERALTYPE;
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
		return Literal.class;
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
	 * Just pass on emeded value as is.
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {		
		
		//number of possibilities here since single child is of type any
		
		//1. has child elements
		if (!node.getChildren().isEmpty()) {
			return factory.literal( node.getChildValue(0) );
		}
			
		//2. no child elements, just return the text if any
		return factory.literal(value);
	}

}