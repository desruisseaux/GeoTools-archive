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

import org.geotools.xml.Binding;
import org.geotools.xml.BindingFactory;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;
import org.picocontainer.defaults.DuplicateComponentKeyRegistrationException;
import javax.xml.namespace.QName;


public class BindingLoader {
    MutablePicoContainer container;

    public BindingLoader() {
        container = new DefaultPicoContainer();
    }

    /**
     * Loads a binding with a specifc QName into a context.
     *
     * @param qName The qualified name of the type of the binding object.
     * @param context The context which is to contain the binding.
     *
     * @return The binding object of the associated type, otherwise null if
     * no such binding could be created.
     *
     */
    public Binding loadBinding(QName qName, MutablePicoContainer context) {
        Class bindingClass = getBinding(qName);

        if (bindingClass == null) {
            return null;
        }

        try {
            context.registerComponentImplementation(bindingClass);
        } catch (DuplicateComponentKeyRegistrationException e) {
            //ok, just means that we have already registerd the class
        }

        return (Binding) context.getComponentInstanceOfType(bindingClass);
    }

    /**
     * Returns the class of the binding  used to parse the type with the
     * specified qualified name.
     *
     * @param type The qualified name of the type of the binding.
     *
     * @return The binding class, or null if no such class exists.
     */
    public Class getBinding(QName type) {
        ComponentAdapter adapter = container.getComponentAdapter(type);

        if (adapter == null) {
            return null;
        }

        return adapter.getComponentImplementation();
    }

    /**
     * @return The container which houses the bindings.
     */
    public MutablePicoContainer getContainer() {
        return container;
    }
    
    /**
     * Sets the container which houses bindings.
     * 
     */
    public void setContainer(MutablePicoContainer container) {
		this.container = container;
	}
}
