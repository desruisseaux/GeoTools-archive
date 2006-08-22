package org.geotools.gml3.bindings;


import org.geotools.xml.*;


import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/gml:ArcSecondsType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;simpleType name="ArcSecondsType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;Number of arc-seconds in a degree-minute-second angular value.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;restriction base="decimal"&gt;
 *          &lt;minInclusive value="0.00"/&gt;
 *          &lt;maxExclusive value="60.00"/&gt;
 *      &lt;/restriction&gt;
 *  &lt;/simpleType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class ArcSecondsTypeBinding extends AbstractSimpleBinding {

	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.ARCSECONDSTYPE;
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