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

import org.eclipse.xsd.XSDAttributeDeclaration;
import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDEnumerationFacet;
import org.eclipse.xsd.XSDFacet;
import org.eclipse.xsd.XSDFactory;
import org.eclipse.xsd.XSDFeature;
import org.eclipse.xsd.XSDLengthFacet;
import org.eclipse.xsd.XSDMaxLengthFacet;
import org.eclipse.xsd.XSDMinLengthFacet;
import org.eclipse.xsd.XSDSchemaContent;
import org.eclipse.xsd.XSDSimpleTypeDefinition;
import org.eclipse.xsd.XSDTypeDefinition;
import org.eclipse.xsd.XSDVariety;
import org.geotools.xml.AttributeInstance;
import org.geotools.xml.Binding;
import org.geotools.xml.ComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.InstanceComponent;
import org.geotools.xml.Node;
import org.geotools.xml.SimpleBinding;
import org.geotools.xml.impl.BindingWalker.Visitor;
import org.geotools.xs.facets.Whitespace;
import org.picocontainer.MutablePicoContainer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;


public class ParseExecutor implements Visitor {
    private InstanceComponent instance;
    private Node node;
    private Object value;
    private MutablePicoContainer context;
    private ParserHandler parser;

    public ParseExecutor(InstanceComponent instance, Node node,
        MutablePicoContainer context, ParserHandler parser ) {
        this.instance = instance;
        this.node = node;
        this.context = context;
        this.parser = parser;

        value = parseFacets(instance);
    }

    public void visit(Binding binding) {
        //reload out of context
        binding = (Binding) context.getComponentInstanceOfType(binding.getClass());

        //execute the binding
        try {
            Object result = value;

            if (binding instanceof SimpleBinding) {
                result = ((SimpleBinding) binding).parse(instance, result);
            } else {
                result = ((ComplexBinding) binding).parse((ElementInstance) instance,
                        node, result);
            }

            //only pass the value along if it was non-null
            if (result != null) {
                value = result;
            }
        } 
        catch (Throwable t) {
            String msg = "Parsing failed for " + instance.getName() + ": "
                + t.toString();
            throw new RuntimeException(msg, t);
        }
    }

    public Object getValue() {
        return value;
    }

