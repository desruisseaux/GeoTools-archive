package org.geotools.filter.v1_1;


import org.geotools.xml.*;

import org.opengis.filter.FilterFactory;		
import org.opengis.filter.expression.Add;
import org.opengis.filter.expression.Expression;

import javax.xml.namespace.QName;

/**
 * Binding object for the element http://www.opengis.net/ogc:Add.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:element name="Add" substitutionGroup="ogc:expression" type="ogc:BinaryOperatorType"/&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class AddBinding extends AbstractComplexBinding {

	FilterFactory filterfactory;		
	public AddBinding( FilterFactory filterfactory ) {
		this.filterfactory = filterfactory;
	}

	/**
	 * @generated
	 */
	public QName getTarget() {
		return OGC.ADD;
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
		return Add.class;
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
		return filterfactory.add( operands[0], operands[1] );
	}

}