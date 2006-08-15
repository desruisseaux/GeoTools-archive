package org.geotools.filter.v1_1;


import org.geotools.filter.expression.Expression;
import org.geotools.xml.*;

import org.opengis.filter.FilterFactory;		
import org.opengis.filter.expression.Multiply;

import javax.xml.namespace.QName;

/**
 * Binding object for the element http://www.opengis.net/ogc:Mul.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:element name="Mul" substitutionGroup="ogc:expression" type="ogc:BinaryOperatorType"/&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class MulBinding extends AbstractComplexBinding {

	FilterFactory filterfactory;		
	public MulBinding( FilterFactory filterfactory ) {
		this.filterfactory = filterfactory;
	}

	/**
	 * @generated
	 */
	public QName getTarget() {
		return OGC.MUL;
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
		return Multiply.class;
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
		return filterfactory.multiply( operands[0], operands[1] );
	}

}