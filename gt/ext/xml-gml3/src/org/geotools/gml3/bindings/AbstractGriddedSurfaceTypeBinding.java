package org.geotools.gml3.bindings;


import org.geotools.xml.*;


import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/gml:AbstractGriddedSurfaceType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;complexType name="AbstractGriddedSurfaceType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;A gridded surface is a parametric curve
 *     surface derived from a rectangular grid in the parameter
 *     space. The rows from this grid are control points for
 *     horizontal surface curves; the columns are control points
 *     for vertical surface curves. The working assumption is that
 *     for a pair of parametric co-ordinates (s, t) that the
 *     horizontal curves for each integer offset are calculated
 *     and evaluated at "s". The defines a sequence of control
 *     points:
 *     
 *     cn(s) : s  1 .....columns 
 *  
 *     From this sequence a vertical curve is calculated for "s",
 *     and evaluated at "t". In most cases, the order of
 *     calculation (horizontal-vertical vs. vertical-horizontal)
 *     does not make a difference. Where it does, the horizontal-   
 *     vertical order shall be the one used.
 *  
 *     Logically, any pair of curve interpolation types can lead
 *     to a subtype of GriddedSurface. The following clauses
 *     define some most commonly encountered surfaces that can
 *     be represented in this manner.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;extension base="gml:AbstractParametricCurveSurfaceType"&gt;
 *              &lt;sequence&gt;
 *                  &lt;group ref="gml:PointGrid"&gt;
 *                      &lt;annotation&gt;
 *                          &lt;documentation&gt;This is the double indexed sequence
 *         of control points, given in row major form. 
 *         NOTE! There in no assumption made about the shape
 *         of the grid. 
 *         For example, the positions need not effect a "21/2D"
 *         surface, consecutive points may be equal in any or all
 *         of the ordinates. Further, the curves in either or both
 *         directions may close.&lt;/documentation&gt;
 *                      &lt;/annotation&gt;
 *                  &lt;/group&gt;
 *                  &lt;element minOccurs="0" name="rows" type="integer"&gt;
 *                      &lt;annotation&gt;
 *                          &lt;documentation&gt;The attribute rows gives the number
 *           of rows in the parameter grid.&lt;/documentation&gt;
 *                      &lt;/annotation&gt;
 *                  &lt;/element&gt;
 *                  &lt;element minOccurs="0" name="columns" type="integer"&gt;
 *                      &lt;annotation&gt;
 *                          &lt;documentation&gt;The attribute columns gives the number
 *          of columns in the parameter grid.&lt;/documentation&gt;
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
public class AbstractGriddedSurfaceTypeBinding extends AbstractComplexBinding {

	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.ABSTRACTGRIDDEDSURFACETYPE;
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