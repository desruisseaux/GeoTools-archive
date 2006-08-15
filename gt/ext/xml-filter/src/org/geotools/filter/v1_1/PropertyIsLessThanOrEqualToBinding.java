package org.geotools.filter.v1_1;


import org.geotools.xml.*;

import org.opengis.filter.FilterFactory;		
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.expression.Expression;

import javax.xml.namespace.QName;

/**
 * Binding object for the element http://www.opengis.net/ogc:PropertyIsLessThanOrEqualTo.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:element name="PropertyIsLessThanOrEqualTo"
 *      substitutionGroup="ogc:comparisonOps" type="ogc:BinaryComparisonOpType"/&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class PropertyIsLessThanOrEqualToBinding extends AbstractComplexBinding {

	FilterFactory filterfactory;		
	public PropertyIsLessThanOrEqualToBinding( FilterFactory filterfactory ) {
		this.filterfactory = filterfactory;
	}

	/**
	 * @generated
	 */
	public QName getTarget() {
		return OGC.PROPERTYISLESSTHANOREQUALTO;
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
		return PropertyIsLessThanOrEqualTo.class;
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
		return filterfactory.lessOrEqual( operands[0], operands[1] );
	}

}