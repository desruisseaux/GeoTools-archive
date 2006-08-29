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

import org.eclipse.xsd.XSDFacet;
import org.eclipse.xsd.XSDSimpleTypeDefinition;
import org.eclipse.xsd.XSDTypeDefinition;
import org.geotools.xml.Binding;
import org.geotools.xml.ComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.InstanceComponent;
import org.geotools.xml.Node;
import org.geotools.xml.SimpleBinding;
import org.geotools.xml.impl.BindingWalker.Visitor;
import org.geotools.xs.facets.Whitespace;
import org.picocontainer.MutablePicoContainer;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;


public class ParseExecutor implements Visitor {
    private InstanceComponent instance;
    private Node node;
    private Object value;
    private MutablePicoContainer context;

    public ParseExecutor(InstanceComponent instance, Node node,
        MutablePicoContainer context) {
        this.instance = instance;
        this.node = node;
        this.context = context;

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
        } catch (Throwable t) {
            String msg = "Parsing failed for " + instance.getName() + ": "
                + t.toString();
            throw new RuntimeException(msg, t);
        }
    }

    public Object getValue() {
        return value;
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
