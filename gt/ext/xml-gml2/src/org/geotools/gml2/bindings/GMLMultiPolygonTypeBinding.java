package org.geotools.gml2.bindings;




import org.geotools.xml.*;

import javax.xml.namespace.QName;

import org.geotools.xml.ComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;
import org.picocontainer.MutablePicoContainer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
/**
 * Binding object for the type http://www.opengis.net/gml:MultiPolygonType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;complexType name="MultiPolygonType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;         A MultiPolygon is defined by one or more
 *              Polygons, referenced through          polygonMember
 *              elements.        &lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;restriction base="gml:GeometryCollectionType"&gt;
 *              &lt;sequence&gt;
 *                  &lt;element ref="gml:polygonMember" maxOccurs="unbounded"/&gt;
 *              &lt;/sequence&gt;
 *              &lt;attribute name="gid" type="ID" use="optional"/&gt;
 *              &lt;attribute name="srsName" type="anyURI" use="required"/&gt;
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
public class GMLMultiPolygonTypeBinding implements ComplexBinding {
	GeometryFactory gFactory;
	
	public GMLMultiPolygonTypeBinding(GeometryFactory gFactory) {
		this.gFactory = gFactory;
	}
	
	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.MULTIPOLYGONTYPE;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
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
		
		GeometryCollection gc = (GeometryCollection)value;
		Polygon[] polygons = new Polygon[gc.getNumGeometries()];
		for (int i = 0; i < gc.getNumGeometries(); i++) {
			polygons[i] = (Polygon) gc.getGeometryN(i);
		}
		
		return gFactory.createMultiPolygon(polygons);
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