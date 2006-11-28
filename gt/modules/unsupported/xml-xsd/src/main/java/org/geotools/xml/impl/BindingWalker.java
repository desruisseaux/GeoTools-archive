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

import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDFeature;
import org.eclipse.xsd.XSDSimpleTypeDefinition;
import org.eclipse.xsd.XSDTypeDefinition;
import org.geotools.xml.Binding;
import org.geotools.xs.bindings.XSAnyTypeBinding;
import org.picocontainer.MutablePicoContainer;
import java.util.ArrayList;
import java.util.Stack;
import java.util.logging.Logger;
import javax.xml.namespace.QName;


public class BindingWalker implements TypeWalker.Visitor {
    BindingLoader loader;
    MutablePicoContainer context;
    ArrayList bindings;

    public BindingWalker(BindingLoader factory, MutablePicoContainer context) {
        this.loader = factory;
        this.context = context;
    }

    public boolean visit(XSDTypeDefinition type) {
        //look up the associated binding object for this type
        Binding binding = null;

        if (type.getName() != null) {
            QName qName = new QName(type.getTargetNamespace(), type.getName());

            //load the binding into the current context
            binding = loader.loadBinding(qName, context);
        } else {
            //special case check, look for an anonymous complex type 
            // with simple content
            if (type instanceof XSDComplexTypeDefinition
                    && type.getBaseType() instanceof XSDSimpleTypeDefinition) {
                //we assign the default complex strategy instread of 
                // delegating to parent, because if we dont, any attributes
                // defined by the type will not be parsed because simple
                // types cannot have attributes.
                //TODO: put this somewhere else, perahps in teh factories
                // that create the strategy objects
                binding = new XSAnyTypeBinding();
            }
        }

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

    public void walk(XSDFeature component, Visitor visitor) {
        //public void walk(XSDTypeDefinition type, Visitor visitor) {
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

    //	public void walk(XSDTypeDefinition type, StrategyVisitor visitor) {
    //		ArrayList strategies = new ArrayList();
    //		
    //		while(type != null) {
    //			//look up the associated strategy object for this type
    //			Strategy strategy = null; 
    //			if (type.getName() != null) {
    //				QName qName = 
    //					new QName(type.getTargetNamespace(),type.getName());
    //				
    //				//first copy the strategy into the current context
    //				try {
    //					context.registerComponentImplementation(
    //						factory.getStrategy(qName)
    //					);
    //				}
    //				catch(DuplicateComponentKeyRegistrationException e) {
    //					//ok, just means that we have already registerd the class
    //				}
    //				strategy = (Strategy) context.getComponentInstanceOfType(factory.getStrategy(qName));
    //			}
    //			else {
    //				//special case check, look for an anonymous complex type 
    //				// with simple content
    //				if (type instanceof XSDComplexTypeDefinition && 
    //						type.getBaseType() instanceof XSDSimpleTypeDefinition) {
    //					//we assign the default complex strategy instread of 
    //					// delegating to parent, because if we dont, any attributes
    //					// defined by the type will not be parsed because simple
    //					// types cannot have attributes.
    //					//TODO: put this somewhere else, perahps in teh factories
    //					// that create the strategy objects
    //					strategy = new XSAnyTypeStrategy();
    //				}
    //			}
    //			
    //			if (strategy != null) {
    //				//add the strategy
    //				strategies.add(strategy);
    //				
    //				//check execution mode, if override break out
    //				if (strategy.getExecutionMode() == Strategy.OVERRIDE)
    //					break;
    //			}
    //			else {
    //				//two posibilities
    //				if (!strategies.isEmpty()) {
    //					//make the last strategy the new root of the hierarchy
    //					break;
    //				}
    //				//else continue on to try to find a strategy further up in 
    //				// type hierarchy	
    //			}
    //		
    //			//get the next base type, if it is equal to this type then we have
    //			// reached the root of the type hierarchy
    //			if (type.equals(type.getBaseType()))
    //				break;
    //			type = type.getBaseType();
    //		}
    //		
    //		//simulated call stack
    //		Stack stack = new Stack();
    //		
    //		//visit from bottom to top
    //		for (int i = 0; i < strategies.size(); i++) {
    //			Strategy strategy = (Strategy) strategies.get(i);
    //			
    //			if (strategy.getExecutionMode() == Strategy.AFTER) {
    //				//put on stack to execute after parent
    //				stack.push(strategy);
    //				continue;
    //			}
    //			
    //			//execute the strategy
    //			visitor.visit(strategy);
    //		}
    //		
    //		//unwind the call stack
    //		while(!stack.isEmpty()) {
    //			Strategy strategy = (Strategy)stack.pop();
    //			visitor.visit(strategy);
    //		}
    //	}
    public static interface Visitor {
        void visit(Binding binding);
    }
}
