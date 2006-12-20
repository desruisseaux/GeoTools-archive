package org.geotools.ml.bindings;


import javax.xml.namespace.QName;

import org.geotools.xml.AbstractSimpleBinding;
import org.geotools.xml.InstanceComponent;

/**
 * Strategy object for the type http://mails/refractions/net:mimeTopLevelType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:simpleType name="mimeTopLevelType"&gt;
 *      &lt;xsd:restriction base="xsd:string"&gt;
 *          &lt;xsd:enumeration value="text"/&gt;
 *          &lt;xsd:enumeration value="multipart"/&gt;
 *          &lt;xsd:enumeration value="application"/&gt;
 *          &lt;xsd:enumeration value="message"/&gt;
 *          &lt;xsd:enumeration value="image"/&gt;
 *          &lt;xsd:enumeration value="audio"/&gt;
 *          &lt;xsd:enumeration value="video"/&gt;
 *      &lt;/xsd:restriction&gt;
 *  &lt;/xsd:simpleType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class MLMimeTopLevelTypeBinding extends AbstractSimpleBinding {
	/**
	 * @generated
	 */
	public QName getTarget() {
		return ML.MIMETOPLEVELTYPE;
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
		
		//shouldn't have to do anything special here
		return value;
	}
}

