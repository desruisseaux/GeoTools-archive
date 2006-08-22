package org.geotools.gml3.bindings;


import org.geotools.xml.*;


import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/gml:GenericMetaDataType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;complexType mixed="true" name="GenericMetaDataType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;Deprecated with GML version 3.1.0.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent mixed="true"&gt;
 *          &lt;extension base="gml:AbstractMetaDataType"&gt;
 *              &lt;sequence&gt;
 *                  &lt;any maxOccurs="unbounded" minOccurs="0" processContents="lax"/&gt;
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
public class GenericMetaDataTypeBinding extends AbstractComplexBinding {

	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.GENERICMETADATATYPE;
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