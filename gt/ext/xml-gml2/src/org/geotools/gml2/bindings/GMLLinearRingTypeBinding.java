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
 * Binding object for the type http://www.opengis.net/gml:LinearRingType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;complexType name="LinearRingType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;         A LinearRing is defined by four or more
 *              coordinate tuples, with          linear interpolation
 *              between them; the first and last coordinates          must
 *              be coincident.       &lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;extension base="gml:AbstractGeometryType"&gt;
 *              &lt;sequence&gt;
 *                  &lt;choice&gt;
 *                      &lt;element ref="gml:coord" minOccurs="4" maxOccurs="unbounded"/&gt;
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
public class GMLLinearRingTypeBinding implements ComplexBinding {
	
	CoordinateSequenceFactory csFactory;
	GeometryFactory gFactory;
	
	public GMLLinearRingTypeBinding(
		CoordinateSequenceFactory csFactory, GeometryFactory gFactory
	) {
		this.csFactory = csFactory;
		this.gFactory = gFactory;
	}
	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.LINEARRINGTYPE;
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
		if (!coordinates.isEmpty() && coordinates.size() < 4)
			throw new RuntimeException("LinearRing must have at least 4 coordinates");
		
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
			
			return gFactory.createLinearRing(lineSeq);
		}
		
		if (node.getChild("coordinates") != null) {
			Node cnode = (Node)node.getChild("coordinates");
			CoordinateSequence lineSeq = (CoordinateSequence)cnode.getValue();
			
			return gFactory.createLinearRing(lineSeq);
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