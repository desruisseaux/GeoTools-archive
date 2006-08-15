package org.geotools.filter.v1_1;


import org.geotools.filter.FilterFactory;
import org.geotools.xml.*;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.spatial.Crosses;

import javax.xml.namespace.QName;

/**
 * Binding object for the element http://www.opengis.net/ogc:Crosses.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:element name="Crosses" substitutionGroup="ogc:spatialOps" type="ogc:BinarySpatialOpType"/&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class CrossesBinding extends AbstractComplexBinding {

	FilterFactory filterfactory;		
	public CrossesBinding( FilterFactory filterfactory ) {
		this.filterfactory = filterfactory;
	}

	/**
	 * @generated
	 */
	public QName getTarget() {
		return OGC.CROSSES;
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
		return Crosses.class;
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
		return filterfactory.crosses( operands[0], operands[1] );
	}

}