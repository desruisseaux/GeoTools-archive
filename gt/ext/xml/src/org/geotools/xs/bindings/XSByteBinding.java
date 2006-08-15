package org.geotools.xs.bindings;

import javax.xml.namespace.QName;

import org.geotools.xml.InstanceComponent;
import org.geotools.xml.SimpleBinding;


/**
 * Binding object for the type http://www.w3.org/2001/XMLSchema:byte.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xs:simpleType name="byte" id="byte"&gt;
 *      &lt;xs:annotation&gt;
 *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#byte"/&gt;
 *      &lt;/xs:annotation&gt;
 *      &lt;xs:restriction base="xs:short"&gt;
 *          &lt;xs:minInclusive value="-128" id="byte.minInclusive"/&gt;
 *          &lt;xs:maxInclusive value="127" id="byte.maxInclusive"/&gt;
 *      &lt;/xs:restriction&gt;
 *  &lt;/xs:simpleType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class XSByteBinding implements SimpleBinding  {

	/**
	 * @generated
	 */	
	public QName getTarget() {
		return XS.BYTE;
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
	 * This binding returns objects of type {@link Byte}
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Class getType() {
		return Byte.class;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * This binding returns objects of type {@link Byte}.
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(InstanceComponent instance, Object value) 
		throws Exception {
		
		String text = (String)value;
		
		if (text.charAt(0) == '+') {
			text = text.substring(1);
		}		
		return new Byte(text);
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public String encode(Object object, String value) {
		Byte b = (Byte)object;
		return b.toString();
	}
	
}