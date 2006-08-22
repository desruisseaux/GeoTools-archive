package org.geotools.gml3.bindings;


import org.geotools.xml.*;


import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/gml:DegreeValueType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;simpleType name="DegreeValueType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;Integer number of degrees in a degree-minute-second or degree-minute angular value, without indication of direction.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;restriction base="nonNegativeInteger"&gt;
 *          &lt;maxInclusive value="359"/&gt;
 *      &lt;/restriction&gt;
 *  &lt;/simpleType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class DegreeValueTypeBinding extends AbstractSimpleBinding {

	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.DEGREEVALUETYPE;
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
	public Object parse(InstanceComponent instance, Object value) 
		throws Exception {
		
		//TODO: implement
		return null;
	}

}