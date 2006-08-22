package org.geotools.gml3.bindings;


import java.util.List;

import org.geotools.xml.*;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;


import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/gml:PolygonType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;complexType name="PolygonType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;A Polygon is a special surface that is defined by a single surface patch. The boundary of this patch is coplanar and the polygon uses planar interpolation in its interior. It is backwards compatible with the Polygon of GML 2, GM_Polygon of ISO 19107 is implemented by PolygonPatch.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;extension base="gml:AbstractSurfaceType"&gt;
 *              &lt;sequence&gt;
 *                  &lt;element minOccurs="0" ref="gml:exterior"/&gt;
 *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:interior"/&gt;
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
public class PolygonTypeBinding extends AbstractComplexBinding {

	GeometryFactory gFactory;
	
	public PolygonTypeBinding( GeometryFactory gFactory ) {
		this.gFactory = gFactory;
	}
	
	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.POLYGONTYPE;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Class getType() {
		return Polygon.class;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {
		
		//TODO: schema allows no exterior ring, but what the heck is that all about ?
		LinearRing exterior = (LinearRing) node.getChildValue( "exterior" );
		LinearRing[] interior = null;
		
		if ( node.hasChild( "interior" ) ) {
			List list = node.getChildValues( "interior" );
			interior = (LinearRing[]) list.toArray( new LinearRing[ list.size() ] );
		}
		
		return gFactory.createPolygon( exterior, interior );
	}

}