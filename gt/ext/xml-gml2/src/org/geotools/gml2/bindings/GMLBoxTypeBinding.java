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
import com.vividsolutions.jts.geom.Envelope;
/**
 * Binding object for the type http://www.opengis.net/gml:BoxType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;complexType name="BoxType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;         The Box structure defines an extent
 *              using a pair of coordinate tuples.       &lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;extension base="gml:AbstractGeometryType"&gt;
 *              &lt;sequence&gt;
 *                  &lt;choice&gt;
 *                      &lt;element ref="gml:coord" minOccurs="2" maxOccurs="2"/&gt;
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
public class GMLBoxTypeBinding implements ComplexBinding {
	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.BOXTYPE;
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
		return Envelope.class;
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
	 * This method returns an object of type 
	 * @link com.vividsolutions.jts.geom.Envelope.
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {
		
		Envelope e = null;
		
		List coordinates = node.getChildren("coord");
		if (!coordinates.isEmpty() && coordinates.size() == 2) {
			Node n1 = (Node) coordinates.get(0);
			Node n2 = (Node) coordinates.get(1);
			CoordinateSequence c1 = (CoordinateSequence)n1.getValue();
			CoordinateSequence c2 = (CoordinateSequence)n2.getValue();
			
			return new Envelope(
				c1.getOrdinate(0,CoordinateSequence.X),
				c2.getOrdinate(0,CoordinateSequence.X),
				c1.getOrdinate(0,CoordinateSequence.Y),
				c2.getOrdinate(0,CoordinateSequence.Y)
			);
		}
		if (!coordinates.isEmpty()) {
			throw new RuntimeException("Envelope can have only two coordinates");
		}
		
		if (node.getChild("coordinates") != null) {
			CoordinateSequence cs = 
				(CoordinateSequence)node.getChild("coordinates").getValue();
			if (cs.size() != 2)
				throw new RuntimeException("Envelope can have only two coordinates");
			
			return new Envelope(
				cs.getOrdinate(0,CoordinateSequence.X),cs.getOrdinate(1,CoordinateSequence.X),
				cs.getOrdinate(0,CoordinateSequence.Y),cs.getOrdinate(1,CoordinateSequence.Y)
			);
		}
		
		throw new RuntimeException("Could not find coordinates for envelope");
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