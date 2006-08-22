package org.geotools.gml3.bindings;


import org.geotools.xml.*;


import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/gml:SolidType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;complexType name="SolidType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;A solid is the basis for 3-dimensional geometry. The extent of a solid is defined by the boundary surfaces (shells). A shell is represented by a composite surface, where every  shell is used to represent a single connected component of the boundary of a solid. It consists of a composite surface (a list of orientable surfaces) connected in a topological cycle (an object whose boundary is empty). Unlike a Ring, a Shell's elements have no natural sort order. Like Rings, Shells are simple.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;extension base="gml:AbstractSolidType"&gt;
 *              &lt;sequence&gt;
 *                  &lt;element minOccurs="0" name="exterior" type="gml:SurfacePropertyType"&gt;
 *                      &lt;annotation&gt;
 *                          &lt;appinfo&gt;
 *                              &lt;sch:pattern name="Check either href or content not both"&gt;
 *                                  &lt;sch:rule context="gml:exterior"&gt;
 *                                      &lt;sch:extends rule="hrefOrContent"/&gt;
 *                                  &lt;/sch:rule&gt;
 *                              &lt;/sch:pattern&gt;
 *                          &lt;/appinfo&gt;
 *                          &lt;documentation&gt;Boundaries of solids are similar to surface boundaries. In normal 3-dimensional Euclidean space, one (composite) surface is distinguished as the exterior. In the more general case, this is not always possible.&lt;/documentation&gt;
 *                      &lt;/annotation&gt;
 *                  &lt;/element&gt;
 *                  &lt;element maxOccurs="unbounded" minOccurs="0"
 *                      name="interior" type="gml:SurfacePropertyType"&gt;
 *                      &lt;annotation&gt;
 *                          &lt;appinfo&gt;
 *                              &lt;sch:pattern name="Check either href or content not both"&gt;
 *                                  &lt;sch:rule context="gml:interior"&gt;
 *                                      &lt;sch:extends rule="hrefOrContent"/&gt;
 *                                  &lt;/sch:rule&gt;
 *                              &lt;/sch:pattern&gt;
 *                          &lt;/appinfo&gt;
 *                          &lt;documentation&gt;Boundaries of solids are similar to surface boundaries.&lt;/documentation&gt;
 *                      &lt;/annotation&gt;
 *                  &lt;/element&gt;
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
public class SolidTypeBinding extends AbstractComplexBinding {

	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.SOLIDTYPE;
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