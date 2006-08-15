package org.geotools.gml2.bindings;



import org.geotools.xml.*;

import java.math.BigDecimal;

import javax.xml.namespace.QName;

import org.geotools.xml.ComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;
import org.picocontainer.MutablePicoContainer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;

/**
 * Binding object for the type http://www.opengis.net/gml:CoordType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;complexType name="CoordType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;         Represents a coordinate tuple in one,
 *              two, or three dimensions.       &lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;sequence&gt;
 *          &lt;element name="X" type="decimal"/&gt;
 *          &lt;element name="Y" type="decimal" minOccurs="0"/&gt;
 *          &lt;element name="Z" type="decimal" minOccurs="0"/&gt;
 *      &lt;/sequence&gt;
 *  &lt;/complexType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class GMLCoordTypeBinding implements ComplexBinding {
	
	CoordinateSequenceFactory csFactory;
	
	public GMLCoordTypeBinding(CoordinateSequenceFactory csFactory) {
		this.csFactory = csFactory;
	}
	
	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.COORDTYPE;
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
	 * Returns a coordinate sequence with a single coordinate in it.
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {
		
		int dimension = 1;
		double x,y,z;
		x = y = z = Double.NaN;
		
		x = ((BigDecimal) node.getChild("X").getValue()).doubleValue();
		
		if (!node.getChildren("Y").isEmpty()) {
			dimension++;
			y = ((BigDecimal) node.getChild("Y").getValue()).doubleValue();
		}
			
		if (!node.getChildren("Z").isEmpty()) {
			dimension++;
			z = ((BigDecimal) node.getChild("Z").getValue()).doubleValue();
		}
			
		//create a coordinate sequence with a single coordinate in it
		CoordinateSequence seq = csFactory.create(1, dimension);
		seq.setOrdinate(0,CoordinateSequence.X, x);
		if (y != Double.NaN) seq.setOrdinate(0,CoordinateSequence.Y,y);
		if (z != Double.NaN) seq.setOrdinate(0,CoordinateSequence.Z,z);
			
		return seq;
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

