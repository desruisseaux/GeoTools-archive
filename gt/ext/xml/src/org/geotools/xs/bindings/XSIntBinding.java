package org.geotools.xs.bindings;

import javax.xml.namespace.QName;

import org.geotools.xml.InstanceComponent;
import org.geotools.xml.SimpleBinding;


/**
 * Binding object for the type http://www.w3.org/2001/XMLSchema:int.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xs:simpleType name="int" id="int"&gt;
 *      &lt;xs:annotation&gt;
 *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#int"/&gt;
 *      &lt;/xs:annotation&gt;
 *      &lt;xs:restriction base="xs:long"&gt;
 *          &lt;xs:minInclusive value="-2147483648" id="int.minInclusive"/&gt;
 *          &lt;xs:maxInclusive value="2147483647" id="int.maxInclusive"/&gt;
 *      &lt;/xs:restriction&gt;
 *  &lt;/xs:simpleType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class XSIntBinding implements SimpleBinding  {

	/**
	 * @generated
	 */	
	public QName getTarget() {
		return XS.INT;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public int getExecutionMode() {
		return OVERRIDE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * This binding returns objects of type {@link Integer}.
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Class getType() {
		return Integer.class;
	}
	
	/**
	 * 
	 * <!-- begin-user-doc -->
	 * This binding returns objects of type {@link Integer}. This binding is an 
	 * override of the parent.
	 * <!-- end-user-doc -->
	 */
	public Object parse(InstanceComponent instance, Object value) 
		throws Exception {
		
		String text = (String)value;		

		if (text.charAt(0) == '+') {
			text = text.substring(1);
		}		
		return new Integer(text);
	}
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public String encode(Object object, String value) {
		Integer integer = (Integer)object;
		return integer.toString();
	}
	
}