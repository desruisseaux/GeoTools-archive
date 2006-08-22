package org.geotools.gml3.bindings;


import org.geotools.xml.*;

import com.vividsolutions.jts.geom.LinearRing;


import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/gml:AbstractRingPropertyType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;complexType name="AbstractRingPropertyType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;Encapsulates a ring to represent the surface boundary property of a surface.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;sequence&gt;
 *          &lt;element ref="gml:_Ring"/&gt;
 *      &lt;/sequence&gt;
 *  &lt;/complexType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class AbstractRingPropertyTypeBinding extends AbstractComplexBinding {

	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.ABSTRACTRINGPROPERTYTYPE;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Class getType() {
		return LinearRing.class;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {
		
		return node.getChildValue( LinearRing.class );
	}

}