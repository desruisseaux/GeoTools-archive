package org.geotools.gml3.bindings;


import java.util.List;

import org.geotools.xml.*;

import com.vividsolutions.jts.geom.Point;


import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/gml:PointArrayPropertyType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;complexType name="PointArrayPropertyType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;A container for an array of points. The elements are always contained in the array property, referencing geometry 
 *  			elements or arrays of geometry elements is not supported.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;sequence&gt;
 *          &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:Point"/&gt;
 *      &lt;/sequence&gt;
 *  &lt;/complexType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class PointArrayPropertyTypeBinding extends AbstractComplexBinding {

	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.POINTARRAYPROPERTYTYPE;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Class getType() {
		return Point[].class;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {
		
		List points = node.getChildValues( Point.class );
		return (Point[])  points.toArray( new Point[ points.size() ] );
	}

}