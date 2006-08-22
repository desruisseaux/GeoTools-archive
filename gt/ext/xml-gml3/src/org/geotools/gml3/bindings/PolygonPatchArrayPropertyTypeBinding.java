package org.geotools.gml3.bindings;


import org.geotools.xml.*;


import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/gml:PolygonPatchArrayPropertyType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;complexType name="PolygonPatchArrayPropertyType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;This type defines a container for an array of 
 *     polygon patches.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;restriction base="gml:SurfacePatchArrayPropertyType"&gt;
 *              &lt;sequence maxOccurs="unbounded" minOccurs="0"&gt;
 *                  &lt;element ref="gml:PolygonPatch"/&gt;
 *              &lt;/sequence&gt;
 *          &lt;/restriction&gt;
 *      &lt;/complexContent&gt;
 *  &lt;/complexType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class PolygonPatchArrayPropertyTypeBinding extends AbstractComplexBinding {

	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.POLYGONPATCHARRAYPROPERTYTYPE;
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