package org.geotools.ml.bindings;


import java.util.List;

import javax.xml.namespace.QName;

import org.geotools.xml.AbstractSimpleBinding;
import org.geotools.xml.InstanceComponent;

/**
 * Strategy object for the type http://mails/refractions/net:bodyType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:simpleType name="bodyType"&gt;
 *      &lt;xsd:restriction base="xsd:string"/&gt;
 *  &lt;/xsd:simpleType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class MLBodyTypeBinding extends AbstractSimpleBinding {
	
	/**
	 * @generated
	 */
	public QName getTarget() {
		return ML.BODYTYPE;
	}
	
	public Class getType() {
		return String.class;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(InstanceComponent instance, Object value) 
		throws Exception {
		
		//just a string
		return value;
	}
}

