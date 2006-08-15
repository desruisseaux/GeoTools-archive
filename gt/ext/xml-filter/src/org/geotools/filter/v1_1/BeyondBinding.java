package org.geotools.filter.v1_1;


import org.geotools.filter.FilterFactory;
import org.geotools.filter.Filters;
import org.geotools.xml.*;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.spatial.Beyond;

import javax.xml.namespace.QName;

/**
 * Binding object for the element http://www.opengis.net/ogc:Beyond.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:element name="Beyond" substitutionGroup="ogc:spatialOps" type="ogc:DistanceBufferType"/&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class BeyondBinding extends AbstractComplexBinding {

	FilterFactory filterfactory;		
	public BeyondBinding( FilterFactory filterfactory ) {
		this.filterfactory = filterfactory;
	}

	/**
	 * @generated
	 */
	public QName getTarget() {
		return OGC.BEYOND;
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
		return Beyond.class;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {
		
		//TODO: units
		Expression[] operands = (Expression[]) value;
		double distance = Filters.asDouble( operands[2] );
		return filterfactory.beyond( operands[0], operands[1], distance, null );
	}

}