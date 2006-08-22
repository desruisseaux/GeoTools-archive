package org.geotools.filter.v1_0;


import java.math.BigDecimal;

import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;

import org.geotools.xml.*;

import org.picocontainer.MutablePicoContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/ogc:BinaryOperatorType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:complexType name="BinaryOperatorType"&gt;
 *      &lt;xsd:complexContent&gt;
 *          &lt;xsd:extension base="ogc:ExpressionType"&gt;
 *              &lt;xsd:sequence&gt;
 *                  &lt;xsd:element maxOccurs="2" minOccurs="2" ref="ogc:expression"/&gt;
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
public class OGCBinaryOperatorTypeBinding implements ComplexBinding {

	private FilterFactory factory;
	
	public OGCBinaryOperatorTypeBinding( FilterFactory factory ){
		this.factory = factory;
	}
	/**
	 * @generated
	 */
	public QName getTarget() {
		return OGC.BINARYOPERATORTYPE;
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
		return Expression.class;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * We check out the instance for the <code>op</code> so we can fail early.
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public void initialize(ElementInstance instance, Node node, MutablePicoContainer context) {

	}
		
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {
		
		//TODO: replace this binding with element bindings
		
		Expression e1 = (Expression) node.getChildValue(0);
		Expression e2 = (Expression) node.getChildValue(1);
		
		String name = instance.getName();
		
		// <xsd:element name="Add" substitutionGroup="ogc:expression" type="ogc:BinaryOperatorType"/>
		if( "Add".equals( name )){
			return factory.add( e1, e2 );
		}		
		// <xsd:element name="Sub" substitutionGroup="ogc:expression" type="ogc:BinaryOperatorType"/>
		if( "Sub".equals( name )){
			return factory.subtract( e1, e2 );
		}		
		// <xsd:element name="Mul" substitutionGroup="ogc:expression" type="ogc:BinaryOperatorType"/>
		if( "Mul".equals( name )){
			return factory.multiply( e1, e2 );
		}		
		// <xsd:element name="Div" substitutionGroup="ogc:expression" type="ogc:BinaryOperatorType"/>
		if( "Div".equals( name )){
			return factory.divide( e1, e2 );
		}
		
		throw new IllegalStateException(
			"BinaryOpperatorType supports Add, Sub, Mul, Div" 
		);			
			
	}
	
	
}