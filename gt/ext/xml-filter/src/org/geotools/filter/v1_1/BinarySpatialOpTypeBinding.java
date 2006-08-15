package org.geotools.filter.v1_1;


import org.geotools.xml.*;

import org.opengis.filter.FilterFactory;		
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.PropertyName;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/ogc:BinarySpatialOpType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;xsd:complexType name="BinarySpatialOpType"&gt;
 *      &lt;xsd:complexContent&gt;
 *          &lt;xsd:extension base="ogc:SpatialOpsType"&gt;
 *              &lt;xsd:sequence&gt;
 *                  &lt;xsd:element ref="ogc:PropertyName"/&gt;
 *                  &lt;xsd:choice&gt;
 *                      &lt;xsd:element ref="gml:_Geometry"/&gt;
 *                      &lt;xsd:element ref="gml:Envelope"/&gt;
 *                  &lt;/xsd:choice&gt;
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
public class BinarySpatialOpTypeBinding extends AbstractComplexBinding {

	FilterFactory filterfactory;		
	public BinarySpatialOpTypeBinding( FilterFactory filterfactory ) {
		this.filterfactory = filterfactory;
	}

	/**
	 * @generated
	 */
	public QName getTarget() {
		return OGC.BINARYSPATIALOPTYPE;
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
		Expression spatial = null;
		
		if ( node.getChild( Geometry.class ) != null ) {
			spatial = filterfactory.literal( node.getChildValue( Geometry.class ) );
		}
		else {
			spatial = filterfactory.literal( node.getChildValue( Envelope.class ) );
		}
		
		return new Expression[] { name, spatial };
	}

}