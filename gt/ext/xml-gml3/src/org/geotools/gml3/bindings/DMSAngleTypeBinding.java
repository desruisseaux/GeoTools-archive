package org.geotools.gml3.bindings;


import org.geotools.xml.*;


import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/gml:DMSAngleType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;complexType name="DMSAngleType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;Angle value provided in degree-minute-second or degree-minute format.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;sequence&gt;
 *          &lt;element ref="gml:degrees"/&gt;
 *          &lt;choice minOccurs="0"&gt;
 *              &lt;element ref="gml:decimalMinutes"/&gt;
 *              &lt;sequence&gt;
 *                  &lt;element ref="gml:minutes"/&gt;
 *                  &lt;element minOccurs="0" ref="gml:seconds"/&gt;
 *              &lt;/sequence&gt;
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
public class DMSAngleTypeBinding extends AbstractComplexBinding {

	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.DMSANGLETYPE;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Class getType() {
		return null;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {
		
		//TODO: implement
		return null;
	}

}