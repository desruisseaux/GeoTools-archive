package org.geotools.xs.bindings;

import javax.xml.namespace.QName;

import org.geotools.xml.InstanceComponent;
import org.geotools.xml.SimpleBinding;


/**
 * Binding object for the type http://www.w3.org/2001/XMLSchema:NMTOKEN.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xs:simpleType name="NMTOKEN" id="NMTOKEN"&gt;
 *      &lt;xs:annotation&gt;
 *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#NMTOKEN"/&gt;
 *      &lt;/xs:annotation&gt;
 *      &lt;xs:restriction base="xs:token"&gt;
 *          &lt;xs:pattern value="\c+" id="NMTOKEN.pattern"&gt;
 *              &lt;xs:annotation&gt;
 *                  &lt;xs:documentation
 *                      source="http://www.w3.org/TR/REC-xml#NT-Nmtoken"&gt;
 *                      pattern matches production 7 from the XML spec           &lt;/xs:documentation&gt;
 *              &lt;/xs:annotation&gt;
 *          &lt;/xs:pattern&gt;
 *      &lt;/xs:restriction&gt;
 *  &lt;/xs:simpleType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class XSNMTOKENBinding implements SimpleBinding  {

	/**
	 * @generated
	 */	
	public QName getTarget() {
		return XS.NMTOKEN;
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