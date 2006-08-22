package org.geotools.gml3.bindings;


import org.geotools.xml.*;


import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/gml:ArcMinutesType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;simpleType name="ArcMinutesType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;Integer number of arc-minutes in a degree-minute-second angular value.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;restriction base="nonNegativeInteger"&gt;
 *          &lt;maxInclusive value="59"/&gt;
 *      &lt;/restriction&gt;
 *  &lt;/simpleType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class ArcMinutesTypeBinding extends AbstractSimpleBinding {

	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.ARCMINUTESTYPE;
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