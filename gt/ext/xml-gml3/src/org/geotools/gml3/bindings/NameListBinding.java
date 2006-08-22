package org.geotools.gml3.bindings;


import org.geotools.xml.*;


import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/gml:NameList.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;simpleType name="NameList"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;XML List based on XML Schema Name type.  An element of this type contains a space-separated list of Name values&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;list itemType="Name"/&gt;
 *  &lt;/simpleType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class NameListBinding extends AbstractSimpleBinding {

	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.NAMELIST;
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
	public Object parse(InstanceComponent instance, Object value) 
		throws Exception {
		
		//TODO: implement
		return null;
	}

}