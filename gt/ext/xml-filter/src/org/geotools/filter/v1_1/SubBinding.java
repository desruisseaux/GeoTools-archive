package org.geotools.filter.v1_1;


import org.geotools.xml.*;

import org.opengis.filter.FilterFactory;		
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Subtract;

import javax.xml.namespace.QName;

/**
 * Binding object for the element http://www.opengis.net/ogc:Sub.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:element name="Sub" substitutionGroup="ogc:expression" type="ogc:BinaryOperatorType"/&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class SubBinding extends AbstractComplexBinding {

	FilterFactory filterfactory;		
	public SubBinding( FilterFactory filterfactory ) {
		this.filterfactory = filterfactory;
	}

	/**
	 * @generated
	 */
	public QName getTarget() {
		return OGC.SUB;
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
		return Subtract.class;
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
		return filterfactory.subtract( operands[0], operands[1] );
	}

}