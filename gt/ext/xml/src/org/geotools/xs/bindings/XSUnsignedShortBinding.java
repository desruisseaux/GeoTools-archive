package org.geotools.xs.bindings;

import javax.xml.namespace.QName;

import org.geotools.xml.InstanceComponent;
import org.geotools.xml.SimpleBinding;


/**
 * Binding object for the type http://www.w3.org/2001/XMLSchema:unsignedShort.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xs:simpleType name="unsignedShort" id="unsignedShort"&gt;
 *      &lt;xs:annotation&gt;
 *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#unsignedShort"/&gt;
 *      &lt;/xs:annotation&gt;
 *      &lt;xs:restriction base="xs:unsignedInt"&gt;
 *          &lt;xs:maxInclusive value="65535" id="unsignedShort.maxInclusive"/&gt;
 *      &lt;/xs:restriction&gt;
 *  &lt;/xs:simpleType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class XSUnsignedShortBinding implements SimpleBinding  {

	/**
	 * @generated
	 */	
	public QName getTarget() {
		return XS.UNSIGNEDSHORT;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public int getExecutionMode() {
		return AFTER;
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
	 * <!-- begin-user-doc -->
	 * This binding returns objects of type {@link Integer}.
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(InstanceComponent instance, Object value) 
		throws Exception {
		return new Integer((String) value);
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