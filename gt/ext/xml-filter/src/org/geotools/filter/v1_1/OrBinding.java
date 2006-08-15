package org.geotools.filter.v1_1;


import org.geotools.filter.Filter;
import org.geotools.xml.*;

import org.opengis.filter.FilterFactory;		
import org.opengis.filter.Or;

import javax.xml.namespace.QName;

/**
 * Binding object for the element http://www.opengis.net/ogc:Or.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:element name="Or" substitutionGroup="ogc:logicOps" type="ogc:BinaryLogicOpType"/&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class OrBinding extends AbstractComplexBinding {

	FilterFactory filterfactory;		
	public OrBinding( FilterFactory filterfactory ) {
		this.filterfactory = filterfactory;
	}

	/**
	 * @generated
	 */
	public QName getTarget() {
		return OGC.OR;
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
		return Or.class;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {
		
		Filter[] operands = (Filter[]) value;
		return filterfactory.and( operands[0], operands[1] );
	}

}