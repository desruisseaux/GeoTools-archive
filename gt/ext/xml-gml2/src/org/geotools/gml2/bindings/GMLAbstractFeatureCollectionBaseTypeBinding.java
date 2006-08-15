package org.geotools.gml2.bindings;


import org.geotools.xml.*;

import javax.xml.namespace.QName;

import org.geotools.feature.FeatureCollections;
import org.geotools.xml.ComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;
import org.picocontainer.MutablePicoContainer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
/**
 * Binding object for the type http://www.opengis.net/gml:AbstractFeatureCollectionBaseType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;complexType name="AbstractFeatureCollectionBaseType" abstract="true"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;         This abstract base type just makes the
 *              boundedBy element mandatory          for a feature
 *              collection.       &lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;restriction base="gml:AbstractFeatureType"&gt;
 *              &lt;sequence&gt;
 *                  &lt;element ref="gml:description" minOccurs="0"/&gt;
 *                  &lt;element ref="gml:name" minOccurs="0"/&gt;
 *                  &lt;element ref="gml:boundedBy"/&gt;
 *              &lt;/sequence&gt;
 *              &lt;attribute name="fid" type="ID" use="optional"/&gt;
 *          &lt;/restriction&gt;
 *      &lt;/complexContent&gt;
 *  &lt;/complexType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class GMLAbstractFeatureCollectionBaseTypeBinding implements ComplexBinding {
	
	FeatureCollections fcFactory;
	
	public GMLAbstractFeatureCollectionBaseTypeBinding(
		FeatureCollections fcFactory
	) {
		this.fcFactory = fcFactory;
	}
	
	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.ABSTRACTFEATURECOLLECTIONBASETYPE;
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
		
		//TODO: the geotools feature api doesn't allow for use to supply the 
		// "correct" subclass without hacking, so for now we just create a 
		// default feature collection.
		return fcFactory.newCollection();
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