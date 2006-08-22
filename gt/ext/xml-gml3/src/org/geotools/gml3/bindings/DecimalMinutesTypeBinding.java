package org.geotools.gml3.bindings;


import org.geotools.xml.*;


import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/gml:DecimalMinutesType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;simpleType name="DecimalMinutesType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;Decimal number of arc-minutes in a degree-minute angular value.&lt;/documentation&gt;
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
public class DecimalMinutesTypeBinding extends AbstractSimpleBinding {

	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.DECIMALMINUTESTYPE;
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