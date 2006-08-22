package org.geotools.gml3.bindings;


import org.geotools.xml.*;


import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/gml:TimeType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;complexType name="TimeType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;Value of a time or temporal quantity, with its units. Uses the MeasureType with the restriction that the unit of measure referenced by uom must be suitable for a time value, such as seconds or weeks.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;simpleContent&gt;
 *          &lt;restriction base="gml:MeasureType"/&gt;
 *      &lt;/simpleContent&gt;
 *  &lt;/complexType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class TimeTypeBinding extends AbstractComplexBinding {

	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.TIMETYPE;
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