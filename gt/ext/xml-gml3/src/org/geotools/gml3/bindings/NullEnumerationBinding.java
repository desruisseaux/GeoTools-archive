package org.geotools.gml3.bindings;


import org.geotools.xml.*;


import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/gml:NullEnumeration.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;simpleType name="NullEnumeration"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt; Some common reasons for a null value:   
 *  			
 *          innapplicable - the object does not have a value
 *          missing - The correct value is not readily available to the sender of this data.  
 *                             Furthermore, a correct value may not exist.
 *          template - the value will be available later
 *          unknown - The correct value is not known to, and not computable by, the sender of this data. 
 *                             However, a correct value probably exists.
 *          withheld - the value is not divulged 
 *          
 *          other:reason - as indicated by "reason" string
 *          
 *          Specific communities may agree to assign more strict semantics when these terms are used in a particular context.  
 *        &lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;union&gt;
 *          &lt;simpleType&gt;
 *              &lt;restriction base="string"&gt;
 *                  &lt;enumeration value="inapplicable"/&gt;
 *                  &lt;enumeration value="missing"/&gt;
 *                  &lt;enumeration value="template"/&gt;
 *                  &lt;enumeration value="unknown"/&gt;
 *                  &lt;enumeration value="withheld"/&gt;
 *              &lt;/restriction&gt;
 *          &lt;/simpleType&gt;
 *          &lt;simpleType&gt;
 *              &lt;restriction base="string"&gt;
 *                  &lt;pattern value="other:\w{2,}"/&gt;
 *              &lt;/restriction&gt;
 *          &lt;/simpleType&gt;
 *      &lt;/union&gt;
 *  &lt;/simpleType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class NullEnumerationBinding extends AbstractSimpleBinding {

	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.NULLENUMERATION;
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