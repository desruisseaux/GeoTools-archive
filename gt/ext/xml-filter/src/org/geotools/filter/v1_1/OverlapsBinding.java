package org.geotools.filter.v1_1;


import org.geotools.filter.FilterFactory;
import org.geotools.xml.*;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.spatial.Overlaps;

import javax.xml.namespace.QName;

/**
 * Binding object for the element http://www.opengis.net/ogc:Overlaps.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:element name="Overlaps" substitutionGroup="ogc:spatialOps" type="ogc:BinarySpatialOpType"/&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class OverlapsBinding extends AbstractComplexBinding {

	FilterFactory filterfactory;		
	public OverlapsBinding( FilterFactory filterfactory ) {
		this.filterfactory = filterfactory;
	}

	/**
	 * @generated
	 */
	public QName getTarget() {
		return OGC.OVERLAPS;
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
		return Overlaps.class;
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
		return filterfactory.overlaps( operands[0], operands[1] );
	}

}