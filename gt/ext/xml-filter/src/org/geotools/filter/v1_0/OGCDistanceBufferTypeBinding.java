package org.geotools.filter.v1_0;


import javax.xml.namespace.QName;

import org.geotools.filter.FilterFactory;
import org.geotools.filter.expression.LiteralExpression;
import org.geotools.xml.ComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.picocontainer.MutablePicoContainer;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Binding object for the type http://www.opengis.net/ogc:DistanceBufferType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:complexType name="DistanceBufferType"&gt;
 *      &lt;xsd:complexContent&gt;
 *          &lt;xsd:extension base="ogc:SpatialOpsType"&gt;
 *              &lt;xsd:sequence&gt;
 *                  &lt;xsd:element ref="ogc:PropertyName"/&gt;
 *                  &lt;xsd:element ref="gml:_Geometry"/&gt;
 *                  &lt;xsd:element name="Distance" type="ogc:DistanceType"/&gt;
 *              &lt;/xsd:sequence&gt;
 *          &lt;/xsd:extension&gt;
 *      &lt;/xsd:complexContent&gt;
 *  &lt;/xsd:complexType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class OGCDistanceBufferTypeBinding implements ComplexBinding {
	private FilterFactory factory;	
	public OGCDistanceBufferTypeBinding( FilterFactory factory ){
		this.factory = factory;
	}	
	/**
	 * @generated
	 */
	public QName getTarget() {
		return OGC.DISTANCEBUFFERTYPE;
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
		
		//TODO: replace with element bindings
		Number distance = (Number) node.getChildValue( Number.class );
		
		PropertyName propertyName = (PropertyName) node.getChildValue( PropertyName.class );
		Literal geometry = factory.literal( node.getChildValue( Geometry.class ) );
		
		String name = instance.getName(); 
		//<xsd:element name="DWithin" substitutionGroup="ogc:spatialOps" type="ogc:DistanceBufferType"/>
		if( "DWithin".equals(name)){
			//TOOD: units
			return factory.dwithin( propertyName, geometry, distance.doubleValue(), null );
		}		
		//<xsd:element name="Beyond" substitutionGroup="ogc:spatialOps" type="ogc:DistanceBufferType"/>
		else if( "Beyond".equals(name)){
			//TODO: units
			return factory.beyond( propertyName, geometry, distance.doubleValue(), null );
		}
		else {
			throw new IllegalArgumentException("Unknown - " + name );			
		}	
		
	}
	
}