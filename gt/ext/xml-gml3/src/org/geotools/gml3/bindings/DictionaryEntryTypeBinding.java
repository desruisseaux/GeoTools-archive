package org.geotools.gml3.bindings;


import org.geotools.xml.*;


import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/gml:DictionaryEntryType.
 *
 * <p>
 *	<pre>
 *	 <code>
 *  &lt;complexType name="DictionaryEntryType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;An entry in a dictionary of definitions. An instance of this type contains or refers to a definition object.  
 *  
 *  The number of definitions contained in this dictionaryEntry is restricted to one, but a DefinitionCollection or Dictionary that contains multiple definitions can be substituted if needed. Specialized descendents of this dictionaryEntry might be restricted in an application schema to allow only including specified types of definitions as valid entries in a dictionary. &lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;sequence minOccurs="0"&gt;
 *          &lt;element ref="gml:Definition"&gt;
 *              &lt;annotation&gt;
 *                  &lt;documentation&gt;This element in a dictionary entry contains the actual definition. &lt;/documentation&gt;
 *              &lt;/annotation&gt;
 *          &lt;/element&gt;
 *      &lt;/sequence&gt;
 *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"&gt;
 *          &lt;annotation&gt;
 *              &lt;documentation&gt;A non-identified reference to a remote entry in this dictionary, to be used when this entry need not be identified to allow external references to this specific entry. The remote entry referenced will usually be in a dictionary in the same XML document. This element will usually be used in dictionaries that are inside of another dictionary. &lt;/documentation&gt;
 *          &lt;/annotation&gt;
 *      &lt;/attributeGroup&gt;
 *  &lt;/complexType&gt; 
 *		
 *	  </code>
 *	 </pre>
 * </p>
 *
 * @generated
 */
public class DictionaryEntryTypeBinding extends AbstractComplexBinding {

	/**
	 * @generated
	 */
	public QName getTarget() {
		return GML.DICTIONARYENTRYTYPE;
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