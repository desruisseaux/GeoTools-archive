package org.geotools.gml2.bindings;


import org.geotools.xml.*;

import javax.xml.namespace.QName;

import org.geotools.xml.SimpleBinding;
import org.geotools.xml.InstanceComponent;

/**
 * Binding object for the type http://www.opengis.net/gml:NullType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;simpleType name="NullType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;         If a bounding shape is not provided for
 *              a feature collection,          explain why. Allowable values
 *              are:         innapplicable - the features do not have
 *              geometry         unknown - the boundingBox cannot be
 *              computed         unavailable - there may be a boundingBox
 *              but it is not divulged         missing - there are no
 *              features       &lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;restriction base="string"&gt;
 *          &lt;enumeration value="inapplicable"/&gt;
 *          &lt;enumeration value="unknown"/&gt;
 *          &lt;enumeration value="unavailable"/&gt;
 *          &lt;enumeration value="missing"/&gt;
 *      &lt;/restriction&gt;
 *  &lt;/simpleType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class GMLNullTypeBinding implements SimpleBinding {
	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.NULLTYPE;
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
	 * Returns an object of type @link com.vividsolutions.jts.geom.Envelope. In 
	 * the event that a <b>null</b> element is given, a null Envelope is 
	 * returned by calling @link com.vividsolutions.jts.geom.Envelope#setToNull().
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public Object parse(InstanceComponent instance, Object value) 
		throws Exception {
		
		//dont do anything special, here just return the string
		return value;
	}
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *	
	 * @generated modifiable
	 */	
	public String encode(Object object, String value) {
		//TODO: implement
		return null;
	}
	
}