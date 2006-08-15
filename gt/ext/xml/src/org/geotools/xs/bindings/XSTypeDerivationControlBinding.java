package org.geotools.xs.bindings;

import javax.xml.namespace.QName;

import org.geotools.xml.InstanceComponent;
import org.geotools.xml.SimpleBinding;


/**
 * Binding object for the type http://www.w3.org/2001/XMLSchema:typeDerivationControl.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xs:simpleType name="typeDerivationControl"&gt;
 *      &lt;xs:annotation&gt;
 *          &lt;xs:documentation&gt;    A utility type, not for public use&lt;/xs:documentation&gt;
 *      &lt;/xs:annotation&gt;
 *      &lt;xs:restriction base="xs:derivationControl"&gt;
 *          &lt;xs:enumeration value="extension"/&gt;
 *          &lt;xs:enumeration value="restriction"/&gt;
 *          &lt;xs:enumeration value="list"/&gt;
 *          &lt;xs:enumeration value="union"/&gt;
 *      &lt;/xs:restriction&gt;
 *  &lt;/xs:simpleType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class XSTypeDerivationControlBinding implements SimpleBinding  {

	/**
	 * @generated
	 */	
	public QName getTarget() {
		return XS.TYPEDERIVATIONCONTROL;
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
		//TODO: implement me	
		
		return null;
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