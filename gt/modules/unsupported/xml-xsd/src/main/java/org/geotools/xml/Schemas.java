/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.xml;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;


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
import org.eclipse.xsd.XSDImport;
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

import org.geotools.resources.Utilities;
import org.geotools.xml.impl.SchemaIndexImpl;
import org.geotools.xml.impl.TypeWalker;
import org.geotools.xml.impl.TypeWalker.Visitor;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoVisitor;


/**
 * Utility class for performing various opreations.
 *
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class Schemas {
    
    private static final Logger LOGGER = Logger.getLogger(Schemas.class.getPackage().getName());
    
    static {
        //need to register custom factory to load schema resources
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap()
                                          .put("xsd",
            new XSDResourceFactoryImpl());
    }

    /**
     * Finds all the XSDSchemas used by the {@link Configuration configuration} 
     * by looking at the configuration's schema locator and its dependencies.
     * 
     * @param configuration the {@link Configuration} for which to find all its
     * related schemas
     * 
     * @return a {@link SchemaIndex} holding the schemas related to 
     * <code>configuration</code>
     */
    public static final SchemaIndex findSchemas(Configuration configuration){
        
        Set configurations = new HashSet(configuration.allDependencies());
        configurations.add(configuration);
        
        List resolvedSchemas = new ArrayList(configurations.size());
        
        for(Iterator it = configurations.iterator(); it.hasNext();){
            Configuration conf = (Configuration) it.next();
            LOGGER.fine("looking up schema for " + conf.getNamespaceURI());
            
            XSDSchemaLocator locator = conf.getSchemaLocator();
            if(locator == null){
                LOGGER.fine("No schema locator for " + conf.getNamespaceURI());
                continue;
            }
            String namespaceURI = conf.getNamespaceURI();
            String schemaLocation = null;
            try{
                URL location = new URL( conf.getSchemaFileURL() );
                schemaLocation = location.toExternalForm();
            }catch(MalformedURLException e){
                throw new RuntimeException(e);
            }
            LOGGER.fine("schema location: " + schemaLocation);
            XSDSchema schema = locator.locateSchema(null, namespaceURI, schemaLocation, null);
            if(schema != null){
                resolvedSchemas.add(schema);
            }
        }
        
        XSDSchema []schemas = (XSDSchema[]) resolvedSchemas.toArray(new XSDSchema[resolvedSchemas.size()]);
        SchemaIndex index = new SchemaIndexImpl(schemas);
        
        return index;
    }

    /**
     * Finds all {@link XSDSchemaLocationResolver}'s used by the configuration.
     * 
     * @param configuration The parser configuration.
     * 
     * @return A list of location resolvers, empty if none found.
     */
    public static List findSchemaLocationResolvers( Configuration configuration ) {
    	List all = configuration.allDependencies();
    	List resolvers = new ArrayList();
    	for ( Iterator c = all.iterator(); c.hasNext(); ) {
    		configuration = (Configuration) c.next();
    		XSDSchemaLocationResolver resolver = configuration.getSchemaLocationResolver();
    		if ( resolver != null ) {
    			resolvers.add( resolver );
    		}
    	}
    	
    	return resolvers;
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
    public static final XSDSchema parse(String location)
        throws IOException {
        return parse(location, (List)null, (List)null);
    }

    /**
     * Parses a schema at the specified location.
     *
     * @param location A uri pointing to the location of the schema.
     * @param locators An array of schema locator objects to be used when
     * parsing imports/includes of the main schema.
     * @param resolvers An array of schema location resolvers used to override
     * schema locations encountered in an instance document or an imported
     * schema.
     *
     * @return The parsed schema, or null if the schema could not be parsed.
     *
     * @throws IOException In the event of a schema parsing error.
     */
    public static final XSDSchema parse(String location,
        XSDSchemaLocator[] locators, XSDSchemaLocationResolver[] resolvers)
        throws IOException {
    	
    	return parse( 
    		location, locators != null ? Arrays.asList( locators ) : (List) null, 
			resolvers != null ? Arrays.asList( resolvers ) : (List) null
    	);
    }
    
    public static final XSDSchema parse( String location, List locators, List resolvers )
    	throws IOException {
    	
        //check for case of file url, make sure it is an absolute reference
        if (new File(location).exists()) {
        	location = new File( location ).getCanonicalFile().toURI().toString();
            //location = new File(location).getCanonicalPath();
        }

        URI uri = URI.createURI(location);
        final ResourceSet resourceSet = new ResourceSetImpl();

        //add the specialized schema location resolvers
        if ((resolvers != null) && !resolvers.isEmpty()) {
            AdapterFactory adapterFactory = new SchemaLocationResolverAdapterFactory(resolvers);
            resourceSet.getAdapterFactories().add(adapterFactory);
        }

        //add the specialized schema locators as adapters
        if ((locators != null) && !locators.isEmpty()) {
            AdapterFactory adapterFactory = new SchemaLocatorAdapterFactory(locators);
            resourceSet.getAdapterFactories().add(adapterFactory);
        }

        XSDResourceImpl xsdMainResource = (XSDResourceImpl) resourceSet
            .createResource(URI.createURI(".xsd"));
        xsdMainResource.setURI(uri);
        xsdMainResource.load(resourceSet.getLoadOptions());

        return xsdMainResource.getSchema();
    }

    /**
     * Returns a list of all child element declarations of the specified
     * element, no order is guaranteed.
     *
     * @param element The parent element.
     *
     * @return A list of @link XSDElementDeclaration objects, one for each
     * child element.
     *
     * @deprecated use {@link #getChildElementDeclarations(XSDTypeDefinition)}
     */
    public static final List getChildElementDeclarations(
        XSDElementDeclaration element) {
        return getChildElementDeclarations(element.getType());
    }

    /**
     * Returns a list of all child element declarations of the specified
     * type, no order is guaranteed.
     *
     * @param type The type.
     *
     * @return A list of @link XSDElementDeclaration objects, one for each
     * child element.
     */
    public static final List getChildElementDeclarations(XSDTypeDefinition type) {
        return getChildElementDeclarations(type, true);
    }

    /**
     * Returns a list of all child element declarations of the specified
     * element.
     * <p>
     *         The <code>includeParents</code> flag controls if this method should
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
        XSDElementDeclaration element, boolean includeParents) {
        return getChildElementDeclarations(element.getType(), includeParents);
    }

    /**
     * Returns the particle for an element declaration that is part of a type.
     * 
     * @param type The type definition.
     * @param name The naem of the child element declaration.
     * 
     * @param includeParents Flag to control wether parent types are included.
     * 
     * @return The particle representing the element declaration, or <code>null</code> if it could 
     * not be found.
     */
    public static final XSDParticle getChildElementParticle( XSDTypeDefinition type, String name, boolean includeParents ) {
    	List particles = getChildElementParticles( type, includeParents );
    	for ( Iterator p = particles.iterator(); p.hasNext(); ) {
    		XSDParticle particle = (XSDParticle) p.next();
    		XSDElementDeclaration element = (XSDElementDeclaration) particle.getContent();
    		if ( element.isElementDeclarationReference() ) {
    			element.getResolvedElementDeclaration();
    		}
    		
    		if ( name.equals( element.getName() ) ) {
    			return particle;
    		}
    	}
    	
    	return null;
    }
    
    /**
     * Returns a list of all child element particles that corresponde to element declarations of 
     * the specified type, no order is guaranteed.
     * <p>
     * The <code>includeParents</code> flag controls if this method should
     * returns those elements defined on parent types.
     * </p>
     * 
     * @param type THe type.
     * @param includeParents flag indicating if parent types should be processed
     *  
     * @return A list of {@link XSDParticle}.
     * 
     */
    public static final List getChildElementParticles( XSDTypeDefinition type, boolean includeParents ) {
        final HashSet contents = new HashSet();
        final ArrayList particles = new ArrayList();
        TypeWalker.Visitor visitor = new TypeWalker.Visitor() {
            public boolean visit(XSDTypeDefinition type) {
                //simple types dont have children
                if (type instanceof XSDSimpleTypeDefinition) {
                    return true;
                }

                XSDComplexTypeDefinition cType = (XSDComplexTypeDefinition) type;

                ElementVisitor visitor = new ElementVisitor() {
                    public void visit(XSDParticle particle) {
                        XSDElementDeclaration element = 
                            (XSDElementDeclaration) particle.getContent();
                        if ( element.isElementDeclarationReference() ) {
                            element = element.getResolvedElementDeclaration();
                        }
                        
                    	//make sure unique
                        if ( !contents.contains( element ) ) {
                            contents.add( element );
                            particles.add( particle );
                        }
                    }
                };

                visitElements(cType, visitor);

                return true;
            }
        };

        if (includeParents) {
            //walk up the type hierarchy of the element to generate a list of 
            // possible elements
            new TypeWalker().rwalk(type,visitor);
        } else {
            //just visit this type
            visitor.visit(type);
        }

        return new ArrayList( particles );
        
    }
    
    /**
     * Returns a list of all child element declarations of the specified
     * type, no order is guaranteed.
     * <p>
     *         The <code>includeParents</code> flag controls if this method should
     * returns those elements defined on parent types.
     * </p>
     * @param type           The type
     * @param includeParents flag indicating if parent types should be processed
     * 
     * @return A list of @link XSDElementDeclaration objects, one for each
     * child element.
     */
    public static final List getChildElementDeclarations( XSDTypeDefinition type, 
                                                          boolean includeParents) {
    	
    	List particles = getChildElementParticles( type, includeParents );
    	List elements = new ArrayList();
    	for ( Iterator p = particles.iterator(); p.hasNext(); ) {
    		XSDParticle particle = (XSDParticle) p.next();
    		XSDElementDeclaration decl = (XSDElementDeclaration) particle.getContent();

	        if (decl.isElementDeclarationReference()) {
	            decl = decl.getResolvedElementDeclaration();
	        }
	        
	        elements.add( decl );
    	}
    	
    
    	return elements;
    }

    /**
     * Returns the base type defintion of <code>type</code> named <code>parentTypeName<code>.
     * <p>
     * This method will handle the case in which the <code>parentTypeName == type.getTypeName()</code>.
     * If no such parent type is found this method will return <code>null</code>.
     * </p>
     * @param type The type.
     * @param parentTypeName The name of the base type to return.
     * 
     * @return The base type, or null if it could not be found.
     */
    public static final XSDTypeDefinition getBaseTypeDefinition( 
    		XSDTypeDefinition type, final QName parentTypeName ) {
    	
    	final List found = new ArrayList();
    	
    	 TypeWalker.Visitor visitor = new TypeWalker.Visitor() {
             public boolean visit(XSDTypeDefinition type) {
            	 if ( nameMatches( type, parentTypeName ) ) {
            		 found.add( type );
            		 return false;
            	 }
            	 
            	 return true;
             }
         };
         new TypeWalker().walk( type, visitor );
         
         return found.isEmpty() ? null : (XSDTypeDefinition) found.get( 0 );
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
    public static final int getMinOccurs(XSDComplexTypeDefinition type,
        XSDElementDeclaration element) {
        final XSDElementDeclaration fElement = element;
        final ArrayList minOccurs = new ArrayList();

        ElementVisitor visitor = new ElementVisitor() {
                public void visit(XSDParticle particle) {
                    XSDElementDeclaration decl = (XSDElementDeclaration) particle
                        .getContent();

                    if (decl.isElementDeclarationReference()) {
                        decl = decl.getResolvedElementDeclaration();
                    }

                    if (decl == fElement) {
                        minOccurs.add(new Integer(particle.getMinOccurs()));
                    }
                }
            };

        visitElements(type, visitor, true);

        if (minOccurs.isEmpty()) {
            throw new IllegalArgumentException("Element: " + element
                + " not found in type: " + type);
        }

        return ((Integer) minOccurs.get(0)).intValue();
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
    public static final int getMaxOccurs(XSDComplexTypeDefinition type,
        XSDElementDeclaration element) {
        final XSDElementDeclaration fElement = element;
        final ArrayList maxOccurs = new ArrayList();

        ElementVisitor visitor = new ElementVisitor() {
                public void visit(XSDParticle particle) {
                    XSDElementDeclaration decl = (XSDElementDeclaration) particle
                        .getContent();

                    if (decl.isElementDeclarationReference()) {
                        decl = decl.getResolvedElementDeclaration();
                    }

                    if (decl == fElement) {
                        maxOccurs.add(new Integer(particle.getMaxOccurs()));
                    }
                }
            };
            
        visitElements(type, visitor, true);

        if (maxOccurs.isEmpty()) {
            throw new IllegalArgumentException("Element: " + element
                + " not found in type: " + type);
        }

        return ((Integer) maxOccurs.get(0)).intValue();
    }


    private static void visitElements(XSDComplexTypeDefinition cType,
            ElementVisitor visitor, boolean includeParents) {
        if(includeParents){

            LinkedList baseTypes = new LinkedList();
            XSDTypeDefinition baseType = cType.getBaseType();
            while(baseType != null && baseType != baseType.getBaseType()){
                if(baseType instanceof XSDComplexTypeDefinition){
                    baseTypes.addLast(baseType);
                }
                baseType = baseType.getBaseType();
            }
            
            for(Iterator it = baseTypes.iterator(); it.hasNext();){
                baseType = (XSDTypeDefinition) it.next();
                visitElements((XSDComplexTypeDefinition) baseType, visitor);
            }
        }
        visitElements(cType, visitor);
    }

    private static void visitElements(XSDComplexTypeDefinition cType,
        ElementVisitor visitor) {
        //simple content cant define children
        if ((cType.getContent() == null)
                || (cType.getContent() instanceof XSDSimpleTypeDefinition)) {
            return;
        }
        
        //use a queue to simulate the recursion
        LinkedList queue = new LinkedList();
        queue.addLast(cType.getContent());

        while (!queue.isEmpty()) {
            XSDParticle particle = (XSDParticle) queue.removeFirst();

            //analyze type of particle content
            int pType = XSDUtil.nodeType(particle.getElement());

            if (pType == XSDConstants.ELEMENT_ELEMENT) {
                visitor.visit(particle);
            } else {
                //model group
                XSDModelGroup grp = null;

                switch (pType) {
                case XSDConstants.GROUP_ELEMENT:

                    XSDModelGroupDefinition grpDef = (XSDModelGroupDefinition) particle
                        .getContent();

                    if (grpDef.isModelGroupDefinitionReference()) {
                        grpDef = grpDef.getResolvedModelGroupDefinition();
                    }

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
     * Returns an element declaration that is contained in the type of another
     * element declaration. The following strategy is used to locate the child
     * element declaration.
     *
     * <ol>
     *         <li>The immediate children of the specified element are examined, if a
     * match is found, it is returned.
     *  </li>If 1. does not match, global elements that derive from the
     *  immediate children are examined.
     * </ol>
     *
     * @param parent the containing element declaration
     * @param qName  the qualified name of the contained element
     *
     * @return The contained element declaration, or false if containment is
     * not satisfied.
     */
    public static final XSDElementDeclaration getChildElementDeclaration(
        XSDElementDeclaration parent, QName qName) {
        //look for a match in a direct child
        List children = getChildElementDeclarations(parent);

        for (Iterator itr = children.iterator(); itr.hasNext();) {
            XSDElementDeclaration element = (XSDElementDeclaration) itr.next();

            if (nameMatches(element, qName)) {
                return element;
            }
        }

        //couldn't find one, look for match in derived elements
        ArrayList derived = new ArrayList();

        for (Iterator itr = children.iterator(); itr.hasNext();) {
            XSDElementDeclaration child = (XSDElementDeclaration) itr.next();
            derived.addAll(getDerivedElementDeclarations(child));
        }

        for (Iterator itr = derived.iterator(); itr.hasNext();) {
            XSDElementDeclaration child = (XSDElementDeclaration) itr.next();

            if (nameMatches(child, qName)) {
                return child;
            }
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
        XSDElementDeclaration element) {
        List elements = element.getSchema().getElementDeclarations();
        List derived = new ArrayList();

        for (Iterator itr = elements.iterator(); itr.hasNext();) {
            XSDElementDeclaration derivee = (XSDElementDeclaration) itr.next();

            if (derivee.equals(element)) {
                continue; //same element
            }

            XSDTypeDefinition type = derivee.getType();

            while (true) {
                if (type.equals(element.getType())) {
                    derived.add(derivee);

                    break;
                }

                if (type.equals(type.getBaseType())) {
                    break;
                }

                type = type.getBaseType();
            }
        }

        return derived;
    }

    /**
     * Returns a list of all attribute declarations declared in the type (or
     * any base type) of the specified element.
     *
     * <p>
     * This method is just a shortcut for {@link #getAttributeDeclarations(XSDTypeDefinition) getAttributeDeclarations(element.getType()}
     * </p>
     * 
     * @param element The element.
     *
     * @return A list of @link XSDAttributeDeclaration objects, one for each
     * attribute of the element.
     */
    public static final List getAttributeDeclarations(
        XSDElementDeclaration element) {
        return getAttributeDeclarations(element.getType());
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
    public static final List getAttributeDeclarations(XSDTypeDefinition type) {
        final ArrayList attributes = new ArrayList();

        //walk up the type hierarchy of the element to generate a list of atts
        TypeWalker.Visitor visitor = new TypeWalker.Visitor() {
            public boolean visit(XSDTypeDefinition type) {
                //simple types dont have attributes
                if (type instanceof XSDSimpleTypeDefinition) {
                    return true;
                }

                XSDComplexTypeDefinition cType = (XSDComplexTypeDefinition) type;

                //get all the attribute content (groups,or uses) and add to q 
                List attContent = cType.getAttributeContents();

                for (Iterator itr = attContent.iterator(); itr.hasNext();) {
                    XSDAttributeGroupContent content = (XSDAttributeGroupContent) itr
                        .next();

                    if (content instanceof XSDAttributeUse) {
                        //an attribute, add it to the list
                        XSDAttributeUse use = (XSDAttributeUse) content;
                        attributes.add(use.getAttributeDeclaration());
                    } else if (content instanceof XSDAttributeGroupDefinition) {
                        //attribute group, add all atts in group to list
                        XSDAttributeGroupDefinition attGrp = (XSDAttributeGroupDefinition) content;

                        if (attGrp.isAttributeGroupDefinitionReference()) {
                            attGrp = attGrp
                                .getResolvedAttributeGroupDefinition();
                        }

                        List uses = attGrp.getAttributeUses();

                        for (Iterator aitr = uses.iterator();
                                aitr.hasNext();) {
                            XSDAttributeUse use = (XSDAttributeUse) aitr
                                .next();
                            attributes.add(use.getAttributeDeclaration());
                        }
                    }
                }

                return true;
            }
        };

        new TypeWalker().walk( type, visitor );
        
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
        XSDElementDeclaration element, QName qName) {
        List atts = getAttributeDeclarations(element);

        for (Iterator itr = atts.iterator(); itr.hasNext();) {
            XSDAttributeDeclaration att = (XSDAttributeDeclaration) itr.next();

            if (nameMatches(att, qName)) {
                return att;
            }
        }

        return null;
    }

    /**
     * Returns a flat list of imports from the specified schema.
     * <p>
     * The method recurses into imported schemas. The list returned is filtered so that
     * duplicate includes are removed. Two includes are considered equal if they have the same
     * target namespace.
     * </p>
     *
     * @param schema The top-level schema.
     *
     * @return A list containing objects of type {@link XSDImport}.
     */
    public static final List getImports(XSDSchema schema) {
        LinkedList queue = new LinkedList();
        ArrayList imports = new ArrayList();
        HashSet added = new HashSet();

        queue.addLast(schema);

        while (!queue.isEmpty()) {
            schema = (XSDSchema) queue.removeFirst();

            List contents = schema.getContents();

            for (Iterator itr = contents.iterator(); itr.hasNext();) {
                XSDSchemaContent content = (XSDSchemaContent) itr.next();

                if (content instanceof XSDImport) {
                    XSDImport imprt = (XSDImport) content;

                    if (!added.contains(imprt.getNamespace())) {
                        imports.add(imprt);
                        added.add(imprt.getNamespace());

                        queue.addLast(imprt.getResolvedSchema());
                    }
                }
            }
        }

        return imports;
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

        while (!queue.isEmpty()) {
            schema = (XSDSchema) queue.removeFirst();
            
            List contents = schema.getContents();

            for (Iterator itr = contents.iterator(); itr.hasNext();) {
                XSDSchemaContent content = (XSDSchemaContent) itr.next();

                if (content instanceof XSDInclude) {
                    XSDInclude include = (XSDInclude) content;

                    if (!added.contains(include.getSchemaLocation())) {
                        includes.add(include);
                        added.add(include.getSchemaLocation());

                        if ( include.getIncorporatedSchema() != null ) {
                        	queue.addLast(include.getIncorporatedSchema());	
                        }
                        else {
                        	LOGGER.fine( "include: " + include + " resulted in null schema");
                        }
                        
                    }
                }
            }
        }

        return includes;
    }

    /**
     * Searches <code>schema</code> for an element which matches <code>name</code>.
     * 
     * @param schema The schema
     * @param name The element to search for
     * 
     * @return The element declaration, or null if it could not be found.
     */
    public static XSDElementDeclaration getElementDeclaration( XSDSchema schema, QName name ) {
    	for ( Iterator e = schema.getElementDeclarations().iterator(); e.hasNext(); ) {
    		XSDElementDeclaration element = (XSDElementDeclaration) e.next();
    		if ( element.getTargetNamespace().equals( name.getNamespaceURI() ) ) {
    			if ( element.getName().equals( name.getLocalPart() ) )
    				return element;
    		}
    	}
    	
    	return null;
    }
    
    /**
     * Method for comparing the name of a schema component to a qualified name.
     * The component name and the qualified name match if both the namespaces
     * match, and the local parts match. Prefixes are ignored. Two strings will
     * match if one of the following conditions hold.
     *
     * <ul>
     *  <li>Both strings are null.
     *  <li>Both strings are the empty string.
     *  <li>One string is null, and the other is the empty string.
     *  <li>Both strings are non-null and non-empty and equals() return true.
     * </ul>
     *
     * @param component The component in question.
     * @param qName The qualifined name.
     *
     */
    public static final boolean nameMatches(XSDNamedComponent component,
        QName qName) {
    	String ns1 = component.getTargetNamespace();
    	String ns2 = qName.getNamespaceURI();
    	String n1 = component.getName();
    	String n2 = qName.getLocalPart();
    	
    	ns1 = "".equals( ns1 ) ? null : ns1;
    	ns2 = "".equals( ns2 ) ? null : ns2;
    	n1 = "".equals( n1 ) ? null : n1;
    	n2 = "".equals( n2 ) ? null : n2;
    	
    	if ( ns1 == null && ns2 != null ) {
    		//try the default namespace
    		if ( component.getSchema() != null ) {
    			ns1 = component.getSchema().getTargetNamespace();
    			if ( "".equals( ns1 ) ) {
    				ns1 = null;
    			}
    		}
    	}
    	
    	return Utilities.equals( ns1 , ns2 ) && Utilities.equals( n1 , n2 );
//    	
//        //is this the element we are looking for
//        if ((component.getTargetNamespace() == null)
//                || "".equals(component.getTargetNamespace())) {
//            if ((qName.getNamespaceURI() == null)
//                    || "".equals(qName.getNamespaceURI())) {
//                //do a local name match
//            	String n1 = component.getName();
//            	if ( "".equals( n1 ) ) {
//            		n1 = null;
//            	}
//            	String n2 = qName.getLocalPart();
//            	if ( "".equals( n2 ) ) {
//            		n2 = null;
//            	}
//                return (n1 == null && n2 == null) || n1.equals( n2 );
//            }
//
//            //assume default namespace
//            if (component.getSchema().getTargetNamespace()
//                             .equals(qName.getNamespaceURI())
//                    && component.getName().equals(qName.getLocalPart())) {
//                return true;
//            }
//        } else if (component.getTargetNamespace().equals(qName.getNamespaceURI())
//                && component.getName().equals(qName.getLocalPart())) {
//            return true;
//        }

        
    }

    /**
     * Returns the namespace prefix mapped to the targetNamespace of the schema.
     *
     * @param schema The schema in question.
     *
     * @return The namesapce prefix, or <code>null</code> if not found.
     */
    public static String getTargetPrefix(XSDSchema schema) {
        String ns = schema.getTargetNamespace();
        Map pre2ns = schema.getQNamePrefixToNamespaceMap();

        for (Iterator itr = pre2ns.entrySet().iterator(); itr.hasNext();) {
            Map.Entry entry = (Map.Entry) itr.next();
            if ( entry.getKey() == null ) 
            	continue;	//default prefix
            
            if (entry.getValue().equals(ns)) {
                return (String) entry.getKey();
            }
        }

        return null;
    }

    /**
     * Obtains all instances of a particular class from a container by navigating 
     * up the container hierachy.
     * 
     * @param container The container.
     * @param clazz The class.
     * 
     * @return A list of all instances of <code>clazz</code>, or the empty list if none found.
     */
    public static List getComponentInstancesOfType( PicoContainer container, Class clazz ) {
    	List instances = new ArrayList();
    	while( container != null ) {
    		List l = container.getComponentInstancesOfType( clazz );
    		instances.addAll( l );
    		container = container.getParent();
    	}
    	
    	return instances;
    }
    
    /**
     * Unregisters a component in the container and all parent containers.
     * 
     * @param container The container.
     * @param key The key of the component.
     * 
     */
    public static void unregisterComponent( PicoContainer container, final Object key ) {
    	//go to the top of the hierachy
    	while( container.getParent() != null ) {
    		container = container.getParent();
    	}
    	
    	container.accept( 
			new PicoVisitor() {
				public Object traverse(Object node) {
					return null;
				}

				public void visitContainer(PicoContainer container) {
					if ( container instanceof MutablePicoContainer ) {
						((MutablePicoContainer)container).unregisterComponent( key );
					}
					
				}

				public void visitComponentAdapter(ComponentAdapter adapter) {
				}

				public void visitParameter(Parameter parameter) {
				}
				
			}
		);
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

    static class SchemaLocatorAdapterFactory extends AdapterFactoryImpl {
        SchemaLocatorAdapter adapter;

        public SchemaLocatorAdapterFactory(List/*<XSDSchemaLocator>*/ locators) {
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
    	
        List/*<XSDSchemaLocator>*/ locators;

        public SchemaLocatorAdapter(List/*<XSDSchemaLocator>*/ locators) {
            this.locators = locators;
        }

        public boolean isAdapterForType(Object type) {
            return type == XSDSchemaLocator.class;
        }

        public XSDSchema locateSchema(XSDSchema xsdSchema, String namespaceURI,
            String rawSchemaLocationURI, String resolvedSchemaLocationURI) {
            for (int i = 0; i < locators.size(); i++) {
            	XSDSchemaLocator locator = (XSDSchemaLocator) locators.get( i );
                XSDSchema schema = 
                	locator.locateSchema(xsdSchema, namespaceURI, rawSchemaLocationURI, resolvedSchemaLocationURI);

                if (schema != null) {
                    return schema;
                }
            }

            return null;
        }
    }

    static class SchemaLocationResolverAdapterFactory extends AdapterFactoryImpl {
        SchemaLocationResolverAdapter adapter;

        public SchemaLocationResolverAdapterFactory( List/*<XSDSchemaLocationResolver>*/ resolvers) {
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
        implements XSDSchemaLocationResolver {
    	
    	List/*<XSDSchemaLocationResolver>*/ resolvers;

        public SchemaLocationResolverAdapter( List/*<XSDSchemaLocationResolver>*/ resolvers ) {
            this.resolvers = resolvers;
        }

        public boolean isAdapterForType(Object type) {
            return type == XSDSchemaLocationResolver.class;
        }

        public String resolveSchemaLocation(
    		XSDSchema schema, String namespaceURI, String rawSchemaLocationURI
		) {
            for (int i = 0; i < resolvers.size(); i++) {
            	XSDSchemaLocationResolver resolver = (XSDSchemaLocationResolver) resolvers.get( i );
                String resolved = 
                	resolver.resolveSchemaLocation(schema, namespaceURI, rawSchemaLocationURI);

                if (resolved != null) {
                    return resolved;
                }
            }

            LOGGER.warning( "Could not resolve schema location: " + rawSchemaLocationURI + " to physical location." );	
            return null;
        }
    }
}
