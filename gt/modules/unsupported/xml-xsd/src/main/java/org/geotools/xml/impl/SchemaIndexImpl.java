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
package org.geotools.xml.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.collections.OrderedMap;
import org.apache.commons.collections.map.ListOrderedMap;
import org.eclipse.xsd.XSDAttributeDeclaration;
import org.eclipse.xsd.XSDAttributeGroupDefinition;
import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDImport;
import org.eclipse.xsd.XSDInclude;
import org.eclipse.xsd.XSDParticle;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDSimpleTypeDefinition;
import org.eclipse.xsd.XSDTypeDefinition;
import org.geotools.xml.SchemaIndex;
import org.geotools.xml.Schemas;


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
    
    /**
	 * Cache of elements to children
	 */ 
	HashMap/*<XSDElementDeclaration,OrderedMap>*/ element2children = new HashMap();
	/**
	 * Cache of elemnets to attributes
	 */
	HashMap/*<XSDElementDeclaratoin,List>*/ element2attributes = new HashMap();
	
    public SchemaIndexImpl(XSDSchema[] schemas) {
        this.schemas = schemas;
    }

    public XSDSchema[] getSchemas() {
        return schemas;
    }

    public XSDImport[] getImports() {
        Collection imports = find(XSDImport.class);

        return (XSDImport[]) imports.toArray(new XSDImport[imports.size()]);
    }

    public XSDInclude[] getIncludes() {
        Collection includes = find(XSDInclude.class);

        return (XSDInclude[]) includes.toArray(new XSDInclude[includes.size()]);
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

        return type;
    }

    protected OrderedMap children( XSDElementDeclaration parent ) {
    	OrderedMap children = (OrderedMap) element2children.get( parent );
    	if ( children == null ) {
    		children = new ListOrderedMap();
    		for ( Iterator i =  Schemas.getChildElementParticles( parent.getType(), true ).iterator(); i.hasNext(); ) {
    			XSDParticle particle = (XSDParticle) i.next();
    			XSDElementDeclaration child = (XSDElementDeclaration) particle.getContent();
    			if ( child.isElementDeclarationReference() ) {
    				child = child.getResolvedElementDeclaration();
    			}
    			
    			QName childName = null;
    			if ( child.getTargetNamespace() != null ) {
    				childName = new QName( child.getTargetNamespace(), child.getName() );
    			}
    			else if ( parent.getTargetNamespace() != null ) {
    				childName = new QName( parent.getTargetNamespace(), child.getName() );
    			}
    			else if ( parent.getType().getTargetNamespace() != null ) {
    				childName = new QName( parent.getType().getTargetNamespace(), child.getName() );
    			}
    			else {
    				childName = new QName( null, child.getName() );
    			}
    			
    			children.put( childName, particle );
    			
    		}
    		element2children.put( parent, children );
    	}
    	
    	return children;
    }
    
    public XSDElementDeclaration getChildElement( XSDElementDeclaration parent, QName childName ) {
    	OrderedMap children = (OrderedMap) children( parent );
    	XSDParticle particle = (XSDParticle) children.get( childName );
    	if ( particle != null ) {
    		XSDElementDeclaration child = (XSDElementDeclaration) particle.getContent();
    		if ( child.isElementDeclarationReference() ) {
    			child = child.getResolvedElementDeclaration();
    		}
    		
    		return child;
    	}
    	
    	return null;
    }
    
    public List getChildElementParticles( XSDElementDeclaration parent ) {
    	return new ArrayList( children( parent ).values() );
    }
    
    public List getAttributes( XSDElementDeclaration element ) {
    	List attributes = (List) element2attributes.get( element );
    	if ( attributes == null ) {
    		attributes = Schemas.getAttributeDeclarations( element );
    		element2attributes.put( element, attributes );
    	}
    	
    	return Collections.unmodifiableList( attributes );
    }
    
    protected Collection find(Class c) {
        ArrayList found = new ArrayList();

        for (int i = 0; i < schemas.length; i++) {
            XSDSchema schema = schemas[i];

            List content = schema.getContents();

            for (Iterator itr = content.iterator(); itr.hasNext();) {
                Object o = itr.next();

                if (c.isAssignableFrom(o.getClass())) {
                    found.add(o);
                }
            }
        }

        return found;
    }

    protected HashMap getElementIndex() {
        if (elementIndex == null) {
            buildElementIndex();
        }

        return elementIndex;
    }

    protected HashMap getAttributeIndex() {
        if (attributeIndex == null) {
            buildAttriubuteIndex();
        }

        return attributeIndex;
    }

    protected HashMap getAttributeGroupIndex() {
        if (attributeGroupIndex == null) {
            buildAttributeGroupIndex();
        }

        return attributeGroupIndex;
    }

    protected HashMap getComplexTypeIndex() {
        if (complexTypeIndex == null) {
            buildTypeIndex();
        }

        return complexTypeIndex;
    }

    protected HashMap getSimpleTypeIndex() {
        if (simpleTypeIndex == null) {
            buildTypeIndex();
        }

        return simpleTypeIndex;
    }

    protected void buildElementIndex() {
        elementIndex = new HashMap();

        for (int i = 0; i < schemas.length; i++) {
            XSDSchema schema = schemas[i];

            for (Iterator e = schema.getElementDeclarations().iterator();
                    e.hasNext();) {
                XSDElementDeclaration element = (XSDElementDeclaration) e.next();

                QName qName = new QName(element.getTargetNamespace(),
                        element.getName());
                elementIndex.put(qName, element);
            }
        }
    }

    protected void buildAttriubuteIndex() {
        attributeIndex = new HashMap();

        for (int i = 0; i < schemas.length; i++) {
            XSDSchema schema = schemas[i];

            for (Iterator a = schema.getAttributeDeclarations().iterator();
                    a.hasNext();) {
                XSDAttributeDeclaration attribute = (XSDAttributeDeclaration) a
                    .next();

                QName qName = new QName(attribute.getTargetNamespace(),
                        attribute.getName());
                attributeIndex.put(qName, attribute);
            }
        }
    }

    protected void buildAttributeGroupIndex() {
        attributeGroupIndex = new HashMap();

        for (int i = 0; i < schemas.length; i++) {
            XSDSchema schema = schemas[i];

            for (Iterator g = schema.getAttributeGroupDefinitions().iterator();
                    g.hasNext();) {
                XSDAttributeGroupDefinition group = (XSDAttributeGroupDefinition) g
                    .next();

                QName qName = new QName(group.getTargetNamespace(),
                        group.getName());
                attributeGroupIndex.put(qName, group);
            }
        }
    }

    protected void buildTypeIndex() {
        complexTypeIndex = new HashMap();
        simpleTypeIndex = new HashMap();

        for (int i = 0; i < schemas.length; i++) {
            XSDSchema schema = schemas[i];

            for (Iterator t = schema.getTypeDefinitions().iterator();
                    t.hasNext();) {
                XSDTypeDefinition type = (XSDTypeDefinition) t.next();
                HashMap index = null;

                if (type instanceof XSDComplexTypeDefinition) {
                    index = complexTypeIndex;
                } else {
                    index = simpleTypeIndex;
                }

                QName qName = new QName(type.getTargetNamespace(),
                        type.getName());
                index.put(qName, type);
            }
        }
    }
}
