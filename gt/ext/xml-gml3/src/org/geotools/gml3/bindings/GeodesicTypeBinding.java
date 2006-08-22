package org.geotools.gml3.bindings;


import org.geotools.xml.*;


import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/gml:GeodesicType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;complexType name="GeodesicType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;A Geodesic consists of two distinct
 *     positions joined by a geodesic curve. The control points of
 *     a Geodesic shall lie on the geodesic between its start
 *     point and end points. Between these two points, a geodesic
 *     curve defined from ellipsoid or geoid model used by the
 *     co-ordinate reference systems may be used to interpolate
 *     other positions. Any other point in the controlPoint array
 *     must fall on this geodesic.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;extension base="gml:GeodesicStringType"/&gt;
 *      &lt;/complexContent&gt;
 *  &lt;/complexType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class GeodesicTypeBinding extends AbstractComplexBinding {

	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.GEODESICTYPE;
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
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {
		
		//TODO: implement
		return null;
	}

}