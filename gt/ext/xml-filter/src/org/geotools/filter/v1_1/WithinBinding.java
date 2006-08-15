package org.geotools.filter.v1_1;


import org.geotools.filter.FilterFactory;
import org.geotools.xml.*;

import org.opengis.filter.expression.Expression;
import org.opengis.filter.spatial.Within;

import javax.xml.namespace.QName;

/**
 * Binding object for the element http://www.opengis.net/ogc:Within.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:element name="Within" substitutionGroup="ogc:spatialOps" type="ogc:BinarySpatialOpType"/&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class WithinBinding extends AbstractComplexBinding {

	FilterFactory filterfactory;		
	public WithinBinding( FilterFactory filterfactory ) {
		this.filterfactory = filterfactory;
	}

	/**
	 * @generated
	 */
	public QName getTarget() {
		return OGC.WITHIN;
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
		return Within.class;
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
		return filterfactory.within( operands[0], operands[1] );
	}

}