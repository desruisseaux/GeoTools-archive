package org.geotools.gml2.bindings;




import org.geotools.xml.*;

import javax.xml.namespace.QName;

import org.geotools.xml.ComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;
import org.picocontainer.MutablePicoContainer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
/**
 * Binding object for the type http://www.opengis.net/gml:PointType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;complexType name="PointType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;         A Point is defined by a single
 *              coordinate tuple.       &lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;extension base="gml:AbstractGeometryType"&gt;
 *              &lt;sequence&gt;
 *                  &lt;choice&gt;
 *                      &lt;element ref="gml:coord"/&gt;
 *                      &lt;element ref="gml:coordinates"/&gt;
 *                  &lt;/choice&gt;
 *              &lt;/sequence&gt;
 *          &lt;/extension&gt;
 *      &lt;/complexContent&gt;
 *  &lt;/complexType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class GMLPointTypeBinding implements ComplexBinding {
	
	GeometryFactory gFactory;
	
	public GMLPointTypeBinding(GeometryFactory gFactory) {
		this.gFactory = gFactory;
	}
	
	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.POINTTYPE;
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
		
		
		if (node.getChild("coord") != null) {
			 CoordinateSequence seq 
			 	= (CoordinateSequence) node.getChild("coord").getValue();
			 return gFactory.createPoint(seq);
		}
		
		if (node.getChild("coordinates") != null) {
			 CoordinateSequence seq 
			 	= (CoordinateSequence) node.getChild("coordinates").getValue();
			 return gFactory.createPoint(seq);
		}
		
		throw new RuntimeException("Could not find a coordinate");
		
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