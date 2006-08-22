package org.geotools.gml3.bindings;


import org.geotools.xml.*;


import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/gml:TriangulatedSurfaceType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;complexType name="TriangulatedSurfaceType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;A triangulated surface is a polyhedral 
 *     surface that is composed only of triangles. There is no
 *     restriction on how the triangulation is derived.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;restriction base="gml:SurfaceType"&gt;
 *              &lt;sequence&gt;
 *                  &lt;group ref="gml:StandardObjectProperties"/&gt;
 *                  &lt;element ref="gml:trianglePatches"&gt;
 *                      &lt;annotation&gt;
 *                          &lt;documentation&gt;This property encapsulates the patches of 
 *        the triangulated surface.&lt;/documentation&gt;
 *                      &lt;/annotation&gt;
 *                  &lt;/element&gt;
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
public class TriangulatedSurfaceTypeBinding extends AbstractComplexBinding {

	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.TRIANGULATEDSURFACETYPE;
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