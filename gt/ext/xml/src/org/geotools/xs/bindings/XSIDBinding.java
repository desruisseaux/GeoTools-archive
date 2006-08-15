package org.geotools.xs.bindings;

import javax.xml.namespace.QName;

import org.geotools.xml.InstanceComponent;
import org.geotools.xml.SimpleBinding;


/**
 * Binding object for the type http://www.w3.org/2001/XMLSchema:ID.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xs:simpleType name="ID" id="ID"&gt;
 *      &lt;xs:annotation&gt;
 *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#ID"/&gt;
 *      &lt;/xs:annotation&gt;
 *      &lt;xs:restriction base="xs:NCName"/&gt;
 *  &lt;/xs:simpleType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class XSIDBinding implements SimpleBinding  {

	/**
	 * @generated
	 */
	public QName getTarget() {
		return XS.ID;
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
	 * This method delegates to its parent binding which returns objects of 
	 * type {@link String}.
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Class getType() {
		return String.class;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * This method delegates to its parent binding which returns objects of 
	 * type {@link String}.
	 * <!-- end-user-doc -->
	 *	!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */
	public Object parse(InstanceComponent instance, Object value) 
		throws Exception {

		//Simply return string value.    
		return value;
	}
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public String encode(Object object, String value) {
		//TODO: implement
		return null;
	}
	
}