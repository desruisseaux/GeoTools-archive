package org.geotools.gml2.bindings;




import org.geotools.xml.*;

import java.util.StringTokenizer;

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
 * Binding object for the type http://www.opengis.net/gml:CoordinatesType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;complexType name="CoordinatesType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;         Coordinates can be included in a single
 *              string, but there is no          facility for validating
 *              string content. The value of the &apos;cs&apos; attribute
 *              is the separator for coordinate values, and the value of the
 *              &apos;ts&apos;          attribute gives the tuple separator
 *              (a single space by default); the          default values may
 *              be changed to reflect local usage.       &lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;simpleContent&gt;
 *          &lt;extension base="string"&gt;
 *              &lt;attribute name="decimal" type="string" use="optional" default="."/&gt;
 *              &lt;attribute name="cs" type="string" use="optional" default=","/&gt;
 *              &lt;attribute name="ts" type="string" use="optional" default=" "/&gt;
 *          &lt;/extension&gt;
 *      &lt;/simpleContent&gt;
 *  &lt;/complexType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class GMLCoordinatesTypeBinding implements ComplexBinding {
	
	CoordinateSequenceFactory csFactory;
	
	public GMLCoordinatesTypeBinding(CoordinateSequenceFactory csFactory) {
		this.csFactory = csFactory;
	}
	
	
	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.COORDINATESTYPE;
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
	 * Returns an object of type {@see com.vividsolutions.jts.geom.CoordinateSequence}
	 * TODO: this method should do more validation of the string
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {
		
		//get the coordinate and tuple seperators
		String decimal = ".";
		String cs = ",";
		String ts = " ";
		
		if (node.getAttribute("decimal") != null) {
			decimal = (String) node.getAttribute("decimal").getValue();
		}
		if (node.getAttribute("cs") != null) {
			cs = (String)node.getAttribute("cs").getValue();
		}
		if (node.getAttribute("ts") != null) {
			ts = (String)node.getAttribute("ts").getValue();
		}
		
		//do the parsing
		String text = instance.getText();
		
		//first tokenize by tuple seperators
		StringTokenizer tuples = new StringTokenizer(text,ts);
		CoordinateSequence seq = null;
		int i = 0;
		int ncoords = tuples.countTokens();	//number of coordinates
		while(tuples.hasMoreTokens()) {
			String tuple = tuples.nextToken();
			
			//next tokenize by coordinate seperator
			String[] oords = tuple.split(cs);
			
			//next tokenize by decimal
			String x = null,y = null,z = null;
			
			//must be at least 1D			
			x = ".".equals(decimal) ? oords[0] 
	                                : oords[0].replaceAll(decimal,".");
			//check for 2 and 3 D
			if (oords.length > 1) {
				y = ".".equals(decimal) ? oords[1] 
		                                : oords[1].replaceAll(decimal,".");
			}
			if (oords.length > 2) {
				z = ".".equals(decimal) ? oords[2] 
			                            : oords[2].replaceAll(decimal,".");
			}
			
			if (seq == null) {
				seq = csFactory.create(ncoords, oords.length); 
			}
			seq.setOrdinate(i, CoordinateSequence.X, Double.parseDouble(x));
			if (y != null) 
				seq.setOrdinate(i, CoordinateSequence.Y,Double.parseDouble(y));
			if (z != null) 
				seq.setOrdinate(i, CoordinateSequence.Z,Double.parseDouble(z));
			i++;
		}
		
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