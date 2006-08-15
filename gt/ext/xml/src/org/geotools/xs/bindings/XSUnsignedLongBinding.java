package org.geotools.xs.bindings;

import java.math.BigDecimal;

import javax.xml.namespace.QName;

import org.geotools.xml.InstanceComponent;
import org.geotools.xml.SimpleBinding;


/**
 * Binding object for the type http://www.w3.org/2001/XMLSchema:unsignedLong.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xs:simpleType name="unsignedLong" id="unsignedLong"&gt;
 *      &lt;xs:annotation&gt;
 *          &lt;xs:appinfo&gt;
 *              &lt;hfp:hasProperty name="bounded" value="true"/&gt;
 *              &lt;hfp:hasProperty name="cardinality" value="finite"/&gt;
 *          &lt;/xs:appinfo&gt;
 *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#unsignedLong"/&gt;
 *      &lt;/xs:annotation&gt;
 *      &lt;xs:restriction base="xs:nonNegativeInteger"&gt;
 *          &lt;xs:maxInclusive value="18446744073709551615" id="unsignedLong.maxInclusive"/&gt;
 *      &lt;/xs:restriction&gt;
 *  &lt;/xs:simpleType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class XSUnsignedLongBinding implements SimpleBinding  {


	/**
	 * @generated
	 */	
	public QName getTarget() {
		return XS.UNSIGNEDLONG;
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
	 * This binding returns objects of type {@link BigDecimal}.
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Class getType() {
		return BigDecimal.class;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * This binding returns objects of type {@link BigDecimal}.
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */
	public Object parse(InstanceComponent instance, Object value) 
		throws Exception {
		
		return new BigDecimal((String)value);
	}
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public String encode(Object object, String value) {
		BigDecimal decimal = (BigDecimal)object;
		return decimal.toString();
	}
	
}