package org.geotools.gml3.bindings;


import java.util.Iterator;
import java.util.List;

import org.geotools.xml.*;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;


import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/gml:MultiLineStringType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;complexType name="MultiLineStringType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;A MultiLineString is defined by one or more LineStrings, referenced through lineStringMember elements. Deprecated with GML version 3.0. Use MultiCurveType instead.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;extension base="gml:AbstractGeometricAggregateType"&gt;
 *              &lt;sequence&gt;
 *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:lineStringMember"/&gt;
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
public class MultiLineStringTypeBinding extends AbstractComplexBinding {

	GeometryFactory gFactory;
	
	public MultiLineStringTypeBinding( GeometryFactory gFactory ) {
		this.gFactory = gFactory;
	}
	
	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.MULTILINESTRINGTYPE;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Class getType() {
		return MultiLineString.class;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(ElementInstance instance, Node node, Object value) 
		throws Exception {
		
		List lines = node.getChildValues( LineString.class );
		return gFactory.createMultiLineString( 
			(LineString[]) lines.toArray( new LineString[ lines.size() ])	
		);
	
	}

}