package org.geotools.xs.bindings;

import javax.xml.namespace.QName;

import org.geotools.xml.ComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;
import org.picocontainer.MutablePicoContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
/**
 * Binding object for the type http://www.w3.org/2001/XMLSchema:numFacet.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xs:complexType name="numFacet"&gt;
 *      &lt;xs:complexContent&gt;
 *          &lt;xs:restriction base="xs:facet"&gt;
 *              &lt;xs:sequence&gt;
 *                  &lt;xs:element ref="xs:annotation" minOccurs="0"/&gt;
 *              &lt;/xs:sequence&gt;
 *              &lt;xs:attribute name="value" type="xs:nonNegativeInteger" use="required"/&gt;
 *              &lt;xs:anyAttribute namespace="##other" processContents="lax"/&gt;
 *          &lt;/xs:restriction&gt;
 *      &lt;/xs:complexContent&gt;
 *  &lt;/xs:complexType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class XSNumFacetBinding implements ComplexBinding  {

	/**
	 * @generated modifiable
	 */	
	public QName getTarget() {
		return XS.NUMFACET;
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