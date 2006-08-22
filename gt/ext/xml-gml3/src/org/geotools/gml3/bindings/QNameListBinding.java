package org.geotools.gml3.bindings;


import org.geotools.xml.*;


import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/gml:QNameList.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;simpleType name="QNameList"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;A set of values, representing a list of token with the lexical value space of QName. The tokens are seperated by whitespace.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;list itemType="QName"/&gt;
 *  &lt;/simpleType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class QNameListBinding extends AbstractSimpleBinding {

	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.QNAMELIST;
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