package org.geotools.filter.v1_0;

import org.geotools.xml.*;

import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.picocontainer.MutablePicoContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/ogc:ExpressionType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:complexType abstract="true" mixed="true" name="ExpressionType"/&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class OGCExpressionTypeBinding implements ComplexBinding {
	
	FilterFactory filterFactory;
	
	public OGCExpressionTypeBinding(FilterFactory filterFactory) {
		this.filterFactory = filterFactory;
	}
	/**
	 * @generated
	 */
	public QName getTarget() {
		return OGC.EXPRESSIONTYPE;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * This stratagy object is a NOP
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
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public void initialize(ElementInstance instance, Node node, MutablePicoContainer context) {
	
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * This binding simply looks for any text in the node and turns it into 
	 * a literal expression. If differnt behaviour is required by a sub binding
	 * then they should ovveride.
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {
		
		return filterFactory.literal( value );
	}

}