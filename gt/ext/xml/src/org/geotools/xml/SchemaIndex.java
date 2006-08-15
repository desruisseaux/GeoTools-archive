package org.geotools.xml;

import javax.xml.namespace.QName;

import org.eclipse.xsd.XSDAttributeDeclaration;
import org.eclipse.xsd.XSDAttributeGroupDefinition;
import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDSimpleTypeDefinition;
import org.eclipse.xsd.XSDTypeDefinition;

public interface SchemaIndex {

	/**
	 * @return The schema itself.
	 */
	XSDSchema[] getSchemas();
	
	/**
	 * Returns the element declaration with the specified qualified name.
	 * 
	 * @param qName the qualified name of the element.
	 * 
	 * @return The element declaration, or null if no such element declaration
	 * exists.
	 */
	XSDElementDeclaration getElementDeclaration(QName qName);
	
	/**
	 * Returns the attribute declaration with the specified qualified name.
	 * 
	 * @param qName the qualified name of the attribute.
	 * 
	 * @return The attribute declaration, or null if no such attribute 
	 * declaration exists.
	 */
	XSDAttributeDeclaration getAttributeDeclaration(QName qName);

	/**
	 * Returns the attribute group definition with the specified qualified name.
	 * 
	 * @param qName the qualified name of the attribute group.
	 * 
	 * @return The attribute group definition, or null if no such attribute 
	 * group definition exists.
	 */
	XSDAttributeGroupDefinition getAttributeGroupDefinition(QName qName);
	
	/**
	 * Returns the complex type definition with the specified qualified name.
	 * 
	 * @param qName qualified name of the complex type.
	 * 
	 * @return The complex type definition, or null if no such complex type
	 * definition exists.
	 */
	XSDComplexTypeDefinition getComplexTypeDefinition(QName qName);
	
	/**
	 * Returns the simple type definition with the specified qualified name.
	 * 
	 * @param qName qualified name of the simple type.
	 * 
	 * @return The simple type definition, or null if no such simple type
	 * definition exists.
	 */
	XSDSimpleTypeDefinition getSimpleTypeDefinition(QName qName);
	
	/**
	 * Returns the type definition with the specified qualified name.
	 * 
	 * @param qName qualified name of the type.
	 * 
	 * @return The type definition, or null if no such type definition exists.
	 */
	XSDTypeDefinition getTypeDefinition(QName qName);
	
}