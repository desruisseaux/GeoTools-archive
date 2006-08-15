package org.geotools.xs.bindings;

import javax.xml.namespace.QName;

import org.geotools.xml.ComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;
import org.picocontainer.MutablePicoContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
/**
 * Binding object for the type http://www.w3.org/2001/XMLSchema:attribute.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xs:complexType name="attribute"&gt;
 *      &lt;xs:complexContent&gt;
 *          &lt;xs:extension base="xs:annotated"&gt;
 *              &lt;xs:sequence&gt;
 *                  &lt;xs:element name="simpleType" minOccurs="0" type="xs:localSimpleType"/&gt;
 *              &lt;/xs:sequence&gt;
 *              &lt;xs:attributeGroup ref="xs:defRef"/&gt;
 *              &lt;xs:attribute name="type" type="xs:QName"/&gt;
 *              &lt;xs:attribute name="use" use="optional" default="optional"&gt;
 *                  &lt;xs:simpleType&gt;
 *                      &lt;xs:restriction base="xs:NMTOKEN"&gt;
 *                          &lt;xs:enumeration value="prohibited"/&gt;
 *                          &lt;xs:enumeration value="optional"/&gt;
 *                          &lt;xs:enumeration value="required"/&gt;
 *                      &lt;/xs:restriction&gt;
 *                  &lt;/xs:simpleType&gt;
 *              &lt;/xs:attribute&gt;
 *              &lt;xs:attribute name="default" type="xs:string"/&gt;
 *              &lt;xs:attribute name="fixed" type="xs:string"/&gt;
 *              &lt;xs:attribute name="form" type="xs:formChoice"/&gt;
 *          &lt;/xs:extension&gt;
 *      &lt;/xs:complexContent&gt;
 *  &lt;/xs:complexType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class XSAttributeBinding implements ComplexBinding  {

	/**
	 * @generated 
	 */	
	public QName getTarget() {
		return XS.ATTRIBUTE;
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
	public void initialize(ElementInstance instance, Node node, MutablePicoContainer context) {
	
	}
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {
		
		//TODO: implement
		return null;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	 public void encode(Object object, Element element, Document document) {
	 	//TODO: implement
	 }
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	 
	 public Object getChild(Object object, QName name) {
	 	//TODO: implement
	 	return null;
	 }
}