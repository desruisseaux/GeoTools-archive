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

import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;
import org.picocontainer.defaults.DuplicateComponentKeyRegistrationException;
import javax.xml.namespace.QName;
import org.geotools.xml.Binding;


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
        ComponentAdapter adapter = container.getComponentAdapter(qName);

        if (adapter == null) {
            return null;
        }

        return (Binding) adapter.getComponentInstance(context);
    }

    /**
     * Loads a binding with a specifc class into a context.
     *
     * @param bindingClass The class of the binding.
     * @param context The context which is to contain the binding.
     *
     * @return The binding object of the associated type, otherwise null if
     * no such binding could be created.
     *
     */
    public Binding loadBinding(Class bindingClass, MutablePicoContainer context) {
        ComponentAdapter adapter = container.getComponentAdapterOfType(bindingClass);

        if (adapter == null) {
            return null;
        }

        return (Binding) adapter.getComponentInstance(context);
    }

    /**
     * Returns the component adapter for a binding with the specified name.
     *
     * @param type The qualified name of the type of the binding.
     *
     * @return The binding class, or null if no such class exists.
     */
    protected ComponentAdapter getBinding(QName type) {
        return container.getComponentAdapter(type);
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