    /**
     * Pre-parses the instance compontent checking the following:
     * <p>
     * 
     * </p>
     * @param instance
     */
    protected Object preParse( InstanceComponent instance ) {
    	// we only preparse text, so simple types
    	XSDSimpleTypeDefinition type = null;
    	if ( instance.getTypeDefinition() instanceof XSDSimpleTypeDefinition ) {
    		type = (XSDSimpleTypeDefinition) instance.getTypeDefinition();
    	}
    	else {
    		XSDComplexTypeDefinition complexType = 
    			(XSDComplexTypeDefinition) instance.getTypeDefinition();
    		if ( complexType.getContentType() instanceof XSDSimpleTypeDefinition ) {
    			type = (XSDSimpleTypeDefinition) complexType.getContentType();
    		}
    	}
    	
    	String text = instance.getText();
    	if ( type != null ) {
    		
    		//alright, lets preparse some text
    		//first base on variety
    		if ( type.getVariety() == XSDVariety.LIST_LITERAL ) {
    			
    			//list, whiteSpace is fixed to "COLLAPSE 
    			text = Whitespace.COLLAPSE.preparse( text );
    			
    			//lists are seperated by spaces
    			String[] list = text.split( " " );
    				
    			//apply the facets
    			// 1. length
    			// 2. maxLength
    			// 3. minLength
    			// 4. enumeration
    			if ( type.getLengthFacet() != null ) {
    				XSDLengthFacet length = type.getLengthFacet();
    				if ( list.length != length.getValue() ) {
    					//validation exception
    				}
    			}
    			if ( type.getMaxLengthFacet() != null ) {
    				XSDMaxLengthFacet length = type.getMaxLengthFacet();
    				if ( list.length > length.getValue() ) {
    					//validation exception
    				}
    			}
    			if ( type.getMinLengthFacet() != null ) {
    				XSDMinLengthFacet length = type.getMinLengthFacet();
    				if ( list.length < length.getValue() ) {
    					//validation exception
    				}
    			}
    			if ( !type.getEnumerationFacets().isEmpty() ) {
    				//gather up all teh possible values
    				Set values = new HashSet();
    				for ( Iterator e = type.getEnumerationFacets().iterator(); e.hasNext(); ) {
    					XSDEnumerationFacet enumeration = (XSDEnumerationFacet) e.next();
    					for ( Iterator v = enumeration.getValue().iterator(); v.hasNext(); ) {
    						values.add( v.next() );
    					}
    				}
    				
    				for ( int i = 0 ; i < list.length; i++ ) {
    					if ( !values.contains( list[ i ] ) ) {
    						//validation exception
    					}
    				}
    			}
    			
    			//now we must parse the items up
    			final XSDSimpleTypeDefinition itemType = type.getItemTypeDefinition();
    			List parsed = new ArrayList();
    			for ( int i = 0; i < list.length; i++ ) {
    				//create a pseudo declaration
    				XSDFeature feature = null;
    				if ( instance instanceof AttributeInstance ) {
    					XSDAttributeDeclaration attribute = 
    						XSDFactory.eINSTANCE.createXSDAttributeDeclaration();
    					attribute.setTypeDefinition( itemType );
    				}
    				else {
    					XSDElementDeclaration element = 
    						XSDFactory.eINSTANCE.createXSDElementDeclaration();
    					element.setTypeDefinition( itemType );
    				}
    				
    				if ( instance.getName() != null ) {
    					feature.setName( instance.getName() );
    				}
    				if ( instance.getNamespace() != null ) {
    					feature.setTargetNamespace( instance.getNamespace() );
    				}
    				
    				//create a new instance of the specified type
    				final XSDFeature declaration = feature;
    				InstanceComponentImpl theInstance = new InstanceComponentImpl() {

						public XSDTypeDefinition getTypeDefinition() {
							return itemType;
						}
						
						public XSDSchemaContent getDeclaration() {
							return (XSDSchemaContent) declaration;
						};
						
					};
    				theInstance.setText( list[ i ] );
    				
    				//perform the parse
    				ParseExecutor executor = new ParseExecutor( theInstance, null, context, parser );
    				BindingWalker walker = new BindingWalker( parser.getBindingLoader(), context );
    				walker.walk( feature, executor );
    				
    				parsed.add( executor.getValue() );
    			}
    			
    			return parsed;
    		}
    		else if ( type.getVariety() == XSDVariety.UNION_LITERAL ) {
    			//union, "valueSpace" and "lexicalSpace" facets are the union of the contained
    			// datatypes
    			return null;
    		}
    		else {
    			
    			//atomic
    			List facets = new ArrayList();
    			for ( Iterator f = type.getFacets().iterator(); f.hasNext(); ) {
    				XSDFacet facet = (XSDFacet) f.next();
    				facets.add( facet );
    			}
    			
    			return null;
    		}
    		
    	}
    	
    	return null;
    }
    
    protected Object parseFacets(InstanceComponent instance) {
        XSDTypeDefinition type = instance.getTypeDefinition();

        String value = instance.getText();

        while (type != null) {
            if (type instanceof XSDSimpleTypeDefinition) {
                XSDSimpleTypeDefinition simpleType = (XSDSimpleTypeDefinition) type;
                List facets = simpleType.getFacets();

                for (Iterator itr = facets.iterator(); itr.hasNext();) {
                    XSDFacet facet = (XSDFacet) itr.next();

                    if ("whiteSpace".equals(facet.getFacetName())) {
                        Whitespace whitespace = Whitespace.valueOf(facet
                                .getLexicalValue());

                        if (whitespace != null) {
                            value = whitespace.preparse(value);
                        }

                        //else TODO: check for validation, throw exception? 
                    }

                    //TODO: other facets
                }
            }

            if (type.equals(type.getBaseType())) {
                break;
            }

            type = type.getBaseType();
        }

        return value;
    }
}
