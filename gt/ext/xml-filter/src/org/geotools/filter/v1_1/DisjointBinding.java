package org.geotools.filter.v1_1;


import org.geotools.filter.FilterFactory;
import org.geotools.xml.*;

import org.opengis.filter.expression.Expression;
import org.opengis.filter.spatial.Disjoint;

import javax.xml.namespace.QName;

/**
 * Binding object for the element http://www.opengis.net/ogc:Disjoint.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:element name="Disjoint" substitutionGroup="ogc:spatialOps" type="ogc:BinarySpatialOpType"/&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class DisjointBinding extends AbstractComplexBinding {

	FilterFactory filterfactory;		
	public DisjointBinding( FilterFactory filterfactory ) {
		this.filterfactory = filterfactory;
	}

	/**
	 * @generated
	 */
	public QName getTarget() {
		return OGC.DISJOINT;
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
		return Disjoint.class;
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
		return filterfactory.disjoint( operands[0], operands[1] );
	}

}