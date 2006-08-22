package org.geotools.gml3.bindings;


import java.util.List;

import org.geotools.xml.*;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;


import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/gml:MultiPolygonType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;complexType name="MultiPolygonType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;A MultiPolygon is defined by one or more Polygons, referenced through polygonMember elements. Deprecated with GML version 3.0. Use MultiSurfaceType instead.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;extension base="gml:AbstractGeometricAggregateType"&gt;
 *              &lt;sequence&gt;
 *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:polygonMember"/&gt;
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
public class MultiPolygonTypeBinding extends AbstractComplexBinding {

	GeometryFactory gFactory;
	
	public MultiPolygonTypeBinding( GeometryFactory gFactory ) {
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
	 * @generated modifiable
	 */	
	public Class getType() {
		return MultiPolygon.class;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {
		
		List polys = node.getChildValues( Polygon.class );
		return gFactory.createMultiPolygon( 
			(Polygon[]) polys.toArray( new Polygon[ polys.size() ] )
		);
		
	}

}