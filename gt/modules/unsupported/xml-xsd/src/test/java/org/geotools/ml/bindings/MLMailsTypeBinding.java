package org.geotools.ml.bindings;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;
/**
 * Strategy object for the type http://mails/refractions/net:mailsType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:complexType name="mailsType"&gt;
 *      &lt;xsd:sequence maxOccurs="unbounded" minOccurs="0"&gt;
 *          &lt;xsd:element name="mail" type="ml:mailType"/&gt;
 *      &lt;/xsd:sequence&gt;
 *  &lt;/xsd:complexType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class MLMailsTypeBinding extends AbstractComplexBinding {
	
	/**
	 * @generated
	 */
	public QName getTarget() {
		return ML.MAILSTYPE;
	}
	
	public Class getType() {
		return List.class;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {
		
		ArrayList list = new ArrayList();
		List children = node.getChildren();
		for (int  i = 0; i <  children.size(); i++) {
			list.add(((Node)children.get(i)).getValue());
		}
		
		return list;
	}
}

