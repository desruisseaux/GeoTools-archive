package org.geotools.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.namespace.QName;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.xsd.XSDAttributeDeclaration;
import org.eclipse.xsd.XSDAttributeGroupContent;
import org.eclipse.xsd.XSDAttributeGroupDefinition;
import org.eclipse.xsd.XSDAttributeUse;
import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDInclude;
import org.eclipse.xsd.XSDModelGroup;
import org.eclipse.xsd.XSDModelGroupDefinition;
import org.eclipse.xsd.XSDNamedComponent;
import org.eclipse.xsd.XSDParticle;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDSchemaContent;
import org.eclipse.xsd.XSDSimpleTypeDefinition;
import org.eclipse.xsd.XSDTypeDefinition;
import org.eclipse.xsd.util.XSDConstants;
import org.eclipse.xsd.util.XSDResourceFactoryImpl;
import org.eclipse.xsd.util.XSDResourceImpl;
import org.eclipse.xsd.util.XSDSchemaLocationResolver;
import org.eclipse.xsd.util.XSDSchemaLocator;
import org.eclipse.xsd.util.XSDUtil;
import org.geotools.xml.impl.TypeWalker;

/**
 * Utility class for performing various opreations.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class Schemas {

	static {
		//need to register custom factory to load schema resources
		Resource.Factory.Registry.INSTANCE
			.getExtensionToFactoryMap().put("xsd", new XSDResourceFactoryImpl());
	}
	
	/**
	 * Parses a schema at the specified location.
	 * 
	 * @param location A uri pointing to the location of the schema.
	 * 
	 * @return The parsed schema, or null if the schema could not be parsed.
	 * 
	 * @throws IOException In the event of a schema parsing error.
	 */
	public static final XSDSchema parse(String location) throws IOException {
		return parse(location, null, null);
	}
	
	/**
	 * Parses a schema at the specified location.
	 * 
	 * @param location A uri pointing to the location of the schema.
	 * @param locators An array of schema locator objects to be used when 
	 * parsing input/includes of the main schema.
	 * @param resolvers An array of schema location resolvers used to override
	 * schema locations encountered in an instance document or an imported
	 * schema.
	 * 
	 * @return The parsed schema, or null if the schema could not be parsed.
	 * 
	 * @throws IOException In the event of a schema parsing error.
	 */
	public static final XSDSchema parse(
		String location, XSDSchemaLocator[] locators, XSDSchemaLocationResolver[] resolvers
	) throws IOException {
		
		//check for case of file url, make sure it is an absolute reference
		if (new File(location).exists()) {
			location = new File(location).getCanonicalPath();
		}
		
		URI uri = URI.createURI(location);
		final ResourceSet resourceSet = new ResourceSetImpl();
		
		//add the specialized schema location resolvers
		if (resolvers != null && resolvers.length > 0) {
			AdapterFactory adapterFactory = 
				new SchemaLocationResolverAdapterFactory(resolvers);
			resourceSet.getAdapterFactories().add(adapterFactory);
		}
		
		//add the specialized schema locators as adapters
		if (locators != null && locators.length > 0) {
			AdapterFactory adapterFactory = 
				new SchemaLocatorAdapterFactory(locators);
			resourceSet.getAdapterFactories().add(adapterFactory);
		}
		
		XSDResourceImpl xsdMainResource = 
			(XSDResourceImpl)resourceSet.createResource(URI.createURI(".xsd"));
	    xsdMainResource.setURI(uri);
		xsdMainResource.load(resourceSet.getLoadOptions());
		    
		return xsdMainResource.getSchema();
	}
	
	/**
	 * Returns a list of all child element declarations of the specified 
	 * element.
	 * 
	 * @param element The parent element.
	 * 
	 * @return A list of @link XSDElementDeclaration objects, one for each 
	 * child element.
	 * 
	 * @deprecated use {@link #getChildElementDeclarations(XSDTypeDefinition)}
	 */
	public static final List getChildElementDeclarations(
		XSDElementDeclaration element
	) {
		return getChildElementDeclarations(element.getType());
	}
	
	/**
	 * Returns a list of all child element declarations of the specified 
	 * type.
	 * 
	 * @param type The type.
	 * 
	 * @return A list of @link XSDElementDeclaration objects, one for each 
	 * child element.
	 */
	public static final List getChildElementDeclarations(XSDTypeDefinition type) {
		return getChildElementDeclarations(type,true);
	}
	
	/**
	 * Returns a list of all child element declarations of the specified 
	 * element.
	 * <p>
	 * 	The <code>includeParents</code> flag controls if this method should
	 * returns those elements defined on parent types.
	 * </p>
	 * @param element The parent element.
	 * @param includeParents Flag indicating if parent types should be processed.
	 * @return A list of @link XSDElementDeclaration objects, one for each 
	 * child element.
	 * 
	 * @deprecated use {@link #getChildElementDeclarations(XSDTypeDefinition, boolean)}
	 */
	public static final List getChildElementDeclarations(
		XSDElementDeclaration element, boolean includeParents
	) {
		return getChildElementDeclarations(element.getType(),includeParents);
	}
	
	/**
	 * Returns a list of all child element declarations of the specified 
	 * element.
	 * <p>
	 * 	The <code>includeParents</code> flag controls if this method should
	 * returns those elements defined on parent types.
	 * </p>
	 * @param element The parent element.
	 * @param includeParents Flag indicating if parent types should be processed.
	 * @return A list of @link XSDElementDeclaration objects, one for each 
	 * child element.
	 */
	public static final List getChildElementDeclarations(
		XSDTypeDefinition type, boolean includeParents
	) { 
		final ArrayList elements = new ArrayList();
		
		TypeWalker.Visitor visitor = new TypeWalker.Visitor() {
			public boolean visit(XSDTypeDefinition type) {
				//simple types dont have children
				if (type instanceof XSDSimpleTypeDefinition)
					return true;
				
				XSDComplexTypeDefinition cType = (XSDComplexTypeDefinition)type;
					
				ElementVisitor visitor = new ElementVisitor() {
					public void visit(XSDParticle particle) {
						//element declaration, add to list
						XSDElementDeclaration decl = 
							(XSDElementDeclaration)particle.getContent();
						
						if (!decl.isElementDeclarationReference())
							elements.add(decl);
						else  
							elements.add(decl.getResolvedElementDeclaration());
						
					}
				};
				visitElements(cType,visitor);
				
				
				return true;
//				
//				//simple content cant define children
//				if (cType.getContent() == null || 
//						(cType.getContent() instanceof XSDSimpleTypeDefinition))
//					return true;
//						
//				//use a queue to simulate the recursion
//				LinkedList queue = new LinkedList();
//				queue.addLast(cType.getContent());
//				
//				while(!queue.isEmpty()) {
//					XSDParticle particle = (XSDParticle)queue.removeFirst();
//					
//					//analyze type of particle content
//					int pType = XSDUtil.nodeType(particle.getElement()); 
//					if (pType == XSDConstants.ELEMENT_ELEMENT) {
//						//element declaration, add to list
//						XSDElementDeclaration decl = 
//							(XSDElementDeclaration)particle.getContent();
//						
//						if (!decl.isElementDeclarationReference())
//							elements.add(decl);
//						else  
//							elements.add(decl.getResolvedElementDeclaration());
//					}
//					else {
//						//model group
//						XSDModelGroup grp = null;
//						switch(pType) {
//							case XSDConstants.GROUP_ELEMENT:
//								XSDModelGroupDefinition grpDef 
//									= (XSDModelGroupDefinition)particle.getContent();
//								if (grpDef.isModelGroupDefinitionReference())
//									grpDef = grpDef.getResolvedModelGroupDefinition();
//								
//								grp = grpDef.getModelGroup();
//								
//								break;
//								
//							case XSDConstants.CHOICE_ELEMENT:
//							case XSDConstants.ALL_ELEMENT:
//							case XSDConstants.SEQUENCE_ELEMENT:	
//								grp = (XSDModelGroup) particle.getContent();
//								break;
//						}
//						
//						if (grp != null) {
//							//enque all particles in the group
//							List parts = grp.getParticles();
//							for (Iterator itr = parts.iterator(); itr.hasNext();) {
//								queue.addLast(itr.next());
//							}
//						}
//					}
//				} //while
//				
//				return true;
			}
		};
			
		if (includeParents) {
			//walk up the type hierarchy of the element to generate a list of 
			// possible elements
			TypeWalker walker = new TypeWalker(type);
			walker.walk(visitor);	
		}
		else {
			//just visit this type
			visitor.visit(type);
		}
		
		return elements;
	}
	
	/**
	 * Returns the minimum number of occurences of an element within a complex
	 * type.
	 * 
	 * @param type The type definition containg the declaration <code>element</code>
	 * @param element The declaration of the element.
	 * 
	 * @return The minimum number of occurences.
	 * 
	 * @throws IllegalArgumentException If the element declaration cannot be 
	 * locaated withing the type definition.
	 */
	public static final int getMinOccurs(
		XSDComplexTypeDefinition type,  XSDElementDeclaration element
	) {
		final XSDElementDeclaration fElement = element;
		final ArrayList minOccurs = new ArrayList(); 
		
		ElementVisitor visitor = new ElementVisitor() {

			public void visit(XSDParticle particle) {
				XSDElementDeclaration decl = 
					(XSDElementDeclaration)particle.getContent();
				
				
				if (decl.isElementDeclarationReference())
					decl = decl.getResolvedElementDeclaration();
				
				if (decl == fElement) {
					minOccurs.add(new Integer(particle.getMinOccurs()));
				}
				
			}
			
		};
		
		visitElements(type,visitor);
		
		if (minOccurs.isEmpty()) {
			throw new IllegalArgumentException(
				"Element: " + element + " not found in type: " + type 
			);
		}
		
		return ((Integer)minOccurs.get(0)).intValue();
 	}
	
	/**
	 * Returns the minimum number of occurences of an element within a complex
	 * type.
	 * 
	 * @param type The type definition containg the declaration <code>element</code>
	 * @param element The declaration of the element.
	 * 
	 * @return The minimum number of occurences.
	 * 
	 * @throws IllegalArgumentException If the element declaration cannot be 
	 * locaated withing the type definition.
	 */
	public static final int getMaxOccurs(
		XSDComplexTypeDefinition type,  XSDElementDeclaration element
	) {
		final XSDElementDeclaration fElement = element;
		final ArrayList maxOccurs = new ArrayList(); 
		
		ElementVisitor visitor = new ElementVisitor() {
			public void visit(XSDParticle particle) {
				XSDElementDeclaration decl = 
					(XSDElementDeclaration)particle.getContent();
				
				if (decl.isElementDeclarationReference())
					decl = decl.getResolvedElementDeclaration();
				
				if (decl == fElement) {
					maxOccurs.add(new Integer(particle.getMaxOccurs()));
				}
			}
		};
		
		visitElements(type,visitor);
		if (maxOccurs.isEmpty()) {
			throw new IllegalArgumentException(
				"Element: " + element + " not found in type: " + type 
			);
		}
		
		return ((Integer)maxOccurs.get(0)).intValue();
 	}
	
	private static void visitElements(XSDComplexTypeDefinition cType, ElementVisitor visitor) {
		//simple content cant define children
		if (cType.getContent() == null || 
				(cType.getContent() instanceof XSDSimpleTypeDefinition))
			return;
				
		//use a queue to simulate the recursion
		LinkedList queue = new LinkedList();
		queue.addLast(cType.getContent());
		
		while(!queue.isEmpty()) {
			XSDParticle particle = (XSDParticle)queue.removeFirst();
			
			//analyze type of particle content
			int pType = XSDUtil.nodeType(particle.getElement()); 
			if (pType == XSDConstants.ELEMENT_ELEMENT) {
				visitor.visit(particle);
			}
			else {
				//model group
				XSDModelGroup grp = null;
				switch(pType) {
					case XSDConstants.GROUP_ELEMENT:
						XSDModelGroupDefinition grpDef 
							= (XSDModelGroupDefinition)particle.getContent();
						if (grpDef.isModelGroupDefinitionReference())
							grpDef = grpDef.getResolvedModelGroupDefinition();
						
						grp = grpDef.getModelGroup();
						
						break;
						
					case XSDConstants.CHOICE_ELEMENT:
					case XSDConstants.ALL_ELEMENT:
					case XSDConstants.SEQUENCE_ELEMENT:	
						grp = (XSDModelGroup) particle.getContent();
						break;
				}
				
				if (grp != null) {
					//enque all particles in the group
					List parts = grp.getParticles();
					for (Iterator itr = parts.iterator(); itr.hasNext();) {
						queue.addLast(itr.next());
					}
				}
			}
		} //while
	}
	
	/**
	 * Simple visitor interface for visiting elements which are part of 
	 * complex types. This interface is private api because there is probably
	 * a better way of visiting the contents of a type.
	 * 
	 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
	 *
	 */
	private static interface ElementVisitor {
		
		/**
		 * The particle containing the element.
		 * @param element
		 */
		void visit(XSDParticle element);
	}
	
	/**
	 * Returns an element declaration that is contained in the type of another
	 * element declaration. The following strategy is used to locate the child
	 * element declaration.
	 * 
	 * <ol>
	 * 	<li>The immediate children of the specified element are examined, if a
	 * match is found, it is returned.
	 *  </li>If 1. does not match, global elements that derive from the 
	 *  immediate children are examined.
	 * </ol>
	 *  
	 * @param element The containing element declaration.
	 * @param qName The qualified name of the contained element.
	 * 
	 * @return The contained element declaration, or false if containment is 
	 * not satisfied.
	 */
	public static final XSDElementDeclaration getChildElementDeclaration(
			XSDElementDeclaration parent, QName qName
	) {
		
		//look for a match in a direct child
		List children = getChildElementDeclarations(parent);
		for (Iterator itr = children.iterator(); itr.hasNext();) {
			XSDElementDeclaration element = (XSDElementDeclaration)itr.next();
			if (nameMatches(element,qName))
				return element;
		}
		
		//couldn't find one, look for match in derived elements
		ArrayList derived = new ArrayList();
		for (Iterator itr = children.iterator(); itr.hasNext();) {
			XSDElementDeclaration child = (XSDElementDeclaration) itr.next();
			derived.addAll(getDerivedElementDeclarations(child));
		}
		
		for (Iterator itr = derived.iterator(); itr.hasNext();) {
			XSDElementDeclaration  child = (XSDElementDeclaration) itr.next();
			if (nameMatches(child,qName))
				return child;
		}
		
		return null;
		
	}
	
	/**
	 * Returns a list of all top level elements that are of a type derived
	 * from the type of the specified element.
	 * 
	 * @param element The element.
	 * 
	 * @return All elements which are of a type derived from the type of the 
	 * specified element.
	 */
	public static final List getDerivedElementDeclarations(
		XSDElementDeclaration element
	) {
		List elements = element.getSchema().getElementDeclarations();
		List derived = new ArrayList();
		
		for (Iterator itr = elements.iterator(); itr.hasNext();) {
			XSDElementDeclaration derivee = (XSDElementDeclaration)itr.next();
			if (derivee.equals(element))
				continue;	//same element
			
			XSDTypeDefinition type = derivee.getType();
			while(true) {
				if (type.equals(element.getType())) {
					derived.add(derivee);
					break;
				}
				
				if (type.equals(type.getBaseType()))
					break;
				
				type = type.getBaseType();
			}
		}
		
		return derived;
	}
	
	/**
	 * Returns a list of all attribute declarations declared in the type (or 
	 * any base type) of the specified element.
	 * 
	 * @param element The element.
	 * 
	 * @return A list of @link XSDAttributeDeclaration objects, one for each 
	 * attribute of the element.
	 */
	public static final List getAttributeDeclarations(XSDElementDeclaration element) {
		final ArrayList attributes = new ArrayList();
		
		//walk up the type hierarchy of the element to generate a list of atts
		TypeWalker walker = new TypeWalker(element.getType());
		
		TypeWalker.Visitor visitor = new TypeWalker.Visitor() {
			public boolean visit(XSDTypeDefinition type) {
			
				//simple types dont have attributes
				if (type instanceof XSDSimpleTypeDefinition)
					return true;
				
				XSDComplexTypeDefinition cType = (XSDComplexTypeDefinition)type;
				
				//get all the attribute content (groups,or uses) and add to q 
				List attContent = cType.getAttributeContents();
				for (Iterator itr = attContent.iterator(); itr.hasNext();) {
					XSDAttributeGroupContent content = 
						(XSDAttributeGroupContent)itr.next();
					
					if (content instanceof XSDAttributeUse) {
						//an attribute, add it to the list
						XSDAttributeUse use = (XSDAttributeUse)content;
						attributes.add(use.getAttributeDeclaration());
					}
					else if (content instanceof XSDAttributeGroupDefinition) {
						//attribute group, add all atts in group to list
						XSDAttributeGroupDefinition attGrp = 
							(XSDAttributeGroupDefinition)content;
						
						if (attGrp.isAttributeGroupDefinitionReference()) {
							attGrp = 
								attGrp.getResolvedAttributeGroupDefinition();
						}
						
						List uses = attGrp.getAttributeUses();
						for (Iterator aitr = uses.iterator(); aitr.hasNext();) {
							XSDAttributeUse use = (XSDAttributeUse)aitr.next();
							attributes.add(use.getAttributeDeclaration());
						}
					}
				}
				
				return true;
			}
		};
		
		walker.walk(visitor);
		return attributes;
	}
	
	/**
	 * Returns an attribute declaration that is contained in the type of another
	 * element declaration.
	 *  
	 * @param element The containing element declaration.
	 * @param qName The qualified name of the contained attribute
	 * 
	 * @return The contained attribute declaration, or false if containment is 
	 * not satisfied.
	 */	
	public static final XSDAttributeDeclaration getAttributeDeclaration(
		XSDElementDeclaration element, QName qName
	) {
		
		List atts = getAttributeDeclarations(element);
		for (Iterator itr = atts.iterator(); itr.hasNext();) {
			XSDAttributeDeclaration att = (XSDAttributeDeclaration)itr.next();
			if (nameMatches(att,qName))
				return att;
		}
		
		return null;
	}
	
	/**
	 * Returns a flat list of includes from the specified schema.
	 * <p> 
	 * The method recurses into included schemas. The list returned is filtered so that 
	 * duplicate includes are removed. Two includes are considered equal if they have the same
	 * uri location
	 * </p>
	 * 
	 * @param schema The top-level schema.
	 * 
	 * @return A list containing objects of type {@link XSDInclude}.
	 */
	public static final List getIncludes(XSDSchema schema) {
		LinkedList queue = new LinkedList();
		ArrayList includes = new ArrayList();
		HashSet added = new HashSet();
		
		queue.addLast(schema);
		
		while(!queue.isEmpty()) {
			schema = (XSDSchema)queue.removeFirst();
			List contents = schema.getContents();
			for (Iterator itr = contents.iterator(); itr.hasNext();) {
				XSDSchemaContent content = (XSDSchemaContent)itr.next();
				if (content instanceof XSDInclude) {
					XSDInclude include = (XSDInclude)content;
					
					if ( !added.contains( include.getSchemaLocation() ) ) {
						includes.add( include );
						added.add( include.getSchemaLocation() );
						
						queue.addLast(include.getIncorporatedSchema());
					}
					
				}
			}
		}
	
		return includes;
	}
	
	/**
	 * Method for comparing the name of a schema component to a qualified name.
	 * The component name and the qualified name match if both the namespaces 
	 * match, and the local parts match. Prefixes are ignored. Two strings will
	 * match if one of the following conditions hold.
	 * 
	 * <ul>
	 * 	<li>Both strings are null.
	 *  <li>Both strings are the empty string.
	 *  <li>One string is null, and the other is the empty string.
	 *  <li>Both strings are non-null and non-empty and equals() return true.
	 * </ul>	
	 * 
	 * @param component The component in question.
	 * @param qName The qualifined name.
	 * 
	 * @return
	 */
	private static final boolean nameMatches(
		XSDNamedComponent component, QName qName
	) {
	
		//is this the element we are looking for
		if (component.getTargetNamespace() == null || "".equals(component.getTargetNamespace())) {
			if (qName.getNamespaceURI() == null || "".equals(qName.getNamespaceURI())) {
				//do a local name match
				return component.getName().equals(qName.getLocalPart());
			}
			
			//assume default namespace
			if (component.getSchema().getTargetNamespace().equals(qName.getNamespaceURI())
					&& component.getName().equals(qName.getLocalPart()))
				return true;
		}
		else if (component.getTargetNamespace().equals(qName.getNamespaceURI())  
				&& component.getName().equals(qName.getLocalPart())) 
			return true;
		
		return false;
	}
	static class SchemaLocatorAdapterFactory extends AdapterFactoryImpl {
		
		SchemaLocatorAdapter adapter;
	
		public SchemaLocatorAdapterFactory(XSDSchemaLocator[] locators) {
			adapter = new SchemaLocatorAdapter(locators);
		}
		
		public boolean isFactoryForType(Object type) {
			return type == XSDSchemaLocator.class;
		}
		
		public Adapter adaptNew(Notifier notifier, Object type) {
			return adapter;
		}
	}
	
	static class SchemaLocatorAdapter extends AdapterImpl 
		implements XSDSchemaLocator {
		
		XSDSchemaLocator[] locators;
		
		public SchemaLocatorAdapter(XSDSchemaLocator[] locators) {
			this.locators = locators;
		}
		
		public boolean isAdapterForType(Object type) {
			return type == XSDSchemaLocator.class;
		}
		
		public XSDSchema locateSchema(XSDSchema xsdSchema, String namespaceURI, String rawSchemaLocationURI, String resolvedSchemaLocationURI) {
			for (int i = 0; i < locators.length; i++) {
				XSDSchema schema = locators[i].locateSchema(
					xsdSchema,namespaceURI,rawSchemaLocationURI,resolvedSchemaLocationURI
				);
				if (schema != null)
					return schema;
			}
			
			return null;
		}
	}
	
	static class SchemaLocationResolverAdapterFactory extends AdapterFactoryImpl {
		
		SchemaLocationResolverAdapter adapter;
	
		public SchemaLocationResolverAdapterFactory(XSDSchemaLocationResolver[] resolvers) {
			adapter = new SchemaLocationResolverAdapter(resolvers);
		}
		
		public boolean isFactoryForType(Object type) {
			return type == XSDSchemaLocationResolver.class;
		}
		
		public Adapter adaptNew(Notifier notifier, Object type) {
			return adapter;
		}
	}
	
	static class SchemaLocationResolverAdapter extends AdapterImpl 
		implements XSDSchemaLocationResolver{
		
		XSDSchemaLocationResolver[] resolvers;
		
		public SchemaLocationResolverAdapter(XSDSchemaLocationResolver[] resolvers) {
			this.resolvers = resolvers;
		}
		
		public boolean isAdapterForType(Object type) {
			return type == XSDSchemaLocationResolver.class;
		}
		
		public String resolveSchemaLocation(XSDSchema schema, String namespaceURI, String rawSchemaLocationURI) {
			for (int i = 0; i < resolvers.length; i++) {
				String resolved = resolvers[i].resolveSchemaLocation(
					schema,namespaceURI,rawSchemaLocationURI
				);
				if (resolved != null)
					return resolved;
			}
			
			return null;		
		}
	}
	
	
	/**
	 * Returns the namespace prefix mapped to the targetNamespace of the schema.
	 * 
	 * @param schema The schema in question.
	 * 
	 * @return The namesapce prefix, or <code>null</code> if not found.
	 */
	public static String getTargetPrefix( XSDSchema schema ) {
		String ns = schema.getTargetNamespace();
		Map pre2ns = schema.getQNamePrefixToNamespaceMap();
		
		for (Iterator itr = pre2ns.entrySet().iterator(); itr.hasNext();) {
			Map.Entry entry = (Map.Entry)itr.next();
			if (entry.getValue().equals(ns)) {
				return  (String)entry.getKey();
			}
		}
		
		return null;
	}
}
