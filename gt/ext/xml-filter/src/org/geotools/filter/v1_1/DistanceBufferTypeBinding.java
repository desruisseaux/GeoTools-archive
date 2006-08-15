package org.geotools.filter.v1_1;


import org.geotools.xml.*;

import org.opengis.filter.FilterFactory;		
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.PropertyName;

import com.vividsolutions.jts.geom.Geometry;

import javax.xml.namespace.QName;

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
public class DistanceBufferTypeBinding extends AbstractComplexBinding {

	FilterFactory filterfactory;		
	public DistanceBufferTypeBinding( FilterFactory filterfactory ) {
		this.filterfactory = filterfactory;
	}

	/**
	 * @generated
	 */
	public QName getTarget() {
		return OGC.DISTANCEBUFFERTYPE;
	}
	
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
		return Expression[].class;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {
		
		PropertyName name = (PropertyName) node.getChildValue( PropertyName.class );
		Geometry geometry = (Geometry) node.getChildValue( Geometry.class );
		Double distance = (Double) node.getChildValue( Double.class );
		
		return new Expression[] {
			name, filterfactory.literal( geometry ), filterfactory.literal( distance )	
		};
	}

}