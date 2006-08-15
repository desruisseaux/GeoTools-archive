package org.geotools.xml.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.eclipse.xsd.XSDAttributeDeclaration;
import org.eclipse.xsd.XSDAttributeGroupDefinition;
import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDImport;
import org.eclipse.xsd.XSDInclude;
import org.eclipse.xsd.XSDNamedComponent;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDSchemaContent;
import org.eclipse.xsd.XSDSimpleTypeDefinition;
import org.eclipse.xsd.XSDTypeDefinition;
import org.geotools.xml.SchemaIndex;

public class SchemaIndexImpl implements SchemaIndex {

	/**
	 * The schemas
	 */
	XSDSchema[] schemas;
	
	/**
	 * Indexes
	 */
	HashMap elementIndex;
	HashMap attributeIndex;
	HashMap attributeGroupIndex;
	HashMap complexTypeIndex;
	HashMap simpleTypeIndex;
	
	public SchemaIndexImpl(XSDSchema[] schemas) {
		this.schemas = schemas;
	}
	
	public XSDSchema[] getSchemas() {
		return schemas;
	}

	public XSDImport[] getImports() {
		Collection imports = find(XSDImport.class);
		return (XSDImport[])imports.toArray(new XSDImport[imports.size()]);
	}
	
	public XSDInclude[] getIncludes() {
		Collection includes = find(XSDInclude.class);
		return (XSDInclude[])includes.toArray(new XSDInclude[includes.size()]);
	}
	
	public XSDElementDeclaration getElementDeclaration(QName qName) {
		return (XSDElementDeclaration) getElementIndex().get(qName);
	}
	
	public XSDAttributeDeclaration getAttributeDeclaration(QName qName) {
		return (XSDAttributeDeclaration) getAttributeIndex().get(qName);
	}

	public XSDAttributeGroupDefinition getAttributeGroupDefinition(QName qName) {
		return (XSDAttributeGroupDefinition) getAttributeGroupIndex().get(qName);
	}

	public XSDComplexTypeDefinition getComplexTypeDefinition(QName qName) {
		return (XSDComplexTypeDefinition) getComplexTypeIndex().get(qName);
	}
	
	public XSDSimpleTypeDefinition getSimpleTypeDefinition(QName qName) {
		return (XSDSimpleTypeDefinition) getSimpleTypeIndex().get(qName);
	}
	
	public XSDTypeDefinition getTypeDefinition(QName qName) {
		XSDTypeDefinition type = getComplexTypeDefinition(qName);
		if (type == null) {
			type = getSimpleTypeDefinition(qName);
		}
		
//		if (type == null) {
//			//could not find type def, try to resolve
//			
//			//TODO: this could cause problems, the reason being that the 
//			// schema object will always resolve to something, even when 
//			// the type can not be found, the default seems to be to just 
//			// create a simple type with the namespace, and name
//			type = schema.resolveTypeDefinition(
//				qName.getNamespaceURI(),qName.getLocalPart()
//			);
//			if (type instanceof XSDComplexTypeDefinition) {
//				//rebuild the complex type index
//				complexTypeIndex = null;
//			}
//			else simpleTypeIndex = null;
//		}
		
		return type;
	}
	
	protected Collection find(Class c) {
		ArrayList found = new ArrayList();
		for (int i = 0; i < schemas.length; i++) {
			XSDSchema schema = schemas[i];
			
			List content = schema.getContents();
			
			for (Iterator itr = content.iterator(); itr.hasNext();) {
				Object o = itr.next();
				if (c.isAssignableFrom(o.getClass()))
					found.add(o);
			}
		}
		
		return found;
	}
	
	protected HashMap getElementIndex() {
		if (elementIndex == null) {
			elementIndex = buildIndex(XSDElementDeclaration.class);
		}
		
		return elementIndex;
	}
	
	protected HashMap getAttributeIndex() {
		if (attributeIndex == null) {
			attributeIndex = buildIndex(XSDAttributeDeclaration.class);
		}
		return attributeIndex;
	}
	
	protected HashMap getAttributeGroupIndex() {
		if (attributeGroupIndex == null) {
			attributeGroupIndex = buildIndex(XSDAttributeGroupDefinition.class);
		}
		return attributeGroupIndex;
	}
	
	protected HashMap getComplexTypeIndex() {
		if (complexTypeIndex == null) {
			complexTypeIndex = buildIndex(XSDComplexTypeDefinition.class);
		}
		
		return complexTypeIndex;
	}
	
	protected HashMap getSimpleTypeIndex() {
		if (simpleTypeIndex == null) {
			simpleTypeIndex = buildIndex(XSDSimpleTypeDefinition.class);
		}
		
		return simpleTypeIndex;
	}
	
	protected HashMap buildIndex(Class c) {
		HashMap index = new HashMap();
		for (int i = 0; i < schemas.length; i++) {
			XSDSchema schema = schemas[i];
			
			List contents = schema.getContents();
			for (Iterator itr = contents.iterator(); itr.hasNext();) {
				XSDSchemaContent content = (XSDSchemaContent) itr.next();
				
				if (!c.isAssignableFrom(content.getClass()))
					continue;
				if (!(content instanceof XSDNamedComponent))
					continue;
				
				XSDNamedComponent namedContent = (XSDNamedComponent)content;
				
				String ns = namedContent.getTargetNamespace();
				
				QName qName = new QName(ns,namedContent.getName());
				index.put(qName,namedContent);
			}
		}
		
		
		return index;
	}
	
}