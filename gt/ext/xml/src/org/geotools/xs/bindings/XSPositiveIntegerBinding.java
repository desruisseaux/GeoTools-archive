package org.geotools.xs.bindings;

import javax.xml.bind.ValidationException;
import javax.xml.namespace.QName;

import org.geotools.xml.InstanceComponent;
import org.geotools.xml.SimpleBinding;


/**
 * Binding object for the type http://www.w3.org/2001/XMLSchema:positiveInteger.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xs:simpleType name="positiveInteger" id="positiveInteger"&gt;
 *      &lt;xs:annotation&gt;
 *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#positiveInteger"/&gt;
 *      &lt;/xs:annotation&gt;
 *      &lt;xs:restriction base="xs:nonNegativeInteger"&gt;
 *          &lt;xs:minInclusive value="1" id="positiveInteger.minInclusive"/&gt;
 *      &lt;/xs:restriction&gt;
 *  &lt;/xs:simpleType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class XSPositiveIntegerBinding implements SimpleBinding  {

	/**
	 * @generated
	 */	
	public QName getTarget() {
		return XS.POSITIVEINTEGER;
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
	 * This binding returns objects of type {@link Number}.
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Class getType() {
		return Number.class;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * Restriction of integer to positive values.
	 * <p>
	 * Please just treat this as a Number, actual value returned
	 * may be BigInteger or Long or Integer.
	 * </p>
	 * @param instance with text to be parsed
	 * @param value Number from parent XSNonNegativeIntegerStratagy
	 * @return Number positive in range 1 to ...
	 * <!-- begin-user-doc -->
	 */
	public Object parse(InstanceComponent instance, Object value) 
		throws Exception {

		Number number = (Number) value;
		
		if( number.intValue() == 0 ){
			throw new ValidationException("positiveInteger value '"+number+"' must be positive.");
		}
		return number;
	}
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public String encode(Object object, String value) throws Exception {
		Number number = (Number) object;
		
		if( number.intValue() == 0 ){
			throw new ValidationException("positiveInteger value '"+number+"' must be positive.");
		}
		
		return value;
	}
	
}