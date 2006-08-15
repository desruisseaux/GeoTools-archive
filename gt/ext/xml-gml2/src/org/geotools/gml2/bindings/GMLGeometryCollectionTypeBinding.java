package org.geotools.gml2.bindings;




import org.geotools.xml.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.geotools.xml.ComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;
import org.picocontainer.MutablePicoContainer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
/**
 * Binding object for the type http://www.opengis.net/gml:GeometryCollectionType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;complexType name="GeometryCollectionType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;         A geometry collection must include one
 *              or more geometries, referenced          through
 *              geometryMember elements. User-defined geometry collections
 *              that accept GML geometry classes as members must
 *              instantiate--or          derive from--this type.       &lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;extension base="gml:AbstractGeometryCollectionBaseType"&gt;
 *              &lt;sequence&gt;
 *                  &lt;element ref="gml:geometryMember" maxOccurs="unbounded"/&gt;
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
public class GMLGeometryCollectionTypeBinding implements ComplexBinding {
	
	GeometryFactory gFactory;
	
	public GMLGeometryCollectionTypeBinding(GeometryFactory gFactory) {
		this.gFactory = gFactory;
	}
	
	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.GEOMETRYCOLLECTIONTYPE;
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
	 * This method returns an object of type @link GeometryCollection
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {
		
		//round up children that are geometries, since this type is often 
		// extended by multi geometries, dont reference members by element name
		List geoms = new ArrayList();
		for (Iterator itr = node.getChildren().iterator(); itr.hasNext();) {
			Node cnode = (Node)itr.next();
			if (cnode.getValue() instanceof Geometry) {
				geoms.add(cnode.getValue());
			}
		}
		
		return gFactory.createGeometryCollection(
			(Geometry[])geoms.toArray(new Geometry[geoms.size()])
		);
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