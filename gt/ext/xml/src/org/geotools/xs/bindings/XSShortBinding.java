package org.geotools.xs.bindings;

import javax.xml.namespace.QName;

import org.geotools.xml.InstanceComponent;
import org.geotools.xml.SimpleBinding;


/**
 * Binding object for the type http://www.w3.org/2001/XMLSchema:short.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xs:simpleType name="short" id="short"&gt;
 *      &lt;xs:annotation&gt;
 *          &lt;xs:documentation source="http://www.w3.org/TR/xmlschema-2/#short"/&gt;
 *      &lt;/xs:annotation&gt;
 *      &lt;xs:restriction base="xs:int"&gt;
 *          &lt;xs:minInclusive value="-32768" id="short.minInclusive"/&gt;
 *          &lt;xs:maxInclusive value="32767" id="short.maxInclusive"/&gt;
 *      &lt;/xs:restriction&gt;
 *  &lt;/xs:simpleType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class XSShortBinding implements SimpleBinding  {

	/**
	 * @generated
	 */	
	public QName getTarget() {
		return XS.SHORT;
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
	 * This binding returns objects of type {@link Short}.
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Class getType() {
		return Short.class;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * This binding returns objects of type {@link Short}.
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(InstanceComponent instance, Object value) 
		throws Exception {
		String text = instance.getText();		

		if (text.charAt(0) == '+') {
			text = text.substring(1);
		}
		return new Short(text); 		
	}
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public String encode(Object object, String value) {
		Short s = (Short)object;
		return s.toString();
	}
	
}