package org.geotools.gml3.bindings;


import org.geotools.xml.*;


import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/gml:booleanOrNullList.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;simpleType name="booleanOrNullList"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;XML List based on the union type defined above.  An element declared with this type contains a space-separated list of boolean values {0,1,true,false} with null values interspersed as needed&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;list itemType="gml:booleanOrNull"/&gt;
 *  &lt;/simpleType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class BooleanOrNullListBinding extends AbstractSimpleBinding {

	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.BOOLEANORNULLLIST;
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