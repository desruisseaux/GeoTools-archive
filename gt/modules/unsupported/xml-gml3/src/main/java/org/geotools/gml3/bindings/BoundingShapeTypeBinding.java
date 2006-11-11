package org.geotools.gml3.bindings;


import org.geotools.xml.*;

import com.vividsolutions.jts.geom.Envelope;


import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/gml:BoundingShapeType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;complexType name="BoundingShapeType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;Bounding shape.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;sequence&gt;
 *          &lt;choice&gt;
 *              &lt;element ref="gml:Envelope"/&gt;
 *              &lt;element ref="gml:Null"/&gt;
 *          &lt;/choice&gt;
 *      &lt;/sequence&gt;
 *  &lt;/complexType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class BoundingShapeTypeBinding extends AbstractComplexBinding {

	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.BoundingShapeType;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Class getType() {
		return Envelope.class;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {
		
		Envelope envelope = (Envelope) node.getChildValue( Envelope.class );
		if ( envelope == null ) {
			envelope = new Envelope();
			envelope.setToNull();
		}
		
		return envelope;
	}

}