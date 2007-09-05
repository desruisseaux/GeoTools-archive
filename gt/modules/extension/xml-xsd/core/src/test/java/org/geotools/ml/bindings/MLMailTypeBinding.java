package org.geotools.ml.bindings;

import java.math.BigInteger;
import java.util.List;

import javax.xml.namespace.QName;

import org.geotools.ml.Attachment;
import org.geotools.ml.Envelope;
import org.geotools.ml.Mail;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;

/**
 * Strategy object for the type http://mails/refractions/net:mailType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:complexType name="mailType"&gt;
 *      &lt;xsd:sequence&gt;
 *          &lt;xsd:element name="envelope" type="ml:envelopeType"/&gt;
 *          &lt;xsd:element name="body" type="ml:bodyType"/&gt;
 *          &lt;xsd:element maxOccurs="unbounded" minOccurs="0"
 *              name="attachment" type="ml:attachmentType"/&gt;
 *      &lt;/xsd:sequence&gt;
 *      &lt;xsd:attribute name="id" type="xsd:integer" use="required"/&gt;
 *  &lt;/xsd:complexType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class MLMailTypeBinding extends AbstractComplexBinding {
	
	/**
	 * @generated
	 */
	public QName getTarget() {
		return ML.MAILTYPE;
	}
	
	public Class getType() {
		return Mail.class;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {
		
		Envelope envelope = (Envelope)node.getChildValue("envelope");
		String body = (String) node.getChildValue("body");
		BigInteger id = (BigInteger)node.getAttributeValue("id");
		
		List atts = node.getChildValues("attachment");
		Attachment[] attachments = (Attachment[]) atts
			.toArray(new Attachment[atts.size()]);
		
		return new Mail(id ,body,envelope,attachments);
	}
}
