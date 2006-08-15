package org.geotools.filter.v1_0;


import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.geotools.xml.*;

import org.opengis.filter.FeatureId;
import org.opengis.filter.FilterFactory;
import org.picocontainer.MutablePicoContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/ogc:FeatureIdType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:complexType name="FeatureIdType"&gt;
 *      &lt;xsd:attribute name="fid" type="xsd:anyURI" use="required"/&gt;
 *  &lt;/xsd:complexType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class OGCFeatureIdTypeBinding implements ComplexBinding {	
	private FilterFactory factory;
	public OGCFeatureIdTypeBinding( FilterFactory factory ){
		this.factory = factory;
	}		
	/**
	 * @generated
	 */
	public QName getTarget() {
		return OGC.FEATUREIDTYPE;
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
		return FeatureId.class;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * This will be a good test of xs:anyURI
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
		URI fid = (URI) node.getAttribute("fid").getValue();
		Set fids = new HashSet();
		fids.add( fid.toString() );
		
		return factory.featureId( fids );
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