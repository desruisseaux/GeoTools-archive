package org.geotools.gml2.bindings;




import org.geotools.xml.*;

import java.util.List;

import javax.xml.namespace.QName;

import org.geotools.xml.ComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;
import org.picocontainer.MutablePicoContainer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.GeometryFactory;
/**
 * Binding object for the type http://www.opengis.net/gml:LineStringType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;complexType name="LineStringType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;         A LineString is defined by two or more
 *              coordinate tuples, with          linear interpolation
 *              between them.        &lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;extension base="gml:AbstractGeometryType"&gt;
 *              &lt;sequence&gt;
 *                  &lt;choice&gt;
 *                      &lt;element ref="gml:coord" minOccurs="2" maxOccurs="unbounded"/&gt;
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
public class GMLLineStringTypeBinding implements ComplexBinding {
	
	CoordinateSequenceFactory csFactory;
	GeometryFactory gFactory;
	
	public GMLLineStringTypeBinding(
		CoordinateSequenceFactory csFactory,GeometryFactory gFactory
	) {
		this.csFactory = csFactory;
		this.gFactory = gFactory;
	}
	
	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.LINESTRINGTYPE;
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
		
		List coordinates = node.getChildren("coord");
		if (coordinates.size() == 1)
			throw new RuntimeException("Linestring must have at least 2 coordinates");
		
		if (!coordinates.isEmpty()) {
			
			Node cnode = (Node)coordinates.get(0);
			CoordinateSequence seq = (CoordinateSequence)cnode.getValue();
			int dimension = GMLUtil.getDimension(seq);
			
			CoordinateSequence lineSeq = csFactory.create(
				coordinates.size(), dimension
			);
			for (int i = 0; i < coordinates.size(); i++) {
				cnode = (Node)coordinates.get(i);
				seq = (CoordinateSequence) cnode.getValue();
				for (int j = 0; j < dimension; j++) {
					lineSeq.setOrdinate(i,j,seq.getOrdinate(0,j));
				}
			}
			
			return gFactory.createLineString(lineSeq);
		}
		
		if (node.getChild("coordinates") != null) {
			Node cnode = (Node)node.getChild("coordinates");
			CoordinateSequence lineSeq = (CoordinateSequence)cnode.getValue();
			
			return gFactory.createLineString(lineSeq);
		}
		
		throw new RuntimeException("Could not find coordinates to build linestring");
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