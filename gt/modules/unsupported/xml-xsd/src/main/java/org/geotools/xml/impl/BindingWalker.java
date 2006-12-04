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
import java.util.Iterator;
import java.util.Stack;

import javax.xml.namespace.QName;

import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDFeature;
import org.eclipse.xsd.XSDParticle;
import org.eclipse.xsd.XSDSimpleTypeDefinition;
import org.eclipse.xsd.XSDTypeDefinition;
import org.geotools.xml.Binding;
import org.geotools.xml.Schemas;
import org.geotools.xs.bindings.XS;
import org.geotools.xs.bindings.XSAnyTypeBinding;
import org.picocontainer.MutablePicoContainer;


public class BindingWalker implements TypeWalker.Visitor {
    BindingLoader loader;
    MutablePicoContainer context;
    ArrayList bindings;

    XSDFeature component;
    XSDTypeDefinition container;
    
    public BindingWalker(BindingLoader factory, MutablePicoContainer context) {
        this.loader = factory;
        this.context = context;
    }

    public boolean visit(XSDTypeDefinition type) {
        //look up the associated binding object for this type
        
    	QName bindingName = null;
        if (type.getName() != null) {
            bindingName = new QName(type.getTargetNamespace(), type.getName());
        } 
        else {
        	//anonymous type, does it belong to a global element
        	for ( Iterator e = type.getSchema().getElementDeclarations().iterator(); e.hasNext(); ) {
        		XSDElementDeclaration element = (XSDElementDeclaration) e.next();
        		if ( type.equals( element.getAnonymousTypeDefinition() ) ) {
        			//TODO: this naming convention for anonymous types could conflict with 
        			// other types in the schema
        			bindingName = new QName( type.getTargetNamespace(), "_" + element.getName() );
        			break;
        		}
        	}
        	
        	if ( bindingName == null ) {
        		//do we have a containing type?
            	if ( container != null ) {
            		//get the anonymous element, and look it up in the container type
            		if ( type.getContainer() instanceof XSDElementDeclaration ) { 
            			XSDElementDeclaration anonymous = (XSDElementDeclaration) type.getContainer();
                		XSDParticle particle = 
                			Schemas.getChildElementParticle( container, anonymous.getName(), true );
                		if ( particle != null ) {
                			bindingName = new QName( 
            					container.getTargetNamespace(), container.getName() + "_" + anonymous.getName() 
        					);
                		}
                	}
            	}
        	}
        	
        	
        	if ( bindingName == null ) {
        		//special case check, look for an anonymous complex type 
                // with simple content
                if (type instanceof XSDComplexTypeDefinition
                        && type.getBaseType() instanceof XSDSimpleTypeDefinition) {
                    //we assign the default complex binding instread of 
                    // delegating to parent, because if we dont, any attributes
                    // defined by the type will not be parsed because simple
                    // types cannot have attributes.
                    //TODO: put this somewhere else, perahps in teh factories
                    // that create the bindings
                    bindingName = XS.ANYTYPE;
                }
        	}
        }

        //load the binding into the current context
        Binding binding = loader.loadBinding( bindingName, context);
        if (binding != null) {
            //add the strategy
            bindings.add(binding);

            //check execution mode, if override break out
            if (binding.getExecutionMode() == Binding.OVERRIDE) {
                return false;
            }
        } 
        //JD: changing to just continue on up
//        else {
//            //two posibilities
//            if (!bindings.isEmpty()) {
//                //make the last strategy the new root of the hierarchy
//                return false;
//            }
//
//            //else continue on to try to find a strategy further up in 
//            // type hierarchy	
//        }

        return true;
    }

    public void walk(XSDFeature component, Visitor visitor, XSDTypeDefinition container ) {

    	this.container = container;
    	this.component = component;
    	
    	bindings = new ArrayList();

        //first walk the type hierarchy to get the binding objects
        TypeWalker walker = new TypeWalker(component.getType());
        walker.walk(this);

        //also look up a binding to teh instance itself, if found it will go 
        // at the bottom of the binding hierarchy
        if (component.getName() != null) {
            QName qName = new QName(component.getTargetNamespace(),
                    component.getName());
            Binding binding = loader.loadBinding(qName, context);

            if (binding != null) {
                bindings.add(0, binding);
            }
        }

        //simulated call stack
        Stack stack = new Stack();

        //visit from bottom to top
        for (int i = 0; i < bindings.size(); i++) {
            Binding binding = (Binding) bindings.get(i);

            if (binding.getExecutionMode() == Binding.AFTER) {
                //put on stack to execute after parent
                stack.push(binding);

                continue;
            }

            //execute the strategy
            visitor.visit(binding);
        }

        //unwind the call stack
        while (!stack.isEmpty()) {
            Binding binding = (Binding) stack.pop();

            visitor.visit(binding);
        }
    }
    
    public void walk(XSDFeature component, Visitor visitor) {
    	walk( component, visitor, null );
    	
    }

    public static interface Visitor {
        void visit(Binding binding);
    }
}
