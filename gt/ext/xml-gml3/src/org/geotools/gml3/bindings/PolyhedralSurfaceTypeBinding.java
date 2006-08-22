package org.geotools.gml3.bindings;


import org.geotools.xml.*;


import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/gml:PolyhedralSurfaceType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;complexType name="PolyhedralSurfaceType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;A polyhedral surface is a surface composed
 *     of polygon surfaces connected along their common boundary 
 *     curves. This differs from the surface type only in the
 *     restriction on the types of surface patches acceptable.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;restriction base="gml:SurfaceType"&gt;
 *              &lt;sequence&gt;
 *                  &lt;group ref="gml:StandardObjectProperties"/&gt;
 *                  &lt;element ref="gml:polygonPatches"&gt;
 *                      &lt;annotation&gt;
 *                          &lt;documentation&gt;This property encapsulates the patches of 
 *        the polyhedral surface.&lt;/documentation&gt;
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
public class PolyhedralSurfaceTypeBinding extends AbstractComplexBinding {

	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.POLYHEDRALSURFACETYPE;
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