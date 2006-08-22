package org.geotools.gml3.bindings;


import org.geotools.xml.*;


import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/gml:DictionaryType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;complexType name="DictionaryType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;A non-abstract bag that is specialized for use as a dictionary which contains a set of definitions. These definitions are referenced from other places, in the same and different XML documents. In this restricted type, the inherited optional "description" element can be used for a description of this dictionary. The inherited optional "name" element can be used for the name(s) of this dictionary. The inherited "metaDataProperty" elements can be used to reference or contain more information about this dictionary. The inherited required gml:id attribute allows the dictionary to be referenced using this handle. &lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;extension base="gml:DefinitionType"&gt;
 *              &lt;sequence maxOccurs="unbounded" minOccurs="0"&gt;
 *                  &lt;choice&gt;
 *                      &lt;element ref="gml:dictionaryEntry"&gt;
 *                          &lt;annotation&gt;
 *                              &lt;documentation&gt;An entry in this dictionary. The content of an entry can itself be a lower level dictionary or definition collection. This element follows the standard GML property model, so the value may be provided directly or by reference. Note that if the value is provided by reference, this definition does not carry a handle (gml:id) in this context, so does not allow external references to this specific entry in this context. When used in this way the referenced definition will usually be in a dictionary in the same XML document. &lt;/documentation&gt;
 *                          &lt;/annotation&gt;
 *                      &lt;/element&gt;
 *                      &lt;element ref="gml:indirectEntry"&gt;
 *                          &lt;annotation&gt;
 *                              &lt;documentation&gt;An identified reference to a remote entry in this dictionary, to be used when this entry should be identified to allow external references to this specific entry. &lt;/documentation&gt;
 *                          &lt;/annotation&gt;
 *                      &lt;/element&gt;
 *                  &lt;/choice&gt;
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
public class DictionaryTypeBinding extends AbstractComplexBinding {

	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.DICTIONARYTYPE;
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