package org.geotools.filter.v1_1;


import org.geotools.filter.expression.Expression;
import org.geotools.xml.*;

import org.opengis.filter.FilterFactory;		
import org.opengis.filter.expression.Divide;

import javax.xml.namespace.QName;

/**
 * Binding object for the element http://www.opengis.net/ogc:Div.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:element name="Div" substitutionGroup="ogc:expression" type="ogc:BinaryOperatorType"/&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class DivBinding extends AbstractComplexBinding {

	FilterFactory filterfactory;		
	public DivBinding( FilterFactory filterfactory ) {
		this.filterfactory = filterfactory;
	}

	/**
	 * @generated
	 */
	public QName getTarget() {
		return OGC.DIV;
	}
	
	public int getExecutionMode() {
		return AFTER;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Class getType() {
		return Divide.class;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {
		
		Expression[] operands = (Expression[]) value;
		return filterfactory.divide( operands[0], operands[1] );
	}

}